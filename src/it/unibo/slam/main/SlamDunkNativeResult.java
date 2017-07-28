package it.unibo.slam.main;

/**
 * Class used to represent the enumerative resulting from the execution of the algorithm.<br>
 * It is referred only to the native algorithm, while the one written in Java has it's own separated enumerative.
 */
public final class SlamDunkNativeResult
{
	/**
	 * Tracking failed (usually because too few matches have been found).
	 */
	public static final int TRACKING_FAILED = 0;
	
	/**
	 * Frame tracked (but is not a keyframe).
	 */
	public static final int FRAME_TRACKED = 1;
	
	/**
	 * Keyframe detected.
	 */
	public static final int KEYFRAME_DETECTED = 2;
}
