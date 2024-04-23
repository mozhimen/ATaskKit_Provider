package com.mozhimen.netk.app.install.helpers

import android.content.Context
import android.content.Intent
import android.util.Log
import com.mozhimen.basick.utilk.android.util.UtilKLogWrapper
import com.mozhimen.basick.elemk.android.content.bases.BaseBroadcastReceiver
import com.mozhimen.basick.elemk.android.content.cons.CIntent
import com.mozhimen.basick.lintk.optins.OApiInit_InApplication
import com.mozhimen.basick.lintk.optins.permission.OPermission_QUERY_ALL_PACKAGES
import com.mozhimen.basick.utilk.android.content.UtilKPackage
import com.mozhimen.basick.utilk.android.content.getVersionCode
import com.mozhimen.netk.app.install.NetKAppInstallManager
import com.mozhimen.netk.app.install.NetKAppUnInstallManager

/**
 * @ClassName InstallKFlymeReceiver
 * @Description TODO
 * @Author Mozhimen
 * @Date 2023/11/9 9:30
 * @Version 1.0
 */
class NetKAppInstallReceiver : BaseBroadcastReceiver() {
    @OptIn(OApiInit_InApplication::class, OPermission_QUERY_ALL_PACKAGES::class)
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            intent?.let { intent ->
                intent.dataString?.let { dataString ->
                    UtilKLogWrapper.d(TAG, "onReceive: dataString $dataString")
                    val apkPackName = dataString.split(":")[1]
                    if (apkPackName.isEmpty()) return
                    UtilKLogWrapper.i(TAG, "onReceive: action ${intent.action} apkPackName $apkPackName")
                    when (intent.action) {
                        CIntent.ACTION_PACKAGE_REMOVED -> {//需要主动移除掉保存的应用
                            NetKAppUnInstallManager.onUninstallSuccess(apkPackName)
                        }

                        CIntent.ACTION_PACKAGE_ADDED, CIntent.ACTION_PACKAGE_REPLACED -> {//有应用发生变化，强制刷新应用
                            val packageInfo = UtilKPackage.getInstalledPackages(context, false).find { it.packageName == apkPackName }
                            if (packageInfo != null) {
                                NetKAppInstallManager.onInstallSuccess(apkPackName, packageInfo.getVersionCode())
                            } else {
                                NetKAppInstallManager.onInstallSuccess(apkPackName)
                                UtilKLogWrapper.e(TAG, "onReceive: cant find packageInfo just now")
                            }
                        }

                        else -> {}
                    }
                }
            }
        }
    }
}