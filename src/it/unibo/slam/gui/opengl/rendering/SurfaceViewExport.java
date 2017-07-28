package it.unibo.slam.gui.opengl.rendering;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

/**
 * SurfaceView that shows the export point cloud that will be saved.
 */
public class SurfaceViewExport extends GLSurfaceView
{
	/**
	 * The renderer.
	 */
	private RendererExport renderer;
	
	/**
	 * Current movement mode. Depends on the touch input received.
	 */
	private MovementMode mode;
	
	private float previousFirstX;
	private float previousFirstY;
	
	private float previousSecondX;
	private float previousSecondY;
	
	private float previousMidX;
	private float previousMidY;
	
	private float distance;
	//private float radians;
	
	/**
	 * Basic constructor.
	 * @param context The Android context.
	 */
	public SurfaceViewExport(Context context)
	{
		super(context);
		
		mode = MovementMode.NONE;
		
		setEGLContextClientVersion(2);
		
		renderer = new RendererExport(context);
		setRenderer(renderer);
		setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
	}
	
	/**
	 * Gets the Renderer.
	 * @return The Renderer.
	 */
	public RendererExport getRendererExport()
	{
		return renderer;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent e)
	{
		float dx, dy;
		float dxDist, dyDist;
		float distanceDiff;
		//float radiansDiff;
		float mxDiff, myDiff;
		
	    switch (e.getActionMasked())
	    {
	    	case MotionEvent.ACTION_DOWN:
	    		previousFirstX = e.getX();
	    		previousFirstY = e.getY();
	    		mode = MovementMode.ROTATE_PITCH_YAW;
	    		break;
	    
	    	case MotionEvent.ACTION_POINTER_DOWN:
	    		previousFirstX = e.getX(0);
	    		previousFirstY = e.getY(0);
	    		previousSecondX = e.getX(1);
	    		previousSecondY = e.getY(1);
	    		previousMidX = (previousFirstX + previousSecondX) / 2F;
	    		previousMidY = (previousFirstY + previousSecondY) / 2F;
	    		dxDist = previousSecondX - previousFirstX;
	    		dyDist = previousSecondY - previousFirstY;
	    		distance = (float)Math.sqrt(dxDist * dxDist + dyDist * dyDist);
	    		//radians = (float)Math.atan2(dyDist, dxDist);
                mode = MovementMode.PAN_ZOOM_AND_ROLL;
                break;
	    	
	    	case MotionEvent.ACTION_POINTER_UP:
                mode = MovementMode.NONE;
                break;
               
	    	case MotionEvent.ACTION_UP:
                mode = MovementMode.NONE;
                renderer.endIncrementalRotation();
                break;
                
	        case MotionEvent.ACTION_MOVE:
	        	if (mode == MovementMode.ROTATE_PITCH_YAW)
	        	{
	        		dx = e.getX() - previousFirstX;
		    		dy = -(e.getY() - previousFirstY);
		    		renderer.performRotationIncremental(-dx / (float)getWidth(), dy / (float)getHeight(), 0);
	        		requestRender();
	        	}
	        	else if (mode == MovementMode.PAN_ZOOM_AND_ROLL)
	        	{
	        		dxDist = e.getX(1) - e.getX(0);
	        		dyDist = e.getY(1) - e.getY(0);
		    		distanceDiff = (float)Math.sqrt(dxDist * dxDist + dyDist * dyDist) - distance;
		    		//radiansDiff = (float)Math.atan2(dyDist, dxDist) - radians;
		    		mxDiff = ((e.getX(0) + e.getX(1)) / 2F) - previousMidX;
		    		myDiff = -(((e.getY(0) + e.getY(1)) / 2F) - previousMidY);
		    		renderer.performZoom(distanceDiff / (float)getHeight());
		    		renderer.performPan(mxDiff / (float)getWidth(), myDiff / (float)getHeight());
		    		//renderer.performRotation(0, 0, -radiansDiff);
	        		distance = (float)Math.sqrt(dxDist * dxDist + dyDist * dyDist);
	        		//radians = (float)Math.atan2(dyDist, dxDist);
	        		previousMidX = (e.getX(0) + e.getX(1)) / 2F;
	        		previousMidY = (e.getY(0) + e.getY(1)) / 2F;
	        		requestRender();
	        	}
	            break;
	    }
	    
	    return true;
	}
}