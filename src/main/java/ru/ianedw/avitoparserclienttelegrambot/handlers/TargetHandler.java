package ru.ianedw.avitoparserclienttelegrambot.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ianedw.avitoparserclienttelegrambot.client.ApiClient;
import ru.ianedw.avitoparserclienttelegrambot.models.Person;
import ru.ianedw.avitoparserclienttelegrambot.models.Rule;
import ru.ianedw.avitoparserclienttelegrambot.models.Target;
import ru.ianedw.avitoparserclienttelegrambot.models.TargetDTO;
import ru.ianedw.avitoparserclienttelegrambot.services.PeopleService;
import ru.ianedw.avitoparserclienttelegrambot.services.RulesService;
import ru.ianedw.avitoparserclienttelegrambot.services.TargetsService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class TargetHandler {
    private final PeopleService peopleService;
    private final TargetsService targetsService;
    private final RulesService rulesService;
    private final ApiClient apiClient;

    @Autowired
    public TargetHandler(PeopleService peopleService, TargetsService targetsService, RulesService rulesService, ApiClient apiClient) {
        this.peopleService = peopleService;
        this.targetsService = targetsService;
        this.rulesService = rulesService;
        this.apiClient = apiClient;
    }

    public String beginAddTarget(Person person) {
        person.setLastCommand("/addLink");
        peopleService.save(person);
        return "Вставьте ссылку на поиск с десктопной версии авито с сортировкой по дате";
    }

    public String addLink(Person person, String receivedMessage) {
        if (receivedMessage.contains("https://avito.ru")) {
            return "Пока бот поддерживает только сайт авито.\nВставте ссылку с авито.";
        }
        Target target = targetsService.getTargetByLink(receivedMessage);
        if (target == null) {
            TargetDTO dto = new TargetDTO();
            dto.setLink(receivedMessage);
            dto = apiClient.sendTargetToParseServer(dto);

            target = new Target();
            target.setId(dto.getId());
            target.setLink(dto.getLink());
            target.setName(getTargetNameFromLinkParams(receivedMessage));

            List<Person> people = new ArrayList<>();
            people.add(person);

            target.setPeople(people);
            person.setLastCommand("/addRule:" + target.getId());
            person.getTargets().add(target);
            targetsService.save(target);
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

    private String getTargetNameFromLinkParams(String link) {
        String searchParam = Arrays.stream(link.split("&")).filter(s -> s.contains("q=")).findAny().orElse(null);
        return searchParam.split("=")[1].replaceAll("\\+", " ");
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
        return "Цель удалена\nВы в главном меню";
    }
}