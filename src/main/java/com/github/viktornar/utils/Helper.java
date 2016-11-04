package com.github.viktornar.utils;

import com.github.viktornar.model.Atlas;
import com.github.viktornar.model.Extent;
import org.apache.pdfbox.util.PDFMergerUtility;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;


public class Helper {
	public static double pixelsToInches(int sizeInPixels, int dpi) {
		int sizeInInches = sizeInPixels / dpi;
		return sizeInInches;
	}

	public static int inchesToPixels(double sizeInInches, int dpi) {
		int sizeInPixels = (int) (sizeInInches * dpi);
		return sizeInPixels;
	}

	public static Extent getExtentOfPage(Atlas atlas, int column, int row) {
		Double xmin = atlas.getExtent().getXmin();
		Double xmax = atlas.getExtent().getXmax();
		Double ymin = atlas.getExtent().getYmin();
		Double ymax = atlas.getExtent().getYmax();
		
		Double pXmax = xmin + ((xmax - xmin) /atlas.getColumns()) * column;
		Double pXmin = pXmax - (xmax - xmin) /atlas.getColumns();

		Double pYmin = ymax - ((ymax - ymin) / atlas.getRows()) * row;
		Double pYmax = pYmin + (ymax - ymin) / atlas.getRows();

		Extent extent = new Extent();
		extent.setXmin(pXmin);
		extent.setYmin(pYmin);
		extent.setXmax(pXmax);
		extent.setYmax(pYmax);

		return extent;
	}

	public static void mergePages(String atlasFolder, String atlasName) {
		try {
			PDFMergerUtility mergePdf = new PDFMergerUtility();
			File _folder = new File(atlasFolder);
			File[] filesInFolder;
			filesInFolder = _folder.listFiles();
			if (filesInFolder != null && filesInFolder.length > 0) {
				Arrays.sort(filesInFolder);
				
				for (File file : filesInFolder) {
					mergePdf.addSource(file);
				}
				
				mergePdf.setDestinationFileName(atlasFolder + File.separator + atlasName);
				mergePdf.mergeDocuments();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String[] getRandomlyNames(final int characterLength, final int generateSize) {
	    HashSet<String> list = new HashSet<>();
	    for (int i = 0; i < generateSize; ++i) {
	        String name = null;
	        do {
	            name = org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric(
	                    org.apache.commons.lang3.RandomUtils.nextInt(characterLength - 1, characterLength - 1) + 1);
	        } while(list.contains(name));
	        list.add(name);
	    }
	        
	    return list.toArray(new String[]{});
	}
	
	public static boolean createAtlasFolder(String atlasFolder) { 
		boolean result = false;
		File atlasDir = new File(atlasFolder);

		if (!atlasDir.exists()) {
		    try{
		    	atlasDir.mkdir();
		        result = true;
		    } 
		    catch(SecurityException se){
		        se.printStackTrace();
		    }        
		}
		return result;
	}

	public static boolean isFileExist(String id, String folder, String prefix) {
		boolean fileExist = false;

		File f = new File(
				String.format(
						"%s/%s/%s%s.pdf",
						folder,
						id,
						prefix,
						id
				)
		);

		if (f.exists() && !f.isDirectory()) {
			fileExist = true;
		}

		return fileExist;
	}

	public static ExecutorService getExecutorService(int poolSize) {
		final ThreadFactory threadFactory = runnable -> {
			Thread thr = new Thread(runnable);
			thr.setDaemon(false);
			return thr;
		};

		return Executors.newFixedThreadPool(poolSize, threadFactory);
	}
}
