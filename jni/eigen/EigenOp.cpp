#include <jni.h>
#include <android/log.h>

#include <stdio.h>
#include <stdlib.h>
#include <math.h>

#include <Eigen/Geometry>
#include <Eigen/SVD>
#include <Eigen/LU> // Required for determinant computation

using namespace std;

#define  LOG_TAG	"EigenOp"
#define  LOGI(...)	__android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define  LOGE(...)	__android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

extern "C"
{
	JNIEXPORT jdoubleArray JNICALL Java_it_unibo_slam_sampleconsensus_RANSAC_calculateCurrentTransformation(JNIEnv *env, jobject obj,
			jdoubleArray ref1, jdoubleArray ref2, jdoubleArray ref3, jdoubleArray query1, jdoubleArray query2, jdoubleArray query3);
	JNIEXPORT jdoubleArray JNICALL Java_it_unibo_slam_sampleconsensus_abstracts_SampleConsensus_calculateMeanTransformation(JNIEnv *env, jobject obj,
			jdoubleArray H, jdoubleArray meanReference, jdoubleArray meanQuery);
	JNIEXPORT jdoubleArray JNICALL Java_it_unibo_slam_tracker_FeatureTracker_calculateIncrementalPose(JNIEnv *env, jobject obj,
			jdoubleArray lastTrackedPose, jdoubleArray activeWindowPose);
	JNIEXPORT jdoubleArray JNICALL Java_it_unibo_slam_graph_GraphBackend_calculateRelativeTransformation(JNIEnv *env, jobject obj,
			jdoubleArray to, jdoubleArray from);
	JNIEXPORT jdoubleArray JNICALL Java_it_unibo_slam_graph_GraphBackend_calculateFrameTransformation(JNIEnv *env, jobject obj,
			jdoubleArray pivotToWorld, jdoubleArray poseFromMap);
}

JNIEXPORT jdoubleArray JNICALL Java_it_unibo_slam_sampleconsensus_RANSAC_calculateCurrentTransformation(JNIEnv *env, jobject obj,
		jdoubleArray ref1, jdoubleArray ref2, jdoubleArray ref3, jdoubleArray query1, jdoubleArray query2, jdoubleArray query3)
{
	Eigen::Matrix3d referenceMatrix, queryMatrix;
	Eigen::Isometry3d currentTransformation = Eigen::Isometry3d::Identity();

	jdoubleArray currentTransformationArray;
	currentTransformationArray = env->NewDoubleArray(16);
	double *tempCurrentTransformationArray;
	double tempRef1[3], tempRef2[3], tempRef3[3];
	double tempQuery1[3], tempQuery2[3], tempQuery3[3];

	env->GetDoubleArrayRegion(ref1, 0, 3, tempRef1);
	env->GetDoubleArrayRegion(ref2, 0, 3, tempRef2);
	env->GetDoubleArrayRegion(ref3, 0, 3, tempRef3);
	env->GetDoubleArrayRegion(query1, 0, 3, tempQuery1);
	env->GetDoubleArrayRegion(query2, 0, 3, tempQuery2);
	env->GetDoubleArrayRegion(query3, 0, 3, tempQuery3);

	referenceMatrix.row(0) = Eigen::Vector3d(tempRef1[0], tempRef1[1], tempRef1[2]);
	referenceMatrix.row(1) = Eigen::Vector3d(tempRef2[0], tempRef2[1], tempRef2[2]);
	referenceMatrix.row(2) = Eigen::Vector3d(tempRef3[0], tempRef3[1], tempRef3[2]);
	queryMatrix.col(0) = Eigen::Vector3d(tempQuery1[0], tempQuery1[1], tempQuery1[2]);
	queryMatrix.col(1) = Eigen::Vector3d(tempQuery2[0], tempQuery2[1], tempQuery2[2]);
	queryMatrix.col(2) = Eigen::Vector3d(tempQuery3[0], tempQuery3[1], tempQuery3[2]);

	// Compute centroids
	const Eigen::Vector3d cRef = (referenceMatrix.row(0) + referenceMatrix.row(1) + referenceMatrix.row(2)) * (0.33333333333333333333);
	const Eigen::Vector3d cQry = (queryMatrix.col(0) + queryMatrix.col(1) + queryMatrix.col(2)) * (0.33333333333333333333);

	referenceMatrix.row(0) -= cRef;
	referenceMatrix.row(1) -= cRef;
	referenceMatrix.row(2) -= cRef;
	queryMatrix.col(0) -= cQry;
	queryMatrix.col(1) -= cQry;
	queryMatrix.col(2) -= cQry;
	const Eigen::Matrix3d H = queryMatrix * referenceMatrix;

	// SVD
	Eigen::JacobiSVD<Eigen::Matrix3d> svd(H, Eigen::ComputeFullU | Eigen::ComputeFullV);
	Eigen::Matrix3d V = svd.matrixV();
	Eigen::Matrix3d R = V * svd.matrixU().transpose();
	if (R.determinant() < 0.0)
	{
		V.col(2) = V.col(2) * -1.0;
		R.noalias() = V * svd.matrixU().transpose();
	}

	currentTransformation = Eigen::Isometry3d(R);
	currentTransformation.translation() = cRef - R * cQry;

	tempCurrentTransformationArray = currentTransformation.data();
	env->SetDoubleArrayRegion(currentTransformationArray, 0, 16, tempCurrentTransformationArray);
	return currentTransformationArray;
}

JNIEXPORT jdoubleArray JNICALL Java_it_unibo_slam_sampleconsensus_abstracts_SampleConsensus_calculateMeanTransformation(JNIEnv *env, jobject obj,
		jdoubleArray H, jdoubleArray meanReference, jdoubleArray meanQuery)
{
	jdoubleArray currentTransformationArray;
	currentTransformationArray = env->NewDoubleArray(16);
	double *tempCurrentTransformationArray;
	double tempH[9];
	double tempMeanReference[3];
	double tempMeanQuery[3];

	env->GetDoubleArrayRegion(H, 0, 9, tempH);
	env->GetDoubleArrayRegion(meanReference, 0, 3, tempMeanReference);
	env->GetDoubleArrayRegion(meanQuery, 0, 3, tempMeanQuery);

	const Eigen::Map<Eigen::Matrix3d> HMap(tempH);
	const Eigen::Vector3d meanReferenceEigen(tempMeanReference[0], tempMeanReference[1], tempMeanReference[2]);
	const Eigen::Vector3d meanQueryEigen(tempMeanQuery[0], tempMeanQuery[1], tempMeanQuery[2]);

	// SVD
	Eigen::JacobiSVD<Eigen::Matrix3d> svd(HMap, Eigen::ComputeFullU | Eigen::ComputeFullV);
	Eigen::Matrix3d V = svd.matrixV();
	Eigen::Matrix3d R = V * svd.matrixU().transpose();
	if (R.determinant() < 0.0)
	{
		V.col(2) = V.col(2) * -1.0;
		R.noalias() = V * svd.matrixU().transpose();
	}

	Eigen::Isometry3d currentTransformation = Eigen::Isometry3d::Identity();
	currentTransformation.rotate(R);
	currentTransformation.translation() = meanReferenceEigen - R * meanQueryEigen;

	tempCurrentTransformationArray = currentTransformation.data();
	env->SetDoubleArrayRegion(currentTransformationArray, 0, 16, tempCurrentTransformationArray);
	return currentTransformationArray;
}

JNIEXPORT jdoubleArray JNICALL Java_it_unibo_slam_tracker_FeatureTracker_calculateIncrementalPose(JNIEnv *env, jobject obj,
		jdoubleArray lastTrackedPose, jdoubleArray activeWindowPose)
{
	jdoubleArray incrementalPoseArray;
	incrementalPoseArray = env->NewDoubleArray(16);
	double *tempIncrementalPoseArray;
	double tempLastTrackedPose[16];
	double tempActiveWindowPose[16];

	env->GetDoubleArrayRegion(lastTrackedPose, 0, 16, tempLastTrackedPose);
	env->GetDoubleArrayRegion(activeWindowPose, 0, 16, tempActiveWindowPose);

	const Eigen::Isometry3d lastTrackedPoseEigen(Eigen::Matrix4d::Map(tempLastTrackedPose));
	const Eigen::Isometry3d activeWindowPoseEigen(Eigen::Matrix4d::Map(tempActiveWindowPose));

	Eigen::Isometry3d incrementalPose = lastTrackedPoseEigen.inverse() * activeWindowPoseEigen;

	tempIncrementalPoseArray = incrementalPose.data();
	env->SetDoubleArrayRegion(incrementalPoseArray, 0, 16, tempIncrementalPoseArray);
	return incrementalPoseArray;
}

JNIEXPORT jdoubleArray JNICALL Java_it_unibo_slam_graph_GraphBackend_calculateRelativeTransformation(JNIEnv *env, jobject obj,
		jdoubleArray to, jdoubleArray from)
{
	jdoubleArray relativeTransformationArray;
	relativeTransformationArray = env->NewDoubleArray(16);
	double *tempRelativeTransformationArray;
	double tempTo[16];
	double tempFrom[16];

	env->GetDoubleArrayRegion(to, 0, 16, tempTo);
	env->GetDoubleArrayRegion(from, 0, 16, tempFrom);

	const Eigen::Isometry3d toEigen(Eigen::Matrix4d::Map(tempTo));
	const Eigen::Isometry3d fromEigen(Eigen::Matrix4d::Map(tempFrom));

	Eigen::Isometry3d relativeTransformation = toEigen.inverse() * fromEigen;

	tempRelativeTransformationArray = relativeTransformation.data();
	env->SetDoubleArrayRegion(relativeTransformationArray, 0, 16, tempRelativeTransformationArray);
	return relativeTransformationArray;
}

JNIEXPORT jdoubleArray JNICALL Java_it_unibo_slam_graph_GraphBackend_calculateFrameTransformation(JNIEnv *env, jobject obj,
		jdoubleArray pivotToWorld, jdoubleArray poseFromMap)
{
	jdoubleArray frameTransformationArray;
	frameTransformationArray = env->NewDoubleArray(16);
	double *tempFrameTransformationArray;
	double tempPivotToWorld[16];
	double tempPoseFromMap[16];

	env->GetDoubleArrayRegion(pivotToWorld, 0, 16, tempPivotToWorld);
	env->GetDoubleArrayRegion(poseFromMap, 0, 16, tempPoseFromMap);

	const Eigen::Isometry3d pivotToWorldEigen(Eigen::Matrix4d::Map(tempPivotToWorld));
	const Eigen::Isometry3d poseFromMapEigen(Eigen::Matrix4d::Map(tempPoseFromMap));

	Eigen::Isometry3d frameTransformation = pivotToWorldEigen * poseFromMapEigen.inverse();

	tempFrameTransformationArray = frameTransformation.data();
	env->SetDoubleArrayRegion(frameTransformationArray, 0, 16, tempFrameTransformationArray);
	return frameTransformationArray;
}
