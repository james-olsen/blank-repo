package platformIndependentCore.exceptions;

/**
 * InvalidDataException is a RunTimeException to be thrown when invalid data is
 * encountered
 *
 * @author VBAAUSTAYLOL
 *
 */
public class InvalidDataException extends RuntimeException {

	/** generated serial version UID */
	private static final long serialVersionUID = 1016236266409680676L;

	/**
	 * Constructor passes the exception message to the RunTimeException constructor
	 *
	 * @param exception text to include in exception
	 */
	public InvalidDataException(String exception) {
		super(exception);
	}

}
