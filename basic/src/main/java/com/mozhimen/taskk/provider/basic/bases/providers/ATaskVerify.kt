package com.mozhimen.taskk.provider.basic.bases.providers

import androidx.annotation.CallSuper
import com.mozhimen.taskk.provider.basic.annors.ATaskName
import com.mozhimen.taskk.provider.basic.bases.ATask
import com.mozhimen.taskk.provider.basic.cons.CTaskState
import com.mozhimen.taskk.provider.basic.cons.STaskFinishType
import com.mozhimen.taskk.provider.basic.db.AppTask
import com.mozhimen.taskk.provider.basic.interfaces.ITaskLifecycle

/**
 * @ClassName ITaskProviderVerify
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/20
 * @Version 1.0
 */
abstract class ATaskVerify(iTaskLifecycle: ITaskLifecycle?) : ATask(iTaskLifecycle) {

    override fun getTaskName(): String {
        return ATaskName.TASK_VERIFY
    }

    @CallSuper
    override fun taskStart(appTask: AppTask) {
        onTaskStarted(CTaskState.STATE_VERIFYING, appTask)
    }

    override fun taskResume(appTask: AppTask) {
    }

    override fun taskPause(appTask: AppTask) {
    }

    override fun taskCancel(appTask: AppTask) {
    }
}