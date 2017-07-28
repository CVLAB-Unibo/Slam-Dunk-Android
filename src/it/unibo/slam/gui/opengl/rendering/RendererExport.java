package it.unibo.slam.gui.opengl.rendering;

import it.unibo.slam.datatypes.eigen.typefloat.EigenVector3F;
import it.unibo.slam.export.ExportFileFormat;
import it.unibo.slam.export.PointCloudExporter;
import it.unibo.slam.gui.opengl.camera.ArcballCamera;
import it.unibo.slam.gui.opengl.camera.interfaces.Camera;
import it.unibo.slam.gui.opengl.model.BoundingBox;
import it.unibo.slam.gui.opengl.model.PointCloudStatic;
import it.unibo.slam.gui.opengl.shader.BoundingBoxShaderParams;
import it.unibo.slam.gui.opengl.shader.PointCloudShaderParams;
import it.unibo.slam.gui.opengl.shader.ShaderProgram;
import it.unibo.slam.gui.opengl.utils.GlUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import it.unibo.slam.R;

import android.content.Context;
import android.content.res.AssetManager;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;

/**
 * Renderer class used for drawing the user interface.
 */
public class RendererExport implements Renderer
{
	/**
	 * Android context mainly used to retrieve shader files at runtime.
	 */
	private Context context;
	
	/**
	 * Shader program for the point cloud rendering.
	 */
	private ShaderProgram shaderProgramPointCloud = null;
	
	/**
	 * Shader program for the bounding box rendering.
	 */
	private ShaderProgram shaderProgramBoundingBox = null;
	
	/**
	 * Handle of the ModelViewProjection matrix referred to the internal point cloud.
	 */
	private int uMVPMatrixPointCloudHandle = -1;
	
	/**
	 * Handle for the position values referred to the internal point cloud.
	 */
	private int aPositionPointCloudHandle = 0;
	
	/**
	 * Handle for the color values referred to the internal point cloud.
	 */
	private int aColorPointCloudHandle = 0;
	
	/**
	 * Handle of the ModelViewProjection matrix referred to the internal bounding box.
	 */
	private int uMVPMatrixBoundingBoxHandle = -1;
	
	/**
	 * Handle for the position values referred to the internal bounding box.
	 */
	private int aPositionBoundingBoxHandle = 0;
	
	/**
	 * Handle for the color values referred to the internal bounding box.
	 */
	private int uColorBoundingBoxHandle = 0;
	
	/**
	 * View matrix, taken from the camera (V).
	 */
	private float[] viewMatrix = new float[16];
	
	/**
	 * Projection matrix (P).
	 */
	private float[] projectionMatrix = new float[16];
	
	/**
	 * ViewProjection matrix (V * P).
	 */
	private float[] viewProjectionMatrix = new float[16];
	
	/**
	 * ModelViewProjection matrix (M * V * P).
	 */
	private float[] modelViewProjectionMatrix = new float[16];
	
	/**
	 * Vertical field of view (in degrees).
	 */
	private float vFov;
	
	/**
	 * Camera used to move in the current 3D scene.
	 */
	private Camera camera;
	
	/**
	 * Export point cloud.
	 */
	private PointCloudStatic pointCloudToExport = null;
	
	/**
	 * Bounding box of the point cloud.
	 */
	private BoundingBox boundingBox = null;
	
	/**
	 * True if the view projection matrix needs to be calculated again.
	 * This usually happens after a modification in the position of the camera.
	 */
	private boolean recomputeViewProjection;
	
	/**
	 * If set to true it enables the zoom fit.
	 */
	private boolean zoomFit;
	
	/**
	 * Basic constructor.
	 * @param context The Android context.
	 */
	public RendererExport(Context context)
	{
		this.context = context;
		Matrix.setIdentityM(viewMatrix, 0);
		Matrix.setIdentityM(projectionMatrix, 0);
		Matrix.setIdentityM(viewProjectionMatrix, 0);
		Matrix.setIdentityM(modelViewProjectionMatrix, 0);
		camera = new ArcballCamera(new EigenVector3F(0, 0, -1), new EigenVector3F(0, 0, 0), new EigenVector3F(0, -1, 0));
		recomputeViewProjection = false;
		zoomFit = true;
	}
	
	/**
	 * Sets the elements composing the internal point cloud.
	 * @param vertexArray The vertex array of the point cloud.
	 * @param colorArray The color array of the point cloud.
	 */
	public void setPointCloudComponents(float[] vertexArray, byte[] colorArray)
	{
		if (uMVPMatrixPointCloudHandle >= 0)
			pointCloudToExport = new PointCloudStatic(	new PointCloudShaderParams(aPositionPointCloudHandle, aColorPointCloudHandle), 
														vertexArray, colorArray);
		else
			pointCloudToExport = new PointCloudStatic(null, vertexArray, colorArray);
	}
	
	/**
	 * Sets the elements composing the internal bounding box.
	 * @param boundingBoxMin The minimum point of the bounding box.
	 * @param boundingBoxMax The maximum point of the bounding box.
	 * @param boundingBoxColor The color of the bounding box.
	 */
	public void setBoundingBoxComponents(float[] boundingBoxMin, float[] boundingBoxMax, float[] boundingBoxColor)
	{
		if (uMVPMatrixBoundingBoxHandle >= 0)
			boundingBox = new BoundingBox(	new BoundingBoxShaderParams(aPositionBoundingBoxHandle, uColorBoundingBoxHandle), 
											boundingBoxMin, boundingBoxMax, boundingBoxColor);
		else
			boundingBox = new BoundingBox(null, boundingBoxMin, boundingBoxMax, boundingBoxColor);
	}
	
	/**
	 * Zoom fit the camera using the bounding box extents.
	 */
	private void zoomFitCamera()
	{
		float sizeX = boundingBox.getMax().getX() - boundingBox.getMin().getX();
		float sizeY = boundingBox.getMax().getY() - boundingBox.getMin().getY();
		float sizeZ = boundingBox.getMax().getZ() - boundingBox.getMin().getZ();
		
		float maxSize = Math.max(Math.max(sizeX, sizeY), sizeZ);
		float minSize = Math.min(Math.min(sizeX, sizeY), sizeZ);
		
		if (maxSize == sizeX)
		{
			if (minSize == sizeY)
			{
				float ratio = sizeX / sizeZ;
				float hFov = 2 * (float)Math.toDegrees(Math.atan(Math.tan(Math.toRadians(vFov) / 2) * ratio));
				float cameraDistanceToSide = maxSize / (2 * (float)Math.tan(Math.toRadians(hFov) / 2));
				float cameraDistanceTotal = cameraDistanceToSide + minSize / 2;
				
				camera = new ArcballCamera(	new EigenVector3F(0, -cameraDistanceTotal, 0), 
						new EigenVector3F(0, 1 - cameraDistanceTotal, 0), 
						new EigenVector3F(0, 0, 1));
			}
			else
			{
				float ratio = sizeX / sizeY;
				float hFov = 2 * (float)Math.toDegrees(Math.atan(Math.tan(Math.toRadians(vFov) / 2) * ratio));
				float cameraDistanceToSide = maxSize / (2 * (float)Math.tan(Math.toRadians(hFov) / 2));
				float cameraDistanceTotal = cameraDistanceToSide + minSize / 2;
				
				camera = new ArcballCamera(	new EigenVector3F(0, 0, -cameraDistanceTotal), 
						new EigenVector3F(0, 0, 1 - cameraDistanceTotal), 
						new EigenVector3F(0, 1, 0));
			}
		}
		else if (maxSize == sizeY)
		{
			if (minSize == sizeX)
			{
				float cameraDistanceToSide = maxSize / (2 * (float)Math.tan(Math.toRadians(vFov) / 2));
				float cameraDistanceTotal = cameraDistanceToSide + minSize / 2;
				
				camera = new ArcballCamera(	new EigenVector3F(-cameraDistanceTotal, 0, 0), 
						new EigenVector3F(1 - cameraDistanceTotal, 0, 0), 
						new EigenVector3F(0, 1, 0));
			}
			else
			{
				float cameraDistanceToSide = maxSize / (2 * (float)Math.tan(Math.toRadians(vFov) / 2));
				float cameraDistanceTotal = cameraDistanceToSide + minSize / 2;
				
				camera = new ArcballCamera(	new EigenVector3F(0, 0, -cameraDistanceTotal), 
						new EigenVector3F(0, 0, 1 - cameraDistanceTotal), 
						new EigenVector3F(0, 1, 0));
			}
		}
		else
		{
			if (minSize == sizeX)
			{
				float ratio = sizeZ / sizeY;
				float hFov = 2 * (float)Math.toDegrees(Math.atan(Math.tan(Math.toRadians(vFov) / 2) * ratio));
				float cameraDistanceToSide = maxSize / (2 * (float)Math.tan(Math.toRadians(/*vFov*/hFov) / 2));
				float cameraDistanceTotal = cameraDistanceToSide + minSize / 2;
				
				camera = new ArcballCamera(	new EigenVector3F(-cameraDistanceTotal, 0, 0), 
						new EigenVector3F(1 - cameraDistanceTotal, 0, 0), 
						new EigenVector3F(0, 1, 0)/*new EigenVector3F(0, 0, 1)*/);
			}
			else
			{
				float cameraDistanceToSide = maxSize / (2 * (float)Math.tan(Math.toRadians(vFov) / 2));
				float cameraDistanceTotal = cameraDistanceToSide + minSize / 2;
				
				camera = new ArcballCamera(	new EigenVector3F(0, -cameraDistanceTotal, 0), 
						new EigenVector3F(0, 1 - cameraDistanceTotal, 0), 
						new EigenVector3F(0, 0, 1));
			}
		}
	}
	
	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config)
	{
		AssetManager assetManager = context.getAssets();
		String vertexShaderPointCloudFileName = context.getString(R.string.vertex_shader_point_cloud_file_name);
		String fragmentShaderPointCloudFileName = context.getString(R.string.fragment_shader_point_cloud_file_name);

		try
		{
			shaderProgramPointCloud = ShaderProgram.createFromStream(	assetManager.open(vertexShaderPointCloudFileName), 
																		assetManager.open(fragmentShaderPointCloudFileName));
		}
		catch (IOException e)
		{
			Logger.getLogger("RendererSLAM").severe("Error loading the shader program!");
			System.exit(1);
		}

		String vertexShaderBoundingBoxFileName = context.getString(R.string.vertex_shader_bounding_box_file_name);
		String fragmentShaderBoundingBoxFileName = context.getString(R.string.fragment_shader_bounding_box_file_name);

		try
		{
			shaderProgramBoundingBox = ShaderProgram.createFromStream(	assetManager.open(vertexShaderBoundingBoxFileName), 
																		assetManager.open(fragmentShaderBoundingBoxFileName));
		}
		catch (IOException e)
		{
			Logger.getLogger("RendererSLAM").severe("Error loading the shader program!");
			System.exit(1);
		}
		
		GLES20.glUseProgram(shaderProgramPointCloud.getId());
		
		uMVPMatrixPointCloudHandle = GLES20.glGetUniformLocation(shaderProgramPointCloud.getId(), "uMVPMatrix");
		aPositionPointCloudHandle = GLES20.glGetAttribLocation(shaderProgramPointCloud.getId(), "aPosition");
		aColorPointCloudHandle = GLES20.glGetAttribLocation(shaderProgramPointCloud.getId(), "aColor");
		
		uMVPMatrixBoundingBoxHandle = GLES20.glGetUniformLocation(shaderProgramBoundingBox.getId(), "uMVPMatrix");
		aPositionBoundingBoxHandle = GLES20.glGetAttribLocation(shaderProgramBoundingBox.getId(), "aPosition");
		uColorBoundingBoxHandle = GLES20.glGetUniformLocation(shaderProgramBoundingBox.getId(), "uColor");
		
		// Fix point cloud previously created
		if (pointCloudToExport != null)
			pointCloudToExport = new PointCloudStatic(	new PointCloudShaderParams(aPositionPointCloudHandle, aColorPointCloudHandle), 
														pointCloudToExport.getUntransformedVertexArray(),
														pointCloudToExport.getColorArray());
		
		// Fix bounding box previously created
		if (boundingBox != null)
			boundingBox = new BoundingBox(	new BoundingBoxShaderParams(aPositionBoundingBoxHandle, uColorBoundingBoxHandle),
											boundingBox.getMin().getValue(), boundingBox.getMax().getValue(), 
											boundingBox.getColor().getValue());
		
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		GLES20.glDepthFunc(GLES20.GL_LEQUAL);
		GLES20.glDepthMask(true);
		
		GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
		GLES20.glEnable(GLES20.GL_BLEND);
		
		GLES20.glClearColor(0.5F, 0.5F, 0.5F, 1.0F);
		GLES20.glClearDepthf(1.0F);
		
		viewMatrix = camera.getMatrix();
		
		recomputeViewProjection = true;
	}
	
	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height)
	{
		GLES20.glViewport(0, 0, width, height);
		
		float ratio = (float)width / (float)height;
		float near = 0.1F, far = 100F;
		vFov = 60F;
		
		projectionMatrix = GlUtils.gluPerspectiveMatrix(vFov, ratio, near, far);
		
		recomputeViewProjection = true;
	}
	
	@Override
	public void onDrawFrame(GL10 gl)
	{
		if (zoomFit && boundingBox != null && pointCloudToExport != null)
		{
			zoomFitCamera();
			zoomFit = false;
		}
		
		GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
		
		if (recomputeViewProjection)
		{
			viewMatrix = camera.getMatrix();
			Matrix.multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
			recomputeViewProjection = false;
		}
		
		// Drawing point cloud
		if (pointCloudToExport != null)
		{
			GLES20.glUseProgram(shaderProgramPointCloud.getId());
			Matrix.multiplyMM(modelViewProjectionMatrix, 0, viewProjectionMatrix, 0, pointCloudToExport.getModelMatrix(), 0);
			GLES20.glUniformMatrix4fv(uMVPMatrixPointCloudHandle, 1, false, modelViewProjectionMatrix, 0);
			pointCloudToExport.draw();
		}
		
		// Drawing bounding box
		if (boundingBox != null)
		{
			GLES20.glUseProgram(shaderProgramBoundingBox.getId());
			Matrix.multiplyMM(modelViewProjectionMatrix, 0, viewProjectionMatrix, 0, boundingBox.getModelMatrix(), 0);
			GLES20.glUniformMatrix4fv(uMVPMatrixBoundingBoxHandle, 1, false, modelViewProjectionMatrix, 0);
			boundingBox.draw();
		}
	}
	
	/**
	 * Rotates the camera of the amount considered in the input parameters.
	 * @param yaw Yaw angle.
	 * @param pitch Pitch angle.
	 * @param roll Roll angle.
	 */
	public void performRotation(float yaw, float pitch, float roll)
	{
		camera.rotate(yaw, pitch, roll);
		recomputeViewProjection = true;
	}
	
	/**
	 * Incrementally rotates the camera of the amount considered in the input parameters.
	 * @param yaw Yaw angle.
	 * @param pitch Pitch angle.
	 * @param roll Roll angle.
	 */
	public void performRotationIncremental(float yaw, float pitch, float roll)
	{
		camera.rotateIncremental(yaw, pitch, roll);
		recomputeViewProjection = true;
	}
	
	/**
	 * Ends the incremental rotation of the camera.
	 */
	public void endIncrementalRotation()
	{
		camera.endIncrementalRotation();
	}
	
	/**
	 * Zooms the camera of the amount specified as input.
	 * @param amount The amount to zoom.
	 */
	public void performZoom(float amount)
	{
		camera.zoom(amount);
		recomputeViewProjection = true;
	}
	
	/**
	 * Pans the camera of the amount specified in the input parameter.
	 * @param dx Amount of pan in the x direction.
	 * @param dy Amount of pan in the y direction.
	 */
	public void performPan(float dx, float dy)
	{
		camera.pan(-dx, -dy);
		recomputeViewProjection = true;
	}
	
	/**
	 * Saves the point cloud.
	 * @param fileName Path in which to save the point cloud.
	 * @param fileFormat File format.
	 */
	public void savePointCloud(String fileName, ExportFileFormat fileFormat)
	{
		List<PointCloudStatic> pointCloudList = new ArrayList<PointCloudStatic>();
		pointCloudList.add(pointCloudToExport);
		PointCloudExporter.exportTo(fileName, fileFormat, pointCloudList);
	}
	
	/**
	 * Requests the execution of a zoom fit operation.
	 */
	public void requestZoomFit()
	{
		zoomFit = true;
	}
}
