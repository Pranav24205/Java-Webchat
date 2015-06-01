package webchat.ui;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.SortedSet;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import webchat.message.Message;
import webchat.message.networking.NotLoggedInException;
import webchat.message.networking.client.MessageClient;
import webchat.users.networking.client.UserClient;

/**
 * Main Interface object
 *
 * Takes care of interaction between GUI objects and event handling.
 *
 * @author Jing Yi Xie
 * @version 1.0
 */

public class ClientUI extends Application implements EventHandler<ActionEvent> {
	String user;
	String newUser;
	String newPass;
	final ServScene servScene = new ServScene();
	final ChatScene chatScene = new ChatScene();
	final RegScene regScene = new RegScene();
	final MsgScene msgScene = new MsgScene();
	
	static Font f = Font.loadFont(
			ClientUI.class.getResource("/SegoeUILight.ttf").toExternalForm(),
			17);
	static Font fLarge = Font.loadFont(
			ClientUI.class.getResource("/SegoeUILight.ttf").toExternalForm(),
			25);
	static Font fBig = Font.loadFont(
			ClientUI.class.getResource("/SegoeUILight.ttf").toExternalForm(),
			45);
	static Font font = Font.loadFont(
			ClientUI.class.getResource("/AvenirLTStd-Light.otf")
			.toExternalForm(), 17);
	static Font font2 = Font.loadFont(
			ClientUI.class.getResource("/NexaLight.otf").toExternalForm(), 17);
	
	UserUpdate userupd = new UserUpdate(this.msgScene);
	Messenger messenger = new Messenger(this.msgScene);
	UserInfo userinfo = new UserInfo();
	
	Text blank;
	Text userName;
	Text msgText;
	Text time;
	Text onlineUser;
	
	Scene serv;
	Scene chat;
	Scene reg;
	Scene msg;
	
	String[] usersOnline = null;
	
	public UserClient client;
	MessageClient msgclient;
	String ip;
	byte[] userInstance;
	Stage stage;
	
	/**
	 * Main method used to run the applications.
	 *
	 * @param Unused
	 *            .
	 * @return Nothing.
	 */
	
	public void writeMsg(SortedSet<Message> messages) {
		for (final Message message : messages) {
			try {
				this.blank = new Text();
				this.userName = new Text(message.getUsername() + " said");
				this.msgText = new Text("\"" + message.getContent() + "\"");
				this.msgText.setWrappingWidth(527);
				this.time = new Text("" + message.getTimestamp()); // TODO make
																	// this
																	// actual
																	// time
				this.time.setFill(Color.GREY);
				this.userName.setFill(Color.GREY);
				this.time.setId("textstyle3");
				this.userName.setId("textstyle3");
				
				this.msgText.setId("messagetext");
				this.msgScene.msgs.getChildren().addAll(this.userName,
						this.msgText, this.time, this.blank);
			} catch (final Exception e) {
				reportMessageException(e);
			}
		}
	}
	
	public void writeUsers(String[] users) {
		for (final String userName : users) {
			try {
				this.onlineUser = new Text(userName);
				this.onlineUser.setFill(Color.WHITE);
				this.onlineUser
				.setStyle("-fx-effect: dropshadow( three-pass-box , rgba(255,255,255,0.8) , 5, 0.0 , 0 , 1);"
						+ "-fx-font-family: AvenirLTStd-Light;"
						+ "-fx-font-size: 30;");
				this.onlineUser.setId(userName);
				
				if (this.usersOnline != null) {
					final Collection<String> collection = new ArrayList<String>();
					collection.addAll(Arrays.asList(this.usersOnline));
					collection.addAll(Arrays.asList(users));
					this.usersOnline = collection.toArray(new String[] {});
				} else {
					this.usersOnline = users;
				}
				
				this.msgScene.box2.getChildren().add(this.onlineUser);
				
			} catch (final Exception e) {
				reportUserNameException(e);
			}
		}
	}
	
	public String[] getUsers() {
		return this.usersOnline;
	}
	
	public ClientUI getUI() {
		return this;
	}
	
	public static void main(String[] args) {
		launch(args);
	}
	
	public void reportMessageException(final Throwable t) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				ClientUI.this.msgScene.msgs.getChildren().addAll(
						ClientUI.this.userName, ClientUI.this.msgText,
						ClientUI.this.time, ClientUI.this.blank);
			}
		});
	}
	
	public void reportUserNameException(final Throwable t) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				ClientUI.this.msgScene.box2.getChildren().add(
						ClientUI.this.onlineUser);
			}
		});
	}
	
	/**
	 * This is used to set up the application by adding event handlers to the
	 * appropriate elements and changing the scene when necessary.
	 */
	@Override
	public void start(final Stage primaryStage) {
		this.serv = this.servScene.createServ();
		this.chat = this.chatScene.createChat();
		this.reg = this.regScene.createReg();
		this.msg = this.msgScene.createMsg();
		
		final StageModifier stagemod = new StageModifier(this.servScene,
				this.msgScene, this.chatScene, this.regScene, primaryStage);
		stagemod.draggable();
		stagemod.testmethod();
		
		this.stage = primaryStage;
		
		primaryStage.setTitle("Web Chat");
		primaryStage.setScene(this.serv);
		primaryStage.setResizable(false);
		primaryStage.setWidth(this.serv.getWidth());
		primaryStage.setHeight(this.serv.getHeight());
		primaryStage.show();
		
		this.chatScene.register.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				ClientUI.this.chatScene.clrAll();
				primaryStage.setScene(ClientUI.this.reg);
			}
		});
		
		this.chatScene.enter.setOnAction(this);
		
		this.servScene.enter.setOnAction(this);
		
		this.regScene.enter.setOnAction(this);
		
		this.msgScene.enter.setOnAction(this);
		
		this.msgScene.logout.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				primaryStage.setScene(ClientUI.this.serv);
				ClientUI.this.msgScene.clrAll();
				ClientUI.this.msgclient.stopClient();
				try {
					ClientUI.this.client.logout(ClientUI.this.userInstance);
					ClientUI.this.client = null;
					ClientUI.this.msgclient = null;
				} catch (final RemoteException e) {
					e.printStackTrace();
				}
			}
			
		});
		
		this.msgScene.input.addEventFilter(KeyEvent.KEY_PRESSED,
				new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode().equals(KeyCode.ENTER)) {
					if (ClientUI.this.msgScene.input.getText() != null
							&& !ClientUI.this.msgScene.input.getText()
											.trim().isEmpty()) {
						try {
							ClientUI.this.msgclient
											.sendMessage(ClientUI.this.msgScene.input
													.getText());
						} catch (final RemoteException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (final NotLoggedInException e) {
							try {
								ClientUI.this.client
												.logout(ClientUI.this.userInstance);
								ClientUI.this.client = null;
								ClientUI.this.msgclient = null;
								primaryStage
												.setScene(ClientUI.this.serv);
								ClientUI.this.msgScene.clrAll();
								ClientUI.this.msgclient.stopClient();
							} catch (final RemoteException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
						ClientUI.this.msgScene.input.clear();
					}
					// call messenger for message;
					ke.consume();
				}
			}
		});
		
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent we) {
				try {
					ClientUI.this.client.logout(ClientUI.this.userInstance);
					ClientUI.this.client = null;
					ClientUI.this.msgclient = null;
				} catch (final RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}
	
	@Override
	public void handle(ActionEvent e) {
		if (e.getSource() == this.chatScene.enter) {
			try {
				this.userInstance = this.client.signIn(this.chatScene.userBox
						.getText(), this.chatScene.pwBox.getText().getBytes());
				if (this.userInstance != null) {
					this.msgclient = new MessageClient(this.ip,
							this.userInstance, this);
					this.msgclient.startClient();
					this.stage.setScene(this.msg);
					this.chatScene.clrAll();
				} else {
					this.chatScene.warning.setVisible(true);
				}
			} catch (final RemoteException o) {
				this.chatScene.warning.setVisible(true);
			} catch (MalformedURLException | NotBoundException o) {
				o.printStackTrace();
			}
		}
		if (e.getSource() == this.servScene.enter) {
			try {
				this.ip = this.servScene.ipBox.getText();
				this.client = new UserClient(this.ip, this);
				this.client.startClient();
				this.servScene.clrAll();
				this.stage.setScene(this.chat);
			} catch (MalformedURLException | RemoteException
					| NotBoundException o) {
				this.servScene.warning.setVisible(true);
			}
		}
		if (e.getSource() == this.regScene.enter) {
			if (this.regScene.pwBox.getText().equals(
					this.regScene.pwBox2.getText())) {
				try {
					if (this.client.register(this.regScene.userBox.getText(),
							this.regScene.pwBox.getText().getBytes())) {
						this.stage.setScene(this.chat);
						this.regScene.clrAll();
						this.chatScene.warning.setVisible(false);
					} else {
						this.regScene.warning.setVisible(true);
					}
				} catch (final RemoteException o) {
					this.regScene.warning.setVisible(true);
				}
			}
		}
		if (e.getSource() == this.msgScene.enter) {
			if (this.msgScene.input.getText() != null
					&& !this.msgScene.input.getText().trim().isEmpty()) {
				try {
					this.msgclient.sendMessage(this.msgScene.input.getText());
				} catch (final RemoteException o) {
					// TODO Auto-generated catch block
					o.printStackTrace();
				} catch (final NotLoggedInException o) {
					try {
						this.client.logout(this.userInstance);
						this.client = null;
						this.msgclient = null;
						this.stage.setScene(this.serv);
						this.msgScene.clrAll();
						this.msgclient.stopClient();
					} catch (final RemoteException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
				this.msgScene.input.clear();
			}
		}
		
	}
}