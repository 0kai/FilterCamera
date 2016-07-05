#include <jni.h>
#include <Android/log.h>
#include <string>

extern "C"
jstring
Java_z0kai_filtercamera_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    __android_log_write(ANDROID_LOG_ERROR, "TAG", hello.c_str());
    return env->NewStringUTF(hello.c_str());
}
