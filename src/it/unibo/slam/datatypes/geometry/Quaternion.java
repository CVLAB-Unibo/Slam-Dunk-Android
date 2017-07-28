package it.unibo.slam.datatypes.geometry;

import it.unibo.slam.datatypes.eigen.typefloat.EigenMatrix3F;

/**
 * Class representing a quaternion.
 */
public class Quaternion
{
	/**
	 * X coordinate.
	 */
	private float x;
	
	/**
	 * Y coordinate.
	 */
	private float y;
	
	/**
	 * Z coordinate.
	 */
	private float z;
	
	/**
	 * W coordinate.
	 */
	private float w;
	
	/**
	 * Base constructor.
	 * @param x The x coordinate.
	 * @param y The y coordinate.
	 * @param z The z coordinate.
	 * @param w The w coordinate.
	 */
	public Quaternion(float x, float y, float z, float w)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}
	
	/**
	 * Gets the x coordinate.
	 * @return The x coordinate.
	 */
	public float getX()
	{
		return x;
	}
	
	/**
	 * Gets the y coordinate.
	 * @return The y coordinate.
	 */
	public float getY()
	{
		return y;
	}
	
	/**
	 * Gets the z coordinate.
	 * @return The z coordinate.
	 */
	public float getZ()
	{
		return z;
	}
	
	/**
	 * Gets the w coordinate.
	 * @return The w coordinate.
	 */
	public float getW()
	{
		return w;
	}
	
	/**
	 * Sets the x coordinate.
	 * @param x The x coordinate to set.
	 */
	public void setX(float x)
	{
		this.x = x;
	}
	
	/**
	 * Sets the y coordinate.
	 * @param y The y coordinate to set.
	 */
	public void setY(float y)
	{
		this.y = y;
	}
	
	/**
	 * Sets the z coordinate.
	 * @param z The z coordinate to set.
	 */
	public void setZ(float z)
	{
		this.z = z;
	}
	
	/**
	 * Sets the w coordinate.
	 * @param w The w coordinate to set.
	 */
	public void setW(float w)
	{
		this.w = w;
	}
	
	/**
	 * Converts the quaternion to a rotation matrix (column major).
	 * @return The rotation matrix (column major).
	 */
	public EigenMatrix3F toRotationMatrix()
	{
		/*
		
			|       2     2                                |
	        | 1 - 2Y  - 2Z    2XY - 2WZ      2XZ + 2WY     |
	        |                                              |
	        |                       2     2                |
	    M = | 2XY + 2WZ       1 - 2X  - 2Z   2YZ - 2WX     |
	        |                                              |
	        |                                      2     2 |
	        | 2XZ - 2WY       2YZ + 2WX      1 - 2X  - 2Y  |
	        |                                              |
			
		 */
		
		float[] rotationMatrix = new float[9];
		
		float x2 = x + x;
		float y2 = y + y;
		float z2 = z + z;
		
		float xx2 = x * x2;
		float yy2 = y * y2;
		float zz2 = z * z2;
		
		rotationMatrix[0] = 1 - yy2 - zz2;
		rotationMatrix[4] = 1 - xx2 - zz2;
		rotationMatrix[8] = 1 - xx2 - yy2;
		
		float xy2 = x * y2;
		float wz2 = w * z2;
		
		rotationMatrix[3] = xy2 - wz2;
		rotationMatrix[1] = xy2 + wz2;
		
		float xz2 = x * z2;
		float wy2 = w * y2;
		
		rotationMatrix[6] = xz2 + wy2;
		rotationMatrix[2] = xz2 - wy2;
		
		float yz2 = y * z2;
		float wx2 = w * x2;
		
		rotationMatrix[7] = yz2 - wx2;
		rotationMatrix[5] = yz2 + wx2;
		
		return new EigenMatrix3F(rotationMatrix);
	}
	
	/**
	 * Converts a rotation matrix into a quaternion.
	 * @param rotationMatrix The rotation matrix.
	 * @return The resulting quaternion.
	 */
	public static Quaternion fromRotationMatrix(EigenMatrix3F rotationMatrix)
	{
		Quaternion quaternion = new Quaternion(0, 0, 0, 0);
		
		float trace = rotationMatrix.trace();
		
		if (trace > 0.0F)
		{
			float S = (float)Math.sqrt(1.0F + trace) * 2.0F;
			float revS = 1 / S;
			
			quaternion.setX((rotationMatrix.get(2, 1) - rotationMatrix.get(1, 2)) * revS);
			quaternion.setY((rotationMatrix.get(0, 2) - rotationMatrix.get(2, 0)) * revS);
			quaternion.setZ((rotationMatrix.get(1, 0) - rotationMatrix.get(0, 1)) * revS);
			quaternion.setW(0.25F * S);
		}
		else if (rotationMatrix.get(0, 0) > rotationMatrix.get(1, 1) && rotationMatrix.get(0, 0) > rotationMatrix.get(2, 2))
		{
			float S = (float)Math.sqrt(1.0F + rotationMatrix.get(0, 0) - rotationMatrix.get(1, 1) - rotationMatrix.get(2, 2)) * 2.0F;
			float revS = 1 / S;
			
			quaternion.setX(0.25F * S);
			quaternion.setY((rotationMatrix.get(0, 1) + rotationMatrix.get(1, 0)) * revS);
			quaternion.setZ((rotationMatrix.get(0, 2) + rotationMatrix.get(2, 0)) * revS);
			quaternion.setW((rotationMatrix.get(2, 1) - rotationMatrix.get(1, 2)) * revS);
		}
		else if (rotationMatrix.get(1, 1) > rotationMatrix.get(2, 2))
		{
			float S = (float)Math.sqrt(1.0F + rotationMatrix.get(1, 1) - rotationMatrix.get(0, 0) - rotationMatrix.get(2, 2)) * 2.0F;
			float revS = 1 / S;
			
			quaternion.setX((rotationMatrix.get(0, 1) + rotationMatrix.get(1, 0)) * revS);
			quaternion.setY(0.25F * S);
			quaternion.setZ((rotationMatrix.get(1, 2) + rotationMatrix.get(2, 1)) * revS);
			quaternion.setW((rotationMatrix.get(0, 2) - rotationMatrix.get(2, 0)) * revS);
		}
		else
		{
			float S = (float)Math.sqrt(1.0F + rotationMatrix.get(2, 2) - rotationMatrix.get(0, 0) - rotationMatrix.get(1, 1)) * 2.0F;
			float revS = 1 / S;
			
			quaternion.setX((rotationMatrix.get(0, 2) + rotationMatrix.get(2, 0)) * revS);
			quaternion.setY((rotationMatrix.get(1, 2) + rotationMatrix.get(2, 1)) * revS);
			quaternion.setZ(0.25F * S);
			quaternion.setW((rotationMatrix.get(1, 0) - rotationMatrix.get(0, 1)) * revS);
		}
		
		return quaternion;
	}
}
