package ru.ianedw.telegrambots.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.ianedw.telegrambots.models.Rule;

@Repository
public interface RulesRepository extends JpaRepository<Rule, Integer> {

}
