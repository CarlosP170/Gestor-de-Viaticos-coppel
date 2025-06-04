package com.example.gestorviaticoscoppel;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.gestorviaticoscoppel.dao.ViajeDAO;
import com.example.gestorviaticoscoppel.models.Viaje;
import java.text.DecimalFormat;

public class ViaticosViajeActivity extends AppCompatActivity {

    private TextView textViewTitle;
    private TextView textViewSubtitle;
    private ImageView imageHotel;
    private ImageView imageComida;
    private ImageView imageTransporte;
    private ImageView imageGasolina;
    private TextView textMonto1;
    private TextView textMonto2;
    private TextView textMonto3;
    private TextView textMonto4;
    private TextView buttonSubirFacturas;
    private TextView buttonConsultarEstatus;
    private TextView buttonRegresarMenu;

    private String origen;
    private int idViaje;
    private Viaje viajeActual;
    private DecimalFormat formatoPeso = new DecimalFormat("#,###.00");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viaticos_viaje);

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
        textViewTitle = findViewById(R.id.textViewTitle);
        textViewSubtitle = findViewById(R.id.textViewSubtitle);
        imageHotel = findViewById(R.id.imageHotel);
        imageComida = findViewById(R.id.imageComida);
        imageTransporte = findViewById(R.id.imageTransporte);
        imageGasolina = findViewById(R.id.imageGasolina);
        textMonto1 = findViewById(R.id.textMonto1);
        textMonto2 = findViewById(R.id.textMonto2);
        textMonto3 = findViewById(R.id.textMonto3);
        textMonto4 = findViewById(R.id.textMonto4);
        buttonSubirFacturas = findViewById(R.id.buttonSubirFacturas);
        buttonConsultarEstatus = findViewById(R.id.buttonConsultarEstatus);
        buttonRegresarMenu = findViewById(R.id.buttonRegresarMenu);
    }

    private void setupClickListeners() {
        buttonSubirFacturas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (viajeActual != null) {
                    Intent intent = new Intent(ViaticosViajeActivity.this, SubirFacturasActivity.class);
                    intent.putExtra("origen", origen);
                    intent.putExtra("idViaje", idViaje);
                    startActivity(intent);
                } else {
                    Toast.makeText(ViaticosViajeActivity.this,
                            "Error: No se han cargado los datos del viaje", Toast.LENGTH_SHORT).show();
                }
            }
        });

        buttonConsultarEstatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (viajeActual != null) {
                    Intent intent = new Intent(ViaticosViajeActivity.this, ConsultarEstatusActivity.class);
                    intent.putExtra("origen", origen);
                    intent.putExtra("idViaje", idViaje);
                    startActivity(intent);
                } else {
                    Toast.makeText(ViaticosViajeActivity.this,
                            "Error: No se han cargado los datos del viaje", Toast.LENGTH_SHORT).show();
                }
            }
        });

        buttonRegresarMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                regresarMenuAnterior();
            }
        });
    }

    private void cargarDatosViaje() {
        ViajeDAO.obtenerViajeActivo(getCurrentUserId(), new ViajeDAO.ViajeSingleCallback() {
            @Override
            public void onSuccess(Viaje viaje) {
                if (viaje.getIdViaje() == idViaje) {
                    viajeActual = viaje;
                    runOnUiThread(() -> actualizarInterfaz());
                } else {
                    Toast.makeText(ViaticosViajeActivity.this,
                            "Error: El viaje no coincide", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(ViaticosViajeActivity.this,
                        "Error al cargar el viaje: " + error, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void actualizarInterfaz() {
        if (viajeActual == null) return;

        textViewTitle.setText("Viáticos de Viaje - " + viajeActual.getFolioViaje());

        double saldoHotel = viajeActual.getMontoHotelAutorizado() - viajeActual.getMontoHotelGastado();
        double saldoComida = viajeActual.getMontoComidaAutorizado() - viajeActual.getMontoComidaGastado();
        double saldoTransporte = viajeActual.getMontoTransporteAutorizado() - viajeActual.getMontoTransporteGastado();
        double saldoGasolina = viajeActual.getMontoGasolinaAutorizado() - viajeActual.getMontoGasolinaGastado();

        if (saldoHotel < 0) saldoHotel = 0;
        if (saldoComida < 0) saldoComida = 0;
        if (saldoTransporte < 0) saldoTransporte = 0;
        if (saldoGasolina < 0) saldoGasolina = 0;

        textMonto1.setText("$ " + formatoPeso.format(saldoHotel));
        textMonto2.setText("$ " + formatoPeso.format(saldoComida));
        textMonto3.setText("$ " + formatoPeso.format(saldoTransporte));
        textMonto4.setText("$ " + formatoPeso.format(saldoGasolina));

        actualizarEstadoBotones();
    }

    private void actualizarEstadoBotones() {
        if (viajeActual == null) return;

        String estado = viajeActual.getEstado();

        if ("En_Curso".equals(estado)) {
            buttonSubirFacturas.setEnabled(true);
            buttonSubirFacturas.setAlpha(1.0f);
            buttonConsultarEstatus.setEnabled(true);
            buttonConsultarEstatus.setAlpha(1.0f);
        } else {
            buttonSubirFacturas.setEnabled(false);
            buttonSubirFacturas.setAlpha(0.5f);
            buttonConsultarEstatus.setEnabled(false);
            buttonConsultarEstatus.setAlpha(0.5f);

            Toast.makeText(this, "El viaje está en estado: " + estado, Toast.LENGTH_LONG).show();
        }
    }

    private String getCurrentUserId() {
        if ("gerente".equals(origen)) {
            return "90313132";
        }
        return "90126701";
    }

    private void regresarMenuAnterior() {
        if ("gerente".equals(origen)) {
            Intent intent = new Intent(ViaticosViajeActivity.this, GestorPersonalActivity.class);
            startActivity(intent);
            finish();
        } else {
            Intent intent = new Intent(ViaticosViajeActivity.this, GestorColaboradorActivity.class);
            startActivity(intent);
            finish();
        }
    }
}