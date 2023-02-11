package ru.ianedw.avitoparserclienttelegrambot.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.ianedw.avitoparserclienttelegrambot.models.Rule;

@Repository
public interface RulesRepository extends JpaRepository<Rule, Integer> {

}
