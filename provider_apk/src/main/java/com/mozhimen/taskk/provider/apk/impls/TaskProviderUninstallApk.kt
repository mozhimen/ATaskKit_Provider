package com.mozhimen.taskk.provider.apk.impls

import android.content.Context
import com.mozhimen.basick.lintk.optins.OApiInit_InApplication
import com.mozhimen.basick.utilk.android.util.UtilKLogWrapper
import com.mozhimen.installk.manager.InstallKManager
import com.mozhimen.taskk.provider.basic.interfaces.ITaskProviderLifecycle
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskProviderUninstall
import com.mozhimen.taskk.provider.basic.cons.CTaskState
import com.mozhimen.taskk.provider.basic.cons.STaskFinishType
import com.mozhimen.taskk.provider.basic.db.AppTask
import com.mozhimen.taskk.provider.basic.db.AppTaskDaoManager

/**
 * @ClassName TaskProviderUninstallDefault
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/19
 * @Version 1.0
 */
class TaskProviderUninstallApk(private val _iTaskProviderLifecycle: ITaskProviderLifecycle?) : ATaskProviderUninstall(_iTaskProviderLifecycle) {

    override fun getSupportFileExtensions(): List<String> {
        return listOf("apk")
    }

    ////////////////////////////////////////////////////

    override fun taskStart(appTask: AppTask) {
        //
        onTaskStarted(CTaskState.STATE_UNINSTALLING, appTask)
    }

    override fun taskResume(appTask: AppTask) {
        onTaskStarted(CTaskState.STATE_UNINSTALLING, appTask)
    }

    override fun taskPause(appTask: AppTask) {
        onTaskPaused(CTaskState.STATE_UNINSTALL_PAUSE, appTask)
    }

    override fun taskCancel(appTask: AppTask) {
        onTaskFinished(CTaskState.STATE_UNINSTALL_CANCEL, STaskFinishType.CANCEL, appTask)
    }
}