package platformIndependentCore.datafiles;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import platformIndependentCore.exceptions.InvalidDataException;
import platformIndependentCore.exceptions.MissingFileException;
import platformIndependentCore.exceptions.WrappedException;
import platformIndependentCore.scripts.Arguments;
import platformIndependentCore.utilities.BidirectionalMap;
import platformIndependentCore.utilities.ConfigProperties;

/**
 * Class to interact with a Formatted CSV Data File
 *
 * @author VBAAUSTAYLOL
 *
 */
public class CsvDataFile implements DataFile {
	/** log file for this class */
	static Logger log = LogManager.getLogger(CsvDataFile.class.getName());
	/** convenience variable for one minute represented in milliseconds */
	protected static final long ONE_MINUTE_IN_MILLIS = 60000;
	/** character used as the delimiter in this CSV file */
	static String DELIMITER = "~";
	/**
	 * The FORCE_TEXT variable is used to force Excel to read each CSV cell as Text
	 */
	String FORCE_TEXT = "`";
	/**
	 * The numerical row index is required to be the first cell value in a row, so
	 * the column index for that cell is always 0
	 */
	static final int ROW_INDEX_COLUMN = 0;
	/** File Name for the CSV file */
	protected String csvFileName = "";
	/** Number of rows in the file, defaults to -1 */
	private int numRows = -1;
	/** Holds the column index for every column in the header row */
	LinkedHashMap<String, Integer> headerCache = new LinkedHashMap<String, Integer>();
	/** Header Row object */
	CsvRow headerRow = null;

	/**
	 * Caches each row as a String[] using the testDataId value from that row as the
	 * key
	 */
	HashMap<String, CsvRow> rowCache = new HashMap<String, CsvRow>();

	/** Keep an ordered list of data ids */
	List<String> allDataIds = new ArrayList<String>();

	/**
	 * Caches the testDataId values for each row using the numerical row index as
	 * the key
	 */
	BidirectionalMap<Integer, String> dataIdCache = new BidirectionalMap<Integer, String>();
	/**
	 * Designates if the hash maps need to be updated, if false, they will be
	 * calculated
	 */
	private boolean updatedHashMaps = false;
	/** File instance for the csvFile */
	File csvFile = new File("");
	/** error message explaining to check formatting */
	private String checkFormattingMsg = "\n Make sure you have correctly formatted your datafile (which will add the "
			+ FORCE_TEXT + " character to each cell) and that it contains the entry you are searching for.";

	/**
	 *
	 * @param filePath to locate file
	 */
	public CsvDataFile(String filePath) {
		csvFileName = filePath;
		// format();
		setUp(filePath);
	}

	/**
	 *
	 * @param filePath  to locate file
	 * @param forceText TRUE if the FORCE_TEXT character (`) is used, FALSE if not
	 */
	public CsvDataFile(String filePath, boolean forceText) {
		setForceText(forceText);
		setUp(filePath);
	}

	/**
	 * Allows you to read a CSV file a delimiter that differs from the default
	 *
	 * @param filePath  to locate file
	 * @param delimiter specifies the delimiter used in this file
	 */
	public CsvDataFile(String filePath, String delimiter) {
		csvFileName = filePath;
		DELIMITER = delimiter;
		FORCE_TEXT = "";
		if (headerCache == null || headerCache.isEmpty()) {
			File file = new File(filePath);
			if (file.exists() && file.length() > 0) {
				populateHeaderCache();
			}
		}

		format();
	}

	/**
	 * Will add a new row to the end of the datasheet, setting the TestDataId to the
	 * specified value
	 *
	 * @param testDataId - unique id to identify the row
	 */
	public void addNewRow(String testDataId) {
		Objects.requireNonNull("The dataId for a new row must not by null!");

		// first verify if the data file always contains the Data Id, if so, throw an
		// exception
		if (isDataIdPresent(testDataId)) {
			throw new InvalidDataException("Unable to add row for DATA_ID=" + testDataId + " in " + csvFileName
					+ " because an entry with this DATA ID already exists in the data file. Please select a unique DATA ID");
		}
		String newLine = "";

		File file = new File(csvFileName);
		int dataIdColumnIndex = getDataIdColumnIndex();
		String newContent = "";

		BufferedReader reader = null;

		FileWriter writer = null;
		// Find the index of the last row and increment one.
		// We don't want to simply count the number of row
		// because if a row has been deleted, it could cause
		// a duplicate index when adding a new row

		try {
			reader = getReader(); // new BufferedReader(new FileReader(file));

			// Reading all the lines of input text file into newContent
			String line = reader.readLine();

			int rowIndex = 0;
			while (line != null && rowIndex >= 0) {
				CsvRow row = new CsvRow(line);
				// String[] cells = line.split(DELIMITER, -1);
				// First remove the old value from the cache
				String currentDataId = row.getCell(dataIdColumnIndex);
				rowCache.remove(currentDataId);

				// add the current line to the value being built
				newContent = newContent + line + System.lineSeparator();
				// read next line for the while loop
				line = reader.readLine();
			}
			// All existing rows have been added, time to add the new row
			boolean first = true;
			for (String currHeader : headerCache.keySet()) {
				// for (int i=0; i < headerCache.keySet().size(); i++){
				String cellContent = FORCE_TEXT;
				if (currHeader.equalsIgnoreCase(getDataIdColumn())) {
					cellContent += testDataId;
				} else if (currHeader.equals("0")) {
					cellContent += String.valueOf(getNewRowIndex());
				}
				if (first) {
					newLine += cellContent;
					first = false;
				} else {
					newLine += DELIMITER + cellContent;
				}

			}

			newContent = newContent + newLine + System.lineSeparator();

			writer = new FileWriter(file);
			// write the value out to the file
			writer.write(newContent);
		} catch (IOException e) {
			e.printStackTrace();
			throw new WrappedException(e);
		} finally {
			try {
				// Closing the resources
				if (reader != null) {
					reader.close();
				}

				if (writer != null) {
					writer.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// return newIndex;
		reset();
	}

	/**
	 * This method will iterate through the lines in the file and associated the
	 * test data id with the row, storing it for easier access later. It will also
	 * generate and store a numeric index to support legacy test scripts that worked
	 * with row index numbers
	 */
	private void calculateAndStoreRowInfomation() {
		// Reset to make sure to clear out previously stored data
		reset();

		int dataIdColumnIndex = getColumnIndex(DATA_ID_COLUMN);

		FileInputStream input = null;
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = getReader();
			Stream<String> lines = bufferedReader.lines();

			Iterator<String> iterator = lines.iterator();
			int index = 0;
			while (iterator.hasNext()) {
				String rawRow = iterator.next();
				CsvRow row = new CsvRow(rawRow);
				String currentTestId = row.getCell(dataIdColumnIndex);
				rowCache.put(currentTestId, row);
				dataIdCache.put(index, currentTestId);
				// skip the header row
				if (index > 0) {
					allDataIds.add(currentTestId);
				}
				index++;

			}
			lines.close();
		} finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		updatedHashMaps = true;
	}

	@Override
	@Deprecated
	public void deleteRowFromDataSheet(int dataRecordIndex) {
		deleteRowFromDataSheet(dataIdCache.get(dataRecordIndex));
	}

	@Override
	public void deleteRowFromDataSheet(String testDataId) {
		// deleteRowFromDataSheet(getRecordIndex(testDataID));

		int dataIdColumnIndex = getDataIdColumnIndex();
		File fileToBeModified = new File(csvFileName);

		String newContent = "";

		BufferedReader reader = null;

		FileWriter writer = null;

		try {
			reader = getReader();

			// Reading all the lines of input text file into newContent
			// will skip over the line to be deleted
			String line = reader.readLine();
			log.debug(line);
			int rowIndex = 0;
			String currentDataId = "";
			while (line != null && rowIndex >= 0) {
				CsvRow row = new CsvRow(line);
				// String[] cells = line.split(DELIMITER, -1);
				currentDataId = row.getCell(dataIdColumnIndex);

				// If the current data id matches the testDataId of the row to be deleted do not
				// add it to the value to be written back to the file
				if (!testDataId.equals(currentDataId)) {
					newContent = newContent + line + System.lineSeparator();
				}
				line = reader.readLine();
			}
			reader.close();
			reader = null;
			writer = new FileWriter(fileToBeModified);

			writer.write(newContent);
		} catch (IOException e) {
			e.printStackTrace();
			throw new WrappedException(e);
		} finally {
			try {
				// Closing the resources

				if (reader != null) {
					reader.close();
				}

				if (writer != null) {
					writer.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		reset();
	}

	/**
	 * Will update the format to ensure that every cell starts with the FORCE_TEXT
	 * character
	 */
	void format() {
		BufferedReader reader = null;
		FileWriter writer = null;
		try {
			reader = getReader();
			String line = reader.readLine();
			String newContent = "";
			while (line != null) {
				CsvRow currRow = new CsvRow(line);
				String[] cells = currRow.getCells();
				String newline = "";
				// rebuild the current line
				for (String cell : cells) {
					newline += FORCE_TEXT + cell + DELIMITER;
				}
				// add the current line to the value being built
				newContent = newContent + newline + System.lineSeparator();
				// read next line for the while loop
				line = reader.readLine();
			}
			reader.close();
			reader = null;
			writer = new FileWriter(csvFileName);
			// write the value out to the file
			writer.write(newContent);

		} catch (IOException e) {
			e.printStackTrace();
			throw new WrappedException(e);
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				if (writer != null) {
					writer.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// int getColumnIndex(String string);

	/**
	 * Will return the File object this CsvDataFile represents
	 *
	 * @return File object
	 */
	@Override
	public File getFile() {
		if (csvFile == null) {
			csvFile = new File(csvFileName);
		}
		return csvFile;
	}

	/**
	 * Will get an Arguments object containing the provided arguments for the given
	 * dataId.
	 *
	 * This method will use the column header values for columns after the
	 * START_ARGS column as the keys and then the cell values for those columns in
	 * the matching row for the given data id as the values
	 *
	 * @param dataId to retrieve Arguments for
	 * @return Arguments object containing the values provided for the dataID
	 */
	public Arguments getArguments(String dataId) {
		Arguments args = new Arguments();
		int startIndex = getColumnIndex("START_ARGS") + 1;
		int currIndex = startIndex;
		int numArgs = getNumberOfArguments();
		// loop through all the columns after the START_ARGS column
		while (currIndex < startIndex + numArgs) {
			// for each column, get the column header
			String key = getKeyByValue(headerCache, currIndex);
			if (key != null && !key.isEmpty()) {
				// get the value for that column in the specified row
				String value = getData(dataId, key);
				// add this value to the args
				args.set(key, value);
			}
			currIndex++;
		}
		// return the final set of args
		return args;
	}

	/**
	 * Returns the Column Index that corresponds with the given column name
	 *
	 * @param columnName of desired column
	 * @return int index of column
	 */
	private int getColumnIndex(String columnName) {
		columnName = columnName.toUpperCase().replaceAll("`", "");
		int index = -1;
		if (headerCache.containsKey(columnName)) {
			index = headerCache.get(columnName);
		}
		return index;
	}

	/**
	 * Will return the set of Columns for this data file
	 *
	 * @return Set<String> columns
	 */
	Set<String> getColumns() {
		return headerCache.keySet();
	}

	/**
	 * Returns the string value of the cell in the specified record
	 * (dataRecordIndex) from the specified column (columnName). Defaults to an
	 * empty string if there is no value
	 *
	 * @param dataRecordIndex integer to identify the row index
	 * @param columnName      to locate cell in row
	 * @return Value of the cell specified by dataRecordIndex and columnName
	 */
	@Override
	@Deprecated
	public synchronized String getData(int dataRecordIndex, String columnName) {
		// Make column case insensitive
		CsvRow row = null;

		String testDataID = "";

		// Check to see if we have cached the dataId that corresponds
		// to the dataRecordIndex (or row index)
		if (dataIdCache.containsKey(dataRecordIndex)) {
			testDataID = dataIdCache.get(dataRecordIndex);
		} else {
			// If it is not cached, we must search the file for a match
			String rowStart = FORCE_TEXT + String.valueOf(dataRecordIndex) + DELIMITER;
			row = getFirstRow(rowStart);
			if (row != null) {
				testDataID = row.getCell(getDataIdColumnIndex());
				dataIdCache.put(dataRecordIndex, testDataID);
				rowCache.put(testDataID, row);
			} else {
				throw new InvalidDataException("Unable to find value for row=" + dataRecordIndex + checkFormattingMsg);
			}

		}
		return getData(testDataID, columnName);

	}

	/**
	 * Returns the string value of the cell in the specified record (testDataID)
	 * from the specified column (columnName). Defaults to an empty string if there
	 * is no value.
	 *
	 * Leading and trailing whitespace will be trimmed off before returning
	 *
	 * @param testDataID to identify the row
	 * @param columnName to locate cell in row
	 * @return value of the matching cell
	 */
	@Override
	public synchronized String getData(String testDataID, String columnName) {
		CsvRow row = getRow(testDataID);
		int colIndex = getColumnIndex(columnName);

		if (row == null) {
			throw new InvalidDataException("No records were found with a DATA_ID=" + testDataID + checkFormattingMsg);
		}

		if (colIndex < 0 || colIndex >= row.getColumnCount()) {
			log.debug(row.toString());
			for (int i = 0; i < row.getColumnCount(); i++) {
				log.debug("~" + row.getCell(i));
			}
			log.debug("Row Size=" + row.getColumnCount() + ", colIndex = " + colIndex);
			throw new InvalidDataException("No columns were found for=" + columnName + checkFormattingMsg);

		}
		String value = row.getCell(colIndex);
		value = value.replace(FORCE_TEXT, "");

		return value.trim();

	}

	/**
	 * Will return the Column header used to identify the DATA_ID or TestDataId
	 *
	 * @return Data Id Column Header
	 */
	private String getDataIdColumn() {
		return DATA_ID_COLUMN;
	}

	/**
	 * Will return the column index for the DATA_ID column
	 *
	 * @return int column index for DATA_ID
	 */
	private int getDataIdColumnIndex() {
		return getColumnIndex(getDataIdColumn());
	}

	/**
	 * Returns the data id for the first row that contains the specified cell value
	 * in the specified column
	 *
	 * @param columnHeader to identify the cell in a row
	 * @param cellValue    to match to locate the row
	 * @return String data id for the matching row
	 */
	public String getDataIdForFirstMatchingRow(String columnHeader, String cellValue) {
		int columnIndex = getColumnIndex(columnHeader);
		String dataId = "";
		List<String> rows = getRowsContaining(cellValue);

		for (String r : rows) {
			log.debug(r);
			CsvRow currRow = new CsvRow(r);
			if (currRow.getCell(columnIndex).equals(cellValue)) {
				int dataIdIndex = getColumnIndex(DATA_ID_COLUMN);
				dataId = currRow.getCell(dataIdIndex);
				break;
			}
		}

		return dataId;

	}

	@Override
	public List<String> getDataIds() {
		List<String> currDataIds = new ArrayList<String>();
		if (!updatedHashMaps) {
			calculateAndStoreRowInfomation();
		}
		currDataIds.addAll(allDataIds);
		return currDataIds;
	}

	/**
	 * Returns the first row that matches the criteria
	 *
	 * @param columnHeader to locate cell
	 * @param cellValue    to identify row
	 * @return CsvRow that matches provided criteria
	 */
	public CsvRow getFirstMatchingRow(String columnHeader, String cellValue) {
		int columnIndex = getColumnIndex(columnHeader);
		CsvRow matchingRow = new CsvRow("");
		List<String> rows = getRowsWithCell(cellValue);

		for (String r : rows) {
			CsvRow currRow = new CsvRow(r);
			if (currRow.getCell(columnIndex).equals(cellValue)) {
				matchingRow = currRow;
				break;
			}
		}
		return matchingRow;

	}

	/**
	 * Will search and return the first row matching the regex provided
	 *
	 * @param rowStart text to identify row
	 * @return CsvRow first matching row
	 */
	private CsvRow getFirstRow(String rowStart) {
		CsvRow row = null;
		FileInputStream input = null;
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = getReader();

			Stream<String> lines = bufferedReader.lines();
			Optional<String> currRow;

			// If no search criteria is provided, return the very first row
			// otherwise find the first row matching the criteria
			if (rowStart.isEmpty()) {
				currRow = lines.findFirst();
			} else {
				currRow = lines.filter(line -> line.startsWith(rowStart)).findFirst();
			}
			if (currRow.isPresent()) {
				row = new CsvRow(currRow.get());
			}

			lines.close();
		} finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		if (row == null && rowStart.isEmpty()) {
			throw new InvalidDataException("Error reading header row!");
		} else if (row == null) {
			throw new InvalidDataException("No rows matching a Row Index (first cell value) of  " + rowStart
					+ " were found." + checkFormattingMsg);
		}
		return row;
	}

	/**
	 * Will return the header row for this data file
	 *
	 * @return CsvRow header row
	 */
	private CsvRow getHeaderRow() {
		if (headerRow == null) {
			populateHeaderCache();
		}
		return headerRow;
	}

	/**
	 * This will find and return the last row in the file
	 *
	 * @return CsvRow - last row
	 */
	private CsvRow getLastRow() {
		String last = "";
		FileInputStream input = null;
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = getReader();
			Stream<String> lines = bufferedReader.lines();
			// look for a cell containing the FORCE_TEXT character and the
			// testDataID
			Object[] lineArray = lines.toArray();
			last = (String) lineArray[lineArray.length - 1];
			lines.close();
		} finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return new CsvRow(last);
	}

	/**
	 * Returns a List of rows that match the criteria
	 *
	 * @param columnHeader to locate cell
	 * @param cellValue    to identify row
	 * @return ArrayList{CsvRow} of matching rows
	 */
	public ArrayList<CsvRow> getMatchingRows(String columnHeader, String cellValue) {
		int columnIndex = getColumnIndex(columnHeader);
		ArrayList<CsvRow> matchingRows = new ArrayList<CsvRow>();
		List<String> rows = getRowsWithCell(cellValue);

		for (String r : rows) {
			CsvRow currRow = new CsvRow(r);
			if (currRow.getCell(columnIndex).equals(cellValue)) {
				matchingRows.add(currRow);
			}
		}
		return matchingRows;

	}

	/**
	 * Will return the next available row in the data file
	 *
	 * @param dataUsedIndex - column index for the DATA_USED column
	 * @return CsvRow next available row
	 */
	private CsvRow getNextAvailRow(int dataUsedIndex) {
		Optional<String> match = null;
		CsvRow availRow = null;

		if (dataUsedIndex < 0) {
			throw new InvalidDataException(
					"Can not use getNextUnusedRecord method with a datasheet missing the " + DATA_USED_COLUMN
							+ " column header. Please check your data sheets to verify they have the required column");
		}

		try (Stream<String> lines = Files.lines(csvFile.toPath())) {
			match = lines.filter(line -> line.contains(DELIMITER + FORCE_TEXT + AVAILABLE_VALUE + DELIMITER))
					.findFirst();
			lines.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw new WrappedException("Exception accessing your Data File. Please make sure it is not in use. " + e);
		}

		if (match != null && match.isPresent()) {
			CsvRow row = new CsvRow(match.get());
			if (row.getCell(dataUsedIndex).toUpperCase().equals(AVAILABLE_VALUE)) {
				availRow = row;
			} else {
				// If their data file contains cell values matching "AVAIL"
				// outside of the DATA_USED_COLUMN you might get here. Throwing
				// an exception for now. If people
				// need to use the word "AVAIL" outside of this column, we can
				// revisit returning a collection and verifying the position
				throw new InvalidDataException("CSVDATAFILE ERROR in getNextUnusedRecord: Encountered cell value of "
						+ AVAILABLE_VALUE + " outside of " + DATA_USED_COLUMN
						+ ". Please revisit data file values, contact the Automation Team for addition help");

			}
		}
		return availRow;
	}

	@Override
	public synchronized String getNextUnusedDataId(boolean reserveRecord) {
		String dataId = null;
		int dataUsedIndex = getColumnIndex(DATA_USED_COLUMN);
		int dataIdIndex = getColumnIndex(DATA_ID_COLUMN);
		CsvRow row = getNextAvailRow(dataUsedIndex);
		if (row != null) {
			dataId = row.getCell(dataIdIndex);
		}
//		else {
//			// If their data file contains cell values matching "AVAIL"
//			// outside of the DATA_USED_COLUMN you might get here. Throwing
//			// an exception for now. If people
//			// need to use the word "AVAIL" outside of this column, we can
//			// revisit returning a collection and verifying the position
//			throw new InvalidDataException("CSVDATAFILE ERROR " + "No rows currently have a value of " + DATA_AVAILABLE
//					+ " inside of the " + DATA_USED_COLUMN + " column");
////					+ "in getNextUnusedRecord: Encountered cell value of "
////					+ DATA_AVAILABLE + " outside of " + DATA_USED_COLUMN
////					+ ". Please revisit data file values, contact the Automation Team for addition help");
//
//		}

		String exceptionMessage = "Unable to locate next unused data id in file: " + csvFileName
				+ ".\n\n Please verify that " + "available rows are marked with the value " + AVAILABLE_VALUE
				+ " in the " + DATA_USED_COLUMN + checkFormattingMsg;
		if (row == null) {
			throw new InvalidDataException(
					exceptionMessage + ". Further Debugging Info: row was null in getNextUnusedDataId().");
		}
		if (dataId == null) {
			throw new InvalidDataException(
					exceptionMessage + ". Further Debugging Info: dataId was null in getNextUnusedDataId().");
		}

		if (reserveRecord && dataId != null && !dataId.isEmpty()) {
			writeToDataSheet(dataId, DATA_USED_COLUMN, USED_VALUE);

		}
		return dataId;
	}

	/**
	 * Returns the dataRecordIndex value for the next unused record. This value does
	 * not map to the row number in Excel. We are only looking at the records, not
	 * the header row.
	 *
	 * @param reserveRecord - if set to true, then the next unused record of data
	 *                      will be marked as used
	 * @return dataRecordIndex of next unused record
	 */
	@Override
	@Deprecated
	public synchronized int getNextUnusedRecord(boolean reserveRecord) {
		String rowIndex = null;
		int dataUsedIndex = getColumnIndex(DATA_USED_COLUMN);
		CsvRow row = getNextAvailRow(dataUsedIndex);

		if (row == null) {
			throw new InvalidDataException("Unable to locate next unused data id in file: " + csvFileName
					+ ".\n\n Please verify that " + "available rows are marked with the value " + AVAILABLE_VALUE
					+ " in the " + DATA_USED_COLUMN + checkFormattingMsg);
		}
		rowIndex = row.getCell(ROW_INDEX_COLUMN);

		if (reserveRecord && rowIndex != null && !rowIndex.isEmpty()) {
			writeToDataSheet(Integer.valueOf(rowIndex), DATA_USED_COLUMN, USED_VALUE);

		}
		return Integer.valueOf(rowIndex);
	}

	/**
	 * Will give the next numerical index for the file
	 *
	 * @return int numerical index for new row
	 */
	private int getNewRowIndex() {
		CsvRow lastRow = getLastRow();
		// Find the index of the last row and increment one.
		// We don't want to simply count the number of row
		// because if a row has been deleted, it could cause
		// a duplicate index when adding a new row
		return Integer.valueOf(lastRow.getCell(0)) + 1;
	}

	/**
	 * Will return the number of Argument columns for the file
	 *
	 * @return number of Argument columns after START_ARGS
	 */
	int getNumberOfArguments() {
		int startIndex = getColumnIndex("START_ARGS");
		return getNumberOfColumns() - startIndex;
	}

	/**
	 * Package scoped method to return the number of columns in this data file
	 *
	 * @return int number of columns
	 */
	public int getNumberOfColumns() {
		CsvRow firstRow = getHeaderRow();
		return firstRow.getColumnCount();
	}

	@Override
	public int getNumberOfRecords() {
		if (numRows <= 0) {
			BufferedReader bufferedReader = null;
			// Find all the lines in the file and return the count
			// TODO - may have to add logic to limit to rows with
			// rowId/DATA_ID set
			try {
				bufferedReader = getReader();
				// subtract one to ignore the header row
				numRows = (int) bufferedReader.lines().count() - 1;
				bufferedReader.close();
			} catch (IOException e) {
				e.printStackTrace();
				throw new WrappedException(e);
			} finally {
				try {
					if (bufferedReader != null) {
						bufferedReader.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return numRows;
	}

	@Override
	public int getNumberOfRowsByValue(String columnHeader, String value) {
		int colIndex = getColumnIndex(columnHeader);
		List<String> results = new ArrayList<String>();
		int numMatch = 0;

		if (colIndex < 0) {
			throw new InvalidDataException("The datasheet is missing the " + columnHeader
					+ " column header while trying to call getNumberOfRowsByValue. "
					+ "Please check your data sheets to verify they have the required column" + checkFormattingMsg);
		}

		results = getRowsWithCell(value);

		// Loop through the results and verify the value is in the correct
		// column and count the matches
		for (String r : results) {
			CsvRow row = new CsvRow(r);
			if (row.getCell(colIndex).equalsIgnoreCase(value)) {
				numMatch++;
			}
		}

		return numMatch;

	}

	@Override
	public int getNumberOfUnusedRecords() {
		return getNumberOfRowsByValue(DATA_USED_COLUMN, AVAILABLE_VALUE);
	}

	/**
	 * Will create and return a BufferedReader with settings to Ignore Malformed
	 * Input
	 *
	 * @return BufferedReader instance
	 */
	private BufferedReader getReader() {
		// rowId/DATA_ID set
		FileInputStream input;
		BufferedReader bufferedReader = null;
		try {
			input = new FileInputStream(new File(csvFileName));
			CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();
			decoder.onMalformedInput(CodingErrorAction.IGNORE);
			InputStreamReader reader = new InputStreamReader(input, decoder);
			bufferedReader = new BufferedReader(reader);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new WrappedException(e);
		}
		return bufferedReader;

	}

	@Override
	public int getRecordIndex(String testDataId) {

		int rowIndex = -1;
		if (dataIdCache.containsValue(testDataId)) {
			rowIndex = dataIdCache.getKey(testDataId);
		} else {
			if (!updatedHashMaps) {
				calculateAndStoreRowInfomation();
				if (dataIdCache.containsValue(testDataId)) {
					rowIndex = dataIdCache.getKey(testDataId);
				}
			}
		}
//		CsvRow rowIndex =
//		//CsvRow row = getRow(testDataID);
//		if (row != null) {
//			String cell = row.getCell(ROW_INDEX_COLUMN);
//			if (cell != null && !cell.isEmpty()) {
//				rowIndex = Integer.valueOf(cell);
//			}
//		}
		return rowIndex;
	}

	/**
	 * Returns the cached CsvRow representing cells in the matching row
	 *
	 * @param testDataID to identify the row
	 * @return CsvRow
	 */
	private CsvRow getRow(String testDataID) {
		log.debug("getRow: " + testDataID);
		CsvRow row = null;
		List<String> list = new ArrayList<>();

		if (rowCache.containsKey(testDataID)) {
			row = rowCache.get(testDataID);
		} else {
			log.debug("getRow: DATA_ID=" + getDataIdColumn());

			int testDataColumnIndex = getDataIdColumnIndex();

			if (testDataColumnIndex < 0) {
				throw new InvalidDataException(csvFileName
						+ "\n\nCan not use getRow method with a datasheet missing the " + getDataIdColumn()
						+ " column header. Please check your data sheets to verify they have the required column"
						+ checkFormattingMsg);
			}

			list = getRowsWithCell(testDataID);
			for (String line : list) {
				CsvRow currRow = new CsvRow(line);
				if (currRow.getCell(testDataColumnIndex).equalsIgnoreCase(testDataID)) {
					row = currRow;
					rowCache.put(testDataID, row);
					break;

				}
			}
		}

		return row;

	}

	/**
	 * Will return a List containing all the rows that contain a cell matching the
	 * specified value
	 *
	 * @param requiredText to search for in the row
	 * @return List<String> rows with cells that contain required text
	 */
	private List<String> getRowsContaining(String requiredText) {
		FileInputStream input = null;
		BufferedReader bufferedReader = null;
		List<String> list = new ArrayList<>();
		try {
			bufferedReader = getReader();
			Stream<String> lines = bufferedReader.lines();

			// look for a cell containing the FORCE_TEXT character and the testDataID
			list = lines.filter(line -> line.contains(requiredText)).collect(Collectors.toList());
			lines.close();
		} finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return list;
	}

	// private String getForceText() {
	// return
	// }
	/**
	 * Will return a List containing all the rows that contain a cell matching the
	 * specified value
	 *
	 * @param cellValue to search for
	 * @return List<String> rows with cells that match required text
	 */
	private List<String> getRowsWithCell(String cellValue) {
		return getRowsContaining(FORCE_TEXT + cellValue + DELIMITER);
	}

	/**
	 * Allows a user to see if a specified column exists in the Excel file.
	 *
	 * @param columnHeader to verify
	 * @return boolean TRUE if file contains the column header, FALSE if not
	 */
	@Override
	public boolean isColumnHeaderPresent(String columnHeader) {
		boolean result = false;
		if (headerCache.get(columnHeader) != null) {
			result = true;
		}
		return result;
	}

	@Override
	public boolean isDataIdPresent(String dataId) {
		if (!updatedHashMaps) {
			calculateAndStoreRowInfomation();
		}
		return dataIdCache.containsValue(dataId);
	}

	/**
	 * Method will populate the headerCache with key value pairs where the Key is
	 * the TEST_DATA_ID value and the value is the COLUMN_INDEX
	 */
	private void populateHeaderCache() {
		CsvRow header = getFirstRow("");
		// If the first cell does not start will the FORCE_TEXT character, assume
		// this data file is not configured for it, and set to false
		if (FORCE_TEXT.isEmpty() || !header.toString().startsWith(FORCE_TEXT)) {
			setForceText(false);
		}
		headerRow = header;
		String[] cells = header.getCells();
		for (int i = 0; i < cells.length; i++) {
			headerCache.put(cells[i].toUpperCase().replaceAll("`", ""), i);
		}
	}

	/**
	 * Resets all stored information for this file. Needed if rows are
	 * added/updated/removed
	 */
	private void reset() {
		// remove everything but the header cache as that should never change
		this.dataIdCache.clear();
		this.rowCache.clear();
		this.updatedHashMaps = false;
		this.numRows = -1;
		this.allDataIds.clear();
	}

	/**
	 * This method will clear out everything in the file except the header row
	 */
	public void resetFile() {
		File file = new File(csvFileName);

		BufferedReader reader = null;

		FileWriter writer = null;
		try {
			reader = getReader();

			// Reading all the lines of input text file into newContent
			String line = reader.readLine();

			writer = new FileWriter(file);
			// write the value out to the file
			writer.write(line);
		} catch (IOException e) {
			e.printStackTrace();
			throw new WrappedException(e);
		} finally {
			try {
				// Closing the resources
				if (reader != null) {
					reader.close();
				}

				if (writer != null) {
					writer.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	// /**
	// * Will return the number of Argument columns for the file
	// * @return
	// */
	// int getNumberOfArguments() {
	// int startIndex = getColumnIndex("START_ARGS");
	// return getNumberOfColumns() - startIndex;
	// }
	//
	// /**
	// * Will get an Arguments object containing the provided arguments for
	// * the given dataId.
	// *
	// * This method will use the column header values for columns after the
	// START_ARGS
	// * column as the keys and then the cell values for those columns in the
	// matching row
	// * for the given data id as the values
	// *
	// * @param dataId
	// * @return
	// */
	// public Arguments getArguments(String dataId) {
	// Arguments args = new Arguments();
	// int startIndex = getColumnIndex("START_ARGS") + 1;
	// int currIndex = startIndex;
	// int numArgs = getNumberOfArguments();
	// // loop through all the columns after the START_ARGS column
	// while (currIndex < numArgs) {
	// // for each column, get the column header
	// String key = getKeyByValue(headerCache, currIndex);
	// // get the value for that column in the specified row
	// String value = getData(dataId, key);
	// // add this value to the args
	// args.set(key, value);
	// }
	// // return the final set of args
	// return args;
	// }
	//
	// /**
	// * Will return the String key for the provided Integer value
	// *
	// * @param map
	// * @param value
	// * @return String
	// */
	// private static String getKeyByValue(Map<String, Integer> map, Integer value)
	// {
	// return map.entrySet()
	// .stream()
	// .filter(entry -> Objects.equals(entry.getValue(), value))
	// .findFirst().get().getValue().toString();
	//
	// }

	/**
	 * If force is true, then the FORCE_TEXT character is required in the cell
	 * <p>
	 * Defaults to the ` character so that Excel is forced to read all cells in the
	 * CSV file as plain text
	 *
	 * @param force TRUE, the FORCE_TEXT character is required, FALSE, it is not
	 */
	public void setForceText(boolean force) {
		if (!force) {
			FORCE_TEXT = "";
		}
	}

	/**
	 * Will verify and set up this file instance
	 *
	 * @param filePath of the file
	 */
	private void setUp(String filePath) {
		csvFileName = filePath;

		if (filePath.endsWith(".xlsx")) {
			throw new InvalidDataException(
					"You can not use a .xlsx file with CsvDataFile, please provide a .csv file.");
		} else {
			csvFile = verifyCsvFormat(filePath);
		}

		if (headerCache == null || headerCache.isEmpty()) {
			File file = new File(filePath);
			if (file.exists() && file.length() > 0) {
				populateHeaderCache();
			} else {
				throw new MissingFileException("Unable to locate data file: " + filePath);
			}
		}

	}

	/**
	 * Will make sure the CSV file adheres to our requirements. This initial
	 * verification is made by confirming the first cell in the first row is a 0
	 *
	 * @param csvFile to verify
	 * @return File instance for the csv file
	 * @throws InvalidDataException If the file is not formatted correctly
	 */
	private File verifyCsvFormat(String csvFile) {
		File file = new File(csvFile);
		if (file.exists() && file.length() > 0) {
			CsvRow match = getHeaderRow();
			if (Boolean.valueOf(ConfigProperties.getValue("REQUIRE_CSV_NUMERIC_INDEX", "FALSE"))) {
				if (match == null || !match.getCell(0).equals("0")) {
					// This .csv file is missing the index column
					/***/
					throw new InvalidDataException("CSV file " + csvFileName + " is not formatted correctly. "
							+ " Please reference the CsvDataFile template in the sharedTemplates under your production project for "
							+ "an example of how your data sheet should be formatted.");
				}
			}
		}
		return file;
	}

	/**
	 * Sets the value of the cell matching the specified dataRecordIndex and
	 * columnName to the specified value.
	 *
	 * @param dataRecordIndex integer to identify the row index
	 * @param columnName      to locate cell in row
	 * @param dataToWrite     value to write to the cell
	 */
	@Override
	@Deprecated
	public void writeToDataSheet(int dataRecordIndex, String columnName, String dataToWrite) {
		File file = new File(csvFileName);

		String newContent = "";

		BufferedReader reader = null;

		FileWriter writer = null;

		int colIndex = getColumnIndex(columnName);
		if (colIndex < 0) {
			throw new InvalidDataException("Can not use writeToDataSheet method  for column header = " + columnName
					+ " with a datasheet missing the " + columnName
					+ " column header. Please check your data sheets to verify they have the required column");
		} else {
			try {
				reader = getReader(); // new BufferedReader(new FileReader(file));

				// Reading all the lines of input text file into newContent
				String line = reader.readLine();

				int rowIndex = 0;
				while (line != null && rowIndex >= 0) {
					CsvRow row = new CsvRow(line);
					// String dataId = row.getCell(index)
					// String[] cells = line.split(DELIMITER, -1);
					// First remove the old value from the cacghe
					int dataColumnIndex = getDataIdColumnIndex();
					rowCache.remove(row.getCell(dataColumnIndex));

					// Break the row into cells to find the matching row to be
					// updated
					rowIndex = -1;
					try {
						rowIndex = Integer.valueOf(row.getCell(ROW_INDEX_COLUMN)); // LMT
																					// getRowIndex(dataRecordIndex);//Integer.valueOf(row.getCell(ROW_INDEX_COLUMN));
					} catch (NumberFormatException e) {
						e.printStackTrace();
						throw new WrappedException(e);
					}
					// Check if the current rowIndex matches the dataRecordIndex
					// we are looking for
					if (rowIndex >= 0 && rowIndex == dataRecordIndex) {
						// Found a match, so need to reset the cell for
						// columnName to the dataToWrite value
						String[] cells = row.getCells();
						String newline = "";
						cells[colIndex] = dataToWrite;
						// rebuild the current line
						for (String cell : cells) {
							newline += FORCE_TEXT + cell + DELIMITER;
						}
						// set the current line to the new value with the
						// updated cell
						line = newline;
					}
					// add the current line to the value being built
					newContent = newContent + line + System.lineSeparator();
					// read next line for the while loop
					line = reader.readLine();
				}

				writer = new FileWriter(file);
				// write the value out to the file
				writer.write(newContent);
			} catch (IOException e) {
				e.printStackTrace();
				throw new WrappedException(e);
			} finally {
				try {
					// Closing the resources
					if (reader != null) {
						reader.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					if (writer != null) {
						writer.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				reset();
			}
		}
	}

	@Override
	public synchronized void writeToDataSheet(String id, String columnName, String dataToWrite) {
		File file = new File(csvFileName);
		String newContent = "";

		BufferedReader reader = null;
		FileWriter writer = null;

		int colIndex = getColumnIndex(columnName);
		int dataIdColIndex = getDataIdColumnIndex();
		if (colIndex < 0) {
			throw new InvalidDataException("Can not use writeToDataSheet method  for column header = " + columnName
					+ " with a datasheet missing the " + columnName
					+ " column header. Please check your data sheets to verify they have the required column");
		} else {
			try {
				reader = getReader(); // new BufferedReader(new FileReader(file));

				// Reading all the lines of input text file into newContent
				String line = reader.readLine();

				int rowIndex = 0;
				while (line != null && rowIndex >= 0) {
					CsvRow row = new CsvRow(line);
					// Break the row into cells to find the matching row to be
					// updated
					String currentDataId = row.getCell(dataIdColIndex);

					// Check if the current data id matches the id we are looking for
					if (currentDataId != null && currentDataId.equals(id)) {
						// Found a match, so need to reset the cell for
						// columnName to the dataToWrite value
						String[] cells = row.getCells();
						String newline = "";
						cells[colIndex] = dataToWrite;

						boolean first = true;
						// rebuild the current line
						for (String cell : cells) {
							// we do not want a DELIMETER before the first cell
							if (!first) {
								newline += DELIMITER;
							} else {
								first = false;
							}
							newline += FORCE_TEXT + cell;

						}
						// set the current line to the new value with the
						// updated cell
						line = newline;
					}
					// add the current line to the value being built
					newContent = newContent + line + System.lineSeparator();
					// read next line for the while loop
					line = reader.readLine();
				}

				writer = new FileWriter(file);
				// write the value out to the file
				writer.write(newContent);
			} catch (IOException e) {
				e.printStackTrace();
				throw new WrappedException(e);
			} finally {
				try {
					// Closing the resources
					if (reader != null) {
						reader.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					if (writer != null) {
						writer.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				reset();
			}
		}
	}

}
