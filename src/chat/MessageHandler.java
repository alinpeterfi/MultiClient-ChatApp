package chat;

import java.util.Scanner;

class MessageHandler implements Runnable {

	private Server server;
	private User user;

//constructor
	public MessageHandler(Server server, User user) {
		this.server = server;
		this.user = user;
		this.server.sendUserList(); // send user list when connection is established
	}

	public void run() {
		String message;
		// get messages from the server
		Scanner sc = new Scanner(this.user.getInputStream());
		while (sc.hasNextLine()) {// read messages
			message = sc.nextLine();
			// private messages management
			if (message.charAt(0) == '@' && message.charAt(1) != ' ') {
				if (message.contains(" ")) {
					// if the user enters @ followed by a name of an existing user
					System.out.println("private msg : " + message);
					int firstSpace = message.indexOf(" ");// get the message start index
					String userPrivate = message.substring(1, firstSpace);// get the full message
					server.sendMessageToUser(message.substring(firstSpace + 1, message.length()), user, userPrivate);// send
																														// the
																														// message
																														// to
																														// the
																														// client
				}
				// buzz management
			} else if (message.toLowerCase().equals("buzz")) {
				message = "<b style = 'color: #ff0000'>" + "BUZZ" + "</b>";
				System.out.println(user.getNickname() + ": " + "BUZZ");
				server.sendMessageToAll(message, user); // broadcast buzz
				// theme management, send message to the client
			} else if (message.charAt(0) == '*' && message.charAt(1) != ' ') {
				server.sendGuiInfo(message, user);
			} else {
				// common task broadcast
				System.out.println(user.getNickname() + ": " + message);
				server.sendMessageToAll(message, user);
			}
		}
		// end of thread: remove user from the list, close stream, and update user panel
		server.removeUser(user);
		this.server.sendUserList();
		sc.close();
	}
}
