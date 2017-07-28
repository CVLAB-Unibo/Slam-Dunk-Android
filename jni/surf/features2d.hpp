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

#ifndef __OPENCV_MOD_NONFREE_FEATURES_2D_HPP__
#define __OPENCV_MOD_NONFREE_FEATURES_2D_HPP__

#include <opencv2/features2d/features2d.hpp>

#ifdef __cplusplus

namespace cv
{

/*!
 SURF implementation.

 The class implements SURF algorithm by H. Bay et al.
 */
class CV_EXPORTS_W SURFMOD : public Feature2D
{
public:
    //! the default constructor
    CV_WRAP SURFMOD();
    //! the full constructor taking all the necessary parameters
    explicit CV_WRAP SURFMOD(double hessianThreshold,
                  int nOctaves=4, int nOctaveLayers=2,
                  bool extended=true, bool upright=false);

    //! returns the descriptor size in float's (64 or 128)
    CV_WRAP int descriptorSize() const;

    //! returns the descriptor type
    CV_WRAP int descriptorType() const;

    //! finds the keypoints using fast hessian detector used in SURF
    void operator()(InputArray img, InputArray mask,
                    CV_OUT vector<KeyPoint>& keypoints) const;
    //! finds the keypoints and computes their descriptors. Optionally it can compute descriptors for the user-provided keypoints
    void operator()(InputArray img, InputArray mask,
                    CV_OUT vector<KeyPoint>& keypoints,
                    OutputArray descriptors,
                    bool useProvidedKeypoints=false) const;

    //AlgorithmInfo* info() const;

    CV_PROP_RW double hessianThreshold;
    CV_PROP_RW int nOctaves;
    CV_PROP_RW int nOctaveLayers;
    CV_PROP_RW bool extended;
    CV_PROP_RW bool upright;
    CV_PROP_RW int maxKeypoints;

protected:

    void detectImpl( const Mat& image, vector<KeyPoint>& keypoints, const Mat& mask=Mat() ) const;
    void computeImpl( const Mat& image, vector<KeyPoint>& keypoints, Mat& descriptors ) const;
};

typedef SURFMOD SurfModFeatureDetector;
typedef SURFMOD SurfModDescriptorExtractor;

} /* namespace cv */

#endif /* __cplusplus */

#endif

/* End of file. */
