package platformIndependentCore.datafiles;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import platformIndependentCore.exceptions.WrappedException;

/**
 * Base Class for interacting with Excel files using POI
 *
 * @author vbaaustaylol
 * @author VBADESDunigR
 */
public abstract class ExcelFileBase {

	/** Sheet Index **/
	protected int sheetIndex = -1;
	/** Sheet Name **/
	protected String sheetName = "";
	/** File Name **/
	protected String fileName = "";
	/** Number of milliseconds in a minute (1000 * 60s) **/
	protected static final long ONE_MINUTE_IN_MILLIS = 60000;
	/** Current Row Index **/
	protected int currentRowIndex = -1;
	/** Current Row **/
	protected Row currentRow;
	/** Excel File Header Cache **/
	HashMap<String, Integer> headerCache = new HashMap<String, Integer>();

	/**
	 * Constructor
	 *
	 * @param filePath  to locate the file
	 * @param sheetName to specify the sheet within the Excel file
	 */
	public ExcelFileBase(String filePath, String sheetName) {
		fileName = filePath;
		this.sheetName = sheetName;

		// try to cache the header row for future use
		loadHeaderCache();

	}

	/**
	 * Constructor that allows control over generating a cache of header values.
	 * This is needed if custom code is required for calculating the header
	 *
	 * @param filePath   to locate the file
	 * @param sheetName  to specify the sheet within the Excel file
	 * @param loadHeader true if a cache should be created for header values, false
	 *                   if not
	 */
	public ExcelFileBase(String filePath, String sheetName, boolean loadHeader) {
		fileName = filePath;
		this.sheetName = sheetName;

		if (loadHeader) {
			// try to cache the header row for future use
			loadHeaderCache();
		}

	}

	/**
	 * Constructor
	 *
	 * @param filePath    to locate the file
	 * @param sheetNumber to specify the sheet within the Excel file
	 */
	public ExcelFileBase(String filePath, int sheetNumber) {
		fileName = filePath;
		sheetIndex = sheetNumber;

		// try to cache the header row for future use
		loadHeaderCache();

	}

	/**
	 * Private method to create a lock file, controlling write access to the
	 * spreadsheet that is being updated. Once this update is complete, the lock
	 * file needs to be deleted by the developer to release the file for future
	 * updates.
	 *
	 * @param fileToLock file that needs a lock created
	 * @return lockFile the lock file signaling file is in use
	 * @throws IOException Signals that an I/O exception of some sort has occurred
	 */
	protected synchronized File createLockFile(String fileToLock) throws IOException {

		String lockFilePath = fileToLock.replaceAll(".xlsx|.xls", ".lock");

		File lockFile = new File(lockFilePath);

		boolean locked = false;
		int tries = 0;

		// loop to try and get a lock file. Will time out if the file is already
		// locked (and not released)
		while (!locked && tries < 20) {
			if (lockFile.isFile()) {
				// TODO - verify sleep length. Was 5 seconds
				// Thread.sleep(5000);
				long lastModified = lockFile.lastModified();
				Calendar cal = Calendar.getInstance();
				// Date currDate = new Date();
				long currTime = cal.getTimeInMillis(); // currDate.getTime();

				// if a certain number of minutes has passed, try unlocking the
				// file. We shouldn't have to wait this long
				long expireTime = lastModified + 2 * ONE_MINUTE_IN_MILLIS;
				if (currTime > expireTime) {
					lockFile.delete();
					lockFile.createNewFile();
					locked = true;
					break;
				}
				tries++;
			} else {
				// Thread.sleep(200);
				lockFile.createNewFile();
				locked = true;
			}
		}
		if (!locked) {
			throw new IOException("Unable to gain write access to data file, it is currently in use.");
		}

		return lockFile;

	}

	/**
	 * Returns a Workbook instance for the file inputstream
	 *
	 * @param inputStream for spreadsheet file
	 * @return XSSFWorkbook workbook instance
	 * @throws IOException Signals that an I/O exception of some sort has occurred
	 */
	protected XSSFWorkbook getWorkbook(FileInputStream inputStream) throws IOException {
		XSSFWorkbook workbook = null;

		try {
			workbook = (XSSFWorkbook) WorkbookFactory.create(inputStream);
		} catch (EncryptedDocumentException e) {
			e.printStackTrace();
			throw new WrappedException("EncryptedDocumentException occurred accessing your Data File. " + e);
		} catch (IOException e) {
			e.printStackTrace();
			throw new WrappedException("IOException occurred accessing your Data File. " + e);
		}
		return workbook;
	}

	/**
	 * Returns the value of the cell as a String. Defaults to an empty string if
	 * there is no value
	 *
	 * @param cell to find value of
	 * @return String value of the cell
	 * @throws IllegalStateException will throw exceptions if a cell type cannot be
	 *                               determined
	 */
	protected String getCellValue(Cell cell) {
		String data = "";
		if (cell != null) {
			// Handle based on cell type
			switch (cell.getCellType()) {
			case NUMERIC:
				// getNumericCellValue assumes parsable double cell value, throws exceptions
				// otherwise
				data = String.valueOf(Double.valueOf(cell.getNumericCellValue()));
				break;
			case STRING:
			case BLANK:
			case ERROR:
				// Will return String in case of STRING or ERROR, and an empty String in case of
				// BLANK
				data = cell.getStringCellValue();
				break;
			case FORMULA:
				// Will try, but potentially throw an exception for non-string formulas
				try {
					data = cell.getStringCellValue();
				} catch (IllegalStateException e) {
					data = cell.getCellFormula().toString();
				}
				break;
			case BOOLEAN:
				data = String.valueOf(cell.getBooleanCellValue());
				break;
			case _NONE:
			default:
				throw new IllegalStateException("IllegalStateException: Cell does not have a known cell type.");
			}
		}
		return data;

	}

	/**
	 * Allows a user to see if a specified column exists in the Excel file.
	 *
	 * @param columnHeader to verify
	 * @return boolean true if columnHeader is found, false if not
	 */
	public boolean isColumnHeaderPresent(String columnHeader) {
		boolean result = false;
		if (headerCache.get(columnHeader) != null) {
			result = true;
		}
		return result;
	}

	/**
	 * Returns the int index that matches the specified column text
	 *
	 * @param columnName to locate cell in row
	 * @return int column index for specified column name
	 * @throws IOException Signals that an I/O exception of some sort has occurred
	 */
	protected int getColumnIndex(String columnName) throws IOException {
		if (headerCache.get(columnName) == null) {
			throw new IOException("Unable find column matching: " + columnName);
		}
		return headerCache.get(columnName);
	}

	/**
	 * Will return the File object for this Excel File
	 *
	 * @return File object for the excel file
	 */
	public File getFile() {
		return new File(fileName);
	}

	/**
	 * Returns the string value of the cell in the specified record
	 * (dataRecordIndex) from the specified column (columnName). Defaults to an
	 * empty string if there is no value
	 *
	 * @param dataRecordIndex integer to identify the row index
	 * @param columnName      to locate cell in row
	 * @return String value of specified cell
	 */
	public synchronized String getData(int dataRecordIndex, String columnName) {
		String data = "";
		int tries = 0;
		try {

			data = getDataValue(dataRecordIndex, columnName, tries);

		} catch (InvalidFormatException ife) {
			// TODO LMT RationalTestScript.logWarning("InvalidFormatException for "
			// + columnName + " in row " + dataRecordIndex + ".");
			ife.printStackTrace();
			throw new RuntimeException(ife);
		}

		return data;

	}

	/**
	 * Internal/Private method that will try to get data from the cell. If the get
	 * fails due to an IOException the method will issue a sleep and use recursion
	 * to try again. It will only retry 3 times before throwing the IOException.
	 * This is needed because sometimes a previous POI operation may not have
	 * completed before this call is executed. We sleep to give that a chance to
	 * release the file.
	 *
	 *
	 * @param dataRecordIndex integer to identify the row index
	 * @param columnName      to locate cell in row
	 * @param numberOfTries   current count for number of times we've tried this
	 * @return String value of specified cell
	 * @throws InvalidFormatException an exception occurred opening the file
	 */
	synchronized String getDataValue(int dataRecordIndex, String columnName, int numberOfTries)
			throws InvalidFormatException {
		String data = "";
		Row currentRow = null;
		Cell cell = null;
		OPCPackage opcPackage = null;
		XSSFWorkbook workbook = null;

		try {
			File file = new File(fileName);
			opcPackage = OPCPackage.open(file.getAbsolutePath());

			if (fileName.toLowerCase().endsWith("xlsx")) {
				workbook = new XSSFWorkbook(opcPackage);
			}

			Sheet sheet = workbook.getSheetAt(sheetIndex);
			currentRow = sheet.getRow(dataRecordIndex);

			int column = getColumnIndex(columnName);

			if (currentRow != null) {
				cell = currentRow.getCell(column);
				data = getCellValue(cell);

				cell = null;
				currentRow = null;
			}
		} catch (IOException ioe) {
			// close the workbook and opcPackage before calling again and opening a new one
			if (workbook != null) {
				try {
					workbook.close();
				} catch (IOException e) {
					e.printStackTrace();
					// LMT TODO RationalTestScript
					// .logWarning("ExcelFileBase:getDataValue - IOException trying to close
					// workbook for "
					// + columnName
					// + " in row "
					// + dataRecordIndex
					// + ".");

				}
			}

			// close the opcPackage before calling again and opening a new one
			else if (opcPackage != null) {
				try {
					if (opcPackage.validatePackage(opcPackage)) {
						opcPackage.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
					// RationalTestScript
					// .logWarning("ExcelFileBase:getDataValue - IOException trying to close
					// opcPackage for "
					// + columnName
					// + " in row "
					// + dataRecordIndex
					// + ".");

				} catch (InvalidFormatException e) {
					// RationalTestScript
					// .logWarning("ExcelFileBase:getDataValue - InvalidFormatException trying to to
					// validate
					// opcPackage for "
					// + columnName
					// + " in row "
					// + dataRecordIndex
					// + ".");
					e.printStackTrace();
				}
				opcPackage = null;
			}
			// Just in case there is a race condition, sleep and try one more time
			if (numberOfTries < 2) {
				// RationalTestScript.sleep(0.2);
				data = getDataValue(dataRecordIndex, columnName, ++numberOfTries);
			} else {
				// RationalTestScript.logError("IOException reading data for"
				// + columnName + " in row " + dataRecordIndex + ".");

				ioe.printStackTrace();
				throw new RuntimeException(ioe);
			}
		} finally {
			// close the opcPackage before calling again and opening a new one
			if (workbook != null) {
				try {
					workbook.close();
				} catch (IOException e) {
					e.printStackTrace();
					// RationalTestScript
					// .logWarning("ExcelFileBase:getDataValue - IOException trying to close
					// workbook for "
					// + columnName
					// + " in row "
					// + dataRecordIndex
					// + ".");

				}
			}

			// close the opcPackage before calling again and opening a new one
			else if (opcPackage != null) {
				try {
					if (opcPackage.validatePackage(opcPackage)) {
						opcPackage.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
					// RationalTestScript
					// .logWarning("ExcelFileBase:getDataValue - IOException trying to close
					// workbook for "
					// + columnName
					// + " in row "
					// + dataRecordIndex
					// + ".");

				} catch (InvalidFormatException e) {
					// RationalTestScript
					// .logWarning("ExcelFileBase:getDataValue - InvalidFormatException trying to to
					// validate
					// opcPackage for "
					// + columnName
					// + " in row "
					// + dataRecordIndex
					// + ".");
					e.printStackTrace();
				}
			}
			cell = null;
			currentRow = null;
		}

		return data;

	}

	/**
	 * Read the header row of the file and save each cell index and value for
	 * quick/easy lookup for columns later
	 */
	protected void loadHeaderCache() {
		Workbook workbook = null;
		OPCPackage opcPackage = null;
		try {
			File file = new File(fileName);

			if (file.exists()) {
				if (file.canRead()) {
					opcPackage = OPCPackage.open(file.getAbsolutePath());

					workbook = new XSSFWorkbook(opcPackage);

					if (sheetIndex < 0) {
						sheetIndex = workbook.getSheetIndex(sheetName);
					}

					if (sheetIndex < 0) {
						// RationalTestScript
						// .logWarning("ERROR - Data File sheet name '" + sheetName +"' is not found for
						// "
						// + file.getAbsolutePath()
						// + " Please verify that the sheet name is valid.");
						throw new RuntimeException("ERROR - Data File sheet name '" + sheetName + "' is not found for "
								+ file.getAbsolutePath() + " Please verify that the sheet name is valid.");
					}

					Sheet sheet = workbook.getSheetAt(sheetIndex);
					Row header = sheet.getRow(0);

					if (header != null) {

						for (int i = 0; i < header.getLastCellNum(); i++) {
							headerCache.put(getCellValue(header.getCell(i)), i);
						}
					}

				} else {
					// RationalTestScript
					// .logWarning("ERROR - Data File "
					// + file.getAbsolutePath()
					// + " can not be read. Please verify no other applications have the file
					// open.");
					throw new RuntimeException("ERROR - Data File " + file.getAbsolutePath()
							+ " can not be read. Please verify no other applications have the file open.");

				}
			} else {
				// Only worry about whether file exists if it is a data file and not a results
				// or requirements file
				if (!(this instanceof platformIndependentCore.results.SaveAsExcel)
						&& !(this instanceof platformIndependentCore.datafiles.RequirementsDataFile)) {
					// RationalTestScript
					// .logWarning("ERROR - Data File "
					// + file.getAbsolutePath()
					// + " does not exist. Please check the file name and path and make sure the
					// .xlsx
					// extension is included.");
					throw new RuntimeException("ERROR - Data File " + file.getAbsolutePath()
							+ " does not exist. Please check the file name and path and make sure the .xlsx extension is included.");
				}
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
			// TODO LMT RationalTestScript.logWarning("IOException loading the File: "
			// + ioe.getMessage());

		} catch (InvalidFormatException e) {
			e.printStackTrace();
			// RationalTestScript
			// .logWarning("InvalidFormatException loading the File: "
			// + e.getMessage());

		} finally {
			// make sure everything is closed even in the event of an exception
			if (workbook != null) {
				try {
					workbook.close();
				} catch (IOException e) {
					e.printStackTrace();
					// RationalTestScript
					// .logWarning("ExcelFileBase:loadHeaderCache - IOException trying to close
					// workbook");

				}
			}

			// close the opcPackage before calling again and opening a new one
			else if (opcPackage != null) {
				try {
					if (opcPackage.validatePackage(opcPackage)) {
						opcPackage.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
					// RationalTestScript
					// .logWarning("ExcelFileBase:loadHeaderCache - IOException trying to close
					// opcPackage");

				} catch (InvalidFormatException e) {
					// RationalTestScript
					// .logWarning("ExcelFileBase:getDataValue - InvalidFormatException trying to to
					// validate
					// opcPackage");
					e.printStackTrace();
				}
			}
		}

	}

	/**
	 * Private method to update a single cell in a spreadsheet
	 *
	 * @param workbook        instance of spreadsheet
	 * @param sheet           within the workbook
	 * @param dataRecordIndex integer to identify the row index
	 * @param columnIndex     specifies column cell is located in
	 * @param dataToWrite     value to write to the cell
	 * @throws IOException Signals that an I/O exception of some sort has occurred
	 */
	protected synchronized void updateCell(Workbook workbook, Sheet sheet, int dataRecordIndex, int columnIndex,
			String dataToWrite) throws IOException {
		if (dataRecordIndex > -1 && columnIndex > -1) {

			Row row = sheet.getRow(dataRecordIndex);
			if (row == null) {
				row = sheet.createRow(dataRecordIndex);
			}

			// Now that I have the row,pass that in to the other method. No
			// cellStyle, so set to null
			this.updateCell(workbook, sheet, dataRecordIndex, row, columnIndex, dataToWrite, null);
		}
	}

	/**
	 * Private method to update a single cell in a spreadsheet
	 *
	 * @param workbook        instance of spreadsheet
	 * @param sheet           within the workbook
	 * @param dataRecordIndex integer to identify the row index
	 * @param currentRow      contains the desired cell
	 * @param columnIndex     specifies column cell is located in
	 * @param dataToWrite     value to write to the cell
	 * @param cellStyle       to apply to the cell
	 * @return Cell updated cell object
	 * @throws IOException Signals that an I/O exception of some sort has occurred
	 */
	protected synchronized Cell updateCell(Workbook workbook, Sheet sheet, int dataRecordIndex, Row currentRow,
			int columnIndex, String dataToWrite, CellStyle cellStyle) throws IOException {
		return updateCell(workbook, sheet, dataRecordIndex, currentRow, columnIndex, dataToWrite, cellStyle, null, 0);

	}

	/**
	 * Private method to update a single cell in a spreadsheet
	 *
	 * @param workbook        instance of spreadsheet
	 * @param sheet           within the workbook
	 * @param dataRecordIndex integer to identify the row index
	 * @param currentRow      contains the desired cell
	 * @param columnIndex     specifies column cell is located in
	 * @param dataToWrite     value to write to the cell
	 * @param cellStyle       to apply to the cell
	 * @return updated Cell object
	 * @throws IOException Signals that an I/O exception of some sort has occurred
	 */
	protected synchronized Cell updateCell(Workbook workbook, Sheet sheet, int dataRecordIndex, Row currentRow,
			int columnIndex, RichTextString dataToWrite, CellStyle cellStyle) throws IOException {
		return updateCell(workbook, sheet, dataRecordIndex, currentRow, columnIndex, dataToWrite, cellStyle, null, 0);

	}

	/**
	 * Protected method to update a single cell in a spreadsheet
	 *
	 * @param workbook        instance of spreadsheet
	 * @param sheet           within the workbook
	 * @param dataRecordIndex integer to identify the row index
	 * @param currentRow      contains the desired cell
	 * @param columnIndex     specifies column cell is located in
	 * @param dataToWrite     value to write to the cell
	 * @param cellStyle       to apply to the cell
	 * @param hyperLink       to add to the cell
	 * @return updated Cell object
	 * @throws IOException Signals that an I/O exception of some sort has occurred
	 */
	protected synchronized Cell updateCell(Workbook workbook, Sheet sheet, int dataRecordIndex, Row currentRow,
			int columnIndex, String dataToWrite, CellStyle cellStyle, Hyperlink hyperLink) throws IOException {
		return updateCell(workbook, sheet, dataRecordIndex, currentRow, columnIndex, dataToWrite, cellStyle, hyperLink,
				0);
	}

	/**
	 * Internal/Private method that will try to update the cell. If the update fails
	 * due to an IOException the method will issue a sleep and use recursion to try
	 * again. It will only retry 3 times before throwing the IOException. This is
	 * needed because sometimes a previous POI operation may not have completed
	 * before this call is executed. We sleep to give that a chance to release the
	 * file.
	 *
	 * @param workbook        instance of spreadsheet
	 * @param sheet           within the workbook
	 * @param dataRecordIndex integer to identify the row index
	 * @param currentRow      contains the desired cell
	 * @param columnIndex     specifies column cell is located in
	 * @param dataToWrite     value to write to the cell
	 * @param cellStyle       to apply to the cell
	 * @param hyperLink       to add to the cell
	 * @param numberOfTries   current count of number tries. Will try for a max of 3
	 * @return Cell updated cell object
	 * @throws IOException potentially throws an IOException if there are issues
	 *                     writing to or closing the workbook
	 */
	private synchronized Cell updateCell(Workbook workbook, Sheet sheet, int dataRecordIndex, Row currentRow,
			int columnIndex, String dataToWrite, CellStyle cellStyle, Hyperlink hyperLink, int numberOfTries)
			throws IOException {
		return updateCell(workbook, sheet, dataRecordIndex, currentRow, columnIndex,
				new XSSFRichTextString(dataToWrite), cellStyle, hyperLink, numberOfTries);

	}

	/**
	 * Internal/Private method that will try to update the cell. If the update fails
	 * due to an IOException the method will issue a sleep and use recursion to try
	 * again. It will only retry 3 times before throwing the IOException. This is
	 * needed because sometimes a previous POI operation may not have completed
	 * before this call is executed. We sleep to give that a chance to release the
	 * file.
	 *
	 * @param workbook        instance of spreadsheet
	 * @param sheet           within the workbook
	 * @param dataRecordIndex integer to identify the row index
	 * @param currentRow      contains the desired cell
	 * @param columnIndex     specifies column cell is located in
	 * @param dataToWrite     value to write to the cell
	 * @param cellStyle       to apply to the cell
	 * @param hyperLink       to add to the cell
	 * @param numberOfTries   current count of number tries. Will try for a max of 3
	 * @return Cell updated cell object
	 * @throws IOException potentially throws an IOException if there are issues
	 *                     writing to or closing the workbook
	 */
	private synchronized Cell updateCell(Workbook workbook, Sheet sheet, int dataRecordIndex, Row currentRow,
			int columnIndex, RichTextString dataToWrite, CellStyle cellStyle, Hyperlink hyperLink, int numberOfTries)
			throws IOException {
		Cell cell = currentRow.getCell(columnIndex);
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(fileName);
			// write
			if (cell == null) {
				cell = currentRow.createCell(columnIndex);
			}

			cell.setCellValue(dataToWrite);

			if (cellStyle != null) {
				cell.setCellStyle(cellStyle);
			}

			if (hyperLink != null) {
				cell.setHyperlink(hyperLink);
			}

			workbook.write(fos);

			fos.close();
			workbook = null;
			fos = null;
			cellStyle = null;
			cell = null;
		} catch (IOException ioe) {
			ioe.printStackTrace();
			throw new RuntimeException(ioe);
			// Just incase there is a race condition, sleep and try one more
			// time
			// if (numberOfTries < 2) {
			// //RationalTestScript.sleep(0.2);
			// updateCell(workbook, sheet, dataRecordIndex, currentRow,
			// columnIndex, dataToWrite, cellStyle, hyperLink,
			// numberOfTries++);
			// } else {
			//// RationalTestScript
			//// .logError("IOException updating cell for columnIndex index"
			//// + columnIndex
			//// + " in row "
			//// + dataRecordIndex
			//// + ".");
			//
			// ioe.printStackTrace();
			// throw new RuntimeException(ioe);
			// }
		} finally {
			if (fos != null) {
				try {
					fos.close();
					fos = null;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return cell;
	}

	/**
	 * Sets the value of the cell matching the specified dataRecordIndex and
	 * columnName to the specified value.
	 *
	 * @param dataRecordIndex integer to identify the row index
	 * @param columnName      to locate cell in row
	 * @param dataToWrite     value to write to the cell
	 */
	public void writeToDataSheet(int dataRecordIndex, String columnName, String dataToWrite) {
		File lockFile = null;
		try {
			lockFile = createLockFile(fileName);

			FileInputStream fis = new FileInputStream(fileName);

			Workbook workbook = getWorkbook(fis);
			Sheet sheet = workbook.getSheetAt(sheetIndex);
			fis.close();

			int columnIndex = getColumnIndex(columnName);

			updateCell(workbook, sheet, dataRecordIndex, columnIndex, dataToWrite);

			lockFile.delete();
			lockFile = null;
		} catch (IOException ioe) {
			ioe.printStackTrace();
			// RationalTestScript.logError("IOException writing to data sheet ("
			// + fileName + ") : " + ioe.getMessage());

			throw new WrappedException(ioe);

		} finally {
			if (lockFile != null) {
				lockFile.delete();
				lockFile = null;
			}
		}
	}
}
