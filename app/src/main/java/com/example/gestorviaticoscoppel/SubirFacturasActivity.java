package com.example.gestorviaticoscoppel;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.example.gestorviaticoscoppel.dao.FacturaDAO;
import com.example.gestorviaticoscoppel.dao.ViajeDAO;
import com.example.gestorviaticoscoppel.models.Factura;
import com.example.gestorviaticoscoppel.models.Viaje;
import com.example.gestorviaticoscoppel.utils.FileManager;
import com.example.gestorviaticoscoppel.utils.SessionManager;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;

public class SubirFacturasActivity extends AppCompatActivity {

    private FrameLayout buttonAreaArchivos;
    private TextView textArrastrarArchivo;
    private TextView textSoloUnArchivo;
    private ImageView imageViewMiniatura;
    private EditText editTextMonto;
    private RadioGroup radioGroupCategorias;
    private RadioButton radioHotel, radioComida, radioTransporte, radioGasolina, radioOtros;
    private TextView buttonGuardarDatos;
    private TextView buttonEliminarModificar;
    private TextView buttonRegresarMenu;

    private Uri archivoSeleccionado;
    private ActivityResultLauncher<Intent> selectorArchivos;
    private SessionManager sessionManager;
    private String origen;
    private int idViaje;
    private Viaje viajeActual;
    private boolean procesandoFactura = false;
    private DecimalFormat formatoPeso = new DecimalFormat("#,###.00");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subir_facturas);

        sessionManager = new SessionManager(this);
        obtenerDatosIntent();
        initializeViews();
        initializeFileSelector();
        setupClickListeners();
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
        buttonAreaArchivos = findViewById(R.id.buttonAreaArchivos);
        textArrastrarArchivo = findViewById(R.id.textArrastrarArchivo);
        textSoloUnArchivo = findViewById(R.id.textSoloUnArchivo);
        imageViewMiniatura = findViewById(R.id.imageViewMiniatura);
        editTextMonto = findViewById(R.id.editTextMonto);
        radioGroupCategorias = findViewById(R.id.radioGroupCategorias);
        radioHotel = findViewById(R.id.radioHotel);
        radioComida = findViewById(R.id.radioComida);
        radioTransporte = findViewById(R.id.radioTransporte);
        radioGasolina = findViewById(R.id.radioGasolina);
        radioOtros = findViewById(R.id.radioOtros);
        buttonGuardarDatos = findViewById(R.id.buttonGuardarDatos);
        buttonEliminarModificar = findViewById(R.id.buttonEliminarModificar);
        buttonRegresarMenu = findViewById(R.id.buttonRegresarMenu);
    }

    private void cargarDatosViaje() {
        String currentUserId = sessionManager.getCurrentUserId();
        ViajeDAO.obtenerViajeActivo(currentUserId, new ViajeDAO.ViajeSingleCallback() {
            @Override
            public void onSuccess(Viaje viaje) {
                if (viaje.getIdViaje() == idViaje) {
                    viajeActual = viaje;
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(SubirFacturasActivity.this,
                        "❌ Error al cargar datos del viaje", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initializeFileSelector() {
        selectorArchivos = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            if (data != null && data.getData() != null) {
                                archivoSeleccionado = data.getData();
                                mostrarMiniatura(archivoSeleccionado);
                                Toast.makeText(SubirFacturasActivity.this, "✅ Archivo cargado correctamente", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
        );
    }

    private void setupClickListeners() {
        buttonAreaArchivos.setOnClickListener(v -> {
            if (!procesandoFactura) {
                abrirSelectorArchivos();
            }
        });

        buttonGuardarDatos.setOnClickListener(v -> {
            if (!procesandoFactura) {
                guardarFactura();
            }
        });

        buttonEliminarModificar.setOnClickListener(v -> {
            if (!procesandoFactura) {
                Intent intent = new Intent(SubirFacturasActivity.this, GestionFacturasActivity.class);
                intent.putExtra("origen", origen);
                intent.putExtra("idViaje", idViaje);
                startActivity(intent);
            }
        });

        buttonRegresarMenu.setOnClickListener(v -> {
            if (!procesandoFactura) {
                regresarMenuAnterior();
            }
        });
    }

    private void abrirSelectorArchivos() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        String[] mimeTypes = {"application/pdf", "image/*"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);

        selectorArchivos.launch(Intent.createChooser(intent, "Seleccionar archivo"));
    }

    private void mostrarMiniatura(Uri uri) {
        try {
            String mimeType = getContentResolver().getType(uri);

            if (mimeType != null) {
                if (mimeType.startsWith("image/")) {
                    mostrarMiniaturaImagen(uri);
                } else if (mimeType.equals("application/pdf")) {
                    mostrarMiniaturaPDF(uri);
                } else {
                    mostrarTextoArchivo();
                }
            } else {
                mostrarTextoArchivo();
            }
        } catch (Exception e) {
            mostrarTextoArchivo();
        }
    }

    private void mostrarMiniaturaImagen(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;
            options.inPreferredConfig = Bitmap.Config.RGB_565;

            Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);

            if (bitmap != null) {
                Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 200, 200, true);

                textArrastrarArchivo.setVisibility(View.GONE);
                textSoloUnArchivo.setVisibility(View.GONE);
                imageViewMiniatura.setVisibility(View.VISIBLE);
                imageViewMiniatura.setImageBitmap(resizedBitmap);

                if (resizedBitmap != bitmap) {
                    bitmap.recycle();
                }
            } else {
                mostrarTextoArchivo();
            }

            if (inputStream != null) {
                inputStream.close();
            }
        } catch (IOException | OutOfMemoryError e) {
            mostrarTextoArchivo();
        }
    }

    private void mostrarMiniaturaPDF(Uri uri) {
        try {
            ParcelFileDescriptor fileDescriptor = getContentResolver().openFileDescriptor(uri, "r");
            if (fileDescriptor != null) {
                PdfRenderer pdfRenderer = new PdfRenderer(fileDescriptor);
                if (pdfRenderer.getPageCount() > 0) {
                    PdfRenderer.Page page = pdfRenderer.openPage(0);

                    Bitmap bitmap = Bitmap.createBitmap(200, 280, Bitmap.Config.ARGB_8888);
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

                    textArrastrarArchivo.setVisibility(View.GONE);
                    textSoloUnArchivo.setVisibility(View.GONE);
                    imageViewMiniatura.setVisibility(View.VISIBLE);
                    imageViewMiniatura.setImageBitmap(bitmap);

                    page.close();
                    pdfRenderer.close();
                } else {
                    mostrarTextoArchivo();
                }
                fileDescriptor.close();
            } else {
                mostrarTextoArchivo();
            }
        } catch (Exception e) {
            mostrarTextoArchivo();
        }
    }

    private void mostrarTextoArchivo() {
        String nombreArchivo = obtenerNombreArchivo(archivoSeleccionado);
        textArrastrarArchivo.setVisibility(View.VISIBLE);
        textSoloUnArchivo.setVisibility(View.GONE);
        imageViewMiniatura.setVisibility(View.GONE);
        textArrastrarArchivo.setText("📎 Archivo seleccionado: " + nombreArchivo);
    }

    private void guardarFactura() {
        if (procesandoFactura) {
            Toast.makeText(this, "⏳ Ya se está procesando una factura, espere...", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!validarFormulario()) {
            return;
        }

        procesandoFactura = true;
        deshabilitarControles();

        String monto = editTextMonto.getText().toString().trim().replace("$", "");
        double montoValor = Double.parseDouble(monto);
        String categoria = obtenerCategoriaSeleccionada();

        if (!validarMontoAutorizado(categoria, montoValor)) {
            procesandoFactura = false;
            habilitarControles();
            return;
        }

        String archivoBase64 = FileManager.convertirArchivoABase64(this, archivoSeleccionado);

        if (archivoBase64 == null) {
            Toast.makeText(this, "❌ Error al procesar el archivo", Toast.LENGTH_SHORT).show();
            procesandoFactura = false;
            habilitarControles();
            return;
        }

        Factura factura = new Factura(
                idViaje,
                categoria,
                montoValor,
                obtenerNombreArchivo(archivoSeleccionado),
                obtenerTipoArchivo(),
                archivoBase64
        );

        FacturaDAO.guardarFactura(factura, new FacturaDAO.FacturaCallback() {
            @Override
            public void onSuccess(String message) {
                actualizarMontoGastado(categoria, montoValor);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(SubirFacturasActivity.this,
                        "❌ Error al guardar factura: " + error, Toast.LENGTH_LONG).show();
                procesandoFactura = false;
                habilitarControles();
            }
        });
    }

    private void deshabilitarControles() {
        buttonGuardarDatos.setEnabled(false);
        buttonGuardarDatos.setAlpha(0.5f);
        buttonGuardarDatos.setText("⏳ Procesando...");

        buttonAreaArchivos.setEnabled(false);
        buttonAreaArchivos.setAlpha(0.5f);

        buttonEliminarModificar.setEnabled(false);
        buttonEliminarModificar.setAlpha(0.5f);

        buttonRegresarMenu.setEnabled(false);
        buttonRegresarMenu.setAlpha(0.5f);

        editTextMonto.setEnabled(false);
        radioGroupCategorias.setEnabled(false);

        for (int i = 0; i < radioGroupCategorias.getChildCount(); i++) {
            radioGroupCategorias.getChildAt(i).setEnabled(false);
        }
    }

    private void habilitarControles() {
        buttonGuardarDatos.setEnabled(true);
        buttonGuardarDatos.setAlpha(1.0f);
        buttonGuardarDatos.setText("💾 Guardar Datos");

        buttonAreaArchivos.setEnabled(true);
        buttonAreaArchivos.setAlpha(1.0f);

        buttonEliminarModificar.setEnabled(true);
        buttonEliminarModificar.setAlpha(1.0f);

        buttonRegresarMenu.setEnabled(true);
        buttonRegresarMenu.setAlpha(1.0f);

        editTextMonto.setEnabled(true);
        radioGroupCategorias.setEnabled(true);

        for (int i = 0; i < radioGroupCategorias.getChildCount(); i++) {
            radioGroupCategorias.getChildAt(i).setEnabled(true);
        }
    }

    private boolean validarMontoAutorizado(String categoria, double montoFactura) {
        if (viajeActual == null) {
            Toast.makeText(this, "⚠️ Error: No se han cargado los datos del viaje", Toast.LENGTH_SHORT).show();
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
            double otrosYaGastado = viajeActual.getMontoOtrosGastado();
            double sobranteDisponible = saldoTotalDisponible - otrosYaGastado;

            if (sobranteDisponible < 0) sobranteDisponible = 0;

            if (montoFactura > sobranteDisponible) {
                Toast.makeText(this, "⚠️ El monto excede el sobrante disponible. Sobrante disponible: $" +
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
        }

        double saldoDisponible = montoAutorizado - montoGastado;

        if (montoFactura > saldoDisponible) {
            Toast.makeText(this, "⚠️ Factura excede el monto autorizado. Saldo disponible: $" +
                    formatoPeso.format(saldoDisponible), Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    private void actualizarMontoGastado(String categoria, double monto) {
        ViajeDAO.actualizarMontoGastado(idViaje, categoria, monto, new ViajeDAO.ViajeCallback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    Toast.makeText(SubirFacturasActivity.this,
                            "✅ Factura guardada y monto actualizado correctamente", Toast.LENGTH_SHORT).show();
                    limpiarFormulario();
                    cargarDatosViaje();
                    procesandoFactura = false;
                    habilitarControles();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(SubirFacturasActivity.this,
                            "⚠️ Factura guardada pero error al actualizar monto: " + error, Toast.LENGTH_LONG).show();
                    limpiarFormulario();
                    procesandoFactura = false;
                    habilitarControles();
                });
            }
        });
    }

    private boolean validarFormulario() {
        if (archivoSeleccionado == null) {
            Toast.makeText(this, "⚠️ Por favor seleccione un archivo", Toast.LENGTH_SHORT).show();
            return false;
        }

        String monto = editTextMonto.getText().toString().trim().replace("$", "");
        if (monto.isEmpty()) {
            Toast.makeText(this, "⚠️ Por favor ingrese el monto", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            double montoValor = Double.parseDouble(monto);
            if (montoValor <= 0) {
                Toast.makeText(this, "⚠️ El monto debe ser mayor a cero", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "⚠️ El monto debe ser un número válido", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (radioGroupCategorias.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "⚠️ Por favor seleccione una categoría", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private String obtenerCategoriaSeleccionada() {
        int selectedId = radioGroupCategorias.getCheckedRadioButtonId();

        if (selectedId == R.id.radioHotel) {
            return "Hotel";
        } else if (selectedId == R.id.radioComida) {
            return "Comida";
        } else if (selectedId == R.id.radioTransporte) {
            return "Transporte";
        } else if (selectedId == R.id.radioGasolina) {
            return "Gasolina";
        } else if (selectedId == R.id.radioOtros) {
            return "Otros";
        }
        return "";
    }

    private String obtenerExtensionArchivo() {
        String mimeType = getContentResolver().getType(archivoSeleccionado);
        if (mimeType != null) {
            if (mimeType.startsWith("image/")) {
                if (mimeType.equals("image/jpeg")) return "jpg";
                if (mimeType.equals("image/png")) return "png";
                return "jpg";
            } else if (mimeType.equals("application/pdf")) {
                return "pdf";
            }
        }

        String nombre = obtenerNombreArchivo(archivoSeleccionado);
        if (nombre.contains(".")) {
            return nombre.substring(nombre.lastIndexOf(".") + 1);
        }

        return "jpg";
    }

    private String obtenerTipoArchivo() {
        String mimeType = getContentResolver().getType(archivoSeleccionado);
        if (mimeType != null) {
            if (mimeType.startsWith("image/")) {
                return "IMAGE";
            } else if (mimeType.equals("application/pdf")) {
                return "PDF";
            }
        }
        return "IMAGE";
    }

    private String obtenerNombreArchivo(Uri uri) {
        String nombre = "";
        if (uri != null) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (index != -1) {
                    nombre = cursor.getString(index);
                }
                cursor.close();
            }
        }
        return nombre.isEmpty() ? "archivo_sin_nombre" : nombre;
    }

    private void limpiarFormulario() {
        archivoSeleccionado = null;
        textArrastrarArchivo.setText("📁 Arrastra o pega aquí tus facturas PDF o fotografía del ticket");
        textArrastrarArchivo.setVisibility(View.VISIBLE);
        textSoloUnArchivo.setVisibility(View.VISIBLE);
        imageViewMiniatura.setVisibility(View.GONE);
        editTextMonto.setText("");
        radioGroupCategorias.clearCheck();
    }

    private void regresarMenuAnterior() {
        Intent intent = new Intent(SubirFacturasActivity.this, ViaticosViajeActivity.class);
        intent.putExtra("origen", origen);
        intent.putExtra("idViaje", idViaje);
        startActivity(intent);
        finish();
    }
}