package it.unibo.slam.datatypes.eigen.typedouble;

import it.unibo.slam.datatypes.eigen.typefloat.EigenMatrix3F;

public class EigenMatrix3D extends EigenMatrixD
{
	public EigenMatrix3D()
	{
		super(3, 3);
	}
	
	public EigenMatrix3D(double[] doubleValue)
	{
		super(doubleValue, 3, 3);
	}

	@Override
	public EigenVector3D row(int index)
	{
		return new EigenVector3D(getRowData(index));
	}
	
	@Override
	public EigenVector3D col(int index)
	{
		return new EigenVector3D(getColData(index));
	}
	
	@Override
	public EigenMatrix3F toFloat()
	{
		return new EigenMatrix3F(getFloatValues());
	}
	
	@Override
	public EigenMatrix3D add(EigenDoubleDataType v)
	{
		super.add(v);
		return this;
	}
	
	@Override
	public EigenMatrix3D sub(EigenDoubleDataType v)
	{
		super.sub(v);
		return this;
	}
	
	@Override
	public EigenMatrix3D multiplyScalar(double scalar)
	{
		super.multiplyScalar(scalar);
		return this;
	}
	
	public static EigenMatrix3D getIdentity()
	{
		double[] identity = new double[]
		{
			1, 0, 0,
			0, 1, 0,
			0, 0, 1,
		};
		
		return new EigenMatrix3D(identity);
	}
}
