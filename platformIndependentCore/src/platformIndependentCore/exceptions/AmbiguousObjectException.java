package platformIndependentCore.exceptions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Exception to be thrown when more than one matching object is found for a
 * search
 * <p>
 * Extends RuntimeException
 *
 * @author VBAAUSTAYLOL
 *
 */
public class AmbiguousObjectException extends RuntimeException {
	/**
	 * Generate serial version id
	 */
	private static final long serialVersionUID = -5564131639906121313L;
	/**
	 * Logging
	 */
	static Logger log = LogManager.getLogger(AmbiguousObjectException.class.getName());

	/**
	 * Constructor passes exception details to the RuntimeExcpetion constructor
	 *
	 * @param details of the exception
	 */
	public AmbiguousObjectException(String details) {
		super("Multiple objects were found matching given criteria: " + details);
	}

}
