package platformIndependentCore.exceptions;

/**
 * <b>Name :</b> ReportingException.java
 * <p>
 * <b>Generated :</b> Jul 1, 2020
 * <p>
 * <b>Description : An exception for reporting in XRay</b>
 * <p>
 *
 * @since Jul 1, 2020
 * @author VBAAUSTAYLOL
 */
public class ReportingException extends RuntimeException {
	/** generated serial version UID */
	private static final long serialVersionUID = 4802954736416268527L;

	/**
	 * Constructor passes the details to the RunTimeException constructor
	 *
	 * @param details text to include in exception
	 */
	public ReportingException(String details) {
		super("XRAY Results have not been recorded: " + details);
	}

}
