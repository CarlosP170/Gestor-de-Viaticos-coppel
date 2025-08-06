package com.example.gestorviaticoscoppel;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.gestorviaticoscoppel.dao.SolicitudDAO;
import com.example.gestorviaticoscoppel.dao.ViajeDAO;
import com.example.gestorviaticoscoppel.models.Viaje;
import com.example.gestorviaticoscoppel.utils.NotificationManager;
import com.example.gestorviaticoscoppel.utils.SessionManager;
import java.text.DecimalFormat;

public class AdministradorActivity extends AppCompatActivity {

    private TextView textViewNumeroEmpleado;
    private TextView textViewFolioViaje;
    private TextView textViewMontoHotel;
    private TextView textViewMontoComida;
    private TextView textViewMontoTransporte;
    private TextView textViewMontoGasolina;
    private TextView buttonAutorizarSolicitud;
    private TextView buttonRechazarSolicitud;
    private TextView buttonRegresarMenu;

    private int idSolicitud;
    private String idBeneficiario;
    private String folioViaje;
    private double montoHotel, montoComida, montoTransporte, montoGasolina;
    private String idRevisorRH;
    private DecimalFormat formatoPeso = new DecimalFormat("#,###.00");
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_administrador);

        sessionManager = new SessionManager(this);
        idRevisorRH = sessionManager.getCurrentUserId();

        obtenerDatosSolicitud();
        initializeViews();
        setupClickListeners();
        cargarDatosSolicitud();
    }

    private void obtenerDatosSolicitud() {
        Intent intent = getIntent();
        idSolicitud = intent.getIntExtra("idSolicitud", -1);
        idBeneficiario = intent.getStringExtra("idBeneficiario");
        folioViaje = intent.getStringExtra("folioViaje");
        montoHotel = intent.getDoubleExtra("montoHotel", 0.0);
        montoComida = intent.getDoubleExtra("montoComida", 0.0);
        montoTransporte = intent.getDoubleExtra("montoTransporte", 0.0);
        montoGasolina = intent.getDoubleExtra("montoGasolina", 0.0);

        if (idSolicitud == -1 || idBeneficiario == null || folioViaje == null) {
            Toast.makeText(this, "⚠️ Error: Datos de solicitud incompletos", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initializeViews() {
        textViewNumeroEmpleado = findViewById(R.id.textViewNumeroEmpleado);
        textViewFolioViaje = findViewById(R.id.textViewFolioViaje);
        textViewMontoHotel = findViewById(R.id.textViewMontoHotel);
        textViewMontoComida = findViewById(R.id.textViewMontoComida);
        textViewMontoTransporte = findViewById(R.id.textViewMontoTransporte);
        textViewMontoGasolina = findViewById(R.id.textViewMontoGasolina);
        buttonAutorizarSolicitud = findViewById(R.id.buttonAutorizarSolicitud);
        buttonRechazarSolicitud = findViewById(R.id.buttonRechazarSolicitud);
        buttonRegresarMenu = findViewById(R.id.buttonRegresarMenu);
    }

    private void setupClickListeners() {
        buttonAutorizarSolicitud.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarDialogoAutorizar();
            }
        });

        buttonRechazarSolicitud.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navegarARechazoSolicitud();
            }
        });

        buttonRegresarMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                regresarMenu();
            }
        });
    }

    private void cargarDatosSolicitud() {
        textViewNumeroEmpleado.setText(idBeneficiario);
        textViewFolioViaje.setText("Folio: " + folioViaje);
        textViewMontoHotel.setText("$ " + formatoPeso.format(montoHotel));
        textViewMontoComida.setText("$ " + formatoPeso.format(montoComida));
        textViewMontoTransporte.setText("$ " + formatoPeso.format(montoTransporte));
        textViewMontoGasolina.setText("$ " + formatoPeso.format(montoGasolina));
    }

    private void mostrarDialogoAutorizar() {
        double montoTotal = montoHotel + montoComida + montoTransporte + montoGasolina;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("✅ Autorizar Solicitud");
        builder.setMessage("¿Desea autorizar la solicitud?\n\n" +
                "👤 Empleado: " + idBeneficiario + "\n" +
                "📋 Folio: " + folioViaje + "\n" +
                "💰 Monto Total: $" + formatoPeso.format(montoTotal));

        builder.setPositiveButton("✅ Sí", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                autorizarSolicitud();
            }
        });

        builder.setNegativeButton("❌ No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void navegarARechazoSolicitud() {
        Intent intent = new Intent(AdministradorActivity.this, RechazoSolicitudActivity.class);
        intent.putExtra("idSolicitud", idSolicitud);
        intent.putExtra("idBeneficiario", idBeneficiario);
        intent.putExtra("folioViaje", folioViaje);
        intent.putExtra("idRevisorRH", idRevisorRH);
        startActivity(intent);
        finish();
    }

    private void autorizarSolicitud() {
        buttonAutorizarSolicitud.setEnabled(false);
        buttonRechazarSolicitud.setEnabled(false);

        SolicitudDAO.autorizarSolicitud(idSolicitud, idRevisorRH, new SolicitudDAO.SolicitudCallback() {
            @Override
            public void onSuccess(String message) {
                NotificationManager.crearNotificacionViaticosAutorizados(idBeneficiario, folioViaje);
                crearViajeParaBeneficiario();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(AdministradorActivity.this,
                        "❌ Error al autorizar solicitud: " + error, Toast.LENGTH_LONG).show();
                buttonAutorizarSolicitud.setEnabled(true);
                buttonRechazarSolicitud.setEnabled(true);
            }
        });
    }

    private void crearViajeParaBeneficiario() {
        ViajeDAO.crearViajeDesdeAutorizacion(idSolicitud, new ViajeDAO.ViajeSingleCallback() {
            @Override
            public void onSuccess(Viaje viaje) {
                Toast.makeText(AdministradorActivity.this,
                        "✅ Solicitud autorizada correctamente\n📧 Notificación enviada al empleado",
                        Toast.LENGTH_LONG).show();
                regresarMenu();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(AdministradorActivity.this,
                        "⚠️ Solicitud autorizada pero error al crear viaje: " + error,
                        Toast.LENGTH_LONG).show();
                regresarMenu();
            }
        });
    }

    private void regresarMenu() {
        Intent intent = new Intent(AdministradorActivity.this, RevisionSolicitudesActivity.class);
        startActivity(intent);
        finish();
    }
}