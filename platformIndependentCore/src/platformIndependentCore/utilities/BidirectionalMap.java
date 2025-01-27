package platformIndependentCore.utilities;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class allows operations on the map using either the key or the value
 *
 * @author VBAAUSTAYLOL
 *
 * @param <KeyType>   Type of Keys in this map
 * @param <ValueType> Type of Values in this map
 */
public class BidirectionalMap<KeyType, ValueType> {
	/** Map of Keys to Values */
	private Map<KeyType, ValueType> keyToValueMap = new ConcurrentHashMap<KeyType, ValueType>();
	/** Map of Values to Keys */
	private Map<ValueType, KeyType> valueToKeyMap = new ConcurrentHashMap<ValueType, KeyType>();

	/**
	 * Puts the key/value pair into the map
	 *
	 * @param key   for entry
	 * @param value for entry
	 */
	synchronized public void put(KeyType key, ValueType value) {
		keyToValueMap.put(key, value);
		valueToKeyMap.put(value, key);
	}

	/**
	 * Removes an entry in the map specified by the key
	 *
	 * @param key to remove
	 * @return removedValue
	 */
	synchronized public ValueType removeByKey(KeyType key) {
		ValueType removedValue = keyToValueMap.remove(key);
		valueToKeyMap.remove(removedValue);
		return removedValue;
	}

	/**
	 * Removes an entry in the map specified by the value
	 *
	 * @param value to remove
	 * @return removedKey
	 */
	synchronized public KeyType removeByValue(ValueType value) {
		KeyType removedKey = valueToKeyMap.remove(value);
		keyToValueMap.remove(removedKey);
		return removedKey;
	}

	/**
	 * Checks to see if an entry exists in the map with the specified key
	 *
	 * @param key to check
	 * @return TRUE if map contains key, FALSE if not
	 */
	public boolean containsKey(KeyType key) {
		return keyToValueMap.containsKey(key);
	}

	/**
	 * Checks to see if an entry exists in the map with the specified value
	 *
	 * @param value to check
	 * @return TRUE if map contains value, FALSE if not
	 */
	public boolean containsValue(ValueType value) {
		return keyToValueMap.containsValue(value);
	}

	/**
	 * Method to retrieve the key associated with the specified value
	 *
	 * @param value to get the key for
	 * @return key associated with the value
	 */
	public KeyType getKey(ValueType value) {
		return valueToKeyMap.get(value);
	}

	/**
	 * Method to retrieve the value associated with the specified key
	 *
	 * @param key to get the value for
	 * @return value associated with the key
	 */
	public ValueType get(KeyType key) {
		return keyToValueMap.get(key);
	}

	/**
	 * Will empty the contents of the map
	 */
	public void clear() {
		keyToValueMap.clear();
		valueToKeyMap.clear();
	}

}
