#include "kalman/include/Derivative.h"

Vector3f kalman::numericalDerivativeOrientation(const Vector3f& orientation, const Vector3f& angularVelocity,
												OrientationDerivativeType derivativeType, float deltaDerivative,
												float deltaT)
{
	Vector3f result(0, 0, 0);

	if (derivativeType == ORIENTATION_X || derivativeType == ORIENTATION_Y || derivativeType == ORIENTATION_Z)
	{
		Vector3f orientationPlus(orientation.x(), orientation.y(), orientation.z());
		Vector3f orientationMinus(orientation.x(), orientation.y(), orientation.z());
		Vector3f deltaOrientation = angularVelocity * deltaT;

		switch (derivativeType)
		{
			case ORIENTATION_X:
				orientationPlus.x() += deltaDerivative;
				orientationMinus.x() -= deltaDerivative;
				break;

			case ORIENTATION_Y:
				orientationPlus.y() += deltaDerivative;
				orientationMinus.y() -= deltaDerivative;
				break;

			case ORIENTATION_Z:
				orientationPlus.z() += deltaDerivative;
				orientationMinus.z() -= deltaDerivative;
				break;

			default:
				break;
		}

		float plusNorm = orientationPlus.norm();
		Matrix3f rotationMatrixPlus;
		rotationMatrixPlus = AngleAxisf(plusNorm, (plusNorm == 0) ? Vector3f(1, 0, 0) : (orientationPlus / plusNorm).eval());
		float minusNorm = orientationMinus.norm();
		Matrix3f rotationMatrixMinus;
		rotationMatrixMinus = AngleAxisf(minusNorm, (minusNorm == 0) ? Vector3f(1, 0, 0) : (orientationMinus / minusNorm).eval());
		float deltaNorm = deltaOrientation.norm();
		Matrix3f rotationMatrixDelta;
		rotationMatrixDelta = AngleAxisf(deltaNorm, (deltaNorm == 0) ? Vector3f(1, 0, 0) : (deltaOrientation / deltaNorm).eval());

		AngleAxisf angleAxisPlus;
		angleAxisPlus = rotationMatrixPlus * rotationMatrixDelta;
		AngleAxisf angleAxisMinus;
		angleAxisMinus = rotationMatrixMinus * rotationMatrixDelta;

		Vector3f resultPlus = angleAxisPlus.angle() * angleAxisPlus.axis();
		Vector3f resultMinus = angleAxisMinus.angle() * angleAxisMinus.axis();

		result = (resultPlus - resultMinus) / (2 * deltaDerivative);
	}
	else
	{
		Vector3f orientationCopy(orientation.x(), orientation.y(), orientation.z());
		Vector3f deltaOrientationPlus(angularVelocity.x(), angularVelocity.y(), angularVelocity.z());
		Vector3f deltaOrientationMinus(angularVelocity.x(), angularVelocity.y(), angularVelocity.z());

		switch (derivativeType)
		{
			case ANGULAR_VELOCITY_X:
				deltaOrientationPlus.x() += deltaDerivative;
				deltaOrientationMinus.x() -= deltaDerivative;
				break;

			case ANGULAR_VELOCITY_Y:
				deltaOrientationPlus.y() += deltaDerivative;
				deltaOrientationMinus.y() -= deltaDerivative;
				break;

			case ANGULAR_VELOCITY_Z:
				deltaOrientationPlus.z() += deltaDerivative;
				deltaOrientationMinus.z() -= deltaDerivative;
				break;

			default:
				break;
		}

		deltaOrientationPlus *= deltaT;
		deltaOrientationMinus *= deltaT;

		float orientationNorm = orientationCopy.norm();
		Matrix3f rotationMatrixOrientation;
		rotationMatrixOrientation = AngleAxisf(orientationNorm, (orientationNorm == 0) ? Vector3f(1, 0, 0) : (orientationCopy / orientationNorm).eval());
		float deltaPlusNorm = deltaOrientationPlus.norm();
		Matrix3f rotationMatrixDeltaPlus;
		rotationMatrixDeltaPlus = AngleAxisf(deltaPlusNorm, (deltaPlusNorm == 0) ? Vector3f(1, 0, 0) : (deltaOrientationPlus / deltaPlusNorm).eval());
		float deltaMinusNorm = deltaOrientationMinus.norm();
		Matrix3f rotationMatrixDeltaMinus;
		rotationMatrixDeltaMinus = AngleAxisf(deltaMinusNorm, (deltaMinusNorm == 0) ? Vector3f(1, 0, 0) : (deltaOrientationMinus / deltaMinusNorm).eval());

		AngleAxisf angleAxisPlus;
		angleAxisPlus = rotationMatrixOrientation * rotationMatrixDeltaPlus;
		AngleAxisf angleAxisMinus;
		angleAxisMinus = rotationMatrixOrientation * rotationMatrixDeltaMinus;

		Vector3f resultPlus = angleAxisPlus.angle() * angleAxisPlus.axis();
		Vector3f resultMinus = angleAxisMinus.angle() * angleAxisMinus.axis();

		result = (resultPlus - resultMinus) / (2 * deltaDerivative);
	}

	return result;
}

Vector3f kalman::numericalDerivativeBias(const Vector3f& bias, const Matrix3f& rotationMatrix, BiasDerivativeType derivativeType,
		float deltaDerivative)
{
	Vector3f result(0, 0, 0);

	Vector3f biasPlus(bias.x(), bias.y(), bias.z());
	Vector3f biasMinus(bias.x(), bias.y(), bias.z());

	switch (derivativeType)
	{
		case BIAS_X:
			biasPlus.x() += deltaDerivative;
			biasMinus.x() -= deltaDerivative;
			break;

		case BIAS_Y:
			biasPlus.y() += deltaDerivative;
			biasMinus.y() -= deltaDerivative;
			break;

		case BIAS_Z:
			biasPlus.z() += deltaDerivative;
			biasMinus.z() -= deltaDerivative;
			break;

		default:
			break;
	}

	Vector3f resultPlus = rotationMatrix * biasPlus;
	Vector3f resultMinus = rotationMatrix * biasMinus;

	result = (resultPlus - resultMinus) / (2 * deltaDerivative);

	return result;
}
