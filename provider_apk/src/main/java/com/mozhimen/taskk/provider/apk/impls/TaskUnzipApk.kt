package com.mozhimen.taskk.provider.apk.impls

import android.annotation.SuppressLint
import android.os.Environment
import android.util.Log
import androidx.annotation.WorkerThread
import com.mozhimen.kotlin.utilk.android.app.UtilKApplicationWrapper
import com.mozhimen.kotlin.utilk.android.content.UtilKContextDir
import com.mozhimen.kotlin.utilk.android.os.UtilKHandlerWrapper
import com.mozhimen.kotlin.utilk.android.util.UtilKLogWrapper
import com.mozhimen.kotlin.utilk.java.io.UtilKFileDir
import com.mozhimen.kotlin.utilk.java.io.createFolder
import com.mozhimen.kotlin.utilk.java.io.deleteFile
import com.mozhimen.kotlin.utilk.java.io.inputStream2file_use_ofBufferedOutStream
import com.mozhimen.kotlin.utilk.kotlin.containsAny
import com.mozhimen.kotlin.utilk.kotlin.createFolder
import com.mozhimen.kotlin.utilk.kotlin.deleteFolder
import com.mozhimen.kotlin.utilk.kotlin.endsWithWithAny
import com.mozhimen.kotlin.utilk.kotlin.getSplitLastIndexToStart
import com.mozhimen.kotlin.utilk.kotlin.getStrFolderPath
import com.mozhimen.kotlin.utilk.kotlin.ranges.constraint
import com.mozhimen.kotlin.utilk.kotlin.strFilePath2file
import com.mozhimen.taskk.executor.TaskKExecutor
import com.mozhimen.taskk.provider.apk.cons.CExt
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskUnzip
import com.mozhimen.taskk.provider.basic.cons.CErrorCode
import com.mozhimen.taskk.provider.basic.cons.CTaskState
import com.mozhimen.taskk.provider.basic.cons.STaskFinishType
import com.mozhimen.taskk.provider.basic.db.AppTask
import com.mozhimen.taskk.provider.basic.impls.TaskException
import com.mozhimen.taskk.provider.basic.impls.intErrorCode2taskException
import com.mozhimen.taskk.provider.basic.interfaces.ITaskInterceptor
import com.mozhimen.taskk.provider.basic.interfaces.ITaskLifecycle
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

/**
 * @ClassName TaskProviderUnzipApk
 * @Description TODO
 * @Author Mozhimen / Kolin Zhao
 * @Date 2024/8/21 21:41
 * @Version 1.0
 */
open class TaskUnzipApk(iTaskLifecycle: ITaskLifecycle?) : ATaskUnzip(iTaskLifecycle) {
    //    override val unzipTasks: CopyOnWriteArrayList<AppTask> = CopyOnWriteArrayList()
    override var _unzipDir: File? = UtilKFileDir.External.getFilesDownloads()

    protected val _context = UtilKApplicationWrapper.instance.applicationContext

    protected var _iTaskInterceptor: ITaskInterceptor? = null

    fun setTaskInterceptor(iTaskInterceptor: ITaskInterceptor): TaskUnzipApk {
        _iTaskInterceptor = iTaskInterceptor
        return this
    }

    override fun getIgnorePaths(): List<String> {
        return listOf("__MACOSX/")
    }

    override fun getSupportFileExts(): List<String> {
        return listOf(CExt.EXT_APK)
    }

    //////////////////////////////////////////////////////////////////

    override fun taskStart(appTask: AppTask) {
        if (!appTask.canTaskUnzip()) {
            UtilKLogWrapper.e(TAG, "install: the task hasn't verify success")
            return
        }
        if (appTask.isTaskUnziping()) {
            UtilKLogWrapper.e(TAG, "install: the task is already unziping")
            return
        }
        super.taskStart(appTask)
        if (appTask.taskUnzipEnable) {
            startUnzip(appTask)
        } else {
            onTaskFinished(CTaskState.STATE_UNZIP_SUCCESS, STaskFinishType.SUCCESS, appTask)
        }
    }

    override fun taskCancel(appTask: AppTask) {
        if (appTask.isTaskUnzipSuccess()) {
            _iTaskInterceptor?.deleteOrgFiles(appTask)
            super.taskCancel(appTask)
        }
    }

    //////////////////////////////////////////////////////////////////

    protected fun startUnzip(appTask: AppTask) {
        TaskKExecutor.execute(TAG + getTaskName()) {
            try {
                val strApkFilePathNameUnzip = startUnzipOnBack(appTask)
                UtilKLogWrapper.d(TAG, "unzip: strFilePathUnzip $strApkFilePathNameUnzip")

                if (strApkFilePathNameUnzip.isEmpty())
                    throw CErrorCode.CODE_TASK_UNZIP_FAIL.intErrorCode2taskException()
//                else if (!strFilePathUnzip.endsWith("apk"))
//                    throw CErrorCode.CODE_TASK_UNZIP_FAIL.intErrorCode2taskException()

                /////////////////////////////////////////////////////////

                UtilKHandlerWrapper.post {
                    onTaskFinished(CTaskState.STATE_UNZIP_SUCCESS, STaskFinishType.SUCCESS, appTask.apply {
                        taskUnzipFilePath = strApkFilePathNameUnzip
                    })
                }
            } catch (e: TaskException) {
                UtilKHandlerWrapper.post {
                    onTaskFinished(CTaskState.STATE_UNZIP_FAIL, STaskFinishType.FAIL(e), appTask)
                }
            }
        }
    }

    @WorkerThread
    protected fun startUnzipOnBack(appTask: AppTask): String {
        val dir = _unzipDir ?: throw CErrorCode.CODE_TASK_UNZIP_DIR_NULL.intErrorCode2taskException()
        if (appTask.filePathNameExt.isEmpty())
            throw CErrorCode.CODE_TASK_UNZIP_DIR_NULL.intErrorCode2taskException()
        val fileSource = appTask.filePathNameExt.strFilePath2file()
        val strFilePathNameApk = startUnzipOnBack(fileSource, dir.absolutePath, appTask)
        UtilKLogWrapper.d(TAG, "unzipOnBack: fileSource ${fileSource.absolutePath} strFilePathNameApk $strFilePathNameApk")
        return strFilePathNameApk
    }

    /**
     *
     * @param fileSource File ///storage/emulated/0/Android/data/com.mozhimen.basicktest/files/Download/gameName.apk
     * @param strFilePathDest String ///storage/emulated/0/Android/data/com.mozhimen.basicktest/files/Download/
     */
    @WorkerThread
    protected open fun startUnzipOnBack(fileSource: File, strFilePathDest: String, appTask: AppTask): String {
        // gameName
        val strFileNameReal = fileSource.name.getSplitLastIndexToStart(".", false)//name.subSequence(0, name.lastIndexOf("."))
        // /storage/emulated/0/Android/data/com.mozhimen.basicktest/files/Download/gameName
        val strFilePathDestReal = ("$strFilePathDest${File.separator}$strFileNameReal").also { UtilKLogWrapper.d(TAG, "unzipOnBack: strFilePathDestReal $it") }

        try {
            //创建拷贝目录
            strFilePathDestReal.createFolder()
            // /storage/emulated/0/Android/data/com.mozhimen.basicktest/files/Download/gameName

            //用来记录apk的文件名
            val strFilePathNameNews: MutableList<String> = mutableListOf()
            val zipFile = ZipFile(fileSource)
            val zipFileEntries = zipFile.entries()
            /////////////////////////////////////////////////////////

            var zipEntry: ZipEntry?
            var ioOffset = 0L
            var ioSizeTotal: Long
            var ioSpeedPerSeconds: Long
            var ioProgress = 0f
            var lastIoOffset = 0L
            var lastTime = System.currentTimeMillis()

            while (zipFileEntries.hasMoreElements()) {
                zipEntry = zipFileEntries.nextElement() ?: continue
                val zipEntryName = zipEntry.name
                if (zipEntryName.containsAny(getIgnorePaths())) continue
                if (!zipEntryName.contains("assets")) continue
                if (!zipEntryName.contains("assets/Android") && !zipEntryName.endsWith("apk")) continue

                UtilKLogWrapper.v(TAG, "startUnzipOnBack: zipEntryName $zipEntryName")

                //创建文件夹
                if (zipEntry.isDirectory) {
                    File(strFilePathDestReal, zipEntryName).createFolder()
                    UtilKLogWrapper.v(TAG, "startUnzipOnBack: zipEntryName $zipEntryName 创建目录")
                    continue
                }

                var zipEntryFile = File(zipEntryName)
                Log.d(TAG, "startUnzipOnBack: $zipEntryName zipEntryFile.absolutePath ${zipEntryFile.absolutePath}")
                zipEntryFile =
                    if (zipEntryFile.parentFile != null && zipEntryFile.parentFile!!.absolutePath.contains("Android")) {//先判断当前文件是否含有路径 如 Android/obb/包名/xx.obb
                        File(Environment.getExternalStorageDirectory(), zipEntryFile.absolutePath.replace("assets${File.separator}", ""))//根目录/Android/obb/包名/xx.obb
                    } else {
                        File(strFilePathDestReal, zipEntryName.replace("assets${File.separator}", ""))//如果保护路径则需要把文件复制到根目录下指定的文件夹中
                    }
                zipEntryFile.parentFile?.createFolder()
                zipEntryFile.deleteFile()//如果文件已经存在，则删除

                if (zipEntryFile.name.endsWithWithAny(getSupportFileTasks().keys)) {
                    strFilePathNameNews.add(zipEntryFile.name)
                }

                var strFilePathName = zipEntryFile.absolutePath
                strFilePathName = (if (strFilePathName.startsWith("/storage/emulated/0/Android/data/${appTask.apkPackageName}/")) {
                    UtilKContextDir.Internal.getDataDir(_context).absolutePath.getStrFolderPath() + strFilePathName.replace("/storage/emulated/0/Android/data/${appTask.apkPackageName}/", "")
                } else if (strFilePathName.startsWith("/storage/emulated/0/Android/obb/${appTask.apkPackageName}/")) {
                    UtilKContextDir.Internal.getObbDir(_context).absolutePath.getStrFolderPath() + strFilePathName.replace("/storage/emulated/0/Android/obb/${appTask.apkPackageName}/", "")
                } else strFilePathName).also { Log.d(TAG, "startUnzipOnBack: dest $it") }

                //移动文件
                zipFile.getInputStream(zipEntry).inputStream2file_use_ofBufferedOutStream(strFilePathName, bufferSize = 1024 * 1024, block = { offset: Int, _: Float ->
                    ioOffset += offset
                    if (System.currentTimeMillis() - lastTime > 1000L) {
                        lastTime = System.currentTimeMillis()
                        ioSizeTotal = if (ioOffset > appTask.taskDownloadFileSizeTotal) ioOffset else appTask.taskDownloadFileSizeTotal
                        ioSpeedPerSeconds = ioOffset - lastIoOffset
                        ioProgress = (ioOffset.toFloat() / ioSizeTotal.toFloat()).constraint(0f, 1f) * 100f
                        lastIoOffset = ioOffset
                        UtilKHandlerWrapper.post {
                            onTaskStarted(CTaskState.STATE_UNZIPING, appTask.apply {
                                taskDownloadFileSpeed = ioSpeedPerSeconds
                                taskDownloadFileSizeOffset = ioOffset
                                taskDownloadProgress = ioProgress.toInt()
                            })
                        }
                    }
                })
            }
            zipFile.close()

            //assets没有包含
            if (strFilePathNameNews.isEmpty()) {
                strFilePathDestReal.deleteFolder()
                UtilKLogWrapper.d(TAG, "startUnzipOnBack: 删除解压文件夹")
                return fileSource.absolutePath
            }
            UtilKLogWrapper.d(TAG, "startUnzipOnBack: strFilePathNameNews $strFilePathNameNews")

            //assets没有包含指定文件
            var strFilePathNameNew: String = strFilePathNameNews.get(0)
            if (_sniffTargetFiles.isNotEmpty()) {
                var hasTargetFile = false
                run loop@{
                    _sniffTargetFiles.forEach { sniffTargetFile ->
                        hasTargetFile = strFilePathNameNews.contains(sniffTargetFile)
                        if (hasTargetFile) {
                            strFilePathNameNew = sniffTargetFile
                            return@loop
                        }
                    }
                }
                if (!hasTargetFile) {
                    strFilePathDestReal.deleteFolder()
                    UtilKLogWrapper.d(TAG, "startUnzipOnBack: 删除解压文件夹")
                    return fileSource.absolutePath
                }
            }

            return ("$strFilePathDestReal${File.separator}$strFilePathNameNew").also { UtilKLogWrapper.d(TAG, "unzipApkOnBack: 保留解压文件夹 $it") }
        } catch (e: Exception) {
            strFilePathDestReal.deleteFolder()
            UtilKLogWrapper.d(TAG, "startUnzipOnBack: 删除解压文件夹")
            e.printStackTrace()
            return fileSource.absolutePath.also { UtilKLogWrapper.e(TAG, "unzipOnBack: error ${e.message}") }
        }
    }
}