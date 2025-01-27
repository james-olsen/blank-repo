package platformIndependentCore.core;

/**
 * Class for interacting with a Browser window
 *
 * @author VBAAUSTAYLOL
 *
 */
public interface BrowserObject {

	/**
	 * Will maximize the browser
	 */
	public abstract void maximize();

	/**
	 * Will refresh the browser
	 */
	public abstract void refresh();

	/**
	 * Will load the specified URL
	 *
	 * @param url to load
	 */
	public abstract void loadUrl(String url);

	/**
	 * Returns the AutomatedObject for this browser
	 *
	 * @return AutomatedObject for this browser
	 */
	public abstract AutomatedObject getAutomatedObject();

	/**
	 * Returns the current URL for the browser
	 *
	 * @return String current url
	 */
	public abstract String getCurrentUrl();

	/**
	 * Will close the browser
	 */
	public abstract void close();

}
