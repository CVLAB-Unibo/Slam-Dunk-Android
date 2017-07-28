package it.unibo.slam.datatypes;

import it.unibo.slam.datatypes.eigen.typefloat.EigenVector3F;

import java.util.List;

import org.opencv.core.Mat;

/**
 * Data saved from a single frame.
 */
public class FrameData
{
	/**
	 * Frame timestamp.
	 */
	private double timestamp;
	
	/**
	 * Descriptors of the frame.
	 */
	private Mat descriptors;
	
	/**
	 * Keypoints of the frame (already projected in 3 dimensions).
	 */
	private List<EigenVector3F> keypoints;

	/** 
	 * Sets the timestamp.
	 * @param timestamp The timestamp.
	 */
	public void setTimestamp(double timestamp)
	{
		this.timestamp = timestamp;
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
	 * Sets the descriptors.
	 * @param descriptors The descriptors.
	 */
	public void setDescriptors(Mat descriptors)
	{
		this.descriptors = descriptors;
	}

	/**
	 * Gets the descriptors.
	 * @return The descriptors.
	 */
	public Mat getDescriptors()
	{
		return descriptors;
	}

	/**
	 * Sets the keypoints.
	 * @param keypoints The keypoints.
	 */
	public void setKeypoints(List<EigenVector3F> keypoints)
	{
		this.keypoints = keypoints;
	}

	/**
	 * Gets the keypoints.
	 * @return The keypoints.
	 */
	public List<EigenVector3F> getKeypoints()
	{
		return keypoints;
	}
}
