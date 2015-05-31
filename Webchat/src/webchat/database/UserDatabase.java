package webchat.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import webchat.users.User;

/**
 * A database which will store all of the {@code User}s.
 * 
 * @author Denton Liu
 * @version 2015-05-28
 *
 */
public class UserDatabase {
	
	/** The collection of all the {@code User} objects. */
	private Collection<User> users;
	
	/**
	 * Creates a new database with no {@code User}s.
	 */
	public UserDatabase() {
		users = new ArrayList<>();
	}
	
	/**
	 * Creates a new {@code UserDatabase} from a serialized
	 * {@code Collection<User>} which is specified by {@code file}.
	 * 
	 * @param file
	 *            The physical location of the {@code Collection<User>}.
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public UserDatabase(File file) throws FileNotFoundException, IOException,
			ClassNotFoundException {
		
		ObjectInputStream objectInputStream = new ObjectInputStream(
				new FileInputStream(file));
		this.users = (Collection<User>) objectInputStream.readObject();
		objectInputStream.close();
		
		// clears userInstances
		for (Iterator<User> user = users.iterator(); user.hasNext();) {
			user.next().setUserInstance(null);
		}
	}
	
	/**
	 * Saves the internal {@code Collection<User>} to a file as specified by
	 * {@code file}.
	 * 
	 * @param file
	 *            The location to be saved to.
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void saveDatabase(File file) throws FileNotFoundException,
			IOException {
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(
				new FileOutputStream(file));
		objectOutputStream.writeObject(this.users);
		objectOutputStream.close();
	}
	
	/**
	 * Adds a new user to the database.
	 * 
	 * @param username
	 *            The username.
	 * @param passwordHash
	 *            The password after it has been hashed by the server.
	 * @return {@code true} if the operation was successful, otherwise
	 *         {@code false}.
	 */
	public boolean addUser(String username, byte[] passwordHash) {
		for (Iterator<User> iterator = users.iterator(); iterator.hasNext();) {
			if (iterator.next().getUsername().equals(username)) {
				return false;
			}
		}
		users.add(new User(username, passwordHash.clone()));
		return true;
	}
	
	/**
	 * Verifies that a username and password are correct.
	 * 
	 * @param username
	 *            The username to be verified.
	 * @param passwordHash
	 *            The password after it has been hashed by the server to be
	 *            verified.
	 * @return {@code true} if the username can be found and
	 *         {@code passwordHash} matches, otherwise {@code false}.
	 */
	public boolean isCorrectUserAndPassword(String username, byte[] passwordHash) {
		for (Iterator<User> iterator = users.iterator(); iterator.hasNext();) {
			User user = iterator.next();
			if (user.getUsername().equals(username)) {
				return Arrays.equals(user.getPasswordHash(), passwordHash);
			}
		}
		
		return false;
	}
	
	/**
	 * Sets the {@code userInstance} of a {@code User} in the database to the
	 * one specified.
	 * 
	 * @param username
	 *            The username of the {@code User} whose {@code userInstance}
	 *            will be set.
	 * @param userInstance
	 *            The {@code byte[]} that it will be set to.
	 */
	public void setUserInstance(String username, byte[] userInstance) {
		for (Iterator<User> iterator = users.iterator(); iterator.hasNext();) {
			User user = iterator.next();
			if (user.getUsername().equals(username)) {
				user.setUserInstance(userInstance);
				break;
			}
		}
	}
	
	/**
	 * Returns the username which is associated with a particular
	 * {@code userInstance}.
	 * 
	 * @param userInstance
	 * @return The username which is associated with a particular
	 *         {@code userInstance}.
	 */
	public String getUsernameFromUserInstance(byte[] userInstance) {
		for (Iterator<User> iterator = users.iterator(); iterator.hasNext();) {
			User user = iterator.next();
			if (Arrays.equals(user.getUserInstance(), userInstance)) {
				return user.getUsername();
			}
		}
		
		return null;
	}
}
