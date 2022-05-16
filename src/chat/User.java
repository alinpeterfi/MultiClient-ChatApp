package chat;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;

class User {
	private static int userCount = 0;
	private int userId;
	private PrintStream streamOut;
	private InputStream streamIn;
	private String nickname;
	private Socket client;
	private String color;

	// constructor
	public User(Socket client, String name) throws IOException {
		this.streamOut = new PrintStream(client.getOutputStream());
		this.streamIn = client.getInputStream();
		this.client = client;
		this.nickname = name;
		this.userId = userCount;
		this.color = ColorHandler.getColor(this.userId);
		userCount += 1;
	}

	// getters setters
	public PrintStream getOutStream() {
		return this.streamOut;
	}

	public InputStream getInputStream() {
		return this.streamIn;
	}

	public String getNickname() {
		return this.nickname;
	}

	// print user with his color
	public String toString() {

		return "<u><span style='color:" + this.color + "'>" + this.getNickname() + "</span></u>";

	}
}
