package it.unibo.slam.datatypes.eigen.typefloat;

import it.unibo.slam.datatypes.eigen.typedouble.EigenVector2D;


public class EigenVector2F extends EigenVectorF
{
	public static final int X = 0;
	
	public static final int Y = 1;
	
	public EigenVector2F()
	{
		super(2);
	}
	
	public EigenVector2F(float[] floatValue)
	{
		super(floatValue, 2);
	}
	
	public EigenVector2F(float x, float y)
	{
		super(new float[] { x, y }, 2);
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
	
	@Override
	public EigenVector2D toDouble()
	{
		return new EigenVector2D(getDoubleValues());
	}
	
	@Override
	public EigenVector2F normalize()
	{
		super.normalize();
		return this;
	}
	
	@Override
	public EigenVector2F cwiseAbs()
	{
		super.cwiseAbs();
		return this;
	}
	
	@Override
	public EigenVector2F add(EigenFloatDataType v)
	{
		super.add(v);
		return this;
	}
	
	@Override
	public EigenVector2F addExternal(EigenVectorF v)
	{
		EigenVector2F result = new EigenVector2F(floatValue.clone());
		return result.add(v);
	}
	
	@Override
	public EigenVector2F sub(EigenFloatDataType v)
	{
		super.sub(v);
		return this;
	}
	
	@Override
	public EigenVector2F subExternal(EigenVectorF v)
	{
		EigenVector2F result = new EigenVector2F(floatValue.clone());
		return result.sub(v);
	}
	
	@Override
	public EigenVector2F multiplyScalar(float scalar)
	{
		super.multiplyScalar(scalar);
		return this;
	}
	
	public EigenVector2F normalized()
	{
		float divLength = 1 / length();
		float x = floatValue[X] * divLength;
		float y = floatValue[Y] * divLength;
		return new EigenVector2F(x, y);
	}
}
