/*M///////////////////////////////////////////////////////////////////////////////////////
//
//  IMPORTANT: READ BEFORE DOWNLOADING, COPYING, INSTALLING OR USING.
//
//  By downloading, copying, installing or using the software you agree to this license.
//  If you do not agree to this license, do not download, install,
//  copy or use the software.
//
//
//                           License Agreement
//                For Open Source Computer Vision Library
//
// Copyright (C) 2000-2008, Intel Corporation, all rights reserved.
// Copyright (C) 2009, Willow Garage Inc., all rights reserved.
// Third party copyrights are property of their respective owners.
//
// Redistribution and use in source and binary forms, with or without modification,
// are permitted provided that the following conditions are met:
//
//   * Redistribution's of source code must retain the above copyright notice,
//     this list of conditions and the following disclaimer.
//
//   * Redistribution's in binary form must reproduce the above copyright notice,
//     this list of conditions and the following disclaimer in the documentation
//     and/or other materials provided with the distribution.
//
//   * The name of the copyright holders may not be used to endorse or promote products
//     derived from this software without specific prior written permission.
//
// This software is provided by the copyright holders and contributors "as is" and
// any express or implied warranties, including, but not limited to, the implied
// warranties of merchantability and fitness for a particular purpose are disclaimed.
// In no event shall the Intel Corporation or contributors be liable for any direct,
// indirect, incidental, special, exemplary, or consequential damages
// (including, but not limited to, procurement of substitute goods or services;
// loss of use, data, or profits; or business interruption) however caused
// and on any theory of liability, whether in contract, strict liability,
// or tort (including negligence or otherwise) arising in any way out of
// the use of this software, even if advised of the possibility of such damage.
//
//M*/

#ifndef __OPENCV_MOD_FEATURES_2D_HPP__
#define __OPENCV_MOD_FEATURES_2D_HPP__

#include <opencv2/core/core.hpp>
#include <opencv2/flann/miniflann.hpp>
#include <opencv2/features2d/features2d.hpp>

#ifdef __cplusplus
#include <limits>

namespace cv
{

/*!
  BRISK implementation
 */
class CV_EXPORTS_W BriskDescriptorExtractorParallel : public DescriptorExtractor
{
public:
	CV_WRAP explicit BriskDescriptorExtractorParallel(bool recomputeOrientation=true, float patternScale=1.0f);

	virtual ~BriskDescriptorExtractorParallel();

	// returns the descriptor size in bytes
	int descriptorSize() const;
	// returns the descriptor type
	int descriptorType() const;

	// Compute the BRISK descriptors on an image
	void operator()( InputArray image, InputArray mask, vector<KeyPoint>& keypoints,
			OutputArray descriptors ) const;

	//AlgorithmInfo* info() const;

	// custom setup
	CV_WRAP explicit BriskDescriptorExtractorParallel(std::vector<float> &radiusList, std::vector<int> &numberList,
			float dMax=5.85f, float dMin=8.2f, std::vector<int> indexChange=std::vector<int>());

	// call this to generate the kernel:
	// circle of radius r (pixels), with n points;
	// short pairings with dMax, long pairings with dMin
	CV_WRAP void generateKernel(std::vector<float> &radiusList,
			std::vector<int> &numberList, float dMax=5.85f, float dMin=8.2f,
			std::vector<int> indexChange=std::vector<int>());

	// some helper structures for the Brisk pattern representation
	struct BriskPatternPoint{
		float x;         // x coordinate relative to center
		float y;         // x coordinate relative to center
		float sigma;     // Gaussian smoothing sigma
	};
	struct BriskShortPair{
		unsigned int i;  // index of the first pattern point
		unsigned int j;  // index of other pattern point
	};
	struct BriskLongPair{
		unsigned int i;  // index of the first pattern point
		unsigned int j;  // index of other pattern point
		int weighted_dx; // 1024.0/dx
		int weighted_dy; // 1024.0/dy
	};

protected:

	void computeImpl( const Mat& image, vector<KeyPoint>& keypoints, Mat& descriptors ) const;

	void computeDescriptorsAndOrOrientation(InputArray image, InputArray mask, vector<KeyPoint>& keypoints,
			OutputArray descriptors, bool doOrientation) const;

	// Feature parameters
	CV_PROP_RW bool recomputeOrientation;

	// pattern properties
	BriskPatternPoint* patternPoints_;     //[i][rotation][scale]
	unsigned int points_;                 // total number of collocation points
	float* scaleList_;                     // lists the scaling per scale index [scale]
	unsigned int* sizeList_;             // lists the total pattern size per scale index [scale]
	static const unsigned int scales_;    // scales discretization
	static const float scalerange_;     // span of sizes 40->4 Octaves - else, this needs to be adjusted...
	static const unsigned int n_rot_;    // discretization of the rotation look-up

	// pairs
	int strings_;                        // number of uchars the descriptor consists of
	float dMax_;                         // short pair maximum distance
	float dMin_;                         // long pair maximum distance
	BriskShortPair* shortPairs_;         // d<_dMax
	BriskLongPair* longPairs_;             // d>_dMin
	unsigned int noShortPairs_;         // number of shortParis
	unsigned int noLongPairs_;             // number of longParis

	// general
	static const float basicSize_;
};

} /* namespace cv */

#endif /* __cplusplus */

#endif

/* End of file. */
