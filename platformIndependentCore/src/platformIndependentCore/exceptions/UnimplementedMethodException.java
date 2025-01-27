package platformIndependentCore.exceptions;

import platformIndependentCore.utilities.ConfigProperties;

/**
 * UnimplementedMethodException is a RunTimeException to be thrown when method
 * has not been implemented for the current Automation Tool
 *
 * @author VBAAUSTAYLOL
 *
 */
public class UnimplementedMethodException extends RuntimeException {
	/** Current configured Automation Tool */
	static String automationTool = ConfigProperties.getValue("AUTOMATION_TOOL");
	/** generated serial version UID */
	private static final long serialVersionUID = -1469267951689704035L;

	/**
	 * Constructor passes the details to the RunTimeException constructor
	 *
	 * @param method that has not been implemented for the current Automation Tool
	 */
	public UnimplementedMethodException(String method) {
		super("Please check your current automation tool. The " + method + " method has not been implemented for "
				+ automationTool);
	}

}
