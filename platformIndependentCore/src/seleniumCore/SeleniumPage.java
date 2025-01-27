package seleniumCore;

import platformIndependentCore.core.AutomatedPage;
import platformIndependentCore.exceptions.MissingAutomationToolLibrariesException;

public class SeleniumPage extends AutomatedPage {
	private String homeURL;

	/**
	 * @param homeURL url
	 */
	public SeleniumPage(String homeURL) {
		this.homeURL = homeURL;
		throw new MissingAutomationToolLibrariesException("SELENIUM");

	}

	/**
	 * @return home url
	 */
	public String getHomeURL() {
		return homeURL;
	}

	@Override
	public void loadPage() {
		throw new MissingAutomationToolLibrariesException("SELENIUM");
	}

}
