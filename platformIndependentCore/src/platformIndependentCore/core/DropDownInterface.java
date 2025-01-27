package platformIndependentCore.core;

import java.util.ArrayList;

import platformIndependentCore.core.CriteriaObject.REGEX;

/**
 * Interface for interacting with Drop Downs
 *
 * @author VBAAUSTAYLOL
 *
 */
public interface DropDownInterface {
	/**
	 * Method to select the specified option by text in the drop down
	 *
	 * @param optionText text to select in drop down
	 */
	void select(String optionText);

	/**
	 * Method to select the specified option in the drop down using the specified
	 * REGEX criteria
	 *
	 * @param optionText text to select in drop down
	 * @param regex      specifies type of REGEX to use when searching for subitem
	 */
	void select(String optionText, REGEX regex);

	/**
	 * Method to select the specified option in the drop down using the specified
	 * value.
	 *
	 * @param optionValueToSelect value to select in drop down
	 */
	void selectByValue(String optionValueToSelect);

	/**
	 * Method to return the currently selected text for this drop down
	 *
	 * @return String text value of selected item in drop down
	 */
	String readSelected();

	/**
	 * Will check if the AutomatedObject is currently displayed/visible
	 *
	 * @return TRUE if visible, FALSE is hidden
	 */
	boolean isDisplayed();

	/**
	 * Will return true if the object is enabled on the page, false otherwise
	 *
	 * @return boolean returns true if the object is enabled, false if it is
	 *         disabled
	 */
	boolean isEnabled();

	/**
	 * Will check if the specified option is in the Drop Down. If it is, will return
	 * TRUE, if it is not found, will return FALSE
	 *
	 * @param optionText check if an option with matching text is in the drop down
	 * @return boolean TRUE if found, FALSE if not
	 */
	default boolean isOptionPresent(String optionText) {
		return isOptionPresent(optionText, REGEX.NO_REGEX);
	}

	/**
	 * Will check if the specified option is in the Drop Down. Will use the
	 * specified REGEX setting to match the option. If it is, will return TRUE, if
	 * it is not found, will return FALSE
	 *
	 * @param optionText check if an option with matching text is in the drop down
	 * @param regex      specifies type of REGEX to use when searching for subitem
	 * @return boolean TRUE if found, FALSE if not
	 */
	boolean isOptionPresent(String optionText, REGEX regex);

	/**
	 * Returns all the options in a dropdown in an Arraylist of Strings
	 *
	 * @return ArrayList<String> all the options in a dropdown in an Arraylist of
	 *         Strings
	 */
	ArrayList<String> readAllOptions();
}
