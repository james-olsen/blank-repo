package platformIndependentCore.datafiles;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import platformIndependentCore.exceptions.InvalidDataException;
import platformIndependentCore.utilities.ConfigProperties;

/**
 * Interface to define abstract methods and default implementations for
 * implementing Data File classes
 *
 * @author vbaaustaylol
 *
 */
public interface DataFile {
	// Interface methods are always implicitly public, so the keyword is not needed
	// on method declarations

	// Constants to store required header values, pulling values from Constants file
	// to give easier access to descending classes
	/** Test Execution column header */
	String TEST_EXECUTION_COLUMN = ConfigProperties.getValue(ConfigProperties.TEST_EXECUTION_COLUMN, "TEST_EXECUTION");
	/** Execution Time stamp column header */
	String EXECUTION_TIMESTAMP_COLUMN = "EXECUTION_TIMESTAMP";
	/** Script column header */
	String SCRIPT_COLUMN = ConfigProperties.getValue(ConfigProperties.SCRIPT_COLUMN, "SCRIPT");
	/** Dependencies column header */
	String DEPENDENCIES_COLUMN = ConfigProperties.getValue(ConfigProperties.DEPENDENCIES_COLUMN, "DEPENDENCIES");
	/** Data Used column header */
	String DATA_USED_COLUMN = ConfigProperties.getValue(ConfigProperties.DATA_USED_COLUMN, "DATA_USED");
	/** Data ID column header */
	String DATA_ID_COLUMN = ConfigProperties.getValue(ConfigProperties.DATA_ID_COLUMN, "DATA_ID");
	/** Script Driver File column header for Test Suite Files */
	String SCRIPT_DRIVER_FILE_COLUMN = "SCRIPT_DRIVER_FILE";

	// Constants to store standardized cell values
	/** Available status for data used column for data files */
	String AVAILABLE_VALUE = "AVAIL";
	/** Used status for data used column for data files */
	String USED_VALUE = "USED";
	/** Run status for test execution column in suite files */
	String RUN_VALUE = "RUN";
	/** Skip status for test execution column in suite files */
	String SKIP_VALUE = "SKIP";

	/**
	 * Deletes the row with the corresponding testDataId
	 *
	 * @param testDataId to identify the row
	 */
	void deleteRowFromDataSheet(String testDataId);

	/**
	 * Deletes the row at the specified dataRecordIndex
	 *
	 * @deprecated Please use {@link #deleteRowFromDataSheet(String testDataId)}
	 *             String data ids instead of numerical indexes
	 * @param dataRecordIndex integer to identify the row index
	 */
	@Deprecated
	void deleteRowFromDataSheet(int dataRecordIndex);

	/**
	 * Will return the String key for the provided Integer value
	 *
	 * @param map   The hashmap to search
	 * @param value you are looking to find the key for
	 * @return String key value for the provided value
	 */
	default String getKeyByValue(Map<String, Integer> map, Integer value) {
		for (Entry<String, Integer> entry : map.entrySet()) {
			if (Objects.equals(value, entry.getValue())) {
				return entry.getKey();
			}
		}

		return null;
	}

	/**
	 * Will return the File object this DataFile represents
	 *
	 * @return File object
	 */
	File getFile();

	/**
	 * Returns the cached rowIndex that corresponds to the test data id
	 *
	 * @deprecated Please use String data ids instead of numerical indexes
	 * @param testDataId - the id you are searching for
	 * @return index where that testDataId is located
	 */
	@Deprecated
	int getRecordIndex(String testDataId);

	/**
	 * Returns the number of records in your data file
	 *
	 * @return int number of records
	 */
	int getNumberOfRecords();

	/**
	 * Returns the number of unused records in your data file. If your file does not
	 * have a DATA_USED column, an exception will be thrown
	 *
	 * @return numberUnusedRows in the data file
	 * @throws InvalidDataException if DATA_USED column is not present
	 */
	int getNumberOfUnusedRecords();

	/**
	 * Returns the number of rows for the specified column header that matches the
	 * passed in value.
	 *
	 * @param columnHeader text to locate the desired column
	 * @param value        that you are looking for
	 * @return numberRows that contain the value in the column
	 * @throws InvalidDataException if unable to locate specified column
	 */
	int getNumberOfRowsByValue(String columnHeader, String value);

	/**
	 * Returns the dataRecordIndex value for the next unused record. This value does
	 * not necessarily map to the row number in the Data File. We are only looking
	 * at the records, not the header row.
	 *
	 * @param reserveRecord - if set to true, then the next unused record of data
	 *                      will be marked as used
	 * @return dataRecordIndex of next unused record
	 */
	int getNextUnusedRecord(boolean reserveRecord);

	/**
	 * Returns the data id value for the next unused record.
	 *
	 * @param reserveRecord - if set to true, then the next unused record of data
	 *                      will be marked as used
	 * @return dataId of next unused record
	 */
	String getNextUnusedDataId(boolean reserveRecord);

	/**
	 * Will return all of the values for the specified column in the data file
	 *
	 * @param columnName to retrieve values for
	 * @return Set{String} set of all the values for the column
	 */
	default List<String> getAllValues(String columnName) {
		List<String> allValues = new ArrayList<String>();
		List<String> dataIds = getDataIds();

		for (String currentDataId : dataIds) {
			allValues.add(getData(currentDataId, columnName));
		}

		return allValues;
	}

	/**
	 * Returns a set of all values in the DATA_ID columns
	 *
	 * @return ArrayList{String} data ids
	 */
	List<String> getDataIds();

	/**
	 * Returns the string value of the cell in the specified record (testDataId)
	 * from the specified column (columnName). Defaults to an empty string if there
	 * is no value
	 *
	 * @param testDataId to identify the row
	 * @param columnName to locate cell in row
	 * @return String value in the located cell
	 */
	String getData(String testDataId, String columnName);

	/**
	 * Returns the string value of the cell in the specified record
	 * (dataRecordIndex) from the specified column (columnName). Defaults to an
	 * empty string if there is no value
	 *
	 * @deprecated Please use {@link #getData(String testDataId, String columnName)}
	 *             String data ids instead of numerical indexes
	 * @param dataRecordIndex integer to identify the row index
	 * @param columnName      to locate cell in row
	 * @return String value of the cell
	 */
	@Deprecated
	String getData(int dataRecordIndex, String columnName);

	/**
	 * Returns the string value of the cell in the specified record (testDataId)
	 * from the specified column (columnName). Defaults to an empty string if there
	 * is no value. <br>
	 * If the length of the cell in the data sheet is less than the provided
	 * minNumCharacters, leading 0s will be added to make it the correct length.
	 *
	 * @param testDataId       to identify the row
	 * @param columnName       to locate cell in row
	 * @param minNumCharacters If the length of the cell in the data sheet is less
	 *                         than the provided minNumCharacters, leading 0s will
	 *                         be added to make it the correct length.
	 * @return String value of the cell. Defaults to empty String if there is no
	 *         value
	 */
	default String getData(String testDataId, String columnName, int minNumCharacters) {

		if (!isDataIdPresent(testDataId)) {
			throw new InvalidDataException("No records were found with a TestDataId=" + testDataId);
		}

		String value = getData(testDataId, columnName);
		if (value.length() < minNumCharacters) {
			int paddingLength = minNumCharacters - value.length();
			for (int i = 0; i < paddingLength; i++) {
				value = "0" + value;
			}
		}
		return value;

	}

	/**
	 * Allows a user to see if a specified column exists in the Data file.
	 *
	 * @param columnHeader to verify
	 * @return boolean true if specified columnHeader is found, false if it is not
	 *         found
	 */
	boolean isColumnHeaderPresent(String columnHeader);

	/**
	 * Allows a user to verify if a specified dataId exists in the Data file.
	 *
	 * @param dataId to verify
	 * @return boolean true if specified dataId is found, false if it is not found
	 */
	boolean isDataIdPresent(String dataId);

	/**
	 * Sets the DATA_USED cell matching the specified testDataId and columnName to
	 * the AVAIL.
	 *
	 * @param testDataId to identify the row
	 */
	default void setDataAvail(String testDataId) {
		writeToDataSheet(testDataId, DATA_USED_COLUMN, AVAILABLE_VALUE);

	}

	/**
	 * Sets the DATA_USED cell matching the specified testDataId and columnName to
	 * the USED.
	 *
	 * @param testDataId to identify the row
	 */
	default void setDataUsed(String testDataId) {
		writeToDataSheet(testDataId, DATA_USED_COLUMN, USED_VALUE);

	}

	/**
	 * Sets the value of the cell matching the specified testDataId and columnName
	 * to the specified value.
	 *
	 * @param testDataId  to identify the row
	 * @param columnName  to locate cell in row
	 * @param dataToWrite value to set the specified cell to
	 */
	void writeToDataSheet(String testDataId, String columnName, String dataToWrite);

	/**
	 * Sets the value of the cell matching the specified testDataId and columnName
	 * to the specified value.
	 *
	 * @deprecated Please use
	 *             {@link #writeToDataSheet(String testDataId, String columnName, String dataToWrite)}
	 *             String data ids instead of numerical indexes
	 * @param recordIndex integer to identify the row index
	 * @param columnName  to locate cell in row
	 * @param dataToWrite value to write to the cell
	 */
	@Deprecated
	void writeToDataSheet(int recordIndex, String columnName, String dataToWrite);

}
