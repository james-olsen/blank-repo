package platformIndependentCore.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import platformIndependentCore.exceptions.AutomatedPageStateException;
import platformIndependentCore.tables.TableObject;
import utilities.Deque508;

/**
 * Implementation to interact with the objects on a webpage
 *
 * @author VBAAUSTAYLOL
 *
 */
public abstract class AutomatedPage extends ToolManager {
	/** logger instance for this class */
	private static Logger log = LogManager.getLogger(AutomatedPage.class.getName());
	/** URL of this page */
	private String pageURL;
	/** Action URL Suffix of this page */
	private String actionURLSuffix;
	/** browser instance */
	protected BrowserObject browser;

	/**
	 * Will read the Title property of the current page
	 *
	 * @return String value for the current page title
	 */
	public String readPageTitle() {
		return getAutomationTool().readPageTitle();
	}

	/**
	 * Will return the Class Name for this page class instance
	 *
	 * @return String class name
	 */
	public String getPageClassName() {
		return getClass().getSimpleName();
	}

	// This method may be helpful in the future if we work with an application where
	// the url doesn't change a lot, but the pages do.
	// /**
	// * Returns a reference to the current Browser
	// *
	// * @return BrowserObject
	// */
	// protected BrowserObject getBrowserByTitleContains(String titleContains) {
	// //if (browser == null) {
	// Search search = new Search();
	// search.setWindowTitleContains(titleContains);
	// browser = getAutomationTool().getBrowser(search);
	// //}
	// return browser;
	// }

	/**
	 * Returns a reference to the current Browser
	 *
	 * @param urlContains text to match in the Page and/or Action URL
	 * @return BrowserObject instance matching urlContains
	 */
	protected BrowserObject getBrowserByUrlContains(String urlContains) {
		resetIfNeeded();
		if (browser != null && !browser.getCurrentUrl().contains(urlContains)) {
			browser = null;
		}
		if (browser == null) {
			Search search = new Search();
			// Try both
			search.setWindowUrlContains(urlContains);
			search.setActionUrlContains(urlContains);
			browser = getAutomationTool().getBrowser(search);
		}
		return browser;
	}

	/**
	 * Returns a reference to the current Browser
	 *
	 * @return BrowserObject current browser instance
	 */
	protected BrowserObject getBrowser() {
		resetIfNeeded();
		if (browser == null) {
			Search search = new Search();
			search.setWindowTitleContains(readPageTitle());
			browser = getAutomationTool().getBrowser(search);
		}
		return browser;
	}

	/**
	 * Returns the url for this page object
	 *
	 * @return String configured page url
	 */
	public String getPageUrl() {
		return pageURL;
	}

	/**
	 * Returns the Action URL Suffix for this page object
	 *
	 * @return String configured Action URL Suffix
	 */
	public String getActionUrlSuffix() {
		return actionURLSuffix;
	}

	/**
	 * Will set the page url for this Page object
	 *
	 * @param url expected for this page
	 */
	protected void setPageURL(String url) {
		pageURL = url;
	}

	/**
	 * Will set the Action URL Suffix for this Page object
	 *
	 * @param urlSuffix Action URL Suffix expected for this page
	 */
	protected void setActionURLSuffix(String urlSuffix) {
		actionURLSuffix = urlSuffix;
	}

	/**
	 * Will close the browser
	 */
	public void closeBrowser() {
		if (Deque508.isScanFor508Enabled()) {
			// TODO - commenting out for now as I don't have the sheet name here. Finalize
			// is called at the end of a script completion, so unless it is possible that a
			// single script could run with multiple sheet names this could be OK. If we do
			// need to support multiple sheets in a single script, then I will have to
			// revisit adding code here
			// Deque508.getInstance().finalize508Scan();
		}

		getAutomationTool().getBrowser(getSearch()).close();
		browser = null;
	}

	/**
	 * Will verify if there is currently a browser loaded to the Page URL and/or
	 * Action URL
	 *
	 * @return boolean TRUE if browser found with the configured Page URL and/or
	 *         action URL; False otherwise
	 * @throws AutomatedPageStateException if the Page URL has not been set
	 */
	public boolean isPageLoaded() {
		if (getPageUrl() == null || getPageUrl().isEmpty()) {
			throw new AutomatedPageStateException(
					"You can not call isPageLoaded for an AutomatedPage if you have not first called setPageURL");
		}
		return getAutomationTool().isBrowserPresent(getPageUrl(), getActionUrlSuffix());
	}

	/**
	 * Will load the current page URL in the browser
	 */
	public void loadPage() {
		log.info("LOADING PAGE: " + pageURL);
		getAutomationTool().loadPage(pageURL);
		browser = null;
		waitForPageLoad();
	}

	/**
	 * Will return a DropDownInterface object that matches the provided id
	 *
	 * @param id of the drop down
	 * @return DropDownInterface for matching object
	 */
	protected DropDownInterface getDropDownById(String id) {
		Search search = getSearch();
		search.addCriteria("id", id);
		return getDropDown(search);
	}

	/**
	 * Uses the specified search to locate and return a DropDown
	 *
	 * @param search criteria to find object
	 * @return DropDownInterface for object matching search criteria
	 */
	protected DropDownInterface getDropDown(Search search) {
		resetIfNeeded();
		return getAutomationTool().getDropDown(search);
	}

	/**
	 * Will return a TableObject instance representing the specified AutomatedObject
	 * and primary columns. Note: This would only be used in cases where we weren't
	 * creating a custom table.
	 *
	 * @param object table instance
	 * @param cols   primary columns used to identify rows
	 * @return TableObject instance
	 */
	protected TableObject getTableObject(AutomatedObject object, String... cols) {
		resetIfNeeded();
		return getAutomationTool().getTableObject(object, cols);
	}

	/**
	 * Creates a search object for the specified type and value on the testing
	 * platform currently being executed
	 *
	 * @param type  of criteria
	 * @param value of criteria
	 * @return Search object with criteria
	 */
	protected Search getSearch(String type, String value) {
		Search search = getSearch();
		search.addCriteria(type, value);
		return search;
	}

	/**
	 * Creates a search object. Descending classes can implement getSearch to create
	 * a search with standard requirements.
	 *
	 * This default implementation will create a Search with no criteria and default
	 * settings.
	 *
	 * @return Search new Search object
	 */
	@Override
	protected Search getSearch() {
		Search search = new Search();
		if (getPageUrl() != null && !getPageUrl().isEmpty()) {
			search.setWindowUrlContains(getPageUrl());
		}
		if (getActionUrlSuffix() != null && !getActionUrlSuffix().isEmpty()) {
			search.setActionUrlContains(getActionUrlSuffix());
		}
		return search;
	}

	/**
	 * Will click a link with the matching text <br>
	 * Will issue a <i><b>waitForPageLoad()</b></i> call, which in turn will issue a
	 * <i><b>reset()</b></i> call since they are typically needed after a link is
	 * clicked
	 *
	 * @param text of the link to click
	 */
	protected void clickLink(String text) {
		resetIfNeeded();
		getAutomationTool().clickLink(text, getSearch());
		waitForPageLoad();
	}

	/**
	 * Will select a file from the popup window to select Files from the computer
	 * Relies on the pop up being open and available
	 *
	 * @param file to select from the popup window
	 */
	protected void fileSelect(String file) {
		getAutomationTool().selectFile(file);
	}

	/**
	 * Method to make sure page is fully loaded before moving on to the next command
	 * <br>
	 * Will issue a <i><b>reset()</b></i> call since it is typically needed when a
	 * page refreshes or reloads
	 */
	protected void waitForPageLoad() {
		getAutomationTool().waitForPageLoad();
		reset();
	}

	/**
	 * Method to make sure page is fully loaded before moving on to the next
	 * command. <br>
	 * <br>
	 * This method will perform the click operation on the provided object and then
	 * wait for a new window instance to open before returning
	 *
	 * @param objectToClick object instance that will be clicked within the method
	 * @param nativeClick   specifies if we need to issue a native click or not
	 */
	protected void waitForNewWindowAfterClick(AutomatedObject objectToClick, boolean nativeClick) {
		resetIfNeeded();
		getAutomationTool().waitForNewWindowAfterClick(objectToClick, nativeClick);
	}

}
