package it.unibo.slam.gui.opengl.shader;

import it.unibo.slam.gui.opengl.shader.abstracts.Shader;
import android.opengl.GLES20;

/**
 * Vertex shader class.
 */
public class VertexShader extends Shader
{
	/**
	 * Base constructor (only accessible through loading methods).
	 * @param id The vertex shader id.
	 */
	private VertexShader(int id)
	{
		super(id);
	}
	
	/**
	 * Loads the vertex shader.
	 * @param source The shader source in a string.
	 * @return The vertex shader.
	 * @throws RuntimeException A loading error occurred.
	 */
	public static VertexShader load(String source) throws RuntimeException
	{
		int shaderId = loadShaderId(GLES20.GL_VERTEX_SHADER, source);
		
		if (shaderId == 0)
			throw new RuntimeException("Error in loading the vertex shader");
		
		return new VertexShader(shaderId);
	}
}
