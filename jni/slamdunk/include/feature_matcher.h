
#ifndef SLAM_DUNK_FEATURE_MATCHER_H
#define SLAM_DUNK_FEATURE_MATCHER_H

#include "slamdunk/include/slamdunk_defines.h"
#include <opencv2/flann/flann.hpp>
#include <opencv2/core/core.hpp>
#include <opencv2/features2d/features2d.hpp>

#include <boost/shared_ptr.hpp>

namespace slamdunk
{

  struct SLAM_DUNK_API FMatch
  {
    int m_model_idx, m_feat_idx, m_query_idx;
    float m_match_score;
  };

  class SLAM_DUNK_API FeatureMatcher
  {
    public:
      typedef boost::shared_ptr<FeatureMatcher> Ptr;
      typedef boost::shared_ptr<const FeatureMatcher> ConstPtr;

      virtual ~FeatureMatcher() {};
      virtual void setModels(const std::vector<cv::Mat>& models) = 0;
      virtual void match(cv::Mat query, std::vector<FMatch>& matches) const = 0;
  };

  template<class FLANNDistanceTp>
  class SLAM_DUNK_API RatioMatcher : public FeatureMatcher
  {
    public:
	  cv::DescriptorMatcher *matcher; //TODO
      typedef typename FLANNDistanceTp::ElementType FLANNElementTp;
      typedef cvflann::Index<FLANNDistanceTp> FLANNIndex;

      RatioMatcher(bool interModelRatio, float ratio, int cores)
        : m_cores(cores), m_inter_model_ratio(interModelRatio), m_ratio2(ratio*ratio), m_index(NULL),
          matcher(new cv::BFMatcher(cv::NORM_HAMMING)/*cv::FlannBasedMatcher(new cv::flann::HierarchicalClusteringIndexParams()*//*new cv::flann::LshIndexParams(12, 20, 2)*//*, new cv::flann::SearchParams(-1, 0, true))*/) {}

      virtual ~RatioMatcher() { if (m_index != NULL) delete m_index; delete matcher; }

      virtual void setModels(const std::vector<cv::Mat>& models);
      virtual void match(cv::Mat query, std::vector<FMatch>& matches) const;

      /// If true, do the ratio among different models (eg obj detection), otherwise within the same model (eg frame tracking)
      inline void setInterModelRatio(bool onoff) { m_inter_model_ratio = onoff; }
      inline bool getInterModelRatio() const { return m_inter_model_ratio; }

      inline void setRatio(float ratio) { m_ratio2 = ratio*ratio; }
      inline float getSquaredRatio() const { return m_ratio2; }

    protected:
      virtual FLANNIndex* createIndex(const cvflann::Matrix<FLANNElementTp>& features) const = 0;

    private:
      enum { SLAM_DUNK_KNN_NEIGHBORS = 32 };

      int m_cores;
      bool m_inter_model_ratio;
      float m_ratio2;
      //TODO cv::FlannBasedMatcher *matcher;
      int imgNum;
      int rowNum;
      FLANNIndex* m_index;
      cv::Mat_<FLANNElementTp> m_descriptors;
      std::vector<unsigned> m_model_indices;
      std::vector<int> m_model_zeroes;
  };

  template<class FLANNDistanceTp>
  class SLAM_DUNK_API RatioMatcherKDTreeIndex : public RatioMatcher<FLANNDistanceTp>
  {
    public:
      typedef typename RatioMatcher<FLANNDistanceTp>::FLANNElementTp FLANNElementTp;
      typedef typename RatioMatcher<FLANNDistanceTp>::FLANNIndex FLANNIndex;

      RatioMatcherKDTreeIndex(bool interModelRatio, float ratio = 0.8f, int cores = 1)
        : RatioMatcher<FLANNDistanceTp>(interModelRatio, ratio, cores) {}
    protected:
      virtual FLANNIndex* createIndex(const cvflann::Matrix<FLANNElementTp>& features) const;
  };
  typedef RatioMatcherKDTreeIndex<cvflann::L2<float> > RatioMatcherL2;
  typedef RatioMatcherKDTreeIndex<cvflann::L2<double> > RatioMatcherL2D;

  template<class FLANNDistanceTp>
  class SLAM_DUNK_API RatioMatcherLSH : public RatioMatcher<FLANNDistanceTp>
  {
  public:
        typedef typename RatioMatcher<FLANNDistanceTp>::FLANNElementTp FLANNElementTp;
        typedef typename RatioMatcher<FLANNDistanceTp>::FLANNIndex FLANNIndex;

        RatioMatcherLSH(bool interModelRatio, float ratio = 0.8f, int cores = 1)
          : RatioMatcher<FLANNDistanceTp>(interModelRatio, ratio, cores) {}
    protected:
      virtual FLANNIndex* createIndex(const cvflann::Matrix<FLANNElementTp>& features) const;
  };
  typedef RatioMatcherLSH<cvflann::Hamming<unsigned char> > RatioMatcherLSHHamming;

}

#include "impl/ratio_matcher.hpp"

#endif // SLAM_DUNK_FEATURE_MATCHER_H
