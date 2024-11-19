package com.mozhimen.taskk.provider.basic.annors

import androidx.annotation.StringDef

/**
 * @ClassName ATaskName
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/20
 * @Version 1.0
 */
@StringDef(
    ATaskName.TASK_DOWNLOAD,
    ATaskName.TASK_VERIFY,
    ATaskName.TASK_UNZIP,
    ATaskName.TASK_INSTALL,
    ATaskName.TASK_OPEN,
    ATaskName.TASK_CLOSE,
    ATaskName.TASK_UNINSTALL,
    ATaskName.TASK_DELETE,
    ATaskNodeQueueName.TASK_RESTART,
    ATaskNodeQueueName.TASK_BLOCKER
)
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.TYPE, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FUNCTION)
annotation class ATaskNodeQueueName{
    companion object{
        const val TASK_RESTART = "TASK_RESTART"//重启标志位, 一般用来重新任务
        const val TASK_BLOCKER = "TASK_BLOCKER"//拦截标志位, 一般用来阻塞任务
        const val TASK_MULTI = "TASK_MULTI"//多标志位, 一般用来组合任务
    }
}
