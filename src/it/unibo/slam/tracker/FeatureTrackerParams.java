package it.unibo.slam.tracker;

import it.unibo.slam.datatypes.eigen.typedouble.EigenVector2D;
import it.unibo.slam.matcher.RatioMatcher;
import it.unibo.slam.matcher.interfaces.FeatureMatcher;
import it.unibo.slam.sampleconsensus.RANSAC;
import it.unibo.slam.sampleconsensus.abstracts.SampleConsensus;

import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;

public class FeatureTrackerParams
{
	public double quadtreeRes;
	
	public short quadtreeDepth;
	
	public double activeWindowLength;
	
	public short minMatches;
	
	public short maxFeatsPerFrame;
	
	public EigenVector2D windowMovementStep;
	
	public float percentageFeatOverlap;
	
	public FeatureDetector featureDetector;
	
	public DescriptorExtractor descriptorExtractor;
	
	public FeatureMatcher featureMatcher;
	
	public SampleConsensus outlierRejection;
	
	public float nearPlane;
	
	public float farPlane;
	
	/**
	 * Base constructor.
	 * @param featureDetector Feature detector.
	 * @param descriptorExtractor Descriptor extractor.
	 * @param descriptorMatcher Descriptor matcher.
	 */
	public FeatureTrackerParams(FeatureDetector featureDetector, DescriptorExtractor descriptorExtractor,
								DescriptorMatcher descriptorMatcher)
	{
		quadtreeRes = 0.1;
		quadtreeDepth = 7;
		activeWindowLength = 5;
		minMatches = 5;
		maxFeatsPerFrame = 500;
		windowMovementStep = new EigenVector2D(1, 1);
		percentageFeatOverlap = 0.3F;
		this.featureDetector = featureDetector;
		this.descriptorExtractor = descriptorExtractor;
		featureMatcher = new RatioMatcher(descriptorMatcher);
		outlierRejection = new RANSAC(true, 0.05F);
		nearPlane = 0.3F;
		farPlane = 4;
	}
}
