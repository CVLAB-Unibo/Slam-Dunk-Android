#include "point_cloud/include/PointCloudTransform.h"

void translatePointCloud(float* points, float* pointsReprojected, int pointArrayLength, Eigen::Vector3f &translationVector)
{
	float x, y, z;
	for (int i = 0; i < pointArrayLength; i += 3)
	{
		x = points[i];
		y = points[i + 1];
		z = points[i + 2];

		pointsReprojected[i] = x + translationVector.x();
		pointsReprojected[i + 1] = y + translationVector.y();
		pointsReprojected[i + 2] = z + translationVector.z();
	}
}

void reprojectPointCloud(float* points, float* pointsReprojected, int pointArrayLength, Eigen::Matrix4f &transformationMatrix)
{
	float x, y, z;
	for (int i = 0; i < pointArrayLength; i += 3)
	{
		x = points[i];
		y = points[i + 1];
		z = points[i + 2];

		pointsReprojected[i] = 	transformationMatrix(0, 0) * x + transformationMatrix(0, 1) * y +
								transformationMatrix(0, 2) * z + transformationMatrix(0, 3);
		pointsReprojected[i + 1] = 	transformationMatrix(1, 0) * x + transformationMatrix(1, 1) * y +
									transformationMatrix(1, 2) * z + transformationMatrix(1, 3);
		pointsReprojected[i + 2] = 	transformationMatrix(2, 0) * x + transformationMatrix(2, 1) * y +
									transformationMatrix(2, 2) * z + transformationMatrix(2, 3);
	}
}
