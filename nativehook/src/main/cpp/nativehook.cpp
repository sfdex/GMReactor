#include <jni.h>
#include <string>
#include "log.h"
#include "bytehook.h"
#include "nativehook.h"
#include "sysprop.h"

extern "C" JNIEXPORT jstring JNICALL
Java_com_sfdex_lib_NativeLib_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

void on_library_loaded(const char *name, void *handle) {
    LOGD("on_library_loaded: %s", name);
}

extern "C" [[gnu::visibility("default")]] [[gnu::used]]
NativeOnModuleLoaded native_init(const NativeAPIEntries *entries) {
    SysHook::init();
    SysHook::hook_read_callback();
    return on_library_loaded;
}