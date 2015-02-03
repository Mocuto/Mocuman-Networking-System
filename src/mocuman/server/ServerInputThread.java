package mocuman.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketTimeoutException;

import mocuman.Global;
import mocuman.packets.PacketHolder;
import mocuman.packets.PacketMan;

public class ServerInputThread extends Thread {
	private final ServerMan server;
	private final Socket clientSocket;
	private final ServerClientPair client;
	private final BufferedReader in;
	public int clientID;
	public boolean isRunning = true;
	public ServerInputThread(ServerMan server, ServerClientPair client) throws IOException
	{
		this.server = server;
		this.client = client;
		this.clientSocket = client.clientSocket;
		in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		start();
	}
	@Override
	public void run()
	{
		while(server.isRunning && clientSocket.isConnected() && !clientSocket.isClosed())
		{
			String packetString = null;
			PacketMan packet = null;
			PacketHolder ph = null;
			try{
				//When it receives a line, it uses the PacketHolder object to convert the string into a PacketMan
				//Object
				while((packetString = (String)in.readLine()) != null)
				{
					//System.out.println(packetString);
					ph = Global.gson.fromJson(packetString, PacketHolder.class);
					packet = Global.gson.fromJson(ph.getPacketJson(), server.packetType.getPacket(ph.classname).getClass());
					//System.out.println("Server: Packet recieved! " + packet.type);
					server.handlePacket(packet);
				}
			}
			//Socket has gone the 2 minutes without response
			catch(SocketTimeoutException e)
			{
				//If the client has reached the maximum timeout limit
				if(!server.isClientConnected(client))
				{
					System.out.println("Client socket timed out. Disconnecting");
					server.killClient(client);
					break;
				}
				//If not, send a heart beat and wait for a response
				else
				{
					//System.out.println("Client socket timed out. Sending heartbeat...");
					PacketMan heartbeat = new PacketMan("Heartbeat", -1);
					heartbeat.bounceBack = true;
					server.pushPacketToClient(client, heartbeat);
				}
			}
			catch (IOException e)
			{
				System.out.println("IO Exception, disconnecting client.");
				server.killClient(client);
			}
		}
		if(!clientSocket.isConnected() || clientSocket.isClosed())
		{
			System.out.println("Client has disconnected.");
			isRunning = false;
			server.killClient(client);
		}
		System.out.println("Input thread ended");
	}
	@Override
	public void destroy()
	{
		try {
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
