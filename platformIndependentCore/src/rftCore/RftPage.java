package rftCore;

import platformIndependentCore.core.AutomatedPage;
import platformIndependentCore.exceptions.MissingAutomationToolLibrariesException;

public class RftPage extends AutomatedPage {
	private String homeURL;

	/**
	 * @param homeURL url
	 */
	public RftPage(String homeURL) {
		this.homeURL = homeURL;
		throw new MissingAutomationToolLibrariesException("RFT");

	}

	/**
	 * @return url
	 */
	public String getHomeURL() {
		return homeURL;
	}

	@Override
	public void loadPage() {
		throw new MissingAutomationToolLibrariesException("RFT");
	}

}
