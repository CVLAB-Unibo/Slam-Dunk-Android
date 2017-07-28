package it.unibo.slam.datatypes.params;

/**
 * Class representing the Kalman parameters (for the settings).
 */
public class KalmanParams
{
	/**
	 * If true, enables the execution of the Kalman filter.
	 */
	private boolean executeKalman;
	
	/**
	 * Integer representing the type of Kalman filter used.
	 */
	private int kalmanType;
	
	/**
	 * Default constructor.
	 * @param executeKalman Boolean that enables or disables Kalman execution.
	 * @param kalmanType The Kalman filter type.
	 */
	public KalmanParams(boolean executeKalman, int kalmanType)
	{
		this.executeKalman = executeKalman;
		this.kalmanType = kalmanType;
	}

	/**
	 * Checks if the Kalman execution is enables or disabled.
	 * @return True if the execution of the Kalman filter is enabled, false otherwise.
	 */
	public boolean getExecuteKalman()
	{
		return executeKalman;
	}

	/**
	 * Gets the Kalman filter type.
	 * @return The Kalman filter type.
	 */
	public int getKalmanType()
	{
		return kalmanType;
	}
}