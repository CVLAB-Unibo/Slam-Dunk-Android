#ifndef MATRIX_UTILS_IKF_H
#define MATRIX_UTILS_IKF_H

#include <Eigen/Geometry>

// Gets the skew symmetric matrix of a given vector (usually used for the cross product operation).
Eigen::Matrix3f getSkewSymmetricMatrix(Eigen::Vector3f& vector);

// Gets the omega matrix, used in the quaternion derivative and depending on the angular velocity.
Eigen::Matrix4f getOmegaMatrixJPL(Eigen::Vector3f& angularVelocity);

// Gets the quaternion corresponding to a small angle approximation.
Eigen::Quaternionf getQuaternionFromSmallAngle(Eigen::Vector3f& angle);

#endif // MATRIX_UTILS_IKF_H
