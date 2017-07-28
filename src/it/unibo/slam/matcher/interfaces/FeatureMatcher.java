package it.unibo.slam.matcher.interfaces;

import it.unibo.slam.matcher.FMatch;

import java.util.List;

import org.opencv.core.Mat;

/**
 * Interface representing a generic feature matcher.
 */
public interface FeatureMatcher
{
	/**
	 * Maximum number of neighbors to consider in the matching.
	 */
	public static final int KNN_NEIGHBORS = 32;
	
	/**
	 * Sets the descriptors to use in training the matcher.
	 * @param descriptors The descriptors.
	 */
	public void setDescriptors(List<Mat> descriptors);
	
	/**
	 * Gets the number for train images available in the matcher.
	 * @return The number of images.
	 */
	public int getImageNum();
	
	/**
	 * Executes the matching.
	 * @param query The query.
	 * @param matches A list containing the resulting matches.
	 */
	public void match(Mat query, List<FMatch> matches);
}
