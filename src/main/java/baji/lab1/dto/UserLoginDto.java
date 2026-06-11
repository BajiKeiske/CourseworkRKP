package baji.lab1.dto;


import jakarta.validation.constraints.NotBlank;

public class UserLoginDto {
    @NotBlank(message = "Логин обязателен")
    private String username;

    @NotBlank(message = "Пароль обязателен")
    private String password;

    public String getUsername() { return username; }
    public void setUsername(String email) { this.username = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}