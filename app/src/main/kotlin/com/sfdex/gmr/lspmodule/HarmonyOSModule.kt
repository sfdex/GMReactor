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

private lateinit var module: HarmonyOSModule
private const val TAG = "HarmonyOSModule"

class HarmonyOSModule(base: XposedInterface, param: XposedModuleInterface.ModuleLoadedParam) :
    XposedModule(base, param) {

    init {
        log("$TAG at " + param.processName)
        module = this
    }

    @XposedHooker
    class MyHooker() : XposedInterface.Hooker {
        companion object {
            @JvmStatic
            @BeforeInvocation
            fun beforeInvocation(callback: XposedInterface.BeforeHookCallback): MyHooker {
                if (!getModelInfo().brand.contains("huawei", true)) {
                    module.log("$TAG: NO!")
                    callback.throwAndSkip(NoSuchMethodException())
                }
                return MyHooker()
            }

            @JvmStatic
            @AfterInvocation
            fun afterInvocation(callback: XposedInterface.AfterHookCallback, context: MyHooker) {
                callback.result?.let {
                    if (it is String) {
                        module.log("$TAG: afterInvocation: result: $it, member: ${callback.member}")
                    }
                }
            }
        }
    }

    @SuppressLint("DiscouragedPrivateApi")
    override fun onPackageLoaded(param: XposedModuleInterface.PackageLoadedParam) {
        super.onPackageLoaded(param)
        log("$TAG: onPackageLoaded: " + param.packageName)

        if (!param.isFirstPackage) return

        val clz = Class.forName("com.huawei.system.BuildEx")
        val method = clz.getDeclaredMethod("getOsBrand")

        hook(method, MyHooker::class.java)
    }
}