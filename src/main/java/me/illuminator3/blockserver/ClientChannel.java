package me.illuminator3.blockserver;

import com.sun.corba.se.spi.legacy.connection.LegacyServerSocketEndPointInfo;

import java.io.DataOutputStream;
import java.io.IOException;

public class ClientChannel
    implements IPacketChannel
{
    private final IClient client;
    private final IServer server;

    public ClientChannel(IClient client, IServer server)
    {
        this.client = client;
        this.server = server;
    }

    @Override
    public void writePacket(Packet packet)
        throws IOException
    {
        if (client.getRaw().getSocket().isClosed()) return;
        if (!client.isVerified()) return;

        DataOutputStream outputStream = client.getOutputStream();
        String serialize = packet.serialize();

        try
        {
            outputStream.writeUTF(server.getPacketHolder().getIdFromPacket(packet) + "p__p//" + serialize);
        } catch (IOException ex)
        {
            throw new ReportedException(ex);
        }
    }

    @Override
    public void closeChannel()
        throws IOException
    {
        client.close();
    }
}