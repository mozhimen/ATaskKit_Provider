package com.mozhimen.taskk.provider.basic.bases.providers

import androidx.annotation.CallSuper
import com.mozhimen.installk.manager.commons.IInstallKReceiverProxy
import com.mozhimen.taskk.provider.basic.annors.ATaskName
import com.mozhimen.taskk.provider.basic.bases.ATask
import com.mozhimen.taskk.provider.basic.cons.CTaskState
import com.mozhimen.taskk.provider.basic.cons.STaskFinishType
import com.mozhimen.taskk.provider.basic.db.AppTask
import com.mozhimen.taskk.provider.basic.interfaces.ITaskLifecycle

/**
 * @ClassName INetKAppInstallProvider
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2024/6/21
 * @Version 1.0
 */
abstract class ATaskInstall(iTaskLifecycle: ITaskLifecycle?) : ATask(iTaskLifecycle) {
    protected var _iInstallKReceiverProxy: IInstallKReceiverProxy? = null

    fun setInstallKReceiverProxy(iInstallKReceiverProxy: IInstallKReceiverProxy): ATaskInstall {
        _iInstallKReceiverProxy = iInstallKReceiverProxy
        return this
    }

    override fun getTaskName(): String {
        return ATaskName.TASK_INSTALL
    }

    @CallSuper
    override fun taskStart(appTask: AppTask) {
        onTaskStarted(CTaskState.STATE_INSTALLING, appTask)
    }

    @CallSuper
    override fun taskResume(appTask: AppTask) {
        onTaskStarted(CTaskState.STATE_INSTALLING, appTask)
    }

    @CallSuper
    override fun taskPause(appTask: AppTask) {
        onTaskPaused(CTaskState.STATE_INSTALL_PAUSE, appTask)
    }

    @CallSuper
    override fun taskCancel(appTask: AppTask) {
        onTaskFinished(CTaskState.STATE_INSTALL_CANCEL, STaskFinishType.CANCEL, appTask)
    }
}