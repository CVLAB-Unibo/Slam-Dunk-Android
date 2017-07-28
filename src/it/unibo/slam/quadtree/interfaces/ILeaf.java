package it.unibo.slam.quadtree.interfaces;

import it.unibo.slam.quadtree.QuadTreeElement;
import it.unibo.slam.quadtree.QuadTreeLeafElementRef;

public interface ILeaf<DataType> extends QuadTreeNode<DataType, QuadTreeLeafElementRef<DataType>>
{
	public boolean remove(QuadTreeElement<DataType> element);
}
