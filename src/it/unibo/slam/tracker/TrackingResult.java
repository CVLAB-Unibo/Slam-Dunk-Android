package it.unibo.slam.tracker;

import java.util.List;

import it.unibo.slam.datatypes.FrameData;
import it.unibo.slam.datatypes.eigen.typedouble.EigenIsometry3D;
import it.unibo.slam.matcher.FrameToFrameMatch;

public class TrackingResult
{
	public EigenIsometry3D pose;
	
	public float overlappingScore;
	
	public FrameData frameData;
	
	public List<FrameToFrameMatch> ffMatches;
}
