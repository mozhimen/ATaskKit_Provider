package com.mozhimen.netk.app.install.commons

import java.io.File

/**
 * @ClassName INetKAppInstallProvider
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2024/6/21
 * @Version 1.0
 */
interface INetKAppInstallProvider {
    fun getSupportFileExtensions(): List<String>
    fun install(file: File)
}