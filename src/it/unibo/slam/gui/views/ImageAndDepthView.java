package it.unibo.slam.gui.views;

import it.unibo.slam.buffers.interfaces.ReadOnlyDoubleBuffer;
import it.unibo.slam.datatypes.DataBGR;
import it.unibo.slam.datatypes.DataGrayscale;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;

public class ImageAndDepthView extends View
{
	private int w = 640;
	private int h = 480;
	
	private Bitmap bitmapDepth = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
	private Bitmap bitmapImage = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
	
	private int[] drawPixelsDepth = new int[w * h];
	private int[] drawPixelsImage = new int[w * h];
	
	private ReadOnlyDoubleBuffer<DataGrayscale> doubleBufferGrayscale;
	private ReadOnlyDoubleBuffer<DataBGR> doubleBufferBGR;
	
	public void setDoubleBuffers(ReadOnlyDoubleBuffer<DataGrayscale> doubleBufferGrayscale, ReadOnlyDoubleBuffer<DataBGR> doubleBufferBGR)
	{
		this.doubleBufferBGR = doubleBufferBGR;
		this.doubleBufferGrayscale = doubleBufferGrayscale;
	}
	
    public ImageAndDepthView(Context context)
    {
        super(context);
    }
 
    public ImageAndDepthView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

	@Override
    protected void onDraw(Canvas canvas)
    {
		super.onDraw(canvas);
		
        if (doubleBufferGrayscale == null || !doubleBufferGrayscale.isDataValid() ||
        	doubleBufferBGR == null || !doubleBufferBGR.isDataValid())
        {
        	canvas.drawBitmap(bitmapDepth, 0, 0, null);
        	canvas.drawBitmap(bitmapImage, 0, h, null);
        	invalidate();
        	return;
        }
        
        DataBGR dataBGR = doubleBufferBGR.readData();
		byte[] pixelsBGR = dataBGR.getBGR();
		DataGrayscale dataGrayscale = doubleBufferGrayscale.readData();
		byte[] pixelsGrayscale = dataGrayscale.getGrayscale();
		
		if (w != dataBGR.getWidth() || h != dataBGR.getHeight() ||
			w != dataGrayscale.getWidth() || h != dataGrayscale.getHeight())
		{
			w = dataBGR.getWidth();
			h = dataBGR.getHeight();
			bitmapImage = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
			drawPixelsImage = new int[w * h];
			bitmapDepth = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
			drawPixelsDepth = new int[w * h];
		}
		
		for (int i = 0, j = 0; i < w * h; i++, j += 3)
		{
			drawPixelsImage[i] = Color.rgb(pixelsBGR[j + 2] & 0xFF, pixelsBGR[j + 1] & 0xFF, pixelsBGR[j] & 0xFF);
			drawPixelsDepth[i] = Color.rgb(pixelsGrayscale[i] & 0xFF, pixelsGrayscale[i] & 0xFF, pixelsGrayscale[i] & 0xFF);
		}
		bitmapImage.setPixels(drawPixelsImage, 0, w, 0, 0, w, h);
		bitmapDepth.setPixels(drawPixelsDepth, 0, w, 0, 0, w, h);
		
		canvas.drawBitmap(bitmapDepth, 0, 0, null);
		canvas.drawBitmap(bitmapImage, 0, h, null);
		invalidate();
		
		doubleBufferBGR.notifyReadFinished();
		doubleBufferGrayscale.notifyReadFinished();
		
		return;
    }
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) 
	{
		setMeasuredDimension(w, 2 * h);
	}
}
