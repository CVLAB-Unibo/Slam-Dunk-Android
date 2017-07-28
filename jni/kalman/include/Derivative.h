#ifndef KALMAN_DERIVATIVE_H
#define KALMAN_DERIVATIVE_H

#include <Eigen/Geometry>

#define DELTA_DERIVATIVE_DEFAULT 1E-10

using namespace Eigen;

enum OrientationDerivativeType
{
	ORIENTATION_X,
	ORIENTATION_Y,
	ORIENTATION_Z,
	ANGULAR_VELOCITY_X,
	ANGULAR_VELOCITY_Y,
	ANGULAR_VELOCITY_Z
};

enum BiasDerivativeType
{
	BIAS_X,
	BIAS_Y,
	BIAS_Z
};

namespace kalman
{
	Vector3f numericalDerivativeOrientation(const Vector3f& orientation, const Vector3f& angularVelocity,
											OrientationDerivativeType derivativeType, float deltaDerivative,
											float deltaT);

	Vector3f numericalDerivativeBias(const Vector3f& bias, const Matrix3f& rotationMatrix, BiasDerivativeType derivativeType,
			float deltaDerivative);
}

#endif // KALMAN_DERIVATIVE_H
