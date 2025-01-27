package platformIndependentCore.exceptions;

/**
 * MissingAutomationToolLibrariesException is a RunTimeException to be thrown
 * when a user is configured to run a specific Automation Tool and are missing
 * the required libraries
 *
 * @author VBAAUSTAYLOL
 *
 */
public class MissingAutomationToolLibrariesException extends RuntimeException {

	/** generated serial version UID */
	private static final long serialVersionUID = -825970250359912043L;

	/**
	 * Constructor passes the exception message to the RunTimeException constructor
	 *
	 * @param automationTool configured
	 */
	public MissingAutomationToolLibrariesException(String automationTool) {
		super("You are configured to run " + automationTool + ", but are missing required libraries. "
				+ "Please check your config.properties file is configured "
				+ "for the desired platform and the project build path has the " + "required jar file ("
				+ automationTool.toLowerCase() + "Core.jar) and that"
				+ " it is ordered above the platformIndependentCore.jar. \n\nIf you"
				+ " verify all required jars are in place, then you may need to talk to a Core team"
				+ " member about ensuring this method is implemented for your current automation tool ("
				+ automationTool + ")");
	}

}
