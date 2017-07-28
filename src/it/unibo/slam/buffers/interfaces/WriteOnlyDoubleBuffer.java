package it.unibo.slam.buffers.interfaces;

/**
 * Buffer in which you can only write the data.
 * @param <T> The data.
 */
public interface WriteOnlyDoubleBuffer<T>
{
	/**
	 * Fills the buffer with new data.
	 * @param bufferData The new data.
	 */
	public void fillBuffer(T bufferData);
}
