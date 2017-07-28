
#ifndef SLAM_DUNK_SLAM_DUNK_H
#define SLAM_DUNK_SLAM_DUNK_H

#include "slamdunk/include/slamdunk_defines.h"
#include "slamdunk/include/camera_tracker.h"
#include <g2o/core/hyper_dijkstra.h>
#include <boost/scoped_ptr.hpp>
#include <boost/function.hpp>
#include <Eigen/StdVector>

namespace slamdunk
{

  struct SLAM_DUNK_API SlamDunkParams
  {
    CameraTracker::Ptr tracker;
    unsigned min_extracted_features, rba_rings;
    int cores;
    float kf_overlapping; // [0,1], the lower it is, the more distant kfs are
    bool try_loop_inference;
    bool verbose, debug;

    // default values (you may want to create the tracker then)
    SlamDunkParams()
      : min_extracted_features(30u), rba_rings(3), cores(4),
        kf_overlapping(/*0.5f*/0.7f), try_loop_inference(/*false*/true), verbose(false), debug(false) {}
  };

  class SLAM_DUNK_API SlamDunk
  {
    public:
      enum // return states
      {
        TRACKING_FAILED = 0,
        FRAME_TRACKED,
        KEYFRAME_DETECTED
      };

    public:
      const Eigen::Matrix3f m_inverse_kcam;

      SlamDunk(const Eigen::Matrix3f& inverse_kcam, const SlamDunkParams& params = SlamDunkParams())
        : m_inverse_kcam(inverse_kcam), m_tracker(params.tracker),
          m_min_extracted_features((int)params.min_extracted_features),
          m_kf_overlapping(params.kf_overlapping), m_processed_frames(0),
          m_rba_rings(params.rba_rings), m_cores(params.cores),
          m_last_wmchi2(-1.), m_try_loop_inference(params.try_loop_inference),
          m_verbose(params.verbose), m_debug(params.debug)
      {}

      typedef std::vector<std::pair<double, Eigen::Isometry3d>,
                          Eigen::aligned_allocator< std::pair<double, Eigen::Isometry3d> > > StampedPoseVector;
      typedef boost::function<void (const StampedPoseVector& poses)> debugging_function;

      /** \brief The debugging function is called whenever a new keyframe is detected */
      inline void setDebuggingFunction(const debugging_function& f) { m_deb_func = f; }
      inline void unsetDebuggingFunction() { m_deb_func.clear(); }

      int operator()(const RGBDFrame& frame, Eigen::Isometry3d& estimated_pose);
      int operator()(const RGBDFrame& frame) { Eigen::Isometry3d fake; return (*this)(frame, fake); }

      int executeTracking(const RGBDFrame& frame, Eigen::Isometry3d& estimated_pose);
      int executeOptimization(Eigen::Isometry3d& estimated_pose);

      void forceGlobalOptimization();

      void getMappedPoses(StampedPoseVector& poses) const;
      const StampedPoseVector& getMovedFrames() const { return m_moved_frames; }
      /// A wrapper to access frame data (thread safe)
      const FrameData* getFrameData(int id);
      /// A wrapper to get the residual from the last optimization (thread safe)
      double lastWeightedMeanChi2();
      unsigned getNumberOfKeyframes();

      float getResidual();

      CameraTracker::Ptr m_tracker;

    private:
      typedef cvflann::Index< cvflann::L2<float> > FLANNIndex;
      typedef boost::shared_ptr< cvflann::Index< cvflann::L2<float> > > FLANNIndexPtr;

      //TODO CameraTracker::Ptr m_tracker;
      GraphBackend::Ptr m_graph_ptr;
      boost::scoped_ptr<g2o::HyperDijkstra> m_dijkstra;
      StampedPoseVector m_moved_frames;
      int m_min_extracted_features; // will be compared to cv::Mat::rows which is int
      float m_kf_overlapping;
      float m_residual;
      unsigned m_processed_frames, m_rba_rings;
      int m_cores;
      double m_last_wmchi2;
      bool m_try_loop_inference;
      bool m_verbose, m_debug;
      debugging_function m_deb_func;
  };

}

#endif // SLAM_DUNK_SLAM_DUNK_H
