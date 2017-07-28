package it.unibo.slam.datatypes.params;

/**
 * Class representing the basic intrinsic parameters used by the application.<br>
 * Unused instrinsic parameters are not included in this class.
 */
public class IntrinsicParams
{
	/**
	 * Focal length on the x coordinate.
	 */
	private float focalX;
	
	/**
	 * Focal length on the y coordinate.
	 */
	private float focalY;
	
	/**
	 * Center of view on the x coordinate.
	 */
	private float centerX;
	
	/**
	 * Center of view on the y coordinate.
	 */
	private float centerY;

	/**
	 * Base constructor.
	 * @param focalX The focal length on the x coordinate.
	 * @param focalY The focal length on the y coordinate.
	 * @param centerX The center of view on the x coordinate.
	 * @param centerY The center of view on the y coordinate.
	 */
	public IntrinsicParams(float focalX, float focalY, float centerX, float centerY)
	{
		this.focalX = focalX;
		this.focalY = focalY;
		
		this.centerX = centerX;
		this.centerY = centerY;
	}
	
	/**
	 * Gets the focal length on the x coordinate.
	 * @return The focal length on the x coordinate.
	 */
	public float getFocalX()
	{
		return focalX;
	}

	/**
	 * Gets the focal length on the y coordinate.
	 * @return The focal length on the y coordinate.
	 */
	public float getFocalY()
	{
		return focalY;
	}

	/**
	 * Gets the center of view on the x coordinate.
	 * @return The center of view on the x coordinate.
	 */
	public float getCenterX()
	{
		return centerX;
	}

	/**
	 * Gets the center of view on the y coordinate.
	 * @return The center of view on the y coordinate.
	 */
	public float getCenterY()
	{
		return centerY;
	}
}
