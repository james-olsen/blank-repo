package rftCore;

import platformIndependentCore.core.AutomatedObject;
import platformIndependentCore.core.BrowserObject;
import platformIndependentCore.exceptions.MissingAutomationToolLibrariesException;

public class RftBrowser extends RftObject implements BrowserObject {

	@Override
	public void maximize() {
		throw new MissingAutomationToolLibrariesException("RFT");
	}

	@Override
	public void loadUrl(String url) {
		throw new MissingAutomationToolLibrariesException("RFT");
	}

	@Override
	public AutomatedObject getAutomatedObject() {
		throw new MissingAutomationToolLibrariesException("RFT");
	}

	@Override
	public String getCurrentUrl() {
		throw new MissingAutomationToolLibrariesException("RFT");
	}

	@Override
	public void close() {
		throw new MissingAutomationToolLibrariesException("RFT");
	}

	@Override
	public void refresh() {
		throw new MissingAutomationToolLibrariesException("RFT");

	}

}
