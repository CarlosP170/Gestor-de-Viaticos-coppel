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
import androidx.core.content.ContextCompat;
import com.example.gestorviaticoscoppel.dao.NotificacionDAO;
import com.example.gestorviaticoscoppel.dao.ViajeDAO;
import com.example.gestorviaticoscoppel.models.Notificacion;
import com.example.gestorviaticoscoppel.models.Viaje;
import com.example.gestorviaticoscoppel.utils.SessionManager;
import java.util.ArrayList;
import java.util.List;

public class GestorColaboradorActivity extends AppCompatActivity {

    private TextView textViewTitle;
    private TextView textViewEmployeeId;
    private TextView textViewViaticosLabel;
    private LinearLayout buttonFolioStatus;
    private TextView textFolioLabel;
    private TextView textEstadoLabel;
    private LinearLayout buttonHistorialViajes;
    private TextView textViewInfo;
    private TextView textViewAdvertencia;
    private TextView buttonCerrarSesion;
    private ImageView iconoNotificaciones;

    private SessionManager sessionManager;
    private String idUsuario;
    private String nombreUsuario;
    private Viaje viajeActivo;
    private List<Notificacion> listaNotificaciones = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestor_colaborador);

        sessionManager = new SessionManager(this);
        idUsuario = sessionManager.getCurrentUserId();
        nombreUsuario = sessionManager.getCurrentUserName();

        initializeViews();
        setupClickListeners();
        cargarEstadoViajes();
        cargarEstadoNotificaciones();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarEstadoViajes();
        cargarEstadoNotificaciones();
    }

    private void initializeViews() {
        textViewTitle = findViewById(R.id.textViewTitle);
        textViewEmployeeId = findViewById(R.id.textViewEmployeeId);
        textViewViaticosLabel = findViewById(R.id.textViewViaticosLabel);
        buttonFolioStatus = findViewById(R.id.buttonFolioStatus);
        textFolioLabel = findViewById(R.id.textFolioLabel);
        textEstadoLabel = findViewById(R.id.textEstadoLabel);
        textViewInfo = findViewById(R.id.textViewInfo);
        buttonCerrarSesion = findViewById(R.id.buttonCerrarSesion);
        iconoNotificaciones = findViewById(R.id.iconoNotificaciones);

        textViewTitle.setText("👤 Gestor Colaborador\n" + nombreUsuario);
        textViewEmployeeId.setText(idUsuario);

        crearBotonHistorial();
        agregarLeyendaAdicional();
    }

    private void crearBotonHistorial() {
        LinearLayout parentLayout = (LinearLayout) buttonFolioStatus.getParent();

        buttonHistorialViajes = new LinearLayout(this);
        buttonHistorialViajes.setOrientation(LinearLayout.VERTICAL);
        buttonHistorialViajes.setGravity(android.view.Gravity.CENTER);
        buttonHistorialViajes.setPadding(20, 20, 20, 20);
        buttonHistorialViajes.setClickable(true);
        buttonHistorialViajes.setFocusable(true);
        buttonHistorialViajes.setBackgroundResource(R.drawable.large_gray_area);
        buttonHistorialViajes.setElevation(5);

        LinearLayout.LayoutParams historialParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        historialParams.setMargins(0, 16, 0, 16);
        buttonHistorialViajes.setLayoutParams(historialParams);

        TextView historialText = new TextView(this);
        historialText.setText("📋 Historial de Viajes");
        historialText.setTextSize(20);
        historialText.setTextColor(getResources().getColor(android.R.color.black));
        historialText.setTypeface(null, android.graphics.Typeface.BOLD);
        historialText.setGravity(android.view.Gravity.CENTER);

        buttonHistorialViajes.addView(historialText);

        int folioIndex = parentLayout.indexOfChild(buttonFolioStatus);
        parentLayout.addView(buttonHistorialViajes, folioIndex + 1);
    }

    private void agregarLeyendaAdicional() {
        LinearLayout parentLayout = (LinearLayout) buttonFolioStatus.getParent();

        textViewAdvertencia = new TextView(this);
        textViewAdvertencia.setText("⚠️ En caso de rechazo en la revisión o intermitencias en el proceso, deberá reportarlo inmediatamente con mesa de ayuda");
        textViewAdvertencia.setTextSize(16);
        textViewAdvertencia.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
        textViewAdvertencia.setPadding(20, 20, 20, 20);
        textViewAdvertencia.setLineSpacing(4, 1.0f);
        textViewAdvertencia.setVisibility(View.GONE);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 16, 0, 16);
        textViewAdvertencia.setLayoutParams(params);

        int buttonIndex = parentLayout.indexOfChild(buttonHistorialViajes);
        parentLayout.addView(textViewAdvertencia, buttonIndex + 1);

        textViewInfo.setText("💡 En caso de que no aparezca el viaje en curso 3 días después de solicitarlos, favor de notificarlo a tu gerente para que valide la situación con RH");
    }

    private void setupClickListeners() {
        buttonFolioStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (viajeActivo != null && "En_Curso".equals(viajeActivo.getEstado())) {
                    navigateToViaticos();
                } else {
                    Toast.makeText(GestorColaboradorActivity.this,
                            "⚠️ No hay viaje activo disponible", Toast.LENGTH_SHORT).show();
                }
            }
        });

        buttonHistorialViajes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GestorColaboradorActivity.this, HistorialViajesActivity.class);
                intent.putExtra("idUsuario", idUsuario);
                intent.putExtra("origen", "colaborador");
                startActivity(intent);
            }
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

    private void cargarEstadoViajes() {
        ViajeDAO.obtenerViajeActivo(idUsuario, new ViajeDAO.ViajeSingleCallback() {
            @Override
            public void onSuccess(Viaje viaje) {
                viajeActivo = viaje;
                actualizarBotonPrincipal(viaje);
                actualizarVisibilidadTextos(viaje);
            }

            @Override
            public void onError(String error) {
                viajeActivo = null;
                actualizarBotonSinViajes();
                actualizarVisibilidadTextos(null);
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
                    Toast.makeText(GestorColaboradorActivity.this,
                            "❌ Error al cargar notificaciones", Toast.LENGTH_SHORT).show();
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
            sinNotificaciones.setText("📭 No hay notificaciones disponibles");
            sinNotificaciones.setTextSize(17);
            sinNotificaciones.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));
            sinNotificaciones.setGravity(android.view.Gravity.CENTER);
            sinNotificaciones.setPadding(24, 48, 24, 48);
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
                        Toast.makeText(GestorColaboradorActivity.this,
                                "✅ Todas las notificaciones marcadas como leídas", Toast.LENGTH_SHORT).show();
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

    private void actualizarBotonPrincipal(Viaje viaje) {
        String estado = viaje.getEstado();

        if ("Finalizado".equals(estado) || "Rechazado".equals(estado)) {
            textFolioLabel.setText("📋 Sin Viajes");
            textEstadoLabel.setText("");
            textEstadoLabel.setTextColor(getResources().getColor(android.R.color.darker_gray));
            buttonFolioStatus.setClickable(false);
            buttonFolioStatus.setAlpha(0.7f);
        } else {
            String folio = viaje.getFolioViaje();
            textFolioLabel.setText("🎫 Folio: " + folio);

            switch (estado) {
                case "En_Curso":
                    textEstadoLabel.setText("✅ En Curso");
                    textEstadoLabel.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    buttonFolioStatus.setClickable(true);
                    buttonFolioStatus.setAlpha(1.0f);
                    break;
                case "Enviado_A_Revision":
                    textEstadoLabel.setText("🔍 En Revisión");
                    textEstadoLabel.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                    buttonFolioStatus.setClickable(false);
                    buttonFolioStatus.setAlpha(0.7f);
                    break;
                default:
                    textEstadoLabel.setText("📊 Estado: " + estado);
                    textEstadoLabel.setTextColor(getResources().getColor(android.R.color.darker_gray));
                    buttonFolioStatus.setClickable(false);
                    buttonFolioStatus.setAlpha(0.7f);
                    break;
            }
        }
    }

    private void actualizarBotonSinViajes() {
        textFolioLabel.setText("📋 Sin Viajes");
        textEstadoLabel.setText("");
        textEstadoLabel.setTextColor(getResources().getColor(android.R.color.darker_gray));
        buttonFolioStatus.setClickable(false);
        buttonFolioStatus.setAlpha(0.5f);
    }

    private void actualizarVisibilidadTextos(Viaje viaje) {
        if (viaje != null && ("En_Curso".equals(viaje.getEstado()) || "Enviado_A_Revision".equals(viaje.getEstado()))) {
            textViewAdvertencia.setVisibility(View.VISIBLE);
        } else {
            textViewAdvertencia.setVisibility(View.GONE);
        }
    }

    private void navigateToViaticos() {
        Intent intent = new Intent(GestorColaboradorActivity.this, ViaticosViajeActivity.class);
        intent.putExtra("idViaje", viajeActivo.getIdViaje());
        intent.putExtra("origen", "colaborador");
        startActivity(intent);
    }

    private void mostrarDialogoCerrarSesion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("🚪 Cerrar Sesión");
        builder.setMessage("¿Está seguro que desea cerrar sesión?\n\n⚠️ Asegúrese de haber completado todas las tareas pendientes");

        builder.setPositiveButton("✅ Sí, Cerrar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                cerrarSesion();
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

    private void cerrarSesion() {
        sessionManager.clearSession();
        Intent intent = new Intent(GestorColaboradorActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        Toast.makeText(this, "👋 Sesión cerrada exitosamente", Toast.LENGTH_SHORT).show();
    }
}