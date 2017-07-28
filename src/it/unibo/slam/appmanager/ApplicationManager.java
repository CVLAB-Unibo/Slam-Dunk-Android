package it.unibo.slam.appmanager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Logger;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.FeatureDetector;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import it.unibo.slam.R;
import android.content.Context;
import android.os.Environment;
import it.unibo.slam.buffers.interfaces.ReadOnlyDoubleBuffer;
import it.unibo.slam.buffers.interfaces.WriteOnlyDoubleBuffer;
import it.unibo.slam.datatypes.DataFPS;
import it.unibo.slam.datatypes.DataGrayscale;
import it.unibo.slam.datatypes.DataIMU;
import it.unibo.slam.datatypes.DataRGB;
import it.unibo.slam.datatypes.DataBGRD;
import it.unibo.slam.datatypes.DataNewPointCloudAndPoseUpdates;
import it.unibo.slam.datatypes.DataRendererAdditional;
import it.unibo.slam.datatypes.DataTrajectory;
import it.unibo.slam.datatypes.Pair;
import it.unibo.slam.datatypes.SensorData;
import it.unibo.slam.datatypes.eigen.typedouble.EigenIsometry3D;
import it.unibo.slam.datatypes.eigen.typedouble.EigenMatrix3D;
import it.unibo.slam.datatypes.eigen.typedouble.EigenMatrix4D;
import it.unibo.slam.datatypes.eigen.typedouble.EigenVector3D;
import it.unibo.slam.datatypes.eigen.typefloat.EigenIsometry3F;
import it.unibo.slam.datatypes.eigen.typefloat.EigenMatrix3F;
import it.unibo.slam.datatypes.eigen.typefloat.EigenMatrix4F;
import it.unibo.slam.datatypes.eigen.typefloat.EigenVector3F;
import it.unibo.slam.datatypes.geometry.MatrixFactory;
import it.unibo.slam.datatypes.geometry.Quaternion;
import it.unibo.slam.datatypes.params.AlgorithmParams;
import it.unibo.slam.datatypes.params.GeneralParams;
import it.unibo.slam.datatypes.params.IntrinsicParams;
import it.unibo.slam.datatypes.params.KalmanParams;
import it.unibo.slam.input.ImageGrabber;
import it.unibo.slam.input.abstracts.AbstractAccelerometerListener;
import it.unibo.slam.input.abstracts.AbstractGyroscopeListener;
import it.unibo.slam.input.interfaces.SensorGrabberBGRD;
import it.unibo.slam.kalman.KalmanGlobal;
import it.unibo.slam.main.SlamDunk;
import it.unibo.slam.main.SlamDunkFactory;
import it.unibo.slam.main.SlamDunkNative;
import it.unibo.slam.main.SlamDunkNativeResult;
import it.unibo.slam.main.SlamDunkResult;
import it.unibo.slam.rate.RateCalculatorSMA;
import it.unibo.slam.rate.interfaces.RateCalculator;
import it.unibo.slam.utils.CameraPoseUtils;
import it.unibo.slam.utils.FileSystemUtils;

/**
 * Class used to read the data from the grabber buffer, execute the SLAM algorithm and 
 * write the results on the buffer read by the renderer.
 */
public class ApplicationManager implements Runnable
{
	/**
	 * Grabber buffer in which the data will be read.
	 */
	private ReadOnlyDoubleBuffer<DataBGRD> doubleBufferBGRD;
	
	/**
	 * Buffer containing the data for adding a new point cloud and updating the poses.
	 */
	private WriteOnlyDoubleBuffer<DataNewPointCloudAndPoseUpdates> doubleBufferPointCloud;
	
	/**
	 * Buffer containing the additional rendering data.
	 */
	private WriteOnlyDoubleBuffer<DataRendererAdditional> doubleBufferAdditional;
	
	/**
	 * Image height.
	 */
	private int h = 480;
	
	/**
	 * Image width.
	 */
	private int w = 640;
	
	private IntrinsicParams cameraIntrinsicParams;
	
	/*float focalX = 525F;
	float focalY = 525F;
	float centerX = 319.5F;
	float centerY = 239.5F;*/
	// freiburg1
	/*float focalX = 517.3F;
	float focalY = 516.5F;
	float centerX = 318.6F;
	float centerY = 255.3F;*/
	// senz3d
	/*float focalX = 587.45F;
	float focalY = 600.67F;
	float centerX = 320.0F;
	float centerY = 240.0F;*/
	
	/**
	 * Name of the YML file containing the description of the feature detector.
	 */
	private String featureDetectorFileName;
	
	/**
	 * Name of the YML file containing the description of the descriptor matcher.
	 */
	private String descriptorMatcherFileName;
	
	/**
	 * Integer representing the OpenCV detector type.
	 */
	private int detectorType;
	
	/**
	 * Integer representing the OpenCV extractor type.
	 */
	private int extractorType;
	
	/**
	 * Path in which to save all the trajectories (in case of ImageGrabber execution).
	 */
	private String allTrajectoriesPath;
	
	/**
	 * Path in which to save the keyframe trajectories (in case of ImageGrabber execution).
	 */
	private String keyframeTrajectoriesPath;
	
	/**
	 * Path in which to save the execution time (in case of ImageGrabber execution).
	 */
	private String timePath;
	
	/**
	 * Path in which to save the recordings (in case of StructureGrabber execution).
	 */
	private String recordingPath;
	
	/**
	 * Implementation of the SLAM algorithm. Used to get the pose from the received image.
	 */
	private SlamDunk slam;
	
	/**
	 * Used to calculate the current FPS value.
	 */
	private RateCalculator fpsCalculator;
	
	/**
	 * Grabber used in case we skip the double buffer.
	 */
	private SensorGrabberBGRD grabber;
	
	/**
	 * If true it executes the algorithm, otherwise it just passes the images data to the Renderer.
	 */
	private boolean executeAlgorithm;
	
	/**
	 * If true it terminates the running Thread, if it is executing.
	 */
	private boolean terminate;
	
	/**
	 * General application parameters.
	 */
	private GeneralParams generalParams;
	
	/**
	 * Algorithm parameters.
	 */
	private AlgorithmParams algorithmParams;
	
	/**
	 * Kalman parameters.
	 */
	private KalmanParams kalmanParams;
	
	/**
	 * The accelerometer listener.
	 */
	private AbstractAccelerometerListener accelerometer;
	
	/**
	 * The gyroscope listener.
	 */
	private AbstractGyroscopeListener gyroscope;
	
	/**
	 * Set to true to store the RGB-D values.
	 */
	private boolean takePicture;
	
	/**
	 * Picture id.
	 */
	private int pictureId;
	
	/**
	 * If true the recording of data is enabled.
	 */
	private boolean recording;
	
	/**
	 * Base constructor.
	 * @param doubleBufferBGRD Grabber buffer.
	 * @param doubleBufferPointCloud Buffer for pose update and point cloud adding.
	 * @param doubleBufferAdditional Buffer for additional data to update in the Renderer.
	 * @param context The context.
	 * @param grabber The grabber.
	 * @param executeAlgorithm True to enable the execution of the algorithm, false otherwise.
	 * @param generalParams The general application parameters.
	 * @param kalmanParams The Kalman filter parameters.
	 * @param accelerometer The accelerometer listener.
	 * @param gyroscope The gyroscope listener.
	 */
	public ApplicationManager(	ReadOnlyDoubleBuffer<DataBGRD> doubleBufferBGRD, 
								WriteOnlyDoubleBuffer<DataNewPointCloudAndPoseUpdates> doubleBufferPointCloud,
								WriteOnlyDoubleBuffer<DataRendererAdditional> doubleBufferAdditional,
								Context context, SensorGrabberBGRD grabber, boolean executeAlgorithm,
								GeneralParams generalParams, AlgorithmParams algorithmParams,
								KalmanParams kalmanParams, AbstractAccelerometerListener accelerometer,
								AbstractGyroscopeListener gyroscope)
	{
		this.doubleBufferBGRD = doubleBufferBGRD;
		this.doubleBufferPointCloud = doubleBufferPointCloud;
		this.doubleBufferAdditional = doubleBufferAdditional;
		this.grabber = grabber;
		cameraIntrinsicParams = grabber.getIntrinsicParams();
		this.executeAlgorithm = executeAlgorithm;
		this.generalParams = generalParams;
		this.algorithmParams = algorithmParams;
		this.kalmanParams = kalmanParams;
		this.accelerometer = accelerometer;
		this.gyroscope = gyroscope;
		terminate = false;
		
		takePicture = false;
		pictureId = 1;
		
		try
		{
			FileSystemUtils.writeFileFromAssetsToCache(context.getString(R.string.detector_file_name), context);
			FileSystemUtils.writeFileFromAssetsToCache(context.getString(R.string.flann_file_name), context);
			featureDetectorFileName = context.getCacheDir().getAbsolutePath() + "/" + context.getString(R.string.detector_file_name);
			descriptorMatcherFileName = context.getCacheDir().getAbsolutePath() + "/" + context.getString(R.string.flann_file_name);
			detectorType = FeatureDetector.ORB;
			extractorType = DescriptorExtractor.ORB;
			//detectorType = context.getResources().getInteger(R.integer.detector_type);
			//extractorType = context.getResources().getInteger(R.integer.extractor_type);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		
		allTrajectoriesPath = context.getString(R.string.all_trajectories_output_path);
		keyframeTrajectoriesPath = context.getString(R.string.keyframe_trajectories_output_path);
		timePath = context.getString(R.string.execution_time_output_path);
		recordingPath = context.getString(R.string.recording_folder_path);
		
		fpsCalculator = new RateCalculatorSMA();
	}
	
	@Override
	public void run()
	{
		//TODO sistemare metodo
		
		EigenMatrix3F inverseKCam = null;
		EigenMatrix4F inverseKCamExtended = null;
		EigenMatrix4D res = new EigenMatrix4D();
		EigenMatrix4D res2 = new EigenMatrix4D();
		float[] points = null;
		byte[] colors = null;
		byte[] dataTextureRGB = null;
		byte[] dataTextureDepth = null;
		
		List<DataTrajectory> allPoses = new ArrayList<DataTrajectory>();
		List<DataTrajectory> keyframePoses = new ArrayList<DataTrajectory>();
		
		boolean initializedKalman = false;
		
		long milStart = System.currentTimeMillis();
		
		PrintWriter testWriter = null;
		try
		{
			testWriter = new PrintWriter(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/testPoseKal.txt"));
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		
		PrintWriter accelerometerWriter = null;
		PrintWriter gyroscopeWriter = null;
		PrintWriter rgbDepthWriter = null;
		
		while (!terminate)
		{
			boolean executeAlgorithmNow;
			synchronized (this)
			{
				executeAlgorithmNow = executeAlgorithm;
			}
			
			boolean recordingNow;
			synchronized (this)
			{
				recordingNow = recording;
			}
			
			if (recordingNow && !initializedKalman)
			{
				try
				{
					accelerometerWriter = new PrintWriter(new File(	Environment.getExternalStorageDirectory().getAbsolutePath() + "/" +
																	recordingPath + "/accelerometer.txt"));
					gyroscopeWriter = new PrintWriter(new File(	Environment.getExternalStorageDirectory().getAbsolutePath() + "/" +
																recordingPath + "/gyroscope.txt"));
					rgbDepthWriter = new PrintWriter(new File(	Environment.getExternalStorageDirectory().getAbsolutePath() + "/" +
																recordingPath + "/associatedFilesRecordingSLAM.txt"));
				}
				catch (FileNotFoundException e)
				{
					e.printStackTrace();
					System.exit(1);
				}
				
				// Waiting for gravity calculation
				accelerometer.startCalculatingGravity();
				
				//TODO per ora attesa fissa, modificare con un numero prefissato di valori e notificare da UI l'attesa e di stare fermi
				try
				{
					Thread.sleep(5000L);
				}
				catch (InterruptedException e)
				{
					
				}
				
				accelerometer.requestBuffering();
				gyroscope.requestBuffering();
				
				accelerometerWriter.println("FRAME_RATE " + ((int)accelerometer.getSensorRate()));
				accelerometerWriter.println("GRAVITY " + accelerometer.getGravity().getX() + " " + accelerometer.getGravity().getY() + 
											" " + accelerometer.getGravity().getZ());
				
				gyroscopeWriter.println("FRAME_RATE " + ((int)gyroscope.getSensorRate()));
				
				initializedKalman = true;
			}
			else if (executeAlgorithmNow && kalmanParams.getExecuteKalman() && !initializedKalman)
			{
				// Waiting for gravity calculation
				accelerometer.startCalculatingGravity();
				
				//TODO per ora attesa fissa, modificare con un numero prefissato di valori e notificare da UI l'attesa e di stare fermi
				try
				{
					Thread.sleep(5000L);
				}
				catch (InterruptedException e)
				{
					
				}
				
				accelerometer.disable();
				gyroscope.disable();
					
				KalmanGlobal.initializeKalman(	kalmanParams.getKalmanType(), accelerometer.getSensorPeriod(), 
												gyroscope.getSensorPeriod(), accelerometer.getGravity());
				
				initializedKalman = true;
			}
			
			DataBGRD currentData = null;
			try
			{
				if (generalParams.isGrabberAsThread())
				{
					// Reading data from the double buffer
					currentData = doubleBufferBGRD.readData();
				}
				else
				{
					// Directly grabbing the data
					currentData = grabber.grab();
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
				return;
			}
			
			if (currentData == null)
			{
				// Just for test, writing the trajectories
				if (grabber instanceof ImageGrabber)
				{
					writeTrajectoriesAndExecutionTime(	allPoses, keyframePoses, milStart, 
														allTrajectoriesPath, keyframeTrajectoriesPath, 
														timePath);

					return;//System.exit(0);
				}
				else
					continue;
			}
			
			if (inverseKCam == null)
			{
				// We suppose height and width will not change
				h = currentData.getHeight();
				w = currentData.getWidth();
				points = new float[h * w * 3];
				colors = new byte[h * w * 3];
				dataTextureRGB = new byte[3 * ((w * h) / 4)];
				dataTextureDepth = new byte[(w * h) / 4];
				inverseKCam = MatrixFactory.getInverseKCam(cameraIntrinsicParams.getFocalX(), cameraIntrinsicParams.getFocalY(), 
						cameraIntrinsicParams.getCenterX(), cameraIntrinsicParams.getCenterY());
				inverseKCamExtended = MatrixFactory.getInverseKCamExtended(cameraIntrinsicParams.getFocalX(), cameraIntrinsicParams.getFocalY(),
						cameraIntrinsicParams.getCenterX(), cameraIntrinsicParams.getCenterY());
				slam = SlamDunkFactory.initComplete(featureDetectorFileName, descriptorMatcherFileName, detectorType, extractorType,
													inverseKCam, w, h, algorithmParams);
				SlamDunkNative.init(cameraIntrinsicParams.getFocalX(), cameraIntrinsicParams.getFocalY(), 
						cameraIntrinsicParams.getCenterX(), cameraIntrinsicParams.getCenterY(), w, h,
						algorithmParams.getRbaRings(), algorithmParams.getKeyframeOverlapping(),
						algorithmParams.isTryLoopInference(), algorithmParams.getActiveWindowLength(),
						algorithmParams.isDebugAlgorithm());
			}
			
			if (recordingNow)
			{
				Queue<SensorData> accelerationBuffer = accelerometer.getAccelerationBuffer();
				Queue<SensorData> angularVelocityBuffer = gyroscope.getAngularVelocityBuffer();
				
				while (!accelerationBuffer.isEmpty() && !angularVelocityBuffer.isEmpty())
				{
					SensorData accelerationData = accelerationBuffer.poll();
					SensorData angularVelocityData = angularVelocityBuffer.poll();
					
					if (accelerationData != null)
					{
						String timestampAccelerometer = new BigDecimal(accelerationData.timestamp).setScale(3, BigDecimal.ROUND_HALF_UP).toPlainString();
						accelerometerWriter.println("ACCELERATION " + timestampAccelerometer + " " + accelerationData.values[0] + 
													" " + accelerationData.values[1] + " " + accelerationData.values[2]);
						accelerometerWriter.flush();
					}
					
					if (angularVelocityData != null)
					{
						String timestampGyroscope = new BigDecimal(angularVelocityData.timestamp).setScale(3, BigDecimal.ROUND_HALF_UP).toPlainString();
						gyroscopeWriter.println("ANGULAR_VELOCITY " + timestampGyroscope + " " + angularVelocityData.values[0] + 
												" " + angularVelocityData.values[1] + " " + angularVelocityData.values[2]);
						gyroscopeWriter.flush();
					}
				}
				
				byte[] pictureRGB = new byte[3 * (w * h)];
				short[] pictureDepth = new short[w * h];
				
				createDatasetDepthAndRGB(currentData, pictureDepth, pictureRGB);
				
				Mat matBGR = new Mat(h, w, CvType.CV_8UC3);
				matBGR.put(0, 0, pictureRGB);
				Mat matRGB = new Mat(h, w, CvType.CV_8UC3);
				Imgproc.cvtColor(matBGR, matRGB, Imgproc.COLOR_BGR2RGB);
				
				String timestampData = new BigDecimal(currentData.getTimestamp()).setScale(3, BigDecimal.ROUND_HALF_UP).toPlainString();
				
				Highgui.imwrite(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + recordingPath + "/rgb/" + 
								timestampData + ".png", matRGB);
				
				Mat matDepth = new Mat(h, w, CvType.CV_16UC1);
				matDepth.put(0, 0, pictureDepth);
				Highgui.imwrite(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + recordingPath + "/depth/" + 
						timestampData + ".png", matDepth);
				
				rgbDepthWriter.println(timestampData + " rgb/" + timestampData +  ".png " + timestampData + " depth/" + timestampData +  ".png");
				rgbDepthWriter.flush();
			}
			
			boolean takePictureNow;
			synchronized (this)
			{
				takePictureNow = takePicture;
			}
			if (takePictureNow)
			{
				byte[] pictureRGB = new byte[3 * (w * h)];
				//byte[] pictureDepth = new byte[3 * (w * h)];
				short[] pictureDepth = new short[(w * h)];
				
				//createTextureDepth3AndRGB(currentData, 1, pictureDepth, pictureRGB);
				createTextureDepth16UAndRGB(currentData, 1, pictureDepth, pictureRGB);
				
				Mat matBGR = new Mat(h, w, CvType.CV_8UC3);
				matBGR.put(0, 0, pictureRGB);
				Mat matRGB = new Mat(h, w, CvType.CV_8UC3);
				Imgproc.cvtColor(matBGR, matRGB, Imgproc.COLOR_BGR2RGB);
				
				Highgui.imwrite(Environment.getExternalStorageDirectory().getAbsolutePath() + "/capturedRGB" + pictureId + ".png", matRGB);
				
				/*Mat matDepth = new Mat(h, w, CvType.CV_8UC3);
				matDepth.put(0, 0, pictureDepth);
				Highgui.imwrite(Environment.getExternalStorageDirectory().getAbsolutePath() + "/capturedIR" + pictureId + ".png", matDepth);*/
				
				Mat matDepth = new Mat(h, w, CvType.CV_16UC1);
				matDepth.put(0, 0, pictureDepth);
				Highgui.imwrite(Environment.getExternalStorageDirectory().getAbsolutePath() + "/capturedIR" + pictureId + ".png", matDepth);
				
				synchronized (this)
				{
					takePicture = false;
					pictureId++;
				}
			}
			
			long s;
			SlamDunkResult slamResult = null;
			int slamResult2 = 0;
			EigenIsometry3D cameraPose = EigenIsometry3D.getIdentity();
			EigenIsometry3D cameraPose2 = EigenIsometry3D.getIdentity();
			EigenIsometry3F tempCameraPose = new EigenIsometry3F(EigenIsometry3D.getIdentity().getMatrix().toFloat());
			
			long elapsedTime = 0;
			
			if (kalmanParams.getExecuteKalman() && executeAlgorithmNow)
			{
				accelerometer.requestBuffering();
				gyroscope.requestBuffering();
			}
			
			if (!generalParams.isExecuteNative() && executeAlgorithmNow)
			{
				s = System.currentTimeMillis();
				slamResult = slam.execute(currentData, cameraPose);
				elapsedTime = System.currentTimeMillis() - s;
				System.out.println("JAVA: " + elapsedTime);
			}
			else if (generalParams.isExecuteNative() && executeAlgorithmNow)
			{
				s = System.currentTimeMillis();
				Mat grayscaleImage = new Mat();
				Mat colorImage = new Mat(h, w, CvType.CV_8UC3);
				Mat depthImage = new Mat(h, w, CvType.CV_32FC1);
				colorImage.put(0, 0, currentData.getBGR());
				Imgproc.cvtColor(colorImage, grayscaleImage, Imgproc.COLOR_BGR2GRAY);
				depthImage.put(0, 0, currentData.getDepth());
				/*slamResult2 = SlamDunkNative.execute(currentData.getTimestamp(), grayscaleImage.nativeObj, depthImage.nativeObj, 
						cameraPose2.getMatrix().getValue());
				System.out.println("Array original: " + Arrays.toString(cameraPose2.getMatrix().getValue()));*/
				slamResult2 = SlamDunkNative.executeTracking(currentData.getTimestamp(), grayscaleImage.nativeObj, depthImage.nativeObj, 
						cameraPose2.getMatrix().getValue());
				if (slamResult2 == SlamDunkNativeResult.KEYFRAME_DETECTED)
				{
					if (kalmanParams.getExecuteKalman())
					{
						accelerometer.startExecutingKalman();
						gyroscope.startExecutingKalman();
					}
				}
				else if (slamResult2 == SlamDunkNativeResult.FRAME_TRACKED)
				{
					if (kalmanParams.getExecuteKalman())
					{
						testWriter.println("Residual: " + SlamDunkNative.getResidual());
						
						KalmanGlobal.setVisualObservationNoise(SlamDunkNative.getResidual());
						
						EigenVector3F translationTemp = cameraPose2.getTranslation().toFloat();
						translationTemp.setY(-translationTemp.getY());
						translationTemp.setZ(-translationTemp.getZ());
						KalmanGlobal.handleData(DataIMU.createPositionData(translationTemp));
						
						EigenMatrix3F rotationTemp = cameraPose2.getRotation().toFloat();
						rotationTemp.set(-rotationTemp.get(0, 1), 0, 1);
						rotationTemp.set(-rotationTemp.get(1, 0), 1, 0);
						rotationTemp.set(-rotationTemp.get(0, 2), 0, 2);
						rotationTemp.set(-rotationTemp.get(2, 0), 2, 0);
						KalmanGlobal.handleData(DataIMU.createOrientationData(rotationTemp));
						
						//testWriter.println("Pose pre: " + Arrays.toString(cameraPose2.getMatrix().getValue()));
						//testWriter.flush();
						
						tempCameraPose = KalmanGlobal.getFilteredPose();
						
						translationTemp = tempCameraPose.getTranslation();
						translationTemp.setY(-translationTemp.getY());
						translationTemp.setZ(-translationTemp.getZ());
						tempCameraPose.setTranslation(translationTemp);
						
						rotationTemp = tempCameraPose.getRotation();
						rotationTemp.set(-rotationTemp.get(0, 1), 0, 1);
						rotationTemp.set(-rotationTemp.get(1, 0), 1, 0);
						rotationTemp.set(-rotationTemp.get(0, 2), 0, 2);
						rotationTemp.set(-rotationTemp.get(2, 0), 2, 0);
						tempCameraPose.setRotation(rotationTemp);

						cameraPose2.setRotation(tempCameraPose.getRotation().toDouble());
						//cameraPose2.setMatrix(tempCameraPose.getMatrix().toDouble());
						
						//testWriter.println("Pose post: " + Arrays.toString(tempCameraPose.getMatrix().getValue()));
						//testWriter.flush();
						
						Queue<SensorData> accelerationBuffer = accelerometer.getAccelerationBuffer();
						Queue<SensorData> angularVelocityBuffer = gyroscope.getAngularVelocityBuffer();
						
						while (!angularVelocityBuffer.isEmpty() || !accelerationBuffer.isEmpty())
						{
							SensorData angularVelocity = angularVelocityBuffer.peek();
							SensorData acceleration = accelerationBuffer.peek();
							
							if (angularVelocity == null)
							{
								KalmanGlobal.handleData(DataIMU.createAccelerationData(new EigenVector3F(acceleration.values)));
								accelerationBuffer.remove();
							}
							else
							{
								if (acceleration == null)
								{
									KalmanGlobal.handleData(DataIMU.createAngularVelocityData(new EigenVector3F(angularVelocity.values)));
									angularVelocityBuffer.remove();
								}
								else
								{
									if (angularVelocity.timestamp < acceleration.timestamp)
									{
										KalmanGlobal.handleData(DataIMU.createAngularVelocityData(new EigenVector3F(angularVelocity.values)));
										angularVelocityBuffer.remove();
									}
									else
									{
										KalmanGlobal.handleData(DataIMU.createAccelerationData(new EigenVector3F(acceleration.values)));
										accelerationBuffer.remove();
									}
								}
							}
						}
						
						accelerometer.startExecutingKalman();
						gyroscope.startExecutingKalman();
					}
					
					//testWriter.println("Pose initial: " + Arrays.toString(cameraPose2.getMatrix().getValue()));
					//testWriter.flush();
					
					slamResult2 = SlamDunkNative.executeOptimization(cameraPose2.getMatrix().getValue());
					
					//testWriter.println("Pose final: " + Arrays.toString(cameraPose2.getMatrix().getValue()));
					//testWriter.flush();
				}
				
				elapsedTime = System.currentTimeMillis() - s;
				System.out.println("NATIVE: " + elapsedTime);
			}
			
			float fps = fpsCalculator.getRate((int)elapsedTime);
			
			boolean sendTextures = true;
			
			// Java execution
			if (!generalParams.isExecuteNative())
			{
				if (sendTextures)
				{
					createTextureDepthAndRGB(currentData, dataTextureDepth, dataTextureRGB);
					doubleBufferAdditional.fillBuffer(	new DataRendererAdditional(new DataRGB(dataTextureRGB, w / 2, h / 2), 
														new DataGrayscale(dataTextureDepth, w / 2, h / 2), new DataFPS(fps),
														cameraPose, new EigenIsometry3D(tempCameraPose.getMatrix().toDouble())));
				}
				
				if (executeAlgorithmNow)
				{
					if (slamResult == SlamDunkResult.FRAME_TRACKED || slamResult == SlamDunkResult.KEYFRAME_DETECTED)
					{
						allPoses.add(new DataTrajectory(currentData.getTimestamp(), cameraPose.getTranslation().toFloat(), 
														Quaternion.fromRotationMatrix(cameraPose.getRotation().toFloat())));
					}
					
					if (slamResult == SlamDunkResult.KEYFRAME_DETECTED)
					{
						keyframePoses.add(new DataTrajectory(	currentData.getTimestamp(), cameraPose.getTranslation().toFloat(), 
																Quaternion.fromRotationMatrix(cameraPose.getRotation().toFloat())));
						
						// Point cloud creation, eventually with subsampling
						int k = createProjectedPointCloud(currentData, generalParams.getSubsampleAmount(), points, colors);
						
						float[] pointsReal = new float[k];
						System.arraycopy(points, 0, pointsReal, 0, k);
						byte[] colorsReal = new byte[k];
						System.arraycopy(colors, 0, colorsReal, 0, k);
					
						cameraPose.getMatrix().multiplyWith(inverseKCamExtended, res);
						List<Pair<Double, EigenMatrix4F>> cloudPoses = new ArrayList<Pair<Double, EigenMatrix4F>>();
						for (Pair<Double, EigenIsometry3D> pair : slam.getMovedFrames())
						{
							pair.getSecond().getMatrix().multiplyWith(inverseKCamExtended, res2);
							cloudPoses.add(new Pair<Double, EigenMatrix4F>(pair.getFirst(), res2.toFloat()));
						}
						
						DataNewPointCloudAndPoseUpdates data = new DataNewPointCloudAndPoseUpdates(	currentData.getTimestamp(), pointsReal, colorsReal, 
																									res.toFloat(), cloudPoses);
						doubleBufferPointCloud.fillBuffer(data);
					}
					
					if (slamResult == SlamDunkResult.TRACKING_FAILED)
						Logger.getLogger("ApplicationManager").warning("Tracking failed!");
				}
			}
			// Native execution
			else
			{
				if (sendTextures)
				{
					createTextureDepthAndRGB(currentData, dataTextureDepth, dataTextureRGB);
					doubleBufferAdditional.fillBuffer(	new DataRendererAdditional(new DataRGB(dataTextureRGB, w / 2, h / 2), 
														new DataGrayscale(dataTextureDepth, w / 2, h / 2), new DataFPS(fps), 
														cameraPose2, new EigenIsometry3D(tempCameraPose.getMatrix().toDouble())));
				}
				
				/*boolean testPointCloud = true;
				
				if (testPointCloud)
				{
					int k = createProjectedPointCloud(currentData, 1, points, colors);
					
					float[] pointsReal = new float[k];
					System.arraycopy(points, 0, pointsReal, 0, k);
					byte[] colorsReal = new byte[k];
					System.arraycopy(colors, 0, colorsReal, 0, k);
				
					List<Pair<Double, EigenMatrix4F>> cloudPoses = new ArrayList<Pair<Double, EigenMatrix4F>>();
					cloudPoses.add(new Pair<Double, EigenMatrix4F>(123.0, inverseKCamExtended));
					
					DataNewPointCloudAndPoseUpdates data = new DataNewPointCloudAndPoseUpdates(	123.0, pointsReal, colorsReal, 
																								inverseKCamExtended, cloudPoses);
					doubleBufferPointCloud.fillBuffer(data);
				}*/
				
				if (executeAlgorithmNow)
				{
					if (slamResult2 == SlamDunkNativeResult.FRAME_TRACKED || slamResult2 == SlamDunkNativeResult.KEYFRAME_DETECTED)
					{
						allPoses.add(new DataTrajectory(currentData.getTimestamp(), cameraPose2.getTranslation().toFloat(), 
														Quaternion.fromRotationMatrix(cameraPose2.getRotation().toFloat())));
					}
					
					if (slamResult2 == SlamDunkNativeResult.KEYFRAME_DETECTED)
					{
						keyframePoses.add(new DataTrajectory(	currentData.getTimestamp(), cameraPose2.getTranslation().toFloat(), 
																Quaternion.fromRotationMatrix(cameraPose2.getRotation().toFloat())));
						
						// Point cloud creation, eventually with subsampling
						int k = createProjectedPointCloud(currentData, generalParams.getSubsampleAmount(), points, colors);
						
						float[] pointsReal = new float[k];
						System.arraycopy(points, 0, pointsReal, 0, k);
						byte[] colorsReal = new byte[k];
						System.arraycopy(colors, 0, colorsReal, 0, k);
					
						cameraPose2.getMatrix().multiplyWith(inverseKCamExtended, res);
						List<Pair<Double, EigenMatrix4F>> cloudPoses = new ArrayList<Pair<Double, EigenMatrix4F>>();
						
						int size = SlamDunkNative.getMovedFramesNum();
						double[] timestamps = new double[size];
						double[] isometries = new double[size * 16];
						SlamDunkNative.getMovedFrames(timestamps, isometries);
						for (int i = 0, j = 0; i < size; i++, j += 16)
						{
							EigenMatrix4D matrix = new EigenMatrix4D();
							double[] matrixArray = matrix.getValue();
							for (int x = 0; x < 16; x++)
								matrixArray[x] = isometries[j + x];
							matrix.multiplyWith(inverseKCamExtended, res2);
							cloudPoses.add(new Pair<Double, EigenMatrix4F>(timestamps[i], res2.toFloat()));
						}
						
						DataNewPointCloudAndPoseUpdates data = new DataNewPointCloudAndPoseUpdates(	currentData.getTimestamp(), pointsReal, colorsReal, 
																									res.toFloat(), cloudPoses);
						doubleBufferPointCloud.fillBuffer(data);
					}
					
					if (slamResult2 == SlamDunkNativeResult.TRACKING_FAILED)
						Logger.getLogger("ApplicationManager").warning("Tracking failed!");
				}
			}
			
			doubleBufferBGRD.notifyReadFinished();
		}
		
		testWriter.close();
	}
	
	/**
	 * Writes the trajectories and the execution time on different files.<br>
	 * One file is used for all the trajectories, another one for only the keyframe trajectories and the last
	 * one for the execution time.
	 * @param allPoses All the trajectories stored.
	 * @param keyframePoses The keyframe trajectories.
	 * @param milStart Milliseconds measured on the start of the execution.
	 * @param allPosesPath Path to the file in which to write all the trajectories (relative to the external storage directory).
	 * @param keyframePosesPath Path to the file in which to write the keyframe trajectories (relative to the external storage directory).
	 * @param timePath Path to the file in which to write the execution time (relative to the external storage directory).
	 */
	private void writeTrajectoriesAndExecutionTime(	List<DataTrajectory> allPoses, List<DataTrajectory> keyframePoses, long milStart,
													String allPosesPath, String keyframePosesPath, String timePath)
	{
		// TODO togliere tempo di esecuzione totale?
		
		PrintWriter writer1 = null;
		PrintWriter writer2 = null;
		PrintWriter writer3 = null;
		try
		{
			String sdcardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
			writer1 = new PrintWriter(sdcardPath + "/" + allPosesPath);
			writer2 = new PrintWriter(sdcardPath + "/" + keyframePosesPath);
			writer3 = new PrintWriter(sdcardPath + "/" + timePath);
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
			System.exit(1);
		}

		for (DataTrajectory trajectory : allPoses)
		{
			String string =	(new BigDecimal(trajectory.getTimestamp())).setScale(6, BigDecimal.ROUND_HALF_UP).toPlainString() + " " + 
					(new BigDecimal(trajectory.getTranslation().getX())).setScale(6, BigDecimal.ROUND_HALF_UP).toPlainString() + " " +
					(new BigDecimal(trajectory.getTranslation().getY())).setScale(6, BigDecimal.ROUND_HALF_UP).toPlainString() + " " +
					(new BigDecimal(trajectory.getTranslation().getZ())).setScale(6, BigDecimal.ROUND_HALF_UP).toPlainString() + " " +
					(new BigDecimal(trajectory.getRotation().getX())).setScale(6, BigDecimal.ROUND_HALF_UP).toPlainString() + " " + 
					(new BigDecimal(trajectory.getRotation().getY())).setScale(6, BigDecimal.ROUND_HALF_UP).toPlainString() + " " + 
					(new BigDecimal(trajectory.getRotation().getZ())).setScale(6, BigDecimal.ROUND_HALF_UP).toPlainString() + " " + 
					(new BigDecimal(trajectory.getRotation().getW())).setScale(6, BigDecimal.ROUND_HALF_UP).toPlainString();
			writer1.println(string);
		}

		for (DataTrajectory trajectory : keyframePoses)
		{
			String string = (new BigDecimal(trajectory.getTimestamp())).setScale(6, BigDecimal.ROUND_HALF_UP).toPlainString() + " " + 
					(new BigDecimal(trajectory.getTranslation().getX())).setScale(6, BigDecimal.ROUND_HALF_UP).toPlainString() + " " +
					(new BigDecimal(trajectory.getTranslation().getY())).setScale(6, BigDecimal.ROUND_HALF_UP).toPlainString() + " " +
					(new BigDecimal(trajectory.getTranslation().getZ())).setScale(6, BigDecimal.ROUND_HALF_UP).toPlainString() + " " +
					(new BigDecimal(trajectory.getRotation().getX())).setScale(6, BigDecimal.ROUND_HALF_UP).toPlainString() + " " + 
					(new BigDecimal(trajectory.getRotation().getY())).setScale(6, BigDecimal.ROUND_HALF_UP).toPlainString() + " " + 
					(new BigDecimal(trajectory.getRotation().getZ())).setScale(6, BigDecimal.ROUND_HALF_UP).toPlainString() + " " + 
					(new BigDecimal(trajectory.getRotation().getW())).setScale(6, BigDecimal.ROUND_HALF_UP).toPlainString();
			writer2.println(string);
		}

		writer3.println((float)(System.currentTimeMillis() - milStart) / 1000F);

		writer1.close();
		writer2.close();
		writer3.close();
	}
	
	/**
	 * Creates the texture data from the depth and RGB images, with a fixed x2 subsampling.
	 * @param data The input BGR-D data.
	 * @param dataTextureDepth The output depth texture.
	 * @param dataTextureRGB The output RGB texture.
	 */
	private void createTextureDepthAndRGB(DataBGRD data, byte[] dataTextureDepth, byte[] dataTextureRGB)
	{
		createTextureDepthAndRGB(data, 2, dataTextureDepth, dataTextureRGB);
	}
	
	/**
	 * Creates the texture data from the depth and RGB images, with a certain amount of subsampling.
	 * @param data The input BGR-D data.
	 * @param amount The subsampling amount.
	 * @param dataTextureDepth The output depth texture.
	 * @param dataTextureRGB The output RGB texture.
	 */
	private void createTextureDepthAndRGB(DataBGRD data, int amount, byte[] dataTextureDepth, byte[] dataTextureRGB)
	{
		int dataHeight = data.getHeight();
		int dataWidth = data.getWidth();
		
		byte[] pixels = data.getBGR();
		float[] depthsP = data.getDepth();
		
		int k = 0, kD = 0;
		float f = 0, maxDepth = data.getMaxDepth();
		for (int i = 0, iD = 0; i < dataHeight; i += amount, iD += dataWidth * amount)
		{
			for (int j = 0, iC = iD * 3; j < dataWidth; j += amount, iC += 3 * amount)
			{
				f = depthsP[iD + j];
				dataTextureDepth[kD] = (byte)(255.0F * (f / maxDepth));
				kD++;
				
				dataTextureRGB[k] = pixels[iC + 2];
				k++;
				dataTextureRGB[k] = pixels[iC + 1];
				k++;
				dataTextureRGB[k] = pixels[iC];
				k++;
			}
		}
	}
	
	private void createTextureDepth16UAndRGB(DataBGRD data, int amount, short[] dataTextureDepth, byte[] dataTextureRGB)
	{
		int dataHeight = data.getHeight();
		int dataWidth = data.getWidth();
		
		byte[] pixels = data.getBGR();
		float[] depthsP = data.getDepth();
		
		int k = 0, kD = 0;
		float f = 0;
		for (int i = 0, iD = 0; i < dataHeight; i += amount, iD += dataWidth * amount)
		{
			for (int j = 0, iC = iD * 3; j < dataWidth; j += amount, iC += 3 * amount)
			{
				f = depthsP[iD + j];
				dataTextureDepth[kD] = (short)(f * 1000);
				kD++;
				
				dataTextureRGB[k] = pixels[iC + 2];
				k++;
				dataTextureRGB[k] = pixels[iC + 1];
				k++;
				dataTextureRGB[k] = pixels[iC];
				k++;
			}
		}
	}
	
	/*private void createTextureDepth3AndRGB(DataBGRD data, int amount, byte[] dataTextureDepth, byte[] dataTextureRGB)
	{
		int dataHeight = data.getHeight();
		int dataWidth = data.getWidth();
		
		byte[] pixels = data.getBGR();
		float[] depthsP = data.getDepth();
		
		int k = 0, kD = 0;
		float f = 0, maxDepth = data.getMaxDepth();
		byte tempB;
		for (int i = 0, iD = 0; i < dataHeight; i += amount, iD += dataWidth * amount)
		{
			for (int j = 0, iC = iD * 3; j < dataWidth; j += amount, iC += 3 * amount)
			{
				f = depthsP[iD + j];
				tempB = (byte)(255.0F * (f / maxDepth));
				dataTextureDepth[kD] = tempB;
				kD++;
				dataTextureDepth[kD] = tempB;
				kD++;
				dataTextureDepth[kD] = tempB;
				kD++;
				
				dataTextureRGB[k] = pixels[iC + 2];
				k++;
				dataTextureRGB[k] = pixels[iC + 1];
				k++;
				dataTextureRGB[k] = pixels[iC];
				k++;
			}
		}
	}*/
	
	/**
	 * Creates the correct dataset format for the depth and RGB images.
	 * @param data The input BGR-D data.
	 * @param dataDepth The output depth data.
	 * @param dataRGB The output RGB data.
	 */
	private void createDatasetDepthAndRGB(DataBGRD data, short[] dataDepth, byte[] dataRGB)
	{
		int dataHeight = data.getHeight();
		int dataWidth = data.getWidth();
		
		byte[] pixels = data.getBGR();
		float[] depthsP = data.getDepth();
		
		int k = 0, kD = 0;
		float f = 0;
		for (int i = 0, iD = 0; i < dataHeight; i++, iD += dataWidth)
		{
			for (int j = 0, iC = iD * 3; j < dataWidth; j++, iC += 3)
			{
				f = depthsP[iD + j];
				dataDepth[kD] = (short)(f * 5000);
				kD++;
				
				dataRGB[k] = pixels[iC + 2];
				k++;
				dataRGB[k] = pixels[iC + 1];
				k++;
				dataRGB[k] = pixels[iC];
				k++;
			}
		}
	}
	
	/**
	 * Creates the point cloud resulting in the projection of the image points with the given depth.<br>
	 * It is possible to subsample the point cloud during the projection by specifying the amount of subsampling (2, 4, etc).
	 * @param data The image and depth data.
	 * @param amount The amount of subsampling. If 1 no subsampling is executed. Any other value must be a multiple of 2.
	 * @param pointCloudPoints The resulting points of the point cloud.
	 * @param pointCloudColors The resulting colors of the point cloud.
	 * @return An integer representing the number of points stored in the point cloud.
	 */
	private int createProjectedPointCloud(DataBGRD data, int amount, float[] pointCloudPoints, byte[] pointCloudColors)
	{
		if (amount != 1 && amount % 2 != 0)
			throw new IllegalArgumentException("Invalid amount");
		
		int dataHeight = data.getHeight();
		int dataWidth = data.getWidth();
		
		byte[] bgr = data.getBGR();
		float[] depth = data.getDepth();
		
		int k = 0;
		for (int i = 0, iD = 0; i < dataHeight; i += amount, iD += dataWidth * amount)
		{
			for (int j = 0, iDep = iD, iC = iD * 3; j < dataWidth; j += amount, iDep += amount, iC += 3 * amount)
			{
				float d = depth[iDep];
				if (d > 0)
				{
					pointCloudPoints[k] = j * d;
					pointCloudColors[k] = bgr[iC + 2];
					k++;
					pointCloudPoints[k] = i * d;
					pointCloudColors[k] = bgr[iC + 1];
					k++;
					pointCloudPoints[k] = d;
					pointCloudColors[k] = bgr[iC];
					k++;
				}
			}
		}
		
		return k;
	}
	
	/**
	 * Checks if the algorithm is executing.
	 * @return True if the algorithm is executing, false otherwise.
	 */
	public synchronized boolean isExecutingAlgorithm()
	{
		return executeAlgorithm;
	}
	
	/**
	 * Sets if execute the algorithm or not.
	 * @param executeAlgorithm If true the algorithm will be executed.
	 */
	public synchronized void setExecuteAlgorithm(boolean executeAlgorithm)
	{
		this.executeAlgorithm = executeAlgorithm;
	}
	
	/**
	 * Terminate the Application Manager.
	 */
	public void terminate()
	{
		terminate = true;
	}
	
	/**
	 * Saves the next RGB-D data captured by the algorithm.
	 */
	public synchronized void takePicture()
	{
		takePicture = true;
	}
	
	/**
	 * Starts RGB-D recording.
	 */
	public synchronized void startRecording()
	{
		recording = true;
	}
	
	/**
	 * Stops RGB-D recording
	 * @param calculateCameraPose If true it tries to calculate the camera pose using the solvePnP method of OpenCV.
	 */
	public synchronized void stopRecording(boolean calculateCameraPose)
	{
		if (recording && calculateCameraPose)
		{
			try
			{
				String sdCardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
				
				PrintWriter writer = new PrintWriter(sdCardPath + "/" + recordingPath + "/groundtruth.txt");
				
				BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(sdCardPath + "/" + 
						recordingPath + "/associatedFilesRecordingSLAM.txt")));
				
				String line = reader.readLine();
				boolean firstImage = true;
				EigenVector3D worldTranslation = new EigenVector3D();
				EigenMatrix3D worldRotation = new EigenMatrix3D();
				while (line != null)
				{
					if (!line.trim().equals(""))
					{
						String[] splittedLine = line.split(" ");
						String timestamp = splittedLine[2];
						Mat rgbImg = Highgui.imread(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + recordingPath 
													+ "/" + splittedLine[1]);
						
						EigenVector3D initialTranslation = new EigenVector3D();
						EigenMatrix3D initialRotation = new EigenMatrix3D();
						boolean result = CameraPoseUtils.estimateLocalObjectPose(rgbImg, 9, 6, initialTranslation, initialRotation);
						//boolean result = CameraPoseUtils.estimateLocalObjectPose(rgbImg, 6, 9, initialTranslation, initialRotation);
						
						if (result)
						{
							if (firstImage)
							{
								worldTranslation = new EigenVector3D(initialTranslation.getValue().clone());
								worldRotation = new EigenMatrix3D(initialRotation.getValue().clone());
								
								firstImage = false;
							}
							
							EigenVector3D finalTranslation = new EigenVector3D();
							EigenMatrix3D finalRotation = new EigenMatrix3D();
							CameraPoseUtils.estimateWorldCameraPose(worldTranslation, worldRotation, initialTranslation, initialRotation, 
									finalTranslation, finalRotation);
							
							DataTrajectory trajectory = new DataTrajectory(Double.parseDouble(timestamp), finalTranslation.toFloat(), 
									Quaternion.fromRotationMatrix(finalRotation.toFloat()));
							String string = (new BigDecimal(trajectory.getTimestamp())).setScale(6, BigDecimal.ROUND_HALF_UP).toPlainString() + " " + 
											(new BigDecimal(trajectory.getTranslation().getX())).setScale(6, BigDecimal.ROUND_HALF_UP).toPlainString() + " " +
											(new BigDecimal(trajectory.getTranslation().getY())).setScale(6, BigDecimal.ROUND_HALF_UP).toPlainString() + " " +
											(new BigDecimal(trajectory.getTranslation().getZ())).setScale(6, BigDecimal.ROUND_HALF_UP).toPlainString() + " " +
											(new BigDecimal(trajectory.getRotation().getX())).setScale(6, BigDecimal.ROUND_HALF_UP).toPlainString() + " " + 
											(new BigDecimal(trajectory.getRotation().getY())).setScale(6, BigDecimal.ROUND_HALF_UP).toPlainString() + " " + 
											(new BigDecimal(trajectory.getRotation().getZ())).setScale(6, BigDecimal.ROUND_HALF_UP).toPlainString() + " " + 
											(new BigDecimal(trajectory.getRotation().getW())).setScale(6, BigDecimal.ROUND_HALF_UP).toPlainString();
							writer.println(string);
							writer.flush();
						}
					}
					
					line = reader.readLine();
				}
				
				writer.close();
				reader.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		
		recording = false;
	}
}
