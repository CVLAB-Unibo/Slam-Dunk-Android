/**
 * Native StructureGrabber methods.
 */

#include <jni.h>
#include <android/log.h>

#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <pthread.h>

#include <fstream>

#include <opencv/cv.h>
#include "opencv2/highgui/highgui.hpp"

#include <tango_client_api.h>

#include <boost/scoped_array.hpp>

#include <Eigen/Core>

using namespace std;
using namespace openni;
using namespace cv;
using namespace Eigen;
using namespace boost;

#define  LOG_TAG	"TangoGrabber"
#define  LOGI(...)	__android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define  LOGE(...)	__android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// Global variables accessible only within this source file
namespace
{
	pthread_mutex_t mutexStructure;

	float *depthPtr;
	uint8_t *bgrPtr;
	double timestampVal;
	int resolutionX, resolutionY;
	int totalResolutionStructure;
	VideoStream depthStream, irStream;

	bool initializedStructure = false;
}

class ImageCallback : public VideoStream::NewFrameListener
{
	public:
		void onNewFrame(VideoStream& stream)
		{
			pthread_mutex_lock(&mutexStructure);

			stream.readFrame(&frameRef);

			DepthPixel* depthPtrTemp;
			Grayscale16Pixel* irPtrTemp;
			switch (frameRef.getVideoMode().getPixelFormat())
			{
				// Structure depth format
				case PIXEL_FORMAT_DEPTH_1_MM:
					depthPtrTemp = (DepthPixel*) frameRef.getData();
					memcpy(depthPtr.get(), depthPtrTemp, sizeof(DepthPixel) * totalResolutionStructure);
					timestampVal = ((double)getMillisecondsSinceEpoch() * 0.001);
					break;

				// Structure ir format
				case PIXEL_FORMAT_GRAY16:
					irPtrTemp = (Grayscale16Pixel*) frameRef.getData();
					memcpy(irPtr.get(), irPtrTemp, sizeof(Grayscale16Pixel) * totalResolutionStructure);
					timestampVal = ((double)getMillisecondsSinceEpoch() * 0.001);
					break;

				default:
					LOGI("Unsupported pixel format");
					break;
			}

			pthread_mutex_unlock(&mutexStructure);
		}

	private:
		VideoFrameRef frameRef;
};

// Global variables accessible only within this source file
namespace
{
	Device device;
	ImageCallback listenerDepth, listenerIR;

	Mat rgbIntrinsics;
	Mat rgbDistortion;
	Mat irIntrinsics;
	Mat irDistortion;
	Mat translationVectorRgbIr;
	Mat rotationMatrixRgbIr;
	Mat translationVectorIrRgb;
	Mat rotationMatrixIrRgb;

	Matrix3f eigenIntrinsicRgb;

	Matrix3f eigenRotationIrRgb;
	Vector3f eigenTranslationIrRgb;

	scoped_array<int> mapIr;
	scoped_array<int> revMapIr;
	scoped_array<int> mapRgb;
	scoped_array<int> revMapRgb;
	scoped_array<bool> maskIr;
	scoped_array<bool> maskRgb;

	// Coordinates = RotIrToRgb * [(x - cx) / fx, (y - cy) / fy, 1]
	scoped_array<Vector3f> cachedCoordinates;
}

extern "C"
{
	JNIEXPORT jboolean JNICALL Java_it_unibo_slam_input_StructureGrabber_initNative(JNIEnv *env, jobject obj);
	JNIEXPORT void JNICALL Java_it_unibo_slam_input_StructureGrabber_getResolutionNative(JNIEnv *env, jobject obj,
			jintArray resolution);
	JNIEXPORT jfloat JNICALL Java_it_unibo_slam_input_StructureGrabber_getMaxDepthNative(JNIEnv *env, jobject obj);
	JNIEXPORT void JNICALL Java_it_unibo_slam_input_StructureGrabber_getIntrinsicParamsNative(JNIEnv *env, jobject obj,
			jfloatArray focal, jfloatArray center);
	JNIEXPORT jboolean JNICALL Java_it_unibo_slam_input_StructureGrabber_getFrame(JNIEnv *env, jobject obj,
			jdoubleArray timestamp, jfloatArray depthFrame);
}

void initStandandAndReverseUndistortMapping(Mat cameraMatrix, Mat distortionCoefficients,
											Size imageSize, int *mapXY, int *revMapXY,
											bool *mask = NULL)
{
	float centerX = cameraMatrix.at<float>(0, 2), centerY = cameraMatrix.at<float>(1, 2);
	float focalX = cameraMatrix.at<float>(0, 0), focalY = cameraMatrix.at<float>(1, 1);

	double k1 = distortionCoefficients.at<float>(0, 0);
	double k2 = distortionCoefficients.at<float>(1, 0);
	double p1 = distortionCoefficients.at<float>(2, 0);
	double p2 = distortionCoefficients.at<float>(3, 0);
	double k3 = (distortionCoefficients.rows > 4) ? distortionCoefficients.at<float>(4, 0) : 0;
	double k4 = (distortionCoefficients.rows > 5) ? distortionCoefficients.at<float>(5, 0) : 0;
	double k5 = (distortionCoefficients.rows > 6) ? distortionCoefficients.at<float>(6, 0) : 0;
	double k6 = (distortionCoefficients.rows > 7) ? distortionCoefficients.at<float>(7, 0) : 0;

	for (int i = 0; i < imageSize.width * imageSize.height; i++)
	{
		mapXY[i] = -1;
		revMapXY[i] = -1;
		if (mask != NULL)
			mask[i] = false;
	}

	float x, y, x2, y2, m2xy, r2, kr;
	int id = 0, indexBackProject = 0, u, v;
	for (float ey = 0; ey < imageSize.height; ey++)
	{
		y = (ey - centerY) / focalY;
		y2 = y*y;

		for (float ex = 0; ex < imageSize.width; ex++, id++)
		{
			x = (ex - centerX) / focalX;
			x2 = x*x;
			r2 = x2 + y2;
			m2xy = 2*x*y;

			kr = (1 + ((k3 * r2 + k2) * r2 + k1) * r2) / (1 + ((k6 * r2 + k5) * r2 + k4) * r2);
			u = (int)round(focalX * (x * kr + p1 * m2xy + p2 * (r2 + 2 * x2)) + centerX);
			v = (int)round(focalY * (y * kr + p1 * (r2 + 2 * y2) + p2 * m2xy) + centerY);

			if (u >= 0 && u < imageSize.width && v >= 0 && v < imageSize.height)
			{
				indexBackProject = u + v * imageSize.width;

				if (revMapXY[indexBackProject] == -1)
				{
					mapXY[id] = indexBackProject;
					revMapXY[indexBackProject] = id;
					if (mask != NULL)
						mask[id] = true;
				}
			}
		}
	}
}

void initializeMappingVariables(string calibrationFile, int width, int height)
{
	FileStorage storage(calibrationFile, FileStorage::READ);

	storage["RgbIntrinsics"] >> rgbIntrinsics;
	storage["RgbDistortion"] >> rgbDistortion;
	storage["IrIntrinsics"] >> irIntrinsics;
	storage["IrDistortion"] >> irDistortion;
	storage["T"] >> translationVectorRgbIr;
	storage["R"] >> rotationMatrixRgbIr;

	storage.release();

	rotationMatrixRgbIr.convertTo(rotationMatrixRgbIr, CV_32FC1);
	translationVectorRgbIr.convertTo(translationVectorRgbIr, CV_32FC1);
	rgbIntrinsics.convertTo(rgbIntrinsics, CV_32FC1);
	irIntrinsics.convertTo(irIntrinsics, CV_32FC1);
	rgbDistortion.convertTo(rgbDistortion, CV_32FC1);
	irDistortion.convertTo(irDistortion, CV_32FC1);

	rotationMatrixIrRgb = rotationMatrixRgbIr.t();
	translationVectorIrRgb = -1.0F * rotationMatrixIrRgb * translationVectorRgbIr;

	eigenIntrinsicRgb << 	rgbIntrinsics.at<float>(0, 0), rgbIntrinsics.at<float>(0, 1), rgbIntrinsics.at<float>(0, 2),
							rgbIntrinsics.at<float>(1, 0), rgbIntrinsics.at<float>(1, 1), rgbIntrinsics.at<float>(1, 2),
							rgbIntrinsics.at<float>(2, 0), rgbIntrinsics.at<float>(2, 1), rgbIntrinsics.at<float>(2, 2);

	eigenRotationIrRgb <<	rotationMatrixIrRgb.at<float>(0, 0), rotationMatrixIrRgb.at<float>(0, 1), rotationMatrixIrRgb.at<float>(0, 2),
							rotationMatrixIrRgb.at<float>(1, 0), rotationMatrixIrRgb.at<float>(1, 1), rotationMatrixIrRgb.at<float>(1, 2),
							rotationMatrixIrRgb.at<float>(2, 0), rotationMatrixIrRgb.at<float>(2, 1), rotationMatrixIrRgb.at<float>(2, 2);
	eigenTranslationIrRgb = Vector3f(	translationVectorIrRgb.at<float>(0, 0),
										translationVectorIrRgb.at<float>(1, 0),
										translationVectorIrRgb.at<float>(2, 0));

	mapIr.reset(new int[width * height]);
	revMapIr.reset(new int[width * height]);
	maskIr.reset(new bool[width * height]);
	initStandandAndReverseUndistortMapping(irIntrinsics, irDistortion, Size(width, height), mapIr.get(), revMapIr.get(), maskIr.get());

	mapRgb.reset(new int[width * height]);
	revMapRgb.reset(new int[width * height]);
	maskRgb.reset(new bool[width * height]);
	initStandandAndReverseUndistortMapping(rgbIntrinsics, rgbDistortion, Size(width, height), mapRgb.get(), revMapRgb.get(), maskRgb.get());

	float irCenterX = irIntrinsics.at<float>(0, 2);
	float irCenterY = irIntrinsics.at<float>(1, 2);
	float irFocalX = irIntrinsics.at<float>(0, 0);
	float irFocalY = irIntrinsics.at<float>(1, 1);

	Vector3f tempCoord(0, 0, 1);
	cachedCoordinates.reset(new Vector3f[width * height]);
	for (float v = 0, id = 0; v < height; v++)
	{
		tempCoord[1] = (v - irCenterY) / irFocalY;

		for (float u = 0; u < width; u++, id++)
		{
			tempCoord[0] = (u - irCenterX) / irFocalX;

			cachedCoordinates[id] = eigenRotationIrRgb * tempCoord;
		}
	}
}

JNIEXPORT jboolean JNICALL Java_it_unibo_slam_input_StructureGrabber_initNative(JNIEnv *env, jobject obj)
{
	if (initializedStructure)
		return true;

	pthread_mutex_init(&mutexStructure, NULL);

	char cwd[1024];
	getcwd(cwd, 1024);

	chdir("/sdcard/StructureCalibrator/config");

	Status status = OpenNI::initialize();
	if (status != STATUS_OK)
	{
		LOGE("OpenNI init failed: %s", OpenNI::getExtendedError());
		return false;
	}

	status = device.open(ANY_DEVICE);
	if (status != STATUS_OK)
	{
		LOGE("Couldn't open the device: %s", OpenNI::getExtendedError());
		return false;
	}

	status = depthStream.create(device, SENSOR_DEPTH);
	if (status != STATUS_OK)
	{
		LOGE("Couldn't create the depth stream: %s", OpenNI::getExtendedError());
		return false;
	}

	/*status = irStream.create(device, SENSOR_IR);
	if (status != STATUS_OK)
	{
		LOGE("Couldn't create the infrared stream: %s", OpenNI::getExtendedError());
		return false;
	}*/

	resolutionX = depthStream.getVideoMode().getResolutionX();
	resolutionY = depthStream.getVideoMode().getResolutionY();
	totalResolutionStructure = resolutionX * resolutionY;
	depthPtr.reset(new DepthPixel[totalResolutionStructure]);
	irPtr.reset(new Grayscale16Pixel[totalResolutionStructure]);

	initializeMappingVariables("/sdcard/CalibData.xml", resolutionX, resolutionY);

	depthStream.start();
	depthStream.addNewFrameListener(&listenerDepth);

	/*irStream.start();
	irStream.addNewFrameListener(&listenerIR);*/

	chdir(cwd);

	initializedStructure = true;

	return true;
}

JNIEXPORT void JNICALL Java_it_unibo_slam_input_StructureGrabber_getResolutionNative(JNIEnv *env, jobject obj,
		jintArray resolution)
{
	if (!initializedStructure)
	{
		jint *tempResolution = env->GetIntArrayElements(resolution, NULL);
		tempResolution[0] = 0;
		tempResolution[1] = 0;
		env->ReleaseIntArrayElements(resolution, tempResolution, 0);
		return;
	}

	// Depth and image resolution must be equal
	jint *tempResolution = env->GetIntArrayElements(resolution, NULL);
	tempResolution[0] = depthStream.getVideoMode().getResolutionX();
	tempResolution[1] = depthStream.getVideoMode().getResolutionY();
	env->ReleaseIntArrayElements(resolution, tempResolution, 0);
}

JNIEXPORT jfloat JNICALL Java_it_unibo_slam_input_StructureGrabber_getMaxDepthNative(JNIEnv *env, jobject obj)
{
	if (!initializedStructure)
		return -1;

	return 10.0F;//TODO(jfloat)(depthStream.getMaxPixelValue() * 0.001F);
}

JNIEXPORT void JNICALL Java_it_unibo_slam_input_StructureGrabber_getIntrinsicParamsNative(JNIEnv *env, jobject obj,
		jfloatArray focal, jfloatArray center)
{
	jfloat *tempFocal = env->GetFloatArrayElements(focal, NULL);
	jfloat *tempCenter = env->GetFloatArrayElements(center, NULL);

	if (!initializedStructure)
	{
		tempFocal[0] = 0;
		tempFocal[1] = 0;

		tempCenter[0] = 0;
		tempCenter[1] = 0;
	}
	else
	{
		tempFocal[0] = irIntrinsics.at<float>(0, 0);
		tempFocal[1] = irIntrinsics.at<float>(1, 1);

		tempCenter[0] = irIntrinsics.at<float>(0, 2);
		tempCenter[1] = irIntrinsics.at<float>(1, 2);
	}

	env->ReleaseFloatArrayElements(focal, tempFocal, 0);
	env->ReleaseFloatArrayElements(center, tempCenter, 0);
}

void mapDepthToRgb(DepthPixel *depthIn, float *depthOut)
{
	float rgbCenterX = rgbIntrinsics.at<float>(0, 2);
	float rgbCenterY = rgbIntrinsics.at<float>(1, 2);
	float rgbFocalX = rgbIntrinsics.at<float>(0, 0);
	float rgbFocalY = rgbIntrinsics.at<float>(1, 1);

	int tempIndex = 0, tempU = 0, tempV = 0;
	Vector3f rgbCoord(0, 0, 0);
	float u, v, tempDepth;
	for (int id = 0; id < totalResolutionStructure; id++)
	{
		if (maskIr[id] && depthIn[mapIr[id]] > 0)
		{
			tempDepth = (float)depthIn[mapIr[id]] * 0.001F;
			rgbCoord = ((cachedCoordinates[mapIr[id]] * tempDepth) + eigenTranslationIrRgb);

			if (tempDepth > 0 && rgbCoord.z() > 0)
			{
				rgbCoord.x() = rgbCoord.x() * rgbFocalX + rgbCenterX * rgbCoord.z();
				rgbCoord.y() = rgbCoord.y() * rgbFocalY + rgbCenterY * rgbCoord.z();

				/*u = rgbCoord.x() / rgbCoord.z();
				v = rgbCoord.y() / rgbCoord.z();

				bool useHigherU = false, useHigherV = false;
				if (round(u) == ceil(u))
					useHigherU = true;
				if (round(v) == ceil(v))
					useHigherV = true;*/

				/*for (int x = 0; x < 4; x++)
				{*/

					/*if (x == 0)
					{
						if (useHigherU)
							tempU = (int)u;
						else
							tempU = (int)u - 1;

						if (useHigherV)
							tempV = (int)ceil(v);
						else
							tempV = (int)v;
					}
					else if (x == 1)
					{
						if (useHigherU)
							tempU = (int)ceil(u);
						else
							tempU = (int)u;

						if (useHigherV)
							tempV = (int)ceil(v);
						else
							tempV = (int)v;
					}
					else if (x == 2)
					{
						if (useHigherU)
							tempU = (int)u;
						else
							tempU = (int)u - 1;

						if (useHigherV)
							tempV = (int)v;
						else
							tempV = (int)v - 1;
					}
					else if (x == 3)
					{
						if (useHigherU)
							tempU = (int)ceil(u);
						else
							tempU = (int)u;

						if (useHigherV)
							tempV = (int)v;
						else
							tempV = (int)v - 1;
					}*/

					tempU = (int)round(rgbCoord.x() / rgbCoord.z());
					tempV = (int)round(rgbCoord.y() / rgbCoord.z());

					if (tempU >= 0 && tempU < resolutionX && tempV >= 0 && tempV < resolutionY)
					{
						tempIndex = tempU + tempV * resolutionX;

						if (maskRgb[revMapRgb[tempIndex]])
						{
							if (depthOut[revMapRgb[tempIndex]] == 0)
								depthOut[revMapRgb[tempIndex]] = tempDepth;
							else if (depthOut[revMapRgb[tempIndex]] > 0 && depthOut[revMapRgb[tempIndex]] > tempDepth)
								depthOut[revMapRgb[tempIndex]] = tempDepth;
						}
					}
				//}
			}
		}
	}
}

JNIEXPORT jboolean JNICALL Java_it_unibo_slam_input_StructureGrabber_getFrame(JNIEnv *env, jobject obj,
		jdoubleArray timestamp, jfloatArray depthFrame)
{
	if (!initializedStructure)
		return false;

	pthread_mutex_lock(&mutexStructure);

	/*Mat depth(480, 640, CV_16UC1, depthPtr.get());
	Mat ir(480, 640, CV_16UC1, irPtr.get());
	imwrite("sdcard/depthProva.png", depth * ((float)65535 / (float)3500));
	imwrite("sdcard/irProva.png", ir * ((float)65535 / (float)1023));*/

	jfloat *tempDepth = env->GetFloatArrayElements(depthFrame, NULL);
	/*for (int i = 0; i < totalResolutionStructure; i++)
		tempDepth[i] = irPtr[i] * 10.0F / 1023.0F;*/

	/*for (int i = 0; i < totalResolutionStructure; i++)
		tempDepth[i] = depthPtr[i] * 0.001F;*/
	mapDepthToRgb(depthPtr.get(), tempDepth);

	jdouble *tempTimestamp = env->GetDoubleArrayElements(timestamp, NULL);

	tempTimestamp[0] = (jdouble)timestampVal;

	env->ReleaseFloatArrayElements(depthFrame, tempDepth, 0);
	env->ReleaseDoubleArrayElements(timestamp, tempTimestamp, 0);

	pthread_mutex_unlock(&mutexStructure);

	return true;
}
