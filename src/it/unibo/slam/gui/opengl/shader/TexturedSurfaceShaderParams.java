package it.unibo.slam.gui.opengl.shader;

/**
 * Shader attributes associated with a single textured surface.
 */
public class TexturedSurfaceShaderParams
{
	/**
	 * The texture uniform parameter (index in the shader program).
	 */
	private int textureUniform;
	
	/**
	 * The vertex attribute (index in the shader program).
	 */
	private int vertexAttribute;
	
	/**
	 * The texture coordinate attribute (index in the shader program).
	 */
	private int textureCoordinateAttribute;
	
	/**
	 * Base constructor.
	 * @param textureUniform The texture uniform.
	 * @param vertexAttribute The vertex attribute.
	 * @param textureCoordinateAttribute The texture coordinate attribute.
	 */
	public TexturedSurfaceShaderParams(int textureUniform, int vertexAttribute, int textureCoordinateAttribute)
	{
		this.textureUniform = textureUniform;
		this.vertexAttribute = vertexAttribute;
		this.textureCoordinateAttribute = textureCoordinateAttribute;
	}
	
	/**
	 * Gets the texture uniform.
	 * @return The texture uniform.
	 */
	public int getTextureUniform()
	{
		return textureUniform;
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
	 * Gets the texture coordinate attribute.
	 * @return The texture coordinate attribute.
	 */
	public int getTextureCoordinateAttribute()
	{
		return textureCoordinateAttribute;
	}
	
	/**
	 * Sets the texture uniform.
	 * @param textureUniform The texture uniform to set.
	 */
	public void setTextureUniform(int textureUniform)
	{
		this.textureUniform = textureUniform;
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
	 * Sets the texture coordinate attribute.
	 * @param colorAttribute The texture coordinate attribute to set.
	 */
	public void setTextureCoordinateAttribute(int textureCoordinateAttribute)
	{
		this.textureCoordinateAttribute = textureCoordinateAttribute;
	}
}
