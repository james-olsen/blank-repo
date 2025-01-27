package platformIndependentCore.windows;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.remote.DesiredCapabilities;

import io.appium.java_client.windows.WindowsDriver;
import platformIndependentCore.core.AutomatedPage;
import platformIndependentCore.utilities.ConfigProperties;

/**
 * <b>Name :</b> AutomatedWindowsAppPage.java
 * <p>
 * <b>Generated :</b> Dec 21, 2021
 * <p>
 * <b>Description :</b> Implementation to interact with the objects on a windows
 * application
 * <p>
 *
 * @since Dec 21, 2021
 * @author VBAAUSMakinS
 */
public abstract class AutomatedWindowsApp {
	/** logger instance for this class */
	private static Logger log = LogManager.getLogger(AutomatedPage.class.getName());
	/** Windows Application */
	private String Windows_App;
	/** Platform */
	private String Platform = "Windows";
	/** Device */
	private String Device = "WindowsPC";
	/** WindowsServer_URL */
	private String WindowsServer_URL;
	/** appium windows driver */
	private static WindowsDriver windowSession = null;
	/** Microsoft WinAppDriver.exe process */
	private static Process process = null;

	/**
	 * Will load the Windows application and Launching Win App server
	 *
	 * @return boolean
	 */
	public boolean loadWindowsApp() {
		launchApplicationServer();
		Windows_App = ConfigProperties.getValue("Windows_App");
		WindowsServer_URL = ConfigProperties.getValue("WindowsServer_URL");
		log.info("LOADING APPLICATION: " + Windows_App);
		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities.setCapability("app", Windows_App);
		capabilities.setCapability("platformName", Platform);
		capabilities.setCapability("deviceName", Device);
		try {
			windowSession = new WindowsDriver(new URL(WindowsServer_URL), capabilities);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * will launch the given application server
	 */
	public void launchApplicationServer() {
		if (process == null) {
			String Driver_Loc = ConfigProperties.getValue("Windows_App_Driver_Loc");
			String Driver_Port = ConfigProperties.getValue("AppDriver_Port");
			String[] command = new String[] { Driver_Loc, Driver_Port };
			ProcessBuilder builder = new ProcessBuilder(command).inheritIO();
			try {
				process = builder.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Close the app which was provided in the capabilities at session creation and
	 * quits the session.
	 */
	public void closeWindowsApp() {
		windowSession.close();
		windowSession.quit();
		closeWindowsApplication();
	}

	/**
	 * Will terminate the given Windows application server
	 */
	public void closeWindowsApplication() {
//		if (process != null) {
//			process.destroy();
//		}
		String terminate = ConfigProperties.getValue("CLOSE_ON_TERMINATE");
		if (terminate.equalsIgnoreCase("TRUE")) {
			String Win_App_Exe_FileName = ConfigProperties.getValue("Windows_App_Exe_Filename");
			// process.destroy();
			String[] appcommand = new String[] { "taskkill", "/F", "/IM", Win_App_Exe_FileName };
			ProcessBuilder appbuilder = new ProcessBuilder(appcommand).inheritIO();
			try {
				appbuilder.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Method to make sure element is fully invisible (that means window completly
	 * closes where the element presents) before moving on to the next command <br>
	 *
	 * @param attValue to set the value of attribute
	 */
	public static void waitForElementInvisibleByAccessibilityId(String attValue) {
		for (int i = 0; i < 60; i++) {
			try {
				Thread.sleep(1000);
				windowSession.findElementByAccessibilityId(attValue).isDisplayed();
			} catch (Exception e) {
				System.out.println("waiting for Invisible Accessibility ID by the given time  " + i);
				break;
			}
		}

	}

	/**
	 * Method to make sure element is fully invisible (that means window completly
	 * closes where the element presents) before moving on to the next command <br>
	 *
	 * @param attValue to set the value of attribute
	 */
	public static void waitForElementInvisibleByClassName(String attValue) {
		for (int i = 0; i < 60; i++) {
			try {
				Thread.sleep(1000);
				windowSession.findElementByClassName(attValue).isDisplayed();
			} catch (Exception e) {
				System.out.println("waiting for Invisible Class Name by the given time  " + i);
				break;
			}
		}

	}

	/**
	 * Method to make sure element is fully invisible (that means window completly
	 * closes where the element presents) before moving on to the next command <br>
	 *
	 * @param attValue to set the value of attribute
	 */
	public static void waitForElementInvisibleByName(String attValue) {
		for (int i = 0; i < 60; i++) {
			try {
				Thread.sleep(1000);
				windowSession.findElementByName(attValue).isDisplayed();
			} catch (Exception e) {
				System.out.println("waiting for Invisible Name by the given time  " + i);
				break;
			}
		}

	}

	/**
	 * Method to make sure element is fully invisible (that means window completly
	 * closes where the element presents) before moving on to the next command <br>
	 *
	 * @param attValue to set the value of attribute
	 */
	public static void waitForElementInvisibleByXPath(String attValue) {
		for (int i = 0; i < 60; i++) {
			try {
				Thread.sleep(1000);
				windowSession.findElementByXPath(attValue).isDisplayed();
			} catch (Exception e) {
				System.out.println("waiting for Invisible XPath by the given time  " + i);
				break;
			}
		}

	}

	/**
	 * Method to make sure element is fully loaded before moving on to the next
	 * command when Multiple windows present <br>
	 */
	public static void waitForLoadMultipleWindows() {

		for (int i = 0; i < 60; i++) {
			try {
				Thread.sleep(1000);
				if (windowSession.getWindowHandles().size() > 1) {
					break;
				}
			} catch (Exception e) {
				System.out.println("waiting for next window by the given time" + i);
			}
		}

	}

	/**
	 * Will click the matching object that matches the passed AccessibilityId when
	 * single window presents
	 *
	 * @param attValue to locate descendant object
	 */
	public void clickWindowObjectByAccessibilityId(String attValue) {
		windowSession.findElementByAccessibilityId(attValue).click();
	}

	/**
	 * Will click the matching object that matches the passed Class Name when single
	 * window presents
	 *
	 * @param attValue to locate descendant object
	 */
	public void clickWindowObjectByClassName(String attValue) {
		windowSession.findElementByClassName(attValue).click();
	}

	/**
	 * Will click the matching object that matches the passed XPath when single
	 * window presents
	 *
	 * @param attValue to locate descendant object
	 */
	public void clickWindowObjectByXPath(String attValue) {
		windowSession.findElementByXPath(attValue).click();
	}

	/**
	 * Will click the matching object that matches the passed Name when single
	 * window presents
	 *
	 * @param attValue to locate descendant object
	 */
	public void clickWindowObjectByName(String attValue) {
		windowSession.findElementByName(attValue).click();
	}

	/**
	 * Returns the text value of a Windows Object that is found by passing attribute
	 * type Accessibility Id when single window presents
	 *
	 * @param attValue to locate descendant object
	 * @return String value
	 */
	public String readWindowObjectByAccessibilityId(String attValue) {
		return windowSession.findElementByAccessibilityId(attValue).getText();
	}

	/**
	 * Returns the text value of a Windows Object that is found by passing attribute
	 * type Class Name when single window presents
	 *
	 * @param attValue to locate descendant object
	 * @return String value
	 */
	public String readWindowObjectByClassName(String attValue) {
		return windowSession.findElementByClassName(attValue).getText();
	}

	/**
	 * Returns the text value of a Windows Object that is found by passing attribute
	 * type XPath when single window presents
	 *
	 * @param attValue to locate descendant object
	 * @return String value
	 */
	public String readWindowObjectByXPath(String attValue) {
		return windowSession.findElementByXPath(attValue).getText();
	}

	/**
	 * Returns the text value of a Windows Object that is found by passing attribute
	 * type Name when single window presents
	 *
	 * @param attValue to locate descendant object
	 * @return String
	 */
	public String readWindowObjectByName(String attValue) {
		return windowSession.findElementByName(attValue).getText();
	}

	/**
	 * Sets the passed in value for the object that matches the attribute type
	 * AccessibilityId when single window presents
	 *
	 * @param setValue to set
	 * @param id       to locate descendant object
	 */
	public void setWindowObjectByAccessibilityId(String setValue, String id) {
		windowSession.switchTo().activeElement().click();
		windowSession.findElementByAccessibilityId(id).sendKeys(setValue);
	}

	/**
	 * Sets the passed in value for the object that matches the attribute type XPath
	 * when single window presents
	 *
	 * @param setValue to set
	 * @param xpath    to locate descendant object
	 */
	public void setWindowObjectByXPath(String setValue, String xpath) {
		windowSession.switchTo().activeElement().click();
		windowSession.findElementByXPath(xpath).sendKeys(setValue);
	}

	/**
	 * Sets the passed in value for the object that matches the attribute type Class
	 * Name when single window presents
	 *
	 * @param setValue  to set
	 * @param className to locate descendant object
	 */
	public void setWindowObjectByClassName(String setValue, String className) {
		windowSession.switchTo().activeElement().click();
		windowSession.findElementByClassName(className).sendKeys(setValue);
	}

	/**
	 * Sets the passed in value for the object that matches the attribute type Name
	 * when single window presents
	 *
	 * @param setValue to set
	 * @param attValue to locate
	 */
	public void setWindowObjectByName(String setValue, String attValue) {
		windowSession.switchTo().activeElement().click();
		windowSession.findElementByName(attValue).sendKeys(setValue);
	}

	/**
	 * Sets the passed in value for the object that matches the attribute type XPath
	 * when multiple windows presents
	 *
	 * @param setValue to set
	 * @param attValue to set the value of attribute
	 */
	public void setMultipleWindowsObjectByXPath(String setValue, String attValue) {
		// waitForLoadMultipleWindows();
		String parent = windowSession.getWindowHandle();
		System.out.println(windowSession.getWindowHandles().size());

		Set<String> s = windowSession.getWindowHandles();
		Iterator<String> l1 = s.iterator();

		while (l1.hasNext()) {
			String child_window = l1.next();
			if (!parent.equals(child_window)) {
				windowSession.switchTo().window(child_window);
				System.out.println(windowSession.switchTo().window(child_window).getTitle());
			}
		}

		windowSession.switchTo().activeElement().click();
		windowSession.findElementByXPath(attValue).sendKeys(setValue);
		windowSession.switchTo().window(parent);
		parent = windowSession.getWindowHandle();
	}

	/**
	 * Sets the passed in value for the object that matches the attribute type
	 * AccessibilityId when multiple windows presents
	 *
	 * @param setValue to Set
	 * @param attValue to set attribute value
	 */
	public void setMultipleWindowsObjectByAccessibilityId(String setValue, String attValue) {
		String parent = windowSession.getWindowHandle();
		System.out.println(windowSession.getWindowHandles().size());

		Set<String> s = windowSession.getWindowHandles();
		Iterator<String> l1 = s.iterator();

		while (l1.hasNext()) {
			String child_window = l1.next();
			if (!parent.equals(child_window)) {
				windowSession.switchTo().window(child_window);
				System.out.println(windowSession.switchTo().window(child_window).getTitle());
			}
		}

		windowSession.switchTo().activeElement().click();
		windowSession.findElementByAccessibilityId(attValue).sendKeys(setValue);
	}

	/**
	 * Sets the passed in value for the object that matches the attribute type Class
	 * Name when multiple windows presents
	 *
	 * @param setValue to set the value
	 * @param attValue to set attribute value
	 */
	public void setMultipleWindowsObjectByClassName(String setValue, String attValue) {
		waitForLoadMultipleWindows();
		String parent = windowSession.getWindowHandle();
		System.out.println(windowSession.getWindowHandles().size());
		Set<String> s = windowSession.getWindowHandles();
		Iterator<String> l1 = s.iterator();
		while (l1.hasNext()) {
			String child_window = l1.next();
			if (!parent.equals(child_window)) {
				windowSession.switchTo().window(child_window);
				try {
					windowSession.findElementByClassName(attValue).sendKeys(setValue);
					break;
				} catch (Exception e) {

				}
			}
		}

		parent = windowSession.getWindowHandle();
	}

	/**
	 * Sets the passed in value for the object that matches the attribute type Name
	 * when multiple windows presents
	 *
	 * @param setValue to set the value
	 * @param attValue to set attribute value
	 */
	public void setMultipleWindowsObjectByName(String setValue, String attValue) {
		String parent = windowSession.getWindowHandle();
		System.out.println(windowSession.getWindowHandles().size());

		Set<String> s = windowSession.getWindowHandles();
		Iterator<String> l1 = s.iterator();

		while (l1.hasNext()) {
			String child_window = l1.next();
			if (!parent.equals(child_window)) {
				windowSession.switchTo().window(child_window);
				try {
					windowSession.findElementByName(attValue).sendKeys(setValue);
					break;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		parent = windowSession.getWindowHandle();
	}

	/**
	 * Will return the matching Accessibility Id that is a descendant of this
	 * windowsObject
	 *
	 * @param id    to locate Accessibility ID of object
	 * @param value to be set
	 */
	public void WindowObjectByAccessibilityId(String id, String value) {
		windowSession.findElementByAccessibilityId(id).sendKeys(value);
	}

	/**
	 * Will return the matching XPath that is a descendant of this windowsObject
	 *
	 * @param xpath to locate descendant object
	 * @param value to be set
	 */
	public void WindowObjectByXpath(String xpath, String value) {
		windowSession.findElementByXPath(xpath).sendKeys(value);
	}

	/**
	 * Will return the matching Name that is a descendant of this windowsObject
	 *
	 * @param name  to locate descendant object
	 * @param value to be set
	 */
	public void WindowObjectByName(String name, String value) {
		windowSession.findElementByName(name).sendKeys(value);
	}

	/**
	 * Will return the matching ClassName that is a descendant of this windowsObject
	 *
	 * @param className to locate descendant object
	 * @param value     to be set
	 */
	public void WindowObjectByClassName(String className, String value) {
		windowSession.findElementByClassName(className).sendKeys(value);
	}

	/**
	 * Clicks the object in multiple windows present.
	 */
	public void clickObjectInMultipleWindows() {
		String parent = windowSession.getWindowHandle();
		Set<String> s = windowSession.getWindowHandles();
		Iterator<String> l1 = s.iterator();
		while (l1.hasNext()) {
			String child_window = l1.next();
			if (!parent.equals(child_window)) {
				windowSession.switchTo().window(child_window);
			}
		}
	}

	/**
	 * Clicks the matching object that matches the passed AccessibilityId when
	 * multiple windows present.
	 *
	 * @param attValue to locate descendant object
	 */
	public void clickObjectInMultipleWindowsByAccessibilityId(String attValue) {
		String parent = windowSession.getWindowHandle();
		System.out.println(windowSession.getWindowHandles().size());
		Set<String> s = windowSession.getWindowHandles();
		Iterator<String> l1 = s.iterator();
		while (l1.hasNext()) {
			String child_window = l1.next();
			if (!parent.equals(child_window)) {
				windowSession.switchTo().window(child_window);
			}
			try {
				windowSession.findElementByAccessibilityId(attValue).click();
				break;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		windowSession.switchTo().window(parent);
		parent = windowSession.getWindowHandle();
	}

	/**
	 * Clicks the matching object that matches the passed XPath when multiple
	 * windows present
	 *
	 * @param attValue to locate descendant object
	 */
	public void clickObjectInMultipleWindowsByXPath(String attValue) {
		String parent = windowSession.getWindowHandle();
		Set<String> s = windowSession.getWindowHandles();
		Iterator<String> l1 = s.iterator();
		while (l1.hasNext()) {
			String child_window = l1.next();
			if (!parent.equals(child_window)) {
				windowSession.switchTo().window(child_window);
			}
			try {
				windowSession.findElementByXPath(attValue).click();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Clicks the matching object that matches the passed ClassName when multiple
	 * windows present
	 *
	 * @param attValue to locate descendant object
	 */
	public void clickObjectInMultipleWindowsByClassName(String attValue) {
		String parent = windowSession.getWindowHandle();
		Set<String> s = windowSession.getWindowHandles();
		Iterator<String> l1 = s.iterator();
		while (l1.hasNext()) {
			String child_window = l1.next();
			if (!parent.equals(child_window)) {
				windowSession.switchTo().window(child_window);
			}
			try {
				windowSession.findElementByClassName(attValue).click();

				break;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Clicks the matching object that matches the passed Name when multiple windows
	 * present
	 *
	 * @param attValue to locate descendant object
	 */
	public void clickObjectInMultipleWindowsByName(String attValue) {
		String parent = windowSession.getWindowHandle();
		Set<String> s = windowSession.getWindowHandles();
		Iterator<String> l1 = s.iterator();

		while (l1.hasNext()) {
			String child_window = l1.next();
			if (!parent.equals(child_window)) {
				windowSession.switchTo().window(child_window);
			}
			try {
				windowSession.findElementByName(attValue).click();
				break;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Returns the text value of a Windows Object that is found by passing the given
	 * attribute type when multiple windows present
	 *
	 * @param attributeValue to locate
	 * @param attributeType  to locate
	 * @return String
	 */
	public String readObjectInMultipleWindows(String attributeValue, String attributeType) {
		String parent = windowSession.getWindowHandle();
		String textValue = "";
		Set<String> s = windowSession.getWindowHandles();
		Iterator<String> l1 = s.iterator();
		while (l1.hasNext()) {
			String child_window = l1.next();
			if (!parent.equals(child_window)) {
				switch (attributeType) {
				case "AccessibilityID":
					windowSession.switchTo().window(child_window);
					textValue = readWindowObjectByAccessibilityId(attributeValue);
					break;
				case "ClassName":
					windowSession.switchTo().window(child_window);
					textValue = readWindowObjectByClassName(attributeValue);
					break;
				case "XPath":
					windowSession.switchTo().window(child_window);
					textValue = readWindowObjectByXPath(attributeValue);
					break;
				case "Name":
					windowSession.switchTo().window(child_window);
					textValue = readWindowObjectByName(attributeValue);
					break;
				}
			}
		}
		return textValue;
	}

	/**
	 * Returns the text value of a Windows Object that is found by passing attribute
	 * type Accessibility Id when multiple windows present
	 *
	 * @param attValue attributeValue to locate
	 * @return String Text value
	 */
	public String readMultipleWindowsObjectByAccessibilityID(String attValue) {
		String parent = windowSession.getWindowHandle();
		Set<String> s = windowSession.getWindowHandles();
		Iterator<String> l1 = s.iterator();
		String TextValue = "";
		while (l1.hasNext()) {
			String child_window = l1.next();
			if (!parent.equals(child_window)) {
				windowSession.switchTo().window(child_window);
			}
			try {
				TextValue = windowSession.findElementByAccessibilityId(attValue).getText();
				break;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return TextValue;
	}

	/**
	 * Returns the text value of a Windows Object that is found by passing attribute
	 * type ClassName when multiple windows present
	 *
	 * @param attValue attributeValue to locate
	 * @return String value
	 */
	public String readMultipleWindowsObjectByClassName(String attValue) {
		String parent = windowSession.getWindowHandle();
		Set<String> s = windowSession.getWindowHandles();
		Iterator<String> l1 = s.iterator();
		String TextValue = "";
		while (l1.hasNext()) {
			String child_window = l1.next();
			if (!parent.equals(child_window)) {
				windowSession.switchTo().window(child_window);
			}
			try {
				TextValue = windowSession.findElementByClassName(attValue).getText();
				break;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return TextValue;
	}

	/**
	 * Returns the text value of a Windows Object that is found by passing attribute
	 * type Name when multiple windows present
	 *
	 * @param attValue attributeValue to locate
	 * @return String value
	 */
	public String readMultipleWindowsObjectByName(String attValue) {
		String parent = windowSession.getWindowHandle();
		Set<String> s = windowSession.getWindowHandles();
		Iterator<String> l1 = s.iterator();
		String TextValue = "";
		while (l1.hasNext()) {
			String child_window = l1.next();
			if (!parent.equals(child_window)) {
				windowSession.switchTo().window(child_window);
			}
			try {
				TextValue = windowSession.findElementByName(attValue).getText();
				break;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return TextValue;
	}

	/**
	 * Returns the text value of a Windows Object that is found by passing attribute
	 * type XPath when multiple windows present
	 *
	 * @param attValue attributeValue to locate
	 * @return String value
	 */
	public String readMultipleWindowsObjectByXPath(String attValue) {
		String parent = windowSession.getWindowHandle();
		Set<String> s = windowSession.getWindowHandles();
		Iterator<String> l1 = s.iterator();
		String TextValue = "";
		while (l1.hasNext()) {
			String child_window = l1.next();
			if (!parent.equals(child_window)) {
				windowSession.switchTo().window(child_window);
			}
			try {
				TextValue = windowSession.findElementByXPath(attValue).getText();
				break;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return TextValue;
	}

}
