package it.unibo.slam.datatypes.params;

/**
 * Class representing the general application parameters (from the settings).
 */
public class GeneralParams
{
	/**
	 * If true the grabber is executed in a separate thread.
	 */
	private boolean grabberAsThread;
	
	/**
	 * The amount of subsampling (a multiple of 2).
	 */
	private int subsampleAmount;
	
	/**
	 * If true the algorithm will be executed in C, if false it will be executed in Java.
	 */
	private boolean executeNative;

	/**
	 * Base constructor.
	 * @param grabberAsThread True if the grabber will be executed in a separate thread, false otherwise.
	 * @param subsampleAmount The subsample amount (a multiple of 2).
	 * @param executeNative If true the algorithm will be executed in C, otherwise in Java.
	 */
	public GeneralParams(boolean grabberAsThread, int subsampleAmount, boolean executeNative)
	{
		this.grabberAsThread = grabberAsThread;
		this.subsampleAmount = subsampleAmount;
		this.executeNative = executeNative;
	}

	/**
	 * Checks if the grabber is executed in a separate thread.
	 * @return True if the grabber is executed in a separate thread, false otherwise.
	 */
	public boolean isGrabberAsThread()
	{
		return grabberAsThread;
	}

	/**
	 * Gets the subsample amount.
	 * @return The subsample amount.
	 */
	public int getSubsampleAmount()
	{
		return subsampleAmount;
	}

	/**
	 * Checks if native execution is enabled.
	 * @return True for native execution, false otherwise.
	 */
	public boolean isExecuteNative()
	{
		return executeNative;
	}
}
