package it.unibo.slam.datatypes.eigen.typefloat;

/**
 * Class representing a generic type of float data taking into consideration the classes available in the Eigen library.<br>
 * In specific, this class contains a single float array and an indication of the rows and columns number of this array
 * in order to represent a vector or matrix type. Many utility methods have been implemented in this class or in the subclasses.
 */
public abstract class EigenFloatDataType
{
	/**
	 * Number of rows.
	 */
	protected int rows;
	
	/**
	 * Number of columns.
	 */
	protected int cols;
	
	/**
	 * Float array.
	 */
	protected float[] floatValue;
	
	/**
	 * Empty constructor, initializes all the internal values to 0.
	 * @param rows The number of rows.
	 * @param cols The number of columns.
	 */
	public EigenFloatDataType(int rows, int cols)
	{
		this(new float[rows * cols], rows, cols);
	}
	
	/**
	 * Base constructor.
	 * @param floatValue The values to insert.
	 * @param rows The number of rows.
	 * @param cols The number of columns.
	 */
	public EigenFloatDataType(float[] floatValue, int rows, int cols)
	{
		this.floatValue = floatValue;
		this.rows = rows;
		this.cols = cols;
	}
	
	/**
	 * Gets the internal array of values.
	 * @return The array of values.
	 */
	public float[] getValue()
	{
		return floatValue;
	}
	
	/**
	 * Sets the internal array of values.
	 * @param floatValue The new array of values.
	 */
	public void setValue(float[] floatValue)
	{
		this.floatValue = floatValue;
	}
	
	/**
	 * Gets the number of rows.
	 * @return The number of rows.
	 */
	public int getRows()
	{
		return rows;
	}
	
	/**
	 * Gets the number of columns.
	 * @return The number of columns.
	 */
	public int getCols()
	{
		return cols;
	}
	
	/**
	 * Gets a single internal value by specifying its index.
	 * @param row The row index.
	 * @param col The column index.
	 * @return
	 */
	public float get(int row, int col)
	{
		return floatValue[row + col * rows];
	}
	
	/**
	 * Sets a single internal value by specifying the index in which to set it.
	 * @param value The new value.
	 * @param row The row index.
	 * @param col The column index.
	 */
	public void set(float value, int row, int col)
	{
		floatValue[row + col * rows] = value;
	}
	
	/**
	 * Add operation between two datatypes that takes into consideration the sum of values from the two arrays.
	 * The values considered start from the index 0 and continue until the length of this array.
	 * @param v The input data.
	 * @return The same object in which the operation has been invoked.
	 */
	public EigenFloatDataType add(EigenFloatDataType v)
	{
		for (int i = 0; i < rows * cols; i++)
			floatValue[i] += v.floatValue[i];
		
		return this;
	}
	
	/**
	 * Subtraction operation between two datatypes that takes into consideration the difference of values from the two arrays.
	 * The values considered start from the index 0 and continue until the length of this array.
	 * @param v The input data.
	 * @return The same object in which the operation has been invoked.
	 */
	public EigenFloatDataType sub(EigenFloatDataType v)
	{
		for (int i = 0; i < rows * cols; i++)
			floatValue[i] -= v.floatValue[i];
		
		return this;
	}
	
	/**
	 * Multiply the internal array by a scalar.
	 * @param scalar The scalar.
	 * @return The same object in which the operation has been invoked.
	 */
	public EigenFloatDataType multiplyScalar(float scalar)
	{
		for (int i = 0; i < rows * cols; i++)
			floatValue[i] *= scalar;
		
		return this;
	}
	
	/**
	 * Converts the internal float array in a double array and returns it.
	 * @return The double array.
	 */
	protected double[] getDoubleValues()
	{
		int size = rows * cols;
		double[] values = new double[size];
		
		for (int i = 0; i < size; i++)
			values[i] = (double)floatValue[i];
		
		return values;
	}
	
	/**
	 * Return the internal values transposed
	 * @return The values transposed.
	 */
	protected float[] getTransposedValues()
	{
		float[] transpose = new float[floatValue.length];
		
		for (int i = 0, x = 0; i < rows; i++, x += cols)
			for (int j = 0, y = 0; j < cols; j++, y += rows)
				transpose[j + x] = floatValue[i + y];
		
		return transpose;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		
		if (obj == null)
			return false;
		
		try
		{
			EigenFloatDataType other = (EigenFloatDataType) obj;
			int size = rows * cols;
			int otherSize = other.rows * other.cols;
			if (size != otherSize)
				return false;
			for (int i = 0; i < size; i++)
				if (floatValue[i] != other.floatValue[i])
					return false;
			return true;
		}
		catch (ClassCastException e)
		{
			return false;
		}
	}
}
