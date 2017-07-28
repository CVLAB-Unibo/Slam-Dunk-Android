#include <jni.h>

#include <stdio.h>
#include <stdlib.h>
#include <math.h>

#include <Eigen/Geometry>

#include <android/log.h>

#include "kalman/include/KalmanGlobal.h"
#include "kalman/include/KalmanFilterHandler.h"
#include "kalman/include/BasicExtendedKalmanFilterHandler.h"

#define  LOG_TAG	"Kalman"
#define  LOGI(...)	__android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define  LOGE(...)	__android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

using namespace kalman;
using namespace Eigen;

extern "C"
{
	JNIEXPORT jboolean JNICALL Java_it_unibo_slam_kalman_KalmanGlobal_initializeKalmanNative(JNIEnv *env, jobject obj,
			jint kalmanType, jfloat accelDeltaT, jfloat gyroDeltaT, jfloatArray gravity);
	JNIEXPORT void JNICALL Java_it_unibo_slam_kalman_KalmanGlobal_handleDataNative(JNIEnv *env, jobject obj,
			jint dataType, jfloatArray acceleration, jfloatArray angularVelocity, jfloatArray position,
			jfloatArray orientation);
	JNIEXPORT void JNICALL Java_it_unibo_slam_kalman_KalmanGlobal_getFilteredPoseNative(JNIEnv *env, jobject obj,
			jfloatArray pose);
	JNIEXPORT void JNICALL Java_it_unibo_slam_kalman_KalmanGlobal_setVisualObservationNoise(JNIEnv *env, jobject obj,
			jfloat noiseValue);
}

// Kalman types.
enum
{
	BASIC_KALMAN = 0,
	INDIRECT_KALMAN = 1
};

JNIEXPORT jboolean JNICALL Java_it_unibo_slam_kalman_KalmanGlobal_initializeKalmanNative(JNIEnv *env, jobject obj,
		jint kalmanType, jfloat accelDeltaT, jfloat gyroDeltaT, jfloatArray gravity)
{
	//TODO Cambiare in base al tipo di kalman! per ora inizializzo il basic

	jfloat tempGravity[3];
	env->GetFloatArrayRegion(gravity, 0, 3, tempGravity);
	Vector3f gravityVec(tempGravity[0], tempGravity[1], tempGravity[2]);

	bool success = false;

	if (kalmanType == BASIC_KALMAN)
	{
		KalmanFilterHandler *handler = new BasicExtendedKalmanFilterHandler(accelDeltaT, gyroDeltaT, gravityVec);
		success = KalmanGlobal::getInstance().setHandlerPtr(handler);
		if (!success)
			delete handler;
	}

	return success;
}

JNIEXPORT void JNICALL Java_it_unibo_slam_kalman_KalmanGlobal_handleDataNative(JNIEnv *env, jobject obj,
		jint dataType, jfloatArray acceleration, jfloatArray angularVelocity, jfloatArray position,
		jfloatArray orientation)
{
	jfloat tempAcceleration[3];
	env->GetFloatArrayRegion(acceleration, 0, 3, tempAcceleration);
	Vector3f accelerationVec(tempAcceleration[0], tempAcceleration[1], tempAcceleration[2]);

	jfloat tempAngularVelocity[3];
	env->GetFloatArrayRegion(angularVelocity, 0, 3, tempAngularVelocity);
	Vector3f angularVelocityVec(tempAngularVelocity[0], tempAngularVelocity[1], tempAngularVelocity[2]);

	jfloat tempPosition[3];
	env->GetFloatArrayRegion(position, 0, 3, tempPosition);
	Vector3f positionVec(tempPosition[0], tempPosition[1], tempPosition[2]);

	jfloat tempOrientation[9];
	env->GetFloatArrayRegion(orientation, 0, 9, tempOrientation);
	Matrix3f orientationMat;
	orientationMat << 	tempOrientation[0], tempOrientation[3], tempOrientation[6],
						tempOrientation[1], tempOrientation[4], tempOrientation[7],
						tempOrientation[2], tempOrientation[5], tempOrientation[8];

	KalmanData data;
	data.dataType = dataType;
	data.acceleration = accelerationVec;
	data.angularVelocity = angularVelocityVec;
	data.position = positionVec;
	data.orientation = orientationMat;

	KalmanGlobal::getInstance().getHandlerPtr()->handleData(data);
}

JNIEXPORT void JNICALL Java_it_unibo_slam_kalman_KalmanGlobal_getFilteredPoseNative(JNIEnv *env, jobject obj,
		jfloatArray pose)
{
	jfloat* elements = env->GetFloatArrayElements(pose, NULL);

	Isometry3f poseEigen = KalmanGlobal::getInstance().getHandlerPtr()->getCurrentPose();

	for (int i = 0; i < 16; i++)
		elements[i] = poseEigen.matrix().data()[i];

	env->ReleaseFloatArrayElements(pose, elements, 0);
}

JNIEXPORT void JNICALL Java_it_unibo_slam_kalman_KalmanGlobal_setVisualObservationNoise(JNIEnv *env, jobject obj,
		jfloat noiseValue)
{
	KalmanGlobal::getInstance().getHandlerPtr()->setVisualObservationNoise(noiseValue);
}
