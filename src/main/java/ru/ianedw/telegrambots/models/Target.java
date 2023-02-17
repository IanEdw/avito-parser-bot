package ru.ianedw.telegrambots.models;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "Target")
public class Target {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    @Column(name = "name")
    private String name;
    @Column(name = "link")
    private String link;
    @ManyToMany(mappedBy = "targets")
    private List<Person> people;

    public Target() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int targetId) {
        this.id = targetId;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public List<Person> getPeople() {
        return people;
    }

    public void setPeople(List<Person> hunters) {
        this.people = hunters;
    }
}
