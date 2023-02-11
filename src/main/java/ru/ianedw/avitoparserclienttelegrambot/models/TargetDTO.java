package ru.ianedw.avitoparserclienttelegrambot.models;


public class TargetDTO {
    private int id;
    private String link;

    public TargetDTO() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}