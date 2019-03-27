/***********************************************************************
 * mud.MUDServerInterface
 ***********************************************************************/

package mud;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;

import mudCombat.MUDCombatServerInterface;
import mudDiscussion.MUDDiscussionServerInterface;

public interface MUDServerInterface extends Remote {

	public void initialize() throws RemoteException;

	public String createUser(String playerName, String mudName) throws RemoteException;

	public boolean checkIfPlayerLimitNotExceeded() throws RemoteException;

	public String moveUser(String currentLocation, String direction, String playerName) throws RemoteException;

	public String getStartLocation() throws RemoteException;

	public String getCurrentLocationInfo(String currentLocation) throws RemoteException;

	public String[] getCurrentPlayersInMUD() throws RemoteException;

	public void exit(String playerName) throws RemoteException;

	public MUDCombatServerInterface getCombat(String hostname, int port) throws RemoteException, NamingException;

	public MUDDiscussionServerInterface getDiscussion(String hostname, int port)
			throws RemoteException, NamingException;

	public Boolean ExistsInMud(String playerName) throws RemoteException;

	public int getPlayerInventoryByName(String playerName) throws RemoteException;

	public void updatePlayerInventory(String playerName) throws RemoteException;

	public List<String> getCurrentPlayersInSamePosition(String position) throws RemoteException;

	public String getPlayerLocationInMUD(String playerName) throws RemoteException;

	public int getCurrentPlayersNulberInSamePosition() throws RemoteException;

	public Map<String, Integer> getCurrentPlayers() throws RemoteException;

}