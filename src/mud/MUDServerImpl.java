/***********************************************************************
 * mud.MUDServerImpl
 ***********************************************************************/

package mud;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import mudCombat.MUDCombatServerInterface;
import mudDiscussion.MUDDiscussionServerInterface;

public class MUDServerImpl implements MUDServerInterface {

	// all available MUDs
	// private Map<String, MUD> MUDs = new HashMap<String, MUD>();

	// stores the current MUD that the player is on
	private MUD currentInstance;

	// number of players currently online throughout all MUDs
	// stores the name of the player and the name of the MUD that the player is on
	public static Map<String, Integer> currentPlayers = new HashMap<String, Integer>();

	public static Map<String, Integer> monsters = new HashMap<String, Integer>();

	// maximum number of player currently online
	private static int maxNumberOfPlayers = 10;

	// maximum number of player currently playing in a single MUD
	private static int maxNumberOfPlayersInMUD = 5;

	public MUDServerImpl() throws RemoteException {
	}

	// create the MUD
	public void initialize() {
		// MUDs.put("myMUD", new MUD("mymud.edg", "mymud.msg", "mymud.thg"));
		monsters.put("north", 5);
		monsters.put("west", 5);
		monsters.put("east", 5);
		monsters.put("south", 5);
		currentInstance = new MUD("mymud.edg", "mymud.msg", "mymud.thg");
	}

	// create user
	// returns the location information about the players starting location
	public String createUser(String playerName, String mudName) {
		System.out.println(currentPlayers);

		if (currentPlayers.containsKey(playerName)) {
			System.out.println(playerName + " is already a player  ");
		} else {
			System.out.println("The player " + playerName + " has joined the " + mudName + " MUD.");
			currentInstance.addThing(currentInstance.startLocation(), playerName);
			currentInstance.players.put(playerName.toLowerCase(), currentInstance.startLocation());

			// add the player to the current players list with the MUD
			currentPlayers.put(playerName.toLowerCase(), MUDClient.inventory);
			System.out.println(currentInstance.players.size());
		}

		return currentInstance.locationInfo(currentInstance.startLocation());
	}

	// checks if the current number of players online is not exceeding the maximum
	// number of players
	public boolean checkIfPlayerLimitNotExceeded() {
		if (currentPlayers.size() < maxNumberOfPlayers) {
			return true;
		} else {
			return false;
		}
	}

	// move the user to a give direction
	// returns a string telling that the player has moved a given direction
	public String moveUser(String currentLocation, String direction, String playerName) {
		currentInstance.players.remove(playerName.toLowerCase());
		currentInstance.players.put(playerName.toLowerCase(), direction);
		return currentInstance.moveThing(currentLocation, direction, playerName.toLowerCase());
	}

	// returns the start location of the MUD
	public String getStartLocation() {
		return currentInstance.startLocation();
	}

	// returns the information about the player's current location
	public String getCurrentLocationInfo(String currentLocation) {
		return currentInstance.locationInfo(currentLocation);
	}

	// deletes an object from the location by putting it in the player's inventory
	public void pickUpItem(String currentLocation, String item) {
		currentInstance.delThing(currentLocation, item);
	}

	// returns a list of all players that are currently playing in the same MUD
	public String[] getCurrentPlayersInMUD() {
		// System.out.println(currentInstance.players.keySet().size());
		return currentPlayers.keySet().toArray(new String[currentInstance.players.keySet().size()]);
	}

	public Map<String, Integer> getCurrentPlayers() {
		return currentPlayers;
	}

	// handles the player exiting the MUD by removing it from the players' list
	public void exit(String playerName) {
		currentInstance.players.remove(playerName.toLowerCase());
		currentPlayers.remove(playerName.toLowerCase());
		System.out.println("The player " + playerName + " has left the server.");
	}

	@Override
	public MUDCombatServerInterface getCombat(String name, int port) throws RemoteException, NamingException {
		Context namingContext = new InitialContext();
		String regURL = "rmi://" + name + ":" + port + "/MUDCombatServer";
		return (MUDCombatServerInterface) namingContext.lookup(regURL);
	}

	@Override
	public Boolean ExistsInMud(String playerName) {
		if (currentPlayers.containsKey(playerName.toLowerCase())) {
			System.out.println(playerName + " is here");
			return true;
		} else
			return false;
	}

	@Override
	public int getPlayerInventoryByName(String playerName) {
		return currentPlayers.get(playerName.toLowerCase());
	}

	@Override
	public void updatePlayerInventory(String playerName) throws RemoteException {
		// TODO Auto-generated method stub

		currentPlayers.put(playerName, currentPlayers.get(playerName) - 1);

	}

	@Override
	public String getPlayerLocationInMUD(String playerName) throws RemoteException {
		// TODO Auto-generated method stub
		return currentInstance.players.get(playerName);
	}

	@Override
	public int getCurrentPlayersNulberInSamePosition() throws RemoteException {

		// TODO Auto-generated method stub

		return currentInstance.players.size();

	}

	@Override
	public MUDDiscussionServerInterface getDiscussion(String hostname, int port)
			throws RemoteException, NamingException {
		Context namingContext = new InitialContext();
		String regURL = "rmi://" + hostname + ":" + port + "/MUDDiscussionServer";
		return (MUDDiscussionServerInterface) namingContext.lookup(regURL);
	}

	@Override
	public List<String> getCurrentPlayersInSamePosition(String position) throws RemoteException {
		System.out.println("Avant players : " + position + currentInstance.players);
		List<String> listOfKeys = null;

		// Check if Map contains the given value
		if (currentInstance.players.containsValue(position)) {
			// Create an Empty List
			listOfKeys = new ArrayList<>();

			// Iterate over each entry of map using entrySet
			for (Map.Entry<String, String> entry : currentInstance.players.entrySet()) {
				// Check if value matches with given value
				if (entry.getValue().equals(position)) {
					// Store the key from entry to the list
					listOfKeys.add(entry.getKey());
				}
			}
		}
		System.out.println("players :" + listOfKeys);

		return listOfKeys;
	}

}