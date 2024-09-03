package com.mozhimen.taskk.provider.download

import com.mozhimen.kotlin.lintk.optins.permission.OPermission_INTERNET
import com.mozhimen.kotlin.utilk.android.util.UtilKLogWrapper
import com.mozhimen.kotlin.utilk.java.io.UtilKFileDir
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskDownload
import com.mozhimen.taskk.provider.basic.bases.sets.ATaskSetDownload
import com.mozhimen.taskk.provider.basic.cons.CErrorCode
import com.mozhimen.taskk.provider.basic.cons.CTaskState
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
@OptIn(OPermission_INTERNET::class)
class TaskSetDownload(override val providerDefaults: List<ATaskDownload>) : ATaskSetDownload() {
    override val providers: ConcurrentHashMap<String, ATaskDownload> by lazy {
        ConcurrentHashMap(
            providerDefaults.mapNotNull { (it.getSupportFileTasks() as? Map<String, ATaskDownload>)?.toMutableMap() }.fold(emptyMap()) { acc, nex -> acc + nex }
        )
    }

    override fun taskStart(appTask: AppTask) {
        try {
            if (appTask.isTaskProcess() && !appTask.isAnyTaskPause()) {
                UtilKLogWrapper.d(TAG, "taskStart: the task already start")
                return
            }

            if (appTask.taskDownloadFileSizeTotal != 0L) {
                val availMemory = UtilKFileDir.External.getFilesRootFreeSpace()//当前剩余的空间
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

            super.taskStart(appTask)
        } catch (e: TaskException) {
            onTaskFinished(CTaskState.STATE_DOWNLOAD_FAIL, STaskFinishType.FAIL(e), appTask)//onDownloadFail(appTask, exception)
        } catch (e: Exception) {
            onTaskFinished(CTaskState.STATE_DOWNLOAD_FAIL, STaskFinishType.FAIL(TaskException(e)), appTask)
        }
    }
}