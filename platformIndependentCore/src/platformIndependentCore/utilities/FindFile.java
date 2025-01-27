package platformIndependentCore.utilities;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Optional;

/**
 * Class to locate Files
 *
 * @author VBAAUSTAYLOL
 *
 */
public class FindFile {

	/**
	 * Will find the most recent file that starts with the provided string and will
	 * return the file name
	 *
	 * @param fileNameStartsWith to locate file
	 * @return String most recently modified file starting with provided string
	 */
	public static String getMostRecent(String fileNameStartsWith) {
		String downloadFolder = ConfigProperties.getValue("DOWNLOAD_FOLDER");
		if (downloadFolder.isEmpty()) {
			String home = System.getProperty("user.home");
			downloadFolder = home + "/Downloads/";
		}
		Path dir = Paths.get(downloadFolder); // specify your directory
		String fileName = "";
		Optional<Path> lastFilePath;
		try {
			lastFilePath = Files.list(dir).filter(f -> (!Files.isDirectory(f)
					&& (f.toString().contains(fileNameStartsWith) || f.toString().contains(fileNameStartsWith + " ("))))
					.max(Comparator.comparingLong(f -> f.toFile().lastModified()));
			if (lastFilePath.isPresent()) // your folder may be empty
			{
				fileName = lastFilePath.get().toString();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // finally get the last file using
			// simple comparator by lastModified
		return fileName; // field

	}
}
