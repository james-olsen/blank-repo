package platformIndependentCore.exceptions;

/**
 * AutomationToolNotConfiguredException is a RunTimeException to be thrown when
 * AUTOMATION_TOOL is not set in the config.properties or project.properties
 * files
 *
 * @author VBAAUSTAYLOL
 *
 */
public class AutomationToolNotConfiguredException extends RuntimeException {
	/** generated serial version UID */
	private static final long serialVersionUID = 794041179016066750L;

	/**
	 * Constructor passes the exception message to the RunTimeException constructor
	 */
	public AutomationToolNotConfiguredException() {
		super("Automation Tool not configured in the config.properties file. Please set the AUTOMATION_TOOL variable in config.properties. Example: AUTOMATION_TOOL=RFT or AUTOMATION_TOOL=SELENIUM");
	}
}
