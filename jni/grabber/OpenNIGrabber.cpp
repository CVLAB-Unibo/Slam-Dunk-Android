/**
 * Native OpenNIGrabber methods.
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

using namespace std;
using namespace cv;
using namespace xn;

#define  LOG_TAG	"OpenNIGrabber"
#define  LOGI(...)	__android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define  LOGE(...)	__android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

extern "C"
{
	JNIEXPORT jboolean JNICALL Java_it_unibo_slam_input_OpenNIGrabber_initNative(JNIEnv *env, jobject obj);
	JNIEXPORT jboolean JNICALL Java_it_unibo_slam_input_OpenNIGrabber_getFrame(JNIEnv *env, jobject obj,
			jdoubleArray timestamp, jfloatArray depthFrame, jbyteArray colorFrame);
	JNIEXPORT void JNICALL Java_it_unibo_slam_input_OpenNIGrabber_getResolutionNative(JNIEnv *env, jobject obj,
			jintArray resolution);
	JNIEXPORT jfloat JNICALL Java_it_unibo_slam_input_OpenNIGrabber_getMaxDepthNative(JNIEnv *env, jobject obj);
	//TODO remove?
	/*JNIEXPORT void JNICALL Java_it_unibo_slam_input_OpenNIGrabber_getIntrinsicParamsNative(JNIEnv *env, jobject obj,
			jfloatArray focal, jfloatArray center);*/
}

// Global variables accessible only within this source file
namespace
{
	Context context;
	DepthGenerator depthGenerator;
	ImageGenerator imageGenerator;
	DepthMetaData depthMD;
	ImageMetaData imageMD;

	bool initializedOpenNI15 = false;
}

JNIEXPORT jboolean JNICALL Java_it_unibo_slam_input_OpenNIGrabber_initNative(JNIEnv *env, jobject obj)
{
	if (initializedOpenNI15)
		return true;

	XnStatus status = XN_STATUS_OK;
	ScriptNode scriptNode;
	EnumerationErrors errors;

	status = context.InitFromXmlFile("/data/ni/SamplesConfig.xml", scriptNode, &errors);
	if (status != XN_STATUS_OK)
	{
		LOGE("Context init failed: %s", xnGetStatusString(status));
		return false;
	}

	status = context.FindExistingNode(XN_NODE_TYPE_DEPTH, depthGenerator);
	if (status != XN_STATUS_OK)
	{
		LOGE("Depth generator creation failed: %s", xnGetStatusString(status));
		return false;
	}

	status = context.FindExistingNode(XN_NODE_TYPE_IMAGE, imageGenerator);
	if (status != XN_STATUS_OK)
	{
		LOGE("Image generator creation failed: %s", xnGetStatusString(status));
		return false;
	}

	context.StartGeneratingAll();

	if (depthGenerator.IsCapabilitySupported(XN_CAPABILITY_FRAME_SYNC) &&
		imageGenerator.IsCapabilitySupported(XN_CAPABILITY_FRAME_SYNC) &&
		depthGenerator.GetFrameSyncCap().CanFrameSyncWith(imageGenerator))
	{
		depthGenerator.GetFrameSyncCap().FrameSyncWith(imageGenerator);
	}

	initializedOpenNI15 = true;

	return true;
}

JNIEXPORT void JNICALL Java_it_unibo_slam_input_OpenNIGrabber_getResolutionNative(JNIEnv *env, jobject obj,
		jintArray resolution)
{
	if (!initializedOpenNI15)
	{
		jint *tempResolution = env->GetIntArrayElements(resolution, NULL);
		tempResolution[0] = 0;
		tempResolution[1] = 0;
		env->ReleaseIntArrayElements(resolution, tempResolution, 0);
	}

	// Depth and image resolution must be equal
	jint *tempResolution = env->GetIntArrayElements(resolution, NULL);
	XnMapOutputMode depthResOM;
	depthGenerator.GetMapOutputMode(depthResOM);
	tempResolution[0] = depthResOM.nXRes;
	tempResolution[1] = depthResOM.nYRes;
	env->ReleaseIntArrayElements(resolution, tempResolution, 0);
}

JNIEXPORT jfloat JNICALL Java_it_unibo_slam_input_OpenNIGrabber_getMaxDepthNative(JNIEnv *env, jobject obj)
{
	if (!initializedOpenNI15)
		return -1;

	return (jfloat)(depthGenerator.GetDeviceMaxDepth() * 0.001F);
}

//TODO remove?
/*JNIEXPORT void JNICALL Java_it_unibo_slam_input_OpenNIGrabber_getIntrinsicParamsNative(JNIEnv *env, jobject obj,
		jfloatArray focal, jfloatArray center)
{
	if (!initializedOpenNI15)
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

	XnMapOutputMode depthResOM;
	depthGenerator.GetMapOutputMode(depthResOM);
	float resX = (float)depthResOM.nXRes;
	float resY = (float)depthResOM.nYRes;

	// Focal calculation using the field of view
	jfloat *tempFocal = env->GetFloatArrayElements(focal, NULL);
	XnFieldOfView fov;
	depthGenerator.GetFieldOfView(fov);
	tempFocal[0] = resX / (float)(2 * tan(fov.fHFOV / 2));
	tempFocal[1] = resY / (float)(2 * tan(fov.fVFOV / 2));
	env->ReleaseFloatArrayElements(focal, tempFocal, 0);

	// Default center used
	jfloat *tempCenter = env->GetFloatArrayElements(center, NULL);
	tempCenter[0] = (resX - 1.0F) / 2.0F;
	tempCenter[1] = (resY - 1.0F) / 2.0F;
	env->ReleaseFloatArrayElements(center, tempCenter, 0);
}*/

JNIEXPORT jboolean JNICALL Java_it_unibo_slam_input_OpenNIGrabber_getFrame(JNIEnv *env, jobject obj,
		jdoubleArray timestamp, jfloatArray depthFrame, jbyteArray colorFrame)
{
	if (!initializedOpenNI15)
		return false;

	XnStatus status = XN_STATUS_OK;

	status = context.WaitAndUpdateAll();
	if (status != XN_STATUS_OK)
		return false;

	/*status = context.WaitOneUpdateAll(depthGenerator);
	if (status != XN_STATUS_OK)
	{
		LOGE("Read failed: %s", xnGetStatusString(status));

		FILE* file = fopen("/sdcard/testFile.txt","w+");

		if (file != NULL)
		{
			fprintf(file, "Read depth failed: %s", xnGetStatusString(status));
			fflush(file);
			fclose(file);
		}

		return false;
	}*/

	depthGenerator.GetMetaData(depthMD);
	imageGenerator.GetMetaData(imageMD);

	jfloat *tempDepth = env->GetFloatArrayElements(depthFrame, NULL);
	const XnDepthPixel *depth = depthMD.Data();

	for (int y = 0; y < depthMD.YRes(); y++)
		for (int x = 0; x < depthMD.XRes(); x++, depth++, tempDepth++)
			*tempDepth = (*depth) * 0.001F;

	/*status = context.WaitOneUpdateAll(imageGenerator);
	if (status != XN_STATUS_OK)
	{
		LOGE("Read failed: %s", xnGetStatusString(status));

		FILE* file = fopen("/sdcard/testFile.txt","w+");

		if (file != NULL)
		{
			fprintf(file, "Read image failed: %s", xnGetStatusString(status));
			fflush(file);
			fclose(file);
		}

		return false;
	}*/

	//imageGenerator.GetMetaData(imageMD);

	jbyte *tempColor = env->GetByteArrayElements(colorFrame, NULL);
	const XnRGB24Pixel *color = imageMD.RGB24Data();

	for (int y = 0; y < imageMD.YRes(); y++)
	{
		for (int x = 0; x < imageMD.XRes(); x++, color++, tempColor++)
		{
			*tempColor = color->nBlue;
			tempColor++;
			*tempColor = color->nGreen;
			tempColor++;
			*tempColor = color->nRed;
		}
	}

	jdouble *tempTimestamp = env->GetDoubleArrayElements(timestamp, NULL);

	*tempTimestamp = (jdouble)imageMD.Timestamp();

	env->ReleaseFloatArrayElements(depthFrame, tempDepth, 0);
	env->ReleaseByteArrayElements(colorFrame, tempColor, 0);
	env->ReleaseDoubleArrayElements(timestamp, tempTimestamp, 0);

	return true;
}
