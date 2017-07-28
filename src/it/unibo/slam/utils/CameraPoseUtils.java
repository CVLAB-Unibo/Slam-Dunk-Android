package it.unibo.slam.utils;

import org.opencv.core.Mat;

import it.unibo.slam.datatypes.eigen.typedouble.EigenMatrix3D;
import it.unibo.slam.datatypes.eigen.typedouble.EigenVector3D;

public class CameraPoseUtils
{
	public static boolean estimateLocalObjectPose(	Mat rgbImage, int chessboardWidth, int chessboardHeight, 
													EigenVector3D translation, EigenMatrix3D rotation)
	{
		return estimateLocalObjectPoseNative(	rgbImage.nativeObj, chessboardWidth, chessboardHeight, 
												translation.getValue(), rotation.getValue());
	}
	
	public static void estimateWorldCameraPose(	EigenVector3D worldTranslationVector, EigenMatrix3D worldRotationMatrix,
												EigenVector3D initialTranslationVector, EigenMatrix3D initialRotationMatrix,
												EigenVector3D finalTranslationVector, EigenMatrix3D finalRotationMatrix)
	{
		estimateWorldCameraPoseNative(	worldTranslationVector.getValue(), worldRotationMatrix.getValue(),
										initialTranslationVector.getValue(), initialRotationMatrix.getValue(),
										finalTranslationVector.getValue(), finalRotationMatrix.getValue());
	}
	
	private static native boolean estimateLocalObjectPoseNative(long rgbImage, int chessboardWidth, int chessboardHeight,
																double[] translationVector, double[] rotationVector);
	
	private static native void estimateWorldCameraPoseNative(	double[] worldTranslationVector, double[] worldRotationMatrix,
																double[] initialTranslationVector, double[] initialRotationMatrix,
																double[] finalTranslationVector, double[] finalRotationMatrix);
}
