package mocuman.server;

import java.io.IOException;
import java.net.Socket;

import mocuman.packets.PacketMan;

/**
 * 
 * @author Mocuto
 * This object links a client socket to its respective inputstream and output handlers. Identifies 
 * the client socket with an ID
 */
public class ServerClientPair {

	public ServerInputThread inputThread;
	public ServerOutput output;
	public Socket clientSocket;
	public int ID;
	public long lastTimeNoticed = -1;
	public ServerMan server;
	public ServerClientPair(ServerMan server, Socket clientSocket, int ID) throws IOException {
		// TODO Auto-generated constructor stub
		this.server = server;
		this.clientSocket = clientSocket;
		this.ID = ID;
		inputThread = new ServerInputThread(server, this);
		output = new ServerOutput(this);
	}
	//This function takes a PacketMan object and pushes it to the client
	public void takePacket(PacketMan packet)
	{
		output.sendPacket(packet);
	}
	//This function closes the client-specific input and output objects, then attempts to close the client socket
	public void destroy()
	{
		inputThread.destroy();
		output.destroy();
		try
		{
			clientSocket.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

}
