package it.unibo.slam.input;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import it.unibo.slam.buffers.interfaces.WriteOnlyDoubleBuffer;
import it.unibo.slam.datatypes.DataBGRD;
import it.unibo.slam.datatypes.params.IntrinsicParams;
import it.unibo.slam.input.interfaces.SensorGrabberBGRD;

/**
 * Grabs the images from some files and writes them in the output buffer.
 */
public class ImageGrabber implements SensorGrabberBGRD
{
	/**
	 * The max depth in meters.
	 */
	private float maxDepth;
	
	/**
	 * True if the grabber has already been initialized, false otherwise.
	 */
	private boolean initialized;
	
	/**
	 * True if the grabber is paused, false otherwise.
	 */
	private boolean paused;
	
	/**
	 * True if the grabber has to be terminated, false otherwise.
	 */
	private boolean terminate;
	
	/**
	 * Focal length on x coordinate.
	 */
	private float focalX;
	
	/**
	 * Focal length on y coordinate.
	 */
	private float focalY;
	
	/**
	 * Center of view on x coordinate.
	 */
	private float centerX;
	
	/**
	 * Center of view on y coordinate.
	 */
	private float centerY;
	
	/**
	 * The input stream of the file containing the path to all the images to load.<br/>
	 * The format of the file is the following: [timestamp] [rgb-file-name] [timestamp] [depth-file-name].
	 */
	private InputStream imagesDescriptorStream;
	
	/**
	 * The sub path that will be added to the root directory to specify the starting folder from where to find the images (depth and RGB).
	 * The root directory is the external storage default directory. To this sub path will be added the path to the single image files.
	 */
	private String subPathImages;
	
	/**
	 * Timestamps for all the depth images.
	 */
	private List<Double> timestamps;
	
	/**
	 * Path names for all the RGB images.
	 */
	private List<String> rgbPathNames;
	
	/**
	 * Path names for all the depth images.
	 */
	private List<String> depthPathNames;
	
	/**
	 * The current index in the list of images to read.
	 * It gets incremented after every call to the {@link #grab()}grab() method.
	 */
	private int currentImageIndex;
	
	/**
	 * Buffer in which the data will be written (BGRD image and depth).
	 */
	private WriteOnlyDoubleBuffer<DataBGRD> doubleBufferBGRD;
	
	/**
	 * Temporary RGB image.
	 */
	private Bitmap rgbImageTemp = null;
	
	/**
	 * Temporary RGB pixels (integers).
	 */
	private int[] rgbPixelsInt = null;
	
	/**
	 * Temporary depth image.
	 */
	private Mat depthImageTemp = null;
	
	/**
	 * Temporary depth values (shorts).
	 */
	private short[] depthPixelsShort = null;
	
	/**
	 * Base constructor (default intrinsic parameters).
	 * @param imagesDescriptorStream Stream used to read the descriptor file.
	 * @param doubleBuffer The buffer in which the data will be written.
	 */
	public ImageGrabber(InputStream imagesDescriptorStream, WriteOnlyDoubleBuffer<DataBGRD> doubleBufferBGRD,
						String subPathImages)
	{
		this(	imagesDescriptorStream, doubleBufferBGRD, subPathImages, MAX_DEPTH_DEFAULT,
				new IntrinsicParams(DEFAULT_FOCAL_X, DEFAULT_FOCAL_Y,
									DEFAULT_CENTER_X, DEFAULT_CENTER_Y));
	}
	
	/**
	 * Constructor that specifies the camera intrinsic parameters.
	 * @param imagesDescriptorStream Stream used to read the descriptor file.
	 * @param doubleBuffer The buffer in which the data will be written.
	 * @param cameraIntrinsicParams Intrinsic camera parameters.
	 */
	public ImageGrabber(InputStream imagesDescriptorStream, WriteOnlyDoubleBuffer<DataBGRD> doubleBufferBGRD,
						String subPathImages, float maxDepth, IntrinsicParams cameraIntrinsicParams)
	{
		this.imagesDescriptorStream = imagesDescriptorStream;
		this.doubleBufferBGRD = doubleBufferBGRD;
		this.subPathImages = subPathImages;
		this.maxDepth = maxDepth;
		focalX = cameraIntrinsicParams.getFocalX();
		focalY = cameraIntrinsicParams.getFocalY();
		centerX = cameraIntrinsicParams.getCenterX();
		centerY = cameraIntrinsicParams.getCenterY();
		timestamps = new ArrayList<Double>();
		rgbPathNames = new ArrayList<String>();
		depthPathNames = new ArrayList<String>();
		currentImageIndex = 0;
		initialized = false;
		paused = true;
		terminate = false;
	}
	
	@Override
	public void init() throws Exception
	{
		if (initialized)
			return;
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(imagesDescriptorStream));
		
		// Store the timestamps and path names in some lists for usage during the execution
		String line = reader.readLine();
		while (line != null)
		{
			String[] splittedLine = line.split(" ");
			if (splittedLine == null || splittedLine.length != 4)
				throw new RuntimeException("Incorrect file format.");
			
			timestamps.add(Double.valueOf(splittedLine[2]));
			rgbPathNames.add(splittedLine[1]);
			depthPathNames.add(splittedLine[3]);
			
			line = reader.readLine();
		}
		
		reader.close();
		initialized = true;
		paused = false;
	}

	@Override
	public synchronized void pause()
	{
		paused = true;
	}

	@Override
	public synchronized void resume()
	{
		paused = false;
		notify();
	}
	
	@Override
	public synchronized void terminate()
	{
		terminate = true;
		notify();
	}
	
	@Override
	public DataBGRD grab() throws Exception
	{
		if (!initialized || terminate)
			return null;
		
		synchronized (this)
		{
			while (paused)
			{
				if (terminate)
					return null;
				
				try
				{
					wait();
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		}
		
		if (currentImageIndex >= rgbPathNames.size())
		{
			terminate = true;
			return null;
		}
		
		double timestamp = timestamps.get(currentImageIndex);
		String rgbPathName = rgbPathNames.get(currentImageIndex);
		String depthPathName = depthPathNames.get(currentImageIndex);
		
		String externalStoragePath = Environment.getExternalStorageDirectory().getAbsolutePath();
		rgbImageTemp = BitmapFactory.decodeFile(externalStoragePath + "/" + subPathImages + "/" + rgbPathName);
		depthImageTemp = Highgui.imread(externalStoragePath + "/" + subPathImages + "/" + depthPathName, Highgui.CV_LOAD_IMAGE_ANYDEPTH);
		
		int width = rgbImageTemp.getWidth();
		int height = rgbImageTemp.getHeight();
		int size = width * height;
		
		if (rgbPixelsInt == null)
			rgbPixelsInt = new int[size];
		if (depthPixelsShort == null)
			depthPixelsShort = new short[size];
		
		rgbImageTemp.getPixels(rgbPixelsInt, 0, width, 0, 0, width, height);
		depthImageTemp.get(0, 0, depthPixelsShort);
		
		byte bgrPixels[] = new byte[rgbImageTemp.getWidth() * rgbImageTemp.getHeight() * 3];
		float depthPixels[] = new float[depthImageTemp.cols() * depthImageTemp.rows()];
		for (int i = 0, j = 0; i < size; i++, j += 3)
		{
			bgrPixels[j] = (byte)rgbPixelsInt[i];
			bgrPixels[j + 1] = (byte)(rgbPixelsInt[i] >> 8);
			bgrPixels[j + 2] = (byte)(rgbPixelsInt[i] >> 16);
			depthPixels[i] = (float)depthPixelsShort[i] * 0.0002F;
		}
		
		currentImageIndex++;
		
		return new DataBGRD(timestamp, bgrPixels, depthPixels, rgbImageTemp.getWidth(), rgbImageTemp.getHeight(), maxDepth);
	}

	@Override
	public void run()
	{
		if (!initialized)
			return;
		
		try
		{
			long startTime = System.currentTimeMillis(), endTime;
			DataBGRD imagesCollected = null;
			
			while (!terminate)
			{
				imagesCollected = grab();
				
				if (imagesCollected != null)
				{
					doubleBufferBGRD.fillBuffer(imagesCollected);
					
					endTime = System.currentTimeMillis();
					
					if (currentImageIndex < rgbPathNames.size())
					{
						long timestampCurrentMillis = (long)(timestamps.get(currentImageIndex - 1) * 1000);
						long timestampNextMillis = (long)(timestamps.get(currentImageIndex) * 1000);
						if ((endTime - startTime) < (timestampNextMillis - timestampCurrentMillis))
							Thread.sleep((timestampNextMillis - timestampCurrentMillis) - (endTime - startTime));
					}
					
					startTime = System.currentTimeMillis();
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public float getMaxDepth()
	{
		return maxDepth;
	}

	@Override
	public boolean isInitialized()
	{
		return initialized;
	}
	
	@Override
	public boolean isPaused()
	{
		return paused;
	}

	@Override
	public IntrinsicParams getIntrinsicParams()
	{
		return new IntrinsicParams(focalX, focalY, centerX, centerY);
	}
}
