package ru.ianedw.avitoparserclienttelegrambot.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.ianedw.avitoparserclienttelegrambot.client.ApiClient;
import ru.ianedw.avitoparserclienttelegrambot.models.*;
import ru.ianedw.avitoparserclienttelegrambot.services.PeopleService;
import ru.ianedw.avitoparserclienttelegrambot.services.TargetsService;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class UpdateSender {
    Logger log = LoggerFactory.getLogger(UpdateSender.class);
    private final ApiClient apiClient;
    private final PeopleService peopleService;
    private final TargetsService targetsService;
    private final ClientBot clientBot;
    private Map<Integer, Map<String, Post>> availablePosts;
    private Map<Integer, List<Person>> targetIdsWithPeople;

    @Autowired
    public UpdateSender(ApiClient apiClient, PeopleService peopleService, TargetsService targetsService, ClientBot clientBot) {
        this.apiClient = apiClient;
        this.peopleService = peopleService;
        this.targetsService = targetsService;
        this.clientBot = clientBot;
        availablePosts = new HashMap<>();
        loadAvailablePosts();
        updateTargetMapPeople();
    }

    @Scheduled(initialDelay = 2000, fixedDelay = 5000)
    public void sendUpdateToPeople() {
        try {
            updateTargetMapPeople();
            Update newUpdate = apiClient.getNewUpdate();
            Map<Integer, Map<String, Post>> newTargetPosts = newUpdate.getTargetPosts();

            for (Integer targetId : newTargetPosts.keySet()) {
                log.info("Сравнение апдейтов с targetId: " + targetId);
                if (!availablePosts.containsKey(targetId)) {
                    loadAvailablePosts(targetId, newTargetPosts.get(targetId));
                }

                List<Post> newPosts = getNewPosts(newTargetPosts.get(targetId), availablePosts.get(targetId));

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

            availablePosts = newUpdate.getTargetPosts();
        } catch (TelegramApiException ignored) {
        }
    }

    public void loadAvailablePosts() {
        Map<Integer, Map<String, Post>> newUpdate = apiClient.getNewUpdate().getTargetPosts();
        for (Integer integer : newUpdate.keySet()) {
            if (!availablePosts.containsKey(integer)) {
                loadAvailablePosts(integer, newUpdate.get(integer));
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

    private void updateTargetMapPeople() {
        Map<Integer, List<Person>> map = peopleService.getTargetMapPeople();

        List<Integer> targetIdsToRemove = map.keySet().stream()
                .filter(integer -> map.get(integer).size() == 0)
                .collect(Collectors.toList());
        targetIdsToRemove.forEach(map::remove);
        targetIdsToRemove.forEach(targetsService::delete);
        apiClient.removeTargetsFromServer(targetIdsToRemove);
        targetIdsWithPeople = map;
    }
}
