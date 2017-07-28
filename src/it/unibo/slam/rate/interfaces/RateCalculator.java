package it.unibo.slam.rate.interfaces;

/**
 * Interface of a rate calculator.
 */
public interface RateCalculator
{
	/**
	 * Gets the current rate.
	 * @param lastElapsedMillis The milliseconds passed during the execution of the last iteration.
	 * @return The current rate.
	 */
	public float getRate(int lastElapsedMillis);
}
