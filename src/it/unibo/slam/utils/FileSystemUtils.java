package it.unibo.slam.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import android.content.Context;

/**
 * File system utilities.
 */
public final class FileSystemUtils
{
	/**
	 * Reads a file from the assets folder and writes it into the cache folder.
	 * @param fileName The file name.
	 * @param context The context.
	 * @throws IOException The file to read doesn't exist.
	 */
	public static final void writeFileFromAssetsToCache(String fileName, Context context) throws IOException
	{
		File destinationFile = new File(context.getCacheDir().getAbsolutePath() + "/" + fileName);
		PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(destinationFile, false)));
		BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open(fileName)));
		
		String line;
		while ((line = reader.readLine()) != null)
			writer.println(line);
		
		reader.close();
		writer.close();
	}
}
