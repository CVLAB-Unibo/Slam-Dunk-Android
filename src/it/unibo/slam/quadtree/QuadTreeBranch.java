package it.unibo.slam.quadtree;

import it.unibo.slam.datatypes.eigen.typedouble.EigenVector2D;
import it.unibo.slam.quadtree.interfaces.IBranch;
import it.unibo.slam.quadtree.interfaces.QuadTreeNode;

import java.util.List;

public class QuadTreeBranch<DataType> implements IBranch<DataType>
{
	private LeafType leafType;
	
	private short level;
	
    @SuppressWarnings("unchecked")
	private QuadTreeNode<DataType, QuadTreeLeafElementRef<DataType>>[] children = 
		(QuadTreeNode<DataType, QuadTreeLeafElementRef<DataType>>[]) 
		new QuadTreeNode[4]; // { (0,0), (1,0), (0,1), (1,1) }
    
	public QuadTreeBranch(LeafType leafType, short level)
	{
		this.leafType = leafType;
		this.level = level;
		children[0] = null;
		children[1] = null;
		children[2] = null;
		children[3] = null;
	}

	@Override
	public QuadTreeLeafElementRef<DataType> insert(QuadTreeElement<DataType> element)
	{
		EigenVector2D coordinates = element.getCoordinates();
		
		coordinates.setX(coordinates.getX() * 2);
		coordinates.setY(coordinates.getY() * 2);
		assert(coordinates.getX() >= 0 && coordinates.getX() < 2);
		assert(coordinates.getY() >= 0 && coordinates.getY() < 2);
		
		int index = (int)coordinates.getX() + 2 * (int)coordinates.getY();
		if (coordinates.getX() >= 1)
			coordinates.setX(coordinates.getX() - 1);
		if (coordinates.getY() >= 1)
			coordinates.setY(coordinates.getY() - 1);
		
		if (children[index] == null)
		{
			if (level == 0)
				children[index] = QuadTreeFactory.getInstance().createLeaf(leafType);
			else
				children[index] = new QuadTreeBranch<DataType>(leafType, (short)(level - 1));
		}
		
		return children[index].insert(element);
	}

	@Override
	public boolean isEmpty()
	{
		return (children[0] == null && children[1] == null && children[2] == null && children[3] == null);
	}

	@Override
	public void query(	EigenVector2D offset, double length, EigenVector2D p0,
						EigenVector2D p1, List<QuadTreeElement<DataType>> data)
	{
		length /= 2.0;
		
		if (children[0] != null && p0.getX() < 0.5 && p0.getY() < 0.5)
			children[0].query(	offset, length, new EigenVector2D(p0.getX() * 2, p0.getY() * 2), 
								new EigenVector2D(p1.getX() * 2, p1.getY() * 2), data);
		
		if (children[1] != null && p0.getY() < 0.5 && p1.getX() > 0.5)
			children[1].query(	new EigenVector2D(offset.getX() + length, offset.getY()), length, 
								new EigenVector2D(2 * p0.getX() - 1, 2 * p0.getY()), 
								new EigenVector2D(2 * p1.getX() - 1, 2 * p1.getY()), data);
		
		if (children[2] != null && p0.getX() < 0.5 && p1.getY() > 0.5)
			children[2].query(	new EigenVector2D(offset.getX(), offset.getY() + length), length, 
								new EigenVector2D(2 * p0.getX(), 2 * p0.getY() - 1), 
								new EigenVector2D(2 * p1.getX(), 2 * p1.getY() - 1), data);
		
		if (children[3] != null && p1.getX() > 0.5 && p1.getY() > 0.5)
			children[3].query(	new EigenVector2D(offset.getX() + length, offset.getY() + length), length, 
								new EigenVector2D(2 * p0.getX() - 1, 2 * p0.getY() - 1), 
								new EigenVector2D(2 * p1.getX() - 1, 2 * p1.getY() - 1), data);
	}

	@Override
	public short getLevel()
	{
		return level;
	}
}
