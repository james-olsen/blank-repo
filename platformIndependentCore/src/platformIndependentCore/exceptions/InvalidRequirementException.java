package platformIndependentCore.exceptions;

/**
 * InvalidRequirementException is a RunTimeException to be thrown when invalid
 * requirement is encountered
 *
 * @author VBAAUSTAYLOL
 *
 */
public class InvalidRequirementException extends RuntimeException {

	/** generated serial version UID */
	private static final long serialVersionUID = -3301912133008570068L;

	/**
	 * Constructor passes the exception message to the RunTimeException constructor
	 *
	 * @param exception text to include in exception
	 */
	public InvalidRequirementException(String exception) {
		super(exception);
	}

}
