package platformIndependentCore.api;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import platformIndependentCore.exceptions.InvalidParameterException;
import platformIndependentCore.exceptions.InvalidStateException;

/**
 * <b>Name :</b> APIClientTester.java
 * <p>
 * <b>Generated :</b> Dec 16, 2021
 * <p>
 * <b>Description :</b> An API client tester using the JAX-RS library for
 * performing different HTTP requests. Currently allows HTTP and HTTPS protocols
 * with different calling methods such as GET, POST, PUT, DELETE, HEAD, and
 * OPTIONS. Users using this via the ATI framework shouldn't need to instantiate
 * this class, but rather just have to change the endpoint and set different
 * headers for those endpoints if needed.
 * <p>
 *
 * @since Dec 16, 2021
 * @author OITBAYTjoarN
 */
public class APIClientTester {

	/** API URI for this class */
	private static String URI = new String();

	/** Application content we expect */
	protected static String CONTENT_TYPE = "application/json";
	/** Application longest time willing to wait */
	protected static long TIMEOUT = 10000L;
	/** Authentication header field */
	protected static final String AUTH_HEADER_KEY = "Authorization";

	/** JavaScript Application type */
	public static final String APPLICATION_JAVASCRIPT = "application/javascript";
	/** PDF Application type */
	public static final String APPLICATION_PDF = "application/pdf";
	/** JSON Application type */
	public final String APPLICATION_JSON = "application/json";
	/** XML Application type */
	public final String APPLICATION_SOAP_XML = "application/soap+xml";
	/** XML Application type */
	public final String APPLICATION_XML = "application/xml";
	/** ZIP Application type */
	public final String APPLICATION_ZIP = "application/zip";
	/** Form Application type */
	public final String APPLICATION_FORM = "application/x-www-form-urlencoded";
	/** GIF image type */
	public final String IMAGE_GIF = "image/gif";
	/** JPEG image type */
	public final String IMAGE_JPEG = "image/jpeg";
	/** PNG image type */
	public final String IMAGE_PNG = "image/png";
	/** TIFF image type */
	public final String IMAGE_TIFF = "image/tiff";
	/** SVG+XML image type */
	public final String IMAGE_SVG_XML = "image/svg+xml";
	/** CSS Text type */
	public final String TEXT_CSS = "text/css";
	/** CSV Text type */
	public final String TEXT_CSV = "text/csv";
	/** HTML Text type */
	public final String TEXT_HTML = "text/html";
	/** Plain Text type */
	public final String TEXT_PLAIN = "text/plain";
	/** XML Text type */
	public final String TEXT_XML = "text/xml";

	/** Response of the last API call made */
	private Response webServiceResponse = null;
	/**
	 * Response body, must be logged at the same time webServiceResponse is
	 * generated
	 */
	private String respBody = null;
	/** Webclient to be used for any API calls */
	WebClient client;

	/** logger instance for this class */
	private static Logger log = LogManager.getLogger(APIClientTester.class.getName());

	/**
	 * Constructor to construct the class for us to use
	 */
	public APIClientTester() {
		log.debug("Instantiating API tester");
		client = getWebClient();
	}

	/**
	 * Gets and returns the status code of the last call made
	 *
	 * @return int
	 */
	public int getStatusCode() {
		if (webServiceResponse == null) {
			log.error("No API calls have been made yet");
			throw new InvalidStateException("You must make an API call first.");
		}

		return webServiceResponse.getStatus();
	}

	/**
	 * Gets and returns the status descriptor of the given status code
	 *
	 * @return String
	 */
	public String getStatusReason() {
		if (webServiceResponse == null) {
			log.error("No API calls have been made yet");
			throw new InvalidStateException("You must make an API call first.");
		}

		return webServiceResponse.getStatusInfo().getReasonPhrase();
	}

	/**
	 * Gets and returns the body of the last call made TODO: Add two more functions
	 * to handle JSON and XML responses
	 *
	 * @return String
	 */
	public String getResponseBody() {
		if (respBody == null) {
			log.error("Response is null. No API calls have been made yet");
			throw new InvalidStateException("You must make an API call first.");
		}

		return respBody;
	}

	/**
	 * Gets and returns the headers of the last call made
	 *
	 * @return MultivaluedMap<String, Object>
	 */
	public MultivaluedMap<String, Object> getHeaders() {
		if (webServiceResponse == null) {
			log.error("Response is null. No API calls have been made yet");
			throw new InvalidStateException("You must make an API call first.");
		}

		return webServiceResponse.getHeaders();
	}

	/**
	 * Gets and returns specific parts of the header based on the header key
	 *
	 * @param key key of the header value to get
	 * @return String
	 */
	public String getHeader(String key) {
		if (webServiceResponse == null) {
			log.error("Response is null. No API calls have been made yet");
			throw new InvalidStateException("You must make an API call first.");
		}

		if (!webServiceResponse.getHeaders().containsKey(key)) {
			log.error("Headers has no key of value: " + key);
			throw new InvalidParameterException("Headers has no key of value: " + key);
		}

		return webServiceResponse.getHeaders().get(key).toString();
	}

	/**
	 * Gets and returns date that the request was made for timestamp verifications
	 *
	 * @return Date
	 */
	public Date getDate() {
		if (webServiceResponse == null) {
			log.error("Response is null. No API calls have been made yet");
			throw new InvalidStateException("You must make an API call first.");
		}

		return webServiceResponse.getDate();
	}

	/**
	 * Gets and returns date that the response was last modified
	 *
	 * @return Date
	 */
	public Date getLastModified() {
		if (webServiceResponse == null) {
			log.error("Response is null. No API calls have been made yet");
			throw new InvalidStateException("You must make an API call first.");
		}

		return webServiceResponse.getLastModified();
	}

	/**
	 * Gets and returns the media type that is returned by the call
	 *
	 * @return String
	 */
	public String getMediaType() {
		if (webServiceResponse == null) {
			log.error("Response is null. No API calls have been made yet");
			throw new InvalidStateException("You must make an API call first.");
		}

		return webServiceResponse.getMediaType().toString();
	}

	/**
	 * Gets and returns the cookies that is returned by the call
	 *
	 * @return String
	 */
	public Map<String, NewCookie> getCookies() {
		if (webServiceResponse == null) {
			log.error("Response is null. No API calls have been made yet");
			throw new InvalidStateException("You must make an API call first.");
		}

		return webServiceResponse.getCookies();
	}

	/**
	 * Gets and returns specific parts of the header based on the header key
	 *
	 * @param key key of the header value to get
	 * @return NewCookie
	 */
	public NewCookie getCookie(String key) {
		if (webServiceResponse == null) {
			log.error("Response is null. No API calls have been made yet");
			throw new InvalidStateException("You must make an API call first.");
		}

		if (!webServiceResponse.getCookies().containsKey(key)) {
			log.error("Cookies has no key of value: " + key);
			throw new InvalidParameterException("Headers has no key of value: " + key);
		}

		return webServiceResponse.getCookies().get(key);
	}

	/**
	 * Perform a get request to the set URI
	 */
	public void get() {
		try {
			webServiceResponse = client.get();
			respBody = webServiceResponse.readEntity(String.class);

			log.debug("Date: " + getDate());
			log.debug("Address: " + getEndpoint());
			log.debug("Http-Method: GET");
			log.debug("Content-type: " + getMediaType());
			log.debug("Status: " + getStatusCode() + " " + getStatusReason());
			log.debug("Headers: " + getHeaders().toString());
			log.debug("Content: \n" + getResponseBody() + "\n-----------");
		} catch (RuntimeException e) {
			log.error(e.getMessage());
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}

	/**
	 * Perform a post request to the set URI
	 *
	 * @param body payload we want to send to the object
	 */
	public void post(Object body) {
		try {
			webServiceResponse = client.type(CONTENT_TYPE).post(body);
			respBody = webServiceResponse.readEntity(String.class);

			log.debug("Date: " + getDate());
			log.debug("Address: " + getEndpoint());
			log.debug("Http-Method: POST");
			log.debug("Content-type: " + getMediaType());
			log.debug("Status: " + getStatusCode() + " " + getStatusReason());
			log.debug("Headers: " + getHeaders().toString());
			log.debug("Content: \n" + getResponseBody() + "\n-----------");
		} catch (RuntimeException e) {
			log.error(e.getMessage());
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}

	/**
	 * Perform a put request to the set URI
	 *
	 * @param body payload we want to send to the object
	 */
	public void put(Object body) {
		try {
			webServiceResponse = client.type(CONTENT_TYPE).put(body);
			respBody = webServiceResponse.readEntity(String.class);

			log.debug("Date: " + getDate());
			log.debug("Address: " + getEndpoint());
			log.debug("Http-Method: PUT");
			log.debug("Content-type: " + getMediaType());
			log.debug("Status: " + getStatusCode() + " " + getStatusReason());
			log.debug("Headers Returned: " + getHeaders().toString());
			log.debug("Content Returned: \n" + getResponseBody() + "\n-----------");
		} catch (RuntimeException e) {
			log.error(e.getMessage());
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}

	/**
	 * Perform a delete request to the set URI
	 */
	public void delete() {
		try {
			webServiceResponse = client.delete();
			respBody = webServiceResponse.readEntity(String.class);

			log.debug("Date: " + getDate());
			log.debug("Address: " + getEndpoint());
			log.debug("Http-Method: DELETE");
			log.debug("Content-type: " + getMediaType());
			log.debug("Status: " + getStatusCode() + " " + getStatusReason());
			log.debug("Headers Returned: " + getHeaders().toString());
			log.debug("Content Returned: \n" + getResponseBody() + "\n-----------");
		} catch (RuntimeException e) {
			log.error(e.getMessage());
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}

	/**
	 * Perform a head request to the set URI
	 */
	public void head() {
		try {
			webServiceResponse = client.head();
			respBody = webServiceResponse.readEntity(String.class);

			log.debug("Date: " + getDate());
			log.debug("Address: " + getEndpoint());
			log.debug("Http-Method: HEAD");
			log.debug("Content-type: " + getMediaType());
			log.debug("Status: " + getStatusCode() + " " + getStatusReason());
			log.debug("Headers Returned: " + getHeaders().toString());
			log.debug("Content Returned: \n" + getResponseBody() + "\n-----------");
		} catch (RuntimeException e) {
			log.error(e.getMessage());
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}

	/**
	 * Perform a options request to the set URI
	 */
	public void options() {
		try {
			webServiceResponse = client.options();
			respBody = webServiceResponse.readEntity(String.class);

			log.debug("Date: " + getDate());
			log.debug("Address: " + getEndpoint());
			log.debug("Http-Method: OPTIONS");
			log.debug("Content-type: " + getMediaType());
			log.debug("Status: " + getStatusCode() + " " + getStatusReason());
			log.debug("Headers Returned: " + getHeaders().toString());
			log.debug("Content Returned: \n" + getResponseBody() + "\n-----------");
		} catch (RuntimeException e) {
			log.error(e.getMessage());
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}

	/**
	 * Method to check and validate our URIs before sending it in to be checked out
	 *
	 * @return String
	 */
	public String getEndpoint() {
		return URI;
	}

	/**
	 * Set and validate format of URI endpoint
	 *
	 * @param uri URI string to save for our endpoints for future use
	 */
	public void setEndpoint(String uri) {
		if (uri == null || uri.isEmpty()) {
			throw new InvalidStateException("An endpoint for the application must be specified");
		}
		if (!isURIValid(uri)) {
			throw new InvalidStateException("URI set currently is not valid.");
		}

		log.debug("New endpoint set to " + uri);
		URI = uri;

		client = client.to(URI, false);
	}

	/**
	 * Sets timeout of the WebClient
	 *
	 * @param time time to wait before timeout of call
	 */
	public void setTimeout(long time) {
		if (time < 1) {
			throw new InvalidParameterException("Timeout must be a positive value");
		}

		WebClient.getConfig(client).getHttpConduit().getClient().setReceiveTimeout(TIMEOUT);
	}

	/**
	 * Specifies the content type to expect and use for the call
	 *
	 * @param type content type to expect from the call
	 */
	public void setContentType(String type) {
		if (type == null || type.isBlank()) {
			throw new InvalidParameterException("You must specify a content type");
		}

		CONTENT_TYPE = type;
		client = client.accept(CONTENT_TYPE).type(CONTENT_TYPE);
	}

	/**
	 * Sets an authentication header for basic authentication
	 *
	 * @param username user to authenticate with
	 * @param password password to authenticate with
	 */
	public void setBasicAuthHeader(String username, String password) {
		String userAndPassword = username + ":" + password;
		byte[] userAndPasswordBytes = null;
		try {
			userAndPasswordBytes = userAndPassword.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		if (userAndPasswordBytes == null) {
			throw new InvalidStateException("Error generating basic authentication header field");
		}
		String basicAuth = "Basic " + Base64.getEncoder().encodeToString(userAndPasswordBytes);
		client.header(AUTH_HEADER_KEY, basicAuth);
	}

	/**
	 * Sets an authentication header for Oauth2
	 *
	 * @param token OAuth token
	 */
	public void setOauthHeader(String token) {
		client.header(AUTH_HEADER_KEY, "Bearer " + token);
	}

	/**
	 * Sets the header field with the given values
	 *
	 * @param key    the header field to set
	 * @param values Some array of values to set it as
	 */
	public void setHeaderField(String key, Object... values) {
		client.header(key, values);
	}

	/**
	 * Resets the WebClient and response from scratch
	 */
	public void reset() {
		client.reset();
		client.resetQuery();
		webServiceResponse = null;
	}

	/**
	 * Gets and returns a WebClient
	 *
	 * @return WebClient
	 */
	protected WebClient getWebClient() {
		log.debug("Configuring WebClient at " + URI);

		WebClient client = WebClient.create(getEndpoint());

		WebClient.getConfig(client).getHttpConduit().getClient().setReceiveTimeout(TIMEOUT);
		return client;
	}

	/**
	 * Validates our uri to ensure that the URI is in an acceptable format for us to
	 * use
	 *
	 * @param uri URI to validate
	 * @return boolean
	 */
	protected boolean isURIValid(String uri) {
		// URL Validity pattern
		String regex = "\\b(https?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";

		try {
			Pattern patt = Pattern.compile(regex);
			Matcher matcher = patt.matcher(uri);
			return matcher.matches();
		} catch (RuntimeException e) {
			return false;
		}
	}
}
