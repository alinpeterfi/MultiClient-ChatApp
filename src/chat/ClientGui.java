package chat;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

public class ClientGui extends Thread {
	private JFrame frame;
	private JTextPane chatTextPane;
	private JTextPane userListPane;
	private JTextField txtChatInput;
	private JTextField txtName;
	private JButton sendButton;
	private JButton connectButton;
	private JButton disconnectButton;
	private String oldMsg = "";
	private Thread read;
	private String username;
	private BufferedReader input;
	private PrintWriter output;
	private Socket server;

	// frame initializer
	private void initFrame() {
		frame = new JFrame("chat-app");
		frame.getContentPane().setLayout(null);
		frame.setSize(700, 500);
		frame.setResizable(false);
		// frame event listener, close window and output stream
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				JFrame frame = (JFrame) e.getSource();
				int result = JOptionPane.showConfirmDialog(frame, "Are you sure you want to quit chatting?",
						"Exit Application", JOptionPane.YES_NO_OPTION);

				if (result == JOptionPane.YES_OPTION)
					if (output != null)
						output.close();
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			}
		});
	}

	// pane initializer
	private void initTextPane(JTextPane textPane, int x, int y, int width, int height) {
		textPane.setBounds(x, y, width, height);
		textPane.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(textPane);
		scrollPane.setBounds(x, y, width, height);
		textPane.setContentType("text/html");
		frame.add(scrollPane);
	}

	// pane color initializer
	private void paneColorInit(JTextPane chatTextPane, JTextPane userListPane, JTextField txtChatInput, int isConnect) {
		if (isConnect == 0) { // disconnect button
			chatTextPane.setBackground(Color.LIGHT_GRAY);
			userListPane.setBackground(Color.LIGHT_GRAY);
			txtChatInput.setBackground(Color.white);
		} else {
			chatTextPane.setBackground(new Color(220, 220, 220));
			userListPane.setBackground(new Color(220, 220, 220));
			txtChatInput.setBackground(Color.white);
		}
	}

	// send html to pane
	private void addToChatPane(JTextPane textPane, String message) {
		HTMLDocument doc = (HTMLDocument) textPane.getDocument();
		HTMLEditorKit editorKit = (HTMLEditorKit) textPane.getEditorKit();
		try {
			editorKit.insertHTML(doc, doc.getLength(), message, 0, 0, null);
			textPane.setCaretPosition(doc.getLength());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// constructor
	public ClientGui() {
		chatTextPane = new JTextPane();
		userListPane = new JTextPane();
		txtChatInput = new JTextField();
		sendButton = new JButton("Send");
		connectButton = new JButton("Connect");
		disconnectButton = new JButton("Disconnect");
		this.username = "";
		txtName = new JTextField(this.username);

		// frame init
		initFrame();
		// chat pane init
		initTextPane(chatTextPane, 10, 25, 490, 320);
		// users pane init
		initTextPane(userListPane, 520, 25, 156, 320);
		// chat input
		txtChatInput.setBounds(11, 350, 395, 50);
		JScrollPane txtChatInputSp = new JScrollPane(txtChatInput);
		txtChatInputSp.setBounds(11, 350, 665, 50);
		// username input init
		txtName.setBounds(10, 380, 135, 40);
		// connect button init
		connectButton.setBounds(575, 380, 100, 40);
		connectButton.setEnabled(false);// initial blocat pentru a nu avea empty usernames
		paneColorInit(chatTextPane, userListPane, txtChatInput, 0);
		// send button init
		sendButton.setBounds(575, 410, 100, 35);
		// disconnect button init
		disconnectButton.setBounds(10, 410, 130, 35);
		// add elelements to the frame
		frame.add(connectButton);
		frame.add(txtName);
		frame.setVisible(true);
		// initial chat info
		addToChatPane(chatTextPane, "<h2>Enter your username and press connect to begin chatting </h2>");

		/////////////////////////////// button and textfield listeners
		// chat input event
		txtChatInput.addKeyListener(new KeyAdapter() {
			// send message on Enter
			public void keyPressed(KeyEvent e) {
				// event tasta enter -> trimitem mesajul
				try {
					if (e.getKeyCode() == KeyEvent.VK_ENTER) {
						sendMessage();
					}

					// event arrow up -> preluam mesajul trimis precedent
					if (e.getKeyCode() == KeyEvent.VK_UP) {
						String currentMessage = txtChatInput.getText().trim();
						txtChatInput.setText(oldMsg);
						oldMsg = currentMessage;
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});// event txtChatInput

		// send button event
		sendButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				sendMessage();
			}
		}); // event sendButton

		// username event
		txtName.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent e) {

			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				textValidate();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				textValidate();
			}

			// previne mepty usernames
			public void textValidate() {
				if (txtName.getText().trim().equals("")) {
					connectButton.setEnabled(false);
				} else {
					connectButton.setEnabled(true);
				}
			}
		});// event txtName

		// connect button event
		connectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				try {
					// get username, open socket, send the name to the server
					username = txtName.getText();
					addToChatPane(chatTextPane, "<span>Connecting to server...</span>");
					server = new Socket("127.0.0.1", 49152);
					// add info after connection
					addToChatPane(chatTextPane,
							"<h2>Available commands:</h2>" + "<ul>" + "<li><b>@nickname</b> for private messages</li>"
									+ "<li><b>BUZZ</b> for buzz</li>"
									+ "<li><b>*color</b> for changing the chat background (red, green, blue)</li>"
									+ "</ul><br/>");
					// server streams
					input = new BufferedReader(new InputStreamReader(server.getInputStream()));
					output = new PrintWriter(server.getOutputStream(), true);

					// send username to the server
					output.println(username);

					// create thread for for managing the received server message, modify frame
					// elements
					read = new ServerReader(chatTextPane, userListPane, input, frame, txtChatInput);
					read.start();
					frame.remove(txtName);
					frame.remove(connectButton);
					frame.add(sendButton);
					frame.add(txtChatInputSp);
					frame.add(disconnectButton);
					frame.revalidate();
					frame.repaint();
					paneColorInit(chatTextPane, userListPane, txtChatInput, 1);
				} catch (Exception ex) {
					addToChatPane(chatTextPane, "<span>Could not connect to Server</span>");
					JOptionPane.showMessageDialog(frame, ex.getMessage());
				}
			}
		}); // event connectButton

		// disconnect button event
		disconnectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				// modify frame elements, interrupt the thread and close the output stream
				frame.add(txtName);
				frame.add(connectButton);
				frame.remove(sendButton);
				frame.remove(txtChatInputSp);
				frame.remove(disconnectButton);
				frame.getContentPane().setBackground(new Color(248, 248, 248));
				frame.revalidate();
				frame.repaint();
				read.interrupt();
				// reinitialize
				userListPane.setText(null);
				paneColorInit(chatTextPane, userListPane, txtChatInput, 0);
				addToChatPane(chatTextPane, "<span>Connection closed.</span>");
				output.close();
			}
		});// event disconnectButton
	} // constructor

	// message sending function
	public void sendMessage() {
		try {
			// get the message from the text field
			String message = txtChatInput.getText().trim();
			// return if blank
			if (message.equals("")) {
				return;
			}
			this.oldMsg = message;
			output.println(message);// send message to the server
			txtChatInput.setText(""); // reset textfield
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			System.exit(0);
		}
	}

	public static void main(String[] args) throws Exception {
		new ClientGui();
	}
}
