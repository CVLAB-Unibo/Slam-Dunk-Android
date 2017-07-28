package it.unibo.slam.kalman;

import it.unibo.slam.datatypes.DataIMU;
import it.unibo.slam.datatypes.eigen.typefloat.EigenIsometry3F;
import it.unibo.slam.datatypes.eigen.typefloat.EigenMatrix4F;
import it.unibo.slam.datatypes.eigen.typefloat.EigenVector3F;

/**
 * Class that will be used to initialize and use the desired implementation of the Kalman filter, calling the subsequent C++ routines.
 */
public class KalmanGlobal
{
	public static boolean initializeKalman(int kalmanType, float accelDeltaT, float gyroDeltaT, EigenVector3F gravity)
	{
		return initializeKalmanNative(kalmanType, accelDeltaT, gyroDeltaT, gravity.getValue());
	}
	
	public static void handleData(DataIMU data)
	{
		handleDataNative(	data.getDataType(), data.getAcceleration().getValue(), data.getAngularVelocity().getValue(),
							data.getPosition().getValue(), data.getOrientation().getValue());
	}
	
	public static EigenIsometry3F getFilteredPose()
	{
		EigenIsometry3F result = new EigenIsometry3F(new EigenMatrix4F());
		getFilteredPoseNative(result.getMatrix().getValue());
		return result;
	}
	
	private static native boolean initializeKalmanNative(	int kalmanType, float accelDeltaT, float gyroDeltaT,
															float[] gravity);
	
	private static native void handleDataNative(int dataType, float[] acceleration, float[] angularVelocity, 
												float[] position, float[] orientation);
	
	private static native void getFilteredPoseNative(float[] pose);
	
	public static native void setVisualObservationNoise(float noiseValue);
}
