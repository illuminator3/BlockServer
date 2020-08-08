package me.illuminator3.blockserver;

import java.util.Collection;

public interface IServer
{
    void start(int port);
    IPacketHandler getPacketHandler();
    IClientManager getClientManager();
    IListener getListener();
    void stop();
    ILogger getLogger();
    IPacketHolder getPacketHolder();
    Collection<? extends IAttackBlocker<?>> getAttackBlockers();
    Collection<? extends String> getBlockedIPs();
    void blockIP(String ip);
    boolean isBlocked(String ip);
}