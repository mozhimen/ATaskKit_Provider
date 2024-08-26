package com.mozhimen.taskk.provider.test

import android.annotation.SuppressLint
import android.content.Context
import com.mozhimen.basick.elemk.commons.I_Listener
import com.mozhimen.basick.lintk.optins.OApiInit_InApplication
import com.mozhimen.basick.lintk.optins.permission.OPermission_INTERNET
import com.mozhimen.basick.lintk.optins.permission.OPermission_POST_NOTIFICATIONS
import com.mozhimen.basick.lintk.optins.permission.OPermission_REQUEST_INSTALL_PACKAGES
import com.mozhimen.basick.stackk.cb.StackKCb
import com.mozhimen.basick.utilk.android.util.UtilKLogWrapper
import com.mozhimen.manifestk.xxpermissions.XXPermissionsCheckUtil
import com.mozhimen.manifestk.xxpermissions.XXPermissionsRequestUtil
import com.mozhimen.taskk.provider.apk.TaskProviderApk
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskDownload
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskInstall
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskOpen
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskUninstall
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskUnzip
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskVerify
import com.mozhimen.taskk.provider.core.TaskManager
import com.mozhimen.taskk.provider.install.splits.ackpine.TaskInstallSplitsAckpine

/**
 * @ClassName MainTaskManager
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/23
 * @Version 1.0
 */
@OptIn(OApiInit_InApplication::class, OPermission_INTERNET::class)
object MainTaskManager : TaskManager() {
    val taskProviderApk by lazy { TaskProviderApk(_iTaskLifecycle, this) }

    override fun init(context: Context) {
        if (hasInit()) return
        super.init(context)
        taskProviderApk.init(context)
        UtilKLogWrapper.d(TAG, "init: ")
    }

    override fun getTaskQueues(): Map<String, List<String>> {
        return taskProviderApk.getSupportFileExtensions().associateWith { taskProviderApk.getTaskQueue() }
    }

    ////////////////////////////////////////////////////////////////////

    override fun getTaskDownloads(): List<ATaskDownload> {
        return listOf(taskProviderApk.getTaskDownload())
    }

    override fun getTaskVerifys(): List<ATaskVerify> {
        return listOf(taskProviderApk.getTaskVerify())
    }

    override fun getTaskUnzips(): List<ATaskUnzip> {
        return listOf(taskProviderApk.getTaskUnzip())
    }

    @SuppressLint("MissingPermission")
    @OptIn(OPermission_REQUEST_INSTALL_PACKAGES::class, OPermission_POST_NOTIFICATIONS::class)
    override fun getTaskInstalls(): List<ATaskInstall> {
        return listOf(taskProviderApk.getTaskInstall(), TaskInstallSplitsAckpine(_iTaskLifecycle, _applyPermissionListener = { _, taskInstallSplitsAckpine, appTask ->
            UtilKLogWrapper.d(TAG, "getTaskInstalls: permissions")
            StackKCb.instance.getStackTopActivity()?.let {
                requestPermissionInstall(it) {
                    requestPermissionNotification(it) {
                        taskInstallSplitsAckpine.taskStart(appTask)
                    }
                }
            }
        }))
    }

    @SuppressLint("MissingPermission")
    @OptIn(OPermission_POST_NOTIFICATIONS::class)
    private fun requestPermissionNotification(activityContext: Context,block: I_Listener) {
        if (XXPermissionsCheckUtil.hasPostNotificationPermission(activityContext)) {
            block.invoke()
        } else {
            XXPermissionsRequestUtil.requestPostNotificationPermission(activityContext, onGranted = {
                block.invoke()
            }, onDenied = {})
        }
    }

    @OptIn(OPermission_REQUEST_INSTALL_PACKAGES::class)
    private fun requestPermissionInstall(activityContext: Context, block: I_Listener) {
        if (XXPermissionsCheckUtil.hasInstallPermission(activityContext)) {
            block.invoke()
        } else {
            XXPermissionsRequestUtil.requestInstallPermission(activityContext, onGranted = {
                block.invoke()
            }, onDenied = {})
        }
    }

    override fun getTaskOpens(): List<ATaskOpen> {
        return listOf(taskProviderApk.getTaskOpen())
    }

    override fun getTaskUninstalls(): List<ATaskUninstall> {
        return listOf(taskProviderApk.getTaskUninstall())
    }
}