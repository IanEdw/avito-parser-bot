package ru.ianedw.telegrambots.services;

import org.springframework.stereotype.Service;
import ru.ianedw.telegrambots.models.Person;
import ru.ianedw.telegrambots.models.Rule;
import ru.ianedw.telegrambots.models.Target;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class BotService {
    private final TargetsService targetsService;
    private final PeopleService peopleService;
    private final RulesService rulesService;

    public BotService(TargetsService targetsService, PeopleService peopleService, RulesService rulesService) {
        this.targetsService = targetsService;
        this.peopleService = peopleService;
        this.rulesService = rulesService;
    }


    public String start(Person person, long chatId, String name) {
        if (person == null) {
            person = new Person();
            person.setChatId(chatId);
            person.setName(name);
        }
        person.setLastCommand("/menu");
        peopleService.save(person);
        return "Добро пожаловать";
    }

    public String menu(Person person) {
        person.setLastCommand("/menu");
        peopleService.save(person);
        return "Вы в главном меню";
    }

    public String beginAddTarget(Person person) {
        person.setLastCommand("/addLink");
        peopleService.save(person);
        return "Вставьте ссылку на поиск с десктопной версии авито с сортировкой по дате";
    }

    public String addLink(Person person, String receivedMessage) {
        if (!receivedMessage.contains("https://www.avito.ru")) {
            return "Бот поддерживает только сайт авито.\nВставте ссылку с авито.";
        }

        if (!receivedMessage.contains("q=")) {
            return "Необходимо вставить ссылку с поисковым запросом";
        }

        if (!receivedMessage.contains("s=104")) {
            return "Необходимо задать сортировку по дате\nВставьте ссылку с сортировкой по дате";
        }

        Target target = targetsService.getTargetByLink(receivedMessage);
        if (target == null) {
            target = new Target();
            target.setLink(receivedMessage);
            target.setName(getTargetNameFromLinkParams(receivedMessage));

            List<Person> people = new ArrayList<>();
            people.add(person);

            target.setPeople(people);
            person.getTargets().add(target);
            target = targetsService.save(target);
            person.setLastCommand("/addRule:" + target.getId());
            peopleService.save(person);
        } else {
            target.getPeople().add(person);
            targetsService.save(target);
            person.setLastCommand("/menu");
            person.getTargets().add(target);
            peopleService.save(person);
        }
        return "Введите максимальную цену для этой цели.";
    }

    public String addRule(Person person, String receivedMessage) {
        int targetId = Integer.parseInt(person.getLastCommand().split(":")[1]);
        Rule rule = new Rule();
        rule.setTargetId(targetId);
        rule.setOwner(person);
        rule.setMaxPrice(Integer.parseInt(receivedMessage));
        rulesService.save(rule);
        person.setLastCommand("/menu");
        peopleService.save(person);
        return "Вы в главном меню.";
    }

    public String myTargets(Person person) {
        List<Target> targets = person.getTargets();
        StringBuilder sb = new StringBuilder();
        sb.append("Ваши цели:\n");
        for (Target target : targets) {
            sb.append("Имя - ").append(target.getName()).append(", id - ").append(target.getId()).append("\n");
        }
        return sb.toString();
    }

    public String deleteTargetBegin(Person person) {
        person.setLastCommand("/deleteTarget");
        peopleService.save(person);
        return "Введите id цели которую хотите удалить";
    }

    public String deleteTarget(Person person, String receivedMessage) {
        int targetId;
        try {
            targetId = Integer.parseInt(receivedMessage);
        } catch (NumberFormatException e) {
            return "Введите целое число";
        }
        List<Target> targets = person.getTargets();
        Target targetToRemove = targets.stream().filter(t -> t.getId() == targetId).findAny().orElse(null);

        if (targetToRemove == null) {
            return "Вы не отслеживаете цель с id " + targetId + "\nВведите id цели которую хотите удалить";
        }

        targets.remove(targetToRemove);
        person.setLastCommand("/menu");
        peopleService.save(person);
        targetToRemove = targetsService.getTargetById(targetId);
        if (targetToRemove.getPeople().size() == 0) {
            targetsService.delete(targetId);
        }
        return "Цель удалена\nВы в главном меню";
    }

    public Person getOneByChatId(long chatId) {
        return peopleService.getOneByChatId(chatId);
    }

    private String getTargetNameFromLinkParams(String link) {
        String searchParam = Arrays.stream(link.split("&")).filter(s -> s.contains("q=")).findAny().orElse(null);
        return searchParam.split("=")[1].replaceAll("\\+", " ");
    }
}
