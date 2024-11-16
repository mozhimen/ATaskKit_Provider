package com.mozhimen.taskk.provider.apk.utils

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.content.Context
import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import com.mozhimen.kotlin.elemk.android.app.cons.CNotificationManager
import com.mozhimen.kotlin.lintk.optins.OApiInit_InApplication
import com.mozhimen.kotlin.utilk.android.app.UtilKNotificationManager
import com.mozhimen.kotlin.utilk.android.app.UtilKPendingIntentWrapper
import com.mozhimen.kotlin.utilk.android.content.UtilKApplicationInfo
import com.mozhimen.kotlin.utilk.android.os.UtilKBuildVersion
import com.mozhimen.taskk.provider.basic.db.AppTask
import com.mozhimen.taskk.provider.apk.R
import com.mozhimen.taskk.provider.basic.annors.ATaskNodeQueueName
import com.mozhimen.taskk.provider.basic.bases.ATaskManager

/**
 *
 * @author by chiclaim@google.com
 */
object NotificationUtil {
    const val NETK_APP_NOTIFICATION_CHANNEL_ID = "NETK_APP_NOTIFICATION_CHANNEL_ID"
    const val NETK_APP_NOTIFICATION_GROUP_ID = "NETK_APP_NOTIFICATION_GROUP_ID"

    @OptIn(OApiInit_InApplication::class)
    @JvmStatic
    @SuppressLint("SwitchIntDef")
    fun showNotification(
        context: Context,
        id: Int,
        channelName: CharSequence,
        title: CharSequence,
        appTask: AppTask,
        taskManager: ATaskManager,
        @ATaskNodeQueueName taskNodeQueueName: String,
        intent: Intent? = null,
        @DrawableRes notifierSmallIcon: Int = UtilKApplicationInfo.getIcon(context)
    ) {
        val notificationManager = UtilKNotificationManager.get(context)

        // 在 Android 8.0 及更高版本上，需要在系统中注册应用的通知渠道
        if (UtilKBuildVersion.isAfterV_26_8_O()) {
            val notificationChannel = NotificationChannel(NETK_APP_NOTIFICATION_CHANNEL_ID, channelName, CNotificationManager.IMPORTANCE_LOW)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        val builder = NotificationCompat.Builder(context, NETK_APP_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(notifierSmallIcon)
            .setContentTitle(title)
            .setAutoCancel(
                !appTask.isTaskProcess(taskManager, taskNodeQueueName) || appTask.isTaskUnzipSuccess()
            ) // canceled when it is clicked by the user.
            .setOngoing(appTask.isTaskProcess(taskManager, taskNodeQueueName))

        if (appTask.taskDownloadProgress >= 0) {// don't use setContentInfo(deprecated in API level 24)
            builder.setSubText(context.getString(R.string.netk_app_notifier_subtext_placeholder, appTask.taskDownloadProgress))
        }

        //状态
        when {
            appTask.isTaskSuccess(taskManager, taskNodeQueueName) -> {
                intent?.let {
                    builder.setContentIntent(UtilKPendingIntentWrapper.get_ofActivity_IMMUTABLE(0, it))
                }
            }

            appTask.isTaskUnzipSuccess()->{
                intent?.let {
                    builder.setContentIntent(UtilKPendingIntentWrapper.get_ofActivity_IMMUTABLE(0, it))
                }
            }

            appTask.isAnyTasking() -> {
                builder.setProgress(100, appTask.taskDownloadProgress, appTask.taskDownloadProgress <= 0 || appTask.isAnyTasking())
            }
        }

        //
        notificationManager.notify(id, builder.build())
    }

    @JvmStatic
    fun cancelNotification(context: Context, id: Int) {
        UtilKNotificationManager.get(context).cancel(id)
    }
}