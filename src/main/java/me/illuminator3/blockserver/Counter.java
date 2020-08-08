package me.illuminator3.blockserver;

import java.util.LinkedList;
import java.util.Queue;

public class Counter
{
    private final Queue<Long> count = new LinkedList<>();

    public Counter up()
    {
        count.add(System.currentTimeMillis() + 1000L);

        return this;
    }

    public int getPS()
    {
        long time = System.currentTimeMillis();

        while (!count.isEmpty() && count.peek() < time)
            count.remove();

        return count.size();
    }
}