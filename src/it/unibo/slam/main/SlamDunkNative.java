package it.unibo.slam.main;

/**
 * Native SlamDunk, used for speed gain.
 */
public class SlamDunkNative
{
	public static native void init(	float fx, float fy, float cx, float cy, int cols, int rows, 
									int rbaRings, float keyframeOverlapping, boolean tryLoopInference,
									float activeWindowLength, boolean debugAlgorithm);
	
	public static native int execute(double timestamp, long colorImagePtr, long depthImagePtr, double[] estimatedPose);
	
	public static native int executeTracking(double timestamp, long colorImagePtr, long depthImagePtr, double[] estimatedPose);
	
	public static native int executeOptimization(double[] estimatedPose);
	
	public static native int getMovedFramesNum();
	
	public static native void getMovedFrames(double[] timestamps, double[] isometries);
	
	public static native float getResidual();
}