package platformIndependentCore.exceptions;

/**
 * WrappedException is a RunTimeException to be thrown when another Exception
 * (typically a Checked Exception) is encountered. This lets the exception to be
 * thrown at Runtime rather than requiring the code to recover or handle the
 * exception
 *
 * @author VBAAUSTAYLOL
 *
 */
public class WrappedException extends RuntimeException {
	/** generated serial version UID */
	private static final long serialVersionUID = -6240822076977464700L;

	/**
	 * Constructor passes the exception Message to the RunTimeException constructor
	 *
	 * @param exceptionMessage text to include in exception
	 */
	public WrappedException(String exceptionMessage) {
		super(exceptionMessage);
	}

	/**
	 * Constructor passes the exception to the RunTimeException constructor
	 *
	 * @param e exception to wrap as a RuntimeException
	 */
	public WrappedException(Exception e) {
		super(e);
	}

}
