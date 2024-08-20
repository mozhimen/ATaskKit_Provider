package com.mozhimen.netk.app.task.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.mozhimen.taskk.task.provider.db.AppTask

/**
 * @ClassName AppTaskDao
 * @Description TODO
 * @Author Mozhimen
 * @Date 2023/11/7 16:03
 * @Version 1.0
 */
@Dao
interface AppTaskDao {
    @Query("select * from netk_app_task")
    fun getAll(): List<AppTask>

    @Query("select * from netk_app_task where task_id = :taskId")
    fun get_ofTaskId(taskId: String): List<AppTask>

    @Query("select * from netk_app_task where apk_package_name = :packageName order by task_update_time desc")
    fun get_ofPackageName(packageName: String): List<AppTask>

//    @Query("select * from app_download_task where apk_is_installed = 0")
//    fun getAllDownloading(): List<AppTask>

    //////////////////////////////////////////////////////////

    @Insert
    fun addAll(vararg appTask: AppTask)

    @Update
    fun update(appTask: AppTask)

    @Delete
    fun delete(appTask: AppTask)
}