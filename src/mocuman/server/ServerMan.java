package mocuman.server;

import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;

import org.ini4j.InvalidFileFormatException;
import org.ini4j.Wini;

import mocuman.packets.MessagePacket;
import mocuman.packets.PacketMan;
import mocuman.packets.PacketType;
import mocuman.server.command.KickCommand;
import mocuman.server.command.QuitServer;
import mocuman.server.command.ServerCommand;
/**
 * 
 * @author Mocuto
 * This object acts as the server, connecting to clients with both TCP and UDP and sending packets in the form
 * of PacketMan objects
 * ServerMan uses the ServerClientPair object to communicate with multiple clients, as well
 *  the ServerDatagramThread to send packets to all clients over UDP and the ServerCommandThread to detect and 
 *  execute commands
 */

public class ServerMan {
	private ServerSocket serverSocket = null;
	private DatagramSocket serverDatagram = null;
	protected ArrayList<ServerClientPair> clients = null;
	protected ArrayList<ServerCommand> commands = null;

	public ServerCommandThread serverCommandThread = null;
	public ServerDatagramThread serverDatagramThread = null;
	public boolean isRunning = true;
	public long lastPacketTime = -1;
	public PacketMan lastPacketRecieved = null;
	public static long maxTimeout = 240000; // 240 seconds or 4 minutes
	public static long minTimeout = 120000; // 120 seconds or 2 minutes
	public int lastID = 0;
	public PacketType packetType;
	private int tcpPort, udpReceivePort, udpSendPort;
	
	//public long timeRun = 0, maxTimeRun = 120000;
	public ServerMan() throws IOException 
	{
		clients = new ArrayList<ServerClientPair>();
		commands = new ArrayList<ServerCommand>();
		packetType = new PacketType();
		init();
	}
	public void init() throws IOException
	{
		setConfiguration("serverconfig.ini");
		commands.add(new QuitServer(this));
		commands.add(new KickCommand(this));
		serverCommandThread = new ServerCommandThread(this);
		try {
			System.out.println("Trying to initialize server...");
			serverSocket = new ServerSocket(tcpPort, 2000);
			serverDatagram = new DatagramSocket(udpReceivePort);
			serverDatagramThread = new ServerDatagramThread(this, serverDatagram);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("Server application ending!");
			isRunning = false;
			System.exit(-1);
		}
		System.out.println("Server successfully initialized!" + InetAddress.getLocalHost().getHostAddress() + serverSocket.getLocalPort());
		while(isRunning)
		{
			try
			{
				//Client has connected to the server
				if(!serverSocket.isClosed())
				{
					Socket clientSocket = serverSocket.accept();
					clientSocket.setSoTimeout((int)minTimeout);
					ServerClientPair client = addClient(clientSocket);
					onNewUser(client);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
	
		}
		quit();
	}
	//This function sets the port configuration using an external ini file
	private void setConfiguration(String iniString)
	{
		Wini config = null;
		try {
			config = new Wini(new File(iniString));
		} catch (InvalidFileFormatException e) {
			System.err.println("Invalid file format, exiting.");
			System.exit(-1);
		} catch (IOException e) {
			System.err.println("Could not find configuration file, exiting.");
			System.exit(-1);
		}
		finally {
			tcpPort = config.get("server", "tcpport", int.class);
			udpReceivePort = config.get("server", "udpportr", int.class);
			udpSendPort = config.get("server", "udpports", int.class);
		}
	}
	public static void main(String[] args) throws IOException
	{
		new ServerMan();
	}
	//Callback for when new users connect, may be overridden by child classes
	public void onNewUser(ServerClientPair client)
	{
		MessagePacket mp = new MessagePacket("ID", -1, ((Integer)(client.ID)).toString());
		pushPacketToClient(client, mp);
		System.out.println("Client Connected! ID is now " + client.ID);
	}
	//This function creates a ServerClientPair object to handle the transfer of packets to and from the client.
	public ServerClientPair addClient(Socket clientSocket) throws IOException
	{
		ServerClientPair client = new ServerClientPair(this, clientSocket,
				lastID);
		clients.add(client);
		client.lastTimeNoticed = new Date().getTime();
		lastID++;
		return client;
	}
	//This function returns a client with the corresponding ID. If the client is not found, it will return a null value.
	public ServerClientPair getClientById(int id)
	{
		for (ServerClientPair client : clients)
		{
			if(client.ID == id)
				return client;
		}
		return null;
	}
	//This function returns the command identified by the specified string in the paramater.
	//If the command is not found, it will return a null value
	public ServerCommand getCommand(String ident)
	{
		for(ServerCommand command : commands)
		{
			if(command.hasIdent(ident))
				return command;
		}
		return null;
	}
	public int getUdpSendPort()
	{
		return udpSendPort;
	}
	//This function does packet-specific operations, then updates the time stamps on the packets depature and arrival, and then
	//finally pushes the packet to the clients connected to the server, excluding the original sender of the packet
	public int handlePacket(PacketMan packet)
	{
		//Check out what type of packet it is
		//System.out.println("Sender ID is " + packet.senderID);
		ServerClientPair client = getClientById(packet.senderID);
		
		if(client == null)
			return 0;
		switch(packet.type)
		{
			case "Disconnect Packet":
				System.out.println("Got disconnect packet");
				killClient(getClientById(packet.senderID));
				return 1;
		}
		lastPacketTime = new Date().getTime();
		client.lastTimeNoticed = lastPacketTime;
		packet.timeRecieved = lastPacketTime;
		if(!packetType.packets.containsKey(packet.classname))
		{
			System.out.println("Server does not have packet of this type! " + packet.classname);
			for(PacketMan pp : packetType.packets.values())
				System.out.println("pp name is " + pp.classname);
			return 0;
		}
		//PacketMan newpacket = gson.fromJson(packet.json,packetType.getPacket(packet.classname).getClass());
		packet.serverSideHandle(this);
		pushPacket(packet, packet.senderID);
		return 1;
		
	}
	//This function sends the packet to the client(s)
	//If the packet is set to bounceBack, the packet will only be sent to the original sender. This is used for heartbeats
	//If not, the client is pushed to all clients excluding the original sender
	public int pushPacket(PacketMan packet, int senderID)
	{
		int timesSent = 0;
		if(!packet.bounceBack)
		{
			for(ServerClientPair client : clients)
			{
				if(client.ID != senderID || packet.sendToAll)
				{
					if(pushPacketToClient(client, packet)==1) timesSent++;
				}
			}
		}
		else
		{
			pushPacketToClient(getClientById(packet.senderID), packet);
		}
		return timesSent;
	}
	//This function sends the packet to the specific client
	public int pushPacketToClient(ServerClientPair client, PacketMan packet)
	{
		if(clients.contains(client))
		{
			client.takePacket(packet);
			return 1;
		}
		return 0; //Returns that the server did not recognize the client
	}
	//This function determines whether the client is still connected to the server based on the elapsed time since the client
	//was last "noticed" by the server
	public boolean isClientConnected(ServerClientPair client)
	{
		if(!clients.contains(client))
			return false;
		long currentTime = new Date().getTime();
		if(currentTime - client.lastTimeNoticed > maxTimeout) return false;
		else return true;
	}
	//This function terminates all client related threads and processes and closes the serverSocket, usually followed by the
	//Termination of the program
	public void quit() throws IOException
	{
		killAllClients();
		isRunning = false;
		serverSocket.close();
		serverDatagram.close();
		System.out.println("Server application ending!");
		System.exit(1);
	}
	//callback for when a client has disconnected from the server
	public void onClientDisconnect(ServerClientPair client)
	{
		System.out.println("Client ID " + client.ID + " has disconnected!");
	}
	//This function terminates all client related threads and processes
	public void killAllClients()
	{
		for(ServerClientPair client : clients)
		{
			client.destroy();
		}
		clients.clear();
	}
	//This function terminates the specific client
	public int killClient(ServerClientPair client)
	{
		if(clients.contains(client))
		{
			client.destroy();
			onClientDisconnect(client);
			clients.remove(client);
			return 1;
		}
		else
			return 0; //Returns that the server did not recognize the client
	}
	//This function kicks the client from the server
	public int kickClient(int ID)
	{
		System.out.println("Now kicking User " + ID);
		MessagePacket mp = new MessagePacket("Server Message", -1, "You are now being kicked from the server...");
		ServerClientPair client = getClientById(ID);
		if(client == null)
		{
			System.out.println("User not found!");
			return 0;
		}
		pushPacketToClient(client, mp);
		return killClient(client); //Returns 1 if the client was succesfully kicked
	}
}
