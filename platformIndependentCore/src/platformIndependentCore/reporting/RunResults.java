package platformIndependentCore.reporting;

import java.io.File;
import java.io.IOException;
import java.util.List;

import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONException;
import kong.unirest.json.JSONObject;
import platformIndependentCore.events.Event;
import platformIndependentCore.events.Note;
import platformIndependentCore.events.ScreenShot;
import platformIndependentCore.events.VerificationPoint;
import platformIndependentCore.scripts.ScriptResults;
import platformIndependentCore.utilities.ConfigProperties;

/**
 * <b>Name :</b> RunResults.java
 * <p>
 * <b>Generated :</b> Aug 3, 2020
 * <p>
 * <b>Description :</b> Contains and manages the Results information for a Test
 * Run
 * <p>
 *
 * @since Aug 3, 2020
 * @author VBAAUSTAYLOL
 */
public class RunResults {
	/** number of VPs */
	int vpCountRun = 0;
	/** number of Failed VPs */
	int vpFailedRun = 0;
	/** number of Passed VPs */
	int vpPassRun = 0;
	/** number of Exceptions */
	int exceptionCntRun = 0;
	/** Fail status */
	boolean isFailed = false;
	/** formatted failed VP details */
	String runFailedVpInfo = "";
	/** formatted exception details */
	String runExceptionInfo = "";
	/** Script Results for this run */
	ScriptResults runResults;
	/** Failed VP Details in Table Row format */
	String failedVpRows = "";
	/** Array of Evidence to attach to the result */
	JSONArray runEvidenceArray = new JSONArray();

	/**
	 * Constructor will set the ScriptResults and generate result information
	 *
	 * @param runResults ScriptResults to generate results for
	 */
	public RunResults(ScriptResults runResults) {
		this.runResults = runResults;
		addResults(runResults);
	}

	/**
	 * Will calculate the result details for the current ScriptResults set
	 *
	 * @param currentResults ScriptResults
	 */
	private void addResults(ScriptResults currentResults) {
		List<Event> runEvents = currentResults.getEvents();
		for (Event currEvent : runEvents) {
			if (currEvent.getEventType() == Event.EVENT_TYPE.EXECUTIONSCRIPT
					|| currEvent.getEventType() == Event.EVENT_TYPE.CALLEDSCRIPT) {
				addResults((ScriptResults) currEvent);
			} else if (currEvent.isVerificationPoint()) {
				vpCountRun++;
				VerificationPoint vpEvent = (VerificationPoint) currEvent;
				Object actual = vpEvent.getActual();
				Object expected = vpEvent.getExpected();
				String vpName = vpEvent.getVpName();

				boolean pass = vpEvent.isPass();
				if (!pass) {
					vpFailedRun++;
					// vpFailed++;
					isFailed = true;
					/*
					 * NOTE - The Test Run and the Test Execution support different formatting, so
					 * am building two different Strings to display the information, the briefer
					 * summary for the Test Run and a Results table for the Test Execution
					 */
					// Build the info for the Test Run
					runFailedVpInfo += "[" + vpName + "] " + " EXPECTED:  ( *" + expected + "* )    ACTUAL:  ( *"
							+ actual + "* )" + System.lineSeparator();
					// Also track the failed VPs for the Test Execution that has a summary table
					failedVpRows += "| " + vpEvent.getScriptName() + "| " + vpName + " | " + "EXPECTED:  ( *" + expected
							+ "* ) " + System.lineSeparator() + "ACTUAL:  ( *" + actual + "* )"
							+ System.lineSeparator();
				} else {
					vpPassRun++;
				}

			} else if (currEvent.isNote()) {
				Note note = (Note) currEvent;
				if (note.isException()) {
					isFailed = true;
					// exceptionCnt++;
					exceptionCntRun++;
					runExceptionInfo += note.getNote() + System.lineSeparator();
				}
			} else if (currEvent.isScreenShot()) {

				if (ConfigProperties.getValue(ConfigProperties.DISABLE_SCREEN_SHOT_REPORTING_ATTACHMENT, "FALSE")
						.equalsIgnoreCase("FALSE")) {

					// Add ScreenShots to the Evidence
					ScreenShot screenShotEvent = (ScreenShot) currEvent;

					JSONObject screenShot = new JSONObject();
					File screenShotFile = screenShotEvent.getScreenShotFile();
					screenShot.put("filename", screenShotFile.getName());
					screenShot.put("contentType", "image/png");
					try {
						screenShot.put("data", XrayCloudReporting.encodeFileToBase64Binary(screenShotFile));
						// Add the results log to the evidence
						runEvidenceArray.put(screenShot);
					} catch (JSONException e1) {
						e1.printStackTrace();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}

			}
		}

	}

	/**
	 * Returns number of VPs run
	 *
	 * @return int number of VPs run
	 */
	public int getVpCountRun() {
		return vpCountRun;
	}

	/**
	 * Sets the number of VPs run
	 *
	 * @param vpCountRun the vpCountRun to set
	 */
	public void setVpCountRun(int vpCountRun) {
		this.vpCountRun = vpCountRun;
	}

	/**
	 * Returns the number of VPs that failed
	 *
	 * @return int number of VPs that failed
	 */
	public int getVpFailedRun() {
		return vpFailedRun;
	}

	/**
	 * Sets the number of VPs that failed
	 *
	 * @param vpFailedRun number of failed VPs
	 */
	public void setVpFailedRun(int vpFailedRun) {
		this.vpFailedRun = vpFailedRun;
	}

	/**
	 * Returns the number VPs that passed
	 *
	 * @return int number VPs that passed
	 */
	public int getVpPassRun() {
		return vpPassRun;
	}

	/**
	 * Sets the number of passed VPs
	 *
	 * @param vpPassRun number of VPs that passed
	 */
	public void setVpPassRun(int vpPassRun) {
		this.vpPassRun = vpPassRun;
	}

	/**
	 * Returns the number of Exceptions thrown
	 *
	 * @return int exception count
	 */
	public int getExceptionCntRun() {
		return exceptionCntRun;
	}

	/**
	 * Sets the number of exceptions thrown
	 *
	 * @param exceptionCntRun exception count
	 */
	public void setExceptionCntRun(int exceptionCntRun) {
		this.exceptionCntRun = exceptionCntRun;
	}

	/**
	 * Returns if this run failed
	 *
	 * @return boolean returns TRUE if failed, FALED if passed
	 */
	public boolean isFailed() {
		return isFailed;
	}

	/**
	 * Sets if this run failed or not
	 *
	 * @param isFailed TRUE if failed, FALSE if not
	 */
	public void setFailed(boolean isFailed) {
		this.isFailed = isFailed;
	}

	/**
	 * Returns information on failed VPs
	 *
	 * @return String details for failed VPs
	 */
	public String getRunFailedVpInfo() {
		return runFailedVpInfo;
	}

	/**
	 * Sets the failed VP information
	 *
	 * @param runFailedVpInfo details for failed VPs
	 */
	public void setRunFailedVpInfo(String runFailedVpInfo) {
		this.runFailedVpInfo = runFailedVpInfo;
	}

	/**
	 * Returns information for Exception(s)
	 *
	 * @return String exception information
	 */
	public String getRunExceptionInfo() {
		return runExceptionInfo;
	}

	/**
	 * Sets the exception information
	 *
	 * @param runExceptionInfo exception information
	 */
	public void setRunExceptionInfo(String runExceptionInfo) {
		this.runExceptionInfo = runExceptionInfo;
	}

	/**
	 * Returns the ScriptResults for this test run
	 *
	 * @return ScriptResults runResults
	 */
	public ScriptResults getRunResults() {
		return runResults;
	}

	/**
	 * Sets the ScriptResults for this test run
	 *
	 * @param runResults the runResults to set
	 */
	public void setRunResults(ScriptResults runResults) {
		this.runResults = runResults;
	}

	/**
	 * Returns failed VP rows
	 *
	 * @return String failedVpRows
	 */
	public String getFailedVpRows() {
		return failedVpRows;
	}

	/**
	 * Sets the failed VP rows for reporting summaries
	 *
	 * @param failedVpRows the failedVpRows to set
	 */
	public void setFailedVpRows(String failedVpRows) {
		this.failedVpRows = failedVpRows;
	}

	/**
	 * Returns a JSONArray of all the run evidence to be included in the test run
	 * reporting
	 *
	 * @return JSONArray runEvidenceArray
	 */
	public JSONArray getRunEvidenceArray() {
		return runEvidenceArray;
	}

	/**
	 * Sets the run Evidence array
	 *
	 * @param runEvidenceArray JSONArray of evidence for test run
	 */
	public void setRunEvidenceArray(JSONArray runEvidenceArray) {
		this.runEvidenceArray = runEvidenceArray;
	}

}
