package com.example.gestorviaticoscoppel;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.gestorviaticoscoppel.dao.FacturaDAO;
import com.example.gestorviaticoscoppel.dao.ViajeDAO;
import com.example.gestorviaticoscoppel.models.Factura;
import com.example.gestorviaticoscoppel.models.Viaje;
import com.example.gestorviaticoscoppel.utils.FileManager;
import com.example.gestorviaticoscoppel.utils.SessionManager;
import java.text.DecimalFormat;
import java.util.List;

public class GestionFacturasActivity extends AppCompatActivity {

    private LinearLayout facturasHotel, facturasComida, facturasTransporte, facturasGasolina, facturasOtros;
    private TextView buttonGuardarDatos, buttonRegresarMenu;
    private TextView textFolioTicket;

    private LinearLayout dialogEditarFactura;
    private ImageView facturaSeleccionada;
    private EditText editTextMontoModificar;
    private TextView buttonCerrarDialog, buttonAceptar, buttonCancelar;

    private Factura facturaActual;
    private SessionManager sessionManager;
    private String origen;
    private int idViaje;
    private Viaje viajeActual;
    private DecimalFormat formatoPeso = new DecimalFormat("#,###.00");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestion_facturas);

        sessionManager = new SessionManager(this);
        obtenerDatosIntent();
        initializeViews();
        setupClickListeners();
        cargarDatosViaje();
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
            Toast.makeText(this, "⚠️ Error: No se encontró el viaje", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }

    private void initializeViews() {
        facturasHotel = findViewById(R.id.facturasHotel);
        facturasComida = findViewById(R.id.facturasComida);
        facturasTransporte = findViewById(R.id.facturasTransporte);
        facturasGasolina = findViewById(R.id.facturasGasolina);
        facturasOtros = findViewById(R.id.facturasOtros);

        buttonGuardarDatos = findViewById(R.id.buttonGuardarDatos);
        buttonRegresarMenu = findViewById(R.id.buttonRegresarMenu);

        dialogEditarFactura = findViewById(R.id.dialogEditarFactura);
        facturaSeleccionada = findViewById(R.id.facturaSeleccionada);
        editTextMontoModificar = findViewById(R.id.editTextMontoModificar);
        buttonCerrarDialog = findViewById(R.id.buttonCerrarDialog);
        buttonAceptar = findViewById(R.id.buttonAceptar);
        buttonCancelar = findViewById(R.id.buttonCancelar);

        buttonCancelar.setText("🗑️ Eliminar");

        crearSeccionTicket();
    }

    private void crearSeccionTicket() {
        try {
            LinearLayout mainLayout = (LinearLayout) findViewById(R.id.seccionOtros).getParent();

            if (mainLayout != null) {
                LinearLayout seccionTicket = new LinearLayout(this);
                seccionTicket.setOrientation(LinearLayout.VERTICAL);
                seccionTicket.setPadding(16, 16, 16, 16);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 16, 0, 16);
                seccionTicket.setLayoutParams(params);

                TextView titulo = new TextView(this);
                titulo.setText("🎫 Folio del Ticket:");
                titulo.setTextSize(20);
                titulo.setTextColor(getResources().getColor(android.R.color.black));
                titulo.setTypeface(null, android.graphics.Typeface.BOLD);
                titulo.setGravity(android.view.Gravity.CENTER);
                titulo.setPadding(0, 0, 0, 16);

                textFolioTicket = new TextView(this);
                textFolioTicket.setText("📭 Sin folio guardado");
                textFolioTicket.setTextSize(18);
                textFolioTicket.setTextColor(getResources().getColor(android.R.color.darker_gray));
                textFolioTicket.setGravity(android.view.Gravity.CENTER);
                textFolioTicket.setPadding(16, 24, 16, 24);
                textFolioTicket.setBackgroundResource(R.drawable.gray_id_background);

                LinearLayout.LayoutParams folioParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 120);
                textFolioTicket.setLayoutParams(folioParams);

                seccionTicket.addView(titulo);
                seccionTicket.addView(textFolioTicket);

                int otrosIndex = mainLayout.indexOfChild(findViewById(R.id.seccionOtros));
                mainLayout.addView(seccionTicket, otrosIndex + 1);
            }
        } catch (Exception e) {
            // Error silencioso en creación de sección
        }
    }

    private void setupClickListeners() {
        buttonGuardarDatos.setOnClickListener(v -> guardarCambios());
        buttonRegresarMenu.setOnClickListener(v -> regresarMenu());

        buttonCerrarDialog.setOnClickListener(v -> cerrarDialog());
        buttonAceptar.setOnClickListener(v -> aceptarCambios());
        buttonCancelar.setOnClickListener(v -> eliminarFactura());

        if (textFolioTicket != null) {
            textFolioTicket.setOnClickListener(v -> mostrarDialogEditarFolio());
        }
    }

    private void mostrarDialogEditarFolio() {
        if (viajeActual == null || viajeActual.getFolioTicketSobrante() == null ||
                viajeActual.getFolioTicketSobrante().trim().isEmpty()) {
            return;
        }

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);

        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(60, 60, 60, 60);
        mainLayout.setBackgroundResource(R.drawable.white_input_background);

        android.widget.RelativeLayout headerLayout = new android.widget.RelativeLayout(this);
        LinearLayout.LayoutParams headerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        headerParams.setMargins(0, 0, 0, 40);
        headerLayout.setLayoutParams(headerParams);

        TextView titulo = new TextView(this);
        titulo.setText("✏️ Editar Número de Folio");
        titulo.setTextSize(22);
        titulo.setTextColor(getResources().getColor(android.R.color.black));
        titulo.setTypeface(null, android.graphics.Typeface.BOLD);
        titulo.setGravity(android.view.Gravity.CENTER);
        android.widget.RelativeLayout.LayoutParams tituloParams = new android.widget.RelativeLayout.LayoutParams(
                android.widget.RelativeLayout.LayoutParams.MATCH_PARENT, android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT);
        titulo.setLayoutParams(tituloParams);

        TextView btnCerrar = new TextView(this);
        btnCerrar.setText("✕");
        btnCerrar.setTextSize(23);
        btnCerrar.setTextColor(getResources().getColor(android.R.color.white));
        btnCerrar.setTypeface(null, android.graphics.Typeface.BOLD);
        btnCerrar.setBackgroundResource(R.drawable.button_red_background);
        btnCerrar.setGravity(android.view.Gravity.CENTER | android.view.Gravity.CENTER_VERTICAL);
        btnCerrar.setPadding(0, 0, 0, 0);
        btnCerrar.setClickable(true);
        btnCerrar.setFocusable(true);
        android.widget.RelativeLayout.LayoutParams cerrarParams = new android.widget.RelativeLayout.LayoutParams(70, 70);
        cerrarParams.addRule(android.widget.RelativeLayout.ALIGN_PARENT_END);
        cerrarParams.addRule(android.widget.RelativeLayout.CENTER_VERTICAL);
        btnCerrar.setLayoutParams(cerrarParams);

        headerLayout.addView(titulo);
        headerLayout.addView(btnCerrar);
        mainLayout.addView(headerLayout);

        final EditText input = new EditText(this);
        input.setText(viajeActual.getFolioTicketSobrante());
        input.setTextSize(18);
        input.setTextColor(getResources().getColor(android.R.color.black));
        input.setBackgroundResource(R.drawable.white_rounded_input);
        input.setPadding(30, 50, 30, 50);
        input.setGravity(android.view.Gravity.CENTER);
        LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 150);
        inputParams.setMargins(0, 0, 0, 40);
        input.setLayoutParams(inputParams);
        mainLayout.addView(input);

        LinearLayout buttonLayout = new LinearLayout(this);
        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
        buttonLayout.setGravity(android.view.Gravity.CENTER);
        LinearLayout.LayoutParams buttonLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        buttonLayout.setLayoutParams(buttonLayoutParams);

        TextView btnGuardar = new TextView(this);
        btnGuardar.setText("💾 Guardar");
        btnGuardar.setTextSize(22);
        btnGuardar.setTextColor(getResources().getColor(android.R.color.white));
        btnGuardar.setTypeface(null, android.graphics.Typeface.BOLD);
        btnGuardar.setBackgroundResource(R.drawable.green_button_background);
        btnGuardar.setGravity(android.view.Gravity.CENTER);
        btnGuardar.setPadding(0, 0, 0, 0);
        btnGuardar.setClickable(true);
        btnGuardar.setFocusable(true);
        LinearLayout.LayoutParams btnGuardarParams = new LinearLayout.LayoutParams(300, 120);
        btnGuardarParams.setMargins(0, 0, 20, 0);
        btnGuardar.setLayoutParams(btnGuardarParams);

        TextView btnBorrar = new TextView(this);
        btnBorrar.setText("🗑️ Borrar");
        btnBorrar.setTextSize(22);
        btnBorrar.setTextColor(getResources().getColor(android.R.color.white));
        btnBorrar.setTypeface(null, android.graphics.Typeface.BOLD);
        btnBorrar.setBackgroundResource(R.drawable.button_red_background);
        btnBorrar.setGravity(android.view.Gravity.CENTER);
        btnBorrar.setPadding(0, 0, 0, 0);
        btnBorrar.setClickable(true);
        btnBorrar.setFocusable(true);
        LinearLayout.LayoutParams btnBorrarParams = new LinearLayout.LayoutParams(300, 120);
        btnBorrarParams.setMargins(20, 0, 0, 0);
        btnBorrar.setLayoutParams(btnBorrarParams);

        buttonLayout.addView(btnGuardar);
        buttonLayout.addView(btnBorrar);
        mainLayout.addView(buttonLayout);

        builder.setView(mainLayout);
        android.app.AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        dialog.show();

        android.view.WindowManager.LayoutParams layoutParams = new android.view.WindowManager.LayoutParams();
        layoutParams.copyFrom(dialog.getWindow().getAttributes());
        layoutParams.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.95);
        layoutParams.height = android.view.WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(layoutParams);

        btnCerrar.setOnClickListener(v -> dialog.dismiss());

        btnGuardar.setOnClickListener(v -> {
            String nuevoFolio = input.getText().toString().trim();
            if (!nuevoFolio.isEmpty()) {
                if (validarMontoSobrante(nuevoFolio)) {
                    guardarFolioModificado(nuevoFolio);
                    dialog.dismiss();
                }
            } else {
                Toast.makeText(this, "⚠️ Por favor ingrese un folio válido", Toast.LENGTH_SHORT).show();
            }
        });

        btnBorrar.setOnClickListener(v -> {
            dialog.dismiss();
            mostrarDialogConfirmarEliminacion();
        });
    }

    private boolean validarMontoSobrante(String nuevoFolioStr) {
        if (viajeActual == null) {
            Toast.makeText(this, "❌ Error: No se han cargado los datos del viaje", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            Double.parseDouble(nuevoFolioStr);
            return true;
        } catch (NumberFormatException e) {
            Toast.makeText(this, "⚠️ El folio debe ser un número válido", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private void mostrarDialogMontoExcedido(double sobranteDisponible) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);

        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(60, 50, 60, 50);
        mainLayout.setBackgroundResource(R.drawable.white_input_background);

        android.widget.RelativeLayout headerLayout = new android.widget.RelativeLayout(this);
        LinearLayout.LayoutParams headerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        headerLayout.setLayoutParams(headerParams);

        TextView titulo = new TextView(this);
        titulo.setText("⚠️ Monto Excedido");
        titulo.setTextSize(24);
        titulo.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        titulo.setTypeface(null, android.graphics.Typeface.BOLD);
        titulo.setGravity(android.view.Gravity.CENTER);
        android.widget.RelativeLayout.LayoutParams tituloParams = new android.widget.RelativeLayout.LayoutParams(
                android.widget.RelativeLayout.LayoutParams.MATCH_PARENT, android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT);
        titulo.setLayoutParams(tituloParams);

        TextView btnCerrar = new TextView(this);
        btnCerrar.setText("✕");
        btnCerrar.setTextSize(26);
        btnCerrar.setTextColor(getResources().getColor(android.R.color.white));
        btnCerrar.setTypeface(null, android.graphics.Typeface.BOLD);
        btnCerrar.setBackgroundResource(R.drawable.button_red_background);
        btnCerrar.setGravity(android.view.Gravity.CENTER | android.view.Gravity.CENTER_VERTICAL);
        btnCerrar.setPadding(0, 0, 0, 0);
        btnCerrar.setClickable(true);
        btnCerrar.setFocusable(true);
        android.widget.RelativeLayout.LayoutParams cerrarParams = new android.widget.RelativeLayout.LayoutParams(70, 70);
        cerrarParams.addRule(android.widget.RelativeLayout.ALIGN_PARENT_END);
        cerrarParams.addRule(android.widget.RelativeLayout.CENTER_VERTICAL);
        btnCerrar.setLayoutParams(cerrarParams);

        headerLayout.addView(titulo);
        headerLayout.addView(btnCerrar);
        mainLayout.addView(headerLayout);

        TextView mensaje1 = new TextView(this);
        mensaje1.setText("💰 El folio excede el sobrante disponible");
        mensaje1.setTextSize(18);
        mensaje1.setTextColor(getResources().getColor(android.R.color.black));
        mensaje1.setGravity(android.view.Gravity.CENTER);
        mensaje1.setPadding(0, 30, 0, 20);
        mainLayout.addView(mensaje1);

        TextView mensaje2 = new TextView(this);
        mensaje2.setText("Sobrante disponible: $" + formatoPeso.format(sobranteDisponible));
        mensaje2.setTextSize(18);
        mensaje2.setTextColor(getResources().getColor(android.R.color.black));
        mensaje2.setGravity(android.view.Gravity.CENTER);
        mensaje2.setPadding(0, 0, 0, 30);
        mainLayout.addView(mensaje2);

        builder.setView(mainLayout);
        android.app.AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        btnCerrar.setOnClickListener(v -> dialog.dismiss());

        dialog.show();

        android.view.WindowManager.LayoutParams layoutParams = new android.view.WindowManager.LayoutParams();
        layoutParams.copyFrom(dialog.getWindow().getAttributes());
        layoutParams.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.95);
        layoutParams.height = android.view.WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(layoutParams);
    }

    private void mostrarDialogConfirmarEliminacion() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);

        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(80, 80, 80, 80);
        mainLayout.setBackgroundResource(R.drawable.white_input_background);

        TextView titulo = new TextView(this);
        titulo.setText("🗑️ Confirmación de Eliminación");
        titulo.setTextSize(22);
        titulo.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        titulo.setTypeface(null, android.graphics.Typeface.BOLD);
        titulo.setGravity(android.view.Gravity.CENTER);
        titulo.setPadding(0, 0, 0, 40);
        mainLayout.addView(titulo);

        TextView mensaje = new TextView(this);
        mensaje.setText("¿Está seguro que desea eliminar el folio del ticket?");
        mensaje.setTextSize(18);
        mensaje.setTextColor(getResources().getColor(android.R.color.black));
        mensaje.setGravity(android.view.Gravity.CENTER);
        mensaje.setPadding(0, 0, 0, 50);
        mensaje.setLineSpacing(8, 1.0f);
        mainLayout.addView(mensaje);

        LinearLayout buttonLayout = new LinearLayout(this);
        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
        buttonLayout.setGravity(android.view.Gravity.CENTER);

        TextView btnAceptar = new TextView(this);
        btnAceptar.setText("✅ Aceptar");
        btnAceptar.setTextSize(22);
        btnAceptar.setTextColor(getResources().getColor(android.R.color.white));
        btnAceptar.setTypeface(null, android.graphics.Typeface.BOLD);
        btnAceptar.setBackgroundResource(R.drawable.blue_button_background);
        btnAceptar.setGravity(android.view.Gravity.CENTER);
        btnAceptar.setPadding(0, 0, 0, 0);
        btnAceptar.setClickable(true);
        btnAceptar.setFocusable(true);
        LinearLayout.LayoutParams btnAceptarParams = new LinearLayout.LayoutParams(300, 120);
        btnAceptarParams.setMargins(0, 0, 25, 0);
        btnAceptar.setLayoutParams(btnAceptarParams);

        TextView btnCancelar = new TextView(this);
        btnCancelar.setText("❌ Cancelar");
        btnCancelar.setTextSize(22);
        btnCancelar.setTextColor(getResources().getColor(android.R.color.white));
        btnCancelar.setTypeface(null, android.graphics.Typeface.BOLD);
        btnCancelar.setBackgroundResource(R.drawable.button_red_background);
        btnCancelar.setGravity(android.view.Gravity.CENTER);
        btnCancelar.setPadding(0, 0, 0, 0);
        btnCancelar.setClickable(true);
        btnCancelar.setFocusable(true);
        LinearLayout.LayoutParams btnCancelarParams = new LinearLayout.LayoutParams(300, 120);
        btnCancelarParams.setMargins(25, 0, 0, 0);
        btnCancelar.setLayoutParams(btnCancelarParams);

        buttonLayout.addView(btnAceptar);
        buttonLayout.addView(btnCancelar);
        mainLayout.addView(buttonLayout);

        builder.setView(mainLayout);
        android.app.AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        btnAceptar.setOnClickListener(v -> {
            eliminarFolioTicket();
            dialog.dismiss();
        });

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        dialog.show();

        android.view.WindowManager.LayoutParams layoutParams = new android.view.WindowManager.LayoutParams();
        layoutParams.copyFrom(dialog.getWindow().getAttributes());
        layoutParams.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.95);
        layoutParams.height = android.view.WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(layoutParams);
    }

    private void guardarFolioModificado(String nuevoFolio) {
        ViajeDAO.actualizarFolioTicket(idViaje, nuevoFolio, new ViajeDAO.ViajeCallback() {
            @Override
            public void onSuccess(String message) {
                Toast.makeText(GestionFacturasActivity.this, "✅ Folio actualizado exitosamente", Toast.LENGTH_SHORT).show();
                cargarDatosViaje();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(GestionFacturasActivity.this, "❌ Error al actualizar folio: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void eliminarFolioTicket() {
        ViajeDAO.actualizarFolioTicket(idViaje, "", new ViajeDAO.ViajeCallback() {
            @Override
            public void onSuccess(String message) {
                Toast.makeText(GestionFacturasActivity.this, "🗑️ Folio eliminado exitosamente", Toast.LENGTH_SHORT).show();
                cargarDatosViaje();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(GestionFacturasActivity.this, "❌ Error al eliminar folio: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void cargarDatosViaje() {
        String currentUserId = sessionManager.getCurrentUserId();
        ViajeDAO.obtenerViajeActivo(currentUserId, new ViajeDAO.ViajeSingleCallback() {
            @Override
            public void onSuccess(Viaje viaje) {
                if (viaje.getIdViaje() == idViaje) {
                    viajeActual = viaje;
                    runOnUiThread(() -> {
                        cargarFacturas();
                        actualizarSeccionTicket();
                    });
                } else {
                    runOnUiThread(() -> cargarFacturas());
                }
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> cargarFacturas());
            }
        });
    }

    private void actualizarSeccionTicket() {
        if (viajeActual != null && textFolioTicket != null) {
            String folio = viajeActual.getFolioTicketSobrante();
            if (folio != null && !folio.trim().isEmpty()) {
                textFolioTicket.setText("🎫 " + folio);
                textFolioTicket.setTextColor(0xFF5680FB);
                textFolioTicket.setClickable(true);
                textFolioTicket.setFocusable(true);
            } else {
                textFolioTicket.setText("📭 Sin folio guardado");
                textFolioTicket.setTextColor(getResources().getColor(android.R.color.darker_gray));
                textFolioTicket.setClickable(false);
                textFolioTicket.setFocusable(false);
            }
        }
    }

    private void cargarFacturas() {
        FacturaDAO.obtenerFacturasPorViaje(idViaje, new FacturaDAO.FacturaListCallback() {
            @Override
            public void onSuccess(List<Factura> facturas) {
                runOnUiThread(() -> mostrarFacturas(facturas));
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(GestionFacturasActivity.this,
                            "❌ Error al cargar facturas: " + error, Toast.LENGTH_LONG).show();
                    limpiarTodasLasSecciones();
                });
            }
        });
    }

    private void mostrarFacturas(List<Factura> facturas) {
        limpiarTodasLasSecciones();

        if (facturas.isEmpty()) {
            return;
        }

        for (Factura factura : facturas) {
            try {
                LinearLayout contenedor = obtenerContenedorPorCategoria(factura.getCategoria());
                if (contenedor != null) {
                    agregarFacturaAContenedor(factura, contenedor);
                }
            } catch (Exception e) {
                // Error silencioso al procesar factura
            }
        }
    }

    private void limpiarTodasLasSecciones() {
        facturasHotel.removeAllViews();
        facturasComida.removeAllViews();
        facturasTransporte.removeAllViews();
        facturasGasolina.removeAllViews();
        facturasOtros.removeAllViews();
    }

    private LinearLayout obtenerContenedorPorCategoria(String categoria) {
        switch (categoria) {
            case "Hotel":
                return facturasHotel;
            case "Comida":
                return facturasComida;
            case "Transporte":
                return facturasTransporte;
            case "Gasolina":
                return facturasGasolina;
            case "Otros":
                return facturasOtros;
            default:
                return null;
        }
    }

    private void agregarFacturaAContenedor(Factura factura, LinearLayout contenedor) {
        LinearLayout filaActual = obtenerUltimaFila(contenedor);

        if (filaActual == null || filaActual.getChildCount() >= 3) {
            filaActual = crearNuevaFila();
            contenedor.addView(filaActual);
        }

        ImageView facturaView = crearVistaFactura(factura);
        filaActual.addView(facturaView);
    }

    private LinearLayout obtenerUltimaFila(LinearLayout contenedor) {
        if (contenedor.getChildCount() > 0) {
            View ultimoChild = contenedor.getChildAt(contenedor.getChildCount() - 1);
            if (ultimoChild instanceof LinearLayout) {
                return (LinearLayout) ultimoChild;
            }
        }
        return null;
    }

    private LinearLayout crearNuevaFila() {
        LinearLayout fila = new LinearLayout(this);
        fila.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 12, 0, 12);
        fila.setLayoutParams(params);
        return fila;
    }

    private ImageView crearVistaFactura(Factura factura) {
        ImageView facturaView = new ImageView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(160, 160);
        params.setMargins(0, 0, 8, 0);
        facturaView.setLayoutParams(params);
        facturaView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        facturaView.setBackgroundResource(R.drawable.gray_id_background);
        facturaView.setClickable(true);
        facturaView.setFocusable(true);

        cargarImagenFactura(factura, facturaView);

        facturaView.setOnClickListener(v -> mostrarDialogEditar(factura, facturaView));

        return facturaView;
    }

    private void cargarImagenFactura(Factura factura, ImageView imageView) {
        try {
            Bitmap miniatura = FileManager.cargarImagenFactura(
                    this,
                    factura.getArchivoRuta(),
                    factura.getArchivoTipo(),
                    factura.getArchivoContenidoBase64(),
                    160
            );

            if (miniatura != null) {
                imageView.setImageBitmap(miniatura);
            } else {
                imageView.setImageResource(R.drawable.imagen);
            }

        } catch (OutOfMemoryError e) {
            FileManager.limpiarCacheImagenes();
            imageView.setImageResource(R.drawable.imagen);
        } catch (Exception e) {
            imageView.setImageResource(R.drawable.imagen);
        }
    }

    private void mostrarDialogEditar(Factura factura, ImageView facturaView) {
        facturaActual = factura;

        facturaSeleccionada.setImageDrawable(facturaView.getDrawable());
        editTextMontoModificar.setText(String.valueOf(factura.getMonto()));

        dialogEditarFactura.setVisibility(View.VISIBLE);
    }

    private void cerrarDialog() {
        dialogEditarFactura.setVisibility(View.GONE);
        facturaActual = null;
        editTextMontoModificar.setText("");
    }

    private void aceptarCambios() {
        if (facturaActual == null) return;

        String monto = editTextMontoModificar.getText().toString().trim().replace("$", "");

        if (monto.isEmpty()) {
            Toast.makeText(this, "⚠️ Por favor ingrese un monto", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double montoValor = Double.parseDouble(monto);
            if (montoValor <= 0) {
                Toast.makeText(this, "⚠️ El monto debe ser mayor a cero", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!validarMontoAutorizadoModificacion(facturaActual.getCategoria(), montoValor, facturaActual.getMonto())) {
                return;
            }

            double diferencia = montoValor - facturaActual.getMonto();

            FacturaDAO.modificarFactura(facturaActual.getIdFactura(), montoValor, new FacturaDAO.FacturaCallback() {
                @Override
                public void onSuccess(String message) {
                    actualizarMontoViaje(facturaActual.getCategoria(), diferencia);
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(GestionFacturasActivity.this,
                            "❌ Error al modificar factura: " + error, Toast.LENGTH_LONG).show();
                }
            });

        } catch (NumberFormatException e) {
            Toast.makeText(this, "⚠️ El monto debe ser un número válido", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validarMontoAutorizadoModificacion(String categoria, double nuevoMonto, double montoOriginal) {
        if (viajeActual == null) {
            Toast.makeText(this, "❌ Error: No se han cargado los datos del viaje", Toast.LENGTH_SHORT).show();
            return false;
        }

        if ("Otros".equals(categoria)) {
            double saldoHotel = viajeActual.getMontoHotelAutorizado() - viajeActual.getMontoHotelGastado();
            double saldoComida = viajeActual.getMontoComidaAutorizado() - viajeActual.getMontoComidaGastado();
            double saldoTransporte = viajeActual.getMontoTransporteAutorizado() - viajeActual.getMontoTransporteGastado();
            double saldoGasolina = viajeActual.getMontoGasolinaAutorizado() - viajeActual.getMontoGasolinaGastado();

            if (saldoHotel < 0) saldoHotel = 0;
            if (saldoComida < 0) saldoComida = 0;
            if (saldoTransporte < 0) saldoTransporte = 0;
            if (saldoGasolina < 0) saldoGasolina = 0;

            double saldoTotalDisponible = saldoHotel + saldoComida + saldoTransporte + saldoGasolina;
            double otrosYaGastado = viajeActual.getMontoOtrosGastado() - montoOriginal;
            double sobranteDisponible = saldoTotalDisponible - otrosYaGastado;

            if (sobranteDisponible < 0) sobranteDisponible = 0;

            if (nuevoMonto > sobranteDisponible) {
                Toast.makeText(this, "⚠️ El monto excede el sobrante disponible.\n💰 Sobrante disponible: $" +
                        formatoPeso.format(sobranteDisponible), Toast.LENGTH_LONG).show();
                return false;
            }
            return true;
        }

        double montoAutorizado = 0;
        double montoGastado = 0;

        switch (categoria) {
            case "Hotel":
                montoAutorizado = viajeActual.getMontoHotelAutorizado();
                montoGastado = viajeActual.getMontoHotelGastado();
                break;
            case "Comida":
                montoAutorizado = viajeActual.getMontoComidaAutorizado();
                montoGastado = viajeActual.getMontoComidaGastado();
                break;
            case "Transporte":
                montoAutorizado = viajeActual.getMontoTransporteAutorizado();
                montoGastado = viajeActual.getMontoTransporteGastado();
                break;
            case "Gasolina":
                montoAutorizado = viajeActual.getMontoGasolinaAutorizado();
                montoGastado = viajeActual.getMontoGasolinaGastado();
                break;
            default:
                return true;
        }

        double saldoDisponible = montoAutorizado - montoGastado + montoOriginal;

        if (nuevoMonto > saldoDisponible) {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);

            LinearLayout mainLayout = new LinearLayout(this);
            mainLayout.setOrientation(LinearLayout.VERTICAL);
            mainLayout.setPadding(60, 50, 60, 50);
            mainLayout.setBackgroundResource(R.drawable.white_input_background);

            android.widget.RelativeLayout headerLayout = new android.widget.RelativeLayout(this);
            LinearLayout.LayoutParams headerParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            headerLayout.setLayoutParams(headerParams);

            TextView titulo = new TextView(this);
            titulo.setText("⚠️ Monto Excedido");
            titulo.setTextSize(24);
            titulo.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            titulo.setTypeface(null, android.graphics.Typeface.BOLD);
            titulo.setGravity(android.view.Gravity.CENTER);
            android.widget.RelativeLayout.LayoutParams tituloParams = new android.widget.RelativeLayout.LayoutParams(
                    android.widget.RelativeLayout.LayoutParams.MATCH_PARENT, android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT);
            titulo.setLayoutParams(tituloParams);

            TextView btnCerrar = new TextView(this);
            btnCerrar.setText("✕");
            btnCerrar.setTextSize(26);
            btnCerrar.setTextColor(getResources().getColor(android.R.color.white));
            btnCerrar.setTypeface(null, android.graphics.Typeface.BOLD);
            btnCerrar.setBackgroundResource(R.drawable.button_red_background);
            btnCerrar.setGravity(android.view.Gravity.CENTER | android.view.Gravity.CENTER_VERTICAL);
            btnCerrar.setPadding(0, 0, 0, 0);
            btnCerrar.setClickable(true);
            btnCerrar.setFocusable(true);
            android.widget.RelativeLayout.LayoutParams cerrarParams = new android.widget.RelativeLayout.LayoutParams(70, 70);
            cerrarParams.addRule(android.widget.RelativeLayout.ALIGN_PARENT_END);
            cerrarParams.addRule(android.widget.RelativeLayout.CENTER_VERTICAL);
            btnCerrar.setLayoutParams(cerrarParams);

            headerLayout.addView(titulo);
            headerLayout.addView(btnCerrar);
            mainLayout.addView(headerLayout);

            TextView mensaje1 = new TextView(this);
            mensaje1.setText("💰 La factura excede el monto autorizado");
            mensaje1.setTextSize(18);
            mensaje1.setTextColor(getResources().getColor(android.R.color.black));
            mensaje1.setGravity(android.view.Gravity.CENTER);
            mensaje1.setPadding(0, 30, 0, 20);
            mainLayout.addView(mensaje1);

            TextView mensaje2 = new TextView(this);
            mensaje2.setText("Por favor verifique el monto ingresado");
            mensaje2.setTextSize(18);
            mensaje2.setTextColor(getResources().getColor(android.R.color.black));
            mensaje2.setGravity(android.view.Gravity.CENTER);
            mensaje2.setPadding(0, 0, 0, 30);
            mainLayout.addView(mensaje2);

            builder.setView(mainLayout);
            android.app.AlertDialog dialog = builder.create();
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

            btnCerrar.setOnClickListener(v -> dialog.dismiss());

            dialog.show();

            android.view.WindowManager.LayoutParams layoutParams = new android.view.WindowManager.LayoutParams();
            layoutParams.copyFrom(dialog.getWindow().getAttributes());
            layoutParams.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.95);
            layoutParams.height = android.view.WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setAttributes(layoutParams);

            return false;
        }

        return true;
    }

    private void eliminarFactura() {
        if (facturaActual == null) return;

        double montoARestar = -facturaActual.getMonto();
        String categoria = facturaActual.getCategoria();

        FacturaDAO.eliminarFactura(facturaActual.getIdFactura(), new FacturaDAO.FacturaCallback() {
            @Override
            public void onSuccess(String message) {
                FileManager.eliminarArchivo(facturaActual.getArchivoRuta());
                actualizarMontoViaje(categoria, montoARestar);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(GestionFacturasActivity.this,
                        "❌ Error al eliminar factura: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void actualizarMontoViaje(String categoria, double diferencia) {
        ViajeDAO.actualizarMontoGastado(idViaje, categoria, diferencia, new ViajeDAO.ViajeCallback() {
            @Override
            public void onSuccess(String message) {
                Toast.makeText(GestionFacturasActivity.this,
                        "✅ Cambios guardados exitosamente", Toast.LENGTH_SHORT).show();
                cerrarDialog();
                cargarDatosViaje();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(GestionFacturasActivity.this,
                        "⚠️ Factura actualizada pero error en el viaje: " + error, Toast.LENGTH_LONG).show();
                cerrarDialog();
                cargarDatosViaje();
            }
        });
    }

    private void guardarCambios() {
        Toast.makeText(this, "✅ Cambios guardados exitosamente", Toast.LENGTH_SHORT).show();
    }

    private void regresarMenu() {
        Intent intent = new Intent(GestionFacturasActivity.this, SubirFacturasActivity.class);
        intent.putExtra("origen", origen);
        intent.putExtra("idViaje", idViaje);
        startActivity(intent);
        finish();
    }
}