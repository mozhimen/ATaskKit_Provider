package com.mozhimen.taskk.task.provider.download.okdownload

import android.content.Context
import com.liulishuo.okdownload.DownloadTask
import com.liulishuo.okdownload.OkDownload
import com.liulishuo.okdownload.StatusUtil
import com.liulishuo.okdownload.core.breakpoint.IBreakpointCompare
import com.liulishuo.okdownload.core.cause.EndCause
import com.liulishuo.okdownload.core.cause.ResumeFailedCause
import com.liulishuo.okdownload.core.connection.DownloadOkHttp3Connection
import com.liulishuo.okdownload.core.dispatcher.DownloadDispatcher
import com.liulishuo.okdownload.core.file.ExtProcessFileStrategy
import com.liulishuo.okdownload.core.listener.DownloadListener1
import com.liulishuo.okdownload.core.listener.assist.Listener1Assist
import com.mozhimen.basick.elemk.javax.net.bases.BaseX509TrustManager
import com.mozhimen.basick.utilk.android.util.UtilKLogWrapper
import com.mozhimen.basick.utilk.java.io.UtilKFileDir
import com.mozhimen.basick.utilk.javax.net.UtilKSSLSocketFactory
import com.mozhimen.basick.utilk.kotlin.ranges.constraint
import com.mozhimen.taskk.task.provider.impls.TaskException
import com.mozhimen.taskk.task.provider.impls.intErrorCode2taskException
import com.mozhimen.taskk.task.provider.commons.ITaskProviderLifecycle
import com.mozhimen.taskk.task.provider.commons.providers.ITaskProviderDownload
import com.mozhimen.taskk.task.provider.cons.CErrorCode
import com.mozhimen.taskk.task.provider.cons.CTaskState
import com.mozhimen.taskk.task.provider.cons.STaskFinishType
import com.mozhimen.taskk.task.provider.db.AppTask
import com.mozhimen.taskk.task.provider.db.AppTaskDaoManager
import com.mozhimen.taskk.provider.download.mos.DownloadProgressBundle
import okhttp3.OkHttpClient
import okhttp3.internal.http2.StreamResetException
import java.io.File
import java.lang.Exception
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs

/**
 * @ClassName TaskProviderDownloadOkDownload
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/20
 * @Version 1.0
 */
class TaskProviderDownloadOkDownload(private val _iTaskProviderLifecycle: ITaskProviderLifecycle) : ITaskProviderDownload, DownloadListener1() {

    companion object {
        private const val PARALLEL_RUNNING_COUNT = 3
        private const val RETRY_COUNT_MIN = 10
        private const val BLOCK_SIZE_MIN = 100L
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    private var _breakpointCompare: IBreakpointCompare? = null
    private val _downloadProgressBundles = ConcurrentHashMap<Int, DownloadProgressBundle>()
    private var _okHttpClientBuilder = OkHttpClient.Builder()
        .sslSocketFactory(UtilKSSLSocketFactory.get_ofTLS(), BaseX509TrustManager())
        .hostnameVerifier { _, _ -> true }

    override var _downloadDir: File? = UtilKFileDir.External.getFilesDownloads()

    ///////////////////////////////////////////////////////////////////////////////////////

    fun init(context: Context) {
        try {
            UtilKLogWrapper.d(TAG, "init: resume task num ${getDownloadProgressBundlesCount()}")

            val builder = OkDownload.Builder(context)
                .processFileStrategy(ExtProcessFileStrategy())
                .breakpointCompare(_breakpointCompare)
                .connectionFactory(DownloadOkHttp3Connection.Factory().setBuilder(_okHttpClientBuilder))
            OkDownload.setSingletonInstance(builder.build())
            DownloadDispatcher.setMaxParallelRunningCount(PARALLEL_RUNNING_COUNT)

            AppTaskDaoManager.gets_ofIsTaskDownloading().forEach {
                if (it.isTaskDownloading())
                    taskPause(it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setDownloadDir(downloadDir: File): TaskProviderDownloadOkDownload {
        _downloadDir = downloadDir
        return this
    }

    fun setBreakpointCompare(breakpointCompare: IBreakpointCompare): TaskProviderDownloadOkDownload {
        _breakpointCompare = breakpointCompare
        return this
    }

    fun setOkHttpClientBuilder(okHttpClientBuilder: OkHttpClient.Builder): TaskProviderDownloadOkDownload {
        _okHttpClientBuilder = okHttpClientBuilder
        return this
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    override fun getSupportFileExtensions(): List<String> {
        return listOf("apk")
    }

    override fun taskStart(appTask: AppTask) {

    }

    override fun taskCancel(appTask: AppTask) {
        //downloadWaitCancel
        val downloadTask = getDownloadTask(appTask) ?: run {
            UtilKLogWrapper.d(TAG, "downloadWaitCancel: get download task fail")
            return
        }
        downloadTask.cancel()//然后取消任务
        OkDownload.with().breakpointStore().remove(downloadTask.id)
        downloadTask.file?.delete()

        /**
         * [CNetKAppState.STATE_DOWNLOAD_CANCEL]
         */
        onTaskFinished(CTaskState.STATE_DOWNLOAD_CANCEL, downloadTask.id, STaskFinishType.CANCEL, appTask)
    }

    fun taskPauseAll() {
        for ((_, value) in _downloadProgressBundles) {
            taskPause(value.appTask)
            UtilKLogWrapper.d(TAG, "downloadPauseAll: appTask ${value.appTask}")
        }
    }

    override fun taskPause(appTask: AppTask) {
        val downloadTask = getDownloadTask(appTask) ?: run {
            UtilKLogWrapper.d(TAG, "downloadPause: get download task fail")
            return
        }
        downloadTask.cancel()//取消任务

        /**
         * [CNetKAppState.STATE_DOWNLOAD_PAUSE]
         */
        onTaskPaused(CTaskState.STATE_DOWNLOAD_PAUSE, appTask.apply {
            appTask.taskDownloadFileSpeed = 0
        })
    }

    fun taskResumeAll() {
        for ((_, value) in _downloadProgressBundles.entries) {
            UtilKLogWrapper.d(TAG, "downloadResumeAll: appTask ${value.appTask}")
            if (value.appTask.isTaskPause()) {
                taskResume(value.appTask)
                UtilKLogWrapper.d(TAG, "downloadResumeAll: 恢复下载 appTask ${value.appTask}")
            }
        }
    }

    override fun taskResume(appTask: AppTask) {
        val downloadTask = getDownloadTask(appTask) ?: run {
            UtilKLogWrapper.d(TAG, "downloadResume: get download task fail")
            return
        }
        if (StatusUtil.getStatus(downloadTask) != StatusUtil.Status.RUNNING)
            downloadTask.enqueue(this)

        /**
         * [CNetKAppState.STATE_DOWNLOADING]
         */
        onTaskStarted(CTaskState.STATE_DOWNLOADING, downloadTask.id, appTask.apply {
            taskDownloadFileSpeed = BLOCK_SIZE_MIN
        })
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    private fun onTaskStarted(taskState: Int, downloadId: Int, appTask: AppTask) {
        getDownloadProgressBundle(downloadId, appTask)
        onTaskStarted(taskState, appTask)
    }

    override fun onTaskStarted(taskState: Int, appTask: AppTask) {
        super.onTaskStarted(taskState, appTask)
        _iTaskProviderLifecycle.onTaskStarted(taskState, appTask)
    }

    override fun onTaskPaused(taskState: Int, appTask: AppTask) {
        super.onTaskPaused(taskState, appTask)
        _iTaskProviderLifecycle.onTaskPaused(taskState, appTask)
    }

    private fun onTaskFinished(taskState: Int, downloadId: Int, finishType: STaskFinishType, appTask: AppTask) {
        deleteDownloadProgressBundle(downloadId)
        onTaskFinished(taskState, finishType, appTask)
    }

    override fun onTaskFinished(taskState: Int, finishType: STaskFinishType, appTask: AppTask) {
        super.onTaskFinished(taskState, finishType, appTask)
        _iTaskProviderLifecycle.onTaskFinished(taskState, finishType, appTask)
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    //region # get
    fun getDownloadProgressBundlesCount(): Int {
        return _downloadProgressBundles.size
    }

    fun getDownloadProgressBundle(downloadId: Int, appTask: AppTask): DownloadProgressBundle {
        return setDownloadProgressBundle(downloadId, appTask)
    }

    fun getDownloadProgressBundle(appTask: AppTask): DownloadProgressBundle? {
        val downloadTask = getDownloadTask(appTask) ?: run {
            UtilKLogWrapper.d(TAG, "downloadPause: get download task fail")
            return null
        }
        return getDownloadProgressBundle(downloadTask.id, appTask)
    }

    fun setDownloadProgressBundle(downloadId: Int, appTask: AppTask): DownloadProgressBundle {
        var bundle = _downloadProgressBundles[downloadId]
        if (bundle == null) {
            bundle = DownloadProgressBundle(appTask.apply { taskDownloadId = downloadId })
            _downloadProgressBundles[downloadId] = bundle
        } else {
            appTask.taskDownloadId = downloadId
            bundle.appTask = appTask
        }
        return bundle
    }

    fun deleteDownloadProgressBundle(downloadId: Int) {
        if (downloadId != 0)
            _downloadProgressBundles.remove(downloadId)
    }

    fun getDownloadTask(appTask: AppTask): DownloadTask? {
        val dir = _downloadDir ?: run {
            UtilKLogWrapper.d(TAG, "getDownloadTask: get download dir fail")
            return null
        }
        val downloadTask = DownloadTask.Builder(appTask.taskDownloadUrlCurrent, dir.absolutePath, appTask.fileNameExt, _breakpointCompare).build()
        getDownloadProgressBundle(downloadTask.id, appTask)
        return downloadTask
    }
    //endregion

    ///////////////////////////////////////////////////////////////////////////////////////

    @Throws(TaskException::class)
    fun download(appTask: AppTask) {
        val dir = _downloadDir
            ?: throw CErrorCode.CODE_TASK_DOWNLOAD_PATH_NOT_EXIST.intErrorCode2taskException()
        val downloadTask = DownloadTask.Builder(appTask.taskDownloadUrlCurrent, dir, _breakpointCompare)//先构建一个Task 框架可以保证Id唯一
            .setConnectionCount(1)
            .setFilename(appTask.fileNameExt)
            .setMinIntervalMillisCallbackProcess(1000)// 下载进度回调的间隔时间（毫秒）
            .setPassIfAlreadyCompleted(!appTask.isTaskInstallSuccess())// 任务过去已完成是否要重新下载
            .build()
        when (StatusUtil.getStatus(downloadTask)) {
            StatusUtil.Status.PENDING -> {
                //等待中 不做处理
            }

            StatusUtil.Status.RUNNING -> {
                //下载中，不做处理
            }

            StatusUtil.Status.COMPLETED -> {
                UtilKLogWrapper.d(TAG, "download: StatusUtil.Status.COMPLETED")
//                onDownloadSuccess(appTask)
//                return
            }

            StatusUtil.Status.IDLE -> {

            }
            //StatusUtil.Status.UNKNOWN
            else -> {

            }
        }
        downloadTask.enqueue(this)
        /**
         * [CNetKAppState.STATE_DOWNLOADING]
         */
        onTaskStarted(CTaskState.STATE_DOWNLOADING, downloadTask.id, appTask.apply {
            taskDownloadFileSpeed = BLOCK_SIZE_MIN
        })
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    override fun taskStart(downloadTask: DownloadTask, model: Listener1Assist.Listener1Model) {
        UtilKLogWrapper.d(TAG, "taskStart: task $downloadTask")
        val appTask = AppTaskDaoManager.get_ofTaskDownloadUrlCurrent(downloadTask.url) ?: return
        val bundle = getDownloadProgressBundle(downloadTask.id, appTask)
        /**
         * [CNetKAppState.STATE_DOWNLOADING]
         */
        onTaskStarted(CTaskState.STATE_DOWNLOADING, downloadTask.id, bundle.appTask.apply {
            taskDownloadFileSpeed = BLOCK_SIZE_MIN
        })
    }

    override fun retry(downloadTask: DownloadTask, cause: ResumeFailedCause) {
        UtilKLogWrapper.d(TAG, "retry: task $downloadTask")
    }

    override fun connected(downloadTask: DownloadTask, blockCount: Int, currentOffset: Long, totalLength: Long) {
        UtilKLogWrapper.d(TAG, "connected: task $downloadTask")

        progressInner(downloadTask, currentOffset, totalLength)
    }

    override fun progress(downloadTask: DownloadTask, currentOffset: Long, totalLength: Long) {
        UtilKLogWrapper.d(TAG, "progress: task $downloadTask")

        progressInner(downloadTask, currentOffset, totalLength)
    }

    private fun progressInner(downloadTask: DownloadTask, currentOffset: Long, totalLength: Long) {
        val appTask = AppTaskDaoManager.get_ofTaskDownloadUrlCurrent(downloadTask.url) ?: return
        val bundle = getDownloadProgressBundle(downloadTask.id, appTask)
        val progress = ((currentOffset.toFloat() / totalLength.toFloat()) * 100f).toInt().constraint(1, 100)
        val offsetFileSizePerSeconds = abs(currentOffset - bundle.appTask.taskDownloadFileSizeTotal)

        UtilKLogWrapper.d(TAG, "progress: $progress currentOffset $currentOffset  totalLength $totalLength")
        if (bundle.appTask.isTaskPause()) return
        if (progress < bundle.appTask.taskDownloadProgress) return

        /**
         * [CNetKAppState.STATE_DOWNLOADING]
         */
        onTaskStarted(CTaskState.STATE_DOWNLOADING, downloadTask.id, bundle.appTask.apply {
            taskDownloadFileSpeed = offsetFileSizePerSeconds
            taskDownloadFileSizeOffset = currentOffset
            taskDownloadFileSizeTotal = totalLength
            taskDownloadProgress = progress
        })
    }

    override fun taskEnd(downloadTask: DownloadTask, cause: EndCause, realCause: Exception?, model: Listener1Assist.Listener1Model) {
        UtilKLogWrapper.d(TAG, "taskEnd: $downloadTask cause ${cause.name} realCause ${realCause.toString()}")
        val appTask = AppTaskDaoManager.get_ofTaskDownloadUrlCurrent(downloadTask.url) ?: return
        val bundle = getDownloadProgressBundle(downloadTask.id, appTask)
        when (cause) {
            EndCause.COMPLETED -> {
                onTaskFinished(CTaskState.STATE_DOWNLOAD_SUCCESS, downloadTask.id, STaskFinishType.SUCCESS, bundle.appTask)
            }

            EndCause.CANCELED -> {
                if (bundle.appTask.isTaskPause() || bundle.appTask.isTaskCancel())
                    return
                if (bundle.isRetry) {
                    bundle.isRetry = false
                    download(bundle.appTask)
                    return
                }
                /**
                 * [CNetKAppState.STATE_DOWNLOAD_CANCEL]
                 */
                taskCancel(bundle.appTask)
            }

            else -> {
                if (bundle.appTask.isTaskPause())
                    return

                if (bundle.retryCount < RETRY_COUNT_MIN) {
                    try {
                        bundle.retryCount++
                        bundle.isRetry = true
                        taskPause(bundle.appTask)
                        download(bundle.appTask)
                        UtilKLogWrapper.d(TAG, "taskEnd: MIN通信问题重试 ${bundle.retryCount}次 appTask ${bundle.appTask}")
                    } catch (e: TaskException) {
                        /**
                         * [CNetKAppState.STATE_DOWNLOAD_FAIL]
                         */
                        onTaskFinished(CTaskState.STATE_DOWNLOAD_FAIL, downloadTask.id, STaskFinishType.FAIL(e), bundle.appTask)
                    }
                    return
                } else if (realCause is StreamResetException) {
                    try {
                        bundle.retryCount++
                        bundle.isRetry = true
                        taskCancel(bundle.appTask)
                        download(bundle.appTask)
                        UtilKLogWrapper.d(TAG, "taskEnd: StreamResetException 重新开始下载")
                    } catch (e: TaskException) {
                        /**
                         * [CNetKAppState.STATE_DOWNLOAD_FAIL]
                         */
                        onTaskFinished(CTaskState.STATE_DOWNLOAD_FAIL, downloadTask.id, STaskFinishType.FAIL(e), bundle.appTask)
                    }
                    return
                }

                /**
                 * [CNetKAppState.STATE_DOWNLOAD_FAIL]
                 */
//                NetKApp.instance.onDownloadFail(bundle.appTask, realCause)
                onTaskFinished(CTaskState.STATE_DOWNLOAD_FAIL, downloadTask.id, STaskFinishType.FAIL(TaskException(CErrorCode.CODE_TASK_DOWNLOAD_FAIL)), bundle.appTask)
            }
        }
    }
}