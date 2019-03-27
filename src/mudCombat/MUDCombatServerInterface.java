/***********************************************************************
 * mud.MUDCombatServerInterface
 ***********************************************************************/

package mudCombat;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MUDCombatServerInterface extends Remote {

	public void initialize(Monster monster, int inventory) throws RemoteException, IOException;

	public void updateMonsterInventory(Monster monster) throws RemoteException;

	public char getWinner(char[] winners) throws RemoteException;

	public int getInventory(Monster monster) throws RemoteException;

	public String getWinnerTwoPlayers(String[] winners) throws RemoteException;
}