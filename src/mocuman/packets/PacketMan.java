package mocuman.packets;

import java.io.Serializable;
import java.util.Date;

import mocuman.ClientMan;
import mocuman.server.ServerMan;
/**
 * 
 * @author Mocuto
 * This object is used to send data, functions, and other information to the server and its clients
 */

public class PacketMan implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String type, classname;
	long timeSent;
	public long timeRecieved;
	public int senderID;
	public boolean bounceBack = false;
	public boolean sendToAll = false;
	public int timesSent = 0, maxResends = 2;
	public PacketMan(String type, int ID) 
	{
		this.type = type;
		classname = "PacketMan";
		this.senderID = ID;
		timeSent = new Date().getTime();
	}
	public void clientSideHandle(ClientMan client)
	{
		//Nothing for now
	}
	public void serverSideHandle(ServerMan server)
	{
		//Nothing for now
	}
}
