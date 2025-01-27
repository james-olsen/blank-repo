package platformIndependentCore.vista;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jcraft.jsch.JSchException;

import platformIndependentCore.exceptions.InvalidDataException;
import platformIndependentCore.exceptions.InvalidStateException;
import platformIndependentCore.utilities.ConfigProperties;

/**
 * <b>Name :</b> VistaManager.java
 * <p>
 * <b>Generated :</b> Sep 22, 2021
 * <p>
 * <b>Description : </b> Manages connection to Vista terminal server. Provides
 * methods for interacting with the Vista. The script must first call
 * ConnectAndLoginToVista before making any other calls. After the script is
 * finished running commands on the vista and before the script closes, the
 * script should make one final call to disconnectVista.
 * <p>
 *
 * @since Sep 22, 2021
 * @author vhahacnienhj
 */
public class VistaManager {

	/**
	 * Default number of milliseconds to wait for an expected string or prompt
	 * string.
	 */
	public static Long defaultWaitTime = 10000L;

	/**
	 * Our one and only reference to the connection
	 */
	private static SSHShell sshShell = null;

	/** logger instance for this class */
	private static Logger log = LogManager.getLogger(VistaManager.class.getName());

//	/**
//	 * Connects to the test vista instance and logs into vista with the supplied
//	 * access and verify code.
//	 *
//	 * The connection info for the vista should be specified in config.properties
//	 *
//	 * @param accessCode vista user access code
//	 * @param verifyCode vista user verify code
//	 */
//	public void ConnectAndLoginToVista(String accessCode, String verifyCode) {
//		if (sshShell == null) {
//			try {
//				String host = ConfigProperties.getValue("VISTA_A_HOST_NAME", "");
//				String port = ConfigProperties.getValue("VISTA_A_PORT", "");
//				String key = ConfigProperties.getValue("VISTA_A_SSH_RSA_KEY", "");
//				String username = ConfigProperties.getValue("VISTA_A_USERNAME", "");
//				String password = ConfigProperties.getValue("VISTA_A_PASSWORD", "");
//
//				// open connection to vista
//				sshShell = SSHShell.open2(host, Integer.parseInt(port), username, password, key);
//
//				// enter access and verify code
//				log.debug("waiting for access code prompt and entering");
//				sshShell.runCommand("ACCESS CODE:", accessCode, defaultWaitTime);
//				log.debug("waiting for verify code prompt and entering");
//				sshShell.runCommand("VERIFY CODE:", verifyCode, defaultWaitTime);
//			} catch (JSchException e) {
//				e.printStackTrace();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//	}

	/**
	 * Connects to the test vista instance
	 */
	public void connect() {
		if (sshShell == null) {
			String host = ConfigProperties.getValue("VISTA_A_HOST_NAME", "");
			String port = ConfigProperties.getValue("VISTA_A_PORT", "");
			String key = ConfigProperties.getValue("VISTA_A_SSH_RSA_KEY", "");
			String username = ConfigProperties.getValue("VISTA_A_USERNAME", "");
			String password = ConfigProperties.getValue("VISTA_A_PASSWORD", "");

			if (host.isEmpty() || port.isEmpty() || key.isEmpty() || username.isEmpty() || password.isEmpty()) {
				throw new InvalidDataException(
						"One or more vista connection properties in config.properties are not set. "
								+ "Please check the following properties: VISTA_A_HOST_NAME, VISTA_A_PORT, "
								+ "VISTA_A_SSH_RSA_KEY, VISTA_A_USERNAME, VISTA_A_PASSWORD");
			}

			try {
				// open connection to vista
				sshShell = SSHShell.open2(host, Integer.parseInt(port), username, password, key);
			} catch (JSchException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Waits up to default wait time for the specified string to appear on the Vista
	 * screen.
	 *
	 * @param expectedString The text (usually a prompt for input) for which the
	 *                       method should wait to appear on the vista screen before
	 *                       returning.
	 *
	 * @return boolean True if the expected string appeared on the screen before the
	 *         default wait time expires, false if not.
	 */
	public boolean waitForString(String expectedString) {
		if (sshShell == null) {
			throw new InvalidStateException(
					"vista is not initialized. ConnectAndLoginToVista must be called before calling this method");
		}
		return sshShell.waitForString(expectedString, defaultWaitTime);
	}

	/**
	 * Waits up to specified wait time for the specified string to appear on the
	 * Vista screen.
	 *
	 * @param expectedString The text (usually a prompt for input) for which the
	 *                       method should wait to appear on the vista screen before
	 *                       returning.
	 * @param waitTime       The number of milliseconds to wait for the expected
	 *                       string.
	 *
	 * @return boolean True if the expected string appeared on the screen before the
	 *         specified wait time expires, false if not.
	 */
	public boolean waitForString(String expectedString, Long waitTime) {
		if (sshShell == null) {
			throw new InvalidStateException(
					"vista is not initialized. ConnectAndLoginToVista must be called before calling this method");
		}
		return sshShell.waitForString(expectedString, waitTime);
	}

	/**
	 * Waits up to specified wait time for the specified string to appear on the
	 * Vista screen.
	 *
	 * @param promptArray List of strings (usually a list of prompts for input) for
	 *                    which the method should wait to appear on the vista screen
	 *                    before returning.
	 * @param waitTime    The number of milliseconds to wait for the expected
	 *                    strings.
	 *
	 * @return int Either the index of the matching string in promptArray, or a
	 *         nnegative number indicating an error.
	 *
	 *         Error codes: -1: general error, -2: timeout error, -3: EOF error
	 */
	public int waitForStrings(ArrayList<String> promptArray, Long waitTime) {
		if (sshShell == null) {
			throw new InvalidStateException(
					"vista is not initialized. ConnectAndLoginToVista must be called before calling this method");
		}
		return sshShell.waitForStrings(promptArray, waitTime);
	}

	/**
	 * Waits up to default wait time for the specified string to appear on the Vista
	 * screen and then transmits the specified command to Vista.
	 *
	 * @param expectedString The text (usually a prompt for input) for which the
	 *                       method should wait to appear on the vista screen before
	 *                       transmitting the command.
	 * @param command        The command to be transmitted to Vista.
	 *
	 * @return True if the expected string was found and the command was transmitted
	 */
	public boolean runCommand(String expectedString, String command) {
		if (sshShell == null) {
			throw new InvalidStateException(
					"vista is not initialized. ConnectAndLoginToVista must be called before calling this method");
		}
		return sshShell.runCommand(expectedString, command, defaultWaitTime);

	}

	/**
	 * Sends the specified command to Vista.
	 *
	 * @param command The command to be sent to Vista.
	 *
	 * @return True if command was sent
	 */
	public boolean runCommand(String command) {
		if (sshShell == null) {
			throw new InvalidStateException(
					"vista is not initialized. ConnectAndLoginToVista must be called before calling this method");
		}
		return sshShell.runCommand(command);

	}

	/**
	 * Waits up to specified wait time for the specified string to appear on the
	 * Vista screen and then transmits the specified command to Vista.
	 *
	 * @param expectedString The text (usually a prompt for input) for which the
	 *                       method should wait to appear on the vista screen before
	 *                       transmitting the command.
	 * @param command        The command to be transmitted to Vista.
	 * @param waitTime       Number of milliseconds to wait for the expected string.
	 *
	 * @return True if the expected string was found and the command was transmitted
	 */
	public boolean runCommand(String expectedString, String command, Long waitTime) {
		if (sshShell == null) {
			throw new InvalidStateException(
					"vista is not initialized. ConnectAndLoginToVista must be called before calling this method");
		}
		return sshShell.runCommand(expectedString, command, waitTime);
	}

	/**
	 * Transmits the return key to vista.
	 */
	public void pushEnter() {
		if (sshShell == null) {
			throw new InvalidStateException(
					"vista is not initialized. ConnectAndLoginToVista must be called before calling this method");
		}
		sshShell.pushEnter();
	}

	/**
	 * Waits up to default wait time for the specified string to appear on the Vista
	 * screen and then transmits return key to vista.
	 *
	 * @param expectedString The text (usually a prompt for input) for which the
	 *                       method should wait to appear on the vista screen before
	 *                       transmitting the command.
	 *
	 * @return true if the expected prompt was found and the command was sent, else
	 *         false.
	 */
	public boolean pushEnter(String expectedString) {
		if (sshShell == null) {
			throw new InvalidStateException(
					"vista is not initialized. ConnectAndLoginToVista must be called before calling this method");
		}
		return sshShell.pushEnter(expectedString, defaultWaitTime);
	}

	/**
	 * Waits up to specified wait time for the specified string to appear on the
	 * Vista screen and then transmits return key to vista.
	 *
	 * @param expectedString The text (usually a prompt for input) for which the
	 *                       method should wait to appear on the vista screen before
	 *                       transmitting the command.
	 * @param waitTime       Number of milliseconds to wait for the expected string.
	 *
	 * @return true if the expected prompt was found and the command was sent, else
	 *         false.
	 */
	public boolean pushEnter(String expectedString, Long waitTime) {
		if (sshShell == null) {
			throw new InvalidStateException(
					"vista is not initialized. ConnectAndLoginToVista must be called before calling this method");
		}
		return sshShell.pushEnter(expectedString, waitTime);
	}

	/**
	 * Gets and prints
	 *
	 * @return String
	 */
	public ArrayList<String> getScreenText() {
		if (sshShell == null) {
			throw new InvalidStateException(
					"vista is not initialized. ConnectAndLoginToVista must be called before calling this method");
		}
		return sshShell.getScreenText();
	}

	/**
	 * closes the connection. Be sure to call this when finished with the
	 * connection.
	 */
	public void disconnectVistaShell() {
		if (sshShell != null) {
			sshShell.close();
			sshShell = null;
		}
	}

	/**
	 * Sets the default wait time to the new default wait time
	 *
	 * @param waitTimeMilliSeconds the new default wait time.
	 */
	public void setDefaultWaitTime(Long waitTimeMilliSeconds) {
		defaultWaitTime = waitTimeMilliSeconds;
	}

}
