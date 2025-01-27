package platformIndependentCore.results;

import java.io.File;
import java.util.HashMap;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import platformIndependentCore.core.AutomationHelper;
import platformIndependentCore.datafiles.ExcelFileBase;

public class ResultsLogExcel extends ExcelFileBase implements ResultsLog {

	public static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";

	// // Text Values for the Column Headers in the results file
	// private static final String SCRIPTNAME_COLUMN = "Script Name";
	// private static final String EVENT_COLUMN = "Event";
	// private static final String TIMESTAMP_COLUMN = "Time Stamp";
	// private static final String VP_NAME_COLUMN = "VP Name";
	// private static final String VP_PASSFAIL_COLUMN = "VP PASS/FAIL";
	// private static final String VP_NOTE_COLUMN = "VP Note";
	// private static final String OVERALL_PASSFAIL_COLUMN = "Overall Pass/Fail";
	// private static final String EXECUTION_TIME_COLUMN = "Total Time (s)";
	// private static final String EXECUTION_NOTE_COLUMN = "Execution Note";
	//
	// // Index values for the columns that correlate to the columns position in
	// // the spreadsheet
	// private static final int SCRIPTNAME_INDEX = 0;
	// private static final int EVENT_INDEX = 1;
	// private static final int TIMESTAMP_INDEX = 2;
	// private static final int VP_NAME_INDEX = 3;
	// private static final int VP_PASSFAIL_INDEX = 4;
	// private static final int VP_NOTE_INDEX = 5;
	// private static final int OVERALL_PASSFAIL_INDEX = 6;
	// private static final int EXECUTION_TIME_INDEX = 7;
	// private static final int EXECUTION_NOTE_INDEX = 8;

	static String TEST_RESULTS_SHEET_NAME = "Test Results";

	// // hashmap that lets us correlate the column text with its index
	// private static final HashMap<Integer, String> columnMap = new
	// HashMap<Integer, String>();
	//
	// private static final int COLUMN_COUNT = 9;
	HashMap<String, Boolean> scriptStatus = new HashMap<String, Boolean>();

	int passCnt = 0;
	// These Two values are used to keep a status on the current script
	// execution
	// private boolean passStatus = true;
	boolean endWritten = false;
	boolean terminatedEarly = false;

	Workbook workbook = null;
	Sheet sheet = null;
	File excelFile = null;
	String fileName = "";

	// SaveAsExcel resultsFile;

	int totalCnt = 0;
	int totalPassCnt = 0;
	int currPassCnt = 0;
	String exception = "";

	/**
	 * Constructor, takes in an exception, the xml file and the excel file
	 *
	 * @param exception
	 * @param fXmlFile
	 * @param htmlFile
	 */
	// ResultsLogExcel(String exception, File fXmlFile, File excelFile) {
	// super(excelFile.getAbsolutePath(), "Test Results", false);
	// // fileName = excelFile.getAbsolutePath();
	// // sheetIndex = 0;
	//
	// // createResultsFile(excelFile);
	//// resultsFile = new SaveAsExcel(excelFile.getAbsolutePath(), 0);
	//// saveAsExcel(exception, fXmlFile, excelFile);
	// this.excelFile = excelFile;
	// fileName = excelFile.getAbsolutePath();
	// }

	ResultsLogExcel(File excelFile) {
		super(excelFile.getAbsolutePath(), TEST_RESULTS_SHEET_NAME, false);
		// resultsFile = new SaveAsExcel(excelFile.getAbsolutePath(), 0);
		this.excelFile = excelFile;
		fileName = excelFile.getAbsolutePath();
	}

	public static ResultsLogExcel getExcelLog(String fileName) {
		String xml = fileName + ".xml";
		String xlsx = fileName + ".xlsx";
		ResultsLogExcel log;
		File excelFile = new File(xlsx);
		File xmlFile = new File(xml);
		if (!excelFile.exists() || excelFile.lastModified() < xmlFile.lastModified()) {
			// Create the Excel version of the log
			new SaveAsExcel(xmlFile, excelFile);
		}
		log = new ResultsLogExcel(excelFile);
		return log;
	}

	public static void main(String[] args) {
		String fileName = args[0];
		ResultsLogExcel log = getExcelLog(fileName);
		AutomationHelper.open(log.excelFile);

	}

}
