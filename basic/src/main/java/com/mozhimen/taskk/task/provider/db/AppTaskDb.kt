package com.mozhimen.taskk.task.provider.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.viewpager2.widget.ViewPager2.ScrollState
import com.mozhimen.basick.utilk.android.app.UtilKApplicationWrapper
import com.mozhimen.basick.utilk.android.content.UtilKContextWrapper

/**
 * @ClassName AppDownloadDB
 * @Description TODO
 * @Author Mozhimen
 * @Date 2023/11/7 16:01
 * @Version 1.0
 */
@Database(entities = [AppTask::class], version = 4, exportSchema = false)
abstract class AppTaskDb : RoomDatabase() {
    abstract fun appTaskDao(): AppTaskDao

    companion object {
        @set:Synchronized
        @get:Synchronized
        private lateinit var _appTaskDb: AppTaskDb

        @JvmStatic
        fun init(context: Context) {
            _appTaskDb = Room.databaseBuilder(context, AppTaskDb::class.java, "netk_app_task_db")
//            .fallbackToDestructiveMigration()//使用该方法会在数据库升级异常时重建数据库，但是所有数据会丢失
                .addMigrations(AppTaskMigrations.MIGRATION_1_2, AppTaskMigrations.MIGRATION_2_3, AppTaskMigrations.MIGRATION_3_4)
                .build()
            AppTaskDaoManager.init()
        }

        @JvmStatic
        fun get(): AppTaskDb =
            _appTaskDb

        @JvmStatic
        fun getAppTaskDao(): AppTaskDao =
            _appTaskDb.appTaskDao()
    }
}