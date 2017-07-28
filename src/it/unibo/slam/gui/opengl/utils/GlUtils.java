package it.unibo.slam.gui.opengl.utils;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

/**
 * OpenGL utilities.
 */
public class GlUtils 
{
	/**
	 * Checks if an OpenGL error happened.
	 * @param tag Tag.
	 * @param op Method in which the error appears.
	 */
	public static void checkGlError(String tag, String op)
	{
	    int error;
	    while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR)
	    {
	        Log.e(tag, op + ": glError " + error);
	        throw new RuntimeException(op + ": glError " + error);
	    }
	}
	
	/**
	 * Creates a perspective matrix, given certain parameters.
	 * @param fovy Field of view (in degrees).
	 * @param aspect Aspect ratio.
	 * @param near Near plane distance.
	 * @param far Far plane distance.
	 * @return
	 */
	public static float[] gluPerspectiveMatrix(float fovy, float aspect, float near, float far)
	{
		float[] matrix = new float[16];
		
		float top = near * (float)Math.tan(Math.toRadians(fovy) / 2), bottom = -top;
		float right = aspect * top, left = -right;
		
		Matrix.frustumM(matrix, 0, left, right, bottom, top, near, far);
		
		return matrix;
	}
}
