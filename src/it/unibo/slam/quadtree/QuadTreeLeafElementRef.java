package it.unibo.slam.quadtree;

import it.unibo.slam.quadtree.interfaces.ILeaf;

public class QuadTreeLeafElementRef<DataType>
{
	private QuadTreeElement<DataType> element = null;
	
	private ILeaf<DataType> leaf = null;
	
	public QuadTreeLeafElementRef(QuadTreeElement<DataType> element, ILeaf<DataType> leaf)
	{
		this.element = element;
		this.leaf = leaf;
	}
	
	public boolean erase()
	{
		if (element != null && leaf != null)
			return leaf.remove(element);
		
		return false;
	}   
}
