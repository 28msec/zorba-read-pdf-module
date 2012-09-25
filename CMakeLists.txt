# Copyright 2006-2012 The FLWOR Foundation.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

CMAKE_MINIMUM_REQUIRED(VERSION 2.6)

PROJECT (zorba_read-pdf_module)

MESSAGE (STATUS " --- Module:   read-pdf    ---")
# The util-jvm tool finds Java and JNI, so all standard cached JAVA_
# variables will be set. util-jvm also caches Java_FOUND and JNI_FOUND
# for us.
FIND_PACKAGE (zorba_util-jvm_module QUIET)

IF (zorba_util-jvm_module_FOUND)
  IF (JNI_FOUND)
    INCLUDE_DIRECTORIES (${zorba_util-jvm_module_INCLUDE_DIRS})

    IF (WIN32)
      SET (PDFBOX_PATH "$ENV{PDFBOX_HOME}\\lib C:\\dev\\libraries\\pdfbox C:\\dev\\libraries\\pdfbox\\lib C:\\dev\\libraries\\pdfbox\\build")
      MESSAGE (STATUS "pdfbox fontbox jars searching path: " ${PDFBOX_PATH} )
    ELSE (WIN32)
      SET (PDFBOX_PATH /usr/share/java/
                       /usr/share/java/pdfbox/ /opt/local/share/java/pdfbox/
                       /usr/share/java/fontbox/ /opt/local/share/java/fontbox/
                       ${Java_LIBRARIES})
    ENDIF (WIN32)

    ZORBA_FIND_JAR( PDFBOX_JAR NAMES pdfbox PATHS ${PDFBOX_PATH} )
    ZORBA_FIND_JAR( FONTBOX_JAR NAMES fontbox PATHS ${PDFBOX_PATH} )

    SET(ALL_PDFBOX_JARS FALSE)
    IF(EXISTS ${PDFBOX_JAR})
      IF(EXISTS ${FONTBOX_JAR})
        SET(ALL_PDFBOX_JARS TRUE)
      ENDIF(EXISTS ${FONTBOX_JAR})
    ENDIF(EXISTS ${PDFBOX_JAR})

    IF( ALL_PDFBOX_JARS )

      MESSAGE (STATUS "pdfbox jars found at: " ${PDFBOX_JAR}  ${FONTBOX_JAR})
      # if everything is found build this module
      SET (READ_PDF_VERSION 1.0)

      ENABLE_TESTING ()
      INCLUDE (CTest)

      SET_CMAKE_MODULE_PATH ()

      FIND_PACKAGE (Zorba REQUIRED HINTS "${ZORBA_BUILD_DIR}")
      INCLUDE ("${Zorba_USE_FILE}")

      ADD_SUBDIRECTORY ("src")
      ADD_SUBDIRECTORY ("srcJava")
      ADD_TEST_DIRECTORY("${PROJECT_SOURCE_DIR}/test")
      DONE_DECLARING_ZORBA_URIS ()

    ELSE( ALL_PDFBOX_JARS )
      MESSAGE (STATUS "pdfbox or fontbox jars not found; skipping read-pdf module.")
    ENDIF( ALL_PDFBOX_JARS )

  ELSE (JNI_FOUND)
    MESSAGE (STATUS "Java and/or JNI not found; skipping read-pdf module.")
  ENDIF (JNI_FOUND)

ELSE (zorba_util-jvm_module_FOUND)
  MESSAGE (STATUS "Zorba's util-jvm module not found; skipping read-pdf module.")
ENDIF (zorba_util-jvm_module_FOUND)

MESSAGE (STATUS " --- End module:   read-pdf    ---")