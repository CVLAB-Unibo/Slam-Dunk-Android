package it.unibo.slam.quadtree.interfaces;

import java.util.List;

import it.unibo.slam.datatypes.eigen.typedouble.EigenVector2D;
import it.unibo.slam.quadtree.QuadTreeElement;

public interface QuadTreeNode<DataType, ElementRefType>
{
	public ElementRefType insert(QuadTreeElement<DataType> element);
	
	public boolean isEmpty();
	
	public void query(	EigenVector2D offset, double length, EigenVector2D p0, 
						EigenVector2D p1, List<QuadTreeElement<DataType>> data);
	
	public short getLevel();
}
