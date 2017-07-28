package it.unibo.slam.input;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import it.unibo.slam.datatypes.SensorData;
import it.unibo.slam.datatypes.eigen.typefloat.EigenVector3F;
import it.unibo.slam.input.abstracts.AbstractAccelerometerListener;

public class AccelerometerListenerDataset extends AbstractAccelerometerListener
{
	/**
	 * Timestamps for all the depth images.
	 */
	private List<Double> timestamps;
	
	/**
	 * Reader of the accelerometer data.
	 */
	private BufferedReader accelerometerDataReader;
	
	/**
	 * Recorded accelerometer gravity.
	 */
	private EigenVector3F gravity;
	
	/**
	 * Values of the sensor between two images.
	 */
	private Queue<SensorData> accelerationValues;
	
	/**
	 * Index of the next timestamp.
	 */
	private int nextTimestampId;
	
	/**
	 * Default constructor.
	 * @param imagesDescriptorStream The descriptor for the depth and RGB images, used in order to retried their timestamp
	 * @param accelerometerPath The path to the accelerometer file.
	 */
	public AccelerometerListenerDataset(InputStream imagesDescriptorStream, String accelerometerPath) throws Exception
	{
		timestamps = new ArrayList<Double>();
		accelerationValues = new ConcurrentLinkedQueue<SensorData>();
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(imagesDescriptorStream));
		
		// Store the timestamps in a list for usage during the execution
		String[] splittedLine;
		String line = reader.readLine();
		while (line != null)
		{
			splittedLine = line.split(" ");
			if (splittedLine == null || splittedLine.length != 4)
				throw new RuntimeException("Incorrect file format.");
			
			timestamps.add(Double.valueOf(splittedLine[2]));
			
			line = reader.readLine();
		}
		
		reader.close();
		
		accelerometerDataReader = new BufferedReader(new InputStreamReader(new FileInputStream(accelerometerPath)));
		
		// Reading sensor rate
		line = accelerometerDataReader.readLine();
		splittedLine = line.split(" ");
		currentRate = Float.valueOf(splittedLine[1]);
		
		// Reading gravity
		line = accelerometerDataReader.readLine();
		splittedLine = line.split(" ");
		gravity = new EigenVector3F(Float.valueOf(splittedLine[1]), Float.valueOf(splittedLine[2]), 
									Float.valueOf(splittedLine[3]));
		
		// Skipping acceleration values previous to the first image
		nextTimestampId = 0;
		fillValues();
		accelerationValues.clear();
		
		// Saving acceleration between first and second image
		fillValues();
	}
	
	/**
	 * Saves the acceleration values between two image timestamps.
	 */
	private void fillValues() throws Exception
	{
		if (nextTimestampId >= timestamps.size())
			return;
		
		accelerometerDataReader.mark(1000);
		String line = accelerometerDataReader.readLine();
		//if (line == null)
		//	throw exception!
		String[] splittedLine = line.split(" ");
		double currentTimestampAcceleration = Double.valueOf(splittedLine[1]), timestampImage = timestamps.get(nextTimestampId);
		
		while (currentTimestampAcceleration < timestampImage)
		{
			accelerationValues.add(new SensorData(	new float[] { Float.valueOf(splittedLine[2]), Float.valueOf(splittedLine[3]), 
													Float.valueOf(splittedLine[4]) }, currentTimestampAcceleration));
			
			accelerometerDataReader.mark(1000);
			line = accelerometerDataReader.readLine();
			
			if (line != null)
			{
				splittedLine = line.split(" ");
				currentTimestampAcceleration = Double.valueOf(splittedLine[1]);
			}
			else
				break;
		}
		
		//if (line == null)
		//	throw exception!
		
		accelerometerDataReader.reset();
		nextTimestampId++;
	}
	
	@Override
	public EigenVector3F getGravity()
	{
		return gravity;
	}

	@Override
	public Queue<SensorData> getAccelerationBuffer()
	{
		return accelerationValues;
	}

	@Override
	public void startCalculatingGravity()
	{
		// Do nothing
	}

	@Override
	public void requestBuffering()
	{
		// Do nothing
	}

	@Override
	public void startExecutingKalman()
	{
		// Execute all the acceleration data between two images timestamps
		/*for (SensorData data : accelerationValues)
			handleDataImpl(data);
		
		accelerationValues.clear();
		
		try
		{
			fillValues();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}*/
		
		try
		{
			fillValues();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void disable()
	{
		// Do nothing
	}

	@Override
	public void handleDataImpl(SensorData data)
	{
		//DataIMU dataIMU = DataIMU.createAccelerationData(new EigenVector3F(data.values[0], data.values[1], data.values[2]));
		//KalmanGlobal.handleData(dataIMU);
	}
}
