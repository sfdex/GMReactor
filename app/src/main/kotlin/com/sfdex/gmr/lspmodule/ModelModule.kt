package com.sfdex.gmr.lspmodule

import android.annotation.SuppressLint
import com.sfdex.gmr.getModelInfo
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedInterface.AfterHookCallback
import io.github.libxposed.api.XposedInterface.BeforeHookCallback
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface
import io.github.libxposed.api.XposedModuleInterface.ModuleLoadedParam
import io.github.libxposed.api.XposedModuleInterface.PackageLoadedParam
import io.github.libxposed.api.annotations.AfterInvocation
import io.github.libxposed.api.annotations.BeforeInvocation
import io.github.libxposed.api.annotations.XposedHooker
import kotlin.random.Random

private lateinit var module: ModelModule
private const val TAG = "ModelModule"

class ModelModule(base: XposedInterface, param: ModuleLoadedParam) : XposedModule(base, param) {

    init {
        log("$TAG at " + param.processName)
        module = this
    }

    @XposedHooker
    class MyHooker() : XposedInterface.Hooker {
        companion object {
            @JvmStatic
            @BeforeInvocation
            fun beforeInvocation(callback: BeforeHookCallback): MyHooker {
                setSystemProperties()
                val args = callback.args.filterIsInstance<String>()
                module.log("$TAG: beforeInvocation $args, object(${callback.thisObject})")
                return MyHooker()
            }

            @JvmStatic
            @AfterInvocation
            fun afterInvocation(callback: AfterHookCallback, context: MyHooker) {
                setSystemProperties()
                val args = callback.args.filterIsInstance<String>()
                val result = callback.result ?: ""
                module.log("$TAG: afterInvocation: result: $result, $args")
            }
        }
    }

    @SuppressLint("DiscouragedPrivateApi")
    override fun onPackageLoaded(param: PackageLoadedParam) {
        super.onPackageLoaded(param)
        setSystemProperties()
        log("$TAG onPackageLoaded: " + param.packageName)

        val clz = Class.forName("android.os.SystemProperties")
        val method = clz.getMethod("get", String::class.java, String::class.java)

        hook(method, MyHooker::class.java)
    }
}

private fun setSystemProperties() {
    val model = getModelInfo()
    val clz = Class.forName("android.os.Build")
    setField(clz, "MANUFACTURER", model.manufacturer)
    setField(clz, "BRAND", model.brand)
    setField(clz, "MODEL", model.model)
    setField(clz, "DEVICE", model.device)
    setField(clz, "BOARD", model.board)
    setField(clz, "PRODUCT", model.product)
}

private fun setField(clz: Class<*>, name: String, value: String) {
    val field = clz.getDeclaredField(name)
    field.isAccessible = true
    field.set(null, value)
}