package platformIndependentCore.scripts;

import java.io.File;
import java.io.FileFilter;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.ComparisonFailure;

import platformIndependentCore.core.AutomationHelper;
import platformIndependentCore.core.CriteriaObject.REGEX;
import platformIndependentCore.core.Requirement;
import platformIndependentCore.core.ToolManager;
import platformIndependentCore.datafiles.ExcelDataFile;
import platformIndependentCore.events.Event;
import platformIndependentCore.events.Note;
import platformIndependentCore.events.ScreenShot;
import platformIndependentCore.events.VerificationPoint;
import platformIndependentCore.exceptions.MissingAutomationToolLibrariesException;
import platformIndependentCore.reporting.XrayServerReporting;
import platformIndependentCore.results.ResultsLogExcel;
import platformIndependentCore.results.ResultsLogHTML;
import platformIndependentCore.results.SaveAsXML;
import platformIndependentCore.utilities.ConfigProperties;
import platformIndependentCore.utilities.CoreDateTimeFormat;
import utilities.Deque508;

/**
 * Class to provide the interface for test scripts.
 *
 * This script wraps a TestScript instance that provides the concrete
 * implementation for executing (determined by current testing platform)
 *
 * This class is focused around how to kick off and run the script (and clean
 * up) versus the script instance which contains the logic to be used by the
 * test script itself.
 *
 * This class will also keep track of all the verification points executed
 * during the run.
 *
 * A major need for this manager class is that the TestScripts written for
 * projects can not extend a concrete implementation for a specific platform, so
 * the TestScript class must remain independent of any platform specific code
 *
 * @author VBAAUSTAYLOL
 */
public abstract class TestScriptManager extends ToolManager {
	/** Logger instance for this class */
	private static Logger log = LogManager.getLogger(TestScriptManager.class.getName());
	/** Script instance this will manage */
	private TestScriptInterface scriptInstance;
	/** List of Verification Points that have executed for the script */
	private ArrayList<VerificationPoint> vpList = new ArrayList<VerificationPoint>();
	/** Script name */
	private String scriptName = "";
	/** Exception value */
	private String exception = "";
	/** Base name for Results files */
	private String resultsFileNameBase = "";
	/** Folder for all Results files */
	private String resultsFolderPath = "";
	/** Current Execution Script TestDataId */
	String currentExecScriptTestDataId = "";

	/**
	 * Constructor will set the script instance
	 *
	 * @param scriptInstance2 script instance
	 */
	public TestScriptManager(TestScriptInterface scriptInstance2) {
		scriptInstance = scriptInstance2;
	}

	/**
	 * Will return the Script instance
	 *
	 * @return TestScriptInterface script instance
	 */
	protected TestScriptInterface getTestScript() {
		return scriptInstance;
	}

	/**
	 * Will set the current test data id
	 *
	 * @param testDataID data id for the current test
	 */
	public void setCurrentTestDataId(String testDataID) {
		currentExecScriptTestDataId = testDataID;
	}

	/**
	 * Will return the current test data id
	 *
	 * @return String test data id
	 */
	public String getCurrentTestDataId() {
		return currentExecScriptTestDataId;
	}

	/**
	 * Returns the name of this script instance
	 *
	 * @return String
	 */
	public String getScriptName() {
		if (scriptName == null || scriptName.isEmpty()) {
			scriptName = scriptInstance.getClass().getName();
		}

		return scriptName;
	}

	/**
	 * Will return the results for the script instance
	 *
	 * @return ScriptResults for the script instance
	 */
	public ScriptResults getScriptResults() {
		return scriptInstance.getResults();
	}

	/*
	 * SECTION FOR METHODS USED TO RUN TEST SCRIPTS
	 */

	/**
	 * @return the vpList
	 */
	public ArrayList<VerificationPoint> getVpList() {
		return vpList;
	}

	/**
	 * @param vpList the vpList to set
	 */
	public void setVpList(ArrayList<VerificationPoint> vpList) {
		this.vpList = vpList;
	}

	/**
	 * Add a Verification Point to the results
	 *
	 * @param vp verification point to add to results
	 */
	public void addVerificationPoint(VerificationPoint vp) {
		getVpList().add(vp);
		getTestScript().addEvent(vp);
	}

	/**
	 * Add a Note to the results
	 *
	 * @param note to add
	 */
	public void addNote(Note note) {
		scriptInstance.addEvent(note);
	}

	/**
	 * Add a screenshot to the results
	 *
	 * @param screenShot screenshot
	 */
	public void addScreenShot(ScreenShot screenShot) {
		scriptInstance.addScreenShot(screenShot);
	}

	/**
	 * @return the exception
	 */
	public String getException() {
		return exception;
	}

	/**
	 * Add an exception to the results
	 *
	 * @param exception the exception to set
	 */
	public void setException(Exception exception) {
		this.exception = exception.toString();
		if (!this.exception.startsWith("java.lang.RuntimeException: Modular Script")) {
			scriptInstance.addScreenShot("Exception", "SCREENSHOT for above Exception: " + exception.toString());
		}
		getScriptResults().addEvent(new Note(exception));
	}

	/**
	 * Will run the provided test script
	 *
	 * @param args Arguments
	 * @return ScriptResults
	 */
	public ScriptResults run(Arguments args) {
		XrayServerReporting xrayReport = new XrayServerReporting();

		if (xrayReport.isReportingOn()) {
			log.debug("Validating user entered Xray items from Config");
			xrayReport.validateTestPlan();
			xrayReport.validateVersion();

			if (args.containsKey(ScriptResults.XRAY_TEST_ID)) {
				scriptInstance.getResults().addData(ScriptResults.XRAY_TEST_ID,
						args.getString(ScriptResults.XRAY_TEST_ID));
				xrayReport.validateTests(scriptInstance.getResults().getData(ScriptResults.XRAY_TEST_ID));
			}

			if (args.containsKey(ScriptResults.XRAY_EXECUTION_ID)) {
				scriptInstance.getResults().addData(ScriptResults.XRAY_EXECUTION_ID,
						args.getString(ScriptResults.XRAY_EXECUTION_ID));
				xrayReport.validateTestExecution(scriptInstance.getResults().getData(ScriptResults.XRAY_EXECUTION_ID));
			}
		}
		String resultsFolder = args.containsKey(Arguments.RESULTS_FOLDER) ? args.getString(Arguments.RESULTS_FOLDER)
				: getResultsFolderPath();

		String currScriptName = scriptInstance.getClass().getName();
		boolean modular = false;
		if (currScriptName.equals(getScriptName())) {
			log.debug(">>> THIS IS THE CURRENT SCRIPT ");
		} else {
			log.debug(">>> MODULAR SCRIPT ");
			modular = true;
		}

		String exception = "";
		// Capture Script Start DateTimestamp
		Date startDate = new Date(System.currentTimeMillis());
		scriptInstance.setStartTime(startDate);
		scriptInstance.setResultsFolder(resultsFolder);
		try {
			setup();
			scriptInstance.testScript(args);
		} catch (Exception e) {
			e.printStackTrace();
			exception = "ENDED WITH EXCEPTION: " + e.toString();
			setException(e);
		} finally {
			// Capture Script Stop DateTimestamp
			Date stopDate = new Date(System.currentTimeMillis());
			scriptInstance.setStopTime(stopDate);
			scriptInstance.setDuration(startDate, stopDate);
			if (!modular) {
				onCompletion(exception, scriptInstance);
			}
		}

		return scriptInstance.getResults();
	}

	/**
	 * Will run a suite of test scripts as provided in the Excel file
	 *
	 * @param testSuiteFile contains test scripts to run
	 */
	protected void runSuite(ExcelDataFile testSuiteFile) {
		throw new MissingAutomationToolLibrariesException("Method not yet implemented for Selenium");
	}

	/*
	 * SECTION FOR METHODS USED TO RUN TEST SCRIPTS
	 */

	/**
	 * This test case was determined to be a modular script, so run the script as
	 * one
	 *
	 * @param args Arguments
	 * @return ScriptResults results
	 */
	public ScriptResults runModular(Arguments args) {
		ScriptResults results = runScript(args);
		results.setExecutionScript(false);

		return results;
	}

	/**
	 *
	 * @param args Arguments
	 * @return ScriptResults results
	 */
	public ScriptResults runScript(Arguments args) {

		// Capture Script Start DateTimestamp
		Date startDate = new Date(System.currentTimeMillis());
		if (args.containsKey(Arguments.RESULTS_FOLDER)) {
			scriptInstance.setResultsFolder(args.getString(Arguments.RESULTS_FOLDER));
		} else {
			scriptInstance.setResultsFolder(this.getResultsFolderPath());

		}

		try {
			scriptInstance.testScript(args);
		} catch (Exception exception) {
			log.catching(exception);
			scriptInstance.addNote(exception);
			String exceptionText = exception.toString();
			// If it is NOT the exception that was thrown from the modular then log add the
			// screenshot.
			if (!exceptionText.startsWith("java.lang.RuntimeException: Modular Script")) {
				scriptInstance.addScreenShot("Exception", "SCREENSHOT for above Exception: " + exceptionText);
			}
		}
		// Capture Script Stop DateTimestamp
		Date stopDate = new Date(System.currentTimeMillis());

		// Check if we need to do a 508 scan
		if (Deque508.isScanFor508Enabled()) {
			String axeLog = Deque508.getInstance(scriptInstance.getCurrent508SheetName())
					.finalize508Scan(scriptInstance.getCurrent508SheetName());
			if (axeLog != null && !axeLog.isEmpty()) {
				scriptInstance.addNote(axeLog);
			}
		}

		ScriptResults results = scriptInstance.getResults();

		// Need to set values in the results
		results.setStartTime(startDate);
		results.setDuration(startDate, stopDate);
		results.setStopTime(stopDate);
		results.setScriptName(getScriptName());
		scriptInstance.setResults(results);

		return results;
	}

	/**
	 * Run as an execution script
	 *
	 * @param args script arguments
	 * @return ScriptResults results
	 */
	public ScriptResults runExecution(Arguments args) {
		ScriptResults results = runScript(args);
		results.setExecutionScript(true);

		if (!results.isExecutionScript) {
			scriptInstance.addEvent(results);
		}

		return results;
	}

	/**
	 * Will execute a verification point of two strings. Will return true if they
	 * are the same or false if they are not
	 *
	 * @param vpName        name of the verification point
	 * @param expected      value
	 * @param actual        value
	 * @param requirement   can be null if no requirement
	 * @param executionNote Optional value to add to Results Log
	 * @return boolean TRUE if equals, FALSE if not
	 */
	protected boolean vpEquals(String vpName, String expected, String actual, Requirement requirement,
			String executionNote) {
		return vpMatch(vpName, expected, actual, REGEX.NO_REGEX, requirement, executionNote);
	}

	/**
	 * Will execute a verification point of two strings. Will return true if they
	 * are the same or false if they are not
	 *
	 * @param vpName        name of the verification point
	 * @param expected      value
	 * @param actual        value
	 * @param regex         type
	 * @param requirement   can be null if no requirement
	 * @param executionNote Optional value to add to Results Log
	 * @return boolean TRUE if match, FALSE if not
	 */
	protected boolean vpMatch(String vpName, String expected, String actual, REGEX regex, Requirement requirement,
			String executionNote) {

		Objects.requireNonNull(regex, "REGEX value must not be null");
		boolean equal;
		VerificationPoint vp = new VerificationPoint(vpName, expected, actual);
		vp.setRegex(regex);
		vp.setScriptName(getScriptName());
		String msg = "";
		try {
			getAutomationTool().vpMatches(expected, actual, regex);
			msg = " VP  REGEX(" + regex + ") PASSED: expected=" + expected + ", actual=" + actual;
			log.info(vpName + msg);
			equal = true;
		} catch (ComparisonFailure cfe) {
			// the values do not match
			equal = false;
			msg = "VP REGEX(" + regex + ") FAILED: expected=" + expected + ", actual=" + actual;
			log.warn(vpName + msg);
		}
		String note = "";

		if (requirement != null) {
			note += "<br>REQ: " + requirement.getReqID();
		}
		if (executionNote != null && !executionNote.isEmpty()) {
			note = executionNote + "<BR><BR>" + note;
		}
		vp.setExecutionNote(note);
		vp.setPass(equal);
		addVerificationPoint(vp);

		return equal;
	}

	/**
	 * Execute VP directly from the test script. Compares an actual String formatted
	 * date with a the expected format. If the actual String does not match exactly
	 * the format it would be changed to using the supplied format the test will
	 * fail.
	 *
	 * @param vpName             name of verification point
	 * @param expectedDateFormat The CoreDateTimeFormat object to determine desired
	 *                           format
	 * @param actualDateString   String to test if it conforms to provided format
	 * @param requirement        can be null if no requirement
	 * @param executionNote      Optional value to add to Results Log
	 * @return boolean TRUE if actualDateString is in expectedDateFormat, FALSE if
	 *         not
	 */
	protected boolean vpDate(String vpName, CoreDateTimeFormat expectedDateFormat, String actualDateString,
			Requirement requirement, String executionNote) {

		boolean sameFormat = false;
		String formattedString;
		VerificationPoint vp = new VerificationPoint(vpName, expectedDateFormat, actualDateString);
		vp.setScriptName(getScriptName());
		vp.setRegex(REGEX.DATE);
		String msg = "";
		String note = "";
		boolean equal = false;
		try {
			formattedString = expectedDateFormat.format(actualDateString);
			try {
				getAutomationTool().vpEquals(formattedString, actualDateString);
				msg = " VP PASSED: expected format=" + expectedDateFormat + ", actual=" + actualDateString;
				log.info(vpName + msg);
				equal = true;
			} catch (ComparisonFailure cfe) {
				// the values do not match
				equal = false;
				msg = "VP FAILED: expected format=" + expectedDateFormat + ", actual=" + actualDateString;
				log.warn(vpName + msg);
			}
		} catch (ParseException e) {
			sameFormat = vpEquals(vpName, expectedDateFormat.toString(), actualDateString, requirement, executionNote);
			note = "PARSE EXCEPTION\nExpected date format: " + expectedDateFormat.toString();

		}
		if (requirement != null) {
			if (!note.isEmpty()) {
				note += "<BR><BR>";

			}
			note += "REQ: " + requirement.getReqID();
		}
		vp.setExecutionNote(note);
		vp.setPass(equal);
		addVerificationPoint(vp);

		return sameFormat;
	}

	/**
	 * Will execute a sleep command for the time provided in seconds
	 *
	 * @param sleepTimeInSeconds sleep interval
	 */
	protected void sleep(double sleepTimeInSeconds) {
		try {
			Thread.sleep(TimeUnit.SECONDS.toMillis((long) sleepTimeInSeconds));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	/**
	 * @return int number of called scripts
	 */
	public int getNumberOfCalledScripts() {
		return scriptInstance.getNumberOfCalledScripts();
	}

	/**
	 * Returns the events for this test script.
	 *
	 * @return List{Event} events
	 */
	public List<Event> getEvents() {
		return scriptInstance.getResults().getEvents();
	}

	/**
	 * Returns the timestamp for the current time in yy-MM-dd_HHmmss format
	 *
	 * @return String timestamp in yy-MM-dd_HHmmss format
	 */
	private String getTimeStamp() {
		SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd_HHmmss");
		Timestamp timeStamp = new Timestamp(System.currentTimeMillis());
		return sdf.format(timeStamp);
	}

	/**
	 * Will generate the appropriate results logs
	 *
	 * @param scriptManager manager
	 * @return String results file path
	 */
	protected String generateResultsLog(TestScriptManager scriptManager) {
		String resultsLog = "";
		String resultsFolderName = getResultsFolderPath();

		if (scriptManager.getScriptResults().isMainSuiteTest()) {
			scriptManager.getScriptResults().setMainSuiteTest(true);
		}
		String scriptName = scriptManager.getScriptName();
		if (scriptName.startsWith("testScripts.")) {
			scriptName = scriptName.substring(scriptName.indexOf(".") + 1);
		}

		String status = scriptManager.getScriptResults().getStatus();
		String dataId = scriptManager.getScriptResults().getData(ScriptResults.DATA_ID);
		if (status.startsWith("FAIL")) {
			status = "FAIL" + "_";
		} else {
			status = "";
		}

		String scriptResultsName = status;
		if (dataId != null && !dataId.isEmpty()) {
			scriptResultsName += AutomationHelper.getFileNameSafeString(dataId) + "_";
		}

		scriptResultsName += scriptName;

		String scriptLogBase = resultsFolderName + "//" + scriptResultsName;
		String logType = ConfigProperties.getLogType();

		File xmlFile = new File(scriptLogBase + ".xml");
		SaveAsXML.save(xmlFile, scriptManager);

		// TODO = allow no logging
		if (logType.equalsIgnoreCase("html") || logType.equalsIgnoreCase("both") || logType.isEmpty()) {
			ResultsLogHTML resultsFile = new ResultsLogHTML(exception, xmlFile, scriptLogBase + ".html");
			resultsLog = resultsFile.getHtmlResultsFile().getAbsolutePath();
			AutomationHelper.open(resultsFile.getHtmlResultsFile());
		}
		if (logType.equalsIgnoreCase("excel") || logType.equalsIgnoreCase("both")) {
			log.debug("EXCEL LOG:" + scriptLogBase);
			// This getExcelLog call will generate the file
			ResultsLogExcel.getExcelLog(scriptLogBase);
			resultsLog = scriptLogBase + ".xlsx";
			AutomationHelper.open(new File(resultsLog));
		}
		return resultsLog;
	}

	/**
	 * Will return the location for Results files and folders
	 *
	 * @return String results folder path
	 */
	String getResultsFolderPath() {
		if (resultsFolderPath.isEmpty()) {
			resultsFolderPath = getResultsLogBaseName() + "_Logs";
		}
		return resultsFolderPath;
	}

	/**
	 * Will return the full path of the Results Log, but will not include the
	 * extension (such as .xml, .html, etc)
	 *
	 * @return String Results file name with path and no extension
	 */
	String getResultsLogBaseName() {
		if (resultsFileNameBase.isEmpty()) {
			String path = getScriptResults().getResultsFolder();
			if (path == null || path.isEmpty()) {
				path = AutomationHelper.getTestResultsPath();
			}
			String preservelogs = ConfigProperties.getValue(ConfigProperties.PRESERVE_LOGS);

			// only append date information if we are keeping this log once its no
			// longer current
			String script = getScriptName().substring(getScriptName().lastIndexOf(".") + 1);
			if (preservelogs.equalsIgnoreCase("true")) {
				resultsFileNameBase = path + getTimeStamp() + "_" + script;
			} else {
				resultsFileNameBase = path + script;
			}
		}
		return resultsFileNameBase;

	}

	/**
	 * Remove any existing reset files that were created for this script run
	 */
	protected void removeResetFiles() {
		File dir = new File(AutomationHelper.getTestResultsPath());
		FileFilter fileFilter = new WildcardFileFilter("*.reset");
		File[] files = dir.listFiles(fileFilter);
		for (File file : files) {
			file.delete();
		}
	}

	/**
	 * Executes code that must be run after the test script completes
	 *
	 * @param exception e
	 * @param script    completed
	 */
	protected void onCompletion(String exception, TestScriptInterface script) {
		// setScriptInstance(script);
		try {
			log.info("ON COMPLETION");
			List<Event> events = this.getEvents();
			for (Event e : events) {
				log.debug("               MGR EVENT: " + e.getEventType() + ", " + e.getScriptName());
			}

			events = this.getScriptResults().getEvents(); // getEvents();
			for (Event e : events) {
				log.debug("               SR EVENT: " + e.getEventType() + ", " + e.getScriptName());
			}

			events = this.getTestScript().getResults().getEvents(); // getEvents();
			for (Event e : events) {
				log.debug("               SCRIPT SR EVENT: " + e.getEventType() + ", " + e.getScriptName());
			}
			if (Deque508.isScanFor508Enabled()) {
				try {
					String axeLog = Deque508.getInstance(script.getCurrent508SheetName())
							.finalize508Scan(script.getCurrent508SheetName());
					if (axeLog != null && !axeLog.isEmpty()) {
						script.addNote(axeLog);
					}
				} catch (Exception e) {
					script.addNote("Error generating Section 508 scan");
				}
			}
			String resultsLog = generateResultsLog(script.getManager());
			if (ConfigProperties.getValue(ConfigProperties.XRAY_REPORTING, "FALSE").equalsIgnoreCase("TRUE")) {
				addReporting(script, resultsLog);
				// If this was kicked off from a test suite csv file, create the xray reference
				// file
				if (script.getResults().getData(ScriptResults.TEST_SCRIPTS_FILE) != null) {
					XrayServerReporting xrayReport = new XrayServerReporting();
					xrayReport.createXrayReferenceFile(script.getResults());
				}
			}
			log.debug("COMPLETED");
		} finally {
			removeResetFiles();
			tearDown();
		}
	}

	/*
	 * ABSTRACT METHODS THAT MUST BE IMPLEMENTED BY CONCRETE SUBCLASS
	 */

	/**
	 * Will do all necessary clean up as script is ending
	 */
	protected abstract void tearDown();

	/**
	 * Configures the browser
	 */
	public abstract void setBrowserProperties();

	/**
	 * Sets the length of time before the browser times out when looking for an
	 * object on the page.
	 *
	 * @param timeOutInSeconds time out
	 */
	public abstract void setTimeout(int timeOutInSeconds);

	/**
	 * Loads a browser with the base URL as outlined in the baseURL variable
	 *
	 * @param homeURL to load
	 */
	public abstract void loadPage(String homeURL);

	/**
	 * Will load the given URL
	 *
	 * @param url to load
	 */
	protected abstract void gotoURL(String url);

	/**
	 * Will log a warning message
	 *
	 * @param warningMessage to log
	 */
	protected abstract void logWarning(String warningMessage);

	/**
	 * Extending classes must implement this setup method with all code that is
	 * required before running the test script
	 */
	protected abstract void setup();

	/* *************** XRAY *********************/
	/**
	 * Will add Reporting to XRAY
	 *
	 * @param scriptInstance script to report back
	 * @param resultsLog     where the HTML results log is located
	 */
	private void addReporting(TestScriptInterface scriptInstance, String resultsLog) {
		String name = scriptInstance.getScriptName();
		XrayServerReporting xray = new XrayServerReporting();
		String testExecutionId = xray.reportResults(name, scriptInstance.getManager().getScriptResults(), resultsLog);
		scriptInstance.getResults().addData(ScriptResults.XRAY_EXECUTION_ID, testExecutionId);
	}

}