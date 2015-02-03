package mocuman.server.command;

import java.io.IOException;

import mocuman.server.ServerMan;

public class QuitServer extends ServerCommand {

	public QuitServer(ServerMan server) {
		super(server,"quit", new String[]{"quit", "Quit", "QUIT"});
		// TODO Auto-generated constructor stub
	}
	public int execute(String[] commandToken) {
		super.execute(commandToken);
		try {
			server.quit();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 1;
	}
}
