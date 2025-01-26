package com.mozhimen.taskk.provider.basic.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

/**
 * @ClassName AppTaskDao
 * @Description TODO
 * @Author Mozhimen
 * @Date 2023/11/7 16:03
 * @Version 1.0
 */
@Dao
interface AppTaskDao {
    @Query("select * from netk_app_task where task_channel = :channel order by task_update_time desc")
    fun gets_ofPagingSource(channel: String): PagingSource<Int, AppTask>

    @Query("select * from netk_app_task where task_state > 9 and task_channel = :channel order by task_update_time desc")
    fun gets_process_ofPagingSource(channel: String): PagingSource<Int, AppTask>

    @Query("select * from netk_app_task where task_channel = :channel")
    fun gets_ofAll(channel: String): List<AppTask>

    @Query("select * from netk_app_task where task_id = :taskId and task_channel = :channel")
    fun gets_ofTaskId(taskId: String, channel: String): List<AppTask>

    @Query("select * from netk_app_task where apk_package_name = :packageName and task_channel = :channel order by task_update_time desc")
    fun gets_ofPackageName(packageName: String, channel: String): List<AppTask>

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