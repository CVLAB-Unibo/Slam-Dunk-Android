#ifndef SLAM_DUNK_SLAMDUNK_DEFINES_H
#define SLAM_DUNK_SLAMDUNK_DEFINES_H

  // The following ifdef block is the standard way of creating macros which make exporting 
  // from a DLL simpler. All files within this DLL are compiled with the SLAM_DUNK_EXPORTS
  // symbol defined on the command line. This symbol should not be defined on any project
  // that uses this DLL. This way any other project whose source files include this file see 
  // SEBA_API functions as being imported from a DLL, whereas this DLL sees symbols
  // defined with this macro as being exported.
  #ifdef _MSC_VER
    #ifdef SLAM_DUNK_EXPORTS
      #define SLAM_DUNK_API __declspec(dllexport)
    #else
      #define SLAM_DUNK_API __declspec(dllimport)
    #endif
  #else
    #define SLAM_DUNK_API
  #endif


  #ifdef _MSC_VER
    #ifndef _CRT_SECURE_NO_WARNINGS
      #define _CRT_SECURE_NO_WARNINGS
    #endif
    #define NOMINMAX
    #include "windows.h"
  #endif

  #if defined (_MSC_VER) && _MSC_VER <= 1600  // 1400 == VC++ 8.0, 1600 == VC++ 10.0
    #pragma warning(disable:4251 4231 4660)
  #endif

#endif // SLAM_DUNK_SLAMDUNK_DEFINES_H
