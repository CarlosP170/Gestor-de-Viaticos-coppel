package com.example.gestorviaticoscoppel;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class GestorGerenteActivity extends AppCompatActivity {

    private TextView buttonGestorPersonal;
    private TextView buttonSolicitudViaticos;
    private TextView buttonSolicitudViaticosColaborador;
    private TextView buttonCerrarSesion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestor_gerente);

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        buttonGestorPersonal = findViewById(R.id.buttonGestorPersonal);
        buttonSolicitudViaticos = findViewById(R.id.buttonSolicitudViaticos);
        buttonSolicitudViaticosColaborador = findViewById(R.id.buttonSolicitudViaticosColaborador);
        buttonCerrarSesion = findViewById(R.id.buttonCerrarSesion);
    }

    private void setupClickListeners() {
        buttonGestorPersonal.setOnClickListener(v -> {
            Intent intent = new Intent(GestorGerenteActivity.this, GestorPersonalActivity.class);
            startActivity(intent);
        });

        buttonSolicitudViaticos.setOnClickListener(v -> {
            Intent intent = new Intent(GestorGerenteActivity.this, SolicitudViaticosActivity.class);
            startActivity(intent);
        });

        buttonSolicitudViaticosColaborador.setOnClickListener(v -> {
            Intent intent = new Intent(GestorGerenteActivity.this, SolicitudColaboradorActivity.class);
            startActivity(intent);
        });

        buttonCerrarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarDialogoCerrarSesion();
            }
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
        Intent intent = new Intent(GestorGerenteActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        Toast.makeText(this, "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show();
    }
}