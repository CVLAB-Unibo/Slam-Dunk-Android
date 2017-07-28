package it.unibo.slam.export;

import java.util.List;

import it.unibo.slam.export.ply.PlyFileType;
import it.unibo.slam.export.ply.PointCloudPlyExporter;
import it.unibo.slam.gui.opengl.model.PointCloudStatic;

public class PointCloudExporter
{
	/**
	 * Saves the input point clouds into a file with a certain format.
	 * @param fileName The file path.
	 * @param fileFormat The file format.
	 * @param pointClouds The input point clouds to save.
	 * @return True if the save operation has been successful, false otherwise.
	 */
	public static boolean exportTo(String fileName, ExportFileFormat fileFormat, List<PointCloudStatic> pointClouds)
	{
		boolean result = false;
		
		switch (fileFormat)
		{
			case PLY_ASCII:
				result = PointCloudPlyExporter.exportTo(fileName, PlyFileType.ASCII, pointClouds);
				break;
				
			case PLY_BINARY_LITTLE_ENDIAN:
				result = PointCloudPlyExporter.exportTo(fileName, PlyFileType.BINARY_LITTLE_ENDIAN, pointClouds);
				break;
				
			case PLY_BINARY_BIG_ENDIAN:
				result = PointCloudPlyExporter.exportTo(fileName, PlyFileType.BINARY_BIG_ENDIAN, pointClouds);
				break;
				
			default:
				break;
		}
		
		return result;
	}
	
	/**
	 * This method calculates, based on the input point clouds and the desired octree resolution, a filtered point cloud optimized
	 * for the save operation. The operations executed to obtain the filtered point cloud are:<br>
	 * - Calculate the covariance matrix of the input clouds fused together;<br>
	 * - Move the fused point cloud using the covariance matrix eigenvectors and its centroid. Afterwards, calculate the minimum, 
	 *   maximum and center point of the bounding box in that position, eventually translated to fit the center in the origin of
	 *   the axis;<br>
	 * - Create the octree with the input resolution and the bounding box values and insert the points of the cloud in it. The octree 
	 *   will contain only one point for each voxel, if more points reach the same voxel the mean value will be taken;<br>
	 * - Get the resulting points from the octree, which will represent the output point cloud.
	 * @param inputPointClouds The input point clouds.
	 * @param resolution The octree resolution.
	 * @return The filtered point cloud together with its bounding box.
	 */
	public static ExportData getPointCloudAndBoundingBoxComponentsToExport(	List<PointCloudStatic> inputPointClouds, 
																			float resolution)
	{
		// Calculate the array length (by now color and vertex arrays need to have the same length)
		int totalArrayLength = 0;
		for (PointCloudStatic inputPointCloud : inputPointClouds)
			totalArrayLength += inputPointCloud.getVertexArrayLength();
		
		// Unify the point clouds
		float[] pointsIn = new float[totalArrayLength];
		byte[] colorsIn = new byte[totalArrayLength];
		int arrayOffset = 0;
		for (PointCloudStatic inputPointCloud : inputPointClouds)
		{
			int length = inputPointCloud.getVertexArrayLength();
			System.arraycopy(inputPointCloud.getTransformedVertexArray(), 0, pointsIn, arrayOffset, length);
			System.arraycopy(inputPointCloud.getColorArray(), 0, colorsIn, arrayOffset, length);
			arrayOffset += length;
		}
		
		// Generate filtered point cloud
		int totalArrayLengthDefinitive = generatePointCloudToExport(pointsIn, colorsIn, totalArrayLength, resolution);
		float[] min = new float[3];
		float[] max = new float[3];
		getBoundingBoxMinAndMax(min, max);
		float[] pointsOut = new float[totalArrayLengthDefinitive];
		byte[] colorsOut = new byte[totalArrayLengthDefinitive];
		getGeneratedPointCloud(pointsOut, colorsOut);
		
		// Return the point cloud
		ExportData result = new ExportData(pointsOut, colorsOut, min, max);
		return result;
	}
	
	private static native int generatePointCloudToExport(float[] pointsIn, byte[] colorsIn, int pointArrayLength, float resolution);
	
	private static native void getGeneratedPointCloud(float[] pointsOut, byte[] colorsOut);
	
	private static native void getBoundingBoxMinAndMax(float[] min, float[] max);
}
