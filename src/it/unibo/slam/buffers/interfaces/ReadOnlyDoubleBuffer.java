package it.unibo.slam.buffers.interfaces;

/**
 * Buffer in which you can only read the data.
 * @param <T> The data.
 */
public interface ReadOnlyDoubleBuffer<T>
{
	/**
	 * Gets the internal data.
	 * @return The data.
	 */
	public T readData();
	
	/**
	 * Checks if the internal data is valid.
	 * @return True if the data is valid, false otherwise.
	 */
	public boolean isDataValid();
	
	/**
	 * Notifies that the read operation is finished so that the data can be changed.
	 */
	public void notifyReadFinished();
}