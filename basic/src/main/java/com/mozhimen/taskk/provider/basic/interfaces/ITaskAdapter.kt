package com.mozhimen.taskk.provider.basic.interfaces

import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.mozhimen.taskk.provider.basic.db.AppTask
import com.mozhimen.taskk.provider.basic.impls.TaskException

/**
 * @ClassName ITaskAdapter
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/22
 * @Version 1.0
 */
interface ITaskAdapterDownload<A : Adapter<ViewHolder>> {
    fun onTaskDownloading(adapter: A?, position: Int, appTask: AppTask, progress: Int, currentIndex: Long, totalIndex: Long, offsetIndexPerSeconds: Long) {}//下载进度回调方法
    fun onTaskDownloadPause(adapter: A?, position: Int, appTask: AppTask) {}//下载暂停的回调
    fun onTaskDownloadSuccess(adapter: A?, position: Int, appTask: AppTask) {}//下载成功的回调 不做任何事 此时会去校验应用或者解压npk
    fun onTaskDownloadFail(adapter: A?, position: Int, appTask: AppTask, exception: TaskException) {}//下载失败的回调
    fun onTaskDownloadCancel(adapter: A?, position: Int, appTask: AppTask) {}//下载取消的回调
}

interface ITaskAdapterVerify<A : Adapter<ViewHolder>> {
    fun onTaskVerifying(adapter: A?, position: Int, appTask: AppTask, progress: Int, currentIndex: Long, totalIndex: Long, offsetIndexPerSeconds: Long) {}//应用校验中
    fun onTaskVerifyPause(adapter: A?, position: Int, appTask: AppTask) {}
    fun onTaskVerifySuccess(adapter: A?, position: Int, appTask: AppTask) {}//应用校验成功
    fun onTaskVerifyFail(adapter: A?, position: Int, appTask: AppTask, exception: TaskException) {}//应用校验失败
    fun onTaskVerifyCancel(adapter: A?, position: Int, appTask: AppTask) {}
}

interface ITaskAdapterUnzip<A : Adapter<ViewHolder>> {
    fun onTaskUnziping(adapter: A?, position: Int, appTask: AppTask, progress: Int, currentIndex: Long, totalIndex: Long, offsetIndexPerSeconds: Long) {}//解压中
    fun onTaskUnzipPause(adapter: A?, position: Int, appTask: AppTask) {}
    fun onTaskUnzipSuccess(adapter: A?, position: Int, appTask: AppTask) {}//解压成功
    fun onTaskUnzipFail(adapter: A?, position: Int, appTask: AppTask, exception: TaskException) {}//解压失败
    fun onTaskUnzipCancel(adapter: A?, position: Int, appTask: AppTask) {}
}

interface ITaskAdapterInstall<A : Adapter<ViewHolder>> {
    fun onTaskInstalling(adapter: A?, position: Int, appTask: AppTask, progress: Int, currentIndex: Long, totalIndex: Long, offsetIndexPerSeconds: Long) {}//安装中
    fun onTaskInstallPause(adapter: A?, position: Int, appTask: AppTask) {}//应用安装的监听
    fun onTaskInstallSuccess(adapter: A?, position: Int, appTask: AppTask) {}//安装取消
    fun onTaskInstallFail(adapter: A?, position: Int, appTask: AppTask, exception: TaskException) {}
    fun onTaskInstallCancel(adapter: A?, position: Int, appTask: AppTask) {}
}

interface ITaskAdapterOpen<A : Adapter<ViewHolder>> {
    fun onTaskOpening(adapter: A?, position: Int, appTask: AppTask, progress: Int, currentIndex: Long, totalIndex: Long, offsetIndexPerSeconds: Long) {}//安装中
    fun onTaskOpenPause(adapter: A?, position: Int, appTask: AppTask) {}
    fun onTaskOpenSuccess(adapter: A?, position: Int, appTask: AppTask) {}
    fun onTaskOpenFail(adapter: A?, position: Int, appTask: AppTask, exception: TaskException) {}
    fun onTaskOpenCancel(adapter: A?, position: Int, appTask: AppTask) {}
}

interface ITaskAdapterUninstall<A : Adapter<ViewHolder>> {
    fun onTaskUninstalling(adapter: A?, position: Int, appTask: AppTask, progress: Int, currentIndex: Long, totalIndex: Long, offsetIndexPerSeconds: Long) {}//安装中
    fun onTaskUninstallPause(adapter: A?, position: Int, appTask: AppTask) {}
    fun onTaskUninstallSuccess(adapter: A?, position: Int, appTask: AppTask) {}
    fun onTaskUninstallFail(adapter: A?, position: Int, appTask: AppTask, exception: TaskException) {}
    fun onTaskUninstallCancel(adapter: A?, position: Int, appTask: AppTask) {}
}

interface ITaskAdapter<A : Adapter<ViewHolder>> {
    fun onTaskCreate(adapter: A?, position: Int, appTask: AppTask, isUpdate: Boolean)
    //    fun onTaskWait(adapter: V?, appTask: AppTask) //任务等待的回调
}

interface ITaskAdapters<A : Adapter<ViewHolder>> : ITaskAdapterDownload<A>, ITaskAdapterVerify<A>, ITaskAdapterUnzip<A>, ITaskAdapterInstall<A>, ITaskAdapterOpen<A>, ITaskAdapterUninstall<A>,
    ITaskAdapter<A>