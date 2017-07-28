/**
 * Native Senz3DGrabber methods.
 */

#include <jni.h>
#include <android/log.h>

#include <stdio.h>
#include <stdlib.h>
#include <math.h>

#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/features2d/features2d.hpp>

#include <XnCppWrapper.h>

#include <DSSDKLiteSample.hxx>

using namespace std;
using namespace cv;
using namespace xn;

#define  LOG_TAG	"Senz3DGrabber"
#define  LOGI(...)	__android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define  LOGE(...)	__android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

extern "C"
{
	JNIEXPORT jboolean JNICALL Java_it_unibo_slam_input_Senz3DGrabber_initNative(JNIEnv *env, jobject obj);
	JNIEXPORT void JNICALL Java_it_unibo_slam_input_Senz3DGrabber_getResolutionNative(JNIEnv *env, jobject obj,
			jintArray resolution);
	JNIEXPORT jfloat JNICALL Java_it_unibo_slam_input_Senz3DGrabber_getMaxDepthNative(JNIEnv *env, jobject obj);
	JNIEXPORT void JNICALL Java_it_unibo_slam_input_Senz3DGrabber_getIntrinsicParamsNative(JNIEnv *env, jobject obj,
				jfloatArray focal, jfloatArray center);
	JNIEXPORT void JNICALL Java_it_unibo_slam_input_Senz3DGrabber_runNativeLoop(JNIEnv *env, jobject obj);
	JNIEXPORT void JNICALL Java_it_unibo_slam_input_Senz3DGrabber_exitNativeLoop(JNIEnv *env, jobject obj);
	JNIEXPORT jboolean JNICALL Java_it_unibo_slam_input_Senz3DGrabber_getFrame(JNIEnv *env, jobject obj,
			jdoubleArray timestamp, jfloatArray depthFrame, jbyteArray colorFrame);
}

// Global variables accessible only within this source file
namespace
{
	ConsoleDemo demo;

	bool initializedSenz = false;
	bool runningSenz = false;
}

JNIEXPORT jboolean JNICALL Java_it_unibo_slam_input_Senz3DGrabber_initNative(JNIEnv *env, jobject obj)
{
	if (initializedSenz)
		return true;

	setenv("DEPTHSENSESDK_DIR", "/system", 1);

	try
	{
		demo.init();
	}
	catch (std::exception& exc)
	{
		std::stringstream sse;
		sse << "Eccezione -> " << exc.what();
		LOGE("%s", sse.str().data());
		return false;
	}

	initializedSenz = true;

	return true;
}

JNIEXPORT void JNICALL Java_it_unibo_slam_input_Senz3DGrabber_getResolutionNative(JNIEnv *env, jobject obj,
		jintArray resolution)
{
	if (!initializedSenz)
	{
		jint *tempResolution = env->GetIntArrayElements(resolution, NULL);
		tempResolution[0] = 0;
		tempResolution[1] = 0;
		env->ReleaseIntArrayElements(resolution, tempResolution, 0);
	}

	jint *tempResolution = env->GetIntArrayElements(resolution, NULL);
	tempResolution[0] = demo.getResolutionX();
	tempResolution[1] = demo.getResolutionY();
	env->ReleaseIntArrayElements(resolution, tempResolution, 0);
}

JNIEXPORT jfloat JNICALL Java_it_unibo_slam_input_Senz3DGrabber_getMaxDepthNative(JNIEnv *env, jobject obj)
{
	if (!initializedSenz)
		return -1;

	return demo.getMaxDepth();
}

JNIEXPORT void JNICALL Java_it_unibo_slam_input_Senz3DGrabber_getIntrinsicParamsNative(JNIEnv *env, jobject obj,
		jfloatArray focal, jfloatArray center)
{
	if (!initializedSenz)
	{
		jfloat *tempFocal = env->GetFloatArrayElements(focal, NULL);
		tempFocal[0] = 0;
		tempFocal[1] = 0;
		env->ReleaseFloatArrayElements(focal, tempFocal, 0);

		jfloat *tempCenter = env->GetFloatArrayElements(center, NULL);
		tempCenter[0] = 0;
		tempCenter[1] = 0;
		env->ReleaseFloatArrayElements(center, tempCenter, 0);
	}

	jfloat *tempFocal = env->GetFloatArrayElements(focal, NULL);
	tempFocal[0] = demo.getFocalX();
	tempFocal[1] = demo.getFocalY();
	env->ReleaseFloatArrayElements(focal, tempFocal, 0);

	jfloat *tempCenter = env->GetFloatArrayElements(center, NULL);
	tempCenter[0] = demo.getCenterX();
	tempCenter[1] = demo.getCenterY();
	env->ReleaseFloatArrayElements(center, tempCenter, 0);
}

JNIEXPORT void JNICALL Java_it_unibo_slam_input_Senz3DGrabber_runNativeLoop(JNIEnv *env, jobject obj)
{
	if (!initializedSenz)
		return;

	runningSenz = true;

	demo.run();
}

JNIEXPORT void JNICALL Java_it_unibo_slam_input_Senz3DGrabber_exitNativeLoop(JNIEnv *env, jobject obj)
{
	if (!initializedSenz || !runningSenz)
		return;

	demo.quit();
}

JNIEXPORT jboolean JNICALL Java_it_unibo_slam_input_Senz3DGrabber_getFrame(JNIEnv *env, jobject obj,
		jdoubleArray timestamp, jfloatArray depthFrame, jbyteArray colorFrame)
{
	if (!initializedSenz || !runningSenz)
		return false;

	demo.lockMutex();
	demo.waitForData();

	float *depthArray = demo.getDepthMap();
	unsigned char *colorArray = demo.getColorMap();

	jfloat *tempDepth = env->GetFloatArrayElements(depthFrame, NULL);
	bool doubleRow = false;
	for (int i = 0; i < 640 * 480; i++, tempDepth++, depthArray++)//(int i = 0; i < 640 * 480; i +=2, tempDepth += 2, depthArray++)
	{
		/*if (i % 640 == 0)
		{
			if (doubleRow)
			{
				depthArray -= 320;
				doubleRow = false;
			}
			else
				doubleRow = true;
		}*/

		*tempDepth = *depthArray;
		//*(tempDepth + 1) = *depthArray;
	}

	jbyte *tempColor = env->GetByteArrayElements(colorFrame, NULL);
	for (int i = 0; i < 640 * 480 * 3; i++, tempColor++, colorArray++)
		*tempColor = (jbyte)*colorArray;

	jdouble *tempTimestamp = env->GetDoubleArrayElements(timestamp, NULL);
	*tempTimestamp = demo.getTimestamp();

	demo.releaseMutex();

	env->ReleaseFloatArrayElements(depthFrame, tempDepth, 0);
	env->ReleaseByteArrayElements(colorFrame, tempColor, 0);
	env->ReleaseDoubleArrayElements(timestamp, tempTimestamp, 0);

	return true;
}
