package it.unibo.slam.main;

import it.unibo.slam.datatypes.eigen.typefloat.EigenMatrix3F;
import it.unibo.slam.datatypes.params.AlgorithmParams;
import it.unibo.slam.tracker.FeatureTracker;
import it.unibo.slam.tracker.FeatureTrackerParams;
import it.unibo.slam.tracker.interfaces.CameraTracker;

import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;

/**
 * Factory used to initialize the SLAM algorithm.
 */
public class SlamDunkFactory
{
	/**
	 * Default initialization.
	 * @param featureDetectorFileName Name of the feature detector YML file.
	 * @param descriptorMatcherFileName Name of the descriptor matcher YML file.
	 * @param detectorType The detector type.
	 * @param extractorType The extractor type.
	 * @param inverseKCam Inverse of the camera matrix.
	 * @param imgWidth Image width.
	 * @param imgHeight Image height.
	 * @return The SlamDunk instance used to execute the algorithm.
	 */
	public static SlamDunk initDefault(	String featureDetectorFileName, String descriptorMatcherFileName,
										int detectorType, int extractorType, EigenMatrix3F inverseKCam, 
										int imgWidth, int imgHeight)
	{
		FeatureDetector featureDetector = FeatureDetector.create(detectorType);
		featureDetector.read(featureDetectorFileName);
		
		DescriptorExtractor descriptorExtractor = DescriptorExtractor.create(extractorType);
		
		DescriptorMatcher descriptorMatcher = DescriptorMatcher.create(DescriptorMatcher.FLANNBASED);
		descriptorMatcher.read(descriptorMatcherFileName);
		
		FeatureTrackerParams trackerParams = new FeatureTrackerParams(featureDetector, descriptorExtractor, descriptorMatcher);
		CameraTracker tracker = new FeatureTracker(inverseKCam, imgWidth, imgHeight, trackerParams);
		
		SlamDunkParams slamParams = new SlamDunkParams(tracker);
		SlamDunk slam = new SlamDunk(inverseKCam, slamParams);
		
		return slam;
	}
	
	/**
	 * Complete initialization.
	 * @param featureDetectorFileName Name of the feature detector YML file.
	 * @param descriptorMatcherFileName Name of the descriptor matcher YML file.
	 * @param detectorType The detector type.
	 * @param extractorType The extractor type.
	 * @param inverseKCam Inverse of the camera matrix.
	 * @param imgWidth Image width.
	 * @param imgHeight Image height.
	 * @param algorithmParams Parameters to set (generally differing from the default parameters).
	 * @return The SlamDunk instance used to execute the algorithm.
	 */
	public static SlamDunk initComplete(String featureDetectorFileName, String descriptorMatcherFileName,
										int detectorType, int extractorType, EigenMatrix3F inverseKCam, 
										int imgWidth, int imgHeight, AlgorithmParams algorithmParams)
	{
		FeatureDetector featureDetector = FeatureDetector.create(detectorType);
		featureDetector.read(featureDetectorFileName);
		
		DescriptorExtractor descriptorExtractor = DescriptorExtractor.create(extractorType);
		
		DescriptorMatcher descriptorMatcher = DescriptorMatcher.create(DescriptorMatcher.FLANNBASED);
		descriptorMatcher.read(descriptorMatcherFileName);
		
		FeatureTrackerParams trackerParams = new FeatureTrackerParams(featureDetector, descriptorExtractor, descriptorMatcher);
		trackerParams.activeWindowLength = algorithmParams.getActiveWindowLength();
		CameraTracker tracker = new FeatureTracker(inverseKCam, imgWidth, imgHeight, trackerParams);
		
		SlamDunkParams slamParams = new SlamDunkParams(tracker);
		slamParams.rbaRings = algorithmParams.getRbaRings();
		slamParams.keyframeOverlapping = algorithmParams.getKeyframeOverlapping();
		slamParams.tryLoopInference = algorithmParams.isTryLoopInference();
		//TODO enable/disable debug?
		SlamDunk slam = new SlamDunk(inverseKCam, slamParams);
		
		return slam;
	}
}
