package model;

import java.util.UUID;

public class Player {

    private UUID id;
    private int points;
    private int stick = -1;


    public Player(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public int getPoints() {
        return points;
    }

    public void addPoint() {
        points++;
    }

    public int getStick() {
        return stick;
    }

    public void setStick(int stick) {
        this.stick = stick;
    }
}
