package platformIndependentCore.events;

import java.util.Date;
import java.util.Objects;

import platformIndependentCore.utilities.CoreDateTimeFormat;

/**
 * Class for recording events during an automation run. An event can be a
 * verification point, a test script run or a note or an exception.
 *
 * @author VBAAUSTAYLOL
 *
 */
public class Event {
	/** EVENT_TYPE enum */
	public enum EVENT_TYPE {
		/** Verification Point */
		VP,
		/** Called Script */
		CALLEDSCRIPT,
		/** Execution Script */
		EXECUTIONSCRIPT,
		/** Test Suite execution */
		SUITE,
		/** Note for log */
		NOTE,
		/** Screen shot for log */
		SCREENSHOT
	}

	/**
	 * Constructor creates an Event instance for specified type
	 *
	 * @param type of Event (VP, NOTE, etc)
	 */
	public Event(EVENT_TYPE type) {
		eventType = type;
		createTimestamp();
	}

	/** Date Time format "E, dd MMM yyyy HH:mm:ss" */
	public static final CoreDateTimeFormat DATE_TIME_FORMAT = new CoreDateTimeFormat("E, dd MMM yyyy HH:mm:ss");
	/** Type of Event */
	private EVENT_TYPE eventType;
	/** Requirement VP will be logged against */
	private String timestamp = "";
	/** Name of Script associated with the Event */
	private String scriptName = "";
	/** Execution Note (to include in log) */
	private String executionNote = "";

	/**
	 * Will create and record a timestamp for this event
	 */
	public void createTimestamp() {
		Date currTime = new Date(System.currentTimeMillis());
		timestamp = Event.DATE_TIME_FORMAT.format(currTime);
	}

	/**
	 * Will return the timestamp recorded for this event
	 *
	 * @return String
	 */
	public String getTimestamp() {
		return timestamp;
	}

	/**
	 * @return the scriptName
	 */
	public String getScriptName() {
		return scriptName;
	}

	/**
	 * @param scriptName the scriptName to set
	 */
	public void setScriptName(String scriptName) {
		this.scriptName = scriptName;
	}

	/**
	 * Returns the EVENT_TYPE
	 *
	 * @return EVENT_TYPE
	 */
	public EVENT_TYPE getEventType() {
		return eventType;
	}

	/**
	 * Returns TRUE if this is a verification point event
	 *
	 * @return boolean
	 */
	public boolean isVerificationPoint() {
		return eventType.equals(EVENT_TYPE.VP);
	}

	/**
	 * Returns TRUE if this is a Note event
	 *
	 * @return boolean
	 */
	public boolean isNote() {
		return eventType.equals(EVENT_TYPE.NOTE);
	}

	/**
	 * Returns TRUE if this is an Exception note event
	 *
	 * @return boolean
	 */
	public boolean isException() {
		boolean isException = false;
		// Exceptions are a type of note, so check that first
		if (isNote()) {
			isException = ((Note) this).isException();
		}
		return isException;
	}

	/**
	 * Returns TRUE if this is a called script event
	 *
	 * @return boolean
	 */
	public boolean isCalledScript() {
		return eventType.equals(EVENT_TYPE.CALLEDSCRIPT);
	}

	/**
	 * Returns TRUE if this is an execution sctip event
	 *
	 * @return boolean
	 */
	public boolean isExecutionScript() {
		return eventType.equals(EVENT_TYPE.EXECUTIONSCRIPT);
	}

	/**
	 * Sets the EVENT_TYPE for this event
	 *
	 * @param eventType to set for this event instance
	 */
	public void setEventType(EVENT_TYPE eventType) {
		this.eventType = eventType;
	}

	/**
	 * Returns the Event value for this Verification Point
	 *
	 * @return the event
	 */
	EVENT_TYPE getEvent() {
		return eventType;
	}

	/**
	 * Set the Event value
	 *
	 * @param event the event to set
	 */
	public void setEvent(EVENT_TYPE event) {
		eventType = event;
	}

	/**
	 * Returns the Execution Note value for this Verification Point
	 *
	 * @return the executionNote
	 */
	public String getExecutionNote() {
		return executionNote;
	}

	/**
	 * Set the Execution Note value
	 *
	 * @param note the executionNote to set
	 */
	public void setExecutionNote(String note) {
		Objects.requireNonNull(note, "Can not set the Execution Note for a Verification Point to NULL.");

		this.executionNote = note;
	}

	/**
	 * Verifies if this event is an instance of SCREENSHOT
	 *
	 * @return true if it is a SCREENSHOT, false if not
	 */
	public boolean isScreenShot() {
		return eventType.equals(EVENT_TYPE.SCREENSHOT);
	}

}
