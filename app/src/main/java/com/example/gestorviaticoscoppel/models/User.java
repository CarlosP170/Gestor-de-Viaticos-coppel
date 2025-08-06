package com.example.gestorviaticoscoppel.models;

public class User {
    private String idUsuario;
    private String email;
    private String password;
    private String rol;
    private String name;
    private boolean isLoggedIn;

    public User() {
    }

    public User(String email, String password) {
        this.email = email;
        this.password = password;
        this.isLoggedIn = false;
    }

    public User(String idUsuario, String email, String rol, String name) {
        this.idUsuario = idUsuario;
        this.email = email;
        this.rol = rol;
        this.name = name;
        this.isLoggedIn = true;
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        isLoggedIn = loggedIn;
    }
}