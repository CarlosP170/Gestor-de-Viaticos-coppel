package com.example.gestorviaticoscoppel.dao;

import android.os.AsyncTask;
import com.example.gestorviaticoscoppel.database.DatabaseConnection;
import com.example.gestorviaticoscoppel.models.Viaje;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ViajeDAO {

    public interface ViajeCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    public interface ViajeListCallback {
        void onSuccess(List<Viaje> viajes);
        void onError(String error);
    }

    public interface ViajeSingleCallback {
        void onSuccess(Viaje viaje);
        void onError(String error);
    }

    public static void crearViajeDesdeAutorizacion(int idSolicitud, ViajeSingleCallback callback) {
        new AsyncTask<Void, Void, Viaje>() {
            private String error;

            @Override
            protected Viaje doInBackground(Void... voids) {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    if (conn == null) {
                        error = "Error de conexión";
                        return null;
                    }

                    String selectQuery = "SELECT * FROM Solicitudes_Viaticos WHERE ID_Solicitud = ? AND Estado = 'Autorizada'";
                    PreparedStatement selectStmt = conn.prepareStatement(selectQuery);
                    selectStmt.setInt(1, idSolicitud);
                    ResultSet rs = selectStmt.executeQuery();

                    if (!rs.next()) {
                        error = "Solicitud no encontrada o no autorizada";
                        return null;
                    }

                    String insertQuery = "INSERT INTO Viajes (ID_Solicitud, ID_Usuario, Folio_Viaje, " +
                            "Monto_Hotel_Autorizado, Monto_Comida_Autorizado, " +
                            "Monto_Transporte_Autorizado, Monto_Gasolina_Autorizado) " +
                            "OUTPUT INSERTED.ID_Viaje VALUES (?, ?, ?, ?, ?, ?, ?)";

                    PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
                    insertStmt.setInt(1, rs.getInt("ID_Solicitud"));
                    insertStmt.setString(2, rs.getString("ID_Beneficiario"));
                    insertStmt.setString(3, rs.getString("Folio_Viaje"));
                    insertStmt.setDouble(4, rs.getDouble("Monto_Hotel"));
                    insertStmt.setDouble(5, rs.getDouble("Monto_Comida"));
                    insertStmt.setDouble(6, rs.getDouble("Monto_Transporte"));
                    insertStmt.setDouble(7, rs.getDouble("Monto_Gasolina"));

                    ResultSet insertRs = insertStmt.executeQuery();
                    if (insertRs.next()) {
                        int idViaje = insertRs.getInt(1);
                        return obtenerViajePorId(conn, idViaje);
                    }
                    return null;

                } catch (Exception e) {
                    error = e.getMessage();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Viaje viaje) {
                if (viaje != null) {
                    callback.onSuccess(viaje);
                } else {
                    callback.onError(error != null ? error : "Error al crear viaje");
                }
            }
        }.execute();
    }

    public static void obtenerViajeActivo(String idUsuario, ViajeSingleCallback callback) {
        new AsyncTask<Void, Void, Viaje>() {
            private String error;

            @Override
            protected Viaje doInBackground(Void... voids) {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    if (conn == null) {
                        error = "Error de conexión";
                        return null;
                    }

                    String query = "SELECT * FROM Viajes WHERE ID_Usuario = ? AND Estado IN ('En_Curso', 'Enviado_A_Revision', 'Finalizado', 'Rechazado') ORDER BY Fecha_Inicio DESC";
                    PreparedStatement stmt = conn.prepareStatement(query);
                    stmt.setString(1, idUsuario);
                    ResultSet rs = stmt.executeQuery();

                    if (rs.next()) {
                        return mapearViaje(rs);
                    }
                    return null;

                } catch (Exception e) {
                    error = e.getMessage();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Viaje viaje) {
                if (viaje != null) {
                    callback.onSuccess(viaje);
                } else {
                    callback.onError(error != null ? error : "No hay viaje activo");
                }
            }
        }.execute();
    }

    public static void obtenerViajesFinalizados(String idUsuario, ViajeListCallback callback) {
        new AsyncTask<Void, Void, List<Viaje>>() {
            private String error;

            @Override
            protected List<Viaje> doInBackground(Void... voids) {
                List<Viaje> viajes = new ArrayList<>();
                try (Connection conn = DatabaseConnection.getConnection()) {
                    if (conn == null) {
                        error = "Error de conexión";
                        return null;
                    }

                    String query = "SELECT * FROM Viajes WHERE ID_Usuario = ? AND Estado IN ('Finalizado', 'Rechazado') " +
                            "ORDER BY Fecha_Finalizacion DESC";
                    PreparedStatement stmt = conn.prepareStatement(query);
                    stmt.setString(1, idUsuario);
                    ResultSet rs = stmt.executeQuery();

                    while (rs.next()) {
                        viajes.add(mapearViaje(rs));
                    }
                    return viajes;

                } catch (Exception e) {
                    error = e.getMessage();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(List<Viaje> viajes) {
                if (viajes != null) {
                    callback.onSuccess(viajes);
                } else {
                    callback.onError(error != null ? error : "Error al obtener historial");
                }
            }
        }.execute();
    }

    public static void obtenerViajesHistorialRH(ViajeListCallback callback) {
        new AsyncTask<Void, Void, List<Viaje>>() {
            private String error;

            @Override
            protected List<Viaje> doInBackground(Void... voids) {
                List<Viaje> viajes = new ArrayList<>();
                try (Connection conn = DatabaseConnection.getConnection()) {
                    if (conn == null) {
                        error = "Error de conexión";
                        return null;
                    }

                    String query = "SELECT * FROM Viajes WHERE Estado IN ('Finalizado', 'Rechazado') " +
                            "ORDER BY Fecha_Finalizacion DESC";
                    PreparedStatement stmt = conn.prepareStatement(query);
                    ResultSet rs = stmt.executeQuery();

                    while (rs.next()) {
                        viajes.add(mapearViaje(rs));
                    }
                    return viajes;

                } catch (Exception e) {
                    error = e.getMessage();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(List<Viaje> viajes) {
                if (viajes != null) {
                    callback.onSuccess(viajes);
                } else {
                    callback.onError(error != null ? error : "Error al obtener historial de viajes");
                }
            }
        }.execute();
    }

    public static void actualizarMontoGastado(int idViaje, String categoria, double monto, ViajeCallback callback) {
        new AsyncTask<Void, Void, String>() {
            private String error;

            @Override
            protected String doInBackground(Void... voids) {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    if (conn == null) {
                        error = "Error de conexión";
                        return null;
                    }

                    String columna = "Monto_" + categoria + "_Gastado";
                    String query = "UPDATE Viajes SET " + columna + " = " + columna + " + ? WHERE ID_Viaje = ?";

                    PreparedStatement stmt = conn.prepareStatement(query);
                    stmt.setDouble(1, monto);
                    stmt.setInt(2, idViaje);

                    int result = stmt.executeUpdate();
                    return result > 0 ? "Monto actualizado correctamente" : null;

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
                    callback.onError(error != null ? error : "Error al actualizar monto");
                }
            }
        }.execute();
    }

    public static void actualizarFolioTicket(int idViaje, String folioTicket, ViajeCallback callback) {
        new AsyncTask<Void, Void, String>() {
            private String error;

            @Override
            protected String doInBackground(Void... voids) {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    if (conn == null) {
                        error = "Error de conexión";
                        return null;
                    }

                    String query = "UPDATE Viajes SET Folio_Ticket_Sobrante = ? WHERE ID_Viaje = ?";

                    PreparedStatement stmt = conn.prepareStatement(query);

                    if (folioTicket != null && !folioTicket.trim().isEmpty()) {
                        stmt.setString(1, folioTicket.trim());
                    } else {
                        stmt.setString(1, null);
                    }

                    stmt.setInt(2, idViaje);

                    int result = stmt.executeUpdate();
                    return result > 0 ? "Folio actualizado correctamente" : null;

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
                    callback.onError(error != null ? error : "Error al actualizar folio");
                }
            }
        }.execute();
    }

    public static void enviarARevision(int idViaje, String folioTicket, ViajeCallback callback) {
        new AsyncTask<Void, Void, String>() {
            private String error;

            @Override
            protected String doInBackground(Void... voids) {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    if (conn == null) {
                        error = "Error de conexión";
                        return null;
                    }

                    String query = "UPDATE Viajes SET Estado = 'Enviado_A_Revision', " +
                            "Fecha_Envio_Revision = GETDATE(), Folio_Ticket_Sobrante = ? WHERE ID_Viaje = ?";

                    PreparedStatement stmt = conn.prepareStatement(query);

                    if (folioTicket != null && !folioTicket.trim().isEmpty()) {
                        stmt.setString(1, folioTicket.trim());
                    } else {
                        stmt.setString(1, null);
                    }

                    stmt.setInt(2, idViaje);

                    int result = stmt.executeUpdate();
                    return result > 0 ? "Viaje enviado a revisión" : null;

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
                    callback.onError(error != null ? error : "Error al enviar a revisión");
                }
            }
        }.execute();
    }

    public static void obtenerViajesEnRevision(ViajeListCallback callback) {
        new AsyncTask<Void, Void, List<Viaje>>() {
            private String error;

            @Override
            protected List<Viaje> doInBackground(Void... voids) {
                List<Viaje> viajes = new ArrayList<>();
                try (Connection conn = DatabaseConnection.getConnection()) {
                    if (conn == null) {
                        error = "Error de conexión";
                        return null;
                    }

                    String query = "SELECT * FROM Viajes WHERE Estado = 'Enviado_A_Revision' ORDER BY Fecha_Envio_Revision ASC";
                    PreparedStatement stmt = conn.prepareStatement(query);
                    ResultSet rs = stmt.executeQuery();

                    while (rs.next()) {
                        viajes.add(mapearViaje(rs));
                    }
                    return viajes;

                } catch (Exception e) {
                    error = e.getMessage();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(List<Viaje> viajes) {
                if (viajes != null) {
                    callback.onSuccess(viajes);
                } else {
                    callback.onError(error != null ? error : "Error al obtener viajes en revisión");
                }
            }
        }.execute();
    }

    public static void finalizarViaje(int idViaje, String idRevisor, ViajeCallback callback) {
        new AsyncTask<Void, Void, String>() {
            private String error;

            @Override
            protected String doInBackground(Void... voids) {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    if (conn == null) {
                        error = "Error de conexión";
                        return null;
                    }

                    String query = "UPDATE Viajes SET Estado = 'Finalizado', " +
                            "ID_Revisor_Final = ?, Fecha_Finalizacion = GETDATE() WHERE ID_Viaje = ?";

                    PreparedStatement stmt = conn.prepareStatement(query);
                    stmt.setString(1, idRevisor);
                    stmt.setInt(2, idViaje);

                    int result = stmt.executeUpdate();
                    return result > 0 ? "Viaje finalizado correctamente" : null;

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
                    callback.onError(error != null ? error : "Error al finalizar viaje");
                }
            }
        }.execute();
    }

    public static void rechazarViaje(int idViaje, String idRevisor, ViajeCallback callback) {
        new AsyncTask<Void, Void, String>() {
            private String error;

            @Override
            protected String doInBackground(Void... voids) {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    if (conn == null) {
                        error = "Error de conexión";
                        return null;
                    }

                    String query = "UPDATE Viajes SET Estado = 'Rechazado', " +
                            "ID_Revisor_Final = ?, Fecha_Finalizacion = GETDATE() WHERE ID_Viaje = ?";

                    PreparedStatement stmt = conn.prepareStatement(query);
                    stmt.setString(1, idRevisor);
                    stmt.setInt(2, idViaje);

                    int result = stmt.executeUpdate();
                    return result > 0 ? "Viaje rechazado" : null;

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
                    callback.onError(error != null ? error : "Error al rechazar viaje");
                }
            }
        }.execute();
    }

    public static void rechazarViajeConMotivo(int idViaje, String idRevisor, String motivo, ViajeCallback callback) {
        new AsyncTask<Void, Void, String>() {
            private String error;

            @Override
            protected String doInBackground(Void... voids) {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    if (conn == null) {
                        error = "Error de conexión";
                        return null;
                    }

                    String query = "UPDATE Viajes SET Estado = 'Rechazado', " +
                            "ID_Revisor_Final = ?, Fecha_Finalizacion = GETDATE(), Motivo_Rechazo = ? WHERE ID_Viaje = ?";

                    PreparedStatement stmt = conn.prepareStatement(query);
                    stmt.setString(1, idRevisor);
                    stmt.setString(2, motivo);
                    stmt.setInt(3, idViaje);

                    int result = stmt.executeUpdate();
                    return result > 0 ? "Viaje rechazado con motivo" : null;

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
                    callback.onError(error != null ? error : "Error al rechazar viaje");
                }
            }
        }.execute();
    }

    private static Viaje obtenerViajePorId(Connection conn, int idViaje) throws Exception {
        String query = "SELECT * FROM Viajes WHERE ID_Viaje = ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, idViaje);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            return mapearViaje(rs);
        }
        return null;
    }

    private static Viaje mapearViaje(ResultSet rs) throws Exception {
        Viaje viaje = new Viaje();
        viaje.setIdViaje(rs.getInt("ID_Viaje"));
        viaje.setIdSolicitud(rs.getInt("ID_Solicitud"));
        viaje.setIdUsuario(rs.getString("ID_Usuario"));
        viaje.setFolioViaje(rs.getString("Folio_Viaje"));

        viaje.setMontoHotelAutorizado(rs.getDouble("Monto_Hotel_Autorizado"));
        viaje.setMontoComidaAutorizado(rs.getDouble("Monto_Comida_Autorizado"));
        viaje.setMontoTransporteAutorizado(rs.getDouble("Monto_Transporte_Autorizado"));
        viaje.setMontoGasolinaAutorizado(rs.getDouble("Monto_Gasolina_Autorizado"));

        viaje.setMontoHotelGastado(rs.getDouble("Monto_Hotel_Gastado"));
        viaje.setMontoComidaGastado(rs.getDouble("Monto_Comida_Gastado"));
        viaje.setMontoTransporteGastado(rs.getDouble("Monto_Transporte_Gastado"));
        viaje.setMontoGasolinaGastado(rs.getDouble("Monto_Gasolina_Gastado"));
        viaje.setMontoOtrosGastado(rs.getDouble("Monto_Otros_Gastado"));

        viaje.setEstado(rs.getString("Estado"));
        viaje.setFechaInicio(rs.getString("Fecha_Inicio"));
        viaje.setFechaEnvioRevision(rs.getString("Fecha_Envio_Revision"));
        viaje.setFechaFinalizacion(rs.getString("Fecha_Finalizacion"));
        viaje.setFolioTicketSobrante(rs.getString("Folio_Ticket_Sobrante"));
        viaje.setIdRevisorFinal(rs.getString("ID_Revisor_Final"));
        viaje.setMotivoRechazo(rs.getString("Motivo_Rechazo"));

        return viaje;
    }
}