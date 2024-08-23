package com.mozhimen.taskk.provider.basic.db

import android.util.Log
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mozhimen.basick.utilk.android.util.UtilKLogWrapper
import com.mozhimen.basick.utilk.commons.IUtilK
import com.mozhimen.basick.utilk.kotlin.getSplitFirstIndexToEnd
import com.mozhimen.basick.utilk.kotlin.getSplitFirstIndexToStart
import com.mozhimen.taskk.provider.basic.annors.ATaskName
import com.mozhimen.taskk.provider.basic.cons.CState
import com.mozhimen.taskk.provider.basic.cons.CTaskState
import com.mozhimen.taskk.provider.basic.utils.TaskProviderUtil

/**
 * @ClassName AppFileParam
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2023/10/11 18:26
 * @Version 1.0
 */
@Entity(tableName = "netk_app_task")
data class AppTask(
    @PrimaryKey
    @ColumnInfo(name = "task_id")
    val taskId: String,//主键
    @ColumnInfo(name = "task_state")
    /**
     * @see CNetKAppTaskState
     */
    var taskState: Int,//下载状态
    @ColumnInfo(name = "task_state_init")
    var taskStateInit: Int = taskState,//下载初始状态(便于出现异常时回落)
    @ColumnInfo(name = "task_update_time")
    var taskUpdateTime: Long = System.currentTimeMillis(),//更新时间

    ////////////////////////////////////////////////////////////////

    @ColumnInfo(name = "downloadId")
    var taskDownloadId: Int = 0,
    @ColumnInfo(name = "download_url_current")
    var taskDownloadUrlCurrent: String,//当前使用的下载地址
    @ColumnInfo(name = "download_url")
    var taskDownloadUrlInside: String = taskDownloadUrlCurrent,//内部下载地址
    @ColumnInfo(name = "download_url_outside")
    var taskDownloadUrlOutside: String = taskDownloadUrlCurrent,//外部下载地址
    @ColumnInfo(name = "download_progress")
    var taskDownloadProgress: Int = 0,//下载进度
    @ColumnInfo(name = "download_file_size")
    var taskDownloadFileSizeOffset: Long = 0,
    @ColumnInfo(name = "apk_file_size")
    var taskDownloadFileSizeTotal: Long = 0,//软件大小
    @ColumnInfo(name = "task_download_file_speed")
    var taskDownloadFileSpeed: Long = 0,

    ////////////////////////////////////////////////////////////////

    @ColumnInfo(name = "apk_verify_need")
    var taskVerifyEnable: Boolean,//是否需要检测0,不需要,1需要
    @ColumnInfo(name = "apk_file_md5")
    val taskVerifyFileMd5: String = "",//文件的MD5值

    ////////////////////////////////////////////////////////////////

    @ColumnInfo(name = "apk_unzip_need")
    var taskUnzipEnable: Boolean,
    @ColumnInfo(name = "task_unzip_file_path")
    var taskUnzipFilePath: String = "",

    ////////////////////////////////////////////////////////////////

    @ColumnInfo(name = "apk_icon_url")
    var fileIconUrl: String,
    @ColumnInfo(name = "apk_icon_Id")
    val fileIconId: Int = 0,
    @ColumnInfo("apk_file_name")
    var fileNameExt: String,//和apkName的区别是有后缀
    @ColumnInfo(name = "apk_name")
    val fileName: String = if (fileNameExt.isNotEmpty() && fileNameExt.contains(".")) fileNameExt.getSplitFirstIndexToStart(".") else "",//本地保存的名称 为appid.apk或appid.npk
    @ColumnInfo(name = "file_ext")
    val fileExt: String = if (fileNameExt.isNotEmpty() && fileNameExt.contains(".")) fileNameExt.getSplitFirstIndexToEnd(".") else "",//文件后缀
    @ColumnInfo(name = "apk_path_name")
    var filePathNameExt: String = "",//本地暂存路径

    ////////////////////////////////////////////////////////////////

    @ColumnInfo(name = "apk_package_name")
    val apkPackageName: String,//包名
    @ColumnInfo(name = "apk_version_code")
    val apkVersionCode: Int,
    @ColumnInfo(name = "apk_version_name")
    val apkVersionName: String/*,
    @ColumnInfo(name = "apk_is_installed")
    var apkIsInstalled: Boolean,//是否安装0未,1安装*/
) : IUtilK {
    constructor(
        taskId: String,//主键
        taskState: Int,//下载状态
        taskDownloadUrlCurrent: String,//当前使用的下载地址
        taskVerifyEnable: Boolean,//是否需要检测0,不需要,1需要
        taskVerifyFileMd5: String,//文件的MD5值
        taskUnzipEnable: Boolean,
        fileIconUrl: String,
        fileIconId: Int,
        fileNameExt: String,//和apkName的区别是有后缀
        apkPackageName: String,//包名
        apkVersionCode: Int,
        apkVersionName: String
    ) : this(
        taskId,
        taskState,
        taskState,
        System.currentTimeMillis(),
        0,
        taskDownloadUrlCurrent,
        taskDownloadUrlCurrent,
        taskDownloadUrlCurrent,
        0,
        0,
        0,
        0,
        taskVerifyEnable,
        taskVerifyFileMd5,
        taskUnzipEnable,
        "",
        fileIconUrl,
        fileIconId,
        fileNameExt,
        if (fileNameExt.isNotEmpty() && fileNameExt.contains(".")) fileNameExt.getSplitFirstIndexToStart(".") else "",
        if (fileNameExt.isNotEmpty() && fileNameExt.contains(".")) fileNameExt.getSplitFirstIndexToEnd(".") else "",
        "",
        apkPackageName,
        apkVersionCode,
        apkVersionName
    )

    fun taskDownloadReset() {
        taskDownloadId = 0
        taskDownloadProgress = 0
        taskDownloadFileSizeOffset = 0
        taskDownloadFileSizeTotal = 0
        taskDownloadFileSpeed = 0
    }

    fun getStrTaskState(): String =
        TaskProviderUtil.intTaskState2strTaskState(taskState)

    fun toNewTaskState(taskStateNew: Int) {
        taskState = taskStateNew
        AppTaskDaoManager.addOrUpdate(this)
    }

    fun getCurrentTaskName(@ATaskName firstTaskName: String = ""): String? {
        val task: Int = taskState / 10//->哪个环节
        val state: Int = taskState % 10
        if (taskState == CState.STATE_TASK_SUCCESS) {
            UtilKLogWrapper.d(TAG, "getCurrentTaskName: task $task state $state taskName null STATE_TASK_SUCCESS")
            return null
        }
        return when (task) {
            CState.STATE_TASK_CREATE -> firstTaskName.ifEmpty { null }
            CTaskState.STATE_DOWNLOAD_CREATE / 10 -> ATaskName.TASK_DOWNLOAD//1
            CTaskState.STATE_VERIFY_CREATE / 10 -> ATaskName.TASK_VERIFY
            CTaskState.STATE_UNZIP_CREATE / 10 -> ATaskName.TASK_UNZIP
            CTaskState.STATE_INSTALL_CREATE / 10 -> ATaskName.TASK_INSTALL
            CTaskState.STATE_OPEN_CREATE / 10 -> ATaskName.TASK_OPEN
            CTaskState.STATE_UNINSTALL_CREATE / 10 -> ATaskName.TASK_UNINSTALL
            else -> null
        }.also { UtilKLogWrapper.d(TAG, "getCurrentTaskName: task $task state $state taskName $it") }
    }

    ////////////////////////////////////////////////////////////

    fun isTaskProcess(): Boolean =
        CState.isTaskProcess(taskState)

    fun isTaskCreate(): Boolean =
        CState.isTaskCreate(taskState)

    fun isTaskUpdate(): Boolean =
        CState.isTaskUpdate(taskState)

    fun isTaskCreateOrUpdate(): Boolean =
        CState.isTaskCreateOrUpdate(taskState)

    fun isTaskUnAvailable(): Boolean =
        CState.isTaskUnAvailable(taskState)

    fun isTaskCancel(): Boolean =
        CState.isTaskCancel(taskState)

    fun isTaskSuccess(): Boolean =
        CState.isTaskSuccess(taskState)

    fun isTaskFail(): Boolean =
        CState.isTaskFail(taskState)

    ////////////////////////////////////////////////////////////

    fun isAnyTasking(): Boolean =
        CState.isAnyTasking(taskState)

    fun isAnyTaskPause(): Boolean =
        CState.isAnyTaskPause(taskState)

    fun isAnyTaskSuccess(): Boolean =
        CState.isAnyTaskSuccess(taskState)

    fun isAnyTaskCancel(): Boolean =
        CState.isAnyTaskCancel(taskState)

    fun isAnyTaskFail(): Boolean =
        CState.isAnyTaskFail(taskState)

    fun isAnyTaskResult(): Boolean =
        CState.isAnyTaskResult(taskState)

    ////////////////////////////////////////////////////////////

    fun canTaskDownload(): Boolean =
        CTaskState.canTaskDownload(taskState)

    fun canTaskVerify(): Boolean =
        CTaskState.canTaskVerify(taskState)

    fun canTaskUnzip(): Boolean =
        CTaskState.canTaskUnzip(taskState)

    fun canTaskInstall(): Boolean =
        CTaskState.canTaskInstall(taskState)

    fun canTaskOpen(): Boolean =
        CTaskState.canTaskOpen(taskState)

    fun canTaskUninstall(): Boolean =
        CTaskState.canTaskUninstall(taskState)

    ////////////////////////////////////////////////////////////

    fun atTaskDownload(): Boolean =
        CTaskState.atTaskDownload(taskState)

    fun atTaskVerify(): Boolean =
        CTaskState.atTaskVerify(taskState)

    fun atTaskUnzip(): Boolean =
        CTaskState.atTaskUnzip(taskState)

    fun atTaskInstall(): Boolean =
        CTaskState.atTaskInstall(taskState)

    fun atTaskOpen(): Boolean =
        CTaskState.atTaskOpen(taskState)

    fun atTaskUninstall(): Boolean =
        CTaskState.atTaskUninstall(taskState)

    ////////////////////////////////////////////////////////////

    fun isTaskDownloading(): Boolean =
        CTaskState.isTaskDownloading(taskState)

    fun isTaskVerifying(): Boolean =
        CTaskState.isTaskVerifying(taskState)

    fun isTaskUnziping(): Boolean =
        CTaskState.isTaskUnziping(taskState)

    fun isTaskInstalling(): Boolean =
        CTaskState.isTaskInstalling(taskState)

    fun isTaskOpening(): Boolean =
        CTaskState.isTaskOpening(taskState)

    fun isTaskUninstalling(): Boolean =
        CTaskState.isTaskUninstalling(taskState)

    ////////////////////////////////////////////////////////////

    fun isTaskDownloadSuccess(): Boolean =
        CTaskState.isTaskDownloadSuccess(taskState)

    fun isTaskVerifySuccess(): Boolean =
        CTaskState.isTaskVerifySuccess(taskState)

    fun isTaskUnzipSuccess(): Boolean =
        CTaskState.isTaskUnzipSuccess(taskState)

    fun isTaskInstallSuccess(): Boolean =
        CTaskState.isTaskInstallSuccess(taskState)

    fun isTaskOpenSuccess(): Boolean =
        CTaskState.isTaskOpenSuccess(taskState)

    fun isTaskUninstallSuccess(): Boolean =
        CTaskState.isTaskUninstallSuccess(taskState)

    ////////////////////////////////////////////////////////////

    override fun toString(): String {
        return "AppTask(taskId='$taskId', taskState=$taskState, taskStateInit=$taskStateInit, taskUpdateTime=$taskUpdateTime, taskDownloadId=$taskDownloadId, taskDownloadProgress=$taskDownloadProgress, taskDownloadFileSize=$taskDownloadFileSizeOffset, taskDownloadFileSizeTotal=$taskDownloadFileSizeTotal, taskVerifyEnable=$taskVerifyEnable, taskVerifyFileMd5='$taskVerifyFileMd5', taskUnzipEnable=$taskUnzipEnable, fileIconUrl='$fileIconUrl', fileIconId=$fileIconId, fileName='$fileName', fileExt='$fileExt', fileNameExt='$fileNameExt', filePathNameExt='$filePathNameExt', apkPackageName='$apkPackageName', apkVersionCode=$apkVersionCode, apkVersionName='$apkVersionName', taskDownloadUrlCurrent='$taskDownloadUrlCurrent', taskDownloadUrlInside='$taskDownloadUrlInside', taskDownloadUrlOutside='$taskDownloadUrlOutside')"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AppTask

        if (taskId != other.taskId) return false
        if (taskState != other.taskState) return false
        if (taskStateInit != other.taskStateInit) return false
        if (taskUpdateTime != other.taskUpdateTime) return false
        if (taskDownloadId != other.taskDownloadId) return false
        if (taskDownloadUrlCurrent != other.taskDownloadUrlCurrent) return false
        if (taskDownloadUrlInside != other.taskDownloadUrlInside) return false
        if (taskDownloadUrlOutside != other.taskDownloadUrlOutside) return false
        if (taskDownloadProgress != other.taskDownloadProgress) return false
        if (taskDownloadFileSizeOffset != other.taskDownloadFileSizeOffset) return false
        if (taskDownloadFileSizeTotal != other.taskDownloadFileSizeTotal) return false
        if (taskVerifyEnable != other.taskVerifyEnable) return false
        if (taskVerifyFileMd5 != other.taskVerifyFileMd5) return false
        if (taskUnzipEnable != other.taskUnzipEnable) return false
        if (fileIconUrl != other.fileIconUrl) return false
        if (fileIconId != other.fileIconId) return false
        if (fileName != other.fileName) return false
        if (fileExt != other.fileExt) return false
        if (fileNameExt != other.fileNameExt) return false
        if (filePathNameExt != other.filePathNameExt) return false
        if (apkPackageName != other.apkPackageName) return false
        if (apkVersionCode != other.apkVersionCode) return false
        if (apkVersionName != other.apkVersionName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = taskId.hashCode()
        result = 31 * result + taskState
        result = 31 * result + taskStateInit
        result = 31 * result + taskUpdateTime.hashCode()
        result = 31 * result + taskDownloadId
        result = 31 * result + taskDownloadUrlCurrent.hashCode()
        result = 31 * result + taskDownloadUrlInside.hashCode()
        result = 31 * result + taskDownloadUrlOutside.hashCode()
        result = 31 * result + taskDownloadProgress
        result = 31 * result + taskDownloadFileSizeOffset.hashCode()
        result = 31 * result + taskDownloadFileSizeTotal.hashCode()
        result = 31 * result + taskVerifyEnable.hashCode()
        result = 31 * result + taskVerifyFileMd5.hashCode()
        result = 31 * result + taskUnzipEnable.hashCode()
        result = 31 * result + fileIconUrl.hashCode()
        result = 31 * result + fileIconId
        result = 31 * result + fileName.hashCode()
        result = 31 * result + fileExt.hashCode()
        result = 31 * result + fileNameExt.hashCode()
        result = 31 * result + filePathNameExt.hashCode()
        result = 31 * result + apkPackageName.hashCode()
        result = 31 * result + apkVersionCode
        result = 31 * result + apkVersionName.hashCode()
        return result
    }

    //    fun isTaskWait(): Boolean =
//        !apkIsInstalled && CNetKAppTaskState.isTaskWait(taskState)
}