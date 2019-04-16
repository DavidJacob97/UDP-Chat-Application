package UDPChat.Server;

//
//Source file for the server side. 
//
//Created by Sanny Syberfeldt
//Maintained by Marcus Brohede
//
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Iterator;

public class Server {
	// an array list of Clients
	private ArrayList<ClientConnection> connectedClients = new ArrayList<ClientConnection>();
	DatagramSocket socket;

	public static void main(String[] args) {
		if (args.length < 1) {
			System.err.println("Usage: java Server portnumber");
			System.exit(-1);
		}
		try {
			Server instance = new Server(Integer.parseInt(args[0]));
			SocketListener listener = new SocketListener(instance, instance.socket);
			listener.listenForClientMessages();
		} catch (NumberFormatException e) {
			System.err.println("Error: port number must be an integer.");
			System.exit(-1);
		}
	}

	private Server(int portNumber) {
		// create a socket, attach it to port based on portNumber, and
		// assign it to m_socket
		try {
			socket = new DatagramSocket(portNumber);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	public void handleClientMessages(String message, String sender) {

		if (message.startsWith("/tell")) {
			// - Send a private message to a user using sendPrivateMessage()
			System.out.println("/tell");

			String[] tokens = message.split(",");
			if (tokens.length == 2) {
				String name = tokens[0].substring(6);
				System.out.println(name);
				String mess = tokens[1];
				mess = sender + ":" + mess;
				sendPrivateMessage(mess, name);
			} else {
				System.out.println("error in message format");
			}

		} else if (message.startsWith("/leave")) {
			System.out.println("/leave");
			sendPrivateMessage("You have left the chat room, goodbye", sender);
			leave(sender);
			broadcast(sender + " has left the chat room") ;

		} else if (message.startsWith("/list")) {
			System.out.println("/list");
			String cList = getClientList();
			sendPrivateMessage(cList, sender);

		} else if (message.startsWith("/broadcast")) {
			// Broadcast the message to all connected users using
			// broadcast()

			System.out.println("broadcast");
			message = message.substring(10) ;
			String broadcastMessage = sender + ": " + message;
			broadcastEveryoneElse(broadcastMessage, sender);

		} else {
			System.out.println("wrong command syntax");
			sendPrivateMessage("Wrong command syntax - /list /tell /broadcast /leave", sender);
		}

	}

	// adds a client, returns false if name is already used
	public boolean addClient(String name, InetAddress address, int port) {
		ClientConnection c;
		for (Iterator<ClientConnection> itr = connectedClients.iterator(); itr.hasNext();) {
			c = itr.next();
			if (c.hasName(name)) {
				return false; // Already exists a client with this name
			}
		}
		connectedClients.add(new ClientConnection(name, address, port));
		return true;
	}

	// sends message to defined client
	public void sendPrivateMessage(String message, String name) {
		ClientConnection c;
		for (Iterator<ClientConnection> itr = connectedClients.iterator(); itr.hasNext();) {
			c = itr.next();
			if (c.hasName(name)) {
				c.sendMessage(message, socket);
			}
		}
	}

	// message to everyone connected
	public void broadcast(String message) {
		for (Iterator<ClientConnection> itr = connectedClients.iterator(); itr.hasNext();) {
			itr.next().sendMessage(message, socket);
		}
	}

	// message to everyone connected
	public void broadcastEveryoneElse(String message, String except) {
		for (ClientConnection c: connectedClients) {
			if (!c.getName().equals(except)) {
				c.sendMessage(message, socket);
			}
		}
	}

	// get the name of the client
	public String getNameByAddressPort(InetAddress inet, int port) {
		String name = null;
		for (ClientConnection c : connectedClients) {
			if ((c.getAddress().equals(inet)) && (c.getPort() == port)) {
				name = c.getName();
			}
		}
		return name;
	}

	public ClientConnection getClientConnectionByName(String name) {
		for (ClientConnection c : connectedClients) {
			if (c.getName().equals(name)) {
				return c;
			}
		}
		return null;
	}

	// gets a list of clients connected
	public String getClientList() {
		String clientList = "";
		for (ClientConnection c : connectedClients) {
			clientList += " | " + c.getName();
		}
		return clientList;
	}

	// removes said client
	public void leave(String name) {
		ClientConnection removeConnection = null;
		for (ClientConnection c : connectedClients) {
			if (c.getName().equals(name)) {
				removeConnection = c;
			}
		}
		connectedClients.remove(removeConnection);
	}
}
