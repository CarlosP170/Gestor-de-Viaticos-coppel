package com.example.gestorviaticoscoppel.dao;

import android.os.AsyncTask;
import android.util.Log;
import com.example.gestorviaticoscoppel.database.DatabaseConnection;
import com.example.gestorviaticoscoppel.models.Solicitud;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class SolicitudDAO {

    public interface SolicitudCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    public interface SolicitudesListCallback {
        void onSuccess(List<Solicitud> solicitudes);
        void onError(String error);
    }

    public interface SolicitudSingleCallback {
        void onSuccess(Solicitud solicitud);
        void onError(String error);
    }

    public static void crearSolicitud(Solicitud solicitud, SolicitudCallback callback) {
        new AsyncTask<Void, Void, String>() {
            private String error;

            @Override
            protected String doInBackground(Void... voids) {
                Log.d("SOLICITUD_DAO", "=== INICIANDO doInBackground ===");
                try (Connection conn = DatabaseConnection.getConnection()) {
                    Log.d("SOLICITUD_DAO", "Conexion obtenida: " + (conn != null ? "SI" : "NULL"));

                    if (conn == null) {
                        error = "Error de conexión";
                        Log.e("SOLICITUD_DAO", "CONEXION ES NULL");
                        return null;
                    }

                    conn.setAutoCommit(false);
                    Log.d("SOLICITUD_DAO", "AutoCommit deshabilitado");

                    String query = "INSERT INTO Solicitudes_Viaticos (Folio_Viaje, ID_Solicitante, ID_Beneficiario, " +
                            "Monto_Hotel, Monto_Comida, Monto_Transporte, Monto_Gasolina) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?)";

                    Log.d("SOLICITUD_DAO", "Query: " + query);
                    Log.d("SOLICITUD_DAO", "Folio: " + solicitud.getFolioViaje());
                    Log.d("SOLICITUD_DAO", "Solicitante: " + solicitud.getIdSolicitante());
                    Log.d("SOLICITUD_DAO", "Beneficiario: " + solicitud.getIdBeneficiario());
                    Log.d("SOLICITUD_DAO", "Hotel: " + solicitud.getMontoHotel());
                    Log.d("SOLICITUD_DAO", "Comida: " + solicitud.getMontoComida());
                    Log.d("SOLICITUD_DAO", "Transporte: " + solicitud.getMontoTransporte());
                    Log.d("SOLICITUD_DAO", "Gasolina: " + solicitud.getMontoGasolina());

                    PreparedStatement stmt = conn.prepareStatement(query);
                    stmt.setString(1, solicitud.getFolioViaje());
                    stmt.setString(2, solicitud.getIdSolicitante());
                    stmt.setString(3, solicitud.getIdBeneficiario());
                    stmt.setDouble(4, solicitud.getMontoHotel());
                    stmt.setDouble(5, solicitud.getMontoComida());
                    stmt.setDouble(6, solicitud.getMontoTransporte());
                    stmt.setDouble(7, solicitud.getMontoGasolina());

                    Log.d("SOLICITUD_DAO", "ANTES DE EXECUTE UPDATE");
                    int result = stmt.executeUpdate();
                    Log.d("SOLICITUD_DAO", "RESULT: " + result);

                    conn.commit();
                    Log.d("SOLICITUD_DAO", "COMMIT REALIZADO");

                    return result > 0 ? "Solicitud creada exitosamente" : null;

                } catch (Exception e) {
                    Log.e("SOLICITUD_DAO", "EXCEPCION: " + e.getMessage());
                    Log.e("SOLICITUD_DAO", "STACK TRACE: ", e);
                    error = e.getMessage();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String result) {
                Log.d("SOLICITUD_DAO", "onPostExecute - result: " + result);
                Log.d("SOLICITUD_DAO", "onPostExecute - error: " + error);
                if (result != null) {
                    callback.onSuccess(result);
                } else {
                    callback.onError(error != null ? error : "Error al crear solicitud");
                }
            }
        }.execute();
    }

    public static void obtenerSolicitudesPendientes(SolicitudesListCallback callback) {
        new AsyncTask<Void, Void, List<Solicitud>>() {
            private String error;

            @Override
            protected List<Solicitud> doInBackground(Void... voids) {
                Log.d("RH_DAO_DEBUG", "=== OBTENIENDO SOLICITUDES PENDIENTES ===");
                List<Solicitud> solicitudes = new ArrayList<>();
                try (Connection conn = DatabaseConnection.getConnection()) {
                    Log.d("RH_DAO_DEBUG", "Conexion: " + (conn != null ? "SI" : "NULL"));

                    if (conn == null) {
                        Log.e("RH_DAO_DEBUG", "CONEXION ES NULL");
                        error = "Error de conexión";
                        return null;
                    }

                    String query = "SELECT * FROM Solicitudes_Viaticos " +
                            "WHERE Estado = 'Pendiente' " +
                            "ORDER BY Fecha_Solicitud ASC";

                    Log.d("RH_DAO_DEBUG", "Query: " + query);

                    PreparedStatement stmt = conn.prepareStatement(query);
                    Log.d("RH_DAO_DEBUG", "ANTES DE EXECUTE QUERY");
                    ResultSet rs = stmt.executeQuery();
                    Log.d("RH_DAO_DEBUG", "QUERY EJECUTADO");

                    int contador = 0;
                    while (rs.next()) {
                        contador++;
                        Solicitud solicitud = new Solicitud();
                        solicitud.setIdSolicitud(rs.getInt("ID_Solicitud"));
                        solicitud.setFolioViaje(rs.getString("Folio_Viaje"));
                        solicitud.setIdSolicitante(rs.getString("ID_Solicitante"));
                        solicitud.setIdBeneficiario(rs.getString("ID_Beneficiario"));
                        solicitud.setMontoHotel(rs.getDouble("Monto_Hotel"));
                        solicitud.setMontoComida(rs.getDouble("Monto_Comida"));
                        solicitud.setMontoTransporte(rs.getDouble("Monto_Transporte"));
                        solicitud.setMontoGasolina(rs.getDouble("Monto_Gasolina"));
                        solicitud.setEstado(rs.getString("Estado"));

                        String fechaSolicitud = rs.getString("Fecha_Solicitud");
                        if (fechaSolicitud != null && fechaSolicitud.length() >= 10) {
                            solicitud.setFechaSolicitud(fechaSolicitud.substring(0, 10));
                        } else {
                            solicitud.setFechaSolicitud("2025-05-27");
                        }

                        Log.d("RH_DAO_DEBUG", "Solicitud " + contador + ": ID=" + solicitud.getIdSolicitud() +
                                ", Folio=" + solicitud.getFolioViaje() +
                                ", Beneficiario=" + solicitud.getIdBeneficiario() +
                                ", Estado=" + solicitud.getEstado());

                        solicitudes.add(solicitud);
                    }
                    Log.d("RH_DAO_DEBUG", "TOTAL SOLICITUDES ENCONTRADAS: " + solicitudes.size());
                    return solicitudes;

                } catch (Exception e) {
                    Log.e("RH_DAO_DEBUG", "EXCEPCION: " + e.getMessage());
                    Log.e("RH_DAO_DEBUG", "STACK TRACE: ", e);
                    error = e.getMessage();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(List<Solicitud> solicitudes) {
                Log.d("RH_DAO_DEBUG", "onPostExecute - solicitudes: " + (solicitudes != null ? solicitudes.size() : "NULL"));
                Log.d("RH_DAO_DEBUG", "onPostExecute - error: " + error);
                if (solicitudes != null) {
                    callback.onSuccess(solicitudes);
                } else {
                    callback.onError(error != null ? error : "Error al obtener solicitudes");
                }
            }
        }.execute();
    }

    public static void obtenerSolicitudesHistorial(SolicitudesListCallback callback) {
        new AsyncTask<Void, Void, List<Solicitud>>() {
            private String error;

            @Override
            protected List<Solicitud> doInBackground(Void... voids) {
                List<Solicitud> solicitudes = new ArrayList<>();
                try (Connection conn = DatabaseConnection.getConnection()) {
                    if (conn == null) {
                        error = "Error de conexión";
                        return null;
                    }

                    String query = "SELECT * FROM Solicitudes_Viaticos " +
                            "WHERE Estado IN ('Autorizada', 'Rechazada') " +
                            "ORDER BY Fecha_Revision DESC";

                    PreparedStatement stmt = conn.prepareStatement(query);
                    ResultSet rs = stmt.executeQuery();

                    while (rs.next()) {
                        Solicitud solicitud = new Solicitud();
                        solicitud.setIdSolicitud(rs.getInt("ID_Solicitud"));
                        solicitud.setFolioViaje(rs.getString("Folio_Viaje"));
                        solicitud.setIdSolicitante(rs.getString("ID_Solicitante"));
                        solicitud.setIdBeneficiario(rs.getString("ID_Beneficiario"));
                        solicitud.setMontoHotel(rs.getDouble("Monto_Hotel"));
                        solicitud.setMontoComida(rs.getDouble("Monto_Comida"));
                        solicitud.setMontoTransporte(rs.getDouble("Monto_Transporte"));
                        solicitud.setMontoGasolina(rs.getDouble("Monto_Gasolina"));
                        solicitud.setEstado(rs.getString("Estado"));
                        solicitud.setIdRevisor(rs.getString("ID_Revisor"));

                        String fechaRevision = rs.getString("Fecha_Revision");
                        if (fechaRevision != null && fechaRevision.length() >= 10) {
                            solicitud.setFechaRevision(fechaRevision.substring(0, 10));
                        }

                        solicitudes.add(solicitud);
                    }
                    return solicitudes;

                } catch (Exception e) {
                    error = e.getMessage();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(List<Solicitud> solicitudes) {
                if (solicitudes != null) {
                    callback.onSuccess(solicitudes);
                } else {
                    callback.onError(error != null ? error : "Error al obtener historial de solicitudes");
                }
            }
        }.execute();
    }

    public static void autorizarSolicitud(int idSolicitud, String idRevisor, SolicitudCallback callback) {
        new AsyncTask<Void, Void, String>() {
            private String error;

            @Override
            protected String doInBackground(Void... voids) {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    if (conn == null) {
                        error = "Error de conexión";
                        return null;
                    }

                    String query = "UPDATE Solicitudes_Viaticos SET Estado = 'Autorizada', " +
                            "ID_Revisor = ?, Fecha_Revision = GETDATE() WHERE ID_Solicitud = ?";

                    PreparedStatement stmt = conn.prepareStatement(query);
                    stmt.setString(1, idRevisor);
                    stmt.setInt(2, idSolicitud);

                    int result = stmt.executeUpdate();
                    return result > 0 ? "Solicitud autorizada exitosamente" : null;

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
                    callback.onError(error != null ? error : "Error al autorizar solicitud");
                }
            }
        }.execute();
    }

    public static void rechazarSolicitud(int idSolicitud, String idRevisor, SolicitudCallback callback) {
        new AsyncTask<Void, Void, String>() {
            private String error;

            @Override
            protected String doInBackground(Void... voids) {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    if (conn == null) {
                        error = "Error de conexión";
                        return null;
                    }

                    String query = "UPDATE Solicitudes_Viaticos SET Estado = 'Rechazada', " +
                            "ID_Revisor = ?, Fecha_Revision = GETDATE() WHERE ID_Solicitud = ?";

                    PreparedStatement stmt = conn.prepareStatement(query);
                    stmt.setString(1, idRevisor);
                    stmt.setInt(2, idSolicitud);

                    int result = stmt.executeUpdate();
                    return result > 0 ? "Solicitud rechazada correctamente" : null;

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
                    callback.onError(error != null ? error : "Error al rechazar solicitud");
                }
            }
        }.execute();
    }
}