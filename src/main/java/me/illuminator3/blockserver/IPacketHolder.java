package me.illuminator3.blockserver;

public interface IPacketHolder
{
    void registerPackets();
    Packet createPacketFromId(int packetId);
    int getIdFromPacket(Packet packet);
    boolean isValidId(int id);
}