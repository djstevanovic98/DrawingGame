package server;

import model.Player;
import model.Table;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerMain {

    private static final int PORT = 9999;
    public static ExecutorService executorService;
    public static Table table = new Table();

    public static void main(String[] args) {

        //Table table = new Table(); // treba da koriste sve niti

        try {
            ServerSocket ss = new ServerSocket(PORT);
            executorService = Executors.newCachedThreadPool();
            System.out.println("Krupije je spreman");

            while(true) {
                Socket socket = ss.accept();
                executorService.submit(new ServerThread(socket, table));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        //u ovoj klasi ne gasimo pool jer uvek treba da se vrtimo i cekamo igraca.
    }

    public static void ispisiRezultat(){
        List<Player> finalnaLista = table.getFinalnaLista();
        Player pobednik = null;
        int max = 0;

        System.out.println("\nIgrali su: \n");

        for(Player player: finalnaLista){
            System.out.println("Igrac: " + player.getId() + " broj poena: " + player.getPoints());
            if(player.getPoints() > max){
                max = player.getPoints();
                pobednik = player;
            }
        }
        System.out.println("\nPobednik je: " + pobednik.getId() + " broj poena: " + pobednik.getPoints());

        System.exit(0);
    }

}
