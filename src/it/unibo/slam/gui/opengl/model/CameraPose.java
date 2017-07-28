package it.unibo.slam.gui.opengl.model;

import it.unibo.slam.datatypes.geometry.Quaternion;
import it.unibo.slam.gui.opengl.model.interfaces.DrawableEntity;
import it.unibo.slam.gui.opengl.model.interfaces.ModifiableEntity;
import it.unibo.slam.gui.opengl.shader.CameraPoseShaderParams;
import it.unibo.slam.utils.Constants;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import android.opengl.GLES20;
import android.opengl.Matrix;

/**
 * Class representing a drawable axis.
 */
public class CameraPose implements DrawableEntity, ModifiableEntity
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
	 * Buffer for the vertex indices of the axis line.
	 */
	private IntBuffer indicesBufferLine;
	
	/**
	 * Buffer for the vertex indices of the axis base circle.
	 */
	private IntBuffer indicesBufferCircle;
	
	/**
	 * Buffer for the vertex indices of the axis tip.
	 */
	private IntBuffer indicesBufferTip;
	
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
	 * Id of the index line buffer in the array.
	 */
	private static int INDEX_BUFFER_LINE_ID = 2;
	
	/**
	 * Id of the index circle buffer in the array.
	 */
	private static int INDEX_BUFFER_CIRCLE_ID = 3;
	
	/**
	 * Id of the index tip buffer in the array.
	 */
	private static int INDEX_BUFFER_TIP_ID = 4;
	
	/**
	 * Number of buffers used.
	 */
	private static int BUFFERS_NUM = 5;
	
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
	 * @param attributes Shader attributes for this axis.
	 * @param slices Number of slices.
	 * @param axesLength The length of the drawn axes.
	 * @param arrowsRadius The radius of the arrows.
	 */
	public CameraPose(CameraPoseShaderParams attributes, int slices, float axesLength, float arrowsRadius,
			byte[] colorX, byte[] colorY, byte[] colorZ)
	{
		Matrix.setIdentityM(modelMatrix, 0);
		
		if (attributes != null)
		{
			vertexAttribute = attributes.getVertexAttribute();
			colorAttribute = attributes.getColorAttribute();
		}
		
		// The circle points, the tip point and the two line points
		float[] vertexArray = new float[(slices + 7) * 3];
		byte[] colorArray = new byte[(slices + 7) * 3];
		int[] indexArrayLine = new int[6];
		int[] indexArrayCircle = new int[slices];
		int[] indexArrayTip = new int[slices + 1];
		
		// Line start
		vertexArray[0] = 0.0F;
		vertexArray[1] = 0.0F;
		vertexArray[2] = 0.0F;
		colorArray[0] = colorZ[0];
		colorArray[1] = colorZ[1];
		colorArray[2] = colorZ[2];
		indexArrayLine[0] = 0;
		
		// Line end
		vertexArray[3] = 0.0F;
		vertexArray[4] = 0.0F;
		vertexArray[5] = 0.8F * axesLength;
		colorArray[3] = colorZ[0];
		colorArray[4] = colorZ[1];
		colorArray[5] = colorZ[2];
		indexArrayLine[1] = 1;
		
		// Circle
		float angleOffset = (float)(2 * Math.PI / slices), x = 0.0F, y = 0.0F;
		int index = 2 * 3;
		for (float currentAngle = 0.0F; currentAngle < (float)(2 * Math.PI); currentAngle += angleOffset, index += 3)
		{
			x = arrowsRadius * (float)Math.cos(currentAngle);
			y = arrowsRadius * (float)Math.sin(currentAngle);
			
			vertexArray[index + 0] = x;
			vertexArray[index + 1] = y;
			vertexArray[index + 2] = 0.8F * axesLength;
			colorArray[index + 0] = colorZ[0];
			colorArray[index + 1] = colorZ[1];
			colorArray[index + 2] = colorZ[2];
			
			indexArrayCircle[(index / 3) - 2] = index / 3;
			
			if (currentAngle == 0.0F)
				indexArrayTip[(index / 3) - 1] = index / 3;
			else 
				indexArrayTip[(index / 3) - 1] = 2 + slices - (index / 3 - 2);
		}
		
		// Tip
		vertexArray[index + 0] = 0.0F;
		vertexArray[index + 1] = 0.0F;
		vertexArray[index + 2] = axesLength;
		colorArray[index + 0] = colorZ[0];
		colorArray[index + 1] = colorZ[1];
		colorArray[index + 2] = colorZ[2];
		indexArrayTip[0] = index / 3;
		
		index += 3;
		
		// Line start
		vertexArray[index + 0] = 0.0F;
		vertexArray[index + 1] = 0.0F;
		vertexArray[index + 2] = 0.0F;
		colorArray[index + 0] = colorY[0];
		colorArray[index + 1] = colorY[1];
		colorArray[index + 2] = colorY[2];
		indexArrayLine[2] = index / 3;
		
		index += 3;
		
		// Line end
		vertexArray[index + 0] = 0.0F;
		vertexArray[index + 1] = axesLength;
		vertexArray[index + 2] = 0.0F;
		colorArray[index + 0] = colorY[0];
		colorArray[index + 1] = colorY[1];
		colorArray[index + 2] = colorY[2];
		indexArrayLine[3] = index / 3;
		
		index += 3;
		
		// Line start
		vertexArray[index + 0] = 0.0F;
		vertexArray[index + 1] = 0.0F;
		vertexArray[index + 2] = 0.0F;
		colorArray[index + 0] = colorX[0];
		colorArray[index + 1] = colorX[1];
		colorArray[index + 2] = colorX[2];
		indexArrayLine[4] = index / 3;
		
		index += 3;
		
		// Line end
		vertexArray[index + 0] = axesLength;
		vertexArray[index + 1] = 0.0F;
		vertexArray[index + 2] = 0.0F;
		colorArray[index + 0] = colorX[0];
		colorArray[index + 1] = colorX[1];
		colorArray[index + 2] = colorX[2];
		indexArrayLine[5] = index / 3;
		
		vertexBuffer = ByteBuffer	.allocateDirect(vertexArray.length * Constants.BYTES_PER_FLOAT)
									.order(ByteOrder.nativeOrder()).asFloatBuffer();
		vertexBuffer.put(vertexArray).position(0);
		
		colorBuffer = ByteBuffer.allocateDirect(colorArray.length)
								.order(ByteOrder.nativeOrder());
		colorBuffer.put(colorArray).position(0);
		
		indicesBufferLine = ByteBuffer	.allocateDirect(indexArrayLine.length * Constants.BYTES_PER_INTEGER)
										.order(ByteOrder.nativeOrder()).asIntBuffer();
		indicesBufferLine.put(indexArrayLine).position(0);
		
		indicesBufferCircle = ByteBuffer.allocateDirect(indexArrayCircle.length * Constants.BYTES_PER_INTEGER)
										.order(ByteOrder.nativeOrder()).asIntBuffer();
		indicesBufferCircle.put(indexArrayCircle).position(0);

		indicesBufferTip = ByteBuffer	.allocateDirect(indexArrayTip.length * Constants.BYTES_PER_INTEGER)
										.order(ByteOrder.nativeOrder()).asIntBuffer();
		indicesBufferTip.put(indexArrayTip).position(0);
		
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
		
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, buffersId[INDEX_BUFFER_LINE_ID]);
		GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, indicesBufferLine.capacity() * Constants.BYTES_PER_INTEGER, 
							indicesBufferLine, GLES20.GL_STATIC_DRAW);
		
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, buffersId[INDEX_BUFFER_CIRCLE_ID]);
		GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, indicesBufferCircle.capacity() * Constants.BYTES_PER_INTEGER, 
							indicesBufferCircle, GLES20.GL_STATIC_DRAW);
		
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, buffersId[INDEX_BUFFER_TIP_ID]);
		GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, indicesBufferTip.capacity() * Constants.BYTES_PER_INTEGER, 
							indicesBufferTip, GLES20.GL_STATIC_DRAW);
		
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
	}
	
	@Override
	public synchronized void draw()
	{
		if (destroyed)
			return;
		
		GLES20.glLineWidth(4);
		
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffersId[VERTEX_BUFFER_ID]);
		GLES20.glVertexAttribPointer(vertexAttribute, VERTEX_DATA_SIZE, GLES20.GL_FLOAT, false, 0, 0);
		GLES20.glEnableVertexAttribArray(vertexAttribute);
		
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffersId[COLOR_BUFFER_ID]);
		GLES20.glVertexAttribPointer(colorAttribute, COLOR_DATA_SIZE, GLES20.GL_UNSIGNED_BYTE, true, 0, 0);
		GLES20.glEnableVertexAttribArray(colorAttribute);
		
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, buffersId[INDEX_BUFFER_LINE_ID]);
		GLES20.glDrawElements(GLES20.GL_LINES, indicesBufferLine.capacity(), GLES20.GL_UNSIGNED_INT, 0);
		
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, buffersId[INDEX_BUFFER_CIRCLE_ID]);
		GLES20.glDrawElements(GLES20.GL_TRIANGLE_FAN, indicesBufferCircle.capacity(), GLES20.GL_UNSIGNED_INT, 0);
		
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, buffersId[INDEX_BUFFER_TIP_ID]);
		GLES20.glDrawElements(GLES20.GL_TRIANGLE_FAN, indicesBufferTip.capacity(), GLES20.GL_UNSIGNED_INT, 0);
		
		GLES20.glDisableVertexAttribArray(colorAttribute);
		GLES20.glDisableVertexAttribArray(vertexAttribute);
		
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
		
		GLES20.glLineWidth(1);
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
		indicesBufferLine.clear();
		indicesBufferCircle.clear();
		indicesBufferTip.clear();
		
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