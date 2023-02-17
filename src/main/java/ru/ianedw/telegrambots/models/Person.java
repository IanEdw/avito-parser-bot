package ru.ianedw.telegrambots.models;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "person")
public class Person {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(name = "chat_id")
    private long chatId;
    @Column(name = "name")
    private String name;
    @Column(name = "last_command")
    private String lastCommand;
    @ManyToMany
    @JoinTable(
            name = "person_target",
            joinColumns = @JoinColumn(name = "person_id"),
            inverseJoinColumns = @JoinColumn(name = "target_id")
    )
    private List<Target> targets;
    @OneToMany(mappedBy = "owner")
    private List<Rule> rules;


    public Person() {
    }

    public int getRuleMaxPrice(int targetId) {
        Rule result = rules.stream().filter(rule -> rule.getTargetId() == targetId).findAny().orElse(null);
        if (result != null) {
            return result.getMaxPrice();
        } else {
            return Integer.MAX_VALUE;
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getChatId() {
        return chatId;
    }

    public void setChatId(long chatId) {
        this.chatId = chatId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Target> getTargets() {
        return targets;
    }

    public void setTargets(List<Target> targets) {
        this.targets = targets;
    }

    public String getLastCommand() {
        return lastCommand;
    }

    public void setLastCommand(String lastCommand) {
        this.lastCommand = lastCommand;
    }

    public List<Rule> getRules() {
        return rules;
    }

    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }
}
