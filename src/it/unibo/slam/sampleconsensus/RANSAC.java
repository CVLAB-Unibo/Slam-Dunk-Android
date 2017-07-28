package it.unibo.slam.sampleconsensus;

import java.util.ArrayList;
import java.util.List;

import it.unibo.slam.datatypes.Pair;
import it.unibo.slam.datatypes.eigen.typedouble.EigenIsometry3D;
import it.unibo.slam.datatypes.eigen.typedouble.EigenMatrix4D;
import it.unibo.slam.datatypes.eigen.typedouble.EigenVector4D;
import it.unibo.slam.datatypes.eigen.typefloat.EigenVector3F;
import it.unibo.slam.datatypes.eigen.typefloat.EigenVector4F;
import it.unibo.slam.random.RandomNative;
import it.unibo.slam.sampleconsensus.abstracts.SampleConsensus;

/**
 * Implementation of the RANSAC algorithm.
 */
public class RANSAC extends SampleConsensus
{
	/**
	 * Seed used in the deterministic execution.
	 */
	private static long DETERMINISTIC_SEED = 42L;
	
	//protected Random randomNumberGenerator;
	
	/**
	 * Random number generator.
	 */
	protected RandomNative randomNumberGenerator;
	
	/**
	 * If true the random number generator will be always initialized with the same seed.
	 */
	protected boolean deterministic;
	
	/**
	 * Constructor with default threshold (0.05).
	 * @param deterministic True for deterministic random generation, false otherwise.
	 */
	public RANSAC(boolean deterministic)
	{
		this(deterministic, 0.05F);
	}
	
	/**
	 * Base constructor.
	 * @param deterministic True for deterministic random generation, false otherwise.
	 * @param threshold The threshold.
	 */
	public RANSAC(boolean deterministic, float threshold)
	{
		super(threshold);
		this.deterministic = deterministic;
		randomNumberGenerator = new RandomNative();
		if (!deterministic)
			randomNumberGenerator.seedRandom();
		/*if (!deterministic)
			randomNumberGenerator = new Random();*/
	}
	
	@Override
	public boolean findInliers(	EigenVector3F[] reference, EigenVector3F[] query,
								Pair<Integer, Integer>[] referenceQueryIds)
	{
		int numberOfMatches = referenceQueryIds.length;
		
		if (numberOfMatches < 3)
		{
			inlierIndices.clear();
			return false;
		}
		
		if (deterministic)
			randomNumberGenerator.seed(DETERMINISTIC_SEED);
		/*if (deterministic)
			randomNumberGenerator = new Random(DETERMINISTIC_SEED);*/
		
		randomNumberGenerator.initUniformGenerator(0, numberOfMatches - 1);
		
		int a, b, c;
		int bestScore = 0;
		short currentMaxIterations = maxIterations;
		
		double numberOfMatchesInverse = 1.0 / (double)numberOfMatches;
		double logNum = Math.log(1.0 - probability);
		
		//System.out.println("RANSAC MATCHES NUMBER: " + numberOfMatches);
		
		short it = 0;
		for (it = 0; it < currentMaxIterations; it++)
		{
			a = randomNumberGenerator.nextInt();
			do 
				b = randomNumberGenerator.nextInt();
			while (a == b);
			do
				c = randomNumberGenerator.nextInt();
			while (a == c || b == c);
			
			//System.out.println("RANSAC IDS: (" + a + ", " + b + ", " + c + ")");
			
			/*a = randomNumberGenerator.nextInt(numberOfMatches);
			do 
				b = randomNumberGenerator.nextInt(numberOfMatches);
			while (a == b);
			do
				c = randomNumberGenerator.nextInt(numberOfMatches);
			while (a == c || b == c);*/
			
			double[] ref1 = reference[referenceQueryIds[a].getFirst()].toDouble().getValue();
			double[] ref2 = reference[referenceQueryIds[b].getFirst()].toDouble().getValue();
			double[] ref3 = reference[referenceQueryIds[c].getFirst()].toDouble().getValue();
			double[] query1 = query[referenceQueryIds[a].getSecond()].toDouble().getValue();
			double[] query2 = query[referenceQueryIds[b].getSecond()].toDouble().getValue();
			double[] query3 = query[referenceQueryIds[c].getSecond()].toDouble().getValue();
			
			/*System.out.println("REF1: (" + ref1[0] + ", " + ref1[1] + ", " + ref1[2] + ")");
			System.out.println("REF2: (" + ref2[0] + ", " + ref2[1] + ", " + ref2[2] + ")");
			System.out.println("REF3: (" + ref3[0] + ", " + ref3[1] + ", " + ref3[2] + ")");
			System.out.println("QUERY1: (" + query1[0] + ", " + query1[1] + ", " + query1[2] + ")");
			System.out.println("QUERY2: (" + query2[0] + ", " + query2[1] + ", " + query2[2] + ")");
			System.out.println("QUERY3: (" + query3[0] + ", " + query3[1] + ", " + query3[2] + ")");*/
			
			EigenIsometry3D currentTransformation = EigenIsometry3D.getIdentity();
			currentTransformation.setMatrix(new EigenMatrix4D(calculateCurrentTransformation(ref1, ref2, ref3, query1, query2, query3)));
			
			int currentScore = 0;
			List<Integer> currentInliers = new ArrayList<Integer>();
			EigenVector3F tempVector3Query;
			EigenVector4F tempVector4Query;
			EigenVector3F tempVector3Ref;
			EigenVector4D tempVector4RefD;
			EigenVector4D vector4 = new EigenVector4D();
			for (int rqId = 0; rqId < numberOfMatches; rqId++)
			{
				tempVector3Query = query[referenceQueryIds[rqId].getSecond()];
				tempVector4Query = new EigenVector4F(tempVector3Query.getX(), tempVector3Query.getY(), tempVector3Query.getZ(), 1);
				currentTransformation.getMatrix().multiplyWith(tempVector4Query, vector4);
				tempVector3Ref = reference[referenceQueryIds[rqId].getFirst()];
				tempVector4RefD = new EigenVector4D(tempVector3Ref.getX(), tempVector3Ref.getY(), tempVector3Ref.getZ(), 1);
				vector4.sub(tempVector4RefD);
				
				if (vector4.squaredLength() <= squaredThreshold)
				{
					currentScore++;
					currentInliers.add(rqId);
				}
			}
			
			if (currentScore > bestScore)
			{
				bestScore = currentScore;
				queryToReference = currentTransformation;
				inlierIndices = currentInliers;
				
				if (bestScore == numberOfMatches)
					currentMaxIterations = 0;
				else
					currentMaxIterations = (short)Math.min((double)maxIterations, Math.ceil(logNum / 
							Math.log(1.0 - Math.pow((double)bestScore * numberOfMatchesInverse, 3.0))));
			}
		}
		
		if (bestScore < 4)
		{
			inlierIndices.clear();
			return false;
		}
		
		return true;
	}
	
	private native double[] calculateCurrentTransformation(	double[] ref1, double[] ref2, double ref3[],
															double[] query1, double[] query2, double[] query3);
}
