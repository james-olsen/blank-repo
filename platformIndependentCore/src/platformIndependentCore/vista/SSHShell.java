package platformIndependentCore.vista;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.oro.text.regex.MalformedPatternException;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import expect4j.Closure;
import expect4j.Expect4j;
import expect4j.ExpectState;
import expect4j.matches.Match;
import expect4j.matches.RegExpMatch;

/**
 * <b>Name :</b> SSHShell.java
 * <p>
 * <b>Generated :</b> Sep 21, 2021
 * <p>
 * <b>Description :</b> Utility class to run commands on Linux VM via SSH.
 * Modified from SSHShell.java in
 * https://github.com/Azure/azure-libraries-for-java which was developed under
 * the MIT License
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Microsoft
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * <p>
 *
 * @since Sep 21, 2021
 * @author vhahacnienhj
 */
public class SSHShell {
	/**
	 * our connection to vista
	 */

	private final Session session;
	/**
	 * Channel shell for
	 */

	private final ChannelShell channel;
	/**
	 * Expect object to be inserted in the io stream to/from vista.
	 */
	private final Expect4j expect;

	/**
	 * buffer for storing input from the vista shell connection.
	 */
	private final StringBuilder shellBuffer = new StringBuilder();

	/** logger instance for this class */
	private static Logger log = LogManager.getLogger(SSHShell.class.getName());

	/** String buffer for last 24 lines */
	StringBuilder strBuf = new StringBuilder();
//	private static CircularFifoQueue<String> screenQueue = new CircularFifoQueue<String>(24);

	/**
	 * Creates SSHShell.
	 *
	 * @param host     the host name of the SSH server
	 * @param port     the SSH port
	 * @param userName the vista database user name
	 * @param password the vista database password
	 * @param pKey     the SSH server public key
	 *
	 * @throws JSchException Java secure channel ran into a problem
	 * @throws IOException   a general io exception
	 */
	private SSHShell(String host, int port, String userName, String password, String pKey)
			throws JSchException, IOException {

		// what is going on with loggers?
		JSch jsch = new JSch();
		JSch.setLogger(new VistaJSCHLogger());

		// store host and host's public key in known host file before connecting to
		// prevent "do you trust this host?" and "always trust this host?" prompts.
		String knownHost = host + " ssh-rsa " + pKey;
		jsch.setKnownHosts(new ByteArrayInputStream(knownHost.getBytes()));

		// make a secure socket connection to the host
		this.session = jsch.getSession(userName, host, port);
		session.setPassword(password);
		Hashtable<String, String> config = new Hashtable<>();
		config.put("StrictHostKeyChecking", "no");
		config.put("PreferredAuthentications", "publickey,keyboard-interactive,password");
		session.setConfig(config);
		session.connect(60000); // handles the public private key exchange

		// create a shell channel through the socket connection and insert our expect
		this.channel = (ChannelShell) session.openChannel("shell");
		this.expect = new Expect4j(channel.getInputStream(), channel.getOutputStream());

		// start the terminal session
		channel.connect();
		log.debug("connected to VistA!");

	}

	/**
	 * Opens a SSH shell.
	 *
	 * @param host     the host name
	 * @param port     the ssh port
	 * @param userName the ssh user name
	 * @param password the ssh password
	 * @param key      the ssh public key
	 *
	 * @return the shell
	 *
	 * @throws JSchException exception thrown
	 * @throws IOException   IO exception thrown
	 */
	protected static SSHShell open2(String host, int port, String userName, String password, String key)
			throws JSchException, IOException {
		return new SSHShell(host, port, userName, password, key);
	}

	/**
	 * @param expectString string for which to wait to come across the connection
	 *                     before the command is issued.
	 * @param command      command to issue.
	 * @param waitTime     wait time out in milliseconds.
	 *
	 * @return true if the expected string was found and command sent, else false
	 */
	protected boolean runCommand(String expectString, String command, Long waitTime) {
		String output = null;
		List<Match> matches = new ArrayList<>();

		// if command is a access/verify code which is sensitive
		if (expectString.contains("VERIFY CODE:") || expectString.contains("ACCESS CODE:")) {
			log.debug("Waiting to send command '" + command.replaceAll(".", "*") + "' at expected prompt '"
					+ expectString + "'" + ", wait is " + waitTime + " msec");
		} else {
			log.debug("Waiting to send command '" + command + "' at expected prompt '" + expectString + "'"
					+ ", wait is " + waitTime + " msec");
		}

		try {

			// set up matching and check for match
			synchronized (matches) {
				try {
					Match match = new RegExpMatch(expectString, getExpectClosure());
					matches.add(match);
				} catch (MalformedPatternException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			expect.setDefaultTimeout(waitTime);
			int result = expect.expect(matches);
			output = shellBuffer.toString();

			// if match result is error (-2 is timeout, -1
			if (result < 0) {
				log.error("Expected String '" + expectString + "' was not found found! Result = " + result);
				log.debug("\n------->>>>>-------------POST FAILED WAIT INPUT SHELL BUFFER: -------------\n" + output
						+ "\n----------------->>>>-END SHELL BUFFER -------------------------------\n");
				return false;
			}

			log.debug("Expected string '" + expectString + "' matched on '" + expect.getLastState().getMatch()
					+ "'! Result = " + result);
//			log.debug("\n------->>>>>-------------PRE COMMAND INPUT SHELL BUFFER: -------------\n" + output
//					+ "\n----------------->>>>-END SHELL BUFFER -------------------------------\n");

			// if command is a access/verify code which is sensitive
			if (expectString.contains("VERIFY CODE:") || expectString.contains("ACCESS CODE:")) {
				// mask verify code
				log.info("----> SENDING COMMAND: " + command.replaceAll(".", "*"));
			} else {
				log.info("----> SENDING COMMAND: " + command);
			}
			expect.send(command);
			Thread.sleep(300L);
			expect.send("\r");
			return true;
		} catch (MalformedPatternException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			shellBuffer.setLength(0);
		}
		return false;
	}

	/**
	 * sends command to vista
	 *
	 * @param command command to issue.
	 *
	 * @return true if command sent, else false
	 */
	protected boolean runCommand(String command) {
		try {
			log.info("----> SENDING COMMAND: " + command);
			expect.send(command);
			Thread.sleep(300L);
			expect.send("\r");
		} catch (IOException | InterruptedException e) {
			log.error(e.getMessage());
			return false;
		}
		return true;
	}

	/**
	 * Pushes enter after the expected string appears.
	 *
	 * @param expectString string for which to wait to come across the connection
	 *                     before pushing enter.
	 * @param waitTime     wait time out in milliseconds.
	 *
	 * @return true if the expected string was found and command sent, else false
	 */

	protected boolean pushEnter(String expectString, Long waitTime) {
		String output = null;
		List<Match> matches = new ArrayList<>();
		int result = -1;

		try {
			log.debug("Waiting to push 'Enter' at expected prompt '" + expectString + "'" + ", wait is " + waitTime
					+ " msec");
			// set up the match and wait
			Match match = new RegExpMatch(expectString, getExpectClosure());
			matches.add(match);
			expect.setDefaultTimeout(waitTime);
			result = expect.expect(matches);
			output = shellBuffer.toString();

			// if match result is error (-2 is timeout, -1
			if (result < 0) {
				log.error("Expected String '" + expectString + "' was not found found! Result = " + result);
				log.debug("------->>>>>-------------POST FAILED WAIT INPUT SHELL BUFFER: -------------\n" + output
						+ "\n----------------->>>>-END SHELL BUFFER -------------------------------\n");
				return false;
			}

			// log the result
			log.debug("Expected string '" + expectString + "' matched on '" + expect.getLastState().getMatch()
					+ "'! Result = " + result);
//			log.debug("------->>>>>-------------PRE <CR> INPUT SHELL BUFFER: -------------\n" + output
//					+ "\n----------------->>>>-END SHELL BUFFER -------------------------------\n");

			// hit the return key
			log.debug("----> SENDING COMMAND: <CR>");
			expect.send("\r");
			return true;
		} catch (MalformedPatternException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			shellBuffer.setLength(0);
		}
		return false;

	}

	/**
	 * Pushes enter.
	 *
	 * @return true if command sent, else false
	 */
	protected boolean pushEnter() {
		String output = null;

		try {
			log.info("----> SENDING COMMAND: <CR>");
			expect.send("\r");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			shellBuffer.setLength(0);
		}
		return false;
	}

	/**
	 * checks if an expected string is present in the output
	 *
	 * @param expectString string to search for in output
	 * @param waitTime     wait time out in milliseconds.
	 *
	 * @return true if expected string appeared before the wait time expired, else
	 *         false
	 */
	protected boolean waitForString(String expectString, Long waitTime) {
		boolean matchFound = false;
		String output = null;
		String expectMatch = null;
		List<Match> matches = new ArrayList<>();
		int result = -1;

		try {
			log.debug("Waiting for expected prompt '" + expectString + "'" + ", wait is " + waitTime + " msec");
			Match match = new RegExpMatch(expectString, getExpectClosure());
			matches.add(match);
			expect.setDefaultTimeout(waitTime);
			result = expect.expect(matches);
			output = shellBuffer.toString();

			// if match result is error (-2 is timeout, -1
			if (result < 0) {
				log.error("Expected String '" + expectString + "' was not found found!");
				log.debug("------->>>>>-------------POST FAILED WAIT INPUT SHELL BUFFER: -------------\n" + output
						+ "\n----------------->>>>-END SHELL BUFFER -------------------------------\n");
			} else {
				expectMatch = expect.getLastState().getMatch();
				log.info("Expected string '" + expectString + "' matched on '" + expectMatch + "'! Result = " + result);
//				log.debug("------->>>>>-------------Matching INPUT SHELL BUFFER: -------------\n" + output
//						+ "\n----------------->>>>-END SHELL BUFFER -------------------------------\n");
				matchFound = true;
			}

		} catch (MalformedPatternException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			shellBuffer.setLength(0);
		}

		return matchFound;
	}

	/**
	 * checks if an expected string is present in the output
	 *
	 * @param promptArray array of strings to search for in output
	 * @param waitTime    wait time out in milliseconds.
	 *
	 * @return the promptArray index of first matched string
	 */
	protected int waitForStrings(ArrayList<String> promptArray, Long waitTime) {
		String output = null;
		String expectString = null;
		String expectMatch = null;
		List<Match> matches = new ArrayList<>();
		int result = -1;

		try {
			log.debug("Waiting for expected prompts, wait is " + waitTime + " msec");
			for (String prompt : promptArray) {
				Match match = new RegExpMatch(prompt, getExpectClosure());
				matches.add(match);
			}
			expect.setDefaultTimeout(waitTime);
			result = expect.expect(matches);
			output = shellBuffer.toString();

			// if match result is error (-2 is timeout, -1
			if (result < 0) {
				log.error("None of the expected strings matched! Result = " + result);
				log.debug("------->>>>>-------------POST FAILED MATCH INPUT SHELL BUFFER: --------------\n" + output
						+ "\n----------------->>>>-END SHELL BUFFER -------------------------------\n");
			} else {
				// get the expectedString that matched and the input string that matched
				expectString = promptArray.get(result);
				expectMatch = expect.getLastState().getMatch();
				log.debug(
						"Expected string '" + expectString + "' matched on '" + expectMatch + "'! Result = " + result);
				// log the matching string and the output at the time of the match
//				log.debug("------->>>>>-------------Matching INPUT SHELL BUFFER: --------------\n" + output
//						+ "\n----------------->>>>-END SHELL BUFFER -------------------------------\n");
			}

		} catch (MalformedPatternException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			shellBuffer.setLength(0);
		}

		return result;
	}

	/**
	 * Executes a command on the remote host.
	 *
	 * @param command       the command to be executed
	 * @param getExitStatus return the exit status captured in the stdout
	 * @param withErr       capture the stderr as part of the output
	 *
	 * @return the content of the remote output from executing the command
	 *
	 * @throws Exception exception thrown
	 */
	private String executeCommand(String command, Boolean getExitStatus, Boolean withErr) throws Exception {
		String result = "";
		String resultErr = "";

		Channel channel = this.session.openChannel("exec");
		((ChannelExec) channel).setCommand(command);
		InputStream commandOutput = channel.getInputStream();
		InputStream commandErr = ((ChannelExec) channel).getErrStream();
		channel.connect();
		byte[] tmp = new byte[4096];
		while (true) {
			while (commandOutput.available() > 0) {
				int i = commandOutput.read(tmp, 0, 4096);
				if (i < 0) {
					break;
				}
				result += new String(tmp, 0, i);
			}
			while (commandErr.available() > 0) {
				int i = commandErr.read(tmp, 0, 4096);
				if (i < 0) {
					break;
				}
				resultErr += new String(tmp, 0, i);
			}
			if (channel.isClosed()) {
				if (commandOutput.available() > 0) {
					continue;
				}
				if (getExitStatus) {
					result += "exit-status: " + channel.getExitStatus();
					if (withErr) {
						result += "\n With error:\n" + resultErr;
					}
				}
				break;
			}
			try {
				Thread.sleep(100);
			} catch (Exception ee) {
			}
		}
		channel.disconnect();

		return result;
	}

	/**
	 * Gets and returns the text on the screen buffer
	 *
	 * @return String
	 */
	protected ArrayList<String> getScreenText() {
		if (strBuf.toString().isEmpty()) {
			return new ArrayList<String>();
		}
		ArrayList<String> arrList = new ArrayList<String>(Arrays.asList(strBuf.toString().split("\r\n")));

		return arrList;
	}

	/**
	 * Closes shell.
	 */
	protected void close() {
		log.debug("closing shell");
		if (expect != null) {
			expect.close();
			log.debug("expect closed");
		}
		if (channel != null) {
			channel.disconnect();
			log.debug("channel closed");
		}
		if (session != null) {
			session.disconnect();
			log.debug("session closed");
		}
	}

	/**
	 * @return closure
	 */
	private Closure getExpectClosure() {
		return new Closure() {
			@Override
			public void run(ExpectState expectState) throws Exception {
				String outputBuffer = expectState.getBuffer();
				log.info(outputBuffer);
				shellBuffer.append(outputBuffer);
				addToScreen(shellBuffer.toString());
				// expectState.exp_continue();
			}
		};
	}

	/**
	 * Parses through the current screen buffer and saves it into our screenQueue
	 *
	 * @param buffer buffer of text to parse through
	 */
	private void addToScreen(String buffer) {
		// non-printable, i.e.: not 0-9, a-Z, \t, a blank space, see
		// https://stackoverflow.com/questions/52559031/remove-non-printable-character-from-a-string-in-java
		// Remove unprintable chars
		String str = buffer.replaceAll("[^\\x00-\\xFF]", "");

		// Append all lines
		strBuf.append(str);

		// Format the lines properly first
		List<String> lines = Arrays.asList(strBuf.toString().split("\r\n"));
		lines = new ArrayList<String>(lines.subList(Math.max(0, lines.size() - 24), lines.size()));
		strBuf.setLength(0);
		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);

			// Only append newline if not the last line
			if (i < lines.size() - 1) {
				strBuf.append(line + "\r\n");
			} else {
				strBuf.append(line);
			}
		}
	}
}
