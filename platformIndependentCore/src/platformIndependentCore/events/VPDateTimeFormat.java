package platformIndependentCore.events;

import java.text.ParseException;

import platformIndependentCore.utilities.CoreDateTimeFormat;

/**
 * VPDateTimeFormat extends CoreDateTimeFormat and provides a standard Date/Time
 * format for use with Verification Points
 *
 * @author VBAAUSTAYLOL
 *
 */
class VPDateTimeFormat extends CoreDateTimeFormat {
	/**
	 * Constructor passes the format String to CoreDateTimeFormat constructor
	 *
	 * @param formatString specifies date time format
	 */
	VPDateTimeFormat(String formatString) {
		super(formatString);
	}

	/**
	 * Constructor passes the format to CoreDateTimeFormat constructor
	 *
	 * @param format specifies date time format
	 */
	VPDateTimeFormat(CoreDateTimeFormat format) {
		super(format.toString());
	}

	@Override
	public String format(String dateString) throws ParseException {
		return simpleDateFormat.format(simpleDateFormat.parse(dateString));
	}

}
