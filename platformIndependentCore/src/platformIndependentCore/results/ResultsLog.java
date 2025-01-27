package platformIndependentCore.results;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import platformIndependentCore.core.AutomationHelper;

/**
 * Interface to interact and generate a ResultsLog
 *
 * @author VBAAUSTAYLOL
 *
 */
public interface ResultsLog {

	/**
	 * Will open the specified file in the default desktop application
	 *
	 * @param file to open
	 */
	default void open(File file) {
		Desktop dt = Desktop.getDesktop();
		try {
			dt.open(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Will open an Excel log for the specified file name. If the Excel log does not
	 * exist, it will be generated from existing XML.
	 * <p>
	 * Excel log will be opened in the default Desktop application for .xlsx files
	 *
	 * @param fileName to open (with no file extension)
	 */
	static void openExcelLog(String fileName) {
		String xml = fileName + ".xml";
		String xlsx = fileName + ".xlsx";
		File excelFile = new File(xlsx);
		File xmlFile = new File(xml);

		if (!excelFile.exists() || excelFile.lastModified() < xmlFile.lastModified()) {

			// Create the Excel version of the log
			new SaveAsExcel(xmlFile, excelFile);
		}
		AutomationHelper.open(excelFile);
	}

	/**
	 * Will open an HTML log for the specified file name. If the HTML log does not
	 * exist, it will be generated from existing XML.
	 * <p>
	 * Excel log will be opened in the default Desktop application for .html files
	 *
	 * @param fileName to open (with no file extension)
	 */
	static void openHtmlLog(String fileName) {
		String html = fileName + ".html";
		String xml = fileName + ".xml";

		File htmlFile = new File(html);
		File xmlFile = new File(xml);

		File toOpen = htmlFile;

		if (!htmlFile.exists() || htmlFile.lastModified() < xmlFile.lastModified()) {
			toOpen = new ResultsLogHTML("", xmlFile, html).getHtmlResultsFile();
		}
		AutomationHelper.open(toOpen);
	}
}
