package it.unibo.slam.export.ply;

/**
 * Enumerative representing the PLY file type used (ASCII or Binary Little/Big Endian).
 */
public enum PlyFileType
{
	/**
	 * ASCII format.
	 */
	ASCII,
	
	/**
	 * Binary format (little endian).
	 */
	BINARY_LITTLE_ENDIAN,
	
	/**
	 * Binary format (big endian).
	 */
	BINARY_BIG_ENDIAN;
}
