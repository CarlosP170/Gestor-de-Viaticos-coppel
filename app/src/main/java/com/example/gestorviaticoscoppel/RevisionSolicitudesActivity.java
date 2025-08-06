package com.example.gestorviaticoscoppel;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.gestorviaticoscoppel.dao.SolicitudDAO;
import com.example.gestorviaticoscoppel.models.Solicitud;
import com.example.gestorviaticoscoppel.utils.NotificationManager;
import com.example.gestorviaticoscoppel.utils.SessionManager;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RevisionSolicitudesActivity extends AppCompatActivity {

    private TextView buttonRegresarMenu;
    private TextView buttonHistorialSolicitudes;
    private LinearLayout mainLayout;
    private String idUsuarioRH;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_revision_solicitudes);

        sessionManager = new SessionManager(this);
        idUsuarioRH = sessionManager.getCurrentUserId();

        initializeViews();
        setupClickListeners();
        verificarYActualizarNotificaciones();
        cargarSolicitudesPendientes();
    }

    @Override
    protected void onResume() {
        super.onResume();
        verificarYActualizarNotificaciones();
        cargarSolicitudesPendientes();
    }

    private void initializeViews() {
        buttonRegresarMenu = findViewById(R.id.buttonRegresarMenu);
        buttonHistorialSolicitudes = findViewById(R.id.buttonHistorialSolicitudes);
        mainLayout = buscarLinearLayoutPrincipal();
    }

    private void verificarYActualizarNotificaciones() {
        NotificationManager.verificarYCrearNotificacionesRH(idUsuarioRH);
    }

    private LinearLayout buscarLinearLayoutPrincipal() {
        View rootView = findViewById(android.R.id.content);
        return buscarLinearLayoutRecursivo(rootView);
    }

    private LinearLayout buscarLinearLayoutRecursivo(View view) {
        if (view instanceof LinearLayout) {
            LinearLayout layout = (LinearLayout) view;
            for (int i = 0; i < layout.getChildCount(); i++) {
                if (layout.getChildAt(i) == buttonRegresarMenu) {
                    return layout;
                }
            }
        }

        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                LinearLayout result = buscarLinearLayoutRecursivo(group.getChildAt(i));
                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }

    private void setupClickListeners() {
        buttonRegresarMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                regresarMenu();
            }
        });

        buttonHistorialSolicitudes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RevisionSolicitudesActivity.this, HistorialSolicitudesActivity.class);
                startActivity(intent);
            }
        });
    }

    private void cargarSolicitudesPendientes() {
        SolicitudDAO.obtenerSolicitudesPendientes(new SolicitudDAO.SolicitudesListCallback() {
            @Override
            public void onSuccess(List<Solicitud> solicitudes) {
                mostrarSolicitudes(solicitudes);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(RevisionSolicitudesActivity.this,
                        "❌ Error al cargar solicitudes: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void mostrarSolicitudes(List<Solicitud> solicitudes) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mainLayout == null) {
                        Toast.makeText(RevisionSolicitudesActivity.this,
                                "⚠️ Error: No se pudo encontrar el layout principal",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    limpiarSolicitudesAnteriores();

                    if (solicitudes.isEmpty()) {
                        ajustarPosicionBotones(0);
                        Toast.makeText(RevisionSolicitudesActivity.this,
                                "📭 No hay solicitudes pendientes", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int insertIndex = mainLayout.indexOfChild(buttonHistorialSolicitudes);

                    for (int i = 0; i < solicitudes.size(); i++) {
                        Solicitud solicitud = solicitudes.get(i);
                        TextView botonSolicitud = crearBotonSolicitud(solicitud);
                        mainLayout.addView(botonSolicitud, insertIndex + i);
                    }

                    ajustarPosicionBotones(solicitudes.size());

                    Toast.makeText(RevisionSolicitudesActivity.this,
                            "✅ " + solicitudes.size() + " solicitudes cargadas",
                            Toast.LENGTH_SHORT).show();

                } catch (Exception e) {
                    Toast.makeText(RevisionSolicitudesActivity.this,
                            "❌ Error mostrando solicitudes: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void limpiarSolicitudesAnteriores() {
        if (mainLayout == null) return;

        for (int i = mainLayout.getChildCount() - 1; i >= 0; i--) {
            View child = mainLayout.getChildAt(i);
            if (child instanceof TextView && child != buttonRegresarMenu && child != buttonHistorialSolicitudes) {
                TextView tv = (TextView) child;
                String text = tv.getText().toString();
                if (text.contains("Folio:")) {
                    mainLayout.removeView(child);
                }
            }
        }
    }

    private void ajustarPosicionBotones(int cantidadSolicitudes) {
        LinearLayout.LayoutParams paramsHistorial = (LinearLayout.LayoutParams) buttonHistorialSolicitudes.getLayoutParams();
        LinearLayout.LayoutParams paramsRegresar = (LinearLayout.LayoutParams) buttonRegresarMenu.getLayoutParams();

        if (cantidadSolicitudes == 0) {
            paramsHistorial.topMargin = 200;
            paramsRegresar.topMargin = 16;
        } else {
            paramsHistorial.topMargin = 30;
            paramsRegresar.topMargin = 16;
        }

        buttonHistorialSolicitudes.setLayoutParams(paramsHistorial);
        buttonRegresarMenu.setLayoutParams(paramsRegresar);
    }

    private TextView crearBotonSolicitud(Solicitud solicitud) {
        TextView boton = new TextView(this);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 160);
        params.setMargins(0, 0, 0, 28);
        boton.setLayoutParams(params);

        String fechaFormateada = formatearFecha(solicitud.getFechaSolicitud());
        String textoBoton = "📅 " + fechaFormateada + " - 📋 Folio: " + solicitud.getFolioViaje() + " - 👤 " + solicitud.getIdBeneficiario();
        boton.setText(textoBoton);

        boton.setTextSize(18);
        boton.setTextColor(getResources().getColor(android.R.color.white));
        boton.setGravity(android.view.Gravity.CENTER);
        boton.setPadding(20, 20, 20, 20);
        boton.setBackgroundResource(R.drawable.cylan_button_background);
        boton.setElevation(8);

        boton.setClickable(true);
        boton.setFocusable(true);
        boton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirAdministradorSolicitud(solicitud);
            }
        });

        return boton;
    }

    private String formatearFecha(String fechaOriginal) {
        try {
            if (fechaOriginal == null || fechaOriginal.isEmpty()) {
                return getCurrentDate();
            }

            SimpleDateFormat formatoOriginal = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat formatoMostrar = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

            Date fecha = formatoOriginal.parse(fechaOriginal.substring(0, 10));
            return formatoMostrar.format(fecha);

        } catch (Exception e) {
            return getCurrentDate();
        }
    }

    private String getCurrentDate() {
        SimpleDateFormat formatoMostrar = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return formatoMostrar.format(new Date());
    }

    private void abrirAdministradorSolicitud(Solicitud solicitud) {
        Intent intent = new Intent(RevisionSolicitudesActivity.this, AdministradorActivity.class);
        intent.putExtra("idSolicitud", solicitud.getIdSolicitud());
        intent.putExtra("idBeneficiario", solicitud.getIdBeneficiario());
        intent.putExtra("folioViaje", solicitud.getFolioViaje());
        intent.putExtra("montoHotel", solicitud.getMontoHotel());
        intent.putExtra("montoComida", solicitud.getMontoComida());
        intent.putExtra("montoTransporte", solicitud.getMontoTransporte());
        intent.putExtra("montoGasolina", solicitud.getMontoGasolina());
        startActivity(intent);
    }

    private void regresarMenu() {
        Intent intent = new Intent(RevisionSolicitudesActivity.this, AdministradorRHActivity.class);
        startActivity(intent);
        finish();
    }
}