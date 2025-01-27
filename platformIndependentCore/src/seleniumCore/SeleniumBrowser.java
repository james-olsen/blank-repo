package seleniumCore;

import platformIndependentCore.core.AutomatedObject;
import platformIndependentCore.core.BrowserObject;
import platformIndependentCore.exceptions.MissingAutomationToolLibrariesException;

public class SeleniumBrowser extends SeleniumObject implements BrowserObject {

	@Override
	public void maximize() {
		throw new MissingAutomationToolLibrariesException("SELENIUM");
	}

	@Override
	public void loadUrl(String url) {
		throw new MissingAutomationToolLibrariesException("SELENIUM");
	}

	@Override
	public AutomatedObject getAutomatedObject() {
		throw new MissingAutomationToolLibrariesException("SELENIUM");
	}

	@Override
	public String getCurrentUrl() {
		throw new MissingAutomationToolLibrariesException("SELENIUM");
	}

	@Override
	public void close() {
		throw new MissingAutomationToolLibrariesException("SELENIUM");
	}

	@Override
	public void refresh() {
		throw new MissingAutomationToolLibrariesException("SELENIUM");
	}

}
