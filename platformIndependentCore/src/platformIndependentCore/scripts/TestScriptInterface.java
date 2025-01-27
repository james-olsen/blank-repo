package platformIndependentCore.scripts;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import platformIndependentCore.core.CriteriaObject.REGEX;
import platformIndependentCore.core.Requirement;
import platformIndependentCore.core.ToolManager;
import platformIndependentCore.datafiles.CsvDataFile;
import platformIndependentCore.datafiles.CsvFile;
import platformIndependentCore.datafiles.DataFile;
import platformIndependentCore.datafiles.ExcelDataFile;
import platformIndependentCore.events.Event;
import platformIndependentCore.events.Event.EVENT_TYPE;
import platformIndependentCore.events.Note;
import platformIndependentCore.events.ScreenShot;
import platformIndependentCore.exceptions.WrappedException;
import platformIndependentCore.reporting.XrayServerReporting;
import platformIndependentCore.utilities.ConfigProperties;
import platformIndependentCore.utilities.CoreDateTimeFormat;
import platformIndependentCore.utilities.DateCalculator;
import utilities.Deque508;
import utilities.DataFileHelper;

/**
 * Interface to define methods available for TestScripts
 *
 * @author VBAAUSTAYLOL
 */
public interface TestScriptInterface {
	/** List of ScriptResults for called scripts */
	List<ScriptResults> calledScriptResults = new ArrayList<ScriptResults>();
	/** results folder location */
	String resultsFolder = "";

	/** Execution type enum to handle different types of scripts */
	enum EXECUTION_TYPE {
		/** Modular type script */
		MODULAR,
		/** Execution type script */
		EXECUTION,
		/** Kickoff type of script - the one that initiated all others */
		KICKOFF
	}

	/** logger instance for this class */
	Logger log = LogManager.getLogger(TestScriptInterface.class.getName());

	/**
	 * Will set the Start Time for this script
	 *
	 * @param startDate Date/Time script started
	 */
	default void setStartTime(Date startDate) {
		getResults().setStartTime(startDate);
	}

	/**
	 * Will set the Stop Time for this script
	 *
	 * @param stopDate Date/Time script ended
	 */
	default void setStopTime(Date stopDate) {
		getResults().setStopTime(stopDate);
	}

	/**
	 * Sets how long the script ran
	 *
	 * @param startDate Date/Time script started
	 * @param stopDate  Date/Time script ended
	 */
	default void setDuration(Date startDate, Date stopDate) {
		getResults().setDuration(startDate, stopDate);
	}

	/**
	 * Returns the time this script started
	 *
	 * @return String start time
	 */
	default String getStartTime() {
		return getResults().getData(ScriptResults.START_TIME);
	}

	/**
	 * Returns the time this script stopped
	 *
	 * @return String stop time
	 */
	default String getStopTime() {
		return getResults().getData(ScriptResults.STOP_TIME);
	}

	/**
	 * Returns the amount of time this script ran
	 *
	 * @return String duration
	 */
	default String getDuration() {
		return getResults().getData(ScriptResults.DURATION);
	}

	/**
	 * Returns what type of script this is
	 *
	 * @return EXECUTION_TYPE script type
	 */
	default EXECUTION_TYPE getExecutionType() {
		return getResults().getExecutionType();
	}

	/**
	 * @return the script
	 */
	TestScriptManager getManager();

	// /**
	// * @param script the script to set
	// */
	// default void setScript(TestScriptManager script) {
	// this.script = script;
	// }

	/**
	 * @return the isModularScript
	 */
	boolean isModularScript();

	// /**
	// * @param isModularScript the isModularScript to set
	// */
	// default void setModularScript(boolean isModularScript) {
	// this.isModularScript = isModularScript;
	// results.setEventType(EVENT_TYPE.CALLEDSCRIPT);
	// }

	/**
	 * Will return the results of this test
	 *
	 * @return ScriptResults results
	 */
	ScriptResults getResults();

	/**
	 * @return the calledScriptResults
	 */
	default int getNumberOfCalledScripts() {
		int cnt = 0;
		if (calledScriptResults != null && !calledScriptResults.isEmpty()) {
			cnt = calledScriptResults.size();
		}
		return cnt;
	}

	/**
	 * Will add the Event to the Results object for this test script
	 *
	 * @param event to add to results
	 */
	default void addEvent(Event event) {
		// events.add(event);
		// getCalledScriptResults().add(event);
		getResults().addEvent(event);
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
	 */
	default void vpDateFormat(String vpName, CoreDateTimeFormat expectedDateFormat, String actualDateString) {
		getManager().vpDate(vpName, expectedDateFormat, actualDateString, null, "");
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
	 * @param requirement        Requirement to associate with this Verification
	 *                           Point
	 */
	default void vpDateFormat(String vpName, CoreDateTimeFormat expectedDateFormat, String actualDateString,
			Requirement requirement) {
		getManager().vpDate(vpName, expectedDateFormat, actualDateString, requirement, "");
	}

	/**
	 * Execute VP directly from the test script. Compares an String description for
	 * the desired date format with the actual string. If the actual String does not
	 * match exactly the format it would be changed to using the supplied format the
	 * test will fail.
	 *
	 * @param vpName               name of verification point
	 * @param expectedFormatString The String representation of the desired format
	 * @param actualDateString     String to test if it conforms to provided format
	 */
	default void vpDateFormat(String vpName, String expectedFormatString, String actualDateString) {
		getManager().vpDate(vpName, new CoreDateTimeFormat(expectedFormatString), actualDateString, null, "");

	}

	/**
	 * Execute VP directly from the test script. Compares an actual String formatted
	 * date with a the expected format. If the actual String does not match exactly
	 * the format it would be changed to using the supplied format the test will
	 * fail.
	 *
	 * @param vpName               name of verification point
	 * @param expectedFormatString The String representation of the desired format
	 * @param actualDateString     String to test if it conforms to provided format
	 * @param requirement          Requirement to associate with this Verification
	 *                             Point
	 */
	default void vpDateFormat(String vpName, String expectedFormatString, String actualDateString,
			Requirement requirement) {
		getManager().vpDate(vpName, new CoreDateTimeFormat(expectedFormatString), actualDateString, requirement, "");

	}

	/**
	 * Execute VP directly form the test script. Compares two String values, and
	 * looks to see if the actual string CONTAINS the expected string. The entire
	 * string does NOT have to match.
	 *
	 * @param vpName   name of verification point
	 * @param expected The value you expect to find in the Actual string
	 * @param actual   The actual String that should contain the Expected String
	 */
	default void vpContains(String vpName, String expected, String actual) {
		getManager().vpMatch(vpName, expected, actual, REGEX.CONTAINS, null, "");

	}

	/**
	 * Executes a verification point asserting the actual String contains the
	 * expected String
	 *
	 * @param vpName   name of the verification point
	 * @param expected The value you expect to find in the Actual string
	 * @param actual   The actual String that should contain the Expected String
	 * @param req      Requirement to associate with this Verification Point
	 */
	default void vpContains(String vpName, String expected, String actual, Requirement req) {
		getManager().vpMatch(vpName, expected, actual, REGEX.CONTAINS, req, "");

	}

	/**
	 * Executes a verification point asserting the actual String starts with the
	 * expected String
	 *
	 * @param vpName   name of the verification point
	 * @param expected The value you expect the Actual string to start with
	 * @param actual   The actual String that should start with the Expected String
	 */
	default void vpStartsWith(String vpName, String expected, String actual) {
		getManager().vpMatch(vpName, expected, actual, REGEX.STARTS_WITH, null, "");
	}

	/**
	 * Executes a verification point asserting the actual String starts with the
	 * expected String
	 *
	 * @param vpName   name of the verification point
	 * @param expected The value you expect the Actual string to start with
	 * @param actual   The actual String that should start with the Expected String
	 * @param req      Requirement to associate with this Verification Point
	 */
	default void vpStartsWith(String vpName, String expected, String actual, Requirement req) {
		getManager().vpMatch(vpName, expected, actual, REGEX.STARTS_WITH, req, "");
	}

	/**
	 * Executes a verification point asserting the two strings are equal
	 *
	 * @param vpName   name of the verification point
	 * @param expected The value you expect the Actual string to equal
	 * @param actual   The actual String that should match the Expected String
	 */
	default void vpEquals(String vpName, String expected, String actual) {
		getManager().vpEquals(vpName, expected, actual, null, "");
	}

	/**
	 * Executes a verification point asserting the two strings are equal
	 *
	 * @param vpName   name of the verification point
	 * @param expected The value you expect the Actual string to equal
	 * @param actual   The actual String that should match the Expected String
	 * @param req      Requirement to associate with this Verification Point
	 */
	default void vpEquals(String vpName, String expected, String actual, Requirement req) {
		getManager().vpEquals(vpName, expected, actual, req, "");
	}

	/**
	 * Will execute a verification point with the given name, comparing the actual
	 * and expected values
	 *
	 * @param vpName   name of the verification point
	 * @param expected boolean you expect
	 * @param actual   actual boolean value
	 */
	default void vpEquals(String vpName, boolean expected, boolean actual) {
		getManager().vpEquals(vpName, String.valueOf(expected), String.valueOf(actual), null, "");

	}

	/**
	 * Execute VP directly from the test script. Compares two boolean values. Will
	 * include the specified note in the Execution Note column of the results.
	 *
	 * @param vpName        name of the verification point
	 * @param expected      boolean you expect
	 * @param actual        actual boolean value
	 * @param executionNote note to add to the results log
	 *
	 */
	default void vpEquals(String vpName, boolean expected, boolean actual, String executionNote) {
		getManager().vpEquals(vpName, String.valueOf(expected), String.valueOf(actual), null, executionNote);
	}

	/**
	 * Execute VP directly from the test script. Compares two boolean values.
	 * Requirements must be accessed using {@code REQ.<your requirement id>}. As
	 * your type, you will see the auto-complete options available for Requirement
	 * that are in the Database. If you do not find the Requirement you are looking
	 * for, an upload may be necessary
	 *
	 * @param vpName      name of the verification point
	 * @param expected    boolean you expect
	 * @param actual      actual boolean value
	 * @param requirement Requirement to associate with this Verification Point
	 */
	default void vpEquals(String vpName, boolean expected, boolean actual, Requirement requirement) {
		vpEquals(vpName, String.valueOf(expected), String.valueOf(actual), requirement);
	}

	/**
	 * Execute VP directly from the test script. Compares two boolean values. Will
	 * include the specified note in the Execution Note column of the results.
	 * Requirements must be accessed using {@code REQ.<your requirement id>}. As
	 * your type, you will see the auto-complete options available for Requirement
	 * that are in the Database. If you do not find the Requirement you are looking
	 * for, an upload may be necessary
	 *
	 * @param vpName        name of the verification point
	 * @param expected      boolean you expect
	 * @param actual        actual boolean value
	 * @param executionNote note to add to the results log
	 * @param requirement   Requirement to associate with this Verification Point
	 */
	default void vpEquals(String vpName, boolean expected, boolean actual, String executionNote,
			Requirement requirement) {
		getManager().vpEquals(vpName, String.valueOf(expected), String.valueOf(actual), requirement, executionNote);
	}

	/**
	 * Will add a note to the results
	 *
	 * @param note to add to results
	 */
	default void addNote(String note) {
		getManager().addNote(new Note(note));
	}

	/**
	 * Add a Note event for the exception
	 *
	 * @param exception event
	 */
	default void addNote(Exception exception) {
		getManager().addNote(new Note(exception));
	}

	/**
	 * Add a screen shot event
	 *
	 * @param screenShotName file name
	 * @param screenShotNote note to go with the screen shot
	 */
	default void addScreenShot(String screenShotName, String screenShotNote) {
		// Check if ScreenShots were disabled for the project
		if (ConfigProperties.getValue(ConfigProperties.DISABLE_SCREEN_SHOTS, "FALSE").equalsIgnoreCase("TRUE")) {
			// If screen shots were disabled, still record this event as a Note
			addNote("Screen shot for '" + screenShotName + ", " + screenShotNote
					+ "' was suppressed because DISABLE_SCREEN_SHOTS is set to TRUE at the project level.");
		} else {
			// screen shots are enabled, go ahead and generate screen shot file
			String path = getResultsFolder();
			if (path == null || path.isEmpty()) {
				path = getManager().getResultsFolderPath();
			}

			String timeStamp = DateCalculator.getCurrentDate("mm-dd-yyyy-HH_mm_ss");
			// shorten the script name to only include file name, not the
			// entire folder path
			String scriptName = getManager().getScriptName();
			scriptName = scriptName.substring(scriptName.lastIndexOf(".") + 1);

			File screenShotFolder = new File(path + "\\" + "ScreenShots\\" + scriptName);

			screenShotFolder.mkdirs();
			String screenShotFilePath = screenShotFolder.getAbsolutePath() + "\\" + screenShotName + "_" + timeStamp
					+ ".png";

			Robot robot;
			// Create a File object for the Screen shot
			File screenShotFile = new File(screenShotFilePath);
			try {
				robot = new Robot();
				// Use Robot to capture the current screen for the Screen Shot image
				BufferedImage screenShot = robot
						.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
				// Write the screen shot image out to the file
				ImageIO.write(screenShot, "PNG", screenShotFile);
				// And the screen shot file as an event for the current script
				getManager().addScreenShot(new ScreenShot(screenShotFile, screenShotNote));
			} catch (AWTException e) {
				e.printStackTrace();
				getManager().addNote(new Note("Screen Shot for: " + screenShotNote + "(" + screenShotFilePath
						+ ") FAILED: " + e.getMessage()));
			} catch (IOException e) {
				e.printStackTrace();
				getManager().addNote(new Note("Screen Shot for: " + screenShotNote + "(" + screenShotFilePath
						+ ") FAILED: " + e.getMessage()));
			}
		}
	}

	/**
	 * Will add the specified error to the results
	 *
	 * @param error to add to results
	 */
	default void logError(String error) {
		Note note = new Note(error);
		note.setException(true);
		getManager().addNote(note);
	}

	/**
	 * Will add the specified warning to the results
	 *
	 * @param warning to add to results
	 */
	default void logWarning(String warning) {
		Note note = new Note(warning);
		note.setWarning(true);
		getManager().addNote(note);
	}

	/**
	 * Will execute a sleep for the specified time before moving on to the next
	 * command <br>
	 * <br>
	 * <b>NOTE: This should be used very sparingly. Try using waitForPageLoad and
	 * specifying wait time on searches before attempting to use hard coded sleeps.
	 * Other methods specify a maximum wait, so the script may move on sooner, once
	 * conditions are met. This will always wait the entire time</b>
	 *
	 * @param sleepTimeInSeconds time to sleep
	 */
	default void sleep(double sleepTimeInSeconds) {
		getManager().sleep(sleepTimeInSeconds);
	}

	/**
	 * Will run the script after calculating the current script class name
	 *
	 * @param args arguments to be passed in to the script
	 * @return ScriptResults return from running the script
	 */
	static ScriptResults runScript(Arguments args) {
		String scriptName = Thread.currentThread().getStackTrace()[2].getClassName();
		return runScript(scriptName, args);
	}

	/**
	 * Will run the specified script and pass in the provided arguments
	 *
	 * @param scriptName to run
	 * @param args       to pass into script
	 * @return ScriptResults returned after script completes
	 */
	static ScriptResults runScript(String scriptName, Arguments args) {
		ScriptResults results = new ScriptResults(EVENT_TYPE.CALLEDSCRIPT);
		// see if I can get scriptName without passing it in
		log.debug(Thread.currentThread().getStackTrace()[1].getClassName());
		log.debug(Thread.currentThread().getStackTrace()[1].getMethodName());
		log.debug(Thread.currentThread().getStackTrace()[1].getFileName());
		log.debug(Thread.currentThread().getStackTrace()[2].getClassName());
		log.debug(Thread.currentThread().getStackTrace()[2].getMethodName());
		log.debug(Thread.currentThread().getStackTrace()[2].getFileName());

		log.debug("PACKAGE=" + getCallerCallerClassName());

		log.debug("AutomatedTestScriptImplementation executed for " + scriptName);
		try {
			results = ToolManager.getAutomationTool().runScript(TestClassLoader.loadClass(scriptName).newInstance(),
					args);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return results;
	}

	/**
	 * Will run the specified script with the provided arguments
	 *
	 * @param currentScript to run
	 * @param args          to pass into script
	 */
	@Deprecated
	static void runScript(TestScriptInterface currentScript, Arguments args) {
		ToolManager.getAutomationTool().runScript(currentScript, args);
	}

	/**
	 * Runs the test script
	 *
	 * @param args script arguments
	 */
	@Deprecated
	default void runTestScript(Arguments args) {
		testScript(args);
	}

	/**
	 * Will run the specified modular script, will pass in empty Arguments
	 *
	 * @param scriptName of modular script to run
	 * @return Object returned from modular script
	 */
	default Object runModularScript(String scriptName) {
		Arguments args = new Arguments();
		// Set the results folder so any screen shots can be placed in it
		args.set(Arguments.RESULTS_FOLDER, getResultsFolder());
		return runModularScript(scriptName, args);
	}

	/**
	 * Will run the specified modular script, will pass in specified Arguments
	 *
	 * @param scriptName to run
	 * @param args       arguments to pass in to modular
	 * @return Object returned from modular script
	 */
	default Object runModularScript(String scriptName, Arguments args) {
		log.debug("MODULAR SCRIPT NAME=" + scriptName);
		ScriptResults results = new ScriptResults(EVENT_TYPE.CALLEDSCRIPT);
		args.set(Arguments.RESULTS_FOLDER, getResultsFolder());
		if (Deque508.isScanFor508Enabled()) {
			if (args.containsKey(ConfigProperties.SECTION_508_SHEET_NAME)) {
				args.set(ConfigProperties.SECTION_508_SHEET_NAME, args.get(ConfigProperties.SECTION_508_SHEET_NAME));
			} else {
				args.set(ConfigProperties.SECTION_508_SHEET_NAME, getCurrent508SheetName());
			}
		}
		results.setResultsFolder(getResultsFolder());
		log.debug("AutomatedTestScriptImplementation executed for " + scriptName);
		try {
			results = ToolManager.getAutomationTool()
					.runModularScript(TestClassLoader.loadClass(scriptName).newInstance(), args);
			results.setScriptName(scriptName);
			log.debug(" results SCRIPT NAME=" + results.getScriptName());
			addEvent(results);

		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			e.printStackTrace();
			// Throw the Exception. This means that a return value will not be passed back,
			// but since we encountered an exception, it may not be accurate anyway
			throw new WrappedException(e);
		}
		if (results.isFailedWithException()) {
			throw new RuntimeException("Modular Script [" + results.getScriptName() + "] failed with Exception");
		}
		return results.getReturnValue();
	}

	/**
	 * Will run the testscripts from the specified data file.
	 *
	 * @param testSuite data file
	 */
	default void runSuite(DataFile testSuite) {
		runSuite(testSuite, "");
	}

	/**
	 * Will run the test scripts from the specified data file.
	 *
	 * @param testSuite     data file
	 * @param xrayExecutionId XRAY Test Execution id for reporting
	 */
	default void runSuite(DataFile testSuite, String xrayExecutionId) {
		// Only run when the status is RUN (ignore Failure statuses)
		runSuite(testSuite, xrayExecutionId, true, false, false);
	}

	/**
	 * Will run the testscripts from the specified data file that are currently
	 * marked as FAILED. There are two possible types of failures (FAILED_VP,
	 * FAILED_EXCEPTION) and you can specify which statuses should be re-run. This
	 * method will not run scripts marked RUN, only FAILED_VP or FAILED_EXCEPTION
	 *
	 * @param testSuite               data file
	 * @param isExecuteFailedVps        TRUE if FAILED_VP status should be rerun,
	 *                                  FALSE if not
	 * @param isExecuteFailedExceptions TRUE if FAILED_EXCEPTION status should be
	 *                                  rerun, FALSE if not
	 */
	default void runFailuresInSuite(DataFile testSuite, boolean isExecuteFailedVps,
			boolean isExecuteFailedExceptions) {
		// Do not run RUN status, and pass in provided values for VP and Exception
		// Failures
		runSuite(testSuite, "", false, isExecuteFailedVps, isExecuteFailedExceptions);
	}

	/**
	 * Will run the test scripts from the specified data file. If Xray Reporting is
	 * turned on, and a value for xrayTestExecutionId is empty, this method will
	 * reference the XrayReferenceFile for the suite to report results back to the
	 * same Test Execution as the previous run of the same suite if
	 * isExecuteFailedVps or isExecuteFailedExceptions is set to true.
	 *
	 * @param testSuite                 data file
	 * @param xrayExecutionId           XRAY Test Execution id for reporting
	 * @param isExecuteRun              TRUE if RUN status should be run, FALSE if
	 *                                  not
	 * @param isExecuteFailedVps        TRUE if FAILED_VP status should be rerun,
	 *                                  FALSE if not
	 * @param isExecuteFailedExceptions TRUE if FAILED_EXCEPTION status should be
	 *                                  rerun, FALSE if not
	 */
	default void runSuite(DataFile testSuite, String xrayExecutionId, boolean isExecuteRun, boolean isExecuteFailedVps,
			boolean isExecuteFailedExceptions) {

		ArrayList<String> runStatuses = new ArrayList<String>();
		if (isExecuteRun) {
			runStatuses.add(DataFile.RUN_VALUE);
		}
		if (isExecuteFailedVps) {
			runStatuses.add(ScriptResults.FAILED_VP);
		}
		if (isExecuteFailedExceptions) {
			runStatuses.add(ScriptResults.FAILED_EXCEPTION);
		}

		String suiteFolderPath = getManager().getResultsLogBaseName() + "_Logs";
		// Since this is a suite, generate the folder to contain results in
		File suiteFolder = new File(suiteFolderPath);
		suiteFolder.mkdirs();
		setResultsFolder(suiteFolderPath);

		// Allow users to override the test execution column to support legacy data
		// sheets
		String executionColumn = ConfigProperties.getValue(ConfigProperties.TEST_EXECUTION_COLUMN,
				DataFile.TEST_EXECUTION_COLUMN);

		ScriptResults results = getResults();
		results.setMainSuiteTest(true);
		// Set the data file
		results.addData(ScriptResults.TEST_SCRIPTS_FILE, testSuite.getFile().getName());
		results.addData(ScriptResults.TEST_SCRIPTS_TO_RUN, runStatuses.toString());
		List<String> ids = testSuite.getDataIds();
		// remove the header row from the set of ids to execute
		ids.remove(DataFile.DATA_ID_COLUMN);

		// Determine if the suite is using old legacy rftShared framework script
		// execution file by looking for the new script driver file column
		boolean isLegacySuite = !testSuite.isColumnHeaderPresent(DataFile.SCRIPT_DRIVER_FILE_COLUMN);

		// If we are re-running failures, and an xrayExecutionId was not passed into
		// runSuite look for the xray reference file to set the
		// Xray Test Execution Id and the Xray Test Plan Id
		boolean isXrayOn = ConfigProperties.getValue(ConfigProperties.XRAY_REPORTING, "FALSE").equalsIgnoreCase("TRUE");

		if (isXrayOn) {
			List<String> xrayTestIds = new ArrayList<String>();
			for (int i = 0; i < ids.size(); i++) {
				String id = ids.get(i);
				String status = testSuite.getData(id, executionColumn).trim();
				if (runStatuses.contains(status.toUpperCase())) {
					if (isLegacySuite) {
						xrayTestIds.add(testSuite.getData(id, ScriptResults.XRAY_TEST_ID));
					} else {
						CsvDataFile driver = getDataFile(testSuite.getData(id, DataFile.SCRIPT_DRIVER_FILE_COLUMN));
						xrayTestIds.add(driver.getData(id, ScriptResults.XRAY_TEST_ID));
					}
				}
			}
			// Validate XRAY_TEST_IDs are valid tests before running any
			// scripts
			XrayServerReporting xrayReport = new XrayServerReporting();
			xrayReport.validateTests(xrayTestIds.toArray(new String[0]));

			if (xrayExecutionId == null | xrayExecutionId.isEmpty() && isExecuteFailedExceptions | isExecuteFailedVps) {
				CsvDataFile xrayRef = xrayReport.getXrayReferenceFile(results);
				if (xrayRef != null) {

					String dataId = results.getData(ScriptResults.TEST_SCRIPTS_FILE);
					xrayExecutionId = xrayRef.getData(dataId, xrayReport.XRAY_REF_EXECUTION_ID_COLUMN);
					// get the test plan number and set it in the config file
					String xrayTestPlanId = xrayRef.getData(dataId, xrayReport.XRAY_REF_PLAN_ID_COLUMN);
					ConfigProperties.setValue(ConfigProperties.XRAY_PLAN_ID, xrayTestPlanId);
					String xrayFixVersion = xrayRef.getData(dataId, xrayReport.XRAY_REF_FIX_VERSION_COLUMN);
					ConfigProperties.setValue(ConfigProperties.XRAY_FIX_VERSION, xrayFixVersion);
				}
			}

		}

		for (String testDataID : ids) {

			String complete = testSuite.getData(testDataID, executionColumn).trim();

			// continue if the current test script has not been completed
			if (runStatuses.contains(complete.toUpperCase())) {
//				String testDataID = testScripts.getData(id, DataFile.DATA_ID_COLUMN);
				DataFile scriptDriverFile;
				if (!isLegacySuite) {
					String scriptDriverFileName = testSuite.getData(testDataID, DataFile.SCRIPT_DRIVER_FILE_COLUMN);
					scriptDriverFile = getDataFile(scriptDriverFileName);
					// We also have the testScripts file which is the 'test suite' in this case
				} else {
					// We only have one file, the script execution file - from the legacy RFT
					// framework, it combines the functionality of the testSuite and
					// scriptDriverFile
					scriptDriverFile = testSuite;
				}

				String script = scriptDriverFile.getData(testDataID, DataFile.SCRIPT_COLUMN);
				log.debug(">>> " + script + ": " + complete);

				getManager().setCurrentTestDataId(testDataID);

				// Check to see if the dependencies test execution passed.
				// If there is not a DEPENDENCIES column, assume there are no dependencies
				if (testSuite.isColumnHeaderPresent(DataFile.DEPENDENCIES_COLUMN)) {
					String dependency = testSuite.getData(testDataID, DataFile.DEPENDENCIES_COLUMN);
					if (!dependency.replace("`", "").isEmpty()) {
						String[] dependencies = dependency.split(";");
						boolean dependenciesRan = true;
						for (int j = 0; j < dependencies.length; j++) {
							if (!testSuite.getData(dependencies[j].trim(), executionColumn)
									.equalsIgnoreCase(ScriptResults.PASSED)) {
								dependenciesRan = false;
								break;
							}
						}
						if (!dependenciesRan) {
							// if a dependency did not PASS, go to the
							// next test script
							System.out.println("!!!!!!!!! NOTE: Script with DATA_ID " + testDataID
									+ " did not run due to a failure in one of its dependencies !!!!!!!");
							continue;
						}
					}
				}

				// Build array of test data to pass into the execution script
				Arguments data = ((CsvDataFile) scriptDriverFile).getArguments(testDataID);
				// Since this is a suite, pass the folder location for results

				data.set(Arguments.RESULTS_FOLDER, suiteFolderPath);
				// TODO - add support to run from another project (not immediate need)
				// if (projectNameForExecutionScript.isEmpty()) {
				// projectNameForExecutionScript = getCurrentProject().getName();
				// }

				String runXrayTestId = "";
				if (scriptDriverFile.isColumnHeaderPresent(ScriptResults.XRAY_TEST_ID)) {
					runXrayTestId = scriptDriverFile.getData(testDataID, ScriptResults.XRAY_TEST_ID);
				}

				String dataId = testSuite.getFile().getName() + " / " + testDataID;
				data.set(ScriptResults.DATA_ID, dataId);
				data.set(ScriptResults.XRAY_TEST_ID, runXrayTestId);
				data.set(ScriptResults.XRAY_EXECUTION_ID, xrayExecutionId);

				if (Deque508.isScanFor508Enabled()) {
					data.set(ConfigProperties.SECTION_508_SHEET_NAME, getCurrent508SheetName());
				}

				ScriptResults currResults = TestClassLoader.runExecutionScript(script, data);
				currResults.addData(ScriptResults.XRAY_TEST_ID, runXrayTestId);
				currResults.addData(ScriptResults.DATA_ID, testSuite.getFile().getName() + " / " + testDataID);

				testSuite.writeToDataSheet(testDataID, DataFile.EXECUTION_TIMESTAMP_COLUMN, currResults.getTimestamp());
				// write the status of the script out to the data file (PASSED, FAILED_VP,
				// FAILED_EXECUTION)
				testSuite.writeToDataSheet(testDataID, executionColumn, currResults.getStatus());
				results.addEvent(currResults);
			}
		}
		if (xrayExecutionId != null && !xrayExecutionId.isEmpty()) {
			results.addData(ScriptResults.XRAY_EXECUTION_ID, xrayExecutionId);
		}
		setResults(results);

	}

	/**
	 * Will set the results object for this test script
	 *
	 * @param results to assign this test script
	 */
	void setResults(ScriptResults results);

	/**
	 *
	 * //TODO This can be removed, I assume? TODO - This was an experiment to see if
	 * we could remove the requirement from concrete classes to implement a main if
	 * they want to be runnable. I dont think it worked, but need to verify before
	 * removing
	 *
	 * @param args to pass in to the test script
	 * @return ScriptResults return from running the script
	 */
	static ScriptResults main(Arguments args) {
		log.debug("BASE AutomatedTestScriptImplementation executed");
		return runScript(args);
	}

	/**
	 * Returns the current class name
	 *
	 * @return String class name
	 */
	static String getClassName() {
		return Thread.currentThread().getStackTrace()[2].getClassName();
	}

	/**
	 * TODO = Might rename to getPackageName Need to verify if this is correct
	 *
	 * Will return null if it can not locate the name
	 *
	 * @return String CallerCallerClasName (aka Package name)
	 */
	static String getCallerCallerClassName() {
		StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
		String callerClassName = null;
		for (int i = 1; i < stElements.length; i++) {
			StackTraceElement ste = stElements[i];
			if (!ste.getClassName().equals(TestScript.class.getName())
					&& ste.getClassName().indexOf("java.lang.Thread") != 0) {
				if (callerClassName == null) {
					callerClassName = ste.getClassName();
				} else if (!callerClassName.equals(ste.getClassName())) {
					return ste.getClassName();
				}
			}
		}
		return null;
	}

	/**
	 * Returns the name of this script instance
	 *
	 * @return String
	 */
	default String getScriptName() {
		return getManager().getScriptName();
	}

	/**
	 * Extending classes must implement the testScript method. This will contain the
	 * logic for the test script as written by the tester
	 *
	 * @param args arguments to pass in to test script
	 */
	void testScript(Arguments args);

	/**
	 * Will get the specified data file from the datasets folder specified in config
	 * properties. If there is not a specified location, will default to
	 * localDataSets
	 *
	 * @param fileName  locates data file
	 * @param sheetName specifies sheet within the Excel file
	 * @return ExcelDataFile instance for the specified file and sheet
	 */
	default ExcelDataFile getDataFile(String fileName, String sheetName) {
		File path = new File("");
		String dataFolder = ConfigProperties.getValue("DATASETS_FOLDER", "/dataSets/localDataSets");
		String filePath = path.getAbsolutePath() + dataFolder + "/" + fileName;
		log.debug(filePath);
		return new ExcelDataFile(filePath, sheetName);
	}

	/**
	 * Will get the specified data file from the datasets folder specified in config
	 * properties. If there is not a specified location, will default to
	 * localDataSets
	 *
	 * @param fileName of the csv data file
	 * @return CsvDataFile instance for the specified file name
	 */
	default CsvDataFile getDataFile(String fileName) {
		return DataFileHelper.getDataFile(fileName);
	}

	/**
	 * Will get the specified data file from the datasets folder specified in config
	 * properties. If there is not a specified location, will default to
	 * localDataSets
	 *
	 * @param fileName of the csv data file
	 * @return CsvFile instance for the specified file name
	 */
	default CsvFile getUnformattedDataFile(String fileName) {
		String filePath = getDataSetsPath() + "/" + fileName;

		return new CsvFile(filePath);
	}

	/**
	 * Returns the file path to where the data sets are stored
	 *
	 * @return String file path
	 */
	default String getDataSetsPath() {
		return DataFileHelper.getDataSetsPath();
	}

	/**
	 * Will set the script manager for this instance
	 *
	 * @param script manager
	 */
	void setScript(TestScriptManager script);

	/**
	 * Will set the boolean specifying if this is a modular script or not
	 *
	 * @param isModularScript TRUE it is a modular, FALSE it is not
	 */
	void setModularScript(boolean isModularScript);

	/**
	 * Will add a ScreenShot even to the results for this test script instance
	 *
	 * @param screenShot to add to the results
	 */
	default void addScreenShot(ScreenShot screenShot) {
		getResults().addEvent(screenShot);
	}

	/**
	 * Will set the folder for results
	 *
	 * @param resultsFolder to store results in
	 */
	default void setResultsFolder(String resultsFolder) {
		getResults().setResultsFolder(resultsFolder);
	}

	/**
	 * Will return the folder for results
	 *
	 * @return String results folder
	 */
	default String getResultsFolder() {
		String resultsFolder = getResults().getResultsFolder();
		if (resultsFolder.isEmpty()) {
			resultsFolder = getManager().getScriptResults().getResultsFolder();
		}
		return resultsFolder;
	}

	/**
	 * Will set the Sheet Name for 508 Reporting in the current script
	 *
	 * @param current508SheetName Name of the Sheet in the 508 Excel document
	 */
	void setCurrent508SheetName(String current508SheetName);

	/**
	 * Will return the current Sheet Name for 508 Reporting in the current script
	 *
	 * @return String sheet name
	 */
	String getCurrent508SheetName();

}