package ru.ianedw.telegrambots.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.ianedw.telegrambots.bot.ClientBot;
import ru.ianedw.telegrambots.models.Person;
import ru.ianedw.telegrambots.models.Post;
import ru.ianedw.telegrambots.models.Target;
import ru.ianedw.telegrambots.services.PeopleService;
import ru.ianedw.telegrambots.services.TargetsService;
import ru.ianedw.telegrambots.util.NotTargetPost;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
public class Parser {
    Logger log = LoggerFactory.getLogger(Parser.class);
    private final ClientBot clientBot;
    private final PeopleService peopleService;
    private final TargetsService targetsService;
    private Map<Integer, Map<String, Post>> availablePosts;
    private Map<Integer, List<Person>> targetIdsWithPeople;
    private List<Target> targets;
    private final String XPATH_QUERY = "//div[@data-marker='catalog-serp']//div[@data-marker='item']";


    @Autowired
    public Parser(ClientBot clientBot, PeopleService peopleService, TargetsService targetsService) {
        this.clientBot = clientBot;
        this.peopleService = peopleService;
        this.targetsService = targetsService;
        reloadAvailablePosts();
    }

    @Scheduled(timeUnit = TimeUnit.SECONDS, initialDelay = 30, fixedDelay = 30)
    public void updateAvailablePosts() {
        log.info("Start updateAvailablePosts()");
        updateTargetsMapPeople();
        Collections.shuffle(targets);
        for (Target target : targets) {
            log.info("updateAvailablePosts(" + target.getId() + ")");
            if (availablePosts.containsKey(target.getId())) {
                updatePosts(target);
            } else {
                loadAvailablePosts(target);
            }
        }
        log.info("--------------------------------------------\n\n");
    }

    @Scheduled(cron = "0 0 0 * * *")
    private void reloadAvailablePosts() {
        updateTargetsMapPeople();
        loadAvailablePosts(targets);
    }

    private void updatePosts(Target target) {
        try {
            Element body = Jsoup.connect(target.getLink()).get().body();

            for (int i = 0; i < body.selectXpath(XPATH_QUERY).size(); i++) {
                try {
                    updatePost(body, i, target.getId());
                } catch (NotTargetPost e) {
                    break;
                }
            }
        } catch (IOException e) {
            log.warn("-----------JSOUP COULDN'T CONNECT------------");
        }
    }

    private void updatePost(Element body, int blockNumber, int targetId) {
        String expression = String.format(XPATH_QUERY + "[%d]", blockNumber + 1);
        String link = findPostLink(body, expression);
        Map<String, Post> targetPosts = availablePosts.get(targetId);

        if (!isPostPublicationTimeValid(body, expression)) {
            targetPosts.remove(link);
            throw new NotTargetPost();
        }

        if (targetPosts.containsKey(link)) {
            return;
        }
        log.info("Detected new post");
        Post newPost = new Post();

        newPost.setLink(link);
        newPost.setName(findPostName(body, expression));
        newPost.setPrice(findPostPrice(body, expression));

        targetPosts.put(link, newPost);
        sendNewPostToPeople(targetId, newPost);
    }

    private void sendNewPostToPeople(int targetId, Post newPost) {
        try {
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
        } catch (TelegramApiException ignored) {
        }
    }


    public void loadAvailablePosts(List<Target> targets) {
        availablePosts = new HashMap<>();
        targets.forEach(this::loadAvailablePosts);
    }

    private void loadAvailablePosts(Target target) {
        Map<String, Post> posts = new HashMap<>();
        try {
            Element body = Jsoup.connect(target.getLink()).get().body();

            for (int i = 0; i < body.selectXpath(XPATH_QUERY).size(); i++) {
                try {
                    Post post = loadPost(body, i);
                    posts.put(post.getLink(), post);
                } catch (NotTargetPost e) {
                    break;
                }
            }

            availablePosts.put(target.getId(), posts);
            log.info("Loaded " + posts.size() + " posts");
        } catch (IOException e) {
            log.warn("-----------JSOUP COULDN'T CONNECT------------");
        }
    }

    private Post loadPost(Element body, int blockNumber) {
        String expression = String.format(XPATH_QUERY + "[%d]", blockNumber + 1);

        if (isPostPublicationTimeValid(body, expression)) {
            Post post = new Post();

            post.setName(findPostName(body, expression));
            post.setLink(findPostLink(body, expression));
            post.setPrice(findPostPrice(body, expression));

            return post;
        } else {
            throw new NotTargetPost();
        }
    }


    private boolean isPostPublicationTimeValid(Element body, String parentExpression) {
        String dateText = body.selectXpath(parentExpression + "//div[@data-marker='item-date']").text();
        String[] dateArray = dateText.split(" ");
        String timeUnit = dateArray[1];

        switch (timeUnit) {
            case "минут", "минуту", "минуты" -> {
                return true;
            }
            case "часов", "час" -> {
                return Integer.parseInt(dateArray[0]) <= 3;
            }
            default -> {
                return false;
            }
        }
    }

    private String findPostName(Element body, String parentExpression) {
        return body.selectXpath(parentExpression + "//a[@data-marker='item-title']").attr("title");
    }

    private String findPostLink(Element body, String parentExpression) {
        return "https://www.avito.ru" + body.selectXpath(parentExpression + "//a[@data-marker='item-title']").attr("href");
    }

    private int findPostPrice(Element body, String parentExpression) {
        return Integer.parseInt(body.selectXpath(parentExpression + "//meta[@itemprop='price']").attr("content"));
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

        targetIdsWithPeople = map;
        updateTargets();
    }

    private void updateTargets() {
        targets = targetsService.getAllTargets();
    }
}
