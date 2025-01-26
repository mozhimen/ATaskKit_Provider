package com.mozhimen.taskk.provider.test

import android.annotation.SuppressLint
import android.os.Bundle
import com.mozhimen.bindk.bases.viewbinding.activity.BaseActivityVB
import com.mozhimen.kotlin.elemk.commons.I_Listener
import com.mozhimen.kotlin.lintk.optins.OApiInit_InApplication
import com.mozhimen.kotlin.lintk.optins.permission.OPermission_MANAGE_EXTERNAL_STORAGE
import com.mozhimen.kotlin.lintk.optins.permission.OPermission_READ_EXTERNAL_STORAGE
import com.mozhimen.kotlin.lintk.optins.permission.OPermission_WRITE_EXTERNAL_STORAGE
import com.mozhimen.kotlin.utilk.android.util.UtilKLogWrapper
import com.mozhimen.manifestk.xxpermissions.XXPermissionsCheckUtil
import com.mozhimen.manifestk.xxpermissions.XXPermissionsRequestUtil
import com.mozhimen.taskk.provider.apk.utils.AppTaskUtil
import com.mozhimen.taskk.provider.basic.annors.AState
import com.mozhimen.taskk.provider.basic.annors.ATaskName
import com.mozhimen.taskk.provider.basic.annors.ATaskNodeQueueName
import com.mozhimen.taskk.provider.basic.cons.STaskFinishType
import com.mozhimen.taskk.provider.basic.db.AppTask
import com.mozhimen.taskk.provider.basic.db.AppTaskDaoManager
import com.mozhimen.taskk.provider.basic.impls.TaskException
import com.mozhimen.taskk.provider.basic.commons.ITasks2
import com.mozhimen.taskk.provider.test.databinding.ActivityMain2Binding

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class MainActivity : BaseActivityVB<ActivityMain2Binding>(), ITasks2 {
    var appTask = AppTask(
        "0",
        AState.STATE_TASK_CREATE,
        "lelejoy",
        "https://cf-lele-res.lelejoy.com/lelejoy.apk",
        true,
        "",
        true,
        "",
        R.drawable.ic_launcher_foreground,
        "lelejoy.apk",
        "com.ty.lelejoy",
        38,
        "1.8.8"
    )

    @OptIn(OApiInit_InApplication::class)
    override fun initView(savedInstanceState: Bundle?) {
        appTask = AppTaskUtil.generateAppTask_ofDb_installed_version(MainTaskManager.default(), appTask, ATaskName.TASK_INSTALL)
        UtilKLogWrapper.d(TAG, "initView: get_ofTaskId ${AppTaskDaoManager.instance.default().get_ofTaskId(appTask.id)}")
        UtilKLogWrapper.d(TAG, "initView: get_ofApkPackageName_ApkVersionCode ${AppTaskDaoManager.instance.default().get_ofApkPackageName_ApkVersionCode(appTask.apkPackageName, appTask.apkVersionCode)}")
        UtilKLogWrapper.d(
            TAG,
            "initView: get_ofTaskId_ApkPackageName_ApkVersionCode ${AppTaskDaoManager.instance.default().get_ofTaskId_ApkPackageName_ApkVersionCode(appTask.id, appTask.apkPackageName, appTask.apkVersionCode)}"
        )
        UtilKLogWrapper.d(TAG, "initView: gets_ofApkPackageName ${AppTaskDaoManager.instance.default().gets_ofApkPackageName(appTask.apkPackageName)}")
        UtilKLogWrapper.d(TAG, "initView: gets_ofApkPackageName_satisfyApkVersionCode ${AppTaskDaoManager.instance.default().gets_ofApkPackageName_satisfyApkVersionCode(appTask.apkPackageName, appTask.apkVersionCode)}")

        vb.mainTxt.text = appTask.getTaskStateStr()

        vb.mainBtn.setOnClickListener {
            requestPermissionStorage {
                if (appTask.isTaskProcess(MainTaskManager.default(), ATaskName.TASK_INSTALL) && appTask.isAnyTasking()) {//任务中->暂停
                    MainTaskManager.default().taskPause(appTask, ATaskName.TASK_INSTALL)
                } else if (appTask.isTaskProcess(MainTaskManager.default(), ATaskName.TASK_INSTALL) && appTask.isAnyTaskPause()) {//任务暂停->继续
                    MainTaskManager.default().taskStart(appTask, ATaskName.TASK_INSTALL)
                } else if (appTask.isTaskProcess(MainTaskManager.default(), ATaskName.TASK_INSTALL) && appTask.isAnyTaskSuccess()) {//任务途中成功但等待用户操作安装->继续任务
                    MainTaskManager.default().taskStart(appTask, ATaskName.TASK_INSTALL)
                } else if (appTask.isTaskSuccess(MainTaskManager.default(), ATaskName.TASK_INSTALL)) {//任务成功->下一个taskNodeQueue例如打开
                    MainTaskManager.default().taskStart(appTask, ATaskName.TASK_OPEN)
                } else if (appTask.isTaskCreateOrUpdate()) {//任务还在idle状态->开始任务
                    MainTaskManager.default().taskStart(appTask, ATaskName.TASK_INSTALL)
                }
            }
        }
        vb.mainBtn.setOnLongClickListener {
            requestPermissionStorage {
                MainTaskManager.default().taskCancel(appTask, ATaskName.TASK_INSTALL)
            }
            true
        }
        MainTaskManager.default().registerTask2Listener(this)
    }


    @OptIn(OPermission_READ_EXTERNAL_STORAGE::class, OPermission_WRITE_EXTERNAL_STORAGE::class, OPermission_MANAGE_EXTERNAL_STORAGE::class)
    @SuppressLint("MissingPermission")
    private fun requestPermissionStorage(block: I_Listener) {
        if (XXPermissionsCheckUtil.hasReadWritePermission(this)) {
            block.invoke()
        } else {
            XXPermissionsRequestUtil.requestReadWritePermission(this, onGranted = {
                block.invoke()
            }, onDenied = {

            })
        }
    }

    @OptIn(OApiInit_InApplication::class)
    override fun onDestroy() {
        MainTaskManager.default().unregisterTask2Listener(this)
        super.onDestroy()
    }

    //////////////////////////////////////////////////////////////////////

    override fun onTaskCreate(appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String, isUpdate: Boolean) {
        vb.mainTxt.text = appTask.getTaskStateStr()
        this.appTask = appTask
    }

    override fun onTaskUnavailable(appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String) {
        vb.mainTxt.text = appTask.getTaskStateStr()
        this.appTask = appTask
    }

    override fun onTaskFinish(appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String, finishType: STaskFinishType) {
        vb.mainTxt.text = appTask.getTaskStateStr()
        this.appTask = appTask
    }

    //////////////////////////////////////////////////////////////////////

    override fun onTasking(appTask: AppTask, taskNodeQueueName: String, progress: Int, currentIndex: Long, totalIndex: Long, offsetIndexPerSeconds: Long) {
        vb.mainTxt.text = appTask.getTaskStateStr()
        this.appTask = appTask
    }

    override fun onTaskPause(appTask: AppTask, taskNodeQueueName: String) {
        vb.mainTxt.text = appTask.getTaskStateStr()
        this.appTask = appTask
    }

    override fun onTaskSuccess(appTask: AppTask, taskNodeQueueName: String) {
        vb.mainTxt.text = appTask.getTaskStateStr()
        this.appTask = appTask
    }

    override fun onTaskFail(appTask: AppTask, taskNodeQueueName: String, exception: TaskException) {
        vb.mainTxt.text = appTask.getTaskStateStr()
        this.appTask = appTask
    }

    override fun onTaskCancel(appTask: AppTask, taskNodeQueueName: String) {
        vb.mainTxt.text = appTask.getTaskStateStr()
        this.appTask = appTask
    }

    //////////////////////////////////////////////////////////////////////

//    private lateinit var binding: ActivityMainBinding
//    private lateinit var fullscreenContent: TextView
//    private lateinit var fullscreenContentControls: LinearLayout
//    private val hideHandler = Handler(Looper.myLooper()!!)
//
//    private val hidePart2Runnable = Runnable {
//        // Delayed removal of status and navigation bar
//        if (Build.VERSION.SDK_INT >= 30) {
//            fullscreenContent.windowInsetsController?.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
//        } else {
//            // Note that some of these constants are new as of API 16 (Jelly Bean)
//            // and API 19 (KitKat). It is safe to use them, as they are inlined
//            // at compile-time and do nothing on earlier devices.
//            fullscreenContent.systemUiVisibility =
//                View.SYSTEM_UI_FLAG_LOW_PROFILE or
//                        View.SYSTEM_UI_FLAG_FULLSCREEN or
//                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
//                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
//                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
//                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//        }
//    }
//    private val showPart2Runnable = Runnable {
//        // Delayed display of UI elements
//        supportActionBar?.show()
//        fullscreenContentControls.visibility = View.VISIBLE
//    }
//    private var isFullscreen: Boolean = false
//
//    private val hideRunnable = Runnable { hide() }
//
//    /**
//     * Touch listener to use for in-layout UI controls to delay hiding the
//     * system UI. This is to prevent the jarring behavior of controls going away
//     * while interacting with activity UI.
//     */
//    private val delayHideTouchListener = View.OnTouchListener { view, motionEvent ->
//        when (motionEvent.action) {
//            MotionEvent.ACTION_DOWN -> if (AUTO_HIDE) {
//                delayedHide(AUTO_HIDE_DELAY_MILLIS)
//            }
//
//            MotionEvent.ACTION_UP -> view.performClick()
//            else -> {
//            }
//        }
//        false
//    }
//
//    @SuppressLint("ClickableViewAccessibility")
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        binding = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)
//
//        isFullscreen = true
//
//        // Set up the user interaction to manually show or hide the system UI.
//        fullscreenContent = binding.fullscreenContent
//        fullscreenContent.setOnClickListener { toggle() }
//
//        fullscreenContentControls = binding.fullscreenContentControls
//
//        // Upon interacting with UI controls, delay any scheduled hide()
//        // operations to prevent the jarring behavior of controls going away
//        // while interacting with the UI.
//        binding.dummyButton.setOnTouchListener(delayHideTouchListener)
//    }
//
//    override fun onPostCreate(savedInstanceState: Bundle?) {
//        super.onPostCreate(savedInstanceState)
//
//        // Trigger the initial hide() shortly after the activity has been
//        // created, to briefly hint to the user that UI controls
//        // are available.
//        delayedHide(100)
//    }
//
//    private fun toggle() {
//        if (isFullscreen) {
//            hide()
//        } else {
//            show()
//        }
//    }
//
//    private fun hide() {
//        // Hide UI first
//        supportActionBar?.hide()
//        fullscreenContentControls.visibility = View.GONE
//        isFullscreen = false
//
//        // Schedule a runnable to remove the status and navigation bar after a delay
//        hideHandler.removeCallbacks(showPart2Runnable)
//        hideHandler.postDelayed(hidePart2Runnable, UI_ANIMATION_DELAY.toLong())
//    }
//
//    private fun show() {
//        // Show the system bar
//        if (Build.VERSION.SDK_INT >= 30) {
//            fullscreenContent.windowInsetsController?.show(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
//        } else {
//            fullscreenContent.systemUiVisibility =
//                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
//                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//        }
//        isFullscreen = true
//
//        // Schedule a runnable to display UI elements after a delay
//        hideHandler.removeCallbacks(hidePart2Runnable)
//        hideHandler.postDelayed(showPart2Runnable, UI_ANIMATION_DELAY.toLong())
//    }
//
//    /**
//     * Schedules a call to hide() in [delayMillis], canceling any
//     * previously scheduled calls.
//     */
//    private fun delayedHide(delayMillis: Int) {
//        hideHandler.removeCallbacks(hideRunnable)
//        hideHandler.postDelayed(hideRunnable, delayMillis.toLong())
//    }
//
//    companion object {
//        /**
//         * Whether or not the system UI should be auto-hidden after
//         * [AUTO_HIDE_DELAY_MILLIS] milliseconds.
//         */
//        private const val AUTO_HIDE = true
//
//        /**
//         * If [AUTO_HIDE] is set, the number of milliseconds to wait after
//         * user interaction before hiding the system UI.
//         */
//        private const val AUTO_HIDE_DELAY_MILLIS = 3000
//
//        /**
//         * Some older devices needs a small delay between UI widget updates
//         * and a change of the status and navigation bar.
//         */
//        private const val UI_ANIMATION_DELAY = 300
//    }
}