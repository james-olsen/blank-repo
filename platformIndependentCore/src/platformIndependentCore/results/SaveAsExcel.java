package platformIndependentCore.results;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import platformIndependentCore.datafiles.ExcelFileBase;

/**
 * Concrete implementation for ExcelResultsFile This class will be used to
 * create and update an Excel spreadsheet with results from a test script,
 * including lines for VerificationPoint results
 *
 * @author VBAAUSTAYLOL
 *
 */
public class SaveAsExcel extends ExcelFileBase {
	/** yyyy-MM-dd HH:mm:ss **/
	public static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";

	// Text Values for the Column Headers in the results file
	/** Script Name **/
	private static final String SCRIPTNAME_COLUMN = "Script Name";
	/** Event **/
	private static final String EVENT_COLUMN = "Event";
	/** Time Stamp **/
	private static final String TIMESTAMP_COLUMN = "Time Stamp";
	/** VP Name **/
	private static final String VP_NAME_COLUMN = "VP Name";
	/** VP PASS/FAIL **/
	private static final String VP_PASSFAIL_COLUMN = "VP PASS/FAIL";
	/** VP Note **/
	private static final String VP_NOTE_COLUMN = "VP Note";
	/** Overall Pass/Fail **/
	private static final String OVERALL_PASSFAIL_COLUMN = "Overall Pass/Fail";
	/** Total Time (s) **/
	private static final String EXECUTION_TIME_COLUMN = "Total Time (s)";
	/** Execution Note **/
	private static final String EXECUTION_NOTE_COLUMN = "Execution Note";

	// Index values for the columns that correlate to the columns position in
	// the spreadsheet
	/** 0 **/
	private static final int SCRIPTNAME_INDEX = 0;
	/** 1 **/
	private static final int EVENT_INDEX = 1;
	/** 2 **/
	private static final int TIMESTAMP_INDEX = 2;
	/** 3 **/
	private static final int VP_NAME_INDEX = 3;
	/** 4 **/
	private static final int VP_PASSFAIL_INDEX = 4;
	/** 5 **/
	private static final int VP_NOTE_INDEX = 5;
	/** 6 **/
	private static final int OVERALL_PASSFAIL_INDEX = 6;
	/** 7 **/
	private static final int EXECUTION_TIME_INDEX = 7;
	/** 8 **/
	private static final int EXECUTION_NOTE_INDEX = 8;
	/** VPs Executed: **/
	private static final String TOTAL_VPs = "VPs Executed: ";
	/** VPs Passed: **/
	private static final String PASS_VPs = "VPs Passed: ";
	/** EXCEPTIONS: **/
	private static final String EXCEPTION_CNT = "EXCEPTIONS: ";

	/** hashmap that lets us correlate the column text with its index **/
	private static final HashMap<Integer, String> columnMap = new HashMap<Integer, String>();
	/** Script Status **/
	HashMap<String, Boolean> scriptStatus = new HashMap<String, Boolean>();
	/** **/
	private static final int COLUMN_COUNT = 9;

	/** pass status used to keep status of the current script execution **/
	private boolean passStatus = true;
	/** end written: used to keep status of the current script execution **/
	boolean endWritten = false;
	/** terminated early: used to keep status of the current script execution **/
	boolean terminatedEarly = false;
	/** **/
	Workbook workbook = null;
	/** **/
	Sheet sheet = null;
	/** **/
	File excelFile = null;
	/** **/
	String fileName = "";
	/** **/
	int passCnt = 0;
	/** **/
	int totalCnt = 0;
	// int totalPassCnt = 0;
	/** **/
	int currPassCnt = 0;
	/** **/
	int exceptionCnt = 0;
	/** **/
	String exception = "";
	/** **/
	static Logger log = LogManager.getLogger(SaveAsExcel.class.getName());

	/**
	 * Constructor for the concrete implementation of the ExcelResultsFile class,
	 * which creates and updates an Excel based results file
	 *
	 *
	 * @param xmlFile   xml
	 * @param excelFile excel
	 */
	public SaveAsExcel(File xmlFile, File excelFile) {
		super(excelFile.getAbsolutePath(), ResultsLogExcel.TEST_RESULTS_SHEET_NAME, false);
		populateColumnMap();

		fileName = excelFile.getAbsolutePath();
		this.excelFile = excelFile;

		// if the file does not exist, or is older than the xml file, create it now.
		if (!excelFile.exists() || excelFile.lastModified() < xmlFile.lastModified()) {
			createResultsFile(excelFile);
			sheetIndex = workbook.getSheetIndex(sheetName);
			save(xmlFile, excelFile);
		}
	}

	/**
	 * Will add a VP Summary tab to the Excel File
	 */
	private void addSummaryTab() {
		log.debug("PASS CNT=" + passCnt);
		log.debug("TOTAL CNT=" + totalCnt);
		try {

			FileInputStream fis = new FileInputStream(fileName);
			Workbook workbook = getWorkbook(fis);

			Sheet summarySheet = workbook.createSheet("VP Summary");
			int sheetIndex = workbook.getSheetIndex(summarySheet);

			FileOutputStream fos = new FileOutputStream(fileName);
			workbook.write(fos);
			fos.close();
			fos = null;
			// writeSummaryRow("Script Start Time", this.)
			writeSummaryRow("TOTAL NUMBER OF VPs", String.valueOf(totalCnt), sheetIndex);
			writeSummaryRow("NUMBER OF VPs PASSED", String.valueOf(passCnt), sheetIndex);
			writeSummaryRow("NUMBER OF VPs FAILED", String.valueOf(totalCnt - passCnt), sheetIndex);
			writeSummaryRow("NUMBER OF EXCEPTIONS", String.valueOf(exceptionCnt), sheetIndex);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Constructor for the concrete implementation of the ExcelResultsFile class,
	 * which creates and updates an Excel based results file
	 *
	 * @param node      n
	 * @param excelFile to save
	 */
	public SaveAsExcel(Node node, File excelFile) {
		super(excelFile.getAbsolutePath(), ResultsLogExcel.TEST_RESULTS_SHEET_NAME, false);
		populateColumnMap();

		fileName = excelFile.getAbsolutePath();
		this.excelFile = excelFile;
		if (excelFile.exists()) {
			excelFile.delete();
		}

		createResultsFile(excelFile);
		sheetIndex = workbook.getSheetIndex(sheetName);
		boolean currPass = addScriptResults(node, true);
		log.debug(excelFile.getName() + " PASS?" + currPass);
		if (!currPass) {
			this.passStatus = false;
		}

		addSummaryTab();
	}

	/**
	 * Auto-size the columns
	 *
	 * @param sheet within the workbook
	 */
	private void autoSize(Sheet sheet) {
		for (int i = 0; i < COLUMN_COUNT; i++) {
			sheet.autoSizeColumn(i);
		}
	}

	/**
	 * Will save an HTML log representation of the provided XML file
	 *
	 * @param fXmlFile  xml
	 * @param excelFile excel
	 */
	private synchronized void save(File fXmlFile, File excelFile) {
		// String script = "NOT SET";
		try {

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);

			// optional, but recommended
			// read this -
			// http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
			doc.getDocumentElement().normalize();

			log.debug("Root element :" + doc.getDocumentElement().getNodeName());

			// NodeList nList = doc.getElementsByTagName("VP");
			NodeList suiteL = doc.getElementsByTagName(SaveAsXML.SUITE_TAG);
			NodeList executionL = doc.getElementsByTagName(SaveAsXML.EXECUTION_SCRIPT_TAG);

			Element scriptNode;
			if (suiteL.getLength() > 0) {
				Element suiteNode = (Element) suiteL.item(0);
				String suiteName = suiteNode.getAttribute("name");
				writeSuiteStart(suiteNode);
				boolean pass = true;
				// String currScriptName = suiteNode.getAttribute("name");
				String suiteFolderName = excelFile.getAbsolutePath();
				suiteFolderName = suiteFolderName.substring(0, suiteFolderName.lastIndexOf("\\")); // .replace(".xlsx",
																									// "");
																									// // +
																									// "//" +
																									// suiteName;
				suiteFolderName = suiteFolderName + "//" + suiteName;
				File suiteFolder = new File(suiteFolderName);
				if (!suiteFolder.exists()) {
					suiteFolder.mkdirs();
				}
				NodeList execL = suiteNode.getElementsByTagName(SaveAsXML.TEST_FROM_SUITE_TAG);
				for (int i = 0; i < execL.getLength(); i++) {
					Node n = execL.item(i);
					String currScriptName = ((Element) n).getAttribute("name");

					File currExcelFile = new File(suiteFolderName + "//" + currScriptName + ".xlsx");
					log.debug("SUITE TEST FILE: " + currExcelFile);
					SaveAsExcel suiteScript = new SaveAsExcel(n, currExcelFile);
					totalCnt += suiteScript.totalCnt;
					passCnt += suiteScript.passCnt;
					boolean currPass = suiteScript.passStatus;
					if (!currPass) {
						this.passStatus = false;
					}
					writeSuiteScript(n, suiteScript);
				}
				scriptStatus.put(suiteName, pass);
				writeSuiteEnd(suiteNode);
				log.debug("----- END suite ----");

			} else if (executionL.getLength() > 0) {
				// generate execution script html
				scriptNode = (Element) executionL.item(0);
				// Add line for Execution Script
				// boolean currPass =
				addScriptResults(scriptNode, true);
				// resultsFile.writeEndTestResults(scriptNode.getAttribute("name"), "TEMP",
				// true);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// Date currentDate = new Date();
		// CoreDateTimeFormat dateFormat = new CoreDateTimeFormat("M/d/yyyy");
		// String title = script + " Results " + dateFormat.format(currentDate);

		addSummaryTab();
	}

	/**
	 * Add a new row to signify the execution of a script within a Suite
	 *
	 * @param node       n
	 * @param resultFile results as excel
	 */
	private synchronized void writeSuiteScript(Node node, SaveAsExcel resultFile) {
		String name = node.getAttributes().getNamedItem(SaveAsXML.NAME_TAG).getTextContent();
		String startTime = node.getAttributes().getNamedItem(SaveAsXML.START_TIME_TAG).getTextContent();
		// String stopTime =
		// node.getAttributes().getNamedItem(SaveAsXML.END_TIME_TAG).getTextContent();
		String executionDuration = node.getAttributes().getNamedItem(SaveAsXML.DURATION_TAG).getTextContent();

		ExcelResultsRow newRow = new ExcelResultsRow();
		newRow.setTimeStamp(startTime);
		newRow.setEvent("SUITE SCRIPT");

		newRow.setScriptName(name);

		String passFail = resultFile.passStatus ? "PASS" : "FAIL";

		newRow.setOverallPassFail(passFail);
		int vpPassCnt = resultFile.passCnt;
		int vpCnt = resultFile.totalCnt;
		String vpInfo = TOTAL_VPs + vpCnt + "\n" + PASS_VPs + vpPassCnt;
		if (resultFile.exceptionCnt > 0) {
			vpInfo += "\n" + EXCEPTION_CNT + resultFile.exceptionCnt;
			exceptionCnt += resultFile.exceptionCnt;
		}
		newRow.setVpNote(vpInfo);
		newRow.setExecutionDuration(executionDuration);
		File currFile = new File(resultFile.fileName);
		// Make the note equal to a link to the screenshot
		newRow.setExecutionNote("Results Log: " + currFile);

		String fileLink = currFile.getAbsolutePath();
		fileLink = fileLink.replace("C:\\", "file:///");
		fileLink = fileLink.replace('\\', '/');
		fileLink = fileLink.replaceAll(" ", "%20");
		newRow.setExecutionNoteHyperlink(fileLink);
		writeNewRow(newRow, false);

	}

	/**
	 * Returns the pass/fail status for a script
	 *
	 * @param scriptName script
	 * @return boolean TRUE if passed, FALSE if not
	 */
	private boolean getStatus(String scriptName) {
		boolean pass = false;
		if (scriptStatus != null && scriptStatus.containsKey(scriptName)) {
			pass = scriptStatus.get(scriptName);
		}
		return pass;
	}

	/**
	 * Will look for an element with the given tagName, if it exists, then it will
	 * return the text value for it
	 *
	 * @param node    n
	 * @param tagName tag
	 * @return String value
	 */
	private static String getValue(Node node, String tagName) {
		Element element = (Element) node;

		String val = "";
		if (element.getElementsByTagName(tagName).item(0) != null) {
			val = element.getElementsByTagName(tagName).item(0).getTextContent();
		}

		return val;
	}

	/**
	 * Creates a new file for the specified filename/path
	 *
	 * @param fileName file
	 */
	private synchronized void createResultsFile(File fileName) {
		log.info("create new results file");
		// Workbook workbook = null;
		FileOutputStream fos = null;
		try {
			if (!fileName.exists()) {
				fileName.createNewFile();
			} else {
				fileName.delete();
				fileName.createNewFile();
				Thread.sleep(200);
			}
			// create new file
			workbook = new XSSFWorkbook();

			sheet = workbook.createSheet("Test Results");
			sheetIndex = workbook.getSheetIndex(sheet);

			fos = new FileOutputStream(fileName);
			workbook.write(fos);
			fos.close();
			fos = null;
			writeNewHeaderRow();

			// loadHeaderCache();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			if (workbook != null) {
				try {
					workbook.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	/**
	 * Sets the pass status variable to false and terminated early to true
	 */
	@SuppressWarnings("unused")
	private void failTestDidNotComplete() {
		passStatus = false;
		terminatedEarly = true;
	}

	/**
	 * Will make sure the columnMap has all of the values assigned
	 */
	private void populateColumnMap() {
		// Populate our columnMap
		columnMap.put(SCRIPTNAME_INDEX, SCRIPTNAME_COLUMN);
		columnMap.put(EVENT_INDEX, EVENT_COLUMN);
		columnMap.put(TIMESTAMP_INDEX, TIMESTAMP_COLUMN);
		columnMap.put(VP_NAME_INDEX, VP_NAME_COLUMN);
		columnMap.put(VP_PASSFAIL_INDEX, VP_PASSFAIL_COLUMN);
		columnMap.put(VP_NOTE_INDEX, VP_NOTE_COLUMN);
		columnMap.put(OVERALL_PASSFAIL_INDEX, OVERALL_PASSFAIL_COLUMN);
		columnMap.put(EXECUTION_TIME_INDEX, EXECUTION_TIME_COLUMN);
		columnMap.put(EXECUTION_NOTE_INDEX, EXECUTION_NOTE_COLUMN);

	}

	/**
	 * This will return a boolean value indicating the over-all pass/fail status of
	 * the Script Results after adding result rows for the script and all additional
	 * result rows for child events of the associated Node.
	 *
	 * Note: this will create all the table rows to represent the given script. It
	 * will create rows for every verification point, and called script (along with
	 * the events that the called script generated as well).
	 *
	 * @param scriptNode        a reference to the current Script Node in the DOM
	 * @param isExecutionScript boolean value indicating whether this is an
	 *                          Execution Script
	 * @return boolean the over-all pass/fail status of the script and associated
	 *         child events (results)
	 */
	private boolean addScriptResults(Node scriptNode, boolean isExecutionScript) {
		boolean pass = true;

		String name = scriptNode.getAttributes().getNamedItem(SaveAsXML.NAME_TAG).getTextContent();
		log.debug("NAME=" + name);
		// default status to PASS, and failures will update this to FAIL
		scriptStatus.put(name, true);

		NamedNodeMap atts = scriptNode.getAttributes();
		for (int i = 0; i < atts.getLength(); i++) {
			log.debug("att" + i + ": " + atts.item(i).getNodeName() + "=" + atts.item(i).getNodeValue());
		}
		String startTime = scriptNode.getAttributes().getNamedItem(SaveAsXML.START_TIME_TAG).getTextContent();
		String stopTime = scriptNode.getAttributes().getNamedItem(SaveAsXML.END_TIME_TAG).getTextContent();
		String executionDuration = scriptNode.getAttributes().getNamedItem(SaveAsXML.DURATION_TAG).getTextContent();

		writeTestResultsStart(name, startTime, "CALL SCRIPT, STARTING TEST SCRIPT", false);

		boolean childrenPass = addChildEvents(scriptNode);

		String overallPass = "FAIL";
		if (childrenPass && getStatus(name)) {
			overallPass = "PASS";
		} else {
			pass = false;
		}
		writeTestResultsEnd(name, stopTime, overallPass, executionDuration, "", isExecutionScript);

		return pass;

	}

	/**
	 * This will return a boolean value indicating the over-all pass/fail status of
	 * child events (results) after adding the associated child events to the passed
	 * in Node.
	 *
	 * Note: This will create the table rows for all the events (vps, and called
	 * scripts) that occur for the given object.
	 *
	 * @param currNode the current node
	 * @return boolean the over-all pass/fail status of associated child events
	 *         (results)
	 */
	private boolean addChildEvents(Node currNode) {

		String currName = currNode.getAttributes().getNamedItem(SaveAsXML.NAME_TAG).getTextContent();
		log.debug("*** ADD CHILD EVENTS FOR " + currName);
		boolean pass = true;
		NodeList allElements = currNode.getChildNodes();

		for (int temp = 0; temp < allElements.getLength(); temp++) {

			Node currChildElem = allElements.item(temp);
			String currChildName = currChildElem.getAttributes().getNamedItem(SaveAsXML.NAME_TAG).getTextContent();

			if (currChildElem != null && currChildElem.getNodeType() == Node.ELEMENT_NODE) {

				switch (currChildElem.getNodeName()) {

				case SaveAsXML.EXECUTION_SCRIPT_TAG:

				case SaveAsXML.CALLED_SCRIPT_TAG:
					addScriptResults(currChildElem, currChildElem.getNodeName().equals(SaveAsXML.EXECUTION_SCRIPT_TAG));

					// check if the child event failed, if so this overall should fail as well
					log.debug("scriptName=" + currChildName + ", scriptStatus = " + scriptStatus);
					boolean childPass = getStatus(currChildName);
					if (!childPass) {
						log.debug("NODE NAME=" + currName);
						scriptStatus.put(currName, false);
						pass = false;
					}
					break;
				case SaveAsXML.VP_TAG:
					totalCnt++;
					boolean passFail = writeVP((Element) currChildElem);
					if (passFail) {
						currPassCnt++;
					} else {
						scriptStatus.put(getValue(currChildElem, SaveAsXML.VP_SCRIPT_NAME), false);
						// check if the child event failed, if so this overall should fail as well
						scriptStatus.put(getValue(currNode, SaveAsXML.VP_SCRIPT_NAME), false);
						pass = false;
					}
					break;
				case SaveAsXML.NOTE_TAG:
					boolean exception = writeNote(currChildElem);

					if (exception) {
						pass = false;
						exceptionCnt++;
						scriptStatus.put(getValue(currChildElem, SaveAsXML.VP_SCRIPT_NAME), false);
						// check if the child event failed, if so this overall should fail as well
						scriptStatus.put(getValue(currNode, SaveAsXML.VP_SCRIPT_NAME), false);
					}
					break;

				}
			}
		}
		log.debug(">>> END ADD CHILD EVENTS " + currName + "  PASS?" + pass);
		return pass;

	}

	// /**
	// * Write an Exception to the results file
	// *
	// * @param note
	// * - contains info on the exception to be written in the Note column
	// */
	// private synchronized void writeException(String note) {
	// Calendar cal = Calendar.getInstance();
	// SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
	// String date = sdf.format(cal.getTime());
	// ExcelResultsRow newRow = new ExcelResultsRow();
	//
	// newRow.setEvent("EXCEPTION");
	//
	// newRow.setTimeStamp(date);
	// newRow.setExecutionNote(note);
	//
	// writeNewRow(newRow, false);
	// }

	/**
	 * Add the header row to the beginning of a new file
	 */
	private synchronized void writeNewHeaderRow() {
		log.debug("add new header row");
		try {
			FileInputStream fis = new FileInputStream(fileName);
			Workbook workbook = getWorkbook(fis);
			Sheet sheet = workbook.getSheetAt(sheetIndex);
			fis.close();

			int newRowNumber = 0;

			Row newRow = sheet.createRow(newRowNumber);

			CellStyle style = workbook.createCellStyle();
			style.setFillPattern(FillPatternType.FINE_DOTS);
			style.setFillBackgroundColor(IndexedColors.DARK_BLUE.getIndex());
			style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
			style.setAlignment(HorizontalAlignment.CENTER);

			Font my_font = workbook.createFont();
			/* set the weight of the font */
			my_font.setBold(true);
			/* Also make the font color to WHITE */
			my_font.setColor(IndexedColors.WHITE.getIndex());
			/* attach the font to the style created earlier */
			style.setFont(my_font);

			for (int i = 0; i < COLUMN_COUNT; i++) {
				Cell cell = newRow.createCell(i);
				updateCell(workbook, sheet, newRowNumber, i, columnMap.get(i));
				cell.setCellStyle(style);
				sheet.autoSizeColumn(i);
			}

			newRow.setRowStyle(style);

			// Lock the header row
			sheet.createFreezePane(0, 1);

			FileOutputStream fos = new FileOutputStream(fileName);

			workbook.write(fos);

			fos.close();

			style = null;
			my_font = null;
			newRow = null;
			System.gc();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {

		}

	}

	/**
	 * Write an new row marking the start or end of execution of a test script
	 *
	 * @param row            a reference to the ExcelResultsRow
	 * @param isCalledScript boolean value indicating whether this is a Called
	 *                       Script
	 * @param autoSize       boolean value used to indicate whether the spreadsheet
	 *                       columns should be auto-sized
	 */
	private synchronized void writeNewStartEndRow(ExcelResultsRow row, boolean isCalledScript, boolean autoSize) {
		writeNewStartEndRow(excelFile, fileName, row, isCalledScript, autoSize);
	}

	/**
	 * Write an new row marking the start or end of execution of a test script
	 *
	 * @param currResultsFile a reference to the current Results File
	 * @param fileName        the Results File Name
	 * @param row             a reference to the ExcelResultsRow
	 * @param isCalledScript  boolean value indicating whether this is a Called
	 *                        Script
	 * @param autoSize        boolean value used to indicate whether the spreadsheet
	 *                        columns should be auto-sized
	 */
	private synchronized void writeNewStartEndRow(File currResultsFile, String fileName, ExcelResultsRow row,
			boolean isCalledScript, boolean autoSize) {

		File lockFile = null;
		try {
			lockFile = createLockFile(fileName);
			FileInputStream fis = new FileInputStream(fileName);
			Workbook workbook = getWorkbook(fis);
			Sheet sheet = workbook.getSheetAt(sheetIndex);
			fis.close();

			int newRowNumber = sheet.getLastRowNum() + 1;

			CellStyle style = workbook.createCellStyle();
			CellStyle currentStyle = style; // noStyle;

			// write
			Row newRow = sheet.createRow(newRowNumber);

			short fillForeground = IndexedColors.WHITE.getIndex();
			short fillBackground = IndexedColors.WHITE.getIndex();

			if (isCalledScript) {
				fillForeground = IndexedColors.GREY_40_PERCENT.getIndex();
			} else {
				if (row.getEvent().equalsIgnoreCase("START")) {
					// E0FFFF
					fillForeground = IndexedColors.LIGHT_TURQUOISE.getIndex(); // AQUA
					fillBackground = IndexedColors.LIGHT_TURQUOISE.getIndex();
				} else {
					fillForeground = IndexedColors.GREY_40_PERCENT.getIndex();
					fillBackground = IndexedColors.GREY_25_PERCENT.getIndex();
				}
			}
			if (row.getEvent().equalsIgnoreCase("END")) {
				fillForeground = IndexedColors.PALE_BLUE.getIndex();
				fillBackground = IndexedColors.PALE_BLUE.getIndex();
			}

			style.setFillForegroundColor(fillForeground);
			style.setFillBackgroundColor(fillBackground);

			style.setAlignment(HorizontalAlignment.LEFT);

			style.setFillPattern(FillPatternType.FINE_DOTS);
			newRow.setRowStyle(style);
			currentStyle = style;

			Font row_font = workbook.createFont();
			/* set the weight of the font */
			row_font.setBold(true);
			/* attach the font to the style created earlier */
			style.setFont(row_font);

			// Add formatting if the event is an exception
			CellStyle eventStyle = currentStyle; // workbook.createCellStyle();
			eventStyle.setAlignment(HorizontalAlignment.CENTER);

			updateCell(workbook, sheet, newRowNumber, newRow, SCRIPTNAME_INDEX, row.getScriptName(), currentStyle);
			updateCell(workbook, sheet, newRowNumber, newRow, EVENT_INDEX, row.getEvent(), eventStyle);
			updateCell(workbook, sheet, newRowNumber, newRow, TIMESTAMP_INDEX, row.getTimeStamp(), currentStyle);
			updateCell(workbook, sheet, newRowNumber, newRow, VP_NAME_INDEX, row.getVpName(), currentStyle);

			CellStyle noteStyle = workbook.createCellStyle();
			noteStyle.setFillForegroundColor(fillForeground);
			noteStyle.setFillBackgroundColor(fillBackground);
			noteStyle.setFillPattern(FillPatternType.FINE_DOTS);

			noteStyle.setWrapText(true);

			updateCell(workbook, sheet, newRowNumber, newRow, VP_NOTE_INDEX, row.getVpNote(), noteStyle);

			// Add red/green background formatting to this cell
			CellStyle overallPFStyle = workbook.createCellStyle();

			if (!isCalledScript && row.getEvent().equals("END")
					&& row.getOverallPassFail().toUpperCase().equals("PASS")) {
				overallPFStyle.setFillBackgroundColor(IndexedColors.GREEN.getIndex());

				overallPFStyle.setFillForegroundColor(IndexedColors.GREEN.getIndex());

				overallPFStyle.setFillPattern(FillPatternType.FINE_DOTS);
				overallPFStyle.setAlignment(HorizontalAlignment.CENTER);

			}

			else if (!isCalledScript && row.getEvent().equals("END")
					&& row.getOverallPassFail().toUpperCase().equals("FAIL")) {
				overallPFStyle.setFillBackgroundColor(IndexedColors.RED.getIndex());
				overallPFStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
				overallPFStyle.setFillPattern(FillPatternType.FINE_DOTS);

			} else {
				overallPFStyle = currentStyle;
			}
			overallPFStyle.setAlignment(HorizontalAlignment.CENTER);

			updateCell(workbook, sheet, newRowNumber, newRow, OVERALL_PASSFAIL_INDEX, row.getOverallPassFail(),
					overallPFStyle);

			String executionTime = "";
			if (!row.getExecutionDuration().isEmpty()) {
				executionTime = row.getExecutionDuration();
			}

			updateCell(workbook, sheet, newRowNumber, newRow, EXECUTION_TIME_INDEX, executionTime, currentStyle);

			if (row.getExecutionNoteHyperlink() != "") {
				CreationHelper createHelper = workbook.getCreationHelper();
				Hyperlink url_link = createHelper.createHyperlink(HyperlinkType.FILE);
				url_link.setAddress(row.getExecutionNoteHyperlink());

				Font my_font = workbook.createFont();
				/* set the weight of the font */
				my_font.setBold(true);
				/* Also make the font color to BLUE */
				my_font.setColor(IndexedColors.BLUE.getIndex());
				/* attach the font to the style created earlier */
				noteStyle.setFont(my_font);

				updateCell(workbook, sheet, newRowNumber, newRow, EXECUTION_NOTE_INDEX, row.getExecutionNote(),
						noteStyle, url_link);
			} else {
				updateCell(workbook, sheet, newRowNumber, newRow, EXECUTION_NOTE_INDEX, row.getExecutionNote(),
						noteStyle);
			}

			if (autoSize) {
				autoSize(sheet);
			}

			if (!isCalledScript && row.getEvent().equals("END")) {
				CellStyle breakstyle = workbook.createCellStyle();

				breakstyle.setFillBackgroundColor(IndexedColors.BLUE_GREY.getIndex());
				breakstyle.setFillPattern(FillPatternType.FINE_DOTS);

				Row newRowBreak = sheet.createRow(newRowNumber + 1);
				newRowBreak.setRowStyle(breakstyle);
				breakstyle = null;
			}

			FileOutputStream fos = new FileOutputStream(fileName);
			workbook.write(fos);

			fos.close();

			lockFile.delete();
			lockFile = null;
			fos = null;
			workbook = null;
			style = null;
			currentStyle = null;
			overallPFStyle = null;
			newRow = null;

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (lockFile != null) {
				lockFile.delete();
			}
		}
	}

	/**
	 * Write an entire new row to the spreadsheet. Will add formatting.
	 *
	 * @param row      a reference to the ExcelResultsRow
	 * @param autoSize boolean value used to indicate whether the spreadsheet
	 *                 columns should be auto-sized
	 */
	private synchronized void writeNewRow(ExcelResultsRow row, boolean autoSize) {
		try {
			FileInputStream fis = new FileInputStream(fileName);
			Workbook workbook = getWorkbook(fis);
			Sheet sheet = workbook.getSheetAt(sheetIndex);
			fis.close();

			int newRowNumber = sheet.getLastRowNum() + 1;
			CellStyle currentStyle = workbook.createCellStyle();

			// write
			Row newRow = sheet.createRow(newRowNumber);

			// No cellStyle, so pass in null
			updateCell(workbook, sheet, newRowNumber, newRow, SCRIPTNAME_INDEX, row.getScriptName(), currentStyle);

			// Add formatting if the event is an exception
			CellStyle eventStyle = workbook.createCellStyle();
			eventStyle.setAlignment(HorizontalAlignment.CENTER);
			if (row.getEvent().equalsIgnoreCase("SUITE SCRIPT")) {

				String vpNote = row.getVpNote();
				XSSFRichTextString richString = new XSSFRichTextString(vpNote);

				// Set up fonts
				Font blueFont = workbook.createFont();
				blueFont.setColor(HSSFColor.HSSFColorPredefined.BLUE.getIndex());
				blueFont.setBold(true);
				if (vpNote.contains(TOTAL_VPs)) {
					// Turn the word EXPECTED blue
					richString.applyFont(0, TOTAL_VPs.length(), blueFont);
					// turn the word ACTUAL blue
					int passIndex = vpNote.indexOf(PASS_VPs);
					if (passIndex > 0) {
						richString.applyFont(passIndex, passIndex + PASS_VPs.length(), blueFont);
					}
					int exceptionIndex = vpNote.indexOf(EXCEPTION_CNT);
					if (exceptionIndex > 0) {
						Font redFont = workbook.createFont();
						redFont.setColor(HSSFColor.HSSFColorPredefined.RED.getIndex());
						redFont.setBold(true);

						richString.applyFont(exceptionIndex, exceptionIndex + EXCEPTION_CNT.length(), redFont);
					}
					CellStyle noteStyle = workbook.createCellStyle();
					noteStyle.setWrapText(true);

					updateCell(workbook, sheet, newRowNumber, newRow, VP_NOTE_INDEX, richString, noteStyle);

				}
			}

			if (row.getEvent().equalsIgnoreCase("EXCEPTION")) {
				Font my_font = workbook.createFont();
				/* set the weight of the font */
				my_font.setBold(true);
				/* Also make the font color to RED */
				my_font.setColor(IndexedColors.RED.getIndex());
				/* attach the font to the style created earlier */
				eventStyle.setFont(my_font);

			}
			updateCell(workbook, sheet, newRowNumber, newRow, EVENT_INDEX, row.getEvent(), eventStyle);
			updateCell(workbook, sheet, newRowNumber, newRow, TIMESTAMP_INDEX, row.getTimeStamp(), currentStyle);
			updateCell(workbook, sheet, newRowNumber, newRow, VP_NAME_INDEX, row.getVpName(), currentStyle);

			// Add red/green font formatting to this cell
			CellStyle vpPFStyle = workbook.createCellStyle();
			if (row.getVpPassFail().toUpperCase().equals("PASS")) {
				Font my_font = workbook.createFont();
				/* set the weight of the font */
				my_font.setBold(true);
				/* Also make the font color to GREEN */
				my_font.setColor(IndexedColors.GREEN.getIndex());
				/* attach the font to the style created earlier */
				vpPFStyle.setFont(my_font);
			} else if (row.getVpPassFail().toUpperCase().equals("FAIL")) {
				Font my_font = workbook.createFont();
				/* set the weight of the font */
				my_font.setBold(true);
				/* Also make the font color to RED */
				my_font.setColor(IndexedColors.RED.getIndex());
				/* attach the font to the style created earlier */
				vpPFStyle.setFont(my_font);

			}
			updateCell(workbook, sheet, newRowNumber, newRow, VP_PASSFAIL_INDEX, row.getVpPassFail(), vpPFStyle);

			CellStyle noteStyle = workbook.createCellStyle();
			noteStyle.setWrapText(true);

			if (row.getEvent().equals("VP")) {
				String vpNote = row.getVpNote();
				XSSFRichTextString richString = new XSSFRichTextString(vpNote);

				if (vpNote.startsWith("EXPECTED")) {
					// Set up fonts
					Font blueFont = workbook.createFont();
					blueFont.setColor(HSSFColor.HSSFColorPredefined.BLUE.getIndex());
					blueFont.setBold(true);
					// Turn the word EXPECTED blue
					richString.applyFont(0, 8, blueFont);
					// turn the word ACTUAL blue
					int actualIndex = vpNote.indexOf("ACTUAL:");
					if (actualIndex > 0) {
						richString.applyFont(actualIndex, actualIndex + 7, blueFont);
					}
					if (vpNote.contains("REQUIREMENT: ")) {
						// Set up fonts
						Font purpleFont = workbook.createFont();
						purpleFont.setColor(HSSFColor.HSSFColorPredefined.VIOLET.getIndex());
						purpleFont.setBold(true);
						// turn the word REQUIREMENT purple
						int requirementIndex = vpNote.indexOf("REQUIREMENT:");
						if (requirementIndex > 0) {
							richString.applyFont(requirementIndex, requirementIndex + 12, purpleFont);
						}
					}
				}

				updateCell(workbook, sheet, newRowNumber, newRow, VP_NOTE_INDEX, richString, noteStyle);

			}

			// Add red/green background formatting to this cell
			CellStyle overallPFStyle = workbook.createCellStyle();

			if (row.getOverallPassFail().toUpperCase().equals("PASS")) {
				// if (row.getEvent().equals("SUITE SCRIPT")){
				Font my_font = workbook.createFont();
				/* set the weight of the font */
				my_font.setBold(true);
				/* Also make the font color to GREEN */
				my_font.setColor(IndexedColors.GREEN.getIndex());
				/* attach the font to the style created earlier */
				overallPFStyle.setFont(my_font);
				overallPFStyle.setAlignment(HorizontalAlignment.CENTER);

			} else if (row.getOverallPassFail().toUpperCase().equals("FAIL")) {
				// if (row.getEvent().equals("SUITE SCRIPT")){
				Font my_font = workbook.createFont();
				/* set the weight of the font */
				my_font.setBold(true);
				/* Also make the font color to RED */
				my_font.setColor(IndexedColors.RED.getIndex());
				/* attach the font to the style created earlier */
				overallPFStyle.setFont(my_font);

				overallPFStyle.setAlignment(HorizontalAlignment.CENTER);
			}
			updateCell(workbook, sheet, newRowNumber, newRow, OVERALL_PASSFAIL_INDEX, row.getOverallPassFail(),
					overallPFStyle);

			String executionTime = "";
			if (!row.getExecutionDuration().isEmpty()) {
				executionTime = row.getExecutionDuration();
			}

			updateCell(workbook, sheet, newRowNumber, newRow, EXECUTION_TIME_INDEX, executionTime, currentStyle);

			if (row.getExecutionNoteHyperlink() != "") {
				CreationHelper createHelper = workbook.getCreationHelper();
				Hyperlink url_link = createHelper.createHyperlink(HyperlinkType.FILE);
				url_link.setAddress(row.getExecutionNoteHyperlink());

				Font my_font = workbook.createFont();
				/* set the weight of the font */
				my_font.setBold(true);
				/* Also make the font color to BLUE */
				my_font.setColor(IndexedColors.BLUE.getIndex());
				/* attach the font to the style created earlier */
				noteStyle.setFont(my_font);

				updateCell(workbook, sheet, newRowNumber, newRow, EXECUTION_NOTE_INDEX, row.getExecutionNote(),
						noteStyle, url_link);
			} else {
				updateCell(workbook, sheet, newRowNumber, newRow, EXECUTION_NOTE_INDEX, row.getExecutionNote(),
						eventStyle);
			}

			if (autoSize) {
				autoSize(sheet);
			}

			if (row.isEndRow()) {
				CellStyle breakstyle = workbook.createCellStyle();

				breakstyle.setFillBackgroundColor(IndexedColors.BLUE_GREY.getIndex());
				breakstyle.setFillPattern(FillPatternType.FINE_DOTS);

				Row newRowBreak = sheet.createRow(newRowNumber + 1);
				newRowBreak.setRowStyle(breakstyle);
				breakstyle = null;
			}

			FileOutputStream fos = new FileOutputStream(fileName);
			workbook.write(fos);

			fos.close();

			fos = null;
			workbook = null;
			eventStyle = null;
			currentStyle = null;
			noteStyle = null;
			vpPFStyle = null;
			newRow = null;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
		}

	}

	/**
	 * Writes the Summary Row.
	 *
	 * @param label      the row Label
	 * @param value      the row Cell Value
	 * @param sheetIndex the Sheet Index
	 */
	private void writeSummaryRow(String label, String value, int sheetIndex) {

		try {
			FileInputStream fis = new FileInputStream(fileName);
			Workbook workbook = getWorkbook(fis);
			Sheet sheet = workbook.getSheetAt(sheetIndex);
			fis.close();

			int newRowNumber = sheet.getLastRowNum() + 1;

			CellStyle labelStyle = workbook.createCellStyle();
			Font boldFont = workbook.createFont();
			/* set the weight of the font */
			boldFont.setBold(true);

			labelStyle.setFont(boldFont);

			// write
			Row newRow = sheet.createRow(newRowNumber);

			// No cellStyle, so pass in null
			updateCell(workbook, sheet, newRowNumber, newRow, 0, label, labelStyle);

			CellStyle valueStyle = workbook.createCellStyle();
			updateCell(workbook, sheet, newRowNumber, newRow, 1, value, valueStyle);

			sheet.autoSizeColumn(0);
			sheet.autoSizeColumn(1);

			FileOutputStream fos = new FileOutputStream(fileName);
			workbook.write(fos);

			fos.close();

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
		}

	}

	/**
	 * This will return a boolean value indicating the exception status of the
	 * Note/Exception Node after adding the row for a given Verification Point.
	 *
	 * @param noteNode a reference to the DOM Node which will contain the Note or
	 *                 Exception
	 * @return boolean a boolean value indication whether there was an exception
	 */
	private boolean writeNote(Node noteNode) {
		Boolean exception = Boolean.valueOf(
				((Element) noteNode).getElementsByTagName(SaveAsXML.NOTE_EXCEPTION_TAG).item(0).getTextContent());
		String event = "NOTE";
		if (exception) {
			event = "EXCEPTION";
		}
		String timestamp = getValue(noteNode, SaveAsXML.TIMESTAMP_TAG);
		String note = getValue(noteNode, SaveAsXML.NAME_TAG);
		ExcelResultsRow newRow = new ExcelResultsRow();
		newRow.setEvent(event);
		newRow.setTimeStamp(timestamp);
		newRow.setExecutionNote(note);
		// if there was an exception, store it for the final status
		if (exception) {
			passStatus = false;
		}

		writeNewRow(newRow, false);
		return exception;
	}

	/**
	 * Add a new row to signify the Start of a new Suite execution
	 *
	 * @param suiteNode a reference to the DOM Node for the Suite Row
	 */
	private synchronized void writeSuiteStart(Element suiteNode) { // String suiteName, String note) {
		// String suiteName = suiteNode.getAttribute("name");
		String timestamp = getValue(suiteNode, SaveAsXML.TIMESTAMP_TAG);
		String note = getValue(suiteNode, SaveAsXML.NOTE_TAG);
		try {
			FileInputStream fis = new FileInputStream(fileName);
			Workbook workbook = getWorkbook(fis);
			Sheet sheet = workbook.getSheetAt(sheetIndex);
			fis.close();

			int newRowNumber = sheet.getLastRowNum() + 1;

			// write
			Row newRow = sheet.createRow(newRowNumber);

			CellStyle style = workbook.createCellStyle();
			style.setFillPattern(FillPatternType.FINE_DOTS);
			style.setFillBackgroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
			style.setFillForegroundColor(IndexedColors.WHITE.getIndex());
			// style.set
			style.setAlignment(HorizontalAlignment.CENTER);

			Font my_font = workbook.createFont();
			/* set the weight of the font */
			my_font.setBold(true);
			/* Also make the font color to BLACK */
			my_font.setColor(IndexedColors.BLACK.getIndex());
			/* attach the font to the style created earlier */
			style.setFont(my_font);

			for (int i = 0; i < COLUMN_COUNT; i++) {
				// Cell cell =
				newRow.createCell(i);
				String value = "";
				switch (i) {
				case SCRIPTNAME_INDEX:
					value = " Start Suite";
					break;
				case EXECUTION_NOTE_INDEX:
					value = note;
					break;
				case TIMESTAMP_INDEX:
					value = timestamp;
					break;
				case EVENT_INDEX:
					value = "SUITE START";
					break;
				}
				updateCell(workbook, sheet, newRowNumber, newRow, i, value, style);
				sheet.autoSizeColumn(i);
			}

			FileOutputStream fos = new FileOutputStream(fileName);

			workbook.write(fos);

			fos.close();

			fos = null;
			workbook = null;
			style = null;
			my_font = null;
			newRow = null;

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
		}
	}

	/**
	 * Add a new row to signify the End of a Suite execution
	 *
	 * @param suiteNode a reference to the DOM Node for the Suite Row
	 */
	private synchronized void writeSuiteEnd(Element suiteNode) {
		String suiteName = suiteNode.getAttribute("name");
		String timestamp = getValue(suiteNode, SaveAsXML.TIMESTAMP_TAG);
		String note = getValue(suiteNode, SaveAsXML.NOTE_TAG);
		boolean suitePass = getStatus(suiteName);

		// if there was a failure, store it for the final status
		if (!suitePass) {
			passStatus = false;
		}
		FileOutputStream fos = null;
		try {

			FileInputStream fis = new FileInputStream(fileName);
			Workbook workbook = getWorkbook(fis);
			Sheet sheet = workbook.getSheetAt(sheetIndex);
			fis.close();

			int newRowNumber = sheet.getLastRowNum() + 1;

			// write
			Row newRow = sheet.createRow(newRowNumber);

			CellStyle style = workbook.createCellStyle();
			style.setFillPattern(FillPatternType.FINE_DOTS);

			if (passStatus) {
				style.setFillBackgroundColor(IndexedColors.LIGHT_GREEN.getIndex());

				style.setFillForegroundColor(IndexedColors.GREEN.getIndex());
			} else {
				style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
				style.setFillBackgroundColor(IndexedColors.RED.getIndex());
			}

			style.setFillPattern(FillPatternType.FINE_DOTS);

			Font my_font = workbook.createFont();
			/* set the weight of the font */
			my_font.setBold(true);
			/* Also make the font color to BLACK */
			my_font.setColor(IndexedColors.BLACK.getIndex());
			/* attach the font to the style created earlier */
			style.setFont(my_font);

			for (int i = 0; i < COLUMN_COUNT; i++) {
				Cell cell = newRow.createCell(i);
				String value = "";
				switch (i) {
				case SCRIPTNAME_INDEX:
					value = "End Suite " + suiteName;
					break;
				case EXECUTION_NOTE_INDEX:
					value = note;
					break;
				case TIMESTAMP_INDEX:
					value = timestamp;
					break;
				case OVERALL_PASSFAIL_INDEX:
					value = passStatus ? "PASS" : "FAIL";
					break;
				case EVENT_INDEX:
					value = "SUITE END";
					break;
				}
				updateCell(workbook, sheet, newRowNumber, i, value);
				cell.setCellStyle(style);
				sheet.autoSizeColumn(i);
			}

			fos = new FileOutputStream(fileName);

			workbook.write(fos);

			fos.close();

			fos = null;
			workbook = null;
			style = null;
			my_font = null;
			newRow = null;

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	/**
	 * Adds a new row to the results file signifying the end of a scripts execution
	 *
	 * @param testName          the Test Name
	 * @param timestamp         the Test Timestamp
	 * @param passFail          the Overall Pass / Fail Status
	 * @param executionTime     the Duration Time
	 * @param note              the Notes
	 * @param isExecutionScript boolean value indicating whether this is an
	 *                          Execution Script
	 */
	private synchronized void writeTestResultsEnd(String testName, String timestamp, String passFail,
			String executionTime, String note, boolean isExecutionScript) {

		ExcelResultsRow newRow = new ExcelResultsRow();
		newRow.setTimeStamp(timestamp);
		newRow.setEvent("END");

		newRow.setScriptName(testName);
		newRow.setOverallPassFail(passFail);
		newRow.setExecutionDuration(executionTime);

		String executionNote = "TOTAL # VPs=" + String.valueOf(totalCnt) + ", " + " PASSED=" + String.valueOf(passCnt)
				+ ", " + " FAILED=" + String.valueOf(totalCnt - passCnt);

		if (exceptionCnt > 0) {
			String msg = " Exception thrown";
			if (exceptionCnt > 1) {
				msg = " Exceptions thrown";
			}
			executionNote += "\n" + exceptionCnt + msg;
		}
		if (!note.isEmpty()) {
			executionNote += "\n" + note;
		}

		newRow.setExecutionNote(executionNote);

		if (isExecutionScript) {
			newRow.setIsEndRow(true);
			writeNewStartEndRow(newRow, !isExecutionScript, true);
		} else {
			writeNewStartEndRow(newRow, !isExecutionScript, true);
		}
		if (isExecutionScript) {
			endWritten = true;
		}
	}

	/**
	 * Add a new row to signify the Start of a new script execution
	 *
	 * @param testName       the Test Name
	 * @param timestamp      the Test Timestamp
	 * @param note           the Notes
	 * @param isCalledScript boolean value indicating whether this is a Called
	 *                       Script
	 */
	private synchronized void writeTestResultsStart(String testName, String timestamp, String note,
			boolean isCalledScript) {
		ExcelResultsRow newRow = new ExcelResultsRow();
		newRow.setTimeStamp(timestamp);
		newRow.setEvent("START");

		newRow.setScriptName(testName);
		newRow.setExecutionNote(note);
		newRow.setIsStartRow(true);

		if (!isCalledScript) {
			writeNewStartEndRow(newRow, isCalledScript, false);
		} else {
			writeNewRow(newRow, false);
		}

		// make sure to reset the status
		passStatus = true;
		endWritten = false;

	}

	/**
	 * Will write the table row for the given verification point to the Excel file.
	 * Will return whether this VP passed or failed
	 *
	 * @param vpNode a reference to the VP Node
	 * @return boolean boolean value indicating the Pass / Fail status of the VP
	 */
	private boolean writeVP(Element vpNode) {

		Boolean pass = Boolean.valueOf(vpNode.getElementsByTagName(SaveAsXML.VP_PASS_TAG).item(0).getTextContent());
		if (pass) {
			passCnt++;
		}

		// Script Name Event Time Stamp VP Name VP PASS/FAIL VP Note Overall Pass/Fail
		// Total Time (s)
		// Execution Note
		String vpNote = "EXPECTED: " + getValue(vpNode, SaveAsXML.VP_EXPECTED_TAG) + "\nACTUAL: "
				+ getValue(vpNode, SaveAsXML.VP_ACTUAL_TAG);

		String passFail = pass ? "PASS" : "FAIL";
		String vpName = vpNode.getAttribute(SaveAsXML.NAME_TAG);
		String scriptName = getValue(vpNode, SaveAsXML.VP_SCRIPT_NAME);
		String timestamp = getValue(vpNode, SaveAsXML.TIMESTAMP_TAG);
		String note = getValue(vpNode, SaveAsXML.NOTE_TAG);

		ExcelResultsRow newRow = new ExcelResultsRow();
		newRow.setScriptName(scriptName);
		newRow.setTimeStamp(timestamp);
		newRow.setEvent("VP");
		newRow.setVpName(vpName);

		newRow.setVpPassFail(passFail);
		newRow.setVpNote(vpNote);

		newRow.setExecutionNote(note);

		writeNewRow(newRow, false);

		// if there was a failure, store it for the final status
		if (!passFail.equalsIgnoreCase("PASS")) {
			passStatus = false;
		}
		return pass;
	}

	/**
	 * Add a note to the Results File that contains a link to a screenshot file
	 *
	 * @param screenShotNote note to go with link to screenshot
	 * @param defaultLink    link
	 * @param date           timestamp
	 */
	public synchronized void writeScreenShot(String screenShotNote, String defaultLink, String date) {
		writeScreenShot("SCREENSHOT", screenShotNote, defaultLink, date);
	}

	/**
	 * Add a note to the Results File that contains a link to a screenshot file
	 *
	 * @param event          event
	 * @param screenShotNote note to go with link to screenshot
	 * @param defaultLink    default link
	 * @param date           timeStamp
	 */
	public synchronized void writeScreenShot(String event, String screenShotNote, String defaultLink, String date) {
		ExcelResultsRow newRow = new ExcelResultsRow();

		newRow.setEvent(event);
		newRow.setTimeStamp(date);

		// Make the note equal to a link to the screenshot
		newRow.setExecutionNote(screenShotNote);
		newRow.setExecutionNoteHyperlink(defaultLink);

		writeNewRow(newRow, false);
	}
}
