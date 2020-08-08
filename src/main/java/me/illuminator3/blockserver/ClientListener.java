package me.illuminator3.blockserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Consumer;

public class ClientListener
    implements IListener
{
    private final Consumer<IClient> onConnect;
    private final int port;
    private final ServerSocket socket;
    public boolean running;
    private final IServer server;

    public ClientListener(Consumer<IClient> onConnect, int port, IServer server)
    {
        this.onConnect = onConnect;
        this.port = port;
        this.server = server;

        try
        {
            this.socket = new ServerSocket(port);
        } catch (IOException ex)
        {
            throw new ReportedException(ex);
        }

        running = true;

        Thread serverThread = new Thread(() ->
        {
            while (running)
            {
                try
                {
                    Socket client = socket.accept();

                    this.server.getLogger().info("A connection from " + client.getInetAddress() + " has been requested");

                    this.onConnect.accept(IClient.fromSocket(client, server));
                } catch (Throwable ex)
                {
                    throw new ReportedException(ex);
                }
            }
        });

        serverThread.start();
    }

    public int getPort()
    {
        return port;
    }
}