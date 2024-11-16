package com.mozhimen.taskk.provider.basic.annors

import androidx.annotation.StringDef
import com.mozhimen.taskk.provider.basic.cons.STaskNode

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
    ATaskName.TASK_DELETE
)
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.TYPE, AnnotationTarget.VALUE_PARAMETER)
annotation class ATaskName {
    companion object {
        const val TASK_DOWNLOAD = "TASK_DOWNLOAD"
        const val TASK_VERIFY = "TASK_VERIFY"
        const val TASK_UNZIP = "TASK_UNZIP"
        const val TASK_INSTALL = "TASK_INSTALL"
        const val TASK_OPEN = "TASK_OPEN"
        const val TASK_CLOSE = "TASK_CLOSE"
        const val TASK_UNINSTALL = "TASK_UNINSTALL"
        const val TASK_DELETE = "TASK_DELETE"

        fun taskName2taskState(@ATaskName taskName: String): @ATaskState Int {
            return when (taskName) {
                TASK_DOWNLOAD -> ATaskState.STATE_DOWNLOAD_CREATE
                TASK_VERIFY -> ATaskState.STATE_VERIFY_CREATE
                TASK_UNZIP -> ATaskState.STATE_UNZIP_CREATE
                TASK_INSTALL -> ATaskState.STATE_INSTALL_CREATE
                TASK_OPEN -> ATaskState.STATE_OPEN_CREATE
                TASK_CLOSE -> ATaskState.STATE_CLOSE_CREATE
                TASK_UNINSTALL -> ATaskState.STATE_UNINSTALL_CREATE
                TASK_DELETE -> ATaskState.STATE_DELETE_CREATE
                else -> AState.STATE_TASK_CREATE
            }
        }

        fun taskNode2taskState(taskNode: STaskNode): @ATaskState Int {
            return taskNode.taskName.taskName2taskState()
        }
    }
}

fun @ATaskName String.taskName2taskState(): @ATaskState Int =
    ATaskName.taskName2taskState(this)

fun STaskNode.taskNode2taskState(): @ATaskState Int =
    ATaskName.taskNode2taskState(this)
