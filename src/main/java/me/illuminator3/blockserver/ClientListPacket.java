package me.illuminator3.blockserver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ClientListPacket
    implements Packet
{
    private Collection<? extends IClient> clients;

    public ClientListPacket(Collection<? extends IClient> clients)
    {
        this.clients = clients;
    }

    public ClientListPacket()
    {

    }

    public Collection<? extends IClient> getClients()
    {
        return clients;
    }

    @Override
    public String serialize()
    {
        String splitter = "@ // @";

        if (clients == null)
            return "";

        List<String> serialized = new ArrayList<>();

        for (IClient client : clients)
            serialized.add(client.getName());

        return String.join(splitter, serialized);
    }

    @Override
    public void deserialize(String s)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onReceive(IServer server, IClient client)
    {
        server.getLogger().error("Cannot receive server side packet (ClientListPacket, 0x02)!");
    }

    @Override
    public void handle(IServer server)
    {
        server.getLogger().error("Cannot handle server side packet (ClientListPacket, 0x02)!");
    }
}