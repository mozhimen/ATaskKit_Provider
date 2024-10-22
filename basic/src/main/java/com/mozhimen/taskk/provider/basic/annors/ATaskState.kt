package com.mozhimen.taskk.provider.basic.annors

import android.util.Log
import androidx.annotation.IntDef
import com.mozhimen.kotlin.lintk.optins.OApiInit_InApplication
import com.mozhimen.taskk.provider.basic.annors.ATaskName.Companion.TASK_CLOSE
import com.mozhimen.taskk.provider.basic.annors.ATaskName.Companion.TASK_DELETE
import com.mozhimen.taskk.provider.basic.annors.ATaskName.Companion.TASK_DOWNLOAD
import com.mozhimen.taskk.provider.basic.annors.ATaskName.Companion.TASK_INSTALL
import com.mozhimen.taskk.provider.basic.annors.ATaskName.Companion.TASK_OPEN
import com.mozhimen.taskk.provider.basic.annors.ATaskName.Companion.TASK_UNINSTALL
import com.mozhimen.taskk.provider.basic.annors.ATaskName.Companion.TASK_UNZIP
import com.mozhimen.taskk.provider.basic.annors.ATaskName.Companion.TASK_VERIFY
import com.mozhimen.taskk.provider.basic.bases.ATask
import com.mozhimen.taskk.provider.basic.bases.ATaskManager

/**
 * @ClassName ATaskState
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/9/23
 * @Version 1.0
 */
@Target(AnnotationTarget.TYPE, AnnotationTarget.VALUE_PARAMETER)
@IntDef(
    ATaskState.STATE_DOWNLOAD_CREATE,
    ATaskState.STATE_DOWNLOADING,
    ATaskState.STATE_DOWNLOAD_PAUSE,
    ATaskState.STATE_DOWNLOAD_CANCEL,
    ATaskState.STATE_DOWNLOAD_SUCCESS,
    ATaskState.STATE_DOWNLOAD_FAIL,

    ATaskState.STATE_VERIFY_CREATE,
    ATaskState.STATE_VERIFYING,
    ATaskState.STATE_VERIFY_PAUSE,
    ATaskState.STATE_VERIFY_CANCEL,
    ATaskState.STATE_VERIFY_SUCCESS,
    ATaskState.STATE_VERIFY_FAIL,

    ATaskState.STATE_UNZIP_CREATE,
    ATaskState.STATE_UNZIPING,
    ATaskState.STATE_UNZIP_PAUSE,
    ATaskState.STATE_UNZIP_CANCEL,
    ATaskState.STATE_UNZIP_SUCCESS,
    ATaskState.STATE_UNZIP_FAIL,

    ATaskState.STATE_INSTALL_CREATE,
    ATaskState.STATE_INSTALLING,
    ATaskState.STATE_INSTALL_PAUSE,
    ATaskState.STATE_INSTALL_CANCEL,
    ATaskState.STATE_INSTALL_SUCCESS,
    ATaskState.STATE_INSTALL_FAIL,

    ATaskState.STATE_OPEN_CREATE,
    ATaskState.STATE_OPENING,
    ATaskState.STATE_OPEN_PAUSE,
    ATaskState.STATE_OPEN_CANCEL,
    ATaskState.STATE_OPEN_SUCCESS,
    ATaskState.STATE_OPEN_FAIL,

    ATaskState.STATE_CLOSE_CREATE,
    ATaskState.STATE_CLOSING,
    ATaskState.STATE_CLOSE_PAUSE,
    ATaskState.STATE_CLOSE_CANCEL,
    ATaskState.STATE_CLOSE_SUCCESS,
    ATaskState.STATE_CLOSE_FAIL,

    ATaskState.STATE_UNINSTALL_CREATE,
    ATaskState.STATE_UNINSTALLING,
    ATaskState.STATE_UNINSTALL_PAUSE,
    ATaskState.STATE_UNINSTALL_CANCEL,
    ATaskState.STATE_UNINSTALL_SUCCESS,
    ATaskState.STATE_UNINSTALL_FAIL,

    ATaskState.STATE_DELETE_CREATE,
    ATaskState.STATE_DELETING,
    ATaskState.STATE_DELETE_PAUSE,
    ATaskState.STATE_DELETE_CANCEL,
    ATaskState.STATE_DELETE_SUCCESS,
    ATaskState.STATE_DELETE_FAIL,
)
annotation class ATaskState {
    companion object {
        fun getTaskCode(@ATaskState taskState: Int): @ATaskState Int =
            (taskState / 10) * 10

        fun taskState2taskName(@ATaskState taskState: Int): @ATaskName String? {
            return (when (taskState) {
                STATE_DOWNLOAD_CREATE  -> TASK_DOWNLOAD
                STATE_VERIFY_CREATE  -> TASK_VERIFY
                STATE_UNZIP_CREATE  -> TASK_UNZIP
                STATE_INSTALL_CREATE  -> TASK_INSTALL
                STATE_OPEN_CREATE  -> TASK_OPEN
                STATE_CLOSE_CREATE  -> TASK_CLOSE
                STATE_UNINSTALL_CREATE  -> TASK_UNINSTALL
                STATE_DELETE_CREATE  -> TASK_DELETE
                else -> null
            }).also { Log.d("ATaskState>>>>>", "taskState2taskName: $it") }
        }

        fun intTaskState2strTaskState(state: Int): String =
            when (state) {
//            CNetKAppState.STATE_DOWNLOAD_WAIT -> "下载等待"
                STATE_DOWNLOADING -> "下载中 "
                STATE_DOWNLOAD_PAUSE -> "下载暂停"
                STATE_DOWNLOAD_CANCEL -> "下载取消"
                STATE_DOWNLOAD_SUCCESS -> "下载成功"
                STATE_DOWNLOAD_FAIL -> "下载失败"

                STATE_VERIFYING -> "验证中 "
                STATE_VERIFY_PAUSE -> "验证暂停"
                STATE_VERIFY_CANCEL -> "验证取消"
                STATE_VERIFY_SUCCESS -> "验证成功"
                STATE_VERIFY_FAIL -> "验证失败"

                STATE_UNZIPING -> "解压中 "
                STATE_UNZIP_PAUSE -> "解压暂停"
                STATE_UNZIP_CANCEL -> "解压取消"
                STATE_UNZIP_SUCCESS -> "解压成功"
                STATE_UNZIP_FAIL -> "解压失败"

                STATE_INSTALLING -> "安装中 "
                STATE_INSTALL_PAUSE -> "安装暂停"
                STATE_INSTALL_CANCEL -> "安装取消"
                STATE_INSTALL_SUCCESS -> "安装成功"
                STATE_INSTALL_FAIL -> "安装失败"

                STATE_OPENING -> "打开中 "
                STATE_OPEN_PAUSE -> "打开暂停"
                STATE_OPEN_CANCEL -> "打开取消"
                STATE_OPEN_SUCCESS -> "打开成功"
                STATE_OPEN_FAIL -> "打开失败"

                STATE_CLOSING -> "关闭中 "
                STATE_CLOSE_PAUSE -> "关闭暂停"
                STATE_CLOSE_CANCEL -> "关闭取消"
                STATE_CLOSE_SUCCESS -> "关闭成功"
                STATE_CLOSE_FAIL -> "关闭失败"

                STATE_UNINSTALLING -> "卸载中 "
                STATE_UNINSTALL_PAUSE -> "卸载暂停"
                STATE_UNINSTALL_CANCEL -> "卸载取消"
                STATE_UNINSTALL_SUCCESS -> "卸载成功"
                STATE_UNINSTALL_FAIL -> "卸载失败"

                STATE_DELETING -> "删除中 "
                STATE_DELETE_PAUSE -> "删除暂停"
                STATE_DELETE_CANCEL -> "删除取消"
                STATE_DELETE_SUCCESS -> "删除成功"
                STATE_DELETE_FAIL -> "删除失败"


                AState.STATE_TASK_CREATE -> "任务创建"
                AState.STATE_TASK_UPDATE -> "任务更新"
//          AState.STATE_TASK_WAIT -> "任务等待"
                AState.STATE_TASK_PAUSE -> "任务暂停"
                AState.STATE_TASK_CANCEL -> "任务取消"
                AState.STATE_TASK_SUCCESS -> "任务成功"
                AState.STATE_TASK_FAIL -> "任务失败"
                else -> "任务中 "
            }

        @OptIn(OApiInit_InApplication::class)
        fun canTaskExecute(
            @ATaskState taskState: Int,
            taskManager: ATaskManager,
            @AFileExt fileExt: String,
            @ATaskQueueName taskQueueName: String,
            @ATaskState currentTaskCode: Int,
            @ATaskState prevTaskCode: Int? = null,
        ): Boolean {
            val preTaskCode =
                taskManager.getPrevTaskName_ofTaskQueue(fileExt, taskQueueName, getTaskCode(taskState).taskState2taskName())?.taskName2taskState() ?: prevTaskCode?.let { getTaskCode(it) }
            return (preTaskCode?.let {
                AState.canTaskExecute(taskState, currentTaskCode, it)
            } ?: run {
//                AState.canTaskStart(taskState)
                true
            }).also {
                Log.d(
                    "ATaskState>>>>>",
                    "canTaskExecute: $it preTaskCode $preTaskCode "
                )
            } //taskState in STATE_DOWNLOAD_CREATE..STATE_DOWNLOAD_PAUSE || taskState in AState.STATE_TASK_CREATE..AState.STATE_TASK_UPDATE
        }

        /////////////////////////////////////////////////////////////////////

        const val STATE_DOWNLOAD_CREATE = 10//STATE_DOWNLOADED = 5//未下载

        //    const val STATE_DOWNLOAD_WAIT = STATE_DOWNLOAD_CREATE + CNetKAppTaskState.STATE_TASK_WAIT//11//下载等待
        const val STATE_DOWNLOADING = STATE_DOWNLOAD_CREATE + AState.STATE_TASKING//12//STATE_DOWNLOAD_IN_PROGRESS = 6//正在下载
        const val STATE_DOWNLOAD_PAUSE = STATE_DOWNLOAD_CREATE + AState.STATE_TASK_PAUSE//13//STATE_DOWNLOAD_PAUSED = 7//下载暂停
        const val STATE_DOWNLOAD_CANCEL = STATE_DOWNLOAD_CREATE + AState.STATE_TASK_CANCEL//17//下载取消
        const val STATE_DOWNLOAD_SUCCESS = STATE_DOWNLOAD_CREATE + AState.STATE_TASK_SUCCESS//18//STATE_DOWNLOAD_COMPLETED = 8//下载完成
        const val STATE_DOWNLOAD_FAIL = STATE_DOWNLOAD_CREATE + AState.STATE_TASK_FAIL//19//STATE_DOWNLOAD_FAILED = 10//下载失败

        @OptIn(OApiInit_InApplication::class)
        fun canTaskDownload(taskState: Int, taskManager: ATaskManager, @AFileExt fileExt: String, @ATaskQueueName taskQueueName: String): Boolean =
            canTaskExecute(taskState, taskManager, fileExt, taskQueueName, STATE_DOWNLOAD_CREATE)

        fun atTaskDownload(taskState: Int): Boolean =
            taskState in STATE_DOWNLOAD_CREATE..STATE_DOWNLOAD_FAIL

        fun isTaskDownloading(taskState: Int): Boolean =
            taskState == STATE_DOWNLOADING

        fun isTaskDownloadPause(taskState: Int): Boolean =
            taskState == STATE_DOWNLOAD_PAUSE

        fun isTaskDownloadSuccess(taskState: Int): Boolean =
            taskState >= STATE_DOWNLOAD_SUCCESS || taskState == AState.STATE_TASK_SUCCESS

        //////////////////////////////////////////////////////////////
        //校验
        const val STATE_VERIFY_CREATE = 20
        const val STATE_VERIFYING = STATE_VERIFY_CREATE + AState.STATE_TASKING//22//STATE_CHECKING = 14//校验中
        const val STATE_VERIFY_PAUSE = STATE_VERIFY_CREATE + AState.STATE_TASK_PAUSE//23
        const val STATE_VERIFY_CANCEL = STATE_VERIFY_CREATE + AState.STATE_TASK_CANCEL//27//STATE_CHECKING_SUCCESS = 15//校验成功
        const val STATE_VERIFY_SUCCESS = STATE_VERIFY_CREATE + AState.STATE_TASK_SUCCESS//28//STATE_CHECKING_SUCCESS = 15//校验成功
        const val STATE_VERIFY_FAIL = STATE_VERIFY_CREATE + AState.STATE_TASK_FAIL//29//STATE_CHECKING_FAILURE = 16//校验失败

        @OptIn(OApiInit_InApplication::class)
        fun canTaskVerify(taskState: Int, taskManager: ATaskManager, @AFileExt fileExt: String, @ATaskQueueName taskQueueName: String): Boolean =
            canTaskExecute(taskState, taskManager, fileExt, taskQueueName, STATE_VERIFY_CREATE)
        //taskState in STATE_DOWNLOAD_SUCCESS..STATE_VERIFY_PAUSE || taskState in AState.STATE_TASK_CREATE..AState.STATE_TASK_UPDATE

        fun atTaskVerify(taskState: Int): Boolean =
            taskState in STATE_VERIFY_CREATE..STATE_VERIFY_FAIL

        fun isTaskVerifying(taskState: Int): Boolean =
            taskState == STATE_VERIFYING

        fun isTaskVerifyPause(taskState: Int): Boolean =
            taskState == STATE_VERIFY_PAUSE

        fun isTaskVerifySuccess(taskState: Int): Boolean =
            taskState >= STATE_VERIFY_SUCCESS || taskState == AState.STATE_TASK_SUCCESS

        //////////////////////////////////////////////////////////////
        //解压
        const val STATE_UNZIP_CREATE = 30
        const val STATE_UNZIPING = STATE_UNZIP_CREATE + AState.STATE_TASKING//30//STATE_UNPACKING = 11//解压中
        const val STATE_UNZIP_PAUSE = STATE_UNZIP_CREATE + AState.STATE_TASK_PAUSE//33
        const val STATE_UNZIP_CANCEL = STATE_UNZIP_CREATE + AState.STATE_TASK_CANCEL//37//STATE_UNPACKING_SUCCESSFUL = 12//解压成功
        const val STATE_UNZIP_SUCCESS = STATE_UNZIP_CREATE + AState.STATE_TASK_SUCCESS//38//STATE_UNPACKING_SUCCESSFUL = 12//解压成功
        const val STATE_UNZIP_FAIL = STATE_UNZIP_CREATE + AState.STATE_TASK_FAIL//39//STATE_UNPACKING_FAILED = 13//解压失败

        @OptIn(OApiInit_InApplication::class)
        fun canTaskUnzip(taskState: Int, taskManager: ATaskManager, @AFileExt fileExt: String, @ATaskQueueName taskQueueName: String): Boolean =
            canTaskExecute(taskState, taskManager, fileExt, taskQueueName, STATE_UNZIP_CREATE)
        //taskState in STATE_VERIFY_SUCCESS..STATE_UNZIP_PAUSE || taskState in AState.STATE_TASK_CREATE..AState.STATE_TASK_UPDATE

        fun atTaskUnzip(taskState: Int): Boolean =
            taskState in STATE_UNZIP_CREATE..STATE_UNZIP_FAIL

        fun isTaskUnziping(taskState: Int): Boolean =
            taskState == STATE_UNZIPING

        fun isTaskUnzipPause(taskState: Int): Boolean =
            taskState == STATE_UNZIP_PAUSE

        fun isTaskUnzipSuccess(taskState: Int): Boolean =
            taskState >= STATE_UNZIP_SUCCESS || taskState == AState.STATE_TASK_SUCCESS

        //////////////////////////////////////////////////////////////
        //安装
        const val STATE_INSTALL_CREATE = 40
        const val STATE_INSTALLING = STATE_INSTALL_CREATE + AState.STATE_TASKING//42//STATE_INSTALLING = 1//安装中
        const val STATE_INSTALL_PAUSE = STATE_INSTALL_CREATE + AState.STATE_TASK_PAUSE//43
        const val STATE_INSTALL_CANCEL = STATE_INSTALL_CREATE + AState.STATE_TASK_CANCEL//47//STATE_INSTALLED = 2//安装取消
        const val STATE_INSTALL_SUCCESS = STATE_INSTALL_CREATE + AState.STATE_TASK_SUCCESS//48//STATE_INSTALLED = 2//已安装
        const val STATE_INSTALL_FAIL = STATE_INSTALL_CREATE + AState.STATE_TASK_FAIL//49//STATE_INSTALLED = 2//已安装

        @OptIn(OApiInit_InApplication::class)
        fun canTaskInstall(taskState: Int, taskManager: ATaskManager, @AFileExt fileExt: String, @ATaskQueueName taskQueueName: String): Boolean =
            canTaskExecute(taskState, taskManager, fileExt, taskQueueName, STATE_INSTALL_CREATE)
//            taskState in STATE_UNZIP_SUCCESS..STATE_INSTALL_PAUSE || taskState in AState.STATE_TASK_CREATE..AState.STATE_TASK_UPDATE

        fun atTaskInstall(taskState: Int): Boolean =
            taskState in STATE_INSTALL_CREATE..STATE_INSTALL_FAIL

        fun isTaskInstalling(taskState: Int): Boolean =
            taskState == STATE_INSTALLING

        fun isTaskInstallPause(taskState: Int): Boolean =
            taskState == STATE_INSTALL_PAUSE

        fun isTaskInstallSuccess(taskState: Int): Boolean =
            taskState >= STATE_INSTALL_SUCCESS || taskState == AState.STATE_TASK_SUCCESS

        //////////////////////////////////////////////////////////////

        //打开
        const val STATE_OPEN_CREATE = 50
        const val STATE_OPENING = STATE_OPEN_CREATE + AState.STATE_TASKING//52
        const val STATE_OPEN_PAUSE = STATE_OPEN_CREATE + AState.STATE_TASK_PAUSE//53
        const val STATE_OPEN_CANCEL = STATE_OPEN_CREATE + AState.STATE_TASK_CANCEL//57
        const val STATE_OPEN_SUCCESS = STATE_OPEN_CREATE + AState.STATE_TASK_SUCCESS//58
        const val STATE_OPEN_FAIL = STATE_OPEN_CREATE + AState.STATE_TASK_FAIL//59

        @OptIn(OApiInit_InApplication::class)
        fun canTaskOpen(taskState: Int, taskManager: ATaskManager, @AFileExt fileExt: String, @ATaskQueueName taskQueueName: String): Boolean =
            canTaskExecute(taskState, taskManager, fileExt, taskQueueName, STATE_OPEN_CREATE)
//            taskState in STATE_INSTALL_SUCCESS..STATE_OPEN_PAUSE || taskState in AState.STATE_TASK_CREATE..AState.STATE_TASK_UPDATE

        fun atTaskOpen(taskState: Int): Boolean =
            taskState in STATE_OPEN_CREATE..STATE_OPEN_FAIL

        fun isTaskOpening(taskState: Int): Boolean =
            taskState == STATE_OPENING

        fun isTaskOpenPause(taskState: Int): Boolean =
            taskState == STATE_OPEN_PAUSE

        fun isTaskOpenSuccess(taskState: Int): Boolean =
            taskState >= STATE_OPEN_SUCCESS || taskState == AState.STATE_TASK_SUCCESS

        //////////////////////////////////////////////////////////////

        //卸载
        const val STATE_CLOSE_CREATE = 60
        const val STATE_CLOSING = STATE_CLOSE_CREATE + AState.STATE_TASKING//52
        const val STATE_CLOSE_PAUSE = STATE_CLOSE_CREATE + AState.STATE_TASK_PAUSE//53
        const val STATE_CLOSE_CANCEL = STATE_CLOSE_CREATE + AState.STATE_TASK_CANCEL//57
        const val STATE_CLOSE_SUCCESS = STATE_CLOSE_CREATE + AState.STATE_TASK_SUCCESS//58
        const val STATE_CLOSE_FAIL = STATE_CLOSE_CREATE + AState.STATE_TASK_FAIL//59

        @OptIn(OApiInit_InApplication::class)
        fun canTaskClose(taskState: Int, taskManager: ATaskManager, @AFileExt fileExt: String, @ATaskQueueName taskQueueName: String): Boolean =
            canTaskExecute(taskState, taskManager, fileExt, taskQueueName, STATE_CLOSE_CREATE, STATE_OPEN_CREATE) || AState.canTaskStart(taskState) || AState.canTaskFinish(taskState)

        fun atTaskClose(taskState: Int): Boolean =
            taskState in STATE_CLOSE_CREATE..STATE_CLOSE_FAIL

        fun isTaskClosing(taskState: Int): Boolean =
            taskState == STATE_CLOSING

        fun isTaskClosePause(taskState: Int): Boolean =
            taskState == STATE_CLOSE_PAUSE

        fun isTaskCloseSuccess(taskState: Int): Boolean =
            taskState >= STATE_CLOSE_SUCCESS || taskState == AState.STATE_TASK_SUCCESS

        //////////////////////////////////////////////////////////////

        //卸载
        const val STATE_UNINSTALL_CREATE = 70
        const val STATE_UNINSTALLING = STATE_UNINSTALL_CREATE + AState.STATE_TASKING//52
        const val STATE_UNINSTALL_PAUSE = STATE_UNINSTALL_CREATE + AState.STATE_TASK_PAUSE//53
        const val STATE_UNINSTALL_CANCEL = STATE_UNINSTALL_CREATE + AState.STATE_TASK_CANCEL//57
        const val STATE_UNINSTALL_SUCCESS = STATE_UNINSTALL_CREATE + AState.STATE_TASK_SUCCESS//58
        const val STATE_UNINSTALL_FAIL = STATE_UNINSTALL_CREATE + AState.STATE_TASK_FAIL//59

        @OptIn(OApiInit_InApplication::class)
        fun canTaskUninstall(taskState: Int, taskManager: ATaskManager, @AFileExt fileExt: String, @ATaskQueueName taskQueueName: String): Boolean =
            canTaskExecute(taskState, taskManager, fileExt, taskQueueName, STATE_UNINSTALL_CREATE, STATE_INSTALL_CREATE) || AState.canTaskStart(taskState) || AState.canTaskFinish(taskState)

        fun atTaskUninstall(taskState: Int): Boolean =
            taskState in STATE_UNINSTALL_CREATE..STATE_UNINSTALL_FAIL

        fun isTaskUninstalling(taskState: Int): Boolean =
            taskState == STATE_UNINSTALLING

        fun isTaskUninstallPause(taskState: Int): Boolean =
            taskState == STATE_UNINSTALL_PAUSE

        fun isTaskUninstallSuccess(taskState: Int): Boolean =
            taskState >= STATE_UNINSTALL_SUCCESS || taskState == AState.STATE_TASK_SUCCESS

        //////////////////////////////////////////////////////////////

        //删除
        const val STATE_DELETE_CREATE = 90
        const val STATE_DELETING = STATE_DELETE_CREATE + AState.STATE_TASKING//92
        const val STATE_DELETE_PAUSE = STATE_DELETE_CREATE + AState.STATE_TASK_PAUSE//93
        const val STATE_DELETE_CANCEL = STATE_DELETE_CREATE + AState.STATE_TASK_CANCEL//97
        const val STATE_DELETE_SUCCESS = STATE_DELETE_CREATE + AState.STATE_TASK_SUCCESS//98
        const val STATE_DELETE_FAIL = STATE_DELETE_CREATE + AState.STATE_TASK_FAIL//99

        @OptIn(OApiInit_InApplication::class)
        fun canTaskDelete(taskState: Int, taskManager: ATaskManager, @AFileExt fileExt: String, @ATaskQueueName taskQueueName: String): Boolean =
            canTaskExecute(
                taskState,
                taskManager,
                fileExt,
                taskQueueName,
                STATE_DELETE_CREATE,
                STATE_DOWNLOAD_CREATE
            )/*taskState in STATE_DOWNLOAD_SUCCESS..STATE_DELETE_PAUSE*/ || AState.canTaskStart(taskState) || AState.canTaskFinish(taskState)

        fun atTaskDelete(taskState: Int): Boolean =
            taskState in STATE_DELETE_CREATE..STATE_DELETE_FAIL

        fun isTaskDeleting(taskState: Int): Boolean =
            taskState == STATE_DELETING

        fun isTaskDeletePause(taskState: Int): Boolean =
            taskState == STATE_DELETE_PAUSE

        fun isTaskDeleteSuccess(taskState: Int): Boolean =
            taskState >= STATE_DELETE_SUCCESS || taskState == AState.STATE_TASK_SUCCESS

        /*//更新
        const val STATE_UPDATE_CREATE = 50//需要更新
        const val STATE_UPDATEING = 51//STATE_NEED_UPDATE = 17//更新中
        const val STATE_UPDATE_SUCCESS = 58
        const val STATE_UPDATE_FAIL = 59*/
    }
}

fun @ATaskState Int.taskState2taskName(): @ATaskName String? =
    ATaskState.taskState2taskName(this)

fun @ATaskState Int.getTaskCode(): Int =
    ATaskState.getTaskCode(this)

fun main() {
    println(48/10*10)
}