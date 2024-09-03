package com.mozhimen.taskk.provider.tradition.impls.install

import android.app.Activity
import android.content.Context
import android.content.IntentFilter
import androidx.lifecycle.LifecycleOwner
import com.mozhimen.kotlin.elemk.android.content.bases.BaseBroadcastReceiver
import com.mozhimen.kotlin.elemk.android.content.bases.BaseBroadcastReceiverProxy2
import com.mozhimen.kotlin.elemk.android.content.cons.CIntent
import com.mozhimen.kotlin.lintk.optins.OApiCall_BindLifecycle
import com.mozhimen.kotlin.lintk.optins.OApiInit_ByLazy
import com.mozhimen.kotlin.lintk.optins.OApiInit_InApplication
import com.mozhimen.basick.stackk.cb.StackKCb
import com.mozhimen.basick.stackk.commons.IStackKListener
import com.mozhimen.kotlin.utilk.android.content.UtilKPackage
import com.mozhimen.kotlin.utilk.android.util.UtilKLogWrapper
import com.mozhimen.kotlin.utilk.wrapper.UtilKSysRom
import com.mozhimen.taskk.provider.basic.db.AppTask

/**
 * @ClassName InstallKFlyme
 * @Description TODO
 * @Author Mozhimen
 * @Date 2023/11/9 9:29
 * @Version 1.0
 */
@OApiInit_InApplication
@OApiCall_BindLifecycle
@OApiInit_ByLazy
class NetKAppInstallProxy(
    context: Context,
    owner: LifecycleOwner,
    receiver: BaseBroadcastReceiver = NetKAppInstallReceiver(),
) : BaseBroadcastReceiverProxy2(
    context, owner, receiver,
    if (UtilKSysRom.isMeizu())
        arrayOf(CIntent.ACTION_PACKAGE_REMOVED)
    else
        arrayOf(CIntent.ACTION_PACKAGE_ADDED, CIntent.ACTION_PACKAGE_REPLACED, CIntent.ACTION_PACKAGE_REMOVED)
), IStackKListener {

    private var _appTask: AppTask? = null

    /////////////////////////////////////////////////////////////////////////////

    fun setAppTask(task: AppTask) {
        _appTask = task
    }

    /////////////////////////////////////////////////////////////////////////////

    override fun registerReceiver() {
        if (UtilKSysRom.isMeizu()) {
            StackKCb.instance.addFrontBackListener(this)
        }
        val intentFilter = IntentFilter()
        if (_actions.isNotEmpty()) {
            for (action in _actions)
                intentFilter.addAction(action)
        }
        intentFilter.addDataScheme("package")
        _activity.registerReceiver(_receiver, intentFilter)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        if (UtilKSysRom.isMeizu()) {
            StackKCb.instance.removeFrontBackListener(this)
        }
        UtilKLogWrapper.d(TAG, "onDestroy: ")
        super.onDestroy(owner)
    }

    override fun onChanged(isFront: Boolean, activity: Activity) {
        if (isFront) {
            if (_appTask != null && UtilKPackage.hasPackage_ofPackageInfo(_context, _appTask!!.apkPackageName)) {
                NetKAppInstallManager.onInstallSuccess(_appTask!!)
            }
            _appTask = null
        }
    }
}