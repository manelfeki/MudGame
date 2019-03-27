/***********************************************************************
 * mud.MUDClient
 ***********************************************************************/

package mud;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.naming.NamingException;

import mudCombat.MUDCombatServerInterface;
import mudCombat.Monster;
import mudDiscussion.MUDDiscussionServerInterface;

public class MUDClient extends Thread implements Serializable {

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

	private static MUDDiscussionServerInterface discussionServer;

	// current location of the player
	private static String currentLocation = "";

	// the number of life points that the player is currently carrying
	public static Integer inventory = 10;
	String NameSender;

	public MUDClient(String playerName) {
		// TODO Auto-generated constructor stub
		this.NameSender = playerName.toLowerCase();
	}

	public static String getplayerName() {
		return playerName.toLowerCase();
	}

	public static void setplayerName(String playerName) {
		MUDClient.playerName = playerName.toLowerCase();
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
				playerName = in.readLine().toLowerCase();
			} catch (IOException e) {
				System.err.println("I/O error.");
				System.err.println(e.getMessage());
			}
			if (serv.ExistsInMud(playerName.toLowerCase().toLowerCase())) {
				System.out.println("You are already a player and you have "
						+ serv.getPlayerInventoryByName(playerName.toLowerCase().toLowerCase()) + "points");
			} else {
				System.out.println("Nice to meet you, " + playerName.toLowerCase().toLowerCase());
				System.out.println();
				System.out.println("Let's begin");

				joinMUD();

				// System.out.println(MUDServerImpl.currentInstance.players.size());
				discussionServer = serv.getDiscussion(hostname, port);

				// discussionServer.addClient(new MUDClient(playerName.toLowerCase()));

			}
			running = true;
			currentLocation = serv.getStartLocation();
			System.out.println("hello Current Location:" + currentLocation);
			displayOptions();

			runGame();
		} catch (

		IOException e) {
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

		List<String> PlayersInSamePiece = null;
		PlayersInSamePiece = serv.getCurrentPlayersInSamePosition(serv.getPlayerLocationInMUD(playerName));
		// Discussion with player in same position
		if (playerInput.startsWith("\"")) {
			int i;
			String s = "";

			System.out.println(PlayersInSamePiece);
			if (PlayersInSamePiece.size() == 1) {
				System.out.println("You are alone in this piece there is no other player to discuss with");
			} else if (PlayersInSamePiece.size() > 1) {
				for (String player : PlayersInSamePiece)

				{
					s = s + player + " & ";
					System.out.println(player);
					discussionServer.addClient(new MUDClient(player));
				}
				String message = playerInput.substring(1);
				// sending the message via MUDDiscussionServer
				System.out.println(discussionServer.broadcastMessage(playerName, message));
				System.out.println("Your message is sent to other players in the same piece:" + s);

			}

		}

		// move the user in a given direction
		if (playerInput.contains("move")) {

			// get the direction where the player wants to move
			String[] directionString = playerInput.split(" ");

			// if the server returns the same location as the player is at right now,
			// it means that there is no path to that direction
			if (currentLocation.equals(serv.moveUser(currentLocation, directionString[1], playerName.toLowerCase()))) {
				System.out
						.println("Sorry, either there isn't a path to this direction, or this direction is not valid.");
			} else {
				// move the player and display information about the new location
				System.out.println("You are going " + directionString[1] + "...");
				currentLocation = serv.moveUser(currentLocation, directionString[1], playerName.toLowerCase());

				monster = new Monster(directionString[1]);
				System.out.println("monster");
				System.out.println(serv.getCurrentLocationInfo(currentLocation));
				combatServ = serv.getCombat(hostname, port);
				combatServ.initialize(monster, serv.getPlayerInventoryByName(playerName.toLowerCase()));
				boolean tour = true;
				// Player choosed to escape
				while (tour && serv.getPlayerInventoryByName(playerName.toLowerCase()) > 0
						&& combatServ.getInventory(monster) > 0) {
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
						serv.updatePlayerInventory(playerName.toLowerCase());
					}

					else if (winner == 'p') {
						System.out.println("This tour is finished, The winner is: The player");
						combatServ.updateMonsterInventory(monster);
					}
					System.out.println("Player inventory:" + serv.getPlayerInventoryByName(playerName.toLowerCase()));
					System.out.println("Monster inventory:" + combatServ.getInventory(monster));
					tour = chooseCombatOrEscape(playerName.toLowerCase());
				}
				if (serv.getPlayerInventoryByName(playerName.toLowerCase()) == 0)
					System.out.println("You are dead");
				else if (combatServ.getInventory(monster) == 0)
					System.out.println("The monster is dead, You can go on");
			}
		}

		// display the contents of player's inventory
		if (playerInput.equals("inventory")) {
			int playerInventory = serv.getPlayerInventoryByName(playerName.toLowerCase());

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

		if (playerInput.contains("attack")) {
			String[] player = playerInput.split(" ");
			if (player[1].equals(playerName)) {
				System.out.println("You can't attack yourself!!");
			} else if (!PlayersInSamePiece.contains(player[1]))
				System.out.println("You are not in the same piece with this player!!");

			else {
				System.out.println("You choosed to attack " + player[1] + " So get ready !!");
				combatServ = serv.getCombat(hostname, port);
				String winner;
				String[] winners = new String[2];
				winners[0] = player[1];
				winners[1] = playerName.toLowerCase();
				System.out.println("A combat is taking place");
				try {
					TimeUnit.SECONDS.sleep(1);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				winner = combatServ.getWinnerTwoPlayers(winners);
				System.out.println("The winner is: " + winner);
				if (winner.equals(playerName.toLowerCase())) {
					serv.updatePlayerInventory(winners[0]);
				} else
					serv.updatePlayerInventory(playerName.toLowerCase());
				System.out.println(playerName.toLowerCase() + " inventory:"
						+ serv.getPlayerInventoryByName(playerName.toLowerCase().toLowerCase()));
				System.out.println(player[1] + " inventory:" + serv.getPlayerInventoryByName(player[1].toLowerCase()));
			}
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
					System.out.println("* " + name + " is already in " + serv.getPlayerLocationInMUD(name));
				}
			}
		}

		// print the available commands to the player
		if (playerInput.equals("help")) {
			displayOptions();
		}
		// show the discussion with the players in same piece
		if (playerInput.equals("discuss")) {
			// System.out.println("discuss" + PlayersInSamePiece);
			if (PlayersInSamePiece.size() == 1) {
				System.out.println("Sorry but you have no message because you are alone in this piece");
			} else if (PlayersInSamePiece.size() > 1)
				for (int i = 0; i < discussionServer.getDiscussion().size(); i++)
					System.out.println(discussionServer.getDiscussion().get(i));

		}
		// exit the game
		if (playerInput.equals("exit")) {

			inventory = 10;

			serv.exit(playerName.toLowerCase());
			running = false;
		}

	}

	public void sendMessageToClient(String message) throws RemoteException {
		System.out.println("from client");
		System.out.println("sendfromclient" + message);
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
		System.out.println("* Help  - display the available commands");
		System.out.println("* Discuss  - check if there are new messages");
		System.out.println("* Attack <playerName.toLowerCase()>  - attack a player in the same piece");
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
			System.out.println(serv.createUser(playerName.toLowerCase(), mudName));
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
				System.out.println(serv.getCurrentLocationInfo(currentLocation));
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