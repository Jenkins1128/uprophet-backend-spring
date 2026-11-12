package com.uprophet.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "login")
public class Login {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 128)
    private String password;

    @Column(name = "user_name", nullable = false, length = 20, unique = true)
    private String userName;

    @Column(name = "users_id", nullable = false)
    private Long usersId;

    @Column(name = "user_registered", nullable = false)
    private Long userRegistered;

    public Long getId() {
        return id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Long getUsersId() {
        return usersId;
    }

    public void setUsersId(Long usersId) {
        this.usersId = usersId;
    }

    public Long getUserRegistered() {
        return userRegistered;
    }

    public void setUserRegistered(Long userRegistered) {
        this.userRegistered = userRegistered;
    }
}
