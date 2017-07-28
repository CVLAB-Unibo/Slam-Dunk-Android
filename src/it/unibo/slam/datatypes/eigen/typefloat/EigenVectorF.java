package it.unibo.slam.datatypes.eigen.typefloat;

import it.unibo.slam.datatypes.eigen.general.ComparisonType;
import it.unibo.slam.datatypes.eigen.typedouble.EigenVectorD;

/**
 * Class representing a generic float row vector.
 */
public class EigenVectorF extends EigenFloatDataType
{
	public EigenVectorF(int num)
	{
		super(num, 1);
	}
	
	public EigenVectorF(float[] floatValue, int num)
	{
		super(floatValue, num, 1);
	}
	
	public float get(int id)
	{
		return floatValue[id];
	}
	
	public void set(float value, int id)
	{
		floatValue[id] = value;
	}
	
	public EigenVectorD toDouble()
	{
		return new EigenVectorD(getDoubleValues(), rows);
	}
	
	public float dot(EigenVectorF v)
	{
		float result = 0;
		
		for (int i = 0; i < rows; i++)
			result += floatValue[i] * v.floatValue[i];
			
		return result;
	}
	
	public float squaredLength()
	{
		return this.dot(this);
	}
	
	public float length()
	{
		return (float)Math.sqrt(squaredLength());
	}
	
	public EigenVectorF normalize()
	{
		double divLength = 1 / length();
		
		for (int i = 0; i < rows; i++)
			floatValue[i] *= divLength;
		
		return this;
	}
	
	public EigenVectorF cwiseAbs()
	{
		for (int i = 0; i < rows; i++)
			floatValue[i] = Math.abs(floatValue[i]);
		
		return this;
	}
	
	@Override
	public EigenVectorF add(EigenFloatDataType v)
	{
		super.add(v);
		return this;
	}
	
	public EigenVectorF addExternal(EigenVectorF v)
	{
		EigenVectorF result = new EigenVectorF(floatValue.clone(), rows);
		return result.add(v);
	}
	
	@Override
	public EigenVectorF sub(EigenFloatDataType v)
	{
		super.sub(v);
		return this;
	}
	
	public EigenVectorF subExternal(EigenVectorF v)
	{
		EigenVectorF result = new EigenVectorF(floatValue.clone(), rows);
		return result.sub(v);
	}
	
	@Override
	public EigenVectorF multiplyScalar(float scalar)
	{
		super.multiplyScalar(scalar);
		return this;
	}
	
	public boolean elementsMajorTo(float value, ComparisonType comparisonType)
	{
		// A single element is enough
		if (comparisonType == ComparisonType.ANY)
		{
			for (int i = 0; i < rows; i++)
				if (floatValue[i] > value)
					return true;
			
			return false;
		}
		// All the elements
		else if (comparisonType == ComparisonType.ALL)
		{
			for (int i = 0; i < rows; i++)
				if (floatValue[i] <= value)
					return false;
			
			return true;
		}
		
		return false;
	}
	
	public boolean elementsMajorEqualTo(float value, ComparisonType comparisonType)
	{
		// A single element is enough
		if (comparisonType == ComparisonType.ANY)
		{
			for (int i = 0; i < rows; i++)
				if (floatValue[i] >= value)
					return true;
			
			return false;
		}
		// All the elements
		else if (comparisonType == ComparisonType.ALL)
		{
			for (int i = 0; i < rows; i++)
				if (floatValue[i] < value)
					return false;
			
			return true;
		}
		
		return false;
	}
	
	public boolean elementsMinorTo(float value, ComparisonType comparisonType)
	{
		// A single element is enough
		if (comparisonType == ComparisonType.ANY)
		{
			for (int i = 0; i < rows; i++)
				if (floatValue[i] < value)
					return true;
			
			return false;
		}
		// All the elements
		else if (comparisonType == ComparisonType.ALL)
		{
			for (int i = 0; i < rows; i++)
				if (floatValue[i] >= value)
					return false;
			
			return true;
		}
		
		return false;
	}
	
	public boolean elementsMinorEqualTo(float value, ComparisonType comparisonType)
	{
		// A single element is enough
		if (comparisonType == ComparisonType.ANY)
		{
			for (int i = 0; i < rows; i++)
				if (floatValue[i] <= value)
					return true;
			
			return false;
		}
		// All the elements
		else if (comparisonType == ComparisonType.ALL)
		{
			for (int i = 0; i < rows; i++)
				if (floatValue[i] > value)
					return false;
			
			return true;
		}
		
		return false;
	}
	
	public EigenMatrixF transpose()
	{
		return new EigenMatrixF(getTransposedValues(), cols, rows);
	}
}
