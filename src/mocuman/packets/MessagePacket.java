package mocuman.packets;

import mocuman.ClientMan;

public class MessagePacket extends PacketMan {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String value = "";
	public MessagePacket(String type, int id, String val) {
		super(type, id);
		this.value = val;
		classname = "MessagePacket";
		// TODO Auto-generated constructor stub
	}
	@Override
	public void clientSideHandle(ClientMan client)
	{
		if(type.equals("ID"))
			client.ID = Integer.parseInt(value);
		else if(type.equals("Server Message"))
			System.out.println("Message from server: " + value);
		else
			System.out.println("User " + senderID + " said: " + value);
	}

}
