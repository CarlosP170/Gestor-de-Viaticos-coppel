package com.example.gestorviaticoscoppel.dao;

import android.os.AsyncTask;
import android.util.Log;
import com.example.gestorviaticoscoppel.database.DatabaseConnection;
import com.example.gestorviaticoscoppel.models.Factura;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class FacturaDAO {

    private static final String TAG = "FacturaDAO";

    public interface FacturaCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    public interface FacturaListCallback {
        void onSuccess(List<Factura> facturas);
        void onError(String error);
    }

    public interface FacturaSingleCallback {
        void onSuccess(Factura factura);
        void onError(String error);
    }

    public static void guardarFactura(Factura factura, FacturaCallback callback) {
        new AsyncTask<Void, Void, String>() {
            private String error;

            @Override
            protected String doInBackground(Void... voids) {
                Log.d(TAG, "=== GUARDANDO FACTURA ===");
                Log.d(TAG, "ID Viaje: " + factura.getIdViaje());
                Log.d(TAG, "Categoria: " + factura.getCategoria());
                Log.d(TAG, "Monto: " + factura.getMonto());
                Log.d(TAG, "Archivo: " + factura.getArchivoNombre());
                Log.d(TAG, "Tipo: " + factura.getArchivoTipo());
                Log.d(TAG, "Base64 disponible: " + (factura.getArchivoContenidoBase64() != null && !factura.getArchivoContenidoBase64().trim().isEmpty()));

                try (Connection conn = DatabaseConnection.getConnection()) {
                    if (conn == null) {
                        error = "Error de conexión";
                        Log.e(TAG, "Conexión a BD es null");
                        return null;
                    }

                    String query = "INSERT INTO Facturas (ID_Viaje, Categoria, Monto, Archivo_Nombre, " +
                            "Archivo_Ruta, Archivo_Tipo, Archivo_Contenido_Base64) VALUES (?, ?, ?, ?, ?, ?, ?)";

                    PreparedStatement stmt = conn.prepareStatement(query);
                    stmt.setInt(1, factura.getIdViaje());
                    stmt.setString(2, factura.getCategoria());
                    stmt.setDouble(3, factura.getMonto());
                    stmt.setString(4, factura.getArchivoNombre());
                    stmt.setString(5, factura.getArchivoRuta() != null ? factura.getArchivoRuta() : "");
                    stmt.setString(6, factura.getArchivoTipo());
                    stmt.setString(7, factura.getArchivoContenidoBase64() != null ? factura.getArchivoContenidoBase64() : "");

                    Log.d(TAG, "Ejecutando INSERT...");
                    int result = stmt.executeUpdate();
                    Log.d(TAG, "Filas afectadas: " + result);

                    return result > 0 ? "Factura guardada exitosamente" : null;

                } catch (Exception e) {
                    error = e.getMessage();
                    Log.e(TAG, "Error guardando factura: " + e.getMessage());
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    Log.d(TAG, "Factura guardada con éxito");
                    callback.onSuccess(result);
                } else {
                    Log.e(TAG, "Error en guardar factura: " + error);
                    callback.onError(error != null ? error : "Error al guardar factura");
                }
            }
        }.execute();
    }

    public static void obtenerFacturasPorViaje(int idViaje, FacturaListCallback callback) {
        new AsyncTask<Void, Void, List<Factura>>() {
            private String error;

            @Override
            protected List<Factura> doInBackground(Void... voids) {
                Log.d(TAG, "=== OBTENIENDO FACTURAS POR VIAJE ===");
                Log.d(TAG, "ID Viaje: " + idViaje);

                List<Factura> facturas = new ArrayList<>();
                try (Connection conn = DatabaseConnection.getConnection()) {
                    if (conn == null) {
                        error = "Error de conexión";
                        Log.e(TAG, "Conexión a BD es null");
                        return null;
                    }

                    String query = "SELECT * FROM Facturas WHERE ID_Viaje = ? ORDER BY Fecha_Subida DESC";
                    PreparedStatement stmt = conn.prepareStatement(query);
                    stmt.setInt(1, idViaje);

                    Log.d(TAG, "Ejecutando query: " + query);
                    ResultSet rs = stmt.executeQuery();

                    int contador = 0;
                    while (rs.next()) {
                        contador++;
                        try {
                            Factura factura = mapearFactura(rs);
                            facturas.add(factura);
                            Log.d(TAG, "Factura " + contador + " mapeada: ID=" + factura.getIdFactura() +
                                    ", Categoria=" + factura.getCategoria() +
                                    ", Monto=" + factura.getMonto() +
                                    ", Base64=" + (factura.getArchivoContenidoBase64() != null && !factura.getArchivoContenidoBase64().trim().isEmpty()));
                        } catch (Exception e) {
                            Log.e(TAG, "Error mapeando factura " + contador + ": " + e.getMessage());
                            e.printStackTrace();
                        }
                    }

                    Log.d(TAG, "Total facturas encontradas: " + facturas.size());
                    return facturas;

                } catch (Exception e) {
                    error = e.getMessage();
                    Log.e(TAG, "Error obteniendo facturas: " + e.getMessage());
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(List<Factura> facturas) {
                if (facturas != null) {
                    Log.d(TAG, "Facturas obtenidas exitosamente: " + facturas.size());
                    callback.onSuccess(facturas);
                } else {
                    Log.e(TAG, "Error obteniendo facturas: " + error);
                    callback.onError(error != null ? error : "Error al obtener facturas");
                }
            }
        }.execute();
    }

    public static void obtenerFacturasPorCategoria(int idViaje, String categoria, FacturaListCallback callback) {
        new AsyncTask<Void, Void, List<Factura>>() {
            private String error;

            @Override
            protected List<Factura> doInBackground(Void... voids) {
                List<Factura> facturas = new ArrayList<>();
                try (Connection conn = DatabaseConnection.getConnection()) {
                    if (conn == null) {
                        error = "Error de conexión";
                        return null;
                    }

                    String query = "SELECT * FROM Facturas WHERE ID_Viaje = ? AND Categoria = ? ORDER BY Fecha_Subida DESC";
                    PreparedStatement stmt = conn.prepareStatement(query);
                    stmt.setInt(1, idViaje);
                    stmt.setString(2, categoria);
                    ResultSet rs = stmt.executeQuery();

                    while (rs.next()) {
                        facturas.add(mapearFactura(rs));
                    }
                    return facturas;

                } catch (Exception e) {
                    error = e.getMessage();
                    Log.e(TAG, "Error obteniendo facturas por categoría: " + e.getMessage());
                    return null;
                }
            }

            @Override
            protected void onPostExecute(List<Factura> facturas) {
                if (facturas != null) {
                    callback.onSuccess(facturas);
                } else {
                    callback.onError(error != null ? error : "Error al obtener facturas por categoría");
                }
            }
        }.execute();
    }

    public static void modificarFactura(int idFactura, double nuevoMonto, FacturaCallback callback) {
        new AsyncTask<Void, Void, String>() {
            private String error;

            @Override
            protected String doInBackground(Void... voids) {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    if (conn == null) {
                        error = "Error de conexión";
                        return null;
                    }

                    String query = "UPDATE Facturas SET Monto = ? WHERE ID_Factura = ?";
                    PreparedStatement stmt = conn.prepareStatement(query);
                    stmt.setDouble(1, nuevoMonto);
                    stmt.setInt(2, idFactura);

                    int result = stmt.executeUpdate();
                    return result > 0 ? "Factura modificada exitosamente" : null;

                } catch (Exception e) {
                    error = e.getMessage();
                    Log.e(TAG, "Error modificando factura: " + e.getMessage());
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    callback.onSuccess(result);
                } else {
                    callback.onError(error != null ? error : "Error al modificar factura");
                }
            }
        }.execute();
    }

    public static void eliminarFactura(int idFactura, FacturaCallback callback) {
        new AsyncTask<Void, Void, String>() {
            private String error;

            @Override
            protected String doInBackground(Void... voids) {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    if (conn == null) {
                        error = "Error de conexión";
                        return null;
                    }

                    String query = "DELETE FROM Facturas WHERE ID_Factura = ?";
                    PreparedStatement stmt = conn.prepareStatement(query);
                    stmt.setInt(1, idFactura);

                    int result = stmt.executeUpdate();
                    return result > 0 ? "Factura eliminada exitosamente" : null;

                } catch (Exception e) {
                    error = e.getMessage();
                    Log.e(TAG, "Error eliminando factura: " + e.getMessage());
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    callback.onSuccess(result);
                } else {
                    callback.onError(error != null ? error : "Error al eliminar factura");
                }
            }
        }.execute();
    }

    public static void obtenerTotalGastadoPorCategoria(int idViaje, String categoria, FacturaSingleCallback callback) {
        new AsyncTask<Void, Void, Double>() {
            private String error;

            @Override
            protected Double doInBackground(Void... voids) {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    if (conn == null) {
                        error = "Error de conexión";
                        return null;
                    }

                    String query = "SELECT SUM(Monto) as Total FROM Facturas WHERE ID_Viaje = ? AND Categoria = ?";
                    PreparedStatement stmt = conn.prepareStatement(query);
                    stmt.setInt(1, idViaje);
                    stmt.setString(2, categoria);
                    ResultSet rs = stmt.executeQuery();

                    if (rs.next()) {
                        return rs.getDouble("Total");
                    }
                    return 0.0;

                } catch (Exception e) {
                    error = e.getMessage();
                    Log.e(TAG, "Error calculando total por categoría: " + e.getMessage());
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Double total) {
                if (total != null) {
                    Factura facturaTemp = new Factura();
                    facturaTemp.setMonto(total);
                    callback.onSuccess(facturaTemp);
                } else {
                    callback.onError(error != null ? error : "Error al calcular total");
                }
            }
        }.execute();
    }

    public static void validarSaldosEnCero(int idViaje, FacturaCallback callback) {
        new AsyncTask<Void, Void, String>() {
            private String error;

            @Override
            protected String doInBackground(Void... voids) {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    if (conn == null) {
                        error = "Error de conexión";
                        return null;
                    }

                    String query = "SELECT " +
                            "(Monto_Hotel_Autorizado - Monto_Hotel_Gastado) as SaldoHotel, " +
                            "(Monto_Comida_Autorizado - Monto_Comida_Gastado) as SaldoComida, " +
                            "(Monto_Transporte_Autorizado - Monto_Transporte_Gastado) as SaldoTransporte, " +
                            "(Monto_Gasolina_Autorizado - Monto_Gasolina_Gastado) as SaldoGasolina, " +
                            "Monto_Otros_Gastado as SaldoOtros " +
                            "FROM Viajes WHERE ID_Viaje = ?";

                    PreparedStatement stmt = conn.prepareStatement(query);
                    stmt.setInt(1, idViaje);
                    ResultSet rs = stmt.executeQuery();

                    if (rs.next()) {
                        double saldoTotal = rs.getDouble("SaldoHotel") + rs.getDouble("SaldoComida") +
                                rs.getDouble("SaldoTransporte") + rs.getDouble("SaldoGasolina");
                        double saldoOtros = rs.getDouble("SaldoOtros");

                        if (saldoTotal == 0 && saldoOtros >= 0) {
                            return "Saldos validados correctamente";
                        } else {
                            return "Los saldos no están en cero. Total restante: $" + saldoTotal;
                        }
                    }
                    return null;

                } catch (Exception e) {
                    error = e.getMessage();
                    Log.e(TAG, "Error validando saldos: " + e.getMessage());
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    callback.onSuccess(result);
                } else {
                    callback.onError(error != null ? error : "Error al validar saldos");
                }
            }
        }.execute();
    }

    private static Factura mapearFactura(ResultSet rs) throws Exception {
        Log.d(TAG, "=== MAPEANDO FACTURA ===");

        Factura factura = new Factura();
        factura.setIdFactura(rs.getInt("ID_Factura"));
        factura.setIdViaje(rs.getInt("ID_Viaje"));
        factura.setCategoria(rs.getString("Categoria"));
        factura.setMonto(rs.getDouble("Monto"));
        factura.setArchivoNombre(rs.getString("Archivo_Nombre"));
        factura.setArchivoRuta(rs.getString("Archivo_Ruta"));
        factura.setArchivoTipo(rs.getString("Archivo_Tipo"));
        factura.setFechaSubida(rs.getString("Fecha_Subida"));

        Log.d(TAG, "Factura básica mapeada - ID: " + factura.getIdFactura() +
                ", Categoria: " + factura.getCategoria() +
                ", Monto: " + factura.getMonto());

        try {
            String base64Content = rs.getString("Archivo_Contenido_Base64");
            if (base64Content != null && !base64Content.trim().isEmpty()) {
                factura.setArchivoContenidoBase64(base64Content);
                Log.d(TAG, "Base64 content cargado - Tamaño: " + base64Content.length());
            } else {
                factura.setArchivoContenidoBase64("");
                Log.w(TAG, "Base64 content está vacío o null");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error cargando Base64 content: " + e.getMessage());
            factura.setArchivoContenidoBase64("");
        }

        Log.d(TAG, "Factura mapeada completamente - Ruta: " + factura.getArchivoRuta() +
                ", Base64 disponible: " + (factura.getArchivoContenidoBase64() != null && !factura.getArchivoContenidoBase64().trim().isEmpty()));

        return factura;
    }
}