package it.unibo.slam.datatypes;

/**
 * Class representing RGB data.
 */
public class DataRGB
{
	/**
	 * RGB data (3 bytes is one pixel color).
	 */
	byte[] rgb;
	
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
	 * @param rgb RGB data.
	 * @param width Array width.
	 * @param height Array height.
	 */
	public DataRGB(byte[] rgb, int width, int height)
	{
		if (rgb == null)
			throw new IllegalArgumentException("One of the arguments is null.");
		
		if (rgb.length != width * height * 3)
			throw new IllegalArgumentException("The length of the RGB array is not equal to width * height * 3.");
		
		this.rgb = rgb;
		this.width = width;
		this.height = height;
	}
	
	/**
	 * Gets the RGB data.
	 * @return The RGB data.
	 */
	public byte[] getRGB()
	{
		return rgb;
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
