package mocuman;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;

import javax.xml.bind.JAXBException;

import mocuman.packets.PacketHolder;
import mocuman.packets.PacketMan;


/**
 * 
 * @author Mocuto
 * This object converts PacketMan objects to binary and then sends them to the server and UDP packets
 */
public class ClientDatagramThread extends Thread {
	public final ClientMan client;
	public final MulticastSocket socket;
	public ClientDatagramThread(ClientMan client, MulticastSocket dgs)
	{
		this.client = client;
		this.socket = dgs;
		this.start();
	}
	@Override
	public void run()
	{
		DatagramPacket packet;
		while(client.isRunning)
		{
			try {
				byte[] buf = new byte[Global.MAX_DATAGRAM];
		    	packet = new DatagramPacket(buf, buf.length);
		    	if(!socket.isClosed())
		    	{
		    		socket.receive(packet);
		    		//Converts binary packet to form of string object
					String received = new String(packet.getData());
					//Removes any emtpy sapce at the end of the strig
					received = received.substring(0, received.lastIndexOf("}")+1);
				    System.out.println("Datagram Packet Recieved: " + received);
				    PacketHolder ph = Global.gson.fromJson(received, PacketHolder.class);
				    PacketMan packetm = Global.gson.fromJson(ph.getPacketJson(), client.packetType.getPacket(ph.classname).getClass());
				    client.handlePacket(packetm);
		    	}
			} catch (IOException | JAXBException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
	
			}
		}
	}
	@Override
	public void destroy()
	{
		
	}
}
