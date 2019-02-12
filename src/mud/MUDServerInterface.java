/***********************************************************************
 * mud.MUDServerInterface
 ***********************************************************************/

package mud;

import java.rmi.Remote;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import mudCombat.MUDCombatServerInterface;

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

	public Boolean ExistsInMud(String playerName) throws RemoteException;

	public int NumberOfLives(String playerName) throws RemoteException;

	public int getPlayerInventoryByName(String playerName) throws RemoteException;
}