package com.mozhimen.taskk.provider.basic.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.mozhimen.kotlin.utilk.BuildConfig
import com.mozhimen.kotlin.utilk.android.app.UtilKApplicationWrapper

/**
 * @ClassName AppDownloadDB
 * @Description TODO
 * @Author Mozhimen
 * @Date 2023/11/7 16:01
 * @Version 1.0
 */
@Database(entities = [AppTask::class], version = 6, exportSchema = false)
abstract class AppTaskDb : RoomDatabase() {
    abstract fun appTaskDao(): AppTaskDao

    companion object {
        private val _appTaskDb: AppTaskDb by lazy {
            Room.databaseBuilder(UtilKApplicationWrapper.instance.get(), AppTaskDb::class.java, "netk_app_task_db")
                .fallbackToDestructiveMigration()//使用该方法会在数据库升级异常时重建数据库，但是所有数据会丢失
                .allowMainThreadQueries()
                .addMigrations(
                    AppTaskMigrations.MIGRATION_1_2,
                    AppTaskMigrations.MIGRATION_2_3,
                    AppTaskMigrations.MIGRATION_3_4,
                    AppTaskMigrations.MIGRATION_4_5,
                    AppTaskMigrations.MIGRATION_5_6
                )
                .build()
        }

        @JvmStatic
        fun get(): AppTaskDb =
            _appTaskDb

        @JvmStatic
        fun getAppTaskDao(): AppTaskDao =
            _appTaskDb.appTaskDao()
    }
}