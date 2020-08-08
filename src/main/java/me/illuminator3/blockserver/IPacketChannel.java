package me.illuminator3.blockserver;

import java.io.IOException;

public interface IPacketChannel
{
    void writePacket(Packet packet) throws IOException;
    void closeChannel() throws IOException;
}