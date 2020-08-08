package me.illuminator3.blockserver;

import java.util.logging.Filter;

public interface ILogger
{
    void info(String msg);
    void warn(String msg);
    void error(String msg);
    void setFilter(Filter filter);
    void exit();
}