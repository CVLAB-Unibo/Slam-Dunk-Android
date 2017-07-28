package it.unibo.slam.datatypes;

/**
 * Class representing BGR-D data.
 */
public class DataBGRD
{
	/**
	 * Timestamp of the data.
	 */
	private double timestamp;
	
	/**
	 * The BGR data stored into an array of bytes (3 bytes for a color).
	 */
	private byte[] bgr;
	
	/**
	 * The depth data stored into an array of floats.
	 */
	private float[] depth;
	
	/**
	 * Data width.
	 */
	private int width;
	
	/**
	 * Data height.
	 */
	private int height;
	
	/**
	 * Max depth allowed.
	 */
	private float maxDepth;
	
	/**
	 * Basic constructor.
	 * @param timestamp The timestamp.
	 * @param bgr BGR data.
	 * @param depth Depth data.
	 * @param width Array width.
	 * @param height Array height.
	 * @param maxDepth Max depth allowed.
	 */
	public DataBGRD(double timestamp, byte[] bgr, float[] depth, int width, int height, float maxDepth)
	{
		if (bgr == null || depth == null)
			throw new IllegalArgumentException("One of the arguments is null.");
		
		if (bgr.length != (depth.length * 3))
			throw new IllegalArgumentException("The two arrays must have a proportional size.");
		
		if (depth.length != width * height)
			throw new IllegalArgumentException("The length of the depth array is not equal to width * height.");
		
		this.timestamp = timestamp;
		
		this.bgr = bgr;
		this.depth = depth;
		
		this.width = width;
		this.height = height;
		
		this.maxDepth = maxDepth;
	}
	
	/**
	 * Gets the timestamp.
	 * @return The timestamp.
	 */
	public double getTimestamp()
	{
		return timestamp;
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
	 * Gets the depth data.
	 * @return The depth data.
	 */
	public float[] getDepth()
	{
		return depth;
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
	
	/**
	 * Gets the max depth.
	 * @return The max depth.
	 */
	public float getMaxDepth()
	{
		return maxDepth;
	}
}
