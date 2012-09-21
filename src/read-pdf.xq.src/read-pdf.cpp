/*
 * Copyright 2006-2008 The FLWOR Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
#include <sstream>
#include <iostream>
#include <cstdlib>
#include <list>

#include <zorba/base64.h>
#include <zorba/base64_stream.h>
#include <zorba/empty_sequence.h>
#include <zorba/diagnostic_list.h>
#include <zorba/function.h>
#include <zorba/external_module.h>
#include <zorba/user_exception.h>
#include <zorba/file.h>
#include <zorba/item_factory.h>
#include <zorba/serializer.h>
#include <zorba/singleton_item_sequence.h>
#include <zorba/vector_item_sequence.h>
#include <zorba/zorba.h>

#include "JavaVMSingleton.h"
#include "CppOutputStreamCreator.h"


#define READPDF_MODULE_NAMESPACE "http://www.zorba-xquery.com/modules/read-pdf"
#define READPDF_OPTIONS_NAMESPACE "http://www.zorba-xquery.com/modules/read-pdf/read-pdf-options"



namespace zorba
{
namespace readpdf
{

class ReadPdfModule;
class ExtractTextFunction;
class RenderToImagesFunction;
class RPOptions;


class JavaException {
};

#define CHECK_EXCEPTION(env)  if ((lException = env->ExceptionOccurred())) throw JavaException()


class ExtractTextFunction : public ContextualExternalFunction
{
  private:
    const ExternalModule* theModule;
    ItemFactory* theFactory;
    XmlDataManager* theDataManager;

  public:
    ExtractTextFunction(const ExternalModule* aModule) :
      theModule(aModule),
      theFactory(Zorba::getInstance(0)->getItemFactory()),
      theDataManager(Zorba::getInstance(0)->getXmlDataManager())
    {}

    ~ExtractTextFunction()
    {}

    virtual String getURI() const
    { return theModule->getURI(); }

    virtual String getLocalName() const
    { return "extractText-internal"; }

    virtual ItemSequence_t
      evaluate(const ExternalFunction::Arguments_t& args,
               const zorba::StaticContext*,
               const zorba::DynamicContext*) const;
};

class RenderToImagesFunction : public ContextualExternalFunction
{
  private:
    const ExternalModule* theModule;
    ItemFactory* theFactory;
    XmlDataManager* theDataManager;

  public:
    RenderToImagesFunction(const ExternalModule* aModule) :
      theModule(aModule),
      theFactory(Zorba::getInstance(0)->getItemFactory()),
            theDataManager(Zorba::getInstance(0)->getXmlDataManager())
    {}

    ~RenderToImagesFunction()
    {}

  public:
    virtual String getURI() const
    { return theModule->getURI(); }

    virtual String getLocalName() const
    { return "renderToImages-internal"; }

    virtual ItemSequence_t
      evaluate(const ExternalFunction::Arguments_t& args,
               const zorba::StaticContext*,
               const zorba::DynamicContext*) const;
};


class ReadPdfModule : public ExternalModule
{
  private:
    ExternalFunction* extractText;
    ExternalFunction* renderToImages;

  public:
    ReadPdfModule() :
        extractText(new ExtractTextFunction(this)),
        renderToImages(new RenderToImagesFunction(this))
    {}

    ~ReadPdfModule()
    {
        delete extractText;
        delete renderToImages;
    }

    virtual String getURI() const
    { return READPDF_MODULE_NAMESPACE; }

    virtual ExternalFunction* getExternalFunction(const String& localName);

    virtual void destroy()
    {
      delete this;
    }
};


class RPOptions
{
public:
  typedef enum
  {
    TEXT_SIMPLE = 1,
    TEXT_HTML = 2
  } textKind_t;

  typedef enum
  {
    IMAGE_JPG = 1,
    IMAGE_PNG = 2
  } imageKind_t;


private:
  String thePassword;

  int theTextKind;
  int theImageKind;
  int32_t theStartPage;
  int32_t theEndPage;

  bool theIgnoreCorruptObjects;
  bool theIgnoreBeads;

  String theStartPageSeparator;
  String theEndPageSeparator;

public:
  RPOptions() : thePassword(""), theTextKind(RPOptions::TEXT_HTML),
    theImageKind(RPOptions::IMAGE_JPG),
    theStartPage(1), theEndPage(0x7FFFFFFF),
    theIgnoreCorruptObjects(false), theIgnoreBeads(false),
    theStartPageSeparator(""), theEndPageSeparator("")
  {}

  void parseExtractOptions(Item optionsNode, ItemFactory *itemFactory);
  void parseRenderOptions(Item optionsNode, ItemFactory *itemFactory);

  String getPassword()
  {
    return thePassword;
  }

  int getTextKind()
  {
    return theTextKind;
  }

  int getImageKind()
  {
    return theImageKind;
  }

  int getStartPage()
  {
    return theStartPage;
  }

  int getEndPage()
  {
    return theEndPage;
  }

  bool isIgnoreCorruptObjects()
  {
    return theIgnoreCorruptObjects;
  }

  bool isIgnoreBeads()
  {
    return theIgnoreBeads;
  }

  String getStartPageSeparator()
  {
    return theStartPageSeparator;
  }

  String getEndPageSeparator()
  {
    return theEndPageSeparator;
  }
};


ExternalFunction* ReadPdfModule::getExternalFunction(const String& localName)
{
  if (localName == "extractText-internal")
  {
      return extractText;
  }
  else if (localName == "renderToImages-internal")
  {
      return renderToImages;
  }

  return 0;
}


std::string extractText(const ExternalFunction::Arguments_t& args,
                        ItemFactory *itemFactory,
                        JNIEnv * env, jlong pdfInputStreamPointer)
{
    // Local variables
    jthrowable lException = 0;
    jclass myClass;
    jmethodID myMethod;

    // read input parm: $options
    Item optionsItem;
    Iterator_t arg1Iter = args[1]->getIterator();
    arg1Iter->open();
    bool hasOptions = arg1Iter->next(optionsItem);
    arg1Iter->close();

    RPOptions options;
    if (hasOptions)
    {
      options.parseExtractOptions(optionsItem, itemFactory);
    }


    // make options object
    jclass optClass = env->FindClass("org/zorbaxquery/modules/readPdf/ZorbaReadPdfModule$Options");
    CHECK_EXCEPTION(env);
    jmethodID optConstrId = env->GetMethodID(optClass, "<init>", "()V" );
    CHECK_EXCEPTION(env);
    jobject optObj = env->NewObject(optClass, optConstrId);
    CHECK_EXCEPTION(env);
    jmethodID optSetPasswordId = env->GetMethodID(optClass, "setPassword", "(Ljava/lang/String;)V" );
    CHECK_EXCEPTION(env);
    jstring jstrPass = env->NewStringUTF(options.getPassword().c_str());
    env->CallVoidMethod(optObj, optSetPasswordId, jstrPass);
    CHECK_EXCEPTION(env);
    jmethodID optSetStartPageId = env->GetMethodID(optClass, "setStartPage", "(I)V" );
    CHECK_EXCEPTION(env);
    env->CallVoidMethod(optObj, optSetStartPageId, options.getStartPage());
    CHECK_EXCEPTION(env);
    jmethodID optSetEndPageId = env->GetMethodID(optClass, "setEndPage", "(I)V" );
    CHECK_EXCEPTION(env);
    env->CallVoidMethod(optObj, optSetEndPageId, options.getEndPage());
    CHECK_EXCEPTION(env);
    jmethodID optSetTextKindId = env->GetMethodID(optClass, "setTextKind", "(I)V" );
    CHECK_EXCEPTION(env);
    env->CallVoidMethod(optObj, optSetTextKindId, options.getTextKind());
    CHECK_EXCEPTION(env);
    jmethodID optSetIgnoreCorruptObjectsId = env->GetMethodID(optClass, "setIgnoreCorruptObjects", "(Z)V" );
    CHECK_EXCEPTION(env);
    env->CallVoidMethod(optObj, optSetIgnoreCorruptObjectsId, options.isIgnoreCorruptObjects());
    CHECK_EXCEPTION(env);
    jmethodID optSetIgnoreBeadsId = env->GetMethodID(optClass, "setIgnoreBeads", "(Z)V" );
    CHECK_EXCEPTION(env);
    env->CallVoidMethod(optObj, optSetIgnoreBeadsId, options.isIgnoreBeads());
    CHECK_EXCEPTION(env);
    jmethodID optSetStartPageSepId = env->GetMethodID(optClass, "setStartPageSeparator", "(Ljava/lang/String;)V" );
    CHECK_EXCEPTION(env);
    jstring jstrStart = env->NewStringUTF(options.getStartPageSeparator().c_str());
    env->CallVoidMethod(optObj, optSetStartPageSepId, jstrStart);
    CHECK_EXCEPTION(env);
    jmethodID optSetEndPageSepId = env->GetMethodID(optClass, "setEndPageSeparator", "(Ljava/lang/String;)V" );
    CHECK_EXCEPTION(env);
    jstring jstrEnd = env->NewStringUTF(options.getEndPageSeparator().c_str());
    env->CallVoidMethod(optObj, optSetEndPageSepId, jstrEnd);
    CHECK_EXCEPTION(env);

    // Create a ZorbaReadPdfModule class
    myClass = env->FindClass("org/zorbaxquery/modules/readPdf/ZorbaReadPdfModule");
    CHECK_EXCEPTION(env);
    //std::cout << "  find class '" << myClass << "'" << std::endl; std::cout.flush();
    myMethod = env->GetStaticMethodID(myClass, "extractText",
        "(JLorg/zorbaxquery/modules/readPdf/ZorbaReadPdfModule$Options;)Ljava/lang/String;");
    CHECK_EXCEPTION(env);
    //std::cout << "  find method '" << myMethod << "'" << std::endl; std::cout.flush();

    jstring resStr = (jstring) env->CallStaticObjectMethod(myClass,
            myMethod, pdfInputStreamPointer, optObj);
    CHECK_EXCEPTION(env);
    const char *str;
    str = env->GetStringUTFChars( (jstring)resStr, NULL);
    if ( str == NULL ) return NULL;

    std::string lResultString(str);

    env->ReleaseStringUTFChars( (jstring)resStr, str);
    //std::cout << "  lResultString '" << lResultString << "'" << std::endl; std::cout.flush();

    return lResultString;
}


ItemSequence_t
ExtractTextFunction::evaluate(const ExternalFunction::Arguments_t& args,
                           const zorba::StaticContext* aStaticContext,
                           const zorba::DynamicContext* aDynamincContext) const
{
  jthrowable lException = 0;
  static JNIEnv* env;

  try
  {
    env = zorba::jvm::JavaVMSingleton::getInstance(aStaticContext)->getEnv();

    // read input param 0
    Iterator_t lIter = args[0]->getIterator();
    lIter->open();

    Item lItem;
    std::string lResultString;

    if( lIter->next(lItem) )
    {
        if (lItem.isStreamable())
        {
          std::istream& lStream = lItem.getStream();
          bool lDecoderAttached = false;

          if (lItem.isEncoded())
          {
            base64::attach(lStream);
            lDecoderAttached = true;
          }

          jlong pdfInputStreamPointer = (jlong) &lStream;
          std::string str = extractText(args, theFactory, env, pdfInputStreamPointer);
          lResultString = str;

          if (lDecoderAttached)
          {
            base64::detach(lStream);
          }
        }
        else
        {
          String lTmpDecodedBuf;
          size_t lSize;
          const char* lMsg = lItem.getBase64BinaryValue(lSize);
          if (lItem.isEncoded())
          {
            String lTmpEncoded(lMsg, lSize);
            // lTmpDecodedBuf is used to make sure lMsg is still alive during HMAC_Update
            lTmpDecodedBuf = encoding::Base64::decode(lTmpEncoded);
            lMsg = lTmpDecodedBuf.c_str();
            lSize = lTmpDecodedBuf.size();
          }
          // .. use the string
          std::stringstream sstream;
          sstream << lMsg;
          std::istream *is = &sstream;
          jlong pdfInputStreamPointer = (jlong) is;

          lResultString = extractText(args, theFactory, env, pdfInputStreamPointer);

        }
    }

    return ItemSequence_t(new SingletonItemSequence(theFactory->createString(lResultString)));

  }
  catch (zorba::jvm::VMOpenException&)
  {
      Item lQName = theFactory->createQName(READPDF_MODULE_NAMESPACE,
                "VM001");
      throw USER_EXCEPTION(lQName, "Could not start the Java VM (is the classpath set?)");
  }
  catch (JavaException&)
  {
      env->ExceptionDescribe();

      jclass stringWriterClass = env->FindClass("java/io/StringWriter");
      jclass printWriterClass = env->FindClass("java/io/PrintWriter");
      jclass throwableClass = env->FindClass("java/lang/Throwable");
      jobject stringWriter = env->NewObject(
                stringWriterClass,
                env->GetMethodID(stringWriterClass, "<init>", "()V"));

      jobject printWriter = env->NewObject(
                printWriterClass,
                env->GetMethodID(printWriterClass, "<init>", "(Ljava/io/Writer;)V"),
                stringWriter);

      env->CallObjectMethod(lException,
                env->GetMethodID(throwableClass, "printStackTrace",
                        "(Ljava/io/PrintWriter;)V"),
                printWriter);

      //env->CallObjectMethod(printWriter, env->GetMethodID(printWriterClass, "flush", "()V"));
      jmethodID toStringMethod =
            env->GetMethodID(stringWriterClass, "toString", "()Ljava/lang/String;");
      jobject errorMessageObj = env->CallObjectMethod( stringWriter, toStringMethod);
      jstring errorMessage = (jstring) errorMessageObj;
      const char *errMsg = env->GetStringUTFChars(errorMessage, NULL);
      std::stringstream s;
      s << "A Java Exception was thrown:" << std::endl << errMsg;
      env->ReleaseStringUTFChars(errorMessage, errMsg);
      String errDescription;
      errDescription += s.str();
      env->ExceptionClear();
      Item errQName = theFactory->createQName(READPDF_MODULE_NAMESPACE,
                "JAVA-EXCEPTION");
      throw USER_EXCEPTION(errQName, errDescription);
  }

  return ItemSequence_t(new EmptySequence());
}


// Render pages code

void renderToImages(const ExternalFunction::Arguments_t& args,
                          ItemFactory *itemFactory,
                          JNIEnv * env,
                          jlong pdfInputStreamPointer,
                          std::vector<Item>& vec)
{
    // Local variables
    jthrowable lException = 0;
    jclass myClass;
    jmethodID myMethod;

    // read input parm: $options
    Item optionsItem;
    Iterator_t arg1Iter = args[1]->getIterator();
    arg1Iter->open();
    bool hasOptions = arg1Iter->next(optionsItem);
    arg1Iter->close();

    RPOptions options;
    if (hasOptions)
    {
      options.parseRenderOptions(optionsItem, itemFactory);
    }


    // make options object
    jclass optClass = env->FindClass("org/zorbaxquery/modules/readPdf/ZorbaReadPdfModule$Options");
    CHECK_EXCEPTION(env);
    jmethodID optConstrId = env->GetMethodID(optClass, "<init>", "()V" );
    CHECK_EXCEPTION(env);
    jobject optObj = env->NewObject(optClass, optConstrId);
    CHECK_EXCEPTION(env);
    jmethodID optSetPasswordId = env->GetMethodID(optClass, "setPassword", "(Ljava/lang/String;)V" );
    CHECK_EXCEPTION(env);
    env->CallVoidMethod(optObj, optSetPasswordId, options.getPassword().c_str());
    CHECK_EXCEPTION(env);
    jmethodID optSetStartPageId = env->GetMethodID(optClass, "setStartPage", "(I)V" );
    CHECK_EXCEPTION(env);
    env->CallVoidMethod(optObj, optSetStartPageId, options.getStartPage());
    CHECK_EXCEPTION(env);
    jmethodID optSetEndPageId = env->GetMethodID(optClass, "setEndPage", "(I)V" );
    CHECK_EXCEPTION(env);
    env->CallVoidMethod(optObj, optSetEndPageId, options.getEndPage());
    CHECK_EXCEPTION(env);
    jmethodID optSetTextKindId = env->GetMethodID(optClass, "setImageKind", "(I)V" );
    CHECK_EXCEPTION(env);
    env->CallVoidMethod(optObj, optSetTextKindId, options.getImageKind());
    CHECK_EXCEPTION(env);
    jmethodID optSetIgnoreCorruptObjectsId = env->GetMethodID(optClass, "setIgnoreCorruptObjects", "(Z)V" );
    CHECK_EXCEPTION(env);
    env->CallVoidMethod(optObj, optSetIgnoreCorruptObjectsId, options.isIgnoreCorruptObjects());
    CHECK_EXCEPTION(env);
    jmethodID optSetIgnoreBeadsId = env->GetMethodID(optClass, "setIgnoreBeads", "(Z)V" );
    CHECK_EXCEPTION(env);
    env->CallVoidMethod(optObj, optSetIgnoreBeadsId, options.isIgnoreBeads());
    CHECK_EXCEPTION(env);

    // Create a ZorbaReadPdfModule class
    myClass = env->FindClass("org/zorbaxquery/modules/readPdf/ZorbaReadPdfModule");
    CHECK_EXCEPTION(env);

    myMethod = env->GetStaticMethodID(myClass, "renderToImages",
        "(JLorg/zorbaxquery/modules/readPdf/ZorbaReadPdfModule$Options;Lorg/zorbaxquery/modules/readPdf/OutputStreamCreator;)V");
    CHECK_EXCEPTION(env);

    // prepare OutputStreamCreator
    CppOutputStreamCreator* osCreator = CppOutputStreamCreator::newInstance(env);

    env->CallStaticVoidMethod(myClass,
            myMethod, pdfInputStreamPointer, optObj, osCreator->getJavaObject());
    CHECK_EXCEPTION(env);

    for ( int i=0; i<osCreator->getSize(); i++)
    {
      std::stringstream* ss = osCreator->getSS(i);
      std::string ssString = ss->str();
      vec.push_back(
          itemFactory->createBase64Binary((const unsigned char*)ssString.c_str(), ssString.size()));
    }

    osCreator->close(env);
    delete osCreator;
}


ItemSequence_t
RenderToImagesFunction::evaluate(const ExternalFunction::Arguments_t& args,
                           const zorba::StaticContext* aStaticContext,
                           const zorba::DynamicContext* aDynamicContext) const
{
  Iterator_t lIter;

  jthrowable lException = 0;
  static JNIEnv* env;

  try
  {
    env = zorba::jvm::JavaVMSingleton::getInstance(aStaticContext)->getEnv();

    // read input param 0
    Iterator_t lIter = args[0]->getIterator();
    lIter->open();

    Item lItem;
    std::vector<Item> vec;

    if( lIter->next(lItem) )
    {
      if (lItem.isStreamable())
      {
        std::istream& lStream = lItem.getStream();
        bool lDecoderAttached = false;

        if (lItem.isEncoded())
        {
          base64::attach(lStream);
          lDecoderAttached = true;
        }

        jlong pdfInputStreamPointer = (jlong) &lStream;
        renderToImages(args, theFactory, env, pdfInputStreamPointer, vec);

        if (lDecoderAttached)
        {
          base64::detach(lStream);
        }
      }
      else
      {
        String lTmpDecodedBuf;
        size_t lSize;
        const char* lMsg = lItem.getBase64BinaryValue(lSize);
        if (lItem.isEncoded())
        {
          String lTmpEncoded(lMsg, lSize);
          // lTmpDecodedBuf is used to make sure lMsg is still alive during HMAC_Update
          lTmpDecodedBuf = encoding::Base64::decode(lTmpEncoded);
          lMsg = lTmpDecodedBuf.c_str();
          lSize = lTmpDecodedBuf.size();
        }
        // .. use the string
        std::stringstream sstream;
        sstream << lMsg;
        std::istream *is = &sstream;
        jlong pdfInputStreamPointer = (jlong) is;

        renderToImages(args, theFactory, env, pdfInputStreamPointer, vec);
      }
    }

    return ItemSequence_t(new VectorItemSequence(vec));
  }
  catch (zorba::jvm::VMOpenException&)
  {
    Item lQName = theFactory->createQName(READPDF_MODULE_NAMESPACE,
                                          "VM001");
    throw USER_EXCEPTION(lQName, "Could not start the Java VM (is the classpath set?)");
  }
  catch (JavaException&)
  {
    env->ExceptionDescribe();

    jclass stringWriterClass = env->FindClass("java/io/StringWriter");
    jclass printWriterClass = env->FindClass("java/io/PrintWriter");
    jclass throwableClass = env->FindClass("java/lang/Throwable");
    jobject stringWriter = env->NewObject(
              stringWriterClass,
              env->GetMethodID(stringWriterClass, "<init>", "()V"));

    jobject printWriter = env->NewObject(
              printWriterClass,
              env->GetMethodID(printWriterClass, "<init>", "(Ljava/io/Writer;)V"),
              stringWriter);

    env->CallObjectMethod(lException,
              env->GetMethodID(throwableClass, "printStackTrace",
                      "(Ljava/io/PrintWriter;)V"),
              printWriter);

    //env->CallObjectMethod(printWriter, env->GetMethodID(printWriterClass, "flush", "()V"));
    jmethodID toStringMethod =
          env->GetMethodID(stringWriterClass, "toString", "()Ljava/lang/String;");
    jobject errorMessageObj = env->CallObjectMethod(
              stringWriter, toStringMethod);
    jstring errorMessage = (jstring) errorMessageObj;
    const char *errMsg = env->GetStringUTFChars(errorMessage, 0);
    std::stringstream s;
    s << "A Java Exception was thrown:" << std::endl << errMsg;
    env->ReleaseStringUTFChars(errorMessage, errMsg);
    std::string err("");
    err += s.str();
    env->ExceptionClear();
    Item lQName = theFactory->createQName(READPDF_MODULE_NAMESPACE,
              "JAVA-EXCEPTION");
    throw USER_EXCEPTION(lQName, err);
  }

  return ItemSequence_t(new EmptySequence());
}



bool compareItemQName(Item item, const char *localname, const char *ns)
{
  int node_kind = item.getNodeKind();
  if(node_kind != store::StoreConsts::elementNode)
    return false;
  Item node_name;
  item.getNodeName(node_name);
  String  item_namespace = node_name.getNamespace();
  if(ns && ns[0] && item_namespace != ns )
  {
    return false;
  }
  String  item_name = node_name.getLocalName();
  if(item_name != localname)
  {
    return false;
  }
  return true;
}


bool getChild(zorba::Iterator_t children, const char *localname, const char *ns,
                           zorba::Item &child_item);
bool getChild(zorba::Item &lItem, const char *localname, const char *ns,
                           zorba::Item &child_item)
{
  Iterator_t    children;
  children = lItem.getChildren();
  children->open();
  bool retval = getChild(children, localname, ns, child_item);
  children->close();
  return retval;
}


bool getChild(zorba::Iterator_t children, const char *localname, const char *ns,
              zorba::Item &child_item)
{
  while(children->next(child_item))
  {
    if(child_item.getNodeKind() != store::StoreConsts::elementNode)
      continue;
    Item    child_name;
    child_item.getNodeName(child_name);
    String  item_namespace = child_name.getNamespace();
    if(item_namespace != ns)
    {
      continue;//next child
    }
    String  item_name = child_name.getLocalName();
    if(item_name != localname)
    {
      continue;//next child
    }
    return true;
  }
  return false;
}

void RPOptions::parseExtractOptions(Item optionsNode, ItemFactory *itemFactory)
{
  if(optionsNode.isNull())
    return;

  if(!compareItemQName(optionsNode, "extract-text-options", READPDF_OPTIONS_NAMESPACE) )
  {
    std::stringstream lErrorMessage;
    Item options_qname;
    optionsNode.getNodeName(options_qname);
    lErrorMessage << "Options field must be of element 'extract-text-options' " <<
                     "instead of '" <<
                     options_qname.getStringValue() << "'. ";
    Item errWrongParamQName;
    String errName("WrongParam");
    errWrongParamQName = itemFactory->createQName(READPDF_OPTIONS_NAMESPACE, errName);
    String errDescription(lErrorMessage.str());
    throw USER_EXCEPTION(errWrongParamQName, errDescription);
  }

  zorba::Item child_item;

  if(getChild(optionsNode, "password", READPDF_OPTIONS_NAMESPACE, child_item))
  {
    thePassword = child_item.getStringValue();
  }

  if(getChild(optionsNode, "text-kind", READPDF_OPTIONS_NAMESPACE, child_item))
  {
    String sct_text = child_item.getStringValue();
    if ( sct_text == "simple" )
      theTextKind = TEXT_SIMPLE;
    else if ( sct_text == "html" )
      theTextKind = TEXT_HTML;
  }

  if(getChild(optionsNode, "start-page", READPDF_OPTIONS_NAMESPACE, child_item))
  {
    String sct_text = child_item.getStringValue();
    theStartPage = atoi(sct_text.c_str());
  }

  if(getChild(optionsNode, "end-page", READPDF_OPTIONS_NAMESPACE, child_item))
  {
    String sct_text = child_item.getStringValue();
    theEndPage = atoi(sct_text.c_str());
  }

  if(getChild(optionsNode, "ignore-corrupt-objects", READPDF_OPTIONS_NAMESPACE, child_item))
  {
    String sct_text = child_item.getStringValue();
    if ( sct_text == "true" || sct_text == "1" )
      theIgnoreCorruptObjects = true;
    else
      theIgnoreCorruptObjects = false;
  }

  if(getChild(optionsNode, "ignore-beads", READPDF_OPTIONS_NAMESPACE, child_item))
  {
    String sct_text = child_item.getStringValue();
    if ( sct_text == "true" || sct_text == "1" )
      theIgnoreBeads = true;
    else
      theIgnoreBeads = false;
  }

  if(getChild(optionsNode, "start-page-separator", READPDF_OPTIONS_NAMESPACE, child_item))
  {
    theStartPageSeparator = child_item.getStringValue();
  }

  if(getChild(optionsNode, "end-page-separator", READPDF_OPTIONS_NAMESPACE, child_item))
  {
    theEndPageSeparator = child_item.getStringValue();
  }
}

void RPOptions::parseRenderOptions(Item optionsNode, ItemFactory *itemFactory)
{
  if(optionsNode.isNull())
    return;

  if(!compareItemQName(optionsNode, "render-to-images-options", READPDF_OPTIONS_NAMESPACE))
  {
    std::stringstream lErrorMessage;
    Item options_qname;
    optionsNode.getNodeName(options_qname);
    lErrorMessage << "Options field must be of element 'render-to-images-options' instead of '" <<
                     options_qname.getStringValue() << "'";
    Item errWrongParamQName;
    String errName("WrongParam");
    errWrongParamQName = itemFactory->createQName(READPDF_OPTIONS_NAMESPACE, errName);
    String errDescription(lErrorMessage.str());
    throw USER_EXCEPTION(errWrongParamQName, errDescription);
  }

  zorba::Item child_item;

  if(getChild(optionsNode, "password", READPDF_OPTIONS_NAMESPACE, child_item))
  {
    thePassword = child_item.getStringValue();
  }

  if(getChild(optionsNode, "image-kind", READPDF_OPTIONS_NAMESPACE, child_item))
  {
    String sct_text = child_item.getStringValue();
    if ( sct_text == "jpg" )
      theImageKind = IMAGE_JPG;
    else if ( sct_text == "png" )
      theImageKind = IMAGE_PNG;
  }

  if(getChild(optionsNode, "start-page", READPDF_OPTIONS_NAMESPACE, child_item))
  {
    String sct_text = child_item.getStringValue();
    theStartPage = atoi(sct_text.c_str());
  }

  if(getChild(optionsNode, "end-page", READPDF_OPTIONS_NAMESPACE, child_item))
  {
      String sct_text = child_item.getStringValue();
      theEndPage = atoi(sct_text.c_str());
  }

  if(getChild(optionsNode, "ignore-corrupt-objects", READPDF_OPTIONS_NAMESPACE, child_item))
  {
    String sct_text = child_item.getStringValue();
    if ( sct_text == "true" || sct_text == "1" )
      theIgnoreCorruptObjects = true;
    else
      theIgnoreCorruptObjects = false;
  }

  if(getChild(optionsNode, "ignore-beads", READPDF_OPTIONS_NAMESPACE, child_item))
  {
    String sct_text = child_item.getStringValue();
    if ( sct_text == "true" || sct_text == "1" )
      theIgnoreBeads = true;
    else
      theIgnoreBeads = false;
  }
}

}} // namespace zorba, readpdf

#ifdef WIN32
#  define DLL_EXPORT __declspec(dllexport)
#else
#  define DLL_EXPORT __attribute__ ((visibility("default")))
#endif

extern "C" DLL_EXPORT zorba::ExternalModule* createModule()
{
  return new zorba::readpdf::ReadPdfModule();
}
