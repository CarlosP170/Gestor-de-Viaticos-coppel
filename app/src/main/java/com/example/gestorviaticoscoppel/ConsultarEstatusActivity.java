package com.example.gestorviaticoscoppel;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.gestorviaticoscoppel.dao.ViajeDAO;
import com.example.gestorviaticoscoppel.models.Viaje;
import java.text.DecimalFormat;

public class ConsultarEstatusActivity extends AppCompatActivity {

    private EditText editTextNumeroRecibo;
    private TextView buttonGuardarDatos;
    private TextView buttonRegresarMenu;
    private TextView buttonEnviarRevision;
    private TextView textSaldoRestante;
    private String origen;
    private int idViaje;
    private Viaje viajeActual;
    private DecimalFormat formatoPeso = new DecimalFormat("#,###.00");
    private String folioTicketGuardado = "";
    private boolean folioYaGuardado = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consultar_estatus);

        obtenerDatosIntent();
        initializeViews();
        setupClickListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarDatosViaje();
    }

    private void obtenerDatosIntent() {
        origen = getIntent().getStringExtra("origen");
        idViaje = getIntent().getIntExtra("idViaje", -1);

        if (origen == null) {
            origen = "colaborador";
        }

        if (idViaje == -1) {
            Toast.makeText(this, "Error: No se encontró el viaje", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }

    private void initializeViews() {
        editTextNumeroRecibo = findViewById(R.id.editTextNumeroRecibo);
        buttonGuardarDatos = findViewById(R.id.buttonGuardarDatos);
        buttonRegresarMenu = findViewById(R.id.buttonRegresarMenu);
        buttonEnviarRevision = findViewById(R.id.buttonEnviarRevision);

        textSaldoRestante = buscarTextViewSaldo();
    }

    private void setupClickListeners() {
        buttonGuardarDatos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardarFolioTicket();
            }
        });

        buttonRegresarMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                regresarMenu();
            }
        });

        buttonEnviarRevision.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enviarARevision();
            }
        });
    }

    private void cargarDatosViaje() {
        ViajeDAO.obtenerViajeActivo(getCurrentUserId(), new ViajeDAO.ViajeSingleCallback() {
            @Override
            public void onSuccess(Viaje viaje) {
                if (viaje.getIdViaje() == idViaje) {
                    viajeActual = viaje;
                    runOnUiThread(() -> {
                        actualizarSaldoRestante();
                        cargarFolioTicketExistente();
                    });
                } else {
                    Toast.makeText(ConsultarEstatusActivity.this,
                            "Error: El viaje no coincide", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(ConsultarEstatusActivity.this,
                        "Error al cargar el viaje: " + error, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void actualizarSaldoRestante() {
        if (viajeActual == null) return;

        double saldoHotel = viajeActual.getMontoHotelAutorizado() - viajeActual.getMontoHotelGastado();
        double saldoComida = viajeActual.getMontoComidaAutorizado() - viajeActual.getMontoComidaGastado();
        double saldoTransporte = viajeActual.getMontoTransporteAutorizado() - viajeActual.getMontoTransporteGastado();
        double saldoGasolina = viajeActual.getMontoGasolinaAutorizado() - viajeActual.getMontoGasolinaGastado();

        double saldoTotal = saldoHotel + saldoComida + saldoTransporte + saldoGasolina - viajeActual.getMontoOtrosGastado();

        if (textSaldoRestante != null) {
            textSaldoRestante.setText("$ " + formatoPeso.format(saldoTotal));
        }

        boolean saldosEnCero = saldoTotal <= 0.01;
        boolean tieneSobrantes = viajeActual.getMontoOtrosGastado() > 0.01;

        if (saldosEnCero) {
            habilitarEnvioRevision();

            if (tieneSobrantes) {
                habilitarSeccionFolio();
                Toast.makeText(this, "Saldos en $0. Puede enviar a revisión.", Toast.LENGTH_SHORT).show();
            } else {
                deshabilitarSeccionFolio();
                Toast.makeText(this, "Saldos en $0. Puede enviar a revisión.", Toast.LENGTH_SHORT).show();
            }
        } else {
            deshabilitarEnvioRevision();
            if (tieneSobrantes) {
                habilitarSeccionFolio();
            } else {
                deshabilitarSeccionFolio();
            }
            Toast.makeText(this, "Saldo restante: $" + formatoPeso.format(saldoTotal), Toast.LENGTH_SHORT).show();
        }
    }

    private void habilitarEnvioRevision() {
        buttonEnviarRevision.setEnabled(true);
        buttonEnviarRevision.setAlpha(1.0f);
    }

    private void deshabilitarEnvioRevision() {
        buttonEnviarRevision.setEnabled(false);
        buttonEnviarRevision.setAlpha(0.5f);
    }

    private void habilitarSeccionFolio() {
        if (!folioYaGuardado) {
            editTextNumeroRecibo.setEnabled(true);
            buttonGuardarDatos.setEnabled(true);
            buttonGuardarDatos.setText("Guardar Datos");
        }
    }

    private void deshabilitarSeccionFolio() {
        editTextNumeroRecibo.setEnabled(false);
        buttonGuardarDatos.setEnabled(false);
    }

    private TextView buscarTextViewSaldo() {
        View rootView = findViewById(android.R.id.content);
        return buscarTextViewRecursivo(rootView, "$ 0");
    }

    private TextView buscarTextViewRecursivo(View view, String textoObjetivo) {
        if (view instanceof TextView) {
            TextView textView = (TextView) view;
            String texto = textView.getText().toString();
            if (texto.contains("$") && !texto.contains("Saldo restante")) {
                return textView;
            }
        } else if (view instanceof android.view.ViewGroup) {
            android.view.ViewGroup group = (android.view.ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                TextView result = buscarTextViewRecursivo(group.getChildAt(i), textoObjetivo);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    private void cargarFolioTicketExistente() {
        if (viajeActual != null && viajeActual.getFolioTicketSobrante() != null &&
                !viajeActual.getFolioTicketSobrante().trim().isEmpty()) {
            folioTicketGuardado = viajeActual.getFolioTicketSobrante();
            folioYaGuardado = true;
            editTextNumeroRecibo.setText(folioTicketGuardado);
            editTextNumeroRecibo.setEnabled(false);
            buttonGuardarDatos.setText("Folio Guardado");
            buttonGuardarDatos.setEnabled(false);
        } else {
            folioYaGuardado = false;
            folioTicketGuardado = "";
            editTextNumeroRecibo.setText("");
            editTextNumeroRecibo.setEnabled(true);
            buttonGuardarDatos.setText("Guardar Datos");
            buttonGuardarDatos.setEnabled(true);
        }
    }

    private void guardarFolioTicket() {
        String numeroRecibo = editTextNumeroRecibo.getText().toString().trim();

        if (numeroRecibo.isEmpty()) {
            Toast.makeText(this, "Por favor ingrese el número de recibo", Toast.LENGTH_SHORT).show();
            return;
        }

        if (numeroRecibo.length() < 3) {
            Toast.makeText(this, "El número de recibo debe tener al menos 3 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

        ViajeDAO.actualizarFolioTicket(idViaje, numeroRecibo, new ViajeDAO.ViajeCallback() {
            @Override
            public void onSuccess(String message) {
                folioTicketGuardado = numeroRecibo;
                folioYaGuardado = true;
                editTextNumeroRecibo.setEnabled(false);
                buttonGuardarDatos.setText("Folio Guardado");
                buttonGuardarDatos.setEnabled(false);
                Toast.makeText(ConsultarEstatusActivity.this, "Folio guardado correctamente", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(ConsultarEstatusActivity.this, "Error al guardar folio: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void enviarARevision() {
        if (viajeActual == null) {
            Toast.makeText(this, "Error: No se han cargado los datos del viaje", Toast.LENGTH_SHORT).show();
            return;
        }

        double saldoHotel = viajeActual.getMontoHotelAutorizado() - viajeActual.getMontoHotelGastado();
        double saldoComida = viajeActual.getMontoComidaAutorizado() - viajeActual.getMontoComidaGastado();
        double saldoTransporte = viajeActual.getMontoTransporteAutorizado() - viajeActual.getMontoTransporteGastado();
        double saldoGasolina = viajeActual.getMontoGasolinaAutorizado() - viajeActual.getMontoGasolinaGastado();

        double saldoTotal = saldoHotel + saldoComida + saldoTransporte + saldoGasolina - viajeActual.getMontoOtrosGastado();

        if (saldoTotal > 0.01) {
            Toast.makeText(this, "No puede enviar a revisión. Saldo pendiente: $" + formatoPeso.format(saldoTotal),
                    Toast.LENGTH_LONG).show();
            return;
        }

        String folioTicket = null;
        if (folioYaGuardado && !folioTicketGuardado.isEmpty()) {
            folioTicket = folioTicketGuardado;
        }

        ViajeDAO.enviarARevision(idViaje, folioTicket, new ViajeDAO.ViajeCallback() {
            @Override
            public void onSuccess(String message) {
                Toast.makeText(ConsultarEstatusActivity.this,
                        "Viaje enviado a revisión correctamente", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(ConsultarEstatusActivity.this, ViaticosViajeActivity.class);
                intent.putExtra("origen", origen);
                intent.putExtra("idViaje", idViaje);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(ConsultarEstatusActivity.this,
                        "Error al enviar a revisión: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private String getCurrentUserId() {
        if ("gerente".equals(origen)) {
            return "90313132";
        }
        return "90126701";
    }

    private void regresarMenu() {
        Intent intent = new Intent(ConsultarEstatusActivity.this, ViaticosViajeActivity.class);
        intent.putExtra("origen", origen);
        intent.putExtra("idViaje", idViaje);
        startActivity(intent);
        finish();
    }
}