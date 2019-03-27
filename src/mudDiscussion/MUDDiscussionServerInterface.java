package mudDiscussion;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

import mud.MUDClient;

public interface MUDDiscussionServerInterface extends Remote {
	String broadcastMessage(String name, String message) throws RemoteException;

	ArrayList<String> getDiscussion() throws RemoteException;

	void addClient(MUDClient chatInterface) throws RemoteException;

}



