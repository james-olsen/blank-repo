package platformIndependentCore.results;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import platformIndependentCore.utilities.ConfigProperties;
import platformIndependentCore.utilities.DateCalculator;
import platformIndependentCore.utilities.DateTimeHelper;

/**
 * <b>Name :</b> DailyLog.java
 * <p>
 * <b>Generated :</b> Jun 1, 2020
 * <p>
 * <b>Description :</b> Class to write out the results of a script execution to
 * the Daily Log
 * <p>
 * A new log will be created each day and will contain a summary of the results
 * for each execution script that completed
 * <p>
 * This class requires DAILY_LOGS_FOLDER to be set in config.properties and will
 * only record results for test scripts in the executionScripts folder
 * <p>
 *
 * @since Jun 1, 2020
 * @author VBAAUSTAYLOL
 */
public class DailyLog {

	/**
	 * Will write out the results for the script details provided to the Daily Log.
	 *
	 * @param scriptName   Script name
	 * @param startTime    Time that the script started
	 * @param endTime      Time that the script ended
	 * @param duration     Amount of time it took the script to complete
	 * @param vpTotal      Total number of VPs executed
	 * @param vpPass       Number of VPs that resulted in PASS status
	 * @param vpFail       Number of VPs that resulted in FAIL status
	 * @param exceptionCnt Number of exceptions thrown during the script execution
	 * @param htmlFile     Location of the full results log for this script
	 *                     execution
	 */
	public static void writeResults(String scriptName, String startTime, String endTime, String duration, int vpTotal,
			int vpPass, int vpFail, int exceptionCnt, String htmlFile) {
		// Get folder location for Daily Logs
		String dailyLogsLocation = ConfigProperties.getValue(ConfigProperties.DAILY_LOGS_FOLDER);
		// Daily logs default to off unless the location is set in ConfigProperties
		// only record scripts that are in the executionScripts folder, ignore others
		if (!dailyLogsLocation.isEmpty() && scriptName.contains("executionScripts")) {
			String delimiter = "~";
			String hostName = "";

			try {
				hostName = InetAddress.getLocalHost().getHostName();
			} catch (UnknownHostException e2) {
				e2.printStackTrace();
			}
			String fileName = dailyLogsLocation + "\\" + hostName + "_"
					+ DateCalculator.getCurrentDate(DateTimeHelper.MONTH3_DAY1_YEAR4) + "_DailyLog.csv";

			FileWriter writer = null;
			File dailyLogFile = new File(fileName);

			try {
				if (!dailyLogFile.exists()) {
					File filePath = new File(dailyLogsLocation);
					// make sure folders exist, make them if they are missing
					filePath.mkdirs();
					// if the log does not exist, create it
					dailyLogFile.createNewFile();
				}
				// create a FileWriter to output the results, set APPEND to TRUE
				writer = new FileWriter(dailyLogFile, true);
				// check to see if the existing log file has any content
				if (dailyLogFile.length() == 0) {
					// if the file was empty, write the header row
					writer.write("OVERALL" + delimiter + "SCRIPT" + delimiter + "HOST" + delimiter + "START_TIME"
							+ delimiter + "END_TIME" + delimiter + "DURATION" + delimiter + "VP_TOTAL" + delimiter
							+ "VP_PASS" + delimiter + "VP_FAIL" + delimiter + "EXCEPTIONS" + delimiter
							+ "FULL RESULTS LOG" + System.lineSeparator());
				}
				// Find the OVERALL status by making sure both the vpFail and exceptionCnt are 0
				String overall = exceptionCnt < 1 && vpFail < 1 ? "PASS" : "FAIL";
				// Now add the new row of data
				writer.write(overall + delimiter + scriptName + delimiter + hostName + delimiter + startTime + delimiter
						+ endTime + delimiter + duration + delimiter + vpTotal + delimiter + vpPass + delimiter + vpFail
						+ delimiter + exceptionCnt + delimiter + htmlFile + System.lineSeparator());
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (writer != null) {
					try {
						writer.flush();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					try {
						writer.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}
