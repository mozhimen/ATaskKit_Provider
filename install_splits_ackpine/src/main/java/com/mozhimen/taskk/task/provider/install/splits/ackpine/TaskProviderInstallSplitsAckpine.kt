package com.mozhimen.taskk.task.provider.install.splits.ackpine

import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.mozhimen.basick.elemk.commons.IAB_Listener
import com.mozhimen.basick.elemk.commons.IA_Listener
import com.mozhimen.basick.lintk.optins.permission.OPermission_POST_NOTIFICATIONS
import com.mozhimen.basick.utilk.java.io.file2uri
import com.mozhimen.basick.utilk.wrapper.UtilKPermission
import com.mozhimen.installk.splits.ackpine.InstallKSplitsAckpine
import com.mozhimen.installk.splits.ackpine.cons.SInstallState
import com.mozhimen.taskk.task.provider.commons.providers.ITaskProviderInstall
import java.io.File

/**
 * @ClassName TaskProviderInstallSplitsAckpine
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2024/6/21
 * @Version 1.0
 */
class TaskProviderInstallSplitsAckpine(
    private val _applyPermissionListener: IAB_Listener<File, TaskProviderInstallSplitsAckpine>? = null,
    private val _installListener: IA_Listener<SInstallState>? = null
) : ITaskProviderInstall {
    override fun getSupportFileExtensions(): List<String> {
        return listOf("zip", "apks", "xapk", "apkm")
    }

    @OptIn(OPermission_POST_NOTIFICATIONS::class)
    override fun install(file: File) {
        if (!UtilKPermission.hasPostNotification()) {
            _applyPermissionListener?.invoke(file, this)
        } else {
            installInternal(file)
        }
    }

    @OPermission_POST_NOTIFICATIONS
    fun installInternal(file: File) {
        file.file2uri()?.let {
            InstallKSplitsAckpine.install(it, ProcessLifecycleOwner.get().lifecycleScope, _installListener)
        } ?: run {
            _installListener?.invoke(SInstallState.Fail(null, null))
        }
    }
}