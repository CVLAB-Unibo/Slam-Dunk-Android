package it.unibo.slam.gui.opengl.rendering;

import it.unibo.slam.buffers.ObservableDoubleBuffer;
import it.unibo.slam.buffers.interfaces.ReadOnlyDoubleBuffer;
import it.unibo.slam.datatypes.DataNewPointCloudAndPoseUpdates;
import it.unibo.slam.datatypes.DataRendererAdditional;

import java.util.Observable;
import java.util.Observer;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

/**
 * SurfaceView of the application. Mainly used to capture the user input and reflect it in a different action on the 3D scene.
 */
public class SurfaceViewSLAM extends GLSurfaceView
{
	/**
	 * Observer connected to the observable double buffers, used to request the update of the surface.
	 */
	private Observer dataObserver = new Observer()
	{
		@Override
		public void update(Observable observable, Object data)
		{
			SurfaceViewSLAM.this.requestRender();
		}
	};
	
	/**
	 * The renderer.
	 */
	private RendererSLAM renderer;
	
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
	public SurfaceViewSLAM(Context context)
	{
		super(context);
		
		mode = MovementMode.NONE;
		
		setEGLContextClientVersion(2);
		
		renderer = new RendererSLAM(context);
		setRenderer(renderer);
		setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
	}
	
	/**
	 * Gets the Renderer.
	 * @return The Renderer.
	 */
	public RendererSLAM getRendererSLAM()
	{
		return renderer;
	}
	
	/**
	 * Sets the double buffers used for the communication between the Application Manager and the Renderer.
	 * @param doubleBufferPointCloud Point cloud double buffer.
	 * @param doubleBufferTexture Double buffer for the additional data.
	 */
	public void setDoubleBuffers(	ReadOnlyDoubleBuffer<DataNewPointCloudAndPoseUpdates> doubleBufferPointCloud,
									ReadOnlyDoubleBuffer<DataRendererAdditional> doubleBufferAdditional)
	{
		if (doubleBufferPointCloud instanceof ObservableDoubleBuffer<?>)
			((ObservableDoubleBuffer<DataNewPointCloudAndPoseUpdates>)doubleBufferPointCloud).addObserver(dataObserver);
		
		if (doubleBufferAdditional instanceof ObservableDoubleBuffer<?>)
			((ObservableDoubleBuffer<DataRendererAdditional>)doubleBufferAdditional).addObserver(dataObserver);
		
		renderer.setDoubleBuffers(doubleBufferPointCloud, doubleBufferAdditional);
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