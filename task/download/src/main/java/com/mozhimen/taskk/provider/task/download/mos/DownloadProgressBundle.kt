package com.mozhimen.taskk.provider.task.download.mos

import com.mozhimen.taskk.provider.basic.db.AppTask

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