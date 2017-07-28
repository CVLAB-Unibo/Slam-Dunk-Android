package it.unibo.slam.datatypes.eigen.typedouble;

public class EigenIsometry3D
{
	private EigenMatrix4D isometryMatrix;
	
	public EigenIsometry3D()
	{
		this(new EigenMatrix4D());
	}
	
	public EigenIsometry3D(EigenMatrix4D isometryMatrix)
	{
		this.isometryMatrix = isometryMatrix;
	}
	
	public EigenMatrix4D getMatrix()
	{
		return isometryMatrix;
	}
	
	public void setMatrix(EigenMatrix4D isometryMatrix)
	{
		this.isometryMatrix = isometryMatrix;
	}
	
	public EigenMatrix3D getRotation()
	{
		double[] matrix = isometryMatrix.getValue();
		double[] rotation = new double[9];
		
		for (int i = 0, k = 0; i < 9; i += 3, k += 4)
			for (int j = 0; j < 3; j++)
				rotation[i + j] = matrix[j + k];
		
		return new EigenMatrix3D(rotation);
	}
	
	public void setRotation(EigenMatrix3D rotation)
	{
		double[] matrix = isometryMatrix.getValue();
		double[] rotationArray = rotation.getValue();
		
		for (int i = 0, k = 0; i < 9; i += 3, k += 4)
			for (int j = 0; j < 3; j++)
				matrix[j + k] = rotationArray[i + j];
	}
	
	public EigenVector3D getTranslation()
	{
		double[] matrix = isometryMatrix.getValue();
		double[] translation = new double[] { matrix[12], matrix[13], matrix[14] };
		return new EigenVector3D(translation);
	}
	
	public void setTranslation(EigenVector3D translation)
	{
		double[] matrix = isometryMatrix.getValue();
		double[] translationArray = translation.getValue();
		System.arraycopy(translationArray, 0, matrix, 12, 3);
	}
	
	public static EigenIsometry3D getIdentity()
	{
		return new EigenIsometry3D(EigenMatrix4D.getIdentity());
	}
}
