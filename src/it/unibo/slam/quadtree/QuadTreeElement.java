package it.unibo.slam.quadtree;

import it.unibo.slam.datatypes.eigen.typedouble.EigenVector2D;

public class QuadTreeElement<DataType>
{
	private EigenVector2D coordinates;
	
	private DataType data;
	
	public QuadTreeElement(EigenVector2D coordinates, DataType data)
	{
		this.coordinates = coordinates;
		this.data = data;
	}

	public EigenVector2D getCoordinates() 
	{
		return coordinates;
	}
	
	public void setCoordinates(EigenVector2D coordinates)
	{
		this.coordinates = coordinates;
	}

	public DataType getData()
	{
		return data;
	}
	
	public void setData(DataType data)
	{
		this.data = data;
	}
}
