package com.example.gestorviaticoscoppel;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.gestorviaticoscoppel.dao.ViajeDAO;
import com.example.gestorviaticoscoppel.models.Viaje;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistorialViajesActivity extends AppCompatActivity {

    private TextView buttonRegresarMenu;
    private LinearLayout historialContainer;
    private String idUsuario;
    private String origen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial_viajes);

        obtenerDatosIntent();
        initializeViews();
        setupClickListeners();
        cargarHistorialViajes();
    }

    private void obtenerDatosIntent() {
        idUsuario = getIntent().getStringExtra("idUsuario");
        origen = getIntent().getStringExtra("origen");

        if (idUsuario == null) {
            idUsuario = "90126701";
        }
        if (origen == null) {
            origen = "colaborador";
        }
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

    private void cargarHistorialViajes() {
        ViajeDAO.obtenerViajesFinalizados(idUsuario, new ViajeDAO.ViajeListCallback() {
            @Override
            public void onSuccess(List<Viaje> viajes) {
                mostrarHistorial(viajes);
            }

            @Override
            public void onError(String error) {
                mostrarSinHistorial();
            }
        });
    }

    private void mostrarHistorial(List<Viaje> viajes) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (historialContainer == null) {
                        return;
                    }

                    historialContainer.removeAllViews();

                    if (viajes.isEmpty()) {
                        mostrarSinHistorial();
                        return;
                    }

                    for (Viaje viaje : viajes) {
                        TextView viajeButton = crearBotonViaje(viaje);
                        historialContainer.addView(viajeButton);
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

                    TextView sinHistorial = new TextView(HistorialViajesActivity.this);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, 150);
                    params.setMargins(0, 50, 0, 50);
                    sinHistorial.setLayoutParams(params);

                    sinHistorial.setText("No hay historial de viajes");
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

    private TextView crearBotonViaje(Viaje viaje) {
        TextView boton = new TextView(this);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 150);
        params.setMargins(0, 0, 0, 20);
        boton.setLayoutParams(params);

        String fechaFormateada = formatearFecha(viaje.getFechaFinalizacion());
        String folio = viaje.getFolioViaje() != null ? viaje.getFolioViaje() : "000000";
        String estado = viaje.getEstado() != null ? viaje.getEstado() : "Finalizado";

        String textoFechaFolio = fechaFormateada + " - Folio: " + folio + "\n";
        String textoCompleto = textoFechaFolio + estado;

        android.text.SpannableString spannable = new android.text.SpannableString(textoCompleto);

        int inicioEstado = textoFechaFolio.length();
        int finEstado = textoCompleto.length();

        int colorEstado;
        if ("Finalizado".equals(estado)) {
            colorEstado = 0xFF2196F3;
        } else if ("Rechazado".equals(estado)) {
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
            if ("gerente".equals(origen)) {
                Intent intent = new Intent(HistorialViajesActivity.this, GestorPersonalActivity.class);
                startActivity(intent);
            } else {
                Intent intent = new Intent(HistorialViajesActivity.this, GestorColaboradorActivity.class);
                startActivity(intent);
            }
            finish();
        } catch (Exception e) {
            finish();
        }
    }
}