package com.example.gestorviaticoscoppel;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.gestorviaticoscoppel.dao.ViajeDAO;
import com.example.gestorviaticoscoppel.models.Viaje;
import com.example.gestorviaticoscoppel.utils.SessionManager;

public class GestorPersonalActivity extends AppCompatActivity {

    private TextView textViewTitle;
    private TextView textViewEmployeeId;
    private TextView textViewViaticosLabel;
    private LinearLayout buttonFolioStatus;
    private TextView textFolioLabel;
    private TextView textEstadoLabel;
    private LinearLayout buttonHistorialViajes;
    private TextView textViewInfo;
    private TextView textViewAdvertencia;
    private TextView buttonRegresarMenu;

    private SessionManager sessionManager;
    private String idUsuario;
    private String nombreUsuario;
    private Viaje viajeActivo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestor_personal);

        sessionManager = new SessionManager(this);
        idUsuario = sessionManager.getCurrentUserId();
        nombreUsuario = sessionManager.getCurrentUserName();

        initializeViews();
        setupClickListeners();
        cargarEstadoViajes();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarEstadoViajes();
    }

    private void initializeViews() {
        textViewTitle = findViewById(R.id.textViewTitle);
        textViewEmployeeId = findViewById(R.id.textViewEmployeeId);
        textViewViaticosLabel = findViewById(R.id.textViewViaticosLabel);
        buttonFolioStatus = findViewById(R.id.buttonFolioStatus);
        textFolioLabel = findViewById(R.id.textFolioLabel);
        textEstadoLabel = findViewById(R.id.textEstadoLabel);
        textViewInfo = findViewById(R.id.textViewInfo);
        buttonRegresarMenu = findViewById(R.id.buttonRegresarMenu);

        textViewTitle.setText("👥 Gestor Personal\n" + nombreUsuario);
        textViewEmployeeId.setText(idUsuario);

        crearBotonHistorial();
        agregarTextoAdvertencia();
        configurarTextoInfo();
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

    private void agregarTextoAdvertencia() {
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
    }

    private void configurarTextoInfo() {
        textViewInfo.setText("💡 En caso de que no aparezca el viaje en curso 3 días después de solicitarlos, favor de validar la situación con RH");
    }

    private void setupClickListeners() {
        buttonFolioStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (viajeActivo != null && "En_Curso".equals(viajeActivo.getEstado())) {
                    navigateToViaticos();
                } else {
                    Toast.makeText(GestorPersonalActivity.this,
                            "⚠️ No hay viaje activo disponible", Toast.LENGTH_SHORT).show();
                }
            }
        });

        buttonHistorialViajes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GestorPersonalActivity.this, HistorialViajesActivity.class);
                intent.putExtra("idUsuario", idUsuario);
                intent.putExtra("origen", "gerente");
                startActivity(intent);
            }
        });

        buttonRegresarMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                regresarMenu();
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
        Intent intent = new Intent(GestorPersonalActivity.this, ViaticosViajeActivity.class);
        intent.putExtra("idViaje", viajeActivo.getIdViaje());
        intent.putExtra("origen", "gerente");
        startActivity(intent);
    }

    private void regresarMenu() {
        Intent intent = new Intent(GestorPersonalActivity.this, GestorGerenteActivity.class);
        startActivity(intent);
        finish();
    }
}