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
    ATaskName.TASK_UNINSTALL,
)
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.TYPE,AnnotationTarget.VALUE_PARAMETER)
annotation class ATaskName {
    companion object {
        const val TASK_DOWNLOAD = "TASK_DOWNLOAD"
        const val TASK_VERIFY = "TASK_VERIFY"
        const val TASK_UNZIP = "TASK_UNZIP"
        const val TASK_INSTALL = "TASK_INSTALL"
        const val TASK_OPEN = "TASK_OPEN"
        const val TASK_UNINSTALL = "TASK_UNINSTALL"
    }
}
