package it.unibo.slam.datatypes.eigen.typedouble;

/**
 * Class representing a generic type of double data taking into consideration the classes available in the Eigen library.<br>
 * In specific, this class contains a single double array and an indication of the rows and columns number of this array
 * in order to represent a vector or matrix type. Many utility methods have been implemented in this class or in the subclasses.
 */
public abstract class EigenDoubleDataType
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
	 * Double array.
	 */
	protected double[] doubleValue;
	
	/**
	 * Empty constructor, initializes all the internal values to 0.
	 * @param rows The number of rows.
	 * @param cols The number of columns.
	 */
	public EigenDoubleDataType(int rows, int cols)
	{
		this(new double[rows * cols], rows, cols);
	}
	
	/**
	 * Base constructor.
	 * @param doubleValue The values to insert.
	 * @param rows The number of rows.
	 * @param cols The number of columns.
	 */
	public EigenDoubleDataType(double[] doubleValue, int rows, int cols)
	{
		this.doubleValue = doubleValue;
		this.rows = rows;
		this.cols = cols;
	}
	
	/**
	 * Gets the internal array of values.
	 * @return The array of values.
	 */
	public double[] getValue()
	{
		return doubleValue;
	}
	
	/**
	 * Sets the internal array of values.
	 * @param doubleValue The new array of values.
	 */
	public void setValue(double[] doubleValue)
	{
		this.doubleValue = doubleValue;
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
	public double get(int row, int col)
	{
		return doubleValue[row + col * rows];
	}
	
	/**
	 * Sets a single internal value by specifying the index in which to set it.
	 * @param value The new value.
	 * @param row The row index.
	 * @param col The column index.
	 */
	public void set(double value, int row, int col)
	{
		doubleValue[row + col * rows] = value;
	}
	
	/**
	 * Add operation between two datatypes that takes into consideration the sum of values from the two arrays.
	 * The values considered start from the index 0 and continue until the length of this array.
	 * @param v The input data.
	 * @return The same object in which the operation has been invoked.
	 */
	public EigenDoubleDataType add(EigenDoubleDataType v)
	{
		for (int i = 0; i < rows * cols; i++)
			doubleValue[i] += v.doubleValue[i];
		
		return this;
	}
	
	/**
	 * Subtraction operation between two datatypes that takes into consideration the difference of values from the two arrays.
	 * The values considered start from the index 0 and continue until the length of this array.
	 * @param v The input data.
	 * @return The same object in which the operation has been invoked.
	 */
	public EigenDoubleDataType sub(EigenDoubleDataType v)
	{
		for (int i = 0; i < rows * cols; i++)
			doubleValue[i] -= v.doubleValue[i];
		
		return this;
	}
	
	/**
	 * Multiply the internal array by a scalar.
	 * @param scalar The scalar.
	 * @return The same object in which the operation has been invoked.
	 */
	public EigenDoubleDataType multiplyScalar(double scalar)
	{
		for (int i = 0; i < rows * cols; i++)
			doubleValue[i] *= scalar;
		
		return this;
	}
	
	/**
	 * Converts the internal double array in a float array and returns it.
	 * @return The float array.
	 */
	protected float[] getFloatValues()
	{
		int size = rows * cols;
		float[] value = new float[size];
		
		for (int i = 0; i < size; i++)
			value[i] = (float)doubleValue[i];
		
		return value;
	}
	
	/**
	 * Return the internal values transposed
	 * @return The values transposed.
	 */
	protected double[] getTransposedValues()
	{
		double[] transpose = new double[doubleValue.length];
		
		for (int i = 0, x = 0; i < rows; i++, x += cols)
			for (int j = 0, y = 0; j < cols; j++, y += rows)
				transpose[j + x] = doubleValue[i + y];
		
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
			EigenDoubleDataType other = (EigenDoubleDataType) obj;
			int size = rows * cols;
			int otherSize = other.rows * other.cols;
			if (size != otherSize)
				return false;
			for (int i = 0; i < size; i++)
				if (doubleValue[i] != other.doubleValue[i])
					return false;
			return true;
		}
		catch (ClassCastException e)
		{
			return false;
		}
	}
}
