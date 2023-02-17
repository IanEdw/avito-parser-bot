package ru.ianedw.telegrambots.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ianedw.telegrambots.models.Person;
import ru.ianedw.telegrambots.repositories.PeopleRepository;

import java.util.*;

@Service
@Transactional(readOnly = true)
public class PeopleService {
    @PersistenceContext
    private EntityManager entityManager;
    private final PeopleRepository repository;

    @Autowired
    public PeopleService(PeopleRepository repository) {
        this.repository = repository;
    }

    public List<Person> getAllPeople() {
        Session session = entityManager.unwrap(Session.class);
        List<Person> people = session.createQuery(
                "select distinct p from Person p left join fetch p.rules", Person.class
        ).getResultList();
        people = session.createQuery(
                "select distinct p from Person p left join fetch p.targets where p in :people", Person.class
        ).setParameter("people", people).getResultList();
        return people;
    }

    public Person getOneByChatId(long chatId) {
        Session session = entityManager.unwrap(Session.class);
        Person person = session.createQuery(
                "select distinct p from Person p left join fetch p.targets where p.chatId =:chatId", Person.class
        ).setParameter("chatId", chatId).getSingleResultOrNull();

        if (person != null) {
            person = session.createQuery(
                    "select distinct p from Person p left join fetch p.rules where p in :person", Person.class
            ).setParameter("person", person).getSingleResultOrNull();
        }
        return person;
    }

    @Transactional
    public void save(Person person) {
        repository.save(person);
    }
}
