package it.unibo.slam.gui.views;

import it.unibo.slam.buffers.interfaces.ReadOnlyDoubleBuffer;
import it.unibo.slam.datatypes.DataBGR;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

public class ImageView extends View
{
	int w = 640;
	int h = 480;
	
	private Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
	
	private int[] drawPixels = new int[w * h];
	
	private ReadOnlyDoubleBuffer<DataBGR> doubleBuffer;
	
	public void setBitmap(Bitmap bitmap)
	{
		this.bitmap = bitmap;
	}
	
	public void setDoubleBuffer(ReadOnlyDoubleBuffer<DataBGR> doubleBuffer)
	{
		this.doubleBuffer = doubleBuffer;
	}
	
    public ImageView(Context context)
    {
        super(context);
    }
 
    public ImageView(Context context, AttributeSet attrs)
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
        
        DataBGR data = doubleBuffer.readData();
		byte[] pixelsB = data.getBGR();
		
		if (w != data.getWidth() || h != data.getHeight())
		{
			w = data.getWidth();
			h = data.getHeight();
			bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
			drawPixels = new int[w * h];
			setLayoutParams(new RelativeLayout.LayoutParams(w, h));
		}
		
		for (int i = 0, j = 0; i < w * h; i++, j += 3)
		{
			drawPixels[i] = Color.rgb(pixelsB[j + 2] & 0xFF, pixelsB[j + 1] & 0xFF, pixelsB[j] & 0xFF);
		}
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
