package com.mozhimen.taskk.provider.test

import android.annotation.SuppressLint
import android.content.Context
import com.mozhimen.kotlin.elemk.commons.I_Listener
import com.mozhimen.kotlin.lintk.optins.OApiInit_InApplication
import com.mozhimen.kotlin.lintk.optins.permission.OPermission_INTERNET
import com.mozhimen.kotlin.lintk.optins.permission.OPermission_POST_NOTIFICATIONS
import com.mozhimen.kotlin.lintk.optins.permission.OPermission_REQUEST_INSTALL_PACKAGES
import com.mozhimen.kotlin.utilk.android.util.UtilKLogWrapper
import com.mozhimen.manifestk.xxpermissions.XXPermissionsCheckUtil
import com.mozhimen.manifestk.xxpermissions.XXPermissionsRequestUtil
import com.mozhimen.stackk.callback.StackKCb
import com.mozhimen.taskk.provider.apk.TaskProviderApk
import com.mozhimen.taskk.provider.basic.annors.ATaskName
import com.mozhimen.taskk.provider.basic.bases.ATaskProvider
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskInstall
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

    override fun getTaskProviders(): List<ATaskProvider> {
        return listOf(taskProviderApk)
    }

    ////////////////////////////////////////////////////////////////////

    @SuppressLint("MissingPermission")
    @OptIn(OPermission_REQUEST_INSTALL_PACKAGES::class, OPermission_POST_NOTIFICATIONS::class)
    override fun getTaskInstalls(): List<ATaskInstall> {
        return mutableListOf(TaskInstallSplitsAckpine(_iTaskLifecycle, _applyPermissionListener = { _, taskInstallSplitsAckpine, appTask ->
            UtilKLogWrapper.d(TAG, "getTaskInstalls: permissions")
            StackKCb.instance.getStackTopActivity()?.let {
                requestPermissionInstall(it) {
                    requestPermissionNotification(it) {
                        taskInstallSplitsAckpine.taskStart(appTask, ATaskName.TASK_INSTALL)
                    }
                }
            }
        })) + super.getTaskInstalls()
    }

    @SuppressLint("MissingPermission")
    @OptIn(OPermission_POST_NOTIFICATIONS::class)
    private fun requestPermissionNotification(activityContext: Context, block: I_Listener) {
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
}