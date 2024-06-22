package com.mozhimen.netk.app.download.mos

import com.mozhimen.netk.app.task.db.AppTask

/**
 * @ClassName AppDownloadProgress
 * @Description TODO
 * @Author Mozhimen
 * @Date 2023/11/8 13:56
 * @Version 1.0
 */
data class MAppDownloadProgress(
    var appTask: AppTask,
    var retryCount: Int = 0,
    var isRetry: Boolean = false
) {
    fun isDownloading(): Boolean =
        appTask.isTaskDownload()
}