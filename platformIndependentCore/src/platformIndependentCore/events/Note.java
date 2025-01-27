package platformIndependentCore.events;

/**
 * Class represents a NOTE or EXCEPTION type event
 *
 * @author VBAAUSTAYLOL
 *
 */
// TODO - Perhaps split this into Exception and Note events, one object for each
public class Note extends Event {
	/** Is this Note an Exception */
	boolean isException = false;
	/** Is this Note a Warning */
	boolean isWarning = false;
	/** Exception associated with this note */
	Exception exception;
	/** Text message for note */
	String note = "";

	/**
	 * Constructor save the note message
	 *
	 * @param note message
	 */
	public Note(String note) {
		super(EVENT_TYPE.NOTE);
		this.note = note;
	}

	/**
	 * Will create an exception note event
	 *
	 * @param e    exception
	 * @param note to accompany exception
	 */
	public Note(Exception e, String note) {
		super(EVENT_TYPE.NOTE);
		isException = true;
		exception = e;
		this.note = note;

	}

	/**
	 * Creates a Note for this Exception
	 *
	 * @param e exception
	 */
	public Note(Exception e) {
		super(EVENT_TYPE.NOTE);
		isException = true;
		exception = e;
		e.printStackTrace();
		StackTraceElement[] fullTrace = e.getStackTrace();
		String stackTrace = e.getMessage() + "\n";
		for (StackTraceElement trace : fullTrace) {
			stackTrace += trace.toString() + "\n";
		}
		this.note = stackTrace;

	}

	/**
	 * Returns TRUE if this note event represents a warning
	 *
	 * @return boolean TRUE if is WARNING
	 */
	public boolean isWarning() {
		return isWarning;
	}

	/**
	 * Returns TRUE if this note event represents an exception
	 *
	 * @return boolean TRUE if is EXCEPTION
	 */
	@Override
	public boolean isException() {
		return isException;
	}

	/**
	 * Sets if this NOTE is an EXCEPTION
	 *
	 * @param isException TRUE if is EXCEPTION, FALSE if not
	 */
	public void setException(boolean isException) {
		this.isException = isException;
	}

	/**
	 * Sets if this NOTE is an WARNING
	 *
	 * @param isWarning TRUE if is WARNING, FALSE if not
	 */
	public void setWarning(boolean isWarning) {
		this.isWarning = isWarning;
	}

	/**
	 * Checks if this Note is an Exception
	 *
	 * @return exception e
	 */
	public Exception getException() {
		return exception;
	}

	/**
	 * Sets the exception
	 *
	 * @param exception e
	 */
	public void setException(Exception exception) {
		this.exception = exception;
	}

	/**
	 * Returns the note
	 *
	 * @return String note
	 */
	public String getNote() {
		return note;
	}

	/**
	 * Sets the note
	 *
	 * @param note value
	 */
	public void setNote(String note) {
		this.note = note;
	}

}
