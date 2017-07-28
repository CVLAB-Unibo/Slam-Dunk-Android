package it.unibo.slam.input;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import it.unibo.slam.datatypes.DataIMU;
import it.unibo.slam.datatypes.SensorData;
import it.unibo.slam.datatypes.eigen.typefloat.EigenVector3F;
import it.unibo.slam.input.abstracts.AbstractAccelerometerListener;
import it.unibo.slam.kalman.KalmanGlobal;
import it.unibo.slam.rate.interfaces.RateCalculator;
import it.unibo.slam.utils.TimeUtils;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

/**
 * Listener for the Accelerometer sensor, it can calculate the gravity vector (if needed) or execute the Kalman filter when
 * the acceleration measurements are available.
 */
public class AccelerometerListener extends AbstractAccelerometerListener implements SensorEventListener
{
	/**
	 * Threshold of the low-pass filter used in gravity calculation.
	 */
	private static float GRAVITY_FILTER_THRESHOLD = 0.8F;
	
	/**
	 * Enumerative representing the accelerometer listener state.
	 */
	private enum AccelerometerState
	{
		/**
		 * The listener is inactive.
		 */
		NONE,
		
		/**
		 * Gravity calculation enabled.
		 */
		CALCULATE_GRAVITY,
		
		/**
		 * Acceleration data buffering enabled.
		 */
		BUFFER_DATA,
		
		/**
		 * Kalman execution enabled.
		 */
		EXECUTE_KALMAN;
	};
	
	/**
	 * The accelerometer listener state.
	 */
	private AccelerometerState state;
	
	/**
	 * The estimated gravity vector.
	 */
	private float[] gravityEstimate;
	
	/**
	 * Buffer that will store the acceleration data while the tracking module is running.
	 */
	private ConcurrentLinkedQueue<SensorData> accelerationBuffer;
	
	/**
	 * Lock for the state enumerative.
	 */
	private Lock stateLock;
	
	/**
	 * Constructor with default rate calculator taken from the superclass.
	 * @param orientation Device natural orientation.
	 */
	public AccelerometerListener()
	{
		super();
		
		init();
	}
	
	/**
	 * Constructor that specifies the rate calculator.
	 * @param orientation Device natural orientation.
	 * @param rateCalculator The rate calculator.
	 */
	public AccelerometerListener(RateCalculator rateCalculator)
	{
		super(rateCalculator);
		
		init();
	}

	@Override
	public EigenVector3F getGravity()
	{
		return new EigenVector3F(gravityEstimate);
	}
	
	@Override
	public Queue<SensorData> getAccelerationBuffer()
	{
		return accelerationBuffer;
	}
	
	/**
	 * Variables initialization.
	 */
	private void init()
	{
		gravityEstimate = new float[3];
		state = AccelerometerState.NONE;
		stateLock = new ReentrantLock();
		accelerationBuffer = new ConcurrentLinkedQueue<SensorData>();
	}
	
	@Override
	public void startCalculatingGravity()
	{
		stateLock.lock();
		state = AccelerometerState.CALCULATE_GRAVITY;
		stateLock.unlock();
	}
	
	@Override
	public void requestBuffering()
	{
		stateLock.lock();
		state = AccelerometerState.BUFFER_DATA;
		stateLock.unlock();
	}
	
	@Override
	public void startExecutingKalman()
	{
		stateLock.lock();
		state = AccelerometerState.EXECUTE_KALMAN;
		accelerationBuffer.clear();
		stateLock.unlock();
	}
	
	@Override
	public void disable()
	{
		stateLock.lock();
		state = AccelerometerState.NONE;
		stateLock.unlock();
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy)
	{
		// TODO Accuracy changed
		
	}

	@Override
	public void onSensorChanged(SensorEvent event)
	{
		handleData(new SensorData(event.values, TimeUtils.getTimestampSeconds()));
	}

	@Override
	public void handleDataImpl(SensorData data)
	{
		stateLock.lock();
		
		switch (state)
		{
			case NONE:
				break;
				
			case CALCULATE_GRAVITY:
				
				//TODO inversion of axis y and z
				filterGravity(data.values, GRAVITY_FILTER_THRESHOLD);
				break;
				
			case BUFFER_DATA:
				
				//TODO inversion of axis y and z
				accelerationBuffer.add(data);
				break;
			
			case EXECUTE_KALMAN:
				
				//TODO inversion of axis y and z
				DataIMU dataIMU = DataIMU.createAccelerationData(new EigenVector3F(data.values[0], data.values[1], data.values[2]));
				KalmanGlobal.handleData(dataIMU);
				break;
				
			default:
				break;
		}
		
		stateLock.unlock();
	}
	
	/**
	 * Low pass filtering of the input acceleration in order to calculate the gravity vector.
	 * @param acceleration Input acceleration.
	 * @param filterThreshold Low pass filter threshold.
	 */
	private void filterGravity(float[] acceleration, float filterThreshold)
	{
		gravityEstimate[0] = gravityEstimate[0] * filterThreshold + acceleration[0] * (1.0F - filterThreshold);
		gravityEstimate[1] = gravityEstimate[1] * filterThreshold + acceleration[1] * (1.0F - filterThreshold);
		gravityEstimate[2] = gravityEstimate[2] * filterThreshold + acceleration[2] * (1.0F - filterThreshold);
	}
}
