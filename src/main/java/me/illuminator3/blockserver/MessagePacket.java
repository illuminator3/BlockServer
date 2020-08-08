package me.illuminator3.blockserver;

import java.io.IOException;
import java.rmi.UnmarshalException;

public class MessagePacket
    implements Packet
{
    private String name,
                   message;
    private boolean invalidName = false;

    public MessagePacket(String name, String message)
    {
        this.name = name;
        this.message = message.substring(0, Math.min(73, message.length()));
    }

    public MessagePacket()
    {

    }

    @Override
    public String serialize()
    {
        return getName() + "&// @ //&" + getMessage();
    }

    @Override
    public void deserialize(String s)
    {
        String[] sa = s.split("&// @ //&", 2);

        this.name = sa[0];
        this.message = sa[1].trim().substring(0, Math.min(73, sa[1].length()));
    }

    @Override
    public void onReceive(IServer server, IClient client)
    {
        if (name != null && !client.getName().equals(name) || message.isEmpty() || name != null && name.trim().isEmpty() || message.contains("Neger") || message.startsWith("Hi") && message.length() == 3)
        {
            invalidName = true;

            server.getLogger().warn("Received invalid message packet from " + client.getRaw().getSocket().getInetAddress() + " (" + getName() + ")");

            try
            {
                client.close();
            } catch (IOException ex)
            {
                throw new ReportedException(ex);
            }

            return;
        }

        server.getLogger().info("Received message packet from " + client.getRaw().getSocket().getInetAddress() + " (" + getName() + "): " + getMessage());
    }

    @Override
    public void handle(IServer server)
    {
        if (invalidName) return;

        server.getClientManager().sendPacket(this);
    }

    public String getMessage()
    {
        return message;
    }

    public String getName()
    {
        return name;
    }
}