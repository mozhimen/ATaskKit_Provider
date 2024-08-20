package com.mozhimen.netk.app.utils

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.content.Context
import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import com.mozhimen.basick.elemk.android.app.cons.CNotificationManager
import com.mozhimen.basick.utilk.android.app.UtilKNotificationManager
import com.mozhimen.basick.utilk.android.app.UtilKPendingIntentWrapper
import com.mozhimen.basick.utilk.android.content.UtilKApplicationInfo
import com.mozhimen.basick.utilk.android.os.UtilKBuildVersion
import com.mozhimen.netk.app.R
import com.mozhimen.taskk.task.provider.cons.CState
import com.mozhimen.taskk.task.provider.cons.CTaskState
import com.mozhimen.taskk.task.provider.db.AppTask

/**
 *
 * @author by chiclaim@google.com
 */
object NetKAppNotificationUtil {
    const val NETK_APP_NOTIFICATION_CHANNEL_ID = "NETK_APP_NOTIFICATION_CHANNEL_ID"
    const val NETK_APP_NOTIFICATION_GROUP_ID = "NETK_APP_NOTIFICATION_GROUP_ID"

    @JvmStatic
    @SuppressLint("SwitchIntDef")
    fun showNotification(
        context: Context,
        id: Int,
        channelName: CharSequence,
        title: CharSequence,
        appTask: AppTask,
        intent: Intent? = null,
        @DrawableRes notifierSmallIcon: Int = UtilKApplicationInfo.getIcon_ofCxt(context)
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
                !appTask.isTaskProcess() || appTask.taskState == CTaskState.STATE_UNZIP_SUCCESS
            ) // canceled when it is clicked by the user.
            .setOngoing(appTask.isTaskProcess())

        if (appTask.taskDownloadProgress >= 0) {// don't use setContentInfo(deprecated in API level 24)
            builder.setSubText(context.getString(R.string.netk_app_notifier_subtext_placeholder, appTask.taskDownloadProgress))
        }
        when {
//            CDownloadManager.STATUS_SUCCESSFUL -> {
            // click to install
//                file?.let {
//                        val clickIntent = createInstallIntent(context, it)
//                        val pendingIntent = PendingIntent.getActivity(
//                            context,
//                            0,
//                            clickIntent,
//                            getPendingIntentFlag()
//                        )
//                        builder.setContentIntent(pendingIntent)
//                }
//            }
            appTask.taskState == CState.STATE_TASK_SUCCESS -> {
                intent?.let {
                    builder.setContentIntent(UtilKPendingIntentWrapper.get_ofActivity_IMMUTABLE(0, it))
                }
            }

            appTask.isTaskUnzipSuccess() -> {
                intent?.let {
                    builder.setContentIntent(UtilKPendingIntentWrapper.get_ofActivity_IMMUTABLE(0, it))
                }
            }

            appTask.isTasking() -> {
                builder.setProgress(
                    100,
                    appTask.taskDownloadProgress,
                    appTask.taskDownloadProgress <= 0 || appTask.taskState == CTaskState.STATE_INSTALLING || appTask.taskState == CTaskState.STATE_VERIFYING/*percent <= 0*/
                )
            }

//            CDownloadManager.STATUS_FAILED -> {
//                val intent =
//                    Intent("${context.packageName}.DownloadService")
//                intent.setPackage(context.packageName)
//                intent.putExtra(CDownloadParameter.EXTRA_URL, url)
//                intent.putExtra(CDownloadParameter.EXTRA_FROM, CDownloadParameter.EXTRA_FROM_NOTIFIER)
//                val pendingIntent =
//                    PendingIntent.getService(context, 1, intent, UtilKPendingIntent.getFlagOfUpdate())
//                builder.setContentIntent(pendingIntent)
//                //builder.addAction(NotificationCompat.Action(null, null, pendingIntent))
//            }
        }
        notificationManager.notify(id, builder.build())
    }

    @JvmStatic
    fun cancelNotification(context: Context, id: Int) {
        UtilKNotificationManager.get(context).cancel(id)
    }
}