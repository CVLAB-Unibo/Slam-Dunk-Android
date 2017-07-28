package it.unibo.slam.gui.opengl.model;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import android.opengl.GLES20;
import android.opengl.Matrix;

import it.unibo.slam.datatypes.eigen.typefloat.EigenVector3F;
import it.unibo.slam.datatypes.geometry.Quaternion;
import it.unibo.slam.gui.opengl.model.interfaces.DrawableEntity;
import it.unibo.slam.gui.opengl.model.interfaces.ModifiableEntity;
import it.unibo.slam.gui.opengl.shader.BoundingBoxShaderParams;
import it.unibo.slam.utils.Constants;

/**
 * Class representing a drawable bounding box.
 */
public class BoundingBox implements DrawableEntity, ModifiableEntity
{
	/**
	 * Amount of coordinates used to represent a single vertex position.
	 */
	private static int VERTEX_DATA_SIZE = 3;
	
	/**
	 * Buffer for the vertex positions.
	 */
	private FloatBuffer vertexBuffer;
	
	/**
	 * Buffer for the vertex indices.
	 */
	private IntBuffer indicesBuffer;
	
	/**
	 * Model matrix.
	 */
	private float[] modelMatrix = new float[16];
	
	/**
	 * Id of the vertex position buffer in the array.
	 */
	private static int VERTEX_BUFFER_ID = 0;
	
	/**
	 * Id of the index buffer in the array.
	 */
	private static int INDEX_BUFFER_ID = 1;
	
	/**
	 * Number of buffers used.
	 */
	private static int BUFFERS_NUM = 2;
	
	/**
	 * Array containing the buffer id after its creation.
	 */
	private int[] buffersId;
	
	/**
	 * The vertex attribute (index for the shader).
	 */
	private int vertexAttribute;
	
	/**
	 * The color uniform parameter (index for the shader).
	 */
	private int colorUniform;
	
	/**
	 * Minimum point of the bounding box.
	 */
	private EigenVector3F min;
	
	/**
	 * Maximum point of the bounding box.
	 */
	private EigenVector3F max;
	
	/**
	 * Color of the bounding box (expressed in float format).
	 */
	private EigenVector3F colorF;
	
	/**
	 * True if the bounding box has been destroyed, false otherwise.
	 */
	private boolean destroyed = false;
	
	/**
	 * Base constructor.
	 * @param attributes Bounding box's attributes.
	 * @param pointMin Bounding box's minimum point.
	 * @param pointMax Bounding box's maximum point.
	 * @param color Bounding box's color.
	 */
	public BoundingBox(BoundingBoxShaderParams attributes, float[] pointMin, float[] pointMax, float[] color)
	{
		Matrix.setIdentityM(modelMatrix, 0);
		
		if (attributes != null)
		{
			vertexAttribute = attributes.getVertexAttribute();
			colorUniform = attributes.getColorUniform();
		}
		
		float[] vertexArray = new float[8 * 3];
		for (int i = 0, j = 0; j < 8; i += 3, j++)
		{
			float[] actualVals = new float[3];
			
			if (j == 0 || j == 2 || j == 4 || j == 6)
				actualVals[0] = pointMin[0];
			else
				actualVals[0] = pointMax[0];
			
			if (j == 0 || j == 1 || j == 4 || j == 5)
				actualVals[1] = pointMin[1];
			else
				actualVals[1] = pointMax[1];
			
			if (j == 0 || j == 1 || j == 2 || j == 3)
				actualVals[2] = pointMin[2];
			else
				actualVals[2] = pointMax[2];
			
			vertexArray[i] = actualVals[0];
			vertexArray[i + 1] = actualVals[1];
			vertexArray[i + 2] = actualVals[2];
		}
		
		vertexBuffer = ByteBuffer	.allocateDirect(vertexArray.length * Constants.BYTES_PER_FLOAT)
									.order(ByteOrder.nativeOrder()).asFloatBuffer();
		vertexBuffer.put(vertexArray).position(0);
		
		int[] indexArray = new int[]
		{
			0, 1,
			0, 2,
			0, 4,
			1, 3,
			1, 5,
			2, 3,
			2, 6,
			3, 7,
			4, 5,
			4, 6,
			5, 7,
			6, 7
		};
		
		indicesBuffer = ByteBuffer	.allocateDirect(indexArray.length * Constants.BYTES_PER_INTEGER)
									.order(ByteOrder.nativeOrder()).asIntBuffer();
		indicesBuffer.put(indexArray).position(0);
		
		min = new EigenVector3F(pointMin.clone());
		max = new EigenVector3F(pointMax.clone());
		colorF = new EigenVector3F(color.clone());
		
		if (attributes != null)
		{
			createVBO();
			initVBO();
			destroyed = false;
		}
		else
			destroyed = true;
	}
	
	/**
	 * Creates the vertex buffer object.
	 */
	private void createVBO()
	{
		buffersId = new int[BUFFERS_NUM];
		GLES20.glGenBuffers(BUFFERS_NUM, buffersId, 0);
	}
	
	/**
	 * Initializes the vertex buffer object.
	 */
	private void initVBO()
	{
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffersId[VERTEX_BUFFER_ID]);
		GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexBuffer.capacity() * Constants.BYTES_PER_FLOAT, 
							vertexBuffer, GLES20.GL_STATIC_DRAW);
		
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, buffersId[INDEX_BUFFER_ID]);
		GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, indicesBuffer.capacity() * Constants.BYTES_PER_INTEGER, 
							indicesBuffer, GLES20.GL_STATIC_DRAW);
		
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
	}
	
	/**
	 * Gets the minimum point of the bounding box.
	 * @return The minimum point.
	 */
	public EigenVector3F getMin()
	{
		return min;
	}
	
	/**
	 * Gets the maximum point of the bounding box.
	 * @return The maximum point.
	 */
	public EigenVector3F getMax()
	{
		return max;
	}
	
	/**
	 * Gets the color of the bounding box.
	 * @return The color.
	 */
	public EigenVector3F getColor()
	{
		return colorF;
	}
	
	@Override
	public void draw()
	{
		if (destroyed)
			return;
		
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffersId[VERTEX_BUFFER_ID]);
		GLES20.glVertexAttribPointer(vertexAttribute, VERTEX_DATA_SIZE, GLES20.GL_FLOAT, false, 0, 0);
		GLES20.glEnableVertexAttribArray(vertexAttribute);
		
		GLES20.glUniform4f(colorUniform, colorF.getX(), colorF.getY(), colorF.getZ(), 1.0F);
		
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, buffersId[INDEX_BUFFER_ID]);
		GLES20.glDrawElements(GLES20.GL_LINES, indicesBuffer.capacity(), GLES20.GL_UNSIGNED_INT, 0);
		
		GLES20.glDisableVertexAttribArray(vertexAttribute);
		
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
	}
	
	@Override
	protected void finalize()
	{
		destroy();
	}
	
	/**
	 * Destroys this bounding box, deleting the VBOs.
	 */
	public void destroy()
	{
		if (destroyed)
			return;
		
		GLES20.glDeleteBuffers(BUFFERS_NUM, buffersId, 0);
		vertexBuffer.clear();
		indicesBuffer.clear();
		
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
}