package me.illuminator3.blockserver;

import java.io.IOException;
import java.net.InetAddress;

public interface IProxyChecker
{
    boolean isProxy(InetAddress address) throws IOException;
}