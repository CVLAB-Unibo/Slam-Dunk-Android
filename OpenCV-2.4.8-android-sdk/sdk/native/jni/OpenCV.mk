# In order to compile your application under cygwin
# you might need to define NDK_USE_CYGPATH=1 before calling the ndk-build

USER_LOCAL_PATH:=$(LOCAL_PATH)
LOCAL_PATH:=$(subst ?,,$(firstword ?$(subst \, ,$(subst /, ,$(call my-dir)))))

OPENCV_TARGET_ARCH_ABI:=$(TARGET_ARCH_ABI)
OPENCV_THIS_DIR:=$(patsubst $(LOCAL_PATH)\\%,%,$(patsubst $(LOCAL_PATH)/%,%,$(call my-dir)))
OPENCV_MK_DIR:=$(dir $(lastword $(MAKEFILE_LIST)))
OPENCV_LIBS_DIR:=$(OPENCV_THIS_DIR)/../libs/$(OPENCV_TARGET_ARCH_ABI)
OPENCV_3RDPARTY_LIBS_DIR:=$(OPENCV_THIS_DIR)/../3rdparty/libs/$(OPENCV_TARGET_ARCH_ABI)
OPENCV_BASEDIR:=
OPENCV_LOCAL_C_INCLUDES:="$(LOCAL_PATH)/$(OPENCV_THIS_DIR)/include/opencv" "$(LOCAL_PATH)/$(OPENCV_THIS_DIR)/include"
OPENCV_MODULES:=contrib legacy ocl ml stitching objdetect ts videostab video photo calib3d features2d highgui imgproc flann androidcamera core

OPENCV_HAVE_GPU_MODULE=off
OPENCV_USE_GPU_MODULE:=

ifeq ($(TARGET_ARCH_ABI),armeabi-v7a)
    ifeq ($(OPENCV_HAVE_GPU_MODULE),on)
        ifneq ($(CUDA_TOOLKIT_DIR),)
            OPENCV_USE_GPU_MODULE:=on
        endif
    endif
endif

CUDA_RUNTIME_LIBS:=cufft npps nppi nppc cudart

ifeq ($(OPENCV_LIB_TYPE),)
    OPENCV_LIB_TYPE:=SHARED
endif

ifeq ($(OPENCV_LIB_TYPE),SHARED)
    OPENCV_LIBS:=java
    OPENCV_LIB_TYPE:=SHARED
else
    OPENCV_LIBS:=$(OPENCV_MODULES)
    OPENCV_LIB_TYPE:=STATIC
endif

ifeq ($(OPENCV_LIB_TYPE),SHARED)
    OPENCV_3RDPARTY_COMPONENTS:=
    OPENCV_EXTRA_COMPONENTS:=
else
    ifeq ($(TARGET_ARCH_ABI),armeabi-v7a)
        OPENCV_3RDPARTY_COMPONENTS:=tbb libjpeg libpng libtiff libjasper IlmImf
        OPENCV_EXTRA_COMPONENTS:=c log m dl z
    endif
    ifeq ($(TARGET_ARCH_ABI),x86)
        OPENCV_3RDPARTY_COMPONENTS:=tbb libjpeg libpng libtiff libjasper IlmImf
        OPENCV_EXTRA_COMPONENTS:=c log m dl z
    endif
    ifeq ($(TARGET_ARCH_ABI),armeabi)
        OPENCV_3RDPARTY_COMPONENTS:= libjpeg libpng libtiff libjasper IlmImf
        OPENCV_EXTRA_COMPONENTS:=c log m dl z
    endif
    ifeq ($(TARGET_ARCH_ABI),mips)
        OPENCV_3RDPARTY_COMPONENTS:=tbb libjpeg libpng libtiff libjasper IlmImf
        OPENCV_EXTRA_COMPONENTS:=c log m dl z
    endif
endif

ifeq (${OPENCV_CAMERA_MODULES},on)
    ifeq ($(TARGET_ARCH_ABI),armeabi)
        OPENCV_CAMERA_MODULES:= native_camera_r2.2.0 native_camera_r2.3.3 native_camera_r3.0.1 native_camera_r4.0.0 native_camera_r4.0.3 native_camera_r4.1.1 native_camera_r4.2.0 native_camera_r4.3.0 native_camera_r4.4.0
    endif
    ifeq ($(TARGET_ARCH_ABI),armeabi-v7a)
        OPENCV_CAMERA_MODULES:= native_camera_r2.2.0 native_camera_r2.3.3 native_camera_r3.0.1 native_camera_r4.0.0 native_camera_r4.0.3 native_camera_r4.1.1 native_camera_r4.2.0 native_camera_r4.3.0 native_camera_r4.4.0
    endif
    ifeq ($(TARGET_ARCH_ABI),x86)
        OPENCV_CAMERA_MODULES:= native_camera_r2.3.3 native_camera_r3.0.1 native_camera_r4.0.3 native_camera_r4.1.1 native_camera_r4.2.0 native_camera_r4.3.0 native_camera_r4.4.0
    endif
    ifeq ($(TARGET_ARCH_ABI),mips)
        OPENCV_CAMERA_MODULES:= native_camera_r4.0.3 native_camera_r4.1.1 native_camera_r4.2.0 native_camera_r4.3.0 native_camera_r4.4.0
    endif
else
    OPENCV_CAMERA_MODULES:=
endif

ifeq ($(OPENCV_LIB_TYPE),SHARED)
    OPENCV_LIB_SUFFIX:=so
else
    OPENCV_LIB_SUFFIX:=a
    OPENCV_INSTALL_MODULES:=on
endif

define add_opencv_module
    include $(CLEAR_VARS)
    LOCAL_MODULE:=opencv_$1
    LOCAL_SRC_FILES:=$(OPENCV_LIBS_DIR)/libopencv_$1.$(OPENCV_LIB_SUFFIX)
    include $(PREBUILT_$(OPENCV_LIB_TYPE)_LIBRARY)
endef

define add_opencv_3rdparty_component
    include $(CLEAR_VARS)
    LOCAL_MODULE:=$1
    LOCAL_SRC_FILES:=$(OPENCV_3RDPARTY_LIBS_DIR)/lib$1.a
    include $(PREBUILT_STATIC_LIBRARY)
endef

define add_opencv_camera_module
    include $(CLEAR_VARS)
    LOCAL_MODULE:=$1
    LOCAL_SRC_FILES:=$(OPENCV_LIBS_DIR)/lib$1.so
    include $(PREBUILT_SHARED_LIBRARY)
endef

ifeq ($(OPENCV_MK_$(OPENCV_TARGET_ARCH_ABI)_ALREADY_INCLUDED),)
    ifeq ($(OPENCV_INSTALL_MODULES),on)
        $(foreach module,$(OPENCV_LIBS),$(eval $(call add_opencv_module,$(module))))
    endif

    $(foreach module,$(OPENCV_3RDPARTY_COMPONENTS),$(eval $(call add_opencv_3rdparty_component,$(module))))
    $(foreach module,$(OPENCV_CAMERA_MODULES),$(eval $(call add_opencv_camera_module,$(module))))

    ifneq ($(OPENCV_BASEDIR),)
        OPENCV_LOCAL_C_INCLUDES += $(foreach mod, $(OPENCV_MODULES), $(OPENCV_BASEDIR)/modules/$(mod)/include)
        ifeq ($(OPENCV_USE_GPU_MODULE),on)
            OPENCV_LOCAL_C_INCLUDES += $(OPENCV_BASEDIR)/modules/gpu/include
        endif
    endif

    #turn off module installation to prevent their redefinition
    OPENCV_MK_$(OPENCV_TARGET_ARCH_ABI)_ALREADY_INCLUDED:=on
endif

ifeq ($(OPENCV_USE_GPU_MODULE),on)
    include $(CLEAR_VARS)
    LOCAL_MODULE:=opencv_gpu
    LOCAL_SRC_FILES:=$(OPENCV_LIBS_DIR)/libopencv_gpu.a
    include $(PREBUILT_STATIC_LIBRARY)
endif

ifeq ($(OPENCV_LOCAL_CFLAGS),)
    OPENCV_LOCAL_CFLAGS := -fPIC -DANDROID -fsigned-char
endif

include $(CLEAR_VARS)
LOCAL_C_INCLUDES += $(OPENCV_LOCAL_C_INCLUDES)
LOCAL_CFLAGS     += $(OPENCV_LOCAL_CFLAGS)

ifeq ($(OPENCV_USE_GPU_MODULE),on)
    LOCAL_C_INCLUDES += $(CUDA_TOOLKIT_DIR)/include
endif

ifeq ($(OPENCV_INSTALL_MODULES),on)
    LOCAL_$(OPENCV_LIB_TYPE)_LIBRARIES += $(foreach mod, $(OPENCV_LIBS), opencv_$(mod))
else
    LOCAL_LDLIBS += -L$(call host-path,$(LOCAL_PATH)/$(OPENCV_LIBS_DIR)) $(foreach lib, $(OPENCV_LIBS), -lopencv_$(lib))
endif

ifeq ($(OPENCV_LIB_TYPE),STATIC)
    LOCAL_STATIC_LIBRARIES += $(OPENCV_3RDPARTY_COMPONENTS)
endif

LOCAL_LDLIBS += $(foreach lib,$(OPENCV_EXTRA_COMPONENTS), -l$(lib))

ifeq ($(OPENCV_USE_GPU_MODULE),on)
    LOCAL_STATIC_LIBRARIES+=libopencv_gpu
    LOCAL_LDLIBS += -L$(CUDA_TOOLKIT_DIR)/lib $(foreach lib, $(CUDA_RUNTIME_LIBS), -l$(lib))
endif

#restore the LOCAL_PATH
LOCAL_PATH:=$(USER_LOCAL_PATH)
