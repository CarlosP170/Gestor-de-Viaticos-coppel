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
import com.example.gestorviaticoscoppel.dao.ViajeDAO;
import com.example.gestorviaticoscoppel.utils.NotificationManager;
import com.example.gestorviaticoscoppel.utils.SessionManager;

public class RechazoRevisionActivity extends AppCompatActivity {

    private TextView textViewTitle;
    private TextView textViewNumeroEmpleado;
    private TextView textViewFolioViaje;
    private TextView buttonSobranteSinFolio;
    private TextView buttonFacturasIrregulares;
    private TextView buttonFacturasIlegibles;
    private TextView buttonGastosFueraTiempo;
    private TextView buttonDepositoIncorrecto;
    private TextView buttonOtroMotivo;
    private EditText editTextOtroMotivo;
    private TextView buttonRechazarRevision;
    private TextView buttonRegresarMenu;

    private int idViaje;
    private String idUsuario;
    private String folioViaje;
    private String idRevisorRH;
    private String motivoSeleccionado = "";
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rechazo_revision);

        sessionManager = new SessionManager(this);
        idRevisorRH = sessionManager.getCurrentUserId();

        obtenerDatos();
        initializeViews();
        setupClickListeners();
        cargarDatos();
    }

    private void obtenerDatos() {
        Intent intent = getIntent();
        idViaje = intent.getIntExtra("idViaje", -1);
        idUsuario = intent.getStringExtra("idUsuario");
        folioViaje = intent.getStringExtra("folioViaje");
        String idRevisorIntent = intent.getStringExtra("idRevisorRH");

        if (idRevisorIntent != null) {
            idRevisorRH = idRevisorIntent;
        }

        if (idViaje == -1 || idUsuario == null || folioViaje == null || idRevisorRH == null) {
            Toast.makeText(this, "⚠️ Error: Datos incompletos", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initializeViews() {
        textViewTitle = findViewById(R.id.textViewTitle);
        textViewNumeroEmpleado = findViewById(R.id.textViewNumeroEmpleado);
        textViewFolioViaje = findViewById(R.id.textViewFolioViaje);
        buttonSobranteSinFolio = findViewById(R.id.buttonSobranteSinFolio);
        buttonFacturasIrregulares = findViewById(R.id.buttonFacturasIrregulares);
        buttonFacturasIlegibles = findViewById(R.id.buttonFacturasIlegibles);
        buttonGastosFueraTiempo = findViewById(R.id.buttonGastosFueraTiempo);
        buttonDepositoIncorrecto = findViewById(R.id.buttonDepositoIncorrecto);
        buttonOtroMotivo = findViewById(R.id.buttonOtroMotivo);
        editTextOtroMotivo = findViewById(R.id.editTextOtroMotivo);
        buttonRechazarRevision = findViewById(R.id.buttonRechazarRevision);
        buttonRegresarMenu = findViewById(R.id.buttonRegresarMenu);
    }

    private void setupClickListeners() {
        buttonSobranteSinFolio.setOnClickListener(v -> seleccionarMotivo("Sobrante sin folio no comprobado"));
        buttonFacturasIrregulares.setOnClickListener(v -> seleccionarMotivo("Facturas con irregularidades o no correspondientes"));
        buttonFacturasIlegibles.setOnClickListener(v -> seleccionarMotivo("Facturas subidas no legibles o no válidas"));
        buttonGastosFueraTiempo.setOnClickListener(v -> seleccionarMotivo("Gastos fuera de tiempo del viaje programado"));
        buttonDepositoIncorrecto.setOnClickListener(v -> seleccionarMotivo("Deposito de sobrante incorrecto"));

        buttonOtroMotivo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String otroTexto = editTextOtroMotivo.getText().toString().trim();
                if (!otroTexto.isEmpty()) {
                    seleccionarMotivo(otroTexto);
                } else {
                    Toast.makeText(RechazoRevisionActivity.this, "⚠️ Escriba el motivo del rechazo", Toast.LENGTH_SHORT).show();
                }
            }
        });

        buttonRechazarRevision.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (motivoSeleccionado.isEmpty()) {
                    Toast.makeText(RechazoRevisionActivity.this, "⚠️ Seleccione un motivo de rechazo", Toast.LENGTH_SHORT).show();
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
        textViewNumeroEmpleado.setText(idUsuario);
        textViewFolioViaje.setText("Folio: " + folioViaje);
    }

    private void seleccionarMotivo(String motivo) {
        motivoSeleccionado = motivo;
        limpiarSelecciones();

        if (motivo.equals("Sobrante sin folio no comprobado")) {
            buttonSobranteSinFolio.setBackgroundResource(R.drawable.orange_button_background);
        } else if (motivo.equals("Facturas con irregularidades o no correspondientes")) {
            buttonFacturasIrregulares.setBackgroundResource(R.drawable.orange_button_background);
        } else if (motivo.equals("Facturas subidas no legibles o no válidas")) {
            buttonFacturasIlegibles.setBackgroundResource(R.drawable.orange_button_background);
        } else if (motivo.equals("Gastos fuera de tiempo del viaje programado")) {
            buttonGastosFueraTiempo.setBackgroundResource(R.drawable.orange_button_background);
        } else if (motivo.equals("Deposito de sobrante incorrecto")) {
            buttonDepositoIncorrecto.setBackgroundResource(R.drawable.orange_button_background);
        } else {
            buttonOtroMotivo.setBackgroundResource(R.drawable.orange_button_background);
        }

        Toast.makeText(this, "✅ Motivo seleccionado: " + motivo, Toast.LENGTH_SHORT).show();
    }

    private void limpiarSelecciones() {
        buttonSobranteSinFolio.setBackgroundResource(R.drawable.large_gray_area);
        buttonFacturasIrregulares.setBackgroundResource(R.drawable.large_gray_area);
        buttonFacturasIlegibles.setBackgroundResource(R.drawable.large_gray_area);
        buttonGastosFueraTiempo.setBackgroundResource(R.drawable.large_gray_area);
        buttonDepositoIncorrecto.setBackgroundResource(R.drawable.large_gray_area);
        buttonOtroMotivo.setBackgroundResource(R.drawable.large_gray_area);
    }

    private void mostrarDialogoConfirmacion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("❌ Rechazar Revisión");
        builder.setMessage("¿Desea rechazar la revisión?\n\n" +
                "👤 Empleado: " + idUsuario + "\n" +
                "📋 Folio: " + folioViaje + "\n\n" +
                "📝 Motivo del rechazo:\n" + motivoSeleccionado);

        builder.setPositiveButton("✅ Sí, Rechazar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                rechazarRevision();
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

    private void rechazarRevision() {
        buttonRechazarRevision.setEnabled(false);

        ViajeDAO.rechazarViajeConMotivo(idViaje, idRevisorRH, motivoSeleccionado, new ViajeDAO.ViajeCallback() {
            @Override
            public void onSuccess(String message) {
                NotificationManager.crearNotificacionViajeRechazadoConMotivo(idUsuario, folioViaje, motivoSeleccionado);

                Toast.makeText(RechazoRevisionActivity.this,
                        "✅ Revisión rechazada correctamente\n📧 Empleado notificado",
                        Toast.LENGTH_LONG).show();
                regresarMenu();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(RechazoRevisionActivity.this,
                        "❌ Error al rechazar revisión: " + error, Toast.LENGTH_LONG).show();
                buttonRechazarRevision.setEnabled(true);
            }
        });
    }

    private void regresarMenu() {
        Intent intent = new Intent(RechazoRevisionActivity.this, AdministradorFinalActivity.class);
        startActivity(intent);
        finish();
    }
}