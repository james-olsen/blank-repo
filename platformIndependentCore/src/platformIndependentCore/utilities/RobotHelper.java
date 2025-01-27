package platformIndependentCore.utilities;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/**
 * Class to simplify using Robot. Contains methods that bundle commands for some
 * common actions with Robot
 *
 * @author VBAAUSTAYLOL
 *
 */
public class RobotHelper {
	/**
	 * Use as a last default when we can't interact with the Automated Objects for
	 * some reason, or there is a non-web based pop up.
	 *
	 * @param value to send to the screen
	 */
	public static void sendKeysWithRobot(String value) {
		try {
			Robot robot = new Robot();
			StringSelection stringSelection = new StringSelection(value);
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			clipboard.setContents(stringSelection, stringSelection);
			robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
			robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);

			robot.keyPress(KeyEvent.VK_CONTROL);
			robot.keyPress(KeyEvent.VK_V);
			robot.keyRelease(KeyEvent.VK_V);
			robot.keyRelease(KeyEvent.VK_CONTROL);
			Thread.sleep(300);

		} catch (AWTException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Use as a last default when we can't interact with the Automated Objects for
	 * some reason, or there is a non-web based pop up.
	 */
	public static void sendEnter() {
		try {
			Robot robot = new Robot();
			robot.keyPress(KeyEvent.VK_ENTER);
			robot.keyRelease(KeyEvent.VK_ENTER);
			Thread.sleep(300);
		} catch (AWTException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Use as a last default when we can't interact with the Automated Objects for
	 * some reason, or there is a non-web based pop up.
	 */
	public static void sendTab() {
		try {
			Robot robot = new Robot();
			robot.keyPress(KeyEvent.VK_TAB);
			robot.keyRelease(KeyEvent.VK_TAB);
			Thread.sleep(300);
		} catch (AWTException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Will use Robot to send a click to the left mouse button
	 */
	public static void leftMouseClick() {
		try {
			Robot robot = new Robot();
			robot.mousePress(InputEvent.BUTTON1_MASK);
			Thread.sleep(300);
		} catch (AWTException e1) {
			e1.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Will perform a right mouse click
	 */
	public static void rightMouseClick() {
		try {
			Robot robot = new Robot();
			robot.mousePress(InputEvent.BUTTON3_MASK);
			Thread.sleep(300);
		} catch (AWTException e1) {
			e1.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
