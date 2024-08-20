package com.mozhimen.taskk.task.provider.commons

import com.mozhimen.basick.utilk.commons.IUtilK
import com.mozhimen.taskk.task.provider.db.AppTask

/**
 * @ClassName INetKAppProvider
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/19
 * @Version 1.0
 */
interface ITaskProvider : IUtilK {
    fun getSupportFileExtensions(): List<String>
    fun process(appTask: AppTask)
    fun onSuccess(appTask: AppTask)
    fun onFail(appTask: AppTask)
}