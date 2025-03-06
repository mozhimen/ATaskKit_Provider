package com.mozhimen.taskk.provider.tradition.iterfaces

import com.mozhimen.taskk.provider.basic.cons.STaskFinishType
import com.mozhimen.taskk.provider.basic.db.AppTask
import com.mozhimen.taskk.provider.basic.impls.TaskException


/**
 * @ClassName IDownloadStateListener
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2023/10/11 18:24
 * @Version 1.0
 */

interface INetKAppStateUninstall {
    fun onUninstallSuccess(appTask: AppTask) {}//应用卸载的监听
}

interface INetKAppStateInstall {
    fun onInstalling(appTask: AppTask) {}//安装中
    fun onInstallSuccess(appTask: AppTask) {}//应用安装的监听
    fun onInstallFail(appTask: AppTask, exception: TaskException) {}
    fun onInstallCancel(appTask: AppTask) {}
}

interface INetKAppStateUnzip {
    fun onUnziping(appTask: AppTask, progress: Int, currentIndex: Long, totalIndex: Long, offsetIndexPerSeconds: Long) {}//解压中
    fun onUnzipSuccess(appTask: AppTask) {}//解压成功
    fun onUnzipFail(appTask: AppTask, exception: TaskException) {}//解压失败
}

interface INetKAppStateVerify {
    fun onVerifying(appTask: AppTask) {}//应用校验中
    fun onVerifySuccess(appTask: AppTask) {}//应用校验成功
    fun onVerifyFail(appTask: AppTask, exception: TaskException) {}//应用校验失败
}

interface INetKAppStateDownload {
    //    fun onDownloadWait(appTask: AppTask) {}
    fun onDownloading(appTask: AppTask, progress: Int, currentIndex: Long, totalIndex: Long, offsetIndexPerSeconds: Long) {}//下载进度回调方法
    fun onDownloadPause(appTask: AppTask) {}//下载暂停的回调
    fun onDownloadCancel(appTask: AppTask) {}//下载取消的回调
    fun onDownloadSuccess(appTask: AppTask) {}//下载成功的回调 不做任何事 此时会去校验应用或者解压npk
    fun onDownloadFail(appTask: AppTask, exception: TaskException) {}//下载失败的回调
}

interface INetKAppStateTask {
    fun onTaskCreate(appTask: AppTask, isUpdate: Boolean)

    //    fun onTaskWait(appTask: AppTask) //任务等待的回调
    fun onTasking(appTask: AppTask, state: Int)//任务进行中
    fun onTaskPause(appTask: AppTask)
    fun onTaskFinish(appTask: AppTask, finishType: STaskFinishType)
}

interface ITaskState : INetKAppStateDownload, INetKAppStateVerify, INetKAppStateUnzip, INetKAppStateInstall, INetKAppStateTask, INetKAppStateUninstall

