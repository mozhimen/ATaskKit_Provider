package com.mozhimen.taskk.provider.task.install.splits.ackpine

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.mozhimen.kotlin.elemk.commons.IABC_Listener
import com.mozhimen.kotlin.elemk.commons.IA_Listener
import com.mozhimen.kotlin.lintk.optins.permission.OPermission_POST_NOTIFICATIONS
import com.mozhimen.kotlin.lintk.optins.permission.OPermission_REQUEST_INSTALL_PACKAGES
import com.mozhimen.kotlin.utilk.android.util.UtilKLogWrapper
import com.mozhimen.kotlin.utilk.java.io.file2uri
import com.mozhimen.kotlin.utilk.kotlin.strFilePath2file
import com.mozhimen.kotlin.utilk.wrapper.UtilKPermission
import com.mozhimen.installk.splits.ackpine.InstallKSplitsAckpine
import com.mozhimen.installk.splits.ackpine.cons.SInstallState
import com.mozhimen.taskk.provider.basic.annors.ATaskNodeQueueName
import com.mozhimen.taskk.provider.basic.bases.ATaskManagerProvider
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskInstall
import com.mozhimen.taskk.provider.basic.db.AppTask
import com.mozhimen.taskk.provider.basic.commons.ITaskLifecycle
import java.io.File

/**
 * @ClassName TaskProviderInstallSplitsAckpine
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2024/6/21
 * @Version 1.0
 */
@OPermission_REQUEST_INSTALL_PACKAGES
@OPermission_POST_NOTIFICATIONS
class TaskInstallSplitsAckpine constructor(
    taskManager:ATaskManagerProvider,
    iTaskLifecycle: ITaskLifecycle,
    private val _applyPermissionListener: IABC_Listener<File, TaskInstallSplitsAckpine, AppTask>? = null,
    private val _installListener: IA_Listener<SInstallState>? = null
) : ATaskInstall(taskManager,iTaskLifecycle) {

    override fun getSupportFileExts(): List<String> {
        return listOf("zip", "apks", "xapk", "apkm", "apk")
    }

    @SuppressLint("MissingSuperCall")
    override fun taskStart(appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String) {
//        super.taskStart(appTask, taskNodeQueueName)
        val file = if (appTask.taskUnzipEnable) {
            appTask.taskUnzipFilePath.strFilePath2file()
        } else {
            appTask.filePathNameExt.strFilePath2file()
        }
        if (!UtilKPermission.hasPostNotification()) {
            UtilKLogWrapper.d(TAG, "taskStart: !UtilKPermission.hasPostNotification()")
            _applyPermissionListener?.invoke(file, this, appTask)
        } else {
            install_internal(file)
        }
    }

    ////////////////////////////////////////////////////////////

    @OPermission_POST_NOTIFICATIONS
    private fun install_internal(file: File) {
        Log.d(TAG, "installInternal: file $file")
        file.file2uri()?.let {
            InstallKSplitsAckpine.install(it, ProcessLifecycleOwner.get().lifecycleScope, _installListener)
        } ?: run {
            _installListener?.invoke(SInstallState.Fail(null, null))
        }
    }
}