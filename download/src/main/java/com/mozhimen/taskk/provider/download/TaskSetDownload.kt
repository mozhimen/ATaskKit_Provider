package com.mozhimen.taskk.provider.download

import com.mozhimen.kotlin.lintk.optins.OApiInit_InApplication
import com.mozhimen.kotlin.lintk.optins.permission.OPermission_INTERNET
import com.mozhimen.kotlin.utilk.android.util.UtilKLogWrapper
import com.mozhimen.kotlin.utilk.java.io.UtilKFileDir
import com.mozhimen.taskk.provider.basic.annors.ATaskQueueName
import com.mozhimen.taskk.provider.basic.annors.ATaskState
import com.mozhimen.taskk.provider.basic.bases.ATaskManager
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskDownload
import com.mozhimen.taskk.provider.basic.bases.sets.ATaskSetDownload
import com.mozhimen.taskk.provider.basic.cons.CErrorCode
import com.mozhimen.taskk.provider.basic.cons.STaskFinishType
import com.mozhimen.taskk.provider.basic.db.AppTask
import com.mozhimen.taskk.provider.basic.impls.TaskException
import com.mozhimen.taskk.provider.basic.impls.intErrorCode2taskException
import java.util.concurrent.ConcurrentHashMap

/**
 * @ClassName TaskProviderSetDownload
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/20
 * @Version 1.0
 */
@OptIn(OPermission_INTERNET::class, OApiInit_InApplication::class)
class TaskSetDownload constructor(taskManager: ATaskManager, override val providerDefaults: List<ATaskDownload>) : ATaskSetDownload(taskManager) {
    override val providers: ConcurrentHashMap<String, ATaskDownload> by lazy {
        ConcurrentHashMap(
            providerDefaults.mapNotNull { (it.getSupportFileTasks() as? Map<String, ATaskDownload>)?.toMutableMap() }.fold(emptyMap()) { acc, nex -> acc + nex }
        )
    }

    @OptIn(OApiInit_InApplication::class)
    override fun taskStart(appTask: AppTask, @ATaskQueueName taskQueueName: String) {
        try {
            if (appTask.isTaskProcess(_taskManager, taskQueueName) && !appTask.isAnyTaskPause()) {
                UtilKLogWrapper.d(TAG, "taskStart: the task already start")
                return
            }

            if (appTask.taskDownloadFileSizeTotal != 0L) {
                val availMemory = UtilKFileDir.External.getFiles_freeSpace()//当前剩余的空间
                val needMinMemory: Long = (appTask.taskDownloadFileSizeTotal * 1.2).toLong()//需要的最小空间
                if (availMemory < needMinMemory) {//如果当前需要的空间大于剩余空间，提醒清理空间
                    throw CErrorCode.CODE_TASK_NEED_MEMORY.intErrorCode2taskException()
                }

                if (appTask.taskUnzipEnable) {
                    val warningsMemory: Long = (appTask.taskDownloadFileSizeTotal * 2.2).toLong()//警告空间
                    if (availMemory < warningsMemory) {//如果当前空间小于警告空间，
                        throw CErrorCode.CODE_TASK_NEED_MEMORY.intErrorCode2taskException()
                    }
                }
            }

            super.taskStart(appTask, taskQueueName)
        } catch (e: TaskException) {
            onTaskFinished(ATaskState.STATE_DOWNLOAD_FAIL, appTask, taskQueueName, STaskFinishType.FAIL(e))//onDownloadFail(appTask, exception)
        } catch (e: Exception) {
            onTaskFinished(ATaskState.STATE_DOWNLOAD_FAIL, appTask, taskQueueName, STaskFinishType.FAIL(TaskException(e)))
        }
    }
}