package platformIndependentCore.utilities;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import platformIndependentCore.exceptions.InvalidDataException;

/**
 * Class for storing a format for DateTime. When passing an int to the
 * constructor, you should use the defined formats from Calendar
 *
 * (ex. new DateFormat(Calendar.SHORT) )
 *
 * When using the String, please use the standard abbreviations for date
 * formatting
 *
 * This class is used to wrap constructors that take the Calendar formats as
 * they take an int. This class is only to be used internally within the Core
 * Forcing users to use our DateTime class with static predefined instances of
 * formats
 *
 * @author VBAAUSTAYLOL
 *
 */
public class CoreDateTimeFormat {
	/** SimpleDateFormat instance */
	protected SimpleDateFormat simpleDateFormat;
	/** logger for this class */
	Logger log = LogManager.getLogger(CoreDateTimeFormat.class.getName());

	/**
	 * When using the constructor, you should use the defined formats from Calendar
	 *
	 * (ex. new DateFormat(Calendar.SHORT) )
	 *
	 * @param calendarFormat from {Calendar}
	 */
	public CoreDateTimeFormat(int calendarFormat) {
		simpleDateFormat = (SimpleDateFormat) DateFormat.getDateInstance(calendarFormat);
	}

	/**
	 * Constructor takes in a String representation of the desired format
	 *
	 *
	 * @param formatString representing desired format
	 */
	public CoreDateTimeFormat(String formatString) {
		simpleDateFormat = new SimpleDateFormat(formatString);
	}

	/**
	 * Formats the specified date String to this format, will throw a ParseException
	 * if unable to format
	 *
	 * @param dateString to format
	 * @return String the formatted date
	 * @throws ParseException of the dateString is a different format
	 */
	public String format(String dateString) throws ParseException {
		return simpleDateFormat.format(simpleDateFormat.parse(dateString));
	}

	/**
	 * Formats the specified date
	 *
	 * @param date to be formatted
	 * @return String value of date, formatted
	 */
	public String format(Date date) {
		return simpleDateFormat.format(date);
	}

	/**
	 * Returns a Date object representing the specified String using the format
	 *
	 * @param dateString to create Date object using current format
	 * @return Date object representing specified String
	 */
	public Date getDate(String dateString) {
		Date date;
		try {
			date = simpleDateFormat.parse(dateString);
		} catch (ParseException e) {
			// the original date does not follow the specified format
			e.printStackTrace();
			throw new InvalidDataException("Error parsing your date value (" + dateString
					+ ") It must follow the same format as specified in your dateFormat " + toString());
		}
		return date;
	}

	/**
	 * Will verify if the date provided can be parsed using the Date Format Will
	 * return true if it can be successfully parsed and the provided date matches
	 * the parsed date, will return false if a ParseException will be thrown or if
	 * the provided date does not match the formatted date
	 *
	 * @param dateToTestString to verify format
	 * @return boolean true if desired format is verified for provided String, false
	 *         if not
	 */
	public boolean verifyDateFormat(String dateToTestString) {

		return verifyDateFormat(dateToTestString, true);
	}

	/**
	 * Will verify if the date provided can be parsed using the Date Format Will
	 * return true if it can be successfully parsed and the provided date matches
	 * the parsed date, will return false if a ParseException will be thrown or if
	 * the provided date does not match the formatted date. Allows a boolean to be
	 * passed in to indicate if case sensitive.
	 *
	 * @param dateToTestString to verify format on
	 * @param caseSensitive    true if case matters, false if not
	 * @return boolean true if desired format is verified for provided String, false
	 *         if not
	 */
	public boolean verifyDateFormat(String dateToTestString, boolean caseSensitive) {
		boolean formatCorrect = false;

		try {
			// We want to be strict with the format here during validation
			simpleDateFormat.setLenient(false);
			Date parsedDate = simpleDateFormat.parse(dateToTestString);

			// String originalValue = dateString;
			// After successfully parsing the string with the format
			// also generate a new formatted string and compare.
			String formattedDateString = simpleDateFormat.format(parsedDate);

			if (!caseSensitive) {
				dateToTestString = dateToTestString.toUpperCase();
				formattedDateString = formattedDateString.toUpperCase();
			}

			if (dateToTestString.equals(formattedDateString)) {
				formatCorrect = true;
			} else {
				// TODO = Can not log to our results file from here
				log.debug("EXPECTED: " + formattedDateString);
				log.debug("ACTUAL: " + dateToTestString);
			}

		} catch (ParseException e) {
			// the original date does not follow the specified format
			formatCorrect = false;
		}
		return formatCorrect;
	}

	/**
	 * Returns the String pattern representing this format
	 *
	 * @return String pattern representing this format
	 */
	@Override
	public String toString() {

		return simpleDateFormat.toPattern();
	}

}
