package me.illuminator3.blockserver;

public interface IAttackBlocker<T>
{
    boolean isValid(T t, IClient client, IServer server);
    Class<?> getBlockingType();
}