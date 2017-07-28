package it.unibo.slam.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.view.Surface;
import android.view.WindowManager;

/**
 * Enumerative representing the natural orientation of the device.
 */
public enum DeviceNaturalOrientation
{
	/**
	 * Landscape natural orientation.
	 */
	LANDSCAPE, 
	
	/**
	 * Portrait natural orientation.
	 */
	PORTRAIT;
	
	/**
	 * Gets the natural orientation of the device.
	 * @param context The Android context.
	 * @return Enumerative expressing the orientation.
	 */
	public static DeviceNaturalOrientation getNaturalOrientation(Context context)
	{
		WindowManager windowManager =  (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
	    Configuration config = context.getResources().getConfiguration();
		int rotation = windowManager.getDefaultDisplay().getRotation();

	    if (((	rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) && config.orientation == Configuration.ORIENTATION_LANDSCAPE) || 
	    		((rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) && config.orientation == Configuration.ORIENTATION_PORTRAIT))
	    {
	    	return LANDSCAPE;
	    }
	    else 
	    {
	    	return PORTRAIT;
	    }
	}
}
