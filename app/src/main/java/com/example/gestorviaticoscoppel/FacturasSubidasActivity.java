package com.example.gestorviaticoscoppel;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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

        if (idViaje == -1) {
            Toast.makeText(this, "⚠️ Error: No se encontró el viaje", Toast.LENGTH_SHORT).show();
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
        FacturaDAO.obtenerFacturasPorViaje(idViaje, new FacturaDAO.FacturaListCallback() {
            @Override
            public void onSuccess(List<Factura> facturas) {
                runOnUiThread(() -> {
                    mostrarFacturas(facturas);
                    cargarFolioTicket(facturas);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(FacturasSubidasActivity.this,
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

        int facturasExitosas = 0;
        int facturasConError = 0;

        for (Factura factura : facturas) {
            try {
                LinearLayout contenedor = obtenerContenedorPorCategoria(factura.getCategoria());
                if (contenedor != null) {
                    agregarFacturaAContenedor(factura, contenedor);
                    facturasExitosas++;
                } else {
                    facturasConError++;
                }
            } catch (Exception e) {
                facturasConError++;
            }
        }

        if (facturasConError > 0) {
            Toast.makeText(this, "📋 " + facturasExitosas + " facturas cargadas, " + facturasConError + " con errores",
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

    private void mostrarDialogVer(Factura factura, ImageView facturaView) {
        facturaActual = factura;

        cargarImagenFactura(factura, facturaSeleccionada);
        textMontoFactura.setText("💰 Monto asignado: $" + formatoPeso.format(factura.getMonto()));

        dialogVerFactura.setVisibility(View.VISIBLE);
    }

    private void cerrarDialog() {
        dialogVerFactura.setVisibility(View.GONE);
        facturaActual = null;
    }

    private void descargarFactura(Factura factura) {
        try {
            File archivoDestino = null;

            if (factura.getArchivoRuta() != null && !factura.getArchivoRuta().isEmpty()) {
                File archivoOriginal = FileManager.obtenerArchivo(this, factura.getArchivoRuta());
                if (archivoOriginal != null) {
                    archivoDestino = crearArchivoDescarga(factura, archivoOriginal);
                }
            }

            if (archivoDestino == null && factura.getArchivoContenidoBase64() != null && !factura.getArchivoContenidoBase64().isEmpty()) {
                String extension = obtenerExtension(factura.getArchivoNombre());
                File archivoTemporal = FileManager.crearArchivoTemporalDesdeBase64(this, factura.getArchivoContenidoBase64(), extension);
                if (archivoTemporal != null) {
                    archivoDestino = crearArchivoDescarga(factura, archivoTemporal);
                    archivoTemporal.delete();
                }
            }

            if (archivoDestino == null) {
                Toast.makeText(this, "❌ Archivo no encontrado", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(this, "📥 Factura descargada: " + archivoDestino.getName(), Toast.LENGTH_LONG).show();

            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", archivoDestino);
            intent.setDataAndType(uri, getMimeType(factura.getArchivoTipo()));
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            try {
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "⚠️ No se puede abrir el archivo", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Toast.makeText(this, "❌ Error al descargar la factura", Toast.LENGTH_SHORT).show();
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
            textFolioTicket.setText("💸 Sobrante depositado: $" + formatoPeso.format(totalOtros));
        } else {
            textFolioTicket.setText("✅ Sin sobrantes");
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