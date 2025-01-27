package rftCore;

import platformIndependentCore.core.DropDownInterface;
import platformIndependentCore.exceptions.MissingAutomationToolLibrariesException;

public abstract class RftDropDownObject extends RftObject implements DropDownInterface {

	@Override
	public void select(String subitemTextToSelect) {
		throw new MissingAutomationToolLibrariesException("RFT");
	}

	@Override
	public String readSelected() {
		throw new MissingAutomationToolLibrariesException("RFT");
	}
}
