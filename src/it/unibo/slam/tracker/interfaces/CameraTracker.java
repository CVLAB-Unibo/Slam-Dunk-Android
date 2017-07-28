package it.unibo.slam.tracker.interfaces;

import java.util.Map;
import java.util.Set;

import it.unibo.slam.datatypes.DataBGRD;
import it.unibo.slam.datatypes.FrameData;
import it.unibo.slam.datatypes.eigen.typedouble.EigenIsometry3D;
import it.unibo.slam.datatypes.g2o.PoseVertex;
import it.unibo.slam.tracker.TrackingResult;

public interface CameraTracker
{
	public boolean track(DataBGRD frame);
	
	public void extractFrameData(DataBGRD frame, FrameData frameData);
	
	public void signalMovedFrames(Map<Integer, PoseVertex> poses);
	
	public void setFrames(Map<Integer, PoseVertex> poses);
	
	public void updateMap();

	public void reset(EigenIsometry3D pose, FrameData frameData);
	
	public TrackingResult getLastTrackingResult();
	
	public Set<Integer> getLastTrackedKeyframes();
}
