package ru.ianedw.avitoparserclienttelegrambot.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.ianedw.avitoparserclienttelegrambot.models.Target;


@Repository
public interface TargetsRepository extends JpaRepository<Target, Integer> {
}
