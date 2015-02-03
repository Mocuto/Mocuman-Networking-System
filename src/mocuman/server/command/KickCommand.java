package mocuman.server.command;

import mocuman.server.ServerMan;

public class KickCommand extends ServerCommand {

	public KickCommand(ServerMan server) {
		super(server, "Kick", new String[] {"kick", "KICK", "Kick"});
		// TODO Auto-generated constructor stub
	}
	public int execute(String[] commandToken)
	{
		super.execute(commandToken);
		int ID = Integer.parseInt(commandToken[1]);
		return server.kickClient(ID);
	}

}
