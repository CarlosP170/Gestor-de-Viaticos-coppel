package com.example.gestorviaticoscoppel;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import com.example.gestorviaticoscoppel.dao.FacturaDAO;
import com.example.gestorviaticoscoppel.models.Factura;
import com.example.gestorviaticoscoppel.utils.FileManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

public class FacturasSubidasActivity extends AppCompatActivity {

    private static final String TAG = "FacturasSubidas";

    private LinearLayout facturasHotel, facturasComida, facturasTransporte, facturasGasolina, facturasOtros;
    private TextView textFolioTicket;
    private TextView buttonRegresarMenu;

    private LinearLayout dialogVerFactura;
    private ImageView facturaSeleccionada;
    private TextView textMontoFactura;
    private TextView buttonCerrarDialog;
    private TextView buttonDescargarArchivo;

    private int idViaje;
    private String folioViaje;
    private boolean soloLectura = false;
    private DecimalFormat formatoPeso = new DecimalFormat("#,###.00");
    private Factura facturaActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facturas_subidas);

        Log.d(TAG, "=== INICIANDO FACTURAS SUBIDAS ACTIVITY ===");

        obtenerDatosIntent();
        initializeViews();
        setupClickListeners();
        cargarFacturas();
    }

    private void obtenerDatosIntent() {
        Intent intent = getIntent();
        idViaje = intent.getIntExtra("idViaje", -1);
        folioViaje = intent.getStringExtra("folioViaje");
        soloLectura = intent.getBooleanExtra("soloLectura", false);

        Log.d(TAG, "ID Viaje: " + idViaje);
        Log.d(TAG, "Folio Viaje: " + folioViaje);
        Log.d(TAG, "Solo Lectura: " + soloLectura);

        if (idViaje == -1) {
            Log.e(TAG, "ID de viaje inválido");
            Toast.makeText(this, "Error: No se encontró el viaje", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initializeViews() {
        facturasHotel = findViewById(R.id.facturasHotel);
        facturasComida = findViewById(R.id.facturasComida);
        facturasTransporte = findViewById(R.id.facturasTransporte);
        facturasGasolina = findViewById(R.id.facturasGasolina);
        facturasOtros = findViewById(R.id.facturasOtros);
        textFolioTicket = findViewById(R.id.textFolioTicket);
        buttonRegresarMenu = findViewById(R.id.buttonRegresarMenu);

        dialogVerFactura = findViewById(R.id.dialogVerFactura);
        facturaSeleccionada = findViewById(R.id.facturaSeleccionada);
        textMontoFactura = findViewById(R.id.textMontoFactura);
        buttonCerrarDialog = findViewById(R.id.buttonCerrarDialog);
        buttonDescargarArchivo = findViewById(R.id.buttonDescargarArchivo);
    }

    private void setupClickListeners() {
        buttonRegresarMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                regresarMenu();
            }
        });

        buttonCerrarDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cerrarDialog();
            }
        });

        buttonDescargarArchivo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (facturaActual != null) {
                    descargarFactura(facturaActual);
                }
            }
        });
    }

    private void cargarFacturas() {
        Log.d(TAG, "=== CARGANDO FACTURAS ===");
        Log.d(TAG, "Solicitando facturas para viaje ID: " + idViaje);

        FacturaDAO.obtenerFacturasPorViaje(idViaje, new FacturaDAO.FacturaListCallback() {
            @Override
            public void onSuccess(List<Factura> facturas) {
                Log.d(TAG, "Facturas obtenidas exitosamente: " + facturas.size());
                runOnUiThread(() -> {
                    mostrarFacturas(facturas);
                    cargarFolioTicket(facturas);
                });
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error cargando facturas: " + error);
                runOnUiThread(() -> {
                    Toast.makeText(FacturasSubidasActivity.this,
                            "Error al cargar facturas: " + error, Toast.LENGTH_LONG).show();
                    limpiarTodasLasSecciones();
                });
            }
        });
    }

    private void mostrarFacturas(List<Factura> facturas) {
        Log.d(TAG, "=== MOSTRANDO FACTURAS ===");
        Log.d(TAG, "Total facturas a mostrar: " + facturas.size());

        limpiarTodasLasSecciones();

        if (facturas.isEmpty()) {
            Log.d(TAG, "No hay facturas para mostrar");
            return;
        }

        int facturasProcesadas = 0;
        int facturasExitosas = 0;
        int facturasConError = 0;

        for (Factura factura : facturas) {
            facturasProcesadas++;
            Log.d(TAG, "Procesando factura " + facturasProcesadas + "/" + facturas.size() +
                    " - ID: " + factura.getIdFactura() +
                    ", Categoria: " + factura.getCategoria());

            try {
                LinearLayout contenedor = obtenerContenedorPorCategoria(factura.getCategoria());
                if (contenedor != null) {
                    agregarFacturaAContenedor(factura, contenedor);
                    facturasExitosas++;
                    Log.d(TAG, "Factura " + factura.getIdFactura() + " agregada exitosamente");
                } else {
                    Log.w(TAG, "Contenedor no encontrado para categoría: " + factura.getCategoria());
                    facturasConError++;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error procesando factura " + factura.getIdFactura() + ": " + e.getMessage());
                e.printStackTrace();
                facturasConError++;
            }
        }

        Log.d(TAG, "Procesamiento completado - Exitosas: " + facturasExitosas + ", Con error: " + facturasConError);

        if (facturasConError > 0) {
            Toast.makeText(this, facturasExitosas + " facturas cargadas, " + facturasConError + " con errores",
                    Toast.LENGTH_SHORT).show();
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
                Log.w(TAG, "Categoría desconocida: " + categoria);
                return null;
        }
    }

    private void agregarFacturaAContenedor(Factura factura, LinearLayout contenedor) {
        LinearLayout filaActual = obtenerUltimaFila(contenedor);

        if (filaActual == null || filaActual.getChildCount() >= 3) {
            filaActual = crearNuevaFila();
            contenedor.addView(filaActual);
        }

        LinearLayout facturaContainer = crearVistaFactura(factura);
        filaActual.addView(facturaContainer);
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

    private LinearLayout crearVistaFactura(Factura factura) {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(160, 160);
        containerParams.setMargins(0, 0, 12, 0);
        container.setLayoutParams(containerParams);

        ImageView facturaView = new ImageView(this);
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(160, 160);
        facturaView.setLayoutParams(imageParams);
        facturaView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        facturaView.setBackgroundResource(R.drawable.gray_id_background);

        cargarImagenFactura(factura, facturaView);

        container.addView(facturaView);

        facturaView.setClickable(true);
        facturaView.setFocusable(true);
        facturaView.setOnClickListener(v -> mostrarDialogVer(factura, facturaView));

        return container;
    }

    private void cargarImagenFactura(Factura factura, ImageView imageView) {
        Log.d(TAG, "=== CARGANDO IMAGEN FACTURA ===");
        Log.d(TAG, "Factura ID: " + factura.getIdFactura());
        Log.d(TAG, "Ruta: " + factura.getArchivoRuta());
        Log.d(TAG, "Tipo: " + factura.getArchivoTipo());
        Log.d(TAG, "Base64 disponible: " + (factura.getArchivoContenidoBase64() != null && !factura.getArchivoContenidoBase64().trim().isEmpty()));

        try {
            Bitmap miniatura = FileManager.cargarImagenFactura(
                    this,
                    factura.getArchivoRuta(),
                    factura.getArchivoTipo(),
                    factura.getArchivoContenidoBase64(),
                    160
            );

            if (miniatura != null) {
                Log.d(TAG, "Miniatura cargada exitosamente para factura " + factura.getIdFactura());
                imageView.setImageBitmap(miniatura);
            } else {
                Log.w(TAG, "No se pudo cargar miniatura para factura " + factura.getIdFactura() + " - usando imagen por defecto");
                imageView.setImageResource(R.drawable.imagen);
            }

        } catch (OutOfMemoryError e) {
            Log.e(TAG, "OutOfMemoryError cargando imagen para factura " + factura.getIdFactura());
            FileManager.limpiarCacheImagenes();
            imageView.setImageResource(R.drawable.imagen);
        } catch (Exception e) {
            Log.e(TAG, "Error cargando imagen para factura " + factura.getIdFactura() + ": " + e.getMessage());
            e.printStackTrace();
            imageView.setImageResource(R.drawable.imagen);
        }
    }

    private void mostrarDialogVer(Factura factura, ImageView facturaView) {
        Log.d(TAG, "Mostrando dialog para factura: " + factura.getIdFactura());
        facturaActual = factura;

        cargarImagenFactura(factura, facturaSeleccionada);
        textMontoFactura.setText("Monto de la factura asignado: $" + formatoPeso.format(factura.getMonto()));

        dialogVerFactura.setVisibility(View.VISIBLE);
    }

    private void cerrarDialog() {
        dialogVerFactura.setVisibility(View.GONE);
        facturaActual = null;
    }

    private void descargarFactura(Factura factura) {
        Log.d(TAG, "=== DESCARGANDO FACTURA ===");
        Log.d(TAG, "Factura ID: " + factura.getIdFactura());

        try {
            File archivoDestino = null;

            if (factura.getArchivoRuta() != null && !factura.getArchivoRuta().isEmpty()) {
                Log.d(TAG, "Intentando descargar desde archivo local: " + factura.getArchivoRuta());
                File archivoOriginal = FileManager.obtenerArchivo(this, factura.getArchivoRuta());
                if (archivoOriginal != null) {
                    archivoDestino = crearArchivoDescarga(factura, archivoOriginal);
                    Log.d(TAG, "Descarga desde archivo local exitosa");
                } else {
                    Log.w(TAG, "Archivo local no encontrado");
                }
            }

            if (archivoDestino == null && factura.getArchivoContenidoBase64() != null && !factura.getArchivoContenidoBase64().isEmpty()) {
                Log.d(TAG, "Intentando descargar desde Base64");
                String extension = obtenerExtension(factura.getArchivoNombre());
                File archivoTemporal = FileManager.crearArchivoTemporalDesdeBase64(this, factura.getArchivoContenidoBase64(), extension);
                if (archivoTemporal != null) {
                    archivoDestino = crearArchivoDescarga(factura, archivoTemporal);
                    archivoTemporal.delete();
                    Log.d(TAG, "Descarga desde Base64 exitosa");
                } else {
                    Log.e(TAG, "No se pudo crear archivo temporal desde Base64");
                }
            }

            if (archivoDestino == null) {
                Log.e(TAG, "No se pudo crear archivo de descarga");
                Toast.makeText(this, "Archivo no encontrado", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d(TAG, "Archivo descargado exitosamente: " + archivoDestino.getAbsolutePath());
            Toast.makeText(this, "Factura descargada: " + archivoDestino.getName(), Toast.LENGTH_LONG).show();

            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", archivoDestino);
            intent.setDataAndType(uri, getMimeType(factura.getArchivoTipo()));
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            try {
                startActivity(intent);
            } catch (Exception e) {
                Log.w(TAG, "No se puede abrir el archivo: " + e.getMessage());
                Toast.makeText(this, "No se puede abrir el archivo", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.e(TAG, "Error descargando factura: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Error al descargar la factura", Toast.LENGTH_SHORT).show();
        }
    }

    private File crearArchivoDescarga(Factura factura, File archivoOrigen) throws IOException {
        File carpetaDescargas = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (!carpetaDescargas.exists()) {
            carpetaDescargas.mkdirs();
        }

        String extension = obtenerExtension(factura.getArchivoNombre());
        String nombreArchivo = "factura_" + factura.getIdFactura() + "_" + System.currentTimeMillis() + "." + extension;
        File archivoDestino = new File(carpetaDescargas, nombreArchivo);

        java.io.FileInputStream inputStream = new java.io.FileInputStream(archivoOrigen);
        FileOutputStream outputStream = new FileOutputStream(archivoDestino);

        byte[] buffer = new byte[8192];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }

        inputStream.close();
        outputStream.close();

        return archivoDestino;
    }

    private String obtenerExtension(String nombreArchivo) {
        if (nombreArchivo.contains(".")) {
            return nombreArchivo.substring(nombreArchivo.lastIndexOf(".") + 1);
        }
        return "jpg";
    }

    private String getMimeType(String tipoArchivo) {
        if ("PDF".equals(tipoArchivo)) {
            return "application/pdf";
        } else {
            return "image/*";
        }
    }

    private void cargarFolioTicket(List<Factura> facturas) {
        double totalOtros = 0;
        for (Factura factura : facturas) {
            if ("Otros".equals(factura.getCategoria())) {
                totalOtros += factura.getMonto();
            }
        }

        if (totalOtros > 0) {
            textFolioTicket.setText("Sobrante depositado: $" + formatoPeso.format(totalOtros));
        } else {
            textFolioTicket.setText("Sin sobrantes");
        }
    }

    private void regresarMenu() {
        if (soloLectura) {
            Intent intent = new Intent(FacturasSubidasActivity.this, AdministradorFinalActivity.class);
            intent.putExtra("idViaje", idViaje);
            intent.putExtra("folioViaje", folioViaje);
            startActivity(intent);
            finish();
        } else {
            Intent intent = new Intent(FacturasSubidasActivity.this, AdministradorFinalActivity.class);
            startActivity(intent);
            finish();
        }
    }
}