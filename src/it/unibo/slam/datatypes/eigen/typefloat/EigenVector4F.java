package it.unibo.slam.datatypes.eigen.typefloat;

import it.unibo.slam.datatypes.eigen.typedouble.EigenVector4D;

public class EigenVector4F extends EigenVectorF
{
	public static final int X = 0;
	
	public static final int Y = 1;
	
	public static final int Z = 2;
	
	public static final int W = 3;
	
	public EigenVector4F()
	{
		super(4);
	}
	
	public EigenVector4F(float[] floatValue)
	{
		super(floatValue, 4);
	}
	
	public EigenVector4F(float x, float y, float z, float w)
	{
		super(new float[] { x, y, z, w }, 4);
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
	
	public float getW()
	{
		return floatValue[W];
	}
	
	public void setW(float value)
	{
		floatValue[W] = value;
	}
	
	@Override
	public EigenVector4D toDouble()
	{
		return new EigenVector4D(getDoubleValues());
	}
	
	@Override
	public EigenVector4F normalize()
	{
		super.normalize();
		return this;
	}
	
	@Override
	public EigenVector4F cwiseAbs()
	{
		super.cwiseAbs();
		return this;
	}
	
	@Override
	public EigenVector4F add(EigenFloatDataType v)
	{
		super.add(v);
		return this;
	}
	
	@Override
	public EigenVector4F addExternal(EigenVectorF v)
	{
		EigenVector4F result = new EigenVector4F(floatValue.clone());
		return result.add(v);
	}
	
	@Override
	public EigenVector4F sub(EigenFloatDataType v)
	{
		super.sub(v);
		return this;
	}
	
	@Override
	public EigenVector4F subExternal(EigenVectorF v)
	{
		EigenVector4F result = new EigenVector4F(floatValue.clone());
		return result.sub(v);
	}
	
	@Override
	public EigenVector4F multiplyScalar(float scalar)
	{
		super.multiplyScalar(scalar);
		return this;
	}
	
	public EigenVector4F normalized()
	{
		float divLength = 1 / length();
		float x = floatValue[X] * divLength;
		float y = floatValue[Y] * divLength;
		float z = floatValue[Z] * divLength;
		float w = floatValue[W] * divLength;
		return new EigenVector4F(x, y, z, w);
	}
}
