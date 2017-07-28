package it.unibo.slam.datatypes;

import it.unibo.slam.datatypes.eigen.typedouble.EigenIsometry3D;

/**
 * Class storing the additional data used by the Renderer.<br>
 * This data is used to show in the user interface the RGB and depth images and the FPS counter.
 */
public class DataRendererAdditional
{
	/**
	 * RGB image.
	 */
	private DataRGB image;
	
	/**
	 * Depth image.
	 */
	private DataGrayscale depth;
	
	/**
	 * FPS value.
	 */
	private DataFPS fps;
	
	/**
	 * The camera pose.
	 */
	private EigenIsometry3D cameraPose;
	
	/**
	 * The camera pose.
	 */
	private EigenIsometry3D cameraPose2;
	
	/**
	 * Basic constructor.
	 * @param image The RGB image.
	 * @param depth The depth image.
	 * @param fps The FPS value.
	 * @param cameraPose The camera pose.
	 */
	public DataRendererAdditional(DataRGB image, DataGrayscale depth, DataFPS fps, EigenIsometry3D cameraPose, EigenIsometry3D cameraPose2)
	{
		this.image = image;
		this.depth = depth;
		this.fps = fps;
		this.cameraPose = cameraPose;
		this.cameraPose2 = cameraPose2;
	}
	
	/**
	 * Gets the RGB image data.
	 * @return The RGB image data.
	 */
	public DataRGB getImageData()
	{
		return image;
	}
	
	/**
	 * Gets the depth image data.
	 * @return The depth image data.
	 */
	public DataGrayscale getDepthData()
	{
		return depth;
	}
	
	/**
	 * Gets the FPS data.
	 * @return The FPS data.
	 */
	public DataFPS getFPSData()
	{
		return fps;
	}
	
	/**
	 * Gets the camera pose.
	 * @return The camera pose.
	 */
	public EigenIsometry3D getCameraPose()
	{
		return cameraPose;
	}
	
	public EigenIsometry3D getCameraPose2()
	{
		return cameraPose2;
	}
}
