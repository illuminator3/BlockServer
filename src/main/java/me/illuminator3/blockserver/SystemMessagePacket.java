package me.illuminator3.blockserver;

import java.io.IOException;

public class SystemMessagePacket
    implements Packet
{
    private String message;

    public SystemMessagePacket(String message)
    {
        this.message = message;
    }

    public SystemMessagePacket()
    {

    }

    @Override
    public String serialize()
    {
        return message;
    }

    @Override
    public void deserialize(String s)
    {
        this.message = s;
    }

    @Override
    public void onReceive(IServer server, IClient client)
    {
        server.getLogger().error("Cannot receive server side packet (SystemMessagePacket, 0x04)");

        try
        {
            client.close();
        } catch (IOException ex)
        {
            throw new ReportedException(ex);
        }
    }

    @Override
    public void handle(IServer server)
    {
        server.getLogger().error("Cannot handle server side packet (SystemMessagePacket, 0x04)");
    }

    public String getMessage()
    {
        return message;
    }
}