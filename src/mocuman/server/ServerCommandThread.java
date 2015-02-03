package mocuman.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import mocuman.server.command.ServerCommand;
/**
 * 
 * 
 * @author Mocuto
 *	Basically, this object listens for inputs from the console. It checks if the server has any matches for the
 *	inputed string
 */

public class ServerCommandThread extends Thread {
	private ServerMan server = null;
	public ServerCommandThread(ServerMan server)
	{
		this.server = server;
		this.start();
	}
	@Override
	public void run()
	{
		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
		String currentCommand;
		try {
			while((currentCommand = stdIn.readLine()) != null)
			{
				String[] commandTokens = currentCommand.split(" ");
				ServerCommand command = server.getCommand(commandTokens[0]);
				if(command != null) command.execute(commandTokens);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
