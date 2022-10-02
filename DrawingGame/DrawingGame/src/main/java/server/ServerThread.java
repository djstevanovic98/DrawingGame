package server;

import com.google.gson.Gson;
import model.*;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.*;

public class ServerThread extends Thread {

    Socket socket;
    BufferedReader in;
    PrintWriter out;

    private Gson gson;
    private Table table;

    public static int MAX_ROUNDS =60;
    static int trenutniIgrac = 0;
    static int brojRundi=1;
    static int counter = 0;
    static int stap = -1;

    static Semaphore semaphore = new Semaphore(0,true);
    static CyclicBarrier runda = new CyclicBarrier(6);

    private static final Object Lock = new Object();
    private static final Object rundaLock = new Object();
    private static final Object barijeraLock = new Object();

    private static boolean primajGoste = true;
    private static int barijeraCounter = 0;



    public ServerThread(Socket socket, Table table) {
        this.socket = socket;
        this.table = table;

        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        gson = new Gson();
    }

    public void run() {

        try {
            Request request = receiveRequest(); // ovde stize request iz ClientThread

            Player player = new Player(request.getId());

            Response response = new Response();
            response.setResult(Result.FAILURE);

            if(request.getAction() == Action.REQUEST_CHAIR && primajGoste) { //pitamo sta zeli
                if(table.giveSeat(player) && MAX_ROUNDS>6){
                    response.setResult(Result.SUCCESS);
                }
                sendResponse(response); //saljemo nazac klijentu
            }

            if(response.getResult()== Result.SUCCESS) {
                Player[] igraci = table.getPlayers();

                if(player.equals(igraci[trenutniIgrac])){
                    response.setAction(Action.REQUEST_STICK);
                }else{
                    response.setAction(Action.REQUEST_GUESS);
                }
                sendResponse(response);

                boolean flagZaIspis = false;

                while (true) {

                    igraci = table.getPlayers();
                    request = receiveRequest();


                    if (request.getAction().equals(Action.REQUEST_STICK)) {
                        try {
                            semaphore.acquire();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        stap = request.getStick();
                        player.setStick(stap);

                        System.out.println(player.getId() + " je izvukao: " + stap);
                        System.out.println("------------------------- RUNDA BROJ: " + brojRundi++ + " -------------------------");
                    } else {
                        int pogodak = request.getStick();
                        player.setStick(pogodak);
                        System.out.println(player.getId() + " je pogadjao: " + pogodak);

                        synchronized (Lock) {
                            ++counter;

                            if ((counter % 5) == 0) {
                                semaphore.release();
                            }
                        }
                    }

                    //semafor od 6 i kad prodje dajemo poene
                    try {
                        runda.await(5000, TimeUnit.MILLISECONDS);
                    } catch (TimeoutException e) {
                        primajGoste = false;

                        response.setAction(Action.REQUEST_LEAVE);
                        sendResponse(response);

                        System.out.println("Igrac: " + player.getId() + " se gasi zbog nedovoljnog broja igraca......");
                        flagZaIspis = true;
                        break;

                    }catch (InterruptedException e){

                    }catch(BrokenBarrierException e){
                        primajGoste = false;

                        synchronized (barijeraLock){
                            ++barijeraCounter;

                            if(barijeraCounter == 5){
                                flagZaIspis = true;
                            }
                        }


                        response.setAction(Action.REQUEST_LEAVE);
                        sendResponse(response);

                        System.out.println("Igrac: " + player.getId() + " se gasi zbog nedovoljnog broja igraca......");
                        break;

                    }

                    if (stap == 3) {
                        if (request.getAction()==Action.REQUEST_STICK) {
                            table.removeSeat(player);
                            System.out.println("Igrac: " + player.getId() + " je izvukao kraci stapic!!\n");

                            response.setAction(Action.REQUEST_LEAVE);
                            sendResponse(response);
                            System.out.println("Restartujemo rundu! ");
                            brojRundi--;
                            break;
                        }else{
                            response.setAction(Action.REQUEST_GUESS);
                            sendResponse(response);
                        }
                        continue;
                    } else {
                        if (request.getAction().equals(Action.REQUEST_GUESS)) {
                            if (player.getStick() == stap) {
                                player.addPoint();
                                System.out.println("Igrac: " + player.getId() + " je dobio poen za pogodak!!!\n");
                            }
                        } else {
                            trenutniIgrac = (trenutniIgrac + 1) % 6;
                        }
                    }

                    synchronized (rundaLock){
                        MAX_ROUNDS--;
                        if(MAX_ROUNDS < 6) {
                            if(player.equals(igraci[trenutniIgrac])) {
                                flagZaIspis = true;
                            }
                            primajGoste = false;
                            response.setAction(Action.REQUEST_LEAVE);
                            sendResponse(response);

                            System.out.println("Igrac: " + player.getId() + " se gasi......");
                            break;
                        }else{
                            if(player.equals(igraci[trenutniIgrac])) {
                                response.setAction(Action.REQUEST_STICK);
                            }else{
                                response.setAction(Action.REQUEST_GUESS);
                            }
                            sendResponse(response);
                        }
                    }
                }
                if(flagZaIspis){
                    try {
                        currentThread().sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    ServerMain.ispisiRezultat();
                }
            }


            // TODO

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
                out.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Request receiveRequest() throws IOException {
        return gson.fromJson(in.readLine(), Request.class);
    }

    private void sendResponse(Response response) {
        out.println(gson.toJson(response));
    }
}
