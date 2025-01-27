package platformIndependentCore.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import platformIndependentCore.core.CriteriaObject.REGEX;
import platformIndependentCore.tables.TableObject;
import platformIndependentCore.utilities.ConfigProperties;

/**
 * Object for configuring a search
 *
 * @author VBAAUSTAYLOL
 *
 */
public class Search extends ToolManager {
	/** parent object */
	protected AutomatedObject parent = null;
	/** Search to find the parent */
	protected Search parentSearch = null;
	/** determines if Ambiguous Object Exception will be thrown */
	boolean throwAmbiguousObject;
	/** determines if Object Not Found Exception will be thrown */
	boolean throwObjectNotFound = true;
	/** If TRUE will only match direct children, false will return any descendant */
	boolean directChild;
	/** Will the Search wait for an object if not found right away */
	boolean waitForObject = true;
	/**
	 * Will Search wait after object is found until either timeout or a non-empty
	 * value exists
	 */
	boolean waitForNonEmpty = false;
	/** Should the search use the default parent */
	boolean useDefaultParent = false;
	/** Should the found object be cached */
	boolean isCacheResult = true;
	/** Wait Time in Milliseconds */
	long waitTimeInMilliseconds = TimeUnit.SECONDS.toMillis(30);
	/** iFrames that the object resides in */
	String[] iFrameIds = new String[0];
	/** String to match in the URL */
	String windowUrlContains = null;
	/** String to match in the Action URL */
	String actionUrlContains = null;
	/** String to match in the Window Title */
	String windowTitleContains = null;
	/** The hashKey to be used with the Search result */
	String hashKey = "";

	/** STATE condition for the matching object (CLICKABLE< VISIBLE, NONEMPTY) */
	CONDITION state = null;

	/** CONDITION ENUM holds valid states to require in Search */
	public enum CONDITION {
		/** Object must be clickable */
		CLICKABLE,
		/** Object must be visible */
		VISIBLE,
		/** Object must be non-empty */
		NONEMPTY
	}

	/** List of all the criteria to locate the object */
	ArrayList<CriteriaObject> criteria = new ArrayList<CriteriaObject>();

	/**
	 * Constructor will copy some of the basic search settings from another search
	 * config. These settings are: - ThrowAmbiguousObject - ThrowExceptions -
	 * WaitForObject
	 *
	 * All other settings will be reset for this specific search and must be set
	 * manually if a user wishes to use non-default values
	 *
	 * @param search use existing search to set basic settings of this new search
	 */
	public Search(Search search) {
		// Copy most of the settings from the specified search config
		setThrowAmbiguousObject(search.isThrowAmbiguousObject());
		setThrowObjectNotFound(search.isThrowObjectNotFound());
		setWaitForObject(search.isWaitForObject());
		setiFrameIds(search.getiFrameIds());
		setWindowTitleContains(search.getWindowTitleContains());
		setWindowUrlContains(search.getWindowUrlContains());
		setActionUrlContains(search.getActionUrlContains());

		setWaitTimeInSeconds(
				Long.valueOf(ConfigProperties.getValue("SEARCH_WAIT_SECONDS", String.valueOf(getWaitTimeInSeconds()))));
	}

	/**
	 * Constructor will use all the configured (in config.properties) or default
	 * values
	 */
	public Search() {
		// Use all default values, but double check the config file
		setUseDefaultParent(Boolean.valueOf(ConfigProperties.getValue(ConfigProperties.SEARCH_USE_DEFAULT_PARENT,
				String.valueOf(useDefaultParent))));
		setThrowAmbiguousObject(Boolean.valueOf(ConfigProperties.getValue(ConfigProperties.SEARCH_THROW_AMBIGUOUS,
				String.valueOf(throwAmbiguousObject))));
		setThrowObjectNotFound(Boolean.valueOf(ConfigProperties.getValue(ConfigProperties.SEARCH_THROW_OBJECT_NOT_FOUND,
				String.valueOf(throwObjectNotFound))));
		setWaitForObject(Boolean
				.valueOf(ConfigProperties.getValue(ConfigProperties.SEARCH_WAIT, String.valueOf(waitForObject))));
		setWaitTimeInSeconds(Long.valueOf(ConfigProperties.getValue(ConfigProperties.SEARCH_WAIT_SECONDS,
				String.valueOf(getWaitTimeInSeconds()))));
	}

	/**
	 * Constructor set throwExceptions, throwAmbiguous and waitForObject
	 *
	 * @param throwExceptions TRUE if search should throw an exception if no
	 *                        matching objects are found, FALSE if not
	 * @param throwAmbiguous  TRUE if search should throw AmbiguousObject if more
	 *                        than one match is found, FALSE if not
	 * @param waitForObject   TRUE if the search will wait for the object, FALSE if
	 *                        not
	 */
	public Search(boolean throwExceptions, boolean throwAmbiguous, boolean waitForObject) {
		setThrowAmbiguousObject(throwAmbiguous);
		setThrowObjectNotFound(throwExceptions);
		setWaitForObject(waitForObject);
		setWaitTimeInSeconds(
				Long.valueOf(ConfigProperties.getValue("SEARCH_WAIT_SECONDS", String.valueOf(getWaitTimeInSeconds()))));
	}

	/**
	 * Will add a Criteria to this search
	 *
	 * @param criteriaName  used to id object
	 * @param criteriaValue used to id object
	 */
	public void addCriteria(String criteriaName, Object criteriaValue) {
		CriteriaObject newCriteria = getAutomationTool().createCriteria(criteriaName, criteriaValue);
		criteria.add(newCriteria);
	}

	/**
	 * Will add a Criteria to this search
	 *
	 * @param criteriaName  used to id object
	 * @param criteriaValue used to id object
	 * @param regexSetting  - REGEX criteria used for search
	 */
	public void addCriteria(String criteriaName, String criteriaValue, REGEX regexSetting) {
		CriteriaObject newCriteria = getAutomationTool().createCriteriaRegex(criteriaName, criteriaValue, regexSetting);
		criteria.add(newCriteria);
	}

	/**
	 * Will add the specified CriteriaObject to this search <BR>
	 * <BR>
	 * <b>THERE SHOULD BE NO REASON TO USE THIS OUTSIDE OF CORE CODE</b> <br>
	 *
	 * @param critObj used to id object
	 */
	@Deprecated
	public void addCriteria(CriteriaObject critObj) {
		criteria.add(critObj);
	}

	/**
	 * Returns the ArrayList for all the object criteria for this search. This will
	 * be the property values used for the search. <BR>
	 * <BR>
	 * <b>THERE SHOULD BE NO REASON TO USE THIS OUTSIDE OF CORE CODE <br>
	 * This is packaged scoped. Automation Tool Core projects will have to use this
	 * method to access the search criteria Arrays since making this public would
	 * make this accessible outside the Core.
	 *
	 * @return ArrayList<CriteriaObject> list of Criteria used to locate object
	 */
	ArrayList<CriteriaObject> getSearchCriteriaArray() {
		return criteria;
	}

	/**
	 * Will return a string representation of the search object
	 *
	 * @return String representation of all Criteria in this Search
	 */
	public String getCriteriaAsString() {
		String details = "SEARCH CONFIG: ";
		details += "||directChild: " + isDirectChild() + "\n, isUseDefaultParent: " + isUseDefaultParent()
				+ "\n, windowUrlContains: " + getWindowUrlContains() + "\n, windowTitleContains: "
				+ getWindowTitleContains() + "\n, actionUrlContains: " + getActionUrlContains() + "\n, iFrameIds: "
				+ Arrays.toString(getiFrameIds()) + "\n, wait: " + isWaitForObject() + ", waitTime: "
				+ getWaitTimeInSeconds() + "\n, throwObjectNotFound: " + isThrowObjectNotFound()
				+ "\n, throwAmbiguous: " + isThrowAmbiguousObject() + " || \n ";

		for (CriteriaObject crit : criteria) {
			details += crit.getCriteriaType() + "=" + crit.getCriteriaValue() + " REGEX=" + crit.getRegexSetting()
					+ "; ";
		}
		if (state != null) {
			details += "\n CONDITION=" + state;
		}

		if (parent != null) {
			details += "\n************* SEARCH: PARENT CRITERIA SET *************";
			// details += parent.toString();

			/**
			 * TODO: #181 - Look into what might cause a WebElement to hang when getting a
			 * property value. This is causing tables to stall/freeze during instantiation.
			 * Need to debug.
			 *
			 * UserAccountsTable in VAO will fail on the following getPropertyValue calls
			 * without this printObjectProperties here. Even a long thread.sleep doesn't fix
			 * whatever is causing this. There doesn't seem to be any structural difference
			 * between this table and any other table we've worked on (in VAO).
			 */
//			getAutomationTool().printObjectProperties(parent, false, false);
			String tag = parent.getPropertyValue("tag");
			String id = parent.getPropertyValue("id");
			String text = parent.readText();
			details += "\n    PARENT ID=" + id + "\n    PARENT TAG=" + tag + "\n    PARENT TEXT=" + text;
			details += "\n************* SEARCH: END PARENT OBJECT INFO *************\n";
		} else {
			details += "\n    NO PARENT CRITERIA PROVIDED \n";
		}
		return details;
	}

	/**
	 * Will generate a hash key to represent the object this search is looking for
	 *
	 * @return hashKey value
	 */
	String getHashKey() {
		// generate a key for to be used for hashmaps storage of this object
		if (hashKey == null || hashKey.isEmpty()) {
			hashKey = "KEY: ";
			hashKey += getCriteriaAsString() + "||PARENT=" + getParent();
		}
		return hashKey;
	}

	/**
	 * Returns the default wait time (in seconds) This is not the configured wait
	 * time, but the default for this project (the value specified in
	 * config.properties using the SEARCH_WAIT_SECONDS setting or the overall
	 * default (30)
	 *
	 * @return long default wait time in seconds
	 */
	public static long getDefaultWaitTimeInSeconds() {
		long defaultWait = TimeUnit.MILLISECONDS.toSeconds(30);
		return Long.valueOf(ConfigProperties.getValue("SEARCH_WAIT_SECONDS", String.valueOf(defaultWait)));
	}

	/**
	 * Sets the desired state or condition of an object to wait for
	 *
	 * @param condition to wait for
	 */
	public void setCondition(CONDITION condition) {
		this.state = condition;
	}

	/**
	 * Specifies if this Search will cache the resulting object
	 *
	 * @return boolean TRUE the object will be cached, FALSE it will not
	 */
	public boolean isCacheResult() {
		return isCacheResult;
	}

	/**
	 * Will set if this object should be cached or not
	 *
	 * @param isCacheResult TRUE the object will be cached, FALSE it will not
	 */
	public void setCacheResult(boolean isCacheResult) {
		this.isCacheResult = isCacheResult;
	}

	/**
	 * Checks if the CLICKABLE condition is required
	 *
	 * @return TRUE if Search requires CLICKABLE, FALSE if not
	 */
	public boolean isWaitForClickable() {
		return isWait(CONDITION.CLICKABLE);
	}

	/**
	 * Checks if the VISIBLE condition is required
	 *
	 * @return TRUE if Search requires VISIBLE, FALSE if not
	 */
	public boolean isWaitForVisible() {
		return isWait(CONDITION.VISIBLE);
	}

	/**
	 * Checks if the required state/condition matches the provided one
	 *
	 * @param condition to verify
	 * @return TRUE if Search requires specified condition, FALSE if not
	 */
	private boolean isWait(CONDITION condition) {
		boolean wait = false;
		if (state != null && state == condition) {
			wait = true;
		}
		return wait;
	}

	/**
	 * Returns array of iFrameIds
	 *
	 * @return the iFrameId
	 */
	public String[] getiFrameIds() {
		return iFrameIds;
	}

	/**
	 * Return parent for this search
	 *
	 * @return AutomatedObject parent
	 */
	public AutomatedObject getParent() {
		if (parent == null && isUseDefaultParent()) {
			parent = getAutomationTool().getDefaultParent();
		}

		return parent;
	}

	/**
	 * returns the waitTime for this search (in milliseconds)
	 *
	 * @return long wait time in milliseconds
	 */
	public long getWaitTimeInMilliseconds() {
		return waitTimeInMilliseconds;
	}

	/**
	 * returns the waitTime for this search (in seconds)
	 *
	 * @return long wait time in seconds
	 */
	public long getWaitTimeInSeconds() {
		return TimeUnit.MILLISECONDS.toSeconds(waitTimeInMilliseconds);
	}

	/**
	 * This will return the substring that the window title must contain
	 *
	 * @return String the windowTitleContains value
	 */
	public String getWindowTitleContains() {
		return windowTitleContains;
	}

	/**
	 * This will return the substring that the Window URL must contain
	 *
	 * @return String the windowUrlContains value
	 */
	public String getWindowUrlContains() {
		return windowUrlContains;
	}

	/**
	 * This will return the substring that the Action URL must contain
	 *
	 * @return String the actionUrlContains value
	 */
	public String getActionUrlContains() {
		return actionUrlContains;
	}

	/**
	 * Returns boolean specifying if the search should wait while searching
	 *
	 * @return boolean TRUE if the search will wait for the object, FALSE if not
	 */
	public boolean isWaitForObject() {
		return waitForObject;
	}

	/**
	 * Returns boolean specifying if the search should wait while searching until
	 * the object has a non-empty value
	 *
	 * @return boolean TRUE if wait for non-empty object, FALSE if not
	 */
	public boolean isWaitForNonEmpty() {
		return waitForNonEmpty;
	}

	/**
	 * Specifies if this search will throw Object Not Found exceptions
	 *
	 * @return boolean TRUE if search should throw exceptions if not found, FALSE if
	 *         not
	 */
	public boolean isThrowObjectNotFound() {
		return throwObjectNotFound;
	}

	/**
	 * Specifies if this search will throw AmbiguousObject exception
	 *
	 * @return boolean TRUE if search should throw AmbiguousObject if more than one
	 *         match is found, FALSE if not
	 */
	public boolean isThrowAmbiguousObject() {
		return throwAmbiguousObject;
	}

	/**
	 * Specifies if this search will use direct child criteria for matching objects
	 *
	 * @return boolean TRUE if it requires object to be a direct child, FALSE if not
	 */
	public boolean isDirectChild() {
		return directChild;
	}

	/**
	 * Verifies if the search should use a default parent
	 *
	 * @return TRUE if it will use a default parent, FALSE if not
	 */
	public boolean isUseDefaultParent() {
		return useDefaultParent;
	}

	/**
	 * Sets the wait time for the search (in milliseconds) Search must also be
	 * configured to waitForObject for this to have an impact
	 *
	 * @param waitTimeInMilliseconds to wait for object
	 */
	public void setWaitTimeInMilliseconds(long waitTimeInMilliseconds) {
		this.waitTimeInMilliseconds = waitTimeInMilliseconds;
	}

	/**
	 * Sets the wait time for the search (in milliseconds) Search must also be
	 * configured to waitForObject for this to have an impact
	 *
	 * @param waitTimeInSeconds to wait for object
	 */
	public void setWaitTimeInSeconds(long waitTimeInSeconds) {
		this.waitForObject = true;
		this.waitTimeInMilliseconds = TimeUnit.SECONDS.toMillis(waitTimeInSeconds);
	}

	/**
	 * Sets if this search should wait for an object
	 *
	 * @param waitForObject TRUE if the search should wait for the object, FALSE if
	 *                      not
	 */
	public void setWaitForObject(boolean waitForObject) {
		this.waitForObject = waitForObject;
	}

	/**
	 * Sets if this search should wait for an object to have a non-empty value
	 *
	 * @param waitForNonEmpty TRUE if the search should wait for the object to have
	 *                        a non-empty value, FALSE if not
	 */
	public void setWaitForNonEmpty(boolean waitForNonEmpty) {
		this.waitForNonEmpty = waitForNonEmpty;
	}

	/**
	 * Sets if this search will throw AmbiguousObject exception
	 *
	 * @param throwAmbiguousObject TRUE if search should throw AmbiguousObject if
	 *                             more than one match is found, FALSE if not
	 */
	public void setThrowAmbiguousObject(boolean throwAmbiguousObject) {
		this.throwAmbiguousObject = throwAmbiguousObject;
	}

	/**
	 * Specifies if this search will throw exceptions
	 *
	 * @param throwObjectNotFound TRUE if search should throw an exception if no
	 *                            matching objects are found, FALSE if not
	 */
	public void setThrowObjectNotFound(boolean throwObjectNotFound) {
		this.throwObjectNotFound = throwObjectNotFound;
	}

	/**
	 * Sets if this search will use direct child criteria for matching objects
	 *
	 * @param directChild TRUE if search will only return direct child matches,
	 *                    false if it will return any descendant matches
	 */
	public void setDirectChild(boolean directChild) {
		this.directChild = directChild;
	}

	/**
	 * Sets the parent for the search to the specified AutomatedObjext
	 *
	 * @param parent to search under
	 */
	public void setParent(AutomatedObject parent) {
		this.parent = parent;
	}

	/**
	 * Sets the parent for the search to the specified AutomatedObjext
	 *
	 * @param parent table object to search under
	 */
	public void setParent(TableObject parent) {
		this.parent = parent.getAutomatedObject();
	}

	/**
	 * Will set the iFrames that this search should execute in. More than one
	 * iFrameId may be specified, but please keep in mind the order does matter.
	 * Multiple iFrames should be specified from the outermost (largest) iframe, to
	 * the smallest, most specific. <br>
	 * <br>
	 * <b>Please note that any specified parent object will be ignored when working
	 * with iFrames</b>
	 *
	 * @param iFrameId list of required iFrameIds, must be in order from outer most
	 *                 to inner most
	 */
	public void setiFrameIds(String... iFrameId) {
		this.iFrameIds = iFrameId;
		// If they are using iFrames, will not want to use default parent
		setUseDefaultParent(false);
	}

	/**
	 * Require the Window Title to contain the specified text <BR>
	 *
	 * @param titleContains text to require in the Title of the page
	 */
	public void setWindowTitleContains(String titleContains) {
		this.windowTitleContains = titleContains;
	}

	/**
	 * Require the Window URL to contain the specified text
	 *
	 * @param urlContains text to require in the URL
	 */
	public void setWindowUrlContains(String urlContains) {
		this.windowUrlContains = urlContains;
	}

	/**
	 * Require the Action URL to contain the specified text
	 *
	 * @param urlContains text to require in the URL
	 */
	public void setActionUrlContains(String urlContains) {
		this.actionUrlContains = urlContains;
	}

	/**
	 * Verifies if UseDefaultParent is set
	 *
	 * @param useDefaultParent TRUE if search should use a default parent, false if
	 *                         not
	 */
	public void setUseDefaultParent(boolean useDefaultParent) {
		this.useDefaultParent = useDefaultParent;
	}

}
