package mudCombat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import mud.MUDClient;

public class MUDCombatServerImpl implements MUDCombatServerInterface {
	Monster monster;
	// prepare the input reader
	private static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	private boolean tour;

	@Override
	public void initialize() throws RemoteException {
		System.out.println("hello from server combat");
		monster = new Monster();
		tour = true;
	}

	@Override
	public void combat(String playerName, String piece) throws RemoteException {
		tour = chooseCombatOrEscape(playerName);
		// Player choosed to escape
		while (tour && MUDClient.getInventory() > 0) {
			char winner;
			char[] winners = { 'm', 'p' };

			System.out.println("A combat is taking place in new tour");
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			winner = winners[(new Random()).nextInt(2)];
			if (winner == 'm') {
				System.out.println("This tour is finished, The winner is: The monster");
				monster.setInventory(monster.getInventory() + 1);
				MUDClient.setInventory(MUDClient.getInventory() - 1);
			}

			else if (winner == 'p') {
				System.out.println("This tour is finished, The winner is: The player");
				monster.setInventory(monster.getInventory() - 1);
				MUDClient.setInventory(MUDClient.getInventory() + 1);
			}
			tour = chooseCombatOrEscape(playerName);
		}

	}

	@Override
	public boolean chooseCombatOrEscape(String playerName) throws RemoteException {
		System.out.println();
		System.out.println("There is a monster in this piece, you can choose from one of these commands:");
		System.out.println();
		System.out.println("* Combat  - attack the monster");
		System.out.println("* Escape  - run away from the monster");
		try {
			System.out.println();
			System.out.print(">> ");
			String playerInput = in.readLine().toLowerCase();
			// attack the monster
			if (playerInput.equals("combat")) {

				System.out.println("You choosed to attack the monster");
				return true;

			}
			// run away from the monster
			if (playerInput.equals("escape")) {

				System.out.println("You choosed to escape from the monster");
				MUDClient.displayOptions();
				return false;
			}

		} catch (IOException e) {
			System.err.println("I/O error.");
			System.err.println(e.getMessage());
		}
		return false;
	}


}
