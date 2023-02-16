package ru.ianedw.avitoparserclienttelegrambot.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.ianedw.avitoparserclienttelegrambot.models.*;
import ru.ianedw.avitoparserclienttelegrambot.parser.Parser;
import ru.ianedw.avitoparserclienttelegrambot.services.PeopleService;
import ru.ianedw.avitoparserclienttelegrambot.services.TargetsService;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
public class UpdateSender {
    Logger log = LoggerFactory.getLogger(UpdateSender.class);
    private final Parser parser;
    private final ClientBot clientBot;
    private final PeopleService peopleService;
    private final TargetsService targetsService;
    private Map<Integer, Map<String, Post>> availablePosts;
    private Map<Integer, List<Person>> targetIdsWithPeople;
    private List<Target> targets;

    @Autowired
    public UpdateSender(Parser parser, ClientBot clientBot, PeopleService peopleService, TargetsService targetsService) {
        this.parser = parser;
        this.clientBot = clientBot;
        this.peopleService = peopleService;
        this.targetsService = targetsService;
        availablePosts = new HashMap<>();
        refreshAvailablePosts();
    }

    @Scheduled(initialDelay = 2000, fixedDelay = 15000)
    public void sendUpdateToPeople() {
        try {
            updateTargetsMapPeople();
            Map<Integer, Map<String, Post>> newAvailablePosts = parser.getAvailablePosts(targets);

            for (Integer targetId : newAvailablePosts.keySet()) {
                log.info("Сравнение апдейтов с targetId: " + targetId);
                if (!availablePosts.containsKey(targetId)) {
                    loadAvailablePosts(targetId, newAvailablePosts.get(targetId));
                }

                List<Post> newPosts = getNewPosts(newAvailablePosts.get(targetId), availablePosts.get(targetId));

                for (Post newPost : newPosts) {
                    String message = "Новое объявление для цели c id -  " + targetId + "\n";
                    int price = newPost.getPrice();
                    List<Person> people = targetIdsWithPeople.get(targetId);
                    if (people == null) {
                        return;
                    }
                    for (Person person : people) {
                        if (price <= person.getRuleMaxPrice(targetId)) {
                            clientBot.sendMessage(person.getChatId(), message + newPost);
                        }
                    }
                }
            }

            availablePosts = newAvailablePosts;
        } catch (TelegramApiException ignored) {
        }
    }

    @Scheduled(timeUnit = TimeUnit.HOURS, fixedDelay = 3)
    private void refreshAvailablePosts() {
        updateTargetsMapPeople();
        loadAvailablePosts();
    }


    private void loadAvailablePosts() {
        Map<Integer, Map<String, Post>> newAvailablePosts = parser.loadAvailablePosts(targets);
        for (Integer integer : newAvailablePosts.keySet()) {
            if (!availablePosts.containsKey(integer)) {
                loadAvailablePosts(integer, newAvailablePosts.get(integer));
            }
        }
    }

    private void loadAvailablePosts(Integer targetId, Map<String, Post> posts) {
        Map<String, Post> map = new HashMap<>(posts);
        availablePosts.put(targetId, map);
    }

    private List<Post> getNewPosts(Map<String, Post> newPosts, Map<String, Post> availablePosts) {
        List<Post> result = new ArrayList<>();

        for (Post newPost : newPosts.values()) {
            if (!availablePosts.containsKey(newPost.getLink())) {
                result.add(newPost);
            }
        }
        return result;
    }

    private void updateTargetsMapPeople() {
        List<Person> people = peopleService.getAllPeople();
        Map<Integer, List<Person>> map = new HashMap<>();

        for (Person person : people) {
            for (Target target : person.getTargets()) {
                List<Person> targetPeople;
                if (map.containsKey(target.getId())) {
                    targetPeople = map.get(target.getId());
                } else {
                    targetPeople = new ArrayList<>();
                }
                targetPeople.add(person);
                map.put(target.getId(), targetPeople);
            }
        }

        List<Integer> targetIdsToRemove = map.keySet().stream()
                .filter(integer -> map.get(integer).size() == 0).toList();
        targetIdsToRemove.forEach(map::remove);
        targetIdsToRemove.forEach(targetsService::delete);
        targetIdsWithPeople = map;
        updateTargets();
    }

    private void updateTargets() {
        targets = targetsService.getAllTargets();
    }
}
