package it.unibo.slam.export;

/**
 * Enumerative representing the file format used by the save operation.
 */
public enum ExportFileFormat
{
	/**
	 * PLY - ASCII format.
	 */
	PLY_ASCII("ply"),
	
	/**
	 * PLY - Binary format (little endian).
	 */
	PLY_BINARY_LITTLE_ENDIAN("ply"),
	
	/**
	 * Binary format (big endian).
	 */
	PLY_BINARY_BIG_ENDIAN("ply");
	
	/**
	 * File format extension.
	 */
	private String extension;
	
	/**
	 * Private constructor.
	 * @param extension The file extension.
	 */
	private ExportFileFormat(String extension)
	{
		this.extension = extension;
	}
	
	/**
	 * Gets the extension of this file format.
	 * @return The file extension as a string;
	 */
	public String getExtension()
	{
		return extension;
	}
}
