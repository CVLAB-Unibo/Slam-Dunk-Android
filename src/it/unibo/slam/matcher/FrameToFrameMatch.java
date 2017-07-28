package it.unibo.slam.matcher;

/**
 * Frame to frame match struct.
 */
public class FrameToFrameMatch
{
	/**
	 * Train image index.
	 */
	public int imgIdx;
	
	/**
	 * Train descriptor index.
	 */
	public int trainIdx;
	
	/**
	 * Query descriptor index.
	 */
	public int queryIdx;
	
	/**
	 * Match score.
	 */
	public float score;
}
