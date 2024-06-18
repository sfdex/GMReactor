package com.sfdex.gmr.lspmodule

import android.annotation.SuppressLint
import com.sfdex.gmr.getModelInfo
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface
import io.github.libxposed.api.annotations.AfterInvocation
import io.github.libxposed.api.annotations.BeforeInvocation
import io.github.libxposed.api.annotations.XposedHooker
import kotlin.random.Random

private lateinit var module: HyperOSModule
private const val TAG = "HyperOSModule"

class HyperOSModule(base: XposedInterface, param: XposedModuleInterface.ModuleLoadedParam) :
    XposedModule(base, param) {

    init {
        log("$TAG: at " + param.processName)
        module = this
    }

    @XposedHooker
    class MyHooker() : XposedInterface.Hooker {
        companion object {
            @JvmStatic
            @BeforeInvocation
            fun beforeInvocation(callback: XposedInterface.BeforeHookCallback): MyHooker {
                if (callback.args[0] == "ro.mi.os.version.code") {
                    if (getModelInfo().brand.contains("xiaomi", true)) {
                        module.log("$TAG: yes code")
                        callback.returnAndSkip("15")
                    } else {
                        module.log("$TAG: No code")
                        callback.returnAndSkip("")
                    }
                }

                if (callback.args[0] == "ro.mi.os.version.name") {
                    if (getModelInfo().brand.contains("xiaomi", true)) {
                        module.log("$TAG: Yes name")
                        callback.returnAndSkip("HyperOS 1.0.5")
                    } else {
                        module.log("$TAG: No name")
                        callback.returnAndSkip("")
                    }
                }

                return MyHooker()
            }

            @JvmStatic
            @AfterInvocation
            fun afterInvocation(callback: XposedInterface.AfterHookCallback, context: MyHooker) {

            }
        }
    }

    @SuppressLint("DiscouragedPrivateApi")
    override fun onPackageLoaded(param: XposedModuleInterface.PackageLoadedParam) {
        super.onPackageLoaded(param)
        log("$TAG onPackageLoaded: " + param.packageName)

        if (!param.isFirstPackage) return

        val clz = Class.forName("android.os.SystemProperties")
        val method = clz.getMethod("get", String::class.java, String::class.java)

        hook(method, MyHooker::class.java)
    }
}