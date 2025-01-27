package utilities;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.swing.JOptionPane;

import platformIndependentCore.datafiles.CsvDataFile;
import platformIndependentCore.exceptions.WrappedException;
import platformIndependentCore.utilities.ConfigProperties;

/**
 * Tool to copy data set files and folders from one location to another
 *
 * @author VBAAUSTAYLOL
 *
 */
public class DataFileHelper {

	/**
	 * Will copy a single file to the specified destination folder.
	 *
	 * The source file to be copied and the destination folder should be given
	 * relative to the current project
	 *
	 * @param sourceFile        - file to copy
	 * @param destinationFolder - location to copy the file to
	 */
	public static void copyDataSetFile(String sourceFile, String destinationFolder) {
		File projectDir = new File("");
		File source = new File(projectDir.getAbsolutePath() + "/" + sourceFile);
		String fileName = source.getName();

		copyAndRenameFile(sourceFile, destinationFolder, fileName);
	}

	/**
	 * Will copy a single file to the specified destination folder.
	 *
	 * The source file to be copied and the destination folder should be given
	 * relative to the current project
	 *
	 * @param sourceFile        - file to copy
	 * @param destinationFolder - location to copy the file to
	 * @param newFileName       - New name of the file after the copy
	 */
	public static void copyAndRenameFile(String sourceFile, String destinationFolder, String newFileName) {
		Exception caughtException = null;
		File projectDir = new File("");
		File source = new File(projectDir.getAbsolutePath() + "/" + sourceFile);
		if (source.exists()) {
			File dest = new File(projectDir.getAbsolutePath() + "/" + destinationFolder + "/" + newFileName);
			String currDateTime = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());

			try {
				// First create a backup
				// Set the default to current working folder + _results
				File file = new File("");
				String backupLocation = file.getAbsolutePath() + "_backups\\" + newFileName.replace(".", "_") + "_"
						+ currDateTime;
				File backupFolder = new File(backupLocation);
				boolean backup = copyUtil(source, dest, backupFolder, new ArrayList<String>());
				showCompletedMessage(source.getAbsolutePath(), dest.getAbsolutePath(), backup, backupLocation);

			} catch (IOException e) {
				e.printStackTrace();
				showErrorPopup(e.getMessage());
				caughtException = e;
			}
		} else {
			showSourceDoesNotExist(source);
		}
		if (caughtException != null) {
			throw new WrappedException(caughtException);
		}

	}

	/**
	 * Method will copy the datafiles from masterDataSets to localDataSets for the
	 * specified project/application
	 *
	 * The sourceFolder and destination folder should be given relative to the
	 * current project
	 *
	 * The exclusions parameters are optional. If you have exclusions, pass in each
	 * individually. If you do not have any exclusions, you need to only provide the
	 * sourceFolder and destinationFolder
	 *
	 * @param sourceFolder      - Folder to copy files from
	 * @param destinationFolder - Folder to copy files to
	 * @param exclusions        - OPTIONAL Any listed folders or files will not be
	 *                          copied
	 */
	public static void copyDataSets(String sourceFolder, String destinationFolder, String... exclusions) {
		String currDateTime = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
		File projectDir = new File("");
		String folderName = destinationFolder;
		if (folderName.contains("\\")) {
			folderName = folderName.substring(folderName.lastIndexOf("\\") + 1);
		}
		String projectLocation = projectDir.getAbsolutePath();
		String backupLocation = projectLocation.substring(0, projectLocation.lastIndexOf("\\")) + "_backups\\"
				+ folderName + "_" + currDateTime;

		List<String> excludedList = Arrays.asList(exclusions);
		File source = new File(sourceFolder);
		if (!source.exists()) {
			showSourceDoesNotExist(source);
		} else {
			File dest = new File(projectDir.getAbsolutePath() + "/" + destinationFolder);
			// only need to back up the file if it already exists in the
			// destination location
			if (dest.isFile() || dest.exists() && dest.isDirectory()) {
				File backup = new File(backupLocation);
				boolean backupMade = false;
				try {
					backupMade = copyUtil(new File(sourceFolder), new File(destinationFolder), backup, excludedList);
				} catch (IOException e) {
					e.printStackTrace();
				}

				if (!backupMade && backup.exists()) {
					backup.delete();
				}
				showCompletedMessage(source.getAbsolutePath(), dest.getAbsolutePath(), backupMade, backupLocation);
			} else {
				showDestinationDoesNotExist(dest);
			}
		}
	}

	/**
	 * Method will back up all the datasets from the specified sourceFolder into the
	 * specified backUpFolder, in a sub-folder "AllFilesBackUp_{dateTimeStamp}"
	 *
	 * @param sourceFolder folder to backup all files in
	 * @param backUpFolder folder to put all backed up files in
	 */
	public static void backUpAllDataSets(String sourceFolder, String backUpFolder) {
		String currDateTime = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());

		String sourceBackupLocation = backUpFolder + "AllFilesBackUp_" + currDateTime;

		File source = new File(sourceFolder);
		if (!source.exists()) {
			showSourceDoesNotExist(source);
		} else {
			try {
				copyAllFilesUtil(new File(sourceFolder), new File(sourceBackupLocation));
				System.out.println("*************************************************************************");
				System.out.println(sourceFolder + " was backed up to " + sourceBackupLocation);
				System.out.println("*************************************************************************");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Method will move the data files from sourceFolder to the destinationFolder,
	 * backing up any destination files that are different from the source files.
	 *
	 * @param sourceFolder            - Folder to copy files from
	 * @param destinationFolder       - Folder to copy files to
	 * @param destinationBackUpFolder - Folder to back up destination files to
	 *
	 */
	public static void moveAllDataSets(String sourceFolder, String destinationFolder, String destinationBackUpFolder) {
		String currDateTime = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());

		// Move data sets from source to destination
		File dest = new File(destinationFolder);
		String destinationBackUpLocation = destinationBackUpFolder.substring(0,
				destinationBackUpFolder.lastIndexOf("\\") + 1) + currDateTime;
		// only need to back up the file if it already exists in the
		// destination location
		if (dest.isFile() || dest.exists() && dest.isDirectory()) {
			boolean backupMade = false;
			File backup = new File(destinationBackUpFolder);
			try {
				List<String> excludedList = Arrays.asList("");
				backupMade = moveUtil(new File(sourceFolder), dest, new File(destinationBackUpLocation), excludedList);
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (!backupMade && backup.exists()) {
				backup.delete();
			}

			String message = "Datasets successfully moved from:\n" + sourceFolder + "\nto:\n" + destinationFolder
					+ "\n\nThere was no need for a backup.";
			if (backupMade && backup.exists()) {
				message = "Datasets successfully moved from:\n" + sourceFolder + "\nto:\n" + destinationFolder
						+ "\n\nAs a precaution a back up of existing datasets was stored at: \n\n"
						+ destinationBackUpLocation
						+ "\n\nOnly files that differed between your source and destination were backed up. \n\nPlease be sure to remove backup files once the intergrity of your data has been validated \nand this backup is no longer needed.";

			}
			System.out.println("*************************************************************************");
			System.out.println(message);
			System.out.println("*************************************************************************");
		} else {
			showDestinationDoesNotExist(dest);
		}

	}

	/**
	 * Method will copy the data files from sourceFolder to the destinationFolder
	 * after backing up the destinationFolder to the specified
	 * destinationBackUpFolder
	 *
	 * @param sourceFolder            - Folder to copy files from
	 * @param destinationFolder       - Folder to copy files to
	 * @param destinationBackUpFolder - Folder to back up destination files to
	 *
	 */
	public static void copyAllDataSets(String sourceFolder, String destinationFolder, String destinationBackUpFolder) {
		String currDateTime = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());

		// copy data sets from source to destination
		File dest = new File(destinationFolder);
		String destinationBackUpLocation = destinationBackUpFolder.substring(0,
				destinationBackUpFolder.lastIndexOf("\\") + 1) + currDateTime;
		// only need to back up the file if it already exists in the
		// destination location
		if (dest.isFile() || dest.exists() && dest.isDirectory()) {
			boolean backupMade = false;
			File backup = new File(destinationBackUpLocation);
			try {
				List<String> excludedList = Arrays.asList("");
				backupMade = copyUtil(new File(sourceFolder), dest, backup, excludedList);
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (!backupMade && backup.exists()) {
				backup.delete();
			}
			showCompletedMessage(sourceFolder, dest.getAbsolutePath(), backupMade, destinationBackUpLocation);

		} else {
			showDestinationDoesNotExist(dest);
		}
	}

	/**
	 * Will copy all files and folders under the sourceLocation unless it is
	 * included as an exclusion
	 *
	 * @param sourceLocation - Folder to copy files from
	 * @param targetLocation - Folder to copy files to
	 * @param backupLocation - Folder to save backup of target
	 * @param exclusions     - OPTIONAL Any listed folders or files will not be
	 *                       copied
	 * @return boolean - TRUE if backup was made, FALSE if it was not
	 * @throws IOException - Signals that an I/O exception of some sort has occurred
	 */
	private static boolean copyUtil(File sourceLocation, File targetLocation, File backupLocation,
			List<String> exclusions) throws IOException {
		boolean backupMade = false;
		Exception caughtException = null;
		// First check if the current source is in the exclusions list. If it
		// matches, skip copy and backup
		if (!exclusions.contains(sourceLocation.getName())) {
			if (!sourceLocation.getAbsolutePath().contains("Backup")) {
				if (sourceLocation.isDirectory()) {
					File newBackup = new File(backupLocation.getAbsolutePath(), targetLocation.getName());
					String[] children = sourceLocation.list();
					for (int i = 0; i < children.length; i++) {
						if (!exclusions.contains(children[i])) {
							copyUtil(new File(sourceLocation, children[i]), new File(targetLocation, children[i]),
									newBackup, exclusions);
						} else {
							System.out.println("EXCLUDING: " + sourceLocation.getAbsolutePath() + "\\" + children[i]);
						}
					}

				} else {
					try {
						// compare the timestamps and file sizes, only execute
						// the copy if there is a difference in size or
						// timestamp detected
						if (isDifferent(sourceLocation, targetLocation)) {
							if (!isAWS() && targetLocation.exists()) {
								// first make sure the directory structure to
								// the back up is created
								String backupFolderPath = backupLocation.getAbsolutePath();
								if (backupFolderPath.contains(sourceLocation.getName())) {
									backupFolderPath = backupFolderPath.substring(
											backupLocation.getAbsolutePath().lastIndexOf(sourceLocation.getName()));
								}
								File backupFolder = new File(backupFolderPath);
								backupFolder.mkdirs();
								File backupFile = new File(backupFolderPath, targetLocation.getName());
								if (!backupFile.exists()) {
									backupFile.createNewFile();
								}

								Files.copy(targetLocation.toPath(), backupFile.toPath(),
										StandardCopyOption.REPLACE_EXISTING);
								backupMade = true;
							}
							if (targetLocation.isFile() && targetLocation.exists()) {
								targetLocation.delete();
							}
							if (!targetLocation.exists()) {
								if (sourceLocation.isFile()) {
									File folder = new File(targetLocation.getAbsolutePath());
									folder.mkdirs();
									// targetLocation.mk
									targetLocation.createNewFile();
								} else {
									targetLocation.mkdirs();
								}
							}

							Files.copy(sourceLocation.toPath(), targetLocation.toPath(),
									StandardCopyOption.REPLACE_EXISTING);

						}

					} catch (IOException e) {
						caughtException = e;
					}
				}
			}
		} else { // current source is in exclusions
			System.out.println("EXCLUDING: " + sourceLocation.getAbsolutePath());
		}
		if (caughtException != null) {
			throw new WrappedException(caughtException);
		}
		if (targetLocation.isDirectory() && targetLocation.list().length <= 0) {
			backupMade = false;
			targetLocation.delete();
		} else {
			backupMade = true;
		}
		return backupMade;
	}

	/**
	 * Will move all files and folders under the sourceLocation to the
	 * targetLocation unless it is included as an exclusion. Will also backup any
	 * folders in the targetLocation if there were differences.
	 *
	 * @param sourceLocation - Folder to copy files from
	 * @param targetLocation - Folder to copy files to
	 * @param backupLocation - Folder to save backup of target
	 * @param exclusions     - OPTIONAL Any listed folders or files will not be
	 *                       copied
	 * @return boolean - TRUE if backup was made, FALSE if it was not
	 * @throws IOException - Signals that an I/O exception of some sort has occurred
	 */
	private static boolean moveUtil(File sourceLocation, File targetLocation, File backupLocation,
			List<String> exclusions) throws IOException {
		boolean backupMade = false;
		Exception caughtException = null;
		// First check if the current source is in the exclusions list. If it
		// matches, skip copy and backup
		if (!exclusions.contains(sourceLocation.getName())) {
			if (!sourceLocation.getAbsolutePath().contains("Backup")) {
				if (sourceLocation.isDirectory()) {
					File newBackup = new File(backupLocation.getAbsolutePath(), targetLocation.getName());
					String[] children = sourceLocation.list();
					for (int i = 0; i < children.length; i++) {
						if (!exclusions.contains(children[i])) {
							moveUtil(new File(sourceLocation, children[i]), new File(targetLocation, children[i]),
									newBackup, exclusions);
						} else {
							System.out.println("EXCLUDING: " + sourceLocation.getAbsolutePath() + "\\" + children[i]);
						}
					}

				} else {
					try {
						// compare the timestamps and file sizes, only execute
						// the backup if there is a difference in size or
						// timestamp detected
						if (isDifferent(sourceLocation, targetLocation)) {
							if (!isAWS() && targetLocation.exists()) {
								// first make sure the directory structure to
								// the back up is created
								String backupFolderPath = backupLocation.getAbsolutePath();
								if (backupFolderPath.contains(sourceLocation.getName())) {
									backupFolderPath = backupFolderPath.substring(
											backupLocation.getAbsolutePath().lastIndexOf(sourceLocation.getName()));
								}
								File backupFolder = new File(backupFolderPath);
								backupFolder.mkdirs();
								File backupFile = new File(backupFolderPath, targetLocation.getName());
								if (!backupFile.exists()) {
									backupFile.createNewFile();
								}
								try {
									// sleep to avoid access denied errors
									Thread.sleep(400);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								Files.copy(targetLocation.toPath(), backupFile.toPath(),
										StandardCopyOption.REPLACE_EXISTING);
								backupMade = true;
							}
						}
						if (targetLocation.isFile() && targetLocation.exists()) {
							targetLocation.delete();
						}
						if (!targetLocation.exists()) {
							if (sourceLocation.isFile()) {
								File folder = new File(targetLocation.getAbsolutePath());
								folder.mkdirs();
								targetLocation.createNewFile();
							} else {
								targetLocation.mkdirs();
							}
						}
						try {
							// sleep to avoid access denied errors
							Thread.sleep(200);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						Files.move(sourceLocation.toPath(), targetLocation.toPath(),
								StandardCopyOption.REPLACE_EXISTING);

						// if all files have been moved from the folder, delete the folder.
						File sourceDirectory = new File(sourceLocation.getParent());
						if (sourceDirectory.list().length == 0) {
							sourceDirectory.delete();
						}
					} catch (IOException e) {
						caughtException = e;
					}
				}
			}
		} else { // current source is in exclusions
			System.out.println("EXCLUDING: " + sourceLocation.getAbsolutePath());
		}
		if (caughtException != null) {
			throw new WrappedException(caughtException);
		}
		if (targetLocation.isDirectory() && targetLocation.list().length <= 0) {
			backupMade = false;
			targetLocation.delete();
		} else {
			backupMade = true;
		}
		return backupMade;
	}

	/**
	 * Will copy all files and folders under the sourceLocation to the
	 * targetLocation
	 *
	 * @param sourceLocation Folder to copy files from
	 * @param targetLocation Folder to copy files to
	 *
	 * @throws IOException - Signals that an I/O exception of some sort has occurred
	 */
	private static void copyAllFilesUtil(File sourceLocation, File targetLocation) throws IOException {
		Exception caughtException = null;
		if (sourceLocation.isDirectory()) {
			String[] children = sourceLocation.list();
			for (int i = 0; i < children.length; i++) {
				copyAllFilesUtil(new File(sourceLocation, children[i]), new File(targetLocation, children[i]));
			}

		} else {
			try {
				if (!targetLocation.exists()) {
					if (sourceLocation.isFile()) {
						File folder = new File(targetLocation.getAbsolutePath());
						folder.mkdirs();
						targetLocation.createNewFile();
					} else {
						targetLocation.mkdirs();
					}
				}
				try {
					// sleep to avoid access denied errors
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				Files.copy(sourceLocation.toPath(), targetLocation.toPath(), StandardCopyOption.REPLACE_EXISTING);

			} catch (IOException e) {
				caughtException = e;
			}
		}
		if (caughtException != null) {
			throw new WrappedException(caughtException);
		}
		if (targetLocation.isDirectory() && targetLocation.list().length <= 0) {
			targetLocation.delete();
		}
	}

	/***********************************************************************************************************************************/

	/**
	 * Will display message for the error
	 *
	 * @param error - Text of error message
	 */
	private static void showErrorPopup(String error) {
		showMessage("Copy may have failed. ERROR: " + error);
	}

	/**
	 * Will display a message to the user that their backup has completed, giving
	 * them the backup location and a reminder to clean out the backups once they
	 * are no longer needed
	 *
	 * @param source         source location
	 * @param dest           destination location
	 * @param backup         backup taken? t/f
	 * @param backupLocation backup location
	 */
	private static void showCompletedMessage(String source, String dest, boolean backup, String backupLocation) {
		File backupFile = new File(backupLocation);
		String message = "Datasets successfully copied from:\n" + source + "\nto:\n" + dest
				+ "\n\nThere was no need for a backup.";
		if (isAWS()) {
			message = "Datasets successfully copied from:\n" + source + "\nto:\n" + dest
					+ "\n\nNo backup was created because AWS_LAB is set to YES.";
		} else if (backup && backupFile.exists()) {
			message = "Datasets successfully copied from:\n" + source + "\nto:\n" + dest
					+ "\n\nAs a precaution a back up of existing datasets was stored at: \n\n" + backupLocation
					+ "\n\nOnly files that differed between your source and destination were backed up. \n\nPlease be sure to remove backup files once the intergrity of your data has been validated \nand this backup is no longer needed.";

		}
		showMessage(message);
	}

	/**
	 * Will output to console and then if popups are allowed, display the popup
	 *
	 * @param message - text to be shown in popup
	 */
	private static void showMessage(String message) {
		System.out.println("*************************************************************************");
		System.out.println(message);
		System.out.println("*************************************************************************");
		if (allowPopups()) {
			JOptionPane.showMessageDialog(null, message);
		}

	}

	/**
	 * Will show an error message about the specified source not existing
	 *
	 * @param source File instance
	 */
	private static void showSourceDoesNotExist(File source) {
		String message = "Copy failed because the source location (" + source.getAbsolutePath()
				+ ") does not exist! \nPlease correct and re-run";
		showMessage(message);
		throw new WrappedException(message);
	}

	/**
	 * Will show an error message about the specified source not existing
	 *
	 * @param source File instance
	 */
	private static void showDestinationDoesNotExist(File source) {
		String message = "Copy failed because the destination location (" + source.getAbsolutePath()
				+ ") does not exist! \nPlease correct and re-run";
		showMessage(message);
		throw new WrappedException(message);
	}

	/**
	 * We want to suppress popups if this is run in the AWS environment, so this
	 * metho will check the config.properties file and return true is AWS_LAB is set
	 * to TRUE
	 *
	 * @return boolean
	 */
	private static boolean allowPopups() {
		return !isAWS();
	}

	/**
	 * Will check the config.properties file to see if AWS_LAB is YES
	 *
	 * @return boolean
	 */
	private static boolean isAWS() {
		return ConfigProperties.getValue("AWS_LAB").equalsIgnoreCase("YES");
	}

	/**
	 * Compares two files to check if the contents are the same
	 *
	 * @param firstFile  to compare
	 * @param secondFile to compare
	 * @return boolean
	 */
	private static boolean isDifferent(File firstFile, File secondFile) {
		boolean different = false;
		Path firstPath = firstFile.toPath();
		Path secondPath = secondFile.toPath();
		try {
			if (firstFile.exists()) {
				if (secondFile.exists()) {
					if (Files.size(firstPath) != Files.size(secondPath)) {
						different = true;
					}
					if (!different) {
						byte[] first = Files.readAllBytes(firstPath);
						byte[] second = Files.readAllBytes(secondPath);
						different = !Arrays.equals(first, second);
					}
				} else {
					different = true;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return different;
	}

	/**
	 * Returns the file path to where the data sets are stored
	 *
	 * @return String file path
	 */
	public static String getDataSetsPath() {
		File path = new File("");
		String dataSetsFolder = ConfigProperties.getValue("DATASETS_FOLDER", "/dataSets/localDataSets");
		String dataSetsPath = path.getAbsolutePath() + dataSetsFolder;
		return dataSetsPath;
	}

	/**
	 * Will get the specified data file from the datasets folder specified in config
	 * properties. If there is not a specified location, will default to
	 * localDataSets
	 *
	 * @param fileName of the csv data file
	 * @return CsvDataFile instance for the specified file name
	 */
	public static CsvDataFile getDataFile(String fileName) {
		String filePath;
		// if a full path has been passed in as the fileName, for example,
		// C:\Automation\Selenium\Workspace\project-name\dataSets\dataFile.csv, set the
		// file path to the fileName
		if (fileName.contains(":")) {
			filePath = fileName;
		} else {
			filePath = getDataSetsPath() + "/" + fileName;
		}
		return new CsvDataFile(filePath);
	}

}
