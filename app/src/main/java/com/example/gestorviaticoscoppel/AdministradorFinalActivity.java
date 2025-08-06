package com.example.gestorviaticoscoppel;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.example.gestorviaticoscoppel.dao.ViajeDAO;
import com.example.gestorviaticoscoppel.utils.NotificationManager;
import com.example.gestorviaticoscoppel.utils.SessionManager;
import java.text.DecimalFormat;

public class AdministradorFinalActivity extends AppCompatActivity {

    private TextView textViewNumeroEmpleado;
    private TextView textViewFolioViaje;
    private TextView textViewMontoHotel;
    private TextView textViewMontoComida;
    private TextView textViewMontoTransporte;
    private TextView textViewMontoGasolina;
    private TextView textViewMontoOtros;
    private TextView buttonFolioTicket;
    private ImageView imagenFacturas;
    private TextView buttonFinalizarViaje;
    private TextView buttonRechazarRevision;
    private TextView buttonRegresarMenu;

    private int idViaje;
    private String idUsuario;
    private String folioViaje;
    private double saldoRestante;
    private String folioTicketSobrante;
    private String idRevisorRH;
    private DecimalFormat formatoPeso = new DecimalFormat("#,###.00");
    private SessionManager sessionManager;

    private double hotelAutorizado, comidaAutorizado, transporteAutorizado, gasolinaAutorizado;
    private double hotelGastado, comidaGastado, transporteGastado, gasolinaGastado, otrosGastado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_administrador_final);

        sessionManager = new SessionManager(this);
        idRevisorRH = sessionManager.getCurrentUserId();

        obtenerDatosViaje();
        initializeViews();
        setupClickListeners();
        cargarDatosViaje();
        validarSaldos();
    }

    private void obtenerDatosViaje() {
        Intent intent = getIntent();
        idViaje = intent.getIntExtra("idViaje", -1);
        idUsuario = intent.getStringExtra("idUsuario");
        folioViaje = intent.getStringExtra("folioViaje");
        saldoRestante = intent.getDoubleExtra("saldoRestante", 0.0);
        folioTicketSobrante = intent.getStringExtra("folioTicketSobrante");

        hotelAutorizado = intent.getDoubleExtra("montoHotelAutorizado", 0.0);
        comidaAutorizado = intent.getDoubleExtra("montoComidaAutorizado", 0.0);
        transporteAutorizado = intent.getDoubleExtra("montoTransporteAutorizado", 0.0);
        gasolinaAutorizado = intent.getDoubleExtra("montoGasolinaAutorizado", 0.0);

        hotelGastado = intent.getDoubleExtra("montoHotelGastado", 0.0);
        comidaGastado = intent.getDoubleExtra("montoComidaGastado", 0.0);
        transporteGastado = intent.getDoubleExtra("montoTransporteGastado", 0.0);
        gasolinaGastado = intent.getDoubleExtra("montoGasolinaGastado", 0.0);
        otrosGastado = intent.getDoubleExtra("montoOtrosGastado", 0.0);

        if (idViaje == -1 || idUsuario == null || folioViaje == null) {
            Toast.makeText(this, "⚠️ Error: Datos de viaje incompletos", Toast.LENGTH_SHORT).show();
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
        textViewMontoOtros = findViewById(R.id.textViewMontoOtros);
        buttonFolioTicket = findViewById(R.id.buttonFolioTicket);
        imagenFacturas = findViewById(R.id.imagenFacturas);
        buttonFinalizarViaje = findViewById(R.id.buttonFinalizarViaje);
        buttonRechazarRevision = findViewById(R.id.buttonRechazarRevision);
        buttonRegresarMenu = findViewById(R.id.buttonRegresarMenu);
    }

    private void setupClickListeners() {
        imagenFacturas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdministradorFinalActivity.this, FacturasSubidasActivity.class);
                intent.putExtra("idViaje", idViaje);
                intent.putExtra("folioViaje", folioViaje);
                intent.putExtra("soloLectura", true);
                intent.putExtra("montoHotel", hotelAutorizado);
                intent.putExtra("montoComida", comidaAutorizado);
                intent.putExtra("montoTransporte", transporteAutorizado);
                intent.putExtra("montoGasolina", gasolinaAutorizado);
                intent.putExtra("montoOtros", 0.0);
                startActivity(intent);
            }
        });

        buttonFinalizarViaje.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarDialogoFinalizar();
            }
        });

        buttonRechazarRevision.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navegarARechazoRevision();
            }
        });

        buttonRegresarMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                regresarMenu();
            }
        });
    }

    private void cargarDatosViaje() {
        textViewNumeroEmpleado.setText(idUsuario);
        textViewFolioViaje.setText("Folio: " + folioViaje);

        actualizarTextoMontoCategoria(textViewMontoHotel, hotelAutorizado, hotelGastado);
        actualizarTextoMontoCategoria(textViewMontoComida, comidaAutorizado, comidaGastado);
        actualizarTextoMontoCategoria(textViewMontoTransporte, transporteAutorizado, transporteGastado);
        actualizarTextoMontoCategoria(textViewMontoGasolina, gasolinaAutorizado, gasolinaGastado);

        actualizarTextoSobrantes();
    }

    private void actualizarTextoMontoCategoria(TextView textView, double autorizado, double gastado) {
        String texto = "💰 Autorizado: $" + formatoPeso.format(autorizado) +
                "\n📋 Comprobado: $" + formatoPeso.format(gastado);

        textView.setText(texto);

        double saldo = autorizado - gastado;
        if (saldo < -0.01) {
            textView.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
        } else if (saldo > 0.01) {
            textView.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_dark));
        } else {
            textView.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
        }
    }

    private void actualizarTextoSobrantes() {
        if (otrosGastado <= 0.01) {
            textViewMontoOtros.setText("✅ Sin Sobrantes");
            textViewMontoOtros.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
            buttonFolioTicket.setText("✅ No requiere folio de ticket");
            buttonFolioTicket.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
        } else {
            textViewMontoOtros.setText("💸 Sobrantes Comprobados:\n$" + formatoPeso.format(otrosGastado));
            textViewMontoOtros.setTextColor(ContextCompat.getColor(this, android.R.color.holo_blue_dark));

            if (folioTicketSobrante != null && !folioTicketSobrante.trim().isEmpty()) {
                buttonFolioTicket.setText("🎫 Folio del ticket:\n" + folioTicketSobrante);
                buttonFolioTicket.setTextColor(ContextCompat.getColor(this, android.R.color.holo_blue_dark));
            } else {
                buttonFolioTicket.setText("⚠️ ATENCIÓN:\n🎫 Sobrante sin folio de ticket");
                buttonFolioTicket.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
                buttonFolioTicket.setTextSize(17);
            }
        }
    }

    private void validarSaldos() {
        double totalAutorizado = hotelAutorizado + comidaAutorizado + transporteAutorizado + gasolinaAutorizado;
        double totalGastado = hotelGastado + comidaGastado + transporteGastado + gasolinaGastado + otrosGastado;
        double diferencia = Math.abs(totalAutorizado - totalGastado);

        boolean tieneExcedentes = (hotelGastado > hotelAutorizado + 0.01) ||
                (comidaGastado > comidaAutorizado + 0.01) ||
                (transporteGastado > transporteAutorizado + 0.01) ||
                (gasolinaGastado > gasolinaAutorizado + 0.01);

        boolean sobrante_sin_folio = (otrosGastado > 0.01) &&
                (folioTicketSobrante == null || folioTicketSobrante.trim().isEmpty());

        if (diferencia > 0.01 || tieneExcedentes) {
            buttonFinalizarViaje.setEnabled(false);
            buttonFinalizarViaje.setAlpha(0.5f);
            buttonFinalizarViaje.setText("❌ No se puede finalizar");

            String mensaje = "⚠️ INCONSISTENCIAS DETECTADAS:\n\n";
            if (diferencia > 0.01) {
                mensaje += "💰 Diferencia en saldos: $" + formatoPeso.format(diferencia) + "\n";
            }
            if (tieneExcedentes) {
                mensaje += "📈 Excedentes en categorías detectados\n";
            }
            if (sobrante_sin_folio) {
                mensaje += "🎫 Sobrante sin folio de ticket\n";
            }
            mensaje += "\n🔍 Revise las inconsistencias antes de continuar";

            Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
        } else {
            buttonFinalizarViaje.setEnabled(true);
            buttonFinalizarViaje.setAlpha(1.0f);
            buttonFinalizarViaje.setText("✅ Finalizar Viaje");

            String mensajeExito = "✅ Viaje listo para finalizar\n💰 Todos los saldos validados correctamente";
            if (sobrante_sin_folio) {
                mensajeExito += "\n⚠️ Nota: Sobrante sin folio de ticket";
            }

            Toast.makeText(this, mensajeExito, Toast.LENGTH_LONG).show();
        }
    }

    private void mostrarDialogoFinalizar() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("🏁 Finalizar Viaje");

        String mensaje = "¿Confirma la finalización del viaje?\n\n" +
                "👤 Empleado: " + idUsuario + "\n" +
                "📋 Folio: " + folioViaje + "\n\n" +
                "💼 RESUMEN FINANCIERO:\n" +
                "🏨 Hotel: $" + formatoPeso.format(hotelGastado) + " / $" + formatoPeso.format(hotelAutorizado) + "\n" +
                "🍽️ Comida: $" + formatoPeso.format(comidaGastado) + " / $" + formatoPeso.format(comidaAutorizado) + "\n" +
                "🚗 Transporte: $" + formatoPeso.format(transporteGastado) + " / $" + formatoPeso.format(transporteAutorizado) + "\n" +
                "⛽ Gasolina: $" + formatoPeso.format(gasolinaGastado) + " / $" + formatoPeso.format(gasolinaAutorizado) + "\n";

        if (otrosGastado > 0) {
            mensaje += "💸 Sobrantes: $" + formatoPeso.format(otrosGastado) + "\n";
        }

        mensaje += "\n✅ Saldos validados correctamente\n📋 Facturas revisadas\n\n⚠️ Esta acción cerrará definitivamente el viaje";

        builder.setMessage(mensaje);

        builder.setPositiveButton("✅ Sí, Finalizar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finalizarViaje();
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

    private void navegarARechazoRevision() {
        Intent intent = new Intent(AdministradorFinalActivity.this, RechazoRevisionActivity.class);
        intent.putExtra("idViaje", idViaje);
        intent.putExtra("idUsuario", idUsuario);
        intent.putExtra("folioViaje", folioViaje);
        intent.putExtra("idRevisorRH", idRevisorRH);
        startActivity(intent);
        finish();
    }

    private void finalizarViaje() {
        buttonFinalizarViaje.setEnabled(false);
        buttonRechazarRevision.setEnabled(false);

        ViajeDAO.finalizarViaje(idViaje, idRevisorRH, new ViajeDAO.ViajeCallback() {
            @Override
            public void onSuccess(String message) {
                NotificationManager.crearNotificacionViajeFinalizado(idUsuario, folioViaje);

                Toast.makeText(AdministradorFinalActivity.this,
                        "🏁 Viaje finalizado exitosamente\n📧 Empleado notificado correctamente",
                        Toast.LENGTH_LONG).show();
                regresarMenu();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(AdministradorFinalActivity.this,
                        "❌ Error al finalizar viaje: " + error, Toast.LENGTH_LONG).show();
                buttonFinalizarViaje.setEnabled(true);
                buttonRechazarRevision.setEnabled(true);
            }
        });
    }

    private void regresarMenu() {
        Intent intent = new Intent(AdministradorFinalActivity.this, SupervisionViajesActivity.class);
        startActivity(intent);
        finish();
    }
}