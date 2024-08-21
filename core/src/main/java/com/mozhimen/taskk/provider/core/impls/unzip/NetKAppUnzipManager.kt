package com.mozhimen.taskk.provider.core.impls.unzip

import android.os.Environment
import androidx.annotation.WorkerThread
import com.mozhimen.basick.lintk.optins.OApiInit_InApplication
import com.mozhimen.basick.taskk.executor.TaskKExecutor
import com.mozhimen.basick.taskk.handler.TaskKHandler
import com.mozhimen.basick.utilk.android.util.UtilKLogWrapper
import com.mozhimen.basick.utilk.commons.IUtilK
import com.mozhimen.basick.utilk.java.io.createFolder
import com.mozhimen.basick.utilk.java.io.deleteFile
import com.mozhimen.basick.utilk.java.io.flushClose
import com.mozhimen.basick.utilk.kotlin.collections.containsBy
import com.mozhimen.basick.utilk.kotlin.createFolder
import com.mozhimen.basick.utilk.kotlin.deleteFolder
import com.mozhimen.basick.utilk.kotlin.getSplitLastIndexToStart
import com.mozhimen.basick.utilk.kotlin.strFilePath2file
import com.mozhimen.taskk.provider.basic.cons.CErrorCode
import com.mozhimen.taskk.provider.basic.db.AppTask
import com.mozhimen.taskk.provider.basic.impls.TaskException
import com.mozhimen.taskk.provider.basic.impls.intErrorCode2taskException
import com.mozhimen.taskk.provider.core.NetKApp
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.CopyOnWriteArrayList
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

/**
 * @ClassName AppZipManager
 * @Description TODO
 * @Author Mozhimen
 * @Date 2023/11/7 15:09
 * @Version 1.0
 */
@OApiInit_InApplication
internal object NetKAppUnzipManager : IUtilK {
    /**
     *  过滤在mac上压缩时自动生成的__MACOSX文件夹
     */
    private const val MAC__IGNORE = "__MACOSX/"

    //////////////////////////////////////////////////////////////////


    //    private val _unzippingProgressTasks = CopyOnWriteArrayList<AppTask>()
    private var _strSourceApkNameUnzip = ""

    //////////////////////////////////////////////////////////////////

    /**
     * strSourceApkNameUnzip 解压后的apk名称
     */
    @JvmStatic
    fun init(strSourceApkNameUnzip: String) {
        _strSourceApkNameUnzip = strSourceApkNameUnzip
    }

    /**
     * 判断当前应用是否在解压过程中
     * @return true 表示正在解压中 false 不在解压中
     */
    @JvmStatic
    fun isUnziping(appTask: AppTask): Boolean {
        return _unzippingTasks.containsBy { it.taskId == appTask.taskId } && appTask.atTaskUnzip()
    }

    @JvmStatic
    fun hasUnziping(): Boolean {
        return _unzippingTasks.isNotEmpty()
    }

    //////////////////////////////////////////////////////////////////

    @JvmStatic
    fun unzip(appTask: AppTask) {
//        if (isUnziping(appTask)) {//正在解压
//            UtilKLogWrapper.d(TAG, "unzip: the task already unziping")
//            return
//        }
        /**
         * [CNetKAppState.STATE_UNZIPING]
         */
        onUnziping(appTask, 100, appTask.taskDownloadFileSizeTotal, appTask.taskDownloadFileSizeTotal, 0)

        if (appTask.fileNameExt.endsWith("apk") && !appTask.taskUnzipEnable) {
            onUnzipSuccess(appTask)
        }

        TaskKExecutor.execute(TAG + "unzip") {
            try {
                val strPathNameUnzip = unzipOnBack(appTask)
                UtilKLogWrapper.d(TAG, "unzip: strPathNameUnzip $strPathNameUnzip")
                if (strPathNameUnzip.isEmpty())
                    throw CErrorCode.CODE_TASK_UNZIP_FAIL.intErrorCode2taskException()
                else if (!strPathNameUnzip.endsWith("apk"))
                    throw CErrorCode.CODE_TASK_UNZIP_FAIL.intErrorCode2taskException()

                /////////////////////////////////////////////////////////

//                TaskKHandler.post {
//                    onUnzipSuccess(appTask.apply { apkPathName = strPathNameUnzip })
//                }
            } catch (e: TaskException) {
                TaskKHandler.post {
                    /**
                     * [CNetKAppState.STATE_UNZIP_FAIL]
                     */
                    NetKApp.instance.onUnzipFail(appTask, e)
                }
            }
        }
    }

    private fun onUnzipSuccess(appTask: AppTask) {
        UtilKLogWrapper.d(TAG, "onUnzipSuccess: 解压成功 appTask $appTask")

        /**
         * [CNetKAppState.STATE_UNZIP_SUCCESS]
         */
        NetKApp.instance.onUnzipSuccess(appTask)
    }

    private fun onUnziping(appTask: AppTask, progress: Int, currentIndex: Long, totalIndex: Long, offsetIndexPerSeconds: Long) {
        UtilKLogWrapper.v(TAG, "onUnziping: 解压进度 progress $progress")
        /**
         * [CNetKAppState.STATE_UNZIPING]
         */
        NetKApp.instance.onUnziping(appTask, progress, currentIndex, totalIndex, offsetIndexPerSeconds)
    }

    private val _unzippingTasks = CopyOnWriteArrayList<AppTask>()

    @WorkerThread
    private fun unzipOnBack(appTask: AppTask): String {
        _unzippingTasks.add(appTask)//开始解压，添加到列表中

        val externalFilesDownloadDir = NetKApp.instance.getDownloadPath() ?: run {
            throw CErrorCode.CODE_TASK_UNZIP_DIR_NULL.intErrorCode2taskException()
        }
        val fileSource = appTask.filePathNameExt.strFilePath2file()
        UtilKLogWrapper.d(TAG, "unzipOnBack: fileSource ${fileSource.absolutePath}")

        val strPathNameApk = if (appTask.fileNameExt.endsWith(".npk"))
            unzipNpkOnBack(fileSource, externalFilesDownloadDir.absolutePath)
        else
            unzipApkOnBack(fileSource, appTask, externalFilesDownloadDir.absolutePath)

        _unzippingTasks.remove(appTask)//解压成功，移除
        return strPathNameApk
    }

    //////////////////////////////////////////////////////////////////

    /**
     * 解压文件
     * @param fileSource 需要解压的文件
     * @param strFilePathDest 目标路径 当前项目中使用的是 /Android/data/包名/Download/文件名/
     */
    @WorkerThread
    private fun unzipNpkOnBack(fileSource: File, strFilePathDest: String): String {
        try {
            val fileNameReal = fileSource.name.getSplitLastIndexToStart(".", false)//name.subSequence(0, name.lastIndexOf("."))
            val strFilePathDestReal = (strFilePathDest + File.separator + fileNameReal).also { UtilKLogWrapper.d(TAG, "unzipOnBack: strFilePathDestReal $it") }

            //用来记录apk的文件名
            var apkName = ""
            val zipFile = ZipFile(fileSource)
            val entries = zipFile.entries()
            val bytes = ByteArray(1024 * 1024)
            var zipEntry: ZipEntry?
            while (entries.hasMoreElements()) {
                zipEntry = entries.nextElement() ?: continue
                if (zipEntry.name.contains(MAC__IGNORE)) continue
                if (zipEntry.isDirectory) {
                    File(strFilePathDestReal, zipEntry.name).createFolder()
                    continue
                }
                var tempFile = File(zipEntry.name)

                tempFile.parentFile?.let {//先判断当前文件是否含有路径 如 Android/obb/包名/xx.obb
                    tempFile = File(Environment.getExternalStorageDirectory(), tempFile.absolutePath)//根目录/Android/obb/包名/xx.obb
                } ?: kotlin.run {
                    tempFile = File(strFilePathDestReal, zipEntry.name)//如果保护路径则需要把文件复制到根目录下指定的文件夹中
                }
                tempFile.parentFile?.createFolder()
                UtilKLogWrapper.d(TAG, "unzipOnBack: tempFilePath ${tempFile.absolutePath}")
                //如果文件已经存在，则删除
                tempFile.deleteFile()
                if (tempFile.name.endsWith(".apk")) {
                    apkName = tempFile.name
                }
                val bufferedOutputStream = BufferedOutputStream(FileOutputStream(tempFile))
                val inputStream = zipFile.getInputStream(zipEntry)
                var len: Int
                while ((inputStream.read(bytes).also { len = it }) != -1) {
                    bufferedOutputStream.write(bytes, 0, len)
                }
                bufferedOutputStream.flushClose()
                inputStream.close()
            }
            zipFile.close()
            return strFilePathDestReal + File.separator + apkName
        } catch (e: Exception) {
            e.printStackTrace()
            UtilKLogWrapper.e(TAG, "unzipOnBack: error ${e.message}")
//            throw CNetKAppErrorCode.CODE_UNZIP_FAIL.intErrorCode2taskException()
            return ""
        }
    }

    /**
     *
     * @param apkFileSource File
     * @param strApkFilePathDest String /storage/emulated/0/data/com.xx.xxx/files/Download/
     */
    @WorkerThread
    private fun unzipApkOnBack(apkFileSource: File, appTask: AppTask, strApkFilePathDest: String): String {
        val strApkFileNameReal = apkFileSource.name.getSplitLastIndexToStart(".", false)//name.subSequence(0, name.lastIndexOf("."))
        val strApkFilePathDestReal = (strApkFilePathDest + File.separator + strApkFileNameReal).also { UtilKLogWrapper.d(TAG, "unzipOnBack: strFilePathDestReal $it") }

        try {
            strApkFilePathDestReal.createFolder()
            ///storage/emulated/0/data/com.xx.xxx/files/Download/xxx/

            //用来记录apk的文件名
            var apkFileName = ""
            val zipFile = ZipFile(apkFileSource)
            val zipFileEntries = zipFile.entries()
//            val bytes = ByteArray(1024 * 1024)
            /////////////////////////////////////////////////////////

            var zipEntry: ZipEntry?
            var totalOffset = 0L
            var totalSize = 0L
            var lastOffset = 0L
            var lastTime = System.currentTimeMillis()
            while (zipFileEntries.hasMoreElements()) {
                zipEntry = zipFileEntries.nextElement() ?: continue
                val zipEntryName = zipEntry.name
                if (zipEntryName.contains(MAC__IGNORE)) continue
                if (!zipEntryName.contains("assets")) continue
                if (!zipEntryName.contains("assets/Android") && !zipEntryName.endsWith("apk")) continue
                UtilKLogWrapper.v(TAG, "unzipApkOnBack: zipEntryName ${zipEntryName}")

//                if (zipEntry.isDirectory) {
//                    File(strApkFilePathDestReal, zipEntryName).createFolder()
//                    continue
//                }
//
//                var zipEntryFile = File(zipEntryName)
//
//                zipEntryFile = if (zipEntryFile.parentFile != null && zipEntryFile.parentFile!!.absolutePath.contains("Android")) {//先判断当前文件是否含有路径 如 Android/obb/包名/xx.obb
//                    File(Environment.getExternalStorageDirectory(), zipEntryFile.absolutePath.replace("assets" + File.separator, ""))//根目录/Android/obb/包名/xx.obb
//                } else {
//                    File(strApkFilePathDestReal, zipEntryName.replace("assets" + File.separator, ""))//如果保护路径则需要把文件复制到根目录下指定的文件夹中
//                }
//                zipEntryFile.parentFile?.createFolder()
//                UtilKLogWrapper.d(TAG, "unzipApkOnBack: tempFilePath ${zipEntryFile.absolutePath}")
//
//                zipEntryFile.deleteFile()//如果文件已经存在，则删除
//                if (zipEntryFile.name.endsWith("apk")) {
//                    apkFileName = zipEntryFile.name
//                    UtilKLogWrapper.d(TAG, "unzipApkOnBack: apkFileName $apkFileName")
//                }
//
//                //移动文件
//                zipFile.getInputStream(zipEntry).inputStream2file_use_ofBufferedOutStream(zipEntryFile, bufferSize = 1024 * 1024, block = { offset: Int, _: Float ->
//                    totalOffset += offset
//                    if (System.currentTimeMillis() - lastTime > 1000L) {
//                        lastTime = System.currentTimeMillis()
//                        totalSize = if (totalOffset > appTask.apkFileSize) totalOffset else appTask.apkFileSize
//                        val offsetIndexPerSeconds = totalOffset - lastOffset
//                        lastOffset = totalOffset
//                        val progress = (totalOffset.toFloat() / totalSize.toFloat()).constraint(0f, 1f) * 100f
//                        TaskKHandler.post {
//                            onUnziping(appTask, progress.toInt(), totalOffset, totalSize, offsetIndexPerSeconds)
//                        }
//                    }
//                })
            }
            zipFile.close()

            //结束
            if (apkFileName.isEmpty() || (_strSourceApkNameUnzip.isNotEmpty() && apkFileName != _strSourceApkNameUnzip)) {
                strApkFilePathDestReal.deleteFolder()
                return apkFileSource.absolutePath.also { UtilKLogWrapper.d(TAG, "unzipApkOnBack: 删除解压文件夹") }
            }

            return (strApkFilePathDestReal + File.separator + apkFileName).also { UtilKLogWrapper.d(TAG, "unzipApkOnBack: 保留解压文件夹 $it") }
        } catch (e: Exception) {
            strApkFilePathDestReal.deleteFolder()
            e.printStackTrace()
            return apkFileSource.absolutePath.also { UtilKLogWrapper.e(TAG, "unzipOnBack: error ${e.message}") }
//            throw CNetKAppErrorCode.CODE_UNZIP_FAIL.intErrorCode2taskException()
        }
    }
}