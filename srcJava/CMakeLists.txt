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

# all external module libraries are generated in the directory
# of the corresponding .xq file
MESSAGE(STATUS "Add srcJava")

# Java

find_package( Java)
set( NON_NATIVE_TARGET TRUE)


# Verbose makefile
set( CMAKE_VERBOSE_MAKEFILE TRUE)

set( JAR_NAME ${PROJECT_NAME}-${READ_PDF_VERSION}.jar )
set( CMAKE_SOURCE_DIR ${CMAKE_CURRENT_SOURCE_DIR})

# find all java source files
file(GLOB_RECURSE JAVA_SOURCE1_FILES RELATIVE "${CMAKE_CURRENT_SOURCE_DIR}"
    "${CMAKE_CURRENT_SOURCE_DIR}/*.java")

# copy all .java files into BINARY_DIR
FOREACH(JAVA_SOURCE1_FILE ${JAVA_SOURCE1_FILES})
    CONFIGURE_FILE("${CMAKE_CURRENT_SOURCE_DIR}/${JAVA_SOURCE1_FILE}" "${CMAKE_CURRENT_BINARY_DIR}/${JAVA_SOURCE1_FILE}" COPYONLY)
ENDFOREACH(JAVA_SOURCE1_FILE ${JAVA_SOURCE1_FILES})

# find all java files, this will also find Config.java with all the correct values inside
file(GLOB_RECURSE JAVA_SOURCE_FILES RELATIVE "${CMAKE_CURRENT_BINARY_DIR}"
    "${CMAKE_CURRENT_BINARY_DIR}/*.java")

IF (WIN32)
  set( JAVA_CLASS_PATH
    "${JAVA_CLASS_PATH};${PDFBOX_JAR};${FONTBOX_JAR};${JEMPBOX_JAR};${COMMLOG_JAR}"
    )
ELSE (WIN32)
  set( JAVA_CLASS_PATH
    ${JAVA_CLASS_PATH}:${PDFBOX_JAR}:${FONTBOX_JAR}:${JEMPBOX_JAR}:${COMMLOG_JAR}
    )
ENDIF (WIN32)

#MESSAGE (STATUS "--!!!!!---" ${JAVA_COMPILE} " -d " ${CMAKE_CURRENT_BINARY_DIR}
#           " -cp " ${JAVA_CLASS_PATH} "   "
#           ${JAVA_SOURCE_FILES}
#   " WORKING_DIRECTORY " ${CMAKE_CURRENT_BINARY_DIR}
#   " SOURCES " ${JAVA_SOURCE_FILES})

# Compile all java files
add_custom_target( ${PROJECT_NAME}.jar.classes
   COMMAND ${JAVA_COMPILE} -d ${CMAKE_CURRENT_BINARY_DIR}
           -cp ${JAVA_CLASS_PATH}
           ${JAVA_SOURCE_FILES}
   WORKING_DIRECTORY ${CMAKE_CURRENT_BINARY_DIR}
   SOURCES ${JAVA_SOURCE_FILES})
add_dependencies(${PROJECT_NAME}.jar.classes ${JAVA_SOURCE_FILES} )


file(GLOB_RECURSE JAVA_CLASS_FILES RELATIVE "${CMAKE_CURRENT_BINARY_DIR}"
    "${CMAKE_CURRENT_BINARY_DIR}/*.class")
#MESSAGE (STATUS "found .classes: " ${JAVA_SOURCE_FILES})

# jar all class files into a big jar
add_custom_command(
  OUTPUT ${CMAKE_CURRENT_BINARY_DIR}/${JAR_NAME}
  COMMAND ${JAVA_ARCHIVE} cf ${JAR_NAME} org
  DEPENDS ${PROJECT_NAME}.jar.classes
  WORKING_DIRECTORY ${CMAKE_CURRENT_BINARY_DIR} )

add_custom_target( ${JAR_NAME} ALL
  DEPENDS ${CMAKE_CURRENT_BINARY_DIR}/${JAR_NAME} )

# add Jar dependencies
DECLARE_ZORBA_JAR(FILE ${CMAKE_CURRENT_BINARY_DIR}/${JAR_NAME}
  TARGET ${JAR_NAME})
DECLARE_ZORBA_JAR(FILE ${PDFBOX_JAR} EXTERNAL)
DECLARE_ZORBA_JAR(FILE ${FONTBOX_JAR} EXTERNAL)
DECLARE_ZORBA_JAR(FILE ${JEMPBOX_JAR} EXTERNAL)
DECLARE_ZORBA_JAR(FILE ${COMMLOG_JAR} EXTERNAL)

MESSAGE(STATUS "End srcJava")