package com.mozhimen.taskk.provider.basic.interfaces

import com.mozhimen.taskk.provider.basic.db.AppTask

/**
 * @ClassName ITaskProviderEvent
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/20
 * @Version 1.0
 */
interface ITaskProviderEvent {
    fun taskStart(appTask: AppTask)
    fun taskPause(appTask: AppTask)
    fun taskResume(appTask: AppTask)
    fun taskCancel(appTask: AppTask)
}