package me.illuminator3.blockserver;

import java.util.Collection;

public interface IClientManager
{
    void handleRequest(IClient client);
    Collection<? extends IClient> getConnectedClients();
    void setConnectedClients(Collection<? extends IClient> clients);
    void sendPacket(Packet packet);
}