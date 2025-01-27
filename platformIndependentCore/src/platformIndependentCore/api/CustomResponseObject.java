package platformIndependentCore.api;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.InvalidParameterException;
import java.util.Collection;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import platformIndependentCore.exceptions.InvalidStateException;

/**
 * <b>Name :</b> CustomResponseObject.java
 * <p>
 * <b>Generated :</b> Dec 22, 2021
 * <p>
 * <b>Description :</b> This is a class to handle well formed responses that are
 * returned with a status code of 200. This class generates hash maps for an
 * interface that the user can later use to access and edit their returned
 * response data if it is in the form of JSON, JSON Array, or XML.
 * <p>
 *
 * @since Dec 22, 2021
 * @author OITBAYTjoarN
 */
public class CustomResponseObject {
	/** JSON Object instance for handling basic JSON and XML */
	private JSONObject jsonObj = null;
	/** Special Case if we have a JSON Array */
	private JSONArray jsonArr = null;
	/** JSON_STATE - JSON, XML_STATE - XML, JSON_ARRAY_STATE - JSONArray */
	private int state = -XML_STATE;

	/** Constant JSON_STATE */
	private static final int JSON_STATE = 0;
	/** Constant XML_STATE */
	private static final int XML_STATE = 1;
	/** Constant JSON_ARRAY_STATE */
	private static final int JSON_ARRAY_STATE = 2;

	/**
	 * Constructor
	 *
	 * @param response response received from the API call
	 */
	public CustomResponseObject(String response) {
		String formattedResponse = response.trim();
		if (formattedResponse.startsWith("{")) { // JSON
			jsonObj = new JSONObject(formattedResponse);
			state = JSON_STATE;
		} else if (formattedResponse.startsWith("<?xml")) { // XML
			jsonObj = XML.toJSONObject(formattedResponse);
			state = XML_STATE;
		} else if (formattedResponse.startsWith("[")) {
			jsonArr = new JSONArray(formattedResponse);
			state = JSON_ARRAY_STATE;
		} else {
			throw new InvalidParameterException("Cannot handle a response that is not JSON, JSON array, or XML.");
		}
	}

	/**
	 * Verifies if we have a JSON document
	 *
	 * @return boolean
	 */
	public boolean isJson() {
		return state == JSON_STATE;
	}

	/**
	 * Verifies if we have an XML document
	 *
	 * @return boolean
	 */
	public boolean isXML() {
		return state == XML_STATE;
	}

	/**
	 * Verifies if we have an array of JSON
	 *
	 * @return boolean
	 */
	public boolean isJsonArray() {
		return state == JSON_ARRAY_STATE;
	}

	/**
	 * Gets and returns the XML/JSON hashmap we have in store
	 *
	 * @return JSONObject
	 */
	public JSONObject getResponseObject() {
		if (jsonObj == null) {
			throw new InvalidStateException(
					"A response was not initialized here. Response may be in the form of a JSON array.");
		}

		return jsonObj;
	}

	/**
	 * Gets and returns the JSONArray we have in store
	 *
	 * @return JSONArray
	 */
	public JSONArray getJsonArray() {
		if (jsonArr == null) {
			throw new InvalidStateException(
					"JSON Array was not initialized here. Response may be in the form of JSON or XML.");
		}

		return jsonArr;
	}

	/**
	 * Gets and returns an Object value if it is XML or JSON
	 *
	 * @param key key of the Object to return
	 * @return Object
	 */
	public Object get(String key) {
		if (state != JSON_STATE && state != XML_STATE) {
			throw new InvalidStateException("Please specify an index, object is an array.");
		}

		return jsonObj.get(key);
	}

	/**
	 * Gets and returns an Object value if it is XML or JSON
	 *
	 * @param index index of the JSON object we want
	 * @param key   key of the Object to return
	 * @return Object
	 */
	public Object get(int index, String key) {
		if (state != JSON_ARRAY_STATE) {
			throw new InvalidStateException("Response is not an array.");
		}

		JSONObject obj = (JSONObject) jsonArr.get(index);
		return obj.get(key);
	}

	/**
	 * Gets and returns an String value if it is XML or JSON
	 *
	 * @param key key of the Object to return
	 * @return String
	 */
	public String getString(String key) {
		if (state != JSON_STATE && state != XML_STATE) {
			throw new InvalidStateException("Please specify an index, object is an array.");
		}

		return jsonObj.getString(key);
	}

	/**
	 * Gets and returns an Object value if it is XML or JSON
	 *
	 * @param index index of the JSON object we want
	 * @param key   key of the Object to return
	 * @return Object
	 */
	public Object getString(int index, String key) {
		if (state != JSON_ARRAY_STATE) {
			throw new InvalidStateException("Response is not an array.");
		}

		JSONObject obj = (JSONObject) jsonArr.get(index);
		return obj.getString(key);
	}

	/**
	 * Gets and returns an int value if it is XML or JSON
	 *
	 * @param key key of the Object to return
	 * @return int
	 */
	public int getInt(String key) {
		if (state != JSON_STATE && state != XML_STATE) {
			throw new InvalidStateException("Please specify an index, object is an array.");
		}

		return jsonObj.getInt(key);
	}

	/**
	 * Gets and returns an int value if it is XML or JSON
	 *
	 * @param index index of the JSON object we want
	 * @param key   key of the Object to return
	 * @return int
	 */
	public int getInt(int index, String key) {
		if (state != JSON_ARRAY_STATE) {
			throw new InvalidStateException("Response is not an array.");
		}

		JSONObject obj = (JSONObject) jsonArr.get(index);
		return obj.getInt(key);
	}

	/**
	 * Gets and returns a BigDecimal value if it is XML or JSON
	 *
	 * @param key key of the Object to return
	 * @return BigDecimal
	 */
	public BigDecimal getBigDecimal(String key) {
		if (state != JSON_STATE && state != XML_STATE) {
			throw new InvalidStateException("Please specify an index, object is an array.");
		}

		return jsonObj.getBigDecimal(key);
	}

	/**
	 * Gets and returns an BigDecimal value if it is XML or JSON
	 *
	 * @param index index of the JSON object we want
	 * @param key   key of the Object to return
	 * @return BigDecimal
	 */
	public BigDecimal getBigDecimal(int index, String key) {
		if (state != JSON_ARRAY_STATE) {
			throw new InvalidStateException("Response is not an array.");
		}

		JSONObject obj = (JSONObject) jsonArr.get(index);
		return obj.getBigDecimal(key);
	}

	/**
	 * Gets and returns a BigInteger value if it is XML or JSON
	 *
	 * @param key key of the Object to return
	 * @return BigInteger
	 */
	public BigInteger getBigInteger(String key) {
		if (state != JSON_STATE && state != XML_STATE) {
			throw new InvalidStateException("Please specify an index, object is an array.");
		}

		return jsonObj.getBigInteger(key);
	}

	/**
	 * Gets and returns an BigInteger value if it is XML or JSON
	 *
	 * @param index index of the JSON object we want
	 * @param key   key of the Object to return
	 * @return BigInteger
	 */
	public BigInteger getBigInteger(int index, String key) {
		if (state != JSON_ARRAY_STATE) {
			throw new InvalidStateException("Response is not an array.");
		}

		JSONObject obj = (JSONObject) jsonArr.get(index);
		return obj.getBigInteger(key);
	}

	/**
	 * Gets and returns a boolean value if it is XML or JSON
	 *
	 * @param key key of the Object to return
	 * @return boolean
	 */
	public boolean getBoolean(String key) {
		if (state != JSON_STATE && state != XML_STATE) {
			throw new InvalidStateException("Please specify an index, object is an array.");
		}

		return jsonObj.getBoolean(key);
	}

	/**
	 * Gets and returns an boolean value if it is XML or JSON
	 *
	 * @param index index of the JSON object we want
	 * @param key   key of the Object to return
	 * @return boolean
	 */
	public boolean getBoolean(int index, String key) {
		if (state != JSON_ARRAY_STATE) {
			throw new InvalidStateException("Response is not an array.");
		}

		JSONObject obj = (JSONObject) jsonArr.get(index);
		return obj.getBoolean(key);
	}

	/**
	 * Gets and returns a double value if it is XML or JSON
	 *
	 * @param key key of the Object to return
	 * @return double
	 */
	public double getDouble(String key) {
		if (state != JSON_STATE && state != XML_STATE) {
			throw new InvalidStateException("Please specify an index, object is an array.");
		}

		return jsonObj.getDouble(key);
	}

	/**
	 * Gets and returns an double value if it is XML or JSON
	 *
	 * @param index index of the JSON object we want
	 * @param key   key of the Object to return
	 * @return double
	 */
	public double getDouble(int index, String key) {
		if (state != JSON_ARRAY_STATE) {
			throw new InvalidStateException("Response is not an array.");
		}

		JSONObject obj = (JSONObject) jsonArr.get(index);
		return obj.getDouble(key);
	}

	/**
	 * Gets and returns a float value if it is XML or JSON
	 *
	 * @param key key of the Object to return
	 * @return float
	 */
	public float getFloat(String key) {
		if (state != JSON_STATE && state != XML_STATE) {
			throw new InvalidStateException("Please specify an index, object is an array.");
		}

		return jsonObj.getFloat(key);
	}

	/**
	 * Gets and returns an float value if it is XML or JSON
	 *
	 * @param index index of the JSON object we want
	 * @param key   key of the Object to return
	 * @return float
	 */
	public float getFloat(int index, String key) {
		if (state != JSON_ARRAY_STATE) {
			throw new InvalidStateException("Response is not an array.");
		}

		JSONObject obj = (JSONObject) jsonArr.get(index);
		return obj.getFloat(key);
	}

	/**
	 * Gets and returns a JSONArray value if it is XML or JSON
	 *
	 * @param key key of the Object to return
	 * @return JSONArray
	 */
	public JSONArray getJSONArray(String key) {
		if (state != JSON_STATE && state != XML_STATE) {
			throw new InvalidStateException("Please specify an index, object is an array.");
		}

		return jsonObj.getJSONArray(key);
	}

	/**
	 * Gets and returns an JSONArray value if it is XML or JSON
	 *
	 * @param index index of the JSON object we want
	 * @param key   key of the Object to return
	 * @return JSONArray
	 */
	public JSONArray getJSONArray(int index, String key) {
		if (state != JSON_ARRAY_STATE) {
			throw new InvalidStateException("Response is not an array.");
		}

		JSONObject obj = (JSONObject) jsonArr.get(index);
		return obj.getJSONArray(key);
	}

	/**
	 * Gets and returns a JSONObject value if it is XML or JSON
	 *
	 * @param key key of the Object to return
	 * @return JSONObject
	 */
	public JSONObject getJSONObject(String key) {
		if (state != JSON_STATE && state != XML_STATE) {
			throw new InvalidStateException("Please specify an index, object is an array.");
		}

		return jsonObj.getJSONObject(key);
	}

	/**
	 * Gets and returns an JSONObject value if it is XML or JSON
	 *
	 * @param index index of the JSON object we want
	 * @param key   key of the Object to return
	 * @return JSONObject
	 */
	public JSONObject getJSONObject(int index, String key) {
		if (state != JSON_ARRAY_STATE) {
			throw new InvalidStateException("Response is not an array.");
		}

		JSONObject obj = (JSONObject) jsonArr.get(index);
		return obj.getJSONObject(key);
	}

	/**
	 * Gets and returns a long value if it is XML or JSON
	 *
	 * @param key key of the Object to return
	 * @return long
	 */
	public long getLong(String key) {
		if (state != JSON_STATE && state != XML_STATE) {
			throw new InvalidStateException("Please specify an index, object is an array.");
		}

		return jsonObj.getLong(key);
	}

	/**
	 * Gets and returns an long value if it is XML or JSON
	 *
	 * @param index index of the JSON object we want
	 * @param key   key of the Object to return
	 * @return long
	 */
	public long getLong(int index, String key) {
		if (state != JSON_ARRAY_STATE) {
			throw new InvalidStateException("Response is not an array.");
		}

		JSONObject obj = (JSONObject) jsonArr.get(index);
		return obj.getLong(key);
	}

	/**
	 * Gets and returns a Number value if it is XML or JSON
	 *
	 * @param key key of the Object to return
	 * @return Number
	 */
	public Number getNumber(String key) {
		if (state != JSON_STATE && state != XML_STATE) {
			throw new InvalidStateException("Please specify an index, object is an array.");
		}

		return jsonObj.getNumber(key);
	}

	/**
	 * Gets and returns an Number value if it is XML or JSON
	 *
	 * @param index index of the JSON object we want
	 * @param key   key of the Object to return
	 * @return Number
	 */
	public Number getNumber(int index, String key) {
		if (state != JSON_ARRAY_STATE) {
			throw new InvalidStateException("Response is not an array.");
		}

		JSONObject obj = (JSONObject) jsonArr.get(index);
		return obj.getNumber(key);
	}

	/**
	 * Get an array of field names from a JSONObject
	 *
	 * @return String[]
	 */
	public String[] getNames() {
		if (state != JSON_STATE && state != XML_STATE) {
			throw new InvalidStateException("Please specify an index, object is an array.");
		}

		return JSONObject.getNames(jsonObj);
	}

	/**
	 * Get an array of field names from a JSONArray at specific index
	 *
	 * @param index index of the JSON object we want
	 * @return String[]
	 */
	public String[] getNumber(int index) {
		if (state != JSON_ARRAY_STATE) {
			throw new InvalidStateException("Response is not an array.");
		}

		JSONObject obj = (JSONObject) jsonArr.get(index);
		return JSONObject.getNames(obj);
	}

	/**
	 * Puts a boolean value at specific key
	 *
	 * @param key   key of the item to replace
	 * @param value value item to set
	 * @return JSONObject
	 */
	public JSONObject put(String key, boolean value) {
		if (state != JSON_STATE && state != XML_STATE) {
			throw new InvalidStateException("Please specify an index, object is an array.");
		}

		return jsonObj.put(key, value);
	}

	/**
	 * Puts a boolean value at specific key
	 *
	 * @param index index of the JSON array to access
	 * @param key   key of the item to replace
	 * @param value value item to set
	 * @return JSONObject
	 */
	public JSONObject put(int index, String key, boolean value) {
		if (state != JSON_ARRAY_STATE) {
			throw new InvalidStateException("Response is not an array.");
		}

		JSONObject obj = (JSONObject) jsonArr.get(index);
		return obj.put(key, value);
	}

	/**
	 * Puts a Collection value at specific key
	 *
	 * @param key   key of the item to replace
	 * @param value value item to set
	 * @return JSONObject
	 */
	public JSONObject put(String key, Collection<?> value) {
		if (state != JSON_STATE && state != XML_STATE) {
			throw new InvalidStateException("Please specify an index, object is an array.");
		}

		return jsonObj.put(key, value);
	}

	/**
	 * Puts a Collection value at specific key
	 *
	 * @param index index of the JSON array to access
	 * @param key   key of the item to replace
	 * @param value value item to set
	 * @return JSONObject
	 */
	public JSONObject put(int index, String key, Collection<?> value) {
		if (state != JSON_ARRAY_STATE) {
			throw new InvalidStateException("Response is not an array.");
		}

		JSONObject obj = (JSONObject) jsonArr.get(index);
		return obj.put(key, value);
	}

	/**
	 * Puts a double value at specific key
	 *
	 * @param key   key of the item to replace
	 * @param value value item to set
	 * @return JSONObject
	 */
	public JSONObject put(String key, double value) {
		if (state != JSON_STATE && state != XML_STATE) {
			throw new InvalidStateException("Please specify an index, object is an array.");
		}

		return jsonObj.put(key, value);
	}

	/**
	 * Puts a double value at specific key
	 *
	 * @param index index of the JSON array to access
	 * @param key   key of the item to replace
	 * @param value value item to set
	 * @return JSONObject
	 */
	public JSONObject put(int index, String key, double value) {
		if (state != JSON_ARRAY_STATE) {
			throw new InvalidStateException("Response is not an array.");
		}

		JSONObject obj = (JSONObject) jsonArr.get(index);
		return obj.put(key, value);
	}

	/**
	 * Puts a float value at specific key
	 *
	 * @param key   key of the item to replace
	 * @param value value item to set
	 * @return JSONObject
	 */
	public JSONObject put(String key, float value) {
		if (state != JSON_STATE && state != XML_STATE) {
			throw new InvalidStateException("Please specify an index, object is an array.");
		}

		return jsonObj.put(key, value);
	}

	/**
	 * Puts a float value at specific key
	 *
	 * @param index index of the JSON array to access
	 * @param key   key of the item to replace
	 * @param value value item to set
	 * @return JSONObject
	 */
	public JSONObject put(int index, String key, float value) {
		if (state != JSON_ARRAY_STATE) {
			throw new InvalidStateException("Response is not an array.");
		}

		JSONObject obj = (JSONObject) jsonArr.get(index);
		return obj.put(key, value);
	}

	/**
	 * Puts a int value at specific key
	 *
	 * @param key   key of the item to replace
	 * @param value value item to set
	 * @return JSONObject
	 */
	public JSONObject put(String key, int value) {
		if (state != JSON_STATE && state != XML_STATE) {
			throw new InvalidStateException("Please specify an index, object is an array.");
		}

		return jsonObj.put(key, value);
	}

	/**
	 * Puts a int value at specific key
	 *
	 * @param index index of the JSON array to access
	 * @param key   key of the item to replace
	 * @param value value item to set
	 * @return JSONObject
	 */
	public JSONObject put(int index, String key, int value) {
		if (state != JSON_ARRAY_STATE) {
			throw new InvalidStateException("Response is not an array.");
		}

		JSONObject obj = (JSONObject) jsonArr.get(index);
		return obj.put(key, value);
	}

	/**
	 * Puts a long value at specific key
	 *
	 * @param key   key of the item to replace
	 * @param value value item to set
	 * @return JSONObject
	 */
	public JSONObject put(String key, long value) {
		if (state != JSON_STATE && state != XML_STATE) {
			throw new InvalidStateException("Please specify an index, object is an array.");
		}

		return jsonObj.put(key, value);
	}

	/**
	 * Puts a long value at specific key
	 *
	 * @param index index of the JSON array to access
	 * @param key   key of the item to replace
	 * @param value value item to set
	 * @return JSONObject
	 */
	public JSONObject put(int index, String key, long value) {
		if (state != JSON_ARRAY_STATE) {
			throw new InvalidStateException("Response is not an array.");
		}

		JSONObject obj = (JSONObject) jsonArr.get(index);
		return obj.put(key, value);
	}

	/**
	 * Puts a Map value at specific key
	 *
	 * @param key   key of the item to replace
	 * @param value value item to set
	 * @return JSONObject
	 */
	public JSONObject put(String key, Map<?, ?> value) {
		if (state != JSON_STATE && state != XML_STATE) {
			throw new InvalidStateException("Please specify an index, object is an array.");
		}

		return jsonObj.put(key, value);
	}

	/**
	 * Puts a Map value at specific key
	 *
	 * @param index index of the JSON array to access
	 * @param key   key of the item to replace
	 * @param value value item to set
	 * @return JSONObject
	 */
	public JSONObject put(int index, String key, Map<?, ?> value) {
		if (state != JSON_ARRAY_STATE) {
			throw new InvalidStateException("Response is not an array.");
		}

		JSONObject obj = (JSONObject) jsonArr.get(index);
		return obj.put(key, value);
	}

	/**
	 * Puts a Object value at specific key
	 *
	 * @param key   key of the item to replace
	 * @param value value item to set
	 * @return JSONObject
	 */
	public JSONObject put(String key, Object value) {
		if (state != JSON_STATE && state != XML_STATE) {
			throw new InvalidStateException("Please specify an index, object is an array.");
		}

		return jsonObj.put(key, value);
	}

	/**
	 * Puts a Object value at specific key
	 *
	 * @param index index of the JSON array to access
	 * @param key   key of the item to replace
	 * @param value value item to set
	 * @return JSONObject
	 */
	public JSONObject put(int index, String key, Object value) {
		if (state != JSON_ARRAY_STATE) {
			throw new InvalidStateException("Response is not an array.");
		}

		JSONObject obj = (JSONObject) jsonArr.get(index);
		return obj.put(key, value);
	}

	/**
	 * Put a key/value pair in the JSONObject, but only if the key and the value are
	 * both non-null, and only if there is not already a member with that name.
	 *
	 * @param key   key of the item to replace
	 * @param value value item to set
	 * @return JSONObject
	 */
	public JSONObject putOnce(String key, Object value) {
		if (state != JSON_STATE && state != XML_STATE) {
			throw new InvalidStateException("Please specify an index, object is an array.");
		}

		return jsonObj.putOnce(key, value);
	}

	/**
	 * Put a key/value pair in the JSONObject, but only if the key and the value are
	 * both non-null, and only if there is not already a member with that name.
	 *
	 * @param index index of the JSON array to access
	 * @param key   key of the item to replace
	 * @param value value item to set
	 * @return JSONObject
	 */
	public JSONObject putOnce(int index, String key, Object value) {
		if (state != JSON_ARRAY_STATE) {
			throw new InvalidStateException("Response is not an array.");
		}

		JSONObject obj = (JSONObject) jsonArr.get(index);
		return obj.putOnce(key, value);
	}

	/**
	 * Gets and returns a Object from the JSON array at a given index
	 *
	 * @param index index of the JSON object to return
	 * @return Object
	 */
	public Object getObjectAtIndex(int index) {
		if (state != JSON_ARRAY_STATE) {
			throw new InvalidStateException(
					"JSON Array was not initialized here. Response may be in the form of JSON or XML.");
		}

		return jsonArr.get(index);
	}

	/**
	 * Gets and returns a BigDecimal from the JSON array at a given index
	 *
	 * @param index index of the JSON object to return
	 * @return BigDecimal
	 */
	public BigDecimal getBigDecimalAtIndex(int index) {
		if (state != JSON_ARRAY_STATE) {
			throw new InvalidStateException(
					"JSON Array was not initialized here. Response may be in the form of JSON or XML.");
		}

		return jsonArr.getBigDecimal(index);
	}

	/**
	 * Gets and returns a BigInteger from the JSON array at a given index
	 *
	 * @param index index of the JSON object to return
	 * @return BigInteger
	 */
	public BigInteger getBigIntegerAtIndex(int index) {
		if (state != JSON_ARRAY_STATE) {
			throw new InvalidStateException(
					"JSON Array was not initialized here. Response may be in the form of JSON or XML.");
		}

		return jsonArr.getBigInteger(index);
	}

	/**
	 *
	 * Gets and returns a boolean from the JSON array at a given index
	 *
	 * @param index index of the JSON object to return
	 * @return boolean
	 */
	public boolean getBooleanAtIndex(int index) {
		if (state != JSON_ARRAY_STATE) {
			throw new InvalidStateException(
					"JSON Array was not initialized here. Response may be in the form of JSON or XML.");
		}

		return jsonArr.getBoolean(index);
	}

	/**
	 * Gets and returns a double from the JSON array at a given index
	 *
	 * @param index index of the JSON object to return
	 * @return double
	 */
	public double getDoubleAtIndex(int index) {
		if (state != JSON_ARRAY_STATE) {
			throw new InvalidStateException(
					"JSON Array was not initialized here. Response may be in the form of JSON or XML.");
		}

		return jsonArr.getDouble(index);
	}

	/**
	 * Gets and returns a float from the JSON array at a given index
	 *
	 * @param index index of the JSON object to return
	 * @return float
	 */
	public float getFloatAtIndex(int index) {
		if (state != JSON_ARRAY_STATE) {
			throw new InvalidStateException(
					"JSON Array was not initialized here. Response may be in the form of JSON or XML.");
		}

		return jsonArr.getFloat(index);
	}

	/**
	 * Gets and returns a int from the JSON array at a given index
	 *
	 * @param index index of the JSON object to return
	 * @return int
	 */
	public int getIntAtIndex(int index) {
		if (state != JSON_ARRAY_STATE) {
			throw new InvalidStateException(
					"JSON Array was not initialized here. Response may be in the form of JSON or XML.");
		}

		return jsonArr.getInt(index);
	}

	/**
	 * Gets and returns a JSONArray from the JSON array at a given index
	 *
	 * @param index index of the JSON object to return
	 * @return JSONArray
	 */
	public JSONArray getJSONArrayAtIndex(int index) {
		if (state != JSON_ARRAY_STATE) {
			throw new InvalidStateException(
					"JSON Array was not initialized here. Response may be in the form of JSON or XML.");
		}

		return jsonArr.getJSONArray(index);
	}

	/**
	 * Gets and returns a JSONObject from the JSON array at a given index
	 *
	 * @param index index of the JSON object to return
	 * @return JSONObject
	 */
	public JSONObject getJSONObjectAtIndex(int index) {
		if (state != JSON_ARRAY_STATE) {
			throw new InvalidStateException(
					"JSON Array was not initialized here. Response may be in the form of JSON or XML.");
		}

		return jsonArr.getJSONObject(index);
	}

	/**
	 * Gets and returns a long from the JSON array at a given index
	 *
	 * @param index index of the JSON object to return
	 * @return long
	 */
	public long getLongAtIndex(int index) {
		if (state != JSON_ARRAY_STATE) {
			throw new InvalidStateException(
					"JSON Array was not initialized here. Response may be in the form of JSON or XML.");
		}

		return jsonArr.getLong(index);
	}

	/**
	 * Gets and returns a Number from the JSON array at a given index
	 *
	 * @param index index of the JSON object to return
	 * @return Number
	 */
	public Number getNumberAtIndex(int index) {
		if (state != JSON_ARRAY_STATE) {
			throw new InvalidStateException(
					"JSON Array was not initialized here. Response may be in the form of JSON or XML.");
		}

		return jsonArr.getNumber(index);
	}

	/**
	 * Gets and returns a String from the JSON array at a given index
	 *
	 * @param index index of the JSON object to return
	 * @return String
	 */
	public String getStringAtIndex(int index) {
		if (state != JSON_ARRAY_STATE) {
			throw new InvalidStateException(
					"JSON Array was not initialized here. Response may be in the form of JSON or XML.");
		}

		return jsonArr.getString(index);
	}

	/**
	 * A wrapper function to traverse the XML object and return an Object
	 *
	 * @param keyPath Argument to specify the path of the key we want
	 * @return Object
	 */
	public Object getXMLResponseValue(String... keyPath) {
		if (state != XML_STATE) {
			throw new InvalidStateException("Current response is not an XML response.");
		}

		Object obj = null;

		// Traverse the path
		JSONObject temp = jsonObj;
		for (int i = 0; i < keyPath.length - 1; i++) {
			temp = temp.getJSONObject(keyPath[i]);
		}

		// Write the object as needed
		obj = temp.get(keyPath[keyPath.length - 1]);

		return obj;
	}

	/**
	 * A wrapper function to traverse the XML object and return a BigDecimal
	 *
	 * @param keyPath Argument to specify the path of the key we want
	 * @return BigDecimal
	 */
	public BigDecimal getXMLResponseValueBigDecimal(String... keyPath) {
		if (state != XML_STATE) {
			throw new InvalidStateException("Current response is not an XML response.");
		}

		BigDecimal obj = null;

		// Traverse the path
		JSONObject temp = jsonObj;
		for (int i = 0; i < keyPath.length - 1; i++) {
			temp = temp.getJSONObject(keyPath[i]);
		}

		// Write the object as needed
		obj = temp.getBigDecimal(keyPath[keyPath.length - 1]);

		return obj;
	}

	/**
	 * A wrapper function to traverse the XML object and return a BigInteger
	 *
	 * @param keyPath Argument to specify the path of the key we want
	 * @return BigInteger
	 */
	public BigInteger getXMLResponseValueBigInteger(String... keyPath) {
		if (state != XML_STATE) {
			throw new InvalidStateException("Current response is not an XML response.");
		}

		BigInteger obj = null;

		// Traverse the path
		JSONObject temp = jsonObj;
		for (int i = 0; i < keyPath.length - 1; i++) {
			temp = temp.getJSONObject(keyPath[i]);
		}

		// Write the object as needed
		obj = temp.getBigInteger(keyPath[keyPath.length - 1]);

		return obj;
	}

	/**
	 * A wrapper function to traverse the XML object and return a boolean
	 *
	 * @param keyPath Argument to specify the path of the key we want. If none
	 *                specified, it returns the section we ask for
	 * @return boolean
	 */
	public boolean getXMLResponseValueBoolean(String... keyPath) {
		if (state != XML_STATE) {
			throw new InvalidStateException("Current response is not an XML response.");
		}

		boolean obj;

		// Traverse the path
		JSONObject temp = jsonObj;
		for (int i = 0; i < keyPath.length - 1; i++) {
			temp = temp.getJSONObject(keyPath[i]);
		}

		// Write the object as needed
		obj = temp.getBoolean(keyPath[keyPath.length - 1]);

		return obj;
	}

	/**
	 * A wrapper function to traverse the XML object and return a double
	 *
	 * @param keyPath Argument to specify the path of the key we want.
	 * @return double
	 */
	public double getXMLResponseValueDouble(String... keyPath) {
		if (state != XML_STATE) {
			throw new InvalidStateException("Current response is not an XML response.");
		}

		double obj;

		// Traverse the path
		JSONObject temp = jsonObj;
		for (int i = 0; i < keyPath.length - 1; i++) {
			temp = temp.getJSONObject(keyPath[i]);
		}

		// Write the object as needed
		obj = temp.getDouble(keyPath[keyPath.length - 1]);

		return obj;
	}

	/**
	 * A wrapper function to traverse the XML object and return a float
	 *
	 * @param keyPath Argument to specify the path of the key we want.
	 * @return float
	 */
	public float getXMLResponseValueFloat(String... keyPath) {
		if (state != XML_STATE) {
			throw new InvalidStateException("Current response is not an XML response.");
		}

		float obj;

		// Traverse the path
		JSONObject temp = jsonObj;
		for (int i = 0; i < keyPath.length - 1; i++) {
			temp = temp.getJSONObject(keyPath[i]);
		}

		// Write the object as needed
		obj = temp.getFloat(keyPath[keyPath.length - 1]);

		return obj;
	}

	/**
	 * A wrapper function to traverse the XML object and return a int
	 *
	 * @param keyPath Argument to specify the path of the key we want.
	 * @return int
	 */
	public int getXMLResponseValueInt(String... keyPath) {
		if (state != XML_STATE) {
			throw new InvalidStateException("Current response is not an XML response.");
		}

		int obj;

		// Traverse the path
		JSONObject temp = jsonObj;
		for (int i = 0; i < keyPath.length - 1; i++) {
			temp = temp.getJSONObject(keyPath[i]);
		}

		// Write the object as needed
		obj = temp.getInt(keyPath[keyPath.length - 1]);

		return obj;
	}

	/**
	 * A wrapper function to traverse the XML object and return a JSONArray
	 *
	 * @param keyPath Argument to specify the path of the key we want.
	 * @return JSONArray
	 */
	public JSONArray getXMLResponseValueJSONArray(String... keyPath) {
		if (state != XML_STATE) {
			throw new InvalidStateException("Current response is not an XML response.");
		}

		JSONArray obj;

		// Traverse the path
		JSONObject temp = jsonObj;
		for (int i = 0; i < keyPath.length - 1; i++) {
			temp = temp.getJSONObject(keyPath[i]);
		}

		// Write the object as needed
		obj = temp.getJSONArray(keyPath[keyPath.length - 1]);

		return obj;
	}

	/**
	 * A wrapper function to traverse the XML object and return a JSONObject
	 *
	 * @param keyPath Argument to specify the path of the key we want
	 * @return JSONObject
	 */
	public JSONObject getXMLResponseValueJSONObject(String... keyPath) {
		if (state != XML_STATE) {
			throw new InvalidStateException("Current response is not an XML response.");
		}

		JSONObject obj;

		// Traverse the path
		JSONObject temp = jsonObj;
		for (int i = 0; i < keyPath.length - 1; i++) {
			temp = temp.getJSONObject(keyPath[i]);
		}

		// Write the object as needed
		obj = temp.getJSONObject(keyPath[keyPath.length - 1]);

		return obj;
	}

	/**
	 * A wrapper function to traverse the XML object and return a long
	 *
	 * @param keyPath Argument to specify the path of the key we want
	 * @return long
	 */
	public long getXMLResponseValueLong(String... keyPath) {
		if (state != XML_STATE) {
			throw new InvalidStateException("Current response is not an XML response.");
		}

		long obj;

		// Traverse the path
		JSONObject temp = jsonObj;
		for (int i = 0; i < keyPath.length - 1; i++) {
			temp = temp.getJSONObject(keyPath[i]);
		}

		// Write the object as needed
		obj = temp.getLong(keyPath[keyPath.length - 1]);

		return obj;
	}

	/**
	 * A wrapper function to traverse the XML object and return a Number
	 *
	 * @param keyPath Argument to specify the path of the key we want.
	 * @return long
	 */
	public Number getXMLResponseValueNumber(String... keyPath) {
		if (state != XML_STATE) {
			throw new InvalidStateException("Current response is not an XML response.");
		}

		Number obj;

		JSONObject temp = jsonObj;
		for (int i = 0; i < keyPath.length - 1; i++) {
			temp = temp.getJSONObject(keyPath[i]);
		}

		// Write the object as needed
		obj = temp.getNumber(keyPath[keyPath.length - 1]);

		return obj;
	}

	/**
	 * A wrapper function to traverse the XML object and return a String
	 *
	 * @param keyPath Argument to specify the path of the key we want.
	 * @return String
	 */
	public String getXMLResponseValueString(String... keyPath) {
		if (state != XML_STATE) {
			throw new InvalidStateException("Current response is not an XML response.");
		}

		String obj;

		// Traverse the path
		JSONObject temp = jsonObj;
		for (int i = 0; i < keyPath.length - 1; i++) {
			temp = temp.getJSONObject(keyPath[i]);
		}

		// Write the object as needed
		obj = temp.getString(keyPath[keyPath.length - 1]);

		return obj;
	}

	/**
	 * A wrapper function to traverse the XML object and return a keys available for
	 * the given object
	 *
	 * @param keyPath Argument to specify the path of the key we want.
	 * @return String[]
	 */
	public String[] getXMLResponseNames(String... keyPath) {
		if (state != XML_STATE) {
			throw new InvalidStateException("Current response is not an XML response.");
		}

		String[] obj;

		// Traverse the path
		JSONObject temp = jsonObj;
		for (int i = 0; i < keyPath.length; i++) {
			temp = temp.getJSONObject(keyPath[i]);
		}

		// Write the object as needed
		obj = JSONObject.getNames(temp);

		return obj;
	}

	/**
	 * Puts a new object or edits a current object for an XML response value
	 *
	 * @param key   key to associate item with on get
	 * @param value value to put item
	 * @param path  Path to put item in
	 * @return JSONObject
	 */
	public JSONObject putXMLResponseValue(String key, boolean value, String... path) {
		if (state != XML_STATE) {
			throw new InvalidStateException("Current response is not an XML response.");
		}

		// Traverse the path
		JSONObject temp = jsonObj;
		for (String loc : path) {
			temp = temp.getJSONObject(loc);
		}

		temp.put(key, value);

		return jsonObj;
	}

	/**
	 * Puts a new object or edits a current object for an XML response value
	 *
	 * @param key   key to associate item with on get
	 * @param value value to put item
	 * @param path  Path to put item in
	 * @return JSONObject
	 */
	public JSONObject putXMLResponseValue(String key, Collection<?> value, String... path) {
		if (state != XML_STATE) {
			throw new InvalidStateException("Current response is not an XML response.");
		}

		// Traverse the path
		JSONObject temp = jsonObj;
		for (String loc : path) {
			temp = temp.getJSONObject(loc);
		}

		temp.put(key, value);

		return jsonObj;
	}

	/**
	 * Puts a new object or edits a current object for an XML response value
	 *
	 * @param key   key to associate item with on get
	 * @param value value to put item
	 * @param path  Path to put item in
	 * @return JSONObject
	 */
	public JSONObject putXMLResponseValue(String key, double value, String... path) {
		if (state != XML_STATE) {
			throw new InvalidStateException("Current response is not an XML response.");
		}

		// Traverse the path
		JSONObject temp = jsonObj;
		for (String loc : path) {
			temp = temp.getJSONObject(loc);
		}

		temp.put(key, value);

		return jsonObj;
	}

	/**
	 * Puts a new object or edits a current object for an XML response value
	 *
	 * @param key   key to associate item with on get
	 * @param value value to put item
	 * @param path  Path to put item in
	 * @return JSONObject
	 */
	public JSONObject putXMLResponseValue(String key, float value, String... path) {
		if (state != XML_STATE) {
			throw new InvalidStateException("Current response is not an XML response.");
		}

		// Traverse the path
		JSONObject temp = jsonObj;
		for (String loc : path) {
			temp = temp.getJSONObject(loc);
		}

		temp.put(key, value);

		return jsonObj;
	}

	/**
	 * Puts a new object or edits a current object for an XML response value
	 *
	 * @param key   key to associate item with on get
	 * @param value value to put item
	 * @param path  Path to put item in
	 * @return JSONObject
	 */
	public JSONObject putXMLResponseValue(String key, int value, String... path) {
		if (state != XML_STATE) {
			throw new InvalidStateException("Current response is not an XML response.");
		}

		// Traverse the path
		JSONObject temp = jsonObj;
		for (String loc : path) {
			temp = temp.getJSONObject(loc);
		}

		temp.put(key, value);

		return jsonObj;
	}

	/**
	 * Puts a new object or edits a current object for an XML response value
	 *
	 * @param key   key to associate item with on get
	 * @param value value to put item
	 * @param path  Path to put item in
	 * @return JSONObject
	 */
	public JSONObject putXMLResponseValue(String key, long value, String... path) {
		if (state != XML_STATE) {
			throw new InvalidStateException("Current response is not an XML response.");
		}

		// Traverse the path
		JSONObject temp = jsonObj;
		for (String loc : path) {
			temp = temp.getJSONObject(loc);
		}

		temp.put(key, value);

		return jsonObj;
	}

	/**
	 * Puts a new object or edits a current object for an XML response value
	 *
	 * @param key   key to associate item with on get
	 * @param value value to put item
	 * @param path  Path to put item in
	 * @return JSONObject
	 */
	public JSONObject putXMLResponseValue(String key, Map<?, ?> value, String... path) {
		if (state != XML_STATE) {
			throw new InvalidStateException("Current response is not an XML response.");
		}

		// Traverse the path
		JSONObject temp = jsonObj;
		for (String loc : path) {
			temp = temp.getJSONObject(loc);
		}

		temp.put(key, value);

		return jsonObj;
	}

	/**
	 * Puts a new object or edits a current object for an XML response value
	 *
	 * @param key   key to associate item with on get
	 * @param value value to put item
	 * @param path  Path to put item in
	 * @return JSONObject
	 */
	public JSONObject putXMLResponseValue(String key, Object value, String... path) {
		if (state != XML_STATE) {
			throw new InvalidStateException("Current response is not an XML response.");
		}

		// Traverse the path
		JSONObject temp = jsonObj;
		for (String loc : path) {
			temp = temp.getJSONObject(loc);
		}

		temp.put(key, value);

		return jsonObj;
	}

	/**
	 * Put a key/value pair in the JSONObject, but only if the key and the value are
	 * both non-null, and only if there is not already a member with that name.
	 *
	 * @param key   key to associate item with on get
	 * @param value value to put item
	 * @param path  Path to put item in
	 * @return JSONObject
	 */
	public JSONObject putOnceXMLResponseValue(String key, Object value, String... path) {
		if (state != XML_STATE) {
			throw new InvalidStateException("Current response is not an XML response.");
		}

		// Traverse the path
		JSONObject temp = jsonObj;
		for (String loc : path) {
			temp = temp.getJSONObject(loc);
		}

		jsonObj.putOnce(key, value);

		return jsonObj;
	}

	/**
	 * Returns a string instance of the Response in store
	 *
	 * @return String
	 */
	@Override
	public String toString() {
		if (state == JSON_STATE) {
			return jsonObj.toString();
		} else if (state == JSON_ARRAY_STATE) {
			return jsonArr.toString();
		} else if (state == XML_STATE) {
			return XML.toString(jsonObj);
		} else {
			throw new InvalidStateException("An unexpected state is set in Custom Response Object.");
		}
	}

	/**
	 * Returns a string instance of the JSON Object/Array in store
	 *
	 * @param indent indenting to use for the toString() function
	 * @return String
	 */
	public String toString(int indent) {
		if (state == JSON_STATE) {
			return jsonObj.toString(indent);
		} else if (state == JSON_ARRAY_STATE) {
			return jsonArr.toString(indent);
		} else if (state == XML_STATE) {
			return getPrettyString(XML.toString(jsonObj), indent);
		} else {
			throw new InvalidStateException("An unexpected state is set in Custom Response Object.");
		}
	}

	/**
	 * Helper function to format XML
	 *
	 * @param xmlData xml data unformatted
	 * @param indent  indent size of the
	 * @return String
	 */
	private String getPrettyString(String xmlData, int indent) {
		try {
			// Turn xml string into a document
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
					.parse(new InputSource(new ByteArrayInputStream(xmlData.getBytes("utf-8"))));

			// Remove whitespaces outside tags
			document.normalize();
			XPath xPath = XPathFactory.newInstance().newXPath();
			NodeList nodeList = (NodeList) xPath.evaluate("//text()[normalize-space()='']", document,
					XPathConstants.NODESET);

			for (int i = 0; i < nodeList.getLength(); ++i) {
				Node node = nodeList.item(i);
				node.getParentNode().removeChild(node);
			}

			// Setup pretty print options
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			transformerFactory.setAttribute("indent-number", indent);
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", Integer.toString(indent));

			// Return pretty print xml string
			StringWriter stringWriter = new StringWriter();
			transformer.transform(new DOMSource(document), new StreamResult(stringWriter));
			return stringWriter.toString();
		} catch (Exception e) {
			throw new RuntimeException(e); // simple exception handling, please review it
		}
	}

	/**
	 * Clears and resets all values
	 */
	public void clear() {
		jsonArr.clear();
		jsonObj.clear();
	}
}
