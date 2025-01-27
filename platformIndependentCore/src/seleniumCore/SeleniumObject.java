package seleniumCore;

import platformIndependentCore.core.AutomatedObject;
import platformIndependentCore.exceptions.MissingAutomationToolLibrariesException;

public class SeleniumObject extends AutomatedObject {

	@Override
	public Object readValue() {
		throw new MissingAutomationToolLibrariesException("SELENIUM");

	}

	@Override
	public void setValue(Object value) {
		throw new MissingAutomationToolLibrariesException("SELENIUM");

	}

	@Override
	public void click() {
		throw new MissingAutomationToolLibrariesException("SELENIUM");

	}

	@Override
	public AutomatedObject[] getChildren() {
		throw new MissingAutomationToolLibrariesException("SELENIUM");
	}

	@Override
	public boolean isSelected() {
		throw new MissingAutomationToolLibrariesException("SELENIUM");
	}

	@Override
	public AutomatedObject getParent() {
		throw new MissingAutomationToolLibrariesException("SELENIUM");
	}

	@Override
	public String getPropertyValue(String property) {
		throw new MissingAutomationToolLibrariesException("SELENIUM");
	}

	@Override
	public void click(boolean nativeClick) {
		throw new MissingAutomationToolLibrariesException("SELENIUM");
	}

	@Override
	public void sendKeys(String text) {
		throw new MissingAutomationToolLibrariesException("SELENIUM");
	}

	@Override
	public void clear() {
		throw new MissingAutomationToolLibrariesException("SELENIUM");
	}

	@Override
	public void moveMouseTo() {
		throw new MissingAutomationToolLibrariesException("SELENIUM");
	}

	@Override
	public boolean isDisplayed() {
		throw new MissingAutomationToolLibrariesException("SELENIUM");
	}

	@Override
	public void doubleClick() {
		throw new MissingAutomationToolLibrariesException("SELENIUM");
	}

	@Override
	public void hover() {
		throw new MissingAutomationToolLibrariesException("SELENIUM");
	}

}
