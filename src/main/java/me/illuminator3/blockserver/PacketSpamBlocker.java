package me.illuminator3.blockserver;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.util.Map;
import java.util.WeakHashMap;

public class PacketSpamBlocker
    implements IAttackBlocker<Packet>
{
    private final Map<InetAddress, Counter> packetCounter = new WeakHashMap<>();
    private final Map<InetAddress, Boolean> warnSend = new WeakHashMap<>();

    @Override
    public boolean isValid(Packet packet, IClient client, IServer server)
    {
        Counter counter;

        if (!packetCounter.containsKey(client.getRaw().getSocket().getInetAddress()))
        {
            counter = new Counter();
        }
        else
            counter = packetCounter.get(client.getRaw().getSocket().getInetAddress());

        boolean valid = true;

        if (counter.up().getPS() > 10)
        {
            if (!warnSend.containsKey(client.getRaw().getSocket().getInetAddress()) || !warnSend.get(client.getRaw().getSocket().getInetAddress()))
            {
                server.getLogger().warn(client.getRaw().getSocket().getInetAddress() + " send an invalid amount of Packets! (10+ PPS)");

                warnSend.put(client.getRaw().getSocket().getInetAddress(), true);
            }

            try
            {
                client.close();
            } catch (IOException ex)
            {
                throw new ReportedException(ex);
            }

            valid = false;
        }
        else
            warnSend.put(client.getRaw().getSocket().getInetAddress(), false);

        packetCounter.put(client.getRaw().getSocket().getInetAddress(), counter);

        return valid;
    }

    @Override
    public Class<?> getBlockingType()
    {
        return Packet.class;
    }
}