package it.unibo.slam.datatypes.eigen.typedouble;

import it.unibo.slam.datatypes.eigen.general.ComparisonType;
import it.unibo.slam.datatypes.eigen.typefloat.EigenVectorF;

/**
 * Class representing a generic double row vector.
 */
public class EigenVectorD extends EigenDoubleDataType
{
	public EigenVectorD(int num)
	{
		super(num, 1);
	}
	
	public EigenVectorD(double[] doubleValue, int num)
	{
		super(doubleValue, num, 1);
	}
	
	public double get(int id)
	{
		return doubleValue[id];
	}
	
	public void set(double value, int id)
	{
		doubleValue[id] = value;
	}
	
	public EigenVectorF toFloat()
	{
		return new EigenVectorF(getFloatValues(), rows);
	}
	
	public double dot(EigenVectorD v)
	{
		double result = 0;
		
		for (int i = 0; i < rows; i++)
			result += doubleValue[i] * v.doubleValue[i];
			
		return result;
	}
	
	public double squaredLength()
	{
		return this.dot(this);
	}
	
	public double length()
	{
		return Math.sqrt(squaredLength());
	}
	
	public EigenVectorD normalize()
	{
		double divLength = 1 / length();
		
		for (int i = 0; i < rows; i++)
			doubleValue[i] *= divLength;
		
		return this;
	}
	
	public EigenVectorD cwiseAbs()
	{
		for (int i = 0; i < rows; i++)
			doubleValue[i] = Math.abs(doubleValue[i]);
		
		return this;
	}
	
	@Override
	public EigenVectorD add(EigenDoubleDataType v)
	{
		super.add(v);
		return this;
	}
	
	public EigenVectorD addExternal(EigenVectorD v)
	{
		EigenVectorD result = new EigenVectorD(doubleValue.clone(), rows);
		return result.add(v);
	}
	
	@Override
	public EigenVectorD sub(EigenDoubleDataType v)
	{
		super.sub(v);
		return this;
	}
	
	public EigenVectorD subExternal(EigenVectorD v)
	{
		EigenVectorD result = new EigenVectorD(doubleValue.clone(), rows);
		return result.sub(v);
	}
	
	@Override
	public EigenVectorD multiplyScalar(double scalar)
	{
		super.multiplyScalar(scalar);
		return this;
	}
	
	public EigenMatrixD multiplyWith(EigenMatrixD m)
	{
		//TODO risistemare?
		
		double[] resultArray = new double[rows * m.cols];
		
		for (int i = 0; i < rows; i++)
			for (int j = 0, k = 0; j < m.cols; j++, k += rows)
				resultArray[i + k] = doubleValue[i] * m.doubleValue[j];
		
		return new EigenMatrixD(resultArray, rows, m.cols);
	}
	
	public boolean elementsMajorTo(double value, ComparisonType comparisonType)
	{
		// A single element is enough
		if (comparisonType == ComparisonType.ANY)
		{
			for (int i = 0; i < rows; i++)
				if (doubleValue[i] > value)
					return true;
			
			return false;
		}
		// All the elements
		else if (comparisonType == ComparisonType.ALL)
		{
			for (int i = 0; i < rows; i++)
				if (doubleValue[i] <= value)
					return false;
			
			return true;
		}
		
		return false;
	}
	
	public boolean elementsMajorEqualTo(double value, ComparisonType comparisonType)
	{
		// A single element is enough
		if (comparisonType == ComparisonType.ANY)
		{
			for (int i = 0; i < rows; i++)
				if (doubleValue[i] >= value)
					return true;
			
			return false;
		}
		// All the elements
		else if (comparisonType == ComparisonType.ALL)
		{
			for (int i = 0; i < rows; i++)
				if (doubleValue[i] < value)
					return false;
			
			return true;
		}
		
		return false;
	}
	
	public boolean elementsMinorTo(double value, ComparisonType comparisonType)
	{
		// A single element is enough
		if (comparisonType == ComparisonType.ANY)
		{
			for (int i = 0; i < rows; i++)
				if (doubleValue[i] < value)
					return true;
			
			return false;
		}
		// All the elements
		else if (comparisonType == ComparisonType.ALL)
		{
			for (int i = 0; i < rows; i++)
				if (doubleValue[i] >= value)
					return false;
			
			return true;
		}
		
		return false;
	}
	
	public boolean elementsMinorEqualTo(double value, ComparisonType comparisonType)
	{
		// A single element is enough
		if (comparisonType == ComparisonType.ANY)
		{
			for (int i = 0; i < rows; i++)
				if (doubleValue[i] <= value)
					return true;
			
			return false;
		}
		// All the elements
		else if (comparisonType == ComparisonType.ALL)
		{
			for (int i = 0; i < rows; i++)
				if (doubleValue[i] > value)
					return false;
			
			return true;
		}
		
		return false;
	}
	
	public EigenMatrixD transpose()
	{
		return new EigenMatrixD(getTransposedValues(), cols, rows);
	}
}
