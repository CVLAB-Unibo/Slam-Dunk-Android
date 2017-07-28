#include "point_cloud/include/OrientedBoundingBox.h"
#include "point_cloud/include/PointCloudTransform.h"

obb::OrientedBoundingBox::OrientedBoundingBox(float *points, int pointArrayLength, bool reprojectInput)
{
	// Calculate centroid
	Eigen::Vector3f centroid;
	calculateCentroid(points, pointArrayLength, centroid);

	// Calculate normalized covariance matrix
	Eigen::Matrix3f covarianceMatrix;
	calculateCovarianceMatrixNormalized(points, pointArrayLength, centroid, covarianceMatrix);

	// Calculate eigenvectors
	Eigen::SelfAdjointEigenSolver<Eigen::Matrix3f> eigenSolver(covarianceMatrix, Eigen::ComputeEigenvectors);
	Eigen::Matrix3f eigenvectorsMatrix = eigenSolver.eigenvectors();
	eigenvectorsMatrix.col(2) = eigenvectorsMatrix.col(0).cross(eigenvectorsMatrix.col(1));

	/* Create the inverse of the transformation matrix and reproject the point cloud, saving it in a different array
	 * if reprojectInput is false or in the same array otherwise */
	Eigen::Matrix4f transformationMatrixInv = Eigen::Matrix4f::Identity();
	transformationMatrixInv.block<3,3>(0,0) = eigenvectorsMatrix.transpose();
	transformationMatrixInv.block<3,1>(0,3) = -1.0F * (transformationMatrixInv.block<3,3>(0,0) * centroid);
	float *pointsReprojected = (reprojectInput) ? points : new float[pointArrayLength];
	reprojectPointCloud(points, pointsReprojected, pointArrayLength, transformationMatrixInv);

	// Calculate minimum and maximum points of the bounding box and the center
	Eigen::Vector3f min, max;
	calculateMinMax(pointsReprojected, pointArrayLength, min, max);
	Eigen::Vector3f center = (min + max) / 2;

	// Assign the values to the class fields
	rotationMatrix = eigenvectorsMatrix;
	translationVector = rotationMatrix * center + centroid;
	minPoint = min - center;
	maxPoint = max - center;

	if (reprojectInput)
	{
		// Translate the point cloud in order to have the center in the origin
		Eigen::Vector3f centerInv = -1.0F * center;
		translatePointCloud(points, points, pointArrayLength, centerInv);
	}
	else
	{
		// Deallocate reprojected points array (if created)
		delete [] pointsReprojected;
	}
}

void obb::OrientedBoundingBox::calculateCentroid(float* points, int pointArrayLength, Eigen::Vector3f &centroid)
{
	centroid.setZero();

	for (int i = 0; i < pointArrayLength; i += 3)
	{
		centroid[0] += points[i];
		centroid[1] += points[i + 1];
		centroid[2] += points[i + 2];
	}

	centroid /= (pointArrayLength / 3);
}

void obb::OrientedBoundingBox::calculateCovarianceMatrixNormalized(float* points, int pointArrayLength, Eigen::Vector3f &centroid,
		Eigen::Matrix3f &covarianceMatrix)
{
	covarianceMatrix.setZero();

	Eigen::Vector3f pt;
	for (int i = 0; i < pointArrayLength; i += 3)
	{
		pt[0] = points[i] - centroid[0];
		pt[1] = points[i + 1] - centroid[1];
		pt[2] = points[i + 2] - centroid[2];

		covarianceMatrix(1, 1) += pt.y() * pt.y();
		covarianceMatrix(1, 2) += pt.y() * pt.z();
		covarianceMatrix(2, 2) += pt.z() * pt.z();

		pt *= pt.x();

		covarianceMatrix(0, 0) += pt.x();
		covarianceMatrix(0, 1) += pt.y();
		covarianceMatrix(0, 2) += pt.z();
	}

	covarianceMatrix(1, 0) = covarianceMatrix(0, 1);
	covarianceMatrix(2, 0) = covarianceMatrix(0, 2);
	covarianceMatrix(2, 1) = covarianceMatrix(1, 2);

	covarianceMatrix /= (pointArrayLength / 3);
}

void obb::OrientedBoundingBox::calculateMinMax(float *points, int pointArrayLength, Eigen::Vector3f &min, Eigen::Vector3f &max)
{
	min[0] = FLT_MAX;
	min[1] = FLT_MAX;
	min[2] = FLT_MAX;

	max[0] = -FLT_MAX;
	max[1] = -FLT_MAX;
	max[2] = -FLT_MAX;

	for (int i = 0; i < pointArrayLength; i += 3)
	{
		// Min/max on x
		if (points[i] < min[0])
			min[0] = points[i];
		if (points[i] > max[0])
			max[0] = points[i];

		// Min/max on y
		if (points[i + 1] < min[1])
			min[1] = points[i + 1];
		if (points[i + 1] > max[1])
			max[1] = points[i + 1];

		// Min/max on z
		if (points[i + 2] < min[2])
			min[2] = points[i + 2];
		if (points[i + 2] > max[2])
			max[2] = points[i + 2];
	}
}

const Eigen::Matrix3f& obb::OrientedBoundingBox::getRotationMatrix()
{
	return rotationMatrix;
}

const Eigen::Vector3f& obb::OrientedBoundingBox::getTranslationVector()
{
	return translationVector;
}

const Eigen::Vector3f& obb::OrientedBoundingBox::getMinPoint()
{
	return minPoint;
}

const Eigen::Vector3f& obb::OrientedBoundingBox::getMaxPoint()
{
	return maxPoint;
}
