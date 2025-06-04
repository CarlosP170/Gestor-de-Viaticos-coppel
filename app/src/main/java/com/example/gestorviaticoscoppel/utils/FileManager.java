package com.example.gestorviaticoscoppel.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Base64;
import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileManager {

    private static final String TAG = "FileManager";
    private static final String DIRECTORIO_APP = "gestor_viaticos";
    private static final String DIRECTORIO_FACTURAS = "facturas";

    public static String convertirArchivoABase64(Context context, Uri archivoUri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(archivoUri);
            if (inputStream == null) {
                Log.e(TAG, "InputStream es null para URI: " + archivoUri);
                return null;
            }

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                byteArrayOutputStream.write(buffer, 0, length);
            }

            byte[] fileBytes = byteArrayOutputStream.toByteArray();
            String base64String = Base64.encodeToString(fileBytes, Base64.DEFAULT);

            inputStream.close();
            byteArrayOutputStream.close();

            Log.d(TAG, "Archivo convertido a Base64. Tamaño: " + base64String.length() + " caracteres");
            return base64String;

        } catch (IOException e) {
            Log.e(TAG, "Error convirtiendo archivo a Base64: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] convertirBase64ABytes(String base64String) {
        try {
            if (base64String == null || base64String.trim().isEmpty()) {
                Log.w(TAG, "Base64 string está vacío o null");
                return null;
            }
            return Base64.decode(base64String, Base64.DEFAULT);
        } catch (Exception e) {
            Log.e(TAG, "Error decodificando Base64: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static Bitmap cargarMiniaturaDesdeBase64(String base64String, String tipoArchivo, int tamaño) {
        try {
            if (base64String == null || base64String.trim().isEmpty()) {
                Log.w(TAG, "Base64 está vacío para tipo: " + tipoArchivo);
                return null;
            }

            Log.d(TAG, "Cargando miniatura desde Base64. Tipo: " + tipoArchivo + ", Tamaño Base64: " + base64String.length());

            if ("IMAGE".equals(tipoArchivo)) {
                return cargarMiniaturaImagenBase64(base64String, tamaño);
            } else if ("PDF".equals(tipoArchivo)) {
                return cargarMiniaturaPDFBase64(base64String, tamaño);
            }

            Log.w(TAG, "Tipo de archivo no soportado: " + tipoArchivo);
            return null;

        } catch (Exception e) {
            Log.e(TAG, "Error cargando miniatura desde Base64: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private static Bitmap cargarMiniaturaImagenBase64(String base64String, int tamaño) {
        try {
            byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
            Log.d(TAG, "Bytes decodificados: " + decodedBytes.length);

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length, options);

            options.inSampleSize = calcularInSampleSize(options, tamaño, tamaño);
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.RGB_565;

            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length, options);

            if (bitmap != null) {
                int size = Math.min(bitmap.getWidth(), bitmap.getHeight());
                int x = (bitmap.getWidth() - size) / 2;
                int y = (bitmap.getHeight() - size) / 2;

                Bitmap croppedBitmap = Bitmap.createBitmap(bitmap, x, y, size, size);
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(croppedBitmap, tamaño, tamaño, true);

                if (croppedBitmap != bitmap) {
                    croppedBitmap.recycle();
                }
                bitmap.recycle();

                Log.d(TAG, "Miniatura imagen Base64 creada exitosamente");
                return scaledBitmap;
            } else {
                Log.e(TAG, "No se pudo decodificar imagen desde Base64");
            }

        } catch (OutOfMemoryError e) {
            Log.e(TAG, "OutOfMemoryError procesando imagen Base64");
            System.gc();
        } catch (Exception e) {
            Log.e(TAG, "Error procesando imagen Base64: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    private static Bitmap cargarMiniaturaPDFBase64(String base64String, int tamaño) {
        try {
            byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
            Log.d(TAG, "PDF bytes decodificados: " + decodedBytes.length);

            File tempFile = File.createTempFile("temp_pdf", ".pdf");
            FileOutputStream fos = new FileOutputStream(tempFile);
            fos.write(decodedBytes);
            fos.close();

            ParcelFileDescriptor fileDescriptor = ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY);
            if (fileDescriptor != null) {
                PdfRenderer pdfRenderer = new PdfRenderer(fileDescriptor);
                if (pdfRenderer.getPageCount() > 0) {
                    PdfRenderer.Page page = pdfRenderer.openPage(0);

                    Bitmap bitmap = Bitmap.createBitmap(tamaño, tamaño, Bitmap.Config.ARGB_8888);
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

                    page.close();
                    pdfRenderer.close();
                    fileDescriptor.close();
                    tempFile.delete();

                    Log.d(TAG, "Miniatura PDF Base64 creada exitosamente");
                    return bitmap;
                }
                fileDescriptor.close();
            }
            tempFile.delete();

        } catch (Exception e) {
            Log.e(TAG, "Error procesando PDF Base64: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public static File crearArchivoTemporalDesdeBase64(Context context, String base64String, String extension) {
        try {
            if (base64String == null || base64String.trim().isEmpty()) {
                Log.w(TAG, "Base64 vacío para crear archivo temporal");
                return null;
            }

            byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);

            File tempFile = File.createTempFile("factura_", "." + extension, context.getCacheDir());
            FileOutputStream fos = new FileOutputStream(tempFile);
            fos.write(decodedBytes);
            fos.close();

            Log.d(TAG, "Archivo temporal creado: " + tempFile.getAbsolutePath());
            return tempFile;

        } catch (Exception e) {
            Log.e(TAG, "Error creando archivo temporal desde Base64: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static File obtenerDirectorioFacturas(Context context) {
        File directorioApp = new File(context.getFilesDir(), DIRECTORIO_APP);
        if (!directorioApp.exists()) {
            directorioApp.mkdirs();
        }

        File directorioFacturas = new File(directorioApp, DIRECTORIO_FACTURAS);
        if (!directorioFacturas.exists()) {
            directorioFacturas.mkdirs();
        }

        return directorioFacturas;
    }

    public static String guardarArchivo(Context context, Uri archivoUri, int idViaje, String extension) {
        try {
            File directorioFacturas = obtenerDirectorioFacturas(context);
            String nombreArchivo = "factura_" + idViaje + "_" + System.currentTimeMillis() + "." + extension;
            File archivoDestino = new File(directorioFacturas, nombreArchivo);

            InputStream inputStream = context.getContentResolver().openInputStream(archivoUri);
            if (inputStream == null) {
                Log.e(TAG, "No se pudo abrir InputStream para guardar archivo");
                return null;
            }

            FileOutputStream outputStream = new FileOutputStream(archivoDestino);

            byte[] buffer = new byte[8192];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.close();
            inputStream.close();

            if (archivoDestino.exists() && archivoDestino.length() > 0) {
                Log.d(TAG, "Archivo guardado exitosamente: " + archivoDestino.getAbsolutePath());
                return archivoDestino.getAbsolutePath();
            }

        } catch (IOException e) {
            Log.e(TAG, "Error guardando archivo: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public static File obtenerArchivo(Context context, String rutaArchivo) {
        if (rutaArchivo == null || rutaArchivo.trim().isEmpty()) {
            Log.w(TAG, "Ruta de archivo vacía");
            return null;
        }

        File archivo = new File(rutaArchivo);
        Log.d(TAG, "Buscando archivo en: " + rutaArchivo + " - Existe: " + archivo.exists());

        if (!archivo.exists()) {
            File directorioFacturas = obtenerDirectorioFacturas(context);
            String nombreArchivo = archivo.getName();
            archivo = new File(directorioFacturas, nombreArchivo);
            Log.d(TAG, "Buscando en directorio facturas: " + archivo.getAbsolutePath() + " - Existe: " + archivo.exists());
        }

        return archivo.exists() ? archivo : null;
    }

    public static Bitmap cargarMiniatura(Context context, String rutaArchivo, int tamaño) {
        File archivo = obtenerArchivo(context, rutaArchivo);
        if (archivo == null) {
            Log.w(TAG, "Archivo no encontrado para ruta: " + rutaArchivo);
            return null;
        }

        try {
            Log.d(TAG, "Cargando miniatura desde archivo: " + archivo.getAbsolutePath());

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(archivo.getAbsolutePath(), options);

            options.inSampleSize = calcularInSampleSize(options, tamaño, tamaño);
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.RGB_565;

            Bitmap bitmap = BitmapFactory.decodeFile(archivo.getAbsolutePath(), options);

            if (bitmap != null) {
                int size = Math.min(bitmap.getWidth(), bitmap.getHeight());
                int x = (bitmap.getWidth() - size) / 2;
                int y = (bitmap.getHeight() - size) / 2;

                Bitmap croppedBitmap = Bitmap.createBitmap(bitmap, x, y, size, size);
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(croppedBitmap, tamaño, tamaño, true);

                if (croppedBitmap != bitmap) {
                    croppedBitmap.recycle();
                }
                bitmap.recycle();

                Log.d(TAG, "Miniatura desde archivo creada exitosamente");
                return scaledBitmap;
            }

        } catch (OutOfMemoryError e) {
            Log.e(TAG, "OutOfMemoryError cargando miniatura");
            System.gc();
        } catch (Exception e) {
            Log.e(TAG, "Error cargando miniatura: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public static Bitmap cargarMiniaturaPDF(Context context, String rutaArchivo, int tamaño) {
        File archivo = obtenerArchivo(context, rutaArchivo);
        if (archivo == null) {
            Log.w(TAG, "Archivo PDF no encontrado para ruta: " + rutaArchivo);
            return null;
        }

        try {
            Log.d(TAG, "Cargando miniatura PDF desde archivo: " + archivo.getAbsolutePath());

            ParcelFileDescriptor fileDescriptor = ParcelFileDescriptor.open(archivo, ParcelFileDescriptor.MODE_READ_ONLY);
            if (fileDescriptor != null) {
                PdfRenderer pdfRenderer = new PdfRenderer(fileDescriptor);
                if (pdfRenderer.getPageCount() > 0) {
                    PdfRenderer.Page page = pdfRenderer.openPage(0);

                    Bitmap bitmap = Bitmap.createBitmap(tamaño, tamaño, Bitmap.Config.ARGB_8888);
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

                    page.close();
                    pdfRenderer.close();
                    fileDescriptor.close();

                    Log.d(TAG, "Miniatura PDF desde archivo creada exitosamente");
                    return bitmap;
                }
                fileDescriptor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error cargando miniatura PDF: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public static Bitmap cargarImagenFactura(Context context, String rutaArchivo, String tipoArchivo, String base64Content, int tamaño) {
        Log.d(TAG, "=== CARGANDO IMAGEN FACTURA ===");
        Log.d(TAG, "Ruta: " + rutaArchivo);
        Log.d(TAG, "Tipo: " + tipoArchivo);
        Log.d(TAG, "Base64 disponible: " + (base64Content != null && !base64Content.trim().isEmpty()));
        Log.d(TAG, "Tamaño solicitado: " + tamaño);

        if (rutaArchivo != null && !rutaArchivo.trim().isEmpty()) {
            Log.d(TAG, "PASO 1: Intentando cargar desde archivo local");

            if ("IMAGE".equals(tipoArchivo)) {
                Bitmap miniatura = cargarMiniatura(context, rutaArchivo, tamaño);
                if (miniatura != null) {
                    Log.d(TAG, "ÉXITO: Imagen cargada desde archivo local");
                    return miniatura;
                }
            } else if ("PDF".equals(tipoArchivo)) {
                Bitmap miniaturaPDF = cargarMiniaturaPDF(context, rutaArchivo, tamaño);
                if (miniaturaPDF != null) {
                    Log.d(TAG, "ÉXITO: PDF cargado desde archivo local");
                    return miniaturaPDF;
                }
            }
            Log.w(TAG, "FALLO: No se pudo cargar desde archivo local");
        } else {
            Log.d(TAG, "PASO 1: Saltado - No hay ruta de archivo local");
        }

        if (base64Content != null && !base64Content.trim().isEmpty()) {
            Log.d(TAG, "PASO 2: Intentando cargar desde Base64 (FALLBACK)");
            Bitmap miniaturaBase64 = cargarMiniaturaDesdeBase64(base64Content, tipoArchivo, tamaño);
            if (miniaturaBase64 != null) {
                Log.d(TAG, "ÉXITO: Imagen cargada desde Base64");
                return miniaturaBase64;
            }
            Log.w(TAG, "FALLO: No se pudo cargar desde Base64");
        } else {
            Log.w(TAG, "PASO 2: Saltado - No hay contenido Base64");
        }

        Log.e(TAG, "ERROR TOTAL: No se pudo cargar imagen desde ninguna fuente");
        return null;
    }

    private static int calcularInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static boolean eliminarArchivo(String rutaArchivo) {
        try {
            if (rutaArchivo == null || rutaArchivo.trim().isEmpty()) {
                Log.w(TAG, "Ruta vacía para eliminar archivo");
                return false;
            }

            File archivo = new File(rutaArchivo);
            boolean eliminado = archivo.exists() && archivo.delete();
            Log.d(TAG, "Archivo eliminado: " + rutaArchivo + " - Éxito: " + eliminado);
            return eliminado;
        } catch (Exception e) {
            Log.e(TAG, "Error eliminando archivo: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static void limpiarCacheImagenes() {
        Log.d(TAG, "Limpiando cache de imágenes");
        System.gc();
    }
}