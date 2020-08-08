package me.illuminator3.blockserver;

public class ReportedException
    extends RuntimeException
{
    public ReportedException()
    {
        super();
    }

    public ReportedException(String message)
    {
        super(message);
    }

    public ReportedException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public ReportedException(Throwable cause)
    {
        super(cause);
    }

    protected ReportedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}