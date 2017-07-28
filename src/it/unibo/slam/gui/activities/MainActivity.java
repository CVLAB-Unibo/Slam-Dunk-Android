package it.unibo.slam.gui.activities;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import it.unibo.slam.R;
import it.unibo.slam.appmanager.ApplicationManager;
import it.unibo.slam.buffers.ConcurrentDoubleBuffer;
import it.unibo.slam.buffers.ObservableDoubleBuffer;
import it.unibo.slam.datatypes.DataBGRD;
import it.unibo.slam.datatypes.DataNewPointCloudAndPoseUpdates;
import it.unibo.slam.datatypes.DataRendererAdditional;
import it.unibo.slam.datatypes.params.AlgorithmParams;
import it.unibo.slam.datatypes.params.GeneralParams;
import it.unibo.slam.datatypes.params.KalmanParams;
import it.unibo.slam.export.ExchangedExportDataSingleton;
import it.unibo.slam.export.ExportData;
import it.unibo.slam.gui.dialogs.GrabberSelectionDialog;
import it.unibo.slam.gui.dialogs.UsedGrabber;
import it.unibo.slam.gui.dialogs.GrabberSelectionDialog.GrabberSelectionDialogListener;
import it.unibo.slam.gui.fragments.SettingsFragment;
import it.unibo.slam.gui.fragments.SurfaceViewSLAMFragment;
import it.unibo.slam.gui.fragments.SurfaceViewSLAMFragment.OnCreateSurfaceViewListener;
import it.unibo.slam.gui.opengl.rendering.SurfaceViewSLAM;
import it.unibo.slam.input.AccelerometerListener;
import it.unibo.slam.input.AccelerometerListenerDataset;
import it.unibo.slam.input.GyroscopeListener;
import it.unibo.slam.input.GyroscopeListenerDataset;
import it.unibo.slam.input.ImageGrabber;
import it.unibo.slam.input.OpenNIGrabber;
import it.unibo.slam.input.Senz3DGrabber;
import it.unibo.slam.input.StructureGrabber;
import it.unibo.slam.input.abstracts.AbstractAccelerometerListener;
import it.unibo.slam.input.abstracts.AbstractGyroscopeListener;
import it.unibo.slam.input.interfaces.SensorGrabberBGRD;
import it.unibo.slam.jni.JniLib;
import it.unibo.slam.kalman.KalmanType;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

/**
 * Main activity of this application. Used to select the grabber to use and to initialize all the other components.
 */
public class MainActivity extends Activity implements GrabberSelectionDialogListener, OnCreateSurfaceViewListener
{
	/**
	 * The view that will render the point clouds and other UI elements.
	 */
	private SurfaceViewSLAM view;
	
	/**
	 * Used Grabber.
	 */
	private SensorGrabberBGRD grabber;
	
	/**
	 * Application Manager.
	 */
	private ApplicationManager applicationManager;
	
	/**
	 * Sensor Manager.
	 */
	private SensorManager sensorManager;
	
	/**
	 * Accelerometer sensor.
	 */
	private Sensor accelerometer;
	
	/**
	 * Accelerometer Listener.
	 */
	private AbstractAccelerometerListener accelerometerListener;
	
	/**
	 * Accelerometer sensor.
	 */
	private Sensor gyroscope;
	
	/**
	 * Accelerometer Listener.
	 */
	private AbstractGyroscopeListener gyroscopeListener;
	
	/**
	 * Options Menu.
	 */
	private Menu menu;
	
	/**
	 * Player of the camera click sound.
	 */
	private MediaPlayer cameraClickPlayer;
	
	/**
	 * Callback invoked after the loading procedure of the OpenCV library.
	 */
	private BaseLoaderCallback loaderCallback = new BaseLoaderCallback(this)
	{
        @Override
        public void onManagerConnected(int status)
        {
            switch (status)
            {
                case LoaderCallbackInterface.SUCCESS:
                	// Loads the library before the first usage
                	JniLib.loadLibrary();
	                
                	if (!deviceFound)
                	{
                		// Dialog that allows the selection of the grabber to use
    	                DialogFragment grabberSelectionDialog = new GrabberSelectionDialog();
    	                grabberSelectionDialog.setCancelable(false);
    	                grabberSelectionDialog.show(getFragmentManager(), "GrabberSelection");
                	}
	                
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };
    
    private UsbManager mUsbManager;
    
    private static final String ACTION_USB_PERMISSION = "it.unibo.slam.permissions.USB_PERMISSION";
    
    private boolean deviceFound = false;
    
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() 
    {
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) 
            {
                synchronized (this) 
                {
                    UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) 
                    {
                        if(device != null)
                        {
                            UsbDeviceConnection deviceConnection    = mUsbManager.openDevice( device );
                            if (deviceConnection != null)
            	            {
            	            	// Dialog that allows the selection of the grabber to use
            	                DialogFragment grabberSelectionDialog = new GrabberSelectionDialog();
            	                grabberSelectionDialog.setCancelable(false);
            	                grabberSelectionDialog.show(getFragmentManager(), "GrabberSelection");
            	            }
                            Log.d( "USB",  deviceConnection.getSerial() );
                        }
                    } 
                    else 
                    {
                        Log.d( "USB", "permission denied for device " + device);
                    }
                }
            }
        }
    };
    
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		getFragmentManager().beginTransaction()
							.add(android.R.id.content, new SurfaceViewSLAMFragment(), "SurfaceViewSLAMFragment")
							.commit();
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		// Initializes default OpenCV library
		// OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_8, this, loaderCallback);
		
		mUsbManager = (UsbManager)getSystemService(Context.USB_SERVICE);
	    HashMap<String, UsbDevice> stringDeviceMap = mUsbManager.getDeviceList();
	    Collection<UsbDevice> usbDevices = stringDeviceMap.values();

	    PendingIntent mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
	    IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
	    registerReceiver(mUsbReceiver, filter);

	    Iterator<UsbDevice> usbDeviceIter = usbDevices.iterator();
	    deviceFound = false;
	    while (usbDeviceIter.hasNext())
	    {
	    	UsbDevice usbDevice = usbDeviceIter.next();
	        if (usbDevice.getVendorId() == 7463 && usbDevice.getProductId() == 1536)
	        {
	            // Request permission to access the device.
	            mUsbManager.requestPermission(usbDevice, mPermissionIntent);
	            deviceFound = true;
	            break;
	        }
	    }
		
		// Initializes OpenCV custom library (libopencv_java.so)
		if (OpenCVLoader.initDebug())
		{
			loaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
		}
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
		
		if (grabber != null)
			grabber.pause();
		
		if (sensorManager != null)
		{
			if (accelerometer != null && accelerometerListener != null)
				sensorManager.unregisterListener((AccelerometerListener)accelerometerListener, accelerometer);
			
			if (gyroscope != null && gyroscopeListener != null)
				sensorManager.unregisterListener((GyroscopeListener)gyroscopeListener, gyroscope);
		}
		
		if (cameraClickPlayer != null)
		{
			cameraClickPlayer.release();
			cameraClickPlayer = null;
		}
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		
		if (grabber != null)
			grabber.resume();
		
		if (sensorManager != null)
		{
			if (accelerometer != null && accelerometerListener != null)
				sensorManager.registerListener((AccelerometerListener)accelerometerListener, accelerometer, SensorManager.SENSOR_DELAY_GAME);
			
			if (gyroscope != null && gyroscopeListener != null)
				sensorManager.registerListener((GyroscopeListener)gyroscopeListener, gyroscope, SensorManager.SENSOR_DELAY_GAME);
		}
	}
	
	@Override
	public void onDestroy()
	{
		if (grabber != null)
			grabber.terminate();
		
		if (applicationManager != null)
			applicationManager.terminate();
		
		super.onDestroy();
	}
	
	@Override
	public void onBackPressed()
	{
		if(getFragmentManager().findFragmentByTag("SettingsFragment") != null)
		{
			super.onBackPressed();
			grabber.resume();
		}
		else
			super.onBackPressed();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present
		this.menu = menu;
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		boolean result = false;
		
	    // Handle item selection
	    switch (item.getItemId())
	    {
	    	case R.id.action_start_stop:
	    		if (item.getTitle().equals(getString(R.string.action_start)))
	    		{
	    			start();
	    			item.setTitle(R.string.action_stop);
	    		}
	    		else if (item.getTitle().equals(getString(R.string.action_stop)))
	    		{
	    			stop();
	    			item.setTitle(R.string.action_start);
	    		}
	    		result = true;
	    		break;
	    		
	        case R.id.action_export:
	        	export();
	            result = true;
	            break;
	            
	        case R.id.action_settings:
	        	Toast.makeText(	this, "Changes that affect the execution of the algorithm will be applied in the next run", 
	        			Toast.LENGTH_LONG).show();
	        	getFragmentManager().beginTransaction()
	        						.replace(android.R.id.content, new SettingsFragment(), "SettingsFragment")
	        						.addToBackStack("Settings")
	        						.commit();
	        	grabber.pause();
	        	result = true;
	        	break;
	        	
	        case R.id.action_take_picture:
	        	Runnable takePictureRunnable = new Runnable()
	        	{
	        		@Override
	        		public void run()
	        		{
	        			try
	        			{
							Thread.sleep(3000);
						}
	        			catch (InterruptedException e)
	        			{
	        				
						}
	        			
	        			if (cameraClickPlayer == null)
	        			{
	        				cameraClickPlayer = MediaPlayer.create(MainActivity.this, R.raw.camera_click);
	        				cameraClickPlayer.setLooping(false);
	        			}
	        			cameraClickPlayer.start();
	        			
	        			applicationManager.takePicture();
	        		}
	        	};
	        	Thread takePictureThread = new Thread(takePictureRunnable);
	        	takePictureThread.start();
	        	result = true;
	        	break;
	        	
	        case R.id.action_recording:
	    		if (item.getTitle().equals(getString(R.string.action_start_recording)))
	    		{
	    			startRecording();
	    			item.setTitle(R.string.action_stop_recording);
	    		}
	    		else if (item.getTitle().equals(getString(R.string.action_stop_recording)))
	    		{
	    			stopRecording();
	    			item.setTitle(R.string.action_start_recording);
	    		}
	    		result = true;
	    		break;
	    		
	        default:
	            result = super.onOptionsItemSelected(item);
	            break;
	    }
	    
	    return result;
	}
	
	/**
	 * Starts the execution of the algorithm.
	 */
	private void start()
	{
		applicationManager.setExecuteAlgorithm(true);
	}
	
	/**
	 * Stops the execution of the algorithm.
	 */
	private void stop()
	{
		applicationManager.setExecuteAlgorithm(false);
	}
	
	/**
	 * Starts the recording.
	 */
	private void startRecording()
	{
		// TODO add folder
		applicationManager.startRecording();
	}
	
	/**
	 * Stops the recording.
	 */
	private void stopRecording()
	{
		// TODO add folder
		boolean solvePnP = true;//false;
		applicationManager.stopRecording(solvePnP);
	}
	
	/**
	 * Method used to get the export data that will be used to save the point cloud into a file and
	 * show the results into a different Activity.
	 */
	private void export()
	{
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		
		// Export settings (octree resolution)
    	float octreeResolution = preferences.getFloat(	getString(R.string.octree_resolution_pref_key),
    													Float.parseFloat(getString(R.string.octree_resolution_default)));
    	
		ExportData data = view.getRendererSLAM().getDataToExport(octreeResolution);
		ExchangedExportDataSingleton.getInstance().setExportData(data);
		
		Intent intent = new Intent(this, ExportActivity.class);
		startActivity(intent);
	}

	@Override
	public void onGrabberSelected(UsedGrabber usedGrabber)
	{
		try
		{
			ConcurrentDoubleBuffer<DataBGRD> doubleBufferBGRD = new ConcurrentDoubleBuffer<DataBGRD>();
	    	ObservableDoubleBuffer<DataNewPointCloudAndPoseUpdates> doubleBufferPointCloud = 
	    			new ObservableDoubleBuffer<DataNewPointCloudAndPoseUpdates>();
	    	ObservableDoubleBuffer<DataRendererAdditional> doubleBufferAdditional = new ObservableDoubleBuffer<DataRendererAdditional>();
			
	    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
	    	
	    	// General settings
	    	int subsampleAmount = preferences.getInt(	getString(R.string.subsample_amount_pref_key),
	    												getResources().getInteger(R.integer.subsample_amount_default));
	    	boolean executeNative = preferences.getBoolean(	getString(R.string.execute_native_pref_key),
	    													getResources().getBoolean(R.bool.execute_native_default));
	    	boolean grabberAsThread = preferences.getBoolean(	getString(R.string.grabber_as_thread_pref_key),
																getResources().getBoolean(R.bool.grabber_as_thread_default));
	    	
	    	// Algorithm settings
	    	int rbaRings = preferences.getInt(	getString(R.string.rba_rings_pref_key),
	    										getResources().getInteger(R.integer.rba_rings_default));
	    	float keyframeOverlapping = preferences.getFloat(	getString(R.string.keyframe_overlapping_pref_key),
	    														Float.parseFloat(getString(R.string.keyframe_overlapping_default)));
	    	boolean tryLoopInference = preferences.getBoolean(	getString(R.string.try_loop_inference_pref_key),
																getResources().getBoolean(R.bool.try_loop_inference_default));
	    	float activeWindowLength = preferences.getFloat(getString(R.string.active_window_length_pref_key),
	    													Float.parseFloat(getString(R.string.active_window_length_default)));
	    	boolean debugAlgorithm = preferences.getBoolean(getString(R.string.debug_algorithm_pref_key),
															getResources().getBoolean(R.bool.debug_algorithm_default));
	    	
	    	boolean executeAlgorithmNow = false;
	    	boolean enableInertialSensors = false;
	    	
			switch (usedGrabber)
	    	{
	    		case IMAGE:
	    			InputStream imageStream = getAssets().open(getString(R.string.image_grabber_descriptor_file_name));
	    			String subPathImages = getString(R.string.image_grabber_images_folder_name);
	    			grabber = new ImageGrabber(imageStream, doubleBufferBGRD, subPathImages);
	    			menu.findItem(R.id.action_start_stop).setTitle(getString(R.string.action_stop));
	    			executeAlgorithmNow = true;
	    			enableInertialSensors = false;
	    			break;
	    			
	    		case IMAGE_INERTIAL:
	    			InputStream imageStreamInertial = new FileInputStream(Environment.getExternalStorageDirectory().getAbsolutePath() + 
	    					"/RecordingSLAM/associatedFilesRecordingSLAM.txt");
	    			String subPathImagesInertial = "RecordingSLAM";
	    			//TODO intrinsics
	    			grabber = new ImageGrabber(	imageStreamInertial, doubleBufferBGRD, subPathImagesInertial/*,
	    										10.0F, new IntrinsicParams(564.8766F, 564.0297F, 317.49078F, 249.65196F)*/);
	    			menu.findItem(R.id.action_start_stop).setTitle(getString(R.string.action_stop));
	    			executeAlgorithmNow = true;
	    			enableInertialSensors = true;
	    			break;
	    			
	    		case KINECT_OR_XTION:
	    			grabber = new OpenNIGrabber(doubleBufferBGRD);
	    			executeAlgorithmNow = false;
	    			break;
	    			
	    		case SENZ3D:
	    			grabber = new Senz3DGrabber(doubleBufferBGRD);
	    			executeAlgorithmNow = false;
	    			enableInertialSensors = false;
	    			break;
	    			
	    		case STRUCTURE:
	    			grabber = new StructureGrabber(doubleBufferBGRD);
	    			executeAlgorithmNow = false;
	    			enableInertialSensors = false;//true;
	    			menu.add(Menu.NONE, R.id.action_take_picture, 101, R.string.action_take_picture);
	    			menu.add(Menu.NONE, R.id.action_recording, 102, R.string.action_start_recording);
	    			break;
	    			
	    		default:
	    			InputStream imageStreamDefault = getAssets().open(getString(R.string.image_grabber_descriptor_file_name));
	    			String subPathImagesDefault = getString(R.string.image_grabber_images_folder_name);
	    			grabber = new ImageGrabber(imageStreamDefault, doubleBufferBGRD, subPathImagesDefault);
	    			menu.findItem(R.id.action_start_stop).setTitle(getString(R.string.action_stop));
	    			executeAlgorithmNow = true;
	    			enableInertialSensors = false;
	    			break;
	    	}
	    	
			grabber.init();
			
			view.setDoubleBuffers(doubleBufferPointCloud, doubleBufferAdditional);
			
			if (enableInertialSensors)
			{
				if (grabber instanceof ImageGrabber)
				{
					accelerometerListener = new AccelerometerListenerDataset(new FileInputStream(
							Environment.getExternalStorageDirectory().getAbsolutePath() + "/RecordingSLAM/associatedFilesRecordingSLAM.txt"), 
							Environment.getExternalStorageDirectory().getAbsolutePath() + "/RecordingSLAM/accelerometer.txt");
					gyroscopeListener = new GyroscopeListenerDataset(new FileInputStream(
							Environment.getExternalStorageDirectory().getAbsolutePath() + "/RecordingSLAM/associatedFilesRecordingSLAM.txt"), 
							Environment.getExternalStorageDirectory().getAbsolutePath() + "/RecordingSLAM/gyroscope.txt");
				}
				else if (grabber instanceof StructureGrabber)
				{
					sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
				
					accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
					if (accelerometer != null)
					{
						accelerometerListener = new AccelerometerListener();
						sensorManager.registerListener((AccelerometerListener)accelerometerListener, accelerometer, SensorManager.SENSOR_DELAY_GAME);
					}
					
					gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
					if (gyroscope != null)
					{
						gyroscopeListener = new GyroscopeListener();
						sensorManager.registerListener((GyroscopeListener)gyroscopeListener, gyroscope, SensorManager.SENSOR_DELAY_GAME);
					}
				}
			}
			
			// TODO rendere modificabile dalle impostazioni
			applicationManager = new ApplicationManager(doubleBufferBGRD, doubleBufferPointCloud, 
					doubleBufferAdditional, MainActivity.this, grabber, executeAlgorithmNow,
					new GeneralParams(grabberAsThread, subsampleAmount, executeNative),
					new AlgorithmParams(rbaRings, keyframeOverlapping, tryLoopInference,
							activeWindowLength, debugAlgorithm),
					new KalmanParams(enableInertialSensors, KalmanType.BASIC_KALMAN),
					accelerometerListener, gyroscopeListener);
			
			// Starting the Grabber thread, if required
			if (grabberAsThread)
			{
				Thread grabberThread = new Thread(grabber);
				grabberThread.start();
			}
			
			// Starting the Application Manager thread
			Thread applicationManagerThread = new Thread(applicationManager);
			applicationManagerThread.start();
		}
		catch (Exception e)
		{
			System.exit(1);
		}
	}
	
	@Override
	public void onSurfaceViewCreated(SurfaceViewSLAM view)
	{
		this.view = view;
	}
}