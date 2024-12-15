package com.mozhimen.taskk.provider.core

import com.mozhimen.taskk.provider.basic.annors.ATaskName

/**
 * @ClassName Test
 * @Description TODO
 * @Author Mozhimen / Kolin Zhao
 * @Date 2024/9/22 22:23
 * @Version 1.0
 */
fun main() {
    val taskProviderApk = TaskProviderApk()
    val taskProviderMod = TaskProviderMod()
    val a : Map<String, Map<String, List<String>>> = listOf(taskProviderApk, taskProviderMod).map { provider -> (provider.getSupportFileExtensions().associateWith { provider.getTaskNodeQueues() }) }
        .fold(mutableMapOf()) { acc, nex -> acc += nex;acc }
    println(a)
}

class TaskProviderApk : ATaskProvider() {
    override fun getSupportFileExtensions(): List<String> {
        return listOf("apk", "xapk")
    }

    override fun getTaskNodeQueues(): Map<String, List<String>> {
        return mapOf(
            ATaskName.TASK_INSTALL to listOf(ATaskName.TASK_DOWNLOAD, ATaskName.TASK_VERIFY, ATaskName.TASK_UNZIP, ATaskName.TASK_INSTALL),
            ATaskName.TASK_OPEN to listOf(ATaskName.TASK_OPEN),
            ATaskName.TASK_UNINSTALL to listOf(ATaskName.TASK_UNINSTALL, ATaskName.TASK_DELETE)
        )
    }

}

class TaskProviderMod : ATaskProvider() {
    override fun getSupportFileExtensions(): List<String> {
        return listOf("zip")
    }

    override fun getTaskNodeQueues(): Map<String, List<String>> {
        return mapOf(
            ATaskName.TASK_DOWNLOAD to listOf(ATaskName.TASK_DOWNLOAD),
            ATaskName.TASK_INSTALL to listOf(ATaskName.TASK_INSTALL),
            ATaskName.TASK_UNINSTALL to listOf(ATaskName.TASK_UNINSTALL),
            ATaskName.TASK_DELETE to listOf(ATaskName.TASK_DELETE)
        )
    }
}

abstract class ATaskProvider {
    abstract fun getSupportFileExtensions(): List<String>
    abstract fun getTaskNodeQueues(): Map<String, List<String>>
}
