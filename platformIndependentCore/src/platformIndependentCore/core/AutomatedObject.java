package platformIndependentCore.core;

/**
 * Class defines abstract methods for Automated Object implementations.
 *
 * @author VBAAUSTAYLOL
 */
public abstract class AutomatedObject extends ToolManager {
	/** hash key for the object */
	String hashKey = "";

	/**
	 * Setter for the hashkey value
	 *
	 * @param hashKey used for object caching in the core
	 */
	void setHashKey(String hashKey) {
		this.hashKey = hashKey;
	}

	/**
	 * Returns the hashkey used for this object
	 *
	 * @return String hashKey
	 */
	String getHashKey() {
		return hashKey;
	}

	/**
	 * Will read and return the value of this AutomatedObject as a String <br>
	 * <b>NOTE:</b> This method will trim whitespace from the value
	 *
	 * @return String text of object (minus any leading or trailing whitespace)
	 */
	public String readText() {
		return readValue() == null ? "" : readValue().toString().trim();
	}

	/**
	 * Will clear out any value in the object
	 */
	public abstract void clear();

	/**
	 * Will click the AutomatedObject
	 */
	public abstract void click();

	/**
	 * Will move the mouse to the AutomatedObject
	 */
	public abstract void moveMouseTo();

	/**
	 * Will move the mouse to the AutomatedObject for hover text
	 */
	public abstract void hover();

	/**
	 * Will click the AutomatedObject<br>
	 * <br>
	 * This method allows a user to specify if they want to use a native click()
	 * method from the Automation Tool, or if they are experiencing issues with
	 * .click() methods not resulting in actual clicks, they can pass in FALSE to
	 * try an alternate method of sending an {ENTER} command to the object.
	 *
	 * @param nativeClick true if the .click() method in the underlying Automation
	 *                    Tool is to be used, false if an {ENTER} command is to be
	 *                    sent to the object.
	 */
	public abstract void click(boolean nativeClick);

	/**
	 * Will return true if the object is enabled on the page, false otherwise
	 *
	 * @return boolean returns true if the object is enabled, false if it is
	 *         disabled
	 */
	public boolean isEnabled() {
		boolean enabled = false;
		String value = getPropertyValue("class");
		// getAutomationTool().printObjectProperties(this, false, false);
		if (value == null || value.isEmpty()) {
			value = getPropertyValue("disabled");
			enabled = !Boolean.valueOf(value);
		} else {
			enabled = value.toLowerCase().contains("disabled");
		}
		return enabled;
	}

	/**
	 * Will return true if AutomatedObject is selected, false if not.
	 *
	 * @return boolean true if the object is selected false if not
	 */
	public abstract boolean isSelected();

	/**
	 * Will return the Children objects for this AutomatedObject
	 *
	 * @return AutomatedObject[] children objects of the current AutomatedObject
	 */
	public abstract AutomatedObject[] getChildren();

	/**
	 * Will return the matching object that is a descendant of this AutomatedObject
	 *
	 * @param search criteria to locate descendant object
	 * @return AutomatedObject descendant object matching search criteria
	 */
	@Override
	public AutomatedObject getObject(Search search) {
		search.setParent(this);
		return getAutomationTool().getAutomatedObject(search);
	}

	/**
	 * Will return the Parent object for this AutomatedObject
	 *
	 * @return AutomatedObject parent object
	 */
	public abstract AutomatedObject getParent();

	/**
	 * Will return the value of the specified property.
	 *
	 * @param property to find value of
	 * @return String value of the specified property
	 */
	public abstract String getPropertyValue(String property);

	/**
	 * Sets the value of the object and will continue to verify and retry setting it
	 * until the entered text matches the desired value<br>
	 * <br>
	 *
	 * <b> This is only for use on problematic fields where the entered characters
	 * sometimes end up in the wrong order</b>
	 *
	 * @param value to set the object to
	 */
	@Deprecated
	public void setValueAndRetryUntilVerified(String value) {

		click();
		if (!readText().isEmpty()) {
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// Clear out the value in the field before setting the text
			clear();
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		sendKeys(value);

		if (!readText().equals(value)) {
			setValueAndRetryUntilVerified(value);
		}
	}

	/**
	 * Will read and return the value of this AutomatedObject
	 *
	 * @return Object value of this AutomatedObject
	 */
	public abstract Object readValue();

	/**
	 * Will set the value of this AutomatedObject Please note that if the object is
	 * not editable through the GUI, this will not work
	 *
	 * @param value to set this object to
	 */
	public abstract void setValue(Object value);

	/**
	 * Will use Tool specific commands to send keys to the object, replicating
	 * typing
	 *
	 * @param text to send to this AutomatedObject
	 */
	public abstract void sendKeys(String text);

	/**
	 * Will check if the AutomatedObject is currently displayed/visible
	 *
	 * @return TRUE if visible, FALSE is hidden
	 */
	public abstract boolean isDisplayed();

	/**
	 * Will perform a double click on the object
	 */
	public abstract void doubleClick();

	@Override
	public String toString() {
		String value = "";
		String tag = getPropertyValue("tag");
		String id = getPropertyValue("id");
		String text = readText();
		value += "\n**** AUTOMATED OBJECT:  TO STRING ***";

		value += "\n     ID=" + id + "\n     TAG=" + tag + "\n     TEXT=" + text;
		value += "\n*************************************\n";
		return value;
	}

}
