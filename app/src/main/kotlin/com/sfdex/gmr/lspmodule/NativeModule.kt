package com.sfdex.gmr.lspmodule

import android.annotation.SuppressLint
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface

private const val TAG = "GmrNativeModule"
private lateinit var module: NativeModule

class NativeModule(base: XposedInterface, param: XposedModuleInterface.ModuleLoadedParam) :
    XposedModule(base, param) {

    init {
        log("$TAG at " + param.processName)
        module = this
    }

    @SuppressLint("DiscouragedPrivateApi")
    override fun onPackageLoaded(param: XposedModuleInterface.PackageLoadedParam) {
        super.onPackageLoaded(param)
        log("$TAG: onPackageLoaded: " + param.packageName)

        try {
            System.loadLibrary("nativehook")
            log("$TAG: loadLibrary(\"nativehook\") success")
        } catch (e: Exception) {
            e.printStackTrace()
            log("$TAG: ${e.stackTraceToString()}")
        }
    }
}