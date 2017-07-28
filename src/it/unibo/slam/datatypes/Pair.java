package it.unibo.slam.datatypes;

/**
 * Class representing a pair of values.
 * @param <F> First value.
 * @param <S> Second value.
 */
public class Pair<F, S>
{
	/**
	 * First value.
	 */
	private F first;
	
	/**
	 * Second value.
	 */
	private S second;
	
	/**
	 * Base constructor.
	 * @param first The first value.
	 * @param second The second value.
	 */
	public Pair(F first, S second)
	{
		this.first = first;
		this.second = second;
	}
	
	/**
	 * Gets the first value.
	 * @return The fist value.
	 */
	public F getFirst()
	{
		return first;
	}
	
	/**
	 * Sets the first value.
	 * @param first The first value to set.
	 */
	public void setFirst(F first)
	{
		this.first = first;
	}
	
	/**
	 * Gets the second value.
	 * @return The second value.
	 */
	public S getSecond()
	{
		return second;
	}
	
	/**
	 * Sets the second value.
	 * @param second The second value to set.
	 */
	public void setSecond(S second)
	{
		this.second = second;
	}
}
