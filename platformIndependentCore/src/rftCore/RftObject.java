package rftCore;

import platformIndependentCore.core.AutomatedObject;
import platformIndependentCore.exceptions.MissingAutomationToolLibrariesException;

public class RftObject extends AutomatedObject {

	@Override
	public Object readValue() {
		throw new MissingAutomationToolLibrariesException("RFT");
	}

	@Override
	public void setValue(Object value) {
		throw new MissingAutomationToolLibrariesException("RFT");
	}

	@Override
	public void click() {
		throw new MissingAutomationToolLibrariesException("RFT");
	}

	@Override
	public AutomatedObject[] getChildren() {
		throw new MissingAutomationToolLibrariesException("RFT");
	}

	@Override
	public boolean isSelected() {
		throw new MissingAutomationToolLibrariesException("RFT");
	}

	@Override
	public AutomatedObject getParent() {
		throw new MissingAutomationToolLibrariesException("RFT");
	}

	@Override
	public String getPropertyValue(String property) {
		throw new MissingAutomationToolLibrariesException("RFT");
	}

	@Override
	public void click(boolean nativeClick) {
		throw new MissingAutomationToolLibrariesException("RFT");
	}

	@Override
	public void sendKeys(String text) {
		throw new MissingAutomationToolLibrariesException("RFT");
	}

	@Override
	public void clear() {
		throw new MissingAutomationToolLibrariesException("RFT");
	}

	@Override
	public void moveMouseTo() {
		throw new MissingAutomationToolLibrariesException("RFT");
	}

	@Override
	public boolean isDisplayed() {
		throw new MissingAutomationToolLibrariesException("RFT");
	}

	@Override
	public void doubleClick() {
		throw new MissingAutomationToolLibrariesException("RFT");
	}

	@Override
	public void hover() {
		throw new MissingAutomationToolLibrariesException("RFT");
	}

}
