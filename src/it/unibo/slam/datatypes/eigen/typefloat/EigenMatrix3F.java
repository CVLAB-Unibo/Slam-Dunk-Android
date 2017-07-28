package it.unibo.slam.datatypes.eigen.typefloat;

import it.unibo.slam.datatypes.eigen.typedouble.EigenMatrix3D;

public class EigenMatrix3F extends EigenMatrixF
{
	public EigenMatrix3F()
	{
		super(3, 3);
	}
	
	public EigenMatrix3F(float[] floatValue)
	{
		super(floatValue, 3, 3);
	}
	
	@Override
	public EigenVector3F row(int index)
	{
		return new EigenVector3F(getRowData(index));
	}
	
	@Override
	public EigenVector3F col(int index)
	{
		return new EigenVector3F(getColData(index));
	}
	
	@Override
	public EigenMatrix3D toDouble()
	{
		return new EigenMatrix3D(getDoubleValues());
	}
	
	@Override
	public EigenMatrix3F add(EigenFloatDataType v)
	{
		super.add(v);
		return this;
	}
	
	@Override
	public EigenMatrix3F sub(EigenFloatDataType v)
	{
		super.sub(v);
		return this;
	}
	
	@Override
	public EigenMatrix3F multiplyScalar(float scalar)
	{
		super.multiplyScalar(scalar);
		return this;
	}
	
	public static EigenMatrix3F getIdentity()
	{
		float[] identity = new float[]
		{
			1, 0, 0,
			0, 1, 0,
			0, 0, 1,
		};
		
		return new EigenMatrix3F(identity);
	}
}