package chat;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Client {

	private String host;
	private int port;

	public static void main(String[] args) throws UnknownHostException, IOException {
		new Client("127.0.0.1", 12345).run();
	}

	public Client(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public void run() throws UnknownHostException, IOException {
		// connect client to server
		Socket client = new Socket(host, port);
		System.out.println("Client successfully connected to the server!");

		// get Socket output stream
		PrintStream output = new PrintStream(client.getOutputStream());

		// read the username
		Scanner sc = new Scanner(System.in);
		System.out.print("Enter a username: ");
		String username = sc.nextLine();

		// send nickname to server
		output.println(username);

		// create a new thread for server messages handling
		new Thread(new ReceivedMessagesHandler(client.getInputStream())).start();

		// read messages from keyboard and send to server
		System.out.println("Messages: \n");

		// while new messages
		while (sc.hasNextLine()) {
			output.println(sc.nextLine());
		}

		// end ctrl D
		output.close();
		sc.close();
		client.close();
	}
}

class ReceivedMessagesHandler implements Runnable {

	private InputStream server;

	public ReceivedMessagesHandler(InputStream server) {
		this.server = server;
	}

	public void run() {
		// receive server messages and print out to screen
		Scanner s = new Scanner(server);
		String tmp = "";
		while (s.hasNextLine()) {
			tmp = s.nextLine();
			if (tmp.charAt(0) == '[') {
				tmp = tmp.substring(1, tmp.length() - 1);
				System.out.println("\nUSERS LIST: " + new ArrayList<String>(Arrays.asList(tmp.split(","))) + "\n");
			} else {
				try {
					System.out.println("\n" + tmp);
					// System.out.println(tmp);
				} catch (Exception ignore) {
				}
			}
		}
		s.close();
	}

}
