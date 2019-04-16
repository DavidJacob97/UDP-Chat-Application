package UDPChat.Client;

import java.awt.event.*;

public class Client implements ActionListener {

	private String name = null;
	private final ChatGUI gui;
	private ServerConnection connection = null;

	public static void main(String[] args) {
		if (args.length < 3) {
			System.err.println("Usage: java Client serverhostname serverportnumber username");
			System.exit(-1);
		}

		try {
			Client instance = new Client(args[2]);
			instance.connectToServer(args[0], Integer.parseInt(args[1]));
		} catch (NumberFormatException e) {
			System.err.println("Error: port number must be an integer.");
			System.exit(-1);
		}
	}

	private Client(String userName) {
		name = userName;

		// Start up GUI (runs in its own thread)
		gui = new ChatGUI(this, name);
	}

	private void connectToServer(String hostName, int port) {
		// Create a new server connection
		connection = new ServerConnection(hostName, port);
		if (connection.handshake(name)) {
			gui.displayMessage("Connected to the Chatroom");
			listenForServerMessages();
		} else {
			System.err.println("Unable to connect to server");
			gui.displayMessage("Name already exists");
		}
	}

	private void listenForServerMessages() {

		do {
			String message = connection.receiveChatMessage();
			// doesn't display ACK messages from Server
			if (!message.equals("ACK")) {
				gui.displayMessage(message);
				if (message.equals("You have left the chat room, goodbye")) {
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					System.exit(0); 
				}
			}
		} while (true);
	}

	// Sole ActionListener method; acts as a callback from GUI when user hits
	// enter in input field
	@Override
	public void actionPerformed(ActionEvent e) {
		// Since the only possible event is a carriage return in the text input
		// field, the text in the chat input field can now be sent to the
		// server.

		// send chatMessageAtLeastOnce() instead of sendChatMessage()
		String input = gui.getInput() ;
		// don't want joins coming through here
		if (!(input.startsWith("/join"))) {
			connection.sendChatMessageAtLeastOnce(input);
		}
		gui.clearInput();
	}
}
