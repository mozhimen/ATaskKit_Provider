package com.mozhimen.taskk.provider.basic.commons

import com.mozhimen.taskk.provider.basic.annors.ATaskNodeQueueName
import com.mozhimen.taskk.provider.basic.cons.STaskFinishType
import com.mozhimen.taskk.provider.basic.db.AppTask
import com.mozhimen.taskk.provider.basic.impls.TaskException

/**
 * @ClassName ITaskState
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/22
 * @Version 1.0
 */
interface ITaskDownload {
    fun onTaskDownloading(appTask: AppTask, progress: Int, currentIndex: Long, totalIndex: Long, offsetIndexPerSeconds: Long) {}
    fun onTaskDownloadPause(appTask: AppTask) {}
    fun onTaskDownloadSuccess(appTask: AppTask) {}//应用卸载的监听
    fun onTaskDownloadFail(appTask: AppTask, exception: TaskException) {}
    fun onTaskDownloadCancel(appTask: AppTask) {}
}

interface ITaskVerify {
    fun onTaskVerifying(appTask: AppTask, progress: Int, currentIndex: Long, totalIndex: Long, offsetIndexPerSeconds: Long) {}
    fun onTaskVerifyPause(appTask: AppTask) {}
    fun onTaskVerifySuccess(appTask: AppTask) {}//应用卸载的监听
    fun onTaskVerifyFail(appTask: AppTask, exception: TaskException) {}
    fun onTaskVerifyCancel(appTask: AppTask) {}
}

interface ITaskUnzip {
    fun onTaskUnziping(appTask: AppTask, progress: Int, currentIndex: Long, totalIndex: Long, offsetIndexPerSeconds: Long) {}
    fun onTaskUnzipPause(appTask: AppTask) {}
    fun onTaskUnzipSuccess(appTask: AppTask) {}//应用卸载的监听
    fun onTaskUnzipFail(appTask: AppTask, exception: TaskException) {}
    fun onTaskUnzipCancel(appTask: AppTask) {}
}

interface ITaskInstall {
    fun onTaskInstalling(appTask: AppTask, progress: Int, currentIndex: Long, totalIndex: Long, offsetIndexPerSeconds: Long) {}
    fun onTaskInstallPause(appTask: AppTask) {}
    fun onTaskInstallSuccess(appTask: AppTask) {}//应用卸载的监听
    fun onTaskInstallFail(appTask: AppTask, exception: TaskException) {}
    fun onTaskInstallCancel(appTask: AppTask) {}
}

interface ITaskOpen {
    fun onTaskOpening(appTask: AppTask, progress: Int, currentIndex: Long, totalIndex: Long, offsetIndexPerSeconds: Long) {}
    fun onTaskOpenPause(appTask: AppTask) {}
    fun onTaskOpenSuccess(appTask: AppTask) {}//应用卸载的监听
    fun onTaskOpenFail(appTask: AppTask, exception: TaskException) {}
    fun onTaskOpenCancel(appTask: AppTask) {}
}

interface ITaskClose {
    fun onTaskClosing(appTask: AppTask, progress: Int, currentIndex: Long, totalIndex: Long, offsetIndexPerSeconds: Long) {}
    fun onTaskClosePause(appTask: AppTask) {}
    fun onTaskCloseSuccess(appTask: AppTask) {}//应用卸载的监听
    fun onTaskCloseFail(appTask: AppTask, exception: TaskException) {}
    fun onTaskCloseCancel(appTask: AppTask) {}
}

interface ITaskUninstall {
    fun onTaskUninstalling(appTask: AppTask, progress: Int, currentIndex: Long, totalIndex: Long, offsetIndexPerSeconds: Long) {}
    fun onTaskUninstallPause(appTask: AppTask) {}
    fun onTaskUninstallSuccess(appTask: AppTask) {}//应用卸载的监听
    fun onTaskUninstallFail(appTask: AppTask, exception: TaskException) {}
    fun onTaskUninstallCancel(appTask: AppTask) {}
}

interface ITaskDelete {
    fun onTaskDeleting(appTask: AppTask, progress: Int, currentIndex: Long, totalIndex: Long, offsetIndexPerSeconds: Long) {}
    fun onTaskDeletePause(appTask: AppTask) {}
    fun onTaskDeleteSuccess(appTask: AppTask) {}//文件删除的监听
    fun onTaskDeleteFail(appTask: AppTask, exception: TaskException) {}
    fun onTaskDeleteCancel(appTask: AppTask) {}
}

interface ITask {
    fun onTaskCreate(appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String, isUpdate: Boolean) {}
    fun onTaskUnavailable(appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String) {}
    fun onTaskFinish(appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String, finishType: STaskFinishType) {}
}

interface ITasks : ITaskDownload, ITaskVerify, ITaskUnzip, ITaskInstall, ITaskOpen, ITaskClose, ITaskUninstall, ITaskDelete, ITask
//    /**
//     * 任务等待取消的回调
//     */
//    fun onTaskWaitCancel(appTask: AppTask) {}
//
//    /**
//     * 任务删除的回调
//     */
//    fun onTaskCancel(appTask: AppTask) {}
//
//    /**
//     * 任务成功
//     */
//    fun onTaskSuccess(appTask: AppTask) {}
//
//    /**
//     * 任务失败
//     */
//    fun onTaskFail(appTask: AppTask) {}

//interface INetKAppStateBook {
//    /**
//     * 预约状态发生变化的回调
//     *@param appFileParams 应用Id
//     *
//     */
//    fun onReservationStateChange(appFileParams: AppTask, booked: Boolean)
//}
