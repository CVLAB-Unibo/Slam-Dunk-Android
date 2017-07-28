package it.unibo.slam.export;

/**
 * Output of the exporting algorithm. Only primitive types have been used to improve portability.
 */
public class ExportData
{
	/**
	 * Vertices of the point cloud to export.
	 */
	private float[] vertexArray;
	
	/**
	 * Colors of the point cloud to export.
	 */
	private byte[] colorArray;
	
	/**
	 * Minimum point of the bounding box.
	 */
	private float[] boundingBoxMin;
	
	/**
	 * Maximum point of the bounding box.
	 */
	private float[] boundingBoxMax;
	
	/**
	 * Base constructor.
	 * @param vertexArray Point cloud vertices.
	 * @param colorArray Point cloud colors.
	 * @param boundingBoxMin Minimum point of the bounding box.
	 * @param boundingBoxMax Maximum point of the bounding box.
	 */
	public ExportData(float[] vertexArray, byte[] colorArray, float[] boundingBoxMin, float[] boundingBoxMax)
	{
		this.vertexArray = vertexArray;
		this.colorArray = colorArray;
		this.boundingBoxMin = boundingBoxMin;
		this.boundingBoxMax = boundingBoxMax;
	}

	/**
	 * Gets the vertices of the point cloud.
	 * @return The vertices of the point cloud.
	 */
	public float[] getVertexArray()
	{
		return vertexArray;
	}

	/**
	 * Gets the colors of the point cloud.
	 * @return The colors of the point cloud.
	 */
	public byte[] getColorArray()
	{
		return colorArray;
	}

	/**
	 * Gets the minimum point of the bounding box.
	 * @return The minimum point of the bounding box.
	 */
	public float[] getBoundingBoxMin()
	{
		return boundingBoxMin;
	}

	/**
	 * Gets the maximum point of the bounding box.
	 * @return The maximum point of the bounding box.
	 */
	public float[] getBoundingBoxMax()
	{
		return boundingBoxMax;
	}
}
