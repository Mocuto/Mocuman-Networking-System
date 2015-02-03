package mocuman.packets;

import mocuman.Global;

/**
 * 
 * @author Mocuto
 * This helper class stores PacketMan objects in a portable format, to be sent via TCP or UDP
 */
public class PacketHolder {

	private String packetJson;
	public String classname;
	public PacketHolder(PacketMan packetman) {
		packetJson = Global.gson.toJson(packetman);
		classname = packetman.classname;
	}
	public PacketHolder(String pj, String cn) {
		packetJson = pj;
		classname = cn;
	}
	public PacketMan getPacket()
	{
		return Global.gson.fromJson(packetJson, PacketMan.class);
	}
	public String getPacketJson()
	{
		return packetJson;
	}
	public PacketHolder setPacketJson(String json)
	{
		this.packetJson = json;
		return this;
	}
	public PacketHolder setPacketJson(PacketMan packetman)
	{
		this.packetJson = Global.gson.toJson(packetman);
		return this;
	}

}
