package com.mozhimen.taskk.provider.apk.helpers

/**
 * @ClassName Proxy
 * @Description TODO
 * @Author Mozhimen / Kolin Zhao
 * @Date 2024/8/25 2:02
 * @Version 1.0
 */
import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleOwner
import com.mozhimen.basick.bases.BaseWakeBefDestroyLifecycleObserver
import com.mozhimen.kotlin.elemk.android.app.cons.CNotificationManager
import com.mozhimen.kotlin.lintk.optins.OApiCall_BindLifecycle
import com.mozhimen.kotlin.lintk.optins.OApiInit_ByLazy
import com.mozhimen.kotlin.lintk.optins.OApiInit_InApplication
import com.mozhimen.kotlin.utilk.android.app.UtilKNotificationManager
import com.mozhimen.kotlin.utilk.android.app.UtilKPendingIntentWrapper
import com.mozhimen.kotlin.utilk.android.content.UtilKApplicationInfo
import com.mozhimen.taskk.provider.apk.utils.NotificationUtil
import com.mozhimen.taskk.provider.basic.db.AppTask
import com.mozhimen.taskk.provider.apk.R
import com.mozhimen.taskk.provider.basic.annors.ATaskQueueName
import com.mozhimen.taskk.provider.basic.bases.ATaskManager

/**
 * @ClassName NetKAppNotificationProxy
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2024/1/3
 * @Version 1.0
 */
@OApiInit_ByLazy
@OApiCall_BindLifecycle
class NotificationProxy : BaseWakeBefDestroyLifecycleObserver() {
    private val _builders = mutableMapOf<Int, NotificationCompat.Builder>()
    private val _notificationManager by lazy {
        UtilKNotificationManager.get(_context)
    }

    fun init(channelName: String) {
        UtilKNotificationManager.createNotificationChannel(_notificationManager, NotificationUtil.NETK_APP_NOTIFICATION_CHANNEL_ID, channelName, CNotificationManager.IMPORTANCE_LOW)
    }

    @OptIn(OApiInit_InApplication::class)
    fun showNotification(
        id: Int,
        title: String,
        appTask: AppTask,
        taskManager: ATaskManager,
        @ATaskQueueName taskQueueName: String,
        intent: Intent? = null,
        @DrawableRes notifierSmallIcon: Int = UtilKApplicationInfo.getIcon(_context)
    ) {
        val builder: NotificationCompat.Builder = _builders[id] ?: run {
            NotificationCompat.Builder(_context, NotificationUtil.NETK_APP_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(notifierSmallIcon)
                .also { _builders[id] = it }
        }
        //标题
        builder.apply {
            setContentTitle(title)
            setAutoCancel(
                !appTask.isTaskProcess(taskManager, taskQueueName) || appTask.isTaskUnzipSuccess()
            ) // canceled when it is clicked by the user.
            setOngoing(appTask.isTaskProcess(taskManager, taskQueueName))
        }
        //子标题
        if (appTask.taskDownloadProgress in 1..99) {// don't use setContentInfo(deprecated in API level 24)
            builder.setSubText(_context.getString(R.string.netk_app_notifier_subtext_placeholder, appTask.taskDownloadProgress))
        } else {
            builder.setSubText("")
        }
        when {
            appTask.isTaskSuccess(taskManager, taskQueueName) -> {
                intent?.let {
                    builder.setContentIntent(UtilKPendingIntentWrapper.get_ofActivity_IMMUTABLE(0, it))
                }
                builder.setProgress(0, 0, false)
            }

            appTask.isTaskUnzipSuccess() -> {
                intent?.let {
                    builder.setContentIntent(UtilKPendingIntentWrapper.get_ofActivity_IMMUTABLE(0, it))
                }
                builder.setProgress(0, 0, false)
            }

            appTask.isAnyTasking() -> {
                builder.setProgress(
                    100,
                    appTask.taskDownloadProgress,
                    appTask.taskDownloadProgress <= 0 || appTask.taskDownloadProgress >= 100 || appTask.isTaskInstalling() || appTask.isTaskVerifying()/*percent <= 0*/
                )
            }
        }
        _notificationManager.notify(id, builder.build())
    }

    fun cancelNotification(id: Int) {
        UtilKNotificationManager.get(_context).cancel(id)
        if (_builders.containsKey(id)) {
            _builders.remove(id)
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        try {
            _builders.forEach { (t, _) ->
                UtilKNotificationManager.get(_context).cancel(t)
            }
            _builders.clear()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onDestroy(owner)
    }
}