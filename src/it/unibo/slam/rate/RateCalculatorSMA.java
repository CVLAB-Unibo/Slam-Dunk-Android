package it.unibo.slam.rate;

import it.unibo.slam.rate.interfaces.RateCalculator;

/**
 * Rate calculator using the mean of previous collected values and considering just 
 * a limited amount of values in this calculation.
 */
public class RateCalculatorSMA implements RateCalculator
{
	/**
	 * Maximum number of values considered.
	 */
	private static final int MAX_ITERATIONS = 10;
	
	/**
	 * Array that stores the previous collected values in terms of elapsed milliseconds.
	 */
	private int[] lastIterations;
	
	/**
	 * The current index in the array.
	 */
	private int currentIndex;
	
	/**
	 * Sum of collected values.
	 */
	private int sumIterations;
	
	/**
	 * True the array is being cycled for the first time, false otherwise.
	 */
	private boolean firstCycle;
	
	/**
	 * Base constructor.
	 */
	public RateCalculatorSMA()
	{
		lastIterations = new int[MAX_ITERATIONS];
		sumIterations = 0;
		currentIndex = 0;
		firstCycle = true;
	}
	
	@Override
	public float getRate(int lastElapsedMillis)
	{
		sumIterations -= lastIterations[currentIndex];
		sumIterations += lastElapsedMillis;
		lastIterations[currentIndex] = lastElapsedMillis;
		currentIndex++;
		
		if (currentIndex == MAX_ITERATIONS)
		{
			currentIndex = 0;
			firstCycle = false;
		}
		
		return 1000F / ((float)sumIterations / (float)(firstCycle ? currentIndex : MAX_ITERATIONS));
	}
	
}
