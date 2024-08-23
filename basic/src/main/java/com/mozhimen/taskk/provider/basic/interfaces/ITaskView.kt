package com.mozhimen.taskk.provider.basic.interfaces

import com.mozhimen.taskk.provider.basic.db.AppTask
import com.mozhimen.taskk.provider.basic.impls.TaskException

/**
 * @ClassName ITaskView
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/22
 * @Version 1.0
 */
interface ITaskViewDownload<V> {
    fun onTaskDownloading(view: V?, appTask: AppTask, progress: Int, currentIndex: Long, totalIndex: Long, offsetIndexPerSeconds: Long) {}//下载进度回调方法
    fun onTaskDownloadPause(view: V?, appTask: AppTask) {}//下载暂停的回调
    fun onTaskDownloadCancel(view: V?, appTask: AppTask) {}//下载取消的回调
    fun onTaskDownloadSuccess(view: V?, appTask: AppTask) {}//下载成功的回调 不做任何事 此时会去校验应用或者解压npk
    fun onTaskDownloadFail(view: V?, appTask: AppTask, exception: TaskException) {}//下载失败的回调
}

interface ITaskViewVerify<V> {
    fun onTaskVerifying(view: V?, appTask: AppTask, progress: Int, currentIndex: Long, totalIndex: Long, offsetIndexPerSeconds: Long) {}
    fun onTaskVerifyPause(view: V?, appTask: AppTask) {}
    fun onTaskVerifySuccess(view: V?, appTask: AppTask) {}
    fun onTaskVerifyFail(view: V?, appTask: AppTask, exception: TaskException) {}
    fun onTaskVerifyCancel(view: V?, appTask: AppTask) {}
}

interface ITaskViewUnzip<V> {
    fun onTaskUnziping(view: V?, appTask: AppTask, progress: Int, currentIndex: Long, totalIndex: Long, offsetIndexPerSeconds: Long) {}
    fun onTaskUnzipPause(view: V?, appTask: AppTask) {}
    fun onTaskUnzipSuccess(view: V?, appTask: AppTask) {}
    fun onTaskUnzipFail(view: V?, appTask: AppTask, exception: TaskException) {}
    fun onTaskUnzipCancel(view: V?, appTask: AppTask) {}
}

interface ITaskViewInstall<V> {
    fun onTaskInstalling(view: V?, appTask: AppTask, progress: Int, currentIndex: Long, totalIndex: Long, offsetIndexPerSeconds: Long) {}
    fun onTaskInstallPause(view: V?, appTask: AppTask) {}
    fun onTaskInstallSuccess(view: V?, appTask: AppTask) {}
    fun onTaskInstallFail(view: V?, appTask: AppTask, exception: TaskException) {}
    fun onTaskInstallCancel(view: V?, appTask: AppTask) {}
}

interface ITaskViewOpen<V> {
    fun onTaskOpening(view: V?, appTask: AppTask, progress: Int, currentIndex: Long, totalIndex: Long, offsetIndexPerSeconds: Long) {}
    fun onTaskOpenPause(view: V?, appTask: AppTask) {}
    fun onTaskOpenSuccess(view: V?, appTask: AppTask) {}
    fun onTaskOpenFail(view: V?, appTask: AppTask, exception: TaskException) {}
    fun onTaskOpenCancel(view: V?, appTask: AppTask) {}
}

interface ITaskViewUninstall<V> {
    fun onTaskUninstalling(view: V?, appTask: AppTask, progress: Int, currentIndex: Long, totalIndex: Long, offsetIndexPerSeconds: Long) {}
    fun onTaskUninstallPause(view: V?, appTask: AppTask) {}
    fun onTaskUninstallSuccess(view: V?, appTask: AppTask) {}
    fun onTaskUninstallFail(view: V?, appTask: AppTask, exception: TaskException) {}
    fun onTaskUninstallCancel(view: V?, appTask: AppTask) {}
}

interface ITaskView<V> {
    fun onTaskCreate(view: V?, appTask: AppTask, isUpdate: Boolean)
    fun onTaskUnavailable(view: V?, appTask: AppTask){}
    fun onTaskSuccess(view: V?, appTask: AppTask){}
}

interface ITaskViews<V> :
    ITaskViewDownload<V>,
    ITaskViewVerify<V>,
    ITaskViewUnzip<V>,
    ITaskViewInstall<V>,
    ITaskViewOpen<V>,
    ITaskViewUninstall<V>,
    ITaskView<V>