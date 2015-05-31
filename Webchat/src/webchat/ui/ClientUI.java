package webchat.ui;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.SortedSet;

import webchat.message.Message;
import webchat.message.networking.client.MessageClient;
import webchat.users.networking.client.UserClient;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
			ClientUI.class.getResource("/res/SegoeUILight.ttf")
					.toExternalForm(), 17);
	static Font fLarge = Font.loadFont(
			ClientUI.class.getResource("/res/SegoeUILight.ttf")
					.toExternalForm(), 25);
	static Font fBig = Font.loadFont(
			ClientUI.class.getResource("/res/SegoeUILight.ttf")
					.toExternalForm(), 45);
	static Font font = Font.loadFont(
			ClientUI.class.getResource("/res/AvenirLTStd-Light.otf")
					.toExternalForm(), 17);
	static Font font2 = Font.loadFont(
			ClientUI.class.getResource("/res/NexaLight.otf").toExternalForm(),
			17);

	UserUpdate userupd = new UserUpdate(msgScene);
	Messenger messenger = new Messenger(msgScene);
	UserInfo userinfo = new UserInfo();
	
	Text blank;
	Text userName;
	Text msgText;
	Text time;
	
	Scene serv;
	Scene chat;
	Scene reg;
	Scene msg;

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
		for (Message message : messages) {
			try{
			System.out.println(message.getContent() + " ui");
			blank = new Text();
			userName = new Text(message.getUsername() + " said");
			msgText = new Text("\"" + message.getContent() + "\"");
			msgText.setWrappingWidth(527);
			time = new Text("" + message.getTimestamp());
			time.setFill(Color.GREY);
			userName.setFill(Color.GREY);
			time.setId("textstyle3");
			userName.setId("textstyle3");

			msgText.setId("messagetext");
			System.out.println("test1");
			msgScene.msgs.getChildren().addAll(userName, msgText, time, blank);
			}
			catch (Exception e)
			  {
			    reportAndLogException(e);
			  }
		}
	}

	public ClientUI getUI() {
		return this;
	}

	public static void main(String[] args) {
		launch(args);
	}
	
	public void reportAndLogException(final Throwable t)
	  {
	    Platform.runLater(new Runnable() {
	      @Override public void run() {
	    	  msgScene.msgs.getChildren().addAll(userName, msgText, time, blank); 
	    	  System.out.println("test2");
	      }
	    });
	  }

	/**
	 * This is used to set up the application by adding event handlers to the
	 * appropriate elements and changing the scene when necessary.
	 */
	public void start(final Stage primaryStage) {
		serv = servScene.createServ();
		chat = chatScene.createChat();
		reg = regScene.createReg();
		msg = msgScene.createMsg();

		this.stage = primaryStage;

		primaryStage.setTitle("Web Chat");
		primaryStage.setScene(serv);
		primaryStage.setResizable(false);
		primaryStage.setWidth(serv.getWidth());
		primaryStage.setHeight(serv.getHeight());
		primaryStage.show();

		primaryStage.heightProperty().addListener(new ChangeListener<Object>() {
			@Override
			public void changed(ObservableValue<?> observable, Object oldvalue,
					Object newValue) {
				msgScene.imview.setFitHeight(primaryStage.getHeight());
				msgScene.imview.setFitWidth(primaryStage.getWidth());
				regScene.imview.setFitHeight(primaryStage.getHeight());
				regScene.imview.setFitWidth(primaryStage.getWidth());
				chatScene.imview.setFitHeight(primaryStage.getHeight());
				chatScene.imview.setFitWidth(primaryStage.getWidth());
			}
		});

		chatScene.register.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				chatScene.clrAll();
				primaryStage.setScene(reg);
			}
		});

		chatScene.enter.setOnAction(this);

		servScene.enter.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					ip = servScene.ipBox.getText();
					client = new UserClient(ip);
					servScene.clrAll();
					primaryStage.setScene(chat);
				} catch (MalformedURLException | RemoteException
						| NotBoundException e) {
					servScene.warning.setVisible(true);
				}
			}
		});

		regScene.enter.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (regScene.pwBox.getText().equals(regScene.pwBox2.getText())) {
					try {
						if (client.register(regScene.userBox.getText(),
								regScene.pwBox.getText().getBytes())) {
							primaryStage.setScene(chat);
							regScene.clrAll();
							chatScene.warning.setVisible(false);
						} else
							regScene.warning.setVisible(true);
					} catch (RemoteException e) {
						regScene.warning.setVisible(true);
					}
				}
			}
		});

		msgScene.logout.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				primaryStage.setScene(serv);
				msgScene.clrAll();
				msgclient.stopClient();
				try {
					client.logout(userInstance);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}

		});

		msgScene.enter.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent actionEvent) {
				if (msgScene.input.getText() != null
						&& !msgScene.input.getText().trim().isEmpty()) {
					try {
						msgclient.sendMessage(msgScene.input.getText());
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					msgScene.input.clear();
				}
			}
		});

		msgScene.input.addEventFilter(KeyEvent.KEY_PRESSED,
				new EventHandler<KeyEvent>() {
					@Override
					public void handle(KeyEvent ke) {
						if (ke.getCode().equals(KeyCode.ENTER)) {
							if (msgScene.input.getText() != null
									&& !msgScene.input.getText().trim()
											.isEmpty()) {
								try {
									msgclient.sendMessage(msgScene.input
											.getText());
								} catch (RemoteException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								msgScene.input.clear();
							}
							// call messenger for message;
							ke.consume();
						}
					}
				});
	}

	@Override
	public void handle(ActionEvent e) {
		System.out.println(e);
		if (e.getSource() == chatScene.enter) {
			try {
				userInstance = client.signIn(chatScene.userBox.getText(),
						chatScene.pwBox.getText().getBytes());
				if (userInstance != null) {
					msgclient = new MessageClient(ip, userInstance, this);
					msgclient.startClient();
					stage.setScene(msg);
					chatScene.clrAll();
				} else
					chatScene.warning.setVisible(true);
			} catch (RemoteException o) {
				chatScene.warning.setVisible(true);
			} catch (MalformedURLException | NotBoundException o) {
				o.printStackTrace();
			}
		}

	}
}