package model;

import java.util.ArrayList;
import java.util.List;

public class  Table {

    private Player[] players;
    public List<Player> finalnaLista;

    public Table() {
        this.players = new Player[6];
        this.finalnaLista = new ArrayList<Player>();
    }

    public synchronized boolean giveSeat(Player player) {
        for (int i = 0; i < 6; i++) {
            if (players[i] == null) {
                players[i] = player;
                finalnaLista.add(player);
                return true;
            }
        }

        return false;
    }


    public synchronized void removeSeat(Player player) {
        for (int i = 0; i < 6; i++) {
            if (players[i] == player) {
                players[i] = null;
                break;
            }
        }
    }

    public Player[] getPlayers() {
        return players;
    }

    public List<Player> getFinalnaLista() {
        return finalnaLista;
    }
}
