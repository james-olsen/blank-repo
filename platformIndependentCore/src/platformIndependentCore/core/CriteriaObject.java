package platformIndependentCore.core;

/**
 * Representation for a criteria condition used for searching objects
 *
 * @author VBAAUSTAYLOL
 *
 */
public abstract class CriteriaObject extends ToolManager {
	/** Will hold a reference to the testing platform specific criteria */
	protected Object criteria;
	/** Type of criteria */
	protected String criteriaType = "";
	/** Value of criteria */
	protected Object criteriaValue = "";
	/** boolean determines if regular expression criteria exists */
	protected boolean regex = false;
	/** Type of Regular Expression for criteria */
	protected REGEX regexSetting = REGEX.NO_REGEX;

	/**
	 * Will return the REGEX setting for the criteria
	 *
	 * @return REGEX type of Regular Expression
	 */
	public REGEX getRegexSetting() {
		return regexSetting;
	}

	/**
	 * Will set the REGEX for this criteria
	 *
	 * @param regexSetting REGEX to use in criteria
	 */
	public void setRegexSetting(REGEX regexSetting) {
		this.regexSetting = regexSetting;
	}

	/**
	 * Enum to define types of regex
	 * 
	 * @author VBAAUSTAYLOL
	 *
	 */
	public enum REGEX {
		/** STARTS-WITH REGEX */
		STARTS_WITH("starts-with"),
		/** CONTAINS REGEX */
		CONTAINS("contains"),
		/** NO REGEX */
		NO_REGEX(""),
		/** DATE FORMAT COMPARE */
		DATE("date format"),
		/**
		 * NORMALIZE-SPACE strips leading and trailing white-space from a string,
		 * replaces sequences of whitespace characters by a single space, and returns
		 * the resulting string
		 */
		NORMALIZE_SPACE("normalize-space");

		/** xpath value */
		private String xpath;

		/**
		 * constructor takes in xpath value
		 *
		 * @param xpath xpath to be paired with REGEX
		 */
		private REGEX(String xpath) {
			this.xpath = xpath;
		}

		@Override
		public String toString() {
			return xpath;
		}
	}

	/**
	 * Will create an instance for the specified criteria type and value specific
	 * for the current testing platform
	 *
	 * @param criteriaType  type
	 * @param criteriaValue value
	 * @return CriteriaObject criteria
	 */
	public static CriteriaObject createInstance(String criteriaType, Object criteriaValue) {
		return getAutomationTool().createCriteria(criteriaType, criteriaValue);
	}

	/**
	 * Will create an instance for the specified criteria type and value specific
	 * for the current testing platform
	 *
	 * @param criteriaType  type
	 * @param criteriaValue value
	 * @param regexSetting  regex type
	 * @return CriteriaObject criteria
	 */
	public static CriteriaObject createRegexInstance(String criteriaType, String criteriaValue, REGEX regexSetting) {
		return getAutomationTool().createCriteriaRegex(criteriaType, criteriaValue);
	}

	/**
	 * Returns the type
	 *
	 * @return String criteria type
	 */
	public String getCriteriaType() {
		return criteriaType;
	}

	/**
	 * Returns the value for this criteria
	 *
	 * @return Object criteria value
	 */
	public Object getCriteriaValue() {
		return criteriaValue;
	}

	/**
	 * Will return the testing platform specific criteria object
	 *
	 * @return Object criteria
	 */
	@Deprecated
	public Object getCriteria() {
		return criteria;
	}

}
