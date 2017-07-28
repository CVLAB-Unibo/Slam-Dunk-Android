#include <jni.h>
#include <android/log.h>

#include <cpu-features.h>

#include <stdio.h>
#include <stdlib.h>
#include <math.h>

#include <Eigen/Core>

using namespace std;

#define  LOG_TAG	"MatrixOp"
#define  LOGI(...)	__android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define  LOGE(...)	__android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

template <typename T1, typename T2> void c_mulVector(T1 matrix[], T2 vector[], int rows, int cols, T1 result[]);
//void arm_neon_mulVector3F(float matrix[], float vector[], float result[]);
template <typename T1, typename T2> void c_mulMatrix(T1 matrix1[], T2 matrix2[], int rows1, int cols1, int cols2, T1 result[]);
//void arm_neon_mulMatrix3F(float matrix1[], float matrix2[], float result[]);

//bool neon = (android_getCpuFamily() == ANDROID_CPU_FAMILY_ARM) && ((android_getCpuFeatures() & ANDROID_CPU_ARM_FEATURE_NEON) != 0);

extern "C"
{
	JNIEXPORT jfloatArray JNICALL Java_it_unibo_slam_datatypes_eigen_typefloat_EigenMatrixF_matrixMulVectorF(JNIEnv *env, jobject obj,
			jfloatArray m, jfloatArray v, jint numRowsMatrix, jint numColsMatrix);
	JNIEXPORT jfloatArray JNICALL Java_it_unibo_slam_datatypes_eigen_typefloat_EigenMatrixF_matrixMulMatrixF(JNIEnv *env, jobject obj,
			jfloatArray m1, jfloatArray m2, jint numRowsMatrix1, jint numColsMatrix1, jint numColsMatrix2);
	JNIEXPORT jfloatArray JNICALL Java_it_unibo_slam_datatypes_eigen_typefloat_EigenMatrixF_matrixMulVectorFD(JNIEnv *env, jobject obj,
			jfloatArray m, jdoubleArray v, jint numRowsMatrix, jint numColsMatrix);
	JNIEXPORT jfloatArray JNICALL Java_it_unibo_slam_datatypes_eigen_typefloat_EigenMatrixF_matrixMulMatrixFD(JNIEnv *env, jobject obj,
			jfloatArray m1, jdoubleArray m2, jint numRowsMatrix1, jint numColsMatrix1, jint numColsMatrix2);
	JNIEXPORT jdoubleArray JNICALL Java_it_unibo_slam_datatypes_eigen_typedouble_EigenMatrixD_matrixMulVectorD(JNIEnv *env, jobject obj,
			jdoubleArray m, jdoubleArray v, jint numRowsMatrix, jint numColsMatrix);
	JNIEXPORT jdoubleArray JNICALL Java_it_unibo_slam_datatypes_eigen_typedouble_EigenMatrixD_matrixMulMatrixD(JNIEnv *env, jobject obj,
			jdoubleArray m1, jdoubleArray m2, jint numRowsMatrix1, jint numColsMatrix1, jint numColsMatrix2);
	JNIEXPORT jdoubleArray JNICALL Java_it_unibo_slam_datatypes_eigen_typedouble_EigenMatrixD_matrixMulVectorDF(JNIEnv *env, jobject obj,
			jdoubleArray m, jfloatArray v, jint numRowsMatrix, jint numColsMatrix);
	JNIEXPORT jdoubleArray JNICALL Java_it_unibo_slam_datatypes_eigen_typedouble_EigenMatrixD_matrixMulMatrixDF(JNIEnv *env, jobject obj,
			jdoubleArray m1, jfloatArray m2, jint numRowsMatrix1, jint numColsMatrix1, jint numColsMatrix2);
}

JNIEXPORT jfloatArray JNICALL Java_it_unibo_slam_datatypes_eigen_typefloat_EigenMatrixF_matrixMulVectorF(JNIEnv *env, jobject obj,
		jfloatArray m, jfloatArray v, jint numRowsMatrix, jint numColsMatrix)
{
	jfloatArray result;
	int rows = numRowsMatrix;
	int cols = numColsMatrix;
	int sizeMatrix = rows * cols;
	result = env->NewFloatArray(rows);

	float *tempResult = new float[rows];
	float *tempM = new float[sizeMatrix];
	float *tempV = new float[cols];

	env->GetFloatArrayRegion(m, 0, sizeMatrix, tempM);
	env->GetFloatArrayRegion(v, 0, cols, tempV);

	c_mulVector<float, float>(tempM, tempV, rows, cols, tempResult);

	env->SetFloatArrayRegion(result, 0, rows, tempResult);

	delete [] tempResult;
	delete [] tempM;
	delete [] tempV;

	return result;
}

JNIEXPORT jfloatArray JNICALL Java_it_unibo_slam_datatypes_eigen_typefloat_EigenMatrixF_matrixMulMatrixF(JNIEnv *env, jobject obj,
		jfloatArray m1, jfloatArray m2, jint numRowsMatrix1, jint numColsMatrix1, jint numColsMatrix2)
{
	jfloatArray result;
	int rows1 = numRowsMatrix1;
	int cols1 = numColsMatrix1;
	int cols2 = numColsMatrix2;
	int sizeMatrix1 = rows1 * cols1;
	int sizeMatrix2 = cols1 * cols2;
	int sizeResult = rows1 * cols2;
	result = env->NewFloatArray(sizeResult);

	float *tempResult = new float[sizeResult];
	float *tempM1 = new float[sizeMatrix1];
	float *tempM2 = new float[sizeMatrix2];

	env->GetFloatArrayRegion(m1, 0, sizeMatrix1, tempM1);
	env->GetFloatArrayRegion(m2, 0, sizeMatrix2, tempM2);

	c_mulMatrix<float, float>(tempM1, tempM2, rows1, cols1, cols2, tempResult);

	env->SetFloatArrayRegion(result, 0, sizeResult, tempResult);

	delete [] tempResult;
	delete [] tempM1;
	delete [] tempM2;

	return result;
}

JNIEXPORT jfloatArray JNICALL Java_it_unibo_slam_datatypes_eigen_typefloat_EigenMatrixF_matrixMulVectorFD(JNIEnv *env, jobject obj,
		jfloatArray m, jdoubleArray v, jint numRowsMatrix, jint numColsMatrix)
{
	jfloatArray result;
	int rows = numRowsMatrix;
	int cols = numColsMatrix;
	int sizeMatrix = rows * cols;
	result = env->NewFloatArray(rows);

	float *tempResult = new float[rows];
	float *tempM = new float[sizeMatrix];
	double *tempV = new double[cols];

	env->GetFloatArrayRegion(m, 0, sizeMatrix, tempM);
	env->GetDoubleArrayRegion(v, 0, cols, tempV);

	c_mulVector<float, double>(tempM, tempV, rows, cols, tempResult);

	env->SetFloatArrayRegion(result, 0, rows, tempResult);

	delete [] tempResult;
	delete [] tempM;
	delete [] tempV;

	return result;
}

JNIEXPORT jfloatArray JNICALL Java_it_unibo_slam_datatypes_eigen_typefloat_EigenMatrixF_matrixMulMatrixFD(JNIEnv *env, jobject obj,
		jfloatArray m1, jdoubleArray m2, jint numRowsMatrix1, jint numColsMatrix1, jint numColsMatrix2)
{
	jfloatArray result;
	int rows1 = numRowsMatrix1;
	int cols1 = numColsMatrix1;
	int cols2 = numColsMatrix2;
	int sizeMatrix1 = rows1 * cols1;
	int sizeMatrix2 = cols1 * cols2;
	int sizeResult = rows1 * cols2;
	result = env->NewFloatArray(sizeResult);

	float *tempResult = new float[sizeResult];
	float *tempM1 = new float[sizeMatrix1];
	double *tempM2 = new double[sizeMatrix2];

	env->GetFloatArrayRegion(m1, 0, sizeMatrix1, tempM1);
	env->GetDoubleArrayRegion(m2, 0, sizeMatrix2, tempM2);

	c_mulMatrix<float, double>(tempM1, tempM2, rows1, cols1, cols2, tempResult);

	env->SetFloatArrayRegion(result, 0, sizeResult, tempResult);

	delete [] tempResult;
	delete [] tempM1;
	delete [] tempM2;

	return result;
}

JNIEXPORT jdoubleArray JNICALL Java_it_unibo_slam_datatypes_eigen_typedouble_EigenMatrixD_matrixMulVectorD(JNIEnv *env, jobject obj,
		jdoubleArray m, jdoubleArray v, jint numRowsMatrix, jint numColsMatrix)
{
	jdoubleArray result;
	int rows = numRowsMatrix;
	int cols = numColsMatrix;
	int sizeMatrix = rows * cols;
	result = env->NewDoubleArray(rows);

	double *tempResult = new double[rows];
	double *tempM = new double[sizeMatrix];
	double *tempV = new double[cols];

	env->GetDoubleArrayRegion(m, 0, sizeMatrix, tempM);
	env->GetDoubleArrayRegion(v, 0, cols, tempV);

	c_mulVector<double, double>(tempM, tempV, rows, cols, tempResult);

	env->SetDoubleArrayRegion(result, 0, rows, tempResult);

	delete [] tempResult;
	delete [] tempM;
	delete [] tempV;

	return result;
}

JNIEXPORT jdoubleArray JNICALL Java_it_unibo_slam_datatypes_eigen_typedouble_EigenMatrixD_matrixMulMatrixD(JNIEnv *env, jobject obj,
		jdoubleArray m1, jdoubleArray m2, jint numRowsMatrix1, jint numColsMatrix1, jint numColsMatrix2)
{
	jdoubleArray result;
	int rows1 = numRowsMatrix1;
	int cols1 = numColsMatrix1;
	int cols2 = numColsMatrix2;
	int sizeMatrix1 = rows1 * cols1;
	int sizeMatrix2 = cols1 * cols2;
	int sizeResult = rows1 * cols2;
	result = env->NewDoubleArray(sizeResult);

	double *tempResult = new double[sizeResult];
	double *tempM1 = new double[sizeMatrix1];
	double *tempM2 = new double[sizeMatrix2];

	env->GetDoubleArrayRegion(m1, 0, sizeMatrix1, tempM1);
	env->GetDoubleArrayRegion(m2, 0, sizeMatrix2, tempM2);

	c_mulMatrix<double, double>(tempM1, tempM2, rows1, cols1, cols2, tempResult);

	env->SetDoubleArrayRegion(result, 0, sizeResult, tempResult);

	delete [] tempResult;
	delete [] tempM1;
	delete [] tempM2;

	return result;
}

JNIEXPORT jdoubleArray JNICALL Java_it_unibo_slam_datatypes_eigen_typedouble_EigenMatrixD_matrixMulVectorDF(JNIEnv *env, jobject obj,
		jdoubleArray m, jfloatArray v, jint numRowsMatrix, jint numColsMatrix)
{
	jdoubleArray result;
	int rows = numRowsMatrix;
	int cols = numColsMatrix;
	int sizeMatrix = rows * cols;
	result = env->NewDoubleArray(rows);

	double *tempResult = new double[rows];
	double *tempM = new double[sizeMatrix];
	float *tempV = new float[cols];

	env->GetDoubleArrayRegion(m, 0, sizeMatrix, tempM);
	env->GetFloatArrayRegion(v, 0, cols, tempV);

	c_mulVector<double, float>(tempM, tempV, rows, cols, tempResult);

	env->SetDoubleArrayRegion(result, 0, rows, tempResult);

	delete [] tempResult;
	delete [] tempM;
	delete [] tempV;

	return result;
}

JNIEXPORT jdoubleArray JNICALL Java_it_unibo_slam_datatypes_eigen_typedouble_EigenMatrixD_matrixMulMatrixDF(JNIEnv *env, jobject obj,
		jdoubleArray m1, jfloatArray m2, jint numRowsMatrix1, jint numColsMatrix1, jint numColsMatrix2)
{
	jdoubleArray result;
	int rows1 = numRowsMatrix1;
	int cols1 = numColsMatrix1;
	int cols2 = numColsMatrix2;
	int sizeMatrix1 = rows1 * cols1;
	int sizeMatrix2 = cols1 * cols2;
	int sizeResult = rows1 * cols2;
	result = env->NewDoubleArray(sizeResult);

	double *tempResult = new double[sizeResult];
	double *tempM1 = new double[sizeMatrix1];
	float *tempM2 = new float[sizeMatrix2];

	env->GetDoubleArrayRegion(m1, 0, sizeMatrix1, tempM1);
	env->GetFloatArrayRegion(m2, 0, sizeMatrix2, tempM2);

	c_mulMatrix<double, float>(tempM1, tempM2, rows1, cols1, cols2, tempResult);

	env->SetDoubleArrayRegion(result, 0, sizeResult, tempResult);

	delete [] tempResult;
	delete [] tempM1;
	delete [] tempM2;

	return result;
}

template <typename T1, typename T2>
void c_mulMatrix(T1 matrix1[], T2 matrix2[], int rows1, int cols1, int cols2, T1 result[])
{
	T1 tempVal;

	for (int x = 0, y = 0; x < cols2; x++, y += cols1)
	{
		for (int i = 0; i < rows1; i++, result++)
		{
			tempVal = 0;
			for (int j = 0, k = 0; j < cols1; j++, k += rows1)
				tempVal += matrix1[i + k] * matrix2[j + y];
			*result = tempVal;
		}
	}
}

template <typename T1, typename T2>
void c_mulVector(T1 matrix[], T2 vector[], int rows, int cols, T1 result[])
{
	T1 tempVal;

	for (int i = 0; i < rows; i++)
	{
		tempVal = 0;
		for (int j = 0, k = 0; j < cols; j++, k += rows)
			tempVal += matrix[i + k] * (T1)vector[j];
		result[i] = tempVal;
	}
}

/*void arm_neon_mulVector3F(float matrix[], float vector[], float result[])
{
	int temp;
	asm volatile
	(
		"mov                    %3, #12						\n\t"   //r3 = 12
		"vld1.32                {d0, d1}, [%1]             	\n\t"   //Q0 = v
		"vld1.32                {d2, d3}, [%0], %3     		\n\t"   //Q1 = m
		"vld1.32                {d4, d5}, [%0], %3        	\n\t"   //Q2 = m+12
		"vld1.32                {d6, d7}, [%0], %3         	\n\t"   //Q3 = m+24

		"vmul.f32               q9, q1, d0[0]             	\n\t"   //Q9 = Q1*Q0[0]
		"vmla.f32               q9, q2, d0[1]              	\n\t"   //Q9 += Q2*Q0[1]
		"vmla.f32               q9, q3, d1[0]              	\n\t"   //Q9 += Q3*Q0[2]
		"vmov.f32               q0, q9           			\n\t"   //Q0 = q9

		"vst1.32                d0, [%2]!                	\n\t"   //r2 = D24
		"fsts                   s2, [%2]                 	\n\t"   //r2 = D25[0]

		:
		: "r" (matrix), "r" (vector), "r" (result), "r" (temp)
		: "q0", "q9", "q10", "q11", "q12", "q13", "memory"
	);
}

void arm_neon_mulMatrix3F(float matrix1[], float matrix2[], float result[])
{
	asm volatile
	(
		"vld1.32                {d0, d1}, [%1]!			\n\t"   //q0 = m1
		"vld1.32                {d2, d3}, [%1]!			\n\t"   //q1 = m1+4
		"flds                   s8, [%1]				\n\t"   //q2 = m1+8

		"vld1.32                {d6, d7}, [%0]			\n\t"   //q3[0] = m0
		"add                    %0, %0, #12				\n\t"   //q3[0] = m0
		"vld1.32                {d8, d9}, [%0]			\n\t"   //q4[0] = m0+12
		"add                    %0, %0, #12				\n\t"   //q3[0] = m0
		"vld1.32                {d10}, [%0]				\n\t"   //q5[0] = m0+24
		"add                    %0, %0, #8				\n\t"   //q3[0] = m0
		"flds                   s22, [%0]				\n\t"   //q2 = m1+8

		"vmul.f32               q6, q3, d0[0]			\n\t"   //q12 = q3 * d0[0]
		"vmul.f32               q7, q3, d1[1]			\n\t"   //q13 = q3 * d2[0]
		"vmul.f32               q8, q3, d3[0]			\n\t"   //q14 = q3 * d4[0]
		"vmla.f32               q6, q4, d0[1] 			\n\t"   //q12 = q9 * d0[1]
		"vmla.f32               q7, q4, d2[0]    		\n\t"   //q13 = q9 * d2[1]
		"vmla.f32               q8, q4, d3[1] 			\n\t"   //q14 = q9 * d4[1]
		"vmla.f32               q6, q5, d1[0]			\n\t"   //q12 = q10 * d0[0]
		"vmla.f32               q7, q5, d2[1]			\n\t"   //q13 = q10 * d2[0]
		"vmla.f32               q8, q5, d4[0]			\n\t"   //q14 = q10 * d4[0]

		"vmov.f32               q0, q8					\n\t"   //q14 = q10 * d4[0]
		"vst1.32                {d12, d13}, [%2]		\n\t"   //d = q12
		"add                    %2, %2, #12				\n\t"   //q3[0] = m0
		"vst1.32                {d14, d15}, [%2]		\n\t"   //d+4 = q13
		"add                    %2, %2, #12				\n\t"   //q3[0] = m0
		"vst1.32                {d0}, [%2]				\n\t"   //d+8 = q14
		"add                    %2, %2, #8				\n\t"   //q3[0] = m0
		"fsts                   s2, [%2]				\n\t"   //d = q12

		:
		: "r"(matrix1), "r"(matrix2), "r"(result)
		: "d8", "d9", "d10", "d11", "d12", "d13", "d14", "d15", "memory"
	);
}*/
