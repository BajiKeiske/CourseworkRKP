package baji.lab1.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import javax.sql.DataSource;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

/**
 * Конфигурационный класс для настройки безопасности приложения.
 * <p>
 * Этот класс определяет правила авторизации, аутентификации,
 * настройки формы входа, механизм "запомнить меня" и logout.
 * Использует Spring Security для защиты endpoints и управления сессиями.
 * </p>
 *
 * <p>Основные возможности:</p>
 * <ul>
 *   <li>Отключение CSRF защиты для упрощения API запросов</li>
 *   <li>Разграничение доступа на основе ролей (ADMIN/USER)</li>
 *   <li>Кастомная форма логина с обработкой успешного входа</li>
 *   <li>Постоянное хранилище токенов для "запомнить меня" в базе данных</li>
 *   <li>Шифрование паролей с помощью BCrypt</li>
 * </ul>
 *
 * @author Baji
 * @version 1.0
 * @since 1.0
 * @see org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
 * @see org.springframework.security.web.SecurityFilterChain
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Источник данных для работы с базой данных.
     * Используется для хранения токенов "запомнить меня".
     * Автоматически внедряется Spring контейнером.
     */
    @Autowired
    private DataSource dataSource;

    /**
     * Создает и настраивает цепочку фильтров безопасности.
     * <p>
     * Метод определяет:
     * </p>
     * <ul>
     *   <li>Отключение CSRF для упрощения работы с API</li>
     *   <li>Правила доступа к различным endpoints</li>
     *   <li>Настройку формы логина и обработку успешной аутентификации</li>
     *   <li>Механизм "запомнить меня" с хранением токенов в БД</li>
     *   <li>Настройку выхода из системы (logout)</li>
     * </ul>
     *
     * <p><strong>Правила доступа:</strong></p>
     * <ul>
     *   <li>Публичные endpoints: главная страница, логин, регистрация, верификация,
     *   статические ресурсы, API для продуктов, категорий и брендов,
     *   оформление заказа, каталог и детали продуктов - доступны всем</li>
     *   <li>/admin/** - только для пользователей с ролью ADMIN</li>
     *   <li>/user/** - только для пользователей с ролью USER</li>
     *   <li>Все остальные запросы - только для аутентифицированных пользователей</li>
     * </ul>
     *
     * <p><strong>Логика перенаправления после успешного входа:</strong></p>
     * <ul>
     *   <li>Администраторы перенаправляются на /admin/products</li>
     *   <li>Обычные пользователи перенаправляются на /user/products/catalog</li>
     * </ul>
     *
     * @param http объект HttpSecurity для настройки безопасности
     * @return настроенный SecurityFilterChain
     * @throws Exception если возникает ошибка при настройке безопасности
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers(
                                "/",
                                "/login",
                                "/register",
                                "/verify",
                                "/error",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/webjars/**",
                                "/api/products",
                                "/api/categories",
                                "/api/brands",
                                "/api/**",
                                "/order/checkout",
                                "/user/products/catalog",
                                "/user/products/details/**"
                        ).permitAll()

                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/user/**").hasRole("USER")
                        .anyRequest().authenticated()
                )

                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler((request, response, authentication) -> {
                            boolean isAdmin = authentication.getAuthorities().stream()
                                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

                            if (isAdmin) {
                                response.sendRedirect("/admin/products");
                            } else {
                                response.sendRedirect("/user/products/catalog");
                            }
                        })
                        .failureUrl("/login?error=true")
                        .permitAll()
                )

                .rememberMe(remember -> remember
                        .rememberMeParameter("remember-me")
                        .tokenValiditySeconds(60 * 60 * 24 * 365)
                        .tokenRepository(persistentTokenRepository())

                )

                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                );

        return http.build();
    }

    /**
     * Создает и настраивает кодировщик паролей.
     * <p>
     * Используется алгоритм BCrypt для хеширования паролей,
     * что обеспечивает надежное хранение пользовательских учетных данных.
     * BCrypt автоматически генерирует соль и обеспечивает защиту от
     * атак по словарю и радужным таблицам.
     * </p>
     *
     * @return экземпляр PasswordEncoder с реализацией BCrypt
     * @see BCryptPasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Создает репозиторий для хранения токенов "запомнить меня".
     * <p>
     * Использует JDBC для сохранения токенов в базе данных, что позволяет
     * сохранять сессии между перезапусками приложения и обеспечивает
     * возможность отслеживания активных сессий "запомнить меня".
     * </p>
     * <p>
     * Требует наличия в базе данных таблицы persistent_logins со следующей структурой:
     * </p>
     * <pre>
     * CREATE TABLE persistent_logins (
     *     username VARCHAR(64) NOT NULL,
     *     series VARCHAR(64) PRIMARY KEY,
     *     token VARCHAR(64) NOT NULL,
     *     last_used TIMESTAMP NOT NULL
     * );
     * </pre>
     *
     * @return настроенный PersistentTokenRepository для работы с БД
     * @see JdbcTokenRepositoryImpl
     * @see PersistentTokenRepository
     */
    @Bean
    public PersistentTokenRepository persistentTokenRepository() {
        JdbcTokenRepositoryImpl repository =
                new JdbcTokenRepositoryImpl();

        repository.setDataSource(dataSource);

        return repository;
    }
}