package it.unibo.slam.datatypes;

public class SensorData
{
	public final float[] values;
	
	public final double timestamp;
	
	public SensorData(float[] values, double timestamp)
	{
		this.values = new float[values.length];
		System.arraycopy(values, 0, this.values, 0, values.length);
		
		this.timestamp = timestamp;
	}
}
