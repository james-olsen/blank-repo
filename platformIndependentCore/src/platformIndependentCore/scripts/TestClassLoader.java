package platformIndependentCore.scripts;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import platformIndependentCore.core.ToolManager;
import platformIndependentCore.events.Event.EVENT_TYPE;
import platformIndependentCore.scripts.TestScriptInterface.EXECUTION_TYPE;

/**
 * Will load classes based on their names. Contains methods to kick off
 * TestScripts given their names, it will locate the correct class and run
 *
 * @author VBAAUSTAYLOL
 *
 */
public class TestClassLoader extends ToolManager {
	/** logger for class */
	static Logger log = LogManager.getLogger(TestClassLoader.class.getName());

	/**
	 * Will load the class with matching name This method searches for classes in
	 * the same manner as the loadClass(String, boolean) method. It is invoked by
	 * the Java virtual machine to resolve class references.
	 *
	 * @param name of class to load
	 * @return {@code Class<TestScriptInterface>}
	 * @throws ClassNotFoundException if unable to find class with matching name
	 */
	@SuppressWarnings("unchecked")
	public static Class<TestScriptInterface> loadClass(String name) throws ClassNotFoundException {
		Class<TestScriptInterface> foundClass = null;
		ClassLoader classLoader = TestClassLoader.class.getClassLoader();

		try {
			foundClass = (Class<TestScriptInterface>) classLoader.loadClass(name);
			log.info("aClass.getName() = " + foundClass.getName());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return foundClass;
	}

	/**
	 * Will load the class with specified name for RFT
	 *
	 * @param name class name
	 * @return {@code Class<TestScript>} script class
	 * @throws ClassNotFoundException if unable to find class with matching name
	 */
	@SuppressWarnings("unchecked")
	public static Class<TestScript> loadRftClass(String name) throws ClassNotFoundException {
		Class<TestScript> foundClass = null;
		ClassLoader classLoader = TestClassLoader.class.getClassLoader();

		try {
			foundClass = (Class<TestScript>) classLoader.loadClass(name);
			log.info("aClass.getName() = " + foundClass.getName());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return foundClass;
	}

	/**
	 * Will run the specified script as a modular script and provide it the given
	 * arguments
	 *
	 * @param name of script to run
	 * @param args script arguments
	 * @return ScriptResults returned from running script
	 */
	@Deprecated
	public static ScriptResults runTestScript(String name, Arguments args) {
		ScriptResults results = null;
		try {
			results = getAutomationTool().runModularScript(TestClassLoader.loadClass(name).newInstance(), args);
			results.setScriptName(name);
			results.setExecutionType(EXECUTION_TYPE.MODULAR);

		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return results;
	}

	/**
	 * Will run the specified script as an execution script and provide it the given
	 * arguments
	 *
	 * @param name of script to run
	 * @param args script arguments
	 * @return ScriptResults returned from running script
	 */
	public static ScriptResults runExecutionScript(String name, Arguments args) {
		ScriptResults results = new ScriptResults(EVENT_TYPE.EXECUTIONSCRIPT);
		results.setExecutionType(EXECUTION_TYPE.EXECUTION);
		try {
			results = getAutomationTool().runExecutionScript(TestClassLoader.loadClass(name).newInstance(), args); // runModularScript(TestClassLoader.loadClass(name).newInstance(),
																													// args);
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		results.setCalledFromSuite(true);
		// TODO If we wanted to attempt to send the individual Test execution results
		// into Xray at the end of each execution script, this might be the location to
		// send those results to Xray. The question becomes what format the results
		// information is in at this point.
		// Need to then make sure that we don't send the results of individual tests
		// during the last post to xray
		// Maybe add a report results specifically for execution script and another for
		// the suite
//				if(xray reporting is turned on) {
//				XrayServerReporting xray = new XrayServerReporting();
//				xray.reportResults(testKey, results, resultsLog);
//				}
		return results;
	}

}
