package it.unibo.slam.datatypes.eigen.typefloat;

import it.unibo.slam.datatypes.eigen.typedouble.EigenVector3D;

public class EigenVector3F extends EigenVectorF
{
	public static final int X = 0;
	
	public static final int Y = 1;
	
	public static final int Z = 2;
	
	public EigenVector3F(long nativePointer)
	{
		super(3);
	}
	
	public EigenVector3F()
	{
		super(3);
	}
	
	public EigenVector3F(float[] floatValue)
	{
		super(floatValue, 3);
	}
	
	public EigenVector3F(float x, float y, float z)
	{
		this(new float[] { x, y, z });
	}
	
	public float getX()
	{
		return floatValue[X];
	}
	
	public void setX(float value)
	{
		floatValue[X] = value;
	}
	
	public float getY()
	{
		return floatValue[Y];
	}
	
	public void setY(float value)
	{
		floatValue[Y] = value;
	}
	
	public float getZ()
	{
		return floatValue[Z];
	}
	
	public void setZ(float value)
	{
		floatValue[Z] = value;
	}
	
	@Override
	public EigenVector3D toDouble()
	{
		return new EigenVector3D(getDoubleValues());
	}
	
	public EigenVector3F cross(EigenVector3F v)
	{
		float[] vValue = v.getValue();
		float x = floatValue[Y] * vValue[Z] - floatValue[Z] * vValue[Y];
		float y = floatValue[Z] * vValue[X] - floatValue[X] * vValue[Z];
		float z = floatValue[X] * vValue[Y] - floatValue[Y] * vValue[X];
		return new EigenVector3F(x, y, z);
	}
	
	@Override
	public EigenVector3F normalize()
	{
		super.normalize();
		return this;
	}
	
	@Override
	public EigenVector3F cwiseAbs()
	{
		super.cwiseAbs();
		return this;
	}
	
	@Override
	public EigenVector3F add(EigenFloatDataType v)
	{
		super.add(v);
		return this;
	}
	
	@Override
	public EigenVector3F addExternal(EigenVectorF v)
	{
		EigenVector3F result = new EigenVector3F(floatValue.clone());
		return result.add(v);
	}
	
	@Override
	public EigenVector3F sub(EigenFloatDataType v)
	{
		super.sub(v);
		return this;
	}
	
	@Override
	public EigenVector3F subExternal(EigenVectorF v)
	{
		EigenVector3F result = new EigenVector3F(floatValue.clone());
		return result.sub(v);
	}
	
	@Override
	public EigenVector3F multiplyScalar(float scalar)
	{
		super.multiplyScalar(scalar);
		return this;
	}
	
	public EigenVector3F normalized()
	{
		float divLength = 1 / length();
		float x = floatValue[X] * divLength;
		float y = floatValue[Y] * divLength;
		float z = floatValue[Z] * divLength;
		return new EigenVector3F(x, y, z);
	}
}