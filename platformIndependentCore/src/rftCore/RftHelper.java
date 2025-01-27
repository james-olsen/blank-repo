package rftCore;

import platformIndependentCore.core.AutomatedObject;
import platformIndependentCore.core.BrowserObject;
import platformIndependentCore.core.CriteriaObject;
import platformIndependentCore.core.DropDownInterface;
import platformIndependentCore.core.Search;
import platformIndependentCore.exceptions.MissingAutomationToolLibrariesException;

public class RftHelper {

	public static AutomatedObject getObject(AutomatedObject parent, CriteriaObject criteria) {
		throw new MissingAutomationToolLibrariesException("RFT");
	}

	public static void clickLink(String text) {
		throw new MissingAutomationToolLibrariesException("RFT");
	}

	public static DropDownInterface getDropDownObject(AutomatedObject parent, CriteriaObject criteria) {
		throw new MissingAutomationToolLibrariesException("RFT");
	}

	public static AutomatedObject searchForObject(Search config) {
		throw new MissingAutomationToolLibrariesException("RFT");
	}

	public static DropDownInterface getDropDownObject(Search dropDownSearch) {
		throw new MissingAutomationToolLibrariesException("RFT");
	}

	public static AutomatedObject getDefaultParent() {
		throw new MissingAutomationToolLibrariesException("RFT");
	}

	public static BrowserObject getBrowser(String pageUrl) {
		throw new MissingAutomationToolLibrariesException("RFT");
	}

	public static void loadPage(String url) {
		throw new MissingAutomationToolLibrariesException("RFT");
	}

}
