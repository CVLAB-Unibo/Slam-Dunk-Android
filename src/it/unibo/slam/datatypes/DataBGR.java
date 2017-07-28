package it.unibo.slam.datatypes;

/**
 * Class representing BGR data.
 */
public class DataBGR
{
	/**
	 * BGR data (3 bytes is one pixel color).
	 */
	byte[] bgr;
	
	/**
	 * Data width.
	 */
	private int width;
	
	/**
	 * Data height.
	 */
	private int height;
	
	/**
	 * Basic constructor.
	 * @param bgr BGR data.
	 * @param width Array width.
	 * @param height Array height.
	 */
	public DataBGR(byte[] bgr, int width, int height)
	{
		if (bgr == null)
			throw new IllegalArgumentException("One of the arguments is null.");
		
		if (bgr.length != width * height * 3)
			throw new IllegalArgumentException("The length of the BGR array is not equal to width * height * 3.");
		
		this.bgr = bgr;
		this.width = width;
		this.height = height;
	}
	
	/**
	 * Gets the BGR data.
	 * @return The BGR data.
	 */
	public byte[] getBGR()
	{
		return bgr;
	}
	
	/**
	 * Gets the width.
	 * @return The width.
	 */
	public int getWidth()
	{
		return width;
	}
	
	/**
	 * Gets the height.
	 * @return The height.
	 */
	public int getHeight()
	{
		return height;
	}
}
