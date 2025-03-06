package com.mozhimen.taskk.provider.test

import android.annotation.SuppressLint
import com.mozhimen.kotlin.lintk.optins.OApiInit_InApplication
import com.mozhimen.kotlin.lintk.optins.permission.OPermission_POST_NOTIFICATIONS
import com.mozhimen.kotlin.lintk.optins.permission.OPermission_REQUEST_INSTALL_PACKAGES
import com.mozhimen.kotlin.utilk.android.util.UtilKLogWrapper
import com.mozhimen.stackk.callback.StackKCb
import com.mozhimen.taskk.provider.apk.TaskProviderApk
import com.mozhimen.taskk.provider.basic.annors.ATaskName
import com.mozhimen.taskk.provider.basic.bases.ATaskManagerProvider
import com.mozhimen.taskk.provider.basic.bases.ATaskProvider
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskInstall
import com.mozhimen.taskk.provider.basic.commons.ITaskLifecycle
import com.mozhimen.taskk.provider.basic.cons.STaskNode
import com.mozhimen.taskk.provider.basic.cons.plus
import com.mozhimen.taskk.provider.core.BaseTaskManagerProvider
import com.mozhimen.taskk.provider.task.install.splits.ackpine.TaskInstallSplitsAckpine
import com.mozhimen.taskk.provider.test.helpers.PermissionUtil

/**
 * @ClassName MainTaskManager
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/23
 * @Version 1.0
 */
@OApiInit_InApplication
class MainTaskManagerProvider : BaseTaskManagerProvider("") {

    class TaskProviderApk1(
        iTaskLifecycle: ITaskLifecycle,
        taskManager: ATaskManagerProvider,
    ) : TaskProviderApk(iTaskLifecycle, taskManager) {
        override fun getTaskNodeQueues(): Map<String, List<STaskNode>> {
            return mapOf(
                ATaskName.TASK_INSTALL to listOf(STaskNode.TaskNodeDownload, STaskNode.TaskNodeVerify + STaskNode.TaskNodeBlocker, STaskNode.TaskNodeInstall),
                ATaskName.TASK_OPEN to listOf(STaskNode.TaskNodeOpen),
                ATaskName.TASK_UNINSTALL to listOf(STaskNode.TaskNodeUninstall, STaskNode.TaskNodeDelete, STaskNode.TaskNodeRestart)
            )
        }
    }

    override fun getTaskProviders(): List<ATaskProvider> {
        return listOf(TaskProviderApk1(_iTaskLifecycle, this))
    }

    ////////////////////////////////////////////////////////////////////

    @SuppressLint("MissingPermission")
    @OptIn(OPermission_REQUEST_INSTALL_PACKAGES::class, OPermission_POST_NOTIFICATIONS::class)
    override fun getTaskInstalls(): List<ATaskInstall> {
        return mutableListOf(TaskInstallSplitsAckpine(this, _iTaskLifecycle, _applyPermissionListener = { _, taskInstallSplitsAckpine, appTask ->
            UtilKLogWrapper.d(TAG, "getTaskInstalls: permissions")
            StackKCb.instance.getStackTopActivity()?.let {
                PermissionUtil.requestPermissionInstall(it) {
                    PermissionUtil.requestPermissionNotification(it) {
                        taskInstallSplitsAckpine.taskStart(appTask, ATaskName.TASK_INSTALL)
                    }
                }
            }
        })) + super.getTaskInstalls()
    }
}