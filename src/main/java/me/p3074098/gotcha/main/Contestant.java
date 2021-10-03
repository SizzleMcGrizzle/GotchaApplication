package me.p3074098.gotcha.main;

import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Contestant implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private final int id;
    private final String name;
    
    private int kills = 0;
    private int round = -1;
    private boolean dead = false;
    
    public Contestant(String name, int id) {
        this.name = name;
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public int getId() {
        return id;
    }
    
    public int getKills() {
        return kills;
    }
    
    public void addKill() {
        kills++;
    }
    
    public void setRoundOut(int roundOut) {
        this.round = roundOut;
    }
    
    public int getRoundOut() {
        return round;
    }
    
    public boolean isDead() {
        return dead;
    }
    
    public void setDead(boolean dead) {
        this.dead = dead;
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
    }
    
    private void readObject(ObjectInputStream ois) {
        try {
            ois.defaultReadObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error while loading Contestant class.");
            e.printStackTrace();
        }
    }
    
}
