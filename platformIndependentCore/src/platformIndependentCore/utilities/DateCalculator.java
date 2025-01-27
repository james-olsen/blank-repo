package platformIndependentCore.utilities;

import java.util.Calendar;
import java.util.Date;

/**
 * Class used to get Current Date and also adjust the date by specified
 * increments
 *
 * @author VBAAUSTAYLOL
 *
 */
public class DateCalculator {

	/**
	 * Will return the current date in the specified format
	 *
	 * @param dateFormatString format to return current Date in
	 * @return String current date formatted as specified
	 */
	public static String getCurrentDate(String dateFormatString) {
		return getCurrentDate(new CoreDateTimeFormat(dateFormatString));
	}

	/**
	 * Will return the current date in the specified format
	 *
	 * @param coreDateTimeFormat to use on current date
	 * @return String formatted current date using provided format
	 */
	public static String getCurrentDate(CoreDateTimeFormat coreDateTimeFormat) {
		Date currentDate = new Date();
		return coreDateTimeFormat.format(currentDate);
	}

	/**
	 * Will adjust the specified date by the number of days (can be positive or
	 * negative). Returns a String representation of the new date, following the
	 * specified format
	 *
	 * Your originalDate *MUST* follow the same format as specified by
	 * coreDateTimeFormat
	 *
	 * @param coreDateTimeFormat desired format
	 * @param originalDate       must follow same format as provided
	 *                           coreDateTimeFormat
	 * @param numberOfDays       to adjust the original date by (can be positive or
	 *                           negative)
	 * @return String resulting date after adjustment, using provided format
	 */
	public static String adjustDateByDays(CoreDateTimeFormat coreDateTimeFormat, String originalDate,
			int numberOfDays) {
		return adjustDate(coreDateTimeFormat, originalDate, Calendar.DAY_OF_MONTH, numberOfDays);
	}

	/**
	 * Will adjust the specified date by the number of days (can be positive or
	 * negative). Returns a String representation of the new date, following the
	 * specified format
	 *
	 * Your originalDate *MUST* follow the same format as specified by
	 * dateFormatString
	 *
	 * @param dateFormatString desired format
	 * @param originalDate     must follow same format as provided
	 *                         coreDateTimeFormat
	 * @param numberOfDays     to adjust the original date by (can be positive or
	 *                         negative)
	 * @return String resulting date after adjustment, using provided format
	 */
	public static String adjustDateByDays(String dateFormatString, String originalDate, int numberOfDays) {
		return adjustDate(new CoreDateTimeFormat(dateFormatString), originalDate, Calendar.DAY_OF_MONTH, numberOfDays);
	}

	/**
	 * Will adjust the specified date by the number of months (can be positive or
	 * negative). Returns a String representation of the new date, following the
	 * specified format
	 *
	 * Your originalDate *MUST* follow the same format as specified by
	 * coreDateTimeFormat
	 *
	 * @param coreDateTimeFormat desired format
	 * @param originalDate       must follow same format as provided
	 *                           coreDateTimeFormat
	 * @param numberOfMonths     to adjust the original date by (can be positive or
	 *                           negative)
	 * @return String resulting date after adjustment, using provided format
	 */
	public static String adjustDateByMonths(CoreDateTimeFormat coreDateTimeFormat, String originalDate,
			int numberOfMonths) {
		return adjustDate(coreDateTimeFormat, originalDate, Calendar.MONTH, numberOfMonths);
	}

	/**
	 * Will adjust the specified date by the number of months (can be positive or
	 * negative). Returns a String representation of the new date, following the
	 * specified format
	 *
	 * Your originalDate *MUST* follow the same format as specified by
	 * dateFormatString
	 *
	 * @param dateFormatString desired format
	 * @param originalDate     must follow same format as provided
	 *                         coreDateTimeFormat
	 * @param numberOfMonths   to adjust the original date by (can be positive or
	 *                         negative)
	 * @return String resulting date after adjustment, using provided format
	 */
	public static String adjustDateByMonths(String dateFormatString, String originalDate, int numberOfMonths) {
		return adjustDate(new CoreDateTimeFormat(dateFormatString), originalDate, Calendar.MONTH, numberOfMonths);
	}

	/**
	 * Will adjust the specified date by the number of years (can be positive or
	 * negative). Returns a String representation of the new date, following the
	 * specified format
	 *
	 * Your originalDate *MUST* follow the same format as specified by
	 * coreDateTimeFormat
	 *
	 * @param coreDateTimeFormat desired format
	 * @param originalDate       must follow same format as provided
	 *                           coreDateTimeFormat
	 * @param numberOfYears      to adjust the original date by (can be positive or
	 *                           negative)
	 * @return String resulting date after adjustment, using provided format
	 */
	public static String adjustDateByYears(CoreDateTimeFormat coreDateTimeFormat, String originalDate,
			int numberOfYears) {
		return adjustDate(coreDateTimeFormat, originalDate, Calendar.YEAR, numberOfYears);
	}

	/**
	 * Will adjust the specified date by the number of years (can be positive or
	 * negative). Returns a String representation of the new date, following the
	 * specified format
	 *
	 * Your originalDate *MUST* follow the same format as specified by
	 * dateFormatString
	 *
	 * @param dateFormatString desired format
	 * @param originalDate     must follow same format as provided
	 *                         coreDateTimeFormat
	 * @param numberOfYears    to adjust the original date by (can be positive or
	 *                         negative)
	 * @return String resulting date after adjustment, using provided format
	 */
	public static String adjustDateByYears(String dateFormatString, String originalDate, int numberOfYears) {
		return adjustDate(new CoreDateTimeFormat(dateFormatString), originalDate, Calendar.YEAR, numberOfYears);
	}

	/**
	 * Will adjust the specified date by the specified calendar fields and
	 * adjustment value (can be positive or negative). Returns a String
	 * representation of the new date, following the specified format
	 *
	 * Your originalDate *MUST* follow the same format as specified by
	 * coreDateTimeFormat
	 *
	 * @param coreDateTimeFormat desired format
	 * @param originalDate       must follow same format as provided
	 *                           coreDateTimeFormat
	 * @param calendarField      specifies unit to be adjusted (DAYS, MONTHS, YEARS)
	 * @param adjustment         to be made to the original date by (can be positive
	 *                           or negative)
	 * @return String resulting date after adjustment, using provided format
	 */
	private static String adjustDate(CoreDateTimeFormat coreDateTimeFormat, String originalDate, int calendarField,
			int adjustment) {
		// Create a calendar object
		Calendar c = Calendar.getInstance();
		// set to original date
		c.setTime(coreDateTimeFormat.getDate(originalDate));
		// make the adjustment
		c.add(calendarField, adjustment);

		return coreDateTimeFormat.format(c.getTime());
	}

	/**
	 * Will verify if the date provided can be parsed using the Date Format Will
	 * return true if it can be successfully parsed and the provided date matches
	 * the parsed date, will return false if a ParseException will be thrown or if
	 * the provided date does not match the formatted date
	 *
	 * @param dateFormatString desired format
	 * @param dateToTestString to verify is in desired format
	 * @return boolean true if the provided date matches desired format, false it
	 *         not
	 */
	public static boolean isFormatCorrect(String dateFormatString, String dateToTestString) {
		CoreDateTimeFormat coreDateTimeFormat = new CoreDateTimeFormat(dateFormatString);

		return coreDateTimeFormat.verifyDateFormat(dateToTestString);
	}

	/**
	 * Will verify if the date provided can be parsed using the Date Format Will
	 * return true if it can be successfully parsed and the provided date matches
	 * the parsed date, will return false if a ParseException will be thrown or if
	 * the provided date does not match the formatted date
	 *
	 * @param coreDateTimeFormat desired format
	 * @param dateToTestString   to verify is in desired format
	 * @return boolean true if the provided date matches desired format, false it
	 *         not
	 */
	public static boolean isFormatCorrect(CoreDateTimeFormat coreDateTimeFormat, String dateToTestString) {
		return coreDateTimeFormat.verifyDateFormat(dateToTestString);
	}

	/**
	 * Will verify if the date provided can be parsed using the Date Format Will
	 * return true if it can be successfully parsed and the provided date matches
	 * the parsed date, will return false if a ParseException will be thrown or if
	 * the provided date does not match the formatted date. Allows a boolean to be
	 * passed in to indicate if case sensitive.
	 *
	 * @param coreDateTimeFormat desired format
	 * @param dateToTestString   to verify is in desired format
	 * @param caseSensitive      true if case matters, false if not
	 * @return boolean true if the provided date matches desired format, false it
	 *         not
	 */
	public static boolean isFormatCorrect(CoreDateTimeFormat coreDateTimeFormat, String dateToTestString,
			boolean caseSensitive) {
		return coreDateTimeFormat.verifyDateFormat(dateToTestString, caseSensitive);
	}

}
