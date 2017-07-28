package it.unibo.slam.datatypes.eigen.typedouble;

import it.unibo.slam.datatypes.eigen.typefloat.EigenVector3F;

public class EigenVector3D extends EigenVectorD
{
	public static final int X = 0;
	
	public static final int Y = 1;
	
	public static final int Z = 2;
	
	public EigenVector3D()
	{
		super(3);
	}
	
	public EigenVector3D(double[] doubleValue)
	{
		super(doubleValue, 3);
	}
	
	public EigenVector3D(double x, double y, double z)
	{
		super(new double[] { x, y, z }, 3);
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
	
	public EigenVector3D cross(EigenVector3D v)
	{
		double[] vValue = v.getValue();
		double x = doubleValue[Y] * vValue[Z] - doubleValue[Z] * vValue[Y];
		double y = doubleValue[Z] * vValue[X] - doubleValue[X] * vValue[Z];
		double z = doubleValue[X] * vValue[Y] - doubleValue[Y] * vValue[X];
		return new EigenVector3D(x, y, z);
	}
	
	@Override
	public EigenVector3F toFloat()
	{
		return new EigenVector3F(getFloatValues());
	}
	
	@Override
	public EigenVector3D normalize()
	{
		super.normalize();
		return this;
	}
	
	@Override
	public EigenVector3D cwiseAbs()
	{
		super.cwiseAbs();
		return this;
	}
	
	@Override
	public EigenVector3D add(EigenDoubleDataType v)
	{
		super.add(v);
		return this;
	}
	
	@Override
	public EigenVector3D addExternal(EigenVectorD v)
	{
		EigenVector3D result = new EigenVector3D(doubleValue.clone());
		return result.add(v);
	}
	
	@Override
	public EigenVector3D sub(EigenDoubleDataType v)
	{
		super.sub(v);
		return this;
	}
	
	@Override
	public EigenVector3D subExternal(EigenVectorD v)
	{
		EigenVector3D result = new EigenVector3D(doubleValue.clone());
		return result.sub(v);
	}
	
	@Override
	public EigenVector3D multiplyScalar(double scalar)
	{
		super.multiplyScalar(scalar);
		return this;
	}
	
	public EigenVector3D normalizeExternal()
	{
		double divLength = 1 / length();
		double x = doubleValue[X] * divLength;
		double y = doubleValue[Y] * divLength;
		double z = doubleValue[Z] * divLength;
		return new EigenVector3D(x, y, z);
	}
}
