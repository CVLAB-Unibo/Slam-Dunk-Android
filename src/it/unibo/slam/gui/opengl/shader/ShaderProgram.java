package it.unibo.slam.gui.opengl.shader;

import it.unibo.slam.gui.opengl.utils.GlUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.opengl.GLES20;
import android.util.Log;

/**
 * Class representing a compiled shader program.
 */
public class ShaderProgram
{
	/**
	 * Tag used in error checks.
	 */
	private static String TAG = "ShaderProgram";
	
	/**
	 * Shader program id.
	 */
	private int id;
	
	/**
	 * Vertex shader.
	 */
	private VertexShader vertexShader;
	
	/**
	 * Fragment shader.
	 */
	private FragmentShader fragmentShader;
	
	/**
	 * Base constructor (accessible only through the creation methods).
	 * @param id The shader program id.
	 * @param vertexShader The vertex shader.
	 * @param fragmentShader The fragment shader.
	 */
	private ShaderProgram(int id, VertexShader vertexShader, FragmentShader fragmentShader)
	{
		this.id = id;
		this.vertexShader = vertexShader;
		this.fragmentShader = fragmentShader;
	}
	
	/**
	 * Creates a shader program.
	 * @param fileNameVertexShaderSource The file name of the vertex shader source.
	 * @param fileNameFragmentShaderSource The file name of the fragment shader source.
	 * @return The shader program.
	 * @throws IOException One of the files has not been found or an I/O error occurred while reading the files.
	 * @throws RuntimeException A loading error occurred.
	 */
	public static ShaderProgram createFromFile(String fileNameVertexShaderSource, String fileNameFragmentShaderSource) 
			throws IOException, RuntimeException
	{
		return createFromStream(new FileInputStream(fileNameVertexShaderSource), new FileInputStream(fileNameFragmentShaderSource));
	}
	
	/**
	 * Creates a shader program.
	 * @param fileVertexShaderSource File of the vertex shader.
	 * @param fileFragmentShaderSource File of the fragment shader.
	 * @return The shader program.
	 * @throws IOException An I/O error occurred while reading the files.
	 * @throws RuntimeException A loading error occurred.
	 */
	public static ShaderProgram createFromFile(File fileVertexShaderSource, File fileFragmentShaderSource) 
			throws IOException, RuntimeException
	{
		return createFromStream(new FileInputStream(fileVertexShaderSource), new FileInputStream(fileVertexShaderSource));
	}
	
	/**
	 * Creates a shader program.
	 * @param streamVertexShaderSource InputStream of the vertex shader.
	 * @param streamFragmentShaderSource InputStream of the fragment shader.
	 * @return The shader program.
	 * @throws IOException An I/O error occurred while reading the files.
	 * @throws RuntimeException A loading error occurred.
	 */
	public static ShaderProgram createFromStream(InputStream streamVertexShaderSource, InputStream streamFragmentShaderSource) 
			throws IOException, RuntimeException
	{
		String vertexShaderSource = buildTextFromStream(streamVertexShaderSource);
		String fragmentShaderSource = buildTextFromStream(streamFragmentShaderSource);
		
		return createFromSource(vertexShaderSource, fragmentShaderSource);
	}
	
	/**
	 * Creates a string from the content of a file.
	 * @param stream The InputStream used to read the file.
	 * @return The string created.
	 * @throws IOException An I/O error occurred while reading the file.
	 */
	private static String buildTextFromStream(InputStream stream) throws IOException
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		String line;
		StringBuilder stringBuilder = new StringBuilder();

		while ((line = reader.readLine()) != null)
		{
			stringBuilder.append(line);
			stringBuilder.append("\n");
		}
		
		reader.close();
		return stringBuilder.toString();
	}
	
	/**
	 * Loads the shader program using vertex and fragment shader sources.
	 * @param vertexShaderSource The vertex shader source contained in a string.
	 * @param fragmentShaderSource The fragment shader source contained in a string.
	 * @return The resulting shader program.
	 * @throws RuntimeException A loading error occurred.
	 */
	public static ShaderProgram createFromSource(String vertexShaderSource, String fragmentShaderSource) throws RuntimeException
	{
	    VertexShader vertexShader = VertexShader.load(vertexShaderSource);
	    FragmentShader fragmentShader = FragmentShader.load(fragmentShaderSource);
	    
	    int programId = GLES20.glCreateProgram();
	    if (programId != 0)
	    {
	        GLES20.glAttachShader(programId, vertexShader.getId());
	        GlUtils.checkGlError(TAG, "glAttachVertexShader");
	        GLES20.glAttachShader(programId, fragmentShader.getId());
	        GlUtils.checkGlError(TAG, "glAttachFragmentShader");
	        GLES20.glLinkProgram(programId);
	        int[] linkStatus = new int[1];
	        GLES20.glGetProgramiv(programId, GLES20.GL_LINK_STATUS, linkStatus, 0);
	        if (linkStatus[0] != GLES20.GL_TRUE)
	        {
	            Log.e(TAG, "Could not link program: ");
	            Log.e(TAG, GLES20.glGetProgramInfoLog(programId));
	            GLES20.glDeleteProgram(programId);
	            programId = 0;
	        }
	    }
	    
	    if (programId == 0)
			throw new RuntimeException("Error in loading the shader program");
	    
	    return new ShaderProgram(programId, vertexShader, fragmentShader);
	}
	
	/**
	 * Gets the shader program id.
	 * @return The shader program id.
	 */
	public int getId()
	{
		return id;
	}
	
	/**
	 * Gets the vertex shader.
	 * @return The vertex shader.
	 */
	public VertexShader getVertexShader()
	{
		return vertexShader;
	}
	
	/**
	 * Gets the fragment shader.
	 * @return The fragment shader.
	 */
	public FragmentShader getFragmentShader()
	{
		return fragmentShader;
	}
}
