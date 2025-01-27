package platformIndependentCore.datafiles;

import java.util.HashMap;

/**
 * Class to represent a COMMAND within a Command File that is between
 * STARTSCROLLGROUP and ENDSCROLLGROUP rows This class extends ExcelFileCommand
 * and adds an 'executed' flag
 *
 * @author vbaaustaylol
 *
 */
public class ExcelFileScrollingCommand extends ExcelFileCommand {
	boolean executed = false;

	/**
	 * Creates an ExcelFileScrollingCommand with two datafiles
	 *
	 * @param commandFile           cmd file
	 * @param commandFileIndex      cmd file index
	 * @param dataFile              data file
	 * @param dataFileRowIndex      data file index
	 * @param secondaryDataFile     secondary data file
	 * @param secondaryFileRowIndex secondary file index
	 * @param defaultDelimiter      default delimiter
	 */
	public ExcelFileScrollingCommand(ExcelDataFile commandFile, int commandFileIndex, ExcelDataFile dataFile,
			int dataFileRowIndex, ExcelDataFile secondaryDataFile, int secondaryFileRowIndex, String defaultDelimiter) {
		super(commandFile, commandFileIndex, dataFile, dataFileRowIndex, secondaryDataFile, secondaryFileRowIndex, null,
				defaultDelimiter);

	}

	/**
	 * Creates an ExcelScrollingCommand with one datafile
	 *
	 * @param commandFile           cmd file
	 * @param commandFileTestDataID testDataId for cmd file
	 * @param dataFile              data file
	 * @param dataFileTestDataID    testDataId for cmd file
	 * @param defaultDelimiter      default delimiter
	 */
	public ExcelFileScrollingCommand(ExcelDataFile commandFile, String commandFileTestDataID, ExcelDataFile dataFile,
			String dataFileTestDataID, String defaultDelimiter) {
		super(commandFile, commandFile.getRecordIndex(commandFileTestDataID), dataFile,
				dataFile.getRecordIndex(dataFileTestDataID), null, -1, null, defaultDelimiter);

	}

	/**
	 * Creates an ExcelFileScrollingCommand with one datafile and a hashmap
	 *
	 * @param commandFile           cmd file
	 * @param commandFileTestDataID testDataId for cmd file
	 * @param dataFile              data file
	 * @param dataFileTestDataID    testDataId for cmd file
	 * @param params                hashmap
	 * @param defaultDelimiter      default delimiter
	 */
	public ExcelFileScrollingCommand(ExcelDataFile commandFile, String commandFileTestDataID, ExcelDataFile dataFile,
			String dataFileTestDataID, HashMap<String, String> params, String defaultDelimiter) {
		super(commandFile, commandFile.getRecordIndex(commandFileTestDataID), dataFile,
				dataFile.getRecordIndex(dataFileTestDataID), null, -1, params, defaultDelimiter);

	}

	/**
	 * Returns true if this command has executed, false if it has not
	 *
	 * @return boolean TRUE if executed, FALSE if not
	 */
	public boolean hasExecuted() {
		return executed;
	}

	/**
	 * Sets the executed value to true
	 */
	public void setExecuted() {
		executed = true;
	}

}
