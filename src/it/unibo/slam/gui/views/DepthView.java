package it.unibo.slam.gui.views;

import it.unibo.slam.buffers.interfaces.ReadOnlyDoubleBuffer;
import it.unibo.slam.datatypes.DataGrayscale;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;

public class DepthView extends View
{
    int w = 640;
	int h = 480;
	
	private Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
	
	private int[] drawPixels = new int[w * h];
	
	private ReadOnlyDoubleBuffer<DataGrayscale> doubleBuffer;
	
	public void setBitmap(Bitmap bitmap)
	{
		this.bitmap = bitmap;
	}
	
	public void setDoubleBuffer(ReadOnlyDoubleBuffer<DataGrayscale> doubleBuffer)
	{
		this.doubleBuffer = doubleBuffer;
	}
	
    public DepthView(Context context)
    {
        super(context);
    }
 
    public DepthView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

	@Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        
        if (doubleBuffer == null || !doubleBuffer.isDataValid())
        {
        	canvas.drawBitmap(bitmap, 0, 0, null);
        	invalidate();
        	return;
        }
        
        DataGrayscale data = doubleBuffer.readData();
		byte[] pixelsB = data.getGrayscale();
		
		if (w != data.getWidth() || h != data.getHeight())
		{
			w = data.getWidth();
			h = data.getHeight();
			bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
			drawPixels = new int[w * h];
		}
		
		for (int i = 0; i < w * h; i++)
			drawPixels[i] = Color.rgb(pixelsB[i] & 0xFF, pixelsB[i] & 0xFF, pixelsB[i] & 0xFF);
		bitmap.setPixels(drawPixels, 0, w, 0, 0, w, h);
		
		canvas.drawBitmap(bitmap, 0, 0, null);
		invalidate();
		
		doubleBuffer.notifyReadFinished();
    }
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) 
	{
		setMeasuredDimension(w, h);
	}
}
