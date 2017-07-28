package it.unibo.slam.gui.opengl.camera.interfaces;

/**
 * Interface for a generic OpenGL camera.
 */
public interface Camera
{
	/**
	 * Rotate this camera, directly updating its position.
	 * @param yaw Yaw angle.
	 * @param pitch Pitch angle.
	 * @param roll Roll angle.
	 */
	public void rotate(float yaw, float pitch, float roll);
	
	/**
	 * Rotate this camera incrementally. The camera position is updated based on the same initial position. In order to set
	 * the initial position as the actual, a call to {@link #endIncrementalRotation()} is needed.
	 * @param yaw Yaw angle.
	 * @param pitch Pitch angle.
	 * @param roll Roll angle.
	 */
	void rotateIncremental(float yaw, float pitch, float roll);

	/**
	 * Ends the incremental rotation, updating the initial position to the actual position reached by the camera.
	 */
	void endIncrementalRotation();
	
	/**
	 * Zooms this camera.
	 * @param amount The amount to zoom.
	 */
	public void zoom(float amount);
	
	/**
	 * Pan this camera.
	 * @param dx The amount to move on the camera x direction.
	 * @param dy The amount to move on the camera y direction.
	 */
	public void pan(float dx, float dy);
	
	/**
	 * Gets the camera matrix.
	 * @return The camera matrix.
	 */
	public float[] getMatrix();
}
