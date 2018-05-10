#include <jni.h>
#include <string>
#include "stdlib.h"
#include "string.h"
#include <android/log.h>

#define LOG_TAG       "WM"

extern "C" {

int wm(int, char **);

jstring JNICALL Java_com_wavematters_muwave_MainActivity_hexTime(
                          JNIEnv *env, jobject /* this */  )
{
    //
    // format system time to a hexidecimal string
    //
    time_t    rawtime;
    time_t    seconds = time( &rawtime );
    char      str[16];
    sprintf( str, "%0lX", seconds );  // secs since 1970/01/01 in hexadecimal
    return env->NewStringUTF(str);
}

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
    for( ; i < 100; i++ ) {
        if( !(argv[i] = strtok_r( pcmd, " ", &pcmd ))) break;
    }
    free( pcmd );
    argc = i;
    return( wm( argc, argv ) );
}

}
