package com.mozhimen.netk.app.cons

import com.mozhimen.netk.app.tasks.download.mos.AppDownloadException

/**
 * @ClassName ENetKAppFinish
 * @Description TODO
 * @Author Mozhimen
 * @Date 2023/11/10 17:41
 * @Version 1.0
 */
sealed class SNetKAppFinishType {
    object SUCCESS : SNetKAppFinishType()
    object CANCEL : SNetKAppFinishType()
    data class FAIL(val exception: _root_ide_package_.com.mozhimen.netk.app.tasks.download.mos.AppDownloadException) : SNetKAppFinishType()
}