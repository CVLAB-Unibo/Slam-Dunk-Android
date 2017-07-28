package it.unibo.slam.datatypes.eigen.typefloat;

import it.unibo.slam.datatypes.eigen.typedouble.EigenMatrix4D;

public class EigenMatrix4F extends EigenMatrixF
{
	public EigenMatrix4F()
	{
		super(4, 4);
	}
	
	public EigenMatrix4F(float[] floatValue)
	{
		super(floatValue, 4, 4);
	}

	@Override
	public EigenVector4F row(int index)
	{
		return new EigenVector4F(getRowData(index));
	}
	
	@Override
	public EigenVector4F col(int index)
	{
		return new EigenVector4F(getColData(index));
	}
	
	@Override
	public EigenMatrix4D toDouble()
	{
		return new EigenMatrix4D(getDoubleValues());
	}
	
	@Override
	public EigenMatrix4F add(EigenFloatDataType v)
	{
		super.add(v);
		return this;
	}
	
	@Override
	public EigenMatrix4F sub(EigenFloatDataType v)
	{
		super.sub(v);
		return this;
	}
	
	@Override
	public EigenMatrix4F multiplyScalar(float scalar)
	{
		super.multiplyScalar(scalar);
		return this;
	}
	
	public void multiplyWithVector3(EigenVector3F v, EigenVectorF res)
	{
		super.multiplyWith(new EigenVector4F(v.getX(), v.getY(), v.getZ(), 1), res);
	}
	
	public static EigenMatrix4F getIdentity()
	{
		float[] identity = new float[]
		{
			1, 0, 0, 0,
			0, 1, 0, 0,
			0, 0, 1, 0,
			0, 0, 0, 1
		};
		
		return new EigenMatrix4F(identity);
	}
}
