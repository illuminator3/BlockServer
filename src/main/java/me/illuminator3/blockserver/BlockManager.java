package me.illuminator3.blockserver;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BlockManager
    implements IClientManager
{
    public List<IClient> connectedClients = new CopyOnWriteArrayList<>();
    private final ExecutorService service = Executors.newCachedThreadPool();
    private final IServer server;
    private final Timer timer = new Timer();

    public BlockManager(IServer server)
    {
        this.server = server;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void handleRequest(IClient client)
    {
        boolean valid = true;

        for (IAttackBlocker<?> attackBlocker : server.getAttackBlockers())
        {
            if (attackBlocker.getBlockingType().equals(Socket.class))
            {
                IAttackBlocker<Socket> blocker = (IAttackBlocker<Socket>) attackBlocker;

                if (!blocker.isValid(client.getRaw().getSocket(), client, server))
                    valid = false;
            }
        }

        if (!valid) return;

        server.getLogger().info(client.getRaw().getSocket().getInetAddress() + " has connected (+)");

        for (IClient ic : connectedClients)
        {
            if (ic.getRaw().getSocket().getInetAddress().equals(client.getRaw().getSocket().getInetAddress()))
            {
                server.getLogger().warn(client.getRaw().getSocket().getInetAddress() + " was already connected. Disconnecting his duplicate");

                try
                {
                    ic.close();
                } catch (IOException ex)
                {
                    throw new ReportedException(ex);
                }

                break;
            }
        }

        connectedClients.add(client);

        timer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                if (client.isConnected() && !client.isVerified())
                {
                    server.getLogger().warn(client.getRaw().getSocket().getInetAddress() + " didn't send a name update packet within 10 seconds of connecting. Disconnecting him...");

                    try
                    {
                        client.close();
                    } catch (IOException ex)
                    {
                        throw new ReportedException(ex);
                    }
                }
            }
        }, 10 * 1000);

        service.submit(() -> {
            while (client.isConnected())
            {
                try
                {
                    DataInputStream input = client.getInputStream();
                    String in = input.readUTF();

                    if (in.length() > 2_000)
                    {
                        server.getLogger().warn(client.getRaw().getSocket().getInetAddress() + " send an invalid size of bytes!");

                        client.close();

                        return;
                    }

                    String[] sa = in.split("p__p//", 2);
                    int packetId = Integer.parseInt(sa[0]);

                    for (IAttackBlocker<?> blocker : server.getAttackBlockers())
                    {
                        if (blocker instanceof InvalidIdSpamBlocker)
                        {
                            InvalidIdSpamBlocker invalidIdSpamBlocker = (InvalidIdSpamBlocker) blocker;

                            invalidIdSpamBlocker.isValid(packetId, client, server);
                        }
                    }

                    if (!server.getPacketHolder().isValidId(packetId))
                    {
                        server.getLogger().error(client.getRaw().getSocket().getInetAddress() + " tried to send an invalid packet id! (" + packetId + ")");

                        client.close();

                        return;
                    }

                    String data = sa[1];

                    Packet packet = server.getPacketHolder().createPacketFromId(packetId);

                    boolean packetValid = true;

                    for (IAttackBlocker<?> attackBlocker : server.getAttackBlockers())
                    {
                        if (attackBlocker.getBlockingType().equals(Packet.class))
                        {
                            IAttackBlocker<Packet> blocker = (IAttackBlocker<Packet>) attackBlocker;

                            if (!blocker.isValid(packet, client, server))
                                packetValid = false;
                        }
                    }

                    if (!packetValid) return;

                    if (!client.isVerified() && !(packet instanceof NameUpdatePacket) && client.isConnected())
                    {
                        server.getLogger().warn(client.getRaw().getSocket().getInetAddress() + " tried to send a packet without updating his name first. Closing his connection...");

                        client.close();

                        continue;
                    }

                    packet.deserialize(data);

                    packet.onReceive(server, client);

                    this.server.getPacketHandler().handlePacket(packet);
                } catch (Throwable ex)
                {
                    throw new ReportedException(ex);
                }
            }

            server.getLogger().info(client.getRaw().getSocket().getInetAddress() + " has disconnected (-)");

            if (client.isVerified())
                sendPacket(new SystemMessagePacket(client.getName() + " has disconnected"));

            connectedClients.remove(client);
        });
    }

    @Override
    public Collection<? extends IClient> getConnectedClients()
    {
        return connectedClients;
    }

    @Override
    public void setConnectedClients(Collection<? extends IClient> clients)
    {
        connectedClients = new CopyOnWriteArrayList<>(clients);
    }

    @Override
    public void sendPacket(Packet packet)
    {
        for (IClient client : connectedClients)
        {
            if (client.isVerified())
            {
                try
                {
                    client.getChannel().writePacket(packet);
                } catch (Throwable ex)
                {
                    throw new ReportedException(ex);
                }
            }
        }
    }
}