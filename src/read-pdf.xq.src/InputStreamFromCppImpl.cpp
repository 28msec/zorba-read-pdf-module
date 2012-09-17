#include <jni.h>
#include <iostream>





#ifndef _Included_org_zorbaxquery_modules_readPdf_InputStreamFromCpp
#define _Included_org_zorbaxquery_modules_readPdf_InputStreamFromCpp

#ifdef WIN32
#  define DLL_EXPORT __declspec(dllexport)
#else
#  define DLL_EXPORT __attribute__ ((visibility("default")))
#endif

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jint JNICALL DLL_EXPORT
Java_org_zorbaxquery_modules_readPdf_InputStreamFromCpp_nativeRead
    (JNIEnv * env, jobject jobj, jbyteArray ba, jint jsize, jlong p)
{
        //std::cout << "c:  nativeRead  available space:" << jsize << std::endl; std::cout.flush();

        std::istream *is = (std::istream*)p;

        if ( is->eof() )
        {
            //std::cout << "c:    eof"  << std::endl; std::cout.flush();
            return -1;
        }

        if ( is->fail() || is->bad() )
        {
            //std::cout << "c:    FAIL"  << std::endl; std::cout.flush();
            return -2;
        }

        char * buf = new char[jsize];

        is->read(buf, jsize);
        jint actualReadSize = is->gcount();

        env->SetByteArrayRegion(ba, 0, actualReadSize, (jbyte *)buf);
        delete buf;

        if ( actualReadSize == 0 && is->eof() )
        {
            //std::cout << "c:    eof"  << std::endl; std::cout.flush();
            return -1;
        }

        //std::cout << "c:   return " << actualReadSize << std::endl; std::cout.flush();
        return actualReadSize;
}

#ifdef __cplusplus
}
#endif
#endif

