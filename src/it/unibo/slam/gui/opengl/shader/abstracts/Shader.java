package it.unibo.slam.gui.opengl.shader.abstracts;

import android.opengl.GLES20;
import android.util.Log;

/**
 * Generic shader class.
 */
public abstract class Shader
{
	/**
	 * Tag used in error checking.
	 */
	protected static String TAG = "Shader";
	
	/**
	 * Shader id.
	 */
	protected int id;
	
	/**
	 * Base abstract constructor.
	 * @param id The shader id.
	 */
	protected Shader(int id)
	{
		this.id = id;
	}
	
	/**
	 * Loads the shader and return its id.
	 * @param shaderType The shader type (vertex / fragment).
	 * @param source The source in a string.
	 * @return The shader id.
	 */
	protected static int loadShaderId(int shaderType, String source)
	{
		int shaderId = GLES20.glCreateShader(shaderType);
        if (shaderId != 0)
        {
            GLES20.glShaderSource(shaderId, source);
            GLES20.glCompileShader(shaderId);
            int[] compiled = new int[1];
            GLES20.glGetShaderiv(shaderId, GLES20.GL_COMPILE_STATUS, compiled, 0);
            if (compiled[0] == 0)
            {
                Log.e(TAG, "Could not compile shader " + shaderType + ":");
                Log.e(TAG, GLES20.glGetShaderInfoLog(shaderId));
                GLES20.glDeleteShader(shaderId);
                shaderId = 0;
            }
        }
        
        return shaderId;
	}
	
	/**
	 * Gets the shader id.
	 * @return The shader id.
	 */
	public int getId()
	{
		return id;
	}
}
