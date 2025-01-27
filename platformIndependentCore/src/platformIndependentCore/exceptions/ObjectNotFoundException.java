package platformIndependentCore.exceptions;

/**
 * ObjectNotFoundException is a RuntimeException that is thrown when the
 * Automation Tool is unable to locate a specified object during a Search
 *
 * @author VBAAUSTAYLOL
 *
 */
public class ObjectNotFoundException extends RuntimeException {
	/** generated serial version UID */
	private static final long serialVersionUID = 7042154931702527360L;

	/**
	 * Constructor passes the details to the RunTimeException constructor
	 *
	 * @param details text to include in exception
	 */
	public ObjectNotFoundException(String details) {
		super("No objects were found matching given criteria: " + details);
	}
}
