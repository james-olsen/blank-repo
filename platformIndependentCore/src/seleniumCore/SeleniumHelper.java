package seleniumCore;

import java.util.ArrayList;

import platformIndependentCore.core.AutomatedObject;
import platformIndependentCore.core.BrowserObject;
import platformIndependentCore.core.CriteriaObject;
import platformIndependentCore.core.CriteriaObject.REGEX;
import platformIndependentCore.core.DropDownInterface;
import platformIndependentCore.core.Search;
import platformIndependentCore.events.ScreenShot;
import platformIndependentCore.exceptions.MissingAutomationToolLibrariesException;
import platformIndependentCore.tables.TableObject;

/**
 * Skeleton implementation for SeleniumHelper. Actual functionality will be
 * provided in seleniumCore.
 *
 * Javadoc comments have been suppressed here. Please find actual comments in
 * the full implementation in SeleniumCore.
 *
 * @author VBAAUSTAYLOL
 *
 */
@SuppressWarnings("javadoc")
public class SeleniumHelper {
	public static AutomatedObject getObject(AutomatedObject parent, CriteriaObject criteria) {
		throw new MissingAutomationToolLibrariesException("SELENIUM");
	}

	public static void clickLink(String linkText, Search search) {
		throw new MissingAutomationToolLibrariesException("SELENIUM");
	}

	public static void clickWithJavascript(Search defaultSearch, AutomatedObject object) {
		throw new MissingAutomationToolLibrariesException("SELENIUM");
	}

	public static DropDownInterface getDropDownObject(AutomatedObject parent, CriteriaObject criteria) {
		throw new MissingAutomationToolLibrariesException("SELENIUM");
	}

	public static DropDownInterface getDropDownObject(Search search) {
		throw new MissingAutomationToolLibrariesException("SELENIUM");
	}

	public static AutomatedObject getObjectWithSelenium(Search config) {
		throw new MissingAutomationToolLibrariesException("SELENIUM");
	}

	public static ArrayList<AutomatedObject> getObjectsWithSelenium(Search config) {
		throw new MissingAutomationToolLibrariesException("SELENIUM");
	}

	public static AutomatedObject getDefaultParent() {
		throw new MissingAutomationToolLibrariesException("SELENIUM");
	}

	public static BrowserObject getBrowser(String pageUrl) {
		throw new MissingAutomationToolLibrariesException("SELENIUM");
	}

	public static TableObject getTable(AutomatedObject object, String[] cols) {
		throw new MissingAutomationToolLibrariesException("SELENIUM");
	}

	public static void vpEquals(String expected, String actual) {
		throw new MissingAutomationToolLibrariesException("SELENIUM");
	}

	public static void selectFile(String file) {
		throw new MissingAutomationToolLibrariesException("SELENIUM");
	}

	public static void vpContains(String expected, String actual) {
		throw new MissingAutomationToolLibrariesException("SELENIUM");

	}

	public static void waitForPageLoad() {
		throw new MissingAutomationToolLibrariesException("SELENIUM");

	}

	public static void sendKeys(SeleniumObject field, String text) {
		throw new MissingAutomationToolLibrariesException("SELENIUM");

	}

	public static void clear(SeleniumObject field) {
		throw new MissingAutomationToolLibrariesException("SELENIUM");

	}

	public static void clickAlertAcceptButton() {
		throw new MissingAutomationToolLibrariesException("SELENIUM");

	}

	public static void clickAlertCancelButton() {
		throw new MissingAutomationToolLibrariesException("SELENIUM");

	}

	public static String readAlertMessage() {
		throw new MissingAutomationToolLibrariesException("SELENIUM");

	}

	public static void waitForObject(Search search) {
		throw new MissingAutomationToolLibrariesException("SELENIUM");

	}

	public static void vpMatches(String expected, String actual, REGEX regex) {
		throw new MissingAutomationToolLibrariesException("SELENIUM");

	}

	public static void dragAndDrop(SeleniumObject objectToMove, SeleniumObject objectForDropLocation) {
		throw new MissingAutomationToolLibrariesException("SELENIUM");
	}
	//
	// public static void clickLink(AutomatedObject parent, String linkText) {
	// throw new MissingPlatformLibrariesException("SELENIUM");
	//
	// }

	public static void enterSecurityCredentialsInPopup(String userName, String password) {
		throw new MissingAutomationToolLibrariesException("SELENIUM");
	}

	public static AutomatedObject getParentFrame(String[] frameIds) {
		throw new MissingAutomationToolLibrariesException("SELENIUM");
	}

	public static ScreenShot takeSnapShot(String screenShotPath, String screenShotNote) {
		throw new MissingAutomationToolLibrariesException("SELENIUM");

	}

	public static void waitForNewWindowAfterClick(AutomatedObject objectToClick, boolean nativeClick) {
		throw new MissingAutomationToolLibrariesException("SELENIUM");

	}

	public static boolean isAlertPresent() {
		throw new MissingAutomationToolLibrariesException("SELENIUM");
	}

	public static String readPageTitle() {
		throw new MissingAutomationToolLibrariesException("SELENIUM");
	}

	public static void waitForNotVisible(Search search) {
		throw new MissingAutomationToolLibrariesException("SELENIUM");
	}

	public static BrowserObject getBrowser(Search search) {
		throw new MissingAutomationToolLibrariesException("SELENIUM");
	}

	public static void closeBrowser(Search search) {

	}

	public static boolean isBrowserPresent(String urlContains) {
		throw new MissingAutomationToolLibrariesException("SELENIUM");
	}

	public static boolean isBrowserPresent(String urlContains, String actionUrlContains) {
		throw new MissingAutomationToolLibrariesException("SELENIUM");
	}

	/**
	 * Will slide the slider to desired value
	 *
	 * @param sliderObject object to set
	 * @param desiredValue the value to set it to
	 */
	public static void setSlider(SeleniumObject sliderObject, int desiredValue) {
		throw new MissingAutomationToolLibrariesException("SELENIUM");
	}

	/**
	 * Will use Selenium Action to move to the specified object and click
	 *
	 * @param search        default search to locate driver
	 * @param objectToClick the object to click
	 */
	public static void clickWithAction(Search search, AutomatedObject objectToClick) {
		throw new MissingAutomationToolLibrariesException("SELENIUM");
	}
}
