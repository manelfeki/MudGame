/***********************************************************************
 * mud.MUDClient
 ***********************************************************************/

package mud;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.concurrent.TimeUnit;

import javax.naming.NamingException;

import mudCombat.MUDCombatServerInterface;
import mudCombat.Monster;

public class MUDClient {

	// prepare the input reader
	private static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

	// the player's name that they will be using throughout the game
	private static String playerName = "";

	private static String hostname;

	private static int port;

	private static Monster monster;

	// specify whether the game is running or not
	private static boolean running = false;

	// the name of the MUD that the player is currently on
	private static String mudName = "";

	// remote server interface
	private static MUDServerInterface serv;

	private static MUDCombatServerInterface combatServ;

	// current location of the player
	private static String currentLocation = "";

	// the number of life points that the player is currently carrying
	public static Integer inventory = 10;

	public static String getPlayerName() {
		return playerName;
	}

	public static void setPlayerName(String playerName) {
		MUDClient.playerName = playerName;
	}

	public static String getCurrentLocation() {
		return currentLocation;
	}

	public static void setCurrentLocation(String currentLocation) {
		MUDClient.currentLocation = currentLocation;
	}

	public static Integer getInventory() {
		return inventory;
	}

	public static void setInventory(Integer inventory) {
		MUDClient.inventory = inventory;
	}

	// main game class
	public static void main(String args[]) throws RemoteException, NamingException {

		if (args.length < 2) {
			System.err.println("Usage:\njava MUDClient <host> <port>");
			return;
		}

		hostname = args[0];
		port = Integer.parseInt(args[1]);

		System.setProperty("java.security.policy", "mud.policy");
		System.setSecurityManager(new SecurityManager());

		try {
			String regURL = "rmi://" + hostname + ":" + port + "/MUDServer";
			System.out.println("Looking up " + regURL);

			serv = (MUDServerInterface) Naming.lookup(regURL);

			// prepare the MUD
			serv.initialize();

			// set up the game
			System.out.println("Welcome!");
			System.out.println("What is your name?");
			try {
				System.out.print(">> ");
				playerName = in.readLine();
			} catch (IOException e) {
				System.err.println("I/O error.");
				System.err.println(e.getMessage());
			}
			if (serv.ExistsInMud(playerName)) {
				System.out.println("You are already a player and you have " + serv.getPlayerInventoryByName(playerName)
						+ "points");
			} else {
				System.out.println("Nice to meet you, " + playerName);
				System.out.println();
				System.out.println("Let's begin");

				joinMUD();
				// System.out.println(MUDServerImpl.currentInstance.players.size());
			}
			running = true;
			currentLocation = serv.getStartLocation();
			displayOptions();

			runGame();
		} catch (IOException e) {
			System.err.println("I/O error.");
			System.err.println(e.getMessage());
		} catch (java.rmi.NotBoundException e) {
			System.err.println("Server not bound.");
			System.err.println(e.getMessage());
		}
	}

	// runs the whole game, while 'running' variable is true
	// gets the player's input and displays the output
	private static void runGame() throws RemoteException, NamingException {

		while (running)
			try {
				System.out.println();
				System.out.print(">> ");
				String playerInput = in.readLine().toLowerCase();
				handlePlayerInput(playerInput);

			} catch (IOException e) {
				System.err.println("I/O error.");
				System.err.println(e.getMessage());
			}
	}

	// handle an input from the player
	private static void handlePlayerInput(String playerInput) throws NamingException, IOException {

		// move the user in a given direction
		if (playerInput.contains("move")) {

			// get the direction where the player wants to move
			String[] directionString = playerInput.split(" ");

			// if the server returns the same location as the player is at right now,
			// it means that there is no path to that direction
			if (currentLocation.equals(serv.moveUser(currentLocation, directionString[1], playerName))) {
				System.out
						.println("Sorry, either there isn't a path to this direction, or this direction is not valid.");
			} else {
				// move the player and display information about the new location
				System.out.println("You are going " + directionString[1] + "...");
				currentLocation = serv.moveUser(currentLocation, directionString[1], playerName);
				monster = new Monster(directionString[1]);
				System.out.println("monster");
				System.out.println(serv.getCurrentLocationInfo(currentLocation));
				combatServ = serv.getCombat(hostname, port);
				combatServ.initialize(monster, serv.getPlayerInventoryByName(playerName));
				boolean tour = true;
				// Player choosed to escape
				while (tour && serv.getPlayerInventoryByName(playerName) > 0 && combatServ.getInventory(monster) > 0) {
					char winner;
					char[] winners = { 'm', 'p' };

					System.out.println("A combat is taking place in new tour");
					try {
						TimeUnit.SECONDS.sleep(1);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					winner = combatServ.getWinner(winners);
					if (winner == 'm') {
						System.out.println("This tour is finished, The winner is: The monster");
						serv.updatePlayerInventory(playerName);
					}

					else if (winner == 'p') {
						System.out.println("This tour is finished, The winner is: The player");
						combatServ.updateMonsterInventory(monster);
					}
					System.out.println("Player inventory:" + serv.getPlayerInventoryByName(playerName));
					System.out.println("Monster inventory:" + combatServ.getInventory(monster));
					tour = chooseCombatOrEscape(playerName);
				}
				if (serv.getPlayerInventoryByName(playerName) == 0)
					System.out.println("You are dead");
				else if (combatServ.getInventory(monster) == 0)
					System.out.println("The monster is dead, You can go on");
			}
		}

		// display the contents of player's inventory
		if (playerInput.equals("inventory")) {
			int playerInventory = serv.getPlayerInventoryByName(playerName);

			// if there isn't any items, inform the player that the inventory is empty
			if (playerInventory < 1) {
				System.out.println("You are dead");
			} else {

				// otherwise, display the number of life point
				System.out.println("You have:" + playerInventory);
			}
		}

		// get the information about player's surroundings
		if (playerInput.equals("location")) {
			System.out.println("You look around...");
			System.out.println(serv.getCurrentLocationInfo(currentLocation));
		}

		// display the list of players currently playing in the MUD"
		if (playerInput.equals("players")) {

			// get the list of all players in the MUD
			String[] currentPlayers = serv.getCurrentPlayersInMUD();
			// check if the player is the only player in the MUD and inform them about it
			if (currentPlayers.length < 2) {
				System.out.println("You're the only player in this MUD.");
			} else {
				// otherwise, list all the players
				System.out.println("Currently, these players are playing in this MUD: ");
				System.out.println();

				for (String name : currentPlayers) {
					System.out.println("* " + name);
				}
			}
		}

		// print the available commands to the player
		if (playerInput.equals("help")) {
			displayOptions();
		}

		// exit the game
		if (playerInput.equals("exit")) {

			inventory = 10;

			serv.exit(playerName);
			running = false;
		}

	}

	// displays all possible command options to the player
	public static void displayOptions() {
		System.out.println();
		System.out.println("You can choose from one of these commands:");
		System.out.println();
		System.out.println("* Move <direction>  - move to a selected direction (north, east, south, west)");
		System.out.println("* Inventory  - see your number of life points");
		System.out.println("* Location  - display the information about your surroundings");
		System.out.println("* Players  - display the list of players currently playing in the same MUD");
		System.out.println("* Discussion <playerName>  - Discuss with another player");
		System.out.println("* Help  - display the available commands");
		System.out.println("* Exit  - exit the game");
	}

	// allows the player to join one of the existing MUDs
	private static void joinMUD() throws RemoteException {
		mudName = "myMUD";

		// check if the current total number of players online is not exceeding the
		// maximum, otherwise terminate the program
		if (!serv.checkIfPlayerLimitNotExceeded()) {
			System.out
					.println("Sorry, the total number of available players has been exceeded. Please try again later.");
			System.exit(0);
		} else {

			// move the player to the MUD
			System.out.println(serv.createUser(playerName, mudName));
		}
	}

	public static boolean chooseCombatOrEscape(String playerName) {
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
				displayOptions();
				return false;
			} else {
				System.out.println("You should choose escape or combat");
			}

		} catch (IOException e) {
			System.err.println("I/O error.");
			System.err.println(e.getMessage());
		}
		return false;
	}
}