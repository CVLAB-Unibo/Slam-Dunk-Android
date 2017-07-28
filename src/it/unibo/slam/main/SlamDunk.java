package it.unibo.slam.main;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import it.unibo.slam.datatypes.DataBGRD;
import it.unibo.slam.datatypes.FrameData;
import it.unibo.slam.datatypes.Pair;
import it.unibo.slam.datatypes.eigen.typedouble.EigenIsometry3D;
import it.unibo.slam.datatypes.eigen.typedouble.EigenMatrix4D;
import it.unibo.slam.datatypes.eigen.typefloat.EigenMatrix3F;
import it.unibo.slam.datatypes.g2o.PoseVertex;
import it.unibo.slam.graph.GraphBackend;
import it.unibo.slam.tracker.TrackingResult;
import it.unibo.slam.tracker.interfaces.CameraTracker;

/**
 * Main class implementing the SLAM algorithm.
 */
public class SlamDunk
{
	/**
	 * Inverse of the camera matrix K.
	 */
	private EigenMatrix3F inverseKCam;
	
	/**
	 * The camera tracker used for the tracking process.
	 */
	private CameraTracker cameraTracker;
	
	/**
	 * The graph used to store the nodes and that will execute the optimization process.
	 */
	private GraphBackend graph;
	
	/**
	 * Frames moved after the last optimization.
	 */
	private List<Pair<Double, EigenIsometry3D>> movedFrames;
	
	/**
	 * Minimum number of extracted features. If the number of features is lower than this, the frame
	 * will be discarded.
	 */
	private int minimumExtractedFeatures;
	
	/**
	 * Keyframe overlapping value.
	 */
	private float keyframeOverlapping;
	
	/**
	 * Stores the number of processed frames.
	 */
	private int processedFrames;
	
	/**
	 * Number of levels considered to retried the subgraph used in the local optimization.
	 */
	private int rbaRings;
	
	// TODO remove?
	/**
	 * Cores used in the system.
	 */
	private int cores;

	/**
	 * The last weighted mean chi squared.
	 */
	private double lastWMChi2;
	
	/**
	 * If true the algorithm will execute the loop check.
	 */
	private boolean tryLoopInference;
	
	/**
	 * True during the first iteration of the algorithm, false after that.
	 */
	private boolean firstFrame;
	
	/**
	 * Base constructor.
	 * @param inverseKCam The inverse of the camera matrix.
	 * @param params Additional parameters.
	 */
	public SlamDunk(EigenMatrix3F inverseKCam, SlamDunkParams params)
	{
		this.inverseKCam = inverseKCam;
		cameraTracker = params.cameraTracker;
		movedFrames = new ArrayList<Pair<Double, EigenIsometry3D>>();
		minimumExtractedFeatures = params.minimumExtractedFeatures;
		keyframeOverlapping = params.keyframeOverlapping;
		processedFrames = 0;
		rbaRings = params.rbaRings;
		cores = params.cores;
		lastWMChi2 = -1.0;
		tryLoopInference = params.tryLoopInference;
		firstFrame = true;
	}
	
	/**
	 * Gets the inverse of the camera matrix.
	 * @return The inverse of the camera matrix.
	 */
	public EigenMatrix3F getInverseKCam()
	{
		return inverseKCam;
	}
	
	/**
	 * Returns the mapped poses.
	 * @param poses The mapped poses.
	 */
	public void getMappedPoses(List<Pair<Double, EigenIsometry3D>> poses)
	{
		for (PoseVertex vx : graph.getVertices())
			poses.add(new Pair<Double, EigenIsometry3D>(vx.getUserData().getTimestamp(), vx.getEstimate()));
	}
	
	/**
	 * Gets the moved frames in the current iteration.
	 * @return The moved frames.
	 */
	public List<Pair<Double, EigenIsometry3D>> getMovedFrames()
	{
		return movedFrames;
	}
	
	/**
	 * Gets the frame data specified by the identifier.
	 * @param id The identifier.
	 * @return The frame data.
	 */
	public FrameData getFrameData(int id)
	{
		return graph.getVertexData(id);
	}
	
	/**
	 * Gets the residual from the last optimization.
	 * @return The residual.
	 */
	public double getLastWeightedMeanChi2()
	{
		return lastWMChi2;
	}
	
	/**
	 * Gets the number of keyframes currently managed by the algorithm.
	 * @return The number of keyframes.
	 */
	public int getNumberOfKeyframes()
	{
		return graph.getNumberOfVertices();
	}
	
	/**
	 * Executes an iteration of the SLAM algorithm.
	 * @param frame The input frame (BGRD data).
	 * @param estimatedPose The estimated pose.
	 * @return An enumerative showing the result of the operation (tracking failed, frame tracked or keyframe detected).
	 */
	public SlamDunkResult execute(DataBGRD frame, EigenIsometry3D estimatedPose)
	{
		SlamDunkResult trackingState = SlamDunkResult.TRACKING_FAILED;

		long start;
		
		// First frame
		if (firstFrame)
		{
			firstFrame = false;
			
			// Extract frame data
			FrameData frameData = new FrameData();
			cameraTracker.extractFrameData(frame, frameData);
			
			// Check number of features
			if (frameData.getDescriptors().rows() < minimumExtractedFeatures)
			{
				Logger.getLogger("SLAM").warning("Too few features extracted, first frame not set");
			}
			else
			{
				trackingState = SlamDunkResult.KEYFRAME_DETECTED;
				
				graph = new GraphBackend();
				
				// Create a new vertex and set its frame data
				estimatedPose.setMatrix(EigenMatrix4D.getIdentity());
				graph.setVertexEstimate(processedFrames, estimatedPose, frameData);
				
				if (tryLoopInference)
					initDijkstraNative();
				
				movedFrames.clear();
				Map<Integer, PoseVertex> poseSet = new Hashtable<Integer, PoseVertex>();
				for (PoseVertex vx : graph.getVertices())
				{
					poseSet.put(vx.getId(), vx);
					movedFrames.add(new Pair<Double, EigenIsometry3D>(vx.getUserData().getTimestamp(), vx.getEstimate()));
				}
				
				cameraTracker.reset(estimatedPose, frameData);
				cameraTracker.setFrames(poseSet);
			}
		}
		else
		{
			if (cameraTracker.track(frame))
			{
				trackingState = SlamDunkResult.FRAME_TRACKED;
				
				TrackingResult tr = cameraTracker.getLastTrackingResult();
				estimatedPose.setMatrix(new EigenMatrix4D(tr.pose.getMatrix().getValue().clone()));
				
				System.out.println("OVERLAPPING SCORE: " + tr.overlappingScore);
				
				// Keyframe detection
				boolean isKeyframe = tr.overlappingScore < keyframeOverlapping;
				
				// Check if a loop closure has happened
				if (!isKeyframe && tryLoopInference)
				{
					int[] trackedKeyframes = new int[cameraTracker.getLastTrackedKeyframes().size()];
					int trId = 0;
					for (Integer i : cameraTracker.getLastTrackedKeyframes())
					{
						trackedKeyframes[trId] = i;
						trId++;
					}
					
					isKeyframe = loopClosureCheck(trackedKeyframes, trackedKeyframes.length, rbaRings);

					if (isKeyframe)
						Logger.getLogger("SLAM").info("Metric loop detected!");
				}
				
				if (isKeyframe)
				{
					trackingState = SlamDunkResult.KEYFRAME_DETECTED;
					
					PoseVertex currentVx = graph.setVertexEstimate(processedFrames, estimatedPose, tr.frameData);
					if (tryLoopInference)
						updateAdjacencyMap(currentVx.getId());
					
					// RBA optimization
					Set<Integer> vSet = new HashSet<Integer>();
					Set<Integer> fSet = new HashSet<Integer>();
					graph.addFrameMatchesXYZ(processedFrames, tr.ffMatches, null);
					start = System.currentTimeMillis();
					int totalIterations = graph.relativeOptimization(processedFrames, rbaRings, 100, vSet, fSet);
					System.out.println("OPTIMIZE: " + (System.currentTimeMillis() - start) + " - Iterations -> " + totalIterations);
					lastWMChi2 = graph.getWeightedMeanChi2();

					// Get solution
					movedFrames.clear();
					Map<Integer, PoseVertex> poseSet = new Hashtable<Integer, PoseVertex>();
					for (Integer vxId : vSet)
					{
						PoseVertex vx = graph.getVertex(vxId);
						poseSet.put(vxId, vx);
						movedFrames.add(new Pair<Double, EigenIsometry3D>(vx.getUserData().getTimestamp(), vx.getEstimate()));
					}
					
					// Signal moved frames
					cameraTracker.signalMovedFrames(poseSet);
					graph.getVertexEstimate(processedFrames, estimatedPose);
				}
				else
				{
					cameraTracker.updateMap();
				}
			}
		}
		
		if (trackingState != SlamDunkResult.TRACKING_FAILED)
			processedFrames++;
		
		return trackingState;
	}
	
	/**
	 * Executes the algorithm without taking the resulting pose.
	 * @param frame The input frame (BGRD data).
	 * @return The result of the execution.
	 */
	public SlamDunkResult execute(DataBGRD frame)
	{
		EigenIsometry3D fake = new EigenIsometry3D();
		return execute(frame, fake);
	}
	
	/**
	 * Forces global optimization of the graph.
	 */
	public void forceGlobalOptimization()
	{
		graph.optimize(100);
		lastWMChi2 = graph.getWeightedMeanChi2();
		
		// Get solution
		movedFrames.clear();
		Map<Integer, PoseVertex> poseSet = new Hashtable<Integer, PoseVertex>();
		for (PoseVertex vx : graph.getVertices())
		{
			poseSet.put(vx.getId(), vx);
			movedFrames.add(new Pair<Double, EigenIsometry3D>(vx.getUserData().getTimestamp(), vx.getEstimate()));
		}
		
		// Signal moved frames
		cameraTracker.signalMovedFrames(poseSet);
	}
	
	private native void initDijkstraNative();
	
	private native boolean loopClosureCheck(int[] trackedKeyframes, int size, int rbaRings);
	
	private native void updateAdjacencyMap(int id);
}