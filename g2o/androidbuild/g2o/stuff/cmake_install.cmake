# Install script for directory: /home/falthiel/Downloads/g2o/g2o/stuff

# Set the install prefix
IF(NOT DEFINED CMAKE_INSTALL_PREFIX)
  SET(CMAKE_INSTALL_PREFIX "/home/falthiel/Downloads/android-toolchain/user")
ENDIF(NOT DEFINED CMAKE_INSTALL_PREFIX)
STRING(REGEX REPLACE "/$" "" CMAKE_INSTALL_PREFIX "${CMAKE_INSTALL_PREFIX}")

# Set the install configuration name.
IF(NOT DEFINED CMAKE_INSTALL_CONFIG_NAME)
  IF(BUILD_TYPE)
    STRING(REGEX REPLACE "^[^A-Za-z0-9_]+" ""
           CMAKE_INSTALL_CONFIG_NAME "${BUILD_TYPE}")
  ELSE(BUILD_TYPE)
    SET(CMAKE_INSTALL_CONFIG_NAME "Release")
  ENDIF(BUILD_TYPE)
  MESSAGE(STATUS "Install configuration: \"${CMAKE_INSTALL_CONFIG_NAME}\"")
ENDIF(NOT DEFINED CMAKE_INSTALL_CONFIG_NAME)

# Set the component getting installed.
IF(NOT CMAKE_INSTALL_COMPONENT)
  IF(COMPONENT)
    MESSAGE(STATUS "Install component: \"${COMPONENT}\"")
    SET(CMAKE_INSTALL_COMPONENT "${COMPONENT}")
  ELSE(COMPONENT)
    SET(CMAKE_INSTALL_COMPONENT)
  ENDIF(COMPONENT)
ENDIF(NOT CMAKE_INSTALL_COMPONENT)

# Install shared libraries without execute permission?
IF(NOT DEFINED CMAKE_INSTALL_SO_NO_EXE)
  SET(CMAKE_INSTALL_SO_NO_EXE "1")
ENDIF(NOT DEFINED CMAKE_INSTALL_SO_NO_EXE)

IF(NOT CMAKE_INSTALL_COMPONENT OR "${CMAKE_INSTALL_COMPONENT}" STREQUAL "Unspecified")
  list(APPEND CMAKE_ABSOLUTE_DESTINATION_FILES
   "/home/falthiel/Downloads/android-toolchain/user/lib/libg2o_stuff.so")
  IF (CMAKE_WARN_ON_ABSOLUTE_INSTALL_DESTINATION)
    message(WARNING "ABSOLUTE path INSTALL DESTINATION : ${CMAKE_ABSOLUTE_DESTINATION_FILES}")
  ENDIF (CMAKE_WARN_ON_ABSOLUTE_INSTALL_DESTINATION)
  IF (CMAKE_ERROR_ON_ABSOLUTE_INSTALL_DESTINATION)
    message(FATAL_ERROR "ABSOLUTE path INSTALL DESTINATION forbidden (by caller): ${CMAKE_ABSOLUTE_DESTINATION_FILES}")
  ENDIF (CMAKE_ERROR_ON_ABSOLUTE_INSTALL_DESTINATION)
FILE(INSTALL DESTINATION "/home/falthiel/Downloads/android-toolchain/user/lib" TYPE SHARED_LIBRARY FILES "/home/falthiel/Downloads/g2o/lib/libg2o_stuff.so")
  IF(EXISTS "$ENV{DESTDIR}/home/falthiel/Downloads/android-toolchain/user/lib/libg2o_stuff.so" AND
     NOT IS_SYMLINK "$ENV{DESTDIR}/home/falthiel/Downloads/android-toolchain/user/lib/libg2o_stuff.so")
    IF(CMAKE_INSTALL_DO_STRIP)
      EXECUTE_PROCESS(COMMAND "/home/falthiel/Downloads/android-toolchain/bin/arm-linux-androideabi-strip" "$ENV{DESTDIR}/home/falthiel/Downloads/android-toolchain/user/lib/libg2o_stuff.so")
    ENDIF(CMAKE_INSTALL_DO_STRIP)
  ENDIF()
ENDIF(NOT CMAKE_INSTALL_COMPONENT OR "${CMAKE_INSTALL_COMPONENT}" STREQUAL "Unspecified")

IF(NOT CMAKE_INSTALL_COMPONENT OR "${CMAKE_INSTALL_COMPONENT}" STREQUAL "Unspecified")
  list(APPEND CMAKE_ABSOLUTE_DESTINATION_FILES
   "/home/falthiel/Downloads/android-toolchain/user/include/g2o/stuff/filesys_tools.h;/home/falthiel/Downloads/android-toolchain/user/include/g2o/stuff/opengl_primitives.h;/home/falthiel/Downloads/android-toolchain/user/include/g2o/stuff/opengl_wrapper.h;/home/falthiel/Downloads/android-toolchain/user/include/g2o/stuff/command_args.h;/home/falthiel/Downloads/android-toolchain/user/include/g2o/stuff/property.h;/home/falthiel/Downloads/android-toolchain/user/include/g2o/stuff/sampler.h;/home/falthiel/Downloads/android-toolchain/user/include/g2o/stuff/color_macros.h;/home/falthiel/Downloads/android-toolchain/user/include/g2o/stuff/unscented.h;/home/falthiel/Downloads/android-toolchain/user/include/g2o/stuff/g2o_stuff_api.h;/home/falthiel/Downloads/android-toolchain/user/include/g2o/stuff/scoped_pointer.h;/home/falthiel/Downloads/android-toolchain/user/include/g2o/stuff/misc.h;/home/falthiel/Downloads/android-toolchain/user/include/g2o/stuff/sparse_helper.h;/home/falthiel/Downloads/android-toolchain/user/include/g2o/stuff/macros.h;/home/falthiel/Downloads/android-toolchain/user/include/g2o/stuff/timeutil.h;/home/falthiel/Downloads/android-toolchain/user/include/g2o/stuff/os_specific.h;/home/falthiel/Downloads/android-toolchain/user/include/g2o/stuff/string_tools.h;/home/falthiel/Downloads/android-toolchain/user/include/g2o/stuff/tictoc.h")
  IF (CMAKE_WARN_ON_ABSOLUTE_INSTALL_DESTINATION)
    message(WARNING "ABSOLUTE path INSTALL DESTINATION : ${CMAKE_ABSOLUTE_DESTINATION_FILES}")
  ENDIF (CMAKE_WARN_ON_ABSOLUTE_INSTALL_DESTINATION)
  IF (CMAKE_ERROR_ON_ABSOLUTE_INSTALL_DESTINATION)
    message(FATAL_ERROR "ABSOLUTE path INSTALL DESTINATION forbidden (by caller): ${CMAKE_ABSOLUTE_DESTINATION_FILES}")
  ENDIF (CMAKE_ERROR_ON_ABSOLUTE_INSTALL_DESTINATION)
FILE(INSTALL DESTINATION "/home/falthiel/Downloads/android-toolchain/user/include/g2o/stuff" TYPE FILE FILES
    "/home/falthiel/Downloads/g2o/g2o/stuff/filesys_tools.h"
    "/home/falthiel/Downloads/g2o/g2o/stuff/opengl_primitives.h"
    "/home/falthiel/Downloads/g2o/g2o/stuff/opengl_wrapper.h"
    "/home/falthiel/Downloads/g2o/g2o/stuff/command_args.h"
    "/home/falthiel/Downloads/g2o/g2o/stuff/property.h"
    "/home/falthiel/Downloads/g2o/g2o/stuff/sampler.h"
    "/home/falthiel/Downloads/g2o/g2o/stuff/color_macros.h"
    "/home/falthiel/Downloads/g2o/g2o/stuff/unscented.h"
    "/home/falthiel/Downloads/g2o/g2o/stuff/g2o_stuff_api.h"
    "/home/falthiel/Downloads/g2o/g2o/stuff/scoped_pointer.h"
    "/home/falthiel/Downloads/g2o/g2o/stuff/misc.h"
    "/home/falthiel/Downloads/g2o/g2o/stuff/sparse_helper.h"
    "/home/falthiel/Downloads/g2o/g2o/stuff/macros.h"
    "/home/falthiel/Downloads/g2o/g2o/stuff/timeutil.h"
    "/home/falthiel/Downloads/g2o/g2o/stuff/os_specific.h"
    "/home/falthiel/Downloads/g2o/g2o/stuff/string_tools.h"
    "/home/falthiel/Downloads/g2o/g2o/stuff/tictoc.h"
    )
ENDIF(NOT CMAKE_INSTALL_COMPONENT OR "${CMAKE_INSTALL_COMPONENT}" STREQUAL "Unspecified")

