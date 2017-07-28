package it.unibo.slam.buffers.interfaces;

/**
 * Buffer in which you can read and write the data.
 * @param <T> The data.
 */
public interface ReadWriteDoubleBuffer<T> extends ReadOnlyDoubleBuffer<T>, WriteOnlyDoubleBuffer<T>
{
	
}
