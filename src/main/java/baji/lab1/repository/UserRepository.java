package baji.lab1.repository;

import baji.lab1.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);  // поиск по логину (нужен для входа)
    Optional<User> findByEmail(String email);        // поиск по email
    List<User> findAll();                             // все пользователи

    // НОВЫЕ МЕТОДЫ ДЛЯ ФТ-49:
    List<User> findByUsernameContainingIgnoreCase(String username);  // поиск по части имени
    List<User> findByBlocked(boolean blocked);                        // фильтр по статусу блокировки
}