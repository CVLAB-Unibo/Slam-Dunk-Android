package it.unibo.slam.datatypes.eigen.typedouble;

import it.unibo.slam.datatypes.eigen.typefloat.EigenMatrixF;
import it.unibo.slam.datatypes.eigen.typefloat.EigenVectorF;

/**
 * Class representing a generic double matrix (column major order).
 */
public class EigenMatrixD extends EigenDoubleDataType
{
	public EigenMatrixD(int rows, int cols)
	{
		super(rows, cols);
	}
	
	public EigenMatrixD(double[] doubleValue, int rows, int cols)
	{
		super(doubleValue, rows, cols);
	}
	
	public EigenVectorD row(int index)
	{
		return new EigenVectorD(getRowData(index), cols);
	}
	
	protected double[] getRowData(int index)
	{
		double result[] = new double[cols];
		for (int i = index, j = 0; j < cols; i += rows, j++)
			result[j] = doubleValue[i];
		return result;
	}
	
	public EigenVectorD col(int index)
	{
		return new EigenVectorD(getColData(index), rows);
	}
	
	protected double[] getColData(int index)
	{
		double result[] = new double[rows];
		int start = rows * index;
		System.arraycopy(doubleValue, start, result, 0, rows);
		return result;
	}
	
	public double trace()
	{
		int size = (rows < cols) ? rows : cols;
		
		double tempValue = 0;
		for (int i = 0, k = 0; i < size; i++, k += size + 1)
			tempValue += doubleValue[k];
		
		return tempValue;
	}
	
	@Override
	public EigenMatrixD add(EigenDoubleDataType v)
	{
		super.add(v);
		return this;
	}
	
	@Override
	public EigenMatrixD sub(EigenDoubleDataType v)
	{
		super.sub(v);
		return this;
	}
	
	@Override
	public EigenMatrixD multiplyScalar(double scalar)
	{
		super.multiplyScalar(scalar);
		return this;
	}
	
	public void multiplyWith(EigenVectorD v, EigenVectorD res)
	{
		res.setValue(matrixMulVectorD(doubleValue, v.doubleValue, rows, cols));
	}
	
	public void multiplyWith(EigenMatrixD m, EigenMatrixD res)
	{
		res.setValue(matrixMulMatrixD(doubleValue, m.doubleValue, rows, cols, m.cols));
	}
	
	public void multiplyWith(EigenVectorF v, EigenVectorD res)
	{
		res.setValue(matrixMulVectorDF(doubleValue, v.getValue(), rows, cols));
	}
	
	public void multiplyWith(EigenMatrixF m, EigenMatrixD res)
	{
		res.setValue(matrixMulMatrixDF(doubleValue, m.getValue(), rows, cols, m.getCols()));
	}
	
	public EigenMatrixF toFloat()
	{
		return new EigenMatrixF(getFloatValues(), rows, cols);
	}
	
	private native double[] matrixMulVectorD(double[] m, double[] v, int numRowsMatrix, int numColsMatrix);
	private native double[] matrixMulMatrixD(double[] m1, double[] m2, int numRowsMatrix1, int numColsMatrix1, int numColsMatrix2);
	private native double[] matrixMulVectorDF(double[] m, float[] v, int numRowsMatrix, int numColsMatrix);
	private native double[] matrixMulMatrixDF(double[] m1, float[] m2, int numRowsMatrix1, int numColsMatrix1, int numColsMatrix2);
}
