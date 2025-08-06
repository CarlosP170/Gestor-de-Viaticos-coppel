package com.example.gestorviaticoscoppel.dao;

import android.os.AsyncTask;
import com.example.gestorviaticoscoppel.database.DatabaseConnection;
import com.example.gestorviaticoscoppel.models.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public interface LoginCallback {
        void onSuccess(User user);
        void onError(String error);
    }

    public interface UsuarioValidacionCallback {
        void onSuccess(User user);
        void onError(String error);
    }

    public interface UsuarioListCallback {
        void onSuccess(List<User> usuarios);
        void onError(String error);
    }

    public static void login(String email, String password, LoginCallback callback) {
        new AsyncTask<Void, Void, User>() {
            private String error;

            @Override
            protected User doInBackground(Void... voids) {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    if (conn == null) {
                        error = "Error de conexión";
                        return null;
                    }

                    String query = "SELECT ID_Usuario, Email, Rol, Nombre FROM Usuarios WHERE Email = ? AND Password_Hash = ? AND Activo = 1";
                    PreparedStatement stmt = conn.prepareStatement(query);
                    stmt.setString(1, email);
                    stmt.setString(2, password);

                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        User user = new User();
                        user.setIdUsuario(rs.getString("ID_Usuario"));
                        user.setEmail(rs.getString("Email"));
                        user.setRol(rs.getString("Rol"));
                        user.setName(rs.getString("Nombre"));
                        user.setLoggedIn(true);
                        return user;
                    }
                } catch (Exception e) {
                    error = e.getMessage();
                }
                return null;
            }

            @Override
            protected void onPostExecute(User user) {
                if (user != null) {
                    callback.onSuccess(user);
                } else {
                    callback.onError(error != null ? error : "Credenciales incorrectas");
                }
            }
        }.execute();
    }

    public static void validarUsuarioExistente(String idUsuario, UsuarioValidacionCallback callback) {
        new AsyncTask<Void, Void, User>() {
            private String error;

            @Override
            protected User doInBackground(Void... voids) {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    if (conn == null) {
                        error = "Error de conexión";
                        return null;
                    }

                    String query = "SELECT ID_Usuario, Email, Rol, Nombre FROM Usuarios WHERE ID_Usuario = ? AND Activo = 1";
                    PreparedStatement stmt = conn.prepareStatement(query);
                    stmt.setString(1, idUsuario);

                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        User user = new User();
                        user.setIdUsuario(rs.getString("ID_Usuario"));
                        user.setEmail(rs.getString("Email"));
                        user.setRol(rs.getString("Rol"));
                        user.setName(rs.getString("Nombre"));
                        return user;
                    }
                } catch (Exception e) {
                    error = e.getMessage();
                }
                return null;
            }

            @Override
            protected void onPostExecute(User user) {
                if (user != null) {
                    callback.onSuccess(user);
                } else {
                    callback.onError(error != null ? error : "Usuario no encontrado");
                }
            }
        }.execute();
    }

    public static void obtenerUsuariosPorRol(String rol, UsuarioListCallback callback) {
        new AsyncTask<Void, Void, List<User>>() {
            private String error;

            @Override
            protected List<User> doInBackground(Void... voids) {
                List<User> usuarios = new ArrayList<>();
                try (Connection conn = DatabaseConnection.getConnection()) {
                    if (conn == null) {
                        error = "Error de conexión";
                        return null;
                    }

                    String query = "SELECT ID_Usuario, Email, Rol, Nombre FROM Usuarios WHERE Rol = ? AND Activo = 1";
                    PreparedStatement stmt = conn.prepareStatement(query);
                    stmt.setString(1, rol);

                    ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        User user = new User();
                        user.setIdUsuario(rs.getString("ID_Usuario"));
                        user.setEmail(rs.getString("Email"));
                        user.setRol(rs.getString("Rol"));
                        user.setName(rs.getString("Nombre"));
                        usuarios.add(user);
                    }
                    return usuarios;

                } catch (Exception e) {
                    error = e.getMessage();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(List<User> usuarios) {
                if (usuarios != null) {
                    callback.onSuccess(usuarios);
                } else {
                    callback.onError(error != null ? error : "Error al obtener usuarios por rol");
                }
            }
        }.execute();
    }
}