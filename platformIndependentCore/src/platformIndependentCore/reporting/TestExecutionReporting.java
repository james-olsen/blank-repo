package platformIndependentCore.reporting;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.json.JSONObject;
import platformIndependentCore.exceptions.WrappedException;
import platformIndependentCore.scripts.ScriptResults;
import platformIndependentCore.utilities.ConfigProperties;

/**
 * <b>Name :</b> TestExecutionReporting.java
 * <p>
 * <b>Generated :</b> Aug 3, 2020
 * <p>
 * <b>Description :</b> Abstract class for Test Execution Reporting
 * <p>
 *
 * @since Aug 3, 2020
 * @author VBAAUSTAYLOL
 */
public abstract class TestExecutionReporting {
	/** URL for the reporting server */
	private String SERVER_URL;
	/** XRAY Authentication file */
	private static String AUTH_FILE = ConfigProperties.getValue(ConfigProperties.XRAY_AUTH_FILE);

	// TODO in future if we need to implement cloud, create a variable to check if
	// cloud reporting or server (use CLOUD or SERVER Url specified in the
	// project.properties)
	/**
	 * Will create an instance of the TestExecution server
	 *
	 * @param serverUrl base url of the reporting server
	 */
	public TestExecutionReporting(String serverUrl) {
		SERVER_URL = serverUrl.trim();
	}

	/**
	 * Will return a string with the absolute path to the authentication file
	 *
	 * @return String authentication file
	 */
	protected static String getAuthenticationFile() {
		return AUTH_FILE;
	}

	/**
	 * Will return the base url for the reporting server
	 *
	 * @return String server url
	 */
	protected String getServerUrl() {
		if (!SERVER_URL.endsWith("/")) {
			SERVER_URL += "/";
		}
		return SERVER_URL;
	}

	/**
	 * Will return if Reporting is turned on or not
	 *
	 * @return boolean TRUE if reporting is on, FALSE if it is off
	 */
	protected abstract boolean isReportingOn();

	/**
	 * Will take the list of event and status and record them in XRAY as a Test
	 * Execution
	 *
	 * @param testKey    Test Case key
	 * @param results    ScriptResults with information on test run
	 * @param resultsLog location of the results log for this test run
	 * @return String Test Execution key
	 */
	public abstract String reportResults(String testKey, ScriptResults results, String resultsLog);

	/**
	 * Will return the URL used to import execution results
	 *
	 * @return String execution import url
	 */
	protected abstract String getExecutionImportUrl();

	/**
	 * Will use the XRAY authentication file to get a TOKEN to be used for
	 * subsequent requests
	 *
	 * @return String Bearer token
	 */
	protected String getAuthenticationToken() {
		String uri = getServerUrl() + "api/v1/authenticate";
		String request = "curl -H \"Content-Type: application/json\" -X POST --data @\"" + getAuthenticationFile()
				+ "\" " + uri;

		String response;
		try {
			response = sendCurl(request);
			// Strip out the beginning and end double quotes from the response. The response
			// should not include double quotes, so just stripping them all out
			response = response.replace("\"", "");
		} catch (IOException e) {
			e.printStackTrace();
			throw new WrappedException("ERROR gettting Authentication Token: " + e.getLocalizedMessage());
		}

		return response;
	}

	/**
	 * Uses cUrl to send a request to the Xray server
	 *
	 * @param request to send
	 * @return String response text
	 * @throws IOException if something goes wrong
	 */
	protected String sendCurl(String request) throws IOException {
		System.out.println("REQUEST: " + request);
		Process process = Runtime.getRuntime().exec(request);
		InputStream inputStream = process.getInputStream();
		String text = IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());

		int exitCode = process.exitValue();
		System.out.println("REQUEST EXIT CODE: " + exitCode);
		System.out.println("EXIT TEXT: " + text);
		return text;
	}

	/**
	 * Will send POST to the XRAY Could server and return the response
	 *
	 * @param url http request to post
	 * @param obj the JSON data to send
	 * @return HttpResponse<JsonNode> with response
	 */
	protected abstract HttpResponse<JsonNode> sendPostXray(String url, JSONObject obj);

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
