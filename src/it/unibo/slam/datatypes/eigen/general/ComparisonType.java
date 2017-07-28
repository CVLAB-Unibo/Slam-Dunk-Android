package it.unibo.slam.datatypes.eigen.general;

/**
 * Enumerative expressing the type of comparison used when a multiplicity of elements are compared.
 */
public enum ComparisonType
{
	/**
	 * At least one element has to respect the comparison.
	 */
	ANY,
	
	/**
	 * All the elements have to respect the comparison.
	 */
	ALL;
}
