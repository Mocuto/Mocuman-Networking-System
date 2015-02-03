package mocuman;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketTimeoutException;

import javax.xml.bind.JAXBException;

import mocuman.packets.PacketHolder;
import mocuman.packets.PacketMan;

/**
 * 
 * @author Mocuto
 * This class receives data in the form of PacketMan objects from the server via TCP protocol. It then handles
 * the packet client-side
 */
public class ClientInputThread extends Thread {

	private final ClientMan client;
	private final Socket socket;
	private final BufferedReader in;
	public ClientInputThread(ClientMan client) throws IOException {
		this.client = client;
		this.socket = client.mocumanSocket;
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		this.start();
	}
	@Override
	public void run()
	{
		while(client.isRunning)
		{
			PacketMan packet = null;
			PacketHolder ph = null;
			String packetString = null;
			try {
				while((packetString = (String)in.readLine()) != null)
				{
					//Creates PacketMan object using received string and PacketHolder class
					ph = Global.gson.fromJson(packetString, PacketHolder.class);
					packet = Global.gson.fromJson(ph.getPacketJson(), client.packetType.getPacket(ph.classname).getClass());
					//System.out.println("Recieved Packet! " + packet.type);
					client.handlePacket(packet);
					
				}
			}
			//Socket has gone the 2 minutes without response
			catch (SocketTimeoutException e)
			{
				//If the client has reached the maximum timeout limit
				if(!client.isConnected())
				{
					System.out.println("Timed out. Disconnecting");
					try {
						client.end();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
				//If not, send a heart beat and wait for a response
				else
				{
					//System.out.println("Time out. Sending heartbeat...");
					PacketMan heartbeat = new PacketMan("Heartbeat", client.ID);
					heartbeat.bounceBack = true;
					try {
						client.pushPacket(heartbeat);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
			catch (EOFException e)
			{
				e.printStackTrace();
				try {
					client.end();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			catch ( IOException e) {
				System.out.println("Disconnect from server. Ending application.");
				try {
					client.end();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			} catch (JAXBException e) {
				e.printStackTrace();
			}
		}
	}
	@Override
	public void destroy()
	{
		try {
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
