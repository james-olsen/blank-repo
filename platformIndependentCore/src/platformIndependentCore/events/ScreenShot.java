package platformIndependentCore.events;

import java.io.File;

/**
 * ScreenShot class is a type of Event. Will contain screen shot and note to
 * include in results log
 *
 * @author VBAAUSTAYLOL
 *
 */
public class ScreenShot extends Event {
	/** Note to include in log */
	String note = "";
	/** File where screenshot is located */
	File screenShotFile = null;

	/**
	 * Constructor for screen shot with note
	 *
	 * @param screenShotFile File containing screen shot
	 * @param note           to include in log
	 */
	public ScreenShot(File screenShotFile, String note) {
		super(EVENT_TYPE.SCREENSHOT);
		this.note = note;
		this.screenShotFile = screenShotFile;
	}

	/**
	 * Constructor for screen shot
	 *
	 * @param screenShotFile File containing screen shot
	 */
	public ScreenShot(File screenShotFile) {
		super(EVENT_TYPE.SCREENSHOT);
		this.screenShotFile = screenShotFile;
	}
	//
	// /**
	// * @return the name
	// */
	// public String getName() {
	// return name;
	// }
	//
	// /**
	// * @param name the name to set
	// */
	// public void setName(String name) {
	// this.name = name;
	// }

	/**
	 * @return the note
	 */
	public String getNote() {
		return note;
	}

	/**
	 * @param note the note to set
	 */
	public void setNote(String note) {
		this.note = note;
	}

	/**
	 * @return the screenShotFile
	 */
	public File getScreenShotFile() {
		return screenShotFile;
	}

	/**
	 * @param screenShotFile the screenShotPath to set
	 */
	public void setScreenShotFile(File screenShotFile) {
		this.screenShotFile = screenShotFile;
	}

}
