package it.unibo.slam.datatypes;

/**
 * Class representing FPS data.
 */
public class DataFPS
{
	/**
	 * The current FPS.
	 */
	private float fps;
	
	/**
	 * Basic constructor.
	 * @param fps Current measured frames per second.
	 */
	public DataFPS(float fps)
	{
		this.fps = fps;
	}
	
	/**
	 * Gets the current FPS.
	 * @return The current FPS.
	 */
	public float getFPS()
	{
		return fps;
	}
}