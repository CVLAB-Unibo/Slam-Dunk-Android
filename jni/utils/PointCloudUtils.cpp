#include <jni.h>

#include <stdio.h>
#include <stdlib.h>
#include <math.h>

#include <Eigen/Geometry>

#include "point_cloud/include/PointCloudTransform.h"

extern "C"
{
	JNIEXPORT void JNICALL Java_it_unibo_slam_gui_opengl_model_PointCloudStatic_reprojectPointCloudVertices(JNIEnv *env, jobject obj,
			jfloatArray transform, jint pointArrayLength, jfloatArray pointsInOut);
}

JNIEXPORT void JNICALL Java_it_unibo_slam_gui_opengl_model_PointCloudStatic_reprojectPointCloudVertices(JNIEnv *env, jobject obj,
		jfloatArray transform, jint pointArrayLength, jfloatArray pointsInOut)
{
	float tempTransform[16];
	env->GetFloatArrayRegion(transform, 0, 16, tempTransform);
	Eigen::Matrix4f transformEigen(Eigen::Matrix4f::Map(tempTransform));

	float *tempPointsInOut = env->GetFloatArrayElements(pointsInOut, NULL);

	reprojectPointCloud(tempPointsInOut, tempPointsInOut, pointArrayLength, transformEigen);

	env->ReleaseFloatArrayElements(pointsInOut, tempPointsInOut, 0);
}
