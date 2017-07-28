package it.unibo.slam.matcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorMatcher;

import it.unibo.slam.matcher.interfaces.FeatureMatcher;

/**
 * Implementation of the feature matcher with ratio test filtering.
 */
public class RatioMatcher implements FeatureMatcher
{
	/**
	 * OpenCV Descriptor Matcher.
	 */
	private DescriptorMatcher descriptorMatcher;
	
	/**
	 * Ratio used in the ratio test.
	 */
	private float ratioSquared;
	
	/**
	 * If true the ratio test will consider the first two keypoints from different images,
	 * if false the keypoints will be chosen from the same image.
	 */
	private boolean interModelRatio;

	/**
	 * Number of train images stored in the matcher.
	 */
	private int imgNum;
	
	/**
	 * Maximum number of descriptors in the different train images.
	 */
	private int rowNum;
	
	/**
	 * Base constructor.
	 * @param descriptorMatcher The OpenCV matcher.
	 * @param ratio The ratio.
	 * @param interModelRatio True for inter model ratio (the ratio test will be applied considering keypoints 
	 * on different images), false otherwise.
	 */
	public RatioMatcher(DescriptorMatcher descriptorMatcher, float ratio, boolean interModelRatio)
	{
		this.descriptorMatcher = descriptorMatcher;
		ratioSquared = ratio * ratio;
		this.interModelRatio = interModelRatio;
		imgNum = 0;
		rowNum = 0;
	}
	
	/**
	 * Constructor with default ratio (0.8) and inter model ratio (false).
	 * @param descriptorMatcher The OpenCV matcher.
	 */
	public RatioMatcher(DescriptorMatcher descriptorMatcher)
	{
		this(descriptorMatcher, 0.8F, false);
	}
	
	@Override
	public void setDescriptors(List<Mat> descriptors)
	{
		imgNum = descriptors.size();
		descriptorMatcher.clear();
		descriptorMatcher.add(descriptors);
		
		descriptorMatcher.train();
		
		rowNum = 0;
		for (Mat descriptor : descriptors)
		{
			if (rowNum < descriptor.rows())
				rowNum = descriptor.rows();
		}
	}
	
	@Override
	public int getImageNum()
	{
		return imgNum;
	}

	@Override
	public void match(Mat query, List<FMatch> matches)
	{
		long s = System.currentTimeMillis();
		List<MatOfDMatch> matchesNotFiltered = new ArrayList<MatOfDMatch>();
		descriptorMatcher.knnMatch(query, matchesNotFiltered, KNN_NEIGHBORS);
		System.out.println("MATCHING: " + (System.currentTimeMillis() - s));
		
		int rows = query.rows();
		List<MatchHypothesis> tempMatches = new ArrayList<MatchHypothesis>(rows);
		Iterator<MatOfDMatch> actualMatchesIterator = matchesNotFiltered.iterator();
		for (int i = 0; i < rows; i++)
		{
			DMatch[] actualMatches = actualMatchesIterator.next().toArray();
			
			// Find the second nearest neighbor descriptor
			for (int ne = 1; ne < KNN_NEIGHBORS && actualMatches.length > ne; ne++)
			{
				// No need to check the distance because the results are sorted
				if ((actualMatches[0].imgIdx != actualMatches[ne].imgIdx && interModelRatio) ||
					(actualMatches[0].imgIdx == actualMatches[ne].imgIdx && !interModelRatio))
				{
					if (actualMatches[0].distance <= ratioSquared * actualMatches[ne].distance && actualMatches[ne].distance > 0)
					{
						MatchHypothesis match = new MatchHypothesis();
						match.imgIdx = actualMatches[0].imgIdx;
						match.queryIdx = i;
						match.trainIdx = actualMatches[0].trainIdx;
						match.ratio = actualMatches[0].distance / actualMatches[ne].distance;
						tempMatches.add(match);
					}
					
					break;
				}
			}
		}
		
		Collections.sort(tempMatches, MatchHypothesis.RATIO_COMPARATOR);
		
		boolean[][] matchPresent = new boolean[imgNum][rowNum];
		matches.clear();
		// Clear double matches
		for (Iterator<MatchHypothesis> it = tempMatches.iterator(); it.hasNext(); )
		{
			MatchHypothesis match = it.next();
			
			if (!matchPresent[match.imgIdx][match.trainIdx])
			{
				matchPresent[match.imgIdx][match.trainIdx] = true;
				FMatch fMatch = new FMatch();
				fMatch.imgIdx = match.imgIdx;
				fMatch.queryIdx = match.queryIdx;
				fMatch.trainIdx = match.trainIdx;
				fMatch.score = 1F - (float)Math.sqrt(match.ratio);
				matches.add(fMatch);
			}
		}
	}
}
