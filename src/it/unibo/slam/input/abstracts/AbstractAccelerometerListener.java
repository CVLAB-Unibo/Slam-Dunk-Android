package it.unibo.slam.input.abstracts;

import java.util.Queue;

import it.unibo.slam.datatypes.SensorData;
import it.unibo.slam.datatypes.eigen.typefloat.EigenVector3F;
import it.unibo.slam.rate.interfaces.RateCalculator;

public abstract class AbstractAccelerometerListener extends AbstractSensorGrabberInertial
{
	/**
	 * Abstract class base constructor with rate calculator.
	 */
	public AbstractAccelerometerListener(RateCalculator rateCalculator)
	{
		super(rateCalculator);
	}
	
	/**
	 * Abstract class constructor.
	 */
	public AbstractAccelerometerListener()
	{
		super();
	}
	
	/**
	 * Gets the gravity estimate.
	 * @return The gravity estimate.
	 */
	public abstract EigenVector3F getGravity();
	
	/**
	 * Gets the buffer containing the accelerations that are not been filtered yet.
	 * @return The acceleration buffer.
	 */
	public abstract Queue<SensorData> getAccelerationBuffer();
	
	/**
	 * Enables gravity calculation.
	 */
	public abstract void startCalculatingGravity();
	
	/**
	 * Request data buffering.
	 */
	public abstract void requestBuffering();
	
	/**
	 * Enables Kalman filter execution.
	 */
	public abstract void startExecutingKalman();
	
	/**
	 * Momentarily disables the listener.
	 */
	public abstract void disable();
}
