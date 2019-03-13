/***********************************************************************
 * mud.MUDServerMainline
 ***********************************************************************/

package mud;

import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.server.UnicastRemoteObject;

import mudCombat.MUDCombatServerImpl;
import mudCombat.MUDCombatServerInterface;
import mudDiscussion.MUDDiscussionServerImpl;
import mudDiscussion.MUDDiscussionServerInterface;

public class MUDServerMainline {

	public static void main(String args[]) {

		if (args.length < 2) {
			System.err.println("Usage:\njava MUDServerMainline <registryport> <serverport>");
			return;
		}

		try {
			String hostname = (InetAddress.getLocalHost()).getCanonicalHostName();
			int registryport = Integer.parseInt(args[0]);
			int serverport = Integer.parseInt(args[1]);

			System.setProperty("java.security.policy", "mud.policy");
			System.setSecurityManager(new SecurityManager());

			MUDServerImpl mudserver = new MUDServerImpl();
			MUDServerInterface mudstub = (MUDServerInterface) UnicastRemoteObject.exportObject(mudserver, serverport);

			String regURL = "rmi://" + hostname + ":" + registryport + "/MUDServer";
			System.out.println("Registering " + regURL);
			Naming.rebind(regURL, mudstub);

			MUDCombatServerImpl mudCombatserver = new MUDCombatServerImpl();
			MUDCombatServerInterface mudCombatstub = (MUDCombatServerInterface) UnicastRemoteObject
					.exportObject(mudCombatserver, serverport);

			String uRL = "rmi://" + hostname + ":" + registryport + "/MUDCombatServer";
			System.out.println("Registering " + uRL);
			Naming.rebind(uRL, mudCombatstub);

			MUDDiscussionServerImpl mudDiscussionserver = new MUDDiscussionServerImpl();
			MUDDiscussionServerInterface mudDiscussionstub = (MUDDiscussionServerInterface) UnicastRemoteObject
					.exportObject(mudDiscussionserver, serverport);

			String discussionuRL = "rmi://" + hostname + ":" + registryport + "/MUDDiscussionServer";
			System.out.println("Registering " + discussionuRL);
			Naming.rebind(discussionuRL, mudDiscussionstub);

		} catch (java.net.UnknownHostException e) {
			System.err.println("Cannot get local host name.");
			System.err.println(e.getMessage());
		} catch (java.io.IOException e) {
			System.err.println("Failed to register.");
			System.err.println(e.getMessage());
		}
	}
}