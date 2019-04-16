package UDPChat.Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class SocketListener {
	Server server;
	DatagramSocket socket;
	boolean ackReceived = false;

	public SocketListener(Server server, DatagramSocket socket) {
		this.server = server;
		this.socket = socket;
	}

	public void listenForClientMessages() {
		while (true) {
			// Listen for client messages.
			System.out.println("Listening for messages");
			byte[] buf = new byte[1024];
			DatagramPacket receivePacket = new DatagramPacket(buf, buf.length);
			try {
				socket.receive(receivePacket);
			} catch (IOException e) {
				e.printStackTrace();
			}
			// extract info from the receive packet
			InetAddress inet = receivePacket.getAddress();
			int port = receivePacket.getPort();
			String message = new String(receivePacket.getData(), 0, receivePacket.getLength());

			System.out.println(message + " from " + inet.getHostAddress() + " senders port " + port);
			String sender = server.getNameByAddressPort(inet, port);

			// dont send ACK for join
			// just send response
			if (message.startsWith("/join") && sender == null) {
				String[] tokens = message.split(" ");
				String clientName = tokens[1];
				// Try to create a new ClientConnection using addClient(),
				// send response message to client detailing whether it was successful
				System.out.println("Connnection request for " + clientName);

				String reply = null;
				if (server.addClient(clientName, inet, port)) {
					System.out.println("Client added");
					reply = "Success : Client added";
					server.broadcastEveryoneElse(clientName + " has joined the chat room", clientName);
				} else {
					// Note - Client could already exists because of lost ACK
					// check if same or different inet and port
					System.out.println("Client already exists");
					ClientConnection cc = server.getClientConnectionByName(clientName);
					if ((inet.equals(cc.getAddress())) && (port == cc.getPort())) {
						// ACK got lost
						System.out.println("Resending success message");
						reply = "Success : Client added";
					} else {
						// name really is taken
						System.out.println("Sending Error message");
						reply = "Error, client already exists";
					}
				}
				byte[] replyBuf = reply.getBytes();
				DatagramPacket packet2 = new DatagramPacket(replyBuf, replyBuf.length, inet, port);
				try {
					socket.send(packet2);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {

				if (sender != null) {
					server.sendPrivateMessage("ACK", sender);
					server.handleClientMessages(message, sender);
				}
			}
		}
	}
}
