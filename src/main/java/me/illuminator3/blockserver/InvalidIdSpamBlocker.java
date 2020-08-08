package me.illuminator3.blockserver;

import java.net.Socket;

public class InvalidIdSpamBlocker
    implements IAttackBlocker<Integer>
{
    private final Counter connectionCounter = new Counter();

    @Override
    public boolean isValid(Integer id, IClient client, IServer server)
    {
        if (server.getPacketHolder().isValidId(id)) return true;

        if (connectionCounter.up().getPS() > 20)
            setFilter(server);
        else
            resetFilter(server);

        return true;
    }

    @Override
    public Class<?> getBlockingType()
    {
        return Integer.class;
    }

    private boolean filter = false;

    private void setFilter(IServer server)
    {
        if (filter) return;

        server.getLogger().warn("The server is under attack! Stay calm and wait for it to end! (20+ SPS)");

        filter = true;

        server.getLogger().setFilter(record -> !record.getMessage().toLowerCase().contains("invalid packet id"));
    }

    private void resetFilter(IServer server)
    {
        if (!filter) return;

        filter = false;

        server.getLogger().warn("The server is no longer under attack c:");

        server.getLogger().setFilter(null);
    }
}