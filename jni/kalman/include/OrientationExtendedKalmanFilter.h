#ifndef ORIENTATION_EXTENDED_KALMAN_FILTER_H
#define ORIENTATION_EXTENDED_KALMAN_FILTER_H

#include "KalmanFilter.h"
#include "KalmanState.h"
#include "KalmanGlobal.h"

namespace kalman
{
	class OrientationExtendedKalmanFilter
	{
		private:
			// The matrix P.
			Matrix<float, 6, 6> errorCovariancePriori, errorCovariancePosteriori;

			// The matrix F.
			Matrix<float, 6, 6> stateTransitionMatrix;

			// The matrix Q.
			Matrix<float, 6, 6> processNoiseCovarianceMatrix;

			// The matrix H for the inertial update.
			Matrix<float, 3, 6> observationMatrixInertial;

			// The matrix H for the visual update.
			Matrix<float, 3, 6> observationMatrixVisual;

			// The matrix R for the inertial update.
			Matrix3f observationNoiseCovarianceMatrixInertial;

			// The matrix R for the visual update.
			Matrix3f observationNoiseCovarianceMatrixVisual;

			// The Kalman filter internal state.
			OrientationStateEKF statePriori, statePosteriori;

			// Time intervals.
			float deltaT, halfDeltaT2;

		public:
			EIGEN_MAKE_ALIGNED_OPERATOR_NEW;

			OrientationExtendedKalmanFilter(float deltaT)
			{
				this->deltaT = deltaT;
				halfDeltaT2 = 0.5F * deltaT * deltaT;

				init();
			}

			Vector3f getOrientation();

			void setInertialObservationNoise(float noiseValue);

			void setVisualObservationNoise(float noiseValue);

			void init();

			void predict();

			void updateInertial(const Vector3f& angularVelocity);

			void updateVisual(const Vector3f& orientation);
	};
}

#endif // ORIENTATION_EXTENDED_KALMAN_FILTER_H
