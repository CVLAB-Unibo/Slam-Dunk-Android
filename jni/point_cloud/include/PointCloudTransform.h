#ifndef POINT_CLOUD_TRANSFORM_H
#define POINT_CLOUD_TRANSFORM_H

#include <stdio.h>
#include <stdlib.h>
#include <math.h>

#include <Eigen/Geometry>
#include <Eigen/Eigenvalues>

// Translate the points given as input, using the translation vector provided
void translatePointCloud(float* points, float* pointsReprojected, int pointArrayLength, Eigen::Vector3f &translationVector);

// Reproject the points given as input, using the transformation matrix provided
void reprojectPointCloud(float* points, float* pointsReprojected, int pointArrayLength, Eigen::Matrix4f &transformationMatrix);

#endif // POINT_CLOUD_TRANSFORM_H
