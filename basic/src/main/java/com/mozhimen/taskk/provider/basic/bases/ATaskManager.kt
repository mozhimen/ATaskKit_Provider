package com.mozhimen.taskk.provider.basic.bases

import android.content.Context
import android.util.Log
import androidx.annotation.CallSuper
import com.mozhimen.kotlin.elemk.commons.IA_Listener
import com.mozhimen.kotlin.lintk.optins.OApiInit_InApplication
import com.mozhimen.kotlin.lintk.optins.permission.OPermission_INTERNET
import com.mozhimen.kotlin.utilk.android.util.UtilKLogWrapper
import com.mozhimen.kotlin.utilk.bases.BaseUtilK
import com.mozhimen.taskk.provider.basic.annors.AFileExt
import com.mozhimen.taskk.provider.basic.annors.AState
import com.mozhimen.taskk.provider.basic.annors.ATaskName
import com.mozhimen.taskk.provider.basic.annors.ATaskNodeQueueName
import com.mozhimen.taskk.provider.basic.annors.ATaskState
import com.mozhimen.taskk.provider.basic.annors.getTaskCode
import com.mozhimen.taskk.provider.basic.annors.taskName2taskState
import com.mozhimen.taskk.provider.basic.bases.sets.ATaskSetDelete
import com.mozhimen.taskk.provider.basic.bases.sets.ATaskSetDownload
import com.mozhimen.taskk.provider.basic.bases.sets.ATaskSetInstall
import com.mozhimen.taskk.provider.basic.bases.sets.ATaskSetOpen
import com.mozhimen.taskk.provider.basic.bases.sets.ATaskSetUninstall
import com.mozhimen.taskk.provider.basic.bases.sets.ATaskSetUnzip
import com.mozhimen.taskk.provider.basic.bases.sets.ATaskSetVerify
import com.mozhimen.taskk.provider.basic.cons.STaskFinishType
import com.mozhimen.taskk.provider.basic.db.AppTask
import com.mozhimen.taskk.provider.basic.db.AppTaskDaoManager
import com.mozhimen.taskk.provider.basic.impls.TaskException
import com.mozhimen.taskk.provider.basic.commons.ITask
import com.mozhimen.taskk.provider.basic.commons.ITaskEvent
import com.mozhimen.taskk.provider.basic.commons.ITaskLifecycle
import com.mozhimen.taskk.provider.basic.commons.ITasks
import com.mozhimen.taskk.provider.basic.commons.ITasks2
import com.mozhimen.taskk.provider.basic.cons.STaskNode
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @ClassName ITaskProviderSets
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/20
 * @Version 1.0
 */
@OApiInit_InApplication
abstract class ATaskManager : BaseUtilK(), ITask, ITaskEvent {
    protected val _isInit = AtomicBoolean(false)
    protected val _taskListeners = CopyOnWriteArrayList<ITasks>()
    protected val _task2Listeners = CopyOnWriteArrayList<ITasks2>()
    protected val _taskSets: ConcurrentHashMap<@ATaskName String, ATaskSet<*>> by lazy {
        ConcurrentHashMap<String, ATaskSet<*>>(
            getTaskSets().associateBy { it.getTaskName() }
        )
    }

    //一个文件后缀的作用的任务链
    protected val _taskNodeQuenes by lazy {
        ConcurrentHashMap<@AFileExt String, Map<@ATaskNodeQueueName String, List<STaskNode>>>(getTaskNodeQueues())
    }

    protected val _iTaskLifecycle: ITaskLifecycle = object : ITaskLifecycle {
        override fun onTaskStarted(taskState: Int, appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String) {
            if (appTask.isAnyTasking()) {
                onTask_ofIng(appTask, taskNodeQueueName, appTask.taskDownloadProgress, appTask.taskDownloadFileSizeOffset, appTask.taskDownloadFileSizeTotal, appTask.taskDownloadFileSpeed)
            } else {
                onTask_ofOther(appTask, taskNodeQueueName)
            }
        }

        override fun onTaskPaused(taskState: Int, appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String) {
            onTask_ofOther(appTask, taskNodeQueueName)
        }

        override fun onTaskFinished(taskState: Int, appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String, finishType: STaskFinishType) {
            when (finishType) {
                STaskFinishType.SUCCESS -> onTask_ofOther(appTask, taskNodeQueueName)
                STaskFinishType.CANCEL -> onTask_ofOther(appTask, taskNodeQueueName)
                is STaskFinishType.FAIL -> onTask_ofFail(appTask, taskNodeQueueName, finishType.exception)
            }
        }
    }

    /////////////////////////////////////////////////////////////////

    @CallSuper
    open fun init(context: Context) {
        if (hasInit()) return
        _isInit.compareAndSet(false, true)
        AppTaskDaoManager.init()
        getTaskSets().forEach {
            it.init(context)
        }
        getTaskProviders().forEach {
            it.init(context)
        }
        UtilKLogWrapper.d(TAG, "init: ")
    }

    fun hasInit(): Boolean =
        _isInit.get().also { UtilKLogWrapper.d(TAG, "hasInit: $it") }

    fun registerTaskListener(listener: ITasks) {
        if (!_taskListeners.contains(listener)) {
            _taskListeners.add(listener)
        }
    }

    fun registerTask2Listener(listener: ITasks2) {
        if (!_task2Listeners.contains(listener)) {
            _task2Listeners.add(listener)
        }
    }

    fun unregisterTaskListener(listener: ITasks) {
        val indexOf = _taskListeners.indexOf(listener)
        if (indexOf >= 0)
            _taskListeners.removeAt(indexOf)
    }

    fun unregisterTask2Listener(listener: ITasks2) {
        val indexOf = _task2Listeners.indexOf(listener)
        if (indexOf >= 0)
            _task2Listeners.removeAt(indexOf)
    }

    fun getAppTaskDaoManager(): AppTaskDaoManager =
        AppTaskDaoManager

    /////////////////////////////////////////////////////////////////

    fun getTaskSet(@ATaskName taskName: String): ATaskSet<*>? {
        return _taskSets[taskName].also { UtilKLogWrapper.d(TAG, "getTaskSet: taskName $taskName taskSet $it") }
    }

//    fun getNextTaskSet(fileExt: String, @ATaskNodeQueueName taskNodeQueueName: String, @ATaskName taskName: String): ATaskSet<*>? {
//        return getNextTaskName(fileExt, taskNodeQueueName, taskName)?.let { getTaskSet(it) }
//    }

    /////////////////////////////////////////////////////////////////

    fun getTaskNodeQueue(@AFileExt fileExt: String, @ATaskNodeQueueName taskNodeQueueName: String): List<STaskNode>? {
        return _taskNodeQuenes.get(fileExt)?.get(taskNodeQueueName)
    }

    fun getCurrTaskNode_ofTaskNodeQueue(appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String, firstTaskNode_ofTaskNodeQueue: STaskNode? = null): STaskNode? {
        return appTask.getCurrentTaskNode(this, taskNodeQueueName, firstTaskNode_ofTaskNodeQueue)
    }

    fun getNextTaskNode_ofTaskNodeQueue(@AFileExt fileExt: String, @ATaskNodeQueueName taskNodeQueueName: String, currTaskNode: STaskNode): STaskNode? {
        val taskNodeQueue = getTaskNodeQueue(fileExt, taskNodeQueueName) ?: return null
        val currentIndex = taskNodeQueue.indexOf(currTaskNode)
        if (currentIndex < 0) return null
        val nextIndex = currentIndex + 1
        return (if (nextIndex in taskNodeQueue.indices) {
            taskNodeQueue[nextIndex]
        } else
            null).also { UtilKLogWrapper.d(TAG, "getNextTaskName: $it") }
    }

    fun hasTaskNode_ofTaskNodeQueue(@AFileExt fileExt: String, @ATaskNodeQueueName taskNodeQueueName: String, taskNode: STaskNode): Boolean {
        return getTaskNodeQueue(fileExt, taskNodeQueueName)?.contains(taskNode) ?: false
    }

    fun getLastTaskNode_ofTaskNodeQueue(@AFileExt fileExt: String, @ATaskNodeQueueName taskNodeQueueName: String): STaskNode? {
        return getTaskNodeQueue(fileExt, taskNodeQueueName)?.lastOrNull()
    }

    fun getFirstTaskNode_ofTaskNodeQueue(@AFileExt fileExt: String, @ATaskNodeQueueName taskNodeQueueName: String): STaskNode? {
        return getTaskNodeQueue(fileExt, taskNodeQueueName)?.firstOrNull()
    }

    fun getPrevTaskNode_ofTaskNodeQueue(@AFileExt fileExt: String, @ATaskNodeQueueName taskNodeQueueName: String, taskNode: STaskNode?): STaskNode? {
        taskNode ?: run {
            Log.d(TAG, "getPrevTaskNode_ofTaskNodeQueue: currentTaskName is null")
            return null
        }
        val indexOf = getTaskNodeQueue(fileExt, taskNodeQueueName)?.indexOf(taskNode)
        if (indexOf != null && indexOf != -1) {
            val preIndexOf = indexOf - 1
            return (if (preIndexOf < 0) {
                null
            } else {
                getTaskNodeQueue(fileExt, taskNodeQueueName)?.get(preIndexOf)
            }).also { Log.d(TAG, "getPrevTaskNode_ofTaskNodeQueue: $it") }
        } else
            return null
    }

    fun getTaskNodeQueueName_ofTaskNode(@AFileExt fileExt: String, taskNode: STaskNode): @ATaskNodeQueueName String? {
        val taskNodeQueue = _taskNodeQuenes.get(fileExt) ?: return null
        return taskNodeQueue.filter { it.value.contains(taskNode) }.keys.firstOrNull()
    }

    /////////////////////////////////////////////////////////////////

    abstract fun getTaskNodeQueues(): Map<String, Map<String, List<STaskNode>>>
    abstract fun getTaskSets(): List<ATaskSet<*>>
    abstract fun getTaskProviders(): List<ATaskProvider>

    /////////////////////////////////////////////////////////////////

    @OPermission_INTERNET
    fun getTaskSetDownload(): ATaskSetDownload? {
        return getTaskSet(ATaskName.TASK_DOWNLOAD) as? ATaskSetDownload
    }

    fun getTaskSetVerify(): ATaskSetVerify? {
        return getTaskSet(ATaskName.TASK_VERIFY) as? ATaskSetVerify
    }

    fun getTaskSetUnzip(): ATaskSetUnzip? {
        return getTaskSet(ATaskName.TASK_UNZIP) as? ATaskSetUnzip
    }

    fun getTaskSetInstall(): ATaskSetInstall? {
        return getTaskSet(ATaskName.TASK_INSTALL) as? ATaskSetInstall
    }

    fun getTaskSetOpen(): ATaskSetOpen? {
        return getTaskSet(ATaskName.TASK_OPEN) as? ATaskSetOpen
    }

    fun getTaskSetUninstall(): ATaskSetUninstall? {
        return getTaskSet(ATaskName.TASK_UNINSTALL) as? ATaskSetUninstall
    }

    fun getTaskSetDelete(): ATaskSetDelete? {
        return getTaskSet(ATaskName.TASK_DELETE) as? ATaskSetDelete
    }

    /////////////////////////////////////////////////////////////////

    suspend fun taskStart(appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String, finishType: STaskFinishType, @ATaskState taskState: Int): AppTask =
        suspendCancellableCoroutine { coroutine ->
            taskStart(appTask, taskNodeQueueName, finishType, taskState) {
                coroutine.resumeWith(Result.success(appTask))
            }
        }

    fun taskStart(appTaskInner: AppTask, @ATaskNodeQueueName taskNodeQueueName: String, finishType: STaskFinishType, @ATaskState taskState: Int, listener: IA_Listener<AppTask>) {
        registerTaskListener(object : ITasks {
            override fun onTaskCreate(appTask: AppTask, taskNodeQueueName: String, isUpdate: Boolean) {
                if (appTaskInner.id == appTask.id) {
                    unregisterTaskListener(this)
                }
            }

            override fun onTaskDownloadCancel(appTask: AppTask) {
                if (appTaskInner.id == appTask.id && finishType == STaskFinishType.CANCEL && taskState.getTaskCode() == ATaskState.STATE_DOWNLOAD_CREATE) {
                    unregisterTaskListener(this)
                    listener.invoke(appTask)
                }
            }

            override fun onTaskDownloadSuccess(appTask: AppTask) {
                if (appTaskInner.id == appTask.id && finishType == STaskFinishType.SUCCESS && taskState.getTaskCode() == ATaskState.STATE_DOWNLOAD_CREATE) {
                    unregisterTaskListener(this)
                    listener.invoke(appTask)
                }
            }

            override fun onTaskDownloadFail(appTask: AppTask, exception: TaskException) {
                if (appTaskInner.id == appTask.id && finishType is STaskFinishType.FAIL && taskState.getTaskCode() == ATaskState.STATE_DOWNLOAD_CREATE) {
                    unregisterTaskListener(this)
                    listener.invoke(appTask)
                }
            }

            /////////////////////////////////////////////////////////

            override fun onTaskVerifyCancel(appTask: AppTask) {
                if (appTaskInner.id == appTask.id && finishType == STaskFinishType.CANCEL && taskState.getTaskCode() == ATaskState.STATE_VERIFY_CREATE) {
                    unregisterTaskListener(this)
                    listener.invoke(appTask)
                }
            }

            override fun onTaskVerifySuccess(appTask: AppTask) {
                if (appTaskInner.id == appTask.id && finishType == STaskFinishType.SUCCESS && taskState.getTaskCode() == ATaskState.STATE_VERIFY_CREATE) {
                    unregisterTaskListener(this)
                    listener.invoke(appTask)
                }
            }

            override fun onTaskVerifyFail(appTask: AppTask, exception: TaskException) {
                if (appTaskInner.id == appTask.id && finishType is STaskFinishType.FAIL && taskState.getTaskCode() == ATaskState.STATE_VERIFY_CREATE) {
                    unregisterTaskListener(this)
                    listener.invoke(appTask)
                }
            }

            /////////////////////////////////////////////////////////

            override fun onTaskUnzipCancel(appTask: AppTask) {
                if (appTaskInner.id == appTask.id && finishType == STaskFinishType.CANCEL && taskState.getTaskCode() == ATaskState.STATE_UNZIP_CREATE) {
                    unregisterTaskListener(this)
                    listener.invoke(appTask)
                }
            }

            override fun onTaskUnzipSuccess(appTask: AppTask) {
                if (appTaskInner.id == appTask.id && finishType == STaskFinishType.SUCCESS && taskState.getTaskCode() == ATaskState.STATE_UNZIP_CREATE) {
                    unregisterTaskListener(this)
                    listener.invoke(appTask)
                }
            }

            override fun onTaskUnzipFail(appTask: AppTask, exception: TaskException) {
                if (appTaskInner.id == appTask.id && finishType is STaskFinishType.FAIL && taskState.getTaskCode() == ATaskState.STATE_UNZIP_CREATE) {
                    unregisterTaskListener(this)
                    listener.invoke(appTask)
                }
            }

            /////////////////////////////////////////////////////////

            override fun onTaskInstallCancel(appTask: AppTask) {
                if (appTaskInner.id == appTask.id && finishType == STaskFinishType.CANCEL && taskState.getTaskCode() == ATaskState.STATE_INSTALL_CREATE) {
                    unregisterTaskListener(this)
                    listener.invoke(appTask)
                }
            }

            override fun onTaskInstallSuccess(appTask: AppTask) {
                if (appTaskInner.id == appTask.id && finishType == STaskFinishType.SUCCESS && taskState.getTaskCode() == ATaskState.STATE_INSTALL_CREATE) {
                    unregisterTaskListener(this)
                    listener.invoke(appTask)
                }
            }

            override fun onTaskInstallFail(appTask: AppTask, exception: TaskException) {
                if (appTaskInner.id == appTask.id && finishType is STaskFinishType.FAIL && taskState.getTaskCode() == ATaskState.STATE_INSTALL_CREATE) {
                    unregisterTaskListener(this)
                    listener.invoke(appTask)
                }
            }

            /////////////////////////////////////////////////////////

            override fun onTaskOpenCancel(appTask: AppTask) {
                if (appTaskInner.id == appTask.id && finishType == STaskFinishType.CANCEL && taskState.getTaskCode() == ATaskState.STATE_OPEN_CREATE) {
                    unregisterTaskListener(this)
                    listener.invoke(appTask)
                }
            }

            override fun onTaskOpenSuccess(appTask: AppTask) {
                if (appTaskInner.id == appTask.id && finishType == STaskFinishType.SUCCESS && taskState.getTaskCode() == ATaskState.STATE_OPEN_CREATE) {
                    unregisterTaskListener(this)
                    listener.invoke(appTask)
                }
            }

            override fun onTaskOpenFail(appTask: AppTask, exception: TaskException) {
                if (appTaskInner.id == appTask.id && finishType is STaskFinishType.FAIL && taskState.getTaskCode() == ATaskState.STATE_OPEN_CREATE) {
                    unregisterTaskListener(this)
                    listener.invoke(appTask)
                }
            }

            /////////////////////////////////////////////////////////

            override fun onTaskCloseCancel(appTask: AppTask) {
                if (appTaskInner.id == appTask.id && finishType == STaskFinishType.CANCEL && taskState.getTaskCode() == ATaskState.STATE_CLOSE_CREATE) {
                    unregisterTaskListener(this)
                    listener.invoke(appTask)
                }
            }

            override fun onTaskCloseSuccess(appTask: AppTask) {
                if (appTaskInner.id == appTask.id && finishType == STaskFinishType.SUCCESS && taskState.getTaskCode() == ATaskState.STATE_CLOSE_CREATE) {
                    unregisterTaskListener(this)
                    listener.invoke(appTask)
                }
            }

            override fun onTaskCloseFail(appTask: AppTask, exception: TaskException) {
                if (appTaskInner.id == appTask.id && finishType is STaskFinishType.FAIL && taskState.getTaskCode() == ATaskState.STATE_CLOSE_CREATE) {
                    unregisterTaskListener(this)
                    listener.invoke(appTask)
                }
            }

            /////////////////////////////////////////////////////////

            override fun onTaskUninstallCancel(appTask: AppTask) {
                if (appTaskInner.id == appTask.id && finishType == STaskFinishType.CANCEL && taskState.getTaskCode() == ATaskState.STATE_UNINSTALL_CREATE) {
                    unregisterTaskListener(this)
                    listener.invoke(appTask)
                }
            }

            override fun onTaskUninstallSuccess(appTask: AppTask) {
                if (appTaskInner.id == appTask.id && finishType == STaskFinishType.SUCCESS && taskState.getTaskCode() == ATaskState.STATE_UNINSTALL_CREATE) {
                    unregisterTaskListener(this)
                    listener.invoke(appTask)
                }
            }

            override fun onTaskUninstallFail(appTask: AppTask, exception: TaskException) {
                if (appTaskInner.id == appTask.id && finishType is STaskFinishType.FAIL && taskState.getTaskCode() == ATaskState.STATE_UNINSTALL_CREATE) {
                    unregisterTaskListener(this)
                    listener.invoke(appTask)
                }
            }

            /////////////////////////////////////////////////////////

            override fun onTaskDeleteCancel(appTask: AppTask) {
                if (appTaskInner.id == appTask.id && finishType == STaskFinishType.CANCEL && taskState.getTaskCode() == ATaskState.STATE_DELETE_CREATE) {
                    unregisterTaskListener(this)
                    listener.invoke(appTask)
                }
            }

            override fun onTaskDeleteSuccess(appTask: AppTask) {
                if (appTaskInner.id == appTask.id && finishType == STaskFinishType.SUCCESS && taskState.getTaskCode() == ATaskState.STATE_DELETE_CREATE) {
                    unregisterTaskListener(this)
                    Log.d(TAG, "onTaskDeleteSuccess: ")
                    listener.invoke(appTask)
                }
            }

            override fun onTaskDeleteFail(appTask: AppTask, exception: TaskException) {
                if (appTaskInner.id == appTask.id && finishType is STaskFinishType.FAIL && taskState.getTaskCode() == ATaskState.STATE_DELETE_CREATE) {
                    unregisterTaskListener(this)
                    listener.invoke(appTask)
                }
            }
        })

        taskStart(appTaskInner, taskNodeQueueName)
    }

    override fun taskStart(appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String) {
        if (!canTaskStart(appTask, taskNodeQueueName))
            return
        UtilKLogWrapper.d(TAG, "taskStart: appTask $appTask")
        val currTaskNode = getCurrTaskNode_ofTaskNodeQueue(appTask, taskNodeQueueName, getFirstTaskNode_ofTaskNodeQueue(appTask.fileExt, taskNodeQueueName) ?: return) ?: return
        var nextTaskNode = getNextTaskNode_ofTaskNodeQueue(appTask.fileExt, taskNodeQueueName, currTaskNode)
        UtilKLogWrapper.d(TAG, "taskStart: currTaskName $currTaskNode nextTaskName $nextTaskNode")
        if (appTask.isAnyTaskSuccess() || appTask.isAnyTasking() || appTask.isAnyTaskPause()) {
            UtilKLogWrapper.d(TAG, "taskStart: getNextTaskSet")
            if (nextTaskNode != null && (nextTaskNode != STaskNode.TaskNodeRestart)) {//继续下一个任务
//                getNextTaskSet(appTask.fileExt, taskNodeQueueName, currTaskName)?.taskStart(appTask, taskNodeQueueName)
                getTaskSet(nextTaskNode.taskName)?.taskStart(appTask, taskNodeQueueName)
            } else if (!hasTaskNode_ofTaskNodeQueue(appTask.fileExt, taskNodeQueueName, currTaskNode)) {//另外一个队列的任务
                nextTaskNode = getFirstTaskNode_ofTaskNodeQueue(appTask.fileExt, taskNodeQueueName)
                nextTaskNode?.let {
                    getTaskSet(nextTaskNode.taskName)?.taskStart(appTask, taskNodeQueueName)
                }
            } else if (appTask.isAnyTaskPause()) {//是暂停的就继续
                getTaskSet(currTaskNode.taskName)?.taskStart(appTask, taskNodeQueueName)
            } else {
                UtilKLogWrapper.d(TAG, "taskStart: getNextTaskSet is null")
            }
        } else {
            UtilKLogWrapper.d(TAG, "taskStart: getTaskSet")
            getTaskSet(currTaskNode.taskName)?.taskStart(appTask, taskNodeQueueName)
        }
    }

    override fun taskResume(appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String) {
        if (!canTaskResume(appTask, taskNodeQueueName))
            return
        UtilKLogWrapper.d(TAG, "taskResume: appTask $appTask")
        getTaskSet(getCurrTaskNode_ofTaskNodeQueue(appTask, taskNodeQueueName)?.taskName ?: return)?.taskResume(appTask, taskNodeQueueName)
    }

    override fun taskPause(appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String) {
        if (!canTaskPause(appTask, taskNodeQueueName))
            return
        UtilKLogWrapper.d(TAG, "taskPause: appTask $appTask")
        getTaskSet(getCurrTaskNode_ofTaskNodeQueue(appTask, taskNodeQueueName)?.taskName ?: return)?.taskPause(appTask, taskNodeQueueName)
    }

    override fun taskCancel(appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String) {
        if (!canTaskCancel(appTask, taskNodeQueueName))
            return
        UtilKLogWrapper.d(TAG, "taskCancel: appTask $appTask")
        getTaskSet(getCurrTaskNode_ofTaskNodeQueue(appTask, taskNodeQueueName)?.taskName ?: return)?.taskCancel(appTask, taskNodeQueueName)
    }

    /////////////////////////////////////////////////////////////////

    override fun canTaskStart(appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String): Boolean {
        if (appTask.isTaskProcess(this, taskNodeQueueName) && !appTask.isAnyTaskPause() && !appTask.isAnyTaskSuccess() && !appTask.isAnyTasking()) {
            UtilKLogWrapper.d(TAG, "canTaskStart: task is process")
            return false
        }
        if (appTask.isTaskSuccess(this, taskNodeQueueName)) {
            UtilKLogWrapper.d(TAG, "canTaskStart: task is success")
            return false
        }
        return (getTaskSet(getFirstTaskNode_ofTaskNodeQueue(appTask.fileExt, taskNodeQueueName)?.taskName ?: return false)?.canTaskStart(appTask, taskNodeQueueName) ?: false).also {
            Log.d(
                TAG,
                "canTaskStart: $it"
            )
        }
    }

    override fun canTaskResume(appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String): Boolean {
        if (!appTask.isTaskProcess(this, taskNodeQueueName)) {
            UtilKLogWrapper.d(TAG, "taskResume: task is not process")
            return false
        }
        if (!appTask.isAnyTaskPause()) {
            UtilKLogWrapper.d(TAG, "taskResume: task is not pause")
            return false
        }
        return getTaskSet(getCurrTaskNode_ofTaskNodeQueue(appTask, taskNodeQueueName)?.taskName ?: return false)?.canTaskResume(appTask, taskNodeQueueName) ?: false
    }

    override fun canTaskPause(appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String): Boolean {
        if (!appTask.isTaskProcess(this, taskNodeQueueName)) {
            UtilKLogWrapper.d(TAG, "taskPause: task is not process")
            return false
        }
        if (appTask.isAnyTaskPause()) {
            UtilKLogWrapper.d(TAG, "taskPause: already pause")
            return false
        }
        return getTaskSet(getCurrTaskNode_ofTaskNodeQueue(appTask, taskNodeQueueName)?.taskName ?: return false)?.canTaskPause(appTask, taskNodeQueueName) ?: false
    }

    override fun canTaskCancel(appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String): Boolean {
        if (appTask.isTaskSuccess(this, taskNodeQueueName)) {
            UtilKLogWrapper.d(TAG, "canTaskStart: task is success")
            return false
        }
        if (!appTask.isTaskProcess(this, taskNodeQueueName)) {
            UtilKLogWrapper.d(TAG, "canTaskCancel: task is not process")
            return false
        }
        return getTaskSet(getCurrTaskNode_ofTaskNodeQueue(appTask, taskNodeQueueName)?.taskName ?: return false)?.canTaskCancel(appTask, taskNodeQueueName) ?: false
    }

    /////////////////////////////////////////////////////////////////

    override fun onTaskCreate(appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String, isUpdate: Boolean) {
        appTask.toTaskStateNew(if (isUpdate) AState.STATE_TASK_UPDATE else AState.STATE_TASK_CREATE)
        _iTaskLifecycle.onTaskStarted(appTask.taskState, appTask, taskNodeQueueName)
    }

    override fun onTaskUnavailable(appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String) {
        appTask.toTaskStateNew(AState.STATE_TASK_UNAVAILABLE)
        _iTaskLifecycle.onTaskStarted(appTask.taskState, appTask, taskNodeQueueName)
    }

//    fun onTaskSuccess(appTask: AppTask) {
//        onTaskFinish(appTask, STaskFinishType.SUCCESS)
//    }
//
//    fun onTaskCancel(appTask: AppTask) {
//        onTaskFinish(appTask, STaskFinishType.CANCEL)
//    }
//
//    fun onTaskFail(appTask: AppTask, exception: TaskException) {
//        onTaskFinish(appTask, STaskFinishType.FAIL(exception))
//    }

    override fun onTaskFinish(appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String, finishType: STaskFinishType) {
        appTask.toTaskStateNew(
            when (finishType) {
                STaskFinishType.SUCCESS -> getLastTaskNode_ofTaskNodeQueue(appTask.fileExt, taskNodeQueueName)?.taskName?.taskName2taskState()?.plus(AState.STATE_TASK_SUCCESS) ?: AState.STATE_TASK_SUCCESS
                STaskFinishType.CANCEL -> AState.STATE_TASK_CANCEL
                is STaskFinishType.FAIL -> AState.STATE_TASK_FAIL
            }
        )
        _iTaskLifecycle.onTaskFinished(appTask.taskState, appTask, taskNodeQueueName, finishType)
    }

    /////////////////////////////////////////////////////////////////

    fun onTask_ofFail(appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String, exception: TaskException) {
        UtilKLogWrapper.d(TAG, "onTask: id ${appTask.id} state ${appTask.getTaskStateStr()} exception ${exception.msg} appTask $appTask")
        for (listener in _taskListeners) {
            UtilKLogWrapper.d(TAG, "onTask_ofFail: listener ${listener}")
            when (appTask.taskState) {
                AState.STATE_TASK_FAIL -> listener.onTaskFinish(appTask, taskNodeQueueName, STaskFinishType.FAIL(exception))
                ATaskState.STATE_DOWNLOAD_FAIL -> listener.onTaskDownloadFail(appTask, exception)
                ATaskState.STATE_VERIFY_FAIL -> listener.onTaskVerifyFail(appTask, exception)
                ATaskState.STATE_UNZIP_FAIL -> listener.onTaskUnzipFail(appTask, exception)
                ATaskState.STATE_INSTALL_FAIL -> listener.onTaskInstallFail(appTask, exception)
                ATaskState.STATE_OPEN_FAIL -> listener.onTaskOpenFail(appTask, exception)
                ATaskState.STATE_UNINSTALL_FAIL -> listener.onTaskUninstallFail(appTask, exception)
                ATaskState.STATE_DELETE_FAIL -> listener.onTaskDeleteFail(appTask, exception)
            }
        }
        for (listener in _task2Listeners) {
            when (appTask.taskState) {
                AState.STATE_TASK_FAIL -> listener.onTaskFinish(appTask, taskNodeQueueName, STaskFinishType.FAIL(exception))
                else -> listener.onTaskFail(appTask, taskNodeQueueName, exception)
            }
        }
        //
        if (appTask.isTaskFail())
            onTaskCreate(appTask, taskNodeQueueName, appTask.taskStateInit == AState.STATE_TASK_UPDATE)
        else if (appTask.isAnyTaskFail())
            onTaskFinish(appTask, taskNodeQueueName, STaskFinishType.FAIL(exception))
    }

    fun onTask_ofIng(appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String, progress: Int, currentIndex: Long, totalIndex: Long, offsetIndexPerSeconds: Long) {
        UtilKLogWrapper.d(
            TAG,
            "onTask: id ${appTask.id} state ${appTask.getTaskStateStr()} progress $progress currentIndex $currentIndex totalIndex $totalIndex offsetIndexPerSeconds $offsetIndexPerSeconds appTask $appTask"
        )
        for (listener in _taskListeners) {
            UtilKLogWrapper.d(TAG, "onTask_ofIng: listener ${listener}")
            when (appTask.taskState) {
                ATaskState.STATE_DOWNLOADING -> listener.onTaskDownloading(appTask, progress, currentIndex, totalIndex, offsetIndexPerSeconds)
                ATaskState.STATE_VERIFYING -> listener.onTaskVerifying(appTask, progress, currentIndex, totalIndex, offsetIndexPerSeconds)
                ATaskState.STATE_UNZIPING -> listener.onTaskUnziping(appTask, progress, currentIndex, totalIndex, offsetIndexPerSeconds)
                ATaskState.STATE_INSTALLING -> listener.onTaskInstalling(appTask, progress, currentIndex, totalIndex, offsetIndexPerSeconds)
                ATaskState.STATE_OPENING -> listener.onTaskOpening(appTask, progress, currentIndex, totalIndex, offsetIndexPerSeconds)
                ATaskState.STATE_UNINSTALLING -> listener.onTaskUninstalling(appTask, progress, currentIndex, totalIndex, offsetIndexPerSeconds)
                ATaskState.STATE_DELETING -> listener.onTaskDeleting(appTask, progress, currentIndex, totalIndex, offsetIndexPerSeconds)
            }
        }
        for (listener in _task2Listeners) {
            listener.onTasking(appTask, taskNodeQueueName, progress, currentIndex, totalIndex, offsetIndexPerSeconds)
        }
    }

    fun onTask_ofOther(appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String) {
        UtilKLogWrapper.d(TAG, "onTask: id ${appTask.id} state ${appTask.getTaskStateStr()} appTask $appTask")
        for (listener in _taskListeners) {
            UtilKLogWrapper.d(TAG, "onTask_ofOther: listener ${listener}")
            when (appTask.taskState) {
                AState.STATE_TASK_CREATE -> listener.onTaskCreate(appTask, taskNodeQueueName, false)
                AState.STATE_TASK_UPDATE -> listener.onTaskCreate(appTask, taskNodeQueueName, true)
                AState.STATE_TASK_UNAVAILABLE -> listener.onTaskUnavailable(appTask, taskNodeQueueName)
                AState.STATE_TASK_SUCCESS -> listener.onTaskFinish(appTask, taskNodeQueueName, STaskFinishType.SUCCESS)
                AState.STATE_TASK_CANCEL -> listener.onTaskFinish(appTask, taskNodeQueueName, STaskFinishType.CANCEL)
                ///////////////////////////////////////////////////////////////////////////////
                ATaskState.STATE_DOWNLOAD_PAUSE -> listener.onTaskDownloadPause(appTask)
                ATaskState.STATE_DOWNLOAD_SUCCESS -> listener.onTaskDownloadSuccess(appTask)
                ATaskState.STATE_DOWNLOAD_CANCEL -> listener.onTaskDownloadCancel(appTask)
                ///////////////////////////////////////////////////////////////////////////////
                ATaskState.STATE_VERIFY_PAUSE -> listener.onTaskVerifyPause(appTask)
                ATaskState.STATE_VERIFY_SUCCESS -> listener.onTaskVerifySuccess(appTask)
                ATaskState.STATE_VERIFY_CANCEL -> listener.onTaskVerifyCancel(appTask)
                ///////////////////////////////////////////////////////////////////////////////
                ATaskState.STATE_UNZIP_PAUSE -> listener.onTaskUnzipPause(appTask)
                ATaskState.STATE_UNZIP_SUCCESS -> listener.onTaskUnzipSuccess(appTask)
                ATaskState.STATE_UNZIP_CANCEL -> listener.onTaskUnzipCancel(appTask)
                ///////////////////////////////////////////////////////////////////////////////
                ATaskState.STATE_INSTALL_PAUSE -> listener.onTaskInstallPause(appTask)
                ATaskState.STATE_INSTALL_SUCCESS -> listener.onTaskInstallSuccess(appTask)
                ATaskState.STATE_INSTALL_CANCEL -> listener.onTaskInstallCancel(appTask)
                ///////////////////////////////////////////////////////////////////////////////
                ATaskState.STATE_OPEN_PAUSE -> listener.onTaskOpenPause(appTask)
                ATaskState.STATE_OPEN_SUCCESS -> listener.onTaskOpenSuccess(appTask)
                ATaskState.STATE_OPEN_CANCEL -> listener.onTaskOpenCancel(appTask)
                ///////////////////////////////////////////////////////////////////////////////
                ATaskState.STATE_UNINSTALL_PAUSE -> listener.onTaskUninstallPause(appTask)
                ATaskState.STATE_UNINSTALL_SUCCESS -> listener.onTaskUninstallSuccess(appTask)
                ATaskState.STATE_UNINSTALL_CANCEL -> listener.onTaskUninstallCancel(appTask)
                ///////////////////////////////////////////////////////////////////////////////
                ATaskState.STATE_DELETE_PAUSE -> listener.onTaskDeletePause(appTask)
                ATaskState.STATE_DELETE_SUCCESS -> listener.onTaskDeleteSuccess(appTask)
                ATaskState.STATE_DELETE_CANCEL -> listener.onTaskDeleteCancel(appTask)
            }
        }
        for (listener in _task2Listeners) {
            when {
                appTask.taskState == AState.STATE_TASK_CREATE -> listener.onTaskCreate(appTask, taskNodeQueueName, false)
                appTask.taskState == AState.STATE_TASK_UPDATE -> listener.onTaskCreate(appTask, taskNodeQueueName, true)
                appTask.taskState == AState.STATE_TASK_UNAVAILABLE -> listener.onTaskUnavailable(appTask, taskNodeQueueName)
                appTask.taskState == AState.STATE_TASK_SUCCESS -> listener.onTaskFinish(appTask, taskNodeQueueName, STaskFinishType.SUCCESS)
                appTask.taskState == AState.STATE_TASK_CANCEL -> listener.onTaskFinish(appTask, taskNodeQueueName, STaskFinishType.CANCEL)
                appTask.isAnyTaskPause() -> listener.onTaskPause(appTask, taskNodeQueueName)
                appTask.isAnyTaskSuccess() -> listener.onTaskSuccess(appTask, taskNodeQueueName)
                appTask.isAnyTaskCancel() -> listener.onTaskCancel(appTask, taskNodeQueueName)
            }
        }

        //next
        if (appTask.isAnyTaskSuccess()) {
            val currTaskNode = getCurrTaskNode_ofTaskNodeQueue(appTask, taskNodeQueueName) ?: return
            val nextTaskNode = getNextTaskNode_ofTaskNodeQueue(appTask.fileExt, taskNodeQueueName, currTaskNode)
            if (nextTaskNode != null && nextTaskNode != STaskNode.TaskNodeRestart) {
                getTaskSet(nextTaskNode.taskName)?.taskStart(appTask, taskNodeQueueName)
            } else if (nextTaskNode == STaskNode.TaskNodeRestart) {
                onTaskCreate(appTask, taskNodeQueueName, appTask.taskStateInit == AState.STATE_TASK_UPDATE)
            }
        }
        if (appTask.isAnyTaskCancel() && !appTask.isTaskCancel()) {
            onTaskFinish(appTask, taskNodeQueueName, STaskFinishType.CANCEL)
        } else if (appTask.isTaskCancel()) {
            onTaskCreate(appTask, taskNodeQueueName, appTask.taskStateInit == AState.STATE_TASK_UPDATE)
        }
    }
}