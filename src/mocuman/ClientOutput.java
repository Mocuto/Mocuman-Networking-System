package mocuman;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;

import mocuman.packets.MessagePacket;
import mocuman.packets.PacketHolder;
import mocuman.packets.PacketMan;

import com.google.gson.Gson;

public class ClientOutput {

	private final Socket socket;
	private final MulticastSocket dgs;
	private final PrintWriter out;
	public ClientMan client;
	public ClientOutput(ClientMan sclient, MulticastSocket dgs) throws IOException {
		this.client = sclient;
		this.socket = sclient.mocumanSocket;
		this.dgs = dgs;
		out = new PrintWriter(socket.getOutputStream(), true);
	}
	//This function converts a PacketMan object to a multi-language compatible JSON string object, then sends it
	//via TCP
	public void sendPacket(PacketMan packet) throws IOException
	{
		if(packet.timesSent < packet.maxResends + 1)
		{
			packet.timesSent += 1;
			packet.senderID = client.ID;
			PacketHolder ph = new PacketHolder(packet);
			Gson gson = new Gson();
			String packetString = gson.toJson(ph);
			out.println(packetString);
		}
	}
	//This function converts a MessagePacket object to a multi-language compatible JSON string object, then sends it
	//via TCP
	public void sendMessage(MessagePacket message) throws IOException
	{
		System.out.println("You say: " + message.value);
		sendPacket(message);
	}
	//This function converts a PacketMan object to binary, then sends it
	//via UDP
	public void sendDatagram(PacketMan packet) throws IOException
	{
		byte[] buf = new byte[Global.MAX_DATAGRAM];
		packet.senderID = client.ID;
		packet.timesSent += 1;
		Gson gson = new Gson();
		PacketHolder ph = new PacketHolder(packet);
		String packetString = gson.toJson(ph);
		buf = packetString.getBytes();
		//InetAddress address = InetAddress.getByAddress(new byte[]{(byte)98, (byte)28, (byte)171, (byte)77 });
		DatagramPacket dpacket = new DatagramPacket(buf, buf.length, InetAddress.getByName(client.getDestinationIp()), client.getUdpSendPort());
		dgs.send(dpacket);
		//System.out.println("Datagram sent, bytes long is " + packetString.getBytes().length);
	}
	//This function closes the outputstream
	public void destroy() throws IOException
	{
		out.close();
	}
}
