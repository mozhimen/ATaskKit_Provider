package com.mozhimen.taskk.provider.audio.impls

import com.mozhimen.taskk.provider.audio.cons.CExt
import com.mozhimen.taskk.provider.basic.bases.ATaskManagerProvider
import com.mozhimen.taskk.provider.basic.commons.ITaskLifecycle
import com.mozhimen.taskk.provider.download.okdownload.TaskDownloadOkDownload

/**
 * @ClassName TaskDownloadAudio
 * @Description TODO
 * @Author mozhimen
 * @Date 2025/1/23
 * @Version 1.0
 */
open class TaskDownloadOkDownloadAudio(
    taskManager: ATaskManagerProvider,
    iTaskLifecycle: ITaskLifecycle,
) : TaskDownloadOkDownload(taskManager, iTaskLifecycle) {
    override fun getSupportFileExts(): List<String> {
        return listOf(
            CExt.EXT_MP3,
            CExt.EXT_MP3,
            CExt.EXT_AVI,
            CExt.EXT_MKV,
            CExt.EXT_MOV,
            CExt.EXT_WMV,
            CExt.EXT_FLV,
            CExt.EXT_WEBM,
            CExt.EXT_3GP,
            CExt.EXT_MPEG,
            CExt.EXT_VOB,
            CExt.EXT_TS,
            CExt.EXT_ASF,
            CExt.EXT_M2TS,
            CExt.EXT_BDMV,
        )
    }
}