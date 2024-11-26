package com.mozhimen.taskk.provider.basic.db

import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mozhimen.kotlin.elemk.commons.IHasId
import com.mozhimen.kotlin.lintk.optins.OApiInit_InApplication
import com.mozhimen.kotlin.utilk.android.util.UtilKLogWrapper
import com.mozhimen.kotlin.utilk.commons.IUtilK
import com.mozhimen.kotlin.utilk.kotlin.getSplitFirstIndexToEnd
import com.mozhimen.kotlin.utilk.kotlin.getSplitFirstIndexToStart
import com.mozhimen.taskk.provider.basic.annors.AState
import com.mozhimen.taskk.provider.basic.annors.ATaskNodeQueueName
import com.mozhimen.taskk.provider.basic.annors.ATaskState
import com.mozhimen.taskk.provider.basic.bases.ATaskManager
import com.mozhimen.taskk.provider.basic.cons.STaskNode
import java.io.Serializable

/**
 * @ClassName AppFileParam
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2023/10/11 18:26
 * @Version 1.0
 */
@Entity(tableName = "netk_app_task")
data class AppTask constructor(
    @PrimaryKey
    @ColumnInfo(name = "task_id")
    override val id: String,//主键
    @ColumnInfo(name = "task_state")
    var taskState: Int,//下载状态
    @ColumnInfo(name = "task_state_init")
    var taskStateInit: Int = taskState,//下载初始状态(便于出现异常时回落)
    @ColumnInfo(name = "task_name")
    var taskName: String,
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
    var taskDownloadFileSizeOffset: Long = 0,//下载的大小
    @ColumnInfo(name = "apk_file_size")
    var taskDownloadFileSizeTotal: Long = 0,//软件大小
    @ColumnInfo(name = "task_download_file_speed")
    var taskDownloadFileSpeed: Long = 0,

    ////////////////////////////////////////////////////////////////

    @ColumnInfo(name = "apk_verify_need")
    var taskVerifyEnable: Boolean,//是否需要检测0,不需要,1需要
    @ColumnInfo(name = "apk_file_md5")
    var taskVerifyFileMd5: String = "",//文件的MD5值

    ////////////////////////////////////////////////////////////////

    @ColumnInfo(name = "apk_unzip_need")
    var taskUnzipEnable: Boolean,
    @ColumnInfo(name = "task_unzip_file_path")
    var taskUnzipFilePath: String = "",

    ////////////////////////////////////////////////////////////////

    @ColumnInfo(name = "apk_icon_url")
    var fileIconUrl: String,
    @ColumnInfo(name = "apk_icon_Id")
    var fileIconId: Int = 0,
    @ColumnInfo("apk_file_name")
    var fileNameExt: String,//和apkName的区别是有后缀
    @ColumnInfo(name = "apk_name")
    var fileName: String = if (fileNameExt.isNotEmpty() && fileNameExt.contains(".")) fileNameExt.getSplitFirstIndexToStart(".") else "",//本地保存的名称 为appid.apk或appid.npk
    @ColumnInfo(name = "file_ext")
    var fileExt: String = if (fileNameExt.isNotEmpty() && fileNameExt.contains(".")) fileNameExt.getSplitFirstIndexToEnd(".") else "",//文件后缀
    @ColumnInfo(name = "apk_path_name")
    var filePathNameExt: String = "",//本地暂存路径

    ////////////////////////////////////////////////////////////////

    @ColumnInfo(name = "apk_package_name")
    var apkPackageName: String,//包名
    @ColumnInfo(name = "apk_version_code")
    var apkVersionCode: Int,
    @ColumnInfo(name = "apk_version_name")
    var apkVersionName: String,
    /*,
    @ColumnInfo(name = "apk_is_installed")
    var apkIsInstalled: Boolean,//是否安装0未,1安装*/
) : IUtilK, IHasId, Serializable, Parcelable {

    //for compose
    constructor() : this("", AState.STATE_TASK_CREATE, "taskName taskName taskName taskName taskName taskName taskName", "", false, "", false, "", "")

    //for apk
    constructor(
        taskId: String,//主键
        taskState: Int,//下载状态
        taskName: String,
        taskDownloadUrlCurrent: String,//当前使用的下载地址
        taskVerifyEnable: Boolean,//是否需要检测0,不需要,1需要
        taskVerifyFileMd5: String,//文件的MD5值
        taskUnzipEnable: Boolean,
        fileIconUrl: String,
        fileIconId: Int,
        fileNameExt: String,//和apkName的区别是有后缀
        apkPackageName: String,//包名
        apkVersionCode: Int,
        apkVersionName: String,
    ) : this(
        taskId,
        taskState,
        taskState,
        taskName,
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

    //for zip
    constructor(
        taskId: String,//主键
        taskState: Int,//下载状态
        taskName: String,
        taskDownloadUrlCurrent: String,//当前使用的下载地址
        taskVerifyEnable: Boolean,//是否需要检测0,不需要,1需要
        taskVerifyFileMd5: String,//文件的MD5值
        taskUnzipEnable: Boolean,
        fileNameExt: String,//和apkName的区别是有后缀
        apkPackageName: String,//包名
    ) : this(
        taskId,
        taskState,
        taskState,
        taskName,
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
        "",
        0,
        fileNameExt,
        if (fileNameExt.isNotEmpty() && fileNameExt.contains(".")) fileNameExt.getSplitFirstIndexToStart(".") else "",
        if (fileNameExt.isNotEmpty() && fileNameExt.contains(".")) fileNameExt.getSplitFirstIndexToEnd(".") else "",
        "",
        apkPackageName,
        0,
        ""
    )

    //for parcel
    constructor(parcel: Parcel) : this(
        parcel.readString()?:"",
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString()?:"",
        parcel.readLong(),
        parcel.readInt(),
        parcel.readString()?:"",
        parcel.readString()?:"",
        parcel.readString()?:"",
        parcel.readInt(),
        parcel.readLong(),
        parcel.readLong(),
        parcel.readLong(),
        parcel.readByte() != 0.toByte(),
        parcel.readString()?:"",
        parcel.readByte() != 0.toByte(),
        parcel.readString()?:"",
        parcel.readString()?:"",
        parcel.readInt(),
        parcel.readString()?:"",
        parcel.readString()?:"",
        parcel.readString()?:"",
        parcel.readString()?:"",
        parcel.readString()?:"",
        parcel.readInt(),
        parcel.readString()?:""
    ) {
    }

    ////////////////////////////////////////////////////////////////

    fun taskDownloadReset() {
        taskDownloadId = 0
        taskDownloadProgress = 0
        taskDownloadFileSizeOffset = 0
        taskDownloadFileSpeed = 0
    }

    fun toTaskStateNew(taskStateNew: Int) {
        taskState = taskStateNew
        if (isTaskCreateOrUpdate())
            taskStateInit = taskState
        AppTaskDaoManager.addOrUpdate(this)
    }

    ////////////////////////////////////////////////////////////////

    fun getTaskStateStr(): String =
        ATaskState.intTaskState2strTaskState(taskState)

    @OptIn(OApiInit_InApplication::class)
    fun getCurrentTaskNode(taskManager: ATaskManager, @ATaskNodeQueueName taskNodeQueueName: String, firstTaskNode_ofTaskNodeQueue: STaskNode? = null): STaskNode? {
        val taskCode: Int = getTaskCode()//->哪个环节
        val stateCode: Int = getStateCode()//->哪个状态
        if (isTaskSuccess(taskManager, taskNodeQueueName)) {
            UtilKLogWrapper.d(TAG, "getCurrentTaskName: task $taskCode state $stateCode taskName null STATE_TASK_SUCCESS")
            return null
        }
        return when (taskCode) {
            AState.STATE_TASK_CREATE -> firstTaskNode_ofTaskNodeQueue.also { if (it == null) Log.e(TAG, "getCurrentTaskName: firstTaskName ifEmpty") }
            ATaskState.STATE_DOWNLOAD_CREATE -> STaskNode.TaskNodeDownload//ATaskName.TASK_DOWNLOAD//1
            ATaskState.STATE_VERIFY_CREATE -> STaskNode.TaskNodeVerify//ATaskName.TASK_VERIFY
            ATaskState.STATE_UNZIP_CREATE -> STaskNode.TaskNodeUnzip//ATaskName.TASK_UNZIP
            ATaskState.STATE_INSTALL_CREATE -> STaskNode.TaskNodeInstall//ATaskName.TASK_INSTALL
            ATaskState.STATE_OPEN_CREATE -> STaskNode.TaskNodeOpen//ATaskName.TASK_OPEN
            ATaskState.STATE_CLOSE_CREATE -> STaskNode.TaskNodeClose//ATaskName.TASK_CLOSE
            ATaskState.STATE_UNINSTALL_CREATE -> STaskNode.TaskNodeUninstall//ATaskName.TASK_UNINSTALL
            ATaskState.STATE_DELETE_CREATE -> STaskNode.TaskNodeDelete//ATaskName.TASK_DELETE
            else -> null
        }.also { UtilKLogWrapper.d(TAG, "getCurrentTaskName: task $taskCode state $stateCode taskName $it") }
    }

    ////////////////////////////////////////////////////////////////

    fun getTaskCode(): @ATaskState Int =
        ATaskState.getTaskCode(taskState)

    fun getStateCode(): @AState Int =
        AState.getStateCode(taskState)

    ////////////////////////////////////////////////////////////////

    @OptIn(OApiInit_InApplication::class)
    fun canTaskStart(taskManager: ATaskManager, @ATaskNodeQueueName taskNodeQueueName: String): Boolean {
        return taskManager.canTaskStart(this, taskNodeQueueName)
    }

    @OptIn(OApiInit_InApplication::class)
    fun canTaskResume(taskManager: ATaskManager, @ATaskNodeQueueName taskNodeQueueName: String): Boolean {
        return taskManager.canTaskResume(this, taskNodeQueueName)
    }

    @OptIn(OApiInit_InApplication::class)
    fun canTaskPause(taskManager: ATaskManager, @ATaskNodeQueueName taskNodeQueueName: String): Boolean {
        return taskManager.canTaskPause(this, taskNodeQueueName)
    }

    @OptIn(OApiInit_InApplication::class)
    fun canTaskCancel(taskManager: ATaskManager, @ATaskNodeQueueName taskNodeQueueName: String): Boolean {
        return taskManager.canTaskCancel(this, taskNodeQueueName)
    }

    ////////////////////////////////////////////////////////////

    @OptIn(OApiInit_InApplication::class)
    fun isTaskProcess(taskManager: ATaskManager, @ATaskNodeQueueName taskNodeQueueName: String): Boolean =
        AState.isTaskProcess(taskState, taskManager, fileExt, taskNodeQueueName)

    fun isTaskProcess(): Boolean =
        AState.isTaskProcess(taskState)

    fun isTaskCreate(): Boolean =
        AState.isTaskCreate(taskState)

    fun isTaskUpdate(): Boolean =
        AState.isTaskUpdate(taskState)

    fun isTaskCreateOrUpdate(): Boolean =
        AState.isTaskCreateOrUpdate(taskState)

    fun isTaskUnAvailable(): Boolean =
        AState.isTaskUnAvailable(taskState)

    fun isTaskCancel(): Boolean =
        AState.isTaskCancel(taskState)

    @OptIn(OApiInit_InApplication::class)
    fun isTaskSuccess(taskManager: ATaskManager, @ATaskNodeQueueName taskNodeQueueName: String): Boolean =
        AState.isTaskSuccess(taskState, taskManager, fileExt, taskNodeQueueName)

    fun isTaskSuccess(): Boolean =
        AState.isTaskSuccess(taskState)

    fun isTaskFail(): Boolean =
        AState.isTaskFail(taskState)

    ////////////////////////////////////////////////////////////

    fun isAnyTasking(): Boolean =
        AState.isAnyTasking(taskState)

    fun isAnyTaskPause(): Boolean =
        AState.isAnyTaskPause(taskState)

    fun isAnyTaskSuccess(): Boolean =
        AState.isAnyTaskSuccess(taskState)

    fun isAnyTaskCancel(): Boolean =
        AState.isAnyTaskCancel(taskState)

    fun isAnyTaskFail(): Boolean =
        AState.isAnyTaskFail(taskState)

    fun isAnyTaskResult(): Boolean =
        AState.isAnyTaskResult(taskState)

    ////////////////////////////////////////////////////////////

    @OptIn(OApiInit_InApplication::class)
    fun canTaskDownload(taskManager: ATaskManager, @ATaskNodeQueueName taskNodeQueueName: String): Boolean =
        ATaskState.canTaskDownload(taskState, taskManager, fileExt, taskNodeQueueName)

    @OptIn(OApiInit_InApplication::class)
    fun canTaskVerify(taskManager: ATaskManager, @ATaskNodeQueueName taskNodeQueueName: String): Boolean =
        ATaskState.canTaskVerify(taskState, taskManager, fileExt, taskNodeQueueName)

    @OptIn(OApiInit_InApplication::class)
    fun canTaskUnzip(taskManager: ATaskManager, @ATaskNodeQueueName taskNodeQueueName: String): Boolean =
        ATaskState.canTaskUnzip(taskState, taskManager, fileExt, taskNodeQueueName)

    @OptIn(OApiInit_InApplication::class)
    fun canTaskInstall(taskManager: ATaskManager, @ATaskNodeQueueName taskNodeQueueName: String): Boolean =
        ATaskState.canTaskInstall(taskState, taskManager, fileExt, taskNodeQueueName)

    @OptIn(OApiInit_InApplication::class)
    fun canTaskOpen(taskManager: ATaskManager, @ATaskNodeQueueName taskNodeQueueName: String): Boolean =
        ATaskState.canTaskOpen(taskState, taskManager, fileExt, taskNodeQueueName)

    @OptIn(OApiInit_InApplication::class)
    fun canTaskClose(taskManager: ATaskManager, @ATaskNodeQueueName taskNodeQueueName: String): Boolean =
        ATaskState.canTaskClose(taskState, taskManager, fileExt, taskNodeQueueName)

    @OptIn(OApiInit_InApplication::class)
    fun canTaskUninstall(taskManager: ATaskManager, @ATaskNodeQueueName taskNodeQueueName: String): Boolean =
        ATaskState.canTaskUninstall(taskState, taskManager, fileExt, taskNodeQueueName)

    @OptIn(OApiInit_InApplication::class)
    fun canTaskDelete(taskManager: ATaskManager, @ATaskNodeQueueName taskNodeQueueName: String): Boolean =
        ATaskState.canTaskDelete(taskState, taskManager, fileExt, taskNodeQueueName)

    ////////////////////////////////////////////////////////////

    fun atTaskDownload(): Boolean =
        ATaskState.atTaskDownload(taskState)

    fun atTaskVerify(): Boolean =
        ATaskState.atTaskVerify(taskState)

    fun atTaskUnzip(): Boolean =
        ATaskState.atTaskUnzip(taskState)

    fun atTaskInstall(): Boolean =
        ATaskState.atTaskInstall(taskState)

    fun atTaskOpen(): Boolean =
        ATaskState.atTaskOpen(taskState)

    fun atTaskClose(): Boolean =
        ATaskState.atTaskClose(taskState)

    fun atTaskUninstall(): Boolean =
        ATaskState.atTaskUninstall(taskState)

    fun atTaskDelete(): Boolean =
        ATaskState.atTaskDelete(taskState)

    ////////////////////////////////////////////////////////////

    fun isTaskDownloading(): Boolean =
        ATaskState.isTaskDownloading(taskState)

    fun isTaskVerifying(): Boolean =
        ATaskState.isTaskVerifying(taskState)

    fun isTaskUnziping(): Boolean =
        ATaskState.isTaskUnziping(taskState)

    fun isTaskInstalling(): Boolean =
        ATaskState.isTaskInstalling(taskState)

    fun isTaskOpening(): Boolean =
        ATaskState.isTaskOpening(taskState)

    fun isTaskClosing(): Boolean =
        ATaskState.isTaskClosing(taskState)

    fun isTaskUninstalling(): Boolean =
        ATaskState.isTaskUninstalling(taskState)

    fun isTaskDeleting(): Boolean =
        ATaskState.isTaskDeleting(taskState)

    ////////////////////////////////////////////////////////////

    fun isTaskDownloadPause(): Boolean =
        ATaskState.isTaskDownloadPause(taskState)

    fun isTaskVerifyPause(): Boolean =
        ATaskState.isTaskVerifyPause(taskState)

    fun isTaskUnzipPause(): Boolean =
        ATaskState.isTaskUnzipPause(taskState)

    fun isTaskInstallPause(): Boolean =
        ATaskState.isTaskInstallPause(taskState)

    fun isTaskOpenPause(): Boolean =
        ATaskState.isTaskOpenPause(taskState)

    fun isTaskClosePause(): Boolean =
        ATaskState.isTaskClosePause(taskState)

    fun isTaskUninstallPause(): Boolean =
        ATaskState.isTaskUninstallPause(taskState)

    fun isTaskDeletePause(): Boolean =
        ATaskState.isTaskDeletePause(taskState)

    ////////////////////////////////////////////////////////////

    fun isTaskDownloadSuccess(): Boolean =
        ATaskState.isTaskDownloadSuccess(taskState)

    fun isTaskVerifySuccess(): Boolean =
        ATaskState.isTaskVerifySuccess(taskState)

    fun isTaskUnzipSuccess(): Boolean =
        ATaskState.isTaskUnzipSuccess(taskState)

    fun isTaskInstallSuccess(): Boolean =
        ATaskState.isTaskInstallSuccess(taskState)

    fun isTaskOpenSuccess(): Boolean =
        ATaskState.isTaskOpenSuccess(taskState)

    fun isTaskCloseSuccess(): Boolean =
        ATaskState.isTaskCloseSuccess(taskState)

    fun isTaskUninstallSuccess(): Boolean =
        ATaskState.isTaskUninstallSuccess(taskState)

    fun isTaskDeleteSuccess(): Boolean =
        ATaskState.isTaskDeleteSuccess(taskState)

    ////////////////////////////////////////////////////////////

    fun copyOf(appTask: AppTask){
        taskState = appTask.taskState
        taskStateInit = appTask.taskStateInit
        taskName = appTask.taskName
        taskUpdateTime = appTask.taskUpdateTime
        taskDownloadId = appTask.taskDownloadId
        taskDownloadUrlCurrent = appTask.taskDownloadUrlCurrent
        taskDownloadUrlInside = appTask.taskDownloadUrlInside
        taskDownloadUrlOutside = appTask.taskDownloadUrlOutside
        taskDownloadProgress = appTask.taskDownloadProgress
        taskDownloadFileSizeOffset = appTask.taskDownloadFileSizeOffset
        taskDownloadFileSizeTotal = appTask.taskDownloadFileSizeTotal
        taskDownloadFileSpeed = appTask.taskDownloadFileSpeed
        taskVerifyEnable = appTask.taskVerifyEnable
        taskVerifyFileMd5 = appTask.taskVerifyFileMd5
        taskUnzipEnable = appTask.taskUnzipEnable
        taskUnzipFilePath = appTask.taskUnzipFilePath
        fileIconUrl = appTask.fileIconUrl
        fileIconId = appTask.fileIconId
        fileNameExt = appTask.fileNameExt
        fileName = appTask.fileName
        fileExt = appTask.fileExt
        filePathNameExt = appTask.filePathNameExt
        apkPackageName = appTask.apkPackageName
        apkVersionCode = appTask.apkVersionCode
        apkVersionName = appTask.apkVersionName
    }

    ////////////////////////////////////////////////////////////

    override fun toString(): String {
        return "AppTask(taskId='$id', taskState=$taskState, taskName=${taskName}, taskStateInit=$taskStateInit, taskUpdateTime=$taskUpdateTime, taskDownloadId=$taskDownloadId, taskDownloadProgress=$taskDownloadProgress, taskDownloadFileSize=$taskDownloadFileSizeOffset, taskDownloadFileSizeTotal=$taskDownloadFileSizeTotal, taskVerifyEnable=$taskVerifyEnable, taskVerifyFileMd5='$taskVerifyFileMd5', taskUnzipEnable=$taskUnzipEnable, fileIconUrl='$fileIconUrl', fileIconId=$fileIconId, fileName='$fileName', fileExt='$fileExt', fileNameExt='$fileNameExt', filePathNameExt='$filePathNameExt', apkPackageName='$apkPackageName', apkVersionCode=$apkVersionCode, apkVersionName='$apkVersionName', taskDownloadUrlCurrent='$taskDownloadUrlCurrent', taskDownloadUrlInside='$taskDownloadUrlInside', taskDownloadUrlOutside='$taskDownloadUrlOutside')"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AppTask

        if (id != other.id) return false
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
        var result = id.hashCode()
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

    ////////////////////////////////////////////////////////////

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeInt(taskState)
        parcel.writeInt(taskStateInit)
        parcel.writeString(taskName)
        parcel.writeLong(taskUpdateTime)
        parcel.writeInt(taskDownloadId)
        parcel.writeString(taskDownloadUrlCurrent)
        parcel.writeString(taskDownloadUrlInside)
        parcel.writeString(taskDownloadUrlOutside)
        parcel.writeInt(taskDownloadProgress)
        parcel.writeLong(taskDownloadFileSizeOffset)
        parcel.writeLong(taskDownloadFileSizeTotal)
        parcel.writeLong(taskDownloadFileSpeed)
        parcel.writeByte(if (taskVerifyEnable) 1 else 0)
        parcel.writeString(taskVerifyFileMd5)
        parcel.writeByte(if (taskUnzipEnable) 1 else 0)
        parcel.writeString(taskUnzipFilePath)
        parcel.writeString(fileIconUrl)
        parcel.writeInt(fileIconId)
        parcel.writeString(fileNameExt)
        parcel.writeString(fileName)
        parcel.writeString(fileExt)
        parcel.writeString(filePathNameExt)
        parcel.writeString(apkPackageName)
        parcel.writeInt(apkVersionCode)
        parcel.writeString(apkVersionName)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AppTask> {
        override fun createFromParcel(parcel: Parcel): AppTask {
            return AppTask(parcel)
        }

        override fun newArray(size: Int): Array<AppTask?> {
            return arrayOfNulls(size)
        }
    }
}