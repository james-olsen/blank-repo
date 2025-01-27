package platformIndependentCore.results;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import platformIndependentCore.core.AutomationHelper;
import platformIndependentCore.utilities.ConfigProperties;
import platformIndependentCore.utilities.CoreDateTimeFormat;
import platformIndependentCore.utilities.DateCalculator;

/**
 * Class for generating an HTML log for XML results
 *
 * @author VBAAUSTAYLOL
 *
 */

public class ResultsLogHTML {
	/** Folder name for placing Execution Script Html Log files */
	private static final String SCRIPT_LOGS = "ScriptLogs";
	/* HTML TAG CONSTANTS */
	/** BODY HTML tag */
	static final String BODY = "<body>";
	/** BODY END HTML tag */
	static final String BODY_END = "</body>";
	/** H1 HTML tag */
	static final String H1 = "<h1>";
	/** H1 END HTML tag */
	static final String H1_END = "</h1>";
	/** H3 HTML tag */
	static final String H3 = "<h1>";
	/** H3 END HTML tag */
	static final String H3_END = "</h1>";
	/** FONT Color=RED HTML tag */
	static final String FONT_RED = "<font color=\"red\">";
	/** FONT Color=ORANGE HTML tag */
	static final String FONT_ORANGE = "<font color=\"darkorange\">";
	/** FONT Color=GREEN HTML tag */
	static final String FONT_GREEN = "<font color=\"green\">";
	/** FONT Color=BLUE HTML tag */
	static final String FONT_BLUE = "<font color=\"blue\">";
	/** FONT Color=PURPLE HTML tag */
	static final String FONT_PURPLE = "<font color=\"purple\">";
	/** FONT END HTML tag */
	static final String FONT_END = "</font>";
	/** TABLE HTML tag */
	static final String TABLE = "<table style=\"border: 0px solid black;\">";
	/** TABLE END HTML tag */
	static final String TABLE_END = "</table>";
	/** THEAD HTML tag */
	static final String THEAD = "<thead>\r\n";
	/** THEAD END HTML tag */
	static final String THEAD_END = "</thead>\r\n";
	/** TBODY HTML tag */
	static final String TBODY = "<tbody>\r\n";
	/** TBODY END HTML tag */
	static final String TBODY_END = "</tbody>\r\n";
	/** TH HTML tag */
	static final String TH = "<th>";
	/** TH align=CENTER HTML tag */
	static final String TH_CENTER = "<th align=\"center\">";
	/** TH END HTML tag */
	static final String TH_END = "</th>\r\n";
	/** TR HTML tag */
	static final String TR = "<tr>";
	/** TR BACKGROUND-COLOR=white HTML tag */
	static final String TR_WHITE = "<tr  style=\"border:none;background-color:white;\">";
	/** TR BACKGROUND-COLOR=#E0FFFF HTML tag */
	static final String TR_LIGHT_GREY = "<tr  style=\"border:none;background-color:#E0FFFF;\">";
	/** TR BACKGROUND-COLOR=lightblue HTML tag */
	static final String TR_LIGHT_BLUE = "<tr  style=\"border:none;background-color:lightblue;\">"; // #AFEEEE;\">";
	/** TR BACKGROUND-COLOR=blue HTML tag */
	static final String TR_BLUE = "<tr  style=\"border:none;background-color:SteelBlue;color:#FFFFFF;\">"; // #9ec4db;\">";
	/** TR END HTML tag */
	static final String TR_END = "</tr>";
	/** TD HTML tag */
	static final String TD = "<td>";
	/** TD style=border:none HTML tag */
	static final String TD_NO_BORDER = "<td style=\"border:none\">";
	/** TD style:border:non align=right HTML tag */
	static final String TD_R_ALIGN = "<td style=\"border:none\" align=\"right\">";
	/** TD END HTML tag */
	static final String TD_END = "</td>";
	/** BR HTML tag */
	static final String BR = "<br>";
	/** B HTML tag */
	static final String BOLD = "<b>";
	/** B END HTML tag */
	static final String BOLD_END = "</b>";
	/** I HTML tag */
	static final String ITALIC = "<i>";
	/** I END HTML tag */
	static final String ITALIC_END = "</i>";
	/** logger for this class */
	static Logger log = LogManager.getLogger(ResultsLogHTML.class.getName());
	/** map to contain scriptStatus for set of scripts called */
	HashMap<String, Boolean> scriptStatus = new HashMap<String, Boolean>();

	/** running count of number of VPs executed */
	int totalVpExecutedCnt = 0;
	/** running count of number of VPs passed */
	int totalVpPassCnt = 0;
	/** running count of number of Execution Scripts executed */
	int totalScriptsExecutedCnt = 0;
	/** running count of number of Execution Scripts passed */
	int totalScriptsPassCnt = 0;
	/** running count of number of exceptions thrown */
	int totalExceptionCnt = 0;
	/** current count of number of VPs passed */
	int currVpPassCnt = 0;
	/** current count of exceptions thrown */
	int currExceptionCnt = 0;
	/** exception details */
	String exception = "";
	/** HTML Results log */
	File htmlFile;
	/** Is Xray Reporting on */
	boolean isXray = false;
	/** The host this script ran on */
	String hostName = "";
	static boolean isXrayRun = ConfigProperties.getValue(ConfigProperties.XRAY_REPORTING, "FALSE")
			.equalsIgnoreCase("TRUE") && !ConfigProperties.getValue(ConfigProperties.XRAY_AUTH_FILE).isEmpty();

	/**
	 * Constructor, takes in an exception, the xml file and the html file name. The
	 * final file name will start with either PASS or FAIL depending on status of
	 * the script execution
	 *
	 * @param exception    e
	 * @param fXmlFile     xml file
	 * @param htmlFileName html file name
	 */
	public ResultsLogHTML(String exception, File fXmlFile, String htmlFileName) {
		htmlFile = save(exception, fXmlFile, htmlFileName);
	}

	/**
	 * Will return the File for the HTML Results log
	 *
	 * @return File htmlFile
	 */
	public File getHtmlResultsFile() {
		return htmlFile;
	}

	/**
	 * Will save an HTML log representation of the provided XML file. Final file
	 * name will start with either PASS or FAIL to reflect the status of the script
	 * execution
	 *
	 * @param exception    e
	 * @param fXmlFile     xml file
	 * @param htmlFileName html file name
	 * @return File htmlFile
	 */
	public File save(String exception, File fXmlFile, String htmlFileName) {
		try {
			hostName = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			// Suppress this
			e.printStackTrace();
		}

		// Check if XRAY_REPORTING is TRUE and XRAY_AUTH_FILE is not empty
		isXray = ConfigProperties.getValue(ConfigProperties.XRAY_REPORTING, "FALSE").equalsIgnoreCase("TRUE")
				&& !ConfigProperties.getValue(ConfigProperties.XRAY_AUTH_FILE).isEmpty();

		String testTable = "";

		String script = fXmlFile.getName().replace(".xml", "");
		try {

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);

			// optional, but recommended read this -
			// http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
			doc.getDocumentElement().normalize();

			log.debug("Root element :" + doc.getDocumentElement().getNodeName());

			NodeList suiteL = doc.getElementsByTagName(SaveAsXML.SUITE_TAG);
			NodeList executionL = doc.getElementsByTagName(SaveAsXML.EXECUTION_SCRIPT_TAG);

			Element scriptNode;
			if (suiteL.getLength() > 0) {
				// Generate a Suite results file that will link to contained execution script
				// results
				Element suiteNode = (Element) suiteL.item(0);
				NodeList execL = suiteNode.getElementsByTagName(SaveAsXML.TEST_FROM_SUITE_TAG);
				File mainResultsFile = new File(htmlFileName);
				// use the folder the html log is in, then create a ScriptLogs folder
				// in it
				String suiteFolderPath = mainResultsFile.getParent() + "\\" + SCRIPT_LOGS;
				File suiteFolder = new File(suiteFolderPath);
				suiteFolder.mkdirs();
				testTable = TABLE + getExecutionSummaryHeaderRow();
				for (int i = 0; i < execL.getLength(); i++) {
					// loop through the execution scripts and generate results for each
					Element currNode = (Element) execL.item(i);
					String scriptName = currNode.getAttribute(SaveAsXML.NAME_TAG);
					// shorten the script name to only include the file name
					scriptName = scriptName.substring(scriptName.lastIndexOf(".") + 1);

					String status = currNode.getAttribute(SaveAsXML.STATUS_TAG);
					if (status.startsWith("FAIL")) {
						status = "FAIL" + "_";
					} else {
						status = "";
					}
					String dataid = currNode.getAttribute(SaveAsXML.DATA_ID_TAG);
					dataid = dataid.substring(dataid.indexOf("/") + 1).trim();
					String scriptResultsName = status;
					// If we have a data id, use that for the file name, otherwise use the script
					// name
					if (dataid != null && !dataid.isEmpty()) {
						scriptResultsName += AutomationHelper.getFileNameSafeString(dataid);
					} else {
						scriptResultsName += scriptName;
					}

					// Add the date (as a numeric value) to the file name to preserve results logs
					// in case the same
					// script is executed multiple times
					File currentHtmlFile = new File(suiteFolder.getAbsolutePath() + "//" + scriptResultsName + "_"
							+ DateCalculator.getCurrentDate("yy-M-d_HH-mm-ss") + ".html");
					// Pass in true so we get back just the summary row with a link to the execution
					// result log
					testTable += getScriptTable(currNode, currentHtmlFile, true);
					// XrayReporting.reportResults(name, scriptInstance.getManager(), resultsLog);
					// Increment number of Execution Scripts run
					totalScriptsExecutedCnt++;
				}
				// now that all the execution script totals have been tallied, generate a
				// summary to include on top of summary table
				testTable = getSuiteTotals(suiteNode) + testTable + TABLE_END;

			} else if (executionL.getLength() > 0) {
				// generate execution script html
				scriptNode = (Element) executionL.item(0);
				testTable += getScriptTable(scriptNode, new File(htmlFileName), false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		Date currentDate = new Date();
		CoreDateTimeFormat dateFormat = new CoreDateTimeFormat("M/d/yyyy");
		String title = script + " Results " + dateFormat.format(currentDate);
		boolean pass = totalExceptionCnt == 0 && totalVpPassCnt == totalVpExecutedCnt ? true : false;

		return createFromTemplate(new File(htmlFileName), title, testTable, pass);
	}

	/**
	 * Will format the Results File name
	 *
	 * @param scriptName name of script
	 * @param status     script execution status
	 * @param dataId     script test data id
	 * @param stopTime   time script ended
	 * @return String scriptResults name
	 */
	public static String getResultsFilePath(String scriptName, String status, String dataId, String stopTime) {
		// shorten the script name to only include the file name
		scriptName = scriptName.substring(scriptName.lastIndexOf(".") + 1);

		if (status.startsWith("FAIL")) {
			status = "FAIL" + "_";
		} else {
			status = "";
		}

		String scriptResultsName = status;
		if (dataId != null && !dataId.isEmpty()) {
			dataId = dataId.substring(dataId.indexOf("/") + 1).trim();
			scriptResultsName += AutomationHelper.getFileNameSafeString(dataId) + "_";
		}
		scriptResultsName += scriptName + "_" + stopTime + ".html";

		return scriptResultsName;
	}

	/**
	 * Will return the string to create a table row for the given verification point
	 *
	 * @param n node
	 * @return String row
	 */
	private static String getTableRowForVp(Node n) {
		String vpRow = TR_WHITE;

		Element eElement = (Element) n;
		Boolean pass = Boolean.valueOf(eElement.getElementsByTagName(SaveAsXML.VP_PASS_TAG).item(0).getTextContent());

		// Script Name Event Time Stamp VP Name VP PASS/FAIL VP Note Overall Pass/Fail
		// Total Time (s)
		// Execution Note

		String status = pass ? "PASS" : "FAIL";
		String font = pass ? FONT_GREEN : FONT_RED;
		vpRow += TR_WHITE;
		vpRow += TD + " " + getValue(eElement, SaveAsXML.VP_SCRIPT_NAME) + TD_END;
		vpRow += TD + "VP" + TD_END;
		vpRow += TD + getValue(eElement, SaveAsXML.TIMESTAMP_TAG) + TD_END;
		vpRow += TD + eElement.getAttribute(SaveAsXML.NAME_TAG) + TD_END;
		vpRow += TD + BOLD + font + status + FONT_END + BOLD_END + TD_END;
		vpRow += TD + FONT_PURPLE + getValue(eElement, SaveAsXML.VP_REGEX_TAG) + FONT_END + BR + FONT_BLUE
				+ "EXPECTED: " + FONT_END + getValue(eElement, SaveAsXML.VP_EXPECTED_TAG) + BR + FONT_BLUE + "ACTUAL: "
				+ FONT_END + getValue(eElement, SaveAsXML.VP_ACTUAL_TAG) + TD_END;
		vpRow += TD + TD_END;
		vpRow += TD + TD_END;
		vpRow += TD + getValue(eElement, SaveAsXML.VP_NOTE_TAG) + TD_END;
		vpRow += TR_END;

		return vpRow;

	}

	/**
	 * Will generate an HTML table row for the details on the execution script
	 * provided
	 *
	 * @param scriptName   execution script name
	 * @param dataid       the data id associated with this script execution
	 * @param xRayTestId   the XRay ID for execution
	 * @param startTime    time script started
	 * @param endTime      time script ended
	 * @param duration     how long script took to execute
	 * @param vpTotal      number of verification points (VPs) executed
	 * @param vpPass       number of VPs passed
	 * @param vpFail       number of VPs failed
	 * @param exceptionCnt number of exceptions thrown
	 * @param htmlFile     file for the HTML log
	 * @return String html to create the table row for this entry
	 */
	private static String getExecutionSummaryRow(String scriptName, String dataid, String xRayTestId, String startTime,
			String endTime, String duration, int vpTotal, int vpPass, int vpFail, int exceptionCnt, File htmlFile) {
		String executionSummaryRow = TR_WHITE;
		boolean pass = vpFail == 0 && exceptionCnt == 0 ? true : false;

		// Try to get a relative path
		String absolutePath = htmlFile.getAbsolutePath();

		String link = absolutePath;
		if (link.contains(SCRIPT_LOGS)) {
			link = "./" + absolutePath.substring(absolutePath.indexOf(SCRIPT_LOGS));
		}

		String status = pass ? "PASS" : "FAIL";
		String font = pass ? FONT_GREEN : FONT_RED;
		executionSummaryRow += TD + BOLD + font + status + FONT_END + BOLD_END + TD_END;
		executionSummaryRow += TD + BOLD + dataid + BOLD_END + TD_END;

		// If Xray logging is turned on add column
		if (isXrayRun) {
			executionSummaryRow += TD + BOLD + xRayTestId + BOLD_END + TD_END;
		}

		executionSummaryRow += TD + "<a href=\"" + link + "\"  target=\"_blank\">" + scriptName + "</a>";

		executionSummaryRow += TD + startTime + TD_END;
		executionSummaryRow += TD + endTime + TD_END;
		executionSummaryRow += TD + duration + TD_END;
		executionSummaryRow += TD + vpTotal + TD_END;
		executionSummaryRow += TD + vpPass + TD_END;
		executionSummaryRow += TD + vpFail + TD_END;
		executionSummaryRow += TD + exceptionCnt + TD_END;
		executionSummaryRow += TR_END;

		return executionSummaryRow;

	}

	/**
	 * Creates the Header row for the Suite Execution script summary table
	 *
	 * @return String html to create table header row
	 */
	private static String getExecutionSummaryHeaderRow() {
		String executionSummaryRow = TR_BLUE;

		executionSummaryRow += TH + "STATUS" + TH_END;
		executionSummaryRow += TH + "SUITE / TEST ID" + TH_END;
		if (isXrayRun) {
			executionSummaryRow += TH + "XRAY TEST ID" + TH_END;
		}
		executionSummaryRow += TH + "SCRIPT" + TH_END;
		executionSummaryRow += TH + "START TIME" + TH_END;
		executionSummaryRow += TH + "END TIME" + TH_END;
		executionSummaryRow += TH + "DURATION" + TH_END;
		executionSummaryRow += TH + "TOTAL VPs" + TH_END;
		executionSummaryRow += TH + "VPs PASSED" + TH_END;
		executionSummaryRow += TH + "VPs FAILED" + TH_END;
		executionSummaryRow += TH + "NUMBER EXCEPTIONS" + TH_END;
		executionSummaryRow += TR_END;

		return executionSummaryRow;

	}

	/**
	 * Will return the string to create a table row for the given verification point
	 *
	 * @param n node
	 * @return String row
	 */
	private static String getTableRowForNote(Node n) {
		String noteRow = TR_WHITE;

		Element eElement = (Element) n;
		Boolean exception = Boolean
				.valueOf(eElement.getElementsByTagName(SaveAsXML.NOTE_EXCEPTION_TAG).item(0).getTextContent());
		Boolean warning = Boolean
				.valueOf(eElement.getElementsByTagName(SaveAsXML.NOTE_WARNING_TAG).item(0).getTextContent());
		String event = "NOTE";
		if (warning) {
			event = "WARNING";
		}
		if (exception) {
			event = "EXCEPTION";
		}

		// Script Name Event Time Stamp VP Name VP PASS/FAIL VP Note Overall Pass/Fail
		// Total Time (s)
		// Execution Note
		String font = warning ? FONT_ORANGE : FONT_BLUE;
		font = exception ? FONT_RED : font;
		noteRow += TR_WHITE;
		noteRow += TD + " " + getValue(eElement, SaveAsXML.VP_SCRIPT_NAME) + TD_END;
		if (exception) {
			noteRow += TD + FONT_RED + event + FONT_END;

		} else if (warning) {
			noteRow += TD + FONT_ORANGE + event + FONT_END;

		} else {
			noteRow += TD + event + TD_END;
		}
		noteRow += TD + getValue(eElement, SaveAsXML.TIMESTAMP_TAG) + TD_END;
		noteRow += "<td colspan=6>" + font + getValue(eElement, SaveAsXML.NAME_TAG) + FONT_END + TD_END;
		noteRow += TR_END;

		return noteRow;

	}

	/**
	 * Will return the string to create a table row for the given screenshot
	 *
	 * @param n           node
	 * @param isFromSuite TRUE if this screenshot was taken inside of a suite
	 *                    execution, FALSE if not
	 * @return String row
	 */
	private String getTableRowForScreenShot(Node n, boolean isFromSuite) {
		String noteRow = TR_WHITE;

		Element eElement = (Element) n;
		String event = "SCREENSHOT";
		String screenShotName = getValue(eElement, SaveAsXML.FILE_TAG);
		screenShotName = screenShotName.substring(screenShotName.lastIndexOf("\\") + 1);

		String file = getValue(eElement, SaveAsXML.FILE_TAG);
		// need to go up one folder to get out of ScriptLogs, and then go into
		// the ScreenShots folder if this is from a Suite (because the log will be in
		// the ScriptLogs folder). Otherwise the ScreenShots folder will be accessible
		// from the current folder
		String link = isFromSuite ? "./../" : "./";
		// now that we have the relative location, add the file path starting with
		// "ScreenShots"
		link += file.substring(file.indexOf("ScreenShots"));

		// Script Name Event Time Stamp VP Name VP PASS/FAIL VP Note Overall Pass/Fail
		// Total Time (s)
		// Execution Note
		String font = FONT_BLUE;
		noteRow += TR_WHITE;
		noteRow += TD + " " + getValue(eElement, SaveAsXML.VP_SCRIPT_NAME) + TD_END;
		noteRow += TD + event + TD_END;

		noteRow += TD + getValue(eElement, SaveAsXML.TIMESTAMP_TAG) + TD_END;

		noteRow += "<td colspan=6>" + font + "<a href=\"" + link + "\"  target=\"_blank\">"
				+ getValue(eElement, SaveAsXML.NAME_TAG) + "</a>" + FONT_END;

		if (isXray) {
			// If XRAY is enabled, then leave a note to unzip so the link will work from the
			// uploaded file in Xray, it can also be viewed from Evidence
			String msg = "";
			String hostName = "";
			try {
				hostName = InetAddress.getLocalHost().getHostName();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
			if (ConfigProperties.getValue(ConfigProperties.DISABLE_SCREEN_SHOT_REPORTING_ATTACHMENT, "FALSE")
					.equalsIgnoreCase("true")) {
				msg = "If you downloaded this log from Xray, the screenshot link will be broken. Screenshot attachments to Xray results is disabled for your project, please retrieve the screenshot from: "
						+ hostName + ". ";
			} else {
				msg = "Ensure that you unzip the log result folder for links to work. Screen Shot is also available under Test Run Evidence.";
			}
			noteRow += BR + BR + ITALIC + msg + ITALIC_END + BR + "Screenshot File Name: " + BOLD + screenShotName
					+ BOLD_END + BR;

		}
		noteRow += TD_END + TR_END;

		return noteRow;

	}

	/**
	 * Will return the string to create all the table rows to represent the given
	 * script. It will create rows for every verification point, and called script
	 * (and the events that the called script generated as well)
	 *
	 * @param n           node
	 * @param isFromSuite TRUE if this script was kicked off from a suite, FALSE if
	 *                    not
	 * @return String rows
	 */
	private String getTableRowsForScript(Node n, boolean isFromSuite) {
		Element scriptElem = (Element) n;

		String name = scriptElem.getAttributes().getNamedItem(SaveAsXML.NAME_TAG).getTextContent();
		// log.debug("getTableRowsForScriptNode::NAME=" + name);
		// default status to PASS, and failures will update this to FAIL
		scriptStatus.put(name, true);

		// NamedNodeMap atts = scriptElem.getAttributes();
		// for (int i = 0; i < atts.getLength(); i++) {
		// log.debug("getTableRowsForScriptNode::att" + i + ": " +
		// atts.item(i).getNodeName() + "=" +
		// atts.item(i).getNodeValue());
		// }
		String startTime = scriptElem.getAttributes().getNamedItem(SaveAsXML.START_TIME_TAG).getTextContent();
		String stopTime = scriptElem.getAttributes().getNamedItem(SaveAsXML.END_TIME_TAG).getTextContent();
		String executionDuration = scriptElem.getAttributes().getNamedItem(SaveAsXML.DURATION_TAG).getTextContent();

		// Adds Row(s) for the results table for this node
		String csResults = TR_LIGHT_GREY;
		// Script Name Event Time Stamp VP Name VP PASS/FAIL VP Note Overall Pass/Fail
		// Total Time (s)
		// Execution Note
		csResults += TD + name + TD_END;
		csResults += TD + "START" + TD_END;
		csResults += TD + startTime + TD_END;
		csResults += "<td colspan=6>" + "CALL SCRIPT, STARTING TEST SCRIPT" + TD_END + TR_END;
		// Add the children nodes for this called script
		csResults += getTableRowsForChildrenEvents(scriptElem, isFromSuite);

		String overallPass = FONT_RED + "FAIL";
		if (scriptStatus.get(name) != null && scriptStatus.get(name)) {
			overallPass = FONT_GREEN + "PASS";
		}
		// String overallPass = scriptStatus.get(name) ? FONT_GREEN + "PASS" : FONT_RED
		// + "FAIL";
		overallPass += FONT_END;
		// Add the summary row for this called script
		csResults += TR_LIGHT_BLUE + TD + name + TD_END;
		csResults += TD + "END" + TD_END;
		csResults += TD + stopTime + TD_END;
		csResults += "<td colspan=4 align='right'>" + overallPass + TD_END;
		csResults += TD + executionDuration + TD_END;
		csResults += TD + TD_END + TR_END;

		return csResults;

	}

	/**
	 * This will return a String needed to create the table rows for all the events
	 * (vps, and called scripts) that occur for the given object.
	 *
	 * @param currNode    node
	 * @param isFromSuite TRUE if this script was kicked off from a suite, FALSE if
	 *                    not
	 * @return String rows
	 */
	private String getTableRowsForChildrenEvents(Node currNode, boolean isFromSuite) {
		String childrenDetails = "";
		NodeList allElements = currNode.getChildNodes();

		for (int temp = 0; temp < allElements.getLength(); temp++) {

			Node currChildElem = allElements.item(temp);

			if (currChildElem != null && currChildElem.getNodeType() == Node.ELEMENT_NODE) {
				switch (currChildElem.getNodeName()) {
				case SaveAsXML.CALLED_SCRIPT_TAG:
				case SaveAsXML.EXECUTION_SCRIPT_TAG:
					childrenDetails += getTableRowsForScript(currChildElem, isFromSuite);
					// check if the child event failed, if so this overall should fail as well
					// boolean childPass = scriptStatus.get(getValue((Element) currNode,
					// SaveAsXML.NAME_TAG));
					String currChildName = currChildElem.getAttributes().getNamedItem(SaveAsXML.NAME_TAG)
							.getTextContent();

					log.debug("scriptName=" + currChildName + ", scriptStatus = " + scriptStatus);
					boolean childPass = getStatus(currChildName); // scriptStatus.get(currChildName);
					if (!childPass) {
						String currName = currNode.getAttributes().getNamedItem(SaveAsXML.NAME_TAG).getTextContent();
						log.debug("NODE NAME=" + currName);

						scriptStatus.put(currName, false);
					}
					break;
				case SaveAsXML.VP_TAG:
					childrenDetails += getTableRowForVp(currChildElem);
					if (getValue((Element) currChildElem, SaveAsXML.VP_PASS_TAG).equals("true")) {
						currVpPassCnt++;
					} else {
						scriptStatus.put(getValue((Element) currChildElem, SaveAsXML.VP_SCRIPT_NAME), false);
						// check if the child event failed, if so this overall should fail as well
						scriptStatus.put(getValue((Element) currNode, SaveAsXML.VP_SCRIPT_NAME), false);
					}
					break;
				case SaveAsXML.NOTE_TAG:
					childrenDetails += getTableRowForNote(currChildElem);
					exception += currChildElem.getTextContent();

					// check for exception, if so, set the status to false
					boolean exception = Boolean
							.valueOf(getValue((Element) currChildElem, SaveAsXML.NOTE_EXCEPTION_TAG));
					if (exception) {
						scriptStatus.put(getValue((Element) currChildElem, SaveAsXML.VP_SCRIPT_NAME), false);
						// check if the child event failed, if so this overall should fail as well
						scriptStatus.put(getValue((Element) currNode, SaveAsXML.VP_SCRIPT_NAME), false);
						currExceptionCnt++;
					}
					break;
				case SaveAsXML.SCREENSHOT_TAG:
					childrenDetails += getTableRowForScreenShot(currChildElem, isFromSuite);
					break;

				}
			}

		}
		return childrenDetails;
	}

	/**
	 * Get the PASS/FAIL status for the script
	 *
	 * @param scriptName to get status for
	 * @return boolean TRUE if script has PASS status, FALSE if not
	 */
	private boolean getStatus(String scriptName) {
		boolean pass = false;
		if (scriptStatus != null && scriptStatus.containsKey(scriptName)) {
			pass = scriptStatus.get(scriptName);
		}
		return pass;
	}

	/**
	 * Returns the string needed to create the entire table for the given script
	 * node
	 *
	 * @param scriptNode     node
	 * @param htmlFile       File for this script
	 * @param isSuiteSummary TRUE if this is for the Suite summary table, FALSE if
	 *                       not
	 * @return String table
	 */
	private String getScriptTable(Element scriptNode, File htmlFile, boolean isSuiteSummary) {

		String scriptName = scriptNode.getAttribute(SaveAsXML.NAME_TAG);
		NodeList nList = scriptNode.getElementsByTagName(SaveAsXML.VP_TAG);

		// Default pass status to true
		scriptStatus.put(scriptName, true);

		String summaryTable = "";

		String style = "<style>\r\n" + "table, th, td {\r\n"
				+ "    border: 1px solid black;border-collapse: collapse;padding: 15px;\r\n" + "}\r\n"
				+ "tr:nth-child(even) {\r\n" + "    background-color: #dddddd;\r\n" + "}" + "</style>";
		log.debug("----------------------------");
		summaryTable += style;

		// Script Name Event Time Stamp VP Name VP PASS/FAIL VP Note Overall Pass/Fail
		// Total Time (s)
		// Execution Note

		summaryTable += TABLE + "\r\n" + THEAD + TR_BLUE + "\r\n";
		summaryTable += TH + "Script" + TH_END;
		summaryTable += TH + "Event" + TH_END;
		summaryTable += TH + "Timestamp" + TH_END;
		summaryTable += TH + "VP Name" + TH_END;
		summaryTable += TH_CENTER + "VP PASS/FAIL" + TH_END;
		summaryTable += TH + "VP Note" + TH_END;
		summaryTable += TH + "Overall Pass/Fail" + TH_END;
		summaryTable += TH + "Total Time (s)" + TH_END;
		summaryTable += TH + "Execution Note" + TH_END + TR_END + THEAD_END;

		summaryTable += TBODY + getTableRowsForScript(scriptNode, isSuiteSummary);

		summaryTable += TBODY_END + TABLE_END;

		/*****************************************
		 * Tags for Summary Table at the top of the results page
		 *****************************************/
		String testTable = "";
		// Values to insert into HTML

		int numOfFailedVPs = nList.getLength() - currVpPassCnt;
		int numOfVPsExecuted = nList.getLength();

		String dataid = scriptNode.getAttributes().getNamedItem(SaveAsXML.DATA_ID_TAG).getTextContent();
		String xRayTestId = scriptNode.getAttributes().getNamedItem(SaveAsXML.XRAY_ID_TAG).getTextContent();
		String startTime = scriptNode.getAttributes().getNamedItem(SaveAsXML.START_TIME_TAG).getTextContent();
		String stopTime = scriptNode.getAttributes().getNamedItem(SaveAsXML.END_TIME_TAG).getTextContent();
		String executionDuration = scriptNode.getAttributes().getNamedItem(SaveAsXML.DURATION_TAG).getTextContent();

		// escape the data id for HTML (sometimes has special characters)
		dataid = StringEscapeUtils.escapeHtml4(dataid);

		String suite = dataid;
		String testDataId = "";
		String[] data = dataid.split("/");
		if (data.length > 1) {
			suite = data[0];
			testDataId = data[1];
		}
		/*****************************************
		 * Add count of VPs pass/fail
		 *****************************************/

		testTable = H1 + scriptName + " Results" + H1_END + style + TABLE;

		if (currExceptionCnt > 0) {
			testTable += getExceptionRow(currExceptionCnt);
		}

		testTable += TR_WHITE + TD_NO_BORDER + TABLE;
		testTable += TR_WHITE + TD_R_ALIGN + BOLD + "Script Start Time:" + BOLD_END + TD_END;
		testTable += TD_NO_BORDER + startTime + TD_END;
		testTable += TR_WHITE + TD_R_ALIGN + BOLD + "Script Stop Time:" + BOLD_END + TD_END;
		testTable += TD_NO_BORDER + stopTime + TD_END;
		testTable += TR_WHITE + TD_R_ALIGN + BOLD + "Script Duration:" + BOLD_END + TD_END;
		testTable += TD_NO_BORDER + executionDuration + TD_END;

		testTable += TR_END + TABLE_END + TD_NO_BORDER + TABLE;

		// VP Summary
		testTable += TR_WHITE + TD_R_ALIGN + BOLD + "Number of VPs Executed:" + BOLD_END + TD_END;
		testTable += TD_NO_BORDER + numOfVPsExecuted + TD_END + TR_END;
		testTable += TR_WHITE + TD_R_ALIGN + BOLD + "Number of VPs Passed:" + BOLD_END + TD_END;
		testTable += TD_NO_BORDER + FONT_GREEN + currVpPassCnt + FONT_END + TD_END + TR_END;
		testTable += TR_WHITE + TD_R_ALIGN + BOLD + "Number of VPs Failed:" + BOLD_END + TD_END;
		testTable += TD_NO_BORDER + FONT_RED + numOfFailedVPs + FONT_END;
		testTable += TD_END + TR_END;
		testTable += TABLE_END + TD_NO_BORDER + TABLE;

		if (xRayTestId.isEmpty() || xRayTestId == "") {
			xRayTestId = "None";
		}
		// TODO
		testTable += TR_WHITE + TD_R_ALIGN + BOLD + "Browser:" + BOLD_END + TD_END;
		testTable += TD_NO_BORDER + ConfigProperties.getBrowser() + TD_END + TR_END;
		if (suite != null && !suite.isEmpty()) {
			testTable += TR_WHITE + TD_R_ALIGN + BOLD + "Suite:" + BOLD_END + TD_END;
			testTable += TD_NO_BORDER + suite + TD_END;
			if (isXray) {
				testTable += TR_WHITE + TD_R_ALIGN + BOLD + "Test Data Id / Xray Test ID:" + BOLD_END + TD_END;
				testTable += TD_NO_BORDER + testDataId + " / " + xRayTestId + TD_END;
			} else {
				testTable += TR_WHITE + TD_R_ALIGN + BOLD + "Test Data Id:" + BOLD_END + TD_END;
				testTable += TD_NO_BORDER + testDataId + TD_END;

			}

			// TODO this is the summary at the top
		} else {
			// if there is not info for a suite/data id, then include test env and host name
			testTable += TR_WHITE + TD_R_ALIGN + BOLD + "Test Env:" + BOLD_END + TD_END;
			testTable += TD_NO_BORDER + ConfigProperties.getTestEnv() + TD_END;
			testTable += TR_WHITE + TD_R_ALIGN + BOLD + "Host:" + BOLD_END + TD_END;
			testTable += TD_NO_BORDER + hostName + TD_END;
		}

		testTable += TABLE_END + TD_END + TR_END + TABLE_END;

		// Write results with the overall script details to the Daily Log. The method
		// will only write the results when DAILY_LOGS set to TRUE in config.properties
		DailyLog.writeResults(scriptName, startTime, stopTime, executionDuration, numOfVPsExecuted, currVpPassCnt,
				numOfFailedVPs, currExceptionCnt, htmlFile.getAbsolutePath());
		boolean pass = currExceptionCnt == 0 && numOfFailedVPs == 0 ? true : false;
		// If the overall status is PASS, increment number of Execution Scripts passed
		if (pass) {
			totalScriptsPassCnt++;
		}

		/*****************************************
		 * Exception/Error Area Below the Summary
		 ****************************************/
		// testTable += H3 + exceptionDetails + H3_END + summaryTable;
		testTable += H3 + H3_END + summaryTable;

		if (isSuiteSummary) {
			Date currentDate = new Date();
			CoreDateTimeFormat dateFormat = new CoreDateTimeFormat("M/d/yyyy");
			String title = scriptName + " Results " + dateFormat.format(currentDate);

			File resultsFile = createFromTemplate(htmlFile, title, testTable, pass);
			testTable = getExecutionSummaryRow(scriptName, dataid, xRayTestId, startTime, stopTime, executionDuration,
					numOfVPsExecuted, currVpPassCnt, numOfFailedVPs, currExceptionCnt, resultsFile);
		}
		// reset the currPassCnt after adding them to the totals
		totalVpExecutedCnt += nList.getLength();
		totalVpPassCnt += currVpPassCnt;
		totalExceptionCnt += currExceptionCnt;
		currVpPassCnt = 0;
		currExceptionCnt = 0;
		return testTable;
	}

	/**
	 * Will generate HTML to display the overall summary of the Suite
	 *
	 * @param suiteNode contains details for the suite
	 * @return String HTML table with Suite summary results
	 */
	private String getSuiteTotals(Element suiteNode) {
		int numOfFailedVPs = totalVpExecutedCnt - totalVpPassCnt;
		String startTimeTime = suiteNode.getAttributes().getNamedItem(SaveAsXML.START_TIME_TAG).getTextContent();
		String stopTimeTime = suiteNode.getAttributes().getNamedItem(SaveAsXML.END_TIME_TAG).getTextContent();
		String executionDuration = suiteNode.getAttributes().getNamedItem(SaveAsXML.DURATION_TAG).getTextContent();

		String scriptName = suiteNode.getAttributes().getNamedItem(SaveAsXML.NAME_TAG).getTextContent();
		String style = "<style>\r\n" + "table, th, td {\r\n"
				+ "    border: 1px solid black;border-collapse: collapse;padding: 15px;\r\n" + "}\r\n"
				+ "tr:nth-child(even) {\r\n" + "    background-color: #dddddd;\r\n" + "}" + "</style>";

		String suiteResultsTable = H1 + scriptName + " Results" + H1_END;
		if (totalExceptionCnt > 0) {
			suiteResultsTable += getExceptionRow(totalExceptionCnt);
		}

		suiteResultsTable += style + TABLE;
		suiteResultsTable += TR_WHITE + TD_NO_BORDER + TABLE;
		suiteResultsTable += TR_WHITE + TD_R_ALIGN + BOLD + "Script Start Time:" + BOLD_END + TD_END;
		suiteResultsTable += TD_NO_BORDER + startTimeTime + TD_END;
		suiteResultsTable += TR_WHITE + TD_R_ALIGN + BOLD + "Script Stop Time:" + BOLD_END + TD_END;
		suiteResultsTable += TD_NO_BORDER + stopTimeTime + TD_END;
		suiteResultsTable += TR_WHITE + TD_R_ALIGN + BOLD + "Script Duration:" + BOLD_END + TD_END;
		suiteResultsTable += TD_NO_BORDER + executionDuration + TD_END;

		suiteResultsTable += TR_END + TABLE_END + TD_NO_BORDER + TABLE;

		// Execution Script Summary
		int totalScriptsFailedCnt = totalScriptsExecutedCnt - totalScriptsPassCnt;
		suiteResultsTable += TR_WHITE + TD_R_ALIGN + BOLD + "Total Execution Scripts:" + BOLD_END + TD_END;
		suiteResultsTable += TD_NO_BORDER + totalScriptsExecutedCnt + TD_END + TR_END;
		suiteResultsTable += TR_WHITE + TD_R_ALIGN + BOLD + "Execution Scripts Passed:" + BOLD_END + TD_END;
		suiteResultsTable += TD_NO_BORDER + FONT_GREEN + totalScriptsPassCnt + FONT_END + TD_END + TR_END;
		suiteResultsTable += TR_WHITE + TD_R_ALIGN + BOLD + "Execution Scripts Failed:" + BOLD_END + TD_END;
		suiteResultsTable += TD_NO_BORDER + FONT_RED + totalScriptsFailedCnt + FONT_END;
		suiteResultsTable += TD_END + TR_END;

		suiteResultsTable += TABLE_END + TD_NO_BORDER + TABLE;
		// suiteResultsTable += TABLE_END + TD_END + TR_END + TABLE_END + TABLE;

		// VP Summary
		suiteResultsTable += TR_WHITE + TD_R_ALIGN + BOLD + "Number of VPs Executed:" + BOLD_END + TD_END;
		suiteResultsTable += TD_NO_BORDER + totalVpExecutedCnt + TD_END + TR_END;
		suiteResultsTable += TR_WHITE + TD_R_ALIGN + BOLD + "Number of VPs Passed:" + BOLD_END + TD_END;
		suiteResultsTable += TD_NO_BORDER + FONT_GREEN + totalVpPassCnt + FONT_END + TD_END + TR_END;
		suiteResultsTable += TR_WHITE + TD_R_ALIGN + BOLD + "Number of VPs Failed:" + BOLD_END + TD_END;
		suiteResultsTable += TD_NO_BORDER + FONT_RED + numOfFailedVPs + FONT_END;
		suiteResultsTable += TD_END + TR_END;

		suiteResultsTable += TABLE_END + TD_NO_BORDER + TABLE;

		suiteResultsTable += TR_WHITE + TD_R_ALIGN + BOLD + "Browser:" + BOLD_END + TD_END;
		suiteResultsTable += TD_NO_BORDER + ConfigProperties.getBrowser() + TD_END + TR_END;
		suiteResultsTable += TR_WHITE + TD_R_ALIGN + BOLD + "Test Env:" + BOLD_END + TD_END;
		suiteResultsTable += TD_NO_BORDER + ConfigProperties.getTestEnv() + TD_END;
		suiteResultsTable += TR_WHITE + TD_R_ALIGN + BOLD + "Host:" + BOLD_END + TD_END;
		suiteResultsTable += TD_NO_BORDER + hostName + TD_END;
		suiteResultsTable += TABLE_END + TD_END + TR_END + TABLE_END;

		return suiteResultsTable;

	}

	/**
	 * Will return an HTML formatted String to display the number of exceptions
	 *
	 * @param exceptionCnt number of exceptions
	 * @return String HTML to display number of exceptions
	 */
	private String getExceptionRow(int exceptionCnt) {
		return FONT_RED + BOLD + "Number of EXCEPTIONS:  " + BOLD_END + FONT_END + exceptionCnt + BR;
	}

	/**
	 * Will look for an element with the given tagName, if it exists, then it will
	 * return the text value for it. If the tag is not found, an empty String is
	 * returned
	 *
	 * @param eElement XML element to retrieve value from
	 * @param tagName  XML tag to get value for
	 * @return String value for the specified tag
	 */
	private static String getValue(Element eElement, String tagName) {
		String val = "";
		if (eElement.getElementsByTagName(tagName).item(0) != null) {
			val = eElement.getElementsByTagName(tagName).item(0).getTextContent();
		}
		return val;
	}

	/**
	 * Uses a template to create the new file. The new file will start with PASS or
	 * FAIL to reflect the status of the script execution
	 *
	 * @param newHtmlFile File to create
	 * @param title       to insert into template
	 * @param body        content to insert into template
	 * @param pass        overall status of this execution script (PASS or FAIL)
	 * @return File htmlFile
	 */
	private static File createFromTemplate(File newHtmlFile, String title, String body, boolean pass) {
		String fileStatus = ""; //
		// Get the base name for the file. Final file with start with the Status
		String baseName = newHtmlFile.getName();
		String path = newHtmlFile.getParentFile().getAbsolutePath();
		// Create the file with the full path plus the status before the html file name
		File fileWithStatus = new File(path + "\\" + fileStatus + baseName);
		try {
			File file = new File("");
			log.debug("CURRENT FOLDER: " + file.getAbsolutePath());
			String templateFolder = "resources//";
			String resultsFolder = ConfigProperties.getValue("TEMPLATE_FOLDER", templateFolder);

			File htmlTemplateFile = new File(resultsFolder + "\\resultsTemplate.html");
			log.debug("template=" + htmlTemplateFile.getAbsolutePath());
			log.debug("exists? " + htmlTemplateFile.exists());

			// added charset to default to get rid of deprecation warning
			String htmlString = FileUtils.readFileToString(htmlTemplateFile, Charset.defaultCharset());
			htmlString = htmlString.replace("$title", title);
			htmlString = htmlString.replace("$body", body);
			// added charset to default to get rid of deprecation warning
			FileUtils.writeStringToFile(fileWithStatus, htmlString, Charset.defaultCharset());
		} catch (IOException i) {

		}
		return fileWithStatus;
	}

}