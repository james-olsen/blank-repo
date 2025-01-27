package platformIndependentCore.exceptions;

/**
 * <b>Name :</b> InvalidStateException.java
 * <p>
 * <b>Generated :</b> Apr 1, 2020
 * <p>
 * <b>Description :</b> Exception to be thrown when there is an Invalid State
 * <p>
 *
 * @since Apr 1, 2020
 * @author vbaaustaylol
 */
public class InvalidStateException extends RuntimeException {

	/**
	 * Generated serial version id
	 */
	private static final long serialVersionUID = -7998857735257979388L;

	/**
	 * Constructor for InvalidSateException
	 *
	 * @param details of exception
	 */
	public InvalidStateException(String details) {
		super("INVALID STATE: " + details);
	}

}
