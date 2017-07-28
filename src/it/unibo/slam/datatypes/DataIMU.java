package it.unibo.slam.datatypes;

import it.unibo.slam.datatypes.eigen.typefloat.EigenMatrix3F;
import it.unibo.slam.datatypes.eigen.typefloat.EigenVector3F;

public class DataIMU
{
	public static int ACCELERATION = 1;
	public static int ANGULAR_VELOCITY = 2;
	public static int POSITION = 4;
	public static int ORIENTATION = 8;
	
	private int dataType;
	
	private EigenVector3F acceleration;
	
	private EigenVector3F angularVelocity;
	
	private EigenVector3F position;
	
	private EigenMatrix3F orientation;
	
	public DataIMU(	int dataType, EigenVector3F acceleration, EigenVector3F angularVelocity,
					EigenVector3F position, EigenMatrix3F orientation)
	{
		this.dataType = dataType;
		this.acceleration = acceleration;
		this.angularVelocity = angularVelocity;
		this.position = position;
		this.orientation = orientation;
	}
	
	public DataIMU()
	{
		this(0, new EigenVector3F(), new EigenVector3F(), new EigenVector3F(), new EigenMatrix3F());
	}
	
	public int getDataType()
	{
		return dataType;
	}
	
	public EigenVector3F getAcceleration()
	{
		return acceleration;
	}

	public EigenVector3F getAngularVelocity()
	{
		return angularVelocity;
	}

	public EigenVector3F getPosition()
	{
		return position;
	}

	public EigenMatrix3F getOrientation()
	{
		return orientation;
	}
	
	public void setAccelerationData(EigenVector3F acceleration)
	{
		dataType |= ACCELERATION;
		this.acceleration = acceleration;
	}
	
	public void setAngularVelocityData(EigenVector3F angularVelocity)
	{
		dataType |= ANGULAR_VELOCITY;
		this.angularVelocity = angularVelocity;
	}
	
	public void setPositionData(EigenVector3F position)
	{
		dataType |= POSITION;
		this.position = position;
	}
	
	public void setOrientationData(EigenMatrix3F orientation)
	{
		dataType |= ORIENTATION;
		this.orientation = orientation;
	}
	
	public static DataIMU createAccelerationData(EigenVector3F acceleration)
	{
		return new DataIMU(	ACCELERATION, acceleration, new EigenVector3F(), 
							new EigenVector3F(), new EigenMatrix3F());
	}
	
	public static DataIMU createAngularVelocityData(EigenVector3F angularVelocity)
	{
		return new DataIMU(	ANGULAR_VELOCITY, new EigenVector3F(), angularVelocity, 
							new EigenVector3F(), new EigenMatrix3F());
	}
	
	public static DataIMU createPositionData(EigenVector3F position)
	{
		return new DataIMU(	POSITION, new EigenVector3F(), new EigenVector3F(), 
							position, new EigenMatrix3F());
	}
	
	public static DataIMU createOrientationData(EigenMatrix3F orientation)
	{
		return new DataIMU(	ORIENTATION, new EigenVector3F(), new EigenVector3F(), 
							new EigenVector3F(), orientation);
	}
}
