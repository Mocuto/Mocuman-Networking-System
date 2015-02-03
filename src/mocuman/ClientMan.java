package mocuman;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;

import javax.xml.bind.JAXBException;

import mocuman.packets.DisconnectPacket;
import mocuman.packets.MessagePacket;
import mocuman.packets.PacketMan;
import mocuman.packets.PacketType;

import org.ini4j.InvalidFileFormatException;
import org.ini4j.Wini;
/**
 * 
 * @author Mocuto
 * This object acts as the client, connecting to the server and transferring packets via TCP and UDP protocol
 * Uses the ClientInputThread object to receive packets from the server
 * Uses the ClientOutput object to send packets to the server
 * Uses the ClientWriter object to receive inputs from the System.in or console
 * Uses the ClientDatagramThread object to send datagram packets to the server
 * Uses PacketTypes to identify packets handled by it
 */

public class ClientMan {

	public Socket mocumanSocket = null;
	public MulticastSocket datagramSocket = null;
	public long lastTimeNoticed = -1;
	public static long maxTimeout = 240000;
	public static long minTimeout = 120000;
	public int ID = -1;
	public boolean isRunning = true;
	public boolean isConnected = false;
	private final ClientOutput output;
	private final ClientInputThread in;
	private final ClientWriter writer;
	private final ClientDatagramThread datagram;
	private String destinationIp;
	private int tcpPort, udpReceivePort, udpSendPort;
	public PacketType packetType = new PacketType();
	public ClientMan() throws IOException
	{
		setConfiguration("config.ini");
		try {
			mocumanSocket = new Socket();
			SocketAddress addr = new InetSocketAddress(destinationIp, tcpPort);
			mocumanSocket.connect(addr);
			datagramSocket = new MulticastSocket(udpReceivePort);
			datagramSocket.setInterface(getExternalNetworkInterface());
			System.out.println(datagramSocket.getInterface());
			InetAddress group = InetAddress.getByName("225.0.0.1");
			datagramSocket.joinGroup(group);
			isConnected = true;
		}
		catch (Exception e)
		{
			System.out.println("Could not connect. Sorry!");
			e.printStackTrace();
			System.exit(-1);
		}
		finally
		{
			mocumanSocket.setSoTimeout((int)minTimeout);
			output = new ClientOutput(this, datagramSocket);
			in = new ClientInputThread(this);
			writer = new ClientWriter(this);
			datagram = new ClientDatagramThread(this, datagramSocket);
		}
		
	}
	//This function sets the server ip and port configuration using an external ini file
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
			destinationIp = config.get("client", "ipaddress");
			tcpPort = config.get("client", "tcpport", int.class);
			udpReceivePort = config.get("client", "udpportr", int.class);
			udpSendPort = config.get("client", "udpports", int.class);
		}
	}
	//This function finds the network interface used for external internet connection
	//If it does not find one, it returns a loopback address
	private InetAddress getExternalNetworkInterface() throws SocketException, UnknownHostException
	{
		InetAddress returningInet = InetAddress.getByName("127.0.0.1");
		Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
        for (NetworkInterface netint : Collections.list(nets))
        {
        	Enumeration<InetAddress> addresses = netint.getInetAddresses();
        	for(InetAddress ia : Collections.list(addresses))
        	{
        		if(!ia.isLoopbackAddress() && ia.isSiteLocalAddress() && !(ia.getHostAddress().indexOf(":") > -1))
        		{
        			returningInet = ia;
        		}
        	}
        }
        return returningInet;
	}
	public int getUdpSendPort()
	{
		return udpSendPort;
	}
	public int getUdpReceivePort()
	{
		return udpReceivePort;
	}
	public int getTcpPort()
	{
		return tcpPort;
	}
	public String getDestinationIp()
	{
		return destinationIp;
	}
	//This function takes a PacketMan object and sends it through the client's output object via TCP
	public void pushPacket(PacketMan packet) throws IOException
	{
		output.sendPacket(packet);
	}
	//This function takes a MessagePacket object and sends it through the client's output object via TCP
	public void pushMessage(MessagePacket packet) throws IOException
	{
		output.sendMessage(packet);
	}
	//This function takes a PacketMan object and sends it through the client's output object via UDP
	public void pushDatagram(PacketMan packet) throws IOException
	{
		output.sendDatagram(packet);
	}
	public static void main(String[] args) throws IOException {
		new ClientMan();
	}
	//This function determines whether the client is still connected to the server based on the elapsed time since the client
	//was last "noticed" by the server
	public boolean isConnected()
	{
		long currentTime = new Date().getTime();
		if (currentTime - lastTimeNoticed > maxTimeout) return false;
		else return true;
	}
	public void handlePacket(PacketMan packet) throws IOException, JAXBException
	{
		lastTimeNoticed = new Date().getTime();
		
		//If the packet is slated to bounce back to the server, do so here
		if(packet.bounceBack)
		{
			pushPacket(packet);
		}
		//Run the packet's clientSideHandle function
		packet.clientSideHandle(this);
	}
	//This function sends a packet to the server signalling its disconnect, then ends all client-modules and
	//sockets
	public void end() throws IOException
	{
		output.sendPacket(new DisconnectPacket(ID));
		isRunning = false;
		in.destroy();
		output.destroy();
		writer.destroy();
		datagram.destroy();
		mocumanSocket.close();
		datagramSocket.close();
		System.out.println("Client application ending!");
		System.exit(1);
	}

}
