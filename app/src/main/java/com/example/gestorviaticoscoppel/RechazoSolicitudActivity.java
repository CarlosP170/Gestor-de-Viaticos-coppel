package com.example.gestorviaticoscoppel;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.gestorviaticoscoppel.dao.SolicitudDAO;
import com.example.gestorviaticoscoppel.dao.ViajeDAO;
import com.example.gestorviaticoscoppel.models.Viaje;
import com.example.gestorviaticoscoppel.utils.NotificationManager;
import com.example.gestorviaticoscoppel.utils.SessionManager;

public class RechazoSolicitudActivity extends AppCompatActivity {

    private TextView textViewTitle;
    private TextView textViewNumeroEmpleado;
    private TextView textViewFolioViaje;
    private TextView buttonNumeroIncorrecto;
    private TextView buttonColaboradorIncorrecto;
    private TextView buttonMontosExcedentes;
    private TextView buttonMontosInsuficientes;
    private TextView buttonCategoriasIncorrectas;
    private TextView buttonOtroMotivo;
    private EditText editTextOtroMotivo;
    private TextView buttonRechazarSolicitud;
    private TextView buttonRegresarMenu;

    private int idSolicitud;
    private String idBeneficiario;
    private String folioViaje;
    private String idRevisorRH;
    private String motivoSeleccionado = "";
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rechazo_solicitud);

        sessionManager = new SessionManager(this);
        idRevisorRH = sessionManager.getCurrentUserId();

        obtenerDatos();
        initializeViews();
        setupClickListeners();
        cargarDatos();
    }

    private void obtenerDatos() {
        Intent intent = getIntent();
        idSolicitud = intent.getIntExtra("idSolicitud", -1);
        idBeneficiario = intent.getStringExtra("idBeneficiario");
        folioViaje = intent.getStringExtra("folioViaje");
        String idRevisorIntent = intent.getStringExtra("idRevisorRH");

        if (idRevisorIntent != null) {
            idRevisorRH = idRevisorIntent;
        }

        if (idSolicitud == -1 || idBeneficiario == null || folioViaje == null || idRevisorRH == null) {
            Toast.makeText(this, "⚠️ Error: Datos incompletos", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initializeViews() {
        textViewTitle = findViewById(R.id.textViewTitle);
        textViewNumeroEmpleado = findViewById(R.id.textViewNumeroEmpleado);
        textViewFolioViaje = findViewById(R.id.textViewFolioViaje);
        buttonNumeroIncorrecto = findViewById(R.id.buttonNumeroIncorrecto);
        buttonColaboradorIncorrecto = findViewById(R.id.buttonColaboradorIncorrecto);
        buttonMontosExcedentes = findViewById(R.id.buttonMontosExcedentes);
        buttonMontosInsuficientes = findViewById(R.id.buttonMontosInsuficientes);
        buttonCategoriasIncorrectas = findViewById(R.id.buttonCategoriasIncorrectas);
        buttonOtroMotivo = findViewById(R.id.buttonOtroMotivo);
        editTextOtroMotivo = findViewById(R.id.editTextOtroMotivo);
        buttonRechazarSolicitud = findViewById(R.id.buttonRechazarSolicitud);
        buttonRegresarMenu = findViewById(R.id.buttonRegresarMenu);
    }

    private void setupClickListeners() {
        buttonNumeroIncorrecto.setOnClickListener(v -> seleccionarMotivo("Número de folio incorrecto o no existe"));
        buttonColaboradorIncorrecto.setOnClickListener(v -> seleccionarMotivo("Número de colaborador incorrecto o no corresponde"));
        buttonMontosExcedentes.setOnClickListener(v -> seleccionarMotivo("Montos solicitados excedentes"));
        buttonMontosInsuficientes.setOnClickListener(v -> seleccionarMotivo("Montos solicitados insuficientes"));
        buttonCategoriasIncorrectas.setOnClickListener(v -> seleccionarMotivo("Montos en categorías incorrectos o inválidos"));

        buttonOtroMotivo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String otroTexto = editTextOtroMotivo.getText().toString().trim();
                if (!otroTexto.isEmpty()) {
                    seleccionarMotivo(otroTexto);
                } else {
                    Toast.makeText(RechazoSolicitudActivity.this, "⚠️ Escriba el motivo del rechazo", Toast.LENGTH_SHORT).show();
                }
            }
        });

        buttonRechazarSolicitud.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (motivoSeleccionado.isEmpty()) {
                    Toast.makeText(RechazoSolicitudActivity.this, "⚠️ Seleccione un motivo de rechazo", Toast.LENGTH_SHORT).show();
                } else {
                    mostrarDialogoConfirmacion();
                }
            }
        });

        buttonRegresarMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                regresarMenu();
            }
        });
    }

    private void cargarDatos() {
        textViewNumeroEmpleado.setText(idBeneficiario);
        textViewFolioViaje.setText("Folio: " + folioViaje);
    }

    private void seleccionarMotivo(String motivo) {
        motivoSeleccionado = motivo;
        limpiarSelecciones();

        if (motivo.equals("Número de folio incorrecto o no existe")) {
            buttonNumeroIncorrecto.setBackgroundResource(R.drawable.orange_button_background);
        } else if (motivo.equals("Número de colaborador incorrecto o no corresponde")) {
            buttonColaboradorIncorrecto.setBackgroundResource(R.drawable.orange_button_background);
        } else if (motivo.equals("Montos solicitados excedentes")) {
            buttonMontosExcedentes.setBackgroundResource(R.drawable.orange_button_background);
        } else if (motivo.equals("Montos solicitados insuficientes")) {
            buttonMontosInsuficientes.setBackgroundResource(R.drawable.orange_button_background);
        } else if (motivo.equals("Montos en categorías incorrectos o inválidos")) {
            buttonCategoriasIncorrectas.setBackgroundResource(R.drawable.orange_button_background);
        } else {
            buttonOtroMotivo.setBackgroundResource(R.drawable.orange_button_background);
        }

        Toast.makeText(this, "✅ Motivo seleccionado: " + motivo, Toast.LENGTH_SHORT).show();
    }

    private void limpiarSelecciones() {
        buttonNumeroIncorrecto.setBackgroundResource(R.drawable.large_gray_area);
        buttonColaboradorIncorrecto.setBackgroundResource(R.drawable.large_gray_area);
        buttonMontosExcedentes.setBackgroundResource(R.drawable.large_gray_area);
        buttonMontosInsuficientes.setBackgroundResource(R.drawable.large_gray_area);
        buttonCategoriasIncorrectas.setBackgroundResource(R.drawable.large_gray_area);
        buttonOtroMotivo.setBackgroundResource(R.drawable.large_gray_area);
    }

    private void mostrarDialogoConfirmacion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("❌ Rechazar Solicitud");
        builder.setMessage("¿Desea rechazar la solicitud?\n\n" +
                "👤 Empleado: " + idBeneficiario + "\n" +
                "📋 Folio: " + folioViaje + "\n\n" +
                "📝 Motivo del rechazo:\n" + motivoSeleccionado);

        builder.setPositiveButton("✅ Sí, Rechazar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                rechazarSolicitud();
            }
        });

        builder.setNegativeButton("❌ Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void rechazarSolicitud() {
        buttonRechazarSolicitud.setEnabled(false);

        SolicitudDAO.rechazarSolicitudConMotivo(idSolicitud, idRevisorRH, motivoSeleccionado, new SolicitudDAO.SolicitudCallback() {
            @Override
            public void onSuccess(String message) {
                NotificationManager.crearNotificacionSolicitudRechazadaConMotivo(idBeneficiario, folioViaje, motivoSeleccionado);

                Toast.makeText(RechazoSolicitudActivity.this,
                        "✅ Solicitud rechazada correctamente\n📧 Empleado notificado",
                        Toast.LENGTH_LONG).show();
                regresarMenu();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(RechazoSolicitudActivity.this,
                        "❌ Error al rechazar solicitud: " + error, Toast.LENGTH_LONG).show();
                buttonRechazarSolicitud.setEnabled(true);
            }
        });
    }

    private void regresarMenu() {
        Intent intent = new Intent(RechazoSolicitudActivity.this, AdministradorActivity.class);
        startActivity(intent);
        finish();
    }
}