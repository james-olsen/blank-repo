package platformIndependentCore.exceptions;

/**
 * <b>Name :</b> InvalidAutomatedPageStateException.java
 * <p>
 * <b>Generated :</b> Apr 15, 2020
 * <p>
 * <b>Description :</b> InvalidAutomatedPageStateException thrown when an action
 * can not be completed on an Automated Page due to some expected condition not
 * being met
 * <p>
 *
 * @since Apr 15, 2020
 * @author vbaaustaylol
 */
public class AutomatedPageStateException extends RuntimeException {

	/**
	 * Generate serial version id
	 */
	private static final long serialVersionUID = -1656900168059262999L;

	/**
	 * Constructor
	 *
	 * @param details for the exception
	 */
	public AutomatedPageStateException(String details) {
		super("Your AutomatedPage is in an invalid state: " + details);
	}
}
