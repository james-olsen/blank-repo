package platformIndependentCore.core;

/**
 * Class to hold the requirement ID and Status for Requirements
 *
 * @author VBAAUSTAYLOL
 *
 */
public class Requirement {
	/** The requirement ID */
	private String reqID;
	/** Status of the requirement */
	private String status;

	/**
	 * Constructor will create Requirement instance
	 *
	 * @param reqID  id of requirement
	 * @param status status of requirement
	 */
	public Requirement(String reqID, String status) {
		this.reqID = reqID;
		this.status = status;
	}

	/**
	 * Returns the REQ ID
	 *
	 * @return String
	 */
	public String getReqID() {
		return reqID;
	}

	/**
	 * Returns the Status
	 *
	 * @return String
	 */
	public String getStatus() {
		return status;
	}
}
