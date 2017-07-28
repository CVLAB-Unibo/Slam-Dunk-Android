package it.unibo.slam.input.abstracts;

import it.unibo.slam.datatypes.SensorData;
import it.unibo.slam.input.interfaces.SensorGrabberInertial;
import it.unibo.slam.rate.RateCalculatorSMA;
import it.unibo.slam.rate.interfaces.RateCalculator;

public abstract class AbstractSensorGrabberInertial implements SensorGrabberInertial
{
	/**
	 * Starting milliseconds for the calculation of the next sensor rate.
	 */
	private long startingMillis;
	
	/**
	 * Sensor rate calculator.
	 */
	private RateCalculator rateCalculator;
	
	/**
	 * Current sensor rate.
	 */
	protected float currentRate;
	
	/**
	 * Abstract class base constructor.
	 */
	public AbstractSensorGrabberInertial(RateCalculator rateCalculator)
	{
		this.rateCalculator = rateCalculator;
		startingMillis = -1L;
		currentRate = 0;
	}
	
	/**
	 * Abstract class constructor with default rate calculator (SMA).
	 */
	public AbstractSensorGrabberInertial()
	{
		this(new RateCalculatorSMA());
	}

	/**
	 * Gets the sensor rate (in Hz).
	 * @return The sensor rate.
	 */
	public float getSensorRate()
	{
		return Math.round(currentRate);
	}
	
	/**
	 * Gets the sensor frequency period (in Hz^-1 = s).
	 * @return The sensor frequency period, if the frequency is 0 it will return a value of -1.
	 */
	public float getSensorPeriod()
	{
		if (Math.round(currentRate) == 0.0F)
			return -1.0F;
		else
			return 1.0F / Math.round(currentRate);
	}
	
	@Override
	public final void handleData(SensorData data)
	{
		calculateRate();
		
		handleDataImpl(data);
	}
	
	/**
	 * Calculates the sensor rate.
	 */
	private void calculateRate()
	{
		if (startingMillis == -1L)
			startingMillis = System.currentTimeMillis();
		else
		{
			currentRate = rateCalculator.getRate((int)(System.currentTimeMillis() - startingMillis));
			startingMillis = System.currentTimeMillis();
		}
	}
	
	/**
	 * Method to implement acting as the handleData() method.
	 * @param data The sensor data.
	 */
	public abstract void handleDataImpl(SensorData data);
}