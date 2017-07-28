
#include "slamdunk/include/slam_dunk.h"
#include "slamdunk/include/pretty_printer.h"
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <boost/timer/timer.hpp>

#include <android/log.h>

#include <time.h>

#define  LOG_TAG4	"slamdunk_app"
#define  LOGI4(...)	__android_log_print(ANDROID_LOG_INFO, LOG_TAG4, __VA_ARGS__)

/////////////////////////////////////////////////////////////////////////////////////////////
const slamdunk::FrameData* slamdunk::SlamDunk::getFrameData(int id)
{
	return m_graph_ptr->getVertexData(id);
}

/////////////////////////////////////////////////////////////////////////////////////////////
double slamdunk::SlamDunk::lastWeightedMeanChi2()
{
	return m_last_wmchi2;
}

/////////////////////////////////////////////////////////////////////////////////////////////
unsigned slamdunk::SlamDunk::getNumberOfKeyframes()
{
	return m_graph_ptr->getNoOfVertices();
}

float slamdunk::SlamDunk::getResidual()
{
	return m_residual;
}

int slamdunk::SlamDunk::executeTracking(const RGBDFrame& frame, Eigen::Isometry3d& estimated_pose)
{
	int tracking_state = SlamDunk::TRACKING_FAILED;
	if(!m_graph_ptr) // This is the first frame
	{
		// Extract frame data
	    FrameData* frame_data = new FrameData();
	    m_tracker->extractFrameData(frame, *frame_data);

	    // Check no of feats
	    if(frame_data->m_descriptors.rows < m_min_extracted_features)
	    {
	    	std::cout << SLAM_DUNK_WARNING_STR("Too few feature extracted, first frame not set") << std::endl;
	    	delete frame_data;
	    }
	    else
	    {
	    	tracking_state = SlamDunk::KEYFRAME_DETECTED;

	    	// create a new vertex and set its frame data
	    	m_graph_ptr.reset(new GraphBackend());
	    	m_graph_ptr->setVerbose(m_verbose);

	    	estimated_pose.setIdentity();
	    	m_graph_ptr->setVertexEstimate(m_processed_frames, estimated_pose, frame_data);

	    	if(m_try_loop_inference)
	    		m_dijkstra.reset(new g2o::HyperDijkstra(&(m_graph_ptr->getGraph())));

	    	m_moved_frames.clear();
	    	CameraTracker::VertexPoseSet pose_set;
	    	const g2o::SparseOptimizer& optimizer = m_graph_ptr->getGraph();
	    	for(g2o::HyperGraph::VertexIDMap::const_iterator it = optimizer.vertices().begin(); it != optimizer.vertices().end(); ++it)
	    	{
	    		const CameraTracker::VertexPoseSet::const_iterator psIt = pose_set.insert(static_cast<const g2o::VertexSE3*>(it->second)).first;
	    		m_moved_frames.push_back(std::make_pair(static_cast<const FrameData*>((*psIt)->userData())->m_timestamp, (*psIt)->estimate()));
	    	}

	    	m_tracker->reset(estimated_pose, *frame_data);
	    	m_tracker->setFrames(pose_set);
	    	if(m_deb_func)
	    		m_deb_func(m_moved_frames);
	    }
	}
	else
	{
		if (m_tracker->track(frame))
		{
			tracking_state = SlamDunk::FRAME_TRACKED;

			const TrackingResult& tr = m_tracker->getTrackingResult();
			estimated_pose = tr.m_pose;

			std::stringstream ss;
			ss << "OVERLAPPING SCORE: " << tr.m_overlapping_score;
			LOGI4("%s", ss.str().data());

			m_residual = 0.0F;
			Eigen::Isometry3d other_pose;
			Eigen::Vector4f actual_keypoint, other_keypoint, tempVec;
			float totalScore = 0.0F, matchingDifference = 0.0F;
			std::vector<Eigen::Vector3f> keypoints = tr.m_frame_data.m_keypoints;
			std::vector<FrameToFrameMatch> matches = tr.m_ff_matches;
			for (int i = 0; i < matches.size(); i++)
			{
				m_graph_ptr->getVertexEstimate(matches[i].m_matching_frame_id, other_pose);
				std::vector<Eigen::Vector3f> other_keypoints = m_graph_ptr->getVertexData(matches[i].m_matching_frame_id)->m_keypoints;

				actual_keypoint = Eigen::Vector4f(	keypoints[matches[i].m_ref_frame_feat].x(),
													keypoints[matches[i].m_ref_frame_feat].y(),
													keypoints[matches[i].m_ref_frame_feat].z(), 1);
				other_keypoint = Eigen::Vector4f(	other_keypoints[matches[i].m_matching_frame_feat].x(),
													other_keypoints[matches[i].m_matching_frame_feat].y(),
													other_keypoints[matches[i].m_matching_frame_feat].z(), 1);

				tempVec = estimated_pose.cast<float>() * actual_keypoint - other_pose.cast<float>() * other_keypoint;
				matchingDifference = tempVec.squaredNorm();
				m_residual += matches[i].m_score * matchingDifference;
				totalScore += matches[i].m_score;
			}
			if (totalScore != 0.0F)
				m_residual /= totalScore;
		}
		else
		{
			if (m_verbose)
				std::cout << SLAM_DUNK_WARNING_STR("Tracking failure") << std::endl;
		}
	}

	return tracking_state;
}

int slamdunk::SlamDunk::executeOptimization(Eigen::Isometry3d& estimated_pose)
{
	int tracking_state = SlamDunk::FRAME_TRACKED;

	m_tracker->updateOverlappingScore(estimated_pose);
	const TrackingResult& tr = m_tracker->getTrackingResult();

	std::stringstream ss;
	ss << "OVERLAPPING SCORE MODIFIED: " << tr.m_overlapping_score;
	LOGI4("%s", ss.str().data());

	// Keyframe detection
	bool is_keyframe = tr.m_overlapping_score < m_kf_overlapping;// || tr.m_frame_data.m_descriptors.rows < m_min_extracted_features)
	if(!is_keyframe && m_try_loop_inference) // check if a loop closure has happened
	{
		const std::set<int>& tracked_keyframes = m_tracker->getLastTrackedKeyframes();
		std::set<int>::const_iterator tkfIt = tracked_keyframes.begin();

		std::vector<g2o::HyperGraph::Vertex*> tracked_vertices(tracked_keyframes.size());
		for(unsigned k = 0; k < tracked_vertices.size(); ++tkfIt, ++k)
			tracked_vertices[k] = m_graph_ptr->getGraph().vertex(*tkfIt);

		g2o::UniformCostFunction edge_cost;
		for(unsigned k = 0; k < tracked_vertices.size() && !is_keyframe; ++k)
		{
			g2o::HyperGraph::Vertex* vx = tracked_vertices[k];
			m_dijkstra->shortestPaths(vx, &edge_cost, /*max distance*/m_rba_rings);
			const g2o::HyperGraph::VertexSet& visited = m_dijkstra->visited();

			for(unsigned k2 = k; k2 < tracked_vertices.size() && !is_keyframe; ++k2)
				is_keyframe = visited.count(tracked_vertices[k2]) == 0;
		}

		if(is_keyframe)
		{
			//std::cout << "METRIC LOOP DETECTED!" << std::endl;
			LOGI4("%s", "METRIC LOOP DETECTED!");
		}
	}

	if(is_keyframe)
	{
		tracking_state = SlamDunk::KEYFRAME_DETECTED;

		boost::timer::cpu_timer stopwatch;
		if(m_verbose)
			std::cout 	<< SLAM_DUNK_INFO_STR("New keyframe detected #") << m_processed_frames
						<< " (" << (m_graph_ptr->getNoOfVertices()+1) << " kfs)" << std::endl;
		FrameData* frameDataPtr = new FrameData(tr.m_frame_data);
		g2o::VertexSE3* current_vx = m_graph_ptr->setVertexEstimate(m_processed_frames, estimated_pose, frameDataPtr);
		if(m_try_loop_inference)
		{
			g2o::HyperDijkstra::AdjacencyMapEntry entry(current_vx, 0,0,std::numeric_limits< double >::max());
			m_dijkstra->adjacencyMap().insert(std::make_pair(entry.child(), entry));
		}

		// RBA optimization
		g2o::HyperGraph::VertexSet vSet, fSet;
		m_graph_ptr->addFrameMatchesXYZ(m_processed_frames, tr.m_ff_matches, NULL);
		stopwatch.start();
		clock_t start = clock();
		const int total_its = m_graph_ptr->relativeOptimization(m_processed_frames, m_rba_rings, 100, &vSet, &fSet);
		stopwatch.stop();
		clock_t end = clock();
		std::stringstream ss;
		ss << "OPTIMIZE: " << (1000.0F * (float)(end - start) / (float)CLOCKS_PER_SEC) << " ITS: " << total_its;
		LOGI4("%s", ss.str().data());
		if(m_verbose)
			std::cout 	<< SLAM_DUNK_INFO_STR("RBA optimization performed (")
						<< total_its << " iterations)";

		m_last_wmchi2 = m_graph_ptr->weightedMeanChi2();
		if(m_verbose)
			std::cout << " >>> " << stopwatch.format() << std::endl;

		// get solution
		m_moved_frames.clear();
		CameraTracker::VertexPoseSet pose_set;
		for(g2o::HyperGraph::VertexSet::const_iterator it = vSet.begin(); it != vSet.end(); ++it)
		{
			const CameraTracker::VertexPoseSet::const_iterator psIt = pose_set.insert(static_cast<const g2o::VertexSE3*>(*it)).first;
			m_moved_frames.push_back(std::make_pair(static_cast<const FrameData*>((*psIt)->userData())->m_timestamp, (*psIt)->estimate()));
		}
		// signal moved frames
		m_tracker->signalMovedFrames(pose_set);
		m_graph_ptr->getVertexEstimate(m_processed_frames, estimated_pose);

		if(m_deb_func)
		{
			m_deb_func(m_moved_frames);
		}
	}
	else
		m_tracker->updateMap();

	if(tracking_state != SlamDunk::TRACKING_FAILED)
		++m_processed_frames;

	return tracking_state;
}

/////////////////////////////////////////////////////////////////////////////////////////////
int slamdunk::SlamDunk::operator()(const RGBDFrame& frame, Eigen::Isometry3d& estimated_pose)
{
  int tracking_state = SlamDunk::TRACKING_FAILED;
  if(!m_graph_ptr) // This is the first frame
  {
    // extract frame data
    FrameData* frame_data = new FrameData();
    m_tracker->extractFrameData(frame, *frame_data);

    // check no of feats
    if(frame_data->m_descriptors.rows < m_min_extracted_features)
    {
      std::cout << SLAM_DUNK_WARNING_STR("Too few feature extracted, first frame not set") << std::endl;
      delete frame_data;
    }
    else
    {
      tracking_state = SlamDunk::KEYFRAME_DETECTED;

      // create a new vertex and set its frame data
      m_graph_ptr.reset(new GraphBackend());
      m_graph_ptr->setVerbose(m_verbose);

      estimated_pose.setIdentity();
      m_graph_ptr->setVertexEstimate(m_processed_frames, estimated_pose, frame_data);

      if(m_try_loop_inference)
        m_dijkstra.reset(new g2o::HyperDijkstra(&(m_graph_ptr->getGraph())));

      m_moved_frames.clear();
      CameraTracker::VertexPoseSet pose_set;
      const g2o::SparseOptimizer& optimizer = m_graph_ptr->getGraph();
      for(g2o::HyperGraph::VertexIDMap::const_iterator it = optimizer.vertices().begin(); it != optimizer.vertices().end(); ++it)
      {
        const CameraTracker::VertexPoseSet::const_iterator psIt = pose_set.insert(static_cast<const g2o::VertexSE3*>(it->second)).first;
        m_moved_frames.push_back(std::make_pair(static_cast<const FrameData*>((*psIt)->userData())->m_timestamp, (*psIt)->estimate()));
      }

      m_tracker->reset(estimated_pose, *frame_data);
      m_tracker->setFrames(pose_set);
      if(m_deb_func)
        m_deb_func(m_moved_frames);
    }
  }
  else
  {
    if(m_tracker->track(frame))
    {
      tracking_state = SlamDunk::FRAME_TRACKED;

      const TrackingResult& tr = m_tracker->getTrackingResult();
      estimated_pose = tr.m_pose;

      std::stringstream ss;
      ss << "OVERLAPPING SCORE: " << tr.m_overlapping_score;
      LOGI4("%s", ss.str().data());

      // Keyframe detection
      bool is_keyframe = tr.m_overlapping_score < m_kf_overlapping;// || tr.m_frame_data.m_descriptors.rows < m_min_extracted_features)
      if(!is_keyframe && m_try_loop_inference) // check if a loop closure has happened
      {
        const std::set<int>& tracked_keyframes = m_tracker->getLastTrackedKeyframes();
        std::set<int>::const_iterator tkfIt = tracked_keyframes.begin();

        std::vector<g2o::HyperGraph::Vertex*> tracked_vertices(tracked_keyframes.size());
        for(unsigned k = 0; k < tracked_vertices.size(); ++tkfIt, ++k)
          tracked_vertices[k] = m_graph_ptr->getGraph().vertex(*tkfIt);

        g2o::UniformCostFunction edge_cost;
        for(unsigned k = 0; k < tracked_vertices.size() && !is_keyframe; ++k)
        {
          g2o::HyperGraph::Vertex* vx = tracked_vertices[k];
          m_dijkstra->shortestPaths(vx, &edge_cost, /*max distance*/m_rba_rings);
          const g2o::HyperGraph::VertexSet& visited = m_dijkstra->visited();

          for(unsigned k2 = k; k2 < tracked_vertices.size() && !is_keyframe; ++k2)
            is_keyframe = visited.count(tracked_vertices[k2]) == 0;
        }

        if(is_keyframe)
        {
          //std::cout << "METRIC LOOP DETECTED!" << std::endl;
          LOGI4("%s", "METRIC LOOP DETECTED!");
        }
      }

      if(is_keyframe)
      {
        tracking_state = SlamDunk::KEYFRAME_DETECTED;

        boost::timer::cpu_timer stopwatch;
        if(m_verbose)
          std::cout << SLAM_DUNK_INFO_STR("New keyframe detected #") << m_processed_frames
                    << " (" << (m_graph_ptr->getNoOfVertices()+1) << " kfs)" << std::endl;
        FrameData* frameDataPtr = new FrameData(tr.m_frame_data);
        g2o::VertexSE3* current_vx = m_graph_ptr->setVertexEstimate(m_processed_frames, estimated_pose, frameDataPtr);
        if(m_try_loop_inference)
        {
          g2o::HyperDijkstra::AdjacencyMapEntry entry(current_vx, 0,0,std::numeric_limits< double >::max());
          m_dijkstra->adjacencyMap().insert(std::make_pair(entry.child(), entry));
        }

        // RBA optimization
        g2o::HyperGraph::VertexSet vSet, fSet;
        m_graph_ptr->addFrameMatchesXYZ(m_processed_frames, tr.m_ff_matches, NULL);
        stopwatch.start();
        clock_t start = clock();
        const int total_its = m_graph_ptr->relativeOptimization(m_processed_frames, m_rba_rings, 100, &vSet, &fSet);
        stopwatch.stop();
        clock_t end = clock();
        std::stringstream ss;
        ss << "OPTIMIZE: " << (1000.0F * (float)(end - start) / (float)CLOCKS_PER_SEC) << " ITS: " << total_its;
        LOGI4("%s", ss.str().data());
        if(m_verbose)
          std::cout << SLAM_DUNK_INFO_STR("RBA optimization performed (")
                    << total_its << " iterations)";

        m_last_wmchi2 = m_graph_ptr->weightedMeanChi2();
        if(m_verbose)
          std::cout << " >>> " << stopwatch.format() << std::endl;

        // get solution
        m_moved_frames.clear();
        CameraTracker::VertexPoseSet pose_set;
        for(g2o::HyperGraph::VertexSet::const_iterator it = vSet.begin(); it != vSet.end(); ++it)
        {
          const CameraTracker::VertexPoseSet::const_iterator psIt = pose_set.insert(static_cast<const g2o::VertexSE3*>(*it)).first;
          m_moved_frames.push_back(std::make_pair(static_cast<const FrameData*>((*psIt)->userData())->m_timestamp, (*psIt)->estimate()));
        }
        // signal moved frames
        m_tracker->signalMovedFrames(pose_set);
        m_graph_ptr->getVertexEstimate(m_processed_frames, estimated_pose);

        if(m_deb_func)
        {
        #ifdef UNIX
          //system(("echo \"" + graphToDot(m_graph_ptr.get(), m_processed_frames, fSet, vSet, "GlobalGraph") + "\" | circo -Tsvg | display").c_str());
          system(("echo \"" + graphToDot(m_graph_ptr.get(), m_processed_frames, fSet, vSet, "GlobalGraph") + "\" | circo -Tsvg | display").c_str());
        #endif
          m_deb_func(m_moved_frames);
        }
      } else
        m_tracker->updateMap();
    } else if(m_verbose)
      std::cout << SLAM_DUNK_WARNING_STR("Tracking failure") << std::endl;
  }

  if(tracking_state != SlamDunk::TRACKING_FAILED)
    ++m_processed_frames;
  return tracking_state;
}

//////////////////////////////////////////////////////////////////////////////////////////////////////////
void slamdunk::SlamDunk::forceGlobalOptimization()
{
//  m_graph_ptr->setFixedView(0,true);
  boost::timer::cpu_timer stopwatch;
  const int total_its = m_graph_ptr->optimize(100);
  stopwatch.stop();
//  m_graph_ptr->setFixedView(0,false);
  m_last_wmchi2 = m_graph_ptr->weightedMeanChi2();

  if(m_verbose)
    std::cout << SLAM_DUNK_INFO_STR("Global optimization performed (")
              << total_its << " iterations)"
              << " >>> " << stopwatch.format() << std::endl;

  // get solution
  m_moved_frames.clear();
  CameraTracker::VertexPoseSet pose_set;
  for(g2o::HyperGraph::VertexIDMap::const_iterator it = m_graph_ptr->getGraph().vertices().begin();
      it != m_graph_ptr->getGraph().vertices().end(); ++it)
  {
    const CameraTracker::VertexPoseSet::const_iterator psIt = pose_set.insert(static_cast<const g2o::VertexSE3*>(it->second)).first;
    m_moved_frames.push_back(std::make_pair(static_cast<const FrameData*>((*psIt)->userData())->m_timestamp, (*psIt)->estimate()));
  }
  // signal moved frames
  m_tracker->signalMovedFrames(pose_set);
}

//////////////////////////////////////////////////////////////////////////////////////////////////////////
void slamdunk::SlamDunk::getMappedPoses(StampedPoseVector& poses) const
{
  for(g2o::HyperGraph::VertexIDMap::const_iterator it = m_graph_ptr->getGraph().vertices().begin();
        it != m_graph_ptr->getGraph().vertices().end(); ++it)
  {
    const g2o::VertexSE3* vx = static_cast<const g2o::VertexSE3*>(it->second);
    poses.push_back(std::make_pair(static_cast<const FrameData*>(vx->userData())->m_timestamp, vx->estimate()));
  }
}
