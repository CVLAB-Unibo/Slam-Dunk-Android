/*********************************************************************
 * Software License Agreement (BSD License)
 *
 *  Copyright (C) 2011  The Autonomous Systems Lab (ASL), ETH Zurich,
 *                Stefan Leutenegger, Simon Lynen and Margarita Chli.
 *  Copyright (c) 2009, Willow Garage, Inc.
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *
 *   * Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above
 *     copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided
 *     with the distribution.
 *   * Neither the name of the Willow Garage nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 *  FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 *  COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 *  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 *  BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 *  CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 *  LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 *  ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *  POSSIBILITY OF SUCH DAMAGE.
 *********************************************************************/

/*
 BRISK - Binary Robust Invariant Scalable Keypoints
 Reference implementation of
 [1] Stefan Leutenegger,Margarita Chli and Roland Siegwart, BRISK:
 Binary Robust Invariant Scalable Keypoints, in Proceedings of
 the IEEE International Conference on Computer Vision (ICCV2011).
 */

#include <opencv2/features2d/features2d.hpp>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <fstream>
#include <stdlib.h>

#include "precomp.hpp"

#include <time.h>

#include <android/log.h>

#define  LOG_TAG_M	"brisk_mod"
#define  LOGI(...)	__android_log_print(ANDROID_LOG_INFO, LOG_TAG_M, __VA_ARGS__)

namespace cv
{

const float BriskDescriptorExtractorParallel::basicSize_ = 12.0f;
const unsigned int BriskDescriptorExtractorParallel::scales_ = 64;
const float BriskDescriptorExtractorParallel::scalerange_ = 30.f; // 40->4 Octaves - else, this needs to be adjusted...
const unsigned int BriskDescriptorExtractorParallel::n_rot_ = 1024; // discretization of the rotation look-up

// constructors
BriskDescriptorExtractorParallel::BriskDescriptorExtractorParallel(bool recomputeOr, float patternScale)
{
	recomputeOrientation = recomputeOr;

	std::vector<float> rList;
	std::vector<int> nList;

	// this is the standard pattern found to be suitable also
	rList.resize(5);
	nList.resize(5);
	const double f = 0.85 * patternScale;

	rList[0] = (float)(f * 0.);
	rList[1] = (float)(f * 2.9);
	rList[2] = (float)(f * 4.9);
	rList[3] = (float)(f * 7.4);
	rList[4] = (float)(f * 10.8);

	nList[0] = 1;
	nList[1] = 10;
	nList[2] = 14;
	nList[3] = 15;
	nList[4] = 20;

	generateKernel(rList, nList, (float)(5.85 * patternScale), (float)(8.2 * patternScale));

}

BriskDescriptorExtractorParallel::BriskDescriptorExtractorParallel(std::vector<float> &radiusList, std::vector<int> &numberList,
		float dMax, float dMin, std::vector<int> indexChange)
{
	generateKernel(radiusList, numberList, dMax, dMin, indexChange);
}

void BriskDescriptorExtractorParallel::generateKernel(std::vector<float> &radiusList, std::vector<int> &numberList, float dMax,
		float dMin, std::vector<int> indexChange)
{
	dMax_ = dMax;
	dMin_ = dMin;

	// get the total number of points
	const int rings = (int)radiusList.size();
	assert(radiusList.size()!=0&&radiusList.size()==numberList.size());
	points_ = 0; // remember the total number of points
	for (int ring = 0; ring < rings; ring++)
	{
		points_ += numberList[ring];
	}
	// set up the patterns
	patternPoints_ = new BriskPatternPoint[points_ * scales_ * n_rot_];
	BriskPatternPoint* patternIterator = patternPoints_;

	// define the scale discretization:
	static const float lb_scale = (float)(log(scalerange_) / log(2.0));
	static const float lb_scale_step = lb_scale / (scales_);

	scaleList_ = new float[scales_];
	sizeList_ = new unsigned int[scales_];

	const float sigma_scale = 1.3f;

	for (unsigned int scale = 0; scale < scales_; ++scale)
	{
		scaleList_[scale] = (float)pow((double) 2.0, (double) (scale * lb_scale_step));
		sizeList_[scale] = 0;

		// generate the pattern points look-up
		double alpha, theta;
		for (size_t rot = 0; rot < n_rot_; ++rot)
		{
			theta = double(rot) * 2 * CV_PI / double(n_rot_); // this is the rotation of the feature
			for (int ring = 0; ring < rings; ++ring)
			{
				for (int num = 0; num < numberList[ring]; ++num)
				{
					// the actual coordinates on the circle
					alpha = (double(num)) * 2 * CV_PI / double(numberList[ring]);
					patternIterator->x = (float)(scaleList_[scale] * radiusList[ring] * cos(alpha + theta)); // feature rotation plus angle of the point
					patternIterator->y = (float)(scaleList_[scale] * radiusList[ring] * sin(alpha + theta));
					// and the gaussian kernel sigma
					if (ring == 0)
					{
						patternIterator->sigma = sigma_scale * scaleList_[scale] * 0.5f;
					}
					else
					{
						patternIterator->sigma = (float)(sigma_scale * scaleList_[scale] * (double(radiusList[ring]))
								* sin(CV_PI / numberList[ring]));
					}
					// adapt the sizeList if necessary
					const unsigned int size = cvCeil(((scaleList_[scale] * radiusList[ring]) + patternIterator->sigma)) + 1;
					if (sizeList_[scale] < size)
					{
						sizeList_[scale] = size;
					}

					// increment the iterator
					++patternIterator;
				}
			}
		}
	}

	// now also generate pairings
	shortPairs_ = new BriskShortPair[points_ * (points_ - 1) / 2];
	longPairs_ = new BriskLongPair[points_ * (points_ - 1) / 2];
	noShortPairs_ = 0;
	noLongPairs_ = 0;

	// fill indexChange with 0..n if empty
	unsigned int indSize = (unsigned int)indexChange.size();
	if (indSize == 0)
	{
		indexChange.resize(points_ * (points_ - 1) / 2);
		indSize = (unsigned int)indexChange.size();

		for (unsigned int i = 0; i < indSize; i++)
			indexChange[i] = i;
	}
	const float dMin_sq = dMin_ * dMin_;
	const float dMax_sq = dMax_ * dMax_;
	for (unsigned int i = 1; i < points_; i++)
	{
		for (unsigned int j = 0; j < i; j++)
		{ //(find all the pairs)
			// point pair distance:
			const float dx = patternPoints_[j].x - patternPoints_[i].x;
			const float dy = patternPoints_[j].y - patternPoints_[i].y;
			const float norm_sq = (dx * dx + dy * dy);
			if (norm_sq > dMin_sq)
			{
				// save to long pairs
				BriskLongPair& longPair = longPairs_[noLongPairs_];
				longPair.weighted_dx = int((dx / (norm_sq)) * 2048.0 + 0.5);
				longPair.weighted_dy = int((dy / (norm_sq)) * 2048.0 + 0.5);
				longPair.i = i;
				longPair.j = j;
				++noLongPairs_;
			}
			else if (norm_sq < dMax_sq)
			{
				// save to short pairs
				assert(noShortPairs_<indSize);
				// make sure the user passes something sensible
				BriskShortPair& shortPair = shortPairs_[indexChange[noShortPairs_]];
				shortPair.j = j;
				shortPair.i = i;
				++noShortPairs_;
			}
		}
	}

	// no bits:
	strings_ = (int) ceil((float(noShortPairs_)) / 128.0) * 4 * 4;
}

inline bool RoiPredicate(const float minX, const float minY, const float maxX, const float maxY, const KeyPoint& keyPt)
{
	const Point2f& pt = keyPt.pt;
	return (pt.x < minX) || (pt.x >= maxX) || (pt.y < minY) || (pt.y >= maxY);
}

// computes the descriptor
void BriskDescriptorExtractorParallel::operator()( InputArray _image, InputArray _mask, vector<KeyPoint>& keypoints,
		OutputArray _descriptors) const
{
	bool doOrientation=true;
	if (!recomputeOrientation)
		doOrientation = false;

	computeDescriptorsAndOrOrientation(_image, _mask, keypoints, _descriptors, doOrientation);
}

// Multi-threaded computing of the orientation
struct BRISKComputeOrientation : ParallelLoopBody
{
	BRISKComputeOrientation( const unsigned int n_rot, Mat& _descriptors, bool _doOrientation,
			const BriskDescriptorExtractorParallel::BriskPatternPoint& _patternPoints, unsigned int _points,
			const Mat& _image, const Mat& _integral,
			const BriskDescriptorExtractorParallel::BriskLongPair& _longPairs, unsigned int _noLongPairs,
			const BriskDescriptorExtractorParallel::BriskShortPair& _shortPairs, unsigned int _noShortPairs,
			vector<cv::KeyPoint>& _keypoints, const vector<int>& _kscales)
	{
		n_rot_ = n_rot;
		descriptors = &_descriptors;
		doOrientation = _doOrientation;
		patternPoints = &_patternPoints;
		points = _points;
		image = &_image;
		integral = &_integral;
		longPairs = &_longPairs;
		noLongPairs = _noLongPairs;
		shortPairs = &_shortPairs;
		noShortPairs = _noShortPairs;
		keypoints = &_keypoints;
		kscales = &_kscales;
	}

	// simple alternative:
	inline int smoothedIntensity(const cv::Mat& image, const cv::Mat& integral, const float key_x,
			const float key_y, const unsigned int scale, const unsigned int rot,
			const unsigned int point) const
	{

		// get the float position
		const BriskDescriptorExtractorParallel::BriskPatternPoint& briskPoint = patternPoints[scale * n_rot_ * points + rot * points + point];
		const float xf = briskPoint.x + key_x;
		const float yf = briskPoint.y + key_y;
		const int x = int(xf);
		const int y = int(yf);
		const int& imagecols = image.cols;

		// get the sigma:
		const float sigma_half = briskPoint.sigma;
		const float area = 4.0f * sigma_half * sigma_half;

		// calculate output:
		int ret_val;
		if (sigma_half < 0.5)
		{
			//interpolation multipliers:
			const int r_x = (int)((xf - x) * 1024);
			const int r_y = (int)((yf - y) * 1024);
			const int r_x_1 = (1024 - r_x);
			const int r_y_1 = (1024 - r_y);
			const uchar* ptr = &image.at<uchar>(y, x);
			size_t step = image.step;
			// just interpolate:
			ret_val = r_x_1 * r_y_1 * ptr[0] + r_x * r_y_1 * ptr[1] +
					r_x * r_y * ptr[step] + r_x_1 * r_y * ptr[step+1];
			return (ret_val + 512) / 1024;
		}

		// this is the standard case (simple, not speed optimized yet):

		// scaling:
		const int scaling = (int)(4194304.0 / area);
		const int scaling2 = int(float(scaling) * area / 1024.0);

		// the integral image is larger:
		const int integralcols = imagecols + 1;

		// calculate borders
		const float x_1 = xf - sigma_half;
		const float x1 = xf + sigma_half;
		const float y_1 = yf - sigma_half;
		const float y1 = yf + sigma_half;

		const int x_left = int(x_1 + 0.5);
		const int y_top = int(y_1 + 0.5);
		const int x_right = int(x1 + 0.5);
		const int y_bottom = int(y1 + 0.5);

		// overlap area - multiplication factors:
		const float r_x_1 = float(x_left) - x_1 + 0.5f;
		const float r_y_1 = float(y_top) - y_1 + 0.5f;
		const float r_x1 = x1 - float(x_right) + 0.5f;
		const float r_y1 = y1 - float(y_bottom) + 0.5f;
		const int dx = x_right - x_left - 1;
		const int dy = y_bottom - y_top - 1;
		const int A = (int)((r_x_1 * r_y_1) * scaling);
		const int B = (int)((r_x1 * r_y_1) * scaling);
		const int C = (int)((r_x1 * r_y1) * scaling);
		const int D = (int)((r_x_1 * r_y1) * scaling);
		const int r_x_1_i = (int)(r_x_1 * scaling);
		const int r_y_1_i = (int)(r_y_1 * scaling);
		const int r_x1_i = (int)(r_x1 * scaling);
		const int r_y1_i = (int)(r_y1 * scaling);

		if (dx + dy > 2)
		{
			// now the calculation:
			uchar* ptr = image.data + x_left + imagecols * y_top;
			// first the corners:
			ret_val = A * int(*ptr);
			ptr += dx + 1;
			ret_val += B * int(*ptr);
			ptr += dy * imagecols + 1;
			ret_val += C * int(*ptr);
			ptr -= dx + 1;
			ret_val += D * int(*ptr);

			// next the edges:
			int* ptr_integral = (int*) integral.data + x_left + integralcols * y_top + 1;
			// find a simple path through the different surface corners
			const int tmp1 = (*ptr_integral);
			ptr_integral += dx;
			const int tmp2 = (*ptr_integral);
			ptr_integral += integralcols;
			const int tmp3 = (*ptr_integral);
			ptr_integral++;
			const int tmp4 = (*ptr_integral);
			ptr_integral += dy * integralcols;
			const int tmp5 = (*ptr_integral);
			ptr_integral--;
			const int tmp6 = (*ptr_integral);
			ptr_integral += integralcols;
			const int tmp7 = (*ptr_integral);
			ptr_integral -= dx;
			const int tmp8 = (*ptr_integral);
			ptr_integral -= integralcols;
			const int tmp9 = (*ptr_integral);
			ptr_integral--;
			const int tmp10 = (*ptr_integral);
			ptr_integral -= dy * integralcols;
			const int tmp11 = (*ptr_integral);
			ptr_integral++;
			const int tmp12 = (*ptr_integral);

			// assign the weighted surface integrals:
			const int upper = (tmp3 - tmp2 + tmp1 - tmp12) * r_y_1_i;
			const int middle = (tmp6 - tmp3 + tmp12 - tmp9) * scaling;
			const int left = (tmp9 - tmp12 + tmp11 - tmp10) * r_x_1_i;
			const int right = (tmp5 - tmp4 + tmp3 - tmp6) * r_x1_i;
			const int bottom = (tmp7 - tmp6 + tmp9 - tmp8) * r_y1_i;

			return (ret_val + upper + middle + left + right + bottom + scaling2 / 2) / scaling2;
		}

		// now the calculation:
		uchar* ptr = image.data + x_left + imagecols * y_top;
		// first row:
		ret_val = A * int(*ptr);
		ptr++;
		const uchar* end1 = ptr + dx;
		for (; ptr < end1; ptr++)
		{
			ret_val += r_y_1_i * int(*ptr);
		}
		ret_val += B * int(*ptr);
		// middle ones:
		ptr += imagecols - dx - 1;
		uchar* end_j = ptr + dy * imagecols;
		for (; ptr < end_j; ptr += imagecols - dx - 1)
		{
			ret_val += r_x_1_i * int(*ptr);
			ptr++;
			const uchar* end2 = ptr + dx;
			for (; ptr < end2; ptr++)
			{
				ret_val += int(*ptr) * scaling;
			}
			ret_val += r_x1_i * int(*ptr);
		}
		// last row:
		ret_val += D * int(*ptr);
		ptr++;
		const uchar* end3 = ptr + dx;
		for (; ptr < end3; ptr++)
		{
			ret_val += r_y1_i * int(*ptr);
		}
		ret_val += C * int(*ptr);

		return (ret_val + scaling2 / 2) / scaling2;
	}

	void operator()(const Range& range) const
	{
		int t1;
		int t2;
		int* values = new int[points];

		for( int k=range.start; k<range.end; k++ )
		{
			cv::KeyPoint& kp = (*keypoints)[k];
			const int& scale = (*kscales)[k];
			int* pvalues = values;
			const float& x = kp.pt.x;
			const float& y = kp.pt.y;

			if (doOrientation)
			{
				// get the gray values in the unrotated pattern
				for (unsigned int i = 0; i < points; i++)
				{
					*(pvalues++) = smoothedIntensity(*image, *integral, x, y, scale, 0, i);
				}

				int direction0 = 0;
				int direction1 = 0;
				// now iterate through the long pairings
				const BriskDescriptorExtractorParallel::BriskLongPair* max = longPairs + noLongPairs;
				for (const BriskDescriptorExtractorParallel::BriskLongPair* iter = longPairs; iter < max; ++iter)
				{
					t1 = *(values + iter->i);
					t2 = *(values + iter->j);
					const int delta_t = (t1 - t2);
					// update the direction:
					const int tmp0 = delta_t * (iter->weighted_dx) / 1024;
					const int tmp1 = delta_t * (iter->weighted_dy) / 1024;
					direction0 += tmp0;
					direction1 += tmp1;
				}
				kp.angle = (float)(atan2((float) direction1, (float) direction0) / CV_PI * 180.0);
				if (kp.angle < 0)
					kp.angle += 360.f;
			}

			int theta;
			if (kp.angle==-1)
			{
				// don't compute the gradient direction, just assign a rotation of 0°
				theta = 0;
			}
			else
			{
				theta = (int) (n_rot_ * (kp.angle / (360.0)) + 0.5);
				if (theta < 0)
					theta += n_rot_;
				if (theta >= int(n_rot_))
					theta -= n_rot_;
			}

			// now also extract the stuff for the actual direction:
			// let us compute the smoothed values
			int shifter = 0;

			//unsigned int mean=0;
			pvalues = values;
			// get the gray values in the rotated pattern
			for (unsigned int i = 0; i < points; i++)
			{
				*(pvalues++) = smoothedIntensity(*image, *integral, x, y, scale, theta, i);
			}

			// now iterate through all the pairings
			unsigned int* ptr = descriptors->ptr<unsigned int>(k);
			const BriskDescriptorExtractorParallel::BriskShortPair* max = shortPairs + noShortPairs;
			for (const BriskDescriptorExtractorParallel::BriskShortPair* iter = shortPairs; iter < max; ++iter)
			{
				t1 = *(values + iter->i);
				t2 = *(values + iter->j);
				if (t1 > t2)
				{
					*ptr |= ((1) << shifter);

				} // else already initialized with zero
				// take care of the iterators:
				++shifter;
				if (shifter == 32)
				{
					shifter = 0;
					++ptr;
				}
			}
		}

		delete [] values;
	}

	unsigned int n_rot_;
	Mat *descriptors;
	const Mat *image;
	const Mat *integral;
	bool doOrientation;
	const BriskDescriptorExtractorParallel::BriskPatternPoint *patternPoints;
	unsigned int points;
	const BriskDescriptorExtractorParallel::BriskLongPair *longPairs;
	unsigned int noLongPairs;
	const BriskDescriptorExtractorParallel::BriskShortPair *shortPairs;
	unsigned int noShortPairs;
	vector<cv::KeyPoint> *keypoints;
	const vector<int> *kscales;
};

void BriskDescriptorExtractorParallel::computeDescriptorsAndOrOrientation(InputArray _image, InputArray _mask,
		vector<KeyPoint>& keypoints, OutputArray _descriptors, bool doOrientation) const
{
	Mat image = _image.getMat(), mask = _mask.getMat();
	if( image.type() != CV_8UC1 )
		cvtColor(image, image, CV_BGR2GRAY);

	//Remove keypoints very close to the border
	size_t ksize = keypoints.size();
	std::vector<int> kscales; // remember the scale per keypoint
	kscales.resize(ksize);
	static const float log2 = 0.693147180559945f;
	static const float lb_scalerange = (float)(log(scalerange_) / (log2));
	std::vector<cv::KeyPoint>::iterator beginning = keypoints.begin();
	std::vector<int>::iterator beginningkscales = kscales.begin();
	static const float basicSize06 = basicSize_ * 0.6f;
	for (size_t k = 0; k < ksize; k++)
	{
		unsigned int scale;
		scale = std::max((int) (scales_ / lb_scalerange * (log(keypoints[k].size / (basicSize06)) / log2) + 0.5), 0);
		// saturate
		if (scale >= scales_)
			scale = scales_ - 1;
		kscales[k] = scale;
		const int border = sizeList_[scale];
		const int border_x = image.cols - border;
		const int border_y = image.rows - border;
		if (RoiPredicate((float)border, (float)border, (float)border_x, (float)border_y, keypoints[k]))
		{
			keypoints.erase(beginning + k);
			kscales.erase(beginningkscales + k);
			if (k == 0)
			{
				beginning = keypoints.begin();
				beginningkscales = kscales.begin();
			}
			ksize--;
			k--;
		}
	}

	// first, calculate the integral image over the whole image:
	// current integral image
	cv::Mat _integral; // the integral image
	cv::integral(image, _integral);

	// resize the descriptors:
	_descriptors.create((int)ksize, strings_, CV_8U);
	cv::Mat descriptors = _descriptors.getMat();
	descriptors.setTo(0);

	// now do the extraction for all keypoints:
	parallel_for_(Range(0, ksize), BRISKComputeOrientation(n_rot_, descriptors, doOrientation,
			*patternPoints_, points_, image, _integral, *longPairs_, noLongPairs_,
			*shortPairs_, noShortPairs_, keypoints, kscales) );
}

int BriskDescriptorExtractorParallel::descriptorSize() const
{
	return strings_;
}

int BriskDescriptorExtractorParallel::descriptorType() const
{
	return CV_8U;
}

BriskDescriptorExtractorParallel::~BriskDescriptorExtractorParallel()
{
	delete[] patternPoints_;
	delete[] shortPairs_;
	delete[] longPairs_;
	delete[] scaleList_;
	delete[] sizeList_;
}

void BriskDescriptorExtractorParallel::computeImpl( const Mat& image, vector<KeyPoint>& keypoints, Mat& descriptors) const
{
	(*this)(image, Mat(), keypoints, descriptors);
}

}
