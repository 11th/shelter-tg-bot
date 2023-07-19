package com.skypro.telegram_team.repository;

import com.skypro.telegram_team.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Collection<User> findByVolunteerTrue();

    List<User> findByState(User.OwnerStateEnum state);

    Collection<User> findByTelegramId(Long telegramId);
}

