package webchat.networking;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * A generic RMI server from which other clients will inherit.
 * 
 * @author Denton Liu
 * @author Filip Francetic
 * @version 2015-05-25
 */
public abstract class GenericServer {
	
	/** The subdomain location of the server. */
	public static final String URL_LOCATION = null;
	/** The location of the server. */
	private String location;
	/** The {@code Remote} that will be bound to this server. */
	private Remote binding;
	
	/**
	 * 
	 * @param binding
	 *            The {@code Remote} that will be bound to this server.
	 */
	public GenericServer(Remote binding) {
		this.binding = binding;
	}
	
	/**
	 * Starts the server and binds the {@code Remote} object to the specified
	 * location.
	 * 
	 * @throws MalformedURLException
	 * @throws RemoteException
	 * @throws AlreadyBoundException
	 */
	public void startServer() throws MalformedURLException, RemoteException,
			AlreadyBoundException {
		
		System.setSecurityManager(new RMISecurityManager());
		LocateRegistry.createRegistry(1099);
		Registry registry = LocateRegistry.getRegistry();
		registry.rebind(location, binding);
	}
}