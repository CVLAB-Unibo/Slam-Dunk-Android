package it.unibo.slam.random;

/**
 * Native random (C++ implementation using boost random generator).
 */
public class RandomNative
{
	/**
	 * Base constructor (empty).
	 */
	public RandomNative()
	{
		
	}
	
	/**
	 * Initializes the random generator with a random seed.
	 */
	public void seedRandom()
	{
		initRandomSeed();
	}
	
	/**
	 * Initializes the random generator with a specific seed.
	 * @param seed The seed.
	 */
	public void seed(long seed)
	{
		initSeed(seed);
	}
	
	/**
	 * Initializes a uniform generator.
	 * @param start Start value of the distribution.
	 * @param end End value of the distribution.
	 */
	public void initUniformGenerator(int start, int end)
	{
		initUniform(start, end);
	}
	
	/**
	 * Gets the next random integer.
	 * @return A random integer.
	 */
	public int nextInt()
	{
		return next();
	}
	
	/**
	 * Native initialization with random seed.
	 */
	private native void initRandomSeed();
	
	/**
	 * Native initialization with specific seed.
	 * @param seed The seed.
	 */
	private native void initSeed(long seed);
	
	/**
	 * Native initialization of the uniform generator.
	 * @param start Start value of the distribution.
	 * @param end End value of the distribution.
	 */
	private native void initUniform(int start, int end);
	
	/**
	 * Native method that returns the next random integer.
	 * @return A random integer.
	 */
	private native int next();
}
