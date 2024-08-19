package com.mozhimen.taskk.task.provider.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mozhimen.taskk.task.provider.cons.CState
import com.mozhimen.taskk.task.provider.cons.CTaskState

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
    var downloadId: Int = 0,
    @ColumnInfo(name = "download_url_current")
    var taskDownloadUrlCurrent: String,//当前使用的下载地址
    @ColumnInfo(name = "download_url")
    var taskDownloadUrlInside: String,//内部下载地址
    @ColumnInfo(name = "download_url_outside")
    var taskDownloadUrlOutside: String,//外部下载地址
    @ColumnInfo(name = "download_progress")
    var taskDownloadProgress: Int,//下载进度
    @ColumnInfo(name = "download_file_size")
    var taskDownloadFileSize: Long,
    @ColumnInfo(name = "apk_file_size")
    var taskDownloadFileSizeTotal: Long,//软件大小

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
    fun isTaskProcess(): Boolean =
        !apkIsInstalled && CState.isTaskProcess(taskState)

//    fun isTaskWait(): Boolean =
//        !apkIsInstalled && CNetKAppTaskState.isTaskWait(taskState)

    fun isTasking(): Boolean =
        !apkIsInstalled && CState.isTasking(taskState)

    fun isTaskPause(): Boolean =
        !apkIsInstalled && CState.isTaskPause(taskState)

    fun isTaskCancel(): Boolean =
        !apkIsInstalled && CState.isTaskCancel(taskState)

    ////////////////////////////////////////////////////////////

    fun isTaskDownload(): Boolean =
        !apkIsInstalled && CTaskState.isTaskDownload(taskState)

    fun isTaskVerify(): Boolean =
        !apkIsInstalled && CTaskState.isTaskVerify(taskState)

    fun isTaskUnzip(): Boolean =
        !apkIsInstalled && CTaskState.isTaskUnzip(taskState)

    fun isTaskInstall(): Boolean =
        !apkIsInstalled && CTaskState.isTaskInstall(taskState)

    ////////////////////////////////////////////////////////////

    fun isDownloading(): Boolean =
        !apkIsInstalled && CTaskState.isDownloading(taskState)

    fun isUnzipSuccess(): Boolean =
        !apkIsInstalled && CTaskState.isUnzipSuccess(taskState)

    ////////////////////////////////////////////////////////////

    fun canInstall(): Boolean =
        CTaskState.canInstall(taskState)

    fun isInstalled(): Boolean =
        CTaskState.isInstalled(taskState) || CTaskState.isInstalled(taskState) || apkIsInstalled

    ////////////////////////////////////////////////////////////

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AppTask) return false

        if (taskId != other.taskId) return false
        if (taskState != other.taskState) return false
        if (taskDownloadUrlInside != other.taskDownloadUrlInside) return false
        if (taskDownloadUrlOutside != other.taskDownloadUrlOutside) return false
        if (taskDownloadUrlCurrent != other.taskDownloadUrlCurrent) return false
        if (taskDownloadProgress != other.taskDownloadProgress) return false
        if (taskDownloadFileSize != other.taskDownloadFileSize) return false
        if (taskDownloadFileSizeTotal != other.taskDownloadFileSizeTotal) return false
        if (taskVerifyFileMd5 != other.taskVerifyFileMd5) return false
        if (apkPackageName != other.apkPackageName) return false
        if (fileName != other.fileName) return false
        if (apkVersionCode != other.apkVersionCode) return false
        if (apkVersionName != other.apkVersionName) return false
        if (fileIconUrl != other.fileIconUrl) return false
        if (fileIconId != other.fileIconId) return false
        if (fileNameExt != other.fileNameExt) return false
        if (filePathNameExt != other.filePathNameExt) return false
        if (apkIsInstalled != other.apkIsInstalled) return false
        if (taskVerifyEnable != other.taskVerifyEnable) return false
        if (taskUnzipEnable != other.taskUnzipEnable) return false
        if (taskUpdateTime != other.taskUpdateTime) return false
        return downloadId == other.downloadId
    }

    override fun hashCode(): Int {
        var result = taskId.hashCode()
        result = 31 * result + taskState
        result = 31 * result + taskDownloadUrlInside.hashCode()
        result = 31 * result + taskDownloadUrlOutside.hashCode()
        result = 31 * result + taskDownloadUrlCurrent.hashCode()
        result = 31 * result + taskDownloadProgress
        result = 31 * result + taskDownloadFileSize.hashCode()
        result = 31 * result + taskDownloadFileSizeTotal.hashCode()
        result = 31 * result + taskVerifyFileMd5.hashCode()
        result = 31 * result + apkPackageName.hashCode()
        result = 31 * result + fileName.hashCode()
        result = 31 * result + apkVersionCode
        result = 31 * result + apkVersionName.hashCode()
        result = 31 * result + fileIconUrl.hashCode()
        result = 31 * result + fileIconId
        result = 31 * result + fileNameExt.hashCode()
        result = 31 * result + filePathNameExt.hashCode()
        result = 31 * result + apkIsInstalled.hashCode()
        result = 31 * result + taskVerifyEnable.hashCode()
        result = 31 * result + taskUnzipEnable.hashCode()
        result = 31 * result + taskUpdateTime.hashCode()
        result = 31 * result + downloadId
        return result
    }

    override fun toString(): String {
        return "AppTask(taskId='$taskId', taskState=$taskState, downloadProgress=$taskDownloadProgress, downloadFileSize=$taskDownloadFileSize, apkFileSize=$taskDownloadFileSizeTotal, apkFileMd5='$taskVerifyFileMd5', apkPackageName='$apkPackageName', apkName='$fileName', apkVersionCode=$apkVersionCode, apkVersionName='$apkVersionName', apkIconUrl='$fileIconUrl', apkIconId=$fileIconId, apkFileName='$fileNameExt', apkPathName='$filePathNameExt', apkIsInstalled=$apkIsInstalled, apkVerifyNeed=$taskVerifyEnable, apkUnzipNeed=$taskUnzipEnable, taskUpdateTime=$taskUpdateTime, downloadId=$downloadId, downloadUrlOutSide='$taskDownloadUrlOutside', downloadUrlCurrent='$taskDownloadUrlCurrent')"
    }
}