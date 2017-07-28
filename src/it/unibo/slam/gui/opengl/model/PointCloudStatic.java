package it.unibo.slam.gui.opengl.model;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import android.opengl.GLES20;
import android.opengl.Matrix;

import it.unibo.slam.datatypes.eigen.typefloat.EigenMatrix4F;
import it.unibo.slam.datatypes.geometry.Quaternion;
import it.unibo.slam.gui.opengl.model.interfaces.DrawableEntity;
import it.unibo.slam.gui.opengl.model.interfaces.ModifiableEntity;
import it.unibo.slam.gui.opengl.shader.PointCloudShaderParams;
import it.unibo.slam.utils.Constants;

/**
 * Class representing a static point cloud (its data will not be modified after its creation).
 */
public class PointCloudStatic implements DrawableEntity, ModifiableEntity
{
	/**
	 * Amount of coordinates used to represent a single vertex position.
	 */
	private static int VERTEX_DATA_SIZE = 3;
	
	/**
	 * Amount of values used to represent a single vertex color.
	 */
	private static int COLOR_DATA_SIZE = 3;
	
	/**
	 * Buffer for the vertex positions.
	 */
	private FloatBuffer vertexBuffer;
	
	/**
	 * Buffer for the vertex colors.
	 */
	private ByteBuffer colorBuffer;
	
	/**
	 * Model matrix.
	 */
	private float[] modelMatrix = new float[16];
	
	/**
	 * Id of the vertex position buffer in the array.
	 */
	private static int VERTEX_BUFFER_ID = 0;
	
	/**
	 * Id of the vertex color buffer in the array.
	 */
	private static int COLOR_BUFFER_ID = 1;
	
	/**
	 * Number of buffers used.
	 */
	private static int BUFFERS_NUM = 2;
	
	/**
	 * Array containing the buffers id after their creation.
	 */
	private int[] buffersId;
	
	/**
	 * The vertex attribute (index for the shader).
	 */
	private int vertexAttribute;
	
	/**
	 * The color attribute (index for the shader).
	 */
	private int colorAttribute;
	
	/**
	 * True if the point cloud has been destroyed, false otherwise.
	 */
	private boolean destroyed = false;
	
	/**
	 * Base constructor.
	 * @param attributes Shader attributes for this point cloud.
	 * @param vertexArray The vertex array.
	 * @param colorArray The color array.
	 */
	public PointCloudStatic(PointCloudShaderParams attributes, float[] vertexArray, byte[] colorArray)
	{
		Matrix.setIdentityM(modelMatrix, 0);
		
		if (attributes != null)
		{
			vertexAttribute = attributes.getVertexAttribute();
			colorAttribute = attributes.getColorAttribute();
		}
		
		vertexBuffer = ByteBuffer	.allocateDirect(vertexArray.length * Constants.BYTES_PER_FLOAT)
									.order(ByteOrder.nativeOrder()).asFloatBuffer();
		vertexBuffer.put(vertexArray).position(0);
			
		colorBuffer = ByteBuffer.allocateDirect(colorArray.length)
								.order(ByteOrder.nativeOrder());
		colorBuffer.put(colorArray).position(0);
		
		if (attributes != null)
		{
			createVBOs();
			initVBOs();
			destroyed = false;
		}
		else
			destroyed = true;
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
		
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffersId[COLOR_BUFFER_ID]);
		GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, colorBuffer.capacity(), 
							colorBuffer, GLES20.GL_STATIC_DRAW);
		
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
	}
	
	/**
	 * Returns the transformed vertex array.
	 * @return The float array representing the vertices multiplied by the model matrix (XYZ format).
	 */
	public synchronized float[] getTransformedVertexArray()
	{
		vertexBuffer.position(0);
		
		EigenMatrix4F modelMatrixEigen = new EigenMatrix4F(modelMatrix);
		float[] array = new float[vertexBuffer.capacity()];
		vertexBuffer.get(array);
		
		// If the model matrix is the identity we don't need to transform the vertices
		if (modelMatrixEigen.equals(EigenMatrix4F.getIdentity()))
		{
			
		}
		// Apply the transformation to all the points
		else
		{
			reprojectPointCloudVertices(modelMatrixEigen.getValue(), array.length, array);
		}
		
		vertexBuffer.position(0);
		
		return array;
	}
	
	/**
	 * Returns the untransformed vertex array.
	 * @return The float array representing the vertices not multiplied by the model matrix (XYZ format).
	 */
	public synchronized float[] getUntransformedVertexArray()
	{
		vertexBuffer.position(0);
		float[] array = new float[vertexBuffer.capacity()];
		vertexBuffer.get(array);
		vertexBuffer.position(0);
		return array;
	}
	
	/**
	 * Returns the color array.
	 * @return The byte array representing the colors (RGB format).
	 */
	public synchronized byte[] getColorArray()
	{
		colorBuffer.position(0);
		byte[] array = new byte[colorBuffer.capacity()];
		colorBuffer.get(array);
		colorBuffer.position(0);
		return array;
	}
	
	/**
	 * Gets the number of points.
	 * @return The number of points.
	 */
	public int getNumberOfPoints()
	{
		return vertexBuffer.capacity() / VERTEX_DATA_SIZE;
	}
	
	/**
	 * Gets the vertex array length.
	 * @return The vertex array length.
	 */
	public int getVertexArrayLength()
	{
		return vertexBuffer.capacity();
	}
	
	/**
	 * Gets the color array length.
	 * @return The color array length.
	 */
	public int getColorArrayLength()
	{
		return colorBuffer.capacity();
	}
	
	@Override
	public synchronized void draw()
	{
		if (destroyed)
			return;
		
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffersId[VERTEX_BUFFER_ID]);
		GLES20.glVertexAttribPointer(vertexAttribute, VERTEX_DATA_SIZE, GLES20.GL_FLOAT, false, 0, 0);
		GLES20.glEnableVertexAttribArray(vertexAttribute);
		
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffersId[COLOR_BUFFER_ID]);
		GLES20.glVertexAttribPointer(colorAttribute, COLOR_DATA_SIZE, GLES20.GL_UNSIGNED_BYTE, true, 0, 0);
		GLES20.glEnableVertexAttribArray(colorAttribute);
		
		GLES20.glDrawArrays(GLES20.GL_POINTS, 0, vertexBuffer.capacity() / VERTEX_DATA_SIZE);
		
		GLES20.glDisableVertexAttribArray(colorAttribute);
		GLES20.glDisableVertexAttribArray(vertexAttribute);
		
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
	}
	
	@Override
	protected void finalize()
	{
		destroy();
	}
	
	/**
	 * Destroys this point cloud, deleting the VBOs.
	 */
	public void destroy()
	{
		if (destroyed)
			return;
		
		GLES20.glDeleteBuffers(BUFFERS_NUM, buffersId, 0);
		vertexBuffer.clear();
		colorBuffer.clear();
		
		destroyed = true;
	}

	@Override
	public void setTranslation(float translationX, float translationY, float translationZ)
	{
		modelMatrix[12] = translationX;
		modelMatrix[13] = translationY;
		modelMatrix[14] = translationZ;
		modelMatrix[15] = 1;
	}

	@Override
	public void setRotation(Quaternion quaternion)
	{
		setRotation(quaternion.toRotationMatrix().getValue());
	}

	@Override
	public void setRotation(float[] rotationMatrix)
	{
		for (int i = 0, x = 0; i < 9; i += 3, x += 4)
			for (int j = 0; j < 3; j++)
				modelMatrix[x + j] = rotationMatrix[i + j];
	}

	@Override
	public float[] getModelMatrix()
	{
		return modelMatrix;
	}
	
	@Override
	public void setModelMatrix(float[] modelMatrix)
	{
		this.modelMatrix = modelMatrix;
	}
	
	//TODO comment
	private native void reprojectPointCloudVertices(float[] transform, int pointArrayLength, float[] pointsInOut);
}