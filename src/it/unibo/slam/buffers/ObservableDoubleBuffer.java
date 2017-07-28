package it.unibo.slam.buffers;

import java.util.Observable;

import it.unibo.slam.buffers.interfaces.ReadWriteDoubleBuffer;

/**
 * Observable double buffer.<br>
 * Write operations and swapping the buffers will notify the observers, 
 * in case new data has been written in the front buffer.
 * @param <T> The data inside this buffer.
 */
public class ObservableDoubleBuffer<T> extends Observable implements ReadWriteDoubleBuffer<T>
{
	/**
	 * Data available in the front buffer.
	 */
	private T frontBufferData;
	
	/**
	 * Indicates if the data in the front buffer is valid or not.
	 */
	private boolean frontBufferDataValid;
	
	/**
	 * Data available in the back buffer.
	 */
	private T backBufferData;
	
	/**
	 * Indicates if the data in the back buffer is valid or not.
	 */
	private boolean backBufferDataValid;
	
	/**
	 * Base constructor.
	 */
	public ObservableDoubleBuffer()
	{
		frontBufferDataValid = false;
		frontBufferData = null;
		backBufferDataValid = false;
		backBufferData = null;
	}
	
	@Override
	public synchronized T readData()
	{
		if (frontBufferDataValid)
			return frontBufferData;
		else
			return null;
	}

	@Override
	public synchronized boolean isDataValid()
	{
		return frontBufferDataValid;
	}

	@Override
	public synchronized void notifyReadFinished()
	{
		frontBufferDataValid = false;
		frontBufferData = null;
		
		if (backBufferDataValid)
		{
			frontBufferDataValid = backBufferDataValid;
			frontBufferData = backBufferData;
			backBufferDataValid = false;
			backBufferData = null;
			setChanged();
			notifyObservers();
		}
	}

	@Override
	public synchronized void fillBuffer(T bufferData)
	{
		if (frontBufferDataValid)
		{
			backBufferDataValid = true;
			backBufferData = bufferData;
		}
		else
		{
			frontBufferDataValid = true;
			frontBufferData = bufferData;
			setChanged();
			notifyObservers();
		}
	}
}