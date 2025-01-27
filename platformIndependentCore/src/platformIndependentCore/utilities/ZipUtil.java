package platformIndependentCore.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * <b>Name :</b> ZipUtil.java
 * <p>
 * <b>Generated :</b> Sep 22, 2020
 * <p>
 * <b>Description :</b>Utility class to create a zip file for a folder.
 * <p>
 *
 * @since Sep 22, 2020
 * @author VBAAUSTAYLOL
 */
public class ZipUtil {

	/**
	 * Will zip the specified source folder and all of its subfolders and contained
	 * files in a zip file with the name specified
	 *
	 * @param sourceFolder       folder that will be zipped
	 * @param destinationZipFile location and name of the zip file to be created
	 * @param foldersToExclude   zero or more Strings for folder to exclude fromt he
	 *                           file list that will be zipped
	 * @return boolean returns true if the zip action was successful; false if it
	 *         was not
	 */
	public static boolean zipFolder(String sourceFolder, String destinationZipFile, String... foldersToExclude) {
		List<String> fileList = generateFileList(new File(sourceFolder), sourceFolder, new ArrayList<String>(),
				foldersToExclude);
		byte[] buffer = new byte[1024];
		String source = new File(sourceFolder).getName();
		FileOutputStream fos = null;
		ZipOutputStream zos = null;
		boolean noExceptions = true;
		try {
			fos = new FileOutputStream(destinationZipFile);
			zos = new ZipOutputStream(fos);
			FileInputStream in = null;

			for (String file : fileList) {
				ZipEntry ze = new ZipEntry(source + File.separator + file);
				zos.putNextEntry(ze);
				try {
					in = new FileInputStream(sourceFolder + File.separator + file);
					int len;
					while ((len = in.read(buffer)) > 0) {
						zos.write(buffer, 0, len);
					}
				} finally {
					in.close();
				}
			}

			zos.closeEntry();
		} catch (IOException ex) {
			noExceptions = false;
			ex.printStackTrace();
		} finally {
			try {
				zos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return noExceptions;
	}

	/**
	 * Will create a list of all the files that exist under the specified folder.
	 * Will recursively build the list to contain all descendant files
	 *
	 * @param currentFolder        the current folder
	 * @param originalSourceFolder the top level folder
	 * @param fileList             list of files under the folder, will be built
	 *                             recursively
	 * @param foldersToExclude     the name of the folders to ignore from the
	 *                             returned List<String>
	 * @return List<String> list of files under the folder
	 */
	public static List<String> generateFileList(File currentFolder, String originalSourceFolder, List<String> fileList,
			String... foldersToExclude) {

		// Build an ArrayList from the String Array from varargs parameters
		ArrayList<String> excludeList = new ArrayList<String>();
		for (String folderName : foldersToExclude) {
			excludeList.add(folderName);
		}
		// add file only
		if (currentFolder.isFile()) {
			if (!isOnExcludeList(excludeList, currentFolder.getAbsolutePath().toString())) {
				fileList.add(generateZipEntry(originalSourceFolder, currentFolder.toString()));
			}
		}

		// if the currentFolder is a directory AND the name isn't in the exclude list,
		// then add it to the file list
		if (currentFolder.isDirectory() && !excludeList.contains(currentFolder.getName())) {
			String[] subNote = currentFolder.list();
			for (String filename : subNote) {
				fileList = generateFileList(new File(currentFolder, filename), originalSourceFolder, fileList,
						foldersToExclude);
			}
		}
		return fileList;
	}

	/**
	 * Loops through the specified array list to check if the specified filePath
	 * contains a folder on the exclude list
	 *
	 * @param foldersToExclude list of folders to exclude from zip file
	 * @param filePath         current folder's full file path
	 * @return boolean true if the filePath contains a folder on the exclude list;
	 *         false if it does not
	 */
	private static boolean isOnExcludeList(ArrayList<String> foldersToExclude, String filePath) {
		boolean exclude = false;

		for (String folder : foldersToExclude) {
			if (filePath.contains(folder)) {
				exclude = true;
			}
		}
		return exclude;
	}

	/**
	 * Will parse out the name and path of the file relative to the original folder
	 *
	 * @param sourceFolder the original top level folder
	 * @param file         current file
	 * @return String file name for zip entry
	 */
	private static String generateZipEntry(String sourceFolder, String file) {
		return file.substring(sourceFolder.length() + 1, file.length());
	}

}
