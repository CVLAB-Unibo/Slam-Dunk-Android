package it.unibo.slam.graph;

import it.unibo.slam.datatypes.FrameData;
import it.unibo.slam.datatypes.eigen.typedouble.EigenIsometry3D;
import it.unibo.slam.datatypes.eigen.typedouble.EigenMatrix4D;
import it.unibo.slam.datatypes.g2o.PoseVertex;
import it.unibo.slam.matcher.FrameToFrameMatch;

import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Back-end for the G2O optimized graph.
 */
public class GraphBackend
{
	/**
	 * Vertices contained in the graph (complete copy in the Java code, while 
	 * just the id and the estimate is available in the native C++ code).
	 */
	private Map<Integer, PoseVertex> vertices;
	
	/**
	 * Base constructor, initializes the native graph and the Java map.
	 */
	public GraphBackend()
	{
		vertices = new Hashtable<Integer, PoseVertex>();
		initNativeGraph();
	}
	
	/**
	 * Gets the vertices inside the graph.
	 * @return The vertices.
	 */
	public Collection<PoseVertex> getVertices()
	{
		return vertices.values();
	}
	
	/**
	 * Returns the vertex identified by an id.
	 * @param id The id of the vertex to search.
	 * @return The found vertex, or <code>null</code> if the vertex is not present.
	 */
	public PoseVertex getVertex(int id)
	{
		return vertices.get(id);
	}
	
	/**
	 * Gets the chi squared value.
	 * @return The chi squared.
	 */
	public double getChi2()
	{
		return getChi2Native();
	}
	
	/**
	 * Gets the weighted mean chi squared value.
	 * @return The weighted mean chi squared.
	 */
	public double getWeightedMeanChi2()
	{
		return getWeightedMeanChi2Native();
	}
	
	/**
	 * Gets the number of vertices in the graph.
	 * @return The number of vertices.
	 */
	public int getNumberOfVertices()
	{
		return vertices.size();
	}
	
	/**
	 * Gets the number of edges in the graph.
	 * @return The number of edges.
	 */
	public int getNumberOfEdges()
	{
		return getNumberOfEdgesNative();
	}
	
	/**
	 * Clear this graph, removing all the vertices and edges.
	 */
	public void clear()
	{
		vertices.clear();
		clearNative();
	}
	
	/**
	 * Sets a vertex as fixed in the graph (used for the optimization process).
	 * @param viewId The id of the vertex.
	 * @param fixed If true the vertex is fixed.
	 */
	public void setFixedView(int viewId, boolean fixed)
	{
		setFixedViewNative(viewId, fixed);
	}
	
	/**
	 * Sets the estimate matrix and the frame data for this vertex.
	 * @param id The vertex id.
	 * @param estimate The estimate matrix.
	 * @param data The frame data.
	 * @return The updated pose vertex. If no vertex exists with the given id, a new vertex is created.
	 */
	public PoseVertex setVertexEstimate(int id, EigenIsometry3D estimate, FrameData data)
	{
		PoseVertex vx = getPose(id, true, estimate);
		vx.setEstimate(estimate);
		vx.setUserData(data);
		return vx;
	}
	
	/**
	 * Gets the estimate matrix of the vertex.
	 * @param id The vertex id.
	 * @param tr The estimate matrix to return.
	 * @return True if the vertex has been found, false otherwise.
	 */
	public boolean getVertexEstimate(int id, EigenIsometry3D tr)
	{
		PoseVertex vx = getPose(id);
		if (vx == null)
			return false;
		tr.setMatrix(new EigenMatrix4D(vx.getEstimate().getMatrix().getValue().clone()));
		return true;
	}
	
	/**
	 * Sets the frame data of the vertex.
	 * @param id The vertex id.
	 * @param data The frame data.
	 */
	public void setVertexData(int id, FrameData data)
	{
		PoseVertex vx = getPose(id);
		if (vx != null)
			vx.setUserData(data);
	}
	
	/**
	 * Gets the frame data of the vertex.
	 * @param id The vertex id.
	 * @return The frame data to return, or null in case the vertex has not been found.
	 */
	public FrameData getVertexData(int id)
	{
		PoseVertex vx = getPose(id);
		return ((vx != null) ? vx.getUserData() : null);
	}
	
	/**
	 * Gets both the estimate matrix and the frame data of a vertex.
	 * @param id The vertex id.
	 * @param tr The estimate matrix, it will be set just if the vertex is found.
	 * @return The frame data of the vertex, or null in case the vertex has not been found.
	 */
	public FrameData getVertexEstimateAndData(int id, EigenIsometry3D tr)
	{
		PoseVertex vx = getPose(id);
		if (vx == null)
			return null;
		tr.setMatrix(new EigenMatrix4D(vx.getEstimate().getMatrix().getValue().clone()));
		return vx.getUserData();
	}
	
	/**
	 * Calculates the relative transformation used to pass from a given vertex estimate to another one.<br>
	 * Given a starting matrix M1 and a final matrix M2, we search the transformation matrix T given by this formula:<br>
	 * M1 * T = M2.
	 * @param idFrom The vertex whose estimate is used as the starting point (M1).
	 * @param idTo The vertex whose estimate is used as the one to obtain (M2).
	 * @param tr The resulting matrix (T).
	 * @return False if at least one of the two vertices is not present in the graph, true otherwise.
	 */
	public boolean getRelativeTransformation(int idFrom, int idTo, EigenIsometry3D tr)
	{
	  PoseVertex vFrom = getPose(idFrom);
	  PoseVertex vTo = getPose(idTo);
	  if (vFrom == null || vTo == null)
	    return false;
	  tr.setMatrix(new EigenMatrix4D(calculateRelativeTransformation(	vTo.getEstimate().getMatrix().getValue(),
			  															vFrom.getEstimate().getMatrix().getValue())));
	  return true;
	}
	
	/**
	 * Updates the vertex poses.
	 * @param poses The poses used for the update.
	 */
	public void updatePoses(Map<Integer, EigenIsometry3D> poses)
	{
		if (vertices.isEmpty())
			return;
		
		EigenIsometry3D pivotToWorld = null;
		EigenIsometry3D poseFromMap = null;
		for (Entry<Integer, EigenIsometry3D> entry : poses.entrySet())
		{
			PoseVertex vx = getPose(entry.getKey());
			if (vx != null)
			{
				pivotToWorld = vx.getEstimate();
				poseFromMap = entry.getValue();
				break;
			}
		}
		
		if (poseFromMap == null)
			return;
		
		EigenIsometry3D frameTransformation = new EigenIsometry3D(new EigenMatrix4D(calculateFrameTransformation(
													pivotToWorld.getMatrix().getValue(), poseFromMap.getMatrix().getValue())));
		
		if (poses.size() > vertices.size())
		{
			for (PoseVertex vx : vertices.values())
			{
				EigenIsometry3D tempIsometry = poses.get(vx.getId());
				if (tempIsometry != null)
				{
					EigenIsometry3D tempResult = new EigenIsometry3D();
					frameTransformation.getMatrix().multiplyWith(tempIsometry.getMatrix(), tempResult.getMatrix());
					vx.setEstimate(tempResult);
					setVertexEstimateNative(vx.getId(), tempResult.getMatrix().getValue());
				}
			}
		}
		else
		{
			for (Entry<Integer, EigenIsometry3D> entry : poses.entrySet())
			{
				PoseVertex vx = getPose(entry.getKey());
				if (vx != null)
				{
					EigenIsometry3D tempResult = new EigenIsometry3D();
					frameTransformation.getMatrix().multiplyWith(entry.getValue().getMatrix(), tempResult.getMatrix());
					vx.setEstimate(tempResult);
					setVertexEstimateNative(vx.getId(), tempResult.getMatrix().getValue());
				}
			}
		}
	}
	
	/**
	 * Sets the vertex poses.
	 * @param poses The poses to set.
	 */
	public void setPoses(Map<Integer, EigenIsometry3D> poses)
	{
		if (poses.size() > vertices.size())
		{
			for (PoseVertex vx : vertices.values())
			{
				EigenIsometry3D tempIsometry = poses.get(vx.getId());
				if (tempIsometry != null)
				{
					vx.setEstimate(tempIsometry);
					setVertexEstimateNative(vx.getId(), tempIsometry.getMatrix().getValue());
				}
			}
		}
		else
		{
			for (Entry<Integer, EigenIsometry3D> entry : poses.entrySet())
			{
				PoseVertex vx = getPose(entry.getKey());
				if (vx != null)
				{
					vx.setEstimate(entry.getValue());
					setVertexEstimateNative(vx.getId(), entry.getValue().getMatrix().getValue());
				}
			}
		}
	}
	
	/**
	 * Removes a pose.
	 * @param id The pose id.
	 */
	public void removePose(int id)
	{
		PoseVertex vx = vertices.remove(id);
		if (vx != null)
			removeVertexNative(id);
	}
	
	/**
	 * Adds the edges between a given vertex and the vertices that share a match with it.
	 * @param referenceId The vertex id of reference.
	 * @param matches The list of matches between the reference and other connected vertices present in the graph.
	 * @param matchingFrames The set of identifiers from the vertices that match the reference id.
	 */
	public void addFrameMatchesXYZ(int referenceId, List<FrameToFrameMatch> matches, Set<Integer> matchingFrames)
	{
		// Get or create the vertex
		PoseVertex referenceVx = getPose(referenceId, true, null);
		FrameData referenceData = referenceVx.getUserData();
		
		for (FrameToFrameMatch ffMatch : matches)
		{
			// Get the vertex
			PoseVertex matchingVx = getPose(ffMatch.imgIdx);
			if (matchingVx == null)
				return;
			FrameData matchingData = matchingVx.getUserData();
			if (matchingFrames != null)
				matchingFrames.add(ffMatch.imgIdx);
			
			addEdgeNative(	referenceVx.getId(), matchingVx.getId(), referenceData.getKeypoints().get(ffMatch.queryIdx).toDouble().getValue(),
							matchingData.getKeypoints().get(ffMatch.trainIdx).toDouble().getValue(), ffMatch.score);
		}
	}
	
	/**
	 * Executes the optimization on all the vertices, limited to a certain number of iterations.
	 * @param maximumNumberOfIterations The maximum number of iterations.
	 * @return The number of iterations executed by the optimization.
	 */
	public int optimize(int maximumNumberOfIterations)
	{
		int result = optimizeNative(maximumNumberOfIterations);
		
		for (int i : vertices.keySet())
			vertices.get(i).getEstimate().getMatrix().setValue(getVertexEstimateNative(i));
		
		return result;
	}
	
	/**
	 * Execute a relative optimization that considers just a certain number of levels in the graph, starting from a root node.
	 * @param frameId The id of the vertex that will be the root of the relative optimization graph.
	 * @param numberOfRings The number of levels to consider.
	 * @param maximumNumberOfIterations The maximum number of iterations.
	 * @param vSet The set of free vertices.
	 * @param fSet The set of fixed vertices.
	 * @return The number of iterations executed by the relative optimization.
	 */
	public int relativeOptimization(int frameId, int numberOfRings, int maximumNumberOfIterations, Set<Integer> vSet, Set<Integer> fSet)
	{
		if (vertices.size() <= 1)
			return 0;
		
		PoseVertex vx = getPose(frameId);
		if (vx == null)
			return 0;
		
		// Active sets
		Set<Integer> vertexSet = (vSet == null) ? new HashSet<Integer>() : vSet;
		Set<Integer> fixedSet = (fSet == null) ? new HashSet<Integer>() : fSet;
		
		if (vertexSet.isEmpty())
			createRBASubset(vx, numberOfRings, vertexSet, fixedSet);
		else
		{
			for (int i : fixedSet)
				setFixedView(i, true);
		}
		
		// We need a fixed pose
		if (fixedSet.isEmpty())
		{
			fixedSet.add(vx.getId());
			setFixedView(vx.getId(), true);
		}
		
		int[] vSetArray = new int[vertexSet.size()];
		int arrayId = 0;
		for (int i : vertexSet)
		{
			vSetArray[arrayId] = i;
			arrayId++;
		}
		
		int result = relativeOptimizeNative(vSetArray, vSetArray.length, maximumNumberOfIterations);
		
		for (int i : vertexSet)
			vertices.get(i).getEstimate().getMatrix().setValue(getVertexEstimateNative(i));
		
		for (int i : fixedSet)
			setFixedView(i, false);
		
		return result;
	}
	
	/**
	 * Creates the sub-graph.
	 * @param origin The root vertex.
	 * @param ring The number of levels.
	 * @param vSet The set of free vertices.
	 * @param fSet The set of fixed vertices.
	 */
	private void createRBASubset(PoseVertex origin, int ring, Set<Integer> vSet, Set<Integer> fSet)
	{
		createRBASubsetNative(origin.getId(), ring, vSet, fSet);
	}
	
	/**
	 * Gets the poses that have been optimized after the optimization.
	 * @param poses The optimized poses.
	 */
	public void getOptimizedPoses(Map<Integer, EigenIsometry3D> poses)
	{
		for (Entry<Integer, PoseVertex> entry : vertices.entrySet())
			poses.put(entry.getKey(), new EigenIsometry3D(new EigenMatrix4D(entry.getValue().getEstimate().getMatrix().getValue().clone())));
	}
	
	/**
	 * Gets the pose vertex from the current map, given its id.<br>
	 * The pose vertex can be created if specified so.
	 * @param poseId The id of the vertex.
	 * @param create If true and the vertex is not found, it will be created.
	 * @param estimateToAdd The estimate to add eventually to the native vertex (it is not added in the java map!).
	 * @return The pose vertex if created or found, null otherwise.
	 */
	protected PoseVertex getPose(int poseId, boolean create, EigenIsometry3D estimateToAdd)
	{
		PoseVertex vx = getPose(poseId);
		
		if (vx == null)
		{
			if (create)
			{
				vx = new PoseVertex(poseId, EigenIsometry3D.getIdentity());
				vertices.put(poseId, vx);
				addVertexWithEstimateNative(poseId, (estimateToAdd == null) ? 	EigenIsometry3D.getIdentity().getMatrix().getValue() :
																				estimateToAdd.getMatrix().getValue());
			}
		}
		else
		{
			if (estimateToAdd != null)
			{
				setVertexEstimateNative(poseId, estimateToAdd.getMatrix().getValue());
			}
		}
		
		return vx;
	}
	
	/**
	 * Gets the pose vertex from the current map, given its id.
	 * @param poseId The id of the vertex.
	 * @return The pose vertex if found, null otherwise.
	 */
	protected PoseVertex getPose(int poseId)
	{
		return vertices.get(poseId);
	}
	
	private native void initNativeGraph();
	
	private native double getChi2Native();
	
	private native double getWeightedMeanChi2Native();
	
	private native int getNumberOfEdgesNative();
	
	private native void clearNative();
	
	private native void setFixedViewNative(int viewId, boolean fixed);
	
	private native void addVertexWithEstimateNative(int poseId, double[] estimate);
	
	private native void setVertexEstimateNative(int poseId, double[] estimate);
	
	private native double[] getVertexEstimateNative(int poseId);
	
	private native void removeVertexNative(int poseId);
	
	private native double[] calculateRelativeTransformation(double[] to, double[] from);
	
	private native double[] calculateFrameTransformation(double[] pivotToWorld, double[] poseFromMap);
	
	private native void addEdgeNative(int referenceId, int matchingId, double[] referenceVx, double[] matchingVx, double score);
	
	private native int optimizeNative(int maximumNumberOfIterations);
	
	private native int relativeOptimizeNative(int[] vSet, int size, int maximumNumberOfIterations);
	
	private native void createRBASubsetNative(int originId, int ring, Object vSet, Object fSet);
}