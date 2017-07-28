package it.unibo.slam.tracker;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Point;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.KeyPoint;
import org.opencv.imgproc.Imgproc;

import it.unibo.slam.datatypes.DataBGRD;
import it.unibo.slam.datatypes.FrameData;
import it.unibo.slam.datatypes.Pair;
import it.unibo.slam.datatypes.eigen.general.ComparisonType;
import it.unibo.slam.datatypes.eigen.typedouble.EigenIsometry3D;
import it.unibo.slam.datatypes.eigen.typedouble.EigenMatrix4D;
import it.unibo.slam.datatypes.eigen.typedouble.EigenVector2D;
import it.unibo.slam.datatypes.eigen.typedouble.EigenVector3D;
import it.unibo.slam.datatypes.eigen.typefloat.EigenIsometry3F;
import it.unibo.slam.datatypes.eigen.typefloat.EigenMatrix3F;
import it.unibo.slam.datatypes.eigen.typefloat.EigenMatrixF;
import it.unibo.slam.datatypes.eigen.typefloat.EigenVector3F;
import it.unibo.slam.datatypes.eigen.typefloat.EigenVector4F;
import it.unibo.slam.datatypes.eigen.typefloat.EigenVectorF;
import it.unibo.slam.datatypes.g2o.PoseVertex;
import it.unibo.slam.matcher.FMatch;
import it.unibo.slam.matcher.FrameToFrameMatch;
import it.unibo.slam.matcher.interfaces.FeatureMatcher;
import it.unibo.slam.quadtree.QuadTree;
import it.unibo.slam.quadtree.QuadTreeElement;
import it.unibo.slam.quadtree.QuadTreeFactory;
import it.unibo.slam.quadtree.VertexID;
import it.unibo.slam.sampleconsensus.abstracts.SampleConsensus;
import it.unibo.slam.tracker.interfaces.CameraTracker;

public class FeatureTracker implements CameraTracker
{
	private static int SLAM_DUNK_FEATURE_GRID_SIZE = 4;
	
	private static int SLAM_DUNK_FEATURE_GRID_SIZE_SQUARED = SLAM_DUNK_FEATURE_GRID_SIZE * SLAM_DUNK_FEATURE_GRID_SIZE;
	
	private TrackingResult lastTrackingResult;
	
	private Set<Integer> lastTrackedKeyframes;
	
	private EigenMatrix3F inverseKCam;
	
	private QuadTree<PoseVertex, VertexID> tree;
	
	private double halfActiveWindowLength;
	
	private EigenVector2D activeWindowCenter;
	
	private EigenVector2D windowMovementStep;
	
	private EigenIsometry3D activeWindowPose;
	
	private float percentageFeatOverlap;
	
	private FeatureDetector featureDetector;
	
	private DescriptorExtractor descriptorExtractor;
	
	private FeatureMatcher featureMatcher;
	
	private SampleConsensus outlierRejection;
	
	private Map<Integer, PoseVertex> referenceVxs;
	
	private short minMatches;
	
	private short maxFeatsPerFrame;
	
	private Mat colorImgMat;
	
	private Mat grayscaleImgMat;
	
	private EigenMatrixF frustumMatrix = new EigenMatrixF(6, 4);
	
	public FeatureTracker(EigenMatrix3F inverseKCam, int imgWidth, int imgHeight, FeatureTrackerParams params)
	{
		lastTrackingResult = new TrackingResult();
		lastTrackedKeyframes = new HashSet<Integer>();
		this.inverseKCam = inverseKCam;
		tree = QuadTreeFactory.getInstance().createDefaultQuadTree(Math.abs(params.quadtreeRes), params.quadtreeDepth, new VertexID());
		halfActiveWindowLength = Math.abs(params.activeWindowLength) / 2.0;
		windowMovementStep = params.windowMovementStep;
		windowMovementStep.cwiseAbs();
		percentageFeatOverlap = params.percentageFeatOverlap;
		featureDetector = params.featureDetector;
		descriptorExtractor = params.descriptorExtractor;
		featureMatcher = params.featureMatcher;
		outlierRejection = params.outlierRejection;
		referenceVxs = new Hashtable<Integer, PoseVertex>();
		minMatches = params.minMatches;
		maxFeatsPerFrame = params.maxFeatsPerFrame;
		colorImgMat = new Mat(imgHeight, imgWidth, CvType.CV_8UC3);
		grayscaleImgMat = new Mat();
		setFrustum(inverseKCam, imgWidth, imgHeight, params.nearPlane, params.farPlane);
	}
	
	public void setFrustum(EigenMatrix3F inverseKCam, int width, int height, float nearPlane, float farPlane)
	{
		EigenVector3F tl = inverseKCam.col(2);
		EigenVector3F tr = new EigenVector3F(tl.getValue().clone());
		tr.set(tr.get(0) + width * inverseKCam.get(0, 0), 0);
		EigenVector3F bl = new EigenVector3F(tl.getValue().clone());
		bl.set(bl.get(1) + height * inverseKCam.get(1, 1), 1);
		EigenVector3F br = new EigenVector3F(tr.get(0), bl.get(1), 1);
		EigenVector3F npl = new EigenVector3F(0, 0, nearPlane);
		EigenVector3F fpl = new EigenVector3F(0, 0, farPlane);
		
		EigenVector3F unitZ = new EigenVector3F(0, 0, 1);
		EigenVector3F values;
		
		frustumMatrix.set(0, 0, 0);
		frustumMatrix.set(0, 0, 1);
		frustumMatrix.set(1, 0, 2);
		frustumMatrix.set(-1 * unitZ.dot(fpl), 0, 3);
		
		frustumMatrix.set(0, 1, 0);
		frustumMatrix.set(0, 1, 1);
		frustumMatrix.set(-1, 1, 2);
		frustumMatrix.set(unitZ.dot(npl), 1, 3);
		
		values = tr.cross(tl).normalize();
		frustumMatrix.set(values.getX(), 2, 0);
		frustumMatrix.set(values.getY(), 2, 1);
		frustumMatrix.set(values.getZ(), 2, 2);
		frustumMatrix.set(0, 2, 3);
		
		values = bl.cross(br).normalize();
		frustumMatrix.set(values.getX(), 3, 0);
		frustumMatrix.set(values.getY(), 3, 1);
		frustumMatrix.set(values.getZ(), 3, 2);
		frustumMatrix.set(0, 3, 3);
		
		values = tl.cross(bl).normalize();
		frustumMatrix.set(values.getX(), 4, 0);
		frustumMatrix.set(values.getY(), 4, 1);
		frustumMatrix.set(values.getZ(), 4, 2);
		frustumMatrix.set(0, 4, 3);
		
		values = br.cross(tr).normalize();
		frustumMatrix.set(values.getX(), 5, 0);
		frustumMatrix.set(values.getY(), 5, 1);
		frustumMatrix.set(values.getZ(), 5, 2);
		frustumMatrix.set(0, 5, 3);
	}
	
	@Override
	public void reset(EigenIsometry3D pose, FrameData frameData)
    {
		lastTrackingResult.pose = new EigenIsometry3D(new EigenMatrix4D(pose.getMatrix().getValue().clone()));
		lastTrackingResult.overlappingScore = 0;
		lastTrackingResult.frameData = frameData;
		lastTrackingResult.ffMatches = new ArrayList<FrameToFrameMatch>();
    }
	
	@Override
    public TrackingResult getLastTrackingResult()
    {
    	return lastTrackingResult;
    }
    
	@Override
    public Set<Integer> getLastTrackedKeyframes()
    { 
    	return lastTrackedKeyframes; 
    }
	
	int iter = 1;
	
	@Override
	public boolean track(DataBGRD frame)
	{
		// Features and keypoints extraction
		List<KeyPoint> keypoints = new ArrayList<KeyPoint>();
		lastTrackingResult.frameData = new FrameData();
		extractFrameData(frame, lastTrackingResult.frameData, keypoints);
		if (lastTrackingResult.frameData.getKeypoints().size() < minMatches)
			return false;
		
		// Frame-frame matching
		List<FMatch> matches = new ArrayList<FMatch>();
		featureMatcher.match(lastTrackingResult.frameData.getDescriptors(), matches);
		if (matches.size() < minMatches)
			return false;
		
		EigenVector3F[] ffReferenceKeypoints = new EigenVector3F[matches.size()];
		@SuppressWarnings("unchecked")
		Pair<Integer, Integer>[] referenceQueryIds = (Pair<Integer, Integer>[]) new Pair[matches.size()];
		PoseVertex[] referenceVxsTemp = new PoseVertex[matches.size()];
		
		int imageNum = featureMatcher.getImageNum();
		PoseVertex[] keyframePoseVertices = new PoseVertex[imageNum];
		EigenMatrix3F[] keyframePoseRotationMatrices = new EigenMatrix3F[imageNum];
		EigenVector3F[] keyframePoseTranslationVectors = new EigenVector3F[imageNum];
		EigenIsometry3F tempKeyframePose;
		for (int i = 0; i < imageNum; i++)
		{
			keyframePoseVertices[i] = referenceVxs.get(i);
			tempKeyframePose = new EigenIsometry3F(keyframePoseVertices[i].getEstimate().getMatrix().toFloat());
			keyframePoseRotationMatrices[i] = tempKeyframePose.getRotation();
			keyframePoseTranslationVectors[i] = tempKeyframePose.getTranslation();
		}
		
		PoseVertex frameVx;
		FMatch tempMatch;
		for (int i = 0; i < matches.size(); i++)
		{
			tempMatch = matches.get(i);
			frameVx = keyframePoseVertices[tempMatch.imgIdx];
			
			ffReferenceKeypoints[i] = new EigenVector3F();
			keyframePoseRotationMatrices[tempMatch.imgIdx].multiplyWith(frameVx.getUserData().getKeypoints().get(tempMatch.trainIdx), ffReferenceKeypoints[i]);
			ffReferenceKeypoints[i].add(keyframePoseTranslationVectors[tempMatch.imgIdx]);
			referenceQueryIds[i] = new Pair<Integer, Integer>(i, tempMatch.queryIdx);
			referenceVxsTemp[i] = frameVx;
		}
		
		EigenVector3F[] keypointsArray = new EigenVector3F[lastTrackingResult.frameData.getKeypoints().size()];
		lastTrackingResult.frameData.getKeypoints().toArray(keypointsArray);
		
		// Outlier rejection
		long s = System.currentTimeMillis();
		outlierRejection.findInliers(ffReferenceKeypoints, keypointsArray, referenceQueryIds);
		System.out.println("RANSAC: " + (System.currentTimeMillis() - s));
		
		List<Integer> rqInliers = outlierRejection.getReferenceQueryInliers();
		if (rqInliers.size() < minMatches)
			return false;
		
		boolean[] inlierGrid = new boolean[SLAM_DUNK_FEATURE_GRID_SIZE_SQUARED];
		lastTrackingResult.ffMatches.clear();
		@SuppressWarnings("unchecked")
		Pair<Integer, Integer>[] referenceQueryIdsInliers = (Pair<Integer, Integer>[]) new Pair[rqInliers.size()];
		lastTrackedKeyframes.clear();
		
		int rqId = 0;
		int count = 0;
		// Get the inliers
		for (int inlier : rqInliers)
		{
			Pair<Integer, Integer> inlierPair = referenceQueryIds[inlier];
			referenceQueryIdsInliers[rqId] = inlierPair;
			
			FrameToFrameMatch ffm = new FrameToFrameMatch();
			ffm.imgIdx = referenceVxsTemp[inlierPair.getFirst()].getId();
			ffm.trainIdx = matches.get(inlierPair.getFirst()).trainIdx;
			ffm.queryIdx = inlierPair.getSecond();
			ffm.score = matches.get(inlierPair.getFirst()).score;
			
			lastTrackingResult.ffMatches.add(ffm);
			lastTrackedKeyframes.add(ffm.imgIdx);
			
			KeyPoint kp = keypoints.get(ffm.queryIdx);
			
			int ug = (int)(kp.pt.x * (float)SLAM_DUNK_FEATURE_GRID_SIZE / (float)frame.getWidth());
		    int vg = (int)(kp.pt.y * (float)SLAM_DUNK_FEATURE_GRID_SIZE / (float)frame.getHeight());
		    assert(ug >= 0 && ug < SLAM_DUNK_FEATURE_GRID_SIZE);
		    assert(vg >= 0 && vg < SLAM_DUNK_FEATURE_GRID_SIZE);
		    int id = ug + vg * SLAM_DUNK_FEATURE_GRID_SIZE;
		    if (!inlierGrid[id])
		    {
		    	inlierGrid[id] = true;
		    	count++;
		    }
		    
		    rqId++;
		}
		
		SampleConsensus.estimateTransformationSVD(	ffReferenceKeypoints, keypointsArray, 
													referenceQueryIdsInliers, lastTrackingResult.pose);
		
		
		EigenIsometry3D incrementalPose = new EigenIsometry3D(new EigenMatrix4D(
				calculateIncrementalPose(	lastTrackingResult.pose.getMatrix().getValue(), 
											activeWindowPose.getMatrix().getValue())));
		double cosTheta = (incrementalPose.getRotation().trace() - 1.0) * 0.5;
		float angleScore = (float)((cosTheta < 0.707106781) ? 0.0 : Math.pow(2.0 * cosTheta * cosTheta - 1.0, 2.0));
		// Overlapping score computation
		lastTrackingResult.overlappingScore = 0.33333333F * (	(float)count / (float)SLAM_DUNK_FEATURE_GRID_SIZE_SQUARED +
																angleScore + 
																(float)(1.0 - Math.min(	1.0, incrementalPose.getTranslation().length() *
																						2.5 / halfActiveWindowLength)));
		
		return true;
	}

	@Override
	public void extractFrameData(DataBGRD frame, FrameData frameData)
	{
		extractFrameData(frame, frameData, null);
	}
	
	public void extractFrameData(DataBGRD frame, FrameData frameData, List<KeyPoint> keypoints)
	{
		frameData.setTimestamp(frame.getTimestamp());
		byte[] pixels = frame.getBGR();
		colorImgMat.put(0, 0, pixels);
		Imgproc.cvtColor(colorImgMat, grayscaleImgMat, Imgproc.COLOR_BGR2GRAY);
		
		// Feature detection and extraction
		long s = System.currentTimeMillis();
		MatOfKeyPoint tempKeypoints = new MatOfKeyPoint();
		featureDetector.detect(grayscaleImgMat, tempKeypoints);
		Mat descriptors = new Mat();
		descriptorExtractor.compute(grayscaleImgMat, tempKeypoints, descriptors);
		System.out.println("FEATURE DETECTION AND EXTRACTION: " + (System.currentTimeMillis() - s));
		
		KeyPoint[] tempKeypointsArray = tempKeypoints.toArray();
		Mat validDescriptors = new Mat(descriptors.rows(), descriptors.cols(), descriptors.type());
		int featWithDepth = 0;
		frameData.setKeypoints(new ArrayList<EigenVector3F>());
		float[] depth = frame.getDepth();
		float z;
		int width = frame.getWidth();
		float[] p3dIn = new float[3];
		EigenVector3F p3dInEigen = new EigenVector3F(p3dIn);
		Point pt;
		
		// Keep only the features with a valid depth value
		for (int i = 0; i < tempKeypointsArray.length && featWithDepth < maxFeatsPerFrame; i++)
		{
			pt = tempKeypointsArray[i].pt;
			z = depth[(int)(pt.x + 0.5) + (int)(pt.y + 0.5) * width];
			
			if (z > 0)
			{
				p3dIn[0] = (float)pt.x * z;
				p3dIn[1] = (float)pt.y * z;
				p3dIn[2] = z;
				EigenVector3F pt3dOutEigen = new EigenVector3F();
				inverseKCam.multiplyWith(p3dInEigen, pt3dOutEigen);
				frameData.getKeypoints().add(pt3dOutEigen);
				if (keypoints != null)
					keypoints.add(tempKeypointsArray[i]);
				descriptors.row(i).copyTo(validDescriptors.row(featWithDepth));
				
				featWithDepth++;
			}
		}
		
		frameData.setDescriptors(validDescriptors.rowRange(0, featWithDepth));
		
		/*for (int i = 0; i < frameData.getKeypoints().size(); i++)
		{
			System.out.println(frameData.getKeypoints().get(i).getX() + "," + frameData.getKeypoints().get(i).getY() 
					+ "," + frameData.getKeypoints().get(i).getZ());
		}

		for (int i = 0; i < frameData.getDescriptors().rows(); i++)
		{
			String ss = "";
			for (int j = 0; j < frameData.getDescriptors().cols(); j++)
			{
				if (j != frameData.getDescriptors().cols() - 1)
					ss += (int)frameData.getDescriptors().get(i, j)[0] + ",";
				else
					ss += (int)frameData.getDescriptors().get(i, j)[0];
			}
			System.out.println(ss);
		}*/
	}

	@Override
	public void signalMovedFrames(Map<Integer, PoseVertex> poses)
	{
		if (poses.isEmpty())
			return;
		
		boolean ok = true;
		EigenVector3D translationTemp;
		// Updates the tree
		for (PoseVertex pose : poses.values())
		{
			translationTemp = pose.getEstimate().getTranslation();
			ok &= tree.update(pose, new EigenVector2D(translationTemp.getX(), translationTemp.getZ()));
		}
		
		if (!ok)
			Logger.getLogger("FeatureTracker").info("At least one frame moved out of the bounds!");
		
		// Updates the feature matcher
		calculateActiveWindow();
	}

	@Override
	public void setFrames(Map<Integer, PoseVertex> poses)
	{
		tree.clear();
		if (poses.isEmpty())
		{
			referenceVxs.clear();
			return;
		}
		
		boolean ok = true;
		EigenVector3D translationTemp;
		for (PoseVertex pose : poses.values())
		{
			translationTemp = pose.getEstimate().getTranslation();
			ok &= tree.insert(pose, new EigenVector2D(translationTemp.getX(), translationTemp.getZ()));
		}
		
		if (!ok)
			Logger.getLogger("FeatureTracker").info("At least one frame is of the bounds!");
		
		// Updates the feature matcher
		calculateActiveWindow();
	}
	
	@Override
	public void updateMap()
	{
		EigenVector3D translation = lastTrackingResult.pose.getTranslation();
		EigenVector2D tempPos = new EigenVector2D(translation.getX(), translation.getZ());
		
		// Should we move the active window?
		if (tempPos.sub(activeWindowCenter).cwiseAbs().sub(windowMovementStep).elementsMajorTo(0, ComparisonType.ANY))
			calculateActiveWindow();
	}
	
	private void calculateActiveWindow()
	{
		List<QuadTreeElement<PoseVertex>> elements = new ArrayList<QuadTreeElement<PoseVertex>>();
		EigenVector3D translation = lastTrackingResult.pose.getTranslation();
		tree.query(	translation.getX() - halfActiveWindowLength, translation.getZ() - halfActiveWindowLength,
					2 * halfActiveWindowLength, 2 * halfActiveWindowLength, elements);
		activeWindowCenter = new EigenVector2D(translation.getX(), translation.getZ());
		activeWindowPose = new EigenIsometry3D(new EigenMatrix4D(lastTrackingResult.pose.getMatrix().getValue().clone()));
		
		List<Mat> frameDescriptors = new ArrayList<Mat>();
		
		EigenVector4F homv = new EigenVector4F(0, 0, 0, 1);
		EigenVectorF res = new EigenVectorF(6);
		Map<Integer, PoseVertex> referenceVxsTemp = new Hashtable<Integer, PoseVertex>();
		for (QuadTreeElement<PoseVertex> element : elements)
		{
			FrameData data = element.getData().getUserData();
			short nfeats = 0;
			
			for (EigenVector3F keypoint : data.getKeypoints())
			{
				homv.setX(keypoint.getX());
				homv.setY(keypoint.getY());
				homv.setZ(keypoint.getZ());
				frustumMatrix.multiplyWith(homv, res);
				
				if (res.elementsMinorTo(0, ComparisonType.ALL))
					nfeats++;
			}
			
			if (((float)nfeats / (float)data.getKeypoints().size()) > percentageFeatOverlap)
			{
				referenceVxsTemp.put(frameDescriptors.size(), element.getData());
				frameDescriptors.add(data.getDescriptors());
			}
		}
		
		if (!frameDescriptors.isEmpty())
		{
			referenceVxs = referenceVxsTemp;
			featureMatcher.setDescriptors(frameDescriptors);
		}
		else
			Logger.getLogger("FeatureTracker").warning("Active window is empty: using last frame.");
	}
	
	private native double[] calculateIncrementalPose(double[] lastTrackedPose, double[] activeWindowPose);
}
