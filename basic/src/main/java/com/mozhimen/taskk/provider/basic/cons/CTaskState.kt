package com.mozhimen.taskk.provider.basic.cons

/**
 * @ClassName CAppDownloadState
 * @Description TODO
 * @Author Mozhimen
 * @Date 2023/11/7 11:36
 * @Version 1.0
 */
object CTaskState {
    const val STATE_DOWNLOAD_CREATE = 10//STATE_DOWNLOADED = 5//未下载

    //    const val STATE_DOWNLOAD_WAIT = STATE_DOWNLOAD_CREATE + CNetKAppTaskState.STATE_TASK_WAIT//11//下载等待
    const val STATE_DOWNLOADING = STATE_DOWNLOAD_CREATE + CState.STATE_TASKING//12//STATE_DOWNLOAD_IN_PROGRESS = 6//正在下载
    const val STATE_DOWNLOAD_PAUSE = STATE_DOWNLOAD_CREATE + CState.STATE_TASK_PAUSE//13//STATE_DOWNLOAD_PAUSED = 7//下载暂停
    const val STATE_DOWNLOAD_CANCEL = STATE_DOWNLOAD_CREATE + CState.STATE_TASK_CANCEL//17//下载取消
    const val STATE_DOWNLOAD_SUCCESS = STATE_DOWNLOAD_CREATE + CState.STATE_TASK_SUCCESS//18//STATE_DOWNLOAD_COMPLETED = 8//下载完成
    const val STATE_DOWNLOAD_FAIL = STATE_DOWNLOAD_CREATE + CState.STATE_TASK_FAIL//19//STATE_DOWNLOAD_FAILED = 10//下载失败

    @JvmStatic
    fun canTaskDownload(state: Int): Boolean =
        state in STATE_DOWNLOAD_CREATE..STATE_DOWNLOAD_PAUSE || state in CState.STATE_TASK_CREATE..CState.STATE_TASK_UPDATE

    @JvmStatic
    fun atTaskDownload(state: Int): Boolean =
        state in STATE_DOWNLOAD_CREATE..STATE_DOWNLOAD_FAIL

    @JvmStatic
    fun isTaskDownloading(state: Int): Boolean =
        state == STATE_DOWNLOADING

    @JvmStatic
    fun isTaskDownloadSuccess(state: Int): Boolean =
        state >= STATE_DOWNLOAD_SUCCESS || state == CState.STATE_TASK_SUCCESS

    //////////////////////////////////////////////////////////////
    //校验
    const val STATE_VERIFY_CREATE = 20
    const val STATE_VERIFYING = STATE_VERIFY_CREATE + CState.STATE_TASKING//20//STATE_CHECKING = 14//校验中
    const val STATE_VERIFY_PAUSE = STATE_VERIFY_CREATE + CState.STATE_TASK_PAUSE//23
    const val STATE_VERIFY_CANCEL = STATE_VERIFY_CREATE + CState.STATE_TASK_CANCEL//27//STATE_CHECKING_SUCCESS = 15//校验成功
    const val STATE_VERIFY_SUCCESS = STATE_VERIFY_CREATE + CState.STATE_TASK_SUCCESS//28//STATE_CHECKING_SUCCESS = 15//校验成功
    const val STATE_VERIFY_FAIL = STATE_VERIFY_CREATE + CState.STATE_TASK_FAIL//29//STATE_CHECKING_FAILURE = 16//校验失败

    @JvmStatic
    fun canTaskVerify(state: Int): Boolean =
        state in STATE_DOWNLOAD_SUCCESS..STATE_VERIFY_PAUSE

    @JvmStatic
    fun atTaskVerify(state: Int): Boolean =
        state in STATE_VERIFY_CREATE..STATE_VERIFY_FAIL

    @JvmStatic
    fun isTaskVerifying(state: Int): Boolean =
        state == STATE_VERIFYING

    @JvmStatic
    fun isTaskVerifySuccess(state: Int): Boolean =
        state >= STATE_VERIFY_SUCCESS || state == CState.STATE_TASK_SUCCESS

    //////////////////////////////////////////////////////////////
    //解压
    const val STATE_UNZIP_CREATE = 30
    const val STATE_UNZIPING = STATE_UNZIP_CREATE + CState.STATE_TASKING//30//STATE_UNPACKING = 11//解压中
    const val STATE_UNZIP_PAUSE = STATE_UNZIP_CREATE + CState.STATE_TASK_PAUSE//33
    const val STATE_UNZIP_CANCEL = STATE_UNZIP_CREATE + CState.STATE_TASK_CANCEL//37//STATE_UNPACKING_SUCCESSFUL = 12//解压成功
    const val STATE_UNZIP_SUCCESS = STATE_UNZIP_CREATE + CState.STATE_TASK_SUCCESS//38//STATE_UNPACKING_SUCCESSFUL = 12//解压成功
    const val STATE_UNZIP_FAIL = STATE_UNZIP_CREATE + CState.STATE_TASK_FAIL//39//STATE_UNPACKING_FAILED = 13//解压失败

    @JvmStatic
    fun canTaskUnzip(state: Int): Boolean =
        state in STATE_VERIFY_SUCCESS..STATE_UNZIP_PAUSE

    @JvmStatic
    fun atTaskUnzip(state: Int): Boolean =
        state in STATE_UNZIP_CREATE..STATE_UNZIP_FAIL

    @JvmStatic
    fun isTaskUnziping(state: Int): Boolean =
        state == STATE_UNZIPING

    @JvmStatic
    fun isTaskUnzipSuccess(state: Int): Boolean =
        state >= STATE_UNZIP_SUCCESS || state == CState.STATE_TASK_SUCCESS

    //////////////////////////////////////////////////////////////
    //安装
    const val STATE_INSTALL_CREATE = 40
    const val STATE_INSTALLING = STATE_INSTALL_CREATE + CState.STATE_TASKING//42//STATE_INSTALLING = 1//安装中
    const val STATE_INSTALL_PAUSE = STATE_INSTALL_CREATE + CState.STATE_TASK_PAUSE//43
    const val STATE_INSTALL_CANCEL = STATE_INSTALL_CREATE + CState.STATE_TASK_CANCEL//47//STATE_INSTALLED = 2//安装取消
    const val STATE_INSTALL_SUCCESS = STATE_INSTALL_CREATE + CState.STATE_TASK_SUCCESS//48//STATE_INSTALLED = 2//已安装
    const val STATE_INSTALL_FAIL = STATE_INSTALL_CREATE + CState.STATE_TASK_FAIL//49//STATE_INSTALLED = 2//已安装

    @JvmStatic
    fun canTaskInstall(state: Int): Boolean =
        state in STATE_UNZIP_SUCCESS..STATE_INSTALL_PAUSE

    @JvmStatic
    fun atTaskInstall(state: Int): Boolean =
        state in STATE_INSTALL_CREATE..STATE_INSTALL_FAIL

    @JvmStatic
    fun isTaskInstalling(state: Int): Boolean =
        state == STATE_INSTALLING

    @JvmStatic
    fun isTaskInstallSuccess(state: Int): Boolean =
        state >= STATE_INSTALL_SUCCESS || state == CState.STATE_TASK_SUCCESS

    //////////////////////////////////////////////////////////////

    //打开
    const val STATE_OPEN_CREATE = 50
    const val STATE_OPENING = STATE_OPEN_CREATE + CState.STATE_TASKING//52
    const val STATE_OPEN_PAUSE = STATE_OPEN_CREATE + CState.STATE_TASK_PAUSE//53
    const val STATE_OPEN_CANCEL = STATE_OPEN_CREATE + CState.STATE_TASK_CANCEL//57
    const val STATE_OPEN_SUCCESS = STATE_OPEN_CREATE + CState.STATE_TASK_SUCCESS//58
    const val STATE_OPEN_FAIL = STATE_OPEN_CREATE + CState.STATE_TASK_FAIL//59

    @JvmStatic
    fun canTaskOpen(state: Int): Boolean =
        state in STATE_INSTALL_SUCCESS..STATE_OPEN_PAUSE

    @JvmStatic
    fun atTaskOpen(state: Int): Boolean =
        state in STATE_OPEN_CREATE..STATE_OPEN_FAIL

    @JvmStatic
    fun isTaskOpening(state: Int): Boolean =
        state == STATE_OPENING

    @JvmStatic
    fun isTaskOpenSuccess(state: Int): Boolean =
        state >= STATE_OPEN_SUCCESS || state == CState.STATE_TASK_SUCCESS

    //////////////////////////////////////////////////////////////

    //卸载
    const val STATE_UNINSTALL_CREATE = 70
    const val STATE_UNINSTALLING = STATE_UNINSTALL_CREATE + CState.STATE_TASKING//52
    const val STATE_UNINSTALL_PAUSE = STATE_UNINSTALL_CREATE + CState.STATE_TASK_PAUSE//53
    const val STATE_UNINSTALL_CANCEL = STATE_UNINSTALL_CREATE + CState.STATE_TASK_CANCEL//57
    const val STATE_UNINSTALL_SUCCESS = STATE_UNINSTALL_CREATE + CState.STATE_TASK_SUCCESS//58
    const val STATE_UNINSTALL_FAIL = STATE_UNINSTALL_CREATE + CState.STATE_TASK_FAIL//59

    @JvmStatic
    fun canTaskUninstall(state: Int): Boolean =
        state in STATE_INSTALL_SUCCESS..STATE_UNINSTALL_PAUSE

    @JvmStatic
    fun atTaskUninstall(state: Int): Boolean =
        state in STATE_UNINSTALL_CREATE..STATE_UNINSTALL_FAIL

    @JvmStatic
    fun isTaskUninstalling(state: Int): Boolean =
        state == STATE_UNINSTALLING

    @JvmStatic
    fun isTaskUninstallSuccess(state: Int): Boolean =
        state >= STATE_UNINSTALL_SUCCESS || state == CState.STATE_TASK_SUCCESS

    //////////////////////////////////////////////////////////////

    /*//更新
    const val STATE_UPDATE_CREATE = 50//需要更新
    const val STATE_UPDATEING = 51//STATE_NEED_UPDATE = 17//更新中
    const val STATE_UPDATE_SUCCESS = 58
    const val STATE_UPDATE_FAIL = 59*/
}