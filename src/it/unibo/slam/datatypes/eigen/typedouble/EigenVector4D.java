package it.unibo.slam.datatypes.eigen.typedouble;

import it.unibo.slam.datatypes.eigen.typefloat.EigenVector4F;

public class EigenVector4D extends EigenVectorD
{
	public static final int X = 0;
	
	public static final int Y = 1;
	
	public static final int Z = 2;
	
	public static final int W = 3;
	
	public EigenVector4D()
	{
		super(4);
	}
	
	public EigenVector4D(double[] doubleValue)
	{
		super(doubleValue, 4);
	}
	
	public EigenVector4D(double x, double y, double z, double w)
	{
		super(new double[] { x, y, z, w }, 4);
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
	
	public double getZ()
	{
		return doubleValue[Z];
	}
	
	public void setZ(double value)
	{
		doubleValue[Z] = value;
	}
	
	public double getW()
	{
		return doubleValue[W];
	}
	
	public void setW(double value)
	{
		doubleValue[W] = value;
	}
	
	@Override
	public EigenVector4F toFloat()
	{
		return new EigenVector4F(getFloatValues());
	}
	
	@Override
	public EigenVector4D normalize()
	{
		super.normalize();
		return this;
	}
	
	@Override
	public EigenVector4D cwiseAbs()
	{
		super.cwiseAbs();
		return this;
	}
	
	@Override
	public EigenVector4D add(EigenDoubleDataType v)
	{
		super.add(v);
		return this;
	}
	
	@Override
	public EigenVector4D addExternal(EigenVectorD v)
	{
		EigenVector4D result = new EigenVector4D(doubleValue.clone());
		return result.add(v);
	}
	
	@Override
	public EigenVector4D sub(EigenDoubleDataType v)
	{
		super.sub(v);
		return this;
	}
	
	@Override
	public EigenVector4D subExternal(EigenVectorD v)
	{
		EigenVector4D result = new EigenVector4D(doubleValue.clone());
		return result.sub(v);
	}
	
	@Override
	public EigenVector4D multiplyScalar(double scalar)
	{
		super.multiplyScalar(scalar);
		return this;
	}
	
	public EigenVector4D normalizeExternal()
	{
		double divLength = 1 / length();
		double x = doubleValue[X] * divLength;
		double y = doubleValue[Y] * divLength;
		double z = doubleValue[Z] * divLength;
		double w = doubleValue[W] * divLength;
		return new EigenVector4D(x, y, z, w);
	}
}
