package chat;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

// read new incoming messages
class ServerReader extends Thread {
	private JFrame frame;
	private JTextPane chatTextPane;
	private JTextPane userListPane;
	private BufferedReader input;
	private JTextField txtChatInput;

//constructor
	ServerReader(JTextPane chatTextPane, JTextPane userListPane, BufferedReader input, JFrame frame,
			JTextField txtChatInput) {
		this.input = input;
		this.chatTextPane = chatTextPane;
		this.userListPane = userListPane;
		this.frame = frame;
		this.txtChatInput = txtChatInput;
	}

	public void run() {
		String message;
		while (!Thread.currentThread().isInterrupted()) {
			try {
				// read message received from the server
				message = input.readLine();
				if (message != null) {
					// if the first character is [ we get the lsit of users
					if (message.charAt(0) == '[') {
						message = message.substring(1, message.length() - 1);// eliminate []
						ArrayList<String> ListUser = new ArrayList<String>( // add the users received from the server to
																			// the list
								Arrays.asList(message.split(", "))// split the array
						);
						userListPane.setText(null);// reset the panel so that users do not repeat
						for (String user : ListUser) {// add users to the users panel
							addToChatPane(userListPane, "@" + user);
						}
					} // user panel management
						// if the message contains * followed by red, gree, blue or default, we set the
						// theme
					else if (message.contains("*")) {
						int indexColor = message.indexOf('*');
						if (indexColor != -1) {
							if (message.toLowerCase().substring(indexColor + 1, indexColor + 4).equals("red")) {
								setTheme(255, 102, 102, 255, 240, 245);
							} else if (message.toLowerCase().substring(indexColor + 1, indexColor + 6)
									.equals("green")) {
								setTheme(60, 179, 113, 240, 255, 240);
							} else if (message.toLowerCase().substring(indexColor + 1, indexColor + 5).equals("blue")) {
								setTheme(0, 112, 255, 240, 248, 255);
							} else if (message.toLowerCase().substring(indexColor + 1, indexColor + 8)
									.equals("default")) {
								this.setTheme(245, 245, 245, 255, 255, 255);
							} else {
								addToChatPane(chatTextPane, "Not a valid color (red, green, blue or default only)!");
							}
						} // IF INDEXCOLOR

					} else {// common task until the thread is terminated *regular message*
						addToChatPane(chatTextPane, message);
					}
				}
			} // disconnect
			catch (IOException ex) {
				System.err.println("Disconnect");
			}
		}
	}

	// function used for setting the theme
	private void setTheme(int redFrame, int greenFrame, int blueFrame, int redPanel, int greenPanel, int bluePanel) {
		this.frame.getContentPane().setBackground(new Color(redFrame, greenFrame, blueFrame));
		this.chatTextPane.setBackground(new Color(redPanel, greenPanel, bluePanel));
		this.userListPane.setBackground(new Color(redPanel, greenPanel, bluePanel));
		this.txtChatInput.setBackground(new Color(redPanel, greenPanel, bluePanel));

	}

	// send html to pane
	private void addToChatPane(JTextPane tp, String msg) {
		HTMLDocument doc = (HTMLDocument) tp.getDocument();
		HTMLEditorKit editorKit = (HTMLEditorKit) tp.getEditorKit();
		try {
			editorKit.insertHTML(doc, doc.getLength(), msg, 0, 0, null);
			tp.setCaretPosition(doc.getLength());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}