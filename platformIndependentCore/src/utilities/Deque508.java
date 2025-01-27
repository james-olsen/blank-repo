package utilities;

import platformIndependentCore.core.AutomatedPage;
import platformIndependentCore.datafiles.ExcelDataFile;
import platformIndependentCore.exceptions.InvalidStateException;
import platformIndependentCore.scripts.TestScriptInterface;
import platformIndependentCore.utilities.ConfigProperties;

/**
 * <b>Name :</b> Deque508.java
 * <p>
 * <b>Generated :</b> Jun 16, 2020
 * <p>
 * <b>Description :</b> Provides an interface for adding Section 508 validation
 * to automated scripts
 * <p>
 * NOTE: This is a skeleton class that allows the integration to be in place
 * without compile errors when running without access to the Deque Axe DevTools
 * 508 libraries. In order to run with Section 508 validation the appropriate
 * project/libraries must be added.
 *
 * @since Jun 16, 2020
 * @author VBAAUSTAYLOL
 * @author VBADESDunigR
 */
public class Deque508 {
	/** Determines if scanFor508 is enabled */
	private static boolean scanFor508 = false;

	/** Stored instance for Singleton implementation */
	private static Deque508 deque508Obj;

	/**
	 * Creates an instance of Deque508 and associates the sheetName with it
	 *
	 * @param sheetName with page class testing information
	 */
	Deque508(String sheetName) {
		// TODO Auto-generated constructor stub.
		// This was added during conversion.
	}

	/**
	 * Method runs the Axe DevTools CLI Reporter tool which converts log files to a
	 * single HTML page report - the report should be found in the
	 * projectName_508_results directory named projectName.html
	 */
	public void runAxeReporter() {
		// do nothing
	}

	/**
	 * Closes the Selenium browser and runs the Axe DevTools CLI Reporter. Note:
	 * This method must be called at the end of every execution script that runs 508
	 * scans.
	 *
	 * @return String message returned from finalize508Scan
	 */
	public String finalize508Scan() {
		// Do nothing, 508 is not configured
		return "Section 508 not configured to run";
	}

	/**
	 * Closes the Selenium browser and runs the Axe DevTools CLI Reporter. Note:
	 * This method must be called at the end of every execution script that runs 508
	 * scans.
	 *
	 * @param currentSheetName the sheet to use in the ExcelDataFile
	 * @return String message returned from finalize508Scan
	 */
	public String finalize508Scan(String currentSheetName) {
		// Do nothing, 508 is not configured
		return "Section 508 not configured to run";
	}

	/**
	 * Static method to get instance of the Deque508 object.
	 *
	 * @param sheetName the sheet to use in the ExcelDataFile
	 * @return Deque508 instance of the Deque508 class
	 */
	public static Deque508 getInstance(String sheetName) {
		if (deque508Obj == null) {
			deque508Obj = new Deque508(sheetName);
		}
		return deque508Obj;
	}

	/**
	 * When using this Section 508 skeleton implementation, SECTION_508_SCAN_ENABLED
	 * should be configured with a value of FALSE. Therefore, this method will
	 * return false.
	 *
	 * However, if SECTION_508_SCAN_ENABLED is set to TRUE, and the workspace
	 * environment is not properly configured, an exception will be thrown
	 * indicating as such.
	 *
	 * @return boolean FALSE; 508 Scans should not be enabled with this Section 508
	 *         implementation.
	 * @throws InvalidStateException if configured to run but we are not picking up
	 *                               the correct Section 508 implementation and
	 *                               libraries
	 */
	public static boolean isScanFor508Enabled() {
		// If configured to run, but hitting this skeleton class, throw an exception
		if (ConfigProperties.getValue(ConfigProperties.SECTION_508_SCAN_ENABLED).equalsIgnoreCase("TRUE")) {
			throw new InvalidStateException(
					"You are configured to run 508 testing, but do not have your workspace configured properly. Please verify you added the appropriate project (ati508Selenium).");
		}
		// Will always return false since this is the skeleton class
		return scanFor508;
	}

	/**
	 * Runs Section 508 Scan on the specified page, but within the specified section
	 * scanWithIn
	 *
	 * @param script     The test script instance this page is being tested in
	 * @param pageToScan HTML Page Instance, class name must line up with page
	 *                   listed in ScannedPages508 excel file, will also be the name
	 *                   of the log file
	 * @param scanWithIn HTML Page section to scan within. Scans within specific
	 *                   section, not entire page.
	 * @return String message from the scan
	 */
	public String scanFor508Compliance(TestScriptInterface script, AutomatedPage pageToScan, String scanWithIn) {
		return "Section 508 not configured to run";
	}

	/**
	 * Sets the data file used to track scanned pages
	 *
	 * @param dataFile the ExcelDataFile for 508 testing
	 */
	public void setDataFile(ExcelDataFile dataFile) {
		// do nothing
	}

}
