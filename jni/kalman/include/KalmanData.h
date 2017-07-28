#ifndef KALMAN_DATA_H
#define KALMAN_DATA_H

#include <Eigen/Core>

using namespace Eigen;

namespace kalman
{
	struct KalmanData
	{
		public:
			EIGEN_MAKE_ALIGNED_OPERATOR_NEW;

			static int ACCELERATION;
			static int ANGULAR_VELOCITY;
			static int POSITION;
			static int ORIENTATION;

			int dataType;

			Vector3f acceleration;

			Vector3f angularVelocity;

			Vector3f position;

			Matrix3f orientation;
	};
}

#endif // KALMAN_DATA_H
