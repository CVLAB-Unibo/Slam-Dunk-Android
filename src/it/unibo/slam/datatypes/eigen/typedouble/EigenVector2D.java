package it.unibo.slam.datatypes.eigen.typedouble;

import it.unibo.slam.datatypes.eigen.typefloat.EigenVector2F;

public class EigenVector2D extends EigenVectorD
{
	public static final int X = 0;
	
	public static final int Y = 1;
	
	public EigenVector2D()
	{
		super(2);
	}
	
	public EigenVector2D(double[] doubleValue)
	{
		super(doubleValue, 2);
	}
	
	public EigenVector2D(double x, double y)
	{
		super(new double[] { x, y }, 2);
	}
	
	public double getX()
	{
		return doubleValue[X];
	}
	
	public void setX(double value)
	{
		doubleValue[X] = value;
	}
	
	public double getY()
	{
		return doubleValue[Y];
	}
	
	public void setY(double value)
	{
		doubleValue[Y] = value;
	}
	
	public EigenVector2F toFloat()
	{
		return new EigenVector2F(getFloatValues());
	}
	
	@Override
	public EigenVector2D normalize()
	{
		super.normalize();
		return this;
	}
	
	@Override
	public EigenVector2D cwiseAbs()
	{
		super.cwiseAbs();
		return this;
	}

	@Override
	public EigenVector2D add(EigenDoubleDataType v)
	{
		super.add(v);
		return this;
	}
	
	@Override
	public EigenVector2D addExternal(EigenVectorD v)
	{
		EigenVector2D result = new EigenVector2D(doubleValue.clone());
		return result.add(v);
	}
	
	@Override
	public EigenVector2D sub(EigenDoubleDataType v)
	{
		super.sub(v);
		return this;
	}
	
	@Override
	public EigenVector2D subExternal(EigenVectorD v)
	{
		EigenVector2D result = new EigenVector2D(doubleValue.clone());
		return result.sub(v);
	}
	
	@Override
	public EigenVector2D multiplyScalar(double scalar)
	{
		super.multiplyScalar(scalar);
		return this;
	}
	
	public EigenVector2D normalizeExternal()
	{
		double divLength = 1 / length();
		double x = doubleValue[X] * divLength;
		double y = doubleValue[Y] * divLength;
		return new EigenVector2D(x, y);
	}
}
