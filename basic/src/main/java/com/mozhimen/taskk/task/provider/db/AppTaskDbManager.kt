package com.mozhimen.taskk.task.provider.db

import android.content.Context
import androidx.annotation.UiThread
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mozhimen.basick.lintk.optins.OApiInit_InApplication
import com.mozhimen.taskk.task.provider.cons.CState
import com.mozhimen.netk.app.task.db.AppTaskDao
import com.mozhimen.netk.app.task.db.AppTaskDaoManager

/**
 * @ClassName DatabaseManager
 * @Description TODO
 * @Author Mozhimen
 * @Date 2023/11/7 16:01
 * @Version 1.0
 */
@OApiInit_InApplication
object AppTaskDbManager {
    private lateinit var _appTaskDb: AppTaskDb

    lateinit var appTaskDao: AppTaskDao
        private set

    @UiThread
    fun init(context: Context) {
        _appTaskDb = Room.databaseBuilder(context.applicationContext, AppTaskDb::class.java, "netk_app_task_db")
//            .fallbackToDestructiveMigration()//使用该方法会在数据库升级异常时重建数据库，但是所有数据会丢失
            .addMigrations(AppTaskMigrations.MIGRATION_1_2, AppTaskMigrations.MIGRATION_2_3, AppTaskMigrations.MIGRATION_3_4)
            .build()
        appTaskDao = _appTaskDb.appTaskDao()
        AppTaskDaoManager.init()
    }
}