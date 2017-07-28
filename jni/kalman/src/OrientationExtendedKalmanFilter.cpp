#include "kalman/include/OrientationExtendedKalmanFilter.h"
#include "kalman/include/Derivative.h"

#include <android/log.h>

#define  LOG_TAG	"kalman_app"
#define  LOGI(...)	__android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define  LOGE(...)	__android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

using namespace kalman;

void OrientationExtendedKalmanFilter::init()
{
	statePriori.orientation = Vector3f(0, 0, 0);
	statePriori.angularVelocity = Vector3f(0, 0, 0);
	statePosteriori.orientation = Vector3f(0, 0, 0);
	statePosteriori.angularVelocity = Vector3f(0, 0, 0);

	// TODO tuning initialization
	errorCovariancePriori = Matrix<float, 6, 6>::Constant(0.1F);
	errorCovariancePosteriori = Matrix<float, 6, 6>::Constant(0.1F);

	// TODO initialize processNoiseCovarianceMatrix
	processNoiseCovarianceMatrix = Matrix<float, 6, 1>::Constant(0.0001).asDiagonal();

	observationMatrixInertial << Matrix3f::Zero(), Matrix3f::Identity();
	observationMatrixVisual << Matrix3f::Identity(), Matrix3f::Zero();
}

Vector3f OrientationExtendedKalmanFilter::getOrientation()
{
	return statePriori.orientation;
}

void OrientationExtendedKalmanFilter::setInertialObservationNoise(float noiseValue)
{
	observationNoiseCovarianceMatrixInertial = Vector3f::Constant(noiseValue).asDiagonal();
}

void OrientationExtendedKalmanFilter::setVisualObservationNoise(float noiseValue)
{
	observationNoiseCovarianceMatrixVisual = Vector3f::Constant(noiseValue).asDiagonal();
}

void OrientationExtendedKalmanFilter::predict()
{
	OrientationDerivativeType derivativeType = ORIENTATION_X;
	Vector3f orientationX = numericalDerivativeOrientation(	statePriori.orientation, statePriori.angularVelocity,
															derivativeType, DELTA_DERIVATIVE_DEFAULT, deltaT);
	derivativeType = ORIENTATION_Y;
	Vector3f orientationY = numericalDerivativeOrientation(	statePriori.orientation, statePriori.angularVelocity,
															derivativeType, DELTA_DERIVATIVE_DEFAULT, deltaT);
	derivativeType = ORIENTATION_Z;
	Vector3f orientationZ = numericalDerivativeOrientation(	statePriori.orientation, statePriori.angularVelocity,
															derivativeType, DELTA_DERIVATIVE_DEFAULT, deltaT);

	derivativeType = ANGULAR_VELOCITY_X;
	Vector3f angularVelocityX = numericalDerivativeOrientation(	statePriori.orientation, statePriori.angularVelocity,
																derivativeType, DELTA_DERIVATIVE_DEFAULT, deltaT);
	derivativeType = ANGULAR_VELOCITY_Y;
	Vector3f angularVelocityY = numericalDerivativeOrientation(	statePriori.orientation, statePriori.angularVelocity,
																derivativeType, DELTA_DERIVATIVE_DEFAULT, deltaT);
	derivativeType = ANGULAR_VELOCITY_Z;
	Vector3f angularVelocityZ = numericalDerivativeOrientation(	statePriori.orientation, statePriori.angularVelocity,
																derivativeType, DELTA_DERIVATIVE_DEFAULT, deltaT);

	stateTransitionMatrix.topLeftCorner<3, 3>() << 	orientationX.x(), orientationY.x(), orientationZ.x(),
													orientationX.y(), orientationY.y(), orientationZ.y(),
													orientationX.z(), orientationY.z(), orientationZ.z();
	stateTransitionMatrix.topRightCorner<3, 3>() << angularVelocityX.x(), angularVelocityY.x(), angularVelocityZ.x(),
													angularVelocityX.y(), angularVelocityY.y(), angularVelocityZ.y(),
													angularVelocityX.z(), angularVelocityY.z(), angularVelocityZ.z();
	stateTransitionMatrix.bottomLeftCorner<3, 3>() << Matrix3f::Zero();
	stateTransitionMatrix.bottomRightCorner<3, 3>() << Matrix3f::Identity();

	float orientationNorm = statePriori.orientation.norm();
	Matrix3f rotationMatrixOrientation;
	rotationMatrixOrientation = AngleAxisf(orientationNorm, (orientationNorm == 0) ? Vector3f(1, 0, 0) :
			(statePriori.orientation / orientationNorm).eval());

	Vector3f deltaOrientation = statePriori.angularVelocity * deltaT;
	float deltaNorm = deltaOrientation.norm();
	Matrix3f rotationMatrixDelta;
	rotationMatrixDelta = AngleAxisf(deltaNorm, (deltaNorm == 0) ? Vector3f(1, 0, 0) :
			(deltaOrientation / deltaNorm).eval());

	AngleAxisf angleAxisFinal;
	angleAxisFinal =  rotationMatrixOrientation * rotationMatrixDelta;

	/*std::stringstream ss;
	Matrix3f prova1 = rotationMatrixOrientation * rotationMatrixDelta;
	Matrix3f prova2 = rotationMatrixDelta.transpose() * rotationMatrixOrientation.transpose();
	prova2.transposeInPlace();
	ss << "Prova 1: " << prova1 << std::endl;
	ss << "Prova 2: " << prova2 << std::endl;
	LOGI("%s", ss.str().data());*/

	statePriori.orientation = angleAxisFinal.angle() * angleAxisFinal.axis();
	statePriori.angularVelocity = statePriori.angularVelocity;

	errorCovariancePriori = stateTransitionMatrix * errorCovariancePriori * stateTransitionMatrix.transpose() + processNoiseCovarianceMatrix;
}

void OrientationExtendedKalmanFilter::updateInertial(const Vector3f& angularVelocity)
{
	Vector3f measurementResidual = angularVelocity - statePriori.angularVelocity;

	// Simplification: H * P * Ht + R -> [0 I] P [0 I]t + R
	Matrix3f covarianceResidual = errorCovariancePriori.bottomRightCorner<3, 3>() + observationNoiseCovarianceMatrixInertial;

	Matrix<float, 6, 3> kalmanGain = errorCovariancePriori * observationMatrixInertial.transpose() * covarianceResidual.inverse();
	Matrix<float, 6, 1> updatedPartComplessive = kalmanGain * measurementResidual;

	float orientationNorm = statePriori.orientation.norm();
	Matrix3f rotationMatrixOrientation;
	rotationMatrixOrientation = AngleAxisf(orientationNorm, (orientationNorm == 0) ? Vector3f(1, 0, 0) :
			(statePriori.orientation / orientationNorm).eval());
	Vector3f deltaOrientation = updatedPartComplessive.head<3>();
	float deltaOrientationNorm = deltaOrientation.norm();
	Matrix3f rotationMatrixDeltaOrientation;
	rotationMatrixDeltaOrientation = AngleAxisf(deltaOrientationNorm, (deltaOrientationNorm == 0) ? Vector3f(1, 0, 0) :
			(deltaOrientation / deltaOrientationNorm).eval());

	AngleAxisf orientationAngleAxis;
	orientationAngleAxis = rotationMatrixOrientation * rotationMatrixDeltaOrientation;
	statePosteriori.orientation = orientationAngleAxis.angle() * orientationAngleAxis.axis();
	//statePosteriori.orientation = statePriori.orientation + updatedPartComplessive.head<3>();
	statePosteriori.angularVelocity = statePriori.angularVelocity + updatedPartComplessive.tail<3>();

	errorCovariancePosteriori = (Matrix<float, 6, 6>::Identity() - kalmanGain * observationMatrixInertial) * errorCovariancePriori;

	// Assignment to priori variables in order to allow further predictions
	statePriori = statePosteriori;
	errorCovariancePriori = errorCovariancePosteriori;
}

void OrientationExtendedKalmanFilter::updateVisual(const Vector3f& orientation)
{
	float orientationNorm = statePriori.orientation.norm();
	Matrix3f rotationMatrixKalman;
	rotationMatrixKalman = AngleAxisf(orientationNorm, (orientationNorm == 0) ? Vector3f(1, 0, 0) :
			(statePriori.orientation / orientationNorm).eval());

	orientationNorm = orientation.norm();
	Matrix3f rotationMatrixVisual;
	rotationMatrixVisual = AngleAxisf(orientationNorm, (orientationNorm == 0) ? Vector3f(1, 0, 0) :
			(orientation / orientationNorm).eval());

	AngleAxisf residual;
	residual = rotationMatrixVisual * rotationMatrixKalman.transpose();
	Vector3f measurementResidual = residual.angle() * residual.axis();

	// Simplification: H * P * Ht + R -> [I 0] P [I 0]t + R
	Matrix3f covarianceResidual = errorCovariancePriori.topLeftCorner<3, 3>() + observationNoiseCovarianceMatrixVisual;

	Matrix<float, 6, 3> kalmanGain = errorCovariancePriori * observationMatrixVisual.transpose() * covarianceResidual.inverse();
	Matrix<float, 6, 1> updatedPartComplessive = kalmanGain * measurementResidual;

	Vector3f deltaOrientation = updatedPartComplessive.head<3>();
	float deltaOrientationNorm = deltaOrientation.norm();
	Matrix3f rotationMatrixDeltaOrientation;
	rotationMatrixDeltaOrientation = AngleAxisf(deltaOrientationNorm, (deltaOrientationNorm == 0) ? Vector3f(1, 0, 0) :
			(deltaOrientation / deltaOrientationNorm).eval());

	AngleAxisf orientationAngleAxis;
	orientationAngleAxis = rotationMatrixDeltaOrientation * rotationMatrixKalman;
	statePosteriori.orientation = orientationAngleAxis.angle() * orientationAngleAxis.axis();
	//statePosteriori.orientation = statePriori.orientation + updatedPartComplessive.head<3>();
	statePosteriori.angularVelocity = statePriori.angularVelocity + updatedPartComplessive.tail<3>();

	errorCovariancePosteriori = (Matrix<float, 6, 6>::Identity() - kalmanGain * observationMatrixVisual) * errorCovariancePriori;

	// Assignment to priori variables in order to allow further predictions
	statePriori = statePosteriori;
	errorCovariancePriori = errorCovariancePosteriori;
}
