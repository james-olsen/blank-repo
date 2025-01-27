package platformIndependentCore.reporting;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONException;
import kong.unirest.json.JSONObject;
import platformIndependentCore.datafiles.CsvDataFile;
import platformIndependentCore.events.Event;
import platformIndependentCore.events.Note;
import platformIndependentCore.events.VerificationPoint;
import platformIndependentCore.exceptions.InvalidDataException;
import platformIndependentCore.exceptions.InvalidParameterException;
import platformIndependentCore.exceptions.InvalidStateException;
import platformIndependentCore.exceptions.ReportingException;
import platformIndependentCore.exceptions.WrappedException;
import platformIndependentCore.scripts.ScriptResults;
import platformIndependentCore.scripts.TestScriptManager;
import platformIndependentCore.utilities.ConfigProperties;
import platformIndependentCore.utilities.CoreDateTimeFormat;
import platformIndependentCore.utilities.CryptoUtils;
import platformIndependentCore.utilities.DateCalculator;
import platformIndependentCore.utilities.DateTimeHelper;
import platformIndependentCore.utilities.ZipUtil;
import utilities.DataFileHelper;

/**
 * <b>Name :</b> XrayServerReporting.java
 * <p>
 * <b>Generated :</b> Sep 18, 2020
 * <p>
 * <b>Description :</b> Class to interact with the Xray Server, provides ability
 * to push test results into Xray
 * <p>
 *
 * @since Sep 18, 2020
 * @author VBAAUSTAYLOL
 */
public class XrayServerReporting extends TestExecutionReporting {
	/** The explicit path with file name to the auth.key */
	private static final String AUTH_KEY_PATH = "C:/Automation/Tools/properties/auth.key";
	/** Logger instance for this class */
	private static Logger log = LogManager.getLogger(TestScriptManager.class.getName());
	/** Jira Unsuccessful login response **/
	private static final String JIRA_LOGIN_UNSUCCESSFUL_RESPONSE = "Client must be authenticated to access this resource.";
	/** TestDataId **/
	private final String TEST_DATA_ID = ConfigProperties.getValue("DATA_ID_COLUMN", "DATA_ID");
	/** Issue_Post_Successful **/
	private final String ISSUE_COMMENT_POST_SUCCESFUL = "Issue_Comment_Post_Successful";
	/** Test_Exec_Post_Successful **/
	private final String TEST_EXEC_POST_SUCCESSFUL = "Test_Exec_Post_Successful";
	/** Attachment_Post_Successful **/
	private final String ATTACHMENT_POST_SUCCESSFUL = "Attachment_Post_Successful";
	/** Location_Issue_Attachment_Json **/
	private final String ISSUE_COMMENT_JSON_LOCATION = "Issue_Comment_JSON_Location";
	/** JSON_File_Location **/
	private final String TEST_EXEC_JSON_LOCATION = "Test_Exec_JSON_Location";
	/** Attachment_File_Location **/
	private final String ATTACHMENT_FILE_LOCATION = "Attachment_File_Location";
	/** Issue_ID **/
	private final String ISSUE_ID = "Issue_ID";
	/** ZIP_FILE_FAILURE **/
	private final String ZIP_FILE_FAILURE = "ZIP_FILE_FAILURE";
	/** Tilde Delimiter **/
	private final String DELIMETER = "~";
	/** Headers for the rolling logs **/
	private final String XRAY_LOG_HEADERS = TEST_DATA_ID + DELIMETER + TEST_EXEC_POST_SUCCESSFUL + DELIMETER
			+ ATTACHMENT_POST_SUCCESSFUL + DELIMETER + ISSUE_COMMENT_POST_SUCCESFUL + DELIMETER
			+ ISSUE_COMMENT_JSON_LOCATION + DELIMETER + ATTACHMENT_FILE_LOCATION + DELIMETER + TEST_EXEC_JSON_LOCATION
			+ DELIMETER + ISSUE_ID;
	/** Location for the JKS certificate for authentication */
	private static final String JKS_CERTIFICATE = ConfigProperties.getValue(ConfigProperties.XRAY_CERTIFICATE);
	/** Test Execution Issue ID */
	private final String TEST_EXEC_ISSUE = "testExecIssue";
	/** Type of Authentication to be used. Should be set to BASIC or CERTIFICATE */
	private static final String AUTHENTICATION_TYPE = ConfigProperties.getValue(ConfigProperties.XRAY_AUTHENTICATION);
	/** Username for authentication */
	private static String username;
	/** token for authentication */
	private static String token;
	/** password for certificate */
	private static String ENCRYPTED_CERTIFICATE_PW;
	/** File writer used to create json file **/
	private static FileWriter file;
	/** Number of milliseconds to wait between post attempts to XRay **/
	private static final int NUM_OF_MILLISECONDS_TO_WAIT = 60000; // 1 minute
	/** HTTP Code 201 **/
	private static final int HTTP_201 = 201;
	/** HTTP Code 200 **/
	private static final int HTTP_200 = 200;
	/** HTTP Code 400 **/
	private static final int HTTP_400 = 400;
	/** Xray Reference File column header for Test Execution Id */
	public String XRAY_REF_EXECUTION_ID_COLUMN = "XRAY_TEST_EXECUTION_ID";
	/** Xray Reference File column header for Test Plan Id */
	public String XRAY_REF_PLAN_ID_COLUMN = "XRAY_TEST_PLAN_ID";
	/** Xray Reference File column header for Fix Version */
	public String XRAY_REF_FIX_VERSION_COLUMN = "XRAY_FIX_VERSION";

	/**
	 * Number of XRay attempts before failing and writing the post to file for post
	 * later
	 **/
	private static final int NUMBER_ATTEMPTS = 3;
	/** the base folder for the failed XRay post to post later **/
	private static final String BASE_FOLDER_FOR_FAILED_JSON_FILES = "testResults/failedXRayPostJsonFiles/";

	/**
	 * Will create an instance of Xray Server Reporting
	 */
	public XrayServerReporting() {
		super(ConfigProperties.getValue(ConfigProperties.XRAY_SERVER_URL));
	}

	/** TRUE if XRAY reporting is turned on */
	private static boolean isXrayOn = ConfigProperties.getValue(ConfigProperties.XRAY_REPORTING, "FALSE")
			.equalsIgnoreCase("TRUE");

	/**
	 * Sets the value of isXrayOn
	 *
	 * @param isOn true if reporting should be set to on, false if it should be
	 *             turned off.
	 */
	private void setIsXrayOn(boolean isOn) {
		isXrayOn = isOn;
	}

	@Override
	public boolean isReportingOn() {
		return isXrayOn;
	}

	/**
	 * Will add a new Test object in Xray, adding the information provided
	 *
	 * @param project      where the Test should be created
	 * @param summary      value to be entered in the summary field
	 * @param description  value to be entered in the description field
	 * @param labels       a list of labels to associate with the Test
	 * @param testSets     (optional) a list of Test Sets to associate with the Test
	 * @param customFields String hashmap <customFieldId, value>
	 * @return String xray id of the newly created Test object
	 */
	public String addTest(String project, String summary, String description, String[] labels,
			ArrayList<String> testSets, HashMap<String, String> customFields) {

		// logic to write out json
		String addTestUrl = getIssueUrl();

		JSONObject testJson = new JSONObject();
		JSONObject fieldsJson = new JSONObject();

		// Set the Project this Test should be entered in
		JSONObject projectJson = new JSONObject();
		projectJson.put("key", project);
		fieldsJson.put("project", projectJson);

		// Set the summary for the Test
		fieldsJson.put("summary", summary);
		// Set the description for the Test
		fieldsJson.put("description", description);

		// Create the issue as a Test Issue
		JSONObject issueTypeJson = new JSONObject();
		issueTypeJson.put("name", "Test");
		fieldsJson.put("issuetype", issueTypeJson);

		// Set the Test Type to Automated
		JSONObject testTypeJson = new JSONObject();
		testTypeJson.put("value", "Automated");
		// Test Type is "customfield_12100"
		fieldsJson.put("customfield_12100", testTypeJson);

		// loop through custom fields hashmap and create JSON objects
		Set<String> keys = customFields.keySet();
		for (String key : keys) {
			JSONObject testKeyJson = new JSONObject();
			testKeyJson.put("value", customFields.get(key));
			fieldsJson.put(key, testKeyJson);
		}

		// Add the Test Sets
		JSONArray testSetsArray = new JSONArray();
		testSetsArray.put(testSets);
		// Test Sets is "customfield_12107"
		fieldsJson.put("customfield_12107", testSets);

		// Add the labels
		JSONArray labelArray = new JSONArray();
		// While I was able to add the String[] to the JSONArray for TestSets, this one
		// fails, and I need to add one label at a time
		for (String label : labels) {
			labelArray.put(label);
		}
		fieldsJson.put("labels", labelArray);

		testJson.put("fields", fieldsJson);

		HttpResponse<JsonNode> results = sendPostXray(addTestUrl, testJson);

		if (results == null) {
			throw new InvalidStateException(
					"Unable to login to Jira/Xray, please verify that Jira/Xray is available and try again.");
		}
		return results.getBody().getObject().getString("key");
	}

	/**
	 * Creates a folder in the testResults folder for failed XRay results
	 */
	private void createFailedFolder() {
		File directory = new File(BASE_FOLDER_FOR_FAILED_JSON_FILES);
		if (!directory.exists()) {
			directory.mkdir();
		}
	}

	@Override
	public String reportResults(String testKey, ScriptResults results, String resultsLog) {
		String testExecutionKey = "";

		List<Event> events = results.getEvents();
		String startTime = results.getData(ScriptResults.START_TIME);
		String endTime = results.getData(ScriptResults.STOP_TIME);
		String xrayTestId = results.getData(ScriptResults.XRAY_TEST_ID);
		String xrayExecutionId = results.getData(ScriptResults.XRAY_EXECUTION_ID);
		String xrayPlanId = ConfigProperties.getValue(ScriptResults.XRAY_PLAN_ID);
		boolean isSuite = results.isMainSuiteTest();
		Date startDate = Event.DATE_TIME_FORMAT.getDate(startTime);
		Date endDate = Event.DATE_TIME_FORMAT.getDate(endTime);
		// Get the format needed for sending dates to Xray via JSON data
		CoreDateTimeFormat machineFormat = new CoreDateTimeFormat("YYYY-MM-dd'T'HH:mm:ssXXX");

		if (isReportingOn()) {
			// Make sure Authentication File is set
			if (getAuthenticationFile() == null || getAuthenticationFile().isEmpty()) {
				throw new ReportingException(
						"XRAY Reporting is turned ON, but you are missing the XRAY_AUTH_FILE is undefined in your config.properties");
			}
			// If this is not a Suite, need an XRAY ID to associate with the Test Run.
			if (!isSuite && (xrayTestId == null || xrayTestId.isEmpty())) {
				throw new ReportingException(
						"XRAY Reporting is turned ON, but you are missing the XRAY ID for this Test Run. You must include the XRAY ID in the runScript method inside the script's public static void main(String[] args) method.");
			}

			/** ******************************************************* */
			/** Gather event information to add to the Comments section */
			/** ******************************************************* */
			boolean isFailed = false;
			int vpCount = 0;
			int vpFailed = 0;
			int vpPass = 0;
			int exceptionCnt = 0;

			String exceptionInfo = "";
			// Only add the table formatting if this is a suite
			String failedVpInfo = isSuite ? "| *SCRIPT* | *VERIFICATION POINT* | *RESULTS*" + System.lineSeparator()
					: "";
			JSONArray testsArray = new JSONArray();

			/** ************************************************************** */
			/** Loop through Events to build information on VPs (and TestRuns) */
			/** ************************************************************** */
			for (Event event : events) {
				if (event.isExecutionScript()) {
					// Check if this is an Execution Script, if so, need to add Test Run results
					ScriptResults runResults = (ScriptResults) event;

					String dataId = runResults.getData(ScriptResults.XRAY_TEST_ID);
					// Generate a Test Run object for the execution script
					/** ********************************************************** */
					/** Create the JSON for the Test Run information, uses Test ID */
					/** ********************************************************** */
					String runStartTime = runResults.getData(ScriptResults.START_TIME);
					String runEndTime = runResults.getData(ScriptResults.STOP_TIME);

					Date runStartDate = Event.DATE_TIME_FORMAT.getDate(runStartTime);
					Date runEndDate = Event.DATE_TIME_FORMAT.getDate(runEndTime);
					String runDuration = runResults.getData(ScriptResults.DURATION);
					String runNote = runResults.getExecutionNote();

					String runFailedVpInfo = "";
					String runExceptionInfo = "";
					RunResults runScriptResults = new RunResults(runResults);
					// now add any results for this script from the runScriptResults object
					JSONArray runEvidenceArray = runScriptResults.getRunEvidenceArray();
					vpCount += runScriptResults.getVpCountRun();
					vpFailed += runScriptResults.getVpFailedRun();
					vpPass += runScriptResults.getVpPassRun();

					exceptionCnt += runScriptResults.getExceptionCntRun();
					if (runScriptResults.isFailed) {
						isFailed = true;
					} else {
						isFailed = false;
					}

					runFailedVpInfo += runScriptResults.getRunFailedVpInfo();
					// Also track the failed VPs for the Test Execution that has a summary table
					failedVpInfo += runScriptResults.getFailedVpRows();
					int vpCountRun = runScriptResults.getVpCountRun();
					int vpFailedRun = runScriptResults.getVpFailedRun();
					int vpPassRun = runScriptResults.getVpPassRun();
					int exceptionCntRun = runScriptResults.getExceptionCntRun();

					// Build details for the Test Run
					// Comment field will hold # Exception, Duration and Note
					String runDurationText = "*Start:* " + runStartTime + System.lineSeparator() + "*Duration:* "
							+ runDuration + System.lineSeparator() + System.lineSeparator();

					String runComment = System.lineSeparator() + "*" + exceptionCntRun + "* Exceptions "
							+ runExceptionInfo + System.lineSeparator() + System.lineSeparator() + runDurationText;
					if (!runNote.isEmpty()) {
						runComment += "*Note:* " + runNote;
					}

					// Comments field will hold all VP info and execution result information
					String testRunDetails = "*" + vpCountRun + " Verification Points*  " + System.lineSeparator()
							+ System.lineSeparator() + "* " + vpPassRun + " PASSED" + System.lineSeparator() + "* "
							+ vpFailedRun + "  FAILED" + System.lineSeparator() + System.lineSeparator()
							+ System.lineSeparator() + "*FAILED VPs*" + System.lineSeparator() + runFailedVpInfo
							+ System.lineSeparator() + System.lineSeparator() + runComment;

					JSONObject testRunJSON = new JSONObject();
					// Build the JSON data for the Test Run details
					testRunJSON.put("testKey", dataId);
					testRunJSON.put("comment", testRunDetails);
					testRunJSON.put("status", isFailed ? "FAIL" : "PASS");
					testRunJSON.put("start", machineFormat.format(runStartDate));
					testRunJSON.put("finish", machineFormat.format(runEndDate));

					// Build a JSON object to attach the HTML Results File
					JSONObject resultsFileJSON = new JSONObject();
					String resultsFile = runResults.getData("RESULTS_FILE");
					if (resultsFile != null && !resultsFile.isEmpty()) {
						File rf = new File(resultsFile);
						resultsFileJSON.put("filename", rf.getName());
						resultsFileJSON.put("contentType", "text/html");
						try {
							resultsFileJSON.put("data", encodeFileToBase64Binary(rf));
							// Add the results log to the evidence
							runEvidenceArray.put(resultsFileJSON);
						} catch (JSONException e1) {
							e1.printStackTrace();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
					testRunJSON.put("evidences", runEvidenceArray);
					// Add this Test Run info to the test array for the suite (Test Execution)
					testsArray.put(testRunJSON);
				} else if (event.isVerificationPoint()) {
					// Check to see if the current event is a Verification Point to add summary info
					// and update the PASS/FAIL status of need be
					vpCount++;
					VerificationPoint vpEvent = (VerificationPoint) event;
					Object actual = vpEvent.getActual();
					Object expected = vpEvent.getExpected();
					String vpName = vpEvent.getVpName();
					boolean pass = vpEvent.isPass();
					if (!pass) {
						vpFailed++;
						isFailed = true;
						failedVpInfo += "[" + vpName + "] " + " EXPECTED:  ( *" + expected + "* )    ACTUAL:  ( *"
								+ actual + "* )" + System.lineSeparator();
					} else {
						vpPass++;
					}

				} else if (event.isNote()) {
					Note note = (Note) event;
					if (note.isException()) {
						isFailed = true;
						exceptionCnt++;
						exceptionInfo += note.getNote() + System.lineSeparator();
					}
				}

			} // End Event loop

			/** ***************************************************************** */
			/** Create the JSON object with the information for the TestExecution */
			/** ***************************************************************** */
			JSONObject executionObject = new JSONObject();
			String note = results.getExecutionNote();
			String duration = results.getData(ScriptResults.DURATION);
			String addOn = "*Start:* " + startTime + "        *End:* " + endTime + System.lineSeparator()
					+ "*Duration:* " + duration + System.lineSeparator() + System.lineSeparator();
			if (!note.isEmpty()) {
				addOn += "*NOTE:* " + note + System.lineSeparator() + System.lineSeparator();
			}
			String details = addOn + "*" + exceptionCnt + " Exceptions*" + exceptionInfo + System.lineSeparator()
					+ System.lineSeparator() + "*" + vpCount + " Verification Points*  " + System.lineSeparator()
					+ System.lineSeparator() + "* *" + vpPass + "* PASSED" + System.lineSeparator() + "* *" + vpFailed
					+ "*  FAILED" + System.lineSeparator() + System.lineSeparator() + "*FAILED VPs*"
					+ System.lineSeparator() + failedVpInfo;

			/** ********************************************************** */
			/** Create the JSON for the Test Run information, uses Test ID */
			/** ********************************************************** */
			if (!isSuite) {
				// Get the Run Results for this script
				RunResults executionScriptResults = new RunResults(results);

				vpCount += executionScriptResults.getVpCountRun();
				vpFailed += executionScriptResults.getVpFailedRun();
				vpPass += executionScriptResults.getVpPassRun();

				exceptionCnt += executionScriptResults.getExceptionCntRun();
				if (executionScriptResults.isFailed) {
					isFailed = true;
				}

				// Also track the failed VPs for the Test Execution that has a summary table
				failedVpInfo += executionScriptResults.getFailedVpRows();

				// This is not a suite, so need to change the formatting of the details
				details = addOn + System.lineSeparator() + System.lineSeparator() + "*" + exceptionCnt + " Exceptions*"
						+ exceptionInfo + System.lineSeparator() + System.lineSeparator() + "*" + vpCount
						+ " Verification Points*  " + System.lineSeparator() + System.lineSeparator() + "* " + vpPass
						+ " PASSED" + System.lineSeparator() + "* " + vpFailed + "  FAILED" + System.lineSeparator()
						+ System.lineSeparator() + "*FAILED VPs*" + System.lineSeparator() + failedVpInfo;
				// Create a JSON object for this test script run
				JSONObject executionTestRunJSON = new JSONObject();

				executionTestRunJSON.put("testKey", xrayTestId);
				executionTestRunJSON.put("comment", details);
				executionTestRunJSON.put("status", isFailed ? "FAIL" : "PASS");
				// add dates/timestamps
				executionTestRunJSON.put("start", machineFormat.format(startDate));
				executionTestRunJSON.put("finish", machineFormat.format(endDate));

				// add evidence
				JSONObject resultsFileJSON = new JSONObject();
				File resultsLogFile = new File(resultsLog);
				resultsFileJSON.put("filename", resultsLogFile.getName());
				resultsFileJSON.put("contentType", "text/html");
				/** Create the Evidence section for the Test Run */
				JSONArray evidenceArray = new JSONArray();

				try {
					resultsFileJSON.put("data", encodeFileToBase64Binary(resultsLogFile));
					// Add the results log to the evidence
					// TODO github issue: #190 Add failed log to the evidence
					evidenceArray.put(resultsFileJSON);
				} catch (JSONException jsonException) {
					jsonException.printStackTrace();
				} catch (IOException ioException) {
					ioException.printStackTrace();
				}

				executionTestRunJSON.put("evidences", evidenceArray);

				// Add this Test Run info to the test array for the suite (Test Execution)
				testsArray.put(executionTestRunJSON);
			}
			// Build the "info" section for the Test Execution object
			JSONObject infoJSON = new JSONObject();
			infoJSON.put("summary",
					"AUTOMATED EXECUTION: " + testKey.substring(testKey.lastIndexOf(".") + 1, testKey.length()) + " "
							+ results.getData(ScriptResults.TEST_SCRIPTS_FILE));

			String description = "This test was run with the ATI framework \n\n" + "Full Script Path: " + testKey
					+ "\n\nTest Scripts File: " + results.getData(ScriptResults.TEST_SCRIPTS_FILE);

			infoJSON.put("description", description);
			infoJSON.put("testPlanKey", xrayPlanId);

			// add dates/timestamps
//			CoreDateTimeFormat machineFormat2 = new CoreDateTimeFormat("YYYY-MM-dd'T'HH:mm:ss-0500");
//			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
//			Date gmt = new Date(sdf.format(date));
//			System.out.println("==========================infoJSONStart: " + machineFormat2.format(startDate));
//			System.out.println("==========================infoJSONEnd: " + machineFormat2.format(endDate));
			// TODO commenting these out as they don't always report the correct time Better
			// not to set them, than have it report incorrectly; Currently if it is 1pm or
			// later - the machineFormat defined at the top of this method, will result in
			// these times being 12 hours behind
//			infoJSON.put("startDate", machineFormat.format(startDate));
//			infoJSON.put("finishDate", machineFormat.format(endDate));

			// Include TEST ENVIRONMENTS if set in config file
			String testEnvironment = ConfigProperties.getValue(ConfigProperties.XRAY_TEST_ENVIRONMENTS);
			if (!testEnvironment.isEmpty()) {
				JSONArray envs = new JSONArray();
				String[] envArray = testEnvironment.split(",");
				for (String currEnv : envArray) {
					envs.put(currEnv);
				}
				infoJSON.put("testEnvironments", envs);
			}

			String fixVersion = ConfigProperties.getValue(ConfigProperties.XRAY_FIX_VERSION);
			if (!fixVersion.isEmpty()) {
				infoJSON.put("version", fixVersion);
			}
			// If we have an xrayExecutionId, use it, if not, then a new Test Execution will
			// be created
			if (xrayExecutionId != null && !xrayExecutionId.isEmpty()) {
				executionObject.put("testExecutionKey", xrayExecutionId);
			}
			executionObject.put("info", infoJSON);
			executionObject.put("tests", testsArray);

			// *********Generate Attachment**************
			String resultsFolder = results.getResultsFolder();
			// Add zip of log files as attachment
			String zipFile = resultsFolder + ".zip";

			// If the flag for not sending screenshots to reporting is true then pass param
			// to exclude the Screenshots folder
			boolean zipSuccess;

			if (ConfigProperties.getValue(ConfigProperties.DISABLE_SCREEN_SHOT_REPORTING_ATTACHMENT, "FALSE")
					.equals("TRUE")) {
				zipSuccess = ZipUtil.zipFolder(resultsFolder, zipFile, "ScreenShots");
			} else {
				zipSuccess = ZipUtil.zipFolder(resultsFolder, zipFile);
			}
			if (!zipSuccess) {
				zipFile = ZIP_FILE_FAILURE;
			}

			// *********Generate Issue Comment**************

			// add comment that will contain the details for this test execution
			JSONObject comment = new JSONObject();
			String commentText = description + "\n\n" + "Ran Scripts Marked As: "
					+ results.getData(ScriptResults.TEST_SCRIPTS_TO_RUN) + "\n\n" + details;
			comment.put("body", commentText);

			// Use the TEST SCRIPTS FILE name if we have one so that we can use it to write
			// to the reference file later
			String name = results.getData(ScriptResults.TEST_SCRIPTS_FILE);
			if (name == null || name.isEmpty()) {
				name = "_SCRIPT_" + results.getScriptName();
			} else {
				name = "_SUITE_" + name;
			}
			String postLogId = generateTestDataLog(name);
			System.out.println("Post log ID for reference if Xray posts fail: " + postLogId);

			testExecutionKey = attemptXrayPost(executionObject, zipFile, comment, postLogId);

		}
		return testExecutionKey;
	}

	/**
	 * Checks the status of the HTTP Response object and returns if the status code
	 * is 200/201. True if post succeeded; false if the code isn't 200 or 201.
	 *
	 * @param jsonResultsResponse response from an attempted post
	 * @return boolean true if post succeeded; false if the code isn't 200 or 201.
	 */
	private boolean isPostSuccessful(HttpResponse<JsonNode> jsonResultsResponse) {
		boolean wasPostSuccessful = false;
		if (jsonResultsResponse != null) {
			int postStatusCode = jsonResultsResponse.getStatus();
			if (postStatusCode == HTTP_200 || postStatusCode == HTTP_201) {
				wasPostSuccessful = true;
			}
		}
		return wasPostSuccessful;
	}

	@Override
	protected HttpResponse<JsonNode> sendPostXray(String url, JSONObject jsonToPost) {
		Map<String, Object> fields = jsonToPost.toMap();

		boolean loginSuccessful = loginToJira();

		HttpResponse<JsonNode> jsonResponse = null;
		boolean postFailed = true;
		if (loginSuccessful) {
			// Keeping output until Xray integration is better verified
			System.out.println("POST URL=" + url);
			System.out.println("POST FIELDS=" + fields.toString());
			log.debug("POST FIELDS LENGTH=" + fields.toString().length());

			int numOfAttempts = 0;
			do {
				System.out.println("SEND UPDATE TO JIRA");
				jsonResponse = Unirest.post(url).header("Content-Type", "application/json")
						.header("accept", "application/json").connectTimeout(60000).body(jsonToPost).asJson();

				int status = jsonResponse.getStatus();
				String statusText = jsonResponse.getStatusText();
				System.out.println("STATUS=" + status);
				System.out.println("STATUS TEXT=" + statusText);
				System.out.println("RESPONSE=" + jsonResponse.getBody());

				if (status == HTTP_400 || statusText.equals(JIRA_LOGIN_UNSUCCESSFUL_RESPONSE)) {
					// bad request or not logged in, don't continue to try posting
					break;
				}

				postFailed = !isPostSuccessful(jsonResponse);
				try {
					if (postFailed) {
						Thread.sleep(NUM_OF_MILLISECONDS_TO_WAIT);
						System.out.println("Waiting " + NUM_OF_MILLISECONDS_TO_WAIT
								+ " milliseconds before trying the post again.");
					}

				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				numOfAttempts++;
			} while (postFailed && numOfAttempts < NUMBER_ATTEMPTS);
		}

		// Note: This could return null if login fails
		return jsonResponse;
	}

	/**
	 * Attempts POST to XRay after scripts have completed running. If the retries
	 * fail, a JSON file will be written to a faildXRayPostJsonFiles folder in the
	 * testResults folder. If this folder doesn't exists, then it will be created as
	 * needed. A post log (data file) will be created (using the specified id) to
	 * track any failed posts so that the posts can be retried at a later time.
	 *
	 * @param executionObject JSONObject that has been built with the info from the
	 *                        results
	 * @param zipFile         the location of the zip file to attach to the issue
	 * @param comment         JSONObject built to contain the comment to add to the
	 *                        issue
	 * @param postLogId       unique id to identify the script results that are
	 *                        attempting to be posted
	 * @return String xray test execution id
	 */
	private String attemptXrayPost(JSONObject executionObject, String zipFile, JSONObject comment, String postLogId) {
		createFailedFolder();
		// Create rolling log file to track failed posts
		CsvDataFile postLog = createXRayFailureLog();
		// Xray JSON Import Execution Results
		String url = getExecutionImportUrl();
		String issueKey = "";
		boolean testExecPostFailed;

		try {
			HttpResponse<JsonNode> testExecResponse = null;
			JsonNode returnValue = null;
			testExecResponse = sendPostXray(url, executionObject);
			testExecPostFailed = !isPostSuccessful(testExecResponse);
			int testExecResponseStatus = testExecResponse != null ? testExecResponse.getStatus() : -1;

			// If the login failed, then the post cannot be successful. The check in
			// isPostSuccessful check to see if the status is 200 or 201. 500 is returned in
			// the case of a login failure.

			String pathToXrayJsonFile = "XRay post didn't fail";
			// If the response was not "Bad Request" (400) aka invalid JSON
			if (testExecResponseStatus != HTTP_400) {
				boolean writeTestExJson = true;
				if (testExecPostFailed) {

					// String used to create json output file from json obj in the individual
					// results folder
					pathToXrayJsonFile = BASE_FOLDER_FOR_FAILED_JSON_FILES + "xrayResults_TestExec_"
							+ DateCalculator.getCurrentDate(DateTimeHelper.YEAR4_MONTH2_DAY2_TIMESTAMP) + ".json";
					// Write a JSON file that has the path used in the POST to xray. We write this
					// file when the post fails due to the XRay server being down or a network
					// issue so we can re-try posts later.
					writeTestExJson = writeJsonToFile(pathToXrayJsonFile, executionObject);

					// If the test execution JSON is not successfully saved, do not write to log, we
					// will never be able to upload it, and we don't want to upload result logs or
					// comment if the test execution can't be posted because then the overall status
					// of the test execution will be out of sync with the other data that has been
					// sent to the record.
					if (writeTestExJson) {

						// Add a new row and track failure in log
						postLog.addNewRow(postLogId);
						postLog.writeToDataSheet(postLogId, TEST_EXEC_POST_SUCCESSFUL, "false");
						postLog.writeToDataSheet(postLogId, TEST_EXEC_JSON_LOCATION, pathToXrayJsonFile);

					}

				}

				System.out.println("--- END initial TestExecution post ---");

				// Assume failure until success
				boolean attachmentFailed = true;
				boolean commentPostFailed = true;
				// Until we know something is wrong with the attachment of comment JSON, assume
				// we want to log failed posts
				boolean logAttachmentFailure = true;
				boolean logCommentJSONFailure = true;

				// *********Attempt to post Attachment & Comment **************

				if (writeTestExJson && testExecResponse != null) {
					// Only attempt posts if original test execution post was successful

					returnValue = testExecResponse.getBody();

					// Get the issue Key for the execution run just entered
					if (returnValue != null && returnValue.getObject().has(TEST_EXEC_ISSUE)) {
						// If the issue Key is found, attach the zipped results file & add comment
						JSONObject execIssue = (JSONObject) returnValue.getObject().get(TEST_EXEC_ISSUE);
						issueKey = execIssue != null ? execIssue.getString("key") : "";

						if (!issueKey.isEmpty()) {
							// If the zip file was created, attach it
							if (!zipFile.equals(ZIP_FILE_FAILURE)) {
								HttpResponse<JsonNode> attachmentResponse = attachFileToIssue(issueKey, zipFile);
								attachmentFailed = !isPostSuccessful(attachmentResponse);
								// 404 will only happen at this point if the file is too big, don't bother
								// saving off the location for future upload as it will never work
								logAttachmentFailure = attachmentResponse != null
										? attachmentResponse.getStatus() != 404
										: true;
							}
							// Add the comment
							HttpResponse<JsonNode> issueCommentResponse = sendPostXray(
									getIssueUrl(issueKey, IssueComponent.COMMENT), comment);
							commentPostFailed = !isPostSuccessful(issueCommentResponse);
							// 400 will only happen if the JSON is bad, don't bother
							// saving off the JSON for future upload as it will never work
							logCommentJSONFailure = issueCommentResponse != null
									? issueCommentResponse.getStatus() != HTTP_400
									: true;
						}
					} else {
						System.out.println("Error with Xray reporting, test execution was not entered.");
					}
				}

				// If Attachment and comment posts failed, write to log
				if ((logAttachmentFailure && attachmentFailed || logCommentJSONFailure && commentPostFailed)
						&& !issueKey.isEmpty()) {
					postLog.addNewRow(postLogId);
					postLog.writeToDataSheet(postLogId, TEST_EXEC_POST_SUCCESSFUL, "true");
					postLog.writeToDataSheet(postLogId, ISSUE_ID, issueKey);
				}

				// If the test execution json write fails we don't want to save off anything for
				// future upload because we won't have a TE to log it against
				if (attachmentFailed && logAttachmentFailure && writeTestExJson) {
					postLog.writeToDataSheet(postLogId, ATTACHMENT_POST_SUCCESSFUL, "false");
					postLog.writeToDataSheet(postLogId, ATTACHMENT_FILE_LOCATION, zipFile);
				} else {
					if (postLog.isDataIdPresent(postLogId)) {
						postLog.writeToDataSheet(postLogId, ATTACHMENT_POST_SUCCESSFUL, "true");
					}
				}

				if (commentPostFailed && logCommentJSONFailure && writeTestExJson) {
					// String used to create json output file from json obj in the individual
					// results folder
					pathToXrayJsonFile = BASE_FOLDER_FOR_FAILED_JSON_FILES + "xrayResults_" + "IssueComment" + "_"
							+ DateCalculator.getCurrentDate(DateTimeHelper.YEAR4_MONTH2_DAY2_TIMESTAMP) + ".json";
					boolean writeCommentJson = writeJsonToFile(pathToXrayJsonFile, comment);
					if (writeCommentJson) {
						postLog.writeToDataSheet(postLogId, ISSUE_COMMENT_POST_SUCCESFUL, "false");
						postLog.writeToDataSheet(postLogId, ISSUE_COMMENT_JSON_LOCATION, pathToXrayJsonFile);
					}
				} else {
					if (postLog.isDataIdPresent(postLogId)) {
						postLog.writeToDataSheet(postLogId, ISSUE_COMMENT_POST_SUCCESFUL, "true");
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return issueKey;
	}

	/**
	 * Returns a new generated data id for the XRay log including data timestamps
	 * with script name
	 *
	 * @param name the script name
	 * @return String new generated data id for the XRay log including data
	 *         timestamps with script name
	 */
	private String generateTestDataLog(String name) {
		return DateCalculator.getCurrentDate(DateTimeHelper.YEAR4_MONTH2_DAY2_TIMESTAMP) + "_" + name;
	}

	/**
	 * Checks to see if the XrayLog file exists and created it with just headers if
	 * it is not found
	 *
	 * @return CsvDataFile the XRayLog object
	 */
	private CsvDataFile createXRayFailureLog() {

		String path = BASE_FOLDER_FOR_FAILED_JSON_FILES + "XrayLog.csv";
		File tempFile = new File(path);
		boolean exists = tempFile.exists();
		FileWriter writer = null;
		if (!exists) {
			try {
				writer = new FileWriter(path);
				writer.write(XRAY_LOG_HEADERS);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
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
		return new CsvDataFile(path);

	}

	/**
	 * Will attach the provided file to the issue.
	 *
	 * @param issueKey identifies the issue this file will be attached to
	 * @param zipFile  file to upload to the issue
	 * @return HttpResponse<JsonNode> Json Response to attachment post
	 */
	private HttpResponse<JsonNode> attachFileToIssue(String issueKey, String zipFile) {
		System.out.println("attemptAttachFileToIssue");
		String fileUploadUrl = getIssueUrl(issueKey, IssueComponent.ATTACHMENT);
		System.out.println("FILE ATTACH URL=" + fileUploadUrl);
		boolean postSuccessful = false;
		boolean loginSuccessful = loginToJira();
		HttpResponse<JsonNode> attachmentResponse = null;

		if (loginSuccessful) {
			int numOfTries = 0;
			do {
				File attachment = new File(zipFile);
				attachmentResponse = Unirest.post(fileUploadUrl).header("X-Atlassian-Token", "no-check")
						.field("file", attachment).connectTimeout(60000).asJson();

				int status = attachmentResponse.getStatus();
				System.out.println("FILE ATTACH STATUS=" + status);
				System.out.println("FILE ATTACH STATUS TEXT=" + attachmentResponse.getStatusText());
				System.out.println("FILE ATTACH RESPONSE=" + attachmentResponse.getBody());
				postSuccessful = isPostSuccessful(attachmentResponse);

				if (postSuccessful) {
					attachment.delete();
				}
				if (status == 403 || status == 404) {
					// permission issue, issue does not exist or file too large; don't continue
					// attempts
					break;
				}

				try {
					if (!postSuccessful) {
						Thread.sleep(NUM_OF_MILLISECONDS_TO_WAIT);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				numOfTries++;

			} while (!postSuccessful && numOfTries < NUMBER_ATTEMPTS);
		}
		// Will return null if login fails
		return attachmentResponse;
	}

	@Override
	protected String getExecutionImportUrl() {
		return getServerUrl() + "rest/raven/2.0/import/execution";
	}

	/**
	 * Returns the URL for an issue post
	 *
	 * @return String the URL for an issue post
	 */
	private String getIssueUrl() {
		return getServerUrl() + "rest/api/2/issue/";
	}

	/** Enum for issue url */
	public enum IssueComponent {
		/** attachments */
		ATTACHMENT("attachments"),
		/** comment */
		COMMENT("comment"),
		/** empty */
		NONE("");

		/** issue component */
		private String component;

		/**
		 * constructor takes in component value
		 *
		 * @param component String component for the issue
		 */
		private IssueComponent(String component) {
			this.component = component;
		}

		@Override
		public String toString() {
			return component;
		}
	}

	/** Enum for project url */
	public enum ProjectComponent {
		/** versions */
		VERSION("versions"),
		/** empty */
		NONE("");

		/** project component */
		private String component;

		/**
		 * constructor takes in component value
		 *
		 * @param component String component for the issue
		 */
		private ProjectComponent(String component) {
			this.component = component;
		}

		@Override
		public String toString() {
			return component;
		}
	}

	/**
	 * Returns the constructed URL for Xray to post to an Issue component. This
	 * includes the issue number passed in in the url.
	 *
	 * @param issueKey       the issue number to post to
	 * @param issueComponent add on to issue url for the component
	 * @return String the constructed URL to post to the specified issue in XRay
	 **/
	private String getIssueUrl(String issueKey, IssueComponent issueComponent) {
		return getIssueUrl() + issueKey + "/" + issueComponent;
	}

	/**
	 * Returns the constructed URL for Xray project components. This includes the
	 * project key specified in the url.
	 *
	 * @param projectKey       the Jira project key
	 * @param projectComponent add on to project url
	 * @return String the constructed URL to post to the specified issue in XRay
	 **/
	private String getProjectUrl(String projectKey, ProjectComponent projectComponent) {
		return getServerUrl() + "rest/api/2/project/" + projectKey + "/" + projectComponent;
	}

	/**
	 * Issues a get to Xray with the specified url
	 *
	 * @param url for get command
	 * @return HttpResponse<JsonNode> response from get command
	 */
	private HttpResponse<JsonNode> sendGetXray(String url) {
		HttpResponse<JsonNode> jsonResponse = null;
		boolean loginSuccessful = loginToJira();
		log.debug("Get url: " + url);
		if (loginSuccessful) {
			jsonResponse = Unirest.get(url).header("Content-Type", "application/json")
					.header("accept", "application/json").asJson();
		}
		return jsonResponse;
	}

	/** Enum for issue type */
	public enum IssueType {
		/** Test Plan */
		TEST_PLAN("Test Plan"),
		/** Test */
		TEST("Test"),
		/** Test Execution */
		TEST_EXECUTION("Test Execution");

		/** issue type */
		private String issueType;

		/**
		 * constructor takes in issueType value
		 *
		 * @param issueType String component for the issue
		 */
		private IssueType(String issueType) {
			this.issueType = issueType;
		}

		@Override
		public String toString() {
			return issueType;
		}
	}

	/**
	 * Validates the Jira/Xray Issue specified is the type of issue expected.
	 *
	 * @param issueKey          Jira issue key for the issue to validate the type
	 * @param expectedIssueType issue type expected for the specified issue
	 * @return boolean true if the issue is of the expected type; false if it is not
	 */
	private boolean validateIssueType(String issueKey, IssueType expectedIssueType) {
		String url = getIssueUrl(issueKey, IssueComponent.NONE);

		log.debug("Get url: " + url);

		HttpResponse<JsonNode> jsonResponse = Unirest.get(url).header("Content-Type", "application/json")
				.header("accept", "application/json").queryString("fields", "issuetype").asJson();

		log.debug("Test Plan get Status: " + jsonResponse.getStatus());

		if (jsonResponse.getStatus() != 200) {
			throw new InvalidParameterException("Please specify a valid Jira/Xray Issue for " + expectedIssueType + ". "
					+ issueKey + " was not found.");
		}

		JSONObject obj = jsonResponse.getBody().getArray().getJSONObject(0);
		log.debug("Test Plan get Names: " + obj.names());
		log.debug("Test Plan get Fields : " + obj.get("fields"));

		JSONObject fields = (JSONObject) obj.get("fields");
		log.debug("Test Plan Field Names: " + fields.names());

		boolean issueTypeMatches = false;
		if (fields.has("issuetype")) {
			JSONObject issuetype = (JSONObject) fields.get("issuetype");
			log.debug("Issue type names: " + issuetype.names());
			String issueTypeName = issuetype.get("name").toString();
			log.debug("Issue type Name: " + issueTypeName);
			if (issueTypeName.equalsIgnoreCase(expectedIssueType.toString())) {
				issueTypeMatches = true;
			}
		}

		return issueTypeMatches;
	}

	/**
	 * Validates that the Test Plan specified in the Config file is a valid Test
	 * Plan in Xray
	 */
	public void validateTestPlan() {
		String xrayTestPlanId = ConfigProperties.getValue(ConfigProperties.XRAY_PLAN_ID);

		if (xrayTestPlanId != null && !xrayTestPlanId.isEmpty() && loginToJira()
				&& !validateIssueType(xrayTestPlanId, IssueType.TEST_PLAN)) {
			// Don't attempt to report this to Xray it will be badly formed JSON and fail
			setIsXrayOn(false);
			throw new InvalidParameterException(
					"Please specify a valid XRAY_PLAN_ID in the config file. Note this must be a issue of type \"Test Plan\"");
		}
	}

	/**
	 * Validates that the Tests specified are valid Tests in Xray. Can pass in a
	 * single issue id, or multiple
	 *
	 * @param xrayTestIds Xray Issue Id(s) for a Test artifacts to validate
	 */
	public void validateTests(String... xrayTestIds) {
		if (xrayTestIds.length > 0 && loginToJira()) {
			for (String id : xrayTestIds) {
				if (id != null && !id.isEmpty() && !validateIssueType(id, IssueType.TEST)) {
					// Don't attempt to report this to Xray it will be badly formed JSON and fail
					setIsXrayOn(false);
					throw new InvalidParameterException(id
							+ " is not a valide XRAY Test ID.  Please specify a valid XRAY_TEST_ID for your script. Note this must be a issue of type \"Test\"");
				}
			}
		}
	}

	/**
	 * Validates that the Test Execution specified is a valid Test Execution in Xray
	 *
	 * @param xrayTestExecutionId Xray Issue Id for a Test artifact
	 */
	public void validateTestExecution(String xrayTestExecutionId) {

		if (xrayTestExecutionId != null && !xrayTestExecutionId.isEmpty() && loginToJira()
				&& !validateIssueType(xrayTestExecutionId, IssueType.TEST_EXECUTION)) {
			// Don't attempt to report this to Xray it will be badly formed JSON and fail
			setIsXrayOn(false);
			throw new InvalidParameterException(xrayTestExecutionId
					+ " is not a valid Xray Test Execution ID. Please specify a valid XRAY_TEST_EXECUTION_ID. Note this must be a issue of type \"Test Execution\"");
		}
	}

	/**
	 * Validates that the version specified in the Config file exists in the Jira
	 * project specified in the Config file
	 *
	 */
	public void validateVersion() {

		String version = ConfigProperties.getValue(ConfigProperties.XRAY_FIX_VERSION);
		if (version != null && !version.isEmpty()) {
			String projectKey = ConfigProperties.getValue(ConfigProperties.JIRA_PROJECT_KEY);
			if (projectKey.isEmpty()) {
				// project key is needed to validate the version
				throw new InvalidParameterException("Please specify a valid JIRA_PROJECT_KEY in the config file.");
			}

			HttpResponse<JsonNode> versionResponse = sendGetXray(getProjectUrl(projectKey, ProjectComponent.VERSION));

			if (versionResponse != null) {
				log.debug("Version get Status: " + versionResponse.getStatus());

				JSONArray versions = versionResponse.getBody().getArray();
				int numVersions = versions.length();
				boolean versionFound = false;
				for (int i = 0; i < numVersions; i++) {
					log.debug("Version JSONObject " + i + ": " + versions.getJSONObject(i));
					log.debug("Version names: " + i + ": " + versions.getJSONObject(i).names());
					if (versions.getJSONObject(i).has("name")) {
						String currentVersion = versions.getJSONObject(i).get("name").toString();
						if (currentVersion.equals(version)) {
							versionFound = true;
							break;
						}
					}
				}

				if (!versionFound) {
					// Don't attempt to report this to Xray it will be badly formed JSON and fail
					setIsXrayOn(false);
					throw new InvalidParameterException("Version: " + version + " was not found in the " + projectKey
							+ " project. Please specify a valid XRAY_FIX_VERSION in the config file.");
				}
			}
		}

	}

	/**
	 * Returns a JSONObject the is built from the the json file from the path passed
	 * in
	 *
	 * @param pathToJsonFile complete path to the json file including the file name
	 * @return JSONObject a JSON object that is built from the the json file from
	 *         the path passed in
	 */
	private JSONObject parseJsonFromFile(String pathToJsonFile) {
		String json;
		try {
			json = new String(Files.readAllBytes(Paths.get(pathToJsonFile)));
		} catch (Exception e) {
			e.printStackTrace();
			throw new InvalidDataException(
					"JSON file could not be found, please pass in a valid path including the file and extension.");
		}

		return new JSONObject(json);
	}

	/**
	 * Writes file to the specified location that contains all the info to build a
	 * JSON object
	 *
	 * @param pathToFileToWrite the full file path including he file name
	 * @param jsonObj           the JSONObject to write the info from
	 * @return boolean true if the write was successful, false otherwise
	 */
	private boolean writeJsonToFile(String pathToFileToWrite, JSONObject jsonObj) {
		boolean noExceptions = true;

		// Write a JSON file that has the info used in the POST to xray. We write this
		// file when the post fails due to the XRay server being down or a network
		// issue we can post manually later.
		System.out.println("=================================================================");
		System.out.println("=================================================================");
		System.out.println("The post to XRay has failed and the JSON file will be written to " + pathToFileToWrite
				+ "  for this execution.");
		System.out.println("=================================================================");
		System.out.println("=================================================================");

		try {
			file = new FileWriter(pathToFileToWrite);
			file.write(jsonObj.toString());
		} catch (IOException e) {
			noExceptions = false;
			e.printStackTrace();
		} finally {
			try {
				file.flush();
				file.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return noExceptions;
	}

	/**
	 * Will return the encrypted password for certificate authentication
	 *
	 * @return String encryptedPassword
	 */
	private static String getEncryptedCertificatePassword() {
		if (ENCRYPTED_CERTIFICATE_PW == null) {
			File file = new File(getAuthenticationFile());
			try {
				ENCRYPTED_CERTIFICATE_PW = Files.readString(file.toPath());
			} catch (IOException ioException) {
				ioException.printStackTrace();
				throw new WrappedException(ioException);
			}
		}
		return ENCRYPTED_CERTIFICATE_PW;
	}

	/**
	 * Will return the username for authentication
	 *
	 * @return String username
	 */
	private static String getUserName() {
		if (username == null) {
			parseUserAuthFile();
		}
		return username;
	}

	/**
	 * Will return the token for authentication
	 *
	 * @return String token
	 */
	private static String getToken() {
		if (token == null) {
			parseUserAuthFile();
		}
		return token;
	}

	/**
	 * Will parse the user name and token from the authentication file
	 */
	private static void parseUserAuthFile() {
		File file = new File(getAuthenticationFile());
		try {
			List<String> auth = Files.readAllLines(file.toPath());

			for (int i = 0; i < auth.size(); i++) {
				String data = auth.get(i);
				if (!data.startsWith("#")) {
					if (data.startsWith("pwd=")) {
						token = data.replace("pwd=", "");
					} else if (data.startsWith("user=")) {
						username = data.replace("user=", "");
					}

				}
			}

			if (token.isEmpty() || token == null || username.isEmpty() || username == null) {
				throw new InvalidDataException(
						"Please ensure you have properly set-up your Xray authentication files. If you do not have Xray credentials configured and would like to run your scripts, please set XRAY_REPORTING=FALSE in your config file until you have Xray credentials configured properly.");
			}

		} catch (IOException e) {
			e.printStackTrace();
			throw new WrappedException(e);
		}
	}

	/**
	 * Will return the decrypted password for the JKS Certificate
	 *
	 * @return String decrypted password
	 */
	private String getDecryptedCertificatePassword() {
		return CryptoUtils.decrypt(getEncryptedCertificatePassword(), new File(AUTH_KEY_PATH));
	}

	/**
	 * Will return a KeyStore object for the JKS Certificate
	 *
	 * @return KeyStore for JKS Certificate
	 */
	private KeyStore getKeyStore() {

		KeyStore keyStore = null;
		// By placing the InputStream assignment in the try block, since it implements
		// Closeable, no need for a finally block, close will be called
		try (InputStream keyStoreStream = new FileInputStream(JKS_CERTIFICATE);) {
			keyStore = KeyStore.getInstance("PKCS12");
			keyStore.load(keyStoreStream, getDecryptedCertificatePassword().toCharArray());

		} catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e) {
			throw new RuntimeException("Exception trying to load KeyStore: " + JKS_CERTIFICATE, e);
		}

		return keyStore;
	}

	/**
	 * This will create an authenticated session with Unirest using either
	 * Certificate or Basic authentication, dependent on the XRAY_AUTHENTICATION
	 * value for Authentication Type
	 *
	 * @return boolean true if login was successful; false if login failed
	 *
	 * @throws InvalidDataException AUTHENTICATION_TYPE specified was not an
	 *                              expected value. Should be set to CERTIFICATE or
	 *                              BASIC in project.properties
	 */
	private boolean loginToJira() {
		boolean loginSuccessful = true;
		int attempts = 0;
		do {
			if (AUTHENTICATION_TYPE.equalsIgnoreCase("CERTIFICATE")) {
				// Configure Unirest to use the KeyStore for the JKS Certificate,
				// passing in a decrypted password for the keystore/certificate
				Unirest.config().clientCertificateStore(getKeyStore(), getDecryptedCertificatePassword());

				// Need to make a call to the application for authentication. If there is a
				// configured value for
				// XRAY_LOGIN_URL, it will be prepended to the call. This would usually only be
				// required for Certificate authentication
				try {
					Unirest.get(ConfigProperties.getValue(ConfigProperties.XRAY_LOGIN_URL, "") + getServerUrl()
							+ "rest/api/2").asEmpty();
					loginSuccessful = true;
				} catch (Exception e) {
					loginSuccessful = false;
					e.printStackTrace();
				}

			} else if (AUTHENTICATION_TYPE.equalsIgnoreCase("BASIC")) {
				// Make the basic call to login, passing in the userName and token for basic
				// authentication
				// If there is a configured value for XRAY_LOGIN_URL, it will be prepended to
				// the call. This would usually only be required for Certificate authentication
				try {
					// TODO if we use this, we may need to check if we are logged in first
					Unirest.get(ConfigProperties.getValue(ConfigProperties.XRAY_LOGIN_URL, "") + getServerUrl()
							+ "rest/api/2").basicAuth(getUserName(), getDecryptedUserToken()).asEmpty();
					loginSuccessful = true;
				} catch (Exception e) {
					loginSuccessful = false;
					e.printStackTrace();
				}
			} else {
				// AUTHENTICATION_TYPE specified was not an expected value, so throw an
				// Exception
				throw new InvalidDataException(
						"Invalid value for XRAY_AUTHENTICATION. Must be \"CERTIFICATE\" or \"BASIC\" but has a value of "
								+ AUTHENTICATION_TYPE
								+ ". Please check/update value for XRAY_AUTHENTICATION in project.properties");
			}
			attempts++;
			// exclude 1st and last attempt for output
			if (attempts != 1 && attempts != NUMBER_ATTEMPTS) {
				System.out.println("Login to Jira failed, will retry.");
			}
		} while (!loginSuccessful && attempts <= NUMBER_ATTEMPTS);

		if (!loginSuccessful) {
			System.out.println("!!!!!!!!!!! Login to Jira FAILED Final Attempt !!!!!!!!!!!!!!!!!!!!");
		}
		return loginSuccessful;
	}

	/**
	 * Returns the decrypted value of the retrieved user token
	 *
	 * @return String the decrypted value of the retrieved user token
	 */
	private String getDecryptedUserToken() {
		return CryptoUtils.decrypt(getToken(), new File(AUTH_KEY_PATH));
	}

	/**
	 * Loops through the Xray log and attempts to re-post all items that had been
	 * logged as failed posts. If items post successfully, deletes the associated
	 * files and removes that failure from the log.
	 *
	 * @param log xray post failure log
	 * @return boolean returns true if all posts are successful; returns false if
	 *         there are any remaining log entries
	 */
	public boolean retryFailedXrayPosts(CsvDataFile log) {
		boolean allPostsSuccessful = false;
		List<String> listOfIds = log.getDataIds();

		for (String id : listOfIds) {
			/**
			 * Test Execution Post
			 */
			if (log.getData(id, TEST_EXEC_POST_SUCCESSFUL).equalsIgnoreCase("false")) {
				System.out.println("Posting Test Execution...");
				String filePath = log.getData(id, TEST_EXEC_JSON_LOCATION);
				System.out.println(TEST_EXEC_JSON_LOCATION + ": " + filePath);
				JSONObject jsonObj = parseJsonFromFile(filePath);
				HttpResponse<JsonNode> jsonResultResponse = sendPostXray(getExecutionImportUrl(), jsonObj);

				if (isPostSuccessful(jsonResultResponse)) {
					String issueId = ((JSONObject) jsonResultResponse.getBody().getObject().get(TEST_EXEC_ISSUE))
							.getString("key");

					log.writeToDataSheet(id, TEST_EXEC_POST_SUCCESSFUL, "true");
					log.writeToDataSheet(id, ISSUE_ID, issueId);
					deleteFile(filePath);

					// We need to update the Xray Reference file so that re-runs will post against
					// this test execution id
					if (id.contains("_SUITE_")) {
						String fileName = id.split("_SUITE_")[1];
						String xrayRefFileLocation = generateXrayReferenceFilePath(fileName);
						CsvDataFile xrayRefFile = new CsvDataFile(xrayRefFileLocation);
						xrayRefFile.writeToDataSheet(fileName, XRAY_REF_EXECUTION_ID_COLUMN, issueId);
					}
				}

			}
			String issueKey = log.getData(id, ISSUE_ID);
			if (!issueKey.isEmpty()) {
				/**
				 * Issue Comment Post
				 */
				if (log.getData(id, ISSUE_COMMENT_POST_SUCCESFUL).equalsIgnoreCase("false")) {
					System.out.println("Posting Issue Comment...");
					String filePath = log.getData(id, ISSUE_COMMENT_JSON_LOCATION);

					JSONObject obj = parseJsonFromFile(filePath);
					HttpResponse<JsonNode> response = sendPostXray(getIssueUrl(issueKey, IssueComponent.COMMENT), obj);
					if (isPostSuccessful(response)) {
						log.writeToDataSheet(id, ISSUE_COMMENT_POST_SUCCESFUL, "true");
						deleteFile(filePath);
					}

				}

				/**
				 * Attachment Post
				 */
				if (log.getData(id, ATTACHMENT_POST_SUCCESSFUL).equalsIgnoreCase("false")) {
					System.out.println("Posting Attachment...");
					String filePath = log.getData(id, ATTACHMENT_FILE_LOCATION);
					if (filePath.equals(ZIP_FILE_FAILURE)) {
						System.out.println("!!!!! NOTICE: The zip action failed for " + id
								+ ". Please go retrieve the result log manually. If it is critical for this file to be uploaded to Xray, manually zip it and attach it to issue: "
								+ issueKey);
						// Set success column to true so that this does not prevent the row of data from
						// being deleted in the log
						log.writeToDataSheet(id, ATTACHMENT_POST_SUCCESSFUL, "true");
					} else {
						System.out.println(ATTACHMENT_FILE_LOCATION + ": " + filePath);
						boolean attachSuccess = isPostSuccessful(attachFileToIssue(issueKey, filePath));
						if (attachSuccess) {
							log.writeToDataSheet(id, ATTACHMENT_POST_SUCCESSFUL, "true");
							deleteFile(filePath);
						}
					}
				}
			}

			boolean testExec = Boolean.valueOf(log.getData(id, TEST_EXEC_POST_SUCCESSFUL));
			boolean attachment = Boolean.valueOf(log.getData(id, ATTACHMENT_POST_SUCCESSFUL));
			boolean issue = Boolean.valueOf(log.getData(id, ISSUE_COMMENT_POST_SUCCESFUL));

			if (testExec && attachment && issue) {
				log.deleteRowFromDataSheet(id);
			}
		}

		int numRemaining = log.getDataIds().size();
		if (numRemaining > 0) {
			System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			System.out.println("Some posts to xray were not successful. There are " + numRemaining
					+ " test executions that are missing data in Xray.  Please attempt to run the XrayPostHelper again.  If there are notes in the console that login is failing, please try again later.");
			System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

		} else {
			allPostsSuccessful = true;
			System.out.println("**** All posts to xray were successful *****");
		}

		return allPostsSuccessful;
	}

	/**
	 * Attempts to delete the file with the path passed in
	 *
	 * @param filePath the path of the file to delete
	 *
	 */
	private void deleteFile(String filePath) {
		File file = new File(filePath);
		boolean deletedSuccessful = file.delete();
		if (!deletedSuccessful) {
			System.out
					.println("The file was not not deleted.  Please manually delete the following file:  " + filePath);
		}
	}

	/**
	 * Returns the location for the Xray Reference File based on the specified name
	 * of the Test Suite data set. Reference files will be stored in a new folder
	 * directly off of dataSets
	 *
	 * @param testSuiteName name of the test suite data set
	 * @return String location for the xray reference file for the specified
	 *         testSuiteName
	 */
	protected String generateXrayReferenceFilePath(String testSuiteName) {
		File file = new File("file");
		String path = file.getAbsolutePath();
		path = path.substring(0, path.lastIndexOf("\\") + 1) + "dataSets\\xrayReferenceFiles\\"
				+ testSuiteName.replace(".csv", "_ReferenceFile.csv");
		return path;
	}

	/**
	 * Returns the CsvDataFile for the XrayReferenceFile if it exists, otherwise
	 * returns null.
	 *
	 * @param results ScriptResults for the script
	 * @return CsvDataFile xray reference file
	 */
	public CsvDataFile getXrayReferenceFile(ScriptResults results) {
		CsvDataFile refDataFile = null;
		String xrayRefFileLocation = generateXrayReferenceFilePath(results.getData(ScriptResults.TEST_SCRIPTS_FILE));
		File referenceFile = new File(xrayRefFileLocation);

		if (referenceFile.exists()) {
			refDataFile = DataFileHelper.getDataFile(xrayRefFileLocation);
		}

		return refDataFile;
	}

	/**
	 * Creates a reference file that contains the XRAY Plan Id and the Xray Test
	 * Execution Id for the set of tests that were run.
	 *
	 * @param results ScriptResults for this script instance
	 */
	public void createXrayReferenceFile(ScriptResults results) {
		String delimiter = "~";

		String testExecutionId = results.getData(ScriptResults.XRAY_EXECUTION_ID);
		String testPlanId = ConfigProperties.getValue(ConfigProperties.XRAY_PLAN_ID, "");
		String xrayFixVersion = ConfigProperties.getValue(ConfigProperties.XRAY_FIX_VERSION, "");

		String testSuiteName = results.getData(ScriptResults.TEST_SCRIPTS_FILE);
		String fileName = generateXrayReferenceFilePath(testSuiteName);

		FileWriter writer = null;
		File referenceFile = new File(fileName);

		try {
			// Delete any previous reference files
			if (referenceFile.exists()) {
				referenceFile.delete();
			}

			File filePath = new File(fileName.substring(0, fileName.lastIndexOf("\\")));
			filePath.mkdirs();
			referenceFile.createNewFile();

			// create a FileWriter to output the data to
			writer = new FileWriter(referenceFile);

			String dataIdHeader = ConfigProperties.getValue(ConfigProperties.DATA_ID_COLUMN, "DATA_ID");
			// Create the header
			writer.write(dataIdHeader + delimiter + XRAY_REF_PLAN_ID_COLUMN + delimiter + XRAY_REF_EXECUTION_ID_COLUMN
					+ delimiter + XRAY_REF_FIX_VERSION_COLUMN + System.lineSeparator());
			// Add the row of data
			writer.write(testSuiteName + delimiter + testPlanId + delimiter + testExecutionId + delimiter
					+ xrayFixVersion + System.lineSeparator());
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

	/**
	 * Creates a file with the encrypted password for a Service Account.
	 *
	 * @param jiraUserName   the user name for the jira account
	 * @param clearUserToken the API user auth token
	 * @param userPwdFile    path to the file that will be generated with
	 *                       information for authenticating into Jira
	 *
	 * @throws IOException if auth file is not found
	 */
	public void createUserAuthFiles(String jiraUserName, String clearUserToken, String userPwdFile) throws IOException {
		if (jiraUserName.isEmpty() || clearUserToken.isEmpty() || userPwdFile.isEmpty()) {
			throw new InvalidParameterException("The parameters cannot be empty, please fix data and retry.");
		}
		if (jiraUserName == null || clearUserToken == null || userPwdFile == null) {
			throw new InvalidParameterException("The parameters cannot be null, please fix data and retry.");
		}

		Properties p1 = new Properties();

		p1.put("user", jiraUserName);
		String encryptedPwd = CryptoUtils.encrypt(clearUserToken, new File(AUTH_KEY_PATH));
		p1.put("pwd", encryptedPwd);
		p1.store(new FileWriter(userPwdFile), "");

		// ==================
		Properties p2 = new Properties();

		p2.load(new FileReader(userPwdFile));
		encryptedPwd = p2.getProperty("pwd");
		System.out.println(encryptedPwd);
		System.out.println(CryptoUtils.decrypt(encryptedPwd, new File(AUTH_KEY_PATH)));
	}

	/**
	 * Creates a file with the user name and encrypted password for an individual
	 * user auth token.
	 *
	 * @param clearServiceAcctPwd password for the Jira service account
	 * @param serviceAcctPwdFile  path to the file that will be generated with
	 *                            information for authenticating into Jira
	 *
	 * @throws IOException if auth file is not found
	 *
	 */
	public void createServiceAccountAuthFile(String clearServiceAcctPwd, String serviceAcctPwdFile) throws IOException {
		String encryptedPwd = CryptoUtils.encrypt(clearServiceAcctPwd, new File(AUTH_KEY_PATH));

		Files.deleteIfExists(new File(serviceAcctPwdFile).toPath());

		BufferedWriter writer = new BufferedWriter(new FileWriter(serviceAcctPwdFile, true));
		writer.append(encryptedPwd);

		writer.close();
		System.out.println("File Created: " + encryptedPwd);
	}

	/**
	 * Creates the folder structure needed for the encryption. Path created -
	 * c:/Automation/Tools/properties
	 *
	 */
	public void createAutomationToolsPropertiesFolder() {
		File directory = new File("c:/Automation");
		if (!directory.exists()) {
			directory.mkdir();
		}
		directory = new File("c:/Automation/Tools");
		if (!directory.exists()) {
			directory.mkdir();
		}
		directory = new File("c:/Automation/Tools/properties");

		if (!directory.exists()) {
			directory.mkdir();
			try {
				// Set attribute to hidden for the properties folder
				Runtime.getRuntime().exec("attrib +H c:/Automation/Tools/properties");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
