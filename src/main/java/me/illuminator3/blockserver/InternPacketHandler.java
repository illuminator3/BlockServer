package me.illuminator3.blockserver;

public class InternPacketHandler
    implements IPacketHandler
{
    private final IServer server;

    public InternPacketHandler(IServer server)
    {
        this.server = server;
    }

    @Override
    public void handlePacket(Packet packet)
    {
        packet.handle(server);
    }
}