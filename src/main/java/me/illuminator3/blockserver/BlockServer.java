package me.illuminator3.blockserver;

import java.io.*;
import java.net.SocketException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class BlockServer
    implements IServer, Runnable
{
    public static void main(String[] args)
    {
        new BlockServer().run();
    }

    private final List<String> blockedIPs = new ArrayList<>();
    private IListener listener;
    private IClientManager clientManager;
    private IPacketHandler packetHandler;
    private ILogger logger;
    private IPacketHolder packetHolder;
    private IInputListener inputListener;
    private List<IAttackBlocker<?>> attackBlockers;
    private List<IProxyChecker> proxyCheckers;
    private Thread emptyThread;
    private boolean running;

    @SuppressWarnings("BusyWait")
    @Override
    public void start(int port)
    {
        logger = new ExternalLogger();

        logger.info("Starting...");

        logger.info("Registering Client Manager...");

        clientManager = new BlockManager(this);

        logger.info("Making a Packet Holder...");

        packetHolder = new SimplePacketHolder();

        logger.info("Registering all packets...");

        packetHolder.registerPackets();

        logger.info("Initialising Packet Handler...");

        packetHandler = new InternPacketHandler(this);

        logger.info("Loading blocked ips...");

        Load : {
            try
            {
                File file = new File("blockedIPs.txt");

                if (!file.exists())
                    break Load;

                List<String> lines = Files.readAllLines(file.toPath());

                String s = String.join("%%", lines);
                String[] sa = s.split("%%");

                blockedIPs.addAll(Arrays.asList(sa));
            } catch (Throwable ex)
            {
                throw new ReportedException(ex);
            }
        }

        logger.info("Activating Client Listener...");

        ExecutorService service = Executors.newCachedThreadPool();

        listener = new ClientListener((client) -> {
            if (!running) return;

            if (blockedIPs.contains(client.getRaw().getSocket().getInetAddress().getHostAddress()))
            {
                logger.info("Blocked the connect request from " + client.getRaw().getSocket().getInetAddress());

                return;
            }

            try
            {
                client.getRaw().getSocket().setSoTimeout(5_000);
                client.getRaw().getSocket().setTcpNoDelay(true);

                service.submit(() -> {
//                    boolean result = false;
//                    int curr = 0;
//
//                    logger.info("Checking " + client.getRaw().getSocket().getInetAddress() + " for a proxy/vpn");
//
//                    for (IProxyChecker checker : proxyCheckers)
//                    {
//                        try
//                        {
//                            if (checker.isProxy(client.getRaw().getSocket().getInetAddress()))
//                            {
//                                logger.info("Checking " + client.getRaw().getSocket().getInetAddress() + " with check " + (++curr) + "...detected");
//
//                                result = true;
//
//                                break;
//                            }
//                            else
//                                logger.info("Checking " + client.getRaw().getSocket().getInetAddress() + " with check " + (++curr) + "...ok");
//                        } catch (IOException ex)
//                        {
//                            throw new ReportedException(ex);
//                        }
//                    }
//
//                    if (result)
//                    {
//                        logger.info("Blocked the connect request from " + client.getRaw().getSocket().getInetAddress() + " [VPN/PROXY]");
//
//                        blockedIPs.add(client.getRaw().getSocket().getInetAddress().getHostAddress());
//
//                        return;
//                    }

                    client.setProxyVerified(true);

                    clientManager.handleRequest(client);
                });
            } catch (OutOfMemoryError err)
            {
                logger.error("Out of memory: " + err.getMessage());
                logger.error("Trying to clear memory...");

                System.gc();

                logger.error("Closing connection...");

                try
                {
                    client.close();
                } catch (IOException ex)
                {
                    throw new ReportedException(ex);
                }
            } catch (SocketException ex)
            {
                throw new ReportedException(ex);
            }
        }, port, this);

        logger.info("Writing to Empty Thread...");

        emptyThread = new Thread(() -> {
            try
            {
                while (System.currentTimeMillis() != Long.MAX_VALUE)
                    Thread.sleep(Long.MAX_VALUE);
            } catch (InterruptedException ignored) {}
        });

        emptyThread.start();

        logger.info("Starting Timer...");

        Timer timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask()
        {
            @Override
            public void run()
            {
                if (!running) return;

                Collection<? extends IClient> clients = clientManager.getConnectedClients().stream().filter(IClient::isVerified).collect(Collectors.toList());

                ClientListPacket packet = new ClientListPacket(clients);

                clientManager.sendPacket(packet);
            }
        }, 100, 100);

        timer.scheduleAtFixedRate(new TimerTask()
        {
            @Override
            public void run()
            {
                clientManager.getConnectedClients().removeIf(client -> !client.isConnected());
            }
        }, 100, 100);

        logger.info("Waiting for the Input Listener...");

        inputListener = new AdvancedInputListener((in) -> {
            if (in.trim().isEmpty()) return;

            if (in.trim().equalsIgnoreCase("stop") || in.trim().equalsIgnoreCase("end"))
                stop();
            else if (in.trim().toLowerCase().startsWith("close"))
            {
                String name = in.split(" ")[1];

                for (IClient client : clientManager.getConnectedClients())
                {
                    if (client.getName().equalsIgnoreCase(name))
                    {
                        try
                        {
                            client.getChannel().writePacket(new SystemMessagePacket("Your connection has been closed by the Console"));
                            client.close();

                            logger.info("Successfully closed the connection of " + client.getRaw().getSocket().getInetAddress() + " (" + client.getName() + ")");
                        } catch (IOException ignored) {}
                    }
                }
            }
            else if (in.trim().equalsIgnoreCase("list"))
            {
                Collection<? extends IClient> connected = clientManager.getConnectedClients();

                if (!connected.isEmpty())
                    logger.info("Connected clients: " + connected.stream().map(client -> client.getRaw().getSocket().getInetAddress() + " (" + client.getName() + ")").collect(Collectors.joining(", ")));
                else
                    logger.info("No users are currently connected");
            }
            else if (in.trim().startsWith("block"))
            {
                String ip = in.trim().split(" ")[1];

                blockIP(ip);

                logger.info("Successfully blocked IP: " + ip);
            }
            else if (in.trim().equalsIgnoreCase("abort"))
            {
                for (IClient iClient : clientManager.getConnectedClients())
                {
                    try
                    {
                        iClient.close();
                    } catch (IOException ex)
                    {
                        throw new ReportedException(ex);
                    }
                }

                logger.info("Closed all existing connections!");
            }
            else
                logger.error("Unknown command: " + in);
        }, /*"> "*/ "");

        logger.info("Setting ShutdownHook...");

        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));

        logger.info("Initialising the Attack Blockers...");

        attackBlockers = new ArrayList<>();

        attackBlockers.add(new InvalidIdSpamBlocker());
        attackBlockers.add(new PacketSpamBlocker());
        attackBlockers.add(new SendSpamBlocker());

        proxyCheckers = new ArrayList<>();

        proxyCheckers.add(new TeohProxyChecker());
        proxyCheckers.add(new V2ProxyChecker());

        logger.info("Successfully started!");

        this.running = true;

        logger.info("Listening on port " + port);
    }

    @Override
    public IPacketHandler getPacketHandler()
    {
        return packetHandler;
    }

    @Override
    public IClientManager getClientManager()
    {
        return clientManager;
    }

    @Override
    public IListener getListener()
    {
        return listener;
    }

    @Override
    public void stop()
    {
        this.running = false;

        logger.info("Shutting down...");

        logger.info("Messaging clients...");

        clientManager.sendPacket(new SystemMessagePacket("The chat will shutdown in 1 second!"));

        try
        {
            Thread.sleep(250L);
        } catch (InterruptedException ex)
        {
            ex.printStackTrace();
        }

        logger.info("Closing all open connections...");

        for (IClient iClient : clientManager.getConnectedClients())
        {
            try
            {
                iClient.close();
            } catch (IOException ex)
            {
                throw new ReportedException(ex);
            }
        }

        logger.info("Saving blocked ips...");

        try
        {
            File file = new File("blockedIPs.txt");

            if (!file.exists())
                file.createNewFile();

            PrintWriter pw = new PrintWriter(file);

            pw.print(String.join("%%", blockedIPs));

            pw.close();
        } catch (Throwable ex)
        {
            throw new ReportedException(ex);
        }

        logger.info("Interrupting the Empty Thread...");

        emptyThread.interrupt();

        logger.info("Have a nice day and goodbye!");

        try
        {
            Thread.sleep(250L);
        } catch (InterruptedException ex)
        {
            ex.printStackTrace();
        }

        logger.exit();

        Runtime.getRuntime().halt(-1);
    }

    @Override
    public ILogger getLogger()
    {
        return logger;
    }

    @Override
    public IPacketHolder getPacketHolder()
    {
        return packetHolder;
    }

    public Thread getEmptyThread()
    {
        return emptyThread;
    }

    @Override
    public List<IAttackBlocker<?>> getAttackBlockers()
    {
        return attackBlockers;
    }

    @Override
    public Collection<? extends String> getBlockedIPs()
    {
        return blockedIPs;
    }

    @Override
    public void blockIP(String ip)
    {
        blockedIPs.add(ip);
    }

    @Override
    public boolean isBlocked(String ip)
    {
        return blockedIPs.contains(ip);
    }

    public IInputListener getInputListener()
    {
        return inputListener;
    }

    @Override
    public void run()
    {
        start(9964);
    }
}