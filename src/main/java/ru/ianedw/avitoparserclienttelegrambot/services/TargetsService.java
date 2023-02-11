package ru.ianedw.avitoparserclienttelegrambot.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ianedw.avitoparserclienttelegrambot.models.Target;
import ru.ianedw.avitoparserclienttelegrambot.repositories.TargetsRepository;

import java.util.List;


@Service
@Transactional(readOnly = true)
public class TargetsService {
    @PersistenceContext
    private EntityManager entityManager;
    private final TargetsRepository targetsRepository;

    @Autowired
    public TargetsService(TargetsRepository targetsRepository) {
        this.targetsRepository = targetsRepository;
    }

    public List<Target> getAllTargets()  {
        Session session = entityManager.unwrap(Session.class);
        return session.createQuery("select t from Target t left join fetch t.people", Target.class).getResultList();
    }

    public Target getTargetByLink(String link) {
        Session session = entityManager.unwrap(Session.class);
        Query<Target> query = session.createQuery("select t from Target t left join fetch t.people where t.link = :link", Target.class);
        query.setParameter("link", link);
        return query.getResultList().stream().findAny().orElse(null);
    }

    public Target getTargetById(int id) {
        Session session = entityManager.unwrap(Session.class);
        Query<Target> query = session.createQuery("select t from Target t left join fetch t.people where t.id = :id", Target.class);
        query.setParameter("id", id);
        return query.getResultList().stream().findAny().orElse(null);
    }

    public List<Integer> getAllTargetsIds() {
        return getAllTargets().stream().mapToInt(Target::getId).boxed().toList();
    }

    @Transactional
    public void save(Target target) {
        targetsRepository.save(target);
    }
}
