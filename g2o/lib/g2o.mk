USER_LOCAL_PATH:=$(LOCAL_PATH)

LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := g2o_core
LOCAL_SRC_FILES := libg2o_core.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := g2o_types_slam3d
LOCAL_SRC_FILES := libg2o_types_slam3d.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := g2o_stuff
LOCAL_SRC_FILES := libg2o_stuff.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := g2o_solver_pcg
LOCAL_SRC_FILES := libg2o_solver_pcg.so
include $(PREBUILT_SHARED_LIBRARY)

LOCAL_PATH:=$(USER_LOCAL_PATH)