package mocuman.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import mocuman.Global;
import mocuman.packets.PacketHolder;
import mocuman.packets.PacketMan;
/**
 * 
 * @author Mocuto
 * This objectThis object is used by the ServerMan and its child classes to take PacketMan objects convert 
 * them into multi-language JSON String and sends it over the output stream
 */
public final class ServerOutput {
	
	private final PrintWriter out;
	//private ServerClientPair client = null;
	//private ServerMan server = null;
	private final Socket clientSocket;
	public ServerOutput(ServerClientPair client) throws IOException {
		//this.server = server;
		//this.client = client;
		this.clientSocket = client.clientSocket;
		out = new PrintWriter(clientSocket.getOutputStream(), true);
	}
	//This function converts a PacketMan object to a multi-language compatible JSON string object, then sends it
	public void sendPacket(PacketMan packet)
	{
		packet.timesSent += 1;
		String packetString = Global.gson.toJson(new PacketHolder(packet));
		out.println(packetString);
	}
	//This function closes the PrintWriter Outputstream
	public void destroy()
	{
		out.close();
	}
}
