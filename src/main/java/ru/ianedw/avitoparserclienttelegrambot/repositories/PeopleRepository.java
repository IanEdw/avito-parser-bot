package ru.ianedw.avitoparserclienttelegrambot.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.ianedw.avitoparserclienttelegrambot.models.Person;

import java.util.Optional;

@Repository
public interface PeopleRepository extends JpaRepository<Person, Integer> {
    Optional<Person> findByChatId(long chatId);
    Optional<Person> findById(int id);
}
