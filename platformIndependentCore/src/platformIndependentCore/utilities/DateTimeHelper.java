package platformIndependentCore.utilities;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Class will defines common Date Time formats to be used with the Date
 * Calculator Hover text over the variables will provide examples of how the
 * date will display for each format
 *
 * Naming convention for these variables. Please do not confuse our naming
 * convention for these predefined formats with the format string convention
 * used to build formats in Java:
 *
 *
 * MONTH1 - Will provide the shortest numeric value for the month (example
 * January will be simply '1', December will be '12') MONTH2 - Will always be a
 * two digit representation for the month (example, January will be '01') MONTH3
 * - Will be a 3 character abbreviation for the Month (example January will be
 * 'Jan') MONTH - The full text value for the month (example: January)
 *
 * DAY1 - Will provide the shortest numeric value for the day (example the first
 * will be simply '1', the 31st will be '31') DAY2 - Will always be a two digit
 * representation for the day (example, the first will be '01')
 *
 * YEAR2 - Will be a digit representation for the year (example 2017 will be
 * '17') YEAR4 - Will be full 4 digit representation of the year (example 2017
 * will be '2017')
 *
 * WD - Will be displayed as the full text value for the day of the week
 * (example 'Tuesday') WD3 - Will be a 3 digit abbreviation for the day of the
 * week (example 'Tue')
 *
 * If the Date is to be separated by '/'s, it will be appended with _SLASHES
 *
 * @author VBAAUSTAYLOL
 *
 */
public class DateTimeHelper {
	/**
	 * Create Annotation to include hover text with description for the requirements
	 */
	@Retention(RetentionPolicy.SOURCE)
	@Target(ElementType.FIELD)
	@interface RequirementDocumentation {
		/** value for hover text */
		String value();
	}

	/**
	 * MONTH1_DAY1_YEAR2_SLASHES: <BR>
	 * '1/1/17' (display for January 1, 2017) <BR>
	 * dateFormatString="M/d/yy"
	 **/
	public final static CoreDateTimeFormat MONTH1_DAY1_YEAR2_SLASHES = new CoreDateTimeFormat("M/d/yy");

	/**
	 * MONTH1_DAY1_YEAR4_SLASHES: <BR>
	 * '1/1/2017' (display for January 1, 2017) <BR>
	 * dateFormatString="M/d/yyyy"
	 **/
	public final static CoreDateTimeFormat MONTH1_DAY1_YEAR4_SLASHES = new CoreDateTimeFormat("M/d/yyyy");

	/**
	 * MONTH2_DAY2_YEAR4_SLASHES (4 digit year, 2 digit month and days): <BR>
	 * '01/01/2017' (display for January 1, 2017) <BR>
	 * dateFormatString="MM/dd/yyyy"
	 **/
	public final static CoreDateTimeFormat MONTH2_DAY2_YEAR4_SLASHES = new CoreDateTimeFormat("MM/dd/yyyy");

	/**
	 * MONTH3_DAY1_YEAR4: <BR>
	 * 'Jan 1, 2017' (display for January 1, 2017) <BR>
	 * dateFormatString="MMM d, yyyy"
	 **/
	public final static CoreDateTimeFormat MONTH3_DAY1_YEAR4 = new CoreDateTimeFormat("MMM d, yyyy");

	/**
	 * MONTH_DAY1_YEAR4: <BR>
	 * 'January 1, 2017' (display for January 1, 2017) <BR>
	 * dateFormatString="MMMM d, yyyy"
	 **/
	public final static CoreDateTimeFormat MONTH_DAY1_YEAR4 = new CoreDateTimeFormat("MMMM d, yyyy");

	/**
	 * WD_MONTH_DAY1_YEAR4: <BR>
	 * 'Tuesday, January 1, 2017' (display for January 1, 2017) <BR>
	 * dateFormatString="EEEE, MMMM d, yyyy"
	 **/
	public final static CoreDateTimeFormat WD_MONTH_DAY1_YEAR4 = new CoreDateTimeFormat("EEEE, MMMM d, yyyy");

	/**
	 * MONTH2_DAY2_YEAR2_SLASHES: <BR>
	 * '01/01/17' (display for January 1, 2017) <BR>
	 * dateFormatString="MM/dd/yy"
	 **/
	public final static CoreDateTimeFormat MONTH2_DAY2_YEAR2_SLASHES = new CoreDateTimeFormat("MM/dd/yy");

	/**
	 * MONTH2_DAY1_YEAR2_SLASHES: <BR>
	 * '01/1/17' (display for January 1, 2017) <BR>
	 * dateFormatString="MM/d/yy"
	 **/
	public final static CoreDateTimeFormat MONTH2_DAY1_YEAR2_SLASHES = new CoreDateTimeFormat("MM/d/yy");

	/**
	 * DAY2_MONTH3_YEAR2: <BR>
	 * '01 Jan 17' (display for January 1, 2017) <BR>
	 * dateFormatString="dd MMM yy"
	 **/
	public final static CoreDateTimeFormat DAY2_MONTH3_YEAR2 = new CoreDateTimeFormat("dd MMM yy");

	/**
	 * DAY2_MONTH3_YEAR4: <BR>
	 * '01 Jan 2017' (display for January 1, 2017) <BR>
	 * dateFormatString="dd MMM yyyy"
	 **/
	public final static CoreDateTimeFormat DAY2_MONTH3_YEAR4 = new CoreDateTimeFormat("dd MMM yyyy");

	/**
	 * MONTH3_YEAR4: <BR>
	 * 'Jan 2017' (display for January 1, 2017) <BR>
	 * dateFormatString="MMM yyyy"
	 **/
	public final static CoreDateTimeFormat MONTH3_YEAR4 = new CoreDateTimeFormat("MMM yyyy");

	/**
	 * YEAR4_MONTH2_DAY2_TIMESTAMP: <BR>
	 * '2017-01-01_140608' (display for January 1, 2017 2:06:08 PM) <BR>
	 * dateFormatString="yyyy-MM-dd_HHmmss"
	 **/
	public final static CoreDateTimeFormat YEAR4_MONTH2_DAY2_TIMESTAMP = new CoreDateTimeFormat("yyyy-MM-dd_HHmmss");

}
