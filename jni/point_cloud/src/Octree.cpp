#include "point_cloud/include/Octree.h"

#include <android/log.h>

#define  LOG_TAG	"Octree"
#define  LOGI(...)	__android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define  LOGE(...)	__android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

oct::OctreeLeaf::OctreeLeaf()
{
	resultingPoint.setZero();
	intermediateColor.setZero();
	resultingColor.setZero();
	totalWeight = 0.0F;
};

oct::OctreeLeaf::~OctreeLeaf()
{

}

bool oct::OctreeLeaf::isEmpty()
{
	return (totalWeight <= 0.0F);
}

unsigned char oct::OctreeLeaf::getLevel()
{
	return 0;
}

void oct::OctreeLeaf::insert(oct::OctreeElement& element, float weight)
{
	if (weight == 1.0F)
	{
		resultingPoint += element.realPoint;
		intermediateColor += element.realColor.cast<float>();
		totalWeight++;
	}
	else
	{
		resultingPoint += weight * element.realPoint;
		intermediateColor += weight * element.realColor.cast<float>();
		totalWeight += weight;
	}
}

void oct::OctreeLeaf::getPoints(oct::OctreeLeaf::PointList& points, oct::OctreeLeaf::ColorList& colors)
{
	if (totalWeight == 1.0F)
	{
		points.push_back(resultingPoint);
		resultingColor = intermediateColor.cast<unsigned char>();
		colors.push_back(resultingColor);
	}
	else
	{
		points.push_back(resultingPoint / totalWeight);
		resultingColor = (intermediateColor / totalWeight).cast<unsigned char>();
		colors.push_back(resultingColor);
	}
}

oct::OctreeBranch::OctreeBranch(unsigned char level) : level(level)
{
	for (int i = 0; i < 8; i++)
		children[i] = NULL;
};

oct::OctreeBranch::~OctreeBranch()
{
	for (int i = 0; i < 8; i++)
		delete children[i];
}

bool oct::OctreeBranch::isEmpty()
{
	for (int i = 0; i < 8; i++)
		if (children[i] != NULL)
			return false;

	return true;
}

unsigned char oct::OctreeBranch::getLevel()
{
	return level;
}

void oct::OctreeBranch::insert(oct::OctreeElement& element, float weight)
{
	element.coordinates *= 2.0F;

	if (element.coordinates.x() < 0.0F || element.coordinates.x() >= 2.0F ||
		element.coordinates.y() < 0.0F || element.coordinates.y() >= 2.0F ||
		element.coordinates.z() < 0.0F || element.coordinates.z() >= 2.0F)
	{
		std::stringstream ss;
		ss << "REFUSED POINT";
		LOGI("%s", ss.str().data());
		return;
	}

	//assert(element.coordinates.x() >= 0.0F && element.coordinates.x() < 2.0F);
	//assert(element.coordinates.y() >= 0.0F && element.coordinates.y() < 2.0F);
	//assert(element.coordinates.z() >= 0.0F && element.coordinates.z() < 2.0F);

	const int index = 	((int)element.coordinates.x()) +
						2 * ((int)element.coordinates.y()) +
						4 * ((int)element.coordinates.z());

	if (element.coordinates.x() >= 1.0F)
		element.coordinates.x() -= 1.0F;
	if (element.coordinates.y() >= 1.0F)
		element.coordinates.y() -= 1.0F;
	if (element.coordinates.z() >= 1.0F)
		element.coordinates.z() -= 1.0F;

	if (children[index] == NULL)
	{
		if (level == 0)
			children[index] = new OctreeLeaf();
		else
			children[index] = new OctreeBranch(level - 1);
	}

	children[index]->insert(element, weight);
}

void oct::OctreeBranch::getPoints(oct::OctreeBranch::PointList& points, oct::OctreeBranch::ColorList& colors)
{
	for (int i = 0; i < 8; i++)
	{
		if (children[i] != NULL)
			children[i]->getPoints(points, colors);
	}
}

oct::Octree::Octree(float *points, unsigned char* colors, int pointArrayLength, const Eigen::Vector3f& minPoint,
					const Eigen::Vector3f& maxPoint, float resolution)
{
	Eigen::Vector3f voxelNum = (maxPoint - minPoint) / resolution;
	int maxVoxelNum = std::max(std::max((int)std::ceil(voxelNum.x()), (int)std::ceil(voxelNum.y())), (int)std::ceil(voxelNum.z()));

	if (maxVoxelNum < 2)
		throw std::invalid_argument("Side length or resolution incompatible");

	int maxDepthDefinitive = (int)std::ceil(std::log((float)maxVoxelNum) / std::log(2.0F));
	sideLengthDefinitive = resolution * (1u << maxDepthDefinitive);
	float sideLengthDefinitiveHalf = sideLengthDefinitive / 2.0F;

	Eigen::Vector3f center = (maxPoint + minPoint) / 2.0F;
	offset = center - Eigen::Vector3f(sideLengthDefinitiveHalf, sideLengthDefinitiveHalf, sideLengthDefinitiveHalf);

	root = new OctreeBranch(maxDepthDefinitive);

	OctreeElement element;
	for (int i = 0; i < pointArrayLength; i += 3)
	{
		element.realPoint[0] = points[i];
		element.realPoint[1] = points[i + 1];
		element.realPoint[2] = points[i + 2];

		element.realColor[0] = colors[i];
		element.realColor[1] = colors[i + 1];
		element.realColor[2] = colors[i + 2];

		element.coordinates = (element.realPoint - offset) / sideLengthDefinitive;

		root->insert(element);
	}
}

oct::Octree::~Octree()
{
	delete root;
}

void oct::Octree::getPoints(oct::Octree::PointList& points, oct::Octree::ColorList& colors)
{
	root->getPoints(points, colors);
}
