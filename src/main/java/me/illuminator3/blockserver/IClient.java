package me.illuminator3.blockserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.channels.SocketChannel;

public interface IClient
{
    void close() throws IOException;
    DataOutputStream getOutputStream() throws IOException;
    IPacketChannel getChannel();
    boolean isConnected();
    Raw getRaw();
    DataInputStream getInputStream() throws IOException;
    Socket __internal_socket__();
    String getName();
    void setName(String name);
    boolean isVerified();
    void setProxyVerified(boolean verified);

    static IClient fromSocket(Socket socket, IServer server)
    {
        return new IClient()
        {
            private final IPacketChannel packetChannel = new ClientChannel(this, server);
            private String name = "null";

            @Override
            public void close()
                throws IOException
            {
                if (isConnected())
                    socket.close();
            }

            @Override
            public DataOutputStream getOutputStream()
                throws IOException
            {
                return new DataOutputStream(socket.getOutputStream());
            }

            @Override
            public IPacketChannel getChannel()
            {
                return packetChannel;
            }

            @Override
            public boolean isConnected()
            {
                return socket.isConnected() && !socket.isClosed();
            }

            @Override
            public Raw getRaw()
            {
                return new Raw(this);
            }

            @Override
            public DataInputStream getInputStream()
                throws IOException
            {
                return new DataInputStream(socket.getInputStream());
            }

            @Override
            public Socket __internal_socket__()
            {
                return socket;
            }

            @Override
            public String getName()
            {
                return name;
            }

            @Override
            public void setName(String name)
            {
                this.name = name;
            }

            private boolean verfied = false;

            @Override
            public boolean isVerified()
            {
                return verfied && !getName().equals("null") && !getName().contains(" ");
            }

            @Override
            public void setProxyVerified(boolean verified)
            {
                this.verfied = verified;
            }
        };
    }

    class Raw
    {
        private final IClient client;

        public Raw(IClient client)
        {
            this.client = client;
        }

        public Socket getSocket()
        {
            return client.__internal_socket__();
        }

        public SocketChannel getSocketChannel()
        {
            return getSocket().getChannel();
        }
    }
}