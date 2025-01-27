package platformIndependentCore.utilities;

import platformIndependentCore.core.ToolManager;

/**
 * Provides access to interact with an Alert window
 *
 * @author VBAAUSTAYLOL
 *
 */
public class Alert extends ToolManager {

	/**
	 * Will return the text from the Alert message
	 *
	 * @return String text on Alert
	 */
	public static String readMessage() {
		return getAutomationTool().readAlertMessage();
	}

	/**
	 * Verifies if an Alert is currently present
	 *
	 * @return TRUE if Alert it present, FALSE if not
	 */
	public static boolean isAlertPresent() {
		return getAutomationTool().isAlertPresent();
	}

	/**
	 * Will click the Accept button on an Alert pop up
	 */
	public static void clickAccept() {
		getAutomationTool().clickAlertAcceptButton();
	}

	/**
	 * Will click the Cancel button on an Alert pop up
	 */
	public static void clickCancel() {
		getAutomationTool().clickAlertCancelButton();
	}

}
