package com.mozhimen.netk.app.helpers

import android.app.NotificationChannel
import android.content.Context
import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleOwner
import com.mozhimen.basick.elemk.android.app.cons.CNotificationManager
import com.mozhimen.basick.elemk.androidx.lifecycle.bases.BaseWakeBefDestroyLifecycleObserver
import com.mozhimen.basick.lintk.optin.OptInApiCall_BindLifecycle
import com.mozhimen.basick.lintk.optin.OptInApiInit_ByLazy
import com.mozhimen.basick.utilk.android.app.UtilKNotificationChannel
import com.mozhimen.basick.utilk.android.app.UtilKNotificationManager
import com.mozhimen.basick.utilk.android.app.UtilKPendingIntent
import com.mozhimen.basick.utilk.android.content.UtilKApplicationInfo
import com.mozhimen.basick.utilk.android.os.UtilKBuildVersion
import com.mozhimen.netk.app.R
import com.mozhimen.netk.app.cons.CNetKAppState
import com.mozhimen.netk.app.task.cons.CNetKAppTaskState
import com.mozhimen.netk.app.task.db.AppTask
import com.mozhimen.netk.app.utils.NetKAppNotificationUtil

/**
 * @ClassName NetKAppNotificationProxy
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2024/1/3
 * @Version 1.0
 */
@OptInApiInit_ByLazy
@OptInApiCall_BindLifecycle
class NetKAppNotificationProxy(private val _activity: AppCompatActivity) : BaseWakeBefDestroyLifecycleObserver() {
    private val _builders = mutableMapOf<Int, NotificationCompat.Builder>()
    private val _notificationManager by lazy {
        UtilKNotificationManager.get(_context)
    }

    fun init(channelName: String) {
        UtilKNotificationManager.createNotificationChannel(_notificationManager, NetKAppNotificationUtil.NETK_APP_NOTIFICATION_CHANNEL_ID, channelName, CNotificationManager.IMPORTANCE_LOW)
    }

    fun showNotification(
        id: Int,
        title: String,
        appTask: AppTask,
        intent: Intent? = null,
        @DrawableRes notifierSmallIcon: Int = UtilKApplicationInfo.getIcon(_activity)
    ) {
        val builder: NotificationCompat.Builder = _builders[id] ?: run {
            NotificationCompat.Builder(_activity, NetKAppNotificationUtil.NETK_APP_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(notifierSmallIcon)
                .also { _builders[id] = it }
        }
        //标题
        builder.apply {
            setContentTitle(title)
            setAutoCancel(
                !appTask.isTaskProcess() || appTask.taskState == CNetKAppState.STATE_UNZIP_SUCCESS
            ) // canceled when it is clicked by the user.
            setOngoing(appTask.isTaskProcess())
        }
        //子标题
        if (appTask.downloadProgress in 1..99) {// don't use setContentInfo(deprecated in API level 24)
            builder.setSubText(_activity.getString(R.string.netk_app_notifier_subtext_placeholder, appTask.downloadProgress))
        } else {
            builder.setSubText("")
        }
        when {
            appTask.taskState == CNetKAppTaskState.STATE_TASK_SUCCESS -> {
                intent?.let {
                    builder.setContentIntent(UtilKPendingIntent.getActivity(_activity, it))
                }
                builder.setProgress(0, 0, false)
            }

            appTask.taskState == CNetKAppState.STATE_UNZIP_SUCCESS -> {
                intent?.let {
                    builder.setContentIntent(UtilKPendingIntent.getActivity(_activity, it))
                }
                builder.setProgress(0, 0, false)
            }

            appTask.isTasking() -> {
                builder.setProgress(
                    100,
                    appTask.downloadProgress,
                    appTask.downloadProgress <= 0 || appTask.downloadProgress >= 100 || appTask.taskState == CNetKAppState.STATE_INSTALLING || appTask.taskState == CNetKAppState.STATE_VERIFYING/*percent <= 0*/
                )
            }
        }
        _notificationManager.notify(id, builder.build())
    }

    fun cancelNotification(id: Int) {
        UtilKNotificationManager.get(_activity).cancel(id)
        if (_builders.containsKey(id)) {
            _builders.remove(id)
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        try {
            _builders.forEach { (t, _) ->
                UtilKNotificationManager.get(_activity).cancel(t)
            }
            _builders.clear()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onDestroy(owner)
    }
}