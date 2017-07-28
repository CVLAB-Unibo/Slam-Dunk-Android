package it.unibo.slam.datatypes.eigen.typedouble;

import it.unibo.slam.datatypes.eigen.typefloat.EigenMatrix4F;

public class EigenMatrix4D extends EigenMatrixD
{
	public EigenMatrix4D()
	{
		super(4, 4);
	}
	
	public EigenMatrix4D(double[] doubleValue)
	{
		super(doubleValue, 4, 4);
	}

	@Override
	public EigenVector4D row(int index)
	{
		return new EigenVector4D(getRowData(index));
	}
	
	@Override
	public EigenVector4D col(int index)
	{
		return new EigenVector4D(getColData(index));
	}
	
	@Override
	public EigenMatrix4F toFloat()
	{
		return new EigenMatrix4F(getFloatValues());
	}
	
	@Override
	public EigenMatrix4D add(EigenDoubleDataType v)
	{
		super.add(v);
		return this;
	}
	
	@Override
	public EigenMatrix4D sub(EigenDoubleDataType v)
	{
		super.sub(v);
		return this;
	}
	
	@Override
	public EigenMatrix4D multiplyScalar(double scalar)
	{
		super.multiplyScalar(scalar);
		return this;
	}
	
	public static EigenMatrix4D getIdentity()
	{
		double[] identity = new double[]
		{
			1, 0, 0, 0,
			0, 1, 0, 0,
			0, 0, 1, 0,
			0, 0, 0, 1
		};
		
		return new EigenMatrix4D(identity);
	}
}
