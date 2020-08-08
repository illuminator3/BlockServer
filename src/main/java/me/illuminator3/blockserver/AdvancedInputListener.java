package me.illuminator3.blockserver;

import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.function.Consumer;

public class AdvancedInputListener
    implements IInputListener
{
    private final Consumer<String> onInput;
    private final String pre;
    private final Thread listenerThread;

    public AdvancedInputListener(Consumer<String> onInput, String pre)
    {
        this.onInput = onInput;
        this.pre = pre;

        this.listenerThread = new Thread(() -> {
            Scanner scanner = new Scanner(new InputStreamReader(System.in));

            while (true)
            {
                System.out.print(pre);
                String next = scanner.nextLine();

                this.onInput(next);
            }
        });

        this.listenerThread.setDaemon(true);
        this.listenerThread.start();
    }

    @Override
    public void onInput(String input)
    {
        this.onInput.accept(input);
    }

    public String getPre()
    {
        return pre;
    }

    public Thread getListenerThread()
    {
        return listenerThread;
    }
}