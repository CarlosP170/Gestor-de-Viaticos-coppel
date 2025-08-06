package com.example.gestorviaticoscoppel.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Base64;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileManager {

    private static final String DIRECTORIO_APP = "gestor_viaticos";
    private static final String DIRECTORIO_FACTURAS = "facturas";

    public static String convertirArchivoABase64(Context context, Uri archivoUri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(archivoUri);
            if (inputStream == null) {
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

            return base64String;

        } catch (IOException e) {
            return null;
        }
    }

    public static byte[] convertirBase64ABytes(String base64String) {
        try {
            if (base64String == null || base64String.trim().isEmpty()) {
                return null;
            }
            return Base64.decode(base64String, Base64.DEFAULT);
        } catch (Exception e) {
            return null;
        }
    }

    public static Bitmap cargarMiniaturaDesdeBase64(String base64String, String tipoArchivo, int tamaño) {
        try {
            if (base64String == null || base64String.trim().isEmpty()) {
                return null;
            }

            if ("IMAGE".equals(tipoArchivo)) {
                return cargarMiniaturaImagenBase64(base64String, tamaño);
            } else if ("PDF".equals(tipoArchivo)) {
                return cargarMiniaturaPDFBase64(base64String, tamaño);
            }

            return null;

        } catch (Exception e) {
            return null;
        }
    }

    private static Bitmap cargarMiniaturaImagenBase64(String base64String, int tamaño) {
        try {
            byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);

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

                return scaledBitmap;
            }

        } catch (OutOfMemoryError e) {
            System.gc();
        } catch (Exception e) {
        }

        return null;
    }

    private static Bitmap cargarMiniaturaPDFBase64(String base64String, int tamaño) {
        try {
            byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);

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

                    return bitmap;
                }
                fileDescriptor.close();
            }
            tempFile.delete();

        } catch (Exception e) {
        }

        return null;
    }

    public static File crearArchivoTemporalDesdeBase64(Context context, String base64String, String extension) {
        try {
            if (base64String == null || base64String.trim().isEmpty()) {
                return null;
            }

            byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);

            File tempFile = File.createTempFile("factura_", "." + extension, context.getCacheDir());
            FileOutputStream fos = new FileOutputStream(tempFile);
            fos.write(decodedBytes);
            fos.close();

            return tempFile;

        } catch (Exception e) {
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
                return archivoDestino.getAbsolutePath();
            }

        } catch (IOException e) {
        }

        return null;
    }

    public static File obtenerArchivo(Context context, String rutaArchivo) {
        if (rutaArchivo == null || rutaArchivo.trim().isEmpty()) {
            return null;
        }

        File archivo = new File(rutaArchivo);

        if (!archivo.exists()) {
            File directorioFacturas = obtenerDirectorioFacturas(context);
            String nombreArchivo = archivo.getName();
            archivo = new File(directorioFacturas, nombreArchivo);
        }

        return archivo.exists() ? archivo : null;
    }

    public static Bitmap cargarMiniatura(Context context, String rutaArchivo, int tamaño) {
        File archivo = obtenerArchivo(context, rutaArchivo);
        if (archivo == null) {
            return null;
        }

        try {
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

                return scaledBitmap;
            }

        } catch (OutOfMemoryError e) {
            System.gc();
        } catch (Exception e) {
        }

        return null;
    }

    public static Bitmap cargarMiniaturaPDF(Context context, String rutaArchivo, int tamaño) {
        File archivo = obtenerArchivo(context, rutaArchivo);
        if (archivo == null) {
            return null;
        }

        try {
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

                    return bitmap;
                }
                fileDescriptor.close();
            }
        } catch (Exception e) {
        }

        return null;
    }

    public static Bitmap cargarImagenFactura(Context context, String rutaArchivo, String tipoArchivo, String base64Content, int tamaño) {
        if (rutaArchivo != null && !rutaArchivo.trim().isEmpty()) {
            if ("IMAGE".equals(tipoArchivo)) {
                Bitmap miniatura = cargarMiniatura(context, rutaArchivo, tamaño);
                if (miniatura != null) {
                    return miniatura;
                }
            } else if ("PDF".equals(tipoArchivo)) {
                Bitmap miniaturaPDF = cargarMiniaturaPDF(context, rutaArchivo, tamaño);
                if (miniaturaPDF != null) {
                    return miniaturaPDF;
                }
            }
        }

        if (base64Content != null && !base64Content.trim().isEmpty()) {
            Bitmap miniaturaBase64 = cargarMiniaturaDesdeBase64(base64Content, tipoArchivo, tamaño);
            if (miniaturaBase64 != null) {
                return miniaturaBase64;
            }
        }

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
                return false;
            }

            File archivo = new File(rutaArchivo);
            boolean eliminado = archivo.exists() && archivo.delete();
            return eliminado;
        } catch (Exception e) {
            return false;
        }
    }

    public static void limpiarCacheImagenes() {
        System.gc();
    }
}