package mocuman.packets;

import mocuman.server.ServerMan;

public class DisconnectPacket extends PacketMan {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DisconnectPacket(int ID) {
		super("Disconnect Packet", ID);
		classname = "DisconnectPacket";
		// TODO Auto-generated constructor stub
	}
	@Override
	public void serverSideHandle(ServerMan server)
	{
		server.killClient(server.getClientById(senderID));
	}
}
