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
//erau final inainte
	private JFrame frame = new JFrame("mIRC");
	private JTextPane chatTextPane;
	private JTextPane userListPane;
	private JTextField txtChatInput;
	private JTextField txtName;
	private JButton sendButton;
	private JButton connectButton;
	private JButton disconnectButton;

	/////// pana aici
	private String oldMsg = "";
	private Thread read;
	private String name;
	private BufferedReader input; // multiple threads si auto flush in comparatie cu scanner
	private PrintWriter output; // pentru text, PrintStream are byte streams
	private Socket server;

	private void initFrame() {
		frame = new JFrame("mIRC");
		frame.getContentPane().setLayout(null);
		frame.setSize(700, 500);
		frame.setResizable(false);
		// daca este apasat butonul x, se inchide socket-ul
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

	private void initTextPane(JTextPane textPane, int x, int y, int width, int height) {
		textPane.setBounds(x, y, width, height);
		textPane.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(textPane);
		scrollPane.setBounds(x, y, width, height);
		textPane.setContentType("text/html");
		frame.add(scrollPane);
	}

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

	public ClientGui() {
		chatTextPane = new JTextPane();
		userListPane = new JTextPane();
		txtChatInput = new JTextField();
		sendButton = new JButton("Send");
		connectButton = new JButton("Connect");
		disconnectButton = new JButton("Disconnect");
		this.name = "";
		txtName = new JTextField(this.name);

		// initializare frame
		initFrame();
		// panou chat
		initTextPane(chatTextPane, 10, 25, 490, 320);
		// panou utilizatori
		initTextPane(userListPane, 520, 25, 156, 320);

		// input user chat
		txtChatInput.setBounds(11, 350, 395, 50);
		JScrollPane txtChatInputSp = new JScrollPane(txtChatInput);
		txtChatInputSp.setBounds(11, 350, 665, 50);

		txtName.setBounds(10, 380, 135, 40);
		connectButton.setBounds(575, 380, 100, 40);
		connectButton.setEnabled(false);// initial blocat pentru a nu avea empty usernames
		paneColorInit(chatTextPane, userListPane, txtChatInput, 0);
		// button send
		sendButton.setBounds(575, 410, 100, 35);
		// button Disconnect
		disconnectButton.setBounds(10, 410, 130, 35);
		frame.add(connectButton);
		frame.add(txtName);
		frame.setVisible(true);
		// info chat initial
		addToChatPane(chatTextPane, "<h2>Enter your username and press connect to begin chatting </h2>");

		/////////////////////////////// event listeners pentru butoane si textfields
		// event pentru chat input
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

		// event pentru chat field
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

		// event Connect
		connectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				try {
					// luam numele, deschidem socket si trimitem numele la server
					name = txtName.getText();
					addToChatPane(chatTextPane, "<span>Connecting to server...</span>");
					server = new Socket("127.0.0.1", 49152);

					addToChatPane(chatTextPane,
							"<h2>Available commands:</h2>" + "<ul>" + "<li><b>@nickname</b> for private messages</li>"
									+ "<li><b>BUZZ</b> for buzz</li>"
									+ "<li><b>*color</b> for changing the chat background (red, green, blue)</li>"
									+ "</ul><br/>");
					// server streams
					input = new BufferedReader(new InputStreamReader(server.getInputStream()));
					output = new PrintWriter(server.getOutputStream(), true);

					// trimitere username la server
					output.println(name);

					// creare thread pt interpretarea mesajului primit de la server si afisare
					// componente aferente
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

		// buton deconectare
		disconnectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				frame.add(txtName);
				frame.add(connectButton);
				frame.remove(sendButton);
				frame.remove(txtChatInputSp);
				frame.remove(disconnectButton);
				frame.getContentPane().setBackground(new Color(248, 248, 248));
				frame.revalidate();
				frame.repaint();
				read.interrupt();
				// reinitializare
				userListPane.setText(null);
				paneColorInit(chatTextPane, userListPane, txtChatInput, 0);
				addToChatPane(chatTextPane, "<span>Connection closed.</span>");
				output.close();
			}
		});// event disconnectButton

		// buton send
		sendButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				sendMessage();
			}
		}); // event sendButton
	} // constructor

	// envoi des messages
	public void sendMessage() {
		try {
			// luam mesajul din input
			String message = txtChatInput.getText().trim();
			// nu facem nimic daca mesajul este gol
			if (message.equals("")) {
				return;
			}
			this.oldMsg = message;
			output.println(message);// trimitem mesajul la server
			txtChatInput.setText(""); // resetam input-ul
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			System.exit(0);
		}
	}

	public static void main(String[] args) throws Exception {
		new ClientGui();
	}

}
