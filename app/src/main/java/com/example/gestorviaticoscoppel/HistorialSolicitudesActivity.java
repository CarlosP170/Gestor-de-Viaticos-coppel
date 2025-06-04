package com.example.gestorviaticoscoppel;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.gestorviaticoscoppel.dao.SolicitudDAO;
import com.example.gestorviaticoscoppel.models.Solicitud;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistorialSolicitudesActivity extends AppCompatActivity {

    private TextView buttonRegresarMenu;
    private LinearLayout historialContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial_solicitudes);

        initializeViews();
        setupClickListeners();
        cargarHistorialSolicitudes();
    }

    private void initializeViews() {
        buttonRegresarMenu = findViewById(R.id.buttonRegresarMenu);
        historialContainer = findViewById(R.id.historialContainer);

        if (historialContainer == null) {
            return;
        }
    }

    private void setupClickListeners() {
        buttonRegresarMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                regresarMenu();
            }
        });
    }

    private void cargarHistorialSolicitudes() {
        SolicitudDAO.obtenerSolicitudesHistorial(new SolicitudDAO.SolicitudesListCallback() {
            @Override
            public void onSuccess(List<Solicitud> solicitudes) {
                mostrarHistorial(solicitudes);
            }

            @Override
            public void onError(String error) {
                mostrarSinHistorial();
            }
        });
    }

    private void mostrarHistorial(List<Solicitud> solicitudes) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (historialContainer == null) {
                        return;
                    }

                    historialContainer.removeAllViews();

                    if (solicitudes.isEmpty()) {
                        mostrarSinHistorial();
                        return;
                    }

                    for (Solicitud solicitud : solicitudes) {
                        TextView solicitudButton = crearBotonSolicitud(solicitud);
                        historialContainer.addView(solicitudButton);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void mostrarSinHistorial() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (historialContainer == null) {
                        return;
                    }

                    historialContainer.removeAllViews();

                    TextView sinHistorial = new TextView(HistorialSolicitudesActivity.this);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, 150);
                    params.setMargins(0, 50, 0, 50);
                    sinHistorial.setLayoutParams(params);

                    sinHistorial.setText("No hay historial de solicitudes");
                    sinHistorial.setTextSize(20);
                    sinHistorial.setTextColor(getResources().getColor(android.R.color.darker_gray));
                    sinHistorial.setGravity(android.view.Gravity.CENTER);
                    sinHistorial.setPadding(16, 16, 16, 16);
                    sinHistorial.setBackgroundResource(R.drawable.large_gray_area);
                    sinHistorial.setElevation(4);

                    historialContainer.addView(sinHistorial);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private TextView crearBotonSolicitud(Solicitud solicitud) {
        TextView boton = new TextView(this);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 150);
        params.setMargins(0, 0, 0, 20);
        boton.setLayoutParams(params);

        String fechaFormateada = formatearFecha(solicitud.getFechaRevision());
        String folio = solicitud.getFolioViaje() != null ? solicitud.getFolioViaje() : "000000";
        String estado = solicitud.getEstado() != null ? solicitud.getEstado() : "Autorizada";

        String textoFechaFolio = fechaFormateada + " - Folio: " + folio + "\n";
        String textoCompleto = textoFechaFolio + estado;

        android.text.SpannableString spannable = new android.text.SpannableString(textoCompleto);

        int inicioEstado = textoFechaFolio.length();
        int finEstado = textoCompleto.length();

        int colorEstado;
        if ("Autorizada".equals(estado)) {
            colorEstado = 0xFF2196F3;
        } else if ("Rechazada".equals(estado)) {
            colorEstado = 0xFFF44336;
        } else {
            colorEstado = 0xFF757575;
        }

        spannable.setSpan(
                new android.text.style.ForegroundColorSpan(colorEstado),
                inicioEstado,
                finEstado,
                android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        spannable.setSpan(
                new android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                inicioEstado,
                finEstado,
                android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        boton.setText(spannable);
        boton.setTextSize(18);
        boton.setTextColor(getResources().getColor(android.R.color.black));
        boton.setGravity(android.view.Gravity.CENTER);
        boton.setPadding(16, 16, 16, 16);
        boton.setBackgroundResource(R.drawable.large_gray_area);
        boton.setElevation(4);

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

    private void regresarMenu() {
        try {
            Intent intent = new Intent(HistorialSolicitudesActivity.this, RevisionSolicitudesActivity.class);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            finish();
        }
    }
}