package it.unibo.slam.gui.opengl.model;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import it.unibo.slam.gui.opengl.model.interfaces.DrawableEntity;
import it.unibo.slam.gui.opengl.shader.TexturedSurfaceShaderParams;
import it.unibo.slam.utils.Constants;

/**
 * Class representing a static point cloud (its data will not be modified after its creation).
 */
public class TexturedSurface implements DrawableEntity
{
	/**
	 * Amount of coordinates used to represent a single vertex position.
	 */
	private static int VERTEX_DATA_SIZE = 2;
	
	/**
	 * Amount of values used to represent a single texture coordinate.
	 */
	private static int TEXTURE_COORDINATE_DATA_SIZE = 2;
	
	/**
	 * Buffer for the vertex positions.
	 */
	private FloatBuffer vertexBuffer;
	
	/**
	 * Buffer for the texture coordinates.
	 */
	private FloatBuffer textureCoordinateBuffer;
	
	/**
	 * Id of the vertex position buffer in the array.
	 */
	private static int VERTEX_BUFFER_ID = 0;
	
	/**
	 * Id of the texture coordinate buffer in the array.
	 */
	private static int TEXTURE_COORDINATE_BUFFER_ID = 1;
	
	/**
	 * Number of buffers used.
	 */
	private static int BUFFERS_NUM = 2;
	
	/**
	 * The texture number.
	 */
	private int textureNumber;
	
	/**
	 * Array containing the buffers id after their creation.
	 */
	private int[] buffersId;
	
	/**
	 * Array containing the texture id after its creation.
	 */
	private int[] textureId;
	
	/**
	 * The texture uniform parameter indicating the id of this texture (index for the shader).
	 */
	private int textureUniform;
	
	/**
	 * The vertex attribute (index for the shader).
	 */
	private int vertexAttribute;
	
	/**
	 * The texture coordinate attribute (index for the shader).
	 */
	private int textureCoordinateAttribute;
	
	/**
	 * True if a texture has been set, false otherwise.
	 */
	private boolean hasTexture = false;
	
	/**
	 * True if the texture has been destroyed, false otherwise.
	 */
	private boolean destroyed = false;
	
	/**
	 * Base constructor.
	 * @param attributes Shader attributes for this textured surface.
	 * @param textureNumber The texture number.
	 * @param vertexArray The vertex array.
	 * @param textureCoordinateArray The texture coordinate array.
	 */
	public TexturedSurface(TexturedSurfaceShaderParams attributes, int textureNumber, float[] vertexArray, float[] textureCoordinateArray)
	{
		textureUniform = attributes.getTextureUniform();
		vertexAttribute = attributes.getVertexAttribute();
		textureCoordinateAttribute = attributes.getTextureCoordinateAttribute();
		
		this.textureNumber = textureNumber;
		
		vertexBuffer = ByteBuffer	.allocateDirect(vertexArray.length * Constants.BYTES_PER_FLOAT)
									.order(ByteOrder.nativeOrder()).asFloatBuffer();
		vertexBuffer.put(vertexArray).position(0);
			
		textureCoordinateBuffer = ByteBuffer.allocateDirect(textureCoordinateArray.length * Constants.BYTES_PER_FLOAT)
											.order(ByteOrder.nativeOrder()).asFloatBuffer();
		textureCoordinateBuffer.put(textureCoordinateArray).position(0);
		
		createTexture();
		
		createVBOs();
		initVBOs();
		
		destroyed = false;
	}
	
	/**
	 * Creates the texture.
	 */
	private void createTexture()
	{
		textureId = new int[1];
		GLES20.glGenTextures(1, textureId, 0);
	}
	
	/**
	 * Creates the vertex buffer objects.
	 */
	private void createVBOs()
	{
		buffersId = new int[BUFFERS_NUM];
		GLES20.glGenBuffers(BUFFERS_NUM, buffersId, 0);
	}
	
	/**
	 * Initializes the vertex buffer objects.
	 */
	private void initVBOs()
	{
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffersId[VERTEX_BUFFER_ID]);
		GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexBuffer.capacity() * Constants.BYTES_PER_FLOAT, 
							vertexBuffer, GLES20.GL_STATIC_DRAW);
		
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffersId[TEXTURE_COORDINATE_BUFFER_ID]);
		GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, textureCoordinateBuffer.capacity() * Constants.BYTES_PER_FLOAT, 
							textureCoordinateBuffer, GLES20.GL_STATIC_DRAW);
		
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
	}
	
	/**
	 * Sets the bitmap that will be used as the texture content.
	 * @param textureBitmap The texture bitmap.
	 */
	public void setTexture(Bitmap textureBitmap)
	{
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId[0]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, textureBitmap, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
		
		hasTexture = true;
	}
	
	/**
	 * Clears the texture, disabling its drawing.
	 */
	public void clearTexture()
	{
		hasTexture = false;
	}
	
	@Override
	public void draw()
	{
		if (destroyed || !hasTexture)
			return;
		
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + textureNumber);
	    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId[0]);
	    GLES20.glUniform1i(textureUniform, textureNumber);
		
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffersId[VERTEX_BUFFER_ID]);
		GLES20.glVertexAttribPointer(vertexAttribute, VERTEX_DATA_SIZE, GLES20.GL_FLOAT, false, 0, 0);
		GLES20.glEnableVertexAttribArray(vertexAttribute);
		
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffersId[TEXTURE_COORDINATE_BUFFER_ID]);
		GLES20.glVertexAttribPointer(textureCoordinateAttribute, TEXTURE_COORDINATE_DATA_SIZE, GLES20.GL_FLOAT, false, 0, 0);
		GLES20.glEnableVertexAttribArray(textureCoordinateAttribute);
		
		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexBuffer.capacity() / VERTEX_DATA_SIZE);
		
		GLES20.glDisableVertexAttribArray(textureCoordinateAttribute);
		GLES20.glDisableVertexAttribArray(vertexAttribute);
		
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
		
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
	}
	
	@Override
	protected void finalize()
	{
		destroy();
	}
	
	/**
	 * Destroys this texture, deleting the VBOs.
	 */
	public void destroy()
	{
		if (destroyed)
			return;
		
		GLES20.glDeleteBuffers(BUFFERS_NUM, buffersId, 0);
		vertexBuffer.clear();
		textureCoordinateBuffer.clear();
		
		destroyed = true;
	}
}