package it.unibo.slam.jni;

/**
 * Class representing the native library. Its only purpose is to load the library before the first usage.
 */
public class JniLib
{
	/**
	 * Loads the native library.<br>
	 * Execute this method before the first native call.
	 */
	public static void loadLibrary()
	{
		System.loadLibrary("gnustl_shared");
		System.loadLibrary("SLAM");
	}
}