package it.unibo.slam.gui.fragments;

import it.unibo.slam.gui.opengl.rendering.SurfaceViewSLAM;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Fragment containing the main SurfaceView.
 */
public class SurfaceViewSLAMFragment extends Fragment
{
	/**
	 * Listener that gets notified after the creation of the SurfaceView.
	 */
	public interface OnCreateSurfaceViewListener
	{
		/**
		 * This method is executed after the creation of the SurfaceView.
		 * @param view The SurfaceView that has been created.
		 */
		public void onSurfaceViewCreated(SurfaceViewSLAM view);
	};
	
	/**
	 * Listener of the SurfaceView creation.
	 */
	private OnCreateSurfaceViewListener listener = null;
    
	/**
	 * The internal SurfaceView.
	 */
	private SurfaceViewSLAM view = null;
	
    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        try
        {
            listener = (OnCreateSurfaceViewListener) activity;
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException(activity.toString() + " must implement OnCreateSurfaceViewListener");
        }
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		if (view == null)
		{
			view = new SurfaceViewSLAM(getActivity());
			listener.onSurfaceViewCreated(view);
		}
        
        return view;
    }
}
