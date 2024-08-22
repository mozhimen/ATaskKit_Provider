package com.mozhimen.taskk.provider.apk.impls

import com.mozhimen.basick.utilk.android.util.UtilKLogWrapper
import com.mozhimen.basick.utilk.java.io.createFolder
import com.mozhimen.basick.utilk.java.io.deleteFile
import com.mozhimen.basick.utilk.kotlin.containAny
import com.mozhimen.basick.utilk.kotlin.createFolder
import com.mozhimen.basick.utilk.kotlin.deleteFolder
import com.mozhimen.basick.utilk.kotlin.getSplitLastIndexToStart
import com.mozhimen.taskk.provider.apk.cons.CExt
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskProviderUnzip
import com.mozhimen.taskk.provider.basic.cons.CTaskState
import com.mozhimen.taskk.provider.basic.cons.STaskFinishType
import com.mozhimen.taskk.provider.basic.db.AppTask
import com.mozhimen.taskk.provider.basic.interfaces.ITaskProviderLifecycle
import java.io.File
import java.util.concurrent.CopyOnWriteArrayList
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

/**
 * @ClassName TaskProviderUnzipApk
 * @Description TODO
 * @Author Mozhimen / Kolin Zhao
 * @Date 2024/8/21 21:41
 * @Version 1.0
 */
class TaskProviderUnzipApk(iTaskProviderLifecycle: ITaskProviderLifecycle?) : ATaskProviderUnzip(iTaskProviderLifecycle) {
    override val unzipTasks: CopyOnWriteArrayList<AppTask> = CopyOnWriteArrayList()

    override fun getIgnorePaths(): List<String> {
        return listOf("__MACOSX/")
    }

    override fun getSupportFileExtensions(): List<String> {
        return listOf(CExt.EXT_APK)
    }

    //////////////////////////////////////////////////////////////////

    override fun taskStart(appTask: AppTask) {
        super.taskStart(appTask)
        if (appTask.taskUnzipEnable) {
            startUnzip(appTask)
        } else {
            onTaskFinished(CTaskState.STATE_UNZIP_SUCCESS, STaskFinishType.SUCCESS, appTask)
        }
    }

    //////////////////////////////////////////////////////////////////

    private fun startUnzip(appTask: AppTask) {

    }

    private fun startUnzipOnBack(fileSource: File, strFilePathDest: String) {
        val strFileNameReal = fileSource.name.getSplitLastIndexToStart(".", false)//name.subSequence(0, name.lastIndexOf("."))
        val strFilePathNameDestReal = ("$strFilePathDest${File.separator}$strFileNameReal").also { UtilKLogWrapper.d(TAG, "unzipOnBack: strFilePathDestReal $it") }

        try {
            strFilePathNameDestReal.createFolder()
            ///storage/emulated/0/data/com.xx.xxx/files/Download/xxx/

            //用来记录apk的文件名
            var apkFileName = ""
            val zipFile = ZipFile(fileSource)
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
                if (zipEntryName.containAny(getIgnorePaths())) continue
                if (!zipEntryName.contains("assets")) continue
                if (!zipEntryName.contains("assets/Android") && !zipEntryName.endsWith("apk")) continue

                UtilKLogWrapper.v(TAG, "startUnzipOnBack: zipEntryName ${zipEntryName}")

                if (zipEntry.isDirectory) {
                    File(strFilePathNameDestReal, zipEntryName).createFolder()
                    continue
                }

                var zipEntryFile = File(zipEntryName)
                zipEntryFile = if (zipEntryFile.parentFile != null && zipEntryFile.parentFile!!.absolutePath.contains("Android")) {//先判断当前文件是否含有路径 如 Android/obb/包名/xx.obb
                    File(Environment.getExternalStorageDirectory(), zipEntryFile.absolutePath.replace("assets" + File.separator, ""))//根目录/Android/obb/包名/xx.obb
                } else {
                    File(strFilePathNameDestReal, zipEntryName.replace("assets" + File.separator, ""))//如果保护路径则需要把文件复制到根目录下指定的文件夹中
                }
                zipEntryFile.parentFile?.createFolder()
                UtilKLogWrapper.d(TAG, "unzipApkOnBack: tempFilePath ${zipEntryFile.absolutePath}")

                zipEntryFile.deleteFile()//如果文件已经存在，则删除
                if (zipEntryFile.name.endsWith("apk")) {
                    apkFileName = zipEntryFile.name
                    UtilKLogWrapper.d(TAG, "unzipApkOnBack: apkFileName $apkFileName")
                }

                //移动文件
                zipFile.getInputStream(zipEntry).inputStream2file_use_ofBufferedOutStream(zipEntryFile, bufferSize = 1024 * 1024, block = { offset: Int, _: Float ->
                    totalOffset += offset
                    if (System.currentTimeMillis() - lastTime > 1000L) {
                        lastTime = System.currentTimeMillis()
                        totalSize = if (totalOffset > appTask.apkFileSize) totalOffset else appTask.apkFileSize
                        val offsetIndexPerSeconds = totalOffset - lastOffset
                        lastOffset = totalOffset
                        val progress = (totalOffset.toFloat() / totalSize.toFloat()).constraint(0f, 1f) * 100f
                        TaskKHandler.post {
                            onUnziping(appTask, progress.toInt(), totalOffset, totalSize, offsetIndexPerSeconds)
                        }
                    }
                })
            }
            zipFile.close()

            //结束
            if (apkFileName.isEmpty() || (_strSourceApkNameUnzip.isNotEmpty() && apkFileName != _strSourceApkNameUnzip)) {
                strFilePathNameDestReal.deleteFolder()
                return fileSource.absolutePath.also { UtilKLogWrapper.d(TAG, "unzipApkOnBack: 删除解压文件夹") }
            }

            return (strFilePathNameDestReal + File.separator + apkFileName).also { UtilKLogWrapper.d(TAG, "unzipApkOnBack: 保留解压文件夹 $it") }
        } catch (e: Exception) {
            strFilePathNameDestReal.deleteFolder()
            e.printStackTrace()
            return fileSource.absolutePath.also { UtilKLogWrapper.e(TAG, "unzipOnBack: error ${e.message}") }
//            throw CNetKAppErrorCode.CODE_UNZIP_FAIL.intErrorCode2taskException()
        }
    }
}