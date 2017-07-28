#include <kalman/include/MatrixUtils.h>

inline Eigen::Matrix3f getSkewSymmetricMatrix(Eigen::Vector3f& vector)
{
	return (Eigen::Matrix3f() <<	0.0F, 			-vector.z(), 	vector.y(),
									vector.z(), 	0.0F, 			-vector.x(),
									-vector.y(), 	vector.x(), 	0.0F).finished();
}

inline Eigen::Matrix4f getOmegaMatrixJPL(Eigen::Vector3f& angularVelocity)
{
	return (Eigen::Matrix4f() <<	0.0F, 					angularVelocity.z(), 	-angularVelocity.y(), 	angularVelocity.x(),
									-angularVelocity.z(), 	0.0F, 					angularVelocity.x(), 	angularVelocity.y(),
									angularVelocity.y(), 	-angularVelocity.x(), 	0.0F, 					angularVelocity.z(),
									-angularVelocity.x(),	-angularVelocity.y(), 	-angularVelocity.z(), 	0.0F).finished();
}

Eigen::Quaternionf getQuaternionFromSmallAngle(Eigen::Vector3f& angle)
{
	float squaredNormDiv4 = angle.squaredNorm() / 4.0F;

	if (squaredNormDiv4 < 1)
	{
		return Eigen::Quaternionf(sqrtf(1 - squaredNormDiv4), angle.x() * 0.5F, angle.y() * 0.5F, angle.z() * 0.5F);
	}
	else
	{
		float w = 1.0F / sqrtf(1 + squaredNormDiv4);
		float f = w * 0.5F;
		return Eigen::Quaternionf(w, angle.x() * f, angle.y() * f, angle.z() * f);
	}
}
