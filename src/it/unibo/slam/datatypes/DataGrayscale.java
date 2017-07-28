package it.unibo.slam.datatypes;

/**
 * Class representing grayscale data.
 */
public class DataGrayscale
{
	/**
	 * Grayscale data (each byte is one pixel color).
	 */
	byte[] grayscale;
	
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
	 * @param grayscale Grayscale data.
	 * @param width Array width.
	 * @param height Array height.
	 */
	public DataGrayscale(byte[] grayscale, int width, int height)
	{
		if (grayscale == null)
			throw new IllegalArgumentException("One of the arguments is null.");
		
		if (grayscale.length != width * height)
			throw new IllegalArgumentException("The length of the grayscale array is not equal to width * height.");
		
		this.grayscale = grayscale;
		this.width = width;
		this.height = height;
	}
	
	/**
	 * Gets the grayscale data.
	 * @return The grayscale data.
	 */
	public byte[] getGrayscale()
	{
		return grayscale;
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
