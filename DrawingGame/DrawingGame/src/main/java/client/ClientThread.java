package client;

import com.google.gson.Gson;
import model.Action;
import model.Request;
import model.Response;
import model.Result;

import java.io.*;
import java.net.Socket;
import java.util.UUID;

public class ClientThread implements Runnable{

    private static final int PORT = 9999;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    private Gson gson;

    public ClientThread() throws IOException {
        socket = new Socket("localhost", PORT);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
        gson = new Gson();
    }

    public void run() {
        Request request = new Request(); // svejedno je da li prazan konstr pa dodajemo
        // zasebno id i setAction ili konstruktor gde ceo id ubacujemo i setAction
        // kod odvojenog mozemo da stvorimo request i bez id, a obrnuto ne bi mogli da
        //inicijalizujemo. moze i new Request(id, Action.Request_chair);
        UUID id = UUID.randomUUID();

        request.setId(id);
        request.setAction(Action.REQUEST_CHAIR);
        sendRequest(request);

        Response response = receiveResponse();

        if(response.getResult() == Result.SUCCESS) {
            System.out.println("Igrad " + id.toString() + " je uspeo da se prikljuci igri.");

            while(true) {

                response = receiveResponse();
                if(response.getAction()==Action.REQUEST_LEAVE){
                    System.out.println("Igrac: " + id + " --- je napustio sto!");
                    break;
                }
                if((response.getAction() == Action.REQUEST_STICK) || (response.getAction() == Action.REQUEST_GUESS) ) {

                    request.setAction(response.getAction());
                    int randomBroj = (int) (Math.random() * 6);

                    request.setStick(randomBroj);
                    sendRequest(request);
                    System.out.println(id + "Poslao broj!!!!");

                }
            }

        }




    }

    public void sendRequest(Request request) {
        out.println(gson.toJson(request));
        //pomocu gson objekat pretvaramo u json i to je to
    }

    public Response receiveResponse() {
        try {
            return gson.fromJson(in.readLine(), Response.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
