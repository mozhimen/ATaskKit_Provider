package com.mozhimen.taskk.task.provider.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mozhimen.netk.app.task.db.AppTaskDao

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
}