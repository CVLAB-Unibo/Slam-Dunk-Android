#ifndef POSITION_LINEAR_KALMAN_FILTER_H
#define POSITION_LINEAR_KALMAN_FILTER_H

#include <pthread.h>

#include "KalmanFilter.h"
#include "KalmanState.h"

namespace kalman
{
	class PositionLinearKalmanFilter
	{
		private:
			pthread_mutex_t orientationLock;

			// The matrix P.
			Matrix<float, 9, 9> errorCovariancePriori, errorCovariancePosteriori;

			// The matrix F.
			Matrix<float, 9, 9> stateTransitionMatrix;

			// The matrix Q.
			Matrix<float, 9, 9> processNoiseCovarianceMatrix;

			// The matrix H for the inertial update.
			Matrix<float, 3, 9> observationMatrixInertial;

			// The matrix H for the visual update.
			Matrix<float, 3, 9> observationMatrixVisual;

			// The matrix R for the inertial update.
			Matrix3f observationNoiseCovarianceMatrixInertial;

			// The matrix R for the visual update.
			Matrix3f observationNoiseCovarianceMatrixVisual;

			// The Kalman filter internal state.
			PositionStateKF statePriori, statePosteriori;

			// Time intervals.
			float deltaT, halfDeltaT2;

			// Estimated gravity.
			Vector3f gravity;

			// Rotation matrix to derive the linear acceleration in the world frame of reference.
			Matrix3f rotationMatrixLocalToGlobal;

		public:
			EIGEN_MAKE_ALIGNED_OPERATOR_NEW;

			PositionLinearKalmanFilter(float deltaT, const Vector3f& gravity)
			{
				this->deltaT = deltaT;
				this->halfDeltaT2 = 0.5F * deltaT * deltaT;

				this->gravity = Vector3f(gravity.x(), gravity.y(), gravity.z());

				init();
			}

			Vector3f getPosition();

			void setInertialObservationNoise(float noiseValue);

			void setVisualObservationNoise(float noiseValue);

			void setCurrentOrientation(const Vector3f& orientation);

			void init();

			void predict();

			void updateInertial(const Vector3f& acceleration);

			void updateVisual(const Vector3f& position);
	};
}

#endif // POSITION_LINEAR_KALMAN_FILTER_H
