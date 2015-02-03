package mocuman;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import mocuman.packets.MessagePacket;
/**
 * 
 * @author Mocuto
 * This object takes inputs from the System.in and converts them into MessagePackets to be sent to the server via
 * TCP
 */
public class ClientWriter extends Thread {

	private final ClientMan client;
	public ClientWriter(ClientMan client) {
		this.client = client;
		this.start();
	}
	@Override
	public void run()
	{
		String sendString = null;
		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
		try {
			while((sendString = stdIn.readLine()) != null)
			{
				MessagePacket message = new MessagePacket("Message", client.ID, sendString);
				client.pushMessage(message);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void destroy()
	{
		
	}


}
