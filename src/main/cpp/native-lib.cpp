#include <jni.h>
#include <string>
#include "stdlib.h"
#include "string.h"
#include <android/log.h>

#define LOG_TAG       "WM LOG"

extern "C" {

jint JNICALL
Java_com_wavematters_muwave_MainActivity_muWave(
        JNIEnv *env, jobject /* this */, jstring command ) {

    char  *pcmd;
    char  *argv[100];
    int    argc;
    int i = 0;
    jboolean isCopy;

    const char *cmd = (env)->GetStringUTFChars( command, &isCopy );
    pcmd = (char *)malloc( strlen(cmd) + 1 );
    strcpy( pcmd, cmd );
    //
    // TODO: Instead of calling libray function wm(), issue command
    //
    for( ; i < 100; i++ ) {
        if( !(argv[i] = strtok_r( pcmd, " ", &pcmd ))) break;
    }
    free( pcmd );
    argc = i;
//  return wm( argc, argv );
    return(0);
}

}
