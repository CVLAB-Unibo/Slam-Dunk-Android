#include <jni.h>
#include <android/log.h>

#include <stdio.h>
#include <stdlib.h>
#include <math.h>

#include "include/slam_dunk.h"

#include <fstream>

#include <boost/timer/timer.hpp>
#include <boost/scoped_ptr.hpp>

#include <Eigen/StdVector>

#define  LOG_TAG	"slamdunk_app"
#define  LOGI(...)	__android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define  LOGE(...)	__android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

//TODO #include "kalman/include/KalmanGlobal.h" ed altri

typedef std::vector<std::pair<double, Eigen::Isometry3d>, Eigen::aligned_allocator< std::pair<double, Eigen::Isometry3d> > > StampedPoseVector;

extern "C"
{
	JNIEXPORT void JNICALL Java_it_unibo_slam_main_SlamDunkNative_init(JNIEnv *env, jobject obj,
			jfloat fx, jfloat fy, jfloat cx, jfloat cy, jint cols, jint rows,
			jint rbaRings, jfloat keyframeOverlapping, jboolean tryLoopInference,
			jfloat activeWindowLength, jboolean debugAlgorithm);
	JNIEXPORT jint JNICALL Java_it_unibo_slam_main_SlamDunkNative_execute(JNIEnv *env, jobject obj,
			jdouble timestamp, jlong colorImagePtr, jlong depthImagePtr, jdoubleArray estimatedPose);
	JNIEXPORT jint JNICALL Java_it_unibo_slam_main_SlamDunkNative_executeTracking(JNIEnv *env, jobject obj,
			jdouble timestamp, jlong colorImagePtr, jlong depthImagePtr, jdoubleArray estimatedPose);
	JNIEXPORT jint JNICALL Java_it_unibo_slam_main_SlamDunkNative_executeOptimization(JNIEnv *env, jobject obj,
			jdoubleArray estimatedPose);
	JNIEXPORT jint JNICALL Java_it_unibo_slam_main_SlamDunkNative_getMovedFramesNum(JNIEnv *env, jobject obj);
	JNIEXPORT void JNICALL Java_it_unibo_slam_main_SlamDunkNative_getMovedFrames(JNIEnv *env, jobject obj,
			jdoubleArray timestamps, jdoubleArray isometries);
	JNIEXPORT jfloat JNICALL Java_it_unibo_slam_main_SlamDunkNative_getResidual(JNIEnv *env, jobject obj);
}

// Global variables accessible only within this source file
namespace
{
	boost::scoped_ptr<slamdunk::SlamDunk> slam;
}

JNIEXPORT void JNICALL Java_it_unibo_slam_main_SlamDunkNative_init(JNIEnv *env, jobject obj,
		jfloat fx, jfloat fy, jfloat cx, jfloat cy, jint cols, jint rows,
		jint rbaRings, jfloat keyframeOverlapping, jboolean tryLoopInference,
		jfloat activeWindowLength, jboolean debugAlgorithm)
{
	Eigen::Matrix3f inverseKCam = Eigen::Matrix3f::Identity();
	inverseKCam(0,0) = 1.0F / fx;
	inverseKCam(1,1) = 1.0F / fy;
	inverseKCam(0,2) = cx * (-1.0F / fx);
	inverseKCam(1,2) = cy * (-1.0F / fy);

	slamdunk::SlamDunkParams sdParams = slamdunk::SlamDunkParams();
	slamdunk::FeatureTrackerParams ftParams = slamdunk::FeatureTrackerParams();
	ftParams.outlier_rejection.reset(new slamdunk::RANSAC(true, 0.05));
	ftParams.active_win_length = activeWindowLength;
	sdParams.tracker.reset(new slamdunk::FeatureTracker(inverseKCam, cols, rows, ftParams));
	sdParams.rba_rings = rbaRings;
	sdParams.kf_overlapping = keyframeOverlapping;
	sdParams.try_loop_inference = (bool)(tryLoopInference != JNI_FALSE);
	//TODO add debug?

	//slam = new slamdunk::SlamDunk(inverseKCam, sdParams);
	slam.reset(new slamdunk::SlamDunk(inverseKCam, sdParams));
}

JNIEXPORT jint JNICALL Java_it_unibo_slam_main_SlamDunkNative_execute(JNIEnv *env, jobject obj,
		jdouble timestamp, jlong colorImagePtr, jlong depthImagePtr, jdoubleArray estimatedPose)
{
	double tempEstimatedPose[16];
	jdouble* elements = env->GetDoubleArrayElements(estimatedPose, NULL);
	for (int i = 0; i < 16; i++)
		tempEstimatedPose[i] = elements[i];

	cv::Mat& colorImage = *(cv::Mat*)colorImagePtr;
	cv::Mat_<float>& depthImage = *(cv::Mat_<float>*)depthImagePtr;

	slamdunk::RGBDFrame frame;
	frame.m_color_image = colorImage;
	frame.m_depth_image = depthImage;
	frame.m_timestamp = timestamp;

	Eigen::Isometry3d estimatedPoseEigen(Eigen::Matrix4d::Map(tempEstimatedPose));

	int result = (*slam)(frame, estimatedPoseEigen);

	for (int i = 0; i < 16; i++)
		elements[i] = estimatedPoseEigen.matrix().data()[i];

	env->ReleaseDoubleArrayElements(estimatedPose, elements, 0);

	return result;
}

JNIEXPORT jint JNICALL Java_it_unibo_slam_main_SlamDunkNative_executeTracking(JNIEnv *env, jobject obj,
		jdouble timestamp, jlong colorImagePtr, jlong depthImagePtr, jdoubleArray estimatedPose)
{
	double tempEstimatedPose[16];
	jdouble* elements = env->GetDoubleArrayElements(estimatedPose, NULL);
	for (int i = 0; i < 16; i++)
		tempEstimatedPose[i] = elements[i];

	cv::Mat& colorImage = *(cv::Mat*)colorImagePtr;
	cv::Mat_<float>& depthImage = *(cv::Mat_<float>*)depthImagePtr;

	slamdunk::RGBDFrame frame;
	frame.m_color_image = colorImage;
	frame.m_depth_image = depthImage;
	frame.m_timestamp = timestamp;

	Eigen::Isometry3d estimatedPoseEigen(Eigen::Matrix4d::Map(tempEstimatedPose));

	int result = slam->executeTracking(frame, estimatedPoseEigen);

	for (int i = 0; i < 16; i++)
		elements[i] = estimatedPoseEigen.matrix().data()[i];

	env->ReleaseDoubleArrayElements(estimatedPose, elements, 0);

	return result;
}

JNIEXPORT jint JNICALL Java_it_unibo_slam_main_SlamDunkNative_executeOptimization(JNIEnv *env, jobject obj,
		jdoubleArray estimatedPose)
{
	double tempEstimatedPose[16];
	jdouble* elements = env->GetDoubleArrayElements(estimatedPose, NULL);
	for (int i = 0; i < 16; i++)
		tempEstimatedPose[i] = elements[i];

	Eigen::Isometry3d estimatedPoseEigen(Eigen::Matrix4d::Map(tempEstimatedPose));

	int result = slam->executeOptimization(estimatedPoseEigen);

	for (int i = 0; i < 16; i++)
		elements[i] = estimatedPoseEigen.matrix().data()[i];

	env->ReleaseDoubleArrayElements(estimatedPose, elements, 0);

	return result;
}

JNIEXPORT jint JNICALL Java_it_unibo_slam_main_SlamDunkNative_getMovedFramesNum(JNIEnv *env, jobject obj)
{
	return slam->getMovedFrames().size();
}

JNIEXPORT void JNICALL Java_it_unibo_slam_main_SlamDunkNative_getMovedFrames(JNIEnv *env, jobject obj,
		jdoubleArray timestamps, jdoubleArray isometries)
{
	jdouble* timestampsElements = env->GetDoubleArrayElements(timestamps, NULL);
	jdouble* isometriesElements = env->GetDoubleArrayElements(isometries, NULL);

	StampedPoseVector movedFrames = slam->getMovedFrames();
	int size = movedFrames.size();

	for (int i = 0, k = 0; i < size; i++, k += 16)
	{
		timestampsElements[i] = movedFrames[i].first;
		const double *data = movedFrames[i].second.data();
		for (int j = 0; j < 16; j++)
			isometriesElements[j + k] = data[j];
	}

	env->ReleaseDoubleArrayElements(timestamps, timestampsElements, 0);
	env->ReleaseDoubleArrayElements(isometries, isometriesElements, 0);
}

JNIEXPORT jfloat JNICALL Java_it_unibo_slam_main_SlamDunkNative_getResidual(JNIEnv *env, jobject obj)
{
	return slam->getResidual();
}
