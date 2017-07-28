# Slam Dunk Android

Android implementation of "Fusion of inertial and visual measurements for rgb-d slam on mobile devices", if you use this code please refer to:

>Brunetto, N., Salti, S., Fioraio, N., Cavallari, T., & Stefano, L. (2015). Fusion of inertial and visual measurements for rgb-d slam on mobile devices. In Proceedings of the IEEE International Conference on Computer Vision Workshops (pp. 1-9).

---


## Instruction to set up the application


### Android Device
	
Application tested on Samsung Galaxy Tab Pro 10.1 and Nexus 7 tablets.
	
**REQUIREMENTS:**
- The device of choice has to support USB host mode in order to connect it to the RGB-D sensors and be able to recognize the components.
- Rooting of the phone required in order to use the compatible RGB-D sensors.
- Install the BusyBox application after the rooting procedure and specify the path "/system/xbin" for the binaries location. It is possible to use a different path. In this case it is only necessary to modify the value of the string "binariesPath" inside the class SuperUserProcess (package it.unibo.slam.runtime).
- By now the native code is compatible with ARMv7 architecture with NEON instructions while the minimum Android version is 3.0.
	
### OpenNI
	
Compiled binaries for Kinect (OpenNI 1.5):
- SensorKinect/Platform/Android/libs/armeabi-v7a
- OpenNI/Platform/Android/libs/armeabi-v7a

Compiled binaries for Xtion Pro Live (OpenNI 1.5):
- Sensor-Unstable/Platform/Android/libs/armeabi-v7a
- OpenNI-Unstable/Platform/Android/libs/armeabi-v7a

Compiled binaries for Structure (OpenNI 2):
- OpenNI-2-Structure/Packaging/Final/OpenNI-android-2.2

The libraries (*.so files) found in the specified folders need to be placed on the tablet in the folder "/system/lib" (root required).At the moment only one possible OpenNI configuration at a time is supported. To change the supported sensor it is needed to place the required libraries in the "/system/lib" folder and change the file "jni/Android.mk" accordingly.

In case of the Structure sensor it is also needed to place in the "/system/lib" folder the files OpenNI.ini and PS1080.ini found in the same folder previously specified.
For the Xtion sensor it is necessary to place the files OpenNI-Unstable/Data/SamplesConfig.xml, OpenNI-Unstable/Data/licenses.xml,OpenNI-Unstable/Data/modules.xml and Sensor-Unstable/Data/GlobalDefaults.ini in the folder "/data/ni" (create it if it doesn't exist) on the tablet.
For the Kinect sensor similar files are available in the folders OpenNI/Data and SensorKinect/Data.

All the files copied inside the "/system/lib" folder need to have the permissions of read and write enabled for all categories of users.
	
### Boost
	
Boost libraries (*.a files) are found in the folder "BoostAndroid/build/lib" and need to be placed in the "/system/lib" folder 
of the device, in the same way as specified for the OpenNI libraries. By now the Boost libraries used by the application are:
- libboost_chrono
- libboost_date_time
- libboost_system
- libboost_timer
	
### G2O

Same as for OpenNI and Boost libraries, in this case the G2O libraries (*.so files) are found in the folder "g2o/lib".
The used libraries are:
- libg2o_core
- libg2o_solver_pcg
- libg2o_stuff
- libg2o_types_slam3d

