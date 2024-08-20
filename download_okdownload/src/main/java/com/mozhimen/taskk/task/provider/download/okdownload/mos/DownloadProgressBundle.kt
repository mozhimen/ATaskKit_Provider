package com.mozhimen.taskk.task.provider.download.okdownload.mos

import com.mozhimen.taskk.task.provider.db.AppTask

/**
 * @ClassName AppDownloadProgress
 * @Description TODO
 * @Author Mozhimen
 * @Date 2023/11/8 13:56
 * @Version 1.0
 */
data class DownloadProgressBundle(
    var appTask: AppTask,
    var retryCount: Int = 0,
    var isRetry: Boolean = false
)