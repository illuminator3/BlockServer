package me.illuminator3.blockserver;

import java.net.Socket;

public class SendSpamBlocker
    implements IAttackBlocker<Socket>
{
    private final Counter connectionCounter = new Counter();

    @Override
    public boolean isValid(Socket socket, IClient client, IServer server)
    {
        if (connectionCounter.up().getPS() > 2)
            setFilter(server);
        else
            resetFilter(server);

        return true;
    }

    @Override
    public Class<?> getBlockingType()
    {
        return Socket.class;
    }

    private boolean filter = false;

    private void setFilter(IServer server)
    {
        if (filter) return;

        server.getLogger().warn("The server is under attack! Stay calm and wait for it to end! (2+ CPS)");

        filter = true;

        server.getLogger().setFilter(record -> !record.getMessage().toLowerCase().contains("connect"));
    }

    private void resetFilter(IServer server)
    {
        if (!filter) return;

        filter = false;

        server.getLogger().warn("The server is no longer under attack c:");

        server.getLogger().setFilter(null);
    }
}