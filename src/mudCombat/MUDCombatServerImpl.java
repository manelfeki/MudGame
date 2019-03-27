package mudCombat;

import java.rmi.RemoteException;
import java.util.Random;

public class MUDCombatServerImpl implements MUDCombatServerInterface {
	private Monster monster;

	@Override
	public void initialize(Monster monster, int inventory) throws RemoteException {
		this.monster = monster;
	}

	@Override
	public void updateMonsterInventory(Monster monster) throws RemoteException {
		// monster.setInventory(monster.getInventory() - 1);
		this.monster.setInventory(this.monster.getInventory() - 1);

	}

	@Override
	public char getWinner(char[] winners) throws RemoteException {
		// TODO Auto-generated method stub

		return winners[(new Random()).nextInt(2)];
	}

	@Override
	public int getInventory(Monster monster) throws RemoteException {
		// TODO Auto-generated method stub
		return this.monster.getInventory();
	}

	@Override
	public String getWinnerTwoPlayers(String[] winners) throws RemoteException {
		// TODO Auto-generated method stub
		return winners[(new Random()).nextInt(2)];
	}

}
