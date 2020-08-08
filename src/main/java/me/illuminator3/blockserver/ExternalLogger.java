package me.illuminator3.blockserver;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.logging.*;

public class ExternalLogger
    implements ILogger
{
    private final Logger logger = Logger.getAnonymousLogger();

    public ExternalLogger()
    {
        logger.setUseParentHandlers(false);

        ConsoleHandler handler = new ConsoleHandler();

        SimpleFormatter formatter = new SimpleFormatter()
        {
            @Override
            public synchronized String format(LogRecord record)
            {
                LocalTime now = LocalTime.now();

                return "[" +
                        format(now.getHour()) +
                        ':' +
                        format(now.getMinute()) +
                        ':' +
                        format(now.getSecond()) +
                        ' ' +
                        record.getLevel().getName().toUpperCase().replace("WARNING", "WARN").replace("SEVERE", "ERROR").replace("INFORMATION", "INFO") +
                        ']' +
                        ':' +
                        ' ' +
                        record.getMessage() +
                        '\n';
            }

            private String format(int i)
            {
                StringBuilder v = new StringBuilder(String.valueOf(i));

                while (v.length() < 2)
                    v.insert(0, '0');

                return v.toString();
            }
        };

        handler.setFormatter(formatter);

        logger.addHandler(handler);

        try
        {
            File logFolder = new File("logs");

            if (!logFolder.exists())
                logFolder.mkdirs();

            DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss");

            FileHandler fh = new FileHandler("logs/" + format.format(LocalDateTime.now()) + ".log");

            fh.setFormatter(formatter);

            logger.addHandler(fh);
        } catch (IOException ex)
        {
            throw new ReportedException(ex);
        }
    }

    @Override
    public void info(String msg)
    {
        logger.info(msg);
    }

    @Override
    public void warn(String msg)
    {
        logger.warning(msg);
    }

    @Override
    public void error(String msg)
    {
        logger.severe(msg);
    }

    @Override
    public void setFilter(Filter filter)
    {
        logger.setFilter(filter);
    }

    @Override
    public void exit()
    {
        Arrays.stream(logger.getHandlers()).peek(Handler::flush).forEach(Handler::close);
    }
}