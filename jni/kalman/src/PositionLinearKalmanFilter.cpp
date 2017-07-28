#include "kalman/include/PositionLinearKalmanFilter.h"
#include "kalman/include/Derivative.h"

#include <android/log.h>

#define  LOG_TAG	"kalman_app"
#define  LOGI(...)	__android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define  LOGE(...)	__android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

using namespace kalman;

void PositionLinearKalmanFilter::init()
{
	pthread_mutex_init(&orientationLock, NULL);

	rotationMatrixLocalToGlobal = Matrix3f::Identity();

	statePriori.position = Vector3f(0, 0, 0);
	statePriori.velocity = Vector3f(0, 0, 0);
	statePriori.acceleration = Vector3f(0, 0, 0);
	statePosteriori.position = Vector3f(0, 0, 0);
	statePosteriori.velocity = Vector3f(0, 0, 0);
	statePosteriori.acceleration = Vector3f(0, 0, 0);

	// TODO tuning initialization
	errorCovariancePriori = Matrix<float, 9, 9>::Constant(0.1F);
	errorCovariancePosteriori = Matrix<float, 9, 9>::Constant(0.1F);

	stateTransitionMatrix = Matrix<float, 9, 9>::Identity();
	stateTransitionMatrix.block<3, 3>(0, 3) = Vector3f::Constant(deltaT).asDiagonal();
	stateTransitionMatrix.block<3, 3>(0, 6) = Vector3f::Constant(halfDeltaT2).asDiagonal();
	stateTransitionMatrix.block<3, 3>(3, 6) = Vector3f::Constant(deltaT).asDiagonal();

	// TODO initialize processNoiseCovarianceMatrix
	processNoiseCovarianceMatrix = Matrix<float, 9, 1>::Constant(0.0001).asDiagonal();

	observationMatrixInertial << Matrix3f::Zero(), Matrix3f::Zero(), Matrix3f::Identity();
	observationMatrixVisual << Matrix3f::Identity(), Matrix3f::Zero(), Matrix3f::Zero();

	// TODO initialize observationNoiseCovarianceMatrix later with set methods

}

Vector3f PositionLinearKalmanFilter::getPosition()
{
	return statePriori.position;
}

void PositionLinearKalmanFilter::setInertialObservationNoise(float noiseValue)
{
	observationNoiseCovarianceMatrixInertial = Vector3f::Constant(noiseValue).asDiagonal();
}

void PositionLinearKalmanFilter::setVisualObservationNoise(float noiseValue)
{
	observationNoiseCovarianceMatrixVisual = Vector3f::Constant(noiseValue).asDiagonal();
}

void PositionLinearKalmanFilter::setCurrentOrientation(const Vector3f& orientation)
{
	pthread_mutex_lock(&orientationLock);
	float orientationNorm = orientation.norm();
	rotationMatrixLocalToGlobal = AngleAxisf(orientationNorm, (orientationNorm == 0) ? Vector3f(1, 0, 0) :
			(orientation / orientationNorm).eval());
	pthread_mutex_unlock(&orientationLock);
}

void PositionLinearKalmanFilter::predict()
{
	statePriori.position = statePriori.position + statePriori.velocity * deltaT + statePriori.acceleration * halfDeltaT2;
	statePriori.velocity = statePriori.velocity + statePriori.acceleration * deltaT;
	statePriori.acceleration = statePriori.acceleration;

	errorCovariancePriori = stateTransitionMatrix * errorCovariancePriori * stateTransitionMatrix.transpose() + processNoiseCovarianceMatrix;
}

void PositionLinearKalmanFilter::updateInertial(const Vector3f& acceleration)
{
	pthread_mutex_lock(&orientationLock);
	Matrix3f rotationMatrix = rotationMatrixLocalToGlobal;
	pthread_mutex_unlock(&orientationLock);

	Vector3f measurementResidual = rotationMatrix * acceleration - gravity - statePriori.acceleration;
	// Simplification: H * P * Ht + R -> [0 0 I] P [0 0 I]t + R

	Matrix3f covarianceResidual = errorCovariancePriori.bottomRightCorner<3, 3>() + observationNoiseCovarianceMatrixInertial;

	Matrix<float, 9, 3> kalmanGain = errorCovariancePriori * observationMatrixInertial.transpose() * covarianceResidual.inverse();
	Matrix<float, 9, 1> updatedPartComplessive = kalmanGain * measurementResidual;

	statePosteriori.position = statePriori.position + updatedPartComplessive.head<3>();
	statePosteriori.velocity = statePriori.velocity + updatedPartComplessive.segment<3>(3);
	statePosteriori.acceleration = statePriori.acceleration + updatedPartComplessive.tail<3>();

	errorCovariancePosteriori = (Matrix<float, 9, 9>::Identity() - kalmanGain * observationMatrixInertial) * errorCovariancePriori;

	// Assignment to priori variables in order to allow further predictions
	statePriori = statePosteriori;
	errorCovariancePriori = errorCovariancePosteriori;
}

void PositionLinearKalmanFilter::updateVisual(const Vector3f& position)
{
	Vector3f measurementResidual = position - statePriori.position;
	// Simplification: H * P * Ht + R -> [I 0 0] P [I 0 0]t + R
	// Matrix3f covarianceResidual = observationMatrixVisual * errorCovariancePriori * observationMatrixVisual() +
	// observationNoiseCovarianceMatrixVisual;
	Matrix3f covarianceResidual = errorCovariancePriori.topLeftCorner<3, 3>() + observationNoiseCovarianceMatrixVisual;

	Matrix<float, 9, 3> kalmanGain = errorCovariancePriori * observationMatrixVisual.transpose() * covarianceResidual.inverse();
	Matrix<float, 9, 1> updatedPartComplessive = kalmanGain * measurementResidual;

	statePosteriori.position = statePriori.position + updatedPartComplessive.head<3>();
	statePosteriori.velocity = statePriori.velocity + updatedPartComplessive.segment<3>(3);
	statePosteriori.acceleration = statePriori.acceleration + updatedPartComplessive.tail<3>();

	errorCovariancePosteriori = (Matrix<float, 9, 9>::Identity() - kalmanGain * observationMatrixVisual) * errorCovariancePriori;

	// Assignment to priori variables in order to allow further predictions
	statePriori = statePosteriori;
	errorCovariancePriori = errorCovariancePosteriori;
}
