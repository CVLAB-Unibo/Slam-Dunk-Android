package it.unibo.slam.datatypes.geometry;

import it.unibo.slam.datatypes.eigen.typefloat.EigenMatrix3F;
import it.unibo.slam.datatypes.eigen.typefloat.EigenMatrix4F;
import it.unibo.slam.datatypes.eigen.typefloat.EigenVector3F;

/**
 * Helper class able to generate different kind of matrices, given certain parameters.
 */
public class MatrixFactory
{
	/**
	 * Computes the inverse of the camera matrix.
	 * @param focalX The x focal.
	 * @param focalY The y focal.
	 * @param centerX The x center.
	 * @param centerY The y center.
	 * @return The matrix (3x3).
	 */
	public static EigenMatrix3F getInverseKCam(float focalX, float focalY, float centerX, float centerY)
	{
		EigenMatrix3F inverseKCam = EigenMatrix3F.getIdentity();
		float invFocalX = 1.0F / focalX;
		float invFocalY = 1.0F / focalY;
		
		inverseKCam.set(invFocalX, 0, 0);
		inverseKCam.set(invFocalY, 1, 1);
		inverseKCam.set(centerX * -invFocalX, 0, 2);
		inverseKCam.set(centerY * -invFocalY, 1, 2);
		
		return inverseKCam;
	}
	
	/**
	 * Computes the inverse of the camera matrix and adds to it a row and a column (all zeros except a one in the last position).
	 * @param focalX The x focal.
	 * @param focalY The y focal.
	 * @param centerX The x center.
	 * @param centerY The y center.
	 * @return The matrix (4x4).
	 */
	public static EigenMatrix4F getInverseKCamExtended(float focalX, float focalY, float centerX, float centerY)
	{
		EigenMatrix4F inverseKCam = EigenMatrix4F.getIdentity();
		float invFocalX = 1.0F / focalX;
		float invFocalY = 1.0F / focalY;
		
		inverseKCam.set(invFocalX, 0, 0);
		inverseKCam.set(invFocalY, 1, 1);
		inverseKCam.set(centerX * -invFocalX, 0, 2);
		inverseKCam.set(centerY * -invFocalY, 1, 2);
		
		return inverseKCam;
	}
	
	/**
	 * Computes the rotation matrix around an arbitrary axis, given a specific angle.
	 * @param axis The axis.
	 * @param angle The angle.
	 * @return The rotation matrix.
	 */
	public static EigenMatrix3F getRotationMatrixAroundAxis(EigenVector3F axis, float angle)
	{
		float matrix[] = new float[9];
		float x = axis.getX(), y = axis.getY(), z = axis.getZ();
		float cosTheta = (float)Math.cos(angle);
		float oneMinusCosTheta = 1F - cosTheta;
		float sinTheta = (float)Math.sin(angle);
		float xSinTheta = x * sinTheta, ySinTheta = y * sinTheta, zSinTheta = z * sinTheta;
		float x2OneMinusCosTheta = x * x * oneMinusCosTheta, y2OneMinusCosTheta = y * y * oneMinusCosTheta,
				z2OneMinusCosTheta = z * z * oneMinusCosTheta;
		float xyOneMinusCosTheta = x * y * oneMinusCosTheta, xzOneMinusCosTheta = x * z * oneMinusCosTheta, 
				yzOneMinusCosTheta = y * z * oneMinusCosTheta;
		
		matrix[0] = cosTheta + x2OneMinusCosTheta;
		matrix[1] = xyOneMinusCosTheta + zSinTheta;
		matrix[2] = xzOneMinusCosTheta - ySinTheta;
		
		matrix[3] = xyOneMinusCosTheta - zSinTheta;
		matrix[4] = cosTheta + y2OneMinusCosTheta;
		matrix[5] = yzOneMinusCosTheta + xSinTheta;
		
		matrix[6] = xzOneMinusCosTheta + ySinTheta;
		matrix[7] = yzOneMinusCosTheta - xSinTheta;
		matrix[8] = cosTheta + z2OneMinusCosTheta;
		
		return new EigenMatrix3F(matrix);
	}
}
