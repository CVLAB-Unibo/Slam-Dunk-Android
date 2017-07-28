package it.unibo.slam.sampleconsensus.abstracts;

import it.unibo.slam.datatypes.Pair;
import it.unibo.slam.datatypes.eigen.typedouble.EigenIsometry3D;
import it.unibo.slam.datatypes.eigen.typedouble.EigenMatrix3D;
import it.unibo.slam.datatypes.eigen.typedouble.EigenMatrix4D;
import it.unibo.slam.datatypes.eigen.typefloat.EigenVector3F;

import java.util.ArrayList;
import java.util.List;

public abstract class SampleConsensus
{
	protected short maxIterations;
	
	protected double squaredThreshold;
	
	protected double probability;
	
	protected List<Integer> inlierIndices;
	
	protected EigenIsometry3D queryToReference;
	
	public SampleConsensus()
	{
		this(0.05);
	}
	
	public SampleConsensus(double threshold)
	{
		maxIterations = 1000;
		squaredThreshold = threshold * threshold;
		probability = 0.99;
		inlierIndices = new ArrayList<Integer>();
		queryToReference = EigenIsometry3D.getIdentity();
	}
	
	public short getMaxIterations()
	{
		return maxIterations;
	}
	
	public void setMaxIterations(short maxIterations)
	{
		this.maxIterations = maxIterations;
	}
	
	public double getSquaredDistanceThreshold()
	{
		return squaredThreshold;
	}
	
	public void setDistanceThreshold(double threshold)
	{
		squaredThreshold = threshold * threshold;
	}
	
	public double getProbability()
	{
		return probability;
	}
	
	public void setProbability(double probability)
	{
		if (probability >=0 && probability < 1)
			this.probability = probability;
	}
	
	public List<Integer> getReferenceQueryInliers()
	{
		return inlierIndices;
	}
	
	public EigenIsometry3D getQueryToReference()
	{
		return queryToReference;
	}
	
	public abstract boolean findInliers(EigenVector3F[] reference, EigenVector3F[] query, 
										Pair<Integer, Integer>[] referenceQueryIds);
	
	public static void estimateTransformationSVD(	EigenVector3F[] reference, EigenVector3F[] query, 
													Pair<Integer, Integer>[] referenceQueryIds,
													EigenIsometry3D queryToReference)
	{
		int numberOfMatches = referenceQueryIds.length;
		
		EigenVector3F meanReference = new EigenVector3F(0, 0, 0);
		EigenVector3F meanQuery = new EigenVector3F(0, 0, 0);
		for (int rqId = 0; rqId < numberOfMatches; rqId++)
		{
			meanReference.add(reference[referenceQueryIds[rqId].getFirst()]);
			meanQuery.add(query[referenceQueryIds[rqId].getSecond()]);
		}
		float numberOfMatchesDiv = 1F / (float)numberOfMatches;
		meanReference.multiplyScalar(numberOfMatchesDiv);
		meanQuery.multiplyScalar(numberOfMatchesDiv);
		
		EigenMatrix3D H = new EigenMatrix3D();
		for (int rqId = 0; rqId < numberOfMatches; rqId++)
		{
			H.add(	query[referenceQueryIds[rqId].getSecond()].subExternal(meanQuery).toDouble().multiplyWith(
					reference[referenceQueryIds[rqId].getFirst()].subExternal(meanReference).toDouble().transpose()));
		}
		
		double[] qtfMatrix = calculateMeanTransformation(	H.getValue(), meanReference.toDouble().getValue(), 
															meanQuery.toDouble().getValue());
		queryToReference.setMatrix(new EigenMatrix4D(qtfMatrix));
	}
	
	private static native double[] calculateMeanTransformation(double[] H, double[] meanReference, double[] meanQuery);
}