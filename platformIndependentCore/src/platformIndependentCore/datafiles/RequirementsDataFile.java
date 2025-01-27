package platformIndependentCore.datafiles;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import platformIndependentCore.exceptions.InvalidRequirementException;

/**
 * RequirementDataFile extends ExcelFileBase and works with Requirements data
 * files The expected columns for this type of file are: REQ ID, REQ Title and
 * Status
 *
 * @author vbaaustaylol
 *
 */
public class RequirementsDataFile extends ExcelFileBase {
	private static final String REQUIREMENT_INDEX_COLUMN = "REQ ID";
	private static final int COLUMN_COUNT = 3;

	public static final String ID_COLUMN_HEADER = "REQ ID";
	public static final String DETAILS_COLUMN_HEADER = "REQ Title";
	public static final String STATUS_COLUMN_HEADER = "Status";

	private HashMap<String, Integer> indexCache = new HashMap<String, Integer>();

	// hashmap that lets us correlate the column text with its index
	private static final HashMap<Integer, String> columnMap = new HashMap<Integer, String>();

	/**
	 * Constructor for Requirements Data File
	 *
	 * @param filePath    to locate the file
	 * @param sheetNumber to specify the sheet within the Excel file
	 */
	public RequirementsDataFile(String filePath, int sheetNumber) {
		super(filePath, sheetNumber);
		initializeRequirementsFile(filePath, "Sheet" + String.valueOf(sheetNumber));
	}

	/**
	 * Constructor for Requirements Data File
	 *
	 * @param filePath  to locate the file
	 * @param sheetName to identify desired sheet in excel file
	 */
	public RequirementsDataFile(String filePath, String sheetName) {
		super(filePath, sheetName);
		initializeRequirementsFile(filePath, sheetName);
	}

	/**
	 * Method will get the Requirements file ready for use, defining the columns,
	 * loading the requirements
	 *
	 * @param fileName  to locate the file
	 * @param sheetName to identify desired sheet in excel file
	 */
	private void initializeRequirementsFile(String fileName, String sheetName) {
		// Populate our columnMap
		columnMap.put(0, ID_COLUMN_HEADER);
		columnMap.put(1, DETAILS_COLUMN_HEADER);
		columnMap.put(2, STATUS_COLUMN_HEADER);
		// if the file does not exist, create it now.
		File requirementsFile = new File(fileName);
		if (!requirementsFile.exists()) {
			createRequirementsFile(fileName, "Sheet" + sheetName);
		}
		loadRequirements();
	}

	/**
	 * Autosize the columns
	 */
	private void autoSize() {
		try {
			FileInputStream fis = new FileInputStream(fileName);
			Workbook workbook = getWorkbook(fis);
			Sheet sheet = workbook.getSheetAt(sheetIndex);
			fis.close();

			for (int i = 0; i < COLUMN_COUNT; i++) {
				sheet.autoSizeColumn(i);
			}

			FileOutputStream fos = new FileOutputStream(fileName);
			workbook.write(fos);
			fos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Creates a new file for the specified filename/path
	 *
	 * @param fileName  to locate the file
	 * @param sheetName to identify desired sheet in excel file
	 * @throws IOException Signals that an I/O exception of some sort has occurred
	 */
	private synchronized void createRequirementsFile(String fileName, String sheetName) {
		sheetIndex = 0;
		File lockFile = null;
		Workbook workbook = null;
		try {
			lockFile = createLockFile(fileName);
			// create new file
			workbook = new XSSFWorkbook();
			workbook.createSheet(sheetName);

			FileOutputStream fos = new FileOutputStream(fileName);
			workbook.write(fos);
			fos.close();
			lockFile.delete();
			lockFile = null;

			writeNewHeaderRow();
			autoSize();

			loadHeaderCache();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			// RationalTestScript
			// .logWarning("FileNotFoundException loading the Results File: "
			// + e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			// RationalTestScript
			// .logWarning("IOException loading the Results File: "
			// + e.getMessage());
		} finally {
			if (workbook != null) {
				try {
					workbook.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (lockFile != null) {
				lockFile.delete();
			}
		}

	}

	/**
	 * Returns the number of records in your data file
	 *
	 * @return int number of records in the data file
	 */
	public int getNumberOfRecords() {
		return indexCache.size();
	}

	/**
	 * Returns a set of the IDs contained in this Requirements file
	 *
	 * @return Set of ID Strings
	 */
	public Set<String> getIDs() {
		return indexCache.keySet();
	}

	/**
	 * Returns the row index for the specified ID
	 *
	 * @param id of desired row
	 * @return int row index
	 */
	public int getRowIndexForID(String id) {
		return indexCache.get(id);
	}

	/**
	 * Returns the value in the cell for the row specified by the ID and the
	 * matching columnName
	 *
	 * @param id         to identify row
	 * @param columnName to locate cell in row
	 * @return String value for cell matching row id and column name
	 */
	public String getData(String id, String columnName) {
		return getData(getRowIndexForID(id), columnName);

	}

	/**
	 * load all of the Values from the ID column as keys, the values are the
	 * corresponding row numbers
	 */
	private void loadRequirements() {
		File lockFile = null;

		try {
			if (isColumnHeaderPresent(REQUIREMENT_INDEX_COLUMN)) {
				int idColumn = getColumnIndex(REQUIREMENT_INDEX_COLUMN);
				int descriptionColumn = getColumnIndex(DETAILS_COLUMN_HEADER);

				if (idColumn >= 0) {
					// Data File contains IDs, cache them for easier row lookup
					lockFile = createLockFile(fileName);

					FileInputStream fis = new FileInputStream(fileName);

					Workbook workbook = getWorkbook(fis);
					Sheet sheet = workbook.getSheetAt(sheetIndex);
					fis.close();

					int rowIndex = 1;

					while (rowIndex <= sheet.getPhysicalNumberOfRows()) {
						Row currentRow = sheet.getRow(rowIndex);

						if (currentRow != null) {
							Cell cell = currentRow.getCell(idColumn);
							Cell cell2 = currentRow.getCell(descriptionColumn);
							String cellValue = "";
							if (cell != null) {
								cellValue = getCellValue(cell).trim();

								boolean seeAbove = false;
								String descValue = "";
								if (cell2 != null) {
									descValue = getCellValue(cell2).trim();

									if (descValue.toLowerCase().startsWith("see above")
											|| descValue.toLowerCase().startsWith("see description above")) {
										seeAbove = true;
									}
								}

								// only store the index for the first occurrence of a REQ ID
								if (!seeAbove && !indexCache.containsKey(cellValue)) {
									indexCache.put(cellValue, rowIndex);
								}
							}
						}
						rowIndex++;

					}

					lockFile.delete();
					lockFile = null;

				}
			} else {
				// if the file does not contain the expected ID column, throw an exception
				throw new InvalidRequirementException(
						"The Requirements Data File is missing the ID column. Please check your file and make sure to include and populate this column");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (lockFile != null) {
				lockFile.delete();
				lockFile = null;
			}
		}
	}

	/**
	 * Add the header row to the beginning of a new file
	 */
	private synchronized void writeNewHeaderRow() {

		File lockFile = null;
		try {
			lockFile = createLockFile(fileName);

			FileInputStream fis = new FileInputStream(fileName);
			Workbook workbook = getWorkbook(fis);
			Sheet sheet = workbook.getSheetAt(sheetIndex);
			fis.close();

			int newRowNumber = 0;

			// write
			Row newRow = sheet.createRow(newRowNumber);

			for (int i = 0; i < COLUMN_COUNT; i++) {
				newRow.createCell(i);
				updateCell(workbook, sheet, newRowNumber, i, columnMap.get(i));
				sheet.autoSizeColumn(i);
			}

			// Lock the header row
			sheet.createFreezePane(0, 1);

			FileOutputStream fos = new FileOutputStream(fileName);

			workbook.write(fos);

			fos.close();

			lockFile.delete();
			lockFile = null;
		} catch (IOException e) {
			e.printStackTrace();
			// RationalTestScript
			// .logWarning("IOException writing to results file "
			// + e.getMessage());
		} finally {
			if (lockFile != null) {
				lockFile.delete();
			}
		}

	}

	/**
	 * Take a SQL ResultSet and writes the values out to the Requirements File
	 *
	 * @param requirements ResultSet that contains the requirement information
	 * @throws SQLException if an Exception is encountered from SQL
	 */
	public void writeRequirementsFromDB(ResultSet requirements) throws SQLException {
		int rowIndex = 1;
		while (requirements.next()) {
			String description = requirements.getString("Requirement_REQ_Title");
			String status = requirements.getString("Requirement_Status");
			String name = requirements.getString("Requirement_REQ_ID");

			// Add code to write to a new file
			writeToDataSheet(rowIndex, RequirementsDataFile.ID_COLUMN_HEADER, name);
			writeToDataSheet(rowIndex, RequirementsDataFile.DETAILS_COLUMN_HEADER, description);
			writeToDataSheet(rowIndex, RequirementsDataFile.STATUS_COLUMN_HEADER, status);

			rowIndex++;

		}

		autoSize();
	}

}
