package com.mozhimen.taskk.provider.basic.db

import com.mozhimen.kotlin.utilk.android.util.UtilKLogWrapper
import androidx.annotation.WorkerThread
import com.mozhimen.kotlin.utilk.commons.IUtilK
import com.mozhimen.taskk.executor.TaskKExecutor
import java.util.concurrent.ConcurrentHashMap
import kotlin.Exception

/**
 * @ClassName AppTaskDaoManager
 * @Description TODO
 * @Author Mozhimen
 * @Date 2023/11/7 16:08
 * @Version 1.0
 */
object AppTaskDaoManager : IUtilK {
    private val _appTasks: ConcurrentHashMap<String, AppTask> = ConcurrentHashMap()

    //////////////////////////////////////////////////////////

    fun init() {
        TaskKExecutor.execute(TAG + "init") {
            _appTasks.putAll(AppTaskDb.getAppTaskDao().get_ofAll().associateBy { it.taskId })
            UtilKLogWrapper.d(TAG, "init: _appTasks $_appTasks")
        }
    }

    //////////////////////////////////////////////////////////

    @JvmStatic
    fun get_ofTaskId(taskId: String): AppTask? {
        return _appTasks[taskId]
    }

    @JvmStatic
    fun get_ofTaskId_ApkPackageName_ApkVersionCode(taskId: String, apkPackageName: String, apkVersionCode: Int): AppTask? {
        val appTask = get_ofTaskId(taskId) ?: return null
        return if (appTask.apkPackageName == apkPackageName && appTask.apkVersionCode == apkVersionCode) appTask else null
    }

    @JvmStatic
    fun get_ofApkPackageName_ApkVersionCode(apkPackageName: String, apkVersionCode: Int): AppTask? {
        return _appTasks.filter { it.value.apkPackageName == apkPackageName && it.value.apkVersionCode == apkVersionCode }.values.firstOrNull()
    }

    @JvmStatic
    fun get_ofTaskDownloadId(taskDownloadId: Int): AppTask? {
        return _appTasks.filter { it.value.taskDownloadId == taskDownloadId }.values.firstOrNull()
    }

    @JvmStatic
    fun get_ofTaskDownloadUrlCurrent(taskDownloadUrlCurrent: String): AppTask? {
        return _appTasks.filter { it.value.taskDownloadUrlCurrent == taskDownloadUrlCurrent }.values.firstOrNull()
    }

    //////////////////////////////////////////////////////////

    fun get_ofTaskName(taskName: String): AppTask? {
        return _appTasks.filter { it.value.taskName == taskName }.values.firstOrNull()
    }

    fun get_ofFileName(fileName: String): AppTask? {
        return _appTasks.filter { it.value.fileName == fileName }.values.firstOrNull()
    }

    fun get_ofFilePathNameExt(filePathNameExt: String): AppTask? {
        return _appTasks.filter { it.value.filePathNameExt == filePathNameExt }.values.firstOrNull()
    }

    //////////////////////////////////////////////////////////

    @JvmStatic
    fun gets_ofApkPackageName(packageName: String): List<AppTask> {
        return _appTasks.filter { it.value.apkPackageName == packageName }.values.toList()
    }

    @JvmStatic
    fun gets_ofApkPackageName_satisfyApkVersionCode(packageName: String, apkVersionCode: Int): List<AppTask> {
        return _appTasks.filter { it.value.apkPackageName == packageName && it.value.apkVersionCode >= apkVersionCode }.values.toList()
            .also { UtilKLogWrapper.d(TAG, "gets_ofApkPackageName_satisfyApkVersionCode: $it") }
    }

    //////////////////////////////////////////////////////////

    @JvmStatic
    fun gets_ofIsTaskProcess(): List<AppTask> {
        return _appTasks.filter { it.value.isTaskProcess() }.values.toList()
    }

    @JvmStatic
    fun gets_ofIsTaskCreate(): List<AppTask> {
        return _appTasks.filter { it.value.isTaskCreate() }.values.toList()
    }

    @JvmStatic
    fun gets_ofIsTaskUpdate(): List<AppTask> {
        return _appTasks.filter { it.value.isTaskUpdate() }.values.toList()
    }

    @JvmStatic
    fun gets_ofIsTaskCreateOrUpdate(): List<AppTask> {
        return _appTasks.filter { it.value.isTaskCreateOrUpdate() }.values.toList()
    }

    @JvmStatic
    fun gets_ofIsTaskUnAvailable(): List<AppTask> {
        return _appTasks.filter { it.value.isTaskUnAvailable() }.values.toList()
    }

    @JvmStatic
    fun gets_ofIsTaskCancel(): List<AppTask> {
        return _appTasks.filter { it.value.isTaskCancel() }.values.toList()
    }

    @JvmStatic
    fun gets_ofIsTaskSuccess(): List<AppTask> {
        return _appTasks.filter { it.value.isTaskSuccess() }.values.toList()
    }

    @JvmStatic
    fun gets_ofIsTaskFail(): List<AppTask> {
        return _appTasks.filter { it.value.isTaskFail() }.values.toList()
    }

    //////////////////////////////////////////////////////////

    @JvmStatic
    fun gets_ofIsAnyTasking(): List<AppTask> {
        return _appTasks.filter { it.value.isAnyTasking() }.values.toList()
    }

    @JvmStatic
    fun gets_ofIsAnyTaskPause(): List<AppTask> {
        return _appTasks.filter { it.value.isAnyTaskPause() }.values.toList()
    }

    @JvmStatic
    fun gets_ofIsAnyTaskSuccess(): List<AppTask> {
        return _appTasks.filter { it.value.isAnyTaskSuccess() }.values.toList()
    }

    @JvmStatic
    fun gets_ofIsAnyTaskCancel(): List<AppTask> {
        return _appTasks.filter { it.value.isAnyTaskCancel() }.values.toList()
    }

    @JvmStatic
    fun gets_ofIsAnyTaskFail(): List<AppTask> {
        return _appTasks.filter { it.value.isAnyTaskFail() }.values.toList()
    }

    @JvmStatic
    fun gets_ofIsAnyTaskResult(): List<AppTask> {
        return _appTasks.filter { it.value.isAnyTaskResult() }.values.toList()
    }

    ////////////////////////////////////////////////////////////

    @JvmStatic
    fun gets_ofAtTaskDownload(): List<AppTask> {
        return _appTasks.filter { it.value.atTaskDownload() }.values.toList()
    }

    @JvmStatic
    fun gets_ofAtTaskVerify(): List<AppTask> {
        return _appTasks.filter { it.value.atTaskVerify() }.values.toList()
    }

    @JvmStatic
    fun gets_ofAtTaskUnzip(): List<AppTask> {
        return _appTasks.filter { it.value.atTaskUnzip() }.values.toList()
    }

    @JvmStatic
    fun gets_ofAtTaskInstall(): List<AppTask> {
        return _appTasks.filter { it.value.atTaskInstall() }.values.toList()
    }

    @JvmStatic
    fun gets_ofAtTaskOpen(): List<AppTask> {
        return _appTasks.filter { it.value.atTaskOpen() }.values.toList()
    }

    @JvmStatic
    fun gets_ofAtTaskUninstall(): List<AppTask> {
        return _appTasks.filter { it.value.atTaskUninstall() }.values.toList()
    }

    ////////////////////////////////////////////////////////////

    @JvmStatic
    fun gets_ofIsTaskDownloading(): List<AppTask> {
        return _appTasks.filter { it.value.isTaskDownloading() }.values.toList()
    }

    @JvmStatic
    fun gets_ofIsTaskVerifying(): List<AppTask> {
        return _appTasks.filter { it.value.isTaskVerifying() }.values.toList()
    }

    @JvmStatic
    fun gets_ofIsTaskUnziping(): List<AppTask> {
        return _appTasks.filter { it.value.isTaskUnziping() }.values.toList()
    }

    @JvmStatic
    fun gets_ofIsTaskInstalling(): List<AppTask> {
        return _appTasks.filter { it.value.isTaskInstalling() }.values.toList()
    }

    @JvmStatic
    fun gets_ofIsTaskOpening(): List<AppTask> {
        return _appTasks.filter { it.value.isTaskOpening() }.values.toList()
    }

    @JvmStatic
    fun gets_ofIsTaskUninstalling(): List<AppTask> {
        return _appTasks.filter { it.value.isTaskUninstalling() }.values.toList()
    }

    ////////////////////////////////////////////////////////////

    @JvmStatic
    fun gets_ofIsTaskDownloadSuccess(): List<AppTask> {
        return _appTasks.filter { it.value.isTaskDownloadSuccess() }.values.toList()
    }

    @JvmStatic
    fun gets_ofIsTaskVerifySuccess(): List<AppTask> {
        return _appTasks.filter { it.value.isTaskVerifySuccess() }.values.toList()
    }

    @JvmStatic
    fun gets_ofIsTaskUnzipSuccess(): List<AppTask> {
        return _appTasks.filter { it.value.isTaskUnzipSuccess() }.values.toList()
    }

    @JvmStatic
    fun gets_ofIsTaskInstallSuccess(): List<AppTask> {
        return _appTasks.filter { it.value.isTaskInstallSuccess() }.values.toList()
    }

    @JvmStatic
    fun gets_ofIsTaskOpenSuccess(): List<AppTask> {
        return _appTasks.filter { it.value.isTaskOpenSuccess() }.values.toList()
    }

    @JvmStatic
    fun gets_ofIsTaskUninstallSuccess(): List<AppTask> {
        return _appTasks.filter { it.value.isTaskUninstallSuccess() }.values.toList()
    }

    ////////////////////////////////////////////////////////////

    fun has_ofAtTaskDownload(): Boolean {
        return gets_ofAtTaskDownload().isNotEmpty()
    }

    fun has_ofAtTaskVerify(): Boolean {
        return gets_ofAtTaskVerify().isNotEmpty()
    }

    fun has_ofAtTaskUnzip(): Boolean {
        return gets_ofAtTaskUnzip().isNotEmpty()
    }

    fun has_ofAtTaskInstall(): Boolean {
        return gets_ofAtTaskInstall().isNotEmpty()
    }

    fun has_ofAtTaskOpen(): Boolean {
        return gets_ofAtTaskOpen().isNotEmpty()
    }

    fun has_ofAtTaskUninstall(): Boolean {
        return gets_ofAtTaskUninstall().isNotEmpty()
    }

    //////////////////////////////////////////////////////////

    fun has_ofTaskId(taskId: String): Boolean {
        return get_ofTaskId(taskId) != null
    }

    //////////////////////////////////////////////////////////

    fun addOrUpdate(vararg appTasks: AppTask) {
        TaskKExecutor.execute(TAG + "addOrUpdate") {
            addOrUpdateOnBack(*appTasks)
        }
    }

    fun delete(appTask: AppTask) {
        TaskKExecutor.execute(TAG + "delete") {
            deleteOnBack(appTask)
        }
    }

    /////////////////////////////////////////////////////////////////////////////

    /**
     * 删除任务
     * @param appTask 需要删除的任务
     */
    @Synchronized
    @WorkerThread
    private fun deleteOnBack(appTask: AppTask) {
        try {
            if (has_ofTaskId(appTask.taskId)) {
                _appTasks.remove(appTask.taskId)
            } else return
            AppTaskDb.getAppTaskDao().delete(appTask)
        } catch (e: Exception) {
            e.printStackTrace()
            UtilKLogWrapper.e(TAG, "deleteOnBack: ", e)
        }
    }

    /**
     * 同步更新，防止多个线程同时更新，出现问题
     */
    @Synchronized
    @WorkerThread
    private fun addOrUpdateOnBack(vararg appTasks: AppTask) {
        try {
            appTasks.forEach { appTask ->
                if (has_ofTaskId(appTask.taskId)) {
                    _appTasks[appTask.taskId] = appTask
                    UtilKLogWrapper.d(TAG, "addOrUpdateOnBack: update")
                    AppTaskDb.getAppTaskDao().update(appTask)//将本条数据插入到数据库
                } else {
                    _appTasks[appTask.taskId] = appTask
                    UtilKLogWrapper.d(TAG, "addOrUpdateOnBack: addAll")
                    AppTaskDb.getAppTaskDao().addAll(appTask)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            UtilKLogWrapper.e(TAG, "addOrUpdateOnBack: ", e)
        }
    }

//    /*    fun queryAppTask(appBaseInfo: AppBaseInfo): AppTask? {
//    val iterator = _appTasks.iterator()
//    while (iterator.hasNext()) {
//        val next = iterator.next()
//        if (next.packName == appBaseInfo.packageName) {
//            //如果Id相同，说明是我们自己安装的,还需要判断下载地址是否相同，如果相同，则返回对象，否则需要重新创建一个
//            if (next.appId == appBaseInfo.id) {
//                if (next.currentDownloadUrl == AppDownloadManager.getDownloadUrl(appBaseInfo)) {
//                    return next
//                } else {
//                    //从数据库中删除
//                    iterator.remove()
//                    AppDownloadManager.executorService.execute {
//                        DatabaseManager.appDownloadParamDao.delete(next)
//                    }
//                    break
//                }
//            }
//        }
//    }
//    return null
//}*/

//废弃
//    fun removeAppTaskForDatabase(appTask: AppTask) {
//        if (appTask.apkPackageName.isEmpty()) return
//        val appTask1 = getByApkPackageName(appTask.apkPackageName) ?: return//从本地数据库中查询出下载信息//如果查询不到，就不处理
//        //if (appTask1.apkIsInstalled)//删除数据库中的其他已安装的数据，相同包名的只保留一条已安装的数据
//        delete(appTask1)
//    }

//    fun getAllDownloading(countDownLatch: CountDownLatch, list: MutableList<AppTask>) {
//        TaskKExecutor.execute(TAG + "getAllDownloading") {
//            val queryAllDownload = DatabaseManager.appDownloadParamDao.getAllDownloading()
//            list.addAll(queryAllDownload)
//            countDownLatch.countDown()
//        }
//    }

//    @JvmStatic
//    fun getByTaskId_PackageName(taskId: String, packageName: String): AppTask? {
//        val appTask: AppTask = _tasks[taskId] ?: kotlin.run {
//            return null
//        }
//        if (appTask.apkPackageName == packageName)
//            return appTask
//        return null
//    }

//    @JvmStatic
//    fun getAllAtTaskDownloadOrWaitOrPause(): List<AppTask> {
//        return _tasks.filter { it.value.isTaskDownload() /*|| it.value.taskState == CNetKAppTaskState.STATE_TASK_WAIT*/ || it.value.taskState == CNetKAppTaskState.STATE_TASK_PAUSE }
//            .map { it.value }
//    }
}