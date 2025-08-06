package com.example.gestorviaticoscoppel;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.gestorviaticoscoppel.dao.SolicitudDAO;
import com.example.gestorviaticoscoppel.models.Solicitud;
import com.example.gestorviaticoscoppel.utils.SessionManager;

public class SolicitudViaticosActivity extends AppCompatActivity {

    private EditText editTextFolioViaje;
    private EditText editTextMontoHotel;
    private EditText editTextMontoComida;
    private EditText editTextMontoTransporte;
    private EditText editTextMontoGasolina;
    private TextView buttonEnviarSolicitud;
    private TextView buttonRegresarMenu;

    private String idGerente;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solicitud_viaticos);

        sessionManager = new SessionManager(this);
        idGerente = sessionManager.getCurrentUserId();

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        editTextFolioViaje = findViewById(R.id.editTextFolioViaje);
        editTextMontoHotel = findViewById(R.id.editTextMontoHotel);
        editTextMontoComida = findViewById(R.id.editTextMontoComida);
        editTextMontoTransporte = findViewById(R.id.editTextMontoTransporte);
        editTextMontoGasolina = findViewById(R.id.editTextMontoGasolina);
        buttonEnviarSolicitud = findViewById(R.id.buttonEnviarSolicitud);
        buttonRegresarMenu = findViewById(R.id.buttonRegresarMenu);
    }

    private void setupClickListeners() {
        buttonEnviarSolicitud.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                crearSolicitud();
            }
        });

        buttonRegresarMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                regresarMenu();
            }
        });
    }

    private void crearSolicitud() {
        if (!validarFormulario()) {
            return;
        }

        String folio = editTextFolioViaje.getText().toString().trim();
        double montoHotel = obtenerMonto(editTextMontoHotel);
        double montoComida = obtenerMonto(editTextMontoComida);
        double montoTransporte = obtenerMonto(editTextMontoTransporte);
        double montoGasolina = obtenerMonto(editTextMontoGasolina);

        Solicitud solicitud = new Solicitud(
                folio,
                idGerente,
                idGerente,
                montoHotel,
                montoComida,
                montoTransporte,
                montoGasolina
        );

        SolicitudDAO.crearSolicitud(solicitud, new SolicitudDAO.SolicitudCallback() {
            @Override
            public void onSuccess(String message) {
                Toast.makeText(SolicitudViaticosActivity.this,
                        "✅ Solicitud enviada correctamente a RH para revisión", Toast.LENGTH_LONG).show();
                limpiarFormulario();
            }

            @Override
            public void onError(String error) {
                if (error.contains("Violation of UNIQUE KEY constraint") || error.contains("duplicate")) {
                    Toast.makeText(SolicitudViaticosActivity.this,
                            "❌ Error: Ya existe una solicitud con ese folio. Use un folio diferente.",
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(SolicitudViaticosActivity.this,
                            "❌ Error al crear solicitud: " + error, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private boolean validarFormulario() {
        String folio = editTextFolioViaje.getText().toString().trim();

        if (folio.isEmpty()) {
            Toast.makeText(this, "⚠️ Por favor ingrese el folio del viaje", Toast.LENGTH_SHORT).show();
            editTextFolioViaje.requestFocus();
            return false;
        }

        if (folio.length() != 6) {
            Toast.makeText(this, "⚠️ El folio debe tener exactamente 6 dígitos", Toast.LENGTH_SHORT).show();
            editTextFolioViaje.requestFocus();
            return false;
        }

        if (!folio.matches("\\d{6}")) {
            Toast.makeText(this, "⚠️ El folio debe contener solo números", Toast.LENGTH_SHORT).show();
            editTextFolioViaje.requestFocus();
            return false;
        }

        String montoHotel = editTextMontoHotel.getText().toString().trim().replace("$", "");
        String montoComida = editTextMontoComida.getText().toString().trim().replace("$", "");
        String montoTransporte = editTextMontoTransporte.getText().toString().trim().replace("$", "");
        String montoGasolina = editTextMontoGasolina.getText().toString().trim().replace("$", "");

        if (montoHotel.isEmpty() && montoComida.isEmpty() &&
                montoTransporte.isEmpty() && montoGasolina.isEmpty()) {
            Toast.makeText(this, "⚠️ Por favor ingrese al menos un monto para alguna categoría", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!validarMonto(montoHotel, "Hotel") || !validarMonto(montoComida, "Comida") ||
                !validarMonto(montoTransporte, "Transporte") || !validarMonto(montoGasolina, "Gasolina")) {
            return false;
        }

        return true;
    }

    private boolean validarMonto(String monto, String categoria) {
        if (monto.isEmpty()) {
            return true;
        }

        try {
            double valor = Double.parseDouble(monto);
            if (valor < 0) {
                Toast.makeText(this, "⚠️ El monto de " + categoria + " no puede ser negativo", Toast.LENGTH_SHORT).show();
                return false;
            }
            if (valor > 999999) {
                Toast.makeText(this, "⚠️ El monto de " + categoria + " es demasiado alto", Toast.LENGTH_SHORT).show();
                return false;
            }
            return true;
        } catch (NumberFormatException e) {
            Toast.makeText(this, "⚠️ El monto de " + categoria + " no es válido", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private double obtenerMonto(EditText editText) {
        String texto = editText.getText().toString().trim().replace("$", "");
        if (texto.isEmpty()) {
            return 0.0;
        }
        try {
            return Double.parseDouble(texto);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private void limpiarFormulario() {
        editTextFolioViaje.setText("");
        editTextMontoHotel.setText("");
        editTextMontoComida.setText("");
        editTextMontoTransporte.setText("");
        editTextMontoGasolina.setText("");
        editTextFolioViaje.requestFocus();
    }

    private void regresarMenu() {
        Intent intent = new Intent(SolicitudViaticosActivity.this, GestorGerenteActivity.class);
        startActivity(intent);
        finish();
    }
}