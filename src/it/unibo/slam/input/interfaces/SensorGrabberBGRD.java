package it.unibo.slam.input.interfaces;

import it.unibo.slam.datatypes.DataBGRD;
import it.unibo.slam.datatypes.params.IntrinsicParams;

/**
 * Generic RGB-D sensor grabber. The data is given in the BGR-D format.
 */
public interface SensorGrabberBGRD extends Runnable
{
	/**
	 * Default max depth in meters.
	 */
	public static final float MAX_DEPTH_DEFAULT = 10F;
	
	/**
	 * The default focal length on the x coordinate.
	 */
	public static float DEFAULT_FOCAL_X = 525F;
	
	/**
	 * The default focal length on the y coordinate.
	 */
	public static float DEFAULT_FOCAL_Y = 525F;
	
	/**
	 * The default center on the x coordinate (based on a resolution of 640x480).
	 */
	public static float DEFAULT_CENTER_X = 319.5F;
	
	/**
	 * The default center on the y coordinate (based on a resolution of 640x480).
	 */
	public static float DEFAULT_CENTER_Y = 239.5F;
	
	/**
	 * Gets the max depth.
	 * @return The max depth.
	 */
	public float getMaxDepth();
	
	/**
	 * Gets the intrinsic parameters of the camera.
	 * @return The intrinsic parameters.
	 */
	public IntrinsicParams getIntrinsicParams();
	
	/**
	 * Initializes this grabber.
	 * @throws Exception An error occurred during the initialization.<br>
	 * The kind of error depends on the implementation of the grabber.
	 */
	public void init() throws Exception;
	
	/**
	 * Checks if the grabber has been initialized.
	 * @return True if the grabber has been initialized, false otherwise.
	 */
	public boolean isInitialized();
	
	/**
	 * Pauses this grabber.
	 */
	public void pause();
	
	/**
	 * Resumes this grabber.
	 */
	public void resume();
	
	/**
	 * Terminate this grabber.
	 */
	public void terminate();
	
	/**
	 * Checks if the grabber is paused.
	 * @return True if the grabber is paused, false otherwise.
	 */
	public boolean isPaused();
	
	/**
	 * Grabs new data.
	 * @return The grabbed data.
	 * @throws Exception An error occured during the operation.<br>
	 * The kind of error depends on the implementation of the grabber.
	 */
	public DataBGRD grab() throws Exception;
	
	@Override
	public void run();
}
