package mocuman.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

import mocuman.Global;
import mocuman.packets.PacketHolder;
import mocuman.packets.PacketMan;

import com.google.gson.Gson;

/**
 * 
 * @author Mocuto
 * This object is used by the ServerMan and its child classes to take PacketMan objects, convert them into
 * binary data, and send them through the Datagram socket
 */
public class ServerDatagramThread extends Thread {

	public ServerMan server = null;
	DatagramSocket socket = null;
	public ServerDatagramThread(ServerMan server, DatagramSocket dgs) {
		this.server = server;
		this.socket = dgs;
		
		this.start();
	}
	//This function converts a PacketMan object to a multi-language compatible JSON strong object, then sends
	//it as a datagram
	public void sendDatagram(PacketMan pm)
	{
		try {
			Gson gson = new Gson();
			pm.timesSent += 1;
			String packetString = gson.toJson(new PacketHolder(pm));
 
			//Loops through all the connected clients
			System.out.println("Server size: " + server.clients.size());
			for(ServerClientPair scp : server.clients)
			{
				System.out.println(scp.clientSocket.getInetAddress());
				InetAddress address = InetAddress.getByName("225.0.0.1");
				DatagramPacket packet = new DatagramPacket(packetString.getBytes(), packetString.getBytes().length, address, server.getUdpSendPort());
			    socket.send(packet);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	    //System.out.println("Datagram Packet sent externally");
	}
	@Override
	public void run()
	{
		while(server.isRunning)
		{
			byte[] buf = new byte[Global.MAX_DATAGRAM];
			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			try {
				socket.receive(packet);
				//Gson gson = new Gson();
				String recieved = new String(packet.getData());
				System.out.println("packet recieved: " + packet.getAddress());
				recieved = recieved.substring(0, recieved.lastIndexOf("}")+1); //Cuts empty space at end of packet
				PacketHolder ph = Global.gson.fromJson(recieved, PacketHolder.class);
				PacketMan np = Global.gson.fromJson(ph.getPacketJson(),server.packetType.getPacket(ph.classname).getClass());
				np.serverSideHandle(server);
				
				sendDatagram(np);
				//InetAddress group = InetAddress.getByName("225.0.0.1");
			    //DatagramPacket newpacket = new DatagramPacket(packet.getData(), packet.getLength(), group, 7779);
			    //socket.send(newpacket);
			    //System.out.println("Datagram Packet sent");
			}
			catch (SocketTimeoutException e)
			{
				System.out.println("Datagram socket timed out, reinitializing");
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}



}
