package com.mozhimen.taskk.task.provider.download.okdownload

import android.content.Context
import com.liulishuo.okdownload.DownloadTask
import com.liulishuo.okdownload.OkDownload
import com.liulishuo.okdownload.StatusUtil
import com.liulishuo.okdownload.core.breakpoint.IBreakpointCompare
import com.liulishuo.okdownload.core.cause.EndCause
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
import com.mozhimen.taskk.provider.download.impls.DownloadException
import com.mozhimen.taskk.provider.download.impls.intErrorCode2downloadException
import com.mozhimen.taskk.task.provider.commons.ITaskProviderLifecycle
import com.mozhimen.taskk.task.provider.commons.providers.ITaskProviderDownload
import com.mozhimen.taskk.task.provider.cons.CErrorCode
import com.mozhimen.taskk.task.provider.cons.CTaskState
import com.mozhimen.taskk.task.provider.db.AppTask
import com.mozhimen.taskk.task.provider.db.AppTaskDaoManager
import com.mozhimen.taskk.task.provider.download.okdownload.mos.DownloadProgressBundle
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
    override var _downloadDir: File? = UtilKFileDir.External.getFilesDownloads()

    ///////////////////////////////////////////////////////////////////////////////////////

    fun init(context: Context) {
        try {
            UtilKLogWrapper.d(TAG, "init: resume task num ${getDownloadProgressBundlesCount()}")

            val builder = OkDownload.Builder(context)
                .processFileStrategy(ExtProcessFileStrategy())
                .breakpointCompare(_breakpointCompare)
                .connectionFactory(
                    DownloadOkHttp3Connection.Factory().setBuilder(
                        OkHttpClient.Builder()
                            .sslSocketFactory(UtilKSSLSocketFactory.get_ofTLS(), BaseX509TrustManager())
                            .hostnameVerifier { _, _ -> true }
                    )
                )
            OkDownload.setSingletonInstance(builder.build())
            DownloadDispatcher.setMaxParallelRunningCount(PARALLEL_RUNNING_COUNT)

            AppTaskDaoManager.gets_ofIsTaskDownloading().forEach {
                getDownloadTask(it)?.let { downloadTask ->
                    setDownloadProgressBundle(downloadTask.id, it)
                }
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
        _downloadProgressBundles.delete(downloadTask.id)

        /**
         * [CNetKAppState.STATE_DOWNLOAD_CANCEL]
         */
        onTaskCanceled(CTaskState.STATE_DOWNLOAD_CANCEL, appTask.apply {
            taskDownloadReset()
        })
    }

    fun taskPauseAll() {
        _downloadProgressBundles.forEach { _, value ->
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

    override fun taskResume(appTask: AppTask) {
        val downloadTask = getDownloadTask(appTask) ?: run {
            UtilKLogWrapper.d(TAG, "downloadResume: get download task fail")
            return
        }
        if (StatusUtil.getStatus(downloadTask) != StatusUtil.Status.RUNNING) {
            downloadTask.enqueue(this)
        }

        /**
         * [CNetKAppState.STATE_DOWNLOADING]
         */
        onTaskStarted(CTaskState.STATE_DOWNLOADING, appTask.apply {
            taskDownloadFileSpeed = BLOCK_SIZE_MIN
        })
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    override fun onTaskStarted(taskState: Int, appTask: AppTask) {
        super.onTaskStarted(taskState, appTask)
        _iTaskProviderLifecycle.onTaskStarted(taskState, appTask)
    }

    override fun onTaskPaused(taskState: Int, appTask: AppTask) {
        super.onTaskPaused(taskState, appTask)
        _iTaskProviderLifecycle.onTaskPaused(taskState, appTask)
    }

    override fun onTaskCanceled(taskState: Int, appTask: AppTask) {
        super.onTaskCanceled(taskState, appTask)
        deleteDownloadProgressBundle(appTask.taskDownloadId)
        _iTaskProviderLifecycle.onTaskCanceled(taskState, appTask)
    }

    override fun onTaskSucceeded(taskState: Int, appTask: AppTask) {
        super.onTaskSucceeded(taskState, appTask)
        deleteDownloadProgressBundle(appTask.taskDownloadId)
        _iTaskProviderLifecycle.onTaskSucceeded(taskState, appTask)
    }

    override fun onTaskFailed(taskState: Int, appTask: AppTask) {
        super.onTaskFailed(taskState, appTask)
        deleteDownloadProgressBundle(appTask.taskDownloadId)
        _iTaskProviderLifecycle.onTaskFailed(taskState, appTask)
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    //region # get
    fun getDownloadProgressBundlesCount(): Int {
        return _downloadProgressBundles.size
    }

    fun getDownloadProgressBundle(downloadId: Int, appTask: AppTask): DownloadProgressBundle {
        var bundle = _downloadProgressBundles[downloadId]
        if (bundle == null) {
            bundle = setDownloadProgressBundle(downloadId, appTask)
        }
        return bundle
    }

    fun getDownloadProgressBundle(appTask: AppTask): DownloadProgressBundle? {
        val downloadTask = getDownloadTask(appTask) ?: run {
            UtilKLogWrapper.d(TAG, "downloadPause: get download task fail")
            return null
        }
        return getDownloadProgressBundle(downloadTask.id, appTask)
    }

    fun setDownloadProgressBundle(downloadId: Int, appTask: AppTask): DownloadProgressBundle {
        val bundle = DownloadProgressBundle(appTask.apply { taskDownloadId = downloadId })
        _downloadProgressBundles[downloadId] = bundle
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
        return DownloadTask.Builder(appTask.taskDownloadUrlCurrent, dir.absolutePath, appTask.fileNameExt, _breakpointCompare).build()
    }
    //endregion

    ///////////////////////////////////////////////////////////////////////////////////////

    @Throws(DownloadException::class)
    fun download(appTask: AppTask) {
        val dir = _downloadDir
            ?: throw CErrorCode.CODE_DOWNLOAD_PATH_NOT_EXIST.intErrorCode2downloadException()
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

        //先根据Id去查找当前队列中有没有相同的任务，
        //如果有相同的任务，则不进行提交
        val bundle = getDownloadProgressBundle(downloadTask.id, appTask)
        if (bundle == null) {
            setDownloadProgressBundle(downloadTask.id, appTask)
        }
        downloadTask.enqueue(this)
        val currentIndex = (appTask.taskDownloadProgress.toFloat() / 100f) * appTask.taskDownloadFileSizeTotal.toFloat()
        /**
         * [CNetKAppState.STATE_DOWNLOADING]
         */
        onTaskStarted(CTaskState.STATE_DOWNLOADING, appTask.apply {
            appTask.taskDownloadFileSizeOffset = currentIndex.toLong()
            appTask.taskDownloadFileSpeed = 0
        })
    }

    fun downloadResumeAllDelay(delayMills: Long) {
        _downloadProgressBundles.forEach { _, value ->
            UtilKLogWrapper.d(TAG, "downloadResumeAll: appTask ${value.appTask}")
            if (value.appTask.isTaskPause()) {
                taskResume(value.appTask)
                UtilKLogWrapper.d(TAG, "downloadResumeAll: 恢复下载 appTask ${value.appTask}")
            }
        }
    }

    fun downloadRetry(appTask: AppTask) {
        val downloadTask = getDownloadTask(appTask) ?: run {
            UtilKLogWrapper.d(TAG, "downloadRetry: get download task fail")
            return
        }
        downloadTask.cancel()//然后取消任务
    }

    /**
     * 删除任务
     */
    fun downloadCancel(appTask: AppTask/*, onDeleteBlock: IAB_Listener<Boolean, Int>?*/) {
        val downloadTask = getDownloadTask(appTask) ?: run {
            UtilKLogWrapper.d(TAG, "downloadCancel: get download task fail")
            return
        }
        downloadTask.cancel()
        _downloadProgressBundles.delete(downloadTask.id)//先从队列中移除
        OkDownload.with().breakpointStore().remove(downloadTask.id)
        downloadTask.file?.delete()

        /**
         * [CNetKAppState.STATE_DOWNLOAD_CANCEL]
         */
        onTaskCanceled(CTaskState.STATE_DOWNLOAD_CANCEL,appTask)
        NetKApp.instance.onDownloadCancel(appTask)
    }

    fun downloadRetryWithClear(appTask: AppTask) {
        val downloadTask = getDownloadTask(appTask) ?: run {
            UtilKLogWrapper.d(TAG, "downloadRetryWithClear: get download task fail")
            return
        }
        downloadTask.cancel()
        OkDownload.with().breakpointStore().remove(downloadTask.id)
        downloadTask.file?.delete()
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    override fun taskStart(downloadTask: DownloadTask, model: Listener1Assist.Listener1Model) {
        UtilKLogWrapper.d(TAG, "taskStart: task $downloadTask")
        var mAppDownloadProgress = getDownloadProgressBundle(downloadTask.id)
        if (mAppDownloadProgress == null) {
            val appTask = AppTaskDaoManager.get_ofTaskDownloadUrlCurrent(downloadTask.url) ?: return
            setDownloadProgressBundle(downloadTask.id, appTask)
            mAppDownloadProgress = getDownloadProgressBundle(downloadTask.id)
        }
        mAppDownloadProgress?.let { appTask ->
            val currentIndex = (appTask.appTask.downloadProgress.toFloat() / 100f) * appTask.appTask.apkFileSize.toFloat()
            /**
             * [CNetKAppState.STATE_DOWNLOADING]
             */
            NetKApp.instance.onDownloading(appTask.appTask, appTask.appTask.downloadProgress.constraint(1, 100), currentIndex.toLong(), appTask.appTask.apkFileSize, 0)
//            }
        }
    }

    override fun retry(downloadTask: DownloadTask, cause: ResumeFailedCause) {
        UtilKLogWrapper.d(TAG, "retry: task $downloadTask")
    }

    override fun connected(downloadTask: DownloadTask, blockCount: Int, currentOffset: Long, totalLength: Long) {
        UtilKLogWrapper.d(TAG, "connected: task $downloadTask")
    }

    override fun progress(downloadTask: DownloadTask, currentOffset: Long, totalLength: Long) {
        var mAppDownloadProgress = getDownloadProgressBundle(downloadTask.id)
        if (mAppDownloadProgress == null) {
            val appTask = AppTaskDaoManager.get_ofTaskDownloadUrlCurrent(downloadTask.url) ?: return
            setDownloadProgressBundle(downloadTask.id, appTask)
            mAppDownloadProgress = getDownloadProgressBundle(downloadTask.id)
        }
        mAppDownloadProgress?.let { appTask ->
            val progress = ((currentOffset.toFloat() / totalLength.toFloat()) * 100f).toInt().constraint(1, 100)
            val offsetFileSizePerSeconds = abs(currentOffset - appTask.appTask.downloadFileSize)

            UtilKLogWrapper.d(TAG, "progress: $progress currentOffset $currentOffset  totalLength $totalLength")
            if (appTask.appTask.isTaskPause()) return
            if (progress < appTask.appTask.downloadProgress) return

            /**
             * [CNetKAppState.STATE_DOWNLOADING]
             */
            NetKApp.instance.onDownloading(
                appTask.appTask.apply {
                    downloadProgress = progress
                    downloadFileSize = currentOffset
                },
                progress, currentOffset, appTask.appTask.apkFileSize, offsetFileSizePerSeconds
            )
        }
    }

    override fun taskEnd(downloadTask: DownloadTask, cause: EndCause, realCause: Exception?, model: Listener1Assist.Listener1Model) {
        UtilKLogWrapper.d(TAG, "taskEnd: $downloadTask cause ${cause.name} realCause ${realCause.toString()}")
        var downloadProgressBundle = getDownloadProgressBundle(downloadTask.id)
        if (downloadProgressBundle == null) {
            val appTask = AppTaskDaoManager.get_ofTaskDownloadUrlCurrent(downloadTask.url) ?: return
            setDownloadProgressBundle(downloadTask.id, appTask)
            downloadProgressBundle = getDownloadProgressBundle(downloadTask.id)
        }
        downloadProgressBundle?.let { bundle ->
            when (cause) {
                EndCause.COMPLETED -> {
                    onDownloadSuccess(bundle.appTask)
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
                    NetKApp.instance.onDownloadCancel(bundle.appTask)//下载取消
                }

                else -> {
                    if (bundle.appTask.isTaskPause())
                        return

                    if (bundle.retryCount < RETRY_COUNT_MIN) {
                        try {
                            bundle.retryCount++
                            bundle.isRetry = true
                            downloadRetry(bundle.appTask)
                            download(bundle.appTask)
                            UtilKLogWrapper.d(TAG, "taskEnd: MIN通信问题重试 ${bundle.retryCount}次 appTask ${bundle.appTask}")
                        } catch (e: AppDownloadException) {
                            /**
                             * [CNetKAppState.STATE_DOWNLOAD_FAIL]
                             */
                            NetKApp.instance.onDownloadFail(bundle.appTask, e)
                            _downloadProgressBundles.delete(downloadTask.id)//从队列里移除掉
                        }
                        return
                    } else if (realCause is StreamResetException) {
                        try {
                            bundle.retryCount++
                            bundle.isRetry = true
                            downloadRetryWithClear(bundle.appTask)
                            download(bundle.appTask)
                            UtilKLogWrapper.d(TAG, "taskEnd: StreamResetException 重新开始下载")
                        } catch (e: AppDownloadException) {
                            /**
                             * [CNetKAppState.STATE_DOWNLOAD_FAIL]
                             */
                            NetKApp.instance.onDownloadFail(bundle.appTask, e)
                            _downloadProgressBundles.delete(downloadTask.id)//从队列里移除掉
                        }
                        return
                    }

                    /**
                     * [CNetKAppState.STATE_DOWNLOAD_FAIL]
                     */
                    NetKApp.instance.onDownloadFail(bundle.appTask, realCause)
                }
            }
        }
        _downloadProgressBundles.delete(downloadTask.id)//从队列里移除掉
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    private fun onDownloadSuccess(appTask: AppTask) {
        /**
         * [CNetKAppState.STATE_DOWNLOAD_SUCCESS]
         */
        NetKApp.instance.onDownloadSuccess(appTask)//下载完成，去安装

        NetKAppVerifyManager.verify(appTask)//下载完成，去安装
    }
}