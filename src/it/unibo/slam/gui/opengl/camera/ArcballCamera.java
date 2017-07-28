package it.unibo.slam.gui.opengl.camera;

import android.opengl.Matrix;
import it.unibo.slam.datatypes.eigen.typefloat.EigenMatrix3F;
import it.unibo.slam.datatypes.eigen.typefloat.EigenVector3F;
import it.unibo.slam.datatypes.geometry.MatrixFactory;
import it.unibo.slam.gui.opengl.camera.interfaces.Camera;

/**
 * Implementation of an arcball camera with axis vectors.
 */
public class ArcballCamera implements Camera
{
	/**
	 * The camera matrix.
	 */
	private float[] cameraMatrix;
	
	/**
	 * If true, the camera matrix need to be recomputed.
	 */
	private boolean recomputeMatrix;
	
	/**
	 * Eye position.
	 */
	private EigenVector3F eye;
	
	/**
	 * Center of view.
	 */
	private EigenVector3F center;
	
	/**
	 * Up vector.
	 */
	private EigenVector3F up;
	
	/**
	 * Initial eye position (for incremental rotation).
	 */
	private EigenVector3F eyeInit;
	
	/**
	 * Initial up vector (for incremental rotation).
	 */
	private EigenVector3F upInit;
	
	/**
	 * Base constructor.
	 * @param eye The eye position.
	 * @param center The center of view.
	 * @param up The up vector.
	 */
	public ArcballCamera(EigenVector3F eye, EigenVector3F center, EigenVector3F up)
	{
		cameraMatrix = new float[4 * 4];
		this.eye = eye;
		this.eyeInit = eye;
		this.center = center;
		this.up = up;
		this.upInit = up;
		Matrix.setLookAtM(	cameraMatrix, 0, eye.getX(), eye.getY(), eye.getZ(), center.getX(), center.getY(), center.getZ(), 
							up.getX(), up.getY(), up.getZ());
		recomputeMatrix = false;
	}
	
	@Override
	public void rotateIncremental(float yaw, float pitch, float roll)
	{
		EigenVector3F dirInv = eyeInit.subExternal(center);
		EigenVector3F right = upInit.cross(dirInv).normalize();
		
		// Rotate calculation
		EigenMatrix3F yawRotation = MatrixFactory.getRotationMatrixAroundAxis(up, yaw);
		EigenMatrix3F pitchRotation = MatrixFactory.getRotationMatrixAroundAxis(right, pitch);
		EigenMatrix3F rollRotation = MatrixFactory.getRotationMatrixAroundAxis(dirInv.normalized().multiplyScalar(-1), roll);
		EigenMatrix3F yawPitchRotation = new EigenMatrix3F();
		EigenMatrix3F pitchRollRotation = new EigenMatrix3F();
		EigenVector3F dirInvMoved = new EigenVector3F();
		EigenVector3F upMoved = new EigenVector3F();
		
		yawRotation.multiplyWith(pitchRotation, yawPitchRotation);
		yawPitchRotation.multiplyWith(dirInv, dirInvMoved);
		
		pitchRotation.multiplyWith(rollRotation, pitchRollRotation);
		pitchRollRotation.multiplyWith(upInit, upMoved);
		
		// Move eye position and up vector
		dirInvMoved.add(center);
		eye = dirInvMoved;
		
		EigenVector3F dir = center.subExternal(eye).normalize();
		right = dir.cross(upMoved.normalized()).normalize();
		up = upMoved.normalize();
		
		recomputeMatrix = true;
	}
	
	@Override
	public void endIncrementalRotation()
	{
		eyeInit = new EigenVector3F(eye.getX(), eye.getY(), eye.getZ());
		upInit = new EigenVector3F(up.getX(), up.getY(), up.getZ());
	}
	
	@Override
	public void rotate(float yaw, float pitch, float roll)
	{
		EigenVector3F dirInv = eye.subExternal(center);
		EigenVector3F right = up.cross(dirInv).normalize();
		
		// Rotate calculation
		EigenMatrix3F yawRotation = MatrixFactory.getRotationMatrixAroundAxis(up, yaw);
		EigenMatrix3F pitchRotation = MatrixFactory.getRotationMatrixAroundAxis(right, pitch);
		EigenMatrix3F rollRotation = MatrixFactory.getRotationMatrixAroundAxis(dirInv.normalized().multiplyScalar(-1), roll);
		EigenMatrix3F yawPitchRotation = new EigenMatrix3F();
		EigenMatrix3F pitchRollRotation = new EigenMatrix3F();
		EigenVector3F dirInvMoved = new EigenVector3F();
		EigenVector3F upMoved = new EigenVector3F();
		
		yawRotation.multiplyWith(pitchRotation, yawPitchRotation);
		yawPitchRotation.multiplyWith(dirInv, dirInvMoved);
		
		pitchRotation.multiplyWith(rollRotation, pitchRollRotation);
		pitchRollRotation.multiplyWith(up, upMoved);
		
		// Move eye position and up vector
		dirInvMoved.add(center);
		eye = dirInvMoved;
		
		EigenVector3F dir = center.subExternal(eye).normalize();
		right = dir.cross(upMoved.normalized()).normalize();
		up = upMoved.normalize();
		
		recomputeMatrix = true;
	}

	@Override
	public void zoom(float amount)
	{
		// Zoom calculation
		EigenVector3F dir = center.subExternal(eye).normalize();
		dir.multiplyScalar(amount);
		
		// Move eye position and center
		eye.add(dir);
		center.add(dir);
		
		recomputeMatrix = true;
	}

	@Override
	public void pan(float dx, float dy)
	{
		// Pan calculation
		EigenVector3F dir = center.subExternal(eye).normalize();
		EigenVector3F right = dir.cross(up).normalize();
		EigenVector3F upClone = new EigenVector3F(up.getValue().clone());
		right.multiplyScalar(dx);
		upClone.multiplyScalar(dy);
		
		// Move eye position and center
		eye.add(right);
		eye.add(upClone);
		center.add(right);
		center.add(upClone);
		
		recomputeMatrix = true;
	}

	@Override
	public float[] getMatrix()
	{
		if (recomputeMatrix)
		{
			Matrix.setLookAtM(	cameraMatrix, 0, eye.getX(), eye.getY(), eye.getZ(), center.getX(), center.getY(), center.getZ(), 
								up.getX(), up.getY(), up.getZ());
			recomputeMatrix = false;
		}
		
		return cameraMatrix;
	}
}