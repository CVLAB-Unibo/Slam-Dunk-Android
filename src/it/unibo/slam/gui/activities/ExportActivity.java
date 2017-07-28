package it.unibo.slam.gui.activities;

import it.unibo.slam.R;

import it.unibo.slam.export.ExchangedExportDataSingleton;
import it.unibo.slam.export.ExportData;
import it.unibo.slam.export.ExportFileFormat;
import it.unibo.slam.gui.opengl.rendering.SurfaceViewExport;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

/**
 * Export activity of this application. Used to show the point cloud to export and eventually save it on a file.
 */
public class ExportActivity extends Activity
{
	/**
	 * The view that will show the point cloud to export, together with its bounding box.
	 */
	private SurfaceViewExport view;
    
	/**
	 * File format used in the export operation.
	 */
	private ExportFileFormat fileFormat;
	
	/**
	 * File path, starting from the sdcard directory.
	 */
	private String filePath;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		
		// Export settings (file format and file path)
		int exportFileFormat = preferences.getInt(	getString(R.string.export_file_format_pref_key),
													getResources().getInteger(R.integer.export_file_format_default));
		fileFormat = ExportFileFormat.values()[exportFileFormat];
		filePath = preferences.getString(	getString(R.string.export_file_path_pref_key),
											getString(R.string.export_file_path_default));
		
		view = new SurfaceViewExport(this);
		ExportData data = ExchangedExportDataSingleton.getInstance().getExportData();
		view.getRendererExport().setPointCloudComponents(	data.getVertexArray(), 
															data.getColorArray());
		view.getRendererExport().setBoundingBoxComponents(	data.getBoundingBoxMin(),
															data.getBoundingBoxMax(),
															new float[] { 0.0F, 0.0F, 1.0F });
		setContentView(view);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present
		getMenuInflater().inflate(R.menu.export, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
	    // Handle item selection
	    switch (item.getItemId())
	    {
	        case R.id.action_save:
	        	save();
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	/**
	 * Method used to save the export data into a file.
	 */
	private void save()
	{
		// Saving to file (for testing)
		String completeFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + filePath;
		view.getRendererExport().savePointCloud(completeFilePath, fileFormat);
	}
}