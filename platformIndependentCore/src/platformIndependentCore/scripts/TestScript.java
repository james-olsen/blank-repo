package platformIndependentCore.scripts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import platformIndependentCore.api.APIClientTester;
import platformIndependentCore.core.AutomatedPage;
import platformIndependentCore.core.AutomationHelper;
import platformIndependentCore.core.ToolManager;
import platformIndependentCore.events.Event.EVENT_TYPE;
import platformIndependentCore.exceptions.InvalidDataException;
import platformIndependentCore.exceptions.InvalidParameterException;
import platformIndependentCore.utilities.ConfigProperties;
import platformIndependentCore.vista.VistaManager;
import platformIndependentCore.windows.AutomatedWindowsApp;
import utilities.Deque508;

/**
 * This is the class that a users Test Scripts will extend. It provides access
 * to methods from TestScriptInterface as well as the ability to run scripts,
 * set and get results.
 *
 * @author VBAAUSTAYLOL
 *
 */
public abstract class TestScript implements TestScriptInterface {
	/** logger for this class */
	static Logger log = LogManager.getLogger(TestScript.class.getName());
	/** Test Script Manager associated with this TestScript */
	TestScriptManager scriptManager;
	/** Specifies if this is a Modular script */
	boolean isModularScript = true;
	/** project name */
	String projectName = "";
	/** suite name */
	String suiteName = "";
	/** Data id associated with the test script */
	String testDataID = "";
	/** Array of arguments for the Test Script */
	String[] arguments;
	/** The ScriptResults for this Test Script */
	ScriptResults results = new ScriptResults(EVENT_TYPE.EXECUTIONSCRIPT);
	/** List of ScriptResults for all scripts called during this script execution */
	List<ScriptResults> calledScriptResults = new ArrayList<ScriptResults>();
	/** The current 508 sheet name */
	String current508SheetName = "";
	/** a Vista for running vista scripts */
	protected VistaManager vista = new VistaManager();
	/** */
	protected APIClientTester client = new APIClientTester();

	/** Windows for running Windows scripts */
	protected static AutomatedWindowsApp windowsApp = new AutomatedWindowsApp() {
	};

	/**
	 * Will set the return value for the script
	 *
	 * @param returnValue value to return to calling script
	 */
	protected void setReturn(Object returnValue) {
		results.setReturnValue(returnValue);
	}

	/**
	 * Returns the Return value for the script
	 *
	 * @return String value returned from script execution
	 */
	protected String getReturn() {
		return (String) results.getReturnValue();
	}

	@Override
	public void setResults(ScriptResults results2) {
		this.results = results2;
	}

	@Override
	public ScriptResults getResults() {
		return results;
	}

	/**
	 * Will run the script with the specified arguments This method will convert the
	 * String[] args to an Arguments object
	 * <p>
	 * <b>This method will require all the arguments to be in label value pairs.
	 * Will assume args is set to {key1, value1, key2, value2, key3, value3} which
	 * will result in 3 entries into Arguments </b>
	 *
	 * @param args script arguments to be in label value pairs.
	 * @return ScriptResults returned from script execution
	 */
	public static ScriptResults runScript(String[] args) {
		return runScript(convertArgs(args));
		// TODO try this instead of having to add the assert to the main of each script,
		// consider times when we don't want a result sent back to jenkins (while full
		// regression is running we don't care if scripts fail)
		// where we need a result passed back to Jenkins
//				ScriptResults results = runScript(convertArgs(args));
//				Assert.assertEquals("PASSED", results.getStatus());
//				return results;
	}

	/**
	 * Will run the script with the specified arguments This method will convert the
	 * String[] args to an Arguments object
	 * <p>
	 * <b>This method will require all the arguments to be in label value pairs.
	 * Will assume args is set to {key1, value1, key2, value2, key3, value3} which
	 * will result in 3 entries into Arguments </b>
	 *
	 * @param args       script arguments to be in label value pairs.
	 * @param xrayTestId The XRAY ID of the Test Script to associate this run with.
	 *                   This is not needed for if running with runSuite
	 * @return ScriptResults returned from script execution
	 */
	public static ScriptResults runScript(String[] args, String xrayTestId) {
		return runScript(args, xrayTestId, "");
	}

	/**
	 * Will run the script with the specified arguments This method will convert the
	 * String[] args to an Arguments object
	 * <p>
	 * <b>This method will require all the arguments to be in label value pairs.
	 * Will assume args is set to {key1, value1, key2, value2, key3, value3} which
	 * will result in 3 entries into Arguments </b>
	 *
	 * @param args            script arguments to be in label value pairs.
	 * @param xrayTestId      The XRAY ID of the Test Script to associate this run
	 *                        with. This is not needed for if running with runSuite
	 * @param xrayExecutionId The XRAY Test Execution id to associate this test run
	 *                        with
	 * @return ScriptResults returned from script execution
	 */
	public static ScriptResults runScript(String[] args, String xrayTestId, String xrayExecutionId) {
		Arguments arguments = convertArgs(args);
		arguments.set(ScriptResults.XRAY_TEST_ID, xrayTestId);
		arguments.set(ScriptResults.XRAY_EXECUTION_ID, xrayExecutionId);
		return runScript(arguments);
	}

	/**
	 * Will run the script with the specified Arguments
	 *
	 * @param args script arguments
	 * @return ScriptResults returned from script execution
	 */
	public static ScriptResults runScript(Arguments args) {
		long startTime = System.nanoTime();

		// String scriptName = Thread.currentThread().getStackTrace()[2].getClassName();
		String scriptName = TestScriptInterface.getCallerCallerClassName();
		ScriptResults results = runScript(scriptName, args);
		// ... the code being measured ...
		long estimatedTime = System.nanoTime() - startTime;
		log.info("ELAPSED TIME = " + estimatedTime);
		// convert to seconds
		log.info("which is " + TimeUnit.NANOSECONDS.toSeconds(estimatedTime) + " seconds");
		return results;

	}

	/**
	 * Will run the script with the specified arguments This method will convert the
	 * String[] args to an Arguments object
	 * <p>
	 * <b>This method will require all the arguments to be in label value pairs.
	 * Will assume args is set to {key1, value1, key2, value2, key3, value3} which
	 * will result in 3 entries into Arguments </b>
	 *
	 * @param scriptName name of script to run
	 * @param args       script arguments to be in label value pairs.
	 * @return ScriptResults returned from script execution
	 */
	@Deprecated
	public static ScriptResults runScript(String scriptName, String[] args) {
		return runScript(scriptName, convertArgs(args));
	}

	/**
	 * Will run the script with the specified Arguments
	 *
	 * @param scriptName name of script to run
	 * @param args       script arguments
	 * @return ScriptResults returned from script execution
	 */
	public static ScriptResults runScript(String scriptName, Arguments args) {
		// see if I can get scriptName without passing it in
		log.debug(Thread.currentThread().getStackTrace()[1].getClassName());
		log.debug(Thread.currentThread().getStackTrace()[1].getMethodName());
		log.debug(Thread.currentThread().getStackTrace()[1].getFileName());
		log.debug(Thread.currentThread().getStackTrace()[2].getClassName());
		log.debug(Thread.currentThread().getStackTrace()[2].getMethodName());
		log.debug(Thread.currentThread().getStackTrace()[2].getFileName());

		log.debug("PACKAGE=" + TestScriptInterface.getCallerCallerClassName());

		log.debug("AutomatedTestScriptImplementation executed for " + scriptName);
		ScriptResults results = null;
		try {
			results = ToolManager.getAutomationTool().runScript(TestClassLoader.loadClass(scriptName).newInstance(),
					args);
		} catch (InstantiationException e) {
			log.catching(e);
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			log.catching(e);
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			log.catching(e);
			e.printStackTrace();
		}
		return results;
	}

	/**
	 * Will run the windows script with the specified arguments This method will
	 * convert the String[] args to an Arguments object for Windows applications
	 * <p>
	 * <b>This method will require all the arguments to be in label value pairs.
	 * Will assume args is set to {key1, value1, key2, value2, key3, value3} which
	 * will result in 3 entries into Arguments </b>
	 *
	 * @param args windows script arguments to be in label value pairs.
	 * @return ScriptResults returned from Windows script execution
	 */
	public static ScriptResults runWindowsScript(String[] args) {
		return runWindowsScript(convertArgs(args));

	}

	/**
	 * Will run the windows script with the specified Arguments
	 *
	 * @param scriptName name of script to run windows application
	 * @param args       windows script arguments
	 * @return ScriptResults returned from Windows script execution
	 */
	public static ScriptResults runWindowsScript(String scriptName, Arguments args) {
		// see if I can get scriptName without passing it in
		log.debug(Thread.currentThread().getStackTrace()[1].getClassName());
		log.debug(Thread.currentThread().getStackTrace()[1].getMethodName());
		log.debug(Thread.currentThread().getStackTrace()[1].getFileName());
		log.debug(Thread.currentThread().getStackTrace()[2].getClassName());
		log.debug(Thread.currentThread().getStackTrace()[2].getMethodName());
		log.debug(Thread.currentThread().getStackTrace()[2].getFileName());

		log.debug("PACKAGE=" + TestScriptInterface.getCallerCallerClassName());

		log.debug("AutomatedTestScriptImplementation executed for " + scriptName);
		ScriptResults results = null;
		try {
			results = ToolManager.getAutomationTool().runScript(TestClassLoader.loadClass(scriptName).newInstance(),
					args);
		} catch (InstantiationException e) {
			log.catching(e);
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			log.catching(e);
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			log.catching(e);
			e.printStackTrace();
		}
		windowsApp.closeWindowsApplication();
		return results;
	}

	/**
	 * Will run the Windows script with the specified Arguments
	 *
	 * @param args Windows script arguments
	 * @return ScriptResults returned from Windows script execution
	 */
	public static ScriptResults runWindowsScript(Arguments args) {
		long startTime = System.nanoTime();

		// String scriptName = Thread.currentThread().getStackTrace()[2].getClassName();
		String scriptName = TestScriptInterface.getCallerCallerClassName();
		ScriptResults results = runWindowsScript(scriptName, args);
		// ... the code being measured ...
		long estimatedTime = System.nanoTime() - startTime;
		log.info("ELAPSED TIME = " + estimatedTime);
		// convert to seconds
		log.info("which is " + TimeUnit.NANOSECONDS.toSeconds(estimatedTime) + " seconds");
		return results;

	}

	/**
	 * Runs Section 508 Scan on the specified page, but within the specified section
	 * scanWithIn
	 *
	 * @param page       Automated Page instance, class name must line up with page
	 *                   listed in ScannedPages508 excel file, will also be the name
	 *                   of the log file
	 * @param scanWithIn HTML Page section to scan within. Scans within specific
	 *                   section, not entire page.
	 * @param sheetName  name of sheet in the current data file to use
	 */
	protected void scanFor508Compliance(AutomatedPage page, String scanWithIn, String sheetName) {
		if (Deque508.isScanFor508Enabled()) {
			AutomationHelper.getAutomationTool().waitForPageLoad();
			String scanMessage = Deque508.getInstance(sheetName).scanFor508Compliance(this, page, scanWithIn);
			addNote(scanMessage);
		}
	}

	/**
	 * Will scan for 508 Compliance
	 *
	 * @param page Automated Page instance, class name must line up with page listed
	 *             in ScannedPages508 Excel file, will also be the name of the log
	 *             file
	 */
	protected void scanFor508Compliance(AutomatedPage page) {
		// Make sure to set the sheet name. This will initiate setup for Section 508
		// scans.
		set508DataFile(getCurrent508SheetName());
		scanFor508Compliance(page, "", getCurrent508SheetName());
	}

	/**
	 * If the config file indicates 508 scans are to be run, sets the data file used
	 * to track scanned pages. The data set name is pulled from the
	 * SECTION_508_DATASET_NAME in the config file, and the sheet name is passed in
	 * to this method.
	 *
	 * NOTE: If you set the SECTION_508_SHEET_NAME setting in your config file, you
	 * do not need call this
	 *
	 * @param sheetName508 the name of the sheet in the Excel file to use
	 */
	protected void set508DataFile(String sheetName508) {
		setCurrent508SheetName(sheetName508);
		if (Deque508.isScanFor508Enabled()) {

			String dataSetName = ConfigProperties.getValue(ConfigProperties.SECTION_508_DATASET_NAME);
			if (dataSetName.isEmpty()) {
				throw new InvalidParameterException(
						"You have not specified a value for SECTION_508_DATASET_NAME in the config file.");
			}
			if (sheetName508.isEmpty()) {
				sheetName508 = ConfigProperties.getValue(ConfigProperties.SECTION_508_SHEET_NAME);
			}
			if (sheetName508.isEmpty()) {
				throw new RuntimeException("The specified sheet name for the set508DataFile is blank."
						+ " You must specify a valid sheet name.");
			}

			Deque508.getInstance(getCurrent508SheetName()).setDataFile(getDataFile(dataSetName, sheetName508));
			addNote("Section 508 Data File set to: " + dataSetName + ", SHEET=" + sheetName508);
		}
	}

	@Override
	public void setCurrent508SheetName(String current508SheetName) {
		this.current508SheetName = current508SheetName;
	}

	/**
	 * Public method to return the name of the current 508 sheet name that
	 * corresponds to the excel file specified for SECTION_508_DATASET_NAME in the
	 * config file
	 *
	 * @return String
	 */
	@Override
	public String getCurrent508SheetName() {
		if (current508SheetName == null || current508SheetName.isEmpty()) {
			current508SheetName = ConfigProperties.getValue(ConfigProperties.SECTION_508_SHEET_NAME);
		}
		if (current508SheetName.isEmpty()) {
			throw new RuntimeException(
					"You are missing the set508DataFile call at the beginning of your execution script."
							+ " You must call this to specify the sheet name of the dataset that tracks your"
							+ " scanned pages.");
		}
		return current508SheetName;
	}

	@Override
	public void setScript(TestScriptManager script) {
		this.scriptManager = script;
	}

	@Override
	public void setModularScript(boolean isModularScript) {
		this.isModularScript = isModularScript;
	}

	@Override
	public TestScriptManager getManager() {
		return scriptManager;
	}

	@Override
	public boolean isModularScript() {
		return isModularScript;
	}

	/**
	 * Will execute a sleep only if the properties file has DEMO_MODE set to TRUE
	 *
	 * @param seconds number of seconds to sleep when in DEMO MODE
	 */
	public void sleepForDemo(long seconds) {
		if (Boolean.valueOf(ConfigProperties.getValue("DEMO_MODE", "FALSE"))) {
			sleep(seconds);
		}
	}

	/**
	 * Will convert the String[] of args to Arguments.
	 *
	 * This method will require all the arguments to be in label value pairs. Will
	 * assume args is set to {key1, value1, key2, value2, key3, value3}
	 *
	 * which will result in 3 entries into Arguments
	 *
	 * @param args to convert
	 * @return Arguments object containing the String[] args
	 */
	protected static Arguments convertArgs(String[] args) {
		Arguments map = new Arguments();

		if (args != null) {

			if (args.length % 2 != 0) {
				throw new InvalidDataException("Odd number of arguments specified. "
						+ "They must be in KEY, VALUE pairs. Provided arguments=" + Arrays.deepToString(args));
			}
			// loop through the pairs, adding as key, value
			for (int i = 0; i < args.length; i = i + 2) {
				map.set(args[i], args[i + 1]);
			}
		}

		return map;
	}

	/**
	 * Will convert the String[] of args to Arguments.
	 *
	 * This method will generate very generic keys for the Arguments object, and is
	 * therefore discouraged
	 *
	 * @param args to convert
	 * @return Arguments object containing the String[] args
	 */
	@Deprecated
	protected static Arguments convertArgsWithGenericKeys(String[] args) {
		Arguments map = new Arguments();
		int i = 1;
		for (String arg : args) {
			map.set("arg" + String.valueOf(i), arg);
		}
		return map;
	}

	// void setStartTime(Date startDate) {
	// getResults().setStartTime(startDate);
	// }
	//
	// void setStopTime(Date stopDate) {
	// getResults().setStopTime(stopDate);
	// }
	//
	// void setDuration(Date startDate, Date stopDate) {
	// getResults().setDuration(startDate, stopDate);
	// }
	//
	// String getStartTime() {
	// return getResults().getData(ScriptResults.START_TIME);
	// }
	//
	// String getStopTime() {
	// return getResults().getData(ScriptResults.STOP_TIME);
	// }
	//
	// String getDuration() {
	// return getResults().getData(ScriptResults.DURATION);
	// }

	//
	// /**
	// * @return the script
	// */
	// TestScriptManager getManager() {
	// if (script == null) {
	// throw new RuntimeException(
	// "Invalid state for Automated Test Script. Script instance has not been
	// initialized");
	// }
	// return script;
	// }
	//
	// /**
	// * @param script the script to set
	// */
	// void setScript(TestScriptManager script) {
	// this.script = script;
	// }
	//
	//
	// /**
	// * @return the isModularScript
	// */
	// boolean isModularScript() {
	// return isModularScript;
	// }
	//
	// /**
	// * @param isModularScript the isModularScript to set
	// */
	// void setModularScript(boolean isModularScript) {
	// this.isModularScript = isModularScript;
	// results.setEventType(EVENT_TYPE.CALLEDSCRIPT);
	// }
	//
	// /**
	// * @return the results
	// */
	// ScriptResults getResults() {
	// return results;
	// }
	//
	// /**
	// * @param results the results to set
	// */
	// void setResults(ScriptResults results) {
	// this.results = results;
	// }
	//
	//// /**
	//// * @return the calledScriptResults
	//// */
	//// List<ScriptResults> getCalledScriptResults() {
	//// return calledScriptResults;
	//// }
	//
	// /**
	// * @return the calledScriptResults
	// */
	// int getNumberOfCalledScripts() {
	// int cnt = 0;
	// if (calledScriptResults != null && !calledScriptResults.isEmpty()) {
	// cnt = calledScriptResults.size();
	// }
	// return cnt;
	// }
	//
	//// /**
	//// * @return the calledScriptResults
	//// */
	//// void addScriptResults(ScriptResults newResults) {
	//// getCalledScriptResults().add(newResults);
	////// this.getCalledScriptResults().
	////// events.add(newResults);
	//// }
	//
	// void addEvent(Event event) {
	// //events.add(event);
	// // getCalledScriptResults().add(event);
	// getResults().addEvent(event);
	// }
	//
	//// /**
	//// * @param calledScriptResults the calledScriptResults to set
	//// */
	//// void setCalledScriptResults(List<ScriptResults> calledScriptResults) {
	//// this.calledScriptResults = calledScriptResults;
	//// }
	//
	//
	// /**
	// * Executes a verification point asserting the two strings are equal
	// *
	// * @param vpName name of the verification point
	// * @param expected value
	// * @param actual value
	// */
	// protected void vpEquals(String vpName, String expected, String actual) {
	// getManager().vpEquals(vpName, expected, actual);
	// }
	//
	// /**
	// *
	// *
	// * @param
	// */
	// protected void addNote(String note) {
	// getManager().addNote(new Note(note));
	// }
	// /**
	// *
	// *
	// * @param
	// */
	// protected void addNote(Exception exception) {
	// getManager().addNote(new Note(exception));
	// }
	//
	// public static void runScript(String[] args) {
	// String scriptName = Thread.currentThread().getStackTrace()[2].getClassName();
	// runScript(scriptName, args);
	// }
	//
	// public static void runScript(String scriptName, String[] args) {
	// // see if I can get scriptName without passing it in
	// log.debug(Thread.currentThread().getStackTrace()[1].getClassName());
	// log.debug(Thread.currentThread().getStackTrace()[1].getMethodName());
	// log.debug(Thread.currentThread().getStackTrace()[1].getFileName());
	// log.debug(Thread.currentThread().getStackTrace()[2].getClassName());
	// log.debug(Thread.currentThread().getStackTrace()[2].getMethodName());
	// log.debug(Thread.currentThread().getStackTrace()[2].getFileName());
	//
	//
	// log.debug("PACKAGE=" + getCallerCallerClassName());;
	//
	// log.debug("AutomatedTestScriptImplementation executed for " +
	// scriptName);
	// try {
	// PlatformImplementations.getAutomationTool()
	// .runScript(TestClassLoader.loadClass(scriptName).newInstance(), args);
	// } catch (InstantiationException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// } catch (IllegalAccessException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// } catch (ClassNotFoundException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }
	//
	// public static void runScript(TestScript currentScript, String[] args) {
	// // see if I can get scriptName without passing it in
	//// log.debug(Thread.currentThread().getStackTrace()[1].getClassName());
	//// log.debug(Thread.currentThread().getStackTrace()[1].getMethodName());
	//// log.debug(Thread.currentThread().getStackTrace()[1].getFileName());
	//// log.debug(Thread.currentThread().getStackTrace()[2].getClassName());
	//// log.debug(Thread.currentThread().getStackTrace()[2].getMethodName());
	//// log.debug(Thread.currentThread().getStackTrace()[2].getFileName());
	////
	////
	//// log.debug("PACKAGE=" + getCallerCallerClassName());;
	////
	// // log.debug("AutomatedTestScriptImplementation executed for " +
	// scriptName);
	// // try {
	// PlatformImplementations.getAutomationTool()
	// .runScript(currentScript, args);
	//// } catch (InstantiationException e) {
	//// // TODO Auto-generated catch block
	//// e.printStackTrace();
	//// } catch (IllegalAccessException e) {
	//// // TODO Auto-generated catch block
	//// e.printStackTrace();
	//// } catch (ClassNotFoundException e) {
	//// // TODO Auto-generated catch block
	//// e.printStackTrace();
	//// }
	// }
	//
	//
	// protected void runTestScript(String[] args) {
	// testScript(args);
	// }
	//
	// public void runModularScript(String scriptName, String... args) {
	// log.debug("AutomatedTestScriptImplementation executed for " +
	// scriptName);
	// try {
	// ScriptResults results = PlatformImplementations.getAutomationTool()
	// .runModularScript(TestClassLoader.loadClass(scriptName).newInstance(), args);
	// // addScriptResults(results);
	// addEvent(results);
	//
	// } catch (InstantiationException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// } catch (IllegalAccessException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// } catch (ClassNotFoundException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// } finally {
	//
	// }
	// }
	//
	// protected void runSuite(String suiteFileName, String sheetName) {
	// File suite = new File("");
	// String[] args = {};
	// ExcelDataFile f = new ExcelDataFile(suite.getAbsolutePath() + "/src/" +
	// suiteFileName , sheetName);
	// ScriptResults results = getResults();
	// results.setMainSuiteTest(true);
	//
	// int cnt = f.getNumberOfRecords(false);
	// for (int i = 1; i <= cnt; i++) {
	// String script = f.getData(i, "Script");
	// results.addEvent(TestClassLoader.runExecutionScript(script, args));
	// }
	// this.setResults(results);
	// // setScriptResults(this.getResults().setCalledFromSuite(true));
	// // try {
	//
	//// TestClassLoader.runTestScript("nca.demoScripts.SearchResultsPageTestScript",
	// args);
	//// TestClassLoader.runTestScript("nca.demoScripts.HomeTestScript", args);
	// }
	//
	// public static void main(String[] args) {
	// log.debug("BASE AutomatedTestScriptImplementation executed");
	// runScript(args);
	// }
	//
	// protected static String getClassName() {
	// return Thread.currentThread().getStackTrace()[2].getClassName();
	// }
	//
	// private static String getCallerCallerClassName() {
	// StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
	// String callerClassName = null;
	// for (int i = 1; i < stElements.length; i++) {
	// StackTraceElement ste = stElements[i];
	// if (!ste.getClassName().equals(TestScript.class.getName())
	// && ste.getClassName().indexOf("java.lang.Thread") != 0) {
	// if (callerClassName == null) {
	// callerClassName = ste.getClassName();
	// } else if (!callerClassName.equals(ste.getClassName())) {
	// return ste.getClassName();
	// }
	// }
	// }
	// return null;
	// }
	//
	//// protected void callScript(String... cmdScript) {
	//// try {
	//// Process procScript = Runtime.getRuntime().exec(cmdScript);
	//// } catch (IOException e) {
	//// e.printStackTrace();
	//// }
	//// }
	//
	// protected void callScript(String testScriptName, String... args) {
	// TestClassLoader.runTestScript(testScriptName, args);
	//
	// }
	//
	//
	// /**
	// * Returns the name of this script instance
	// *
	// * @return String
	// */
	// public String getScriptName() {
	// return script.getScriptName();
	// }
	//
	//
	// /**
	// * Extending classes must implement the testScript method. This will contain
	// the logic for the
	// * test script as written by the tester
	// *
	// * @param args
	// */
	// public abstract void testScript(String[] args);
	//
	//
	//
	// /**
	// * Will get the specified data file from the datasets folder specified in
	// config properties.
	// * If there is not a specified location, will default to localDataSets
	// *
	// * @param args
	// */
	// public ExcelDataFile getDataFile(String fileName, String sheetName) {
	// File path = new File("");
	// String dataFolder = ConfigProperties.getValue("DATASETS_FOLDER",
	// "/testScripts/dataSets/localDataSets");
	// String filePath = path.getAbsolutePath() + dataFolder + "/" + fileName;
	// File src = new File(path.getAbsoluteFile() + "/src/");
	// if (src.exists()){
	// filePath = path.getAbsolutePath() + "/src/" + dataFolder + "/" + fileName;
	// }
	// return new ExcelDataFile(filePath, sheetName);
	// }

	// List<Event> getEvents() {
	// return events;
	// }
}