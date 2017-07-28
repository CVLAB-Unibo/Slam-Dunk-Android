
#include "slamdunk/include/feature_matcher.h"
#include <list>

#include <android/log.h>

#include <time.h>

#include <math.h>

#include <opencv2/calib3d/calib3d.hpp>

#define  LOG_TAG3	"slamdunk_app"
#define  LOGI3(...)	__android_log_print(ANDROID_LOG_INFO, LOG_TAG3, __VA_ARGS__)

template<class FLANNDistanceTp>
void slamdunk::RatioMatcher<FLANNDistanceTp>::setModels(const std::vector<cv::Mat>& models)
{
  assert(cv::DataType<FLANNElementTp>::type == models[0].type());

  imgNum = models.size();

  matcher->clear();
  matcher->add(models);

  matcher->train();

  rowNum = 0;
  for (int i = 0; i < models.size(); i++)
  {
	  if (rowNum < models[i].rows)
		  rowNum = models[i].rows;
  }

  /*m_model_zeroes.resize(models.size());
  int nDesc = 0;
  for(unsigned i = 0; i < models.size(); ++i)
  {
    m_model_zeroes[i] = nDesc;
    nDesc += models[i].rows;
  }
  m_descriptors.create(nDesc, models[0].cols);
  m_model_indices.clear();
  m_model_indices.reserve(nDesc);
  for(unsigned i = 0; i < models.size(); ++i)
  {
    m_model_indices.insert(m_model_indices.end(), (size_t)models[i].rows, i);
    models[i].copyTo(m_descriptors.rowRange(m_model_zeroes[i], m_model_zeroes[i]+models[i].rows));
  }
  assert((int)m_model_indices.size() == nDesc);

  delete m_index;
  cvflann::Matrix<FLANNElementTp> matrix = cvflann::Matrix<FLANNElementTp>(m_descriptors[0], m_descriptors.rows, m_descriptors.cols);
  m_index = createIndex(matrix);
  m_index->buildIndex();*/
}

namespace
{
  struct MatchHypothesis
  {
    int feat_gidx, query_idx;
    float ratio;
  };

  bool matchH_comparison(const MatchHypothesis& mh0, const MatchHypothesis& mh1)
  {
    return mh0.ratio < mh1.ratio;
  }
}

namespace
{
  struct MatchHypothesisMod
  {
    int feat_gidx, query_idx, train_idx;
    float ratio;
  };

  bool matchH_comparisonMod(const MatchHypothesisMod& mh0, const MatchHypothesisMod& mh1)
  {
    return mh0.ratio < mh1.ratio;
  }
}

template<class FLANNDistanceTp>
void slamdunk::RatioMatcher<FLANNDistanceTp>::match(cv::Mat query, std::vector<FMatch>& matches) const
{
  assert(cv::DataType<FLANNElementTp>::type == query.type());
  assert(query.cols == m_descriptors.cols);

  clock_t start = clock();
  std::vector<std::vector<cv::DMatch> > matchesTemp;
  matcher->knnMatch(query, matchesTemp, (int)std::min((double)SLAM_DUNK_KNN_NEIGHBORS, (double)rowNum));
  clock_t end = clock();
  std::stringstream ss;
  ss << "MATCHING: " << (1000.0F * (float)(end - start) / (float)CLOCKS_PER_SEC);
  LOGI3("%s", ss.str().data());

  if (matchesTemp.size() == 0)
	  return;

  std::list<MatchHypothesisMod> match_list;
  for(int k = 0; k < query.rows; ++k)
  {
    std::vector<cv::DMatch> actualMatches = matchesTemp[k];
    if (actualMatches.size() == 0)
  	  continue;
    int model1 = actualMatches[0].imgIdx;

    // find 2nd nearest neighbor descriptor
    for(unsigned ne = 1; ne < SLAM_DUNK_KNN_NEIGHBORS && actualMatches.size() > ne; ++ne)
    {
      // no need to check the distance because results are sorted
      if((actualMatches[ne].imgIdx != model1 && m_inter_model_ratio) ||
         (actualMatches[ne].imgIdx == model1 && !m_inter_model_ratio))
      {
        if(actualMatches[0].distance <= m_ratio2 * actualMatches[ne].distance && actualMatches[ne].distance > 0)
        {
          MatchHypothesisMod mh;
          mh.feat_gidx = model1;
          mh.query_idx = k;
          mh.train_idx = actualMatches[0].trainIdx;
          mh.ratio = actualMatches[0].distance / actualMatches[ne].distance;
          match_list.push_back(mh);
        }
        break;
      }
    }
  }

  match_list.sort(matchH_comparisonMod);

  std::vector<std::vector<bool> > mask(imgNum);
  for (int i = 0; i < imgNum; i++)
  {
  	mask[i] = std::vector<bool>(rowNum, true);
  }
  matches.clear();
  matches.reserve(match_list.size());
  for(std::list<MatchHypothesisMod>::const_iterator it = match_list.begin(); it != match_list.end(); ++it)
  {
	  /*std::stringstream sts;
	  sts << "feat gidx " << it->feat_gidx << " train idx " << it->train_idx;
	  LOGI3("%s", sts.str().data());*/

    if(it->feat_gidx < imgNum && it->train_idx < rowNum && mask[it->feat_gidx][it->train_idx])
    {
      mask[it->feat_gidx][it->train_idx] = false;
      FMatch fm;
      fm.m_model_idx = it->feat_gidx;
      fm.m_feat_idx = it->train_idx;
      fm.m_query_idx = it->query_idx;
      fm.m_match_score = 1.f - std::sqrt(it->ratio);
      matches.push_back(fm);
    }
  }

  //LOGI3("PASSED");

  /*typedef typename FLANNDistanceTp::ResultType DistanceType;

  cvflann::SearchParams search_params(-1, 0, true);
  cvflann::Matrix<FLANNElementTp> query_mtx(query.ptr<FLANNElementTp>(0), query.rows, query.cols, query.step.p[0]);
  cvflann::Matrix<int> indices(new int[SLAM_DUNK_KNN_NEIGHBORS * query.rows], query.rows, SLAM_DUNK_KNN_NEIGHBORS);
  cvflann::Matrix<DistanceType> dists(new DistanceType[SLAM_DUNK_KNN_NEIGHBORS * query.rows], query.rows, SLAM_DUNK_KNN_NEIGHBORS);

  clock_t start = clock();
  m_index->knnSearch(query_mtx, indices, dists, SLAM_DUNK_KNN_NEIGHBORS, search_params);
  clock_t end = clock();
  std::stringstream ss;
  ss << "MATCHING: " << (1000.0F * (float)(end - start) / (float)CLOCKS_PER_SEC);
  LOGI3("%s", ss.str().data());

  std::list<MatchHypothesis> match_list;
  for(int k = 0; k < query.rows; ++k)
  {
    const int* indexPtr = indices[k];
    const DistanceType* distsPtr = dists[k];
    const unsigned model1 = m_model_indices[ indexPtr[0] ];

    // find 2nd nearest neighbor descriptor
    for(unsigned ne = 1; ne < SLAM_DUNK_KNN_NEIGHBORS; ++ne)
    {
      assert(distsPtr[0] <= distsPtr[ne]);
      // no need to check the distance because results are sorted
      if((m_model_indices[ indexPtr[ne] ] != model1 && m_inter_model_ratio) ||
          (m_model_indices[ indexPtr[ne] ] == model1 && !m_inter_model_ratio))
      {
        if((float)distsPtr[0] <= m_ratio2 * (float)distsPtr[ne] && (float)distsPtr[ne] > 0)
        {
          MatchHypothesis mh;
          mh.feat_gidx = indexPtr[0];
          mh.query_idx = k;
          mh.ratio = (float)distsPtr[0] / (float)distsPtr[ne];
          match_list.push_back(mh);
        }
        break;
      }
    }
  }
  match_list.sort(matchH_comparison);*/

  /*for(std::list<MatchHypothesis>::const_iterator it = match_list.begin(); it != match_list.end(); ++it)
  {
  	std::stringstream ss;
  	ss << "Q:" << it->query_idx << "; T:" << it->feat_gidx << "; R:" << it->ratio;
  	LOGI3("%s", ss.str().data());
  }*/

  // clear double matches
  /*std::vector<bool> mask(m_model_indices.size(), true);
  matches.clear();
  matches.reserve(match_list.size());
  for(std::list<MatchHypothesis>::const_iterator it = match_list.begin(); it != match_list.end(); ++it)
    if(mask[it->feat_gidx])
    {
      mask[it->feat_gidx] = false;
      FMatch fm;
      fm.m_model_idx = m_model_indices[ it->feat_gidx ];
      fm.m_feat_idx = it->feat_gidx - m_model_zeroes[fm.m_model_idx];
      fm.m_query_idx = it->query_idx;
      fm.m_match_score = 1.f - std::sqrt(it->ratio);
      matches.push_back(fm);
    }*/

  /*for(std::vector<FMatch>::const_iterator it = matches.begin(); it != matches.end(); ++it)
  {
	std::stringstream ss;
	ss << "Q:" << it->m_query_idx << "; T:" << it->m_feat_idx << "; S:" << it->m_match_score;
	LOGI3("%s", ss.str().data());
  }*/

  /*delete[] indices.data;
  delete[] dists.data;*/
}


template<class FLANNDistanceTp>
typename slamdunk::RatioMatcherKDTreeIndex<FLANNDistanceTp>::FLANNIndex*
  slamdunk::RatioMatcherKDTreeIndex<FLANNDistanceTp>::createIndex(const cvflann::Matrix<FLANNElementTp>& features) const
{
  return new FLANNIndex(features, cvflann::KDTreeIndexParams(4));
}

template<class FLANNDistanceTp>
typename slamdunk::RatioMatcherLSH<FLANNDistanceTp>::FLANNIndex*
  slamdunk::RatioMatcherLSH<FLANNDistanceTp>::createIndex(const cvflann::Matrix<FLANNElementTp>& features) const
{
  return new FLANNIndex(features, /*cvflann::LshIndexParams(12, 20, 2)*/cvflann::HierarchicalClusteringIndexParams());
}
