package me.illuminator3.blockserver;

public interface Packet
{
    String serialize();
    void deserialize(String s);
    void onReceive(IServer server, IClient client);
    void handle(IServer server);
}