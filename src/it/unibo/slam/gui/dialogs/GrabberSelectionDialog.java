package it.unibo.slam.gui.dialogs;

import it.unibo.slam.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Dialog used to select the grabber used by the application.
 */
public class GrabberSelectionDialog extends DialogFragment
{
	/**
	 * Listener interface for the selection dialog.
	 */
	public interface GrabberSelectionDialogListener
	{
		/**
		 * Method invoked when the selection is made.
		 * @param usedGrabber It indicates the grabber selected.
		 */
        public void onGrabberSelected(UsedGrabber usedGrabber);
    }
    
	/**
	 * Listener to the selection of the grabber to use.
	 */
	private GrabberSelectionDialogListener listener = null;
    
    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        try
        {
            listener = (GrabberSelectionDialogListener) activity;
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException(activity.toString() + " must implement GrabberSelectionDialogListener");
        }
    }
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		
		// Dialog creation
		builder.setTitle(R.string.dialog_select_grabber_title).
				setItems(R.array.grabber_list, new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						UsedGrabber usedGrabber = null;
						String selectedGrabber = getActivity().getResources().getStringArray(R.array.grabber_list)[which];
						
						if (selectedGrabber.equals("Image"))
							usedGrabber = UsedGrabber.IMAGE;
						else if (selectedGrabber.equals("Image Inertial"))
							usedGrabber = UsedGrabber.IMAGE_INERTIAL;
						else if (selectedGrabber.equals("Xtion/Kinect"))
							usedGrabber = UsedGrabber.KINECT_OR_XTION;
						else if (selectedGrabber.equals("Senz3D"))
							usedGrabber = UsedGrabber.SENZ3D;
						else if (selectedGrabber.equals("Structure"))
							usedGrabber = UsedGrabber.STRUCTURE;
						else
							usedGrabber = UsedGrabber.IMAGE;
						
						if (listener != null)
							listener.onGrabberSelected(usedGrabber);
					}
				});
		
		return builder.create();
	}
}
