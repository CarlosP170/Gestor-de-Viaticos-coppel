package com.example.gestorviaticoscoppel.dao;

import android.os.AsyncTask;
import com.example.gestorviaticoscoppel.database.DatabaseConnection;
import com.example.gestorviaticoscoppel.models.Notificacion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class NotificacionDAO {

    public interface NotificacionCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    public interface NotificacionListCallback {
        void onSuccess(List<Notificacion> notificaciones);
        void onError(String error);
    }

    public interface NotificacionCountCallback {
        void onSuccess(boolean tieneNoLeidas);
        void onError(String error);
    }

    public static void crearNotificacion(Notificacion notificacion, NotificacionCallback callback) {
        new AsyncTask<Void, Void, String>() {
            private String error;

            @Override
            protected String doInBackground(Void... voids) {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    if (conn == null) {
                        error = "Error de conexión";
                        return null;
                    }

                    String query = "INSERT INTO Notificaciones (ID_Usuario, Tipo, Titulo, Mensaje, Datos_Relacionados) VALUES (?, ?, ?, ?, ?)";
                    PreparedStatement stmt = conn.prepareStatement(query);
                    stmt.setString(1, notificacion.getIdUsuario());
                    stmt.setString(2, notificacion.getTipo());
                    stmt.setString(3, notificacion.getTitulo());
                    stmt.setString(4, notificacion.getMensaje());
                    stmt.setString(5, notificacion.getDatosRelacionados());

                    int result = stmt.executeUpdate();
                    return result > 0 ? "Notificación creada" : null;

                } catch (Exception e) {
                    error = e.getMessage();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    callback.onSuccess(result);
                } else {
                    callback.onError(error != null ? error : "Error al crear notificación");
                }
            }
        }.execute();
    }

    public static void obtenerNotificacionesPorUsuario(String idUsuario, NotificacionListCallback callback) {
        new AsyncTask<Void, Void, List<Notificacion>>() {
            private String error;

            @Override
            protected List<Notificacion> doInBackground(Void... voids) {
                List<Notificacion> notificaciones = new ArrayList<>();
                try (Connection conn = DatabaseConnection.getConnection()) {
                    if (conn == null) {
                        error = "Error de conexión";
                        return null;
                    }

                    String query = "SELECT * FROM Notificaciones WHERE ID_Usuario = ? AND DATEDIFF(day, Fecha_Creacion, GETDATE()) <= 30 ORDER BY Fecha_Creacion DESC";
                    PreparedStatement stmt = conn.prepareStatement(query);
                    stmt.setString(1, idUsuario);
                    ResultSet rs = stmt.executeQuery();

                    while (rs.next()) {
                        Notificacion notificacion = mapearNotificacion(rs);
                        notificaciones.add(notificacion);
                    }
                    return notificaciones;

                } catch (Exception e) {
                    error = e.getMessage();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(List<Notificacion> notificaciones) {
                if (notificaciones != null) {
                    callback.onSuccess(notificaciones);
                } else {
                    callback.onError(error != null ? error : "Error al obtener notificaciones");
                }
            }
        }.execute();
    }

    public static void verificarNotificacionesNoLeidas(String idUsuario, NotificacionCountCallback callback) {
        new AsyncTask<Void, Void, Boolean>() {
            private String error;

            @Override
            protected Boolean doInBackground(Void... voids) {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    if (conn == null) {
                        error = "Error de conexión";
                        return false;
                    }

                    String query = "SELECT COUNT(*) FROM Notificaciones WHERE ID_Usuario = ? AND Leida = 0 AND DATEDIFF(day, Fecha_Creacion, GETDATE()) <= 30";
                    PreparedStatement stmt = conn.prepareStatement(query);
                    stmt.setString(1, idUsuario);
                    ResultSet rs = stmt.executeQuery();

                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                    return false;

                } catch (Exception e) {
                    error = e.getMessage();
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean tieneNoLeidas) {
                if (error == null) {
                    callback.onSuccess(tieneNoLeidas);
                } else {
                    callback.onError(error);
                }
            }
        }.execute();
    }

    public static void verificarNotificacionExistente(String idUsuario, String tipo, NotificacionCountCallback callback) {
        new AsyncTask<Void, Void, Boolean>() {
            private String error;

            @Override
            protected Boolean doInBackground(Void... voids) {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    if (conn == null) {
                        error = "Error de conexión";
                        return false;
                    }

                    String query = "SELECT COUNT(*) FROM Notificaciones WHERE ID_Usuario = ? AND Tipo = ? AND Leida = 0 AND DATEDIFF(day, Fecha_Creacion, GETDATE()) <= 30";
                    PreparedStatement stmt = conn.prepareStatement(query);
                    stmt.setString(1, idUsuario);
                    stmt.setString(2, tipo);
                    ResultSet rs = stmt.executeQuery();

                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                    return false;

                } catch (Exception e) {
                    error = e.getMessage();
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean existe) {
                if (error == null) {
                    callback.onSuccess(existe);
                } else {
                    callback.onError(error);
                }
            }
        }.execute();
    }

    public static void marcarComoLeida(int idNotificacion, NotificacionCallback callback) {
        new AsyncTask<Void, Void, String>() {
            private String error;

            @Override
            protected String doInBackground(Void... voids) {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    if (conn == null) {
                        error = "Error de conexión";
                        return null;
                    }

                    String query = "UPDATE Notificaciones SET Leida = 1 WHERE ID_Notificacion = ?";
                    PreparedStatement stmt = conn.prepareStatement(query);
                    stmt.setInt(1, idNotificacion);

                    int result = stmt.executeUpdate();
                    return result > 0 ? "Notificación marcada como leída" : null;

                } catch (Exception e) {
                    error = e.getMessage();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    callback.onSuccess(result);
                } else {
                    callback.onError(error != null ? error : "Error al marcar notificación");
                }
            }
        }.execute();
    }

    public static void marcarTodasComoLeidas(String idUsuario, NotificacionCallback callback) {
        new AsyncTask<Void, Void, String>() {
            private String error;

            @Override
            protected String doInBackground(Void... voids) {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    if (conn == null) {
                        error = "Error de conexión";
                        return null;
                    }

                    String query = "UPDATE Notificaciones SET Leida = 1 WHERE ID_Usuario = ? AND Leida = 0";
                    PreparedStatement stmt = conn.prepareStatement(query);
                    stmt.setString(1, idUsuario);

                    int result = stmt.executeUpdate();
                    return "Notificaciones actualizadas";

                } catch (Exception e) {
                    error = e.getMessage();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    callback.onSuccess(result);
                } else {
                    callback.onError(error != null ? error : "Error al marcar notificaciones");
                }
            }
        }.execute();
    }

    public static void limpiarNotificacionesAntiguas(NotificacionCallback callback) {
        new AsyncTask<Void, Void, String>() {
            private String error;

            @Override
            protected String doInBackground(Void... voids) {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    if (conn == null) {
                        error = "Error de conexión";
                        return null;
                    }

                    String query = "DELETE FROM Notificaciones WHERE DATEDIFF(day, Fecha_Creacion, GETDATE()) > 30";
                    PreparedStatement stmt = conn.prepareStatement(query);

                    int result = stmt.executeUpdate();
                    return "Notificaciones antiguas eliminadas: " + result;

                } catch (Exception e) {
                    error = e.getMessage();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    callback.onSuccess(result);
                } else {
                    callback.onError(error != null ? error : "Error al limpiar notificaciones");
                }
            }
        }.execute();
    }

    public static void verificarSolicitudesPendientes(NotificacionCountCallback callback) {
        new AsyncTask<Void, Void, Boolean>() {
            private String error;

            @Override
            protected Boolean doInBackground(Void... voids) {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    if (conn == null) {
                        error = "Error de conexión";
                        return false;
                    }

                    String query = "SELECT COUNT(*) FROM Solicitudes_Viaticos WHERE Estado = 'Pendiente'";
                    PreparedStatement stmt = conn.prepareStatement(query);
                    ResultSet rs = stmt.executeQuery();

                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                    return false;

                } catch (Exception e) {
                    error = e.getMessage();
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean tienePendientes) {
                if (error == null) {
                    callback.onSuccess(tienePendientes);
                } else {
                    callback.onError(error);
                }
            }
        }.execute();
    }

    public static void verificarViajesPendientes(NotificacionCountCallback callback) {
        new AsyncTask<Void, Void, Boolean>() {
            private String error;

            @Override
            protected Boolean doInBackground(Void... voids) {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    if (conn == null) {
                        error = "Error de conexión";
                        return false;
                    }

                    String query = "SELECT COUNT(*) FROM Viajes WHERE Estado = 'Enviado_A_Revision'";
                    PreparedStatement stmt = conn.prepareStatement(query);
                    ResultSet rs = stmt.executeQuery();

                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                    return false;

                } catch (Exception e) {
                    error = e.getMessage();
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean tienePendientes) {
                if (error == null) {
                    callback.onSuccess(tienePendientes);
                } else {
                    callback.onError(error);
                }
            }
        }.execute();
    }

    private static Notificacion mapearNotificacion(ResultSet rs) throws Exception {
        Notificacion notificacion = new Notificacion();
        notificacion.setIdNotificacion(rs.getInt("ID_Notificacion"));
        notificacion.setIdUsuario(rs.getString("ID_Usuario"));
        notificacion.setTipo(rs.getString("Tipo"));
        notificacion.setTitulo(rs.getString("Titulo"));
        notificacion.setMensaje(rs.getString("Mensaje"));
        notificacion.setFechaCreacion(rs.getString("Fecha_Creacion"));
        notificacion.setLeida(rs.getBoolean("Leida"));
        notificacion.setDatosRelacionados(rs.getString("Datos_Relacionados"));
        return notificacion;
    }
}