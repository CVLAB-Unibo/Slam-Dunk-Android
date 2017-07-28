package it.unibo.slam.gui.opengl.shader;

/**
 * Shader attributes associated with a single point cloud.
 */
public class CameraPoseShaderParams
{
	/**
	 * The vertex attribute (index in the shader program).
	 */
	private int vertexAttribute;
	
	/**
	 * The color attribute (index in the shader program).
	 */
	private int colorAttribute;
	
	/**
	 * Base constructor.
	 * @param vertexAttribute The vertex attribute.
	 * @param colorAttribute The color attribute.
	 */
	public CameraPoseShaderParams(int vertexAttribute, int colorAttribute)
	{
		this.vertexAttribute = vertexAttribute;
		this.colorAttribute = colorAttribute;
	}
	
	/**
	 * Gets the vertex attribute.
	 * @return The vertex attribute.
	 */
	public int getVertexAttribute()
	{
		return vertexAttribute;
	}
	
	/**
	 * Gets the color attribute.
	 * @return The color attribute.
	 */
	public int getColorAttribute()
	{
		return colorAttribute;
	}
	
	/**
	 * Sets the vertex attribute.
	 * @param vertexAttribute The vertex attribute to set.
	 */
	public void setVertexAttribute(int vertexAttribute)
	{
		this.vertexAttribute = vertexAttribute;
	}
	
	/**
	 * Sets the color attribute.
	 * @param colorAttribute The color attribute to set.
	 */
	public void setColorAttribute(int colorAttribute)
	{
		this.colorAttribute = colorAttribute;
	}
}
