package silkTestCore;

import platformIndependentCore.datafiles.DataFile;
import platformIndependentCore.events.Event.EVENT_TYPE;
import platformIndependentCore.events.VerificationPoint;
import platformIndependentCore.scripts.Arguments;
import platformIndependentCore.scripts.ScriptResults;
import platformIndependentCore.scripts.TestScriptInterface;
import platformIndependentCore.scripts.TestScriptManager;

public class SilkTestScript implements TestScriptInterface {

	TestScriptManager script = null;
	boolean isModularScript = true;

	ScriptResults results = new ScriptResults(EVENT_TYPE.EXECUTIONSCRIPT);

	// ScriptResults results = new ScriptResults(EVENT_TYPE.EXECUTIONSCRIPT);
	protected void setScriptName(String scriptName) {
		// super.setScriptName(scriptName);
	}

	@Override
	public String getStartTime() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getStopTime() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDuration() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setScript(TestScriptManager script) {
		this.script = script;
	}

	/**
	 * @return the isModularScript
	 */
	@Override
	public boolean isModularScript() {
		return isModularScript;
	}

	@Override
	public void setModularScript(boolean isModularScript) {
		this.isModularScript = isModularScript;
	}

	@Override
	public void setResults(ScriptResults results) {
		// this.results = results;
		log.debug("~~~ CODE ISSUE - SHOULD not be calling setResults on TestScriptInterface for RFT");
		log.debug("EXISTING RESULTS=" + this.results.toString() + ", NEW RESULTS=" + results.toString());
	}

	@Override
	public int getNumberOfCalledScripts() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void addNote(String note) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addNote(Exception exception) {
		// TODO Auto-generated method stub

	}

	// @Override
	// public void runModularScript(String scriptName, String... args) {
	// // TODO Auto-generated method stub
	//
	// }

	@Override
	public void runSuite(DataFile suiteFile) {
		// TODO Auto-generated method stub

	}

	// @Override
	// public ScriptResults callScript(String testScriptName, Arguments args) {
	// // TODO Auto-generated method stub
	// return null;
	// }

	@Override
	public void testScript(Arguments args) {
		// TODO Auto-generated method stub

	}

	/**
	 * @return the script
	 */
	@Override
	public TestScriptManager getManager() {
		if (script == null) {
			throw new RuntimeException(
					"Invalid state for Automated Test Script. Script instance has not been initialized");
		}
		return script;
	}

	/**
	 * Executes a verification point asserting the two strings are equal
	 *
	 * @param vpName   name of the verification point
	 * @param expected value
	 * @param actual   value
	 */
	@Override
	public void vpEquals(String vpName, String expected, String actual) {
		// //getManager().vpEquals(vpName, expected, actual);
		// IFtVerificationPoint rftVp = this.vpManual(vpName, expected, actual);
		// boolean pass = rftVp.compare();
		//
		//
		// }
		// protected boolean vpEquals(String vpName, String expected, String actual) {
		// boolean equal;
		boolean pass = false;
		VerificationPoint vp = new VerificationPoint(vpName, expected, actual);
		vp.setScriptName(this.getManager().getScriptName());
		vp.createTimestamp();
		String msg = "";
		// try {
		// getAutomationTool().vpEquals(expected, actual);
		// RationalTestScript rftScript = ((RationalTestScript)getTestScript());
		// IFtVerificationPoint rftVp = getTopScript().vpManual(vpName, expected,
		// actual);
		pass = expected.equals(actual);

		msg = " VP PASSED: expected=" + expected + ", actual=" + actual;
		log.debug(vpName + msg);
		// equal = true;
		// } catch (ComparisonFailure cfe) {
		// // cfe.printStackTrace();
		// // the values do not match
		// pass = false;
		// msg = "VP FAILED: expected=" + expected + ", actual=" + actual;
		// log.debug(vpName + "VP FAILED: expected=" + expected + ", actual=" + actual);
		// }
		vp.setPass(pass);
		this.getManager().addVerificationPoint(vp);

		// return pass;
	}

	// public ScriptResults getResults()
	// {
	//// if (results == null){
	//// results = new
	// ScriptResults(isModularScript?EVENT_TYPE.CALLEDSCRIPT:EVENT_TYPE.EXECUTIONSCRIPT);
	//// }
	// return getManager().getScriptResults();
	// }

	@SuppressWarnings("unused")
	private void vpEquals(TestScriptManager mgr, String vpName, String expected, String actual) {
		// //getManager().vpEquals(vpName, expected, actual);
		// IFtVerificationPoint rftVp = this.vpManual(vpName, expected, actual);
		// boolean pass = rftVp.compare();
		//
		//
		// }
		// protected boolean vpEquals(String vpName, String expected, String actual) {
		// boolean equal;
		boolean pass = false;
		VerificationPoint vp = new VerificationPoint(vpName, expected, actual);
		vp.setScriptName(this.getManager().getScriptName());
		vp.createTimestamp();
		String msg = "";
		// try {
		// getAutomationTool().vpEquals(expected, actual);
		// RationalTestScript rftScript = ((RationalTestScript)getTestScript());
		// IFtVerificationPoint rftVp = getTopScript().vpManual(vpName, expected,
		// actual);
		pass = expected.equals(actual);

		msg = " VP PASSED: expected=" + expected + ", actual=" + actual;
		log.debug(vpName + msg);
		// equal = true;
		// } catch (ComparisonFailure cfe) {
		// // cfe.printStackTrace();
		// // the values do not match
		// pass = false;
		// msg = "VP FAILED: expected=" + expected + ", actual=" + actual;
		// log.debug(vpName + "VP FAILED: expected=" + expected + ", actual=" + actual);
		// }
		vp.setPass(pass);
		this.getManager().addVerificationPoint(vp);

		// return pass;
	}

	// @Override
	// public ExcelDataFile getDataFile(String fileName, String sheetName) {
	// // TODO Auto-generated method stub
	// return null;
	// }
	@Override
	public ScriptResults getResults() {
		return results;
	}

	@Override
	public void setCurrent508SheetName(String current508SheetName) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getCurrent508SheetName() {
		// TODO Auto-generated method stub
		return null;
	}

}
