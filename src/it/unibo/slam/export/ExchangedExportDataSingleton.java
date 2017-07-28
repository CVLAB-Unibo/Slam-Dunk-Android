package it.unibo.slam.export;

/**
 * Singleton class used to exchange data between MainActivity and ExportActivity.
 */
public class ExchangedExportDataSingleton
{
	/**
	 * Singleton instance.
	 */
	private static ExchangedExportDataSingleton instance = new ExchangedExportDataSingleton();
	
	/**
	 * Export data contained in the singleton.
	 */
	private ExportData data = null;
	
	/**
	 * Private singleton constructor.
	 */
	private ExchangedExportDataSingleton()
	{
		
	}
	
	/**
	 * Gets the instance of this singleton.
	 * @return The singleton's instance.
	 */
	public static ExchangedExportDataSingleton getInstance()
	{
		return instance;
	}
	
	/**
	 * Gets the export data.
	 * @return The export data.
	 */
	public ExportData getExportData()
	{
		return data;
	}
	
	/**
	 * Sets the export data.
	 * @param data The export data.
	 */
	public void setExportData(ExportData data)
	{
		this.data = data;
	}
}
