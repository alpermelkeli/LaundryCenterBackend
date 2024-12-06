package org.alpermelkeli.model;

public class Machine {
    private boolean active;
    private String id;
    private Long start;
    private Long time;

    public Machine(boolean active, String id, Long start, Long time) {
        this.active = active;
        this.id = id;
        this.start = start;
        this.time = time;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getStart() {
        return start;
    }

    public void setStart(Long start) {
        this.start = start;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }
}
