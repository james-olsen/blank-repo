package platformIndependentCore.core;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import platformIndependentCore.utilities.ConfigProperties;
import seleniumCore.SeleniumHelper;

/**
 *
 * Class to help perform certain automation tasks.
 *
 *
 * Developer Note: In the future we may want to refactor how the helper classes
 * are implemented. Maybe have them extend the AutomationHelper, and then just
 * add custom implementations as needed
 *
 * @author VBAAUSTAYLOL
 *
 */
public abstract class AutomationHelper extends ToolManager {
	/** Logger instance for this class */
	static Logger log = LogManager.getLogger(SeleniumHelper.class.getName());

	/**
	 * Will use the default application to open the specified file based on the file
	 * type
	 *
	 * @param file instance to open
	 */
	public static void open(File file) {
		Desktop dt = Desktop.getDesktop();
		try {
			dt.open(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Will take in a set of String values and search the java class path for a line
	 * that contains all of the Strings that were passed in. It will return the
	 * first matching line, truncating the value after the last occurrence of the
	 * last String supplied (so the order you pass in your values here matters. Pass
	 * them in the order you expect them to appear in the class path value
	 *
	 * @param textToMatch in classpath
	 * @return String first matching line, truncating after the last occurance of
	 *         the last String supplied in textToMatch
	 */
	public static String getClassPath(String... textToMatch) {
		int numCriteria = textToMatch.length;
		String lastCriteria = textToMatch[numCriteria - 1];
		String path = "";
		String classpath = System.getProperty("java.class.path");
		String[] classpathEntries = classpath.split(File.pathSeparator);
		for (String c : classpathEntries) {
			boolean match = true;
			for (String text : textToMatch) {
				if (!c.contains(text)) {
					match = false;
					break;
				}
			}
			if (match) {
				path = c.substring(0, c.lastIndexOf(lastCriteria) + lastCriteria.length() + 1);
				break;
			}
		}
		return path;
	}

	/**
	 * Will return the absolute path for a temporary reset file for the given class
	 * name
	 *
	 * @param className to get the reset file name for
	 * @return String path to reset file
	 */
	public static String getResetFilePath(String className) {
		return getTestResultsPath() + className + ".reset";
	}

	/**
	 * Will return the absolute path for the test results folder
	 *
	 * @return String test results folder path
	 */
	public static String getTestResultsPath() {
		File currDir = new File("");
		// Check the config file for a custom location, otherwise it will be
		// "testResults" folder in the current working directory
		String filePath = ConfigProperties.getValue(ConfigProperties.RESULTS_FOLDER,
				currDir.getAbsolutePath() + "\\testResults\\");
		File resultsPath = new File(filePath);
		// Make sure the folder exists before returning the path
		if (!resultsPath.exists()) {
			// if it is not found, create the folder
			resultsPath.mkdirs();
		}
		return filePath;
	}

	/**
	 * Method to make a String safe to be used as a file name. Will replace special
	 * characters with text equivalents
	 *
	 * @param originalValue to be converted
	 * @return String safe for file names
	 */
	public static String getFileNameSafeString(String originalValue) {
		return originalValue.replace("<", "LT").replace(">", "GT").replace("=", "EQ").replace(":", "");
	}

	// public static void killProcess(String process) {
	// try {
	// Process killProcess = Runtime.getRuntime().exec("taskkill /F /IM " +
	// process);
	//
	// } catch (IOException e) {
	//// logError(
	//// "All " + process + " processes may not have been killed. Encountered
	// exception: " +
	// e.getMessage());
	//// throw new WrappedException(e);
	// }
	// //Check to make sure the process was killed
	// int numTries = 0;
	// boolean processToKillPresent;
	//
	// // This loop will wait for up to 30 seconds to ensure the process
	// // has been killed
	// do{
	// processToKillPresent = false;
	// try {
	// Thread.sleep(500);
	// } catch (InterruptedException e1) {
	// // TODO Auto-generated catch block
	// e1.printStackTrace();
	// }
	// try {
	// numTries++;
	// String line;
	// Process p = Runtime.getRuntime().exec(System.getenv("windir")
	// +"\\system32\\"+"tasklist.exe");
	// BufferedReader input = new BufferedReader(new
	// InputStreamReader(p.getInputStream()));
	// while ((line = input.readLine()) != null) {
	// if(line.contains(process)){
	// processToKillPresent = true;
	// break;
	// }
	// }
	// input.close();
	// p.destroyForcibly();
	// Runtime.getRuntime().exec("taskkill /F /IM " + process);
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// }while (processToKillPresent && numTries < 60);
	//
	//
	//
	// }
}
