package platformIndependentCore.core;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import platformIndependentCore.exceptions.AutomationToolNotConfiguredException;
import platformIndependentCore.tables.CustomTable;
import platformIndependentCore.utilities.ConfigProperties;

/**
 * This class manages the automation tool in use and provides common methods for
 * retrieving objects and caching them.
 *
 * @author VBAAUSTAYLOL
 *
 */
// TODO - was PlatformImplementations.... need to make sure RFT is updated/refactored correctly
public class ToolManager {
	/** Cache of AutomatedObjects for easy access */
	private HashMap<String, AutomatedObject> cachedObjects = new HashMap<String, AutomatedObject>();
	/** Cache of CustomTable objects */
	private HashMap<String, CustomTable> cachedCustomTables = new HashMap<String, CustomTable>();
	/** The Automation Tool this project is configured to run with */
	private static String configuredTool = ConfigProperties.getValue(ConfigProperties.AUTOMATION_TOOL);
	/** Map of String name to AutomationTools */
	static Map<String, AutomationTool> toolMap = new HashMap<String, AutomationTool>();
	static {
		toolMap.put("rft", AutomationTool.RFT);
		toolMap.put("selenium", AutomationTool.SELENIUM);
		toolMap.put("silktest", AutomationTool.SILKTEST);
	}

	/**
	 * Returns the testing platform currently set in the config.properties file by
	 * the variable AUTOMATION_TOOL
	 *
	 * @return String configured tool
	 */
	protected static String getConfiguredTool() {
		return configuredTool;
	}

	/**
	 * Will reset the cachedObjects & cachedCustomTables to start fresh, purging
	 * anything that may be stale.
	 */
	protected void reset() {
		cachedObjects.clear();
		cachedCustomTables.clear();
	}

	/**
	 * Will remove the entry for the hashKey
	 *
	 * @param hashKey key
	 */
	protected void removeFromCache(String hashKey) {
		cachedObjects.remove(hashKey);
	}

	/**
	 * Will check for a reset file for the current class, if one exists, will reset
	 * and delete the file
	 */
	protected void resetIfNeeded() {
		// In order to track if a class needs a reset we create a temporary file with
		// the Class that needs to be reset as the name. This file will be used to
		// trigger a reset in another class.

		// check if reset file exists for this class
		String resetFileName = AutomationHelper.getResetFilePath(this.getClass().getSimpleName());
		File resetFile = new File(resetFileName);
		if (resetFile.exists()) {
			reset();
			resetFile.delete();
		}
	}

	/**
	 * Uses the specified search config to locate and return the set of matching
	 * objects
	 *
	 * @param search criteria to find objects
	 * @return ArrayList{AutomatedObject} that matches search criteria
	 */
	protected ArrayList<AutomatedObject> getObjects(Search search) {
		// Check to see if the current class has been marked for a reset by another
		// class
		resetIfNeeded();
		return getAutomationTool().getAutomatedObjects(search);
	}

	/**
	 * Uses the specified search config to locate and return the object
	 *
	 * @param search criteria to find object
	 * @return AutomatedObject that matches search criteria
	 */
	protected AutomatedObject getObject(Search search) {
		resetIfNeeded();
		String hashKey = search.getHashKey();
		// If caching was turned off, remove it from the cache
		if (!search.isCacheResult()) {
			cachedObjects.remove(hashKey);
		}
		AutomatedObject obj = cachedObjects.get(hashKey);
		if (obj == null || !search.isCacheResult()) {
			obj = getAutomationTool().getAutomatedObject(search);
			if (search.isCacheResult() && obj != null) {
				cachedObjects.put(hashKey, obj);
				obj.setHashKey(hashKey);
			}
		}
		return obj;
	}

	/**
	 * Will find and return the object with the specified id
	 *
	 * @param id of the object
	 * @return AutomatedObject with the specified id
	 */
	protected AutomatedObject getObjectById(String id) {
		Search search = getSearch();
		search.addCriteria("id", id);

		return getObject(search);
	}

	/**
	 * Will find and return the object with the matching value property
	 *
	 * @param value of the object
	 * @return AutomatedObject with the specified value
	 */
	protected AutomatedObject getObjectByValue(String value) {
		Search search = getSearch();
		search.addCriteria("value", value);

		return getObject(search);
	}

	/**
	 * Will verify if an object matching the given search criteria is present <br>
	 * This method will use a default wait time of <b>1 second</b><br>
	 * This method will always suppress both ObjectNotFound and AmbiguousObject
	 * Exceptions
	 *
	 * @param search criteria to find object criteria
	 * @return boolean true if an object is found matching the search criteria,
	 *         false if not
	 */
	protected boolean isObjectPresent(Search search) {
		return isObjectPresent(search, 1);
	}

	/**
	 * Will verify if an object matching the given search criteria is present <br>
	 * This method will always suppress both ObjectNotFound and AmbiguousObject
	 * Exceptions
	 *
	 * @param search            criteria to find object criteria
	 * @param waitTimeInSeconds how much time to wait before deciding if object is
	 *                          there
	 * @return boolean true if an object is found matching the search criteria,
	 *         false if not
	 */
	protected boolean isObjectPresent(Search search, int waitTimeInSeconds) {
		search.setThrowObjectNotFound(false);
		search.setThrowAmbiguousObject(false);
		search.setWaitTimeInSeconds(waitTimeInSeconds);

		boolean found = false;

		AutomatedObject obj = getObject(search);
		if (obj != null) {
			found = true;
			// getAutomationTool().printObjectProperties(obj, false, false);
		}
		return found;
	}

	/**
	 * This method will recursively search for the innerText of the specified
	 * object, recursively searching through child objects until the first set of
	 * text that is found that matches the specified expression.
	 *
	 * @param object     object to search for the innerText of
	 * @param expression the regular expression that the text you are looking for
	 *                   should match
	 * @return String the first set of text found that matches the specified
	 *         expression
	 */
	protected String getInnerText(AutomatedObject object, String expression) {
		String text = object.readText().trim();
		if (!text.matches(expression)) {
			// look to see if there is hidden text
			text = object.getPropertyValue("innerText").trim();
			// check that the text has alpha/numeric characters, if not keep searching
			if (!text.matches(expression)) {

				// Loop through children objects to find if any of them have innerText
				// that can be used for the object value.
				AutomatedObject[] children = object.getChildren();
				for (AutomatedObject child : children) {
					text = getInnerText(child, expression);
					if (!text.isEmpty()) {
						// we have a value, break out child loop
						break;
					}
				}
			}
		}

		return text;
	}

	/**
	 * Will return the CustomTable for the hashKey, returns NULL if there is not an
	 * entry
	 *
	 * @param hashKey to locate table
	 * @return CustomTable table
	 */
	protected CustomTable getCustomTable(String hashKey) {
		return cachedCustomTables.get(hashKey);
	}

	/**
	 * Will add the entry to the CustomTable cache
	 *
	 * @param hashKey to locate table
	 * @param table   to add to cache
	 */
	protected void putCustomTable(String hashKey, CustomTable table) {
		cachedCustomTables.put(hashKey, table);
	}

	/**
	 * Will remove the entry for the hashKey from the cache
	 *
	 * @param hashKey to remove
	 */
	protected void removeCustomTable(String hashKey) {
		cachedCustomTables.remove(hashKey);
	}

	/**
	 * Creates a search object. Descending classes can implement getSearch to create
	 * a search with standard requirements.
	 *
	 * This default implementation will create a Search with no criteria other than
	 * the default settings for throwing exceptions, waiting for an object and wait
	 * time.
	 *
	 * @return Search new Search object
	 */
	protected Search getSearch() {
		return new Search();
	}

	/**
	 * Returns a reference to the current testing platform. This will enable the
	 * code to trigger the correct platform specific code to execute while
	 * interacting with the page
	 *
	 * @return AutomationTool currently configured
	 */
	public static AutomationTool getAutomationTool() {
		if (getConfiguredTool() == null || getConfiguredTool().isEmpty()) {
			throw new AutomationToolNotConfiguredException();
		}
		AutomationTool tool = toolMap.get(getConfiguredTool().toLowerCase());
		if (tool == null) {
			throw new RuntimeException("UNABLE TO LOCATE LIBRARY FOR: " + getConfiguredTool());
		}

		return tool;
	}

	/**
	 * Returns the ArrayList for all the object criteria for this search. This will
	 * be the property values used for the search. <BR>
	 * <BR>
	 * <b>THERE SHOULD BE NO REASON TO USE THIS OUTSIDE OF CORE CODE</b> <br>
	 * This is a wrapper method that will allow implementations of SeleniumCore and
	 * RftCore, etc to access the getSearchCriteriaArray method without opening it
	 * to be public
	 *
	 * @param search criteria
	 * @return ArrayList{CriteriaObject} all the criteria objects for this search
	 */
	protected static ArrayList<CriteriaObject> getSearchCriteriaArray(Search search) {
		return search.getSearchCriteriaArray();
	}

}
