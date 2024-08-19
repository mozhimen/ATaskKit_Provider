package com.mozhimen.netk.app.tasks.install.impls

import com.mozhimen.basick.lintk.optins.permission.OPermission_REQUEST_INSTALL_PACKAGES
import com.mozhimen.basick.utilk.wrapper.UtilKAppInstall
import com.mozhimen.netk.app.basic.commons.INetKAppInstallProvider
import java.io.File

/**
 * @ClassName NetKAppInstallProviderDefault
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2024/6/21
 * @Version 1.0
 */
@OPermission_REQUEST_INSTALL_PACKAGES
class NetKAppInstallProviderDefault : INetKAppInstallProvider {
    override fun getSupportFileExtensions(): List<String> {
        return listOf("apk")
    }

    override fun install(file: File) {
        UtilKAppInstall.install_ofView(file)
    }
}