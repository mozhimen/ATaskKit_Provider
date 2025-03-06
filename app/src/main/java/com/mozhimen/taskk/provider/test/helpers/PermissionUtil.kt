package com.mozhimen.taskk.provider.test.helpers

import android.annotation.SuppressLint
import android.content.Context
import com.mozhimen.kotlin.elemk.commons.I_Listener
import com.mozhimen.kotlin.lintk.optins.permission.OPermission_MANAGE_EXTERNAL_STORAGE
import com.mozhimen.kotlin.lintk.optins.permission.OPermission_POST_NOTIFICATIONS
import com.mozhimen.kotlin.lintk.optins.permission.OPermission_READ_EXTERNAL_STORAGE
import com.mozhimen.kotlin.lintk.optins.permission.OPermission_REQUEST_INSTALL_PACKAGES
import com.mozhimen.kotlin.lintk.optins.permission.OPermission_WRITE_EXTERNAL_STORAGE
import com.mozhimen.permissionk.xxpermissions.XXPermissionsCheckUtil
import com.mozhimen.permissionk.xxpermissions.XXPermissionsRequestUtil

/**
 * @ClassName PermissionUtil
 * @Description TODO
 * @Author mozhimen
 * @Date 2025/3/6
 * @Version 1.0
 */
object PermissionUtil {
    @JvmStatic
    @SuppressLint("MissingPermission")
    @OptIn(OPermission_READ_EXTERNAL_STORAGE::class, OPermission_WRITE_EXTERNAL_STORAGE::class, OPermission_MANAGE_EXTERNAL_STORAGE::class)
    fun requestPermissionStorage(context: Context, block: I_Listener) {
        if (XXPermissionsCheckUtil.hasPermission_EXTERNAL_STORAGE(context)) {
            block.invoke()
        } else {
            XXPermissionsRequestUtil.requestPermission_EXTERNAL_STORAGE(context, onGranted = {
                block.invoke()
            }, onDenied = {})
        }
    }

    @JvmStatic
    @SuppressLint("MissingPermission")
    @OptIn(OPermission_POST_NOTIFICATIONS::class)
    fun requestPermissionNotification(context: Context, block: I_Listener) {
        if (XXPermissionsCheckUtil.hasPermission_POST_NOTIFICATIONS(context)) {
            block.invoke()
        } else {
            XXPermissionsRequestUtil.requestPermission_POST_NOTIFICATIONS(context, onGranted = {
                block.invoke()
            }, onDenied = {})
        }
    }

    @OptIn(OPermission_REQUEST_INSTALL_PACKAGES::class)
    fun requestPermissionInstall(activityContext: Context, block: I_Listener) {
        if (XXPermissionsCheckUtil.hasPermission_REQUEST_INSTALL_PACKAGES(activityContext)) {
            block.invoke()
        } else {
            XXPermissionsRequestUtil.requestPermission_REQUEST_INSTALL_PACKAGES(activityContext, onGranted = {
                block.invoke()
            }, onDenied = {})
        }
    }
}