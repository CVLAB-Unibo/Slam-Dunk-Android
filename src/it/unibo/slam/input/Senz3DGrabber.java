package it.unibo.slam.input;

import it.unibo.slam.buffers.interfaces.WriteOnlyDoubleBuffer;
import it.unibo.slam.datatypes.DataBGRD;
import it.unibo.slam.datatypes.params.IntrinsicParams;
import it.unibo.slam.input.interfaces.SensorGrabberBGRD;

/**
 * Class representing the grabber of Senz3D webcam.
 */
public class Senz3DGrabber implements SensorGrabberBGRD
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
	 * Runnable used to suspend waiting for data from the Senz3D sensor.
	 */
	private Senz3DRunnable internalLoopRunnable;
	
	/**
	 * Base constructor.
	 * @param doubleBufferBGRD The buffer in which the data will be written.
	 */
	public Senz3DGrabber(WriteOnlyDoubleBuffer<DataBGRD> doubleBufferBGRD)
	{
		this.doubleBufferBGRD = doubleBufferBGRD;
		maxDepth = MAX_DEPTH_DEFAULT;;
		initialized = false;
		paused = true;
		terminate = false;
	}
	
	@Override
	public void init() throws Exception
	{
		if (initialized)
			return;
		
		boolean res = initNative();
		
		if (!res)
			throw new RuntimeException("Initialization error!");
		
		//TODO test native methods
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
		
		internalLoopRunnable = new Senz3DRunnable();
		Thread internalLoopThread = new Thread(internalLoopRunnable);
		internalLoopThread.start();
		
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
		internalLoopRunnable.terminate();
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
		
		byte[] bgrPixels = new byte[width * height * 3];
		float[] frame = new float[width * height];
		double[] timestamp = new double[1];
		boolean res = getFrame(timestamp, frame, bgrPixels);
		
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
	
	private class Senz3DRunnable implements Runnable
	{
		@Override
		public void run()
		{
			Senz3DGrabber.this.runNativeLoop();
		}
		
		public void terminate()
		{
			Senz3DGrabber.this.exitNativeLoop();
		}
	}
	
	private native boolean initNative();
	
	private native void getResolutionNative(int[] resolution);
	
	private native float getMaxDepthNative();
	
	private native void getIntrinsicParamsNative(float[] focal, float[] center);
	
	private native void runNativeLoop();
	
	private native void exitNativeLoop();
	
	private native boolean getFrame(double[] timestamp, float[] depthFrame, byte[] colorFrame);
}
