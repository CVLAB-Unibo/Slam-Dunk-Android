package it.unibo.slam.kalman;

/**
 * This class stores the identifier of the different Kalman types.
 */
public class KalmanType
{
	/**
	 * Basic Kalman filter (Orientation EKF + Position KF).
	 */
	public static final int BASIC_KALMAN = 0;
	
	/**
	 * Indirect Kalman filter.
	 */
	public static final int INDIRECT_KALMAN = 1;
}
