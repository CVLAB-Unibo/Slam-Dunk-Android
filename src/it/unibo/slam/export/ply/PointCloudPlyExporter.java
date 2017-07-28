package it.unibo.slam.export.ply;

import it.unibo.slam.gui.opengl.model.PointCloudStatic;
import it.unibo.slam.utils.Constants;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

/**
 * Class representing an exporter of the PLY format, allowing to save the point clouds in a PLY file.
 */
public class PointCloudPlyExporter
{
	/**
	 * Saves the input point clouds in a PLY file, given its path and its type.
	 * @param fileName The file path.
	 * @param type The file type (ASCII, binary big endian or binary little endian).
	 * @param pointClouds The input point clouds.
	 * @return True if the save operation has been successful, false otherwise.
	 */
	public static boolean exportTo(String fileName, PlyFileType type, List<PointCloudStatic> pointClouds)
	{
		boolean result = false;
		
		switch (type)
		{
			case ASCII:
				result = writeAsciiPly(fileName, pointClouds);
				break;
				
			case BINARY_LITTLE_ENDIAN:
				result = writeBinaryPly(fileName, pointClouds, ByteOrder.LITTLE_ENDIAN);
				break;
				
			case BINARY_BIG_ENDIAN:
				result = writeBinaryPly(fileName, pointClouds, ByteOrder.BIG_ENDIAN);
				break;
				
			default:
				break;
		}
		
		return result;
	}
	
	/**
	 * Writes the point clouds in a PLY file using the ASCII format.
	 * @param fileName The file path.
	 * @param pointClouds The input point clouds.
	 * @return True if the file has been saved, false otherwise.
	 */
	private static boolean writeAsciiPly(String fileName, List<PointCloudStatic> pointClouds)
	{
		boolean result = false;
		
		try
		{
			PrintWriter writer = new PrintWriter(new FileWriter(fileName));
			
			// Writing general informations
			writer.println("ply");
			writer.println("format ascii 1.0");
			writer.println("comment Generated by SLAM DUNK on Android");
			
			// Writing vertices number
			int verticesNumber = 0;
			for (PointCloudStatic pointCloud : pointClouds)
				verticesNumber += pointCloud.getNumberOfPoints();
			writer.println("element vertex " + verticesNumber);
			
			// Writing position property
			writer.println("property float x");
			writer.println("property float y");
			writer.println("property float z");
			
			// Writing color property
			writer.println("property uchar red");
			writer.println("property uchar green");
			writer.println("property uchar blue");
			
			// End of header
			writer.println("end_header");
			
			// Creating buffer
			int bufferSize = 65536;
			StringBuilder rowString = new StringBuilder(bufferSize);
			
			// Writing points
			for (PointCloudStatic pointCloud : pointClouds)
			{
				float[] pointCloudVertices = pointCloud.getTransformedVertexArray();
				byte[] pointCloudColors = pointCloud.getColorArray();
				
				int stringSize = 0;
				char whitespace = ' ', newline = '\n';
				for (int i = 0; i < pointCloudVertices.length; i += 3)
				{
					rowString.	append(pointCloudVertices[i]).append(whitespace).
								append(pointCloudVertices[i + 1]).append(whitespace).
								append(pointCloudVertices[i + 2]).append(whitespace).
								append(pointCloudColors[i] & 0xFF).append(whitespace).
								append(pointCloudColors[i + 1] & 0xFF).append(whitespace).
								append(pointCloudColors[i + 2] & 0xFF).append(newline);
					
					stringSize += rowString.length();
					if (stringSize >= bufferSize)
					{
						writer.write(rowString.toString());
						rowString.setLength(0);
						stringSize = 0;
					}
				}
				
				if (stringSize > 0)
				{
					writer.write(rowString.toString());
					rowString.setLength(0);
				}
			}
			
			writer.close();
			result = true;
		}
		catch (IOException e)
		{
			result = false;
		}
		
		return result;
	}
	
	/**
	 * Writes the point clouds in a PLY file using the binary format.
	 * @param fileName The file path.
	 * @param pointClouds The input point clouds.
	 * @param byteOrder The byte order used in the format (little endian or big endian).
	 * @return True if the file has been saved, false otherwise.
	 */
	private static boolean writeBinaryPly(String fileName, List<PointCloudStatic> pointClouds, ByteOrder byteOrder)
	{
		boolean result = false;
		
		try
		{
			PrintWriter writer = new PrintWriter(new FileWriter(fileName));
			
			// Writing general informations
			writer.println("ply");
			if (byteOrder == ByteOrder.LITTLE_ENDIAN)
				writer.println("format binary_little_endian 1.0");
			else
				writer.println("format binary_big_endian 1.0");
			writer.println("comment Generated by SLAM DUNK on Android");
			
			// Writing vertices number
			int verticesNumber = 0;
			for (PointCloudStatic pointCloud : pointClouds)
				verticesNumber += pointCloud.getColorArray().length / 3;
			writer.println("element vertex " + verticesNumber);
			
			// Writing position property
			writer.println("property float x");
			writer.println("property float y");
			writer.println("property float z");
			
			// Writing color property
			writer.println("property uchar red");
			writer.println("property uchar green");
			writer.println("property uchar blue");
			
			// End of header
			writer.println("end_header");
			writer.close();
			
			// Creating binary stream and buffer
			DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(fileName, true));
			int singleElementSize = 3 * Constants.BYTES_PER_FLOAT + 3;
			int bufferSize = 5000 * singleElementSize;
			ByteBuffer buffer = ByteBuffer.allocateDirect(bufferSize).order(byteOrder);
			buffer.position(0);
			
			// Writing points
			for (PointCloudStatic pointCloud : pointClouds)
			{
				float[] pointCloudVertices = pointCloud.getTransformedVertexArray();
				byte[] pointCloudColors = pointCloud.getColorArray();
				
				int tempSize = 0;
				for (int i = 0; i < pointCloudVertices.length; i += 3)
				{
					buffer.	putFloat(pointCloudVertices[i]).
							putFloat(pointCloudVertices[i + 1]).
							putFloat(pointCloudVertices[i + 2]).
							put(pointCloudColors[i]).
							put(pointCloudColors[i + 1]).
							put(pointCloudColors[i + 2]);
					
					tempSize += singleElementSize;
					if (tempSize >= bufferSize)
					{
						outputStream.write(buffer.array());
						buffer.position(0);
						tempSize = 0;
					}
				}
				
				if (tempSize > 0)
				{
					outputStream.write(buffer.array(), 0, tempSize);
					buffer.position(0);
				}
			}
			
			buffer.clear();
			outputStream.close();
			result = true;
		}
		catch (IOException e)
		{
			result = false;
		}
		
		return result;
	}
}