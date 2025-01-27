package seleniumCore;

import platformIndependentCore.core.CriteriaObject;
import platformIndependentCore.exceptions.MissingAutomationToolLibrariesException;

public class SeleniumCriteriaObject extends CriteriaObject {

	public SeleniumCriteriaObject(String criteriaType, Object value) {
		throw new MissingAutomationToolLibrariesException("SELENIUM");
	}

	public SeleniumCriteriaObject(CriteriaObject[] multipleCriteria) {
		throw new MissingAutomationToolLibrariesException("SELENIUM");
	}

	public SeleniumCriteriaObject(String type, String criteriaRegexValue, REGEX regexSetting) {
		throw new MissingAutomationToolLibrariesException("SELENIUM");
	}

}
