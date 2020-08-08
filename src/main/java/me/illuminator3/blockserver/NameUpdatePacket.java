package me.illuminator3.blockserver;

import java.io.IOException;

public class NameUpdatePacket
    implements Packet
{
    private String name;

    public NameUpdatePacket(String name)
    {
        this.name = name;
    }

    public NameUpdatePacket()
    {

    }

    @Override
    public String serialize()
    {
        return name;
    }

    @Override
    public void deserialize(String s)
    {
        this.name = s;
    }

    @Override
    public void onReceive(IServer server, IClient client)
    {
        if (client.isVerified())
        {
            server.getLogger().error(client.getRaw().getSocket().getInetAddress() + " already updated his name. Disconnecting him...");

            try
            {
                client.close();
            } catch (IOException ex)
            {
                throw new ReportedException(ex);
            }

            return;
        }

        if (name == null || name.contains(" ") || name.length() > 15 || name.trim().isEmpty())
        {
            server.getLogger().error(client.getRaw().getSocket().getInetAddress() + " send an invalid name update packet. Disconnecting him...");

            try
            {
                client.close();
            } catch (IOException ex)
            {
                throw new ReportedException(ex);
            }

            return;
        }

        client.setName(name);

        server.getLogger().info("Got name update from " + client.getRaw().getSocket().getInetAddress() + " (" + name + ")");

        server.getClientManager().sendPacket(new SystemMessagePacket(name + " has connected"));
    }

    @Override
    public void handle(IServer server)
    {

    }
}