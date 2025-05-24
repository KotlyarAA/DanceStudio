package com.example.dancestudio;

import java.io.Serializable;

public class DanceClass implements Serializable {
    private String id;
    private String direction;
    private String level;
    private String trainer;
    private String date;
    private String time;

    public DanceClass() {}

    public DanceClass(String id, String direction, String level, String trainer, String date, String time) {
        this.id = id;
        this.direction = direction;
        this.level = level;
        this.trainer = trainer;
        this.date = date;
        this.time = time;
    }

    public String getId() { return id; }
    public String getDirection() { return direction; }
    public String getLevel() { return level; }
    public String getTrainer() { return trainer; }
    public String getDate() { return date; }
    public String getTime() { return time; }
}
