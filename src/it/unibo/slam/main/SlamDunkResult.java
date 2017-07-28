package it.unibo.slam.main;

/**
 * Result in the execution of SlamDunk.
 */
public enum SlamDunkResult
{
	/**
	 * Tracking failed (usually because too few matches have been found).
	 */
	TRACKING_FAILED, 
	
	/**
	 * Frame tracked (but is not a keyframe).
	 */
	FRAME_TRACKED, 
	
	/**
	 * Keyframe detected.
	 */
	KEYFRAME_DETECTED;
}
