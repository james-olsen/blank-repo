package platformIndependentCore.datafiles;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import platformIndependentCore.exceptions.InvalidDataException;
import platformIndependentCore.exceptions.WrappedException;

/**
 * Concrete implementation for ExcelDataFile. This class will be used to access
 * and update an Excel spreadsheet used for storing test data.
 *
 * Requires a header row in the first row of the spreadsheet
 *
 * Required Columns: DATA_USED and TestDataID
 *
 * The DATA_USED column is only required if you will be calling
 * getNextUnusedRecord() It is used when you for reserving a row of test data
 * and should have a 'true/false' value
 *
 * The TestDataID is only required if you will be passing it into the getData
 * calls. It should contain a unique id so that a tester can reuse and access
 * the same record of data in the future even if the results file changes.
 *
 * @author VBAAUSTAYLOL
 * @author VBADESDunigR
 */
public class ExcelDataFile extends ExcelFileBase implements DataFile {

	/** The number of Records or Rows **/
	protected int numberOfRecords = -1;

	/**
	 * Constructor which takes the file path and sheet number (index)
	 *
	 * @param filePath    the File Path to the ExcelDataFile
	 * @param sheetNumber the Sheet Number (index)
	 */
	public ExcelDataFile(String filePath, int sheetNumber) {
		super(filePath, sheetNumber);
	}

	/**
	 * Constructor which takes the file path and sheet name
	 *
	 * @param filePath  the File Path to the ExcelDataFile
	 * @param sheetName the Sheet Name
	 */
	public ExcelDataFile(String filePath, String sheetName) {
		super(filePath, sheetName);
	}

	/**
	 * Returns the cached rowIndex that corresponds to the test data id
	 *
	 * @param testDataID to identify the row
	 * @return index
	 */
	@Override
	public int getRecordIndex(String testDataID) {
		int index = -1;
		FileInputStream fis = null;
		Workbook workbook = null;
		try {
			int testDataColumnIndex = getColumnIndex(DATA_ID_COLUMN);

			fis = new FileInputStream(fileName);
			workbook = getWorkbook(fis);
			Sheet sheet = workbook.getSheetAt(sheetIndex);
			// search to find the row that has a matching testDataID
			for (int rowIndex = 1; rowIndex <= getNumberOfRecords(); rowIndex++) {
				Row currentRow = sheet.getRow(rowIndex);

				if (getCellValue(currentRow.getCell(testDataColumnIndex)).equals(testDataID)) {
					index = rowIndex;
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new WrappedException("Exception accessing your Data File. Please make sure it is not in use" + e);
		} finally {
			if (workbook != null) {
				try {
					workbook.close();
				} catch (IOException e) {
					e.printStackTrace();
					throw new WrappedException(
							"Exception accessing your Data File. Please make sure it is not in use" + e);

				}
			}
		}

		return index;

	}

	/**
	 * Returns the number of records in your data file
	 *
	 * @return int
	 */
	@Override
	public int getNumberOfRecords() {
		// return indexCache.size();
		if (numberOfRecords < 0) {
			int numberRows = -1;
			Workbook workbook = null;
			try {
				int columnIndex = 0; // getColumnIndex(DATA_ID_COLUMN);

				if (columnIndex >= 0) {
					// Since we know the column exists, set numberRows = 0
					numberRows = 0;

					// Create the input stream from the xlsx/n file
					FileInputStream fis = new FileInputStream(fileName);

					// Create Workbook instance for xlsx/xls file input stream
					if (fileName.toLowerCase().endsWith("xlsx")) {
						workbook = new XSSFWorkbook(fis);
					} else if (fileName.toLowerCase().endsWith("xls")) {
						workbook = new HSSFWorkbook(fis);
					}

					Sheet sheet = workbook.getSheetAt(sheetIndex);
					workbook.close();

					// Counts the number of rows in a column that have a value (not empty).
					for (int currentRow = 1; currentRow <= sheet.getLastRowNum(); currentRow++) {
						if (sheet.getRow(currentRow) != null) {
							Cell cell = sheet.getRow(currentRow).getCell(columnIndex);
							if (!getCellValue(cell).isEmpty()) {
								numberRows++;
							} else {
								// we don't allow empty rows in the middle of data, so we assume we are at the
								// end once we find and empty cell
								break;
							}
							cell = null;
						}
					}
					sheet = null;
					numberOfRecords = numberRows;
				} else {
					// If no column has a header matching the header name passed in
					// an exception is thrown
					throw new InvalidDataException("Column does not exist in spreadsheet: " + DATA_ID_COLUMN);
				}
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
				throw new WrappedException("Your Data File was not found" + e1);
			} catch (IOException e) {
				e.printStackTrace();
				throw new WrappedException("Exception accessing your Data File. Please make sure it is not in use" + e);
			} finally {
				if (workbook != null) {
					try {
						workbook.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			workbook = null;

		}
		return numberOfRecords;

	}

	/**
	 * Returns the number of unused records in your data file. If your file does not
	 * have a DATA_USED column, an exception will be thrown
	 *
	 * @return numberUnusedRows
	 * @throws InvalidDataException thrown if your file does not have a DATA_USED
	 *                              column
	 */
	@Override
	public int getNumberOfUnusedRecords() {
		return getNumberOfRowsByValue(DATA_USED_COLUMN, "false");
	}

	/**
	 * Returns the number of rows for the specified column header that matches the
	 * passed in value.
	 *
	 * @param columnHeader text to locate the desired column
	 * @param value        to search for
	 * @return numberRows that contain the value in the desired column
	 * @throws InvalidDataException Make sure specified column header exists
	 */
	@Override
	public int getNumberOfRowsByValue(String columnHeader, String value) {
		int numberRows = -1;
		Workbook workbook = null;
		try {
			int columnIndex = getColumnIndex(columnHeader);

			if (columnIndex >= 0) {
				// Since we know the column exists, set numberRows = 0
				numberRows = 0;

				// Create the input stream from the xlsx/n file
				FileInputStream fis = new FileInputStream(fileName);

				// Create Workbook instance for xlsx/xls file input stream
				if (fileName.toLowerCase().endsWith("xlsx")) {
					workbook = new XSSFWorkbook(fis);
				} else if (fileName.toLowerCase().endsWith("xls")) {
					workbook = new HSSFWorkbook(fis);
				}

				Sheet sheet = workbook.getSheetAt(sheetIndex);

				workbook.close();

				// Counts the number of rows in a column that have a matching
				// value.
				for (int currentRow = 1; currentRow <= sheet.getLastRowNum(); currentRow++) {
					Cell cell = sheet.getRow(currentRow).getCell(columnIndex);
					// if a cell is null, it will be evaluated as an empty String
					if (getCellValue(cell).equalsIgnoreCase(value)) {
						numberRows++;
					}

					cell = null;
				}
				sheet = null;
			} else {
				// If no column has a header matching the header name passed in
				// an exception is thrown
				throw new InvalidDataException("Column does not exist in spreadsheet: " + columnHeader);
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			throw new WrappedException("Your Data File was not found" + e1);
		} catch (IOException e) {
			e.printStackTrace();
			throw new WrappedException("Exception accessing your Data File. Please make sure it is not in use. " + e);
		} finally {
			if (workbook != null) {
				try {
					workbook.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				workbook = null;
			}
		}
		workbook = null;
		return numberRows;
	}

	/**
	 * Returns the dataRecordIndex value for the next unused record. This value does
	 * not map to the row number in Excel. We are only looking at the records, not
	 * the header row.
	 *
	 * @param reserveRecord - if set to true, then the next unused record of data
	 *                      will be marked as used
	 * @return dataRecordIndex
	 */
	@Override
	public synchronized String getNextUnusedDataId(boolean reserveRecord) {
		int record = getNextUnusedRecord(reserveRecord);
		return getData(record, DATA_ID_COLUMN);
	}

	/**
	 * Returns the dataRecordIndex value for the next unused record. This value does
	 * not map to the row number in Excel. We are only looking at the records, not
	 * the header row.
	 *
	 * @param reserveRecord - if set to true, then the next unused record of data
	 *                      will be marked as used
	 * @return dataRecordIndex
	 */
	@Override
	public synchronized int getNextUnusedRecord(boolean reserveRecord) {
		Workbook workbook = null;
		int dataRowIndex = -1;
		File lockFile = null;

		try {

			// Create the input stream from the xlsx/xls file
			FileInputStream fis = new FileInputStream(fileName);

			// Create Workbook instance for xlsx/xls file input stream
			if (fileName.toLowerCase().endsWith("xlsx")) {
				workbook = new XSSFWorkbook(fis);
			} else if (fileName.toLowerCase().endsWith("xls")) {
				workbook = new HSSFWorkbook(fis);
			}

			Sheet sheet = workbook.getSheetAt(sheetIndex);
			fis.close();
			fis = null;

			if (reserveRecord) {
				lockFile = createLockFile(fileName);
			}

			int columnIndex = getColumnIndex(DATA_USED_COLUMN);
			dataRowIndex = getNextUnusedRecord(sheet, columnIndex);

			if (dataRowIndex == -1) {
				throw new InvalidDataException("All records in the spreadsheet are marked as used.");
			}

			if (reserveRecord) {
				updateCell(workbook, sheet, dataRowIndex, columnIndex, USED_VALUE);
				lockFile.delete();
				lockFile = null;
			}
		} catch (IOException e) {
			e.printStackTrace();

			// Not being able read data will halt the execution - throw the
			// exception
			throw new WrappedException(e);
		} finally {
			if (lockFile != null) {
				lockFile.delete();
				lockFile = null;
			}

			if (workbook != null) {
				try {
					workbook.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				workbook = null;
			}

		}

		return dataRowIndex;

	}

	/**
	 * Returns the index of the next row that has not been reserved/used.
	 *
	 * This value does not map to the row number in Excel. We are only looking at
	 * the records, not the header row.
	 *
	 * @param sheet       within the workbook
	 * @param columnIndex specifies column cell is located in
	 * @return dataRowIndex
	 */
	private synchronized int getNextUnusedRecord(Sheet sheet, int columnIndex) {
		int dataRowIndex = -1;

		if (columnIndex >= 0) {

			// Find the first row in the table that has not been used;
			for (int currentRow = 1; currentRow <= getNumberOfRecords(); currentRow++) {
				Cell cell = sheet.getRow(currentRow).getCell(columnIndex);

				// Any value other than AVAIL (including empty/null) will be counted as USED
				if (getCellValue(cell).equalsIgnoreCase(AVAILABLE_VALUE)) {
					dataRowIndex = currentRow;
					break;
				}
			}

		} else {
			// If no column has a header "Used" or all the rows are marked as
			// used (true), an exception is thrown
			throw new InvalidDataException("Column labeled \"DATA_USED\" does not exist in spreadsheet.");
		}
		return dataRowIndex;

	}

	/**
	 * Returns the string value of the cell in the specified record (testDataID)
	 * from the specified column (columnName). Defaults to an empty string if there
	 * is no value
	 *
	 * @param testDataID to identify the row
	 * @param columnName to locate cell in row
	 * @return data
	 */
	@Override
	public synchronized String getData(String testDataID, String columnName) {
		int index = getRecordIndex(testDataID);

		if (index == -1) {
			throw new InvalidDataException("No records were found with a TestDataID=" + testDataID);
		}

		return getData(index, columnName);

	}

	/**
	 * Sets the value of the cell matching the specified testDataID and columnName
	 * to the specified value.
	 *
	 * @param testDataID  to identify the row
	 * @param columnName  to locate cell in row
	 * @param dataToWrite value to write to the cell
	 */
	@Override
	public void writeToDataSheet(String testDataID, String columnName, String dataToWrite) {
		writeToDataSheet(getRecordIndex(testDataID), columnName, dataToWrite);

	}

	/**
	 * Sets the value of the cell matching the specified testDataID and columnName
	 * to the specified value.
	 *
	 * @param recordIndex integer to identify the row index
	 * @param columnName  to locate cell in row
	 * @param dataToWrite value to write to the cell
	 */
	@Override
	public void writeToDataSheet(int recordIndex, String columnName, String dataToWrite) {
		super.writeToDataSheet(recordIndex, columnName, dataToWrite);

		if (columnName.equals(DATA_ID_COLUMN)) {
			// Need to clear the stored numberOfRecords as there is a new TestDataID
			numberOfRecords = -1;
		}
	}

	/**
	 * Deletes the row with the corresponding testDataID
	 *
	 * @param testDataID to identify the row
	 */
	@Override
	public synchronized void deleteRowFromDataSheet(String testDataID) {
		deleteRowFromDataSheet(getRecordIndex(testDataID));
		// Since the number of rows has changed, reset the stored size
		numberOfRecords = -1;
	}

	/**
	 * Deletes the row at the specified dataRecordIndex
	 *
	 * @param dataRecordIndex integer to identify the row index
	 */
	@Override
	public synchronized void deleteRowFromDataSheet(int dataRecordIndex) {
		File lockFile = null;
		Workbook workbook = null;
		try {
			lockFile = createLockFile(fileName);

			FileInputStream fis = new FileInputStream(fileName);

			workbook = getWorkbook(fis);
			Sheet sheet = workbook.getSheetAt(sheetIndex);

			numberOfRecords = this.getNumberOfRecords();
			Row row = null;

			fis.close();
			fis = null;

			File file = new File(fileName);
			FileOutputStream fos = new FileOutputStream(file);

			if (dataRecordIndex >= 0 && dataRecordIndex < numberOfRecords) {
				sheet.shiftRows(dataRecordIndex + 1, numberOfRecords, -1);
			}

			if (dataRecordIndex == numberOfRecords) {
				row = sheet.getRow(dataRecordIndex);
				if (row != null) {
					sheet.removeRow(row);
				}
			}

			workbook.write(fos);
			workbook.close();
			fos.close();
			fos = null;

			lockFile.delete();
			lockFile = null;
			row = null;
			sheet = null;

			// Since the number of rows has changed, reset the stored size
			numberOfRecords = -1;

		} catch (IOException ioe) {
			ioe.printStackTrace();

			throw new WrappedException(ioe);

		} finally {
			if (lockFile != null) {
				lockFile.delete();
				lockFile = null;
			}

			if (workbook != null) {
				try {
					workbook.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				workbook = null;
			}

		}
	}

	@Override
	public List<String> getDataIds() {
		List<String> dataIds = new ArrayList<String>();

		FileInputStream fis = null;
		Workbook workbook = null;

		try {
			int testDataColumnIndex = getColumnIndex(DATA_ID_COLUMN);

			fis = new FileInputStream(fileName);
			workbook = getWorkbook(fis);
			Sheet sheet = workbook.getSheetAt(sheetIndex);

			// Gather all Data IDs
			int numRecords = getNumberOfRecords();
			for (int rowIndex = 1; rowIndex <= numRecords; rowIndex++) {
				Row currentRow = sheet.getRow(rowIndex);
				if (!currentRow.getCell(testDataColumnIndex).getStringCellValue().isBlank()) {
					dataIds.add(currentRow.getCell(testDataColumnIndex).getStringCellValue());
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
			throw new WrappedException("Exception accessing your Data File. Please make sure it is not in use" + e);
		} finally {
			if (workbook != null) {
				try {
					workbook.close();
				} catch (IOException e) {
					e.printStackTrace();
					throw new WrappedException(
							"Exception accessing your Data File. Please make sure it is not in use" + e);
				}
			}
		}

		return dataIds;
	}

	@Override
	public boolean isDataIdPresent(String dataId) {
		return getRecordIndex(dataId) >= 0;
	}

}
