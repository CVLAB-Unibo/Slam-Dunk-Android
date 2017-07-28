#include <jni.h>

#include <Eigen/Geometry>

#include <opencv/cv.h>
#include <highgui.h>

#include <android/log.h>

#define  LOG_TAG	"CameraPoseUtils"
#define  LOGI(...)	__android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define  LOGE(...)	__android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

using namespace std;
using namespace cv;
using namespace Eigen;

extern "C"
{
	JNIEXPORT jboolean JNICALL Java_it_unibo_slam_utils_CameraPoseUtils_estimateLocalObjectPoseNative(JNIEnv *env, jobject obj,
			jlong rgbImage, jint chessboardWidth, jint chessboardHeight, jdoubleArray translationVector, jdoubleArray rotationVector);

	JNIEXPORT void JNICALL Java_it_unibo_slam_utils_CameraPoseUtils_estimateWorldCameraPoseNative(JNIEnv *env, jobject obj,
			jdoubleArray worldTranslationVector, jdoubleArray worldRotationMatrix, jdoubleArray initialTranslationVector,
			jdoubleArray initialRotationMatrix, jdoubleArray finalTranslationVector, jdoubleArray finalRotationMatrix);
}

namespace
{
	bool initializedPoseVariables = false;

	Mat rgbIntrinsics;
	Mat rgbDistortion;
	Mat irIntrinsics;
	Mat irDistortion;
	Mat translationVectorRgbIr;
	Mat rotationMatrixRgbIr;
	Mat translationVectorIrRgb;
	Mat rotationMatrixIrRgb;
}

void calculateChessboardCorners(Size boardSize, float squareSize, vector<Point3f>& corners)
{
	corners.resize(0);

    for(int i = 0; i < boardSize.height; i++)
        for(int j = 0; j < boardSize.width; j++)
            corners.push_back(Point3f(float(j * squareSize), float(i * squareSize), 0));
}

JNIEXPORT jboolean JNICALL Java_it_unibo_slam_utils_CameraPoseUtils_estimateLocalObjectPoseNative(JNIEnv *env, jobject obj,
		jlong rgbImage, jint chessboardWidth, jint chessboardHeight, jdoubleArray translationVector, jdoubleArray rotationMatrix)
{
	if (!initializedPoseVariables)
	{
		FileStorage storage("/sdcard/CalibrationResults.xml", FileStorage::READ);

		storage["CameraMat1"] >> rgbIntrinsics;
		storage["DistorsionMat1"] >> rgbDistortion;
		storage["CameraMat2"] >> irIntrinsics;
		storage["DistorsionMat2"] >> irDistortion;
		storage["TranslationVec"] >> translationVectorRgbIr;
		storage["RotationMat"] >> rotationMatrixRgbIr;

		storage.release();

		initializedPoseVariables = true;
	}

	vector<Point2f> chessboardCorners;
	Mat rgbMat = *((Mat*)rgbImage);
	Mat grayscaleMat(rgbMat.rows, rgbMat.cols, CV_8U);
	cvtColor(rgbMat, grayscaleMat, CV_RGB2GRAY);

	bool patternFound = findChessboardCorners(grayscaleMat, Size(chessboardWidth, chessboardHeight), chessboardCorners);

	if (patternFound)
	{
		jdouble *translationVectorTemp = env->GetDoubleArrayElements(translationVector, NULL);
		jdouble *rotationMatrixTemp = env->GetDoubleArrayElements(rotationMatrix, NULL);

		cornerSubPix(grayscaleMat, chessboardCorners, Size(5, 5), Size(-1, -1), TermCriteria(CV_TERMCRIT_EPS + CV_TERMCRIT_ITER, 40, 0.001));

		vector<Point3f> objectPoints;
		float sizeSquareSide = 0.026F;
		//float sizeSquareSide = 0.050F;
		calculateChessboardCorners(Size(chessboardWidth, chessboardHeight), sizeSquareSide, objectPoints);

		Mat translationPnP = Mat::zeros(3, 1, CV_64F);
		Mat rotationPnP = Mat::zeros(3, 1, CV_64F);
		solvePnP(objectPoints, chessboardCorners, rgbIntrinsics, rgbDistortion, rotationPnP, translationPnP);

		translationVectorTemp[0] = translationPnP.at<double>(0, 0);
		translationVectorTemp[1] = translationPnP.at<double>(1, 0);
		translationVectorTemp[2] = translationPnP.at<double>(2, 0);

		Vector3d rotationVector;
		rotationVector.x() = rotationPnP.at<double>(0, 0);
		rotationVector.y() = rotationPnP.at<double>(1, 0);
		rotationVector.z() = rotationPnP.at<double>(2, 0);
		double rotationVectorNorm = rotationVector.norm();

		AngleAxisd angleAxisRotation = AngleAxisd(rotationVectorNorm, (rotationVectorNorm == 0) ? Vector3d(1, 0, 0) :
				(rotationVector / rotationVectorNorm).eval());

		Matrix3d rotationMatrixEigen;
		rotationMatrixEigen = angleAxisRotation;

		for (int i = 0; i < 9; i++)
			rotationMatrixTemp[i] = rotationMatrixEigen.data()[i];

		env->ReleaseDoubleArrayElements(translationVector, translationVectorTemp, 0);
		env->ReleaseDoubleArrayElements(rotationMatrix, rotationMatrixTemp, 0);
	}

	return patternFound;
}

JNIEXPORT void JNICALL Java_it_unibo_slam_utils_CameraPoseUtils_estimateWorldCameraPoseNative(JNIEnv *env, jobject obj,
		jdoubleArray worldTranslationVector, jdoubleArray worldRotationMatrix, jdoubleArray initialTranslationVector,
		jdoubleArray initialRotationMatrix, jdoubleArray finalTranslationVector, jdoubleArray finalRotationMatrix)
{
	jdouble worldTranslationVectorTemp[3];
	jdouble worldRotationMatrixTemp[9];
	jdouble initialTranslationVectorTemp[3];
	jdouble initialRotationMatrixTemp[9];

	env->GetDoubleArrayRegion(worldTranslationVector, 0, 3, worldTranslationVectorTemp);
	env->GetDoubleArrayRegion(worldRotationMatrix, 0, 9, worldRotationMatrixTemp);
	env->GetDoubleArrayRegion(initialTranslationVector, 0, 3, initialTranslationVectorTemp);
	env->GetDoubleArrayRegion(initialRotationMatrix, 0, 9, initialRotationMatrixTemp);

	jdouble *finalTranslationVectorTemp = env->GetDoubleArrayElements(finalTranslationVector, NULL);
	jdouble *finalRotationMatrixTemp = env->GetDoubleArrayElements(finalRotationMatrix, NULL);

	Vector3d worldTranslationVectorEigen(worldTranslationVectorTemp[0], worldTranslationVectorTemp[1], worldTranslationVectorTemp[2]);
	Matrix3d worldRotationMatrixEigen;
	worldRotationMatrixEigen << worldRotationMatrixTemp[0], worldRotationMatrixTemp[3], worldRotationMatrixTemp[6],
								worldRotationMatrixTemp[1], worldRotationMatrixTemp[4], worldRotationMatrixTemp[7],
								worldRotationMatrixTemp[2], worldRotationMatrixTemp[5], worldRotationMatrixTemp[8];
	Vector3d initialTranslationVectorEigen(initialTranslationVectorTemp[0], initialTranslationVectorTemp[1], initialTranslationVectorTemp[2]);
	Matrix3d initialRotationMatrixEigen;
	initialRotationMatrixEigen << 	initialRotationMatrixTemp[0], initialRotationMatrixTemp[3], initialRotationMatrixTemp[6],
									initialRotationMatrixTemp[1], initialRotationMatrixTemp[4], initialRotationMatrixTemp[7],
									initialRotationMatrixTemp[2], initialRotationMatrixTemp[5], initialRotationMatrixTemp[8];

	Matrix3d inverseInitialRotation = initialRotationMatrixEigen.transpose();
	Vector3d inverseInitialTranslation = -inverseInitialRotation * initialTranslationVectorEigen;

	Matrix3d finalRotationMatrixEigen = worldRotationMatrixEigen * inverseInitialRotation;
	Vector3d finalTranslationVectorEigen = worldRotationMatrixEigen * inverseInitialTranslation + worldTranslationVectorEigen;

	for (int i = 0; i < 9; i++)
		finalRotationMatrixTemp[i] = finalRotationMatrixEigen.data()[i];

	finalTranslationVectorTemp[0] = finalTranslationVectorEigen.x();
	finalTranslationVectorTemp[1] = finalTranslationVectorEigen.y();
	finalTranslationVectorTemp[2] = finalTranslationVectorEigen.z();

	env->ReleaseDoubleArrayElements(finalTranslationVector, finalTranslationVectorTemp, 0);
	env->ReleaseDoubleArrayElements(finalRotationMatrix, finalRotationMatrixTemp, 0);
}

