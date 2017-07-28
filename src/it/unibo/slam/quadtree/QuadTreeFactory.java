package it.unibo.slam.quadtree;

import it.unibo.slam.quadtree.interfaces.DataID;
import it.unibo.slam.quadtree.interfaces.IBranch;
import it.unibo.slam.quadtree.interfaces.ILeaf;

public class QuadTreeFactory
{
	private static QuadTreeFactory instance = null;
	
	private QuadTreeFactory()
	{
		
	}
	
	public static QuadTreeFactory getInstance()
	{
		if (instance == null)
			instance = new QuadTreeFactory();
		
		return instance;
	}
	
	<DataType> IBranch<DataType> createBranch(BranchType branchType, LeafType leafType, short level)
	{
		IBranch<DataType> result;
		
		switch (branchType)
		{
			case DEFAULT:
				result = new QuadTreeBranch<DataType>(leafType, level);
				break;
				
			default:
				result = null;
				break;
		}
		
		return result;
	}
	
	<DataType> ILeaf<DataType> createLeaf(LeafType leafType)
	{
		ILeaf<DataType> result;
		
		switch (leafType)
		{
			case DEFAULT:
				result = new QuadTreeLeaf<DataType>();
				break;
				
			default:
				result = null;
				break;
		}
		
		return result;
	}
	
	public <DataType, DataIDType extends DataID<DataType>> QuadTree<DataType, DataIDType> createDefaultQuadTree(double resolution, 
			short maxDepth, DataIDType dataId)
	{
		return new QuadTree<DataType, DataIDType>(BranchType.DEFAULT, LeafType.DEFAULT, resolution, maxDepth, dataId);
	}
}
