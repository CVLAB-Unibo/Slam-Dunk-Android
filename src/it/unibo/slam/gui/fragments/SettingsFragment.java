package it.unibo.slam.gui.fragments;

import java.util.StringTokenizer;

import it.unibo.slam.R;
import it.unibo.slam.export.ExportFileFormat;
import it.unibo.slam.gui.preferences.IntegerListPreference;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.widget.Toast;

/**
 * Fragment used for the settings of the application.
 */
public class SettingsFragment extends PreferenceFragment implements OnPreferenceChangeListener
{
	@Override
    public void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
        
        Preference exportFileFormatPreference = findPreference(getString(R.string.export_file_format_pref_key));
        exportFileFormatPreference.setOnPreferenceChangeListener(this);
        
        Preference exportFilePathPreference = findPreference(getString(R.string.export_file_path_pref_key));
        exportFilePathPreference.setOnPreferenceChangeListener(this);
    }

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue)
	{
		// Check the file extension and changes it in case it is different from the one set before
		if (preference.getKey().equals(getString(R.string.export_file_format_pref_key)))
		{
			ExportFileFormat fileFormat = ExportFileFormat.values()[(Integer)newValue];
			String extensionFileFormat = fileFormat.getExtension();
			
			EditTextPreference exportFilePathPreference = (EditTextPreference)findPreference(getString(R.string.export_file_path_pref_key));
			String path = exportFilePathPreference.getText();
			StringTokenizer tokenizer = new StringTokenizer(path);
			String pathWithoutExtension = tokenizer.nextToken(".");
			String extensionFromPath = tokenizer.nextToken();
			if (!extensionFromPath.equalsIgnoreCase(extensionFileFormat))
				exportFilePathPreference.setText(pathWithoutExtension + "." + extensionFileFormat);
			
			return true;
		}
		// Check if the extension is valid given the file format used
		else if (preference.getKey().equals(getString(R.string.export_file_path_pref_key)))
		{
			IntegerListPreference exportFileFormatPreference = (IntegerListPreference)findPreference(getString(R.string.export_file_format_pref_key));
			ExportFileFormat fileFormat = ExportFileFormat.values()[exportFileFormatPreference.getValue()];
			String extensionFileFormat = fileFormat.getExtension();
			
			if (newValue.toString().trim().isEmpty())
			{
				Toast.makeText(getActivity(), "Preference not saved: Invalid path provided", Toast.LENGTH_SHORT).show();
				return false;
			}
			StringTokenizer tokenizer = new StringTokenizer(newValue.toString());
			tokenizer.nextToken(".");
			if (!tokenizer.hasMoreTokens())
			{
				Toast.makeText(getActivity(), "Preference not saved: Invalid path provided", Toast.LENGTH_SHORT).show();
				return false;
			}
			String extensionFromPath = tokenizer.nextToken();
			if (!extensionFromPath.equalsIgnoreCase(extensionFileFormat))
			{
				Toast.makeText(getActivity(), "Preference not saved: Invalid extension provided", Toast.LENGTH_SHORT).show();
				return false;
			}
			else
				return true;
		}
		else
			return true;
	}
}
