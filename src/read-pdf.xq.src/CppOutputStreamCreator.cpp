#include <jni.h>
#include <iostream>
#include <sstream>
#include "JavaVMSingleton.h"
#include "CppOutputStreamCreator.h"


namespace zorba
{
namespace readpdf
{

class JavaException {
};

#define CHECK_EXCEPTION(env)  if ((lException = env->ExceptionOccurred())) throw JavaException()


CppOutputStreamCreator* CppOutputStreamCreator::newInstance(JNIEnv * env)
{
    jthrowable lException = 0;
    jclass myClass;
    jmethodID myMethod;

    myClass = env->FindClass("org/zorbaxquery/modules/readPdf/CppOutputStreamCreator");
    CHECK_EXCEPTION(env);
    //std::cout << "cpp:  got class CppOutputStreamCreator" << std::endl; std::cout.flush();

    myMethod = env->GetStaticMethodID(myClass, "newInstance", "(J)Lorg/zorbaxquery/modules/readPdf/CppOutputStreamCreator;");
    CHECK_EXCEPTION(env);
    //std::cout << "cpp:  got static method newInstance()" << std::endl; std::cout.flush();

    CppOutputStreamCreator* osc = new CppOutputStreamCreator();

    jlong p = (jlong) osc;
    //std::cout << "cpp:  p: " << p << std::endl; std::cout.flush();

    //std::cout << "cpp:  call static method newInstance()" << std::endl; std::cout.flush();
    jobject jo = env->CallStaticObjectMethod(myClass, myMethod, p);
    CHECK_EXCEPTION(env);

    osc->setJavaObject(env->NewGlobalRef(jo));
    return osc;
}

std::ostream* CppOutputStreamCreator::newOutputStream()
{
    std::stringstream* ss = new std::stringstream(std::stringstream::in | std::stringstream::out);
    _ssVector.push_back(ss);
    return ss;
}

void CppOutputStreamCreator::close(JNIEnv * env)
{
/*  Avoid calling java close().

    jthrowable lException = 0;
    jclass myClass;
    jmethodID myMethod;

    myClass = env->FindClass("org/zorbaxquery/modules/readPdf/CppOutputStreamCreator");
    CHECK_EXCEPTION(env);
    //std::cout << "cpp:  got class   CppOutputStreamCreator" << std::endl; std::cout.flush();

    myMethod = env->GetMethodID(myClass, "close", "()V");
    CHECK_EXCEPTION(env);
    //std::cout << "cpp:  got close()" << std::endl; std::cout.flush();

    env->CallVoidMethod(_obj, myMethod);
    CHECK_EXCEPTION(env);
    //std::cout << "cpp:  call close()" << std::endl; std::cout.flush();
*/
    env->DeleteGlobalRef(_obj);
    _obj = NULL;
    //std::cout << "cpp:  call DeleteGlobalRef()" << std::endl; std::cout.flush();
}

}}; // namespace zorba, readpdf

#ifndef _Included_org_zorbaxquery_modules_readPdf_CppOutputStreamCreator
#define _Included_org_zorbaxquery_modules_readPdf_CppOutputStreamCreator

#ifdef WIN32
#  define DLL_EXPORT __declspec(dllexport)
#else
#  define DLL_EXPORT __attribute__ ((visibility("default")))
#endif

#ifdef __cplusplus
extern "C" {
#endif


/*
 * Class:     CppOutputStreamCreator
 * Method:    nativeCreateNewOutputStream
 * Signature: (L)L
 * Reads from istream stored inside cppObjectPointer into ba of maximum jsize.
 * Returns number of bytes/chars read or -1 if eof or -2 if fail or bad stream.
 */
JNIEXPORT jlong JNICALL DLL_EXPORT
Java_org_zorbaxquery_modules_readPdf_CppOutputStreamCreator_nativeCreateNewOutputStream
    (JNIEnv * env, jobject jobj, jlong p)
{
    zorba::readpdf::CppOutputStreamCreator *osc = (zorba::readpdf::CppOutputStreamCreator*)p;
    std::ostream *os = osc->newOutputStream();
    //std::cout << "c:  nativeCreateNewOutputStream() new os: " << (jlong)os << std::endl; std::cout.flush();
    return (jlong)os;
}

#ifdef __cplusplus
}
#endif
#endif  // _Included_org_zorbaxquery_modules_readPdf_CppOutputStreamCreator

