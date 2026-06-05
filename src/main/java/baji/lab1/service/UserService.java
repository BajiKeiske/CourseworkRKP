package baji.lab1.service;

import baji.lab1.entity.User;
import baji.lab1.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    // Найти одного пользователя по ID
    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    // Сохранить пользователя в БД
    public User save(User user) {
        return userRepository.save(user);
    }

    // Удалить пользователя по ID
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    // Все пользователи из БД
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Поиск с фильтрами по имени и статусу блокировки
    public List<User> searchUsers(String username, Boolean blocked) {
        if (username != null && !username.isEmpty()) {
            return userRepository.findByUsernameContainingIgnoreCase(username);
        }
        if (blocked != null) {
            return userRepository.findByBlocked(blocked);
        }
        return userRepository.findAll();
    }

    // Заблокировать пользователя
    public void blockUser(Long id) {
        User user = userRepository.findById(id).orElseThrow();
        user.setBlocked(true);
        userRepository.save(user);
    }

    // Разблокировать пользователя
    public void unblockUser(Long id) {
        User user = userRepository.findById(id).orElseThrow();
        user.setBlocked(false);
        userRepository.save(user);
    }

    // Spring Security вызывает этот метод при входе пользователя
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("Поиск пользователя: " + username);

        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isEmpty()) {
            logger.error("Пользователь не найден: " + username);
            throw new UsernameNotFoundException("Пользователь не найден");
        }

        User user = userOpt.get();

        // Если пользователь заблокирован - запрещаем вход
        if (user.isBlocked()) {
            logger.error("Пользователь заблокирован: " + username);
            throw new LockedException("BLOCKED");
        }

        return user;
    }
}