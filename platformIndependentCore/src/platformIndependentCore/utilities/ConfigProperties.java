package platformIndependentCore.utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class for reading property values from config files. This class will read
 * values from the project.properties file, browser.properties file, and the
 * config.properties file.
 * <p>
 * Will first check the project.properties and browser.properties files for any
 * configuration settings, and then will check the user config.properties file
 * if a property value is not found in the project settings.
 * <p>
 * Will return the property setting from one of the three properties files.
 * Properties not found are defaulted to and empty string.
 * <p>
 *
 * @author VBAAUSTAYLOL
 *
 */
public class ConfigProperties {
	/** Properties pulled from the browser.properties file */
	private static Properties browserProperties = null;
	/** Properties pulled from the config.properties file */
	private static Properties configFileProperties = null;
	/** Properties pulled from the project.properties file */
	private static Properties projectProperties = null;
	/**
	 * Properties pulled from other projects TODO - this might need updating for
	 * project properties
	 */
	private static HashMap<String, Properties> otherProjectConfigFileProperties = new HashMap<String, Properties>();
	/** Specifies which Automation Tool to use (RFT, SELENIUM, etc) */
	public static final String AUTOMATION_TOOL = "AUTOMATION_TOOL";
	/**
	 * Used by development to insert pauses during script run. Should never be set
	 * to true on actual test run
	 */
	public static final String DEMO_MODE = "DEMO_MODE";
	/**
	 * setting to use an alternate method to click as default if default is not
	 * working for the app
	 */
	public static final String NATIVE_CLICK = "NATIVE_CLICK";

	/** Log settings can be set to: HTML, EXCEL, BOTH */
	public static final String LOG_TYPE = "LOG_TYPE";
	/**
	 * Set to true will generate timestamped logs, set to false (or not set), will
	 * retain just one log file
	 */
	public static final String PRESERVE_LOGS = "PRESERVE_LOGS";
	/**
	 * Allow a custom location for the standard XML/HTML Results logs
	 */
	public static final String RESULTS_FOLDER = "RESULTS_FOLDER";

	// Browser settings
	/** Which browser to use: IE, CHROME, FIREFOX, EDGE */
	public static final String BROWSER = "BROWSER";
	/**
	 * Allows the user to specify a different version of the IE Driver from the
	 * default
	 */
	public static final String IE_DRIVER = "IE_DRIVER";
	/**
	 * Allows the user to specify a different version of the EDGE Driver from the
	 * default
	 */
	public static final String EDGE_DRIVER = "EDGE_DRIVER";
	/**
	 * Allows the user to specify a different version of the Chrome Driver from the
	 * default
	 */
	public static final String CHROME_DRIVER = "CHROME_DRIVER";
	/**
	 * Allows the user to specify a different version of the GECKO (Firefox) Driver
	 * from the default
	 */
	public static final String GECKO_DRIVER = "GECKO_DRIVER";
	/**
	 * For Developers only, will leave the Browser open if set to FALSE. Need to be
	 * careful as it leaves processes running.
	 */
	public static final String CLOSE_ON_TERMINATE = "CLOSE_ON_TERMINATE";

	// IEDriverServer settings
	/**
	 * Allows the user to specify a value for the IGNORE_ZOOM_SETTING capability
	 * option for the Internet Explorer Driver.
	 */
	public static final String IE_IGNORE_ZOOM_SETTING = "IE_IGNORE_ZOOM_SETTING";
	/**
	 * Allows the user to specify a value for the REQUIRE_WINDOW_FOCUS capability
	 * option for the Internet Explorer Driver.
	 */
	public static final String IE_REQUIRE_WINDOW_FOCUS = "IE_REQUIRE_WINDOW_FOCUS";
	/**
	 * Allows the user to specify a value for the NATIVE_EVENTS capability option
	 * for the Internet Explorer Driver.
	 */
	public static final String IE_NATIVE_EVENTS = "IE_NATIVE_EVENTS";
	/**
	 * Allows the user to specify a value for the BROWSER_ATTACH_TIMEOUT capability
	 * option for the Internet Explorer Driver.
	 */
	public static final String IE_BROWSER_ATTACH_TIMEOUT = "IE_BROWSER_ATTACH_TIMEOUT";
	/**
	 * Allows the user to specify a value for the
	 * INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS capability option for the
	 * Internet Explorer Driver.
	 */
	public static final String IE_INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS = "IE_INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS";

	/**
	 * Allows the user to specify a value for the UNEXPECTED_ALERT_BEHAVIOUR
	 * capability option for the Internet Explorer Driver.
	 */
	public static final String IE_UNEXPECTED_ALERT_BEHAVIOUR = "IE_UNEXPECTED_ALERT_BEHAVIOUR";

	// Search settings
	/**
	 * The number of seconds a search should wait before resulting in Object Not
	 * Found
	 */
	public static final String SEARCH_WAIT_SECONDS = "SEARCH_WAIT_SECONDS";
	/** Determines if a Search should use a default parent */
	public static final String SEARCH_USE_DEFAULT_PARENT = "SEARCH_USE_DEFAULT_PARENT";
	/**
	 * Determines if Ambiguous Object Exception should be thrown when more than one
	 * matching object is found
	 */
	public static final String SEARCH_THROW_AMBIGUOUS = "SEARCH_THROW_AMBIGUOUS";
	/**
	 * Determines if Object Not Found Exception should be thrown when more no
	 * matching objects are found
	 */
	public static final String SEARCH_THROW_OBJECT_NOT_FOUND = "SEARCH_THROW_OBJECT_NOT_FOUND";
	/** TRUE or FALSE value if the search should wait */
	public static final String SEARCH_WAIT = "SEARCH_WAIT";

	// Datasets settings
	/** Sets location to find datasets */
	public static final String DATASETS_FOLDER = "DATASETS_FOLDER";
	// DATA FILE COLUMNS can be set to custom values
	/** Test Execution column header */
	public static final String TEST_EXECUTION_COLUMN = "TEST_EXECUTION_COLUMN";
	/** Script column header */
	public static final String SCRIPT_COLUMN = "SCRIPT_COLUMN";
	/** Dependencies column header */
	public static final String DEPENDENCIES_COLUMN = "DEPENDENCIES_COLUMN";
	/** Data Used column header */
	public static final String DATA_USED_COLUMN = "DATA_USED_COLUMN";
	/** Data ID column header */
	public static final String DATA_ID_COLUMN = "DATA_ID_COLUMN";
	/**
	 * Determines if the CSV Numeric index is required - This may be outdated since
	 * recent changes handle the difference easier
	 */
	public static final String REQUIRE_CSV_NUMERIC_INDEX = "REQUIRE_CSV_NUMERIC_INDEX";

	// Location settings
//	public static final String TEMPLATE_FOLDER = "TEMPLATE_FOLDER"; // TODO = This may be outdated since setting up
//																	// atiCommon
	/**
	 * Allows code to find any downloaded files if the download location is not
	 * system default
	 */
	public static final String DOWNLOAD_FOLDER = "DOWNLOAD_FOLDER";
	/**
	 * Determines where Daily Logs should be located. If this is not set, no Daily
	 * Logs will be generated
	 */
	public static final String DAILY_LOGS_FOLDER = "DAILY_LOGS_FOLDER";
	/** If set to TRUE, will prevent all screen shots from being taken */
	public static final String DISABLE_SCREEN_SHOTS = "DISABLE_SCREEN_SHOTS";
	/**
	 * If set to TRUE, will prevent the screenshots from being added to xray results
	 **/
	public static final String DISABLE_SCREEN_SHOT_REPORTING_ATTACHMENT = "DISABLE_SCREEN_SHOT_REPORTING";
	/** Sets the Test Environment information */
	private static final String TEST_ENV = "TEST_ENV";
	/** logger for this class */
	static Logger log = LogManager.getLogger(ConfigProperties.class.getName());

	/** PATH to XRAY Authentication File */
	public static final String XRAY_AUTH_FILE = "XRAY_AUTH_FILE";
	/** If set to TRUE XRAY Reporting is on */
	public static final String XRAY_REPORTING = "XRAY_REPORTING";
	/** Base URL of the XRAY Cloud Server */
	public static final String XRAY_CLOUD_URL = "XRAY_CLOUD_URL";
	/** Base URL of the XRAY SERVER */
	public static final String XRAY_SERVER_URL = "XRAY_SERVER_URL";
	/** Xray Certificate location */
	public static final String XRAY_CERTIFICATE = "XRAY_CERTIFICATE";
	/** Authentication Type (should be BASIC or CERTIFICATE) */
	public static final String XRAY_AUTHENTICATION = "XRAY_AUTHENTICATION";
	/** Login URL for XRAY authentication */
	public static final String XRAY_LOGIN_URL = "XRAY_LOGIN_URL";
	/** Xray Id for Test Plan */
	public static final String XRAY_PLAN_ID = "XRAY_PLAN_ID";
	/** Xray Test Environments */
	public static final String XRAY_TEST_ENVIRONMENTS = "XRAY_TEST_ENVIRONMENTS";
	/** Xray Fix Version */
	public static final String XRAY_FIX_VERSION = "XRAY_FIX_VERSION";
	/** Jira Project Key */
	public static final String JIRA_PROJECT_KEY = "JIRA_PROJECT_KEY";

	/** The Scan Enabled value for Section 508 Testing */
	public static final String SECTION_508_SCAN_ENABLED = "SECTION_508_SCAN_ENABLED";
	/** The Results Folder Name for Section 508 Testing */
	public static final String SECTION_508_RESULTS_FOLDER_NAME = "SECTION_508_RESULTS_FOLDER_NAME";
	/** The Dataset Filename for Section 508 Testing */
	public static final String SECTION_508_DATASET_NAME = "SECTION_508_DATASET_NAME";
	/** The Sheet Name for Section 508 Testing */
	public static final String SECTION_508_SHEET_NAME = "SECTION_508_SHEET_NAME";
	/** The Tools Location for Section 508 Testing */
	public static final String SECTION_508_TOOLS = "SECTION_508_TOOLS";

	/**
	 * Get the Properties object that is loaded with settings from the
	 * config.properties
	 *
	 * @return Properties a reference to the Properties object that is loaded with
	 *         settings from the config.properties
	 */
	private static Properties getConfigProperties() {
		if (configFileProperties == null) {
			configFileProperties = new Properties();
			InputStream input = null;
			try {
				File l = new File("");
				log.debug(l.getAbsolutePath());
				input = new FileInputStream("config.properties");

				// load properties file
				configFileProperties.load(input);

			} catch (FileNotFoundException fnf) {
				log.warn("config.properties not found. Default values will be used.");

			} catch (IOException ioe) {
				ioe.printStackTrace();
			} finally {
				if (input != null) {
					try {
						input.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

		}
		return configFileProperties;

	}

	/**
	 * Get the Properties object that is loaded with settings from the
	 * project.properties
	 *
	 * @return Properties a reference to the Properties object that is loaded with
	 *         settings from the project.properties
	 */
	private static Properties getProjectProperties() {
		if (projectProperties == null) {
			projectProperties = new Properties();
			InputStream input = null;
			try {
				File l = new File("");
				log.debug(l.getAbsolutePath());
				input = new FileInputStream(".settings/project.properties");

				// load properties file
				projectProperties.load(input);

			} catch (FileNotFoundException fnf) {
				log.warn("project.properties not found. Default values will be used.");

			} catch (IOException ioe) {
				ioe.printStackTrace();
			} finally {
				if (input != null) {
					try {
						input.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

		}
		return projectProperties;

	}

	/**
	 * Get the Properties object that is loaded with settings from the
	 * browser.properties
	 *
	 * @return Properties a reference to the Properties object that is loaded with
	 *         settings from the browser.properties
	 */
	private static Properties getBrowserProperties() {
		if (browserProperties == null) {
			browserProperties = new Properties();
			InputStream input = null;
			try {
				File l = new File("");
				log.debug(l.getAbsolutePath());
				input = new FileInputStream(".settings/browser.properties");

				// load properties file
				browserProperties.load(input);

			} catch (FileNotFoundException fnf) {
				log.warn("browser.properties not found. Default values will be used.");

			} catch (IOException ioe) {
				ioe.printStackTrace();
			} finally {
				if (input != null) {
					try {
						input.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

		}
		return browserProperties;

	}

	/**
	 * Get the Properties object that is loaded with settings from the
	 * config.properties for a specified project
	 *
	 * @param otherProjectName the name of the project to get the property from
	 * @return Properties for the project
	 */
	private static Properties getProperties(String otherProjectName) {
		if (otherProjectConfigFileProperties.get(otherProjectName) == null) {
			Properties otherProjectProperties = new Properties();
			InputStream input = null;
			try {
				// Get the current directory location of the project where the script
				// resides
				File file = new File("");
				String absPath = file.getAbsolutePath();
				String projectFolderPath = absPath.substring(0, absPath.lastIndexOf("\\") + 1) + otherProjectName;

				input = new FileInputStream(projectFolderPath + "\\config.properties");

				// load properties file
				otherProjectProperties.load(input);
				otherProjectConfigFileProperties.put(otherProjectName, otherProjectProperties);

			} catch (FileNotFoundException fnf) {
				// logWarning("config.properties not found. Default values will be used.");

			} catch (IOException ioe) {
				ioe.printStackTrace();
			} finally {
				if (input != null) {
					try {
						input.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

		}
		return otherProjectConfigFileProperties.get(otherProjectName);

	}

	/**
	 * Returns the value of a specified property from the config.properties file
	 *
	 * @param otherProjectName project that contains desired config.properties file
	 * @param propertyName     in the config file
	 * @return String value associated with the property
	 */
	public static String getValueFromOtherProject(String otherProjectName, String propertyName) {
		String propertyValue = "";

		String value = getProperties(otherProjectName).getProperty(propertyName);
		if (value != null) {
			propertyValue = value;
		}

		return propertyValue;
	}

	/**
	 * Returns the value of a specified property.
	 * <p>
	 * By design, this solution will look first in the project.properties file, if a
	 * value is found there, it will stop and return the value.
	 * <p>
	 * If no value is found in the project.properties file, then it will look in the
	 * browser.properties file.
	 * <p>
	 * If no value is found in the browser.properties file, then it will look in the
	 * config.properties file.
	 * <p>
	 * If no value is found in the project.properties, browser.properties, or
	 * config.properties files, then an empty String will be returned.
	 *
	 * @param propertyName in the config file
	 * @return String value associated with the property
	 */
	public static String getValue(String propertyName) {
		String propertyValue = "";
		// first check for the value in the project properties file
		String value = getProjectProperties().getProperty(propertyName);
		if (value == null || value.isEmpty()) {
			// if property not found in project properties, check
			// the browser properties file
			value = getBrowserProperties().getProperty(propertyName);
		}
		if (value == null || value.isEmpty()) {
			// if property not found in browser properties, check
			// the user config properties file
			value = getConfigProperties().getProperty(propertyName);
		}
		if (value != null) {
			propertyValue = value.trim();
		}

		return propertyValue;
	}

	/**
	 * Returns the value of a specified property
	 * <p>
	 * By design, this solution will look first in the project.properties file, if a
	 * value is found there, it will stop and return the value.
	 * <p>
	 * If no value is found in the project.properties file, then it will look in the
	 * browser.properties file.
	 * <p>
	 * If no value is found in the browser.properties file, then it will look in the
	 * config.properties file.
	 * <p>
	 * If no value is found in the project.properties, browser.properties, or
	 * config.properties files, then the specified defaultValue will be returned
	 *
	 * @param propertyName in the config file
	 * @param defaultValue - The value to default to if there is not an entry in the
	 *                     config.properties file
	 * @return String value of the property
	 */
	public static String getValue(String propertyName, String defaultValue) {
		String configValue = getValue(propertyName);

		return configValue.isEmpty() ? defaultValue : configValue;
	}

	/**
	 * Sets the Property Value in the config.properties file
	 *
	 * @param propertyName  to set
	 * @param propertyValue to set the property to
	 */
	public static void setValue(String propertyName, String propertyValue) {
		try {
			// input the file content to the String "input"
			BufferedReader file = new BufferedReader(new FileReader("config.properties"));
			String line;
			String input = "";
			boolean setValue = false;

			while ((line = file.readLine()) != null) {
				if (line.startsWith(propertyName + "=")) {
					String[] oldSetting = line.split("=");
					String oldValue = oldSetting.length > 1 ? oldSetting[1] : "";
					if (!oldValue.isEmpty()) {
						line = line.replace(oldValue, propertyValue);
					} else {
						line += propertyValue;
					}
					setValue = true;

				}
				input += line + '\n';
			}

			if (!setValue) {
				input += propertyName + "=" + propertyValue;
			}

			file.close();

			// write the new String with the replaced line OVER the same file
			FileOutputStream fileOut = new FileOutputStream("config.properties");

			fileOut.write(input.getBytes());
			fileOut.close();

			// update the value in the cache to match this new value.
			configFileProperties.remove(propertyName);
			configFileProperties.put(propertyName, propertyValue);

		} catch (Exception e) {
			log.error("Problem reading file.");
			log.catching(e);
		}

	}

	/**
	 * Returns the Log Type configured in the config.properties file
	 *
	 * @return String LOG_TYPE value config.properties
	 */
	public static String getLogType() {
		return getValue(LOG_TYPE);
	}

	/**
	 * Returns the Browser configured in the config.properties file
	 *
	 * @return String BROWSER value from config.properties
	 */
	public static String getBrowser() {
		return getValue(BROWSER).toUpperCase();
	}

	/**
	 * Returns the test environment configured in the config.properties file
	 * TEST_ENV can be set to another "_URL" value, or text.
	 *
	 * This method will replace any "_URL" values with the value associated with it
	 *
	 * @return String TEST_ENV value from config.properties
	 */
	public static String getTestEnv() {
		String testEnv = getValue(TEST_ENV).toUpperCase();
		if (testEnv != null && testEnv.endsWith("_URL")) {
			testEnv = getValue(testEnv);
		}

		return testEnv;
	}

	/**
	 * Returns the Automation Tool configured in the project.properties file
	 *
	 * @return String AUTOMATION_TOOL value from project.propeties
	 */
	public static String getAutomationTool() {
		return getValue(AUTOMATION_TOOL);
	}
}
