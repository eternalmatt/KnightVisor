LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := native
LOCAL_SRC_FILES := native.c
LOCAL_CFLAGS    += -O3 -fopenmp

include $(BUILD_SHARED_LIBRARY)
