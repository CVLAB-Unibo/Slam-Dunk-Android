package it.unibo.slam.quadtree;

import it.unibo.slam.datatypes.eigen.typedouble.EigenVector2D;
import it.unibo.slam.quadtree.interfaces.DataID;
import it.unibo.slam.quadtree.interfaces.QuadTreeNode;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * Class representing a QuadTree.
 * @param <DataType>
 * @param <DataIDType>
 */
public class QuadTree<DataType, DataIDType extends DataID<DataType>>
{
	private LeafType leafType;
	
	private BranchType branchType;
	
	private double resolution;
	
	private double length;
	
	private QuadTreeNode<DataType, QuadTreeLeafElementRef<DataType>> root;
	
	private EigenVector2D offset;
	
	private DataIDType dataId;
	
	Map<Integer, QuadTreeLeafElementRef<DataType>> elementMap;
	
	/**
	 * Base constructor.
	 * @param branchType Branch type.
	 * @param leafType Leaf type.
	 * @param resolution Tree resolution.
	 * @param maxDepth Tree max depth.
	 * @param dataId Instance that makes the calculation of the data identifier.
	 */
	public QuadTree(BranchType branchType, LeafType leafType, double resolution, short maxDepth, DataIDType dataId)
	{
		this.resolution = resolution;
		length = resolution * (1 << (maxDepth + 1));
		this.leafType = leafType;
		this.branchType = branchType;
		root = QuadTreeFactory.getInstance().createBranch(branchType, leafType, (short)(maxDepth + 1));
		offset = new EigenVector2D();
		this.dataId = dataId;
		elementMap = new Hashtable<Integer, QuadTreeLeafElementRef<DataType>>();
	}
	
    public double getResolution()
    {
    	return resolution;
    }
    
    public double getLength()
    {
    	return length;
    }
    
    public EigenVector2D getOffset()
    {
    	return offset;
    }
	
    /**
     * Insert new data into the QuadTree.
     * @param data The data to insert.
     * @param coords Coordinates inside the tree.
     * @return True if the insertion has been successful, false otherwise.
     */
	public boolean insert(DataType data, EigenVector2D coords)
	{
		if (root.isEmpty())
		{
			double halfLength = length / 2.0;
			offset.setX(coords.getX() - halfLength);
			offset.setY(coords.getY() - halfLength);
		}
		else if (	coords.getX() < offset.getX() || coords.getX() >= offset.getX() + length ||
					coords.getY() < offset.getY() || coords.getY() >= offset.getY() + length)
		{
			return false;
		}
		
		double lengthDiv = 1 / length;
		QuadTreeElement<DataType> element = new QuadTreeElement<DataType>(	new EigenVector2D((coords.getX() - offset.getX()) * lengthDiv, 
																			(coords.getY() - offset.getY()) * lengthDiv), data);
		elementMap.put(dataId.getDataID(data), root.insert(element));
		
		return true;
	}
	
	/**
	 * Remove data from the QuadTree.
	 * @param data The data to remove.
	 * @return True if the data has been found and removed, false otherwise.
	 */
	public boolean remove(DataType data)
	{
		int id = dataId.getDataID(data);
		QuadTreeLeafElementRef<DataType> elementRef = elementMap.get(id);
		if (elementRef == null)
			return false;
		boolean result = elementRef.erase();
		elementMap.remove(id);
		return result;
	}
	
	/**
	 * Updates data inside the QuadTree.
	 * @param data New data to insert in place of older one.
	 * @param coords Coordinates of the data.
	 * @return True if the update has been successful, false otherwise.
	 */
	public boolean update(DataType data, EigenVector2D coords)
	{
		int id = dataId.getDataID(data);
		QuadTreeLeafElementRef<DataType> elementRef = elementMap.get(id);
		if (elementRef != null)
			elementRef.erase();
		
		if (root.isEmpty())
		{
			double halfLength = length / 2;
			offset.setX(coords.getX() - halfLength);
			offset.setY(coords.getY() - halfLength);
		}
		else if (	coords.getX() < offset.getX() || coords.getX() >= offset.getX() + length ||
					coords.getY() < offset.getY() || coords.getY() >= offset.getY() + length)
		{
			if (elementRef != null)
				elementMap.remove(id);
			return false;
		}
		
		double lengthDiv = 1 / length;
		QuadTreeElement<DataType> element = new QuadTreeElement<DataType>(	new EigenVector2D((coords.getX() - offset.getX()) * lengthDiv, 
																			(coords.getY() - offset.getY()) * lengthDiv), data);
		if (elementRef != null)
			elementMap.remove(id);
		elementMap.put(id, root.insert(element));
		
		return true;
	}
	
	/**
	 * Query search inside the QuadTree.
	 * @param x0 Center x coordinate.
	 * @param y0 Center y coordinate.
	 * @param width Window width.
	 * @param height Window height.
	 * @param data List of the data found in the search.
	 */
	public void query(double x0, double y0, double width, double height, List<QuadTreeElement<DataType>> data)
	{
		if (width < 0)
		{
			x0 += width;
			width *= -1;
		}
		
		if (height < 0)
		{
			y0 += height;
			height *= -1;
		}
		
		if (x0 + width < offset.getX() || x0 >= offset.getX() + length ||
			y0 + height < offset.getY() || y0 >= offset.getY() + length)
			return;
		
		double lengthDiv = 1 / length;
		EigenVector2D p0 = new EigenVector2D((x0 - offset.getX()) * lengthDiv, (y0 - offset.getY()) * lengthDiv);
		EigenVector2D p1 = new EigenVector2D((x0 + width - offset.getX()) * lengthDiv, (y0 + height - offset.getY()) * lengthDiv);
		root.query(offset, length, p0, p1, data);
	}
	
	/**
	 * Query search without specifying the active window parameters.
	 * @param data List of the data found in the search.
	 */
	public void query(List<QuadTreeElement<DataType>> data)
    {
		query(offset.getX(), offset.getY(), offset.getX() + length, offset.getY() + length, data);
    }
	
	/**
	 * Clear the QuadTree.
	 */
	public void clear()
    {
		elementMap.clear();
	    if(!root.isEmpty())
	    {
	    	short level = root.getLevel();
	    	root = QuadTreeFactory.getInstance().createBranch(branchType, leafType, level);
	    }
    }
}