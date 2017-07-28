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
import it.unibo.slam.input.abstracts.AbstractGyroscopeListener;

public class GyroscopeListenerDataset extends AbstractGyroscopeListener
{
	/**
	 * Timestamps for all the depth images.
	 */
	private List<Double> timestamps;
	
	/**
	 * Reader of the gyroscope data.
	 */
	private BufferedReader gyroscopeDataReader;
	
	/**
	 * Values of the sensor between two images.
	 */
	private Queue<SensorData> gyroscopeValues;
	
	/**
	 * Index of the next timestamp.
	 */
	private int nextTimestampId;
	
	/**
	 * Default constructor.
	 * @param imagesDescriptorStream The descriptor for the depth and RGB images, used in order to retried their timestamps.
	 * @param gyroscopePath The path to the gyroscope file.
	 */
	public GyroscopeListenerDataset(InputStream imagesDescriptorStream, String gyroscopePath) throws Exception
	{
		timestamps = new ArrayList<Double>();
		gyroscopeValues = new ConcurrentLinkedQueue<SensorData>();
		
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
		
		gyroscopeDataReader = new BufferedReader(new InputStreamReader(new FileInputStream(gyroscopePath)));
		
		// Reading sensor rate
		line = gyroscopeDataReader.readLine();
		splittedLine = line.split(" ");
		currentRate = Float.valueOf(splittedLine[1]);

		// Skipping gyroscope values previous to the first image
		nextTimestampId = 0;
		fillValues();
		gyroscopeValues.clear();
		
		// Saving angular velocities between first and second image
		fillValues();
	}
	
	/**
	 * Saves the acceleration values between two image timestamps.
	 */
	private void fillValues() throws Exception
	{
		if (nextTimestampId >= timestamps.size())
			return;
		
		gyroscopeDataReader.mark(1000);
		String line = gyroscopeDataReader.readLine();
		//if (line == null)
		//	throw exception!
		String[] splittedLine = line.split(" ");
		double currentTimestampAcceleration = Double.valueOf(splittedLine[1]), timestampImage = timestamps.get(nextTimestampId);
		
		while (currentTimestampAcceleration < timestampImage)
		{
			gyroscopeValues.add(new SensorData(	new float[] { Float.valueOf(splittedLine[2]), Float.valueOf(splittedLine[3]), 
													Float.valueOf(splittedLine[4]) }, currentTimestampAcceleration));
			
			gyroscopeDataReader.mark(1000);
			line = gyroscopeDataReader.readLine();
			
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
		
		gyroscopeDataReader.reset();
		nextTimestampId++;
	}

	@Override
	public Queue<SensorData> getAngularVelocityBuffer()
	{
		return gyroscopeValues;
	}

	@Override
	public void requestBuffering()
	{
		// Do nothing
	}

	@Override
	public void startExecutingKalman()
	{
		// Execute all the gyroscope data between two images timestamps
		/*for (SensorData data : gyroscopeValues)
			handleDataImpl(data);
		
		gyroscopeValues.clear();
		
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
		//DataIMU dataIMU = DataIMU.createAngularVelocityData(new EigenVector3F(data.values[0], data.values[1], data.values[2]));
		//KalmanGlobal.handleData(dataIMU);
	}
}
