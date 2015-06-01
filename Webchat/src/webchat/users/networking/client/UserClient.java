package webchat.users.networking.client;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import webchat.networking.GenericClient;
import webchat.ui.ClientUI;
import webchat.users.PasswordManager;
import webchat.users.networking.UserInterface;
import webchat.users.networking.server.UserServer;

/**
 * The client which will communicate with {@code UserClient}.
 * 
 * @author Denton Liu
 * @version 2015-05-25
 */
public class UserClient extends GenericClient {
	
	/** The time in milliseconds between sucessive message pulls. */
	public static final long REFRESH_RATE = 5000;
	
	/**
	 * The {@code UserInterface} which will be used to communicate with the
	 * server.
	 */
	private UserInterface userInterface;
	/** The {@code Runnable} which will contain the logic to find online users. */
	private OnlineUserRetriever onlineUserRetriever;
	/** A {@code Thread} which will be running to get online users. */
	private Thread userThread;
	
	/**
	 * 
	 * @param serverURL
	 *            The URL of the server.
	 * @throws MalformedURLException
	 * @throws NotBoundException
	 * @throws RemoteException
	 */
	public UserClient(String serverURL, ClientUI ui)
			throws MalformedURLException, NotBoundException, RemoteException {
		super(serverURL, UserServer.URL_LOCATION);
		this.userInterface = (UserInterface) remoteInterface;
		this.onlineUserRetriever = new OnlineUserRetriever(userInterface,
				REFRESH_RATE, ui);
		this.userThread = new Thread(onlineUserRetriever);
	}
	
	public void startClient() {
		userThread.start();
	}
	
	public void stopClient() {
		onlineUserRetriever.stopThread();
	}
	
	/**
	 * Registers a user with the server.
	 * 
	 * @param username
	 *            The username.
	 * @param password
	 *            The password.
	 * @return {@code true} if the registration was successful, otherwise
	 *         {@code false}.
	 */
	public boolean register(String username, byte[] password)
			throws RemoteException {
		byte[] hashedPass = PasswordManager.clientHash(password, username);
		PasswordManager.clearArray(password);
		try {
			return userInterface.register(username, hashedPass);
		} catch (RemoteException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Signs in a user and gets a uniquely identifying {@code byte[]}.
	 * 
	 * @param username
	 *            The username to sign in.
	 * @param password
	 *            The password of the user.
	 * @return {@code null} if the sign in was unsuccessful, otherwise a
	 *         uniquely identifying {@code byte[]}.
	 */
	public byte[] signIn(String username, byte[] password)
			throws RemoteException {
		byte[] hashedPass = PasswordManager.clientHash(password, username);
		PasswordManager.clearArray(password);
		try {
			return userInterface.signIn(username, hashedPass);
		} catch (RemoteException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Signs out a user according to their uniquely identifying {@code byte[]}.
	 * 
	 * @param userInstance
	 *            The uniquely identifying {@code byte[]} to sign out.
	 */
	public void logout(byte[] userInstance) throws RemoteException {
		try {
			userInterface.logout(userInstance);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
}
