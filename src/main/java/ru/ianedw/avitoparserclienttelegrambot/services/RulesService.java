package ru.ianedw.avitoparserclienttelegrambot.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ianedw.avitoparserclienttelegrambot.models.Rule;
import ru.ianedw.avitoparserclienttelegrambot.repositories.RulesRepository;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class RulesService {
    private final RulesRepository repository;

    @Autowired
    public RulesService(RulesRepository repository) {
        this.repository = repository;
    }

    public List<Rule> getAll() {
        return repository.findAll();
    }

    public Rule getRuleById(int id) {
        return repository.findById(id).orElse(null);
    }

    @Transactional
    public void save(Rule rule) {
        repository.save(rule);
    }
}
