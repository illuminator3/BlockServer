package me.illuminator3.blockserver;

import java.util.HashMap;
import java.util.Map;

public class SimplePacketHolder
    implements IPacketHolder
{
    private final Map<Integer, Class<? extends Packet>> registeredPackets = new HashMap<>();

    @Override
    public void registerPackets()
    {
        registerPacket(0x01, MessagePacket.class);
        registerPacket(0x02, ClientListPacket.class);
        registerPacket(0x03, NameUpdatePacket.class);
        registerPacket(0x04, SystemMessagePacket.class);
    }

    private void registerPacket(int id, Class<? extends Packet> clazz)
    {
        registeredPackets.put(id, clazz);
    }

    @Override
    public Packet createPacketFromId(int packetId)
    {
        try
        {
            return registeredPackets.get(packetId).newInstance();
        } catch (InstantiationException | IllegalAccessException ex)
        {
            throw new ReportedException(ex);
        }
    }

    @Override
    public int getIdFromPacket(Packet packet)
    {
        for (Map.Entry<Integer, Class<? extends Packet>> entry : registeredPackets.entrySet())
        {
            int key = entry.getKey();
            Class<? extends Packet> value = entry.getValue();

            if (packet.getClass().equals(value))
                return key;
        }

        return -1;
    }

    @Override
    public boolean isValidId(int id)
    {
        return registeredPackets.containsKey(id);
    }
}