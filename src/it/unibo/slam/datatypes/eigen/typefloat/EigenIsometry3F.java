package it.unibo.slam.datatypes.eigen.typefloat;

public class EigenIsometry3F
{
	private EigenMatrix4F isometryMatrix;
	
	public EigenIsometry3F(EigenMatrix4F isometryMatrix)
	{
		this.isometryMatrix = isometryMatrix;
	}
	
	public EigenMatrix4F getMatrix()
	{
		return isometryMatrix;
	}
	
	public void setMatrix(EigenMatrix4F isometryMatrix)
	{
		this.isometryMatrix = isometryMatrix;
	}
	
	public EigenMatrix3F getRotation()
	{
		float[] matrix = isometryMatrix.getValue();
		float[] rotation = new float[9];
		
		for (int i = 0, k = 0; i < 9; i += 3, k += 4)
			for (int j = 0; j < 3; j++)
				rotation[i + j] = matrix[j + k];
		
		return new EigenMatrix3F(rotation);
	}
	
	public void setRotation(EigenMatrix3F rotation)
	{
		float[] matrix = isometryMatrix.getValue();
		float[] rotationArray = rotation.getValue();
		
		for (int i = 0, k = 0; i < 9; i += 3, k += 4)
			for (int j = 0; j < 3; j++)
				matrix[j + k] = rotationArray[i + j];
	}
	
	public EigenVector3F getTranslation()
	{
		float[] matrix = isometryMatrix.getValue();
		float[] translation = new float[] { matrix[12], matrix[13], matrix[14] };
		return new EigenVector3F(translation);
	}
	
	public void setTranslation(EigenVector3F translation)
	{
		float[] matrix = isometryMatrix.getValue();
		float[] translationArray = translation.getValue();
		System.arraycopy(translationArray, 0, matrix, 12, 3);
	}
	
	public static EigenIsometry3F getIdentity()
	{
		return new EigenIsometry3F(EigenMatrix4F.getIdentity());
	}
}
