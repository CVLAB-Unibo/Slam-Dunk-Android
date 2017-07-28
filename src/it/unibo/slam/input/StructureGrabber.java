package it.unibo.slam.input;

import java.io.IOException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import it.unibo.slam.buffers.interfaces.WriteOnlyDoubleBuffer;
import it.unibo.slam.datatypes.DataBGRD;
import it.unibo.slam.datatypes.params.IntrinsicParams;
import it.unibo.slam.input.interfaces.SensorGrabberBGRD;
import it.unibo.slam.utils.ImageUtils;

/**
 * Class representing the grabber of the Structure sensor (managed natively with OpenNI 2).
 */
public class StructureGrabber implements SensorGrabberBGRD, PreviewCallback
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
	 * True if new RGB data is available, false otherwise.
	 */
	private boolean rgbAvailable;
	
	/**
	 * Frame width.
	 */
	private int width;
	
	/**
	 * Frame height.
	 */
	private int height;
	
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
	 * Buffer in which the sensor data will be written (BGR image and depth) and used by the application manager.
	 */
	private WriteOnlyDoubleBuffer<DataBGRD> doubleBufferBGRD;
	
	/**
	 * The YUV buffer.
	 */
	private byte[] yuvBuffer;
	
	/**
	 * The buffered image.
	 */
	private byte[] bufferedImage;
	
	/**
	 * Dummy surface texture.
	 */
	private SurfaceTexture surfaceTexture;
	
	/**
	 * The device RGB camera.
	 */
	private Camera camera;
	
	private Lock pausedLock;
	
	private Condition pausedCondition;
	
	private Lock rgbAvailableLock;
	
	private Condition rgbAvailableCondition;
	
	/**
	 * Base constructor.
	 * @param doubleBufferBGRD The buffer in which the data will be written.
	 */
	public StructureGrabber(WriteOnlyDoubleBuffer<DataBGRD> doubleBufferBGRD)
	{
		this.doubleBufferBGRD = doubleBufferBGRD;
		maxDepth = MAX_DEPTH_DEFAULT;
		focalX = DEFAULT_FOCAL_X;
		focalY = DEFAULT_FOCAL_Y;
		centerX = DEFAULT_CENTER_X;
		centerY = DEFAULT_CENTER_Y;
		initialized = false;
		paused = true;
		terminate = false;
		rgbAvailable = false;
		pausedLock = new ReentrantLock();
		pausedCondition = pausedLock.newCondition();
		rgbAvailableLock = new ReentrantLock();
		rgbAvailableCondition = rgbAvailableLock.newCondition();
	}
	
	@Override
	public void init() throws Exception
	{
		if (initialized)
			return;
		
		boolean res = initNative();
		
		if (!res)
			throw new RuntimeException("Initialization error!");
		
		int[] resolution = new int[2];
		getResolutionNative(resolution);
		width = resolution[0];
		height = resolution[1];
		
		maxDepth = getMaxDepthNative();
		
		float[] focal = new float[2];
		float[] center = new float[2];
		getIntrinsicParamsNative(focal, center);
		focalX = focal[0];
		focalY = focal[1];
		centerX = center[0];
		centerY = center[1];
		
		// 12 is bits per pixel in NV21 image format.
		yuvBuffer = new byte[(int)(width * height * 12F/8F)];
		bufferedImage = new byte[width * height * 3];
		
		int cameraId = -1;
		for (int i = 0; i < Camera.getNumberOfCameras(); i++)
		{
			CameraInfo cameraInfo = new CameraInfo();
			Camera.getCameraInfo(i, cameraInfo);
			
			if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK)
			{
				cameraId = i;
				break;
			}
		}
		
		if (cameraId == -1)
			throw new RuntimeException("Initialization error!");
		
		camera = Camera.open(cameraId);
		
		surfaceTexture = new SurfaceTexture(42);
		
		Parameters params = camera.getParameters();
		params.setPreviewSize(width, height);
		params.setFocusMode(Parameters.FOCUS_MODE_INFINITY);
		camera.setParameters(params);
		try
		{
			camera.setPreviewTexture(surfaceTexture);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		camera.addCallbackBuffer(yuvBuffer);
		camera.setPreviewCallbackWithBuffer(this);
		camera.startPreview();
		
		initialized = true;
		paused = false;
	}
	
	@Override
	public void onPreviewFrame(byte[] data, Camera camera)
	{
		rgbAvailableLock.lock();
		ImageUtils.convertNV21toBGRNativeNEON(yuvBuffer, bufferedImage, width, height);
		rgbAvailable = true;
		rgbAvailableCondition.signalAll();
		camera.addCallbackBuffer(yuvBuffer);
		rgbAvailableLock.unlock();
	}
	
	@Override
	public void pause()
	{
		pausedLock.lock();
		paused = true;
		pausedLock.unlock();
	}

	@Override
	public void resume()
	{
		pausedLock.lock();
		paused = false;
		pausedCondition.signalAll();
		pausedLock.unlock();
	}
	
	@Override
	public void terminate()
	{
		//TODO deallocation
		pausedLock.lock();
		terminate = true;
		pausedCondition.signalAll();
		pausedLock.unlock();
	}
	
	@Override
	public DataBGRD grab() throws Exception
	{
		if (!initialized || terminate)
			return null;
		
		pausedLock.lock();
		while (paused)
		{
			if (terminate)
			{
				pausedLock.unlock();
				return null;
			}
			
			try
			{
				pausedCondition.await();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		pausedLock.unlock();
		
		byte[] bgrPixels = new byte[width * height * 3];
		float[] frame = new float[width * height];
		double[] timestamp = new double[1];
		boolean res = getFrame(timestamp, frame);
		
		rgbAvailableLock.lock();
		while (!rgbAvailable)
			rgbAvailableCondition.await();
		System.arraycopy(bufferedImage, 0, bgrPixels, 0, bgrPixels.length);
		rgbAvailable = false;
		rgbAvailableLock.unlock();
		
		if (!res)
			return null;
		
		return new DataBGRD(timestamp[0], bgrPixels, frame, width, height, maxDepth);
	}
	
	@Override
	public void run()
	{
		if (!initialized)
			return;
		
		try
		{
			while (!terminate)
			{
				DataBGRD imagesCollected = null;
				
				imagesCollected = grab();
				
				if (imagesCollected != null)
					doubleBufferBGRD.fillBuffer(imagesCollected);
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
	
	private native boolean initNative();
	
	private native void getResolutionNative(int[] resolution);
	
	private native float getMaxDepthNative();
	
	private native void getIntrinsicParamsNative(float[] focal, float[] center);
	
	private native boolean getFrame(double[] timestamp, float[] depthFrame);
}
