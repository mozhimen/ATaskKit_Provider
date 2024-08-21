package com.mozhimen.taskk.provider.core.commons

import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.mozhimen.taskk.provider.basic.cons.STaskFinishType
import com.mozhimen.taskk.provider.basic.db.AppTask
import com.mozhimen.taskk.provider.basic.impls.TaskException

/**
 * @ClassName INetKViewAppState
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2023/11/16 10:58
 * @Version 1.0
 */
interface INetKAppStateAdapter<A : Adapter<ViewHolder>> {
    fun onTaskCreate(adapter: A?, position: Int, appTask: AppTask, isUpdate: Boolean)

    //    fun onTaskWait(adapter: V?, appTask: AppTask) //任务等待的回调
    fun onTasking(adapter: A?, position: Int, appTask: AppTask, state: Int)//任务进行中
    fun onTaskPause(adapter: A?, position: Int, appTask: AppTask)
    fun onTaskFinish(adapter: A?, position: Int, appTask: AppTask, finishType: STaskFinishType)

    fun onDownloadWait(adapter: A?, position: Int, appTask: AppTask) {}
    fun onDownloading(adapter: A?, position: Int, appTask: AppTask, progress: Int, currentIndex: Long, totalIndex: Long, offsetIndexPerSeconds: Long) {}//下载进度回调方法
    fun onDownloadPause(adapter: A?, position: Int, appTask: AppTask) {}//下载暂停的回调
    fun onDownloadCancel(adapter: A?, position: Int, appTask: AppTask) {}//下载取消的回调
    fun onDownloadSuccess(adapter: A?, position: Int, appTask: AppTask) {}//下载成功的回调 不做任何事 此时会去校验应用或者解压npk
    fun onDownloadFail(adapter: A?, position: Int, appTask: AppTask, exception: TaskException) {}//下载失败的回调

    fun onVerifying(adapter: A?, position: Int, appTask: AppTask) {}//应用校验中
    fun onVerifySuccess(adapter: A?, position: Int, appTask: AppTask) {}//应用校验成功
    fun onVerifyFail(adapter: A?, position: Int, appTask: AppTask, exception: TaskException) {}//应用校验失败

    fun onUnziping(adapter: A?, position: Int, appTask: AppTask, progress: Int, currentIndex: Long, totalIndex: Long, offsetIndexPerSeconds: Long) {}//解压中
    fun onUnzipSuccess(adapter: A?, position: Int, appTask: AppTask) {}//解压成功
    fun onUnzipFail(adapter: A?, position: Int, appTask: AppTask, exception: TaskException) {}//解压失败

    fun onInstalling(adapter: A?, position: Int, appTask: AppTask) {}//安装中
    fun onInstallSuccess(adapter: A?, position: Int, appTask: AppTask) {}//应用安装的监听
    fun onInstallCancel(adapter: A?, position: Int, appTask: AppTask) {}//安装取消
    fun onInstallFail(adapter: A?, position: Int, appTask: AppTask, exception: TaskException) {}

    fun onUninstallSuccess(adapter: A?, position: Int, appTask: AppTask) {}//应用卸载的监听
}