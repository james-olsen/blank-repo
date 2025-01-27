package platformIndependentCore.results;

import java.io.File;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import platformIndependentCore.events.Event;
import platformIndependentCore.events.Event.EVENT_TYPE;
import platformIndependentCore.events.Note;
import platformIndependentCore.events.ScreenShot;
import platformIndependentCore.events.VerificationPoint;
import platformIndependentCore.scripts.ScriptResults;
import platformIndependentCore.scripts.TestScriptManager;

/**
 * Class used to generate the XML representation of a test run
 *
 * @author VBAAUSTAYLOL
 *
 */
public class SaveAsXML {

	/** Constant: results **/
	final static String RESULTS_TAG = "results";
	/** Constant: suite **/
	final static String SUITE_TAG = "suite";
	/** Constant: name **/
	final static String NAME_TAG = "name";
	/** Constant: starttime **/
	final static String START_TIME_TAG = "starttime";
	/** Constant: endtime **/
	final static String END_TIME_TAG = "endtime";
	/** Constant: duration **/
	final static String DURATION_TAG = "duration";
	/** Constant: calledscript **/
	final static String CALLED_SCRIPT_TAG = "calledscript";
	/** Constant: executionscript **/
	final static String EXECUTION_SCRIPT_TAG = "executionscript";
	/** Constant: testfromsuite **/
	final static String TEST_FROM_SUITE_TAG = "testfromsuite";
	/** Constant: timestamp **/
	final static String TIMESTAMP_TAG = "timestamp";
	/** Constant: screenshot **/
	final static String SCREENSHOT_TAG = "screenshot";
	/** Constant: file **/
	final static String FILE_TAG = "file";
	/** Constant: note **/
	final static String NOTE_TAG = "note";
	/** Constant: exception **/
	final static String NOTE_EXCEPTION_TAG = "exception";
	/** Constant: warning **/
	final static String NOTE_WARNING_TAG = "warning";
	/** Constant: vp **/
	final static String VP_TAG = "vp";
	/** Constant: scriptname **/
	final static String VP_SCRIPT_NAME = "scriptname";
	/** Constant: regex **/
	final static String VP_REGEX_TAG = "regex";
	/** Constant: expected **/
	final static String VP_EXPECTED_TAG = "expected";
	/** Constant: actual **/
	final static String VP_ACTUAL_TAG = "actual";
	/** Constant: vpNote **/
	final static String VP_NOTE_TAG = "vpNote";
	/** Constant: pass **/
	final static String VP_PASS_TAG = "pass";
	/** Constant: status **/
	final static String STATUS_TAG = "status";
	/** Constant: dataid **/
	final static String DATA_ID_TAG = "dataid";
	/** Constant: xray **/
	final static String XRAY_ID_TAG = "xray";
	/** Logger for logging data for this class **/
	static Logger log = LogManager.getLogger(SaveAsXML.class.getName());

	/**
	 * Will save the test script results for the manager to the provided file
	 *
	 * @param file    to save in
	 * @param manager script with results
	 */
	public static void save(File file, TestScriptManager manager) {
		try {
			File folder = new File(file.getParent());
			folder.mkdirs();

			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			// root elements
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement(RESULTS_TAG);
			doc.appendChild(rootElement);

			// Check to see if the results are for a test suite
			if (manager.getScriptResults().isMainSuiteTest()) {
				doc = generateTestSuiteXml(doc, rootElement, manager);
			} else {
				ScriptResults sr = manager.getScriptResults();

				doc = generateExecutionScriptXml(doc, rootElement, sr, manager);
			}

			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(file);
			transformer.transform(source, result);

			log.info("File saved! " + file.getAbsolutePath());
		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (TransformerException tfe) {
			tfe.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Will add the attributes for the script results to the provided element
	 *
	 * @param doc    the document to modify
	 * @param script the script that is being ran
	 * @param sr     the ScriptResults from the script
	 * @return Element the element generated
	 */
	private static Element setScriptElementDetails(Document doc, Element script, ScriptResults sr) {
		Attr name = doc.createAttribute(NAME_TAG);
		name.setValue(sr.getScriptName());
		script.setAttributeNode(name);

		Attr start = doc.createAttribute(START_TIME_TAG);
		start.setValue(sr.getData(ScriptResults.START_TIME));
		script.setAttributeNode(start);

		Attr stop = doc.createAttribute(END_TIME_TAG);
		stop.setValue(sr.getData(ScriptResults.STOP_TIME));
		script.setAttributeNode(stop);

		Attr duration = doc.createAttribute(DURATION_TAG);
		duration.setValue(sr.getData(ScriptResults.DURATION));
		script.setAttributeNode(duration);

		Attr status = doc.createAttribute(STATUS_TAG);
		status.setValue(sr.getStatus());
		script.setAttributeNode(status);

		Attr dataid = doc.createAttribute(DATA_ID_TAG);
		dataid.setValue(sr.getData(ScriptResults.DATA_ID));
		script.setAttributeNode(dataid);

		Attr xray = doc.createAttribute(XRAY_ID_TAG);
		xray.setValue(sr.getData(ScriptResults.XRAY_TEST_ID));
		script.setAttributeNode(xray);

		return script;
	}

	/**
	 * Will add a called script element to the xml doc
	 *
	 * @param doc    the document to edit
	 * @param parent the parent of the event
	 * @param event  the event to add
	 * @return Document the document that has been edited for the added call script
	 */
	private static Document addCalledScript(Document doc, Element parent, Object event) {
		ScriptResults sr = (ScriptResults) event;
		Element calledScript = doc.createElement(CALLED_SCRIPT_TAG);
		calledScript = setScriptElementDetails(doc, calledScript, sr);
		parent.appendChild(calledScript);
		doc = addEvents(doc, calledScript, sr.getEvents());
		return doc;
	}

	/**
	 * Will add XML elements for all the children events provided to the parent
	 * element
	 *
	 * @param doc        the document to edit
	 * @param parentElem the parent Element
	 * @param events     an array of Events to be added
	 * @return Document the document with the added events
	 */
	private static Document addEvents(Document doc, Element parentElem, List<Event> events) {
		log.debug("Adding " + events.size() + " events");
		/*********************************************************
		 * EVENTS - Including VPs, CalledScripts, Exceptions, etc
		 *********************************************************/
		for (Event event : events) {
			log.debug(event.getEventType() + "   " + event.getScriptName());
			if (event.isVerificationPoint()) {
				doc = addVerificationPoint(doc, parentElem, event);
			} else if (event.isNote()) {
				doc = addNote(doc, parentElem, event);
			} else if (event.isScreenShot()) {
				doc = addScreenShot(doc, parentElem, event);
			} else if (event.isCalledScript()) {
				doc = addScript(doc, parentElem, (ScriptResults) event);
			} else if (event.getEventType().equals(EVENT_TYPE.EXECUTIONSCRIPT)) {
				doc = addScript(doc, parentElem, (ScriptResults) event);
			}
		}
		return doc;
	}

	/**
	 * Will generate the XML elements to represent the provided script, including
	 * its events
	 *
	 * @param doc        the document to edit
	 * @param parentElem the parent element
	 * @param results    the ScriptResults to add
	 * @return Document the document with the results added for the script passed in
	 */
	private static Document addScript(Document doc, Element parentElem, ScriptResults results) {

		String xmlTag = results.isExecutionScript() ? EXECUTION_SCRIPT_TAG : CALLED_SCRIPT_TAG;
		log.debug("!!! Adding " + parentElem.getNodeName() + ": " + results.getScriptName() + ", " + xmlTag);

		if (results.isExecutionScript()) {
			log.debug("EXECUTION!!");
		}

		Element script = doc.createElement(xmlTag);
		script = setScriptElementDetails(doc, script, results);
		doc = addEvents(doc, script, results.getEvents());

		parentElem.appendChild(script);
		return doc;
	}

	/**
	 * Will add a verification point element to the xml document
	 *
	 * @param doc    the document to edit
	 * @param parent the parent of the
	 * @param event  the event to add (vp)
	 * @return Document
	 */
	private static Document addVerificationPoint(Document doc, Element parent, Object event) {
		// staff elements
		// expected element
		VerificationPoint vp = (VerificationPoint) event;

		Element vpElem = doc.createElement(VP_TAG);
		parent.appendChild(vpElem);

		// set name attribute for vp element
		Attr attr = doc.createAttribute(NAME_TAG);
		attr.setValue(vp.getVpName());
		vpElem.setAttributeNode(attr);

		// script name element
		Element scriptNameCS = doc.createElement(VP_SCRIPT_NAME);
		scriptNameCS.appendChild(doc.createTextNode(vp.getScriptName()));
		vpElem.appendChild(scriptNameCS);

		// timestamp element
		Element timestamp = doc.createElement(TIMESTAMP_TAG);
		timestamp.appendChild(doc.createTextNode(vp.getTimestamp().toString()));
		vpElem.appendChild(timestamp);

		// regex element
		String regex = vp.getRegex().toString().toUpperCase();
		if (regex.isEmpty()) {
			regex = "EQUALS";
		}
		Element regexElem = doc.createElement(VP_REGEX_TAG);
		regexElem.appendChild(doc.createTextNode("VP " + regex));
		vpElem.appendChild(regexElem);

		// expected element
		Element exepectedElem = doc.createElement(VP_EXPECTED_TAG);
		exepectedElem.appendChild(doc.createTextNode(vp.getExpected().toString()));
		vpElem.appendChild(exepectedElem);

		// actual element
		Element actualElem = doc.createElement(VP_ACTUAL_TAG);
		actualElem.appendChild(doc.createTextNode(vp.getActual().toString()));
		vpElem.appendChild(actualElem);

		// note element
		Element note = doc.createElement(VP_NOTE_TAG);
		note.appendChild(doc.createTextNode(vp.getExecutionNote()));
		vpElem.appendChild(note);

		// pass result element
		Element pass = doc.createElement(VP_PASS_TAG);
		pass.appendChild(doc.createTextNode(Boolean.toString(vp.isPass())));
		vpElem.appendChild(pass);
		return doc;
	}

	/**
	 * Will add a verification point element to the xml document
	 *
	 * @param doc    the document to edit
	 * @param parent the parent
	 * @param event  the note event to be added to the document
	 * @return Document the document with the note added
	 */
	private static Document addNote(Document doc, Element parent, Object event) {
		// staff elements
		// expected element
		Note note = (Note) event;
		Element noteElem = doc.createElement(NOTE_TAG);
		parent.appendChild(noteElem);

		// set name attribute for vp element
		Attr attr = doc.createAttribute(NAME_TAG);
		attr.setValue(note.isException() ? "EXCEPTION" : "NOTE");
		noteElem.setAttributeNode(attr);

		// timestamp element
		Element timestamp = doc.createElement(TIMESTAMP_TAG);
		timestamp.appendChild(doc.createTextNode(note.getTimestamp().toString()));
		noteElem.appendChild(timestamp);

		// pass result element
		Element details = doc.createElement(NAME_TAG);
		details.appendChild(doc.createTextNode(note.getNote()));
		noteElem.appendChild(details);

		// pass result element
		Element pass = doc.createElement(NOTE_EXCEPTION_TAG);
		boolean isException = note.isException();
		pass.appendChild(doc.createTextNode(Boolean.toString(isException)));
		noteElem.appendChild(pass);

		Element warn = doc.createElement(NOTE_WARNING_TAG);
		boolean isWarning = note.isWarning();
		warn.appendChild(doc.createTextNode(Boolean.toString(isWarning)));
		noteElem.appendChild(warn);

		return doc;
	}

	/**
	 * Will add a verification point element to the xml document
	 *
	 * @param doc    the document to edit
	 * @param parent the parent
	 * @param event  the event screenshot to add
	 * @return Document the document with the screenshot added
	 */
	private static Document addScreenShot(Document doc, Element parent, Object event) {
		// staff elements
		// expected element
		ScreenShot screenShot = (ScreenShot) event;
		Element noteElem = doc.createElement(SCREENSHOT_TAG);
		parent.appendChild(noteElem);

		// set name attribute for vp element
		Attr attr = doc.createAttribute(NAME_TAG);
		attr.setValue("SCREENSHOT");
		noteElem.setAttributeNode(attr);

		// timestamp element
		Element timestamp = doc.createElement(TIMESTAMP_TAG);
		timestamp.appendChild(doc.createTextNode(screenShot.getTimestamp().toString()));
		noteElem.appendChild(timestamp);

		// name element
		Element details = doc.createElement(NAME_TAG);
		details.appendChild(doc.createTextNode(screenShot.getNote()));
		noteElem.appendChild(details);

		// file element
		Element file = doc.createElement(FILE_TAG);

		String value = "file:\\\\" + screenShot.getScreenShotFile().getAbsolutePath();

		file.appendChild(doc.createTextNode(value));
		noteElem.appendChild(file);
		return doc;
	}

	/**
	 * Will create the SUITE xml element and all of the scripts it calls under it
	 *
	 * @param doc         the document to edit
	 * @param rootElement the root element
	 * @param manager     the test script manager
	 * @return Document the document with the test suite added
	 */
	private static Document generateTestSuiteXml(Document doc, Element rootElement, TestScriptManager manager) {

		Element suite = doc.createElement(SUITE_TAG);
		rootElement.appendChild(suite);

		ScriptResults sr = manager.getScriptResults();
		sr.setScriptName(manager.getScriptName());
		suite = setScriptElementDetails(doc, suite, sr);

		List<Event> events = sr.getEvents();
		// doc = addEvents(doc, suite, events);
		for (Event event : events) {
			if (event.isExecutionScript()) {
				doc = generateTestSuiteScriptXml(doc, suite, (ScriptResults) event, TEST_FROM_SUITE_TAG);
			} else if (event.isVerificationPoint()) {
				addVerificationPoint(doc, suite, event);
			}
		}
		return doc;
	}

	/**
	 * Will add the XML elements for the test script kicked off from a test suite
	 *
	 * @param doc        the document to edit
	 * @param parentElem the parent element
	 * @param results    the script results
	 * @param xmlTag     xml tag
	 * @return Document the document with the test suite script added
	 */
	private static Document generateTestSuiteScriptXml(Document doc, Element parentElem, ScriptResults results,
			String xmlTag) {
		Element script = doc.createElement(xmlTag);
		script = setScriptElementDetails(doc, script, results);

		List<Event> events = results.getEvents();
		if (!events.isEmpty()) {
			doc = addEvents(doc, script, events);
		}
		parentElem.appendChild(script);

		return doc;

	}

	/**
	 * This method will generate the XML for the execution script and all of the
	 * event within it. This script is the main script that launches everything else
	 *
	 * @param doc           the document to edit
	 * @param parentElement the script mananger
	 * @param sr            the script results
	 * @param manager       the test script manager
	 * @return Document the document
	 */
	private static Document generateExecutionScriptXml(Document doc, Element parentElement, ScriptResults sr,
			TestScriptManager manager) {
		Element script = doc.createElement(EXECUTION_SCRIPT_TAG);
		parentElement.appendChild(script);

		log.debug("ScriptResults name=" + sr.getScriptName());
		log.debug("MGR name=" + manager.getScriptName());
		log.debug("MGR SR name=" + manager.getScriptResults().getScriptName());
		// this is needed to make sure that the initial script name is set
		sr.setScriptName(manager.getScriptName());

		script = setScriptElementDetails(doc, script, sr);

		List<Event> events = sr.getEvents();
		/*********************************************************
		 * EVENTS - Including VPs, CalledScripts, Exceptions, etc
		 *********************************************************/
		for (Object event : events) {

			if (event instanceof VerificationPoint) {
				doc = addVerificationPoint(doc, script, event);
			} else if (event instanceof ScriptResults) {
				ScriptResults results = (ScriptResults) event;
				log.debug("EVENT SR name=" + results.getScriptName());

				if (results.isMainSuiteTest()) {
					doc = generateExecutionScriptXml(doc, parentElement, results, manager);
				} else {
					doc = addCalledScript(doc, script, results);

				}
			} else if (event instanceof Note) {
				Note note = (Note) event;
				doc = addNote(doc, script, note);
			} else if (event instanceof ScreenShot) {
				ScreenShot screenShot = (ScreenShot) event;
				doc = addScreenShot(doc, script, screenShot);
			}
		}
		return doc;
	}

	// private static Document generateExecutionScriptXml(Document doc, Element
	// parentElement,
	// ScriptResults sr) {
	// Element script = doc.createElement(EXECUTION_SCRIPT_TAG);
	// parentElement.appendChild(script);
	//
	// script = setScriptElementDetails(doc, script, sr);
	//
	// List<Event> events = sr.getEvents();
	// /*********************************************************
	// * EVENTS - Including VPs, CalledScripts, Exceptions, etc
	// *********************************************************/
	// for (Object event : events) {
	//
	// if (event instanceof VerificationPoint) {
	// doc = addVerificationPoint(doc, script, event);
	// } else if (event instanceof ScriptResults) {
	// ScriptResults results = (ScriptResults) event;
	// log.debug("EVENT SR name=" + results.getScriptName());
	// // log.debug("MGR name=" + .getScriptName());
	// // log.debug("MGR SR name=" +
	// // manager.getScriptResults().getScriptName());
	//
	// if (results.isMainSuiteTest()) {
	// doc = generateExecutionScriptXml(doc, script, results);
	// } else {
	// doc = addCalledScript(doc, script, results);
	//
	// }
	// } else if (event instanceof Note) {
	// Note note = (Note) event;
	// doc = addNote(doc, script, note);
	// }
	// }
	// return doc;
	// }

	// /**
	// * Will generate the details for a script element in the xml document
	// *
	// * @param doc
	// * @param script
	// * @param sr
	// * @return Document
	// */
	// private static Document generateCalledScriptXml(Document doc, Element script,
	// ScriptResults sr)
	// {
	// log.debug("!!! Adding " + script.getNodeName() + ": " + sr.getScriptName());
	//
	// // LMT part of the infinite loop
	// doc = addEvents(doc, script, sr.getEvents());
	//
	// return doc;
	// }

}
