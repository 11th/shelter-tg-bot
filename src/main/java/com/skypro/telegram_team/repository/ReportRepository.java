package com.skypro.telegram_team.repository;

import com.skypro.telegram_team.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByAnimalId(long id);

    Collection<Report> findByUserId(Long userId);

    Collection<Report> findByUserIdAndDate(Long userId, LocalDateTime dateTime);
}
