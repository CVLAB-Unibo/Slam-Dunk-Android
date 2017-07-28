#ifndef KALMAN_STATE_H
#define KALMAN_STATE_H

#include <Eigen/Geometry>

using namespace Eigen;

namespace kalman
{
	// State of the linear Kalman filter estimating the device position.
	struct PositionStateKF
	{
		public:
			EIGEN_MAKE_ALIGNED_OPERATOR_NEW;

			Vector3f acceleration;

			Vector3f velocity;

			Vector3f position;
	};

	// State of the Extended Kalman filter estimating the device orientation.
	struct OrientationStateEKF
	{
		public:
			EIGEN_MAKE_ALIGNED_OPERATOR_NEW;

			Vector3f angularVelocity;

			Vector3f orientation;
	};

	// Internal Kalman state of the implemented indirect kalman filter.
	struct IndirectKalmanFilterState
	{
		public:
			EIGEN_MAKE_ALIGNED_OPERATOR_NEW;

			Quaternionf orientationQuaternion;

			Vector3f velocity;

			Vector3f position;
	};
}

#endif // KALMAN_STATE_H
