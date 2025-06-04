package com.example.gestorviaticoscoppel.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import android.util.Log;

public class DatabaseConnection {
    private static final String TAG = "DB_CONNECTION";
    private static final String SERVER = "192.168.100.57";
    private static final String DATABASE = "GestorViaticosCoppel";
    private static final String USERNAME = "androiduser";
    private static final String PASSWORD = "Android123!";
    private static final String CONNECTION_URL =
            "jdbc:jtds:sqlserver://" + SERVER + ":1433;databaseName=" + DATABASE + ";user=" + USERNAME + ";password=" + PASSWORD + ";";

    public static Connection getConnection() {
        Log.d(TAG, "Conectando a nueva IP: " + SERVER);

        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            return DriverManager.getConnection(CONNECTION_URL);
        } catch (ClassNotFoundException | SQLException e) {
            Log.e(TAG, "Error de conexión: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}