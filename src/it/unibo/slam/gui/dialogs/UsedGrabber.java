package it.unibo.slam.gui.dialogs;

/**
 * Enumerative used by the GrabberSelectionDialog in order to show the available grabbers.
 */
public enum UsedGrabber
{
	/**
	 * Image grabber.
	 */
	IMAGE,
	
	/**
	 * Image grabber with inertial sensors.
	 */
	IMAGE_INERTIAL,
	
	/**
	 * Kinect or Xtion grabber (using OpenNI 1.5).
	 */
	KINECT_OR_XTION,
	
	/**
	 * Senz3D grabber.
	 */
	SENZ3D,
	
	/**
	 * Structure grabber (using OpenNI 2 and the Android camera).
	 */
	STRUCTURE;
}
