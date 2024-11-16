package com.mozhimen.taskk.provider.basic.cons

import com.mozhimen.taskk.provider.basic.annors.ATaskName
import com.mozhimen.taskk.provider.basic.annors.ATaskNodeQueueName
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskDelete

/**
 * @ClassName STaskNode
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/11/15
 * @Version 1.0
 */
sealed class STaskNode(@ATaskName val taskName: String) {
    object TaskNodeDownload : STaskNode(ATaskName.TASK_DOWNLOAD)
    object TaskNodeVerify : STaskNode(ATaskName.TASK_VERIFY)
    object TaskNodeUnzip : STaskNode(ATaskName.TASK_UNZIP)
    object TaskNodeInstall : STaskNode(ATaskName.TASK_INSTALL)
    object TaskNodeOpen : STaskNode(ATaskName.TASK_OPEN)
    object TaskNodeClose : STaskNode(ATaskName.TASK_CLOSE)
    object TaskNodeUninstall : STaskNode(ATaskName.TASK_UNINSTALL)
    object TaskNodeDelete : STaskNode(ATaskName.TASK_DELETE)
    object TaskNodeRestart : STaskNode(ATaskNodeQueueName.TASK_RESTART)
    object TaskNodeBlocker : STaskNode(ATaskNodeQueueName.TASK_BLOCKER)
}