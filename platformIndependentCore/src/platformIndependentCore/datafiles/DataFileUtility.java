package platformIndependentCore.datafiles;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.IOUtils;

import platformIndependentCore.exceptions.InvalidDataException;
import platformIndependentCore.exceptions.InvalidParameterException;
import platformIndependentCore.exceptions.WrappedException;
import platformIndependentCore.utilities.CryptoUtils;
import platformIndependentCore.utilities.DateCalculator;
import utilities.DataFileHelper;

/**
 * <b>Name :</b> DataFileUtility.java
 * <p>
 * <b>Generated :</b> Oct 15, 2020
 * <p>
 * <b>Description :</b> Class to handle copy data between local and master data
 * sets. It will handle connecting to a share drive that requires authentication
 * and it will create backups of any master data files that are being updated
 * <p>
 *
 * @since Oct 15, 2020
 * @author VBAAUSTAYLOL
 */
public abstract class DataFileUtility {

	/**
	 * Will copy the specified data set from the local data set to the master
	 * location. It will also create a backup of the current version in master
	 * before copying. This method will copy the entire file. <br>
	 * This method is currently protected to restrict access until we know if there
	 * is a need for it
	 *
	 * @param dataSetName    the name of the local data set file to be copied to
	 *                       master location
	 * @param masterLocation the location for the master data set
	 */
	protected static void copyLocalDataSetToMaster(String dataSetName, Enum<?> masterLocation) {
		CsvDataFile localDataFile = DataFileHelper.getDataFile(dataSetName);
		File backupFile = getBackupFile(dataSetName, masterLocation);
		File masterFile = getCurrentDataFile(dataSetName, masterLocation);
		CsvDataFile masterDataFile = new CsvDataFile(masterFile.getAbsolutePath());
		// copy the local file to master (will first create a backup of master)
		copyDataSet(localDataFile, masterDataFile, backupFile);
	}

	/**
	 * Will copy the specified data set from the local data set to the main/master
	 * location. It will also create a backup of the current version in main/master
	 * before copying. This method will copy the entire file. <br>
	 * This method is currently protected to restrict access until we know if there
	 * is a need for it
	 *
	 * @param dataSetName    the name of the local data set file to be copied to
	 *                       master location
	 * @param mainLocation the location for the main/master data set
	 */
	protected static void copyNewDataSetToMain(String dataSetName, Enum<?> mainLocation) {
		CsvDataFile localDataFile = DataFileHelper.getDataFile(dataSetName);
		File mainFile = getCurrentDataFile(dataSetName, mainLocation);
		if (mainFile.exists()) {
			throw new InvalidParameterException(
					"The file specified: " + dataSetName + " already exists in " + mainLocation.toString());
		}
		// copy the local file to master
		copyNewDataSet(localDataFile, mainFile);
	}

	/**
	 * Will copy the source data set file to the destination data set file. It will
	 * also create a backup of the current version in destination before copying.
	 * This method will copy the entire file. <br>
	 * This method is currently protected to restrict access until we know if there
	 * is a need for it
	 *
	 * @param sourceDataFile      the data set file to be copied over
	 * @param destinationDataFile the location of the destination data set (will be
	 *                            backed up)
	 * @param backupFile          location to backup the destination data set file
	 *                            before the copy is executed
	 */
	private static void copyDataSet(CsvDataFile sourceDataFile, CsvDataFile destinationDataFile, File backupFile) {
		File destinationFile = destinationDataFile.getFile();
		try {
			// Create the back up of the existing master data set
			backUpFile(destinationFile, backupFile);

			// Copy the local data set file to the master data set location
			Files.copy(sourceDataFile.getFile().toPath(), destinationFile.toPath(),
					StandardCopyOption.REPLACE_EXISTING);
			System.out.println("Data file [" + sourceDataFile.getFile().getAbsolutePath() + "] has been copied over to "
					+ destinationFile.getAbsolutePath());

		} catch (IOException e) {
			e.printStackTrace();
			// Let the user know there was an exception during the copy, so rethrow
			throw new WrappedException(e);
		}
	}

	/**
	 * Will copy the source data set file to the destination data set file. This
	 * method will copy the entire file. <br>
	 * This method is currently protected to restrict access until we know if there
	 * is a need for it
	 *
	 * @param sourceDataFile  the data set file to be copied over
	 * @param destinationFile the location of the destination data set (will be
	 *                        backed up)
	 */
	private static void copyNewDataSet(CsvDataFile sourceDataFile, File destinationFile) {
		try {
			// Copy the local data set file to the master data set location
			Files.copy(sourceDataFile.getFile().toPath(), destinationFile.toPath(),
					StandardCopyOption.REPLACE_EXISTING);
			System.out.println("Data file [" + sourceDataFile.getFile().getAbsolutePath() + "] has been copied over to "
					+ destinationFile.getAbsolutePath());

		} catch (IOException e) {
			e.printStackTrace();
			// Let the user know there was an exception during the copy, so rethrow
			throw new WrappedException(e);
		}
	}

	/**
	 * Will copy the specified data set from the source location to the destination
	 * location. It will also create a backup of the current version in destination
	 * before copying. This method will copy the entire file. This method handles
	 * authentication to a password protected destination/drive for the destination
	 * data sets. The required credentials are provided in the authentication file
	 * <br>
	 * This method is currently protected to restrict access until we know if there
	 * is a need for it
	 *
	 * @param dataSetName         name of the data set file to be copied
	 * @param sourceLocation      the location to copy from
	 * @param destinationLocation the location to copy to
	 */
	protected static void copyDataSetWithAuthentication(String dataSetName, Enum<?> sourceLocation,
			Enum<?> destinationLocation) {
		// Get an instance of the AuthenticationFile to parse out credentials
		AuthenticationFile authentication = new AuthenticationFile();
		// Map the destination/master location
		mapDrive(destinationLocation, authentication);
		// Now that the drive is mapped, we can copy data sets
		CsvDataFile sourceDataFile = new CsvDataFile(getCurrentDataFile(dataSetName, sourceLocation).getAbsolutePath());
		File backupFile = getBackupFile(dataSetName, destinationLocation);
		File destinationFile = getCurrentDataFile(dataSetName, destinationLocation);
		CsvDataFile destinationDataFile = new CsvDataFile(destinationFile.getAbsolutePath());
		// Now that I have the file objects, execute the backup and copy
		copyDataSet(sourceDataFile, destinationDataFile, backupFile);

		// remove the drive mapping
		unmapDrive();
	}

	/**
	 * Will copy the entry with matching data id in the local data set to the master
	 * location. It will also create a backup of the current version in master
	 * before copying. This method will not copy the entire data set, just the entry
	 * for the specified data id
	 *
	 * @param dataSetName    name of the data set file to be copied to master
	 * @param dataId         row to copy from local data set to the master data set
	 * @param newRow         TRUE if the row to copy is new, FALSE if it exists in
	 *                       the master data set already
	 * @param masterLocation the location for the master data set
	 * @throws InvalidDataException if the column headers in master do not match
	 *                              those in local
	 */
	protected static void copyLocalRowToMaster(String dataSetName, String dataId, boolean newRow,
			Enum<?> masterLocation) {
		CsvDataFile localDataFile = DataFileHelper.getDataFile(dataSetName);
		File backupFile = getBackupFile(dataSetName, masterLocation);
		File masterFile = getCurrentDataFile(dataSetName, masterLocation);
		CsvDataFile masterDataFile = new CsvDataFile(masterFile.getAbsolutePath());
		copyRowFromSourceToDestination(dataId, newRow, localDataFile, masterDataFile, backupFile);
	}

	/**
	 * Will copy the row with matching data id in the source data set to the
	 * destination location. It will also create a backup of the current version in
	 * destination before copying. This method will not copy the entire data set,
	 * just the entry for the specified data id. This method handles authentication
	 * to a password protected destination/drive for the destination data sets. The
	 * required credentials are provided in the authentication file
	 *
	 * NOTE: making this deprecated until we determine if there is a need for this.
	 * We don't want a user to grab a data file that is not fully updated (if
	 * multiple rows need updating)
	 *
	 * @param dataSetName                    name of the data set file to be copied
	 * @param dataId                         row to copy from source data set to the
	 *                                       master data set
	 * @param newRow                         TRUE if the row to copy is new, FALSE
	 *                                       if it exists in the master data set
	 *                                       already
	 * @param sourceLocation                 the location of the source data set to
	 *                                       copy the row from to the destination
	 *                                       data file
	 * @param authenticationRequiredLocation the location for the destination data
	 *                                       set
	 */
	@Deprecated
	protected static void copyRowWithAuthentication(String dataSetName, String dataId, boolean newRow,
			Enum<?> sourceLocation, Enum<?> authenticationRequiredLocation) {
		// Get an instance of the AuthenticationFile to parse out credentials
		AuthenticationFile authentication = new AuthenticationFile();
		// Map the destination/master location
		mapDrive(authenticationRequiredLocation, authentication);
		// Now that the drive is mapped, we can copy data sets
		CsvDataFile sourceDataFile = new CsvDataFile(sourceLocation + "/" + dataSetName);
		File backupFile = getBackupFile(dataSetName, authenticationRequiredLocation);
		File destinationFile = getCurrentDataFile(dataSetName, authenticationRequiredLocation);
		CsvDataFile destinationDataFile = new CsvDataFile(destinationFile.getAbsolutePath());

		copyRowFromSourceToDestination(dataId, newRow, sourceDataFile, destinationDataFile, backupFile);
		// remove the drive mapping
		unmapDrive();
	}

	/**
	 * This method deletes the row of the specified dataId from the specified data
	 * set, after creating a backup of the current file.
	 *
	 * @param dataSetName    name of data file
	 * @param dataId         to identify row to delete
	 * @param masterLocation location to remove file from
	 */
	protected static void deleteRowFromFile(String dataSetName, String dataId, Enum<?> masterLocation) {
		File backupFile = getBackupFile(dataSetName, masterLocation);
		File currentFile = getCurrentDataFile(dataSetName, masterLocation);
		CsvDataFile dataSet = DataFileHelper.getDataFile(currentFile.getAbsolutePath());
		backUpFile(dataSet.getFile(), backupFile);
		dataSet.deleteRowFromDataSheet(dataId);

		// Re-populate the index column after deletion of row, if column exists
		if (dataSet.isColumnHeaderPresent("0")) {
			List<String> scriptIds = dataSet.getDataIds();
			int count = 1;
			for (String scriptId : scriptIds) {
				dataSet.writeToDataSheet(scriptId, "0", String.valueOf(count));
				count++;
			}
		}

		System.out.println("The row for: " + dataId + " has been sucessfully deleted.");
	}

	/**
	 * Method will move the data files from sourceFolder to the destinationFolder
	 * after backing up the sourceFolder. It will also back up any files that have
	 * differences between the source folder and the destination folder in the
	 * destination's backup location.
	 *
	 * @param sourceFolder      - Folder to copy files from
	 * @param destinationFolder - Folder to copy files to
	 */
	protected static void backupAndMoveDataSets(Enum<?> sourceFolder, Enum<?> destinationFolder) {

		String sourceBackupLocation = getBackupFileLocation(sourceFolder);
		String sourceLocation = getCurrentFileLocation(sourceFolder);
		String destinationBackupLocation = getBackupFileLocation(destinationFolder);
		String destinationLocation = getCurrentFileLocation(destinationFolder);

		DataFileHelper.backUpAllDataSets(sourceLocation, sourceBackupLocation);
		DataFileHelper.moveAllDataSets(sourceLocation, destinationLocation, destinationBackupLocation);
	}

	/**
	 * Method will connect and authenticate to the protected destinationFolder. Then
	 * copy the data sets from the specified source folder to the specified
	 * destinationFolder
	 *
	 * @param sourceFolder      - Folder to copy files from
	 * @param destinationFolder - Folder to copy files to
	 */
	protected static void copyDataSetsWithAuthentication(Enum<?> sourceFolder, Enum<?> destinationFolder) {
		// Get an instance of the AuthenticationFile to parse out credentials
		AuthenticationFile authentication = new AuthenticationFile();
		// Map the destination/master location
		mapDrive(destinationFolder, authentication);
		// Now that the drive is mapped, we can copy data sets
		copyDataSets(sourceFolder, destinationFolder);
		// remove the drive mapping
		unmapDrive();
	}

	/**
	 * Copies all data sets from the sourceFolder to the destinationFolder, backing
	 * up any files in the destination that differ from the source.
	 *
	 * @param sourceFolder      Folder to copy files from
	 * @param destinationFolder Folder to copy files to
	 */
	protected static void copyDataSets(Enum<?> sourceFolder, Enum<?> destinationFolder) {
		String sourceLocation = getCurrentFileLocation(sourceFolder);
		String destinationBackupLocation = getBackupFileLocation(destinationFolder);
		String destinationLocation = getCurrentFileLocation(destinationFolder);

		DataFileHelper.copyAllDataSets(sourceLocation, destinationLocation, destinationBackupLocation);
	}

	/**
	 * Will copy the entry with matching data id in the source data set to the
	 * destination location. It will also create a backup of the current version in
	 * destination before copying. This method will not copy the entire data set,
	 * just the entry for the specified data id
	 *
	 * @param dataId              row to copy from local data set to the master data
	 *                            set
	 * @param newRow              TRUE if the row to copy is new, FALSE if it exists
	 *                            in the master data set already
	 * @param sourceDataFile      file to be copied over to destination
	 * @param destinationDataFile the location for the data set to be copied to
	 * @param backupFile          File to contain the back up of the destination
	 *                            file prior to the copy
	 * @throws InvalidDataException if the column headers in source do not match
	 *                              those in destination
	 */
	private static void copyRowFromSourceToDestination(String dataId, boolean newRow, CsvDataFile sourceDataFile,
			CsvDataFile destinationDataFile, File backupFile) {
		File destinationFile = destinationDataFile.getFile();
		// Create the back up of the existing data set
		backUpFile(destinationFile, backupFile);

		// Now we need to copy over just the one row of data from the local data set to
		// the master data set
		Set<String> columns = sourceDataFile.getColumns();
		Set<String> masterColumns = destinationDataFile.getColumns();
		if (!columns.containsAll(masterColumns) || !masterColumns.containsAll(columns)) {
			// The column headers are different in the two files. Need to halt
			throw new InvalidDataException("Can not copy " + sourceDataFile.getFile().getAbsolutePath() + " to "
					+ destinationFile.getAbsolutePath() + " because their column headers do not match.");
		}

		if (newRow) {
			destinationDataFile.addNewRow(dataId);
		}
		for (String currColumn : columns) {
			if (currColumn.equals("0")) {
				continue;
			}
			String localValue = sourceDataFile.getData(dataId, currColumn);
			destinationDataFile.writeToDataSheet(dataId, currColumn, localValue);
		}
		System.out.println(
				"TEST DATA ID=" + dataId + "\n from SOURCE DATA FILE [" + sourceDataFile.getFile().getAbsolutePath()
						+ "]\n COPIED TO: " + destinationDataFile.getFile().getAbsolutePath());
	}

	/**
	 * Will return the File object for the data set in the specified location
	 *
	 * @param dataSetName the data set
	 * @param location    base location for data sets
	 * @return File data set file
	 */
	private static File getFile(String dataSetName, Enum<?> location) {
		return new File(location + "//" + dataSetName);
	}

	/**
	 * Returns the name of the current folder for the specified location
	 *
	 * @param location location of data set
	 * @return String backup location
	 */
	private static String getCurrentFileLocation(Enum<?> location) {
		return location + "\\Current\\";
	}

	/**
	 * Will return a File for the a Current data file<br>
	 * Current data files are stored in a 'Current' folder off of the base location
	 *
	 *
	 * @param dataSetName name of the data set file
	 * @param location    the location for the data set
	 * @return File for current data set
	 */
	private static File getCurrentDataFile(String dataSetName, Enum<?> location) {
		return getFile("/Current/" + dataSetName, location);
	}

	/**
	 * Returns the name of the backup folder for the specified destinationLocation
	 *
	 * @param destinationLocation location data set will be copied to
	 * @return String backup location
	 */
	private static String getBackupFileLocation(Enum<?> destinationLocation) {
		return destinationLocation + "\\BackUps\\";
	}

	/**
	 * Will return the File object for the Backup file where the destination data
	 * set will be copied to before being updated<br>
	 * Backups are stored in a 'BackUps' folder off of the base location
	 *
	 * @param dataSetName         the name of the data set
	 * @param destinationLocation location data set will be copied to
	 * @return File backup file
	 */
	private static File getBackupFile(String dataSetName, Enum<?> destinationLocation) {
		// Back Ups are stored in a BackUps folder off of the base location
		String destinationBackupLocation = getBackupFileLocation(destinationLocation) + dataSetName.replace(".csv", "");

		// Create the back up file name by inserting a timestamp
		// First, strip off any leading folders so the file isn't double-nested
		String fileName = dataSetName.substring(dataSetName.lastIndexOf("\\") + 1, dataSetName.lastIndexOf(".csv") + 4);
		String username = System.getProperty("user.name");
		File backupFile;
		boolean fileExists;
		do {
			fileExists = false;
			String backupName = fileName.replace(".csv",
					"_" + username + "_" + DateCalculator.getCurrentDate("MMM dd yyyy HH mm ss") + ".csv");

			backupFile = new File(destinationBackupLocation + "//" + backupName);

			if (backupFile.exists()) {
				fileExists = true;
				// Sleep to ensure there are no conflicts with back up file names
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} while (fileExists);

		return backupFile;
	}

	/**
	 * Will back up the specified file to the destination backup file
	 *
	 * @param file       to be backed up
	 * @param backupFile location to create the backup
	 */
	private static void backUpFile(File file, File backupFile) {
		// Make sure the back up folder exists, if not, create it
		if (!backupFile.getParentFile().exists()) {
			backupFile.getParentFile().mkdirs();
		}
		// Create the back up of the existing master data set
		try {
			Files.copy(file.toPath(), backupFile.toPath());
			System.out.println(
					"Data file [" + file.getAbsolutePath() + "] has been backed up to " + backupFile.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
			// Re-throwing to let the user know there was an exception during the copy
			throw new WrappedException(e);
		}
	}

	/**
	 * Will map the master data set location to a drive to allow the copies to take
	 * place. This will map to the M: drive. If the M: is in use on the machine,
	 * must update this code
	 *
	 * @param masterLocation location for master data sets
	 *
	 * @param authentication file containing credentials for drive where master data
	 *                       sets are
	 */
	private static void mapDrive(Enum<?> masterLocation, AuthenticationFile authentication) {
		String drive = masterLocation.toString();
		// Add some output so user is aware of progress
		System.out.println("Connecting to master data set location: " + drive);
		String cmd = "net use M: \"" + drive + "\" /user:" + authentication.getUserName() + " "
				+ authentication.getPassword();
		executeCmd(cmd);
	}

	/**
	 * Will remove the M: drive mapping for the master data set location
	 */
	private static void unmapDrive() {
		// Add some output so user is aware of progress
		System.out.println("Disconnecting from master data set location.");
		executeCmd("net use /del M:");
	}

	/**
	 * Will execute a command via cmd line
	 *
	 * @param cmd to execute
	 */
	private static void executeCmd(String cmd) {
		try {
			Process process = Runtime.getRuntime().exec(cmd);
			InputStream inputStream = process.getInputStream();
			String text = IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());

			int exitCode = process.exitValue();
			if (exitCode == 0) {
				System.out.println("> Completed successfully");
			} else {
				System.out.println("CMD did not complete succesfully:");
				System.out.println("REQUEST EXIT CODE: " + exitCode);
				System.out.println("EXIT TEXT: " + text);
			}
			process.destroy();
		} catch (IOException e1) {
			// Don't halt execution
			e1.printStackTrace();
		}
	}
}

/**
 * Class to handle interactions with the authentication file for the Lab File
 * Share It requires the sharedrive.properties file and the auth.key file to be
 * in the C:/Automation/Tools folder
 *
 * This authentication file will hold the encrypted value for user name and
 * password separated by a colon
 *
 * @author vbaaustaylol
 *
 */
class AuthenticationFile {
	/** key file */
	final String KEY_FILE = "c:/Automation/Tools/properties/auth.key";
	/** password file */
	final String PWD_FILE = "c:/Automation/Tools/sharedrive.properties";

	/** password */
	private String pw = "";
	/** user name */
	private String userName = "";

	/**
	 * Will create an instance of Authentication file. This file is for accessing
	 * the LabShares, the required files are to be in C:/Automation/Tools
	 */
	AuthenticationFile() {
		decryptAuthFile();
	}

	/**
	 * Will decrypt and parse the authentication credentials from the file
	 */
	private void decryptAuthFile() {
		try {
			Properties p2 = new Properties();
			p2.load(new FileReader(PWD_FILE));
			String encryptedPwd = p2.getProperty("pwd");
			// System.out.println("ENCRYPTED=" + encryptedPwd);
			String decrypted = CryptoUtils.decrypt(encryptedPwd, new File(KEY_FILE));
			// System.out.println("DECRYPTED=" + decrypted);
			String[] auth = decrypted.split(":");
			if (auth.length < 2) {
				throw new InvalidDataException("Problem with your authentication file");
			}
			userName = auth[0];
			pw = auth[1];
		} catch (IOException e) {
			throw new WrappedException(e);
		}
	}

	/**
	 * Return the password value
	 *
	 * @return String password
	 */
	String getPassword() {
		return pw;
	}

	/**
	 * Return the user name value
	 *
	 * @return String user name
	 */
	String getUserName() {
		return userName;
	}

}
