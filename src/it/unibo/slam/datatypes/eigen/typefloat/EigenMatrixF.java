package it.unibo.slam.datatypes.eigen.typefloat;

import it.unibo.slam.datatypes.eigen.typedouble.EigenMatrixD;
import it.unibo.slam.datatypes.eigen.typedouble.EigenVectorD;

/**
 * Class representing a generic float matrix (column major order).
 */
public class EigenMatrixF extends EigenFloatDataType
{
	public EigenMatrixF(int rows, int cols)
	{
		super(rows, cols);
	}
	
	public EigenMatrixF(float[] floatValue, int rows, int cols)
	{
		super(floatValue, rows, cols);
	}
	
	public EigenVectorF row(int index)
	{
		return new EigenVectorF(getRowData(index), cols);
	}
	
	protected float[] getRowData(int index)
	{
		float result[] = new float[cols];
		for (int i = index, j = 0; j < cols; i += rows, j++)
			result[j] = floatValue[i];
		return result;
	}
	
	public EigenVectorF col(int index)
	{
		return new EigenVectorF(getColData(index), rows);
	}
	
	protected float[] getColData(int index)
	{
		float result[] = new float[rows];
		int start = rows * index;
		System.arraycopy(floatValue, start, result, 0, rows);
		return result;
	}
	
	public float trace()
	{
		int size = (rows < cols) ? rows : cols;
		
		float tempValue = 0;
		for (int i = 0, k = 0; i < size; i++, k += size + 1)
			tempValue += floatValue[k];
		
		return tempValue;
	}

	@Override
	public EigenMatrixF add(EigenFloatDataType v)
	{
		super.add(v);
		return this;
	}
	
	@Override
	public EigenMatrixF sub(EigenFloatDataType v)
	{
		super.sub(v);
		return this;
	}
	
	@Override
	public EigenMatrixF multiplyScalar(float scalar)
	{
		super.multiplyScalar(scalar);
		return this;
	}
	
	public void multiplyWith(EigenVectorF v, EigenVectorF res)
	{
		res.setValue(matrixMulVectorF(floatValue, v.floatValue, rows, cols));
	}
	
	public void multiplyWith(EigenMatrixF m, EigenMatrixF res)
	{
		res.setValue(matrixMulMatrixF(floatValue, m.floatValue, rows, cols, m.cols));
	}
	
	public void multiplyWith(EigenVectorD v, EigenVectorF res)
	{
		res.setValue(matrixMulVectorFD(floatValue, v.getValue(), rows, cols));
	}
	
	public void multiplyWith(EigenMatrixD m, EigenMatrixF res)
	{
		res.setValue(matrixMulMatrixFD(floatValue, m.getValue(), rows, cols, m.getCols()));
	}
	
	public EigenMatrixD toDouble()
	{
		return new EigenMatrixD(getDoubleValues(), rows, cols);
	}
	
	private native float[] matrixMulVectorF(float[] m, float[] v, int numRowsMatrix, int numColsMatrix);
	private native float[] matrixMulMatrixF(float[] m1, float[] m2, int numRowsMatrix1, int numColsMatrix1, int numColsMatrix2);
	private native float[] matrixMulVectorFD(float[] m, double[] v, int numRowsMatrix, int numColsMatrix);
	private native float[] matrixMulMatrixFD(float[] m1, double[] m2, int numRowsMatrix1, int numColsMatrix1, int numColsMatrix2);
}
