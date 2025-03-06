package com.mozhimen.taskk.provider.tradition.impls.download

import android.content.Context
import android.util.SparseArray
import androidx.core.util.forEach
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
import com.mozhimen.kotlin.lintk.optins.OApiInit_InApplication
import com.mozhimen.kotlin.utilk.android.os.UtilKHandlerWrapper
import com.mozhimen.kotlin.utilk.android.util.UtilKLogWrapper
import com.mozhimen.kotlin.utilk.commons.IUtilK
import com.mozhimen.kotlin.utilk.javax.net.UtilKSSLSocketFactory
import com.mozhimen.kotlin.utilk.kotlin.ranges.constraint
import com.mozhimen.taskk.provider.basic.cons.CErrorCode
import com.mozhimen.taskk.provider.basic.db.AppTask
import com.mozhimen.taskk.provider.basic.db.AppTaskDaoManager
import com.mozhimen.taskk.provider.basic.impls.TaskException
import com.mozhimen.taskk.provider.basic.impls.intErrorCode2taskException
import com.mozhimen.taskk.provider.download.mos.DownloadProgressBundle
import com.mozhimen.taskk.provider.tradition.NetKApp
import com.mozhimen.taskk.provider.tradition.impls.verify.NetKAppVerifyManager
import okhttp3.OkHttpClient
import okhttp3.internal.http2.StreamResetException
import kotlin.math.abs

/**
 * @ClassName AppDownloadManager
 * @Description TODO
 * @Author Mozhimen
 * @Date 2023/11/7 14:19
 * @Version 1.0
 */
@OApiInit_InApplication
internal object NetKAppDownloadManager : DownloadListener1(), IUtilK {
    private const val PARALLEL_RUNNING_COUNT = 3
    private const val RETRY_COUNT_MIN = 10

    //    private const val RETRY_COUNT_MAX = 10
    private const val BLOCK_SIZE_MIN = 100L
    private var _breakpointCompare: IBreakpointCompare? = null
    private val _downloadingTasks = SparseArray<DownloadProgressBundle>()

    //    private val _appDownloadSerialQueue: AppDownloadParallelQueue by lazy { AppDownloadParallelQueue(this) }
//    private val _appDownloadWaitQueue: Queue<DownloadProgressBundle> = Queue<DownloadProgressBundle>()

    ///////////////////////////////////////////////////////////////////////////////////////

    @JvmStatic
    fun init(context: Context, breakpointCompare: IBreakpointCompare) {
        _breakpointCompare = breakpointCompare
        try {
//            AppTaskDaoManager.getAllAtTaskDownloadOrWaitOrPause().forEach {
//                getDownloadTask(it)?.let { downloadTask ->
////                    _downloadingTasks.put(downloadTask.id, DownloadProgressBundle(it))
//
//                }
//                if (it.isTaskWait()) {
//                    downloadWaitCancel(it)
//
//                    /**
//                     * [CNetKAppState.STATE_DOWNLOAD_CANCEL]
//                     */
//                    NetKApp.instance.onDownloadCancel(it)
//                }
//            }

            UtilKLogWrapper.d(TAG, "init: resume task num ${_downloadingTasks.size()}")
//            BreakpointStoreOnSQLite(context, _breakpointCompare)::class.java.name.e(TAG)

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
                    _downloadingTasks.put(downloadTask.id, DownloadProgressBundle(it))
                }
                if (it.isTaskDownloading())
                    downloadPause(it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
//        downloadResumeAll()
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    //region # get
    @JvmStatic
    fun getDownloadTaskCount(): Int {
        return _downloadingTasks.size()
    }

    @JvmStatic
    fun getDownloadTask(appTask: AppTask): DownloadTask? {
        val externalFilesDir = NetKApp.instance.getDownloadPath() ?: run {
            UtilKLogWrapper.d(TAG, "getDownloadTask: get download dir fail")
            return null
        }
        return DownloadTask.Builder(appTask.taskDownloadUrlCurrent, externalFilesDir.absolutePath, appTask.fileNameExt, _breakpointCompare).build()
    }

    /**
     * 查询下载状态
     */
    @JvmStatic
    fun getAppDownloadProgress(appTask: AppTask): DownloadProgressBundle? {
        val downloadTask = getDownloadTask(appTask) ?: run {
            UtilKLogWrapper.d(TAG, "downloadPause: get download task fail")
            return null
        }
        return _downloadingTasks[downloadTask.id]
        /*return when (StatusUtil.getStatus(task)) {
            StatusUtil.Status.PENDING -> {//等待中 不做处理
                DownloadProgressBundle().apply {
                    progressState = CNetKAppState.STATE_DOWNLOAD_WAIT
                }
            }

            StatusUtil.Status.RUNNING -> {//下载中，不做处理
                DownloadProgressBundle().apply {
                    progressState = CNetKAppState.STATE_DOWNLOADING
                    progress = StatusUtil.getCurrentInfo(task)?.let {
                        (it.totalOffset.toFloat() / it.totalLength * 100).toInt()
                    } ?: 0
                }
            }

            StatusUtil.Status.COMPLETED -> {//下载完成，去安装
                DownloadProgressBundle().apply {
                    progressState = CNetKAppState.STATE_DOWNLOAD_SUCCESS
                    progress = 100
                }
            }

            StatusUtil.Status.IDLE -> {
                DownloadProgressBundle().apply {
                    progressState = CNetKAppState.STATE_DOWNLOAD_PAUSE
                    progress = StatusUtil.getCurrentInfo(task)?.let {
                        (it.totalOffset.toFloat() / it.totalLength * 100).toInt()
                    } ?: 0
                }
            }
            //StatusUtil.Status.UNKNOWN
            else -> {
                DownloadProgressBundle().apply {
                    progressState = CNetKAppTaskState.STATE_TASKING
                    progress = StatusUtil.getCurrentInfo(task)?.let {
                        (it.totalOffset.toFloat() / it.totalLength * 100).toInt()
                    } ?: 0
                }
            }
        }*/
    }

//    @JvmStatic
//    fun getDownloadingTaskCount(): Int {
//        var downloadingCount = 0
//        _downloadingTasks.forEach { _, value ->
//            if (value.appTask.taskState == CNetKAppState.STATE_DOWNLOADING)
//                downloadingCount++
//        }
//        return downloadingCount
//    }
    //endregion

    ///////////////////////////////////////////////////////////////////////////////////////

    @Throws(TaskException::class)
    @JvmStatic
    fun download(appTask: AppTask) {
        val externalFilesDir = NetKApp.instance.getDownloadPath()
            ?: throw CErrorCode.CODE_TASK_DOWNLOAD_PATH_NOT_EXIST.intErrorCode2taskException()
        val downloadTask = DownloadTask.Builder(appTask.taskDownloadUrlCurrent, externalFilesDir, _breakpointCompare)//先构建一个Task 框架可以保证Id唯一
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
        if (_downloadingTasks[downloadTask.id] == null) {
            _downloadingTasks.put(downloadTask.id, DownloadProgressBundle(appTask))
        }
        downloadTask.enqueue(this)
//        DownloadTask.enqueue(arrayOf(downloadTask), this)
//        if (_appDownloadSerialQueue.workingTaskId != downloadTask.id)
//            _appDownloadSerialQueue.enqueue(downloadTask)


//        if (getDownloadingTaskCount() > PARALLEL_RUNNING_COUNT) {
//            /**
//             * [CNetKAppState.STATE_DOWNLOAD_WAIT]
//             */
//            NetKApp.onDownloadWait(appTask.apply { downloadId = downloadTask.id })
//        } else {
        val currentIndex = (appTask.taskDownloadProgress.toFloat() / 100f) * appTask.taskDownloadFileSizeTotal.toFloat()
        /**
         * [CNetKAppState.STATE_DOWNLOADING]
         */
        NetKApp.instance.onDownloading(appTask, appTask.taskDownloadProgress.constraint(1, 100), currentIndex.toLong(), appTask.taskDownloadFileSizeTotal, 0)
//        }
//        listener?.onSuccess()
    }

    @JvmStatic
    fun downloadPause(appTask: AppTask) {
        val downloadTask = getDownloadTask(appTask) ?: run {
            UtilKLogWrapper.d(TAG, "downloadPause: get download task fail")
            return
        }
        downloadTask.cancel()//取消任务

        /**
         * [CNetKAppState.STATE_DOWNLOAD_PAUSE]
         */
        NetKApp.instance.onDownloadPause(appTask)
    }

    @JvmStatic
    fun downloadResumeAllDelay(delayMills: Long) {
        UtilKHandlerWrapper.postDelayed(delayMills) {
            _downloadingTasks.forEach { _, value ->
                UtilKLogWrapper.d(TAG, "downloadResumeAll: appTask ${value.appTask}")
                if (value.appTask.isTaskPause()) {
                    downloadResume(value.appTask)
                    UtilKLogWrapper.d(TAG, "downloadResumeAll: 恢复下载 appTask ${value.appTask}")
                } /*else {
                    download(value.appTask)
                    UtilKLogWrapper.d(TAG, "downloadResumeAll: 开始下载 appTask ${value.appTask}")
                }*/
            }
        }
    }

    @JvmStatic
    fun downloadPauseAll() {
        _downloadingTasks.forEach { _, value ->
            downloadPause(value.appTask)
            UtilKLogWrapper.d(TAG, "downloadPauseAll: appTask ${value.appTask}")
        }
    }

    /**
     * 恢复任务
     */
    @JvmStatic
    fun downloadResume(appTask: AppTask) {
        val downloadTask = getDownloadTask(appTask) ?: run {
            UtilKLogWrapper.d(TAG, "downloadResume: get download task fail")
            return
        }
        if (StatusUtil.getStatus(downloadTask) != StatusUtil.Status.RUNNING) {
//            _appDownloadSerialQueue.enqueue(task)
            downloadTask.enqueue(this)
//            DownloadTask.enqueue(arrayOf(downloadTask), this)
        }
//        _downloadingTasks.put(task.id, appTask)

        /**
         * [CNetKAppState.STATE_DOWNLOADING]
         */
        NetKApp.instance.onDownloading(appTask, appTask.taskDownloadProgress.constraint(1, 100), appTask.taskDownloadFileSizeOffset, appTask.taskDownloadFileSizeTotal, BLOCK_SIZE_MIN)
    }

    /**
     * 任务取消等待
     */
    @JvmStatic
    fun downloadWaitCancel(appTask: AppTask/*, onDeleteBlock: IAB_Listener<Boolean, Int>?*/) {
        val downloadTask = getDownloadTask(appTask) ?: run {
//            UtilKHandlerWrapper.post {
//                onDeleteBlock?.invoke(false, CNetKAppErrorCode.CODE_DOWNLOAD_CANT_FIND_TASK)
//            }
            UtilKLogWrapper.d(TAG, "downloadWaitCancel: get download task fail")
            return
        }
        downloadTask.cancel()//然后取消任务
        _downloadingTasks.delete(downloadTask.id)
//        _appDownloadSerialQueue.remove(task)//先从队列中移除
//        DownloadTask.cancel(arrayOf(downloadTask))

        /**
         * [CNetKAppState.STATE_DOWNLOAD_CANCEL]
         */
        NetKApp.instance.onDownloadCancel(appTask)
    }

    @JvmStatic
    fun downloadRetry(appTask: AppTask) {
        val downloadTask = getDownloadTask(appTask) ?: run {
//            UtilKHandlerWrapper.post {
//                onDeleteBlock?.invoke(false, CNetKAppErrorCode.CODE_DOWNLOAD_CANT_FIND_TASK)
//            }
            UtilKLogWrapper.d(TAG, "downloadRetry: get download task fail")
            return
        }
        downloadTask.cancel()//然后取消任务
//        _appDownloadSerialQueue.remove(task)//先从队列中移除
//        DownloadTask.cancel(arrayOf(downloadTask))
    }

    /**
     * 删除任务
     */
    @JvmStatic
    fun downloadCancel(appTask: AppTask/*, onDeleteBlock: IAB_Listener<Boolean, Int>?*/) {
        val downloadTask = getDownloadTask(appTask) ?: run {
//            UtilKHandlerWrapper.post {
//                onDeleteBlock?.invoke(false, CNetKAppErrorCode.CODE_DOWNLOAD_CANT_FIND_TASK)
//            }
            UtilKLogWrapper.d(TAG, "downloadCancel: get download task fail")
            return
        }
        //        DownloadTask.cancel(arrayOf(downloadTask))
//        _appDownloadSerialQueue.remove(task)
        downloadTask.cancel()
        _downloadingTasks.delete(downloadTask.id)//先从队列中移除
        OkDownload.with().breakpointStore().remove(downloadTask.id)
        downloadTask.file?.delete()

        //            onDeleteBlock?.invoke(true, -1)
        /**
         * [CNetKAppState.STATE_DOWNLOAD_CANCEL]
         */
        NetKApp.instance.onDownloadCancel(appTask)
    }

    @JvmStatic
    fun downloadRetryWithClear(appTask: AppTask) {
        val downloadTask = getDownloadTask(appTask) ?: run {
            UtilKLogWrapper.d(TAG, "downloadRetryWithClear: get download task fail")
            return
        }
        downloadTask.cancel()
//        DownloadTask.cancel(arrayOf(downloadTask))
        OkDownload.with().breakpointStore().remove(downloadTask.id)
        downloadTask.file?.delete()
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    override fun taskStart(downloadTask: DownloadTask, model: Listener1Assist.Listener1Model) {
        UtilKLogWrapper.d(TAG, "taskStart: task $downloadTask")
        var mAppDownloadProgress = _downloadingTasks[downloadTask.id]
        if (mAppDownloadProgress == null) {
            val appTask = AppTaskDaoManager.get_ofTaskDownloadUrlCurrent(downloadTask.url) ?: return
            _downloadingTasks[downloadTask.id] = DownloadProgressBundle(appTask)
            mAppDownloadProgress = _downloadingTasks[downloadTask.id]
        }
        mAppDownloadProgress?.let { appTask ->
//            if (getDownloadingTaskCount() > PARALLEL_RUNNING_COUNT) {
//                /**
//                 * [CNetKAppState.STATE_DOWNLOAD_WAIT]
//                 */
//                NetKApp.instance.onDownloadWait(appTask.appTask.apply { downloadId = downloadTask.id })
//            } else {
            val currentIndex = (appTask.appTask.taskDownloadProgress.toFloat() / 100f) * appTask.appTask.taskDownloadFileSizeTotal.toFloat()
            /**
             * [CNetKAppState.STATE_DOWNLOADING]
             */
            NetKApp.instance.onDownloading(appTask.appTask, appTask.appTask.taskDownloadProgress.constraint(1, 100), currentIndex.toLong(), appTask.appTask.taskDownloadFileSizeTotal, 0)
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
        var mAppDownloadProgress = _downloadingTasks[downloadTask.id]
        if (mAppDownloadProgress == null) {
            val appTask = AppTaskDaoManager.get_ofTaskDownloadUrlCurrent(downloadTask.url) ?: return
            _downloadingTasks[downloadTask.id] = DownloadProgressBundle(appTask)
            mAppDownloadProgress = _downloadingTasks[downloadTask.id]
        }
        mAppDownloadProgress?.let { appTask ->
            val progress = ((currentOffset.toFloat() / totalLength.toFloat()) * 100f).toInt().constraint(1, 100)
            val offsetFileSizePerSeconds = abs(currentOffset - appTask.appTask.taskDownloadFileSizeOffset)

            UtilKLogWrapper.d(TAG, "progress: $progress currentOffset $currentOffset  totalLength $totalLength")
            if (appTask.appTask.isTaskPause()) return
            if (progress < appTask.appTask.taskDownloadProgress) return

            /**
             * [CNetKAppState.STATE_DOWNLOADING]
             */
            NetKApp.instance.onDownloading(
                appTask.appTask.apply {
                    taskDownloadProgress = progress
                    taskDownloadFileSizeOffset = currentOffset
                },
                progress, currentOffset, appTask.appTask.taskDownloadFileSizeTotal, offsetFileSizePerSeconds
            )
        }
    }

    override fun taskEnd(downloadTask: DownloadTask, cause: EndCause, realCause: Exception?, model: Listener1Assist.Listener1Model) {
        UtilKLogWrapper.d(TAG, "taskEnd: $downloadTask cause ${cause.name} realCause ${realCause.toString()}")
        var mAppDownloadProgress = _downloadingTasks[downloadTask.id]
        if (mAppDownloadProgress == null) {
            val appTask = AppTaskDaoManager.get_ofTaskDownloadUrlCurrent(downloadTask.url) ?: return
            _downloadingTasks[downloadTask.id] = DownloadProgressBundle(appTask)
            mAppDownloadProgress = _downloadingTasks[downloadTask.id]
        }
        mAppDownloadProgress?.let { appTask ->
            when (cause) {
                EndCause.COMPLETED -> {
                    onDownloadSuccess(appTask.appTask)
                }

                EndCause.CANCELED -> {
                    if (appTask.appTask.isTaskPause() || appTask.appTask.isTaskCancel())
                        return
                    if (appTask.isRetry) {
                        appTask.isRetry = false
                        download(appTask.appTask)
                        return
                    }
                    /**
                     * [CNetKAppState.STATE_DOWNLOAD_CANCEL]
                     */
                    NetKApp.instance.onDownloadCancel(appTask.appTask)//下载取消
                }

                else -> {
                    if (appTask.appTask.isTaskPause())
                        return

                    if (appTask.retryCount < RETRY_COUNT_MIN) {
                        try {
                            appTask.retryCount++
                            appTask.isRetry = true
                            downloadRetry(appTask.appTask)
                            download(appTask.appTask)
                            UtilKLogWrapper.d(TAG, "taskEnd: MIN通信问题重试 ${appTask.retryCount}次 appTask ${appTask.appTask}")
                        } catch (e: TaskException) {
                            /**
                             * [CNetKAppState.STATE_DOWNLOAD_FAIL]
                             */
                            NetKApp.instance.onDownloadFail(appTask.appTask, e)
                            _downloadingTasks.delete(downloadTask.id)//从队列里移除掉
                        }
                        return
                    } else if (realCause is StreamResetException) {
                        try {
                            appTask.retryCount++
                            appTask.isRetry = true
                            downloadRetryWithClear(appTask.appTask)
                            download(appTask.appTask)
                            UtilKLogWrapper.d(TAG, "taskEnd: StreamResetException 重新开始下载")
                        } catch (e: TaskException) {
                            /**
                             * [CNetKAppState.STATE_DOWNLOAD_FAIL]
                             */
                            NetKApp.instance.onDownloadFail(appTask.appTask, e)
                            _downloadingTasks.delete(downloadTask.id)//从队列里移除掉
                        }
                        return
                    }/*else if (appTask.retryCount < RETRY_COUNT_MAX) {
                        try {
                            appTask.retryCount++
                            appTask.isRetry = true
                            downloadRetryWithClear(appTask.appTask)
                            download(appTask.appTask)
                            UtilKLogWrapper.d(TAG, "taskEnd: MAX通信问题重试 ${appTask.retryCount}次 appTask ${appTask.appTask}")
                        } catch (e: TaskException) {
                            */
                    /**
                     * [CNetKAppState.STATE_DOWNLOAD_FAIL]
                     *//*
                            NetKApp.instance.onDownloadFail(appTask.appTask, e)
                            _downloadingTasks.delete(downloadTask.id)//从队列里移除掉
                        }
                        return
                    }*/

                    /**
                     * [CNetKAppState.STATE_DOWNLOAD_FAIL]
                     */
                    NetKApp.instance.onDownloadFail(appTask.appTask, realCause)
                }
            }
        }
        _downloadingTasks.delete(downloadTask.id)//从队列里移除掉
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    @JvmStatic
    private fun onDownloadSuccess(appTask: AppTask) {
        /**
         * [CNetKAppState.STATE_DOWNLOAD_SUCCESS]
         */
        NetKApp.instance.onDownloadSuccess(appTask)//下载完成，去安装

        NetKAppVerifyManager.verify(appTask)//下载完成，去安装
    }
}