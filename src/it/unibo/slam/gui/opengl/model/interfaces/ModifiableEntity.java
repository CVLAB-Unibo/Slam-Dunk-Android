package it.unibo.slam.gui.opengl.model.interfaces;

import it.unibo.slam.datatypes.geometry.Quaternion;

/**
 * Class representing an object whose position in the space is modifiable by changing the value of its model matrix.
 */
public interface ModifiableEntity
{
	/**
	 * Sets the translation part of the matrix.
	 * @param translationX X coordinate of the translation.
	 * @param translationY Y coordinate of the translation.
	 * @param translationZ Z coordinate of the translation.
	 */
	public void setTranslation(float translationX, float translationY, float translationZ);
	
	/**
	 * Sets the rotation part of the matrix.
	 * @param quaternion The quaternion representing the rotation.
	 */
	public void setRotation(Quaternion quaternion);
	
	/**
	 * Sets the rotation part of the matrix.
	 * @param rotationMatrix The rotation matrix (column major).
	 */
	public void setRotation(float[] rotationMatrix);
	
	/**
	 * Returns the transformation matrix for this model.
	 * @return The model matrix.
	 */
	public float[] getModelMatrix();
	
	/**
	 * Sets the transformation matrix for this model
	 * @param modelMatrix The model matrix.
	 */
	public void setModelMatrix(float[] modelMatrix);
}
