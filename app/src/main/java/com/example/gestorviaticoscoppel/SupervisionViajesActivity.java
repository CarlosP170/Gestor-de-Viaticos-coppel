package com.example.gestorviaticoscoppel;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.gestorviaticoscoppel.dao.ViajeDAO;
import com.example.gestorviaticoscoppel.models.Viaje;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SupervisionViajesActivity extends AppCompatActivity {

    private TextView buttonRegresarMenu;
    private TextView buttonHistorialViajes;
    private LinearLayout mainLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supervision_viajes);

        initializeViews();
        setupClickListeners();
        cargarViajesEnRevision();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarViajesEnRevision();
    }

    private void initializeViews() {
        buttonRegresarMenu = findViewById(R.id.buttonRegresarMenu);
        buttonHistorialViajes = findViewById(R.id.buttonHistorialViajes);

        mainLayout = buscarLinearLayoutPrincipal();

        Log.d("SUPERVISION_DEBUG", "Views inicializados correctamente");
        Log.d("SUPERVISION_DEBUG", "MainLayout found: " + (mainLayout != null));
        if (mainLayout != null) {
            Log.d("SUPERVISION_DEBUG", "MainLayout children: " + mainLayout.getChildCount());
        }
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
        Log.d("SUPERVISION_DEBUG", "=== CARGANDO VIAJES EN REVISION ===");

        ViajeDAO.obtenerViajesEnRevision(new ViajeDAO.ViajeListCallback() {
            @Override
            public void onSuccess(List<Viaje> viajes) {
                Log.d("SUPERVISION_DEBUG", "SUCCESS - Viajes encontrados: " + viajes.size());
                mostrarViajes(viajes);
            }

            @Override
            public void onError(String error) {
                Log.e("SUPERVISION_DEBUG", "ERROR: " + error);
                Toast.makeText(SupervisionViajesActivity.this,
                        "Error al cargar viajes: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void mostrarViajes(List<Viaje> viajes) {
        Log.d("SUPERVISION_DEBUG", "=== MOSTRANDO VIAJES ===");
        Log.d("SUPERVISION_DEBUG", "Cantidad a mostrar: " + viajes.size());

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mainLayout == null) {
                        Log.e("SUPERVISION_DEBUG", "MainLayout es null, no se pueden agregar botones");
                        Toast.makeText(SupervisionViajesActivity.this,
                                "Error: No se pudo encontrar el layout principal",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    limpiarViajesAnteriores();

                    if (viajes.isEmpty()) {
                        Log.d("SUPERVISION_DEBUG", "No hay viajes para mostrar");
                        ajustarPosicionBotones(0);
                        return;
                    }

                    int insertIndex = mainLayout.indexOfChild(buttonHistorialViajes);
                    Log.d("SUPERVISION_DEBUG", "Índice de inserción: " + insertIndex);

                    for (int i = 0; i < viajes.size(); i++) {
                        Viaje viaje = viajes.get(i);
                        TextView botonViaje = crearBotonViaje(viaje);

                        mainLayout.addView(botonViaje, insertIndex + i);
                        Log.d("SUPERVISION_DEBUG", "Agregado botón: " + viaje.getFolioViaje() + " en posición " + (insertIndex + i));
                    }

                    ajustarPosicionBotones(viajes.size());

                    Log.d("SUPERVISION_DEBUG", "TODOS los botones agregados exitosamente");
                    Toast.makeText(SupervisionViajesActivity.this,
                            viajes.size() + " viajes cargados",
                            Toast.LENGTH_SHORT).show();

                } catch (Exception e) {
                    Log.e("SUPERVISION_DEBUG", "Error mostrando viajes: " + e.getMessage());
                    e.printStackTrace();
                    Toast.makeText(SupervisionViajesActivity.this,
                            "Error mostrando viajes: " + e.getMessage(),
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
                    Log.d("SUPERVISION_DEBUG", "Removido botón anterior: " + text);
                }
            }
        }
    }

    private void ajustarPosicionBotones(int cantidadViajes) {
        LinearLayout.LayoutParams paramsHistorial = (LinearLayout.LayoutParams) buttonHistorialViajes.getLayoutParams();
        LinearLayout.LayoutParams paramsRegresar = (LinearLayout.LayoutParams) buttonRegresarMenu.getLayoutParams();

        if (cantidadViajes == 0) {
            paramsHistorial.topMargin = 200;
            paramsRegresar.topMargin = 12;
        } else {
            paramsHistorial.topMargin = 30;
            paramsRegresar.topMargin = 12;
        }

        buttonHistorialViajes.setLayoutParams(paramsHistorial);
        buttonRegresarMenu.setLayoutParams(paramsRegresar);
    }

    private TextView crearBotonViaje(Viaje viaje) {
        TextView boton = new TextView(this);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 150);
        params.setMargins(0, 0, 0, 24);
        boton.setLayoutParams(params);

        String fechaFormateada = formatearFecha(viaje.getFechaEnvioRevision());
        String textoBoton = fechaFormateada + " - Folio: " + viaje.getFolioViaje() + " - " + viaje.getIdUsuario();
        boton.setText(textoBoton);

        boton.setTextSize(18);
        boton.setTextColor(getResources().getColor(android.R.color.white));
        boton.setGravity(android.view.Gravity.CENTER);
        boton.setPadding(16, 16, 16, 16);
        boton.setBackgroundResource(R.drawable.green_title_background);
        boton.setElevation(12);

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