package it.unibo.slam.utils;

public final class ImageUtils
{
	/**
	 * Converts the image pixels from the YUV NV21 format to the BGR format.
	 * @param yuv YUV NV21 format pixels.
	 * @param bgr The resulting BGR format pixels
	 * @param width The image width.
	 * @param height The image height.
	 */
	public static final void convertNV21toBGRJava(byte[] yuv, byte[] bgr, int width, int height)
	{
	    final int frameSize = width * height;

	    int y, u, v, r, g, b;
	    
	    for (int i = 0, ci = 0, a = 0; i < height; i++, ci += width)
	    {
	        for (int j = 0; j < width; j++, a += 3)
	        {
	            y = (0xff & ((int) yuv[ci + j]));
	            v = (0xff & ((int) yuv[frameSize + (i >> 1) * width + (j & ~1) + 0]));
	            u = (0xff & ((int) yuv[frameSize + (i >> 1) * width + (j & ~1) + 1]));
	            y = y < 16 ? 16 : y;
	
	            r = (int) (1.164f * (y - 16) + 1.596f * (v - 128));
	            g = (int) (1.164f * (y - 16) - 0.813f * (v - 128) - 0.391f * (u - 128));
	            b = (int) (1.164f * (y - 16) + 2.018f * (u - 128));
	
	            r = r < 0 ? 0 : (r > 255 ? 255 : r);
	            g = g < 0 ? 0 : (g > 255 ? 255 : g);
	            b = b < 0 ? 0 : (b > 255 ? 255 : b);
	
	            bgr[a] = (byte)b;
	            bgr[a + 1] = (byte)g;
	            bgr[a + 2] = (byte)r;
	        }
	    }
	}
	
	/**
	 * Converts the image pixels from the YUV NV21 format to the BGR format (native C code).
	 * @param yuv YUV NV21 format pixels.
	 * @param bgr The resulting BGR format pixels
	 * @param width The image width.
	 * @param height The image height.
	 */
	public static final native void convertNV21toBGRNativeC(byte[] yuv, byte[] bgr, int width, int height);
	
	/**
	 * Converts the image pixels from the YUV NV21 format to the BGR format (native NEON code).
	 * @param yuv YUV NV21 format pixels.
	 * @param bgr The resulting BGR format pixels
	 * @param width The image width.
	 * @param height The image height.
	 */
	public static final native void convertNV21toBGRNativeNEON(byte[] yuv, byte[] bgr, int width, int height);
	
}
