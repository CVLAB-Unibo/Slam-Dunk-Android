package it.unibo.slam.gui.opengl.rendering;

import it.unibo.slam.buffers.interfaces.ReadOnlyDoubleBuffer;
import it.unibo.slam.datatypes.DataFPS;
import it.unibo.slam.datatypes.DataGrayscale;
import it.unibo.slam.datatypes.DataRGB;
import it.unibo.slam.datatypes.DataNewPointCloudAndPoseUpdates;
import it.unibo.slam.datatypes.DataRendererAdditional;
import it.unibo.slam.datatypes.Pair;
import it.unibo.slam.datatypes.eigen.typedouble.EigenIsometry3D;
import it.unibo.slam.datatypes.eigen.typefloat.EigenMatrix4F;
import it.unibo.slam.datatypes.eigen.typefloat.EigenVector3F;
import it.unibo.slam.export.ExportData;
import it.unibo.slam.export.PointCloudExporter;
import it.unibo.slam.gui.opengl.camera.ArcballCamera;
import it.unibo.slam.gui.opengl.camera.interfaces.Camera;
import it.unibo.slam.gui.opengl.model.CameraPose;
import it.unibo.slam.gui.opengl.model.PointCloudStatic;
import it.unibo.slam.gui.opengl.model.TexturedSurface;
import it.unibo.slam.gui.opengl.shader.CameraPoseShaderParams;
import it.unibo.slam.gui.opengl.shader.PointCloudShaderParams;
import it.unibo.slam.gui.opengl.shader.ShaderProgram;
import it.unibo.slam.gui.opengl.shader.TexturedSurfaceShaderParams;
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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;

/**
 * Renderer class used for drawing the user interface.
 */
public class RendererSLAM implements Renderer
{
	/**
	 * Index of the image texture.
	 */
	private static int TEXTURE_IMAGE = 0;
	
	/**
	 * Index of the depth texture.
	 */
	private static int TEXTURE_DEPTH = 1;
	
	/**
	 * Index of the fps texture.
	 */
	private static int TEXTURE_FPS = 2;
	
	/**
	 * Point cloud double buffer. It gets filled when a new point cloud has been received, coming from the Application Manager.
	 */
	private ReadOnlyDoubleBuffer<DataNewPointCloudAndPoseUpdates> doubleBufferPointCloud = null;
	
	/**
	 * Double buffer for additional data. It updates for each new frame captures.
	 */
	private ReadOnlyDoubleBuffer<DataRendererAdditional> doubleBufferAdditional = null;
	
	/**
	 * Android context mainly used to retrieve shader files at runtime.
	 */
	private Context context;
	
	/**
	 * Shader program for the point cloud rendering.
	 */
	private ShaderProgram shaderProgramPointCloud = null;
	
	/**
	 * Shader program for the texture rendering.
	 */
	private ShaderProgram shaderProgramTexture = null;
	
	/**
	 * Handle of the ModelViewProjection matrix for the internal point clouds.
	 */
	private int uMVPMatrixHandle = 0;
	
	/**
	 * Handle for the position value of the internal point clouds.
	 */
	private int aPositionHandle = 0;
	
	/**
	 * Handle for the color value of the internal point clouds.
	 */
	private int aColorHandle = 0;
	
	/**
	 * Handle of the Orthogonal matrix for the textures.
	 */
	private int uOrthoMatrixHandle = 0;
	
	/**
	 * Handle for the texture id.
	 */
	private int uTextureHandle = 0;
	
	/**
	 * Handle for the position value of the textures.
	 */
	private int aPositionTextureHandle = 0;
	
	/**
	 * Handle for the texture coordinate value of the textures.
	 */
	private int aTexCoordinateHandle = 0;
	
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
	 * Orthogonal matrix.
	 */
	private float[] orthoMatrix = new float[16];
	
	/**
	 * Camera used to move in the current 3D scene.
	 */
	private Camera camera;
	
	/**
	 * Stored point clouds.
	 */
	private List<Pair<Double, PointCloudStatic>> pointClouds;
	
	/**
	 * True if the view projection matrix needs to be calculated again.
	 * This usually happens after a modification in the position of the camera.
	 */
	private boolean recomputeViewProjection;
	
	/**
	 * Bitmap in which to write the image texture.
	 */
	private Bitmap textureBitmapImage = null;
	
	/**
	 * Pixels of the image bitmap.
	 */
	private int[] textureBitmapImagePixels = null;
	
	/**
	 * Image texture.
	 */
	private TexturedSurface textureImage;
	
	/**
	 * Bitmap in which to write the depth texture.
	 */
	private Bitmap textureBitmapDepth = null;
	
	/**
	 * Pixels of the depth bitmap.
	 */
	private int[] textureBitmapDepthPixels = null;
	
	/**
	 * Depth texture.
	 */
	private TexturedSurface textureDepth;
	
	/**
	 * Bitmap for the FPS value texture.
	 */
	private Bitmap textureBitmapFPS = null;
	
	/**
	 * The canvas used to draw the FPS value.
	 */
	private Canvas canvasFPS = null;
	
	/**
	 * The size used for the font of the FPS canvas.
	 */
	private float paintSize = 32.0F;
	
	/**
	 * Size of the bitmap that will contain the FPS value.
	 */
	private int canvasBitmapSize;
	
	/**
	 * The FPS value texture.
	 */
	private TexturedSurface textureFPS;
	
	/**
	 * Image width.
	 */
	private int textureWidthImage;
	
	/**
	 * Image height.
	 */
	private int textureHeightImage;
	
	/**
	 * Depth width.
	 */
	private int textureWidthDepth;
	
	/**
	 * Depth height.
	 */
	private int textureHeightDepth;
	
	/**
	 * The SLAM device pose.
	 */
	private CameraPose devicePoseSLAM;
	
	/**
	 * The Kalman device pose.
	 */
	private CameraPose devicePoseKalman;
	
	/**
	 * Basic constructor.
	 * @param context The Android context.
	 */
	public RendererSLAM(Context context)
	{
		this.context = context;
		pointClouds = new ArrayList<Pair<Double, PointCloudStatic>>();
		Matrix.setIdentityM(viewMatrix, 0);
		Matrix.setIdentityM(projectionMatrix, 0);
		Matrix.setIdentityM(viewProjectionMatrix, 0);
		Matrix.setIdentityM(modelViewProjectionMatrix, 0);
		camera = new ArcballCamera(new EigenVector3F(0, 0, 0), new EigenVector3F(0, 0, 1), new EigenVector3F(0, -1, 0));
		recomputeViewProjection = false;
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

		String vertexShaderTextureFileName = context.getString(R.string.vertex_shader_texture_file_name);
		String fragmentShaderTextureFileName = context.getString(R.string.fragment_shader_texture_file_name);

		try
		{
			shaderProgramTexture = ShaderProgram.createFromStream(	assetManager.open(vertexShaderTextureFileName), 
																	assetManager.open(fragmentShaderTextureFileName));
		}
		catch (IOException e)
		{
			Logger.getLogger("RendererSLAM").severe("Error loading the shader program!");
			System.exit(1);
		}
		
		GLES20.glUseProgram(shaderProgramPointCloud.getId());
		
		uMVPMatrixHandle = GLES20.glGetUniformLocation(shaderProgramPointCloud.getId(), "uMVPMatrix");
		aPositionHandle = GLES20.glGetAttribLocation(shaderProgramPointCloud.getId(), "aPosition");
		aColorHandle = GLES20.glGetAttribLocation(shaderProgramPointCloud.getId(), "aColor");
		
		uOrthoMatrixHandle = GLES20.glGetUniformLocation(shaderProgramTexture.getId(), "uOrthoMatrix");
		uTextureHandle = GLES20.glGetUniformLocation(shaderProgramTexture.getId(), "uTexture");
		aPositionTextureHandle = GLES20.glGetAttribLocation(shaderProgramTexture.getId(), "aPosition");
		aTexCoordinateHandle = GLES20.glGetAttribLocation(shaderProgramTexture.getId(), "aTexCoordinate");
		
		// Fix point clouds previously created
		for (Pair<Double, PointCloudStatic> pointCloudPair : pointClouds)
		{
			PointCloudStatic pointCloud = pointCloudPair.getSecond();
			float[] modelMatrix = pointCloud.getModelMatrix();
			PointCloudStatic newPointCloud = new PointCloudStatic(	new PointCloudShaderParams(aPositionHandle, aColorHandle), 
																	pointCloud.getUntransformedVertexArray(),
																	pointCloud.getColorArray());
			newPointCloud.setModelMatrix(modelMatrix);
			pointCloudPair.setSecond(newPointCloud);
		}
		
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
		
		float widthF = (float)width;
		float heightF = (float)height;
		float ratio = widthF / heightF;
		float near = 0.1F, far = 100F;
		float fovy = 60F;
		
		projectionMatrix = GlUtils.gluPerspectiveMatrix(fovy, ratio, near, far);
		
		Matrix.orthoM(orthoMatrix, 0, 0, width, 0, height, -1, 1);
		
		float ratio43 = 4.0F / 3.0F;
		float imageAndDepthHeightOffset = (float)Math.floor(heightF * 0.3F);
		float imageAndDepthWidthOffset = (float)Math.floor(heightF * ratio43 * 0.3F);
		
		float[] imagePositionData = new float[]
		{
			0.0f, imageAndDepthHeightOffset,
			0.0f, 0.0f,
			imageAndDepthWidthOffset, imageAndDepthHeightOffset,
			0.0f, 0.0f,
			imageAndDepthWidthOffset, 0.0f,
			imageAndDepthWidthOffset, imageAndDepthHeightOffset
		};
		
		float[] imageTextureCoordinateData = new float[] 
		{
			0.0f, 0.0f,
			0.0f, 1.0f,
			1.0f, 0.0f,
			0.0f, 1.0f,
			1.0f, 1.0f,
			1.0f, 0.0f
		};
		
		textureImage = new TexturedSurface(	new TexturedSurfaceShaderParams(uTextureHandle, aPositionTextureHandle, aTexCoordinateHandle),
											TEXTURE_IMAGE, imagePositionData, imageTextureCoordinateData);
		
		float[] depthPositionData = new float[]
		{
			widthF - imageAndDepthWidthOffset, imageAndDepthHeightOffset,
			widthF - imageAndDepthWidthOffset, 0.0f,
			widthF, imageAndDepthHeightOffset,
			widthF - imageAndDepthWidthOffset, 0.0f,
			widthF, 0.0f,
			widthF, imageAndDepthHeightOffset
		};
		
		float[] depthTextureCoordinateData = new float[] 
		{
			0.0f, 0.0f,
			0.0f, 1.0f,
			1.0f, 0.0f,
			0.0f, 1.0f,
			1.0f, 1.0f,
			1.0f, 0.0f
		};
		
		textureDepth = new TexturedSurface(	new TexturedSurfaceShaderParams(uTextureHandle, aPositionTextureHandle, aTexCoordinateHandle),
											TEXTURE_DEPTH, depthPositionData, depthTextureCoordinateData);
		
		float minCoord = (float)Math.floor((height < width) ? height : width);
		float fpsOffset = (float)Math.floor(minCoord * 0.15F);
		paintSize = /*(float)Math.floor(minCoord * 0.1F)*/determineMaxTextSize("XX", minCoord * 0.07F);
		canvasBitmapSize = (int)fpsOffset;
		float[] fpsPositionData = new float[]
		{
			widthF - fpsOffset, heightF,
			widthF - fpsOffset, heightF - fpsOffset,
			widthF, heightF,
			widthF - fpsOffset, heightF - fpsOffset,
			widthF, heightF - fpsOffset,
			widthF, heightF
		};
		
		float[] fpsTextureCoordinateData = new float[] 
		{
			0.0f, 0.0f,
			0.0f, 1.0f,
			1.0f, 0.0f,
			0.0f, 1.0f,
			1.0f, 1.0f,
			1.0f, 0.0f
		};
		
		devicePoseSLAM = new CameraPose(new CameraPoseShaderParams(aPositionHandle, aColorHandle), 10, 0.3F, 0.03F,
										new byte[] { (byte)128, (byte)128, (byte)0 },
										new byte[] { (byte)0, (byte)128, (byte)128 },
										new byte[] { (byte)128, (byte)0, (byte)128 });
		devicePoseKalman = new CameraPose(	new CameraPoseShaderParams(aPositionHandle, aColorHandle), 10, 0.3F, 0.03F,
											new byte[] { (byte)255, (byte)0, (byte)0 },
											new byte[] { (byte)0, (byte)255, (byte)0 },
											new byte[] { (byte)0, (byte)0, (byte)255 });
		
		textureFPS = new TexturedSurface(	new TexturedSurfaceShaderParams(uTextureHandle, aPositionTextureHandle, aTexCoordinateHandle),
											TEXTURE_FPS, fpsPositionData, fpsTextureCoordinateData);
		
		recomputeViewProjection = true;
	}
	
	/**
	 * Retrieve the maximum text size to fit in a given width.
	 * @param str Text to check for size.
	 * @param maxWidth Maximum allowed width.
	 * @return The desired text size.
	 */
	private int determineMaxTextSize(String str, float maxWidth)
	{
	    int size = 0;
	    Paint paint = new Paint();

	    do
	    {
	        paint.setTextSize(++size);
	    }
	    while (paint.measureText(str) < maxWidth);

	    return size;
	}
	
	EigenIsometry3D cameraPose = null;
	EigenIsometry3D cameraPose2 = null;
	
	@Override
	public void onDrawFrame(GL10 gl)
	{
		GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
		
		if (doubleBufferPointCloud == null || doubleBufferAdditional == null)
			return;
		
		if (doubleBufferPointCloud.isDataValid())
		{
			// Adding the point cloud
			DataNewPointCloudAndPoseUpdates dpc = doubleBufferPointCloud.readData();
			PointCloudStatic pointCloud = new PointCloudStatic(	new PointCloudShaderParams(aPositionHandle, aColorHandle),
																dpc.getVertices(), dpc.getColors());
			pointCloud.setModelMatrix(dpc.getModelMatrix().getValue());
			addPointCloud(dpc.getTimestamp(), pointCloud);
			updatePointClouds(dpc.getMovedClouds());
			doubleBufferPointCloud.notifyReadFinished();
		}
		
		if (doubleBufferAdditional.isDataValid())
		{
			DataRendererAdditional additionalData = doubleBufferAdditional.readData();
			
			// Image texture creation
			
			DataRGB imageData = additionalData.getImageData();
			
			if (textureBitmapImage == null)
			{
				textureWidthImage = imageData.getWidth();
				textureHeightImage = imageData.getHeight();
				textureBitmapImage = Bitmap.createBitmap(textureWidthImage, textureHeightImage, Bitmap.Config.ARGB_8888);
				textureBitmapImagePixels = new int[textureWidthImage * textureHeightImage];
			}
			
			byte[] pixelsB = imageData.getRGB();
			for (int i = 0, j = 0; i < textureWidthImage * textureHeightImage; i++, j += 3)
			{
				textureBitmapImagePixels[i] = Color.rgb(pixelsB[j] & 0xFF, pixelsB[j + 1] & 0xFF, pixelsB[j + 2] & 0xFF);
			}
			textureBitmapImage.setPixels(textureBitmapImagePixels, 0, textureWidthImage, 0, 0, textureWidthImage, textureHeightImage);
			
			textureImage.setTexture(textureBitmapImage);
	        
	        // Depth texture creation
	        
	        DataGrayscale depthData = additionalData.getDepthData();
			
			if (textureBitmapDepth == null)
			{
				textureWidthDepth = depthData.getWidth();
				textureHeightDepth = depthData.getHeight();
				textureBitmapDepth = Bitmap.createBitmap(textureWidthDepth, textureHeightDepth, Bitmap.Config.ARGB_8888);
				textureBitmapDepthPixels = new int[textureWidthDepth * textureHeightDepth];
			}
			
			byte[] pixelsB2 = depthData.getGrayscale();
			int tempColor = 0;
			for (int i = 0, j = 0; i < textureWidthDepth * textureHeightDepth; i++, j++)
			{
				tempColor = pixelsB2[j] & 0xFF;
				textureBitmapDepthPixels[i] = Color.rgb(tempColor, tempColor, tempColor);
			}
			textureBitmapDepth.setPixels(textureBitmapDepthPixels, 0, textureWidthDepth, 0, 0, textureWidthDepth, textureHeightDepth);
			
			textureDepth.setTexture(textureBitmapDepth);
	        
	        // FPS texture creation
	        
	        DataFPS fpsData = additionalData.getFPSData();
	        
			if (textureBitmapFPS == null)
			{
				textureBitmapFPS = Bitmap.createBitmap(canvasBitmapSize, canvasBitmapSize, Bitmap.Config.ARGB_8888);
				canvasFPS = new Canvas(textureBitmapFPS);
			}
			
			textureBitmapFPS.eraseColor(Color.TRANSPARENT);
			Paint textPaint = new Paint();
			textPaint.setTextSize(paintSize);
			textPaint.setAntiAlias(true);
			textPaint.setColor(Color.YELLOW);
			textPaint.setTextAlign(Align.CENTER);
			canvasFPS.drawText("" + (int)fpsData.getFPS(), canvasBitmapSize / 2, canvasBitmapSize / 2, textPaint);
			
			textureFPS.setTexture(textureBitmapFPS);
	        
			cameraPose = additionalData.getCameraPose();
			cameraPose2 = additionalData.getCameraPose2();
			
	        doubleBufferAdditional.notifyReadFinished();
		}
		
		if (recomputeViewProjection)
		{
			viewMatrix = camera.getMatrix();
			Matrix.multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
			recomputeViewProjection = false;
		}
		
		// Drawing point clouds
		
		GLES20.glUseProgram(shaderProgramPointCloud.getId());
		
		synchronized (pointClouds)
		{
			for (Pair<Double, PointCloudStatic> pointCloudPair : pointClouds)
			{
				Matrix.multiplyMM(modelViewProjectionMatrix, 0, viewProjectionMatrix, 0, pointCloudPair.getSecond().getModelMatrix(), 0);
				GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, modelViewProjectionMatrix, 0);
				pointCloudPair.getSecond().draw();
			}
		}
		
		if (cameraPose2 != null)
		{
			//EigenMatrix3F rotationAroundZ = MatrixFactory.getRotationMatrixAroundAxis(new EigenVector3F(0, 0, 1), (float)Math.PI);
			
			//EigenMatrix3D rotationFinal = cameraPose2.getRotation();//new EigenMatrix3D();
			//cameraPose2.getRotation().multiplyWith(rotationAroundZ, rotationFinal);
			/*rotationFinal.set(-rotationFinal.get(0, 1), 0, 1);
			rotationFinal.set(-rotationFinal.get(1, 0), 1, 0);
			rotationFinal.set(-rotationFinal.get(0, 2), 0, 2);
			rotationFinal.set(-rotationFinal.get(2, 0), 2, 0);*/
			//cameraPose2.setRotation(rotationFinal);
			//cameraPose2.setTranslation(/*new EigenVector3D(0, 0, 0)*/cameraPose.getTranslation());
			
			devicePoseKalman.setModelMatrix(cameraPose2.getMatrix().toFloat().getValue());
			Matrix.multiplyMM(modelViewProjectionMatrix, 0, viewProjectionMatrix, 0, devicePoseKalman.getModelMatrix(), 0);
			GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, modelViewProjectionMatrix, 0);
			devicePoseKalman.draw();
			
			devicePoseSLAM.setModelMatrix(cameraPose.getMatrix().toFloat().getValue());
			Matrix.multiplyMM(modelViewProjectionMatrix, 0, viewProjectionMatrix, 0, devicePoseSLAM.getModelMatrix(), 0);
			GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, modelViewProjectionMatrix, 0);
			devicePoseSLAM.draw();
			
			/*FloatBuffer bufferCameraPoseVertex = ByteBuffer.allocateDirect(6 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
			bufferCameraPoseVertex.put(cameraPose.getTranslation().toFloat().getValue());
			EigenVectorD resultVecMul = new EigenVectorD(4);
			cameraPose.getMatrix().multiplyWith(new EigenVectorD(new double[] { 0, 0, 0.3, 1 }, 4), resultVecMul);
			bufferCameraPoseVertex.put((float)resultVecMul.get(0));
			bufferCameraPoseVertex.put((float)resultVecMul.get(1));
			bufferCameraPoseVertex.put((float)resultVecMul.get(2));
			bufferCameraPoseVertex.position(0);
			
			ByteBuffer bufferCameraPoseColor = ByteBuffer.allocateDirect(6).order(ByteOrder.nativeOrder());
			bufferCameraPoseColor.put((byte)255);
			bufferCameraPoseColor.put((byte)0);
			bufferCameraPoseColor.put((byte)0);
			bufferCameraPoseColor.put((byte)0);
			bufferCameraPoseColor.put((byte)0);
			bufferCameraPoseColor.put((byte)255);
			bufferCameraPoseColor.position(0);
			
			GLES20.glLineWidth(4);
			
			GLES20.glVertexAttribPointer(aPositionHandle, 3, GLES20.GL_FLOAT, false, 0, bufferCameraPoseVertex);
			GLES20.glEnableVertexAttribArray(aPositionHandle);
			
			GLES20.glVertexAttribPointer(aColorHandle, 3, GLES20.GL_UNSIGNED_BYTE, true, 0, bufferCameraPoseColor);
			GLES20.glEnableVertexAttribArray(aColorHandle);
			
			GLES20.glDrawArrays(GLES20.GL_LINES, 0, bufferCameraPoseColor.capacity() / 3);
			
			GLES20.glDisableVertexAttribArray(aColorHandle);
			GLES20.glDisableVertexAttribArray(aPositionHandle);
			
			EigenMatrix3F rotationAroundZ = MatrixFactory.getRotationMatrixAroundAxis(new EigenVector3F(0, 0, 1), (float)Math.PI);
			
			EigenMatrix3D rotationFinal = new EigenMatrix3D();
			cameraPose2.getRotation().multiplyWith(rotationAroundZ, rotationFinal);
			rotationFinal.set(-rotationFinal.get(0, 1), 0, 1);
			rotationFinal.set(-rotationFinal.get(1, 0), 1, 0);
			rotationFinal.set(-rotationFinal.get(0, 2), 0, 2);
			rotationFinal.set(-rotationFinal.get(2, 0), 2, 0);
			cameraPose2.setRotation(rotationFinal);
			
			FloatBuffer bufferCameraPoseVertex2 = ByteBuffer.allocateDirect(6 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
			bufferCameraPoseVertex2.put(cameraPose.getTranslation().toFloat().getValue());
			resultVecMul = new EigenVectorD(3);
			cameraPose2.getMatrix().multiplyWith(new EigenVectorD(new double[] { 0, 0, 0.3, 0 }, 4), resultVecMul);
			bufferCameraPoseVertex2.put((float)resultVecMul.get(0));
			bufferCameraPoseVertex2.put((float)resultVecMul.get(1));
			bufferCameraPoseVertex2.put((float)resultVecMul.get(2));
			bufferCameraPoseVertex2.position(0);
			
			ByteBuffer bufferCameraPoseColor2 = ByteBuffer.allocateDirect(6).order(ByteOrder.nativeOrder());
			bufferCameraPoseColor2.put((byte)255);
			bufferCameraPoseColor2.put((byte)255);
			bufferCameraPoseColor2.put((byte)0);
			bufferCameraPoseColor2.put((byte)0);
			bufferCameraPoseColor2.put((byte)255);
			bufferCameraPoseColor2.put((byte)255);
			bufferCameraPoseColor2.position(0);
			
			GLES20.glVertexAttribPointer(aPositionHandle, 3, GLES20.GL_FLOAT, false, 0, bufferCameraPoseVertex2);
			GLES20.glEnableVertexAttribArray(aPositionHandle);
			
			GLES20.glVertexAttribPointer(aColorHandle, 3, GLES20.GL_UNSIGNED_BYTE, true, 0, bufferCameraPoseColor2);
			GLES20.glEnableVertexAttribArray(aColorHandle);
			
			GLES20.glDrawArrays(GLES20.GL_LINES, 0, bufferCameraPoseColor2.capacity() / 3);
			
			GLES20.glDisableVertexAttribArray(aColorHandle);
			GLES20.glDisableVertexAttribArray(aPositionHandle);*/
		}
		
		// Drawing textures
		
		GLES20.glUseProgram(shaderProgramTexture.getId());
		
		GLES20.glDisable(GLES20.GL_DEPTH_TEST);
		
		GLES20.glUniformMatrix4fv(uOrthoMatrixHandle, 1, false, orthoMatrix, 0);
		textureImage.draw();
		textureDepth.draw();
		textureFPS.draw();
		
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		GLES20.glDepthFunc(GLES20.GL_LEQUAL);
		GLES20.glDepthMask(true);
	}
	
	/**
	 * Adds a new point cloud to the scene.
	 * @param timestamp The point cloud timestamp.
	 * @param pointCloud The point cloud object.
	 */
	public void addPointCloud(double timestamp, PointCloudStatic pointCloud)
	{
		synchronized (pointClouds)
		{
			pointClouds.add(new Pair<Double, PointCloudStatic>(timestamp, pointCloud));
		}
	}
	
	/**
	 * Updates the point clouds matrices.
	 * @param movedClouds The matrices of the moved point clouds with the associated timestamps used for the search.
	 */
	public void updatePointClouds(List<Pair<Double, EigenMatrix4F>> movedClouds)
	{
		synchronized (pointClouds)
		{
			for (Pair<Double, EigenMatrix4F> posePair : movedClouds)
			{
				for (Pair<Double, PointCloudStatic> pointCloudPair : pointClouds)
				{
					if (pointCloudPair.getFirst().equals(posePair.getFirst()))
					{
						pointCloudPair.getSecond().setModelMatrix(posePair.getSecond().getValue().clone());
						break;
					}
				}
			}
		}
	}
	
	/**
	 * Sets the double buffer used by the Renderer.
	 * @param doubleBufferPointCloud The point cloud double buffer.
	 * @param doubleBufferAdditional The additional double buffer used for the texture data.
	 */
	public void setDoubleBuffers(	ReadOnlyDoubleBuffer<DataNewPointCloudAndPoseUpdates> doubleBufferPointCloud,
									ReadOnlyDoubleBuffer<DataRendererAdditional> doubleBufferAdditional)
	{
		this.doubleBufferPointCloud = doubleBufferPointCloud;
		this.doubleBufferAdditional = doubleBufferAdditional;
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
	 * Calculates and returns the point cloud ready to be saved, together with its bounding box.
	 * @param resolution The resolution of the octree used in the creation of the point cloud.
	 * @return The data to export.
	 */
	public ExportData getDataToExport(float resolution)
	{
		List<PointCloudStatic> pointCloudList = new ArrayList<PointCloudStatic>();
		synchronized (pointClouds)
		{
			for (Pair<Double, PointCloudStatic> pointCloudPair : pointClouds)
				pointCloudList.add(pointCloudPair.getSecond());
		}
		
		ExportData data = PointCloudExporter.getPointCloudAndBoundingBoxComponentsToExport(pointCloudList, resolution);
		return data;
	}
}
