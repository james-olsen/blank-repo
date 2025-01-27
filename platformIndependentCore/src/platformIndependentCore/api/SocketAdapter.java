package platformIndependentCore.api;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import org.apache.mina.core.RuntimeIoException;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import platformIndependentCore.exceptions.InvalidStateException;

/**
 * <b>Name :</b> SocketAdapter.java
 * <p>
 * <b>Generated :</b> Mar 18, 2022
 * <p>
 * <b>Description :</b> A socket class to handle sending and receiving TCP
 * messages
 * <p>
 *
 * @since Mar 18, 2022
 * @author OITBAYTjoarN
 */
public class SocketAdapter {
	/** Private variable to hold the host address */
	private String host;
	/** Private variable to hold the port number */
	private Integer port;

	/**
	 * Public static method to send a payload and we can receive a response from the
	 * server if we are expecting one
	 *
	 * @param host    host address of endpoint to write to
	 * @param port    port number of endpoint to write to
	 * @param message message to send in the payload of type IoBuffer
	 * @return String
	 */
	public static final String sendAndReceive(String host, int port, IoBuffer message) {
		TCPClientSenderHandler clientHandler;
		try {
			clientHandler = new TCPClientSenderHandler(message);
			SocketAdapter sender = new SocketAdapter(host, port);
			sender.connectToServer(clientHandler);
			return clientHandler.getResponse();
		} catch (InterruptedException | IOException e) {
			e.printStackTrace();
			throw new InvalidStateException("An error occured in the process of communications: " + e.getMessage());
		}
	}

	/**
	 * Public static method to send a payload and we can receive a response from the
	 * server if we are expecting one
	 *
	 * @param host    host address of endpoint to write to
	 * @param port    port number of endpoint to write to
	 * @param message message to send in the payload of type IoBuffer
	 * @return String
	 */
	public static final String receive(String host, int port, IoBuffer message) {
		TCPClientReceiverHandler clientHandler;
		try {
			clientHandler = new TCPClientReceiverHandler();
			SocketAdapter receiver = new SocketAdapter(host, port);
			receiver.connectToServer(clientHandler);
			return clientHandler.getResponse();
		} catch (InterruptedException | IOException e) {
			e.printStackTrace();
			throw new InvalidStateException("An error occured in the process of communications: " + e.getMessage());
		}
	}

	/**
	 * Convert the message to Mllp and create an IoBuffer for us to pass to a socket
	 * handler
	 *
	 * @param messageToSend the string of the message to send
	 * @return IoBuffer
	 */
	public static IoBuffer encodeMllp(String messageToSend) {
		ByteBuffer buffer = encodeToMllp(messageToSend);
		IoBuffer messageIoBuffer = IoBuffer.allocate(buffer.limit());
		int index = 0;
		while (buffer.hasRemaining() && (index < buffer.limit())) {
			messageIoBuffer.put(index, buffer.get(index));
			index++;
		}
		return messageIoBuffer;
	}

	/**
	 * Decodes our response Mllp message to a readable format
	 *
	 * @param message Mllp encoded message received
	 * @return String
	 */
	public static String decodeMllp(String message) {
		return new String(MllpUtil.decode(new ByteBuffer[] { ByteBuffer.wrap(message.getBytes()) },
				Charset.forName("ISO-8859-1"), Charset.defaultCharset()).array());
	}

	/**
	 * Private constructor, this object should not be constructed in general, it is
	 * meant to have only static methods called in a static way
	 *
	 * @param host the host of the TCP endpoint to connect to
	 * @param port the port number of the TCP endpoint to connect to
	 */
	private SocketAdapter(String host, int port) {
		this.host = host;
		this.port = port;
	}

	/**
	 * Initiates a socket handler connection for our server connection
	 *
	 * @param ioHandler IOhandler class to use for our connection
	 * @throws InterruptedException If connection is interrupted
	 * @throws IOException          if connection has issues with writing/reading
	 */
	private void connectToServer(IoHandler ioHandler) throws InterruptedException, IOException {
		IoSession session;
		NioSocketConnector nioSocketConnector = new NioSocketConnector();
		nioSocketConnector.getFilterChain().addLast("logger", new LoggingFilter());
		nioSocketConnector.setHandler(ioHandler);
		nioSocketConnector.setConnectTimeoutMillis(1000L);
		while (true) {
			try {
				ConnectFuture future = nioSocketConnector.connect(new InetSocketAddress(host, port));
				future.awaitUninterruptibly();
				session = future.getSession();
				break;
			} catch (RuntimeIoException e) {
				e.printStackTrace();
				Thread.sleep(5000L);
			}
		}
		session.getCloseFuture().awaitUninterruptibly();
		nioSocketConnector.dispose();
	}

	/**
	 * Encodes the HL7 message to an MLLP format
	 *
	 * @param hl7Er7Message the HL7 string to convert
	 * @return ByteBuffer
	 */
	private static ByteBuffer encodeToMllp(String hl7Er7Message) {
		ByteBuffer buffer = ByteBuffer.allocate((hl7Er7Message.toString().getBytes()).length);
		buffer.put(hl7Er7Message.toString().getBytes());
		return MllpUtil.encode(buffer, Charset.forName("ISO-8859-1"), Charset.defaultCharset());
	}
}
