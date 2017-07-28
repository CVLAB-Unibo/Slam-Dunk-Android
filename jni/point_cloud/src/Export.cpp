#include <jni.h>

#include <stdio.h>
#include <stdlib.h>
#include <math.h>

#include <Eigen/Geometry>

#include "point_cloud/include/OrientedBoundingBox.h"
#include "point_cloud/include/Octree.h"
#include "point_cloud/include/PointCloudTransform.h"

#include <android/log.h>

#define  LOG_TAG	"Export"
#define  LOGI(...)	__android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define  LOGE(...)	__android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

extern "C"
{
	JNIEXPORT jint JNICALL Java_it_unibo_slam_export_PointCloudExporter_generatePointCloudToExport(JNIEnv *env, jobject obj,
			jfloatArray pointsIn, jbyteArray colorsIn, jint pointArrayLength, jfloat resolution);
	JNIEXPORT void JNICALL Java_it_unibo_slam_export_PointCloudExporter_getGeneratedPointCloud(JNIEnv *env, jobject obj,
			jfloatArray pointsOut, jbyteArray colorsOut);
	JNIEXPORT void JNICALL Java_it_unibo_slam_export_PointCloudExporter_getBoundingBoxMinAndMax(JNIEnv *env, jobject obj,
			jfloatArray min, jfloatArray max);
}

//TODO Meglio accorpare i metodi e ritornare come oggetto un istanza di ExportData che contenga i dati
// Global variables accessible only within this source file
namespace
{
	oct::Octree::PointList pointList;
	oct::Octree::ColorList colorList;
	float pointMin[3];
	float pointMax[3];
}

JNIEXPORT jint JNICALL Java_it_unibo_slam_export_PointCloudExporter_generatePointCloudToExport(JNIEnv *env, jobject obj,
		jfloatArray pointsIn, jbyteArray colorsIn, jint pointArrayLength, jfloat resolution)
{
	float *tempPointsIn = new float[pointArrayLength];
	env->GetFloatArrayRegion(pointsIn, 0, pointArrayLength, tempPointsIn);

	jbyte *tempColorsInSigned = new jbyte[pointArrayLength];
	env->GetByteArrayRegion(colorsIn, 0, pointArrayLength, tempColorsInSigned);
	unsigned char *tempColorsInUnsigned = reinterpret_cast<unsigned char*>(tempColorsInSigned);

	obb::OrientedBoundingBox orientedBoundingBox = obb::OrientedBoundingBox(tempPointsIn, pointArrayLength, true);
	Eigen::Vector3f min = orientedBoundingBox.getMinPoint();
	pointMin[0] = min[0];	pointMin[1] = min[1];	pointMin[2] = min[2];
	Eigen::Vector3f max = orientedBoundingBox.getMaxPoint();
	pointMax[0] = max[0];	pointMax[1] = max[1];	pointMax[2] = max[2];

	oct::Octree octree = oct::Octree(tempPointsIn, tempColorsInUnsigned, pointArrayLength, min, max, resolution);
	octree.getPoints(pointList, colorList);

	delete [] tempPointsIn;
	delete [] tempColorsInSigned;

	return (pointList.size() * 3);
}

JNIEXPORT void JNICALL Java_it_unibo_slam_export_PointCloudExporter_getGeneratedPointCloud(JNIEnv *env, jobject obj,
		jfloatArray pointsOut, jbyteArray colorsOut)
{
	if (pointList.size() == 0 || colorList.size() == 0)
		return;

	int pointArrayLength = pointList.size() * 3;

	float *tempPointsOut = env->GetFloatArrayElements(pointsOut, NULL);

	jbyte *tempColorsOutSigned = env->GetByteArrayElements(colorsOut, NULL);
	unsigned char *tempColorsOutUnsigned = reinterpret_cast<unsigned char*>(tempColorsOutSigned);

	oct::Octree::PointList::iterator itP = pointList.begin();
	oct::Octree::ColorList::iterator itC = colorList.begin();
	for (int i = 0; itP != pointList.end(); itP++, itC++, i += 3)
	{
		tempPointsOut[i] = (*itP)[0];
		tempPointsOut[i + 1] = (*itP)[1];
		tempPointsOut[i + 2] = (*itP)[2];

		tempColorsOutUnsigned[i] = (*itC)[0];
		tempColorsOutUnsigned[i + 1] = (*itC)[1];
		tempColorsOutUnsigned[i + 2] = (*itC)[2];
	}

	env->ReleaseFloatArrayElements(pointsOut, tempPointsOut, 0);
	tempColorsOutSigned = reinterpret_cast<jbyte*>(tempColorsOutUnsigned);

	env->ReleaseByteArrayElements(colorsOut, tempColorsOutSigned, 0);

	pointList.clear();
	colorList.clear();
}

JNIEXPORT void JNICALL Java_it_unibo_slam_export_PointCloudExporter_getBoundingBoxMinAndMax(JNIEnv *env, jobject obj,
		jfloatArray min, jfloatArray max)
{
	float *tempMin = env->GetFloatArrayElements(min, NULL);
	float *tempMax = env->GetFloatArrayElements(max, NULL);

	for (int i = 0; i < 3; i++)
	{
		tempMin[i] = pointMin[i];
		tempMax[i] = pointMax[i];
	}

	env->ReleaseFloatArrayElements(min, tempMin, 0);
	env->ReleaseFloatArrayElements(max, tempMax, 0);
}
