package com.example.gestorviaticoscoppel;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.gestorviaticoscoppel.dao.SolicitudDAO;
import com.example.gestorviaticoscoppel.dao.UserDAO;
import com.example.gestorviaticoscoppel.models.Solicitud;
import com.example.gestorviaticoscoppel.models.User;

public class SolicitudColaboradorActivity extends AppCompatActivity {

    private EditText editTextNumeroEmpleado;
    private EditText editTextFolioViaje;
    private EditText editTextMontoHotel;
    private EditText editTextMontoComida;
    private EditText editTextMontoTransporte;
    private EditText editTextMontoGasolina;
    private TextView buttonEnviarSolicitud;
    private TextView buttonRegresarMenu;

    private String idGerente = "90313132";
    private String idColaboradorValidado = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solicitud_colaborador);

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        editTextNumeroEmpleado = findViewById(R.id.editTextNumeroEmpleado);
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
                crearSolicitudColaborador();
            }
        });

        buttonRegresarMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                regresarMenu();
            }
        });

        editTextNumeroEmpleado.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    validarColaborador();
                }
            }
        });
    }

    private void validarColaborador() {
        String numeroEmpleado = editTextNumeroEmpleado.getText().toString().trim();

        if (numeroEmpleado.isEmpty()) {
            idColaboradorValidado = "";
            return;
        }

        if (numeroEmpleado.equals(idGerente)) {
            Toast.makeText(this, "No puede crear solicitud para sí mismo. Use 'Solicitud de viáticos'",
                    Toast.LENGTH_LONG).show();
            editTextNumeroEmpleado.setText("");
            idColaboradorValidado = "";
            return;
        }

        UserDAO.validarUsuarioExistente(numeroEmpleado, new UserDAO.UsuarioValidacionCallback() {
            @Override
            public void onSuccess(User usuario) {
                if ("Colaborador".equals(usuario.getRol())) {
                    idColaboradorValidado = numeroEmpleado;
                    Toast.makeText(SolicitudColaboradorActivity.this,
                            "Colaborador validado: " + usuario.getName(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SolicitudColaboradorActivity.this,
                            "El usuario debe tener rol de Colaborador", Toast.LENGTH_SHORT).show();
                    editTextNumeroEmpleado.setText("");
                    idColaboradorValidado = "";
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(SolicitudColaboradorActivity.this,
                        "Colaborador no encontrado: " + numeroEmpleado, Toast.LENGTH_SHORT).show();
                editTextNumeroEmpleado.setText("");
                idColaboradorValidado = "";
            }
        });
    }

    private void crearSolicitudColaborador() {
        Log.d("SOLICITUD_COLAB_DEBUG", "=== INICIANDO crearSolicitudColaborador ===");

        if (!validarFormulario()) {
            Log.d("SOLICITUD_COLAB_DEBUG", "FALLO EN VALIDACION");
            return;
        }
        Log.d("SOLICITUD_COLAB_DEBUG", "VALIDACION EXITOSA");

        String numeroEmpleado = editTextNumeroEmpleado.getText().toString().trim();
        String folio = editTextFolioViaje.getText().toString().trim();
        double montoHotel = obtenerMonto(editTextMontoHotel);
        double montoComida = obtenerMonto(editTextMontoComida);
        double montoTransporte = obtenerMonto(editTextMontoTransporte);
        double montoGasolina = obtenerMonto(editTextMontoGasolina);

        Log.d("SOLICITUD_COLAB_DEBUG", "Colaborador: " + numeroEmpleado);
        Log.d("SOLICITUD_COLAB_DEBUG", "Folio: " + folio);
        Log.d("SOLICITUD_COLAB_DEBUG", "Hotel: " + montoHotel);
        Log.d("SOLICITUD_COLAB_DEBUG", "Comida: " + montoComida);
        Log.d("SOLICITUD_COLAB_DEBUG", "Transporte: " + montoTransporte);
        Log.d("SOLICITUD_COLAB_DEBUG", "Gasolina: " + montoGasolina);

        Solicitud solicitud = new Solicitud(
                folio,
                idGerente,
                numeroEmpleado,
                montoHotel,
                montoComida,
                montoTransporte,
                montoGasolina
        );

        Log.d("SOLICITUD_COLAB_DEBUG", "ANTES DE LLAMAR SolicitudDAO.crearSolicitud");

        SolicitudDAO.crearSolicitud(solicitud, new SolicitudDAO.SolicitudCallback() {
            @Override
            public void onSuccess(String message) {
                Log.d("SOLICITUD_COLAB_DEBUG", "SUCCESS: " + message);
                Toast.makeText(SolicitudColaboradorActivity.this,
                        "Solicitud para colaborador " + numeroEmpleado + " enviada correctamente a RH",
                        Toast.LENGTH_LONG).show();
                limpiarFormulario();
            }

            @Override
            public void onError(String error) {
                Log.e("SOLICITUD_COLAB_DEBUG", "ERROR: " + error);
                if (error.contains("Violation of UNIQUE KEY constraint") || error.contains("duplicate")) {
                    Toast.makeText(SolicitudColaboradorActivity.this,
                            "Error: Ya existe una solicitud con ese folio. Use un folio diferente.",
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(SolicitudColaboradorActivity.this,
                            "Error al crear solicitud: " + error, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private boolean validarFormulario() {
        Log.d("SOLICITUD_COLAB_DEBUG", "=== VALIDANDO FORMULARIO ===");

        String numeroEmpleado = editTextNumeroEmpleado.getText().toString().trim();

        if (numeroEmpleado.isEmpty()) {
            Log.d("SOLICITUD_COLAB_DEBUG", "Numero empleado vacio");
            Toast.makeText(this, "Por favor ingrese el número de empleado del colaborador", Toast.LENGTH_SHORT).show();
            editTextNumeroEmpleado.requestFocus();
            return false;
        }

        if (idColaboradorValidado.isEmpty() || !idColaboradorValidado.equals(numeroEmpleado)) {
            Log.d("SOLICITUD_COLAB_DEBUG", "Colaborador no validado");
            Toast.makeText(this, "El colaborador ingresado no es válido", Toast.LENGTH_SHORT).show();
            editTextNumeroEmpleado.requestFocus();
            return false;
        }

        String folio = editTextFolioViaje.getText().toString().trim();

        if (folio.isEmpty()) {
            Log.d("SOLICITUD_COLAB_DEBUG", "Folio vacio");
            Toast.makeText(this, "Por favor ingrese el folio del viaje", Toast.LENGTH_SHORT).show();
            editTextFolioViaje.requestFocus();
            return false;
        }

        if (folio.length() != 6) {
            Log.d("SOLICITUD_COLAB_DEBUG", "Folio no tiene 6 digitos: " + folio.length());
            Toast.makeText(this, "El folio debe tener exactamente 6 dígitos", Toast.LENGTH_SHORT).show();
            editTextFolioViaje.requestFocus();
            return false;
        }

        if (!folio.matches("\\d{6}")) {
            Log.d("SOLICITUD_COLAB_DEBUG", "Folio no son solo numeros");
            Toast.makeText(this, "El folio debe contener solo números", Toast.LENGTH_SHORT).show();
            editTextFolioViaje.requestFocus();
            return false;
        }

        String montoHotel = editTextMontoHotel.getText().toString().trim().replace("$", "");
        String montoComida = editTextMontoComida.getText().toString().trim().replace("$", "");
        String montoTransporte = editTextMontoTransporte.getText().toString().trim().replace("$", "");
        String montoGasolina = editTextMontoGasolina.getText().toString().trim().replace("$", "");

        if (montoHotel.isEmpty() && montoComida.isEmpty() &&
                montoTransporte.isEmpty() && montoGasolina.isEmpty()) {
            Log.d("SOLICITUD_COLAB_DEBUG", "Todos los montos vacios");
            Toast.makeText(this, "Por favor ingrese al menos un monto para alguna categoría", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!validarMonto(montoHotel, "Hotel") || !validarMonto(montoComida, "Comida") ||
                !validarMonto(montoTransporte, "Transporte") || !validarMonto(montoGasolina, "Gasolina")) {
            Log.d("SOLICITUD_COLAB_DEBUG", "Error en validacion de montos");
            return false;
        }

        Log.d("SOLICITUD_COLAB_DEBUG", "FORMULARIO VALIDO");
        return true;
    }

    private boolean validarMonto(String monto, String categoria) {
        if (monto.isEmpty()) {
            return true;
        }

        try {
            double valor = Double.parseDouble(monto);
            if (valor < 0) {
                Toast.makeText(this, "El monto de " + categoria + " no puede ser negativo", Toast.LENGTH_SHORT).show();
                return false;
            }
            if (valor > 999999) {
                Toast.makeText(this, "El monto de " + categoria + " es demasiado alto", Toast.LENGTH_SHORT).show();
                return false;
            }
            return true;
        } catch (NumberFormatException e) {
            Toast.makeText(this, "El monto de " + categoria + " no es válido", Toast.LENGTH_SHORT).show();
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
        editTextNumeroEmpleado.setText("");
        editTextFolioViaje.setText("");
        editTextMontoHotel.setText("");
        editTextMontoComida.setText("");
        editTextMontoTransporte.setText("");
        editTextMontoGasolina.setText("");
        idColaboradorValidado = "";
        editTextNumeroEmpleado.requestFocus();
    }

    private void regresarMenu() {
        Intent intent = new Intent(SolicitudColaboradorActivity.this, GestorGerenteActivity.class);
        startActivity(intent);
        finish();
    }
}