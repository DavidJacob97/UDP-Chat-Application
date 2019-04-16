package UDPChat.Client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Random;

public class ServerConnection {

	// Artificial failure rate of 30% packet loss
	static double TRANSMISSION_FAILURE_RATE = 0.3;
	// static double TRANSMISSION_FAILURE_RATE = 0.0;

	private DatagramSocket socket = null;
	private InetAddress serverAddress = null;
	private int serverPort = -1;

	boolean ackReceived = false;

	public ServerConnection(String hostName, int port) {
		serverPort = port;

		// get address of host based on parameters and assign it to m_serverAddress
		try {
			serverAddress = InetAddress.getByName(hostName);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		// set up socket and assign it to m_socket
		try {
			// picks some port
			socket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	public boolean handshake(String name) {

		String message = "/join " + name;
		System.out.println("handshake: " + message);

		byte[] buf = new byte[1024];
		DatagramPacket receivePacket = new DatagramPacket(buf, buf.length);

		// retransmit, wait for 2 seconds before resending request
		try {
			socket.setSoTimeout(2000);
		} catch (SocketException e1) {
			e1.printStackTrace();
		}
		while (true) {
			sendChatMessage(message);
			try {
				socket.receive(receivePacket);
				break;
			} catch (SocketTimeoutException e) {
				System.out.println("Socket timeout");
				// loop to top of while and retransmit
				continue;
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}
		}
		// reset timeout back
		try {
			socket.setSoTimeout(0);
		} catch (SocketException e1) {
			e1.printStackTrace();
		}

		String reply = new String(receivePacket.getData(), 0, receivePacket.getLength());
		System.out.println("Received Reply " + reply);

		if (reply.startsWith("Success")) {
			return true;
		} else {
			return false;
		}
	}

	// this is the method that implements the "at-least-once" invocation style
	public void sendChatMessageAtLeastOnce(String message) {
		ackReceived = false;
		while (!ackReceived) {
			System.out.println("Sending message " + message);
			sendChatMessage(message);

			// leave time for an ACK
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
	}

	public void sendChatMessage(String message) {
		Random generator = new Random();
		double failure = generator.nextDouble();

		if (failure > TRANSMISSION_FAILURE_RATE) {
			// send a chat message to the server
			byte[] buf = message.getBytes();
			DatagramPacket packet = new DatagramPacket(buf, buf.length, serverAddress, serverPort);
			try {
				socket.send(packet);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			// Message got lost
			System.out.println("Message Lost");
		}
	}

	public String receiveChatMessage() {

		byte[] buf = new byte[1024];
		DatagramPacket receivePacket = new DatagramPacket(buf, buf.length);

		// Receive message from server
		// Note that the main thread can block on receive here without
		// problems, since the GUI runs in a separate thread
		// Note also that this method is not called until the client is connected
		try {
			socket.receive(receivePacket);
		} catch (IOException e) {
			e.printStackTrace();
		}

		String message = new String(receivePacket.getData(), 0, receivePacket.getLength());
		System.out.println("Received Message " + message);

		// check if the the message is an acknowledgment
		// if so, set boolean to indicate acknowledgement received
		if (message.startsWith("ACK")) {
			ackReceived = true;
		} 
		
		return message;
	}
}
