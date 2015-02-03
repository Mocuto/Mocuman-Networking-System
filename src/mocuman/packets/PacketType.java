package mocuman.packets;

import java.util.HashMap;
import java.util.Map;
/**
 * 
 * @author Mocuto
 * This object stores the PacketMan child classes, to be used by the ClientMan and ServerMan objects to identify
 * packets sent to them
 */
public class PacketType {

	public Map<String, PacketMan>packets = new HashMap<String, PacketMan>() { /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

	{
		put("PacketMan", new PacketMan("Packet", -1));
		put("MessagePacket", new MessagePacket("Message", -1, ""));
		put("DisconnectPacket", new DisconnectPacket(-1));
	}
	};
	
	public PacketType() {
		// TODO Auto-generated constructor stub
		
	}
	public PacketMan getPacket(String classname)
	{
		return packets.get(classname);
	}
}
