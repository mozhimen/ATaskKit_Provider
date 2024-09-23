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
    ATaskQueueName.TASK_RESTART
)
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.TYPE, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FUNCTION)
annotation class ATaskQueueName{
    companion object{
        const val TASK_RESTART = "TASK_RESTART"
    }
}
