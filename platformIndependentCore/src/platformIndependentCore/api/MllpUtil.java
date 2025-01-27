package platformIndependentCore.api;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.rmi.UnexpectedException;
import java.security.InvalidParameterException;
import java.util.Arrays;

/**
 * <b>Name :</b> MllpUtil.java
 * <p>
 * <b>Generated :</b> Mar 18, 2022
 * <p>
 * <b>Description :</b> An Mllp Utility class to help with encoding and decoding
 * the HL7 messages into proper buffers
 * <p>
 *
 * @since Mar 18, 2022
 * @author OITBAYTjoarN
 */
public class MllpUtil {
//	public static final byte SOB = 11;
//	public static final byte EOB = 28;
//	public static final byte LF = 10;
//	public static final byte CR = 13;

	/**
	 * A wrapper for easy calling that immediately assumes EOB is not ignored
	 *
	 * @param buffers    an array of byte buffers that is received from the TCP
	 * @param inCharset  input character set format that we are looking at in the
	 *                   buffers
	 * @param outCharset output character set format that we want from this function
	 * @return ByteBuffer
	 */
	public static ByteBuffer decode(ByteBuffer[] buffers, Charset inCharset, Charset outCharset) {
		return decode(buffers, false, inCharset, outCharset);
	}

	/**
	 * Decodes the returned Mllp encoding to a ByteBuffer that can later be
	 * converted to a readable format
	 *
	 * @param buffers    an array of byte buffers that is received from the TCP
	 * @param ignoreEob  specify whether we want care that the buffer contains an
	 *                   ending byte or not for the charset
	 * @param inCharset  input character set format that we are looking at in the
	 *                   buffers
	 * @param outCharset output character set format that we want from this function
	 * @return ByteBuffer
	 */
	public static ByteBuffer decode(ByteBuffer[] buffers, boolean ignoreEob, Charset inCharset, Charset outCharset) {
		boolean eobFound = false;
		StringBuffer buffer = new StringBuffer();
		for (ByteBuffer byteBuffer : buffers) {
			try {
				InputStreamReader reader = new InputStreamReader(
						new ByteArrayInputStream(byteBuffer.array(), 0, byteBuffer.limit()), inCharset);
				int i = -1;
				while ((i = reader.read()) != -1) {
					char c = (char) i;
					if (eobFound) {
						// Reader differs from EOB found
						if ((i == 0) || (i != 13)) {
							throw new UnexpectedException("An issue happened with the Mllp encoding");
						}
						continue;
					}
					if (c == '\013') { // Ignore all \013
						continue;
					}
					if (c == '\034') { // This is the EOB character
						eobFound = true;
						continue;
					}
					buffer.append(c);
				}
			} catch (IOException e) {
				throw new InvalidParameterException("An issue happened with the Mllp encoding");
			}
		}

		if (eobFound || ignoreEob) {
			return doCharacterEncoding(buffer.toString(), outCharset);
		}
		return null;
	}

	/**
	 * Encoding the buffer into a format that is accepted for the Mllp requirement
	 *
	 * @param byteBuffer ByteBuffer source to convert from
	 * @param inCharset  input character set format that we are looking at in the
	 *                   buffers
	 * @param outCharset output character set format that we want from this function
	 * @return ByteBuffer
	 */
	public static ByteBuffer encode(ByteBuffer byteBuffer, Charset inCharset, Charset outCharset) {
		StringBuffer buffer = new StringBuffer();
		byteBuffer = decode(new ByteBuffer[] { byteBuffer }, true, inCharset, inCharset);
		InputStreamReader reader = new InputStreamReader(
				new ByteArrayInputStream(byteBuffer.array(), 0, byteBuffer.limit()), inCharset);
		buffer.append('\013');
		int i = -1;
		int last = 0;
		try {
			while ((i = reader.read()) != -1) {
				char c = (char) i;
				if (i == 10) {
					if ((last != 13) && (last != 10)) {
						buffer.append('\r');
					}
					last = i;
					continue;
				}
				buffer.append(c);
				last = i;
			}
		} catch (IOException e) {
			throw new InvalidParameterException("An issue happened with the Mllp encoding");
		}
		buffer.append('\r').append('\034').append('\r');
		buffer.trimToSize();
		return doCharacterEncoding(buffer.toString(), outCharset);
	}

	/**
	 * Performs an encoding for a buffer that will form the character encoding
	 *
	 * @param str             string of the character to encode from
	 * @param encodingCharset character set format that we want from this function
	 * @return ByteBuffer
	 */
	private static ByteBuffer doCharacterEncoding(String str, Charset encodingCharset) {
		byte[] buff = encodingCharset.encode(str).array();
		return ByteBuffer.wrap(Arrays.copyOf(buff, buff.length - countTrailingNullBytes(buff)));
	}

	/**
	 * Counts training null bytes in a byte buffer
	 *
	 * @param buff buffer to count from
	 * @return int
	 */
	private static int countTrailingNullBytes(byte[] buff) {
		int countNulls = 0;
		for (int i = buff.length - 1; i != 0; i--) {
			if (buff[i] != 0) {
				break;
			}
			countNulls++;
		}
		return countNulls;
	}
}
