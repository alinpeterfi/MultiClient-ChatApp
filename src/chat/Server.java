package chat;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Server {
	private int port;
	private ArrayList<User> clients;
	private ServerSocket server;

	public static void main(String[] args) throws IOException {
		try {
			new Server(49152).run();
		} catch (BindException be) {
			System.err.println("Server is already on");
		}
	}

//constructor
	public Server(int port) {
		this.port = port;
		this.clients = new ArrayList<User>();
	}

	public void run() throws IOException {
		server = new ServerSocket(port) {
			protected void finalize() throws IOException { // close the socket free the resources
				this.close();
			}
		};
		System.out.println("Server on");
		// infinite loop
		while (true) {
			// accept a new client
			Socket client = server.accept();

			// get nusername
			@SuppressWarnings("resource")
			String username = (new Scanner(client.getInputStream())).nextLine();
			System.out.println("New Client: " + username);

			// creare user profile
			User newUser = new User(client, username);

			// add user to the user list
			this.clients.add(newUser);

			// welcome message sent to the client
			newUser.getOutStream().println("<b>Welcome</b> " + newUser.toString());

			// thread message management
			new Thread(new MessageHandler(this, newUser)).start();
		} // while
	}

	// remove user from the users list
	public void removeUser(User user) {
		this.clients.remove(user);
	}

	// broadcast message
	public void sendMessageToAll(String msg, User userSender) {
		for (User client : this.clients) {
			client.getOutStream().println(userSender.toString() + "<span>: " + msg + "</span>");
		}
	}

	// send useful information for the gui
	public void sendGuiInfo(String msg, User userSender) {
		userSender.getOutStream().println(userSender.toString() + "<span>: " + msg + "</span>");
	}

	// send the user list to all the clients
	public void sendUserList() {
		for (User client : this.clients) {
			client.getOutStream().println(this.clients);
		}
	}

	// send private message
	public void sendMessageToUser(String msg, User userSender, String user) {
		boolean userExists = false, sameUser = false;
		for (User userReceiver : this.clients) {// browse the user lsit
			if (userReceiver.getNickname().equals(user)) {// if there is a match
				if (userReceiver != userSender) {// and sender != receiver
					userExists = true;
					userSender.getOutStream()
							.println(userSender.toString() + "<b> -> </b>" + userReceiver.toString() + ": " + msg);// message
																													// snet
																													// to
																													// the
																													// sender

					userReceiver.getOutStream()
							.println("(<b>Private</b>) " + userSender.toString() + "<span>: " + msg + "</span>"); // message
																													// sent
																													// to
																													// the
																													// receiver
				} // flags
				else {
					sameUser = true;
					userExists = true;
				}
			} // if the user exists
		}
		// in case the user does not exist
		if (!userExists)
			userSender.getOutStream().println("<b>" + user + "</b>" + " is not connected!");
		// sender == receiver
		if (sameUser)
			userSender.getOutStream().println("You cannot send private messages to yourself!");
	}
}
