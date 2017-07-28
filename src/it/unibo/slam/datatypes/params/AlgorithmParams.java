package it.unibo.slam.datatypes.params;

/**
 * Class representing the algorithm parameters (from the settings).
 */
public class AlgorithmParams
{
	/**
	 * Levels considered in the Relative Bundle Adjustment operation.
	 */
	private int rbaRings;
	
	/**
	 * Value used in keyframe overlapping check.
	 */
	private float keyframeOverlapping;
	
	/**
	 * If true it enables the loop check.
	 */
	private boolean tryLoopInference;
	
	/**
	 * Active window length.
	 */
	private float activeWindowLength;
	
	/**
	 * If true it enables the debugging of the algorithm.
	 */
	private boolean debugAlgorithm;
	
	/**
	 * Base constructor.
	 * @param rbaRings The number of levels considered in the RBA operation.
	 * @param keyframeOverlapping The keyframe overlapping threshold.
	 * @param tryLoopInference True if the loop check will be enabled, false otherwise.
	 * @param activeWindowLength The active window length.
	 * @param debugAlgorithm True if the debugging of the algorithm will be enabled, false otherwise.
	 */
	public AlgorithmParams(	int rbaRings, float keyframeOverlapping, boolean tryLoopInference,
							float activeWindowLength, boolean debugAlgorithm)
	{
		this.rbaRings = rbaRings;
		this.keyframeOverlapping = keyframeOverlapping;
		this.tryLoopInference = tryLoopInference;
		this.activeWindowLength = activeWindowLength;
		this.debugAlgorithm = debugAlgorithm;
	}

	/**
	 * Gets the RBA rings.
	 * @return The RBA rings.
	 */
	public int getRbaRings()
	{
		return rbaRings;
	}

	/**
	 * Gets the keyframe overlapping threshold.
	 * @return The keyframe overlapping threshold.
	 */
	public float getKeyframeOverlapping()
	{
		return keyframeOverlapping;
	}

	/**
	 * Checks if the control of loop is enabled.
	 * @return True if the loop check is enabled, false otherwise.
	 */
	public boolean isTryLoopInference()
	{
		return tryLoopInference;
	}

	/**
	 * Gets the active window length.
	 * @return The active window length.
	 */
	public float getActiveWindowLength()
	{
		return activeWindowLength;
	}

	/**
	 * Checks if the algorithm debugging is enabled.
	 * @return True if the algorithm debugging is enabled, false otherwise.
	 */
	public boolean isDebugAlgorithm()
	{
		return debugAlgorithm;
	}
}
