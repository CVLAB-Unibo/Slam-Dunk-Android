package it.unibo.slam.input;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import it.unibo.slam.datatypes.DataIMU;
import it.unibo.slam.datatypes.SensorData;
import it.unibo.slam.datatypes.eigen.typefloat.EigenVector3F;
import it.unibo.slam.input.abstracts.AbstractGyroscopeListener;
import it.unibo.slam.kalman.KalmanGlobal;
import it.unibo.slam.rate.interfaces.RateCalculator;
import it.unibo.slam.utils.TimeUtils;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

/**
 * Listener for the Gyroscope sensor, it can execute the Kalman filter when the angular velocity measurements are available.
 */
public class GyroscopeListener extends AbstractGyroscopeListener implements SensorEventListener
{
	/**
	 * Enumerative representing the gyroscope listener state.
	 */
	private enum GyroscopeState
	{
		/**
		 * The listener is inactive.
		 */
		NONE,
		
		/**
		 * Angular velocity data buffering enabled.
		 */
		BUFFER_DATA,
		
		/**
		 * Kalman execution enabled.
		 */
		EXECUTE_KALMAN;
	};
	
	/**
	 * The gyroscope listener state.
	 */
	private GyroscopeState state;
	
	/**
	 * Buffer that will store the angular velocity data while the tracking module is running.
	 */
	private ConcurrentLinkedQueue<SensorData> angularVelocityBuffer;
	
	/**
	 * Lock for the state enumerative.
	 */
	private Lock stateLock;
	
	/**
	 * Constructor with default rate calculator taken from the superclass.
	 * @param orientation Device natural orientation.
	 */
	public GyroscopeListener()
	{
		super();
		
		init();
	}
	
	/**
	 * Constructor that specifies the rate calculator.
	 * @param orientation Device natural orientation.
	 * @param rateCalculator The rate calculator.
	 */
	public GyroscopeListener(RateCalculator rateCalculator)
	{
		super(rateCalculator);
		
		init();
	}
	
	@Override
	public Queue<SensorData> getAngularVelocityBuffer()
	{
		return angularVelocityBuffer;
	}
	
	/**
	 * Variables initialization.
	 */
	private void init()
	{
		state = GyroscopeState.NONE;
		stateLock = new ReentrantLock();
		angularVelocityBuffer = new ConcurrentLinkedQueue<SensorData>();
	}
	
	@Override
	public void requestBuffering()
	{
		stateLock.lock();
		state = GyroscopeState.BUFFER_DATA;
		stateLock.unlock();
	}
	
	@Override
	public void startExecutingKalman()
	{
		stateLock.lock();
		state = GyroscopeState.EXECUTE_KALMAN;
		angularVelocityBuffer.clear();
		stateLock.unlock();
	}
	
	@Override
	public void disable()
	{
		stateLock.lock();
		state = GyroscopeState.NONE;
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
				
			case BUFFER_DATA:
				
				//TODO inversion of axis y and z
				angularVelocityBuffer.add(data);
				break;
				
			case EXECUTE_KALMAN:
				
				//TODO inversion of axis y and z
				DataIMU dataImu = DataIMU.createAngularVelocityData(new EigenVector3F(data.values[0], data.values[1], data.values[2]));
				KalmanGlobal.handleData(dataImu);
				break;
				
			default:
				break;
		}
		
		stateLock.unlock();
	}
}
