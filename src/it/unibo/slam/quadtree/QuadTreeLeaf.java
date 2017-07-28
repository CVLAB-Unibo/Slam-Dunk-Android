package it.unibo.slam.quadtree;

import it.unibo.slam.datatypes.eigen.typedouble.EigenVector2D;
import it.unibo.slam.quadtree.interfaces.ILeaf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class QuadTreeLeaf<DataType> implements ILeaf<DataType>
{
	private List<QuadTreeElement<DataType>> elements;
	
	public QuadTreeLeaf()
	{
		elements = new ArrayList<QuadTreeElement<DataType>>();
	}
	
	@Override
	public boolean remove(QuadTreeElement<DataType> element)
	{
		return elements.remove(element);
	}
	
	@Override
	public QuadTreeLeafElementRef<DataType> insert(QuadTreeElement<DataType> element)
	{
		elements.add(element);
		
		QuadTreeLeafElementRef<DataType> elementRef = new QuadTreeLeafElementRef<DataType>(element, this);
		return elementRef;
	}

	@Override
	public boolean isEmpty()
	{
		return elements.isEmpty();
	}

	@Override
	public void query(	EigenVector2D offset, double length, EigenVector2D p0,
						EigenVector2D p1, List<QuadTreeElement<DataType>> data)
	{
		Iterator<QuadTreeElement<DataType>> iterator = elements.iterator();
		EigenVector2D coordinates;
		QuadTreeElement<DataType> element;
		while (iterator.hasNext())
		{
			element = (QuadTreeElement<DataType>)iterator.next();
			coordinates = element.getCoordinates();
			
			if (coordinates.getX() >= p0.getX() && coordinates.getX() < p1.getX() &&
				coordinates.getY() >= p0.getY() && coordinates.getY() < p1.getY())
			{
				EigenVector2D newCoordinates = new EigenVector2D(	coordinates.getX() * length + offset.getX(), 
																	coordinates.getY() * length + offset.getY());
				QuadTreeElement<DataType> elementCopy = new QuadTreeElement<DataType>(newCoordinates, element.getData());
				data.add(elementCopy);
			}
		}
	}

	@Override
	public short getLevel()
	{
		return 0;
	}
}
