package platformIndependentCore.exceptions;

/**
 * Exception class to be thrown when a File can not be found
 *
 * @author VBAAUSTAYLOL
 *
 */
public class MissingFileException extends RuntimeException {
	/** generated serial version UID */
	private static final long serialVersionUID = 4081815763982896969L;

	/**
	 * Constructor for new MissingFileException
	 *
	 * @param exception message to be shown to users
	 */
	public MissingFileException(String exception) {
		super(exception);
	}

}
