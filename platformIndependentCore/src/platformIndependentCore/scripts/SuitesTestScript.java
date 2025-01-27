package platformIndependentCore.scripts;

import java.util.List;

import platformIndependentCore.datafiles.CsvDataFile;
import platformIndependentCore.datafiles.DataFile;
import platformIndependentCore.utilities.ConfigProperties;

/**
 * Abstract class extends TestScript and provides convenience methods for
 * resetting Test Execution values in suites
 *
 * @author vbaaustaylol
 *
 */
public abstract class SuitesTestScript extends TestScript {
	/** SKIP */
	private static final String SKIP = "SKIP";
	/** RUN */
	private static final String RUN = "RUN";

	/**
	 * Set all scripts to RUN
	 *
	 * @param dataFile suite
	 */
	protected void setAllToRun(CsvDataFile dataFile) {
		setAll(dataFile, RUN);
	}

	/**
	 * Set all scripts to SKIP
	 *
	 * @param dataFile suite
	 */
	protected void setAllToSkip(CsvDataFile dataFile) {
		setAll(dataFile, SKIP);
	}

	/**
	 * Set all scripts to specified value (RUN or SKIP)
	 *
	 * @param dataFile suite
	 * @param value    for the test execution column (for all rows)
	 */
	private void setAll(CsvDataFile dataFile, String value) {
		List<String> scriptIds = dataFile.getDataIds();
		for (String scriptId : scriptIds) {
			dataFile.writeToDataSheet(scriptId, getExecutionColumnName(), value);
		}
	}

	/**
	 * Set all scripts to SKIP, except those specified, which will be set to RUN
	 *
	 * @param dataFile  suite
	 * @param scriptIds test scripts to run
	 */
	protected void setRunOnly(CsvDataFile dataFile, String... scriptIds) {
		setAllToSkip(dataFile);
		for (String scriptId : scriptIds) {
			dataFile.writeToDataSheet(scriptId, getExecutionColumnName(), RUN);
		}
	}

	/**
	 * Will return the configured Execution Column name, or return the default
	 *
	 * @return String Execution column name
	 */
	private String getExecutionColumnName() {
		return ConfigProperties.getValue(ConfigProperties.TEST_EXECUTION_COLUMN, DataFile.TEST_EXECUTION_COLUMN);
	}
}
