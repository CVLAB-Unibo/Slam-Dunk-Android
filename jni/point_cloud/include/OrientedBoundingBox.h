#ifndef ORIENTED_BOUNDING_BOX_H
#define ORIENTED_BOUNDING_BOX_H

#include <stdio.h>
#include <stdlib.h>
#include <math.h>

#include <Eigen/Geometry>
#include <Eigen/Eigenvalues>

namespace obb
{

	// Class representing an Oriented Bounding Box
	class OrientedBoundingBox
	{
		private:

			Eigen::Matrix3f rotationMatrix;
			Eigen::Vector3f translationVector;
			Eigen::Vector3f minPoint;
			Eigen::Vector3f maxPoint;

			// Calculate the centroid from the points given as input
			void calculateCentroid(float* points, int pointArrayLength, Eigen::Vector3f &centroid);

			// Calculate the normalized covariance matrix from the points given as input and the centroid of the points
			void calculateCovarianceMatrixNormalized(float* points, int pointArrayLength, Eigen::Vector3f &centroid,
					Eigen::Matrix3f &covarianceMatrix);

			// Calculate the eigenvectors of the covariance matrix
			void calculateEigenvectors(Eigen::Matrix3f &covarianceMatrix, Eigen::Matrix3f &eigenvectorsMatrix);

			// Calculate the minimum and maximum point from the points given as input
			void calculateMinMax(float *points, int pointArrayLength, Eigen::Vector3f &min, Eigen::Vector3f &max);

		public:

			EIGEN_MAKE_ALIGNED_OPERATOR_NEW

			/* Base constructor, it creates the bounding box given the input points as an array with the associated length and
			 * a boolean that indicates if the input points have to be projected inside the bounding box or kept unchanged */
			OrientedBoundingBox(float *points, int pointArrayLength, bool reprojectInput);

			const Eigen::Matrix3f& getRotationMatrix();

			const Eigen::Vector3f& getTranslationVector();

			const Eigen::Vector3f& getMinPoint();

			const Eigen::Vector3f& getMaxPoint();
	};

}

#endif // ORIENTED_BOUNDING_BOX_H
