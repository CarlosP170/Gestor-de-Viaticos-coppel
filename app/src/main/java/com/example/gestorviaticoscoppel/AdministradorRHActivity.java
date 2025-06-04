package com.example.gestorviaticoscoppel;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.gestorviaticoscoppel.dao.NotificacionDAO;
import com.example.gestorviaticoscoppel.models.Notificacion;
import java.util.ArrayList;
import java.util.List;

public class AdministradorRHActivity extends AppCompatActivity {

    private TextView buttonRevisionSolicitudes;
    private TextView buttonSupervisionViajes;
    private TextView buttonCerrarSesion;
    private ImageView iconoNotificaciones;

    private String idUsuario = "98875256";
    private List<Notificacion> listaNotificaciones = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_administrador_rh);

        initializeViews();
        setupClickListeners();
        cargarEstadoNotificaciones();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarEstadoNotificaciones();
    }

    private void initializeViews() {
        buttonRevisionSolicitudes = findViewById(R.id.buttonRevisionSolicitudes);
        buttonSupervisionViajes = findViewById(R.id.buttonSupervisionViajes);
        buttonCerrarSesion = findViewById(R.id.buttonCerrarSesion);
        iconoNotificaciones = findViewById(R.id.iconoNotificaciones);
    }

    private void setupClickListeners() {
        buttonRevisionSolicitudes.setOnClickListener(v -> {
            Intent intent = new Intent(AdministradorRHActivity.this, RevisionSolicitudesActivity.class);
            startActivity(intent);
        });

        buttonSupervisionViajes.setOnClickListener(v -> {
            Intent intent = new Intent(AdministradorRHActivity.this, SupervisionViajesActivity.class);
            startActivity(intent);
        });

        buttonCerrarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarDialogoCerrarSesion();
            }
        });

        iconoNotificaciones.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarNotificaciones();
            }
        });
    }

    private void cargarEstadoNotificaciones() {
        NotificacionDAO.verificarNotificacionesNoLeidas(idUsuario, new NotificacionDAO.NotificacionCountCallback() {
            @Override
            public void onSuccess(boolean tieneNoLeidas) {
                runOnUiThread(() -> {
                    if (tieneNoLeidas) {
                        iconoNotificaciones.setImageResource(R.drawable.connotificacion);
                    } else {
                        iconoNotificaciones.setImageResource(R.drawable.sinnotificacion);
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    iconoNotificaciones.setImageResource(R.drawable.sinnotificacion);
                });
            }
        });
    }

    private void mostrarNotificaciones() {
        NotificacionDAO.obtenerNotificacionesPorUsuario(idUsuario, new NotificacionDAO.NotificacionListCallback() {
            @Override
            public void onSuccess(List<Notificacion> notificaciones) {
                runOnUiThread(() -> {
                    listaNotificaciones = notificaciones;
                    crearDialogNotificaciones();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(AdministradorRHActivity.this,
                            "Error al cargar notificaciones", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void crearDialogNotificaciones() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_notificaciones, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView buttonCerrar = dialogView.findViewById(R.id.buttonCerrarNotificaciones);
        TextView buttonMarcarTodas = dialogView.findViewById(R.id.buttonMarcarTodasLeidas);
        LinearLayout containerNotificaciones = dialogView.findViewById(R.id.containerNotificaciones);

        if (listaNotificaciones.isEmpty()) {
            TextView sinNotificaciones = new TextView(this);
            sinNotificaciones.setText("No hay notificaciones");
            sinNotificaciones.setTextSize(16);
            sinNotificaciones.setTextColor(getResources().getColor(android.R.color.darker_gray));
            sinNotificaciones.setGravity(android.view.Gravity.CENTER);
            sinNotificaciones.setPadding(20, 40, 20, 40);
            containerNotificaciones.addView(sinNotificaciones);
        } else {
            for (Notificacion notificacion : listaNotificaciones) {
                View itemView = getLayoutInflater().inflate(R.layout.item_notificacion, containerNotificaciones, false);

                TextView textTitulo = itemView.findViewById(R.id.textTituloNotificacion);
                TextView textMensaje = itemView.findViewById(R.id.textMensajeNotificacion);
                TextView textFecha = itemView.findViewById(R.id.textFechaNotificacion);
                View indicadorNoLeida = itemView.findViewById(R.id.indicadorNoLeida);

                textTitulo.setText(notificacion.getTitulo());
                textMensaje.setText(notificacion.getMensaje());
                textFecha.setText(notificacion.getFechaFormateada());

                if (!notificacion.isLeida()) {
                    indicadorNoLeida.setVisibility(View.VISIBLE);
                    itemView.setAlpha(1.0f);
                } else {
                    indicadorNoLeida.setVisibility(View.GONE);
                    itemView.setAlpha(0.7f);
                }

                itemView.setOnClickListener(v -> {
                    if (!notificacion.isLeida()) {
                        marcarNotificacionComoLeida(notificacion);
                    }
                });

                containerNotificaciones.addView(itemView);
            }
        }

        buttonCerrar.setOnClickListener(v -> dialog.dismiss());

        buttonMarcarTodas.setOnClickListener(v -> {
            NotificacionDAO.marcarTodasComoLeidas(idUsuario, new NotificacionDAO.NotificacionCallback() {
                @Override
                public void onSuccess(String message) {
                    runOnUiThread(() -> {
                        dialog.dismiss();
                        cargarEstadoNotificaciones();
                        Toast.makeText(AdministradorRHActivity.this,
                                "Notificaciones marcadas como leídas", Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onError(String error) {}
            });
        });

        dialog.show();
    }

    private void marcarNotificacionComoLeida(Notificacion notificacion) {
        NotificacionDAO.marcarComoLeida(notificacion.getIdNotificacion(), new NotificacionDAO.NotificacionCallback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    cargarEstadoNotificaciones();
                });
            }

            @Override
            public void onError(String error) {}
        });
    }

    private void mostrarDialogoCerrarSesion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cerrar Sesión");
        builder.setMessage("¿Está seguro que desea cerrar sesión?");

        builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                cerrarSesion();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void cerrarSesion() {
        Intent intent = new Intent(AdministradorRHActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        Toast.makeText(this, "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show();
    }
}