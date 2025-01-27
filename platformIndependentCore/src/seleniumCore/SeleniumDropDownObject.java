package seleniumCore;

import platformIndependentCore.core.DropDownInterface;
import platformIndependentCore.exceptions.MissingAutomationToolLibrariesException;

public abstract class SeleniumDropDownObject extends SeleniumObject implements DropDownInterface {

	@Override
	public void select(String subitemTextToSelect) {
		throw new MissingAutomationToolLibrariesException("SELENIUM");
	}

	@Override
	public String readSelected() {
		return (String) readValue();
	}

	@Override
	public boolean isEnabled() {
		return isEnabled();
	}
}
