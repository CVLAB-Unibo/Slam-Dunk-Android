#include <jni.h>
#include <android/log.h>

#include <stdio.h>
#include <stdlib.h>
#include <math.h>

#include <ctime>

#include <boost/random/mersenne_twister.hpp>
#include <boost/random/variate_generator.hpp>
#include <boost/random/uniform_int.hpp>
#include <boost/scoped_ptr.hpp>

using namespace std;

#define  LOG_TAG	"Random"
#define  LOGI(...)	__android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define  LOGE(...)	__android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

extern "C"
{
	JNIEXPORT void JNICALL Java_it_unibo_slam_random_RandomNative_initRandomSeed(JNIEnv *env, jobject obj);
	JNIEXPORT void JNICALL Java_it_unibo_slam_random_RandomNative_initSeed(JNIEnv *env, jobject obj,
			jlong seed);
	JNIEXPORT void JNICALL Java_it_unibo_slam_random_RandomNative_initUniform(JNIEnv *env, jobject obj,
			jint start, jint end);
	JNIEXPORT jint JNICALL Java_it_unibo_slam_random_RandomNative_next(JNIEnv *env, jobject obj);
}

// Global variables accessible only within this source file
namespace
{
	boost::mt19937 randomEngine;
	boost::scoped_ptr< boost::variate_generator< boost::mt19937&, boost::uniform_int<unsigned> > > randomGeneratorPtr;
}

JNIEXPORT void JNICALL Java_it_unibo_slam_random_RandomNative_initRandomSeed(JNIEnv *env, jobject obj)
{
	randomEngine.seed(static_cast<unsigned>(std::time(NULL)));
}

JNIEXPORT void JNICALL Java_it_unibo_slam_random_RandomNative_initSeed(JNIEnv *env, jobject obj,
		jlong seed)
{
	randomEngine.seed(seed);
}

JNIEXPORT void JNICALL Java_it_unibo_slam_random_RandomNative_initUniform(JNIEnv *env, jobject obj,
		jint start, jint end)
{
	randomGeneratorPtr.reset(new boost::variate_generator< boost::mt19937&, boost::uniform_int<unsigned> >
			(randomEngine, boost::uniform_int<unsigned>(start, end)));
}

JNIEXPORT jint JNICALL Java_it_unibo_slam_random_RandomNative_next(JNIEnv *env, jobject obj)
{
	return (*randomGeneratorPtr)();
}
