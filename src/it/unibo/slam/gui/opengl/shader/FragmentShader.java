package it.unibo.slam.gui.opengl.shader;

import it.unibo.slam.gui.opengl.shader.abstracts.Shader;
import android.opengl.GLES20;

/**
 * Fragment shader class.
 */
public class FragmentShader extends Shader
{
	/**
	 * Base constructor (only accessible through loading methods).
	 * @param id The fragment shader id.
	 */
	private FragmentShader(int id)
	{
		super(id);
	}
	
	/**
	 * Loads the fragment shader.
	 * @param source The shader source in a string.
	 * @return The fragment shader.
	 * @throws RuntimeException A loading error occurred.
	 */
	public static FragmentShader load(String source) throws RuntimeException
	{
		int shaderId = loadShaderId(GLES20.GL_FRAGMENT_SHADER, source);
		
		if (shaderId == 0)
			throw new RuntimeException("Error in loading the fragment shader");
		
		return new FragmentShader(shaderId);
	}
}
