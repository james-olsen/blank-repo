package platformIndependentCore.api;

import java.nio.ByteBuffer;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

/**
 * <b>Name :</b> TCPClientReceiverHandler.java
 * <p>
 * <b>Generated :</b> Mar 18, 2022
 * <p>
 * <b>Description :</b> An IoHandlerAdapter class specifically for receiving the
 * TCP messages
 * <p>
 *
 * @since Mar 22, 2022
 * @author OITBAYTjoarN
 */
public class TCPClientReceiverHandler extends IoHandlerAdapter {
	/** Stores a copy of the response */
	private String response;

	@Override
	public void messageReceived(IoSession session, Object message) { // Executes if a message is received
		ByteBuffer buffer = ByteBuffer.allocate(((IoBuffer) message).limit());
		int index = 0;
		do {
			buffer.put(index, ((IoBuffer) message).get(index));
			++index;
		} while (index < ((IoBuffer) message).limit());
		this.response = new String(buffer.array());
	}

	@Override
	public void exceptionCaught(IoSession session, Throwable cause) { // Executes if an exception happens
		session.close(true);
	}

	/**
	 * Gets the response of the string should we need it
	 *
	 * @return String
	 */
	public String getResponse() {
		return this.response;
	}
}
