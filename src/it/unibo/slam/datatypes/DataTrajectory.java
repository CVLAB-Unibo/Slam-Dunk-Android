package it.unibo.slam.datatypes;

import it.unibo.slam.datatypes.eigen.typefloat.EigenVector3F;
import it.unibo.slam.datatypes.geometry.Quaternion;

/**
 * Class representing trajectory data with a translation vector and a rotation quaternion, plus the assigned timestamp.
 */
public class DataTrajectory
{
	/**
	 * Timestamp of the data.
	 */
	private double timestamp;
	
	/**
	 * Translation vector of this trajectory.
	 */
	private EigenVector3F translation;
	
	/**
	 * Quaternion representing the rotation component of this trajectory.
	 */
	private Quaternion rotation;
	
	/**
	 * Basic constructor.
	 * @param timestamp Timestamp.
	 * @param translation Translation vector.
	 * @param rotation Rotation quaternion.
	 */
	public DataTrajectory(double timestamp, EigenVector3F translation, Quaternion rotation)
	{
		this.timestamp = timestamp;
		this.translation = translation;
		this.rotation = rotation;
	}

	/**
	 * Gets the timestamp.
	 * @return The timestamp.
	 */
	public double getTimestamp()
	{
		return timestamp;
	}
	
	/**
	 * Sets the timestamp.
	 * @param timestamp The new timestamp value.
	 */
	public void setTimestamp(double timestamp)
	{
		this.timestamp = timestamp;
	}

	/**
	 * Gets the translation vector.
	 * @return The translation vector.
	 */
	public EigenVector3F getTranslation()
	{
		return translation;
	}
	
	/**
	 * Sets the translation vector.
	 * @param translation The new translation vector.
	 */
	public void setTranslation(EigenVector3F translation)
	{
		this.translation = translation;
	}

	/**
	 * Gets the rotation quaternion.
	 * @return The rotation quaternion.
	 */
	public Quaternion getRotation()
	{
		return rotation;
	}
	
	/**
	 * Sets the rotation quaternion.
	 * @param rotation The new rotation quaternion.
	 */
	public void setRotation(Quaternion rotation)
	{
		this.rotation = rotation;
	}
}