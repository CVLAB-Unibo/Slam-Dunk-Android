package it.unibo.slam.datatypes;

import java.util.List;

import it.unibo.slam.datatypes.eigen.typefloat.EigenMatrix4F;

/**
 * Class representing data in the form of a point cloud with assigned timestamp and the poses that have been updated.
 */
public class DataNewPointCloudAndPoseUpdates
{
	/**
	 * Timestamp associated to the new point cloud.
	 */
	private double timestamp;
	
	/**
	 * Vertices of the point cloud (3 floats each - XYZ).
	 */
	private float[] vertices;
	
	/**
	 * Colors of the vertices (3 bytes each - RGB).
	 */
	private byte[] colors;
	
	/**
	 * The model matrix to apply to the new point cloud.
	 */
	private EigenMatrix4F modelMatrix;
	
	/**
	 * Timestamps and poses of the point clouds to update.
	 */
	private List<Pair<Double, EigenMatrix4F>> movedClouds;
	
	/**
	 * Basic constructor.
	 * @param timestamp The timestamp.
	 * @param vertices The vertices.
	 * @param colors The vertex colors.
	 * @param modelMatrix The model matrix.
	 * @param movedClouds The timestamps and poses of the point clouds to update.
	 * @movedClouds The updated poses of the point clouds.
	 */
	public DataNewPointCloudAndPoseUpdates(double timestamp, float[] vertices, byte[] colors, EigenMatrix4F modelMatrix,
			List<Pair<Double, EigenMatrix4F>> movedClouds)
	{
		if (vertices == null || colors == null || modelMatrix == null)
			throw new IllegalArgumentException("One of the arguments is null.");
		
		if (vertices.length != colors.length)
			throw new IllegalArgumentException("The length of the vertices array is not equal to the lenght of the color array.");
		
		this.timestamp = timestamp;
		this.vertices = vertices;
		this.colors = colors;
		this.modelMatrix = modelMatrix;
		this.movedClouds = movedClouds;
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
	 * Gets the vertices data.
	 * @return The vertices data.
	 */
	public float[] getVertices()
	{
		return vertices;
	}
	
	/**
	 * Gets the colors data.
	 * @return The colors data.
	 */
	public byte[] getColors()
	{
		return colors;
	}
	
	/**
	 * Gets the model matrix.
	 * @return The model matrix.
	 */
	public EigenMatrix4F getModelMatrix()
	{
		return modelMatrix;
	}
	
	/**
	 * Gets the updated poses of the point clouds.
	 * @return The updated poses of the point clouds.
	 */
	public List<Pair<Double, EigenMatrix4F>> getMovedClouds()
	{
		return movedClouds;
	}
}
