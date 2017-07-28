
#ifndef SLAM_DUNK_CAMERA_TRACKER_H
#define SLAM_DUNK_CAMERA_TRACKER_H

#include "slamdunk/include/slamdunk_defines.h"
#include "slamdunk/include/data.h"
#include "slamdunk/include/graph_backend.h"
#include "slamdunk/include/feature_matcher.h"
#include "slamdunk/include/transformation_estimation.h"
#include "slamdunk/include/quadtree.h"

//#include <opencv2/nonfree/features2d.hpp>
#include <surf/features2d.hpp>
#include <brisk/features2d.hpp>
//#include <opencv2/features2d/features2d.hpp>
#include <boost/function.hpp>

namespace slamdunk
{

  struct SLAM_DUNK_API TrackingResult
  {
    EIGEN_MAKE_ALIGNED_OPERATOR_NEW

    Eigen::Isometry3d m_pose;
    float m_overlapping_score; // 0.0 no overlap : 1.0 totally overlapped vs existing keyframes
    float m_overlapping_features; // partial overlapping score depending only on the feature points
    FrameData m_frame_data;
    std::vector<FrameToFrameMatch> m_ff_matches;
  };

  class SLAM_DUNK_API CameraTracker
  {
    public:
      typedef boost::shared_ptr<CameraTracker> Ptr;
      typedef boost::shared_ptr<const CameraTracker> ConstPtr;
      typedef g2o::VertexSE3 const * VertexPoseConstPtr;
      typedef std::set<VertexPoseConstPtr> VertexPoseSet;

      CameraTracker() { m_last_tracked.m_pose = Eigen::Isometry3d::Identity(); m_last_tracked.m_overlapping_score = 0.; }
      virtual ~CameraTracker() {}

      virtual bool track(const RGBDFrame& frame) = 0;
      virtual void extractFrameData(const RGBDFrame& frame, FrameData& frame_data) const = 0;
      virtual void signalMovedFrames(const VertexPoseSet& vset) = 0;
      virtual void setFrames(const VertexPoseSet& vset) = 0;
      virtual void updateMap() = 0;
      virtual void updateOverlappingScore(Eigen::Isometry3d& new_estimated_pose) = 0;

      typedef std::vector<std::pair<double, Eigen::Isometry3d>,
                          Eigen::aligned_allocator< std::pair<double, Eigen::Isometry3d> > > StampedPoseVector;
      typedef boost::function<void (const StampedPoseVector& poses)> debugging_function;
      /** \brief The debugging function is called whenever a new keyframe is detected */
      inline void setDebuggingFunction(const debugging_function& f) { m_deb_func = f; }
      inline void unsetDebuggingFunction() { m_deb_func.clear(); }

      void reset(const Eigen::Isometry3d& pose, const FrameData& frame_data)
      {
        m_last_tracked.m_pose = pose;
        m_last_tracked.m_overlapping_score = 0.0;
        m_last_tracked.m_frame_data = frame_data;
      }
      const TrackingResult& getTrackingResult() const { return m_last_tracked; }
      const std::set<int>& getLastTrackedKeyframes() const { return m_last_tracked_keyframes; }

    protected:
      TrackingResult m_last_tracked;
      std::set<int> m_last_tracked_keyframes;
      debugging_function m_deb_func;
  };

  struct SLAM_DUNK_API FeatureTrackerParams
  {
    /// quad-tree side length is qres * 2^(qdepth+1)
    double quadtree_res;
    unsigned char quadtree_depth;
    double active_win_length;
    unsigned min_matches, max_feats_per_frame;
    Eigen::Vector2d win_movement_step;
    //double max_pov_angle_th; //degrees
    float perc_feat_overlap;
    cv::Ptr<const cv::FeatureDetector> feature_detector;
    cv::Ptr<const cv::DescriptorExtractor> feature_extractor;
    cv::Ptr<const cv::Feature2D> feature_detector_extractor;
    bool use_detector_extractor;
    FeatureMatcher::Ptr feature_matcher;
    SampleConsensus::Ptr outlier_rejection;
    float near_plane, far_plane;

    bool verbose, debug;

    // default values
    FeatureTrackerParams()
      : quadtree_res(0.1), quadtree_depth(7), active_win_length(/*3*/5), min_matches(5), max_feats_per_frame(500),
        win_movement_step(1, 1), perc_feat_overlap(0.3),
        feature_detector(new cv::SurfModFeatureDetector(100, 3, 2, false, true)/*OrbFeatureDetector()*/),
        feature_extractor(new cv::/*BRISK()*//*OrbDescriptorExtractor()*/BriskDescriptorExtractorParallel()),
        feature_detector_extractor(new cv::ORB()),
        use_detector_extractor(false),
        feature_matcher(new RatioMatcherLSHHamming(false, 0.8f, 4)), outlier_rejection(new RANSAC(true, 0.05)),
        near_plane(0.1f), far_plane(10.f), verbose(false), debug(false)
    {}
  };

  class SLAM_DUNK_API FeatureTracker : public CameraTracker
  {
    public:
      const Eigen::Matrix3f m_inverse_kcam;

      SampleConsensus::Ptr m_outlier_rejection; //TODO

      FeatureTracker(const Eigen::Matrix3f& inverse_kcam, int img_width = 640, int img_height = 480,
                     const FeatureTrackerParams& params = FeatureTrackerParams())
        : m_inverse_kcam(inverse_kcam),
          m_tree(std::abs(params.quadtree_res), params.quadtree_depth),
          m_half_active_win_length(std::abs(params.active_win_length)/2.),
          m_win_movement_step(params.win_movement_step.cwiseAbs()),
          //m_min_pov_cosangle_th(std::cos(0.017453293*params.max_pov_angle_th)),
          m_perc_feat_overlap(params.perc_feat_overlap),
          m_feature_detector(params.feature_detector),
          m_feature_extractor(params.feature_extractor),
          m_feature_detector_extractor(params.feature_detector_extractor),
          m_use_detector_extractor(params.use_detector_extractor),
          m_matcher(params.feature_matcher), m_outlier_rejection(params.outlier_rejection),
          m_min_matches(params.min_matches), m_max_feats_per_frame(params.max_feats_per_frame),
          m_verbose(params.verbose), m_debug(params.debug)
      { setFrustum(inverse_kcam, img_width, img_height, params.near_plane, params.far_plane); }

      virtual bool track(const RGBDFrame& frame);
      virtual void extractFrameData(const RGBDFrame& frame, FrameData& frame_data) const;
      virtual void extractFrameData(const RGBDFrame& frame, FrameData& frame_data, std::vector<cv::KeyPoint>* kpts) const;
      virtual void signalMovedFrames(const VertexPoseSet& vset);
      virtual void setFrames(const VertexPoseSet& vset);
      virtual void updateMap();
      virtual void updateOverlappingScore(Eigen::Isometry3d& new_estimated_pose);
      virtual void setFrustum(const Eigen::Matrix3f& inverse_kcam, int width, int height,
                              float near_plane, float far_plane);

    private:
      void calcActiveWindow();

      struct SLAM_DUNK_API vertex_id
      {
        typedef VertexPoseConstPtr DataType;
        int operator()(const DataType& data) const { return data->id(); }
      };

      typedef QuadTree<VertexPoseConstPtr, vertex_id> QuadTreeType;

      QuadTreeType m_tree;
      double m_half_active_win_length;
      Eigen::Vector2d m_active_win_center, m_win_movement_step;
      Eigen::Isometry3d m_active_win_pose;
      //double m_min_pov_cosangle_th;
      float m_perc_feat_overlap;
      cv::Ptr<const cv::FeatureDetector> m_feature_detector;
      cv::Ptr<const cv::DescriptorExtractor> m_feature_extractor;
      cv::Ptr<const cv::Feature2D> m_feature_detector_extractor;
      bool m_use_detector_extractor;
      FeatureMatcher::Ptr m_matcher;
      //TODO SampleConsensus::Ptr m_outlier_rejection;
      std::map<int, VertexPoseConstPtr> m_reference_vxs;
      unsigned m_min_matches, m_max_feats_per_frame;
      bool m_verbose, m_debug;

      Eigen::Matrix<float,6,4,Eigen::RowMajor> m_frustum_mtx;
  };

}

#endif // SLAM_DUNK_CAMERA_TRACKER_H
