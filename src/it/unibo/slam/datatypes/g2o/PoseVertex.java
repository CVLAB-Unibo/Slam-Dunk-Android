package it.unibo.slam.datatypes.g2o;

import it.unibo.slam.datatypes.FrameData;
import it.unibo.slam.datatypes.eigen.typedouble.EigenIsometry3D;

/**
 * Substitute of g2o class VertexSE3 and parent classes.
 */
public class PoseVertex
{
	/**
	 * Identificator of the vertex.
	 */
	private int id;
	
	/**
	 * Vertex pose.
	 */
	private EigenIsometry3D estimate;
	
	/**
	 * Data inside the vertex.
	 */
	private FrameData userData;
	
	/**
	 * Base constructor.
	 * @param id Vertex id.
	 * @param estimate Vertex pose.
	 * @param userData Data.
	 */
	public PoseVertex(int id, EigenIsometry3D estimate, FrameData userData)
	{
		this.id = id;
		this.estimate = estimate;
		this.userData = userData;
	}
	
	/**
	 * Constructor with null data.
	 * @param id Vertex id.
	 * @param estimate Vertex pose.
	 */
	public PoseVertex(int id, EigenIsometry3D estimate)
	{
		this(id, estimate, null);
	}
	
	/**
	 * Constructor with default pose (identity matrix).
	 * @param id Vertex id.
	 * @param userData Data.
	 */
	public PoseVertex(int id, FrameData userData)
	{
		this(id, EigenIsometry3D.getIdentity(), userData);
	}
	
	/**
	 * Constructor with default pose (identity matrix) and null data.
	 * @param id Vertex id.
	 */
	public PoseVertex(int id)
	{
		this(id, EigenIsometry3D.getIdentity(), null);
	}
	
	/**
	 * Gets the vertex id.
	 * @return The vertex id.
	 */
	public int getId()
	{
		return id;
	}
	
	/**
	 * Gets the vertex pose.
	 * @return The vertex pose.
	 */
	public EigenIsometry3D getEstimate()
	{
		return estimate;
	}
	
	/**
	 * Sets the vertex pose.
	 * @param estimate The new vertex pose.
	 */
	public void setEstimate(EigenIsometry3D estimate)
	{
		this.estimate = estimate;
	}
	
	/**
	 * Gets the data.
	 * @return The data.
	 */
	public FrameData getUserData()
	{
		return userData;
	}
	
	/**
	 * Sets the data.
	 * @param userData The data.
	 */
	public void setUserData(FrameData userData)
	{
		this.userData = userData;
	}
}
