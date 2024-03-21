package com.mozhimen.netk.app.install

import android.app.Activity
import android.content.Context
import android.content.IntentFilter
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import com.mozhimen.basick.elemk.android.content.bases.BaseBroadcastReceiver
import com.mozhimen.basick.elemk.android.content.bases.BaseBroadcastReceiverProxy2
import com.mozhimen.basick.elemk.android.content.cons.CIntent
import com.mozhimen.basick.lintk.optins.OApiCall_BindLifecycle
import com.mozhimen.basick.lintk.optins.OApiInit_ByLazy
import com.mozhimen.basick.lintk.optins.OApiInit_InApplication
import com.mozhimen.basick.stackk.cb.StackKCb
import com.mozhimen.basick.stackk.commons.IStackKListener
import com.mozhimen.basick.utilk.android.content.UtilKPackage
import com.mozhimen.basick.utilk.wrapper.UtilKSysRom
import com.mozhimen.netk.app.install.helpers.NetKAppInstallReceiver
import com.mozhimen.netk.app.task.db.AppTask
import java.lang.ref.WeakReference

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
    if (UtilKSysRom.isFlyme())
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
        if (UtilKSysRom.isFlyme()) {
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
        if (UtilKSysRom.isFlyme()) {
            StackKCb.instance.removeFrontBackListener(this)
        }
        Log.d(TAG, "onDestroy: ")
        super.onDestroy(owner)
    }

    override fun onChanged(isFront: Boolean, activityRef: WeakReference<Activity>?) {
        if (isFront) {
            if (_appTask != null && UtilKPackage.hasPackage(_context, _appTask!!.apkPackageName)) {
                NetKAppInstallManager.onInstallSuccess(_appTask!!)
            }
            _appTask = null
        }
    }
}