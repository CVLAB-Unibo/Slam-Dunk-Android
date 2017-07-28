package it.unibo.slam.input.abstracts;

import java.util.Queue;

import it.unibo.slam.datatypes.SensorData;
import it.unibo.slam.rate.interfaces.RateCalculator;

public abstract class AbstractGyroscopeListener extends AbstractSensorGrabberInertial
{
	/**
	 * Abstract class base constructor with rate calculator.
	 */
	public AbstractGyroscopeListener(RateCalculator rateCalculator)
	{
		super(rateCalculator);
	}
	
	/**
	 * Abstract class constructor.
	 */
	public AbstractGyroscopeListener()
	{
		super();
	}
	
	/**
	 * Gets the buffer containing the angular velocities that are not been filtered yet.
	 * @return The angular velocity buffer.
	 */
	public abstract Queue<SensorData> getAngularVelocityBuffer();
	
	/**
	 * Request data buffering.
	 */
	public abstract void requestBuffering();
	
	/**
	 * Enables Kalman execution.
	 */
	public abstract void startExecutingKalman();
	
	/**
	 * Momentarily disables the listener.
	 */
	public abstract void disable();
}