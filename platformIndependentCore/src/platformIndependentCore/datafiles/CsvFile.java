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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import platformIndependentCore.exceptions.InvalidDataException;

/**
 * Class to represent and interact with a basic CSV file
 *
 * @author VBAAUSTAYLOL
 *
 */
public class CsvFile {
	/** logger for this class */
	static Logger log = LogManager.getLogger(CsvDataFile.class.getName());
	/** Name of the CSV file */
	protected String csvFileName = "";
	/** File instance for this CSV file */
	File csvFile = new File("");
	/** Convenience variable to store 1 minute in milliseconds */
	protected static final long ONE_MINUTE_IN_MILLIS = 60000;// millisecs

	/** Holds the column index for every column in the header row */
	HashMap<String, Integer> headerCache = new HashMap<String, Integer>();
	/**
	 * Caches each row as a String[] using the firstCellValue value from that row as
	 * the key
	 */
	HashMap<String, CsvRow> rowCache = new HashMap<String, CsvRow>();
	/** Default delimiter */
	static final String DEFAULT_DELIMITER = "~";
	/** Actual delimiter */
	protected String DELIMITER = "~";

	/**
	 * The numerical row index is required to be the first cell value in a row, so
	 * the column index for that cell is always 0
	 */
	static final int ROW_INDEX_COLUMN = 0;
	/** Custom Data Id Column (in case it is different from default */
	protected String CUSTOM_DATA_ID_COLUMN = "";

	/**
	 * Constructor for CSVFile
	 *
	 * @param filePath file path to the CSV file
	 */
	public CsvFile(String filePath) {
		csvFileName = filePath;
		DELIMITER = DEFAULT_DELIMITER;
		populateHeaderCache();
	}

	/**
	 * Constructor for CSVFile allows for custom delimiter
	 *
	 * @param filePath  file path to CSV file
	 * @param delimiter delimiter used in this CSV file
	 */
	public CsvFile(String filePath, String delimiter) {
		csvFileName = filePath;
		DELIMITER = delimiter;
		populateHeaderCache();
	}

	/**
	 * Returns the file name for this CSVFile
	 *
	 * @return String file name
	 */
	public String getFilename() {
		return csvFileName;
	}

	/**
	 * Will check if this CSV file has the specified column header
	 *
	 * @param columnHeader to verify
	 * @return boolean TRUE if file contains the columnHeader, FALSE if not
	 */
	public boolean hasColumn(String columnHeader) {
		return getColumnIndex(columnHeader) > -1;
	}

	// @Override
	// public int getNumberOfRecords() {
	// // TODO Auto-generated method stub
	// return 0;
	// }
	//
	// @Override
	// public int getNumberOfUnusedRecords() {
	// // TODO Auto-generated method stub
	// return 0;
	// }

	// @Override
	// public int getNumberOfRowsByValue(String columnHeader, String value) {
	// // TODO Auto-generated method stub
	// return 0;
	// }

	/**
	 * Returns the Column Index that corresponds with the given column name
	 *
	 * @param columnName to locate cell in row
	 * @return int column index
	 */
	private int getColumnIndex(String columnName) {
		columnName = columnName.toUpperCase();
		int index = -1;
		if (headerCache.containsKey(columnName)) {
			index = headerCache.get(columnName);
		}
		return index;
	}

	/**
	 * Will search and return the first row matching the regex provided
	 *
	 * @param rowStart text desired row starts with
	 * @return CsvRow row object
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
			throw new InvalidDataException(
					"No rows matching a Row Index (first cell value) of  " + rowStart + " were found.");
			// + checkFormattingMsg);
		}
		return row;
	}

	/**
	 * Method will populate the headerCache with key value pairs where the Key is
	 * the TEST_DATA_ID value and the value is the COLUMN_INDEX
	 */
	private void populateHeaderCache() {
		CsvRow header = getFirstRow("");

		String[] cells = header.getCells();
		for (int i = 0; i < cells.length; i++) {
			headerCache.put(cells[i].toUpperCase(), i);
		}
	}

	/**
	 * Returns the Data from the cell specified with firstCellValue (locates row)
	 * and columnName (locates column)
	 *
	 * @param firstCellValue locates row that starts with provided value
	 * @param columnName     locates column with matching name/header
	 * @return String value from cell located with criteria
	 */
	public String getData(String firstCellValue, String columnName) {
		CsvRow row = getRow(firstCellValue);
		int columnIndex = getColumnIndex(columnName);
		return row.getCell(columnIndex);
	}

	/**
	 * Returns all values under the specified column
	 *
	 * @param columnName to get values for
	 * @return {@code List<String>} list of text in all cells under the column
	 */
	public List<String> getAllValues(String columnName) {
		List<String> allValues = new ArrayList<String>();
		int columnIndex = getColumnIndex(columnName);
		List<CsvRow> allRows = getAllRows();

		for (int i = 1; i < allRows.size(); i++) {
			String curr = allRows.get(i).getCell(columnIndex).trim();
			if (!curr.isEmpty()) {
				allValues.add(curr);
			}
		}

		return allValues;
	}

	/**
	 * Returns the a list of rows that have the specified value in the desired
	 * column
	 * <p>
	 * NOTE: This method assumes that the RowId is in the first column
	 *
	 * @param columnName to match values under
	 * @param value      to locate rows for
	 * @return {@code ArrayList<String>} List of rowIds from the first column for
	 *         rows with specified value under the column
	 */
	public ArrayList<String> getRowIdsByValue(String columnName, String value) {
		ArrayList<String> rowIds = new ArrayList<String>();
		List<String> potentials = getRowsWithCell(value);
		int columnHeaderIndex = getColumnIndex(columnName);
		for (String curr : potentials) {
			CsvRow currRow = new CsvRow(curr);
			if (currRow.getCell(columnHeaderIndex).equals(value)) {
				rowIds.add(currRow.getCell(0));
			}
		}
		return rowIds;
	}

	// public boolean containsColumnHeader(String columnHeader) {
	// // TODO Auto-generated method stub
	// return false;
	// }

	/**
	 * Will write value to the CsvFile
	 *
	 * @param firstCellValue to identify row
	 * @param columnName     column to locate cell to write to
	 * @param dataToWrite    value to write to the file
	 */
	public void writeToDataSheet(String firstCellValue, String columnName, String dataToWrite) {
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

				// int rowIndex = 0;
				while (line != null) {
					CsvRow row = new CsvRow(line);
					// String[] cells = line.split(DELIMITER, -1);
					// First remove the old value from the cacghe
					int dataColumnIndex = 0;
					rowCache.remove(row.getCell(dataColumnIndex));

					// Break the row into cells to find the matching row to be
					// updated
					// rowIndex = -1;
					try {
						// rowIndex = Integer.valueOf(row.getCell(ROW_INDEX_COLUMN));
					} catch (NumberFormatException e) {
						e.printStackTrace();
					}
					String[] cells = row.getCells();
					// Check if the current rowIndex matches the dataRecordIndex
					// we are looking for
					if (cells[0].equals(firstCellValue)) {
						// Found a match, so need to reset the cell for
						// columnName to the dataToWrite value
						String newline = "";
						cells[colIndex] = dataToWrite;
						// rebuild the current line
						for (String cell : cells) {
							newline += cell + DELIMITER;
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
	}

	// public void deleteRowFromDataSheet(String firstCellValue) {
	// // TODO Auto-generated method stub
	//
	// }

	/**
	 * Returns the cached String[] representing cells in the matching row
	 *
	 * @param firstCellValue identify row with this value in the first cell
	 * @return CsvRow matching row
	 */
	public CsvRow getRow(String firstCellValue) {
		log.debug("getRow: " + firstCellValue);
		CsvRow row = null;
		List<String> list = new ArrayList<>();

		if (rowCache.containsKey(firstCellValue)) {
			row = rowCache.get(firstCellValue);
		} else {
			int testDataColumnIndex = 0;

			list = getRowsStartingWithCell(firstCellValue);
			for (String line : list) {
				CsvRow currRow = new CsvRow(line);
				if (currRow.getCell(testDataColumnIndex).equalsIgnoreCase(firstCellValue)) {
					row = currRow;
					rowCache.put(firstCellValue, row);
					break;

				}
			}
		}

		return row;

	}

	/**
	 * Will return a List containing all the rows that contain a cell matching the
	 * specified value. This will match the rows using a starting with, so the
	 * cellValue must be in the first column
	 *
	 * @param cellValue to match row
	 * @return List<String> all matching rows
	 */
	private List<String> getRowsStartingWithCell(String cellValue) {
		FileInputStream input = null;
		BufferedReader bufferedReader = null;
		List<String> list = new ArrayList<>();
		try {
			bufferedReader = getReader();
			Stream<String> lines = bufferedReader.lines();

			// look for a cell containing the FORCE_TEXT character and the firstCellValue
			list = lines.filter(line -> line.startsWith(cellValue + DELIMITER)).collect(Collectors.toList());
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

	/**
	 * Will return a List containing all the rows that contain a cell matching the
	 * specified value
	 *
	 * @return List<String> all rows
	 */
	private List<CsvRow> getAllRows() {
		FileInputStream input = null;
		BufferedReader bufferedReader = null;
		List<String> listRows = new ArrayList<>();
		List<CsvRow> allRows = new ArrayList<>();

		try {
			bufferedReader = getReader();
			Stream<String> lines = bufferedReader.lines();

			listRows = lines.collect(Collectors.toList());

			for (String line : listRows) {
				allRows.add(new CsvRow(line));
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
		return allRows;
	}

	/**
	 * Will return a List containing all the rows that contain a cell matching the
	 * specified value. This value can be anywhere in the row
	 *
	 * @param cellValue to identify rows
	 * @return List<String> rows that contain the provided value
	 */
	private List<String> getRowsWithCell(String cellValue) {
		FileInputStream input = null;
		BufferedReader bufferedReader = null;
		List<String> list = new ArrayList<>();
		try {
			bufferedReader = getReader();
			Stream<String> lines = bufferedReader.lines();

			// look for a cell containing the FORCE_TEXT character and the firstCellValue
			list = lines.filter(line -> line.contains(cellValue))
					// list = lines.filter(line -> line.contains(DELIMITER + cellValue + DELIMITER))
					.collect(Collectors.toList());
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

	/**
	 * Will create and return a BufferedReader with settings to Ignore Malformed
	 * Input
	 *
	 * @return BufferedReader reader
	 */
	private BufferedReader getReader() {
		// rowId/firstCellValue set
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
		}
		return bufferedReader;

	}

	/**
	 * Internal class to manage Rows
	 *
	 * @author VBAAUSTAYLOL
	 *
	 */
	public class CsvRow {
		/** original row value */
		String origRow = "";
		/** cell values for the row */
		String[] cells;

		/**
		 * Constructor for the CsvRow
		 *
		 * @param cells value for all the cells in the row
		 */
		public CsvRow(String[] cells) {
			this.cells = cells;
		}

		/**
		 * Constructor for the CsvRow
		 *
		 * @param row String value for entire row
		 */
		public CsvRow(String row) {
			cells = row.split(DELIMITER, -1);
			origRow = row;
		}

		/**
		 * Will return a String representation of this row
		 */
		@Override
		public String toString() {
			String value = origRow;
			if (value.isEmpty()) {
				value = ">> ";
				for (String s : cells) {
					value += s + DELIMITER;
				}
			}
			return value;
		}

		/**
		 * Returns the cell value at the given index. This method will also remove any
		 * pre-pended characters before returning
		 *
		 * @param index of the cell within the row
		 * @return String value of the specified cell
		 */
		public String getCell(int index) {
			String value = "";

			if (index < cells.length) {
				value = cells[index];
				if (value == null) {
					value = "";
				}
			} else {
				log.error("ERROR READING ROW: " + toString());
				throw new InvalidDataException("Unable to get cell with index: " + index + " for ROW=" + toString());
			}
			log.debug("RETURNING: " + value);
			return value;

		}

		/**
		 * Returns an array of Cell values for this row without the FORCE_TEXT character
		 *
		 * @return String[] value of cells for the row
		 */
		public String[] getCells() {
			return cells;
		}

		/**
		 * Returns the number of Columns (cells) in this row
		 *
		 * @return int number of columns/cells in the row
		 */
		public int getColumnCount() {
			return cells.length;
		}
	}

	/**
	 * Adds a new row to the file
	 *
	 * @param values cell values for the new row
	 */
	public void addNewRow(String... values) {
		String newRow = "";
		for (String cell : values) {
			newRow += cell + DELIMITER;
		}
		File file = new File(csvFileName);

		String newContent = "";

		BufferedReader reader = null;

		FileWriter writer = null;

		try {
			reader = getReader(); // new BufferedReader(new FileReader(file));

			// Reading all the lines of input text file into newContent
			String line = reader.readLine();

			// int rowIndex = 0;
			while (line != null) {
				CsvRow row = new CsvRow(line);
				// String[] cells = line.split(DELIMITER, -1);
				// First remove the old value from the cacghe
				int dataColumnIndex = 0;
				rowCache.remove(row.getCell(dataColumnIndex));

				// Break the row into cells to find the matching row to be
				// updated
				// rowIndex = -1;
				try {
					// rowIndex = Integer.valueOf(row.getCell(ROW_INDEX_COLUMN));
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
				// String[] cells = row.getCells();
				// add the current line to the value being built
				newContent = newContent + line + System.lineSeparator();
				// read next line for the while loop
				line = reader.readLine();
			}
			newContent += newRow;
			writer = new FileWriter(file);
			// write the value out to the file
			writer.write(newContent);
		} catch (IOException e) {
			e.printStackTrace();
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
}
