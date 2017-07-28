package it.unibo.slam.quadtree;

import it.unibo.slam.datatypes.g2o.PoseVertex;
import it.unibo.slam.quadtree.interfaces.DataID;

/**
 * Pose Vertex data identifier.
 */
public class VertexID implements DataID<PoseVertex>
{
	@Override
	public int getDataID(PoseVertex data)
	{
		return data.getId();
	}
}
