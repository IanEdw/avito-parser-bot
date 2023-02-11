package ru.ianedw.avitoparserclienttelegrambot.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ianedw.avitoparserclienttelegrambot.models.Person;
import ru.ianedw.avitoparserclienttelegrambot.services.PeopleService;

@Component
public class MenuHandler {
    private final PeopleService peopleService;

    @Autowired
    public MenuHandler(PeopleService peopleService) {
        this.peopleService = peopleService;
    }

    public String begin(Person person, long chatId, String name) {
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
        return "Вы в главном меню";
    }
}
