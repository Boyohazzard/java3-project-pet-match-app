package edu.java3projectpetmatchapp.repository;

import edu.java3projectpetmatchapp.entity.Application;
import edu.java3projectpetmatchapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByUser(User user);
}
