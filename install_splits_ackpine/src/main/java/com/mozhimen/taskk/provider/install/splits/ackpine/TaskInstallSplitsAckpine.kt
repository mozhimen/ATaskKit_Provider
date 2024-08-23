package com.mozhimen.taskk.provider.install.splits.ackpine

import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.mozhimen.basick.elemk.commons.IAB_Listener
import com.mozhimen.basick.elemk.commons.IA_Listener
import com.mozhimen.basick.lintk.optins.permission.OPermission_POST_NOTIFICATIONS
import com.mozhimen.basick.utilk.java.io.file2uri
import com.mozhimen.basick.utilk.kotlin.strFilePath2file
import com.mozhimen.basick.utilk.wrapper.UtilKPermission
import com.mozhimen.installk.splits.ackpine.InstallKSplitsAckpine
import com.mozhimen.installk.splits.ackpine.cons.SInstallState
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskInstall
import com.mozhimen.taskk.provider.basic.db.AppTask
import com.mozhimen.taskk.provider.basic.interfaces.ITaskLifecycle
import java.io.File

/**
 * @ClassName TaskProviderInstallSplitsAckpine
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2024/6/21
 * @Version 1.0
 */
class TaskInstallSplitsAckpine(
    iTaskLifecycle: ITaskLifecycle,
    private val _applyPermissionListener: IAB_Listener<File, TaskInstallSplitsAckpine>? = null,
    private val _installListener: IA_Listener<SInstallState>? = null
) : ATaskInstall(iTaskLifecycle) {

    override fun getSupportFileExtensions(): List<String> {
        return listOf("zip", "apks", "xapk", "apkm")
    }

    @OptIn(OPermission_POST_NOTIFICATIONS::class)
    override fun taskStart(appTask: AppTask) {
        val file = if (appTask.taskUnzipEnable) {
            appTask.taskUnzipFilePath.strFilePath2file()
        } else {
            appTask.filePathNameExt.strFilePath2file()
        }
        if (!UtilKPermission.hasPostNotification()) {
            _applyPermissionListener?.invoke(file, this)
        } else {
            installInternal(file)
        }
        super.taskStart(appTask)
    }

    ////////////////////////////////////////////////////////////

    @OPermission_POST_NOTIFICATIONS
    private fun installInternal(file: File) {
        file.file2uri()?.let {
            InstallKSplitsAckpine.install(it, ProcessLifecycleOwner.get().lifecycleScope, _installListener)
        } ?: run {
            _installListener?.invoke(SInstallState.Fail(null, null))
        }
    }
}