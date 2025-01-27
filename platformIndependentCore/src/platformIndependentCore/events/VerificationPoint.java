package platformIndependentCore.events;

import java.util.ArrayList;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import platformIndependentCore.core.CriteriaObject.REGEX;
import platformIndependentCore.core.Requirement;
import platformIndependentCore.exceptions.InvalidDataException;
import platformIndependentCore.utilities.CoreDateTimeFormat;

/**
 * Class to represnet Verification Point objects
 *
 * @author vbaaustaylol
 *
 */
public class VerificationPoint extends Event {
	/** logger for class */
	static Logger log = LogManager.getLogger(VerificationPoint.class.getName());

	// the standard variables that are always set for VPs
	/** Name of the VP */
	private String vpName;
	/** Expected Value of the VP */
	private Object expected;
	/** Actual Value of the VP */
	private Object actual;
	// settings for String VPs
	/** Will the VP ignore white spaces */
	private boolean ignoreWhiteSpace = false;
	/** Is the VP a STARTS WITH compare */
	private boolean startsWith = false;
	/** Is the VP a CONTAINS compare */
	private boolean contains = false;
	// Setting for ALL CAPS
	/** Is the VP an ALL CAPS compare */
	private boolean allCaps = false;
	// Settings for Results Logging
	/** The Requirement associated with the VP */
	private Requirement requirement;

	/** PASS/FAIL status for the VP */
	private boolean pass;

	/**
	 * Returns the ALL CAPS setting
	 *
	 * @return boolean TRUE if ALL CAPS, FALSE if not
	 */
	public boolean isAllCaps() {
		return allCaps;
	}

	/**
	 * Sets the ALL CAPS setting
	 *
	 * @param allCaps TRUE if all caps, FALSE if not
	 */
	public void setAllCaps(boolean allCaps) {
		this.allCaps = allCaps;
	}

	/**
	 * Returns the REGEX to be used with this Verification Point
	 *
	 * @return REGEX for the VP
	 */
	public REGEX getRegex() {
		return regex;
	}

	/**
	 * Will set the REGEX to use on this Verification Point
	 *
	 * @param regex to use during VP
	 */
	public void setRegex(REGEX regex) {
		this.regex = regex;
	}

	/** Initialize the REGEX to be NO_REGEX as the default */
	private REGEX regex = REGEX.NO_REGEX;

	/**
	 * @return the expected
	 */
	public Object getExpected() {
		return expected;
	}

	/**
	 * @param expected value the expected to set
	 */
	public void setExpected(Object expected) {
		this.expected = expected;
	}

	/**
	 * @param vpName name of the verification point the vpName to set
	 */
	public void setVpName(String vpName) {
		this.vpName = vpName;
	}

	/**
	 * @return the actual
	 */
	public Object getActual() {
		return actual;
	}

	/**
	 * @param actual value the actual to set
	 */
	public void setActual(Object actual) {
		this.actual = actual;
	}

	/**
	 * @return the pass
	 */
	public boolean isPass() {
		return pass;
	}

	/**
	 * @param pass the pass to set
	 */
	public void setPass(boolean pass) {
		this.pass = pass;
	}

	/**
	 * Constructor for Verification Point comparing two String values
	 *
	 * @param vpName   name of the verification point
	 * @param expected value
	 * @param actual   value
	 */
	public VerificationPoint(String vpName, String expected, String actual) {
		super(EVENT_TYPE.VP);

		// If line separators exist in the passed in Strings, we want to replace
		// them with new lines instead. This causes our VP to execute a
		// multi-line text String correctly.
		this.vpName = vpName;
		this.expected = expected.replace(System.getProperty("line.separator"), "\n").trim();
		this.actual = actual.replace(System.getProperty("line.separator"), "\n").trim();
		createTimestamp();

	}

	/**
	 * Constructor for Verification Point comparing two String values where the
	 * expected value has more than one acceptable value
	 *
	 * @param vpName   name of the verification point
	 * @param expected value
	 * @param actual   value
	 */
	public VerificationPoint(String vpName, String[] expected, String actual) {
		super(EVENT_TYPE.VP);
		// createTimestamp();
		// make sure expected is initialized as a String[] object before trying to
		// access it that way
		this.expected = new String[expected.length];

		this.vpName = vpName;

		// If line separators exist in the passed in Strings, we want to replace
		// them with new lines instead. This causes our VP to execute a
		// multi-line text String correctly.
		for (int i = 0; i < expected.length; i++) {

			((String[]) this.expected)[i] = expected[i].replace(System.getProperty("line.separator"), "\n").trim();
		}

		this.actual = actual.replace(System.getProperty("line.separator"), "\n").trim();

	}

	/**
	 * Constructor for Verification Point comparing two String values where the
	 * expected value has more than one acceptable value
	 *
	 * @param vpName   name of the verification point
	 * @param expected value
	 * @param actual   value
	 */
	@SuppressWarnings("unchecked")
	public VerificationPoint(String vpName, String[] expected, ArrayList<String> actual) {
		super(EVENT_TYPE.VP);
		// make sure expected is initialized as a String[] object before trying to
		// access it that way
		this.expected = new String[expected.length];
		// make sure actual is initialized as an ArrayList<String> object before trying
		// to access it that way
		this.actual = new ArrayList<String>();

		this.vpName = vpName;

		// If line separators exist in the passed in Strings, we want to replace
		// them with new lines instead. This causes our VP to execute a
		// multi-line text String correctly.
		for (int i = 0; i < expected.length; i++) {

			((String[]) this.expected)[i] = expected[i].replace(System.getProperty("line.separator"), "\n").trim();
		}

		for (int i = 0; i < actual.size(); i++) {

			((ArrayList<String>) this.actual).set(i,
					actual.get(i).replace(System.getProperty("line.separator"), "\n").trim());
		}
	}

	/**
	 * Constructor for Verification Point comparing two boolean values
	 *
	 * @param vpName   name of the verification point
	 * @param expected value
	 * @param actual   value
	 */
	public VerificationPoint(String vpName, boolean expected, boolean actual) {
		super(EVENT_TYPE.VP);
		// createTimestamp();
		this.vpName = vpName;
		this.expected = expected;
		this.actual = actual;
	}

	/**
	 * Constructor for Verification Point comparing two boolean values
	 *
	 * @param vpName   name of the verification point
	 * @param expected value
	 * @param actual   value
	 */
	public VerificationPoint(String vpName, CoreDateTimeFormat expected, String actual) {
		super(EVENT_TYPE.VP);
		createTimestamp();
		this.vpName = vpName;
		this.expected = new VPDateTimeFormat(expected);
		this.actual = actual;
	}

	/**
	 * Returns TRUE if this VerificationPoint is comparing two Strings Returns FALSE
	 * if this VerificationPoint is comparing two booleans
	 *
	 * @return boolean TRUE if the VP is a String compare, FALSE if not (which would
	 *         mean it is a boolean compare)
	 */
	boolean isStringVP() {
		return expected.getClass() == String.class;
	}

	/**
	 * Returns TRUE if this VerificationPoint actual has a VPDateTimeFormat FALSE if
	 * this VerificationPoint is comparing anything else
	 *
	 * @return boolean
	 */
	boolean isDateFormatVP() {
		return expected.getClass() == VPDateTimeFormat.class;
	}

	/**
	 * Returns TRUE if this VerificationPoint is comparing two Strings Returns FALSE
	 * if this VerificationPoint is comparing two booleans
	 *
	 * @return boolean
	 */
	boolean isActualArrayList() {
		return actual.getClass() == ArrayList.class;
	}

	/**
	 * Returns TRUE if this VerificationPoint is comparing two Strings Returns FALSE
	 * if this VerificationPoint is comparing two booleans
	 *
	 * @return boolean
	 */
	boolean isBooleanVP() {
		return expected.getClass() == Boolean.class;
	}

	/**
	 * Returns the name for this Verification Point
	 *
	 * @return the vpName
	 */
	public String getVpName() {
		return vpName;
	}

	/**
	 * Returns the Expected value for String comparison VerificationPoints
	 *
	 * @return the expected
	 */
	VPDateTimeFormat getExpectedAsDateFormat() {
		if (isStringVP()) {
			throw new InvalidDataException("Trying to get a VPDateTimeFormat when it is a String");
		} else if (isBooleanVP()) {
			throw new InvalidDataException("Trying to get a VPDateTimeFormat object when it is a Boolean");
		} else if (!isDateFormatVP()) {
			throw new InvalidDataException("Trying to get a VPDateTimeFormat when it is not that type");
		}

		return (VPDateTimeFormat) expected;
	}

	/**
	 * Returns the Expected value for String comparison VerificationPoints
	 *
	 * @return the expected
	 */
	String[] getExpectedAsStringArray() {

		if (isStringVP()) {
			throw new InvalidDataException("Trying to get a String Array object when it is a String");
		}
		if (isDateFormatVP()) {
			throw new InvalidDataException("Trying to get a CoreDateTimeFormat object as a String Array");
		}
		if (isBooleanVP()) {
			throw new InvalidDataException("Trying to get a String Array object when it is a Boolean");
		}
		return (String[]) expected;
	}

	/**
	 * Returns the Expected value for String comparison VerificationPoints
	 *
	 * @return the expected
	 */
	String getExpectedArrayAsString() {

		if (isStringVP()) {
			throw new InvalidDataException("Trying to get a String Array object when it is a String");
		}
		if (isDateFormatVP()) {
			throw new InvalidDataException("Trying to get a CoreDateTimeFormat object as a String");
		}

		if (isBooleanVP()) {
			throw new InvalidDataException("Trying to get a String Array object when it is a Boolean");
		}

		String value = "Any of these values:";
		String[] options = getExpectedAsStringArray();
		log.debug("*** EXPECTED AS STRING ARRAY " + options.length);
		for (String current : getExpectedAsStringArray()) {
			log.debug(" ----" + current);
			value += current + "|";
		}
		return value;
	}

	/**
	 * Returns the Expected value for String comparison VerificationPoints
	 *
	 * @return the expected
	 */
	String getExpectedToString() {
		String toString = expected.toString();
		if (!isStringVP() && !isBooleanVP() && !isDateFormatVP()) {
			// must be an array, get the String representation
			toString = getExpectedArrayAsString();
		}

		return toString;
	}

	/**
	 * Returns the Expected value for String comparison VerificationPoints
	 *
	 * @return the expected
	 */
	String getExpectedAsString() {

		if (!isStringVP()) {
			throw new InvalidDataException("Trying to get a String object when it is a Boolean");
		}
		return (String) expected;
	}

	/**
	 * Returns the Expected Value as a generic object
	 *
	 * @return the expectedValue
	 */
	Object getExpectedObject() {
		return expected;
	}

	/**
	 * Returns the Expected Value as a Boolean
	 *
	 * @return Boolean
	 */
	Boolean getExpectedAsBoolean() {
		if (expected.getClass() != Boolean.class) {
			throw new InvalidDataException("Trying to get a Boolean object when it is a String.");
		}
		return (Boolean) expected;
	}

	/**
	 * Returns actual as an ArrayList. If the actual value was not stored as an
	 * ArrayList, an Exception will be thrown
	 *
	 * @return ArrayList<String>
	 */
	@SuppressWarnings("unchecked")
	ArrayList<String> getActualAsArrayList() {
		// suppressed the unchecked warning because I am manually verifying the type
		// before casting
		if (isBooleanVP()) {
			throw new InvalidDataException("Trying to get a ArrayList object when it is a Boolean");
		}
		if (!isActualArrayList()) {
			throw new InvalidDataException(
					"Trying to get a ArrayList object for Actual values when it is not an ArrayList");
		}
		return (ArrayList<String>) actual;
	}

	/**
	 * Returns the Actual value for String comparison VerificationPoints
	 *
	 * @return the actual
	 */
	String getActualAsString() {
		if (actual.getClass() != String.class) {
			throw new InvalidDataException(
					"Trying to get a String object when it is not a String (my be Boolean or ArrayList).");
		}
		return (String) actual;
	}

	/**
	 * Returns the Actual Value as a generic Object
	 *
	 * @return the actualValue
	 */
	Object getActualObject() {
		return actual;
	}

	/**
	 * Returns the Actual Value as a Boolean
	 *
	 * @return Boolean value of Actual
	 * @throws InvalidDataException if the Actual value is not a Boolean
	 */
	Boolean getActualAsBoolean() {
		if (actual.getClass() != Boolean.class) {
			throw new InvalidDataException("Trying to get a Boolean object when it is a String.");
		}

		return (Boolean) actual;
	}

	/**
	 * Returns TRUE if the requirement value has been set Returns FALSE if the
	 * requirement value has NOT been set
	 *
	 * @return boolean
	 */
	boolean hasRequirement() {
		return requirement != null;
	}

	/**
	 * Returns the requirement value
	 *
	 * @return the requirement
	 */
	Requirement getRequirement() {
		if (!hasRequirement()) {
			throw new InvalidDataException(
					"Trying to access a NULL valued Requirement. Please make sure a requirement is assigned to the this Verification Point before accessing.");
		}
		return requirement;
	}

	/**
	 * Sets the Requirement
	 *
	 * @param requirement the requirement to set
	 */
	public void setRequirement(Requirement requirement) {
		Objects.requireNonNull(requirement, "Requirement can not be set to a null value");
		this.requirement = requirement;
	}

	/**
	 * Returns TRUE if Verification Point is set to ignore Whitespace Returns FALSE
	 * if the Verification Point will include whitespace in the comparison
	 *
	 * @return the ignoreWhiteSpace
	 */
	boolean isIgnoreWhiteSpace() {
		return ignoreWhiteSpace;
	}

	/**
	 * Set if this Verification Point should ignore whitespace
	 *
	 * @param ignoreWhiteSpace the ignoreWhiteSpace to set
	 */
	public void setIgnoreWhiteSpace(boolean ignoreWhiteSpace) {
		this.ignoreWhiteSpace = ignoreWhiteSpace;
	}

	/**
	 * Returns TRUE if Verification Point is set to ignore Whitespace Returns FALSE
	 * if the Verification Point will include whitespace in the comparison
	 *
	 * @return the ignoreWhiteSpace
	 */
	boolean isCheckAllCaps() {
		return allCaps;
	}

	/**
	 * Set if this Verification Point should ignore whitespace
	 *
	 * @param checkAllCaps the ignoreWhiteSpace to set
	 */
	public void setCheckAllCaps(boolean checkAllCaps) {
		this.allCaps = checkAllCaps;
	}

	/**
	 * Returns TRUE if Verification Point is set to matching using Starts With
	 * Returns FALSE if the Verification Point is NOT set to matching using Starts
	 * With
	 *
	 * @return the startsWith
	 */
	boolean isStartsWith() {
		return startsWith;
	}

	/**
	 * Set if this Verification Point will match using a Starts With comparison
	 *
	 * @param startsWith the startsWith to set
	 */
	public void setStartsWith(boolean startsWith) {
		this.startsWith = startsWith;
	}

	/**
	 * Returns TRUE if Verification Point is set to matching using Contains Returns
	 * FALSE if the Verification Point is NOT set to matching using Contains
	 *
	 * @return the contains
	 */
	boolean isContains() {
		return contains;
	}

	/**
	 * Set if this Verification Point will match using a Contains comparison
	 *
	 * @param contains the contains to set
	 */
	public void setContains(boolean contains) {
		this.contains = contains;
	}
}
