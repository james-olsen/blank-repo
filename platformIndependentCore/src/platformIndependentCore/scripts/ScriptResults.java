package platformIndependentCore.scripts;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import platformIndependentCore.events.Event;
import platformIndependentCore.events.VerificationPoint;
import platformIndependentCore.scripts.TestScriptInterface.EXECUTION_TYPE;

/**
 * ScriptResults is an Event that contains the results from a script execution
 *
 * @author VBAAUSTAYLOL
 *
 */
public class ScriptResults extends Event {
	/** IN PROGRESS status (no stop time set) */
	private static final String IN_PROGRESS = "IN_PROGRESS";
	/** PASSED Script status */
	static final String PASSED = "PASSED";
	/** FAILED VP status */
	static final String FAILED_VP = "FAILED_VP";
	/** FAILED EXCEPTION status */
	static final String FAILED_EXCEPTION = "FAILED_EXCEPTION";
	/** logger instance for this class */
	private static Logger log = LogManager.getLogger(ScriptResults.class.getName());

	/**
	 * Constructor takes in the Event Type
	 *
	 * @param type of Event
	 */
	public ScriptResults(EVENT_TYPE type) {
		super(type);
	}

	/** Is this an Execution Script: TRUE is yes, FALSE if no */
	boolean isExecutionScript;
	/** Is this the main suite: TRUE is yes, FALSE if no */
	boolean isMainSuiteTest;
	/** Is this Called from a Suite: TRUE is yes, FALSE if no */
	boolean isCalledFromSuite;
	/** Suite Folder where these results should be written out */
	String resultsFolder = "";

	/** Type of Execution */
	EXECUTION_TYPE type;
	/** Name of test */
	String testName;
	/** Return value from script */
	Object returnValue;

	/** List of Events encountered during this script run */
	List<Event> events = new ArrayList<Event>();
	/** Duration constant for XML and Data map */
	public static final String DURATION = "duration";
	/** Stop Time constant for XML and Data map */
	public static final String STOP_TIME = "stopTime";
	/** Start Time constant for XML and Data map */
	public static final String START_TIME = "startTime";
	/** XRAY ID */
	public static final String XRAY_TEST_ID = "XRAY_TEST_ID";
	/** XRAY ID */
	public static final String XRAY_EXECUTION_ID = "XRAY_EXEC_ID";
	/** XRAY Test Plan ID */
	public static final String XRAY_PLAN_ID = "XRAY_PLAN_ID";
	/** Script execution test data id */
	public static final String DATA_ID = "DATA_ID";
	/** Script status */
	public static final String STATUS = "STATUS";
	/** Test Scripts File */
	public static final String TEST_SCRIPTS_FILE = "TestScriptsFile";
	/** Test Scripts To Run */
	public static final String TEST_SCRIPTS_TO_RUN = "TestScriptsToRun";

	/** Data map contains Label/Value pairs associated with this script execution */
	private HashMap<String, String> data = new HashMap<String, String>();

	/**
	 * Returns the data map of Label/Value pairs associated with this script
	 * execution
	 *
	 * @return HashMap<String, String> data associated with this script
	 */
	HashMap<String, String> getData() {
		return data;
	}

	/**
	 * Will add a Label/Value pair to associate with this script execution
	 *
	 * @param label to identify value
	 * @param value to store information
	 */
	void addData(String label, String value) {
		data.put(label, value);
	}

	/**
	 * Will calculate and save the Duration based on the start and stop times
	 * provided
	 *
	 * @param startDate records starting time
	 * @param stopDate  records end time
	 */
	public void setDuration(Date startDate, Date stopDate) {

		long diff = stopDate.getTime() - startDate.getTime();
		long diffSeconds = diff / 1000 % 60;
		long diffMinutes = diff / (60 * 1000) % 60;
		long diffHours = diff / (1000 * 60 * 60) % 24;
		long diffDays = diff / (1000 * 60 * 60 * 24) % 365;

		// Always provide the Minutes and Seconds
		String executionDuration = getTimeUnitText(diffMinutes, " Minute") + ", "
				+ getTimeUnitText(diffSeconds, "Second");

		// Include Hours if more than 0
		if (diffHours > 0) {
			executionDuration = getTimeUnitText(diffHours, "Hour") + ", " + executionDuration;
		}

		// Include Days if more than 0
		if (diffDays > 0) {
			executionDuration = getTimeUnitText(diffDays, "Day") + ", " + executionDuration;
		}

		addData(ScriptResults.DURATION, executionDuration);
	}

	/**
	 * Will return the text for the specified time unit. This method mainly just
	 * determines if there should be an "s" at the end of the time unit.
	 *
	 * @param timeValue       the number of days, minutes, seconds, etc
	 * @param unitDescription the unit of time (ex: Day, Minute, Seconds, etc)
	 * @return String with the correct format
	 */
	private String getTimeUnitText(long timeValue, String unitDescription) {
		String timeText = timeValue + " " + unitDescription;
		if (timeValue != 1) {
			timeText += "s";
		}
		return timeText;
	}

	/**
	 * Set the return value from the script
	 *
	 * @param returnValue from script completion
	 */
	public void setReturnValue(Object returnValue) {
		this.returnValue = returnValue;
	}

	/**
	 * Returns the value returned from the script
	 *
	 * @return Object return value. Can be any type of object, your code is
	 *         responsible for knowing what to cast it to
	 */
	public Object getReturnValue() {
		return returnValue;
	}

	/**
	 * Will set the stop time to the provided Date time
	 *
	 * @param stopTime when the script completed
	 */
	public void setStopTime(Date stopTime) {

		addData(ScriptResults.STOP_TIME, DATE_TIME_FORMAT.format(stopTime));
	}

	/**
	 * Will set the start time to the provided Date time
	 *
	 * @param startTime when the script started
	 */
	public void setStartTime(Date startTime) {

		addData(ScriptResults.START_TIME, DATE_TIME_FORMAT.format(startTime));
	}

	/**
	 * Returns the Start time in a String format
	 *
	 * @return String representing start time
	 */
	String getStartTime() {
		return getData(ScriptResults.START_TIME);
	}

	/**
	 * Returns the Stop time in a String format
	 *
	 * @return String representing end time
	 */
	String getStopTime() {
		return getData(ScriptResults.STOP_TIME);
	}

	/**
	 * Returns the duration of the script
	 *
	 * @return String representing duration of script
	 */
	String getDuration() {
		return getData(ScriptResults.DURATION);
	}

	/**
	 * Verifies if this is an execution script
	 *
	 * @return isExecutionScript true if it is, false if not
	 */
	@Override
	public boolean isExecutionScript() {
		return isExecutionScript;
	}

	/**
	 * Will check Events to find if there was an Exception during the Script
	 * Execution
	 *
	 * @return boolean TRUE if failed due to Exception, FALSE if not
	 */
	public boolean isFailedWithException() {
		boolean failedWithException = false;
		for (Event event : getEvents()) {
			if (event.isException()) {
				failedWithException = true;
				break;
			}
		}
		return failedWithException;
	}

	/**
	 * @param isExecutionScript the isExecutionScript to set
	 */
	public void setExecutionScript(boolean isExecutionScript) {
		if (isExecutionScript) {
			type = EXECUTION_TYPE.EXECUTION;
		} else {
			type = EXECUTION_TYPE.MODULAR;
		}
		this.isExecutionScript = isExecutionScript;
	}

	/**
	 * @return the isMainSuiteTest
	 */
	public boolean isMainSuiteTest() {
		return isMainSuiteTest;
	}

	/**
	 * @param isMainSuiteTest the isMainSuiteTest to set
	 */
	public void setMainSuiteTest(boolean isMainSuiteTest) {
		this.isMainSuiteTest = isMainSuiteTest;
		type = EXECUTION_TYPE.KICKOFF;
	}

	/**
	 * @return the isCalledFromSuite
	 */
	public boolean isCalledFromSuite() {
		return isCalledFromSuite;
	}

	/**
	 * @param isCalledFromSuite the isCalledFromSuite to set
	 */
	public void setCalledFromSuite(boolean isCalledFromSuite) {
		this.isCalledFromSuite = isCalledFromSuite;
	}

	/**
	 * Will return the folder for results
	 *
	 * @return String results folder
	 */
	public String getResultsFolder() {
		return resultsFolder;
	}

	/**
	 * Will set the folder for where results are being stored
	 *
	 * @param resultsFolder the folder path for all results
	 */
	public void setResultsFolder(String resultsFolder) {
		this.resultsFolder = resultsFolder;
	}

	/**
	 * Will add the event to the results
	 *
	 * @param event to add to results
	 */
	public void addEvent(Event event) {
		log.debug(" --- " + this.toString() + " ADDING EVENT: " + event.getEventType() + ", " + event.getScriptName());
		for (Event e : events) {
			log.debug(" @!@!@!@!@! BEFORE add Event: " + e.getEventType() + ", " + e.getScriptName());
		}
		events.add(event);

		for (Event e : events) {
			log.debug(" @!@!@!@!@! After add Event: " + e.getEventType() + ", " + e.getScriptName());
		}

	}

	/**
	 * @return the events
	 */
	public List<Event> getEvents() {
		return events;
	}

	/**
	 * Will return the value associated with the specified key
	 *
	 * @param key label used to look up data
	 * @return value associated with key
	 */
	public String getData(String key) {
		return getData().get(key);
	}

	/**
	 * Will return the Execution Type associated with this script
	 *
	 * @return EXECUTION_TYPE of script
	 */
	public EXECUTION_TYPE getExecutionType() {
		return type;
	}

	/**
	 * Will set the EXECUTION_TYPE for this script
	 *
	 * @param scriptType EXECUTION_TYPE of script
	 */
	public void setExecutionType(EXECUTION_TYPE scriptType) {
		type = scriptType;
	}

	/**
	 * Will return the overall status for this set of script results. If there is
	 * not an end time for the results, and there have been no failures, will return
	 * a status of IN_PROGRESS
	 *
	 * @return String status "PASSED", "FAILED_EXCEPTION", "FAILED_VP" or
	 *         "IN_PROGRESS"
	 */
	public String getStatus() {
		// if the script has stopped, default to PASSED otherwise default to IN_PROGRESS
		// as the script has not completed to get a PASSED status
		String status = getStopTime().isEmpty() ? IN_PROGRESS : PASSED;

		// Look for both VP Failures and Exceptions
		for (Event event : events) {
			if (event.isException()) {
				status = FAILED_EXCEPTION;
			} else if (event.isVerificationPoint()) {
				VerificationPoint vp = (VerificationPoint) event;
				if (!vp.isPass()) {
					status = FAILED_VP;
				}
				// isExecutionScript was using an overridden implementation that we did not
				// want, so check manually
			} else if (event.isCalledScript() || event.getEventType().equals(EVENT_TYPE.EXECUTIONSCRIPT)) {
				String eventStatus = ((ScriptResults) event).getStatus();
				// Only change the overall status if this one has a failure
				if (eventStatus.startsWith("FAILED")) {
					status = eventStatus;
				}
			}
			// If there is an Exception status, stop because that will override VP failures
			if (status.contains("EXCEPTION")) {
				break;
			}

		}
		return status;
	}

}
