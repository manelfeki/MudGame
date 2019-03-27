package mudDiscussion;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.ArrayList;

import mud.MUDClient;

public class MUDDiscussionServerImpl implements MUDDiscussionServerInterface {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ArrayList<MUDClient> clientList;
	private ArrayList<String> Sendmessage;

	public MUDDiscussionServerImpl() throws RemoteException {
		super();
		clientList = new ArrayList<>();
		Sendmessage = new ArrayList<>();
	}

	@Override
	public void addClient(MUDClient chat) throws RemoteException {
		clientList.add(chat);

	}

	public synchronized String broadcastMessage(String clientname, String message) throws RemoteException {
		System.out.println(clientList);
		for (int i = 0; i < clientList.size(); i++) {
			System.out.println("size" + clientList.size());
			clientList.get(i).sendMessageToClient(clientname.toUpperCase() + " : " + message);
			Sendmessage.add(clientname.toUpperCase() + " : " + message);

			return clientname.toUpperCase() + " : " + message;
		}
		return clientname.toUpperCase() + " : " + message;
	}

	public synchronized ArrayList<String> getDiscussion() throws RemoteException {
		return Sendmessage;
	}

	public static void main(String[] arg) throws RemoteException {
		try {
			Naming.rebind("RMIServer", new MUDDiscussionServerImpl());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
