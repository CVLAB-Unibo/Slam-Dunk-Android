package it.unibo.slam.gui.opengl.shader;

/**
 * Shader attributes associated with a bounding box.
 */
public class BoundingBoxShaderParams
{
	/**
	 * The vertex attribute (index in the shader program).
	 */
	private int vertexAttribute;
	
	/**
	 * The color uniform (index in the shader program).
	 */
	private int colorUniform;
	
	/**
	 * Base constructor.
	 * @param vertexAttribute The vertex attribute.
	 * @param colorUniform The color uniform.
	 */
	public BoundingBoxShaderParams(int vertexAttribute, int colorUniform)
	{
		this.vertexAttribute = vertexAttribute;
		this.colorUniform = colorUniform;
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
	 * Gets the color uniform.
	 * @return The color uniform.
	 */
	public int getColorUniform()
	{
		return colorUniform;
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
	 * Sets the color uniform.
	 * @param colorAttribute The color uniform to set.
	 */
	public void setColorAttribute(int colorUniform)
	{
		this.colorUniform = colorUniform;
	}
}
