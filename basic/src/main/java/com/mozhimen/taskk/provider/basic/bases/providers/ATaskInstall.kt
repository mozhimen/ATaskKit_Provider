package com.mozhimen.taskk.provider.basic.bases.providers

import android.annotation.SuppressLint
import androidx.annotation.CallSuper
import com.mozhimen.taskk.provider.basic.annors.ATaskName
import com.mozhimen.taskk.provider.basic.annors.ATaskQueueName
import com.mozhimen.taskk.provider.basic.bases.ATask
import com.mozhimen.taskk.provider.basic.cons.CState
import com.mozhimen.taskk.provider.basic.cons.CTaskState
import com.mozhimen.taskk.provider.basic.cons.STaskFinishType
import com.mozhimen.taskk.provider.basic.db.AppTask
import com.mozhimen.taskk.provider.basic.interfaces.ITaskInterceptor
import com.mozhimen.taskk.provider.basic.interfaces.ITaskLifecycle

/**
 * @ClassName INetKAppInstallProvider
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2024/6/21
 * @Version 1.0
 */
abstract class ATaskInstall(iTaskLifecycle: ITaskLifecycle?) : ATask(iTaskLifecycle) {

    protected var _iTaskProviderInterceptor: ITaskInterceptor? = null

    fun setTaskInterceptor(iTaskProviderInterceptor: ITaskInterceptor) {
        _iTaskProviderInterceptor = iTaskProviderInterceptor
    }

    override fun getTaskName(): String {
        return ATaskName.TASK_INSTALL
    }

    override fun taskStart(appTask: AppTask, @ATaskQueueName taskQueueName: String) {
    }

    override fun taskResume(appTask: AppTask, @ATaskQueueName taskQueueName: String) {
    }

    override fun taskPause(appTask: AppTask, @ATaskQueueName taskQueueName: String) {
    }

    override fun taskCancel(appTask: AppTask, @ATaskQueueName taskQueueName: String) {
    }

    override fun canTaskStart(appTask: AppTask, @ATaskQueueName taskQueueName: String): Boolean {
        return true
    }

    override fun canTaskResume(appTask: AppTask, @ATaskQueueName taskQueueName: String): Boolean {
        return false
    }

    override fun canTaskPause(appTask: AppTask, @ATaskQueueName taskQueueName: String): Boolean {
        return false
    }

    override fun canTaskCancel(appTask: AppTask, @ATaskQueueName taskQueueName: String): Boolean {
        return false
    }

    @SuppressLint("MissingSuperCall")
    override fun onTaskFinished(taskState: Int, finishType: STaskFinishType, appTask: AppTask) {
        if (finishType is STaskFinishType.SUCCESS){
            if (_iTaskProviderInterceptor?.isAutoDeleteOrgFiles() == true) {
                _iTaskProviderInterceptor?.deleteOrgFiles(appTask)
            }
            val taskStateNew = CState.STATE_TASK_SUCCESS
            appTask.toNewTaskState(taskStateNew)
            _iTaskLifecycle?.onTaskFinished(taskStateNew, STaskFinishType.SUCCESS, appTask)
        }
    }
}