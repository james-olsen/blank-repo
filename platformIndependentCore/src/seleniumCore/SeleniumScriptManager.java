package seleniumCore;

import java.util.ArrayList;

import platformIndependentCore.datafiles.ExcelDataFile;
import platformIndependentCore.events.VerificationPoint;
import platformIndependentCore.exceptions.MissingAutomationToolLibrariesException;
import platformIndependentCore.scripts.TestScriptInterface;
import platformIndependentCore.scripts.TestScriptManager;

public class SeleniumScriptManager extends TestScriptManager {

	ArrayList<VerificationPoint> vpList = new ArrayList<VerificationPoint>();

	String exception = "";

	// @Override
	// protected void testScript(String[] args) {
	// log.debug("running in SeleniumScript instance");
	// }

	public SeleniumScriptManager(TestScriptInterface scriptInstance) {
		super(scriptInstance);
	}

	@Override
	protected void setup() {
		throw new MissingAutomationToolLibrariesException("SELENIUM");

	}

	// @Override
	// protected void assertEquals(String expected, String actual) {
	// throw new MissingPlatformLibrariesException("SELENIUM");
	//
	// }

	@Override
	protected void gotoURL(String url) {
		throw new MissingAutomationToolLibrariesException("SELENIUM");

	}

	@Override
	public void setBrowserProperties() {
		throw new MissingAutomationToolLibrariesException("SELENIUM");

	}

	@Override
	public void setTimeout(int timeOutInSeconds) {
		throw new MissingAutomationToolLibrariesException("SELENIUM");

	}

	@Override
	public void loadPage(String homeURL) {
		throw new MissingAutomationToolLibrariesException("SELENIUM");

	}

	@Override
	protected void runSuite(ExcelDataFile testSuiteFile) {
		throw new MissingAutomationToolLibrariesException("SELENIUM");

	}

	@Override
	protected void logWarning(String warningMessage) {
		throw new MissingAutomationToolLibrariesException("SELENIUM");
	}

	@Override
	protected void tearDown() {
		throw new MissingAutomationToolLibrariesException("SELENIUM");
	}

}
