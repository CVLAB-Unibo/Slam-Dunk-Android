package it.unibo.slam.quadtree;

import it.unibo.slam.quadtree.interfaces.DataID;

/**
 * Dummy data identifier class.
 * @param <DataType>
 */
public class DummyDataID<DataType> implements DataID<DataType>
{
	@Override
	public int getDataID(DataType data)
	{
		return (Integer)data;
	}
}
