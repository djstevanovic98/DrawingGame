package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ClientMain {

    public static ScheduledExecutorService scheduledExecutorService;

    public static void main(String[] args) throws IOException {
        scheduledExecutorService = Executors.newScheduledThreadPool(10);

        BufferedReader tast = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("Unesite broj igraca:");
        int n = Integer.parseInt(tast.readLine());

        for (int i = 0; i < n; i++) {
            int randomBroj = (int) (Math.random()*1000);

            scheduledExecutorService.schedule(new ClientThread(), randomBroj, TimeUnit.MILLISECONDS);
        }

        scheduledExecutorService.shutdown();
    }
}
