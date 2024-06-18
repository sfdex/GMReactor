package com.sfdex.lib

class NativeLib {

    external fun stringFromJNI(): String

    companion object {
        init {
            System.loadLibrary("nativehook")
        }
    }
}