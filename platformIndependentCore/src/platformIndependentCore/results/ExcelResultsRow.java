package platformIndependentCore.results;

/**
 * Container class for the elements (cells) in a Results Row within an
 * ExcelResultsFile
 *
 * Methods are available to the package only
 *
 * @author vbaaustaylol
 *
 */
class ExcelResultsRow {
	/** script name **/
	private String scriptName = "";
	/** time stamp **/
	private String timeStamp = "";
	/** event **/
	private String event = "";
	/** VP Name **/
	private String vpName = "";
	/** VP Pass Fail **/
	private String vpPassFail = "";
	/** VP Note **/
	private String vpNote = "";
	/** Overall Pass Fail **/
	private String overallPassFail = "";
	/** Execution Note **/
	private String executionNote = "";
	/** Execution Note Hyperlink **/
	private String executionNoteHyperlink = "";
	/** is Start Row **/
	private boolean isStartRow = false;
	/** is End Row **/
	private boolean isEndRow = false;
	/** Execution Duration **/
	private String executionDuration = "";

	/**
	 * Sets the value for the script name
	 *
	 * @param scriptName the name of the script
	 */
	void setScriptName(String scriptName) {
		this.scriptName = scriptName;
	}

	/**
	 * Sets the timestamp field
	 *
	 * @param timeStamp the time stamp to set
	 */
	void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}

	/**
	 * Sets the event (ie. Script Start or End or Verification Point)
	 *
	 * @param event the even to set
	 */
	void setEvent(String event) {
		this.event = event;
	}

	/**
	 * Sets the name for the Verification Point
	 *
	 * @param vpName name of the verification point
	 */
	void setVpName(String vpName) {
		this.vpName = vpName;
	}

	/**
	 * Sets the Pass/Fail for a Verification Point
	 *
	 * @param vpPassFail the vp pass fail string to set
	 */
	void setVpPassFail(String vpPassFail) {
		this.vpPassFail = vpPassFail;
	}

	/**
	 * Adds a note for the verification point
	 *
	 * @param vpNote the note to set
	 */
	void setVpNote(String vpNote) {
		this.vpNote = vpNote;
	}

	/**
	 * Set Overall Pass/Fail value for the script
	 *
	 * @param overallPassFail the overall pass fail value to set
	 */
	void setOverallPassFail(String overallPassFail) {
		this.overallPassFail = overallPassFail;
	}

	/**
	 * Sets the Note column
	 *
	 * @param executionNote string that is the execution note to set
	 */
	void setExecutionNote(String executionNote) {
		this.executionNote = executionNote;
	}

	/**
	 * Sets the Hyperlink for the Note column
	 *
	 * @param executionNoteHyperlink string for the execution hyperlink value to set
	 */
	void setExecutionNoteHyperlink(String executionNoteHyperlink) {
		this.executionNoteHyperlink = executionNoteHyperlink;
	}

	// /**
	// * Sets the Execution Time column
	// *
	// * @param executionTime
	// */
	// void setExecutionTime(double executionTime){
	// this.executionTime = executionTime;
	// }
	/**
	 * Sets the Execution Time column
	 *
	 * @param executionDuration the execution time to set
	 */
	void setExecutionDuration(String executionDuration) {
		this.executionDuration = executionDuration;
	}

	/**
	 * Specify if this is the first row for a script
	 *
	 * @param isStartRow the value for isStartRow to set
	 */
	void setIsStartRow(boolean isStartRow) {
		this.isStartRow = isStartRow;
	}

	/**
	 * Specify if this is the last row for a script
	 *
	 * @param isEndRow the value for the isEndRow to set
	 */
	void setIsEndRow(boolean isEndRow) {
		this.isEndRow = isEndRow;
	}

	/**
	 * Returns the value for the Script Name column
	 *
	 * @return scriptName
	 */
	String getScriptName() {
		return scriptName;
	}

	/**
	 * Returns the timestamp value as a String Format of timestamp: "yyyy-MM-dd
	 * HH:mm:ss"
	 *
	 * @return timeStamp
	 */
	String getTimeStamp() {
		return timeStamp;
	}

	/**
	 * Returns the event for this row
	 *
	 * @return event
	 */
	String getEvent() {
		return event;
	}

	/**
	 * Returns the VerificationPoint Name
	 *
	 * @return vpName
	 */
	String getVpName() {
		return vpName;
	}

	/**
	 * Returns the value of the VerificationPoint Pass/Fail field
	 *
	 * @return vpPassFail
	 */
	String getVpPassFail() {
		return vpPassFail;
	}

	/**
	 * Returns the note for the VerificationPoint
	 *
	 * @return vpNote
	 */
	String getVpNote() {
		return vpNote;
	}

	/**
	 * Returns the Overall Pass/Fail value for the script
	 *
	 * @return overallPassFail
	 */
	String getOverallPassFail() {
		return overallPassFail;
	}

	/**
	 * Returns the value for Execution Note
	 *
	 * @return executionNote
	 */
	String getExecutionNote() {
		return executionNote;
	}

	/**
	 * Returns the value for Execution Note
	 *
	 * @return executionNoteHyperlink
	 */
	String getExecutionNoteHyperlink() {
		return executionNoteHyperlink;
	}

	/**
	 * Returns the value for Execution Time
	 *
	 * @return executionTime
	 */
	String getExecutionDuration() {

		return executionDuration;
	}

	/**
	 * Returns TRUE if this is the first row for a script returns FALSE otherwise
	 *
	 * @return isStartRow
	 */
	boolean isStartRow() {
		return isStartRow;
	}

	/**
	 * Returns TRUE if this is the last row for a script returns FALSE otherwise
	 *
	 * @return isEndRow
	 */
	boolean isEndRow() {
		return isEndRow;
	}

}
