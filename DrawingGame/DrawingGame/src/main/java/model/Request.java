package model;

import java.util.UUID;

public class Request {

    private UUID id;
    private Action action;
    private int stick;

    public Request() {

    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public void setStick(int stick) {
        this.stick = stick;
    }

    public int getStick() {
        return stick;
    }
}
