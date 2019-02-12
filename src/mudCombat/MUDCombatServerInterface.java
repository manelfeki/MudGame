/***********************************************************************
 * mud.MUDCombatServerInterface
 ***********************************************************************/

package mudCombat;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MUDCombatServerInterface extends Remote {

	public void initialize() throws RemoteException;

	public void combat(String playerName, String piece) throws RemoteException;

	public boolean chooseCombatOrEscape(String playerName) throws RemoteException;
}