package com.mozhimen.taskk.provider.download.okdownload

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
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
import com.mozhimen.kotlin.elemk.javax.net.bases.BaseX509TrustManager
import com.mozhimen.kotlin.lintk.optins.permission.OPermission_INTERNET
import com.mozhimen.kotlin.utilk.android.util.UtilKLogWrapper
import com.mozhimen.kotlin.utilk.java.io.UtilKFileDir
import com.mozhimen.kotlin.utilk.java.io.createFolder
import com.mozhimen.kotlin.utilk.javax.net.UtilKSSLSocketFactory
import com.mozhimen.kotlin.utilk.kotlin.ranges.constraint
import com.mozhimen.taskk.provider.basic.annors.ATaskName
import com.mozhimen.taskk.provider.basic.annors.ATaskNodeQueueName
import com.mozhimen.taskk.provider.basic.annors.ATaskState
import com.mozhimen.taskk.provider.basic.bases.ATaskManager
import com.mozhimen.taskk.provider.basic.commons.ITaskLifecycle
import com.mozhimen.taskk.provider.basic.impls.TaskException
import com.mozhimen.taskk.provider.basic.impls.intErrorCode2taskException
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskDownload
import com.mozhimen.taskk.provider.basic.cons.CErrorCode
import com.mozhimen.taskk.provider.basic.cons.STaskFinishType
import com.mozhimen.taskk.provider.basic.db.AppTask
import com.mozhimen.taskk.provider.basic.db.AppTaskDaoManager
import com.mozhimen.taskk.provider.download.mos.DownloadProgressBundle
import okhttp3.OkHttpClient
import okhttp3.internal.http2.StreamResetException
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import kotlin.Exception
import kotlin.math.abs

/**
 * @ClassName TaskProviderDownloadOkDownload
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/20
 * @Version 1.0
 */
@OPermission_INTERNET
abstract class TaskDownloadOkDownload(taskManager: ATaskManager,iTaskLifecycle: ITaskLifecycle) : ATaskDownload(taskManager,iTaskLifecycle) {

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
    override var _taskNodeQueueName_ofDownload: String? = ATaskName.TASK_DOWNLOAD

    ///////////////////////////////////////////////////////////////////////////////////////

    override fun init(context: Context) {
        if (!hasInit()) {
            super.init(context)
            try {
                UtilKLogWrapper.d(TAG, "init: resume task num ${getDownloadProgressBundlesCount()}")

                val builder = OkDownload.Builder(context)
                    .processFileStrategy(ExtProcessFileStrategy())
                    .breakpointCompare(_breakpointCompare)
                    .connectionFactory(DownloadOkHttp3Connection.Factory().setBuilder(_okHttpClientBuilder))
                OkDownload.setSingletonInstance(builder.build())
                DownloadDispatcher.setMaxParallelRunningCount(PARALLEL_RUNNING_COUNT)

                AppTaskDaoManager.gets_ofIsTaskDownloading().forEach {
                    if (it.isTaskDownloading() && _taskNodeQueueName_ofDownload != null)
                        taskPause(it, _taskNodeQueueName_ofDownload!!)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setDownloadDir(downloadDir: File): TaskDownloadOkDownload {
        _downloadDir = downloadDir
        return this
    }

    fun setBreakpointCompare(breakpointCompare: IBreakpointCompare): TaskDownloadOkDownload {
        _breakpointCompare = breakpointCompare
        return this
    }

    fun setTaskNodeQueueName_ofDownload(taskNodeQueueName_ofDownload: String): TaskDownloadOkDownload {
        _taskNodeQueueName_ofDownload = taskNodeQueueName_ofDownload
        return this
    }

    fun setOkHttpClientBuilder(okHttpClientBuilder: OkHttpClient.Builder): TaskDownloadOkDownload {
        _okHttpClientBuilder = okHttpClientBuilder
        return this
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    @SuppressLint("MissingSuperCall")
    override fun taskStart(appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String) {
        Log.d(TAG, "taskStart: download")
        download(appTask, taskNodeQueueName)
    }

    @SuppressLint("MissingSuperCall")
    override fun taskCancel(appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String) {
        //downloadWaitCancel
        val downloadTask: DownloadTask? = getDownloadTask(appTask)?.apply {
            cancel()//然后取消任务
            OkDownload.with().breakpointStore().remove(id)
            file?.delete()
        } ?: run {
            UtilKLogWrapper.d(TAG, "downloadWaitCancel: get download task fail")
            null
        }

        /**
         * [CNetKAppState.STATE_DOWNLOAD_CANCEL]
         */
        onTaskFinished(ATaskState.STATE_DOWNLOAD_CANCEL, downloadTask?.id ?: appTask.taskDownloadId, appTask, taskNodeQueueName, STaskFinishType.CANCEL)
    }

    override fun taskPauseAll(@ATaskNodeQueueName taskNodeQueueName: String) {
        for ((_, value) in _downloadProgressBundles) {
            taskPause(value.appTask, taskNodeQueueName)
            UtilKLogWrapper.d(TAG, "downloadPauseAll: appTask ${value.appTask}")
        }
    }

    override fun taskPause(appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String) {
        val downloadTask = getDownloadTask(appTask) ?: run {
            UtilKLogWrapper.d(TAG, "downloadPause: get download task fail")
            return
        }
        downloadTask.cancel()//取消任务

        /**
         * [CNetKAppState.STATE_DOWNLOAD_PAUSE]
         */
        super.taskPause(appTask.apply {
            appTask.taskDownloadFileSpeed = 0
        }, taskNodeQueueName)
    }

    override fun taskResumeAll(@ATaskNodeQueueName taskNodeQueueName: String) {
        for ((_, value) in _downloadProgressBundles.entries) {
            UtilKLogWrapper.d(TAG, "downloadResumeAll: appTask ${value.appTask}")
            if (value.appTask.isAnyTaskPause()) {
                taskResume(value.appTask, taskNodeQueueName)
                UtilKLogWrapper.d(TAG, "downloadResumeAll: 恢复下载 appTask ${value.appTask}")
            }
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun taskResume(appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String) {
        val downloadTask = getDownloadTask(appTask) ?: run {
            UtilKLogWrapper.d(TAG, "downloadResume: get download task fail")
            return
        }
        if (StatusUtil.getStatus(downloadTask) != StatusUtil.Status.RUNNING)
            downloadTask.enqueue(InnerDownloadListener1(taskNodeQueueName))

        /**
         * [CNetKAppState.STATE_DOWNLOADING]
         */
        onTaskStarted(ATaskState.STATE_DOWNLOADING, downloadTask.id, appTask.apply {
            taskDownloadFileSpeed = BLOCK_SIZE_MIN
        }, taskNodeQueueName)
    }


    ///////////////////////////////////////////////////////////////////////////////////////

    private fun onTaskStarted(taskState: Int, downloadId: Int, appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String) {
        getDownloadProgressBundle(downloadId, appTask)
        onTaskStarted(taskState, appTask, taskNodeQueueName)
    }

    private fun onTaskFinished(taskState: Int, downloadId: Int, appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String, finishType: STaskFinishType) {
        deleteDownloadProgressBundle(downloadId)
        onTaskFinished(taskState, appTask, taskNodeQueueName, finishType)
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
        if (downloadId != 0 && _downloadProgressBundles.containsKey(downloadId))
            _downloadProgressBundles.remove(downloadId)
    }

    fun getDownloadTask(appTask: AppTask): DownloadTask? {
        val dir = getAppTaskDownloadDir(appTask) ?: _downloadDir ?: run {
            UtilKLogWrapper.d(TAG, "getDownloadTask: get downloadDir fail")
            return null
        }
        val downloadTask = DownloadTask.Builder(appTask.taskDownloadUrlCurrent, dir.absolutePath, appTask.fileNameExt, _breakpointCompare).build()
        getDownloadProgressBundle(downloadTask.id, appTask)
        return downloadTask
    }
    //endregion

    ///////////////////////////////////////////////////////////////////////////////////////

    @Throws(TaskException::class)
    fun download(appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String) {
        val dir = getAppTaskDownloadDir(appTask) ?: _downloadDir
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
        downloadTask.enqueue(InnerDownloadListener1(taskNodeQueueName))

        /**
         * [CNetKAppState.STATE_DOWNLOADING]
         */
        onTaskStarted(ATaskState.STATE_DOWNLOADING, downloadTask.id, appTask.apply {
            taskDownloadFileSpeed = BLOCK_SIZE_MIN
        }, taskNodeQueueName)
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    private fun getAppTaskDownloadDir(appTask: AppTask): File? {
        if (appTask.filePathNameExt.isNotEmpty()) {
            val file = File(appTask.filePathNameExt)
            return file.parentFile?.apply {
                createFolder()
            }
        }
        return null
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    inner class InnerDownloadListener1(@ATaskNodeQueueName private val taskNodeQueueName: String) : DownloadListener1() {
        override fun taskStart(downloadTask: DownloadTask, model: Listener1Assist.Listener1Model) {
            UtilKLogWrapper.d(TAG, "taskStart: task $downloadTask")
            val appTask = AppTaskDaoManager.get_ofTaskDownloadUrlCurrent(downloadTask.url) ?: return
            val bundle = getDownloadProgressBundle(downloadTask.id, appTask)
            /**
             * [CNetKAppState.STATE_DOWNLOADING]
             */
            onTaskStarted(ATaskState.STATE_DOWNLOADING, downloadTask.id, bundle.appTask.apply {
                taskDownloadFileSpeed = BLOCK_SIZE_MIN
            }, taskNodeQueueName)
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
            val offsetFileSizePerSeconds = abs(currentOffset - bundle.appTask.taskDownloadFileSizeOffset)

            UtilKLogWrapper.d(TAG, "progress: $progress currentOffset $currentOffset  totalLength $totalLength")
            if (bundle.appTask.isAnyTaskPause()) return
            if (progress < bundle.appTask.taskDownloadProgress) return

            /**
             * [CNetKAppState.STATE_DOWNLOADING]
             */
            onTaskStarted(ATaskState.STATE_DOWNLOADING, downloadTask.id, bundle.appTask.apply {
                taskDownloadFileSpeed = offsetFileSizePerSeconds
                taskDownloadFileSizeOffset = currentOffset
                taskDownloadFileSizeTotal = totalLength
                taskDownloadProgress = progress
            }, taskNodeQueueName)
        }

        override fun taskEnd(downloadTask: DownloadTask, cause: EndCause, realCause: Exception?, model: Listener1Assist.Listener1Model) {
            UtilKLogWrapper.d(TAG, "taskEnd: $downloadTask cause ${cause.name} realCause ${realCause.toString()}")
            val appTask = AppTaskDaoManager.get_ofTaskDownloadUrlCurrent(downloadTask.url) ?: return
            val bundle = getDownloadProgressBundle(downloadTask.id, appTask)
            when (cause) {
                EndCause.COMPLETED -> {
                    try {
                        val filePathNameExtTemp = downloadTask.file?.absolutePath ?: throw CErrorCode.CODE_TASK_DOWNLOAD_PATH_NOT_EXIST.intErrorCode2taskException()
                        onTaskFinished(
                            ATaskState.STATE_DOWNLOAD_SUCCESS, downloadTask.id,
                            bundle.appTask.apply {
                                filePathNameExt = filePathNameExtTemp
                            },
                            taskNodeQueueName, STaskFinishType.SUCCESS,
                        )
                    } catch (e: TaskException) {
                        onTaskFinished(ATaskState.STATE_DOWNLOAD_FAIL, downloadTask.id, bundle.appTask, taskNodeQueueName, STaskFinishType.FAIL(e))
                    }
                }

                EndCause.CANCELED -> {
                    if (bundle.appTask.isAnyTaskPause() || bundle.appTask.isAnyTaskCancel())
                        return
                    if (bundle.isRetry) {
                        bundle.isRetry = false
                        download(bundle.appTask, taskNodeQueueName)
                        return
                    }
                    /**
                     * [CNetKAppState.STATE_DOWNLOAD_CANCEL]
                     */
                    taskCancel(bundle.appTask, taskNodeQueueName)
                }

                else -> {
                    if (bundle.appTask.isAnyTaskPause())
                        return

                    if (bundle.retryCount < RETRY_COUNT_MIN) {
                        try {
                            bundle.retryCount++
                            bundle.isRetry = true
                            taskPause(bundle.appTask, taskNodeQueueName)
                            download(bundle.appTask, taskNodeQueueName)
                            UtilKLogWrapper.d(TAG, "taskEnd: MIN通信问题重试 ${bundle.retryCount}次 appTask ${bundle.appTask}")
                        } catch (e: TaskException) {
                            /**
                             * [CNetKAppState.STATE_DOWNLOAD_FAIL]
                             */
                            onTaskFinished(ATaskState.STATE_DOWNLOAD_FAIL, downloadTask.id, bundle.appTask, taskNodeQueueName, STaskFinishType.FAIL(e))
                        }
                        return
                    } else if (realCause is StreamResetException) {
                        try {
                            bundle.retryCount++
                            bundle.isRetry = true
                            taskCancel(bundle.appTask, taskNodeQueueName)
                            download(bundle.appTask, taskNodeQueueName)
                            UtilKLogWrapper.d(TAG, "taskEnd: StreamResetException 重新开始下载")
                        } catch (e: TaskException) {
                            /**
                             * [CNetKAppState.STATE_DOWNLOAD_FAIL]
                             */
                            onTaskFinished(ATaskState.STATE_DOWNLOAD_FAIL, downloadTask.id, bundle.appTask, taskNodeQueueName, STaskFinishType.FAIL(e))
                        }
                        return
                    }

                    /**
                     * [CNetKAppState.STATE_DOWNLOAD_FAIL]
                     */
//                NetKApp.instance.onDownloadFail(bundle.appTask, realCause)
                    onTaskFinished(ATaskState.STATE_DOWNLOAD_FAIL, downloadTask.id, bundle.appTask, taskNodeQueueName, STaskFinishType.FAIL(TaskException(CErrorCode.CODE_TASK_DOWNLOAD_FAIL)))
                }
            }
        }
    }
}