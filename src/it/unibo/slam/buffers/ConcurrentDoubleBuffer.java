package it.unibo.slam.buffers;

import it.unibo.slam.buffers.interfaces.ReadWriteDoubleBuffer;

/**
 * Thread-safe double buffer.<br>
 * Read operations will suspend the thread if the data is not available.<br>
 * Write operations will free the waiting thread.
 * @param <T> The data inside this buffer.
 */
public class ConcurrentDoubleBuffer<T> implements ReadWriteDoubleBuffer<T>
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
	public ConcurrentDoubleBuffer()
	{
		frontBufferDataValid = false;
		frontBufferData = null;
		backBufferDataValid = false;
		backBufferData = null;
	}
	
	@Override
	public synchronized T readData()
	{
		while (!frontBufferDataValid)
		{
			try
			{
				wait();
			}
			catch (InterruptedException e)
			{
				
			}
		}
		
		return frontBufferData;
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
			notifyAll();
		}
	}
}
