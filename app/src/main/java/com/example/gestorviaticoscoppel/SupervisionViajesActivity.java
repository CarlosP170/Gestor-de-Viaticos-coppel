package com.example.gestorviaticoscoppel;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.gestorviaticoscoppel.dao.ViajeDAO;
import com.example.gestorviaticoscoppel.models.Viaje;
import com.example.gestorviaticoscoppel.utils.NotificationManager;
import com.example.gestorviaticoscoppel.utils.SessionManager;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SupervisionViajesActivity extends AppCompatActivity {

    private TextView buttonRegresarMenu;
    private TextView buttonHistorialViajes;
    private LinearLayout mainLayout;
    private String idUsuarioRH;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supervision_viajes);

        sessionManager = new SessionManager(this);
        idUsuarioRH = sessionManager.getCurrentUserId();

        initializeViews();
        setupClickListeners();
        verificarYActualizarNotificaciones();
        cargarViajesEnRevision();
    }

    @Override
    protected void onResume() {
        super.onResume();
        verificarYActualizarNotificaciones();
        cargarViajesEnRevision();
    }

    private void initializeViews() {
        buttonRegresarMenu = findViewById(R.id.buttonRegresarMenu);
        buttonHistorialViajes = findViewById(R.id.buttonHistorialViajes);
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

        buttonHistorialViajes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SupervisionViajesActivity.this, HistorialViajesRHActivity.class);
                startActivity(intent);
            }
        });
    }

    private void cargarViajesEnRevision() {
        ViajeDAO.obtenerViajesEnRevision(new ViajeDAO.ViajeListCallback() {
            @Override
            public void onSuccess(List<Viaje> viajes) {
                mostrarViajes(viajes);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(SupervisionViajesActivity.this,
                        "❌ Error al cargar viajes: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void mostrarViajes(List<Viaje> viajes) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mainLayout == null) {
                        Toast.makeText(SupervisionViajesActivity.this,
                                "⚠️ Error: No se pudo encontrar el layout principal",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    limpiarViajesAnteriores();

                    if (viajes.isEmpty()) {
                        ajustarPosicionBotones(0);
                        Toast.makeText(SupervisionViajesActivity.this,
                                "📭 No hay viajes pendientes de revisión", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int insertIndex = mainLayout.indexOfChild(buttonHistorialViajes);

                    for (int i = 0; i < viajes.size(); i++) {
                        Viaje viaje = viajes.get(i);
                        TextView botonViaje = crearBotonViaje(viaje);
                        mainLayout.addView(botonViaje, insertIndex + i);
                    }

                    ajustarPosicionBotones(viajes.size());

                    Toast.makeText(SupervisionViajesActivity.this,
                            "✅ " + viajes.size() + " viajes cargados",
                            Toast.LENGTH_SHORT).show();

                } catch (Exception e) {
                    Toast.makeText(SupervisionViajesActivity.this,
                            "❌ Error mostrando viajes: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void limpiarViajesAnteriores() {
        if (mainLayout == null) return;

        for (int i = mainLayout.getChildCount() - 1; i >= 0; i--) {
            View child = mainLayout.getChildAt(i);
            if (child instanceof TextView && child != buttonRegresarMenu && child != buttonHistorialViajes) {
                TextView tv = (TextView) child;
                String text = tv.getText().toString();
                if (text.contains("Folio:")) {
                    mainLayout.removeView(child);
                }
            }
        }
    }

    private void ajustarPosicionBotones(int cantidadViajes) {
        LinearLayout.LayoutParams paramsHistorial = (LinearLayout.LayoutParams) buttonHistorialViajes.getLayoutParams();
        LinearLayout.LayoutParams paramsRegresar = (LinearLayout.LayoutParams) buttonRegresarMenu.getLayoutParams();

        if (cantidadViajes == 0) {
            paramsHistorial.topMargin = 200;
            paramsRegresar.topMargin = 16;
        } else {
            paramsHistorial.topMargin = 30;
            paramsRegresar.topMargin = 16;
        }

        buttonHistorialViajes.setLayoutParams(paramsHistorial);
        buttonRegresarMenu.setLayoutParams(paramsRegresar);
    }

    private TextView crearBotonViaje(Viaje viaje) {
        TextView boton = new TextView(this);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 160);
        params.setMargins(0, 0, 0, 28);
        boton.setLayoutParams(params);

        String fechaFormateada = formatearFecha(viaje.getFechaEnvioRevision());
        String textoBoton = "📅 " + fechaFormateada + " - 📋 Folio: " + viaje.getFolioViaje() + " - 👤 " + viaje.getIdUsuario();
        boton.setText(textoBoton);

        boton.setTextSize(18);
        boton.setTextColor(getResources().getColor(android.R.color.white));
        boton.setGravity(android.view.Gravity.CENTER);
        boton.setPadding(20, 20, 20, 20);
        boton.setBackgroundResource(R.drawable.green_title_background);
        boton.setElevation(8);

        boton.setClickable(true);
        boton.setFocusable(true);
        boton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirAdministradorFinal(viaje);
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

    private void abrirAdministradorFinal(Viaje viaje) {
        Intent intent = new Intent(SupervisionViajesActivity.this, AdministradorFinalActivity.class);
        intent.putExtra("idViaje", viaje.getIdViaje());
        intent.putExtra("idUsuario", viaje.getIdUsuario());
        intent.putExtra("folioViaje", viaje.getFolioViaje());
        intent.putExtra("montoHotelAutorizado", viaje.getMontoHotelAutorizado());
        intent.putExtra("montoComidaAutorizado", viaje.getMontoComidaAutorizado());
        intent.putExtra("montoTransporteAutorizado", viaje.getMontoTransporteAutorizado());
        intent.putExtra("montoGasolinaAutorizado", viaje.getMontoGasolinaAutorizado());
        intent.putExtra("montoHotelGastado", viaje.getMontoHotelGastado());
        intent.putExtra("montoComidaGastado", viaje.getMontoComidaGastado());
        intent.putExtra("montoTransporteGastado", viaje.getMontoTransporteGastado());
        intent.putExtra("montoGasolinaGastado", viaje.getMontoGasolinaGastado());
        intent.putExtra("montoOtrosGastado", viaje.getMontoOtrosGastado());
        intent.putExtra("folioTicketSobrante", viaje.getFolioTicketSobrante());
        intent.putExtra("saldoRestante", viaje.getSaldoRestante());
        startActivity(intent);
    }

    private void regresarMenu() {
        Intent intent = new Intent(SupervisionViajesActivity.this, AdministradorRHActivity.class);
        startActivity(intent);
        finish();
    }
}