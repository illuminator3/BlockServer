package me.illuminator3.blockserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.stream.Collectors;

public class TeohProxyChecker
    implements IProxyChecker
{
    @Override
    public boolean isProxy(InetAddress address)
        throws IOException
    {
        String check = "https://ip.teoh.io/api/vpn/{ADDRESS}";

        URL url = new URL(check.replace("{ADDRESS}", address.getHostAddress()));
        URLConnection connection = url.openConnection();
        connection.addRequestProperty("User-Agent", "Mozilla/4.0");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

        if (reader.lines().collect(Collectors.joining(" ")).contains("yes"))
        {
            reader.close();

            return true;
        }

        reader.close();

        return false;
    }
}