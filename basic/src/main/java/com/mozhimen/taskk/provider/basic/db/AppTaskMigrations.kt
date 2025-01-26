package com.mozhimen.taskk.provider.basic.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mozhimen.taskk.provider.basic.annors.AState

/**
 * @ClassName AppTaskMigrations
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/19
 * @Version 1.0
 */
object AppTaskMigrations {
    /**
     * 用户数据库从version 1 升级到 version 2
     */
    internal val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE netk_app_task ADD COLUMN task_state_init INTEGER NOT NULL DEFAULT ${AState.STATE_TASK_CREATE}")
        }
    }

    /**
     * 删除apk_is_installed字段
     */
    internal val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Step 1: 创建新的表，不包含 apk_is_installed 字段
            db.execSQL(
                """
            CREATE TABLE IF NOT EXISTS `new_netk_app_task` (
                `task_id` TEXT NOT NULL,
                `task_state` INTEGER NOT NULL,
                `task_state_init` INTEGER NOT NULL,
                `task_update_time` INTEGER NOT NULL,
                `downloadId` INTEGER NOT NULL,
                `download_url_current` TEXT NOT NULL,
                `download_url` TEXT NOT NULL,
                `download_url_outside` TEXT NOT NULL,
                `download_progress` INTEGER NOT NULL,
                `download_file_size` INTEGER NOT NULL,
                `apk_file_size` INTEGER NOT NULL,
                `apk_verify_need` INTEGER NOT NULL,
                `apk_unzip_need` INTEGER NOT NULL,
                `apk_file_md5` TEXT NOT NULL,
                `apk_package_name` TEXT NOT NULL,
                `apk_name` TEXT NOT NULL,
                `apk_version_code` INTEGER NOT NULL,
                `apk_version_name` TEXT NOT NULL,
                `apk_icon_url` TEXT NOT NULL,
                `apk_icon_Id` INTEGER NOT NULL,
                `apk_file_name` TEXT NOT NULL,
                `apk_path_name` TEXT NOT NULL,
                PRIMARY KEY(`task_id`)
            )
        """
            )

            // Step 2: 将旧表的数据复制到新表中
            db.execSQL(
                """
            INSERT INTO `new_netk_app_task` (
                `task_id`, `task_state`, `task_state_init`, `task_update_time`, `downloadId`,
                `download_url`, `download_url_outside`, `download_url_current`, `download_progress`, `download_file_size`,
                `apk_verify_need`, `apk_unzip_need`, `apk_file_size`, `apk_file_md5`, `apk_package_name`,
                `apk_name`, `apk_version_code`, `apk_version_name`, `apk_icon_url`, `apk_icon_Id`, `apk_file_name`, `apk_path_name`
            ) SELECT 
                `task_id`, `task_state`, `task_state_init`, `task_update_time`, `downloadId`,
                `download_url`, `download_url_outside`, `download_url_current`, `download_progress`, `download_file_size`,
                `apk_verify_need`, `apk_unzip_need`, `apk_file_size`, `apk_file_md5`, `apk_package_name`,
                `apk_name`, `apk_version_code`, `apk_version_name`, `apk_icon_url`, `apk_icon_Id`, `apk_file_name`, `apk_path_name`
            FROM `netk_app_task`
        """
            )

            // Step 3: 删除旧表
            db.execSQL("DROP TABLE `netk_app_task`")

            // Step 4: 将新表重命名为旧表的名称
            db.execSQL("ALTER TABLE `new_netk_app_task` RENAME TO `netk_app_task`")
        }
    }

    /**
     * 新增文件后缀字段file_ext
     */
    internal val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE netk_app_task ADD COLUMN file_ext TEXT NOT NULL DEFAULT 'apk'")
            db.execSQL("ALTER TABLE netk_app_task ADD COLUMN task_download_file_speed INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE netk_app_task ADD COLUMN task_unzip_file_path TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE netk_app_task ADD COLUMN task_name TEXT NOT NULL DEFAULT ''")
        }
    }

    /**
     * 新增通道字段
     */
    internal val MIGRATION_4_5 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE netk_app_task ADD COLUMN taskchannel TEXT NOT NULL DEFAULT ''")
        }
    }
}