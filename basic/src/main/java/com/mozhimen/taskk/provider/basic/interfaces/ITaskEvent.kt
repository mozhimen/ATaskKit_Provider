package com.mozhimen.taskk.provider.basic.interfaces

import com.mozhimen.taskk.provider.basic.db.AppTask

/**
 * @ClassName ITaskProviderEvent
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/20
 * @Version 1.0
 */
interface ITaskEvent {
    fun taskStart(appTask: AppTask)
    fun taskResume(appTask: AppTask)
    fun taskPause(appTask: AppTask)
    fun taskCancel(appTask: AppTask)

    fun canTaskStart(appTask: AppTask): Boolean
    fun canTaskResume(appTask: AppTask): Boolean
    fun canTaskPause(appTask: AppTask): Boolean
    fun canTaskCancel(appTask: AppTask): Boolean
}