package platformIndependentCore.datafiles;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Set;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import platformIndependentCore.exceptions.InvalidDataException;
import platformIndependentCore.utilities.ConfigProperties;

/**
 * <b>Name :</b> ConvertExcelToCsv.java
 * <p>
 * <b>Generated :</b> Jul 29, 2021
 * <p>
 * <b>Description :</b> Contains static methods to allow users to convert Excel
 * data files to the CSV formatted data files. Also contains static methods to
 * assist in formatting CSV data files.
 * <p>
 *
 * @since Jul 29, 2021
 * @author 281JGIVE
 */
public class ConvertExcelToCsv {
	/** One minute in milliseconds */
	protected static final long ONE_MINUTE_IN_MILLIS = 60000;// millisecs

	/**
	 * This method will convert all the Excel Data Files in all the subfolders to
	 * create new .csv files to represent each Sheet in each Excel Data File
	 */
	public static void convertDataSetsInSubFolders() {
		convertDataSets(false);
	}

	/**
	 * This method will convert all the Excel Data Files in all the subfolders to
	 * create new .csv files to represent each Sheet in each Excel Data File
	 *
	 * If the createFolders param is set to TRUE then a subfolder will be created
	 * for each ExcelDataFile and each sheet will have a .csv file within the folder
	 *
	 * If createFolders is FALSE, then csv files will all be create in the same
	 * datasets folder and will be name ExcelFileName_SheetName.csv
	 *
	 * @param createFolders TRUE - create subfolder for each ExcelDataFile; FALSE -
	 *                      csv files will all be in the same folder
	 */
	public static void convertDataSetsInSubFolders(boolean createFolders) {
		String folder = getFullConfiguredDataFilePath();
		System.out.println("FOLDER=" + folder);

		folder = folder.substring(0, folder.lastIndexOf("\\"));

		File f = new File(folder);

		String[] directories = f.list(new FilenameFilter() {
			@Override
			public boolean accept(File current, String name) {
				return new File(current, name).isDirectory();
			}
		});

		for (String d : directories) {
			System.out.println("DIR: " + d);
		}
		convertDataSets(createFolders, directories);
	}

	/**
	 * This method will isolate updates to only folders with names matching the
	 * parameters provided
	 *
	 * For each folder specified, .csv files will be generated for each Excel Data
	 * File. There will be a .csv file for every sheet. The new .csv files will be
	 * named ExcelFileName_SheetName.csv
	 *
	 *
	 * <li>If these are legacy RFT framework data files, the values in the DATA_USED
	 * column will be updated: TRUE values will be changed to USED and FALSE values
	 * will be changed to AVAIL. As well, the values in TEST_EXECUTION column will
	 * be updated: TRUE will be changed to RUN and FALSE will be changed to
	 * SKIP</li>
	 *
	 * @param foldersToConvert List of folders to convert
	 */
	public static void convertDataSets(String... foldersToConvert) {
		convertDataSets(false, foldersToConvert);
	}

	/**
	 * This method will isolate updates to only folders with names matching the
	 * parameters provided
	 *
	 * For each folder specified, .csv files will be generated for each Excel Data
	 * File. There will be a .csv file for every sheet. The new .csv files will be
	 * named ExcelFileName_SheetName.csv if createFolders is FALSE
	 *
	 * If createFolders is TRUE, .csv files for each sheet will be created in a
	 * subfolder named the same as the original ExcelDataFile
	 *
	 *
	 * <li>If these are legacy RFT framework data files, the values in the DATA_USED
	 * column will be updated: TRUE values will be changed to USED and FALSE values
	 * will be changed to AVAIL. As well, the values in TEST_EXECUTION column will
	 * be updated: TRUE will be changed to RUN and FALSE will be changed to
	 * SKIP</li>
	 *
	 * @param createFolders    TRUE - create subfolder for each ExcelDataFile; FALSE
	 *                         - csv files will all be in the same folder
	 * @param foldersToConvert List of folders to convert
	 */
	public static void convertDataSets(boolean createFolders, String... foldersToConvert) {
		String folder = getFullConfiguredDataFilePath();

		for (String d : foldersToConvert) {
			System.out.println("DIR: " + d);
		}

		for (String d : foldersToConvert) {
			System.out.println(">> sub folder " + d);

			String subFolder = folder + d;
			System.out.println("FOLDER=" + subFolder);
			if (folder.isEmpty()) {
				System.out.println(subFolder + " FOLDER IS EMPTY");
			}
			File[] datasets = xlsxFinder(subFolder);

			if (datasets != null) {
				System.out.println("FOUND " + datasets.length + " DATASETS");
				for (File ds : datasets) {
					System.out.println("...DS: " + ds.getAbsolutePath() + "|" + ds.getName());
					// convert(ds);
					convertToCsv(ds, true, createFolders);
				}
			} else {
				System.out.println("FOUND NO DATASETS");
			}
			System.out.println("===============================================================");

		}

	}

	/**
	 * This method converts the Excel Data File to .csv files. There will be a .csv
	 * file for every sheet in the Excel file.
	 * <ul>
	 * <li>Create a .csv file to represent each sheet within the Excel Data
	 * File.</li>
	 * <li>If these are legacy RFT framework data files, the values in the DATA_USED
	 * column will be updated: TRUE values will be changed to USED and FALSE values
	 * will be changed to AVAIL. As well, the values in TEST_EXECUTION column will
	 * be updated: TRUE will be changed to RUN and FALSE will be changed to
	 * SKIP</li>
	 * </ul>
	 *
	 * @param excelFileName Name of file to convert, including file extension
	 */
	public static void convertToCsv(String excelFileName) {
		convertToCsv(excelFileName, false);
	}

	/**
	 * This method converts the Excel Data File to .csv files. There will be a .csv
	 * file for every sheet in the Excel file.
	 * <ul>
	 * <li>Create a .csv file to represent each sheet within the Excel Data
	 * File.</li>
	 * <li>If these are legacy RFT framework data files, the values in the DATA_USED
	 * column will be updated: TRUE values will be changed to USED and FALSE values
	 * will be changed to AVAIL. As well, the values in TEST_EXECUTION column will
	 * be updated: TRUE will be changed to RUN and FALSE will be changed to
	 * SKIP</li>
	 * </ul>
	 *
	 * @param excelFileName Name of file to convert, including file extension
	 * @param createFolder  TRUE will create a folder for the Excel file that will
	 *                      contain files for each Sheet; FALSE all files will be
	 *                      created in the same folder with the name
	 *                      EXCELFILE_SHEET.csv
	 */
	public static void convertToCsv(String excelFileName, boolean createFolder) {
		String fileName = generateFullFileNameAndPath(excelFileName);
		File file = new File(fileName);
		System.out.println("CONVERT: " + fileName + "| exists?" + file.exists());

		convertToCsv(file, true, createFolder);
	}

	/**
	 * This method converts the Excel Data File to .csv files. There will be a .csv
	 * file for every sheet in the Excel file.
	 * <ul>
	 * <li>Create a .csv file to represent each sheet within the Excel Data
	 * File.</li>
	 * <li>If these are legacy RFT framework data files, the values in the DATA_USED
	 * column will be updated: TRUE values will be changed to USED and FALSE values
	 * will be changed to AVAIL. As well, the values in TEST_EXECUTION column will
	 * be updated: TRUE will be changed to RUN and FALSE will be changed to
	 * SKIP</li>
	 * </ul>
	 *
	 * @param folder        This is the folder from the datasets location if it is
	 *                      different from the configured default. The code will go
	 *                      up one folder and switch to the provided folder. So if
	 *                      you config.properties has:
	 *                      datasetsfolder=testScripts\\datasets\\historicalDataSets
	 *                      and you pass in "localDataSets", the code will look in
	 *                      testScripts\\datasets\\localDataSets
	 * @param excelFileName File name should include the ".xlsx" extension
	 */
	public static void convertToCsv(String folder, String excelFileName) {
		convertToCsv(folder, excelFileName, false);
	}

	/**
	 * This method converts the Excel Data File to .csv files. There will be a .csv
	 * file for every sheet in the Excel file.
	 * <ul>
	 * <li>Create a .csv file to represent each sheet within the Excel Data
	 * File.</li>
	 * <li>If these are legacy RFT framework data files, the values in the DATA_USED
	 * column will be updated: TRUE values will be changed to USED and FALSE values
	 * will be changed to AVAIL. As well, the values in TEST_EXECUTION column will
	 * be updated: TRUE will be changed to RUN and FALSE will be changed to
	 * SKIP</li>
	 * </ul>
	 *
	 * @param folder        This is the folder from the datasets location if it is
	 *                      different from the configured default. The code will go
	 *                      up one folder and switch to the provided folder. So if
	 *                      you config.properties has:
	 *                      datasetsfolder=testScripts\\datasets\\historicalDataSets
	 *                      and you pass in "localDataSets", the code will look in
	 *                      testScripts\\datasets\\localDataSets
	 * @param excelFileName File name should include the ".xlsx" extension
	 *
	 * @param createFolders TRUE will create a subfolder for the Excel file; FALSE
	 *                      will not create a subfolder
	 */
	public static void convertToCsv(String folder, String excelFileName, boolean createFolders) {
		String dataFileFolder = getFullConfiguredDataFilePath();

		String fileName = dataFileFolder + folder + "\\" + excelFileName;
		File file = new File(fileName);
		System.out.println("CONVERT: " + file.getAbsolutePath());
		System.out.println("| exists?" + file.exists());

		convertToCsv(file, true, createFolders);
	}

	/**
	 * This method converts the Excel Data File to .csv files. There will be a .csv
	 * file for every sheet in the Excel file.
	 * <ul>
	 * <li>Create a .csv file to represent each sheet within the Excel Data
	 * File.</li>
	 * <li>If these are legacy RFT framework data files, the values in the DATA_USED
	 * column will be updated: TRUE values will be changed to USED and FALSE values
	 * will be changed to AVAIL. As well, the values in TEST_EXECUTION column will
	 * be updated: TRUE will be changed to RUN and FALSE will be changed to
	 * SKIP</li>
	 * </ul>
	 *
	 * @param excelFile     Excel file to convert to csv
	 * @param addTicks      add the back tick (`) character to all cells?
	 * @param createFolders TRUE will create a subfolder for the Excel file; FALSE
	 *                      will not create a subfolder
	 */
	private static void convertToCsv(File excelFile, boolean addTicks, boolean createFolders) {

		BufferedWriter bwr = null;
		FileInputStream fis = null;
		Workbook workbook = null;
		try {
			String csvFilePath = excelFile.getAbsolutePath();
			// strip off the file name
			csvFilePath = csvFilePath.substring(0, csvFilePath.lastIndexOf("\\"));
			String origCsvFilePath = csvFilePath;
			System.out.println("PATH=" + csvFilePath);

			fis = new FileInputStream(excelFile);

			workbook = WorkbookFactory.create(fis);

			int numSheets = workbook.getNumberOfSheets();

			for (int sheetIndex = 0; sheetIndex < numSheets; sheetIndex++) {
				try {
					Sheet sheet = workbook.getSheetAt(sheetIndex);
					// Only create a CSV file if the sheet has content
					if (sheet.iterator().hasNext()) {
						ExcelDataFile df = new ExcelDataFile(excelFile.getAbsolutePath(), sheetIndex);
						int duIndex = -1;
						if (df.headerCache != null && df.headerCache.containsKey(CsvDataFile.DATA_USED_COLUMN)) {
							duIndex = df.headerCache.get(CsvDataFile.DATA_USED_COLUMN);
						}
						int teIndex = -1;
						if (df.headerCache != null && df.headerCache.containsKey(CsvDataFile.TEST_EXECUTION_COLUMN)) {
							teIndex = df.headerCache.get(CsvDataFile.TEST_EXECUTION_COLUMN);
						}
						int numRows = sheet.getLastRowNum();
						boolean hasDataId = df.isColumnHeaderPresent(DataFile.DATA_ID_COLUMN);
						if (hasDataId) {
							numRows = df.getNumberOfRecords();
						}
						Set<String> columns = df.headerCache.keySet();
						int numColumns = columns.size();

						String csvFileName = excelFile.getName().replace(".xlsx",
								"_" + workbook.getSheetName(sheetIndex) + ".csv");

						if (createFolders) {
							csvFilePath = origCsvFilePath + "\\" + excelFile.getName().replace(".xlsx", "");
							File csvFolder = new File(csvFilePath);
							csvFolder.mkdirs();
							csvFileName = workbook.getSheetName(sheetIndex) + ".csv";
						}
						System.out.println("*** CREATE: " + csvFileName);

						workbook.setMissingCellPolicy(Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);

						StringBuffer converted = new StringBuffer();
						for (int rowIndex = 0; rowIndex <= numRows; rowIndex++) {
							Cell cell = null;
							Row row = null;

							int currentCell = 0;
							row = sheet.getRow(rowIndex);
							if (row == null) {
								break;
							}

							for (int colIndex = 0; colIndex < numColumns; colIndex++) {
								cell = row.getCell(colIndex);
								currentCell = cell.getColumnIndex();

								/* Cell processing starts here */

								String csvCell = "";
								switch (cell.getCellType()) {

								case BOOLEAN:
									csvCell = String.valueOf(cell.getBooleanCellValue());
									break;

								case NUMERIC:
									if (DateUtil.isCellDateFormatted(cell)) {
										SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
										csvCell = dateFormat.format(cell.getDateCellValue());
									} else {
										Double value = cell.getNumericCellValue();
										Long longValue = value.longValue();
										csvCell = new String(longValue.toString());

									}
									break;

								case STRING:
									csvCell = cell.getRichStringCellValue().getString();
									break;

								case BLANK:
									csvCell = "";
									break;

								default:
									csvCell = cell.getRichStringCellValue().getString(); // .toUpperCase();

									break;
								}
								if (currentCell == duIndex) {
									if (csvCell.equalsIgnoreCase("TRUE")) {
										csvCell = CsvDataFile.USED_VALUE;
									} else if (csvCell.equalsIgnoreCase("FALSE")) {
										csvCell = DataFile.AVAILABLE_VALUE;
									}
								}
								if (currentCell == teIndex) {
									if (csvCell.equalsIgnoreCase("TRUE")) {
										csvCell = CsvDataFile.RUN_VALUE;
									} else if (csvCell.equalsIgnoreCase("FALSE")) {
										csvCell = DataFile.SKIP_VALUE;
									}
								}
								// To prevent Excel from auto-formatting the
								// CSV, put ' in front of all data
								if (addTicks) {
									csvCell = "`" + csvCell;

								}
								converted.append(csvCell + CsvDataFile.DELIMITER);

							}
							converted.append("\n");

						}
						System.out.println("WRITE DATA FILE SHEET: " + csvFilePath + "\\" + csvFileName);
						File csvFile = new File(csvFilePath + "\\" + csvFileName);
						csvFile.createNewFile();

						String path = csvFilePath + "\\" + csvFileName;
						bwr = new BufferedWriter(
								new OutputStreamWriter(new FileOutputStream(path), StandardCharsets.UTF_8));

						// write contents of StringBuffer to a file
						bwr.write(converted.toString());

						// flush the stream
						bwr.flush();

						// close the stream
						bwr.close();
						bwr = null;

						workbook.close();

					}

				} catch (EncryptedDocumentException e) {
					e.printStackTrace();
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				} finally {
					if (bwr != null) {
						bwr.close();
						bwr = null;
					}

				}
			}

			// Set the default to current working folder + _results
			File file = new File("");
			String projectLocation = file.getAbsolutePath();
			String archiveFolder = projectLocation.substring(0, projectLocation.lastIndexOf("\\")) + "_excel_archives";
			String subFolder = origCsvFilePath.substring(origCsvFilePath.lastIndexOf("\\"));
			// make sure that the archive folders are created
			File currFolder = new File(archiveFolder + subFolder);
			currFolder.mkdirs();

			String archivedExcel = archiveFolder + subFolder + "\\" + excelFile.getName();
			System.out.println("ARCHIVED: " + archivedExcel);
			// Move the current excel data sheet to the archive location
			File archivedFile = new File(archivedExcel);
			if (archivedFile.exists()) {
				archivedFile.delete();
			}
			excelFile.renameTo(archivedFile);

		} catch (EncryptedDocumentException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			if (workbook != null) {
				try {
					workbook.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				workbook = null;
			}
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				fis = null;
			}

		}
	}

	/**
	 * Will return a set of Files that end with .xlsx within the specified folder
	 *
	 * @param folderName folder to search for xlsx files in
	 * @return File[] xlsx files found
	 */
	private static File[] xlsxFinder(String folderName) {
		File dir = new File(folderName);

		return dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				return filename.endsWith(".xlsx");
			}
		});

	}

	/**
	 * Will return a set of Files that end with .csv within the specified folder
	 *
	 * @param folderName folder to search for csv files in
	 * @return File[] csv files found
	 */
	private static File[] csvFinder(String folderName) {
		File dir = new File(folderName);

		return dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				return filename.endsWith(".csv");
			}
		});

	}

	/**
	 * Private method to pull the data file folder path from the config file and
	 * build the complete path name
	 *
	 * @param fileName name of file to build full path for
	 * @return String full file path
	 */
	private static String generateFullFileNameAndPath(String fileName) {
		// First Check the Config File, if set, use that value
		String dataFileFolder = ConfigProperties.getValue(ConfigProperties.DATASETS_FOLDER);
		String filePath = "";

		if (dataFileFolder == "") {
			filePath = fileName;
			throw new InvalidDataException(
					"Data File Path must be specified using the 'datasetsfolder' variable in the config.properties file.");

		} else {
			File file = new File("file");
			filePath = file.getAbsolutePath();
			filePath = filePath.substring(0, filePath.lastIndexOf("\\") + 1) + dataFileFolder + "\\" + fileName;
		}
		return filePath;

	}

	/**
	 * Returns the full path of the configured data sets folder (specified in the
	 * config file)
	 *
	 * @return full path of the configured data sets folder
	 */
	private static String getFullConfiguredDataFilePath() {
		File file = new File("file");
		String filePath = file.getAbsolutePath();

		String folder = ConfigProperties.getValue(ConfigProperties.DATASETS_FOLDER);
		folder = filePath.substring(0, filePath.lastIndexOf("\\")) + folder + "\\";

		return folder;
	}

	/**
	 * Fix the format of the single specified Csv File
	 *
	 * The fileToFix path is relative to the configured datasets folder from your
	 * config.properties file. It will work off one folder up from what is
	 * configured to allow easier access to other data sets folders
	 * <p>
	 * EXAMPLE: if datasetsfolder=testScripts\\datasets\\localDataSets this method
	 * will work off of testScripts\\datasets
	 * <p>
	 * So to format TestData.csv in the masterDataSets folder you would pass in:
	 * masterDataSets\\TestData.csv
	 *
	 * @param fileToFix Name of file to fix format on
	 */
	public static void formatSingleCsvFile(String fileToFix) {
		String folder = getFullConfiguredDataFilePath();
		CsvDataFile csv = new CsvDataFile(folder + fileToFix);
		csv.format();
	}

	/**
	 * Will fix the format for all CSV files under the specified folder(s)
	 *
	 * The foldersToFix path is relative to the configured datasets folder from your
	 * config.properties file. It will work off one folder up from what is
	 * configured to allow easier access to other data sets folders
	 * <p>
	 * EXAMPLE: if datasetsfolder=testScripts\\datasets\\localDataSets this method
	 * will work off of testScripts\\datasets
	 * <p>
	 * So to format the masterDataSets folder you would pass in: masterDataSets
	 *
	 * @param foldersToFix List of folders to format
	 */
	public static void formatCsvFiles(String... foldersToFix) {
		String folder = getFullConfiguredDataFilePath();
		// Remove the last \ that is added in the return of the method call above
		folder = folder.substring(0, folder.lastIndexOf("\\") - 1);

		folder = folder.substring(0, folder.lastIndexOf("\\"));

		for (String d : foldersToFix) {
			System.out.println("DIR: " + d);
		}

		for (String d : foldersToFix) {
			System.out.println(">> sub folder " + d);

			String subFolder = d;
			if (!subFolder.startsWith("C:")) {
				subFolder = folder + "\\" + d;
			}
			System.out.println("FOLDER=" + subFolder);
			if (folder.isEmpty()) {
				System.out.println(subFolder + " FOLDER IS EMPTY");
			}
			File[] datasets = csvFinder(subFolder);

			if (datasets != null) {
				System.out.println("FOUND " + datasets.length + " DATASETS");
				for (File ds : datasets) {
					System.out.println("...DS: " + ds.getAbsolutePath() + "|" + ds.getName());
					CsvDataFile csv = new CsvDataFile(ds.getAbsolutePath());
					csv.format();
				}
			} else {
				System.out.println("FOUND NO DATASETS");
			}

			// Now look for sub folders
			File currFolder = new File(subFolder);
			try {
				Files.list(Paths.get(currFolder.getAbsolutePath())).filter(Files::isDirectory).forEach(item -> {
					formatCsvFiles(item.toString());

				});
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("===============================================================");

		}

	}
}
