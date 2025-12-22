package baji.lab1.service;

import baji.lab1.entity.User;
import baji.lab1.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("ПОИСК ПОЛЬЗОВАТЕЛЯ: " + username);

        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isEmpty()) {
            logger.error("ПОЛЬЗОВАТЕЛЬ НЕ НАЙДЕН: " + username);
            throw new UsernameNotFoundException("User not found: " + username);
        }

        User user = userOpt.get();
        logger.info("ПОЛЬЗОВАТЕЛЬ НАЙДЕН: " + user.getUsername() + ", роль: " + user.getRole());
        logger.info("Email: " + user.getEmail());
        logger.info("Пароль в БД: " + user.getPassword());

        logger.info("Authorities: " + user.getAuthorities());

        return user;
    }
}