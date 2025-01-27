package platformIndependentCore.core;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import platformIndependentCore.core.CriteriaObject.REGEX;
import platformIndependentCore.events.ScreenShot;
import platformIndependentCore.exceptions.UnimplementedMethodException;
import platformIndependentCore.scripts.Arguments;
import platformIndependentCore.scripts.ScriptResults;
import platformIndependentCore.scripts.TestScriptInterface;
import platformIndependentCore.scripts.TestScriptManager;
import platformIndependentCore.tables.TableObject;
import rftCore.RftCriteriaObject;
import rftCore.RftHelper;
import rftCore.RftPage;
import rftCore.RftScriptManager;
import rftCore.RftTable;
import seleniumCore.SeleniumCriteriaObject;
import seleniumCore.SeleniumHelper;
import seleniumCore.SeleniumObject;
import seleniumCore.SeleniumPage;
import seleniumCore.SeleniumScriptManager;
import seleniumCore.seleniumFindObject;
import silkTestCore.SilkCriteriaObject;
import silkTestCore.SilkTestHelper;
import silkTestCore.SilkTestPage;
import silkTestCore.SilkTestScriptManager;

/**
 * Class that controls access to platform specific implementations. This allows
 * the end user to blindly interface with the specific platforms with no
 * knowledge or direct access to it. Test scripts and page classes can be
 * written independent of where they will be executed.
 *
 * Each platform will have an entry into the enum that will provide the concrete
 * implementations for abstract methods defined in the TestingPlatform object.
 * These implementations will hand off to the platform specific classes.
 *
 * There must be "dummy" or "stubbed" implementations of all the platform
 * specific object created here. When running, you must have a fully developed
 * jar file for the specific platform.
 *
 * Trying to run with a platform you do not have the jar file for will result in
 * a MissingPlatformLibraries exception to be thrown
 *
 * @author VBAAUSTAYLOL
 *
 */
// TODO - was named TestingPlatform - need to make sure RFT was updated/refactored correctly

public enum AutomationTool {

	/**
	 * Implementation for running on RFT
	 */
	RFT("rft") {
		// @Override
		// public TestScript getAutomatedTestScriptInstance(TestScriptManager script) {
		// // TODO Auto-generated method stub
		// return null;
		// }

		// @Override
		// public AutomatedObject getAutomatedObject(AutomatedObject parent,
		// CriteriaObject criteria) {
		// return RftHelper.getObject(parent, criteria);
		// }

		@Override
		public void clear(AutomatedObject field) {
			// TODO Auto-generated method stub

		}

		@Override
		public void clickAlertCancelButton() {
			// TODO Auto-generated method stub

		}

		@Override
		public void clickAlertAcceptButton() {
			// TODO Auto-generated method stub

		}

		@Override
		public void clickLink(String linkText, Search search) {
			RftHelper.clickLink(linkText);
		}

		@Override
		public void closeBrowser(Search search) {
			// TODO Auto-generated method stub

		}

		@Override
		public CriteriaObject createCriteria(String type, Object value) {
			return new RftCriteriaObject(type, value);
		}

		@Override
		public CriteriaObject createCriteria(CriteriaObject[] multipleCriteria) {
			return new RftCriteriaObject(multipleCriteria);
		}

		@Override
		public CriteriaObject createCriteriaRegex(String type, String criteriaRegexValue) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public CriteriaObject createCriteriaRegex(String type, String criteriaRegexValue, REGEX regexSetting) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void dragAndDrop(AutomatedObject objectToMove, AutomatedObject objectForDropLocation) {
			// TODO Auto-generated method stub

		}

		@Override
		public void setSlider(AutomatedObject sliderObject, int percentValue) {
			// TODO Auto-generated method stub

		}

		@Override
		public void enterSecurityCredentialsInPopup(String userName, String password) {
			// TODO Auto-generated method stub

		}

		@Override
		TestScriptManager getNewTestScriptManager(TestScriptInterface testScript) {
			return new RftScriptManager(testScript);
		}

		@Override
		public DropDownInterface getDropDown(Search dropDownSearch) {
			return RftHelper.getDropDownObject(dropDownSearch);
		}

		@Override
		public AutomatedObject getAutomatedObject(Search config) {
			return RftHelper.searchForObject(config);
		}

		@Override
		public ArrayList<AutomatedObject> getAutomatedObjects(Search config) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public AutomatedPage getAutomatedPage(String url) {
			return new RftPage(url);
		}

		@Override
		public BrowserObject getBrowser(String pageUrl) {
			// return null;
			// return RftHelper.findBrowser(new RegularExpression(pageUrl, false), true);
			return RftHelper.getBrowser(pageUrl);
		}

		@Override
		public BrowserObject getBrowser(Search search) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public AutomatedObject getDefaultParent() {
			return RftHelper.getDefaultParent();
		}

		@Override
		public AutomatedObject getParentFrame(String... getiFrameIds) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public TableObject getTableObject(AutomatedObject object, String... cols) {
			return new RftTable(object, cols);
		}

		@Override
		public boolean isAlertPresent() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void loadPage(String url) {
			RftHelper.loadPage(url);
		}

		@Override
		public void printObjectProperties(AutomatedObject object, boolean printChildren, boolean printParent) {
			// TODO Auto-generated method stub

		}

		@Override
		public void printObjectsForSearch(Search search, boolean printChildren) {
			// TODO Auto-generated method stub

		}

		@Override
		public String readAlertMessage() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String readPageTitle() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ScriptResults runScript(TestScriptInterface scriptInstance, Arguments args) {
			RftScriptManager script = new RftScriptManager(scriptInstance);
			scriptInstance.setScript(script);
			return script.run(args);

		}

		@Override
		public ScriptResults runModularScript(TestScriptInterface scriptInstance, Arguments args) {
			RftScriptManager script = (RftScriptManager) getNewTestScriptManager(scriptInstance);// new
																									// RftScriptManager(scriptInstance);
			scriptInstance.setScript(script);
			// ScriptResults results = new ScriptResults(EVENT_TYPE.CALLEDSCRIPT);
			// scriptInstance.setResults(results);
			return script.runModular(args);
		}

		@Override
		public ScriptResults runExecutionScript(TestScriptInterface scriptInstance, Arguments args) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void selectFile(String file) {
			// TODO Auto-generated method stub

		}

		@Override
		public void sendKeys(AutomatedObject field, String text) {
			// TODO Auto-generated method stub

		}

		@Override
		public ScreenShot takeScreenShot(String snapShotFilePath, String snapShotNote) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void vpEquals(String expected, String actual) {
			// RftHelper.vpEquals(expected, actual);
			Logger log = LogManager.getLogger(AutomationTool.class.getName());

			log.error("VPs are not executed through the manager for RFT. Please re-check your code.");
		}

		@Override
		public void vpContains(String expected, String actual) {
			// TODO Auto-generated method stub

		}

		@Override
		public void vpMatches(String expected, String actual, REGEX regex) {
			// TODO Auto-generated method stub

		}

		@Override
		public void waitForPageLoad() {
			// TODO Auto-generated method stub

		}

		@Override
		public void waitForNewWindowAfterClick(AutomatedObject objectToClick, boolean nativeClick) {
			// TODO Auto-generated method stub

		}

		@Override
		public void waitForNotVisible(Search search) {
			throw new UnimplementedMethodException("waitForNotVisible");
		}

		@Override
		public void clickWithJavascript(Search defaultSearch, AutomatedObject object) {
			// TODO Auto-generated method stub

		}

		@Override
		public boolean isBrowserPresent(String urlContains) {
			throw new UnimplementedMethodException("isBrowserPresent");
		}

		@Override
		public boolean isBrowserPresent(String urlContains, String actionUrlContains) {
			throw new UnimplementedMethodException("isBrowserPresent");
		}

		@Override
		public void clickInputObject(Search search, AutomatedObject button) {
			// TODO Auto-generated method stub

		}

	},

	/**
	 * Implementation for running on Selenium
	 */
	SELENIUM("selenium") {

		@Override
		public void clear(AutomatedObject field) {
			SeleniumHelper.clear((SeleniumObject) field);
		}

		@Override
		public void closeBrowser(Search search) {
			SeleniumHelper.closeBrowser(search);
		}

		@Override
		public void clickAlertAcceptButton() {
			SeleniumHelper.clickAlertAcceptButton();
		}

		@Override
		public void clickAlertCancelButton() {
			SeleniumHelper.clickAlertCancelButton();
		}

		@Override
		public void clickLink(String linkText, Search search) {
			SeleniumHelper.clickLink(linkText, search);
		}

		@Override
		public CriteriaObject createCriteria(String type, Object value) {
			return new SeleniumCriteriaObject(type, value);
		}

		@Override
		public CriteriaObject createCriteria(CriteriaObject[] multipleCriteria) {
			return new SeleniumCriteriaObject(multipleCriteria);
		}

		@Override
		public CriteriaObject createCriteriaRegex(String type, String criteriaRegexValue) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public CriteriaObject createCriteriaRegex(String type, String criteriaRegexValue, REGEX regexSetting) {
			return new SeleniumCriteriaObject(type, criteriaRegexValue, regexSetting);
		}

		@Override
		public void setSlider(AutomatedObject sliderObject, int percentValue) {
			SeleniumHelper.setSlider((SeleniumObject) sliderObject, percentValue);
		}

		@Override
		public void dragAndDrop(AutomatedObject objectToMove, AutomatedObject objectForDropLocation) {
			SeleniumHelper.dragAndDrop((SeleniumObject) objectToMove, (SeleniumObject) objectForDropLocation);
		}

		@Override
		public void enterSecurityCredentialsInPopup(String userName, String password) {
			SeleniumHelper.enterSecurityCredentialsInPopup(userName, password);
		}

		@Override
		public AutomatedPage getAutomatedPage(String url) {
			return new SeleniumPage(url);
		}

		@Override
		public BrowserObject getBrowser(Search search) {
			return SeleniumHelper.getBrowser(search);
		}

		@Override
		public BrowserObject getBrowser(String pageUrl) {
			return SeleniumHelper.getBrowser(pageUrl);
		}

		@Override
		public DropDownInterface getDropDown(Search dropDownSearch) {
			return SeleniumHelper.getDropDownObject(dropDownSearch);
		}

		@Override
		public AutomatedObject getAutomatedObject(Search config) {
			// TODO Auto-generated method stub
			return SeleniumHelper.getObjectWithSelenium(config);
		}

		@Override
		public ArrayList<AutomatedObject> getAutomatedObjects(Search config) {
			return SeleniumHelper.getObjectsWithSelenium(config);
		}

		@Override
		public AutomatedObject getDefaultParent() {
			return SeleniumHelper.getDefaultParent();
		}

		@Override
		TestScriptManager getNewTestScriptManager(TestScriptInterface testScript) {
			return new SeleniumScriptManager(testScript);
		}

		@Override
		public AutomatedObject getParentFrame(String... frameIds) {
			return SeleniumHelper.getParentFrame(frameIds);
		}

		@Override
		public TableObject getTableObject(AutomatedObject object, String... cols) {
			return SeleniumHelper.getTable(object, cols);
		}

		@Override
		public boolean isAlertPresent() {
			return SeleniumHelper.isAlertPresent();
		}

		@Override
		public boolean isBrowserPresent(String urlContains) {
			return SeleniumHelper.isBrowserPresent(urlContains);
		}

		@Override
		public boolean isBrowserPresent(String urlContains, String actionUrlContains) {
			return SeleniumHelper.isBrowserPresent(urlContains, actionUrlContains);
		}

		@Override
		public void loadPage(String pageUrl) {
			// Need to get the Browser with an empty Search because the URL has not yet been
			// loaded, we want to make sure there is no criteria on the URL when loading.
			// After you get the browser, then you can issue the loadUrl call to navigate
			getBrowser(new Search()).loadUrl(pageUrl);
		}

		@Override
		public void printObjectProperties(AutomatedObject object, boolean printChildren, boolean printParent) {
			seleniumFindObject.printObjectProperties(object, printChildren, printParent);
		}

		@Override
		public void printObjectsForSearch(Search search, boolean printChildren) {
			seleniumFindObject.printObjectsForSearch(search, printChildren);
		}

		@Override
		public String readAlertMessage() {
			return SeleniumHelper.readAlertMessage();
		}

		@Override
		public String readPageTitle() {
			return SeleniumHelper.readPageTitle();
		}

		@Override
		public ScriptResults runScript(TestScriptInterface scriptInstance, Arguments args) {
			SeleniumScriptManager script = new SeleniumScriptManager(scriptInstance);
			scriptInstance.setScript(script);
			return script.run(args);

		}

		@Override
		public ScriptResults runExecutionScript(TestScriptInterface scriptInstance, Arguments args) {
			// return new ScriptResults();
			SeleniumScriptManager script = new SeleniumScriptManager(scriptInstance);
			scriptInstance.setScript(script);
			ScriptResults results = script.runExecution(args);
			return results;
		}

		@Override
		public void selectFile(String file) {
			SeleniumHelper.selectFile(file);
		}

		@Override
		public void sendKeys(AutomatedObject field, String text) {
			SeleniumHelper.sendKeys((SeleniumObject) field, text);
		}

		@Override
		public ScreenShot takeScreenShot(String snapShotFilePath, String snapShotNote) {
			return SeleniumHelper.takeSnapShot(snapShotFilePath, snapShotNote);
		}

		@Override
		public void vpEquals(String expected, String actual) {
			SeleniumHelper.vpEquals(expected, actual);
		}

		@Override
		public void vpContains(String expected, String actual) {
			SeleniumHelper.vpContains(expected, actual);
		}

		@Override
		public void vpMatches(String expected, String actual, REGEX regex) {
			SeleniumHelper.vpMatches(expected, actual, regex);
		}

		@Override
		public void waitForPageLoad() {
			SeleniumHelper.waitForPageLoad();
		}

		@Override
		public void waitForObject(Search search) {
			SeleniumHelper.waitForObject(search);
		}

		@Override
		public void waitForNewWindowAfterClick(AutomatedObject objectToClick, boolean nativeClick) {
			SeleniumHelper.waitForNewWindowAfterClick(objectToClick, nativeClick);
		}

		@Override
		public void waitForNotVisible(Search search) {
			SeleniumHelper.waitForNotVisible(search);
		}

		@Override
		public void clickWithJavascript(Search defaultSearch, AutomatedObject object) {
			SeleniumHelper.clickWithJavascript(defaultSearch, object);
		}

		@Override
		public void clickInputObject(Search search, AutomatedObject button) {
			SeleniumHelper.clickWithAction(search, button);
		}

	},

	/**
	 * Implementation for running on SilkTest
	 */
	SILKTEST("silktest") {

		@Override
		public void clear(AutomatedObject field) {
			// TODO Auto-generated method stub

		}

		@Override
		public void closeBrowser(Search search) {
			// TODO Auto-generated method stub

		}

		@Override
		public CriteriaObject createCriteria(String type, Object value) {
			return new SilkCriteriaObject(type, value);
		}

		@Override
		public CriteriaObject createCriteria(CriteriaObject[] multipleCriteria) {
			return new SilkCriteriaObject(multipleCriteria);
		}

		@Override
		public CriteriaObject createCriteriaRegex(String type, String criteriaRegexValue) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public CriteriaObject createCriteriaRegex(String type, String criteriaRegexValue, REGEX regexSetting) {
			return new SilkCriteriaObject(type, criteriaRegexValue, regexSetting);
		}

		@Override
		public void clickAlertCancelButton() {
			// TODO Auto-generated method stub

		}

		@Override
		public void clickAlertAcceptButton() {
			// TODO Auto-generated method stub

		}

		@Override
		public void clickLink(String linkText, Search search) {
			// TODO Auto-generated method stub
		}

		@Override
		public void dragAndDrop(AutomatedObject objectToMove, AutomatedObject objectForDropLocation) {
			// TODO Auto-generated method stub

		}

		@Override
		public void enterSecurityCredentialsInPopup(String userName, String password) {
			// TODO Auto-generated method stub

		}

		@Override
		public AutomatedObject getAutomatedObject(Search config) {
			// TODO Auto-generated method stub
			return SilkTestHelper.getSilkTestObject(config);
		}

		@Override
		public ArrayList<AutomatedObject> getAutomatedObjects(Search config) {
			return SilkTestHelper.getSilkTestObjects(config);
		}

		@Override
		public AutomatedPage getAutomatedPage(String url) {
			// return SeleniumPage.createInstance(url);
			return new SilkTestPage(url);
		}

		@Override
		public BrowserObject getBrowser(String pageUrl) {
			return SilkTestHelper.getBrowser(pageUrl);
		}

		@Override
		public BrowserObject getBrowser(Search search) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public AutomatedObject getDefaultParent() {
			return SilkTestHelper.getDefaultParent();
		}

		@Override
		public DropDownInterface getDropDown(Search dropDownSearch) {
			return SilkTestHelper.getDropDownObject(dropDownSearch);
		}

		@Override
		TestScriptManager getNewTestScriptManager(TestScriptInterface testScript) {
			return new SilkTestScriptManager(testScript);
		}

		@Override
		public AutomatedObject getParentFrame(String... getiFrameIds) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public TableObject getTableObject(AutomatedObject object, String... cols) {
			return SilkTestHelper.getTable(object, cols);
		}

		@Override
		public boolean isAlertPresent() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void loadPage(String pageUrl) {
			SilkTestHelper.getBrowser(pageUrl);
		}

		@Override
		public void printObjectProperties(AutomatedObject object, boolean printChildren, boolean printParent) {
			// seleniumFindObject.printObjectProperties(object, printChildren, printParent);
		}

		@Override
		public void printObjectsForSearch(Search search, boolean printChildren) {
			// seleniumFindObject.printObjectsForSearch(parent, search, printChildren);
		}

		@Override
		public String readAlertMessage() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String readPageTitle() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ScriptResults runScript(TestScriptInterface scriptInstance, Arguments args) {
			SilkTestScriptManager script = new SilkTestScriptManager(scriptInstance);
			scriptInstance.setScript(script);
			return script.run(args);

		}

		@Override
		public ScriptResults runExecutionScript(TestScriptInterface scriptInstance, Arguments args) {
			// return new ScriptResults();
			SilkTestScriptManager script = new SilkTestScriptManager(scriptInstance);
			scriptInstance.setScript(script);
			ScriptResults results = script.runExecution(args);
			return results;
		}

		@Override
		public void selectFile(String file) {
			SilkTestHelper.selectFile(file);
		}

		@Override
		public void sendKeys(AutomatedObject field, String text) {
			// TODO Auto-generated method stub

		}

		@Override
		public ScreenShot takeScreenShot(String snapShotFilePath, String snapShotNote) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void vpEquals(String expected, String actual) {
			Logger log = LogManager.getLogger(AutomationTool.class.getName());

			log.error("VPs are not executed through the manager for SilkTest. Please re-check your code.");

		}

		@Override
		public void vpContains(String expected, String actual) {
			// TODO Auto-generated method stub

		}

		@Override
		public void vpMatches(String expected, String actual, REGEX regex) {
			// TODO Auto-generated method stub

		}

		@Override
		public void waitForPageLoad() {
			// TODO Auto-generated method stub

		}

		@Override
		public void waitForNewWindowAfterClick(AutomatedObject objectToClick, boolean nativeClick) {
			// TODO Auto-generated method stub

		}

		@Override
		public void waitForNotVisible(Search search) {
			// TODO Auto-generated method stub

		}

		@Override
		public void clickWithJavascript(Search defaultSearch, AutomatedObject object) {
			// TODO Auto-generated method stub

		}

		@Override
		public boolean isBrowserPresent(String urlContains) {
			throw new UnimplementedMethodException("isBrowserPresent");
		}

		@Override
		public boolean isBrowserPresent(String urlContains, String actionUrlContains) {
			throw new UnimplementedMethodException("isBrowserPresent");
		}

		@Override
		public void setSlider(AutomatedObject sliderObject, int percentValue) {
			// TODO Auto-generated method stub

		}

		@Override
		public void clickInputObject(Search search, AutomatedObject button) {
			// TODO Auto-generated method stub

		}

	};

	/** automation tool platform for this instance */
	private String platform;

	/**
	 * Private constructor can only be used from above implementations for each
	 * TestingPlatform. Each type will add implementation for the abstract methods
	 * specific for the platform
	 *
	 * @param platform Automation Tool platform
	 */
	private AutomationTool(String platform) {
		this.platform = platform;
	}

	/**
	 * Will verify if a browser object is currently open with a Page URL and/or
	 * Action URL that contains the supplied URL fragment
	 *
	 * @param urlContains substring of current URL
	 * @return TRUE if browser is found with matching URL, FALSE if not
	 */
	public abstract boolean isBrowserPresent(String urlContains);

	/**
	 * Will verify if a browser object is currently open with a Page URL and/or
	 * Action URL that contains the supplied URL fragment
	 *
	 * @param urlContains       substring of current Page URL
	 * @param actionUrlContains substring of current Action URL
	 * @return TRUE if browser is found with matching URL, FALSE if not
	 */
	public abstract boolean isBrowserPresent(String urlContains, String actionUrlContains);

	/**
	 * Will clear out the specified field
	 *
	 * @param field to clear value
	 */
	public abstract void clear(AutomatedObject field);

	/**
	 * Will close the browser instance that matches the provided search
	 *
	 * @param search to find browser
	 */
	public abstract void closeBrowser(Search search);

	/**
	 * Will click the Cancel button on an Alert pop up
	 */
	public abstract void clickAlertCancelButton();

	/**
	 * Will click the Accept button on an Alert pop up
	 */
	public abstract void clickAlertAcceptButton();

	/**
	 * Will click the link with the matching text. <br>
	 * <br>
	 * <i>Please keep in mind that if you are working with an application that has
	 * multiple windows at the same time or iFrames, this method might not work as
	 * it does not designate which window or frame to search in.</i>
	 *
	 * @param text   to click
	 * @param search default Search criteria
	 */
	public abstract void clickLink(String text, Search search);

	/**
	 * Will use a javascript .click workaround when the traditional click with the
	 * automation tool is not working. This has been useful in instances when the
	 * Automation Tool complains that an object is not clickable when it is clearly
	 * visible and enabled.
	 *
	 * @param defaultSearch Search contains defaults to be used if Search is needed
	 * @param object        to click
	 */
	public abstract void clickWithJavascript(Search defaultSearch, AutomatedObject object);

	/**
	 * Will create a Criteria Object using a regular expression
	 *
	 * @param type               of criteria
	 * @param criteriaRegexValue regex value to match
	 * @param regexSetting       specifies the type of regular expression (ex,
	 *                           CONTAINS, STARTS_WITH, MATCHES)
	 * @return CriteriaObject that represents specified criteria
	 */
	abstract CriteriaObject createCriteriaRegex(String type, String criteriaRegexValue, REGEX regexSetting);

	/**
	 * Creates a criteria object for the type and value
	 *
	 * @param type          of criteria
	 * @param criteriaValue for criteria
	 * @return CriteriaObject representing given information
	 */
	abstract CriteriaObject createCriteria(String type, Object criteriaValue);

	/**
	 * Creates a criteria object for the type and value
	 *
	 * @param type               of criteria
	 * @param criteriaRegexValue for criteria
	 * @return CriteriaObject representing given information
	 */
	abstract CriteriaObject createCriteriaRegex(String type, String criteriaRegexValue);

	// TODO - Do we need this? Marking deprecated until it is determined
	/**
	 * Will create a single CriteriaObject to represent an array of CriteriaObjects
	 *
	 * @param multipleCriteria array of CriteriaObjects
	 * @return CriteriaObject to represent all the criteria in the array
	 */
	@Deprecated
	abstract CriteriaObject createCriteria(CriteriaObject[] multipleCriteria);

	/**
	 * Will drag and drop the object to move to the position determined by the drop
	 * location object (will move the one object on top of the other)
	 *
	 * @param objectToMove          item to drag
	 * @param objectForDropLocation item to give location for the drop
	 */
	public abstract void dragAndDrop(AutomatedObject objectToMove, AutomatedObject objectForDropLocation);

	/**
	 * Will enter the security credentials in the sign in pop up
	 *
	 * @param userName for login
	 * @param password for login
	 */
	public abstract void enterSecurityCredentialsInPopup(String userName, String password);

	/**
	 * Will return the innermost frame for this set of Frame Ids (the frame that
	 * would serve as a parent for any searches done within these frames
	 *
	 * @param getiFrameIds set of frame ids
	 * @return AutomatedObject of parent frame
	 */
	public abstract AutomatedObject getParentFrame(String... getiFrameIds);

	/**
	 * Will find and return the Object matching the specified search
	 *
	 * @param config for search
	 * @return AutomatedObject matching search criteria
	 */
	public abstract AutomatedObject getAutomatedObject(Search config);

	/**
	 * Will find and return the Object matching the specified search
	 *
	 * @param search criteria
	 * @return ArrayList set of matching AutomatedObjects
	 */
	public abstract ArrayList<AutomatedObject> getAutomatedObjects(Search search);

	/**
	 * Will get/load the specified url and return the resulting AutomatedPage
	 *
	 * @param url for the window
	 * @return AutomatedPage instance for resulting page/window
	 */
	public abstract AutomatedPage getAutomatedPage(String url);

	/**
	 * Will return a BrowserObject matching the search criteria
	 *
	 * @param search criteria for browser
	 * @return BrowserObject instance
	 */
	public abstract BrowserObject getBrowser(Search search);

	/**
	 * Will return a BrowserObject for the matching url
	 *
	 * @param url for browser
	 * @return BrowserObject instance
	 */
	public abstract BrowserObject getBrowser(String url);

	/**
	 * Method will return the default parent for the current testing platform
	 *
	 * @return AutomatedObject representing default parent
	 */
	public abstract AutomatedObject getDefaultParent();

	/**
	 * Will return a Drop Down instance matching the search
	 *
	 * @param dropDownSearch criteria
	 * @return DropDownInterface instance
	 */
	public abstract DropDownInterface getDropDown(Search dropDownSearch);

	/**
	 * Will return a new instance of TestScriptManager for the provided test script
	 *
	 * @param testScript to manage
	 * @return TestScriptManager for testscript
	 */
	abstract TestScriptManager getNewTestScriptManager(TestScriptInterface testScript);

	/**
	 * Will return a TableObject instance representing the specified AutomatedObject
	 * and primary columns
	 *
	 * @param object table instance
	 * @param cols   primary columns used to identify rows
	 * @return TableObject instance
	 */
	public abstract TableObject getTableObject(AutomatedObject object, String... cols);

	/**
	 * Will return the Automation Tool type
	 *
	 * @return String automation tool type (example SELENIUM, RFT, etc)
	 */
	public String getType() {
		return platform;
	}

	/**
	 * Verifies if an Alert is currently present
	 *
	 * @return boolean TRUE if Alert is present, FALSE if not
	 */
	public abstract boolean isAlertPresent();

	/**
	 * Will load the url
	 *
	 * @param url to load
	 */
	public abstract void loadPage(String url);

	// NOTE: These methods are marked as deprecated as we do not want them left in
	// active code, the deprecation will trigger a warning to appear in the class if
	// left active
	/**
	 * Will output debug info, not for use in production code. It will output all of
	 * the properties for the specified object, and if specified, its children and
	 * parent
	 *
	 * @param object        to print properties for
	 * @param printChildren if TRUE, will also print properties for children objects
	 * @param printParent   if TRUE will also print properties for the parent object
	 */
	@Deprecated
	public abstract void printObjectProperties(AutomatedObject object, boolean printChildren, boolean printParent);

	/**
	 * Will output debug info, not for use in production code. It will output all of
	 * the properties for each of the objects that match the given search criteria
	 *
	 * @param search        criteria
	 * @param printChildren if TRUE, will also print properties for children objects
	 */
	@Deprecated
	public abstract void printObjectsForSearch(Search search, boolean printChildren);

	/**
	 * Will read the text off of an Alert
	 *
	 * @return String text from the Alert message
	 */
	public abstract String readAlertMessage();

	/**
	 * Will read the current page title
	 *
	 * @return String page title
	 */
	public abstract String readPageTitle();

	/**
	 * Will run the specified script with the provided arguments
	 *
	 * @param currentScript to run
	 * @param args          to pass into the script
	 * @return ScriptResults returned from running the script
	 */
	public abstract ScriptResults runScript(TestScriptInterface currentScript, Arguments args);

	/**
	 * Will run the specified modular script
	 *
	 * @param scriptInstance to run
	 * @param args           to send to the modular script
	 * @return ScriptResults provided from modular script execution
	 */
	public ScriptResults runModularScript(TestScriptInterface scriptInstance, Arguments args) {
		TestScriptManager script = getNewTestScriptManager(scriptInstance);
		scriptInstance.setScript(script);
		return script.runModular(args);

	}

	/**
	 * Will run the execution script with the provided arguments
	 *
	 * @param scriptInstance to run as an execution script
	 * @param args           to pass into the script
	 * @return ScriptResults returned from the script
	 */
	public abstract ScriptResults runExecutionScript(TestScriptInterface scriptInstance, Arguments args);

	/**
	 * Will send keys to the field, simulates typing
	 *
	 * @param field object to send text to
	 * @param text  to send to the field object
	 */
	public abstract void sendKeys(AutomatedObject field, String text);

	/**
	 * Will select a file (using the Windows file select)
	 *
	 * @param file to select
	 */
	public abstract void selectFile(String file);

	/**
	 * Will slide the slider to desired value
	 *
	 * @param sliderObject object to set
	 * @param desiredValue the value to set it to
	 */
	public abstract void setSlider(AutomatedObject sliderObject, int desiredValue);

	/**
	 * Will take a screen shot of the current browser and save it to the file
	 * location specified Will also record the provided note with the screen shot
	 * event
	 *
	 * @param snapShotFilePath location to store snapshot
	 * @param snapShotNote     note to associate with the snapshot
	 * @return ScreenShot instance for resulting snapshot
	 */
	public abstract ScreenShot takeScreenShot(String snapShotFilePath, String snapShotNote);

	@Override
	public String toString() {
		return "TESTING PLATFORM -> " + platform;
	}

	/**
	 * Will execute a verification point verifying if the actual equals the expected
	 *
	 * @param expected value
	 * @param actual   value
	 */
	public abstract void vpEquals(String expected, String actual);

	/**
	 * Will execute a verification point verifying if the actual matches the
	 * expected regular expression
	 *
	 * @param expected value
	 * @param actual   value
	 * @param regex    type of regex
	 */
	public abstract void vpMatches(String expected, String actual, REGEX regex);

	/**
	 * Will execute a verification point verifying if the actual contains the
	 * expected
	 *
	 * @param expected value
	 * @param actual   value
	 */
	public abstract void vpContains(String expected, String actual);

	/**
	 * Wait for an object to appear matching the search criteria. This wait will end
	 * once there is a single object matching the criteria.
	 *
	 * @param search criteria
	 */
	public void waitForObject(Search search) {
		getAutomatedObject(search);
	}

	/**
	 * Will wait until there is not a visible object matching the search criteria
	 * Keep in mind if the object is not immediately visible, this could return
	 * before it is displayed
	 *
	 * @param search criteria
	 */
	public abstract void waitForNotVisible(Search search);

	/**
	 * Method to make sure page is fully loaded before moving on to the next command
	 */
	public abstract void waitForPageLoad();

	/**
	 * Will click the object provided and wait for a new window to appear before
	 * returning
	 *
	 * @param objectToClick the button or link launching the new window
	 * @param nativeClick   specifies if it should use a native .click() (if TRUE),
	 *                      or send an {ENTER} command to the object (if FALSE)
	 */
	public abstract void waitForNewWindowAfterClick(AutomatedObject objectToClick, boolean nativeClick);

	/**
	 * Will click the INPUT object. This is useful for File Select buttons that are
	 * not clickable with normal Automation. Since the "button" is not actually a
	 * button, but an INPUT, the normal .click() will fail in some browsers. This
	 * method serves as an alternate way to get Automation to click that object
	 *
	 * @param search      default Search
	 * @param inputObject object to click
	 */
	public abstract void clickInputObject(Search search, AutomatedObject inputObject);

}
