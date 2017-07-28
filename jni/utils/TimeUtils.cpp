#include <jni.h>

#include <sys/time.h>

#include "utils/include/TimeUtils.h"

unsigned long long getMillisecondsSinceEpoch()
{
	struct timeval tv;
	gettimeofday(&tv, NULL);

	unsigned long long millisecondsSinceEpoch = (unsigned long long)(tv.tv_sec) * 1000 +
												(unsigned long long)(tv.tv_usec) / 1000;

	return millisecondsSinceEpoch;
}

extern "C"
{
	JNIEXPORT jdouble JNICALL Java_it_unibo_slam_utils_TimeUtils_getTimestampSeconds(JNIEnv *env, jobject obj);
}

JNIEXPORT jdouble JNICALL Java_it_unibo_slam_utils_TimeUtils_getTimestampSeconds(JNIEnv *env, jobject obj)
{
	return ((double)getMillisecondsSinceEpoch() * 0.001);
}
