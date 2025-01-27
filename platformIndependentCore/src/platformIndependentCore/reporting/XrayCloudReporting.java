package platformIndependentCore.reporting;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONException;
import kong.unirest.json.JSONObject;
import platformIndependentCore.events.Event;
import platformIndependentCore.events.Note;
import platformIndependentCore.events.VerificationPoint;
import platformIndependentCore.exceptions.ReportingException;
import platformIndependentCore.scripts.ScriptResults;
import platformIndependentCore.utilities.ConfigProperties;
import platformIndependentCore.utilities.CoreDateTimeFormat;

/**
 * <b>Name :</b> XrayCloudReporting.java
 * <p>
 * <b>Generated :</b> Jun 26, 2020
 * <p>
 * <b>Description :</b>Will post Test Execution results to Cloud Version of
 * XRAY. <b>NOTE: As the cloud version has not been available/used since a proof
 * of concept, this class has not been kept up.</b>
 * <p>
 *
 * @since Jun 26, 2020
 * @author VBAAUSTAYLOL
 */
public class XrayCloudReporting {
	/** XRAY CLOUD URL, default to the current cloud URL */
	private static String XRAY_CLOUD_URL = ConfigProperties.getValue(ConfigProperties.XRAY_CLOUD_URL,
			"https://xray.cloud.xpand-it.com");
	/** XRAY Authentication file */
	private static String XRAY_AUTH_FILE = ConfigProperties.getValue(ConfigProperties.XRAY_AUTH_FILE);
	/** TRUE if XRAY reporting is turned on */
	private static boolean IS_XRAY_ON = ConfigProperties.getValue(ConfigProperties.XRAY_REPORTING, "FALSE")
			.equalsIgnoreCase("TRUE");

	/**
	 * Will take the list of event and status and record them in XRAY as a Test
	 * Execution
	 *
	 * @param testKey    Test Case key
	 * @param results    ScriptResults with information on test run
	 * @param resultsLog location of the results log for this test run
	 */
	public static void reportResults(String testKey, ScriptResults results, String resultsLog) {
		List<Event> events = results.getEvents();
		String startTime = results.getData(ScriptResults.START_TIME);
		String endTime = results.getData(ScriptResults.STOP_TIME);
		String xrayTestId = results.getData(ScriptResults.XRAY_TEST_ID);
		String xrayExecutionId = results.getData(ScriptResults.XRAY_EXECUTION_ID);
		boolean isSuite = results.isMainSuiteTest();
		Date startDate = Event.DATE_TIME_FORMAT.getDate(startTime);
		Date endDate = Event.DATE_TIME_FORMAT.getDate(endTime);
		// Get the format needed for sending dates to Xray via JSON data
		CoreDateTimeFormat machineFormat = new CoreDateTimeFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

		if (IS_XRAY_ON) {
			// Make sure Authentication File is set
			if (XRAY_AUTH_FILE.isEmpty()) {
				throw new ReportingException(
						"XRAY Reporting is turned ON, but you are missing the XRAY_AUTH_FILE is undefined in your config.properties");
			}
			// If this is not a Suite, need an XRAY ID to associate with the Test Run.
			if (!isSuite && (xrayTestId == null || xrayTestId.isEmpty())) {
				throw new ReportingException(
						"XRAY Reporting is turned ON, but you are missing the XRAY ID for this Test Run");
			}

			/** Create the Evidence section for the Test Run */
			JSONArray evidenceArray = new JSONArray();

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
							+ System.lineSeparator() + System.lineSeparator() + runComment;// +

					JSONObject testRunJSON = new JSONObject();
					// Build the JSON data for the Test Run details
					testRunJSON.put("testKey", dataId);
					testRunJSON.put("comment", testRunDetails);
					testRunJSON.put("status", isFailed ? "FAILED" : "PASSED");
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

				evidenceArray = executionScriptResults.getRunEvidenceArray();
				vpCount += executionScriptResults.getVpCountRun();
				vpFailed += executionScriptResults.getVpFailedRun();
				vpPass += executionScriptResults.getVpPassRun();

				exceptionCnt += executionScriptResults.getExceptionCntRun();
				if (executionScriptResults.isFailed) {
					isFailed = true;
				}

				// runFailedVpInfo += executionScriptResults.getRunFailedVpInfo();
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
				// TODO - get current test id instead of hardcoding :D
				executionTestRunJSON.put("testKey", xrayTestId);
				executionTestRunJSON.put("comment", details);
				executionTestRunJSON.put("status", isFailed ? "FAILED" : "PASSED");
				executionTestRunJSON.put("start", machineFormat.format(startDate));
				executionTestRunJSON.put("finish", machineFormat.format(endDate));

				JSONObject resultsFileJSON = new JSONObject();
				File resultsLogFile = new File(resultsLog);
				resultsFileJSON.put("filename", resultsLogFile.getName());
				resultsFileJSON.put("contentType", "text/html");
				try {
					resultsFileJSON.put("data", encodeFileToBase64Binary(resultsLogFile));

					// Add the results log to the evidence
					evidenceArray.put(resultsFileJSON);
				} catch (JSONException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
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
			infoJSON.put("description", details);
			// info.put("summary", summary);
			infoJSON.put("startDate", machineFormat.format(startDate));
			infoJSON.put("finishDate", machineFormat.format(endDate));
			// info.put("evidences", evidenceArray);

			// If we have an xrayExecutionId, use it, if not, then a new Test Execution will
			// be
			// created
			if (xrayExecutionId != null && !xrayExecutionId.isEmpty()) {
				executionObject.put("testExecutionKey", xrayExecutionId);
			}
			executionObject.put("info", infoJSON);
			executionObject.put("tests", testsArray);
			// Add log as attachment
			// executionObject.put("evidences", evidenceArray);

			// Xray Cloud JSON Import Execution Results
			String url = XRAY_CLOUD_URL + "/api/v1/import/execution";
			System.out.println("SEND POST URI: " + url);

			try {
				JsonNode returnValue = sendPostXray(url, executionObject);
				System.out.println(returnValue.toPrettyString());
				System.out.println("--- END XrayReporting ---");
				// So far attempts to add an attachment or evidence to the TestExecution have
				// been unsuccessful.
				// // Add log as attachment
				// executionObject.put("evidences", evidenceArray);
				//
				// addAttachment(xrayId, resultsLog);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * Will use the XRAY authentication file to get a TOKEN to be used for
	 * subsequent requests
	 *
	 * @return String Bearer token
	 * @throws IOException if something goes wrong
	 */
	private static String getAuthenticationToken() throws IOException {
		String uri = XRAY_CLOUD_URL + "/api/v1/authenticate";
		String request = "curl -H \"Content-Type: application/json\" -X POST --data @\"" + XRAY_AUTH_FILE + "\" " + uri;

		String response = sendCurl(request);
		// Strip out the beginning and end double quotes from the response. The response
		// should not include double quotes, so just stripping them all out
		response = response.replace("\"", "");
		return response;
	}

	/**
	 * Uses cUrl to send a request to the Xray server
	 *
	 * @param request to send
	 * @return String response text
	 * @throws IOException if something goes wrong
	 */
	private static String sendCurl(String request) throws IOException {
		System.out.println("REQUEST: " + request);
		Process process = Runtime.getRuntime().exec(request);
		InputStream inputStream = process.getInputStream();
		String text = IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());

		int exitCode = process.exitValue();
		System.out.println(exitCode);
		System.out.println(text);
		return text;
	}

	/**
	 * Will send POST to the XRAY Could server and return the response
	 *
	 * @param url http request to post
	 * @param obj the JSON data to send
	 * @return JsonNode with response
	 * @throws InterruptedException if something goes wrong
	 * @throws IOException          if something goes wrong
	 */
	private static JsonNode sendPostXray(String url, JSONObject obj) throws InterruptedException, IOException {
		String token = getAuthenticationToken();
		Map<String, Object> fields = obj.toMap();

		System.out.println("POST URL=" + url);
		System.out.println("POST FIELDS=" + fields.toString());

		HttpResponse<JsonNode> jsonResponse = Unirest.post(url).header("Authorization", "Bearer " + token)
				.header("Content-Type", "application/json").header("accept", "application/json").connectTimeout(60000)
				.body(obj).asJson();
		System.out.println("STATUS=" + jsonResponse.getStatus());
		System.out.println("STATUS TEXT=" + jsonResponse.getStatusText());
		return jsonResponse.getBody();
	}

	// Currently not working
//		/**
//		 * Will send an attachment using curl
//		 *
//		 * @param testKey
//		 * @param filePath
//		 * @return
//		 * @throws IOException
//		 */
//		private static String addAttachment(String testKey, String filePath) throws IOException {
//			String token = getAuthenticationToken();
//			// curl -H "Content-Type: multipart/form-data" -X POST -F attachment=@report.pdf
//			// -H "Authorization: Bearer $token"
//			// https://xray.cloud.xpand-it.com/api/v1/attachments
//			String uri = XRAY_CLOUD_URL + "/api/v1/attachments/"; // + testKey;
//			String request = "curl -H \"Content-Type: multipart/form-data\" -X POST attachment=@" + filePath
//					+ " -H \"Authorization: Bearer $" + token + "\"  " + uri;
//			String response = sendCurl(request);
//			System.out.println(response);
//			return response;
	//
//		}

	/**
	 * Will take the provided file and return a Base 64 Encoded String
	 *
	 * @param file to encode
	 * @return String encoded file
	 * @throws IOException if an IO Exception occurs trying to encode the file
	 */
	static String encodeFileToBase64Binary(File file) throws IOException {
		byte[] encoded = Base64.getEncoder().encode(FileUtils.readFileToByteArray(file));
		return new String(encoded, StandardCharsets.US_ASCII);
	}
}
