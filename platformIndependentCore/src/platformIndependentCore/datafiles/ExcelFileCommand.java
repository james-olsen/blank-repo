package platformIndependentCore.datafiles;

import java.util.HashMap;

import platformIndependentCore.exceptions.InvalidDataException;

public class ExcelFileCommand {
	/*
	 * These variables map to the Command File Columns. The Column Name/Header is
	 * above the declaration
	 */
	// TestDataID
	private String testDataID = "";
	// Operation
	private String operation;

	// SysManOptionPrompt
	private String prompt;

	// SysManPromptEntry
	private String promptEntry;

	// VerificationName
	private String verificationName;

	// VerificationLabel
	private String verificationLabel;

	// VerificationExpectedValue
	private String expectedValue;

	// ScrollsUntil
	private String scrollsUntil = "";

	// ContinuePrompt
	private String scrollPrompt = null;

	// ContinueCommand
	private String scrollCommand = null;

	// DataFileColumn
	private String columnToStoreValueIn = "";

	// DataFileColumn
	private boolean allCapsCheck = false;

	/* Other variables that are set by parsing above values */
	private String command;
	private String delimiter;
	private boolean ignoreSpaces;
	private boolean readBetween;
	private String readBetweenCharacters;
	private boolean startsWith;
	private boolean split;
	private int splitIndex;
	private String splitDelimiter;

	public ExcelFileCommand(ExcelDataFile commandFile, int commandFileIndex) {

	}

	public ExcelFileCommand(ExcelDataFile commandFile, int commandFileIndex, ExcelDataFile dataFile,
			int dataFileRowIndex, ExcelDataFile secondaryDataFile, int secondaryFileRowIndex,
			HashMap<String, String> params, String defaultDelimiter) {

		if (commandFile.isColumnHeaderPresent("TestDataID")) {
			testDataID = lookupCommandFileValue(commandFile, commandFileIndex, "TestDataID", dataFile, dataFileRowIndex,
					secondaryDataFile, secondaryFileRowIndex, params);
		}

		expectedValue = lookupCommandFileValue(commandFile, commandFileIndex, "VerificationExpectedValue", dataFile,
				dataFileRowIndex, secondaryDataFile, secondaryFileRowIndex, params);

		verificationLabel = lookupCommandFileValue(commandFile, commandFileIndex, "VerificationLabel", dataFile,
				dataFileRowIndex, secondaryDataFile, secondaryFileRowIndex, params);
		verificationName = lookupCommandFileValue(commandFile, commandFileIndex, "VerificationName", dataFile,
				dataFileRowIndex, secondaryDataFile, secondaryFileRowIndex, params);

		prompt = lookupCommandFileValue(commandFile, commandFileIndex, "SysManOptionPrompt", dataFile, dataFileRowIndex,
				secondaryDataFile, secondaryFileRowIndex, params);

		promptEntry = lookupCommandFileValue(commandFile, commandFileIndex, "SysManPromptEntry", dataFile,
				dataFileRowIndex, secondaryDataFile, secondaryFileRowIndex, params);

		operation = lookupCommandFileValue(commandFile, commandFileIndex, "Operation", dataFile, dataFileRowIndex,
				secondaryDataFile, secondaryFileRowIndex, params);

		if (commandFile.isColumnHeaderPresent("DataFileColumn")) {
			columnToStoreValueIn = lookupCommandFileValue(commandFile, commandFileIndex, "DataFileColumn", dataFile,
					dataFileRowIndex, secondaryDataFile, secondaryFileRowIndex, params);
		}

		if (commandFile.isColumnHeaderPresent("ScrollsUntil")) {
			scrollsUntil = lookupCommandFileValue(commandFile, commandFileIndex, "ScrollsUntil", dataFile,
					dataFileRowIndex, secondaryDataFile, secondaryFileRowIndex, params);
		}

		if (commandFile.isColumnHeaderPresent("ScrollContinuePrompt")) {
			scrollPrompt = lookupCommandFileValue(commandFile, commandFileIndex, "ScrollContinuePrompt", dataFile,
					dataFileRowIndex, secondaryDataFile, secondaryFileRowIndex, params);
		}

		if (commandFile.isColumnHeaderPresent("ScrollContinueCommand")) {
			scrollCommand = lookupCommandFileValue(commandFile, commandFileIndex, "ScrollContinueCommand", dataFile,
					dataFileRowIndex, secondaryDataFile, secondaryFileRowIndex, params);
		}

		this.command = operation;
		this.delimiter = defaultDelimiter;
		ignoreSpaces = false;

		// need to check if there is an IGNORESPACES option attached to the end
		// of the verify command
		if (command.startsWith("VERIFY") && command.endsWith("IGNORESPACES")) {
			ignoreSpaces = true;
			command = command.substring(0, (command.indexOf("IGNORESPACES")));
		}

		if (command.startsWith("VERIFYALLCAPS")) {
			allCapsCheck = true;
		}

		if (command.contains("SPLITVALUE")) {
			split = true;
			int commandIndex = command.indexOf("SPLITVALUE") + 10;
			// decrement by one for array index versus position number
			splitIndex = Integer.valueOf(command.substring(commandIndex, commandIndex + 1)) - 1;

			// default to 4 spaces for the delimiter
			splitDelimiter = "    ";
			if (!command.endsWith("SPLITVALUE" + String.valueOf(splitIndex + 1))) {

				splitDelimiter = command.split("SPLITVALUE" + String.valueOf(splitIndex + 1))[1];
			}
			command = command.substring(0, command.indexOf("SPLITVALUE"));

		}

		readBetween = false;
		readBetweenCharacters = "";
		if (command.endsWith("()") || command.endsWith("<>")) {
			readBetween = true;
			int newLength = command.length() - 2;
			readBetweenCharacters = command.substring(newLength, newLength + 2); // "()";
			command = command.substring(0, newLength);

			// reading between parantheses, there is no label delimiter
			delimiter = "";
		}

		if (command.endsWith("DELIM")) {
			int delimIndex = command.indexOf("DELIM") - 1;
			delimiter = command.substring(delimIndex, delimIndex + 1);

			command = command.substring(0, delimIndex);
		}

	}

	/**
	 * Returns the value for the DataFileColumn column for this command in the
	 * commandFile
	 *
	 * @return String value for the DataFileColumn column
	 */
	public String getColumnToStoreValueIn() {
		return columnToStoreValueIn;
	}

	/**
	 * Returns the value for the ScrollsUntil column for this command in the
	 * commandFile
	 *
	 * @return String scrollsUntil
	 */
	public String getScrollsUntil() {
		return scrollsUntil;
	}

	/**
	 * Returns the value for the VerificationExpectedValue column for this command
	 * in the commandFile
	 *
	 * @return String expected value
	 */
	public String getExpectedValue() {
		return expectedValue;
	}

	/**
	 * Returns the value for the command parsed from the Operation value
	 *
	 * @return String command
	 */
	public String getCommand() {
		return command;
	}

	/**
	 * Returns the value for the delimiter
	 *
	 * @return String delimiter
	 */
	public String getDelimiter() {
		return delimiter;
	}

	/**
	 * Returns the value for the VerficationLabel column for this command in the
	 * commandFile
	 *
	 * @return String value for the VerficationLabel column
	 */
	public String getVerificationLabel() {
		return verificationLabel;
	}

	/**
	 * Returns the value for the VerficationName column for this command in the
	 * commandFile
	 *
	 * @return String verification name
	 */
	public String getVerificationName() {
		return verificationName;
	}

	/**
	 * Returns the value for the reads between
	 *
	 * @return boolean TRUE if read between
	 */
	public boolean readBetween() {
		return readBetween;
	}

	/**
	 * Returns the value for the readBetweenCharacters
	 *
	 * @return boolean TRUE if read between characters
	 */
	public String getReadBetweenChars() {
		return readBetweenCharacters;
	}

	/**
	 * Returns the value for the startsWith
	 *
	 * @return String starts with
	 */
	public boolean startsWith() {
		return startsWith;
	}

	/**
	 * Returns the value for the ignoreSpaces
	 *
	 * @return boolean TRUE if ignore spaces
	 */
	public boolean getIgnoreSpaces() {
		return ignoreSpaces;
	}

	/**
	 * Returns the value for the split
	 *
	 * @return boolean TRUE if split
	 */
	public boolean isSplitValue() {
		return split;
	}

	/**
	 * Returns the value for the split
	 *
	 * @return boolean TRUE if all caps
	 */
	public boolean isAllCapsCheck() {
		return allCapsCheck;
	}

	/**
	 * Returns the value for the split delimiter
	 *
	 * @return String split delimiter
	 */
	public String getSplitDelimiter() {
		return splitDelimiter;
	}

	/**
	 * Returns the value for the split index
	 *
	 * @return int split index
	 */
	public int getSplitIndex() {
		return splitIndex;
	}

	/**
	 * Returns the value for the SysManOptionPrompt column for this command in the
	 * commandFile
	 *
	 * @return String prompt
	 */
	public String getPrompt() {
		return prompt;
	}

	/**
	 * Returns the value for the SysManPromptEntry column for this command in the
	 * commandFile
	 *
	 * @return String prompt entry
	 */
	public String getPromptEntry() {
		return promptEntry;
	}

	/**
	 * Returns the value for the TestDataID column for this command in the
	 * commandFile
	 *
	 * @return String testDataId
	 */
	public String getTestDataID() {
		return testDataID;
	}

	/**
	 * Returns the value for the ContinuePrompt column for this command in the
	 * commandFile
	 *
	 * @return String scrolling prompt
	 */
	public String getScrollingContinuePrompt() {
		return scrollPrompt;
	}

	/**
	 * Returns the value for the ContinueCommand column for this command in the
	 * commandFile
	 *
	 * @return String scroll command
	 */
	public String getScrollingContinueCommand() {
		return scrollCommand;
	}

	/**
	 * Method will pull the a value from the specified row in the commandFile. If
	 * the value is between {@code < >}, look up the value in the data file (in the
	 * specified row).
	 *
	 * If the value in the command file says to look up in the datafile, and the
	 * datafile or the specified row in the datafile, do not exist, throws an
	 * InvalidDataException
	 *
	 *
	 * @param commandFile           file
	 * @param commandFileRowIndex   index
	 * @param commandFileColumn     column
	 * @param dataFile              data file
	 * @param dataFileRowIndex      data file index
	 * @param secondaryDataFile     secondary data file
	 * @param secondaryFileRowIndex secondary data row index
	 * @param params                hashmap
	 * @return String value
	 */
	protected String lookupCommandFileValue(ExcelDataFile commandFile, int commandFileRowIndex,
			String commandFileColumn, ExcelDataFile dataFile, int dataFileRowIndex, ExcelDataFile secondaryDataFile,
			int secondaryFileRowIndex, HashMap<String, String> params) {
		String value = commandFile.getData(commandFileRowIndex, commandFileColumn);
		// check to see if this is System prompt to be pulled from the
		// connection file
		if (value.startsWith("<") && value.endsWith(">")) {
			value = pullValueFromDataFile(value, dataFile, dataFileRowIndex);
		}

		// pull from the secondary data file
		if (value.startsWith("%") && value.endsWith("%")) {
			value = pullValueFromDataFile(value, secondaryDataFile, secondaryFileRowIndex);

		}

		// pull from the HashMap
		if (params != null && value.startsWith("||") && value.endsWith("||")) {
			String key = value.substring(2, value.length() - 2);
			value = params.get(key);
		}

		return value;

	}

	/**
	 * Return the value from the data file
	 *
	 * @param commandFileValue command file column header
	 * @param dataFile         data file
	 * @param dataFileRowIndex row index
	 * @return String value
	 */
	protected String pullValueFromDataFile(String commandFileValue, ExcelDataFile dataFile, int dataFileRowIndex) {
		// throw an exception if it is configured to pull from a datafile, but
		// there is no datafile
		if (dataFile == null || dataFileRowIndex <= 0) {
			throw new InvalidDataException("Command File is trying to pull data from a data file that does not exist");
		}

		// Pull the value form the datafile
		// Strip off the < > or % % brackets to get the column name for the
		// dataFile
		String dataFileColumnHeader = commandFileValue.substring(1, commandFileValue.length() - 1);
		return dataFile.getData(dataFileRowIndex, dataFileColumnHeader);
	}

}
