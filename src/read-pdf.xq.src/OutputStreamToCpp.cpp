#include <jni.h>
#include <iostream>
#include "JavaVMSingleton.h"


#ifndef _Included_org_zorbaxquery_modules_readPdf_OutputStreamToCpp
#define _Included_org_zorbaxquery_modules_readPdf_OutputStreamToCpp

#ifdef WIN32
#  define DLL_EXPORT __declspec(dllexport)
#else
#  define DLL_EXPORT __attribute__ ((visibility("default")))
#endif

#ifdef __cplusplus
extern "C" {
#endif


/*
 * Class:     OutputStreamToCpp
 * Method:    nativeWrite
 * Signature: ([BIIL)I
 * Reads from istream stored inside cppObjectPointer into ba of maximum jsize.
 * Returns number of bytes/chars read or -1 if eof or -2 if fail or bad stream.
 */
JNIEXPORT jint JNICALL DLL_EXPORT
Java_org_zorbaxquery_modules_readPdf_OutputStreamToCpp_nativeWrite
  (JNIEnv * env, jobject obj, jbyteArray ba, jint off, jint len, jlong p)
{
    std::ostream *os = (std::ostream*)p;

    char * buf = new char[len];
    env->GetByteArrayRegion(ba, off, len, (jbyte *)buf);

    os->write(buf, len);
    delete buf;

    if ( os->bad() )
        return -2;

    jint actualWriteSize = len;
    return actualWriteSize;
}

#ifdef __cplusplus
}
#endif
#endif

