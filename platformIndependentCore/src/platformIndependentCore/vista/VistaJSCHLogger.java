package platformIndependentCore.vista;

/**
 * <b>Name :</b> VistaJSCHLogger.java
 * <p>
 * <b>Generated :</b> Sep 22, 2021
 * <p>
 * <b>Description :</b>
 * <p>
 *
 * @since Sep 22, 2021
 * @author vhahacnienhj
 */
@SuppressWarnings({ "unchecked", "deprecation" })
public class VistaJSCHLogger implements com.jcraft.jsch.Logger {
	/**
	 * log level names
	 */
	@SuppressWarnings("rawtypes")
	static java.util.Hashtable name = new java.util.Hashtable();

	static {
		name.put(new Integer(DEBUG), "DEBUG: ");
		name.put(new Integer(INFO), "INFO: ");
		name.put(new Integer(WARN), "WARN: ");
		name.put(new Integer(ERROR), "ERROR: ");
		name.put(new Integer(FATAL), "FATAL: ");
	}

	@Override
	public boolean isEnabled(int level) {
		return true;
	}

	@Override
	public void log(int level, String message) {
		System.err.print(name.get(new Integer(level)));
		System.err.println(message);
	}
}
