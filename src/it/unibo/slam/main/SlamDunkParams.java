package it.unibo.slam.main;

import it.unibo.slam.tracker.interfaces.CameraTracker;

/**
 * Class representing the SlamDunk algorithm's parameters.
 */
public class SlamDunkParams
{
	public CameraTracker cameraTracker;
	
	public int minimumExtractedFeatures;
	
	public int rbaRings;
	
	public int cores;
	
	public float keyframeOverlapping;
	
	public boolean tryLoopInference;
	
	/**
	 * Constructor with default values (tracker passed as parameter).
	 * @param cameraTracker The tracker.
	 */
	public SlamDunkParams(CameraTracker cameraTracker)
	{
		this.cameraTracker = cameraTracker;
		minimumExtractedFeatures = 30;
		rbaRings = 3;
		cores = 4;
		keyframeOverlapping = 0.7F;
		tryLoopInference = true;
	}
}