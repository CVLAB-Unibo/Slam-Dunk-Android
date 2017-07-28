package it.unibo.slam.gui.opengl.rendering;

/**
 * The movement state. It determines the actions that have to be performed on the view.
 */
public enum MovementMode
{
	/**
	 * No movement.
	 */
	NONE,
	
	/**
	 * Just rotate movement.
	 */
	ROTATE_PITCH_YAW,
	
	/**
	 * Pan and/or zoom. Can be activated simultaneously.
	 */
	PAN_ZOOM_AND_ROLL;
}
