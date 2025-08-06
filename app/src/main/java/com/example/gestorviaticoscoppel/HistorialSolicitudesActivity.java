package com.example.gestorviaticoscoppel;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
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
                    // Error silencioso
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
                            LinearLayout.LayoutParams.MATCH_PARENT, 160);
                    params.setMargins(0, 60, 0, 60);
                    sinHistorial.setLayoutParams(params);

                    sinHistorial.setText("📭 No hay historial de solicitudes disponible");
                    sinHistorial.setTextSize(18);
                    sinHistorial.setTextColor(ContextCompat.getColor(HistorialSolicitudesActivity.this, android.R.color.black));
                    sinHistorial.setGravity(android.view.Gravity.CENTER);
                    sinHistorial.setPadding(20, 20, 20, 20);
                    sinHistorial.setBackgroundResource(R.drawable.large_gray_area);
                    sinHistorial.setElevation(6);

                    historialContainer.addView(sinHistorial);
                } catch (Exception e) {
                    // Error silencioso
                }
            }
        });
    }

    private TextView crearBotonSolicitud(Solicitud solicitud) {
        TextView boton = new TextView(this);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0,24);
        boton.setLayoutParams(params);

        String fechaFormateada = formatearFecha(solicitud.getFechaRevision());
        String folio = solicitud.getFolioViaje() != null ? solicitud.getFolioViaje() : "000000";
        String estado = solicitud.getEstado() != null ? solicitud.getEstado() : "Autorizada";

        String emojiEstado = "";
        String estadoFormateado = "";

        switch (estado) {
            case "Autorizada":
                emojiEstado = "✅";
                estadoFormateado = "Autorizada";
                break;
            case "Rechazada":
                emojiEstado = "❌";
                estadoFormateado = "Rechazada";
                break;
            default:
                emojiEstado = "📊";
                estadoFormateado = estado;
                break;
        }

        String textoFechaFolio = "📅 " + fechaFormateada + " - 🎫 Folio: " + folio + "\n";
        String textoCompleto = textoFechaFolio + emojiEstado + " " + estadoFormateado;

        if ("Rechazada".equals(estado) && solicitud.getMotivoRechazo() != null && !solicitud.getMotivoRechazo().trim().isEmpty()) {
            textoCompleto += "\n💬 Motivo: " + solicitud.getMotivoRechazo();
        }

        android.text.SpannableString spannable = new android.text.SpannableString(textoCompleto);

        int inicioEstado = textoFechaFolio.length();
        int finEstado = textoFechaFolio.length() + emojiEstado.length() + 1 + estadoFormateado.length();

        int colorEstado;
        if ("Autorizada".equals(estado)) {
            colorEstado = 0xFF4CAF50;
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

        if ("Rechazada".equals(estado) && solicitud.getMotivoRechazo() != null && !solicitud.getMotivoRechazo().trim().isEmpty()) {
            int inicioMotivo = finEstado + 1;
            spannable.setSpan(
                    new android.text.style.ForegroundColorSpan(0xFF616161),
                    inicioMotivo,
                    textoCompleto.length(),
                    android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }

        boton.setText(spannable);
        boton.setTextSize(17);
        boton.setTextColor(ContextCompat.getColor(this, android.R.color.black));
        boton.setGravity(android.view.Gravity.CENTER);
        boton.setPadding(20, 20, 20, 20);
        boton.setBackgroundResource(R.drawable.large_gray_area);
        boton.setElevation(6);
        boton.setMinHeight(160);
        boton.setLineSpacing(4, 1.0f);

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