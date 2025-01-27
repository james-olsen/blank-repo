package platformIndependentCore.scripts;

import java.util.HashMap;

import platformIndependentCore.exceptions.InvalidDataException;

/**
 * Class to use for Arguments for test scripts Arguments are stored as KEY,
 * VALUE pairs
 *
 * @author vbaaustaylol
 *
 */
public class Arguments {
	/** Constant for RESULTS_FOLDER argument */
	static final String RESULTS_FOLDER = "RESULTS_FOLDER";
	/** map of key/value arguments where key is a String, and value is an Object */
	private HashMap<String, Object> map = new HashMap<String, Object>();

	/**
	 * Will set the argument for KEY to VALUE
	 *
	 * @param key   the key whose associated value is to be stored
	 * @param value to be associated with the key
	 */
	public void set(String key, Object value) {
		// Always store in ALL CAPS
		map.put(key.toUpperCase(), value);
	}

	/**
	 * Will return the VALUE for the given KEY
	 *
	 * @param key the value whose associated value is to be returned
	 * @return Object value associated with the key
	 * @throws InvalidDataException if KEY is not found
	 */
	public Object get(String key) {
		// Keys are always stored in ALL CAPS, get the same way
		key = key.toUpperCase();
		if (!map.containsKey(key)) {
			// If the key is not found, throw an Exception
			throw new InvalidDataException("Unable to locate an Argument with KEY='" + key
					+ "' (Please note that KEY search is NOT case sensitive)");
		}
		return map.get(key);
	}

	/**
	 * Will check if the key exists in current set of arguments. TRUE if it does,
	 * FALSE if not
	 *
	 * @param key The key whose presence in this Arguments set is to be tested
	 * @return boolean true if this Arguments object contains a mapping for the
	 *         specified key
	 */
	public boolean containsKey(String key) {
		// Keys are always converted to ALL CAPS
		return map.containsKey(key.toUpperCase());
	}

	/**
	 * Will return the VALUE for KEY as a String
	 *
	 * @param key the value whose associated value is to be returned
	 * @return String value of Object associated with the key
	 * @throws InvalidDataException if KEY is not found
	 */
	public String getString(String key) {
		return (String) get(key);
	}

	/**
	 * Will return the VALUE for KEY as a Boolean
	 *
	 * @param key the value whose associated value is to be returned
	 * @return boolean value of Object associated with the key
	 * @throws InvalidDataException if KEY is not found
	 */
	public boolean getBoolean(String key) {
		return (boolean) get(key);
	}

	/**
	 * Will check if the Arguments is EMPTY
	 *
	 * @return boolean TRUE if EMPTY, FALSE is it has values
	 */
	public boolean isEmpty() {
		return map.isEmpty();
	}

	// @Deprecated
	// public HashMap<String, Object> getArgs(){
	// return map;
	// }

}
