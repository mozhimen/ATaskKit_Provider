package com.mozhimen.taskk.task.provider.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mozhimen.taskk.task.provider.cons.CState
import com.mozhimen.taskk.task.provider.cons.CTaskState
import com.mozhimen.taskk.task.provider.utils.TaskProviderUtil

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
    var taskDownloadUrlInside: String,//内部下载地址
    @ColumnInfo(name = "download_url_outside")
    var taskDownloadUrlOutside: String,//外部下载地址
    @ColumnInfo(name = "download_progress")
    var taskDownloadProgress: Int,//下载进度
    @ColumnInfo(name = "download_file_size")
    var taskDownloadFileSizeOffset: Long,
    @ColumnInfo(name = "apk_file_size")
    var taskDownloadFileSizeTotal: Long,//软件大小
    @ColumnInfo(name = "task_download_file_speed")
    var taskDownloadFileSpeed: Long,

    ////////////////////////////////////////////////////////////////

    @ColumnInfo(name = "apk_verify_need")
    var taskVerifyEnable: Boolean,//是否需要检测0,不需要,1需要
    @ColumnInfo(name = "apk_file_md5")
    val taskVerifyFileMd5: String,//文件的MD5值

    ////////////////////////////////////////////////////////////////

    @ColumnInfo(name = "apk_unzip_need")
    var taskUnzipEnable: Boolean,

    ////////////////////////////////////////////////////////////////

    @ColumnInfo(name = "apk_icon_url")
    var fileIconUrl: String,
    @ColumnInfo(name = "apk_icon_Id")
    val fileIconId: Int,
    @ColumnInfo(name = "apk_name")
    val fileName: String,//本地保存的名称 为appid.apk或appid.npk
    @ColumnInfo(name = "file_ext")
    val fileExt: String,//文件后缀
    @ColumnInfo("apk_file_name")
    var fileNameExt: String,//和apkName的区别是有后缀
    @ColumnInfo(name = "apk_path_name")
    var filePathNameExt: String,//本地暂存路径

    ////////////////////////////////////////////////////////////////

    @ColumnInfo(name = "apk_package_name")
    val apkPackageName: String,//包名
    @ColumnInfo(name = "apk_version_code")
    val apkVersionCode: Int,
    @ColumnInfo(name = "apk_version_name")
    val apkVersionName: String/*,
    @ColumnInfo(name = "apk_is_installed")
    var apkIsInstalled: Boolean,//是否安装0未,1安装*/
) {
    fun taskDownloadReset() {
        taskDownloadId = 0
        taskDownloadProgress = 0
        taskDownloadFileSizeOffset = 0
        taskDownloadFileSizeTotal = 0
        taskDownloadFileSpeed = 0
    }

    fun getStrTaskState(): String =
        TaskProviderUtil.intTaskState2strTaskState(taskState)

    ////////////////////////////////////////////////////////////

    fun isTaskProcess(): Boolean =
        /*!apkIsInstalled &&*/ CState.isTaskProcess(taskState)

//    fun isTaskWait(): Boolean =
//        !apkIsInstalled && CNetKAppTaskState.isTaskWait(taskState)

    fun isTasking(): Boolean =
        /*!apkIsInstalled &&*/ CState.isTasking(taskState)

    fun isTaskPause(): Boolean =
        /*!apkIsInstalled &&*/ CState.isTaskPause(taskState)

    fun isTaskSuccess(): Boolean =
        CState.isTaskSuccess(taskState)

    fun isTaskCancel(): Boolean =
        CState.isTaskCancel(taskState)

    fun isTaskFail(): Boolean =
        CState.isTaskFail(taskState)

    ////////////////////////////////////////////////////////////

    fun canTaskDownload(): Boolean =
        CTaskState.canTaskDownload(taskState)

    fun atTaskDownload(): Boolean =
        /*!apkIsInstalled &&*/ CTaskState.atTaskDownload(taskState)

    fun isTaskDownloading(): Boolean =
        /*!apkIsInstalled &&*/ CTaskState.isTaskDownloading(taskState)

    fun isTaskDownloadSuccess(): Boolean =
        CTaskState.isTaskDownloadSuccess(taskState)

    ////////////////////////////////////////////////////////////

    fun canTaskVerify(): Boolean =
        CTaskState.canTaskVerify(taskState)

    fun atTaskVerify(): Boolean =
        /*!apkIsInstalled &&*/ CTaskState.atTaskVerify(taskState)

    fun isTaskVerifying(): Boolean =
        CTaskState.isTaskVerifying(taskState)

    fun isTaskVerifySuccess(): Boolean =
        CTaskState.isTaskVerifySuccess(taskState)

    ////////////////////////////////////////////////////////////

    fun canTaskUnzip(): Boolean =
        CTaskState.canTaskUnzip(taskState)

    fun atTaskUnzip(): Boolean =
        /*!apkIsInstalled &&*/ CTaskState.atTaskUnzip(taskState)

    fun isTaskUnziping(): Boolean =
        CTaskState.isTaskUnziping(taskState)

    fun isTaskUnzipSuccess(): Boolean =
        CTaskState.isTaskUnzipSuccess(taskState)

    ////////////////////////////////////////////////////////////

    fun canTaskInstall(): Boolean =
        CTaskState.canTaskInstall(taskState)

    fun atTaskInstall(): Boolean =
        /*!apkIsInstalled &&*/ CTaskState.atTaskInstall(taskState)

    fun isTaskInstalling(): Boolean =
        CTaskState.isTaskInstalling(taskState)

    fun isTaskInstallSuccess(): Boolean =
        CTaskState.isTaskInstallSuccess(taskState)

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
}