package ru.ianedw.telegrambots.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.ianedw.telegrambots.models.Target;


@Repository
public interface TargetsRepository extends JpaRepository<Target, Integer> {
}
