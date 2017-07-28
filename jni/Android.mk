LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

include $(LOCAL_PATH)/../BoostAndroid/build/lib/Boost.mk

include $(LOCAL_PATH)/../g2o/lib/g2o.mk

include $(LOCAL_PATH)/../OpenNI-2-Structure/Lib/OpenNI2.mk

include $(LOCAL_PATH)/../TangoSDK_Furud_C/lib/Tango.mk

OPENCV_CAMERA_MODULES:=on
OPENCV_INSTALL_MODULES:=on
include $(LOCAL_PATH)/../OpenCV-2.4.8-android-sdk/sdk/native/jni/OpenCV.mk

LOCAL_MODULE	:= SLAM

#LOCAL_SRC_FILES += grabber/OpenNIGrabber.cpp
#LOCAL_SRC_FILES += grabber/Senz3DGrabber.cpp
LOCAL_SRC_FILES += grabber/StructureGrabber.cpp
LOCAL_SRC_FILES += eigen/MatrixOp.cpp
LOCAL_SRC_FILES += eigen/EigenOp.cpp
LOCAL_SRC_FILES += g2o/G2O.cpp
LOCAL_SRC_FILES += g2o/solver_pcg.cpp
LOCAL_SRC_FILES += g2o/optimization_algorithm_levenberg.cpp
LOCAL_SRC_FILES += random/Random.cpp
LOCAL_SRC_FILES += utils/TimeUtils.cpp
LOCAL_SRC_FILES += utils/ImageUtils.cpp
LOCAL_SRC_FILES += utils/PointCloudUtils.cpp
LOCAL_SRC_FILES += utils/CameraPoseUtils.cpp
LOCAL_SRC_FILES += point_cloud/src/PointCloudTransform.cpp
LOCAL_SRC_FILES += point_cloud/src/OrientedBoundingBox.cpp
LOCAL_SRC_FILES += point_cloud/src/Octree.cpp
LOCAL_SRC_FILES += point_cloud/src/Export.cpp
LOCAL_SRC_FILES += slamdunk/src/data.cpp
LOCAL_SRC_FILES += slamdunk/src/edge_se3_xyzpair.cpp
LOCAL_SRC_FILES += slamdunk/src/feature_tracker.cpp
LOCAL_SRC_FILES += slamdunk/src/graph_backend.cpp
LOCAL_SRC_FILES += slamdunk/src/graph_utils.cpp
LOCAL_SRC_FILES += slamdunk/src/slam_dunk.cpp
LOCAL_SRC_FILES += slamdunk/src/transformation_estimation.cpp
LOCAL_SRC_FILES += slamdunk/slamdunk_app.cpp
LOCAL_SRC_FILES += surf/surf.cpp
LOCAL_SRC_FILES += brisk/brisk.cpp
#LOCAL_SRC_FILES += ../DepthSenseSDK/sample/DSSDKLiteSample.cpp
LOCAL_SRC_FILES += kalman/src/MatrixUtils.cpp
LOCAL_SRC_FILES += kalman/src/Derivative.cpp
LOCAL_SRC_FILES += kalman/src/KalmanData.cpp
LOCAL_SRC_FILES += kalman/src/PositionLinearKalmanFilter.cpp
LOCAL_SRC_FILES += kalman/src/OrientationExtendedKalmanFilter.cpp
LOCAL_SRC_FILES += kalman/src/BasicExtendedKalmanFilterHandler.cpp
LOCAL_SRC_FILES += kalman/KalmanMain.cpp

LOCAL_ARM_NEON := true
LOCAL_STATIC_LIBRARIES += cpufeatures
LOCAL_LDLIBS += -llog -ldl -lz

#LOCAL_LDLIBS += -L$(LOCAL_PATH)/../SensorKinect/Platform/Android/libs/armeabi-v7a -llog -ldl -lOpenNI
#LOCAL_LDLIBS += -L$(LOCAL_PATH)/../Sensor-Unstable/Platform/Android/libs/armeabi-v7a -lOpenNI
LOCAL_STATIC_LIBRARIES += boost_system boost_filesystem boost_timer boost_chrono boost_date_time
LOCAL_SHARED_LIBRARIES += g2o_core g2o_types_slam3d g2o_stuff g2o_solver_pcg
LOCAL_SHARED_LIBRARIES += libusb openni2
LOCAL_SHARED_LIBRARIES += tango
#LOCAL_LDLIBS += $(LOCAL_PATH)/../DepthSenseSDK/lib/libDepthSensePlugins.so.1
#LOCAL_LDLIBS += -L$(LOCAL_PATH)/../DepthSenseSDK/lib -lturbojpeg -lDepthSense

LOCAL_C_INCLUDES += utils/include
LOCAL_C_INCLUDES += export/include
LOCAL_C_INCLUDES += surf
LOCAL_C_INCLUDES += brisk
LOCAL_C_INCLUDES += slamdunk/include
LOCAL_C_INCLUDES += kalman/include

#LOCAL_C_INCLUDES += $(LOCAL_PATH)/../OpenNI/Include
#LOCAL_C_INCLUDES += $(LOCAL_PATH)/../OpenNI-Unstable/Include
#LOCAL_C_INCLUDES += $(LOCAL_PATH)/../SensorKinect/Include
#LOCAL_C_INCLUDES += $(LOCAL_PATH)/../Sensor-Unstable/Include
LOCAL_C_INCLUDES += $(LOCAL_PATH)/../OpenNI-2-Structure/Include
LOCAL_C_INCLUDES += $(LOCAL_PATH)/../TangoSDK_Furud_C/include
LOCAL_C_INCLUDES += $(LOCAL_PATH)/../eigen
LOCAL_C_INCLUDES += $(LOCAL_PATH)/../g2o
LOCAL_C_INCLUDES += $(LOCAL_PATH)/../g2o/EXTERNAL/csparse
LOCAL_C_INCLUDES += $(LOCAL_PATH)/../BoostAndroid/build/include/boost-1_53
#LOCAL_C_INCLUDES += $(LOCAL_PATH)/../DepthSenseSDK/include
#LOCAL_C_INCLUDES += $(LOCAL_PATH)/../DepthSenseSDK/sample

include $(BUILD_SHARED_LIBRARY)

$(call import-module,android/cpufeatures)