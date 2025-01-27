package platformIndependentCore.exceptions;

/**
 * InvalidParameterException is a RunTimeException to be thrown when an invalid
 * parameter is encountered
 *
 * @author VBAAUSTAYLOL
 *
 */
public class InvalidParameterException extends RuntimeException {

	/** generated serial version UID */
	private static final long serialVersionUID = 6333259043541147600L;

	/**
	 * Constructor passes the exception message to the RunTimeException constructor
	 *
	 * @param details text to include in exception
	 */
	public InvalidParameterException(String details) {
		super("INVALID PARAMETER: " + details);
	}
}
