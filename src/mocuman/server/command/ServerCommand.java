package mocuman.server.command;

import mocuman.server.ServerMan;

public class ServerCommand {
	public String[] identifiers;
	public String name;
	public ServerMan server;
	public ServerCommand(ServerMan server, String name, String[] ident)
	{
		this.name = name;
		this.identifiers = ident;
		this.server = server;
	}
	public int execute(String[] commandToken)
	{
		System.out.println("Running command: " + name);
		return 1; //Whether the command was executed properly or not
	}
	public boolean hasIdent(String ident)
	{
		for(String str : identifiers)
		{
			if(str.equals(ident))
				return true;
		}
		return false;
	}
}
