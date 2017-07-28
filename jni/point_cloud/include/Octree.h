#ifndef OCTREE_TO_EXPORT_H
#define OCTREE_TO_EXPORT_H

#include <stdio.h>
#include <stdlib.h>
#include <math.h>

#include <stdexcept>
#include <list>

#include <Eigen/Geometry>
#include <Eigen/Eigenvalues>

//TODO typedef Eigen::Matrix<unsigned char, 3, 1> Vector3uc;
//TODO comments

namespace oct
{
	struct OctreeElement
	{
		public:

			EIGEN_MAKE_ALIGNED_OPERATOR_NEW
			Eigen::Vector3f coordinates;
			Eigen::Vector3f realPoint;
			Eigen::Matrix<unsigned char, 3, 1> realColor;
	};

	class OctreeNode
	{
		public:

			EIGEN_MAKE_ALIGNED_OPERATOR_NEW
			typedef std::list<Eigen::Vector3f, Eigen::aligned_allocator<Eigen::Vector3f> > PointList;
			typedef std::list<Eigen::Matrix<unsigned char, 3, 1>, Eigen::aligned_allocator<Eigen::Matrix<unsigned char, 3, 1> > > ColorList;

			virtual ~OctreeNode() {};

			virtual bool isEmpty() = 0;

			virtual unsigned char getLevel() = 0;

			virtual void insert(OctreeElement& element, float weight = 1.0F) = 0;

			virtual void getPoints(PointList& points, ColorList& colors) = 0;
	};

	class OctreeLeaf : public OctreeNode
	{
		private:
			Eigen::Vector3f resultingPoint;
			Eigen::Vector3f intermediateColor;
			Eigen::Matrix<unsigned char, 3, 1> resultingColor;
			float totalWeight;

		public:
			EIGEN_MAKE_ALIGNED_OPERATOR_NEW
			typedef std::list<Eigen::Vector3f, Eigen::aligned_allocator<Eigen::Vector3f> > PointList;
			typedef std::list<Eigen::Matrix<unsigned char, 3, 1>, Eigen::aligned_allocator<Eigen::Matrix<unsigned char, 3, 1> > > ColorList;

			OctreeLeaf();

			virtual ~OctreeLeaf();

			virtual bool isEmpty();

			virtual unsigned char getLevel();

			virtual void insert(OctreeElement& element, float weight = 1.0F);

			virtual void getPoints(PointList& points, ColorList& colors);
	};

	class OctreeBranch : public OctreeNode
	{
		private:
			unsigned char level;
			/* {(0,0,0), (1,0,0), (0,1,0), (1,1,0),
			 * 	(0,0,1), (1,0,1), (0,1,1), (1,1,1)} */
			OctreeNode* children[8];

		public:
			EIGEN_MAKE_ALIGNED_OPERATOR_NEW
			typedef std::list<Eigen::Vector3f, Eigen::aligned_allocator<Eigen::Vector3f> > PointList;
			typedef std::list<Eigen::Matrix<unsigned char, 3, 1>, Eigen::aligned_allocator<Eigen::Matrix<unsigned char, 3, 1> > > ColorList;

			OctreeBranch(unsigned char level);

			virtual ~OctreeBranch();

			virtual bool isEmpty();

			virtual unsigned char getLevel();

			virtual void insert(OctreeElement& element, float weight = 1.0F);

			virtual void getPoints(PointList& points, ColorList& colors);
	};

	// Class representing an Octree
	class Octree
	{
		private:

			float sideLengthDefinitive;
			Eigen::Vector3f offset;
			OctreeNode* root;

		public:

			EIGEN_MAKE_ALIGNED_OPERATOR_NEW
			typedef std::list<Eigen::Vector3f, Eigen::aligned_allocator<Eigen::Vector3f> > PointList;
			typedef std::list<Eigen::Matrix<unsigned char, 3, 1>, Eigen::aligned_allocator<Eigen::Matrix<unsigned char, 3, 1> > > ColorList;

			/* Base constructor, it creates the octree given the input points, the point colors, the bounding box extents and
			 * the desired resolution */
			Octree(	float *points, unsigned char* colors, int pointArrayLength, const Eigen::Vector3f& minPoint,
					const Eigen::Vector3f& maxPoint, float resolution);

			// Get points and colors stored inside the octree
			void getPoints(PointList& points, ColorList& colors);

			~Octree();
	};

}

#endif // OCTREE_TO_EXPORT_H
