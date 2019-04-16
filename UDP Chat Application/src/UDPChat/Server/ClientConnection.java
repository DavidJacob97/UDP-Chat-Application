package UDPChat.Server;

import java.util.Random;
import java.io.IOException;
import java.net.*;


public class ClientConnection {

	static double TRANSMISSION_FAILURE_RATE = 0.3;
	// static double TRANSMISSION_FAILURE_RATE = 0.0;

	private final String clientName;
	private final InetAddress clientAddress;
	private final int clientPort;

	public ClientConnection(String name, InetAddress address, int port) {
		clientName = name;
		clientAddress = address;
		clientPort = port;
	}

	public void sendMessage(String message, DatagramSocket socket) {

		Random generator = new Random();
		double failure = generator.nextDouble();

		if (failure > TRANSMISSION_FAILURE_RATE) {
			// send a message to this client using socket.
			byte[] buf = message.getBytes();

			// * send message via socket
			DatagramPacket packet = new DatagramPacket(buf, buf.length, clientAddress, clientPort);
			try {
				socket.send(packet);
			} catch (IOException e) {
				e.printStackTrace();
			}

		} else {
			System.out.println("Message lost ");
			sendMessage(message, socket) ;
		}
	}

	public InetAddress getAddress() {
		return clientAddress;
	}

	public String getName() {
		return clientName;
	}

	public int getPort() {
		return clientPort;
	}

	public boolean hasName(String testName) {
		return testName.equals(clientName);
	}
}
