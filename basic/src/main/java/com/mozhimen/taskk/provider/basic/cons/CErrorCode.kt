package com.mozhimen.taskk.provider.basic.cons

import com.mozhimen.taskk.provider.basic.annors.ATaskState

/**
 * @ClassName CAppDownloadErrorCode
 * @Description TODO
 * @Author Mozhimen
 * @Date 2023/11/7 13:58
 * @Version 1.0
 */
fun Int.intErrorCode2strError(): String =
    CErrorCode.intAppErrorCode2strAppError(this)

object CErrorCode {
    const val CODE_TASK_FAIL = 0
    const val CODE_TASK_NEED_MEMORY = 1//"存储空间不足，请清理内存后再试"

    const val CODE_TASK_DOWNLOAD_FAIL = ATaskState.STATE_DOWNLOAD_CREATE + 0//
    const val CODE_TASK_DOWNLOAD_PATH_NOT_EXIST = ATaskState.STATE_DOWNLOAD_CREATE + 1//"下载路径不存在"
    const val CODE_TASK_DOWNLOAD_CANT_FIND_TASK = ATaskState.STATE_DOWNLOAD_CREATE + 2//"未找到下载任务！"
    const val CODE_TASK_DOWNLOAD_SERVER_CANCELED = ATaskState.STATE_DOWNLOAD_CREATE + 3//
    const val CODE_TASK_DOWNLOAD_ENOUGH = ATaskState.STATE_DOWNLOAD_CREATE + 4//

    const val CODE_TASK_VERIFY_FAIL = ATaskState.STATE_VERIFY_CREATE + 0
    const val CODE_TASK_VERIFY_DIR_NULL = ATaskState.STATE_VERIFY_CREATE + 1
    const val CODE_TASK_VERIFY_FILE_NOT_EXIST = ATaskState.STATE_VERIFY_CREATE + 2
    const val CODE_TASK_VERIFY_MD5_FAIL = ATaskState.STATE_VERIFY_CREATE + 3
    const val CODE_TASK_VERIFY_FORMAT_INVALID = ATaskState.STATE_VERIFY_CREATE + 4

    const val CODE_TASK_UNZIP_FAIL = ATaskState.STATE_UNZIP_CREATE + 0
    const val CODE_TASK_UNZIP_DIR_NULL = ATaskState.STATE_UNZIP_CREATE + 1
    const val CODE_TASK_UNZIP_CANCEL_FAIL = ATaskState.STATE_UNZIP_CREATE + 2//正在解压, 无法删除

    const val CODE_TASK_INSTALL_FAIL = ATaskState.STATE_INSTALL_CREATE + 0
    const val CODE_TASK_INSTALL_HAST_VERIFY_OR_UNZIP = ATaskState.STATE_INSTALL_CREATE + 1
    const val CODE_TASK_INSTALL_HAS_INSTALLED = ATaskState.STATE_INSTALL_CREATE + 2//已经安装

    const val CODE_TASK_OPEN_FAIL = ATaskState.STATE_OPEN_CREATE + 0

    /////////////////////////////////////////////////////////////////

    @JvmStatic
    fun intAppErrorCode2strAppError(code: Int): String =
        when (code) {
            CODE_TASK_NEED_MEMORY -> "安装所需空间不足"

            CODE_TASK_DOWNLOAD_FAIL -> "下载失败"
            CODE_TASK_DOWNLOAD_PATH_NOT_EXIST -> "下载路径不存在"
            CODE_TASK_DOWNLOAD_CANT_FIND_TASK -> "下载任务丢失"
            CODE_TASK_DOWNLOAD_SERVER_CANCELED -> "下载服务取消"
            CODE_TASK_DOWNLOAD_ENOUGH -> "下载队列已满, 请稍候再试吧"

            CODE_TASK_VERIFY_FAIL -> "验证失败"
            CODE_TASK_VERIFY_DIR_NULL -> "验证路径为空"
            CODE_TASK_VERIFY_FILE_NOT_EXIST -> "验证文件不存在"
            CODE_TASK_VERIFY_MD5_FAIL -> "验证MD5失败"
            CODE_TASK_VERIFY_FORMAT_INVALID -> "验证格式非法"

            CODE_TASK_UNZIP_FAIL -> "解压失败"
            CODE_TASK_UNZIP_DIR_NULL -> "解压路径为空"
            CODE_TASK_UNZIP_CANCEL_FAIL -> "在解压时任务取消失败"

            CODE_TASK_INSTALL_FAIL -> "安装失败"
            CODE_TASK_INSTALL_HAST_VERIFY_OR_UNZIP -> "安装文件未解压或验证"
            CODE_TASK_INSTALL_HAS_INSTALLED -> "已经安装"

            CODE_TASK_OPEN_FAIL -> "打开失败"
            else -> "流程出现错误"
        }
}