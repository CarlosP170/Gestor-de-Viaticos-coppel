package com.example.gestorviaticoscoppel;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.gestorviaticoscoppel.dao.UserDAO;
import com.example.gestorviaticoscoppel.models.User;
import com.example.gestorviaticoscoppel.utils.SessionManager;

public class MainActivity extends AppCompatActivity {

    private EditText editTextEmail;
    private EditText editTextPassword;
    private Button buttonLogin;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessionManager = new SessionManager(this);

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
    }

    private void setupClickListeners() {
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performLogin();
            }
        });
    }

    private void performLogin() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "⚠️ Por favor ingrese email y contraseña", Toast.LENGTH_SHORT).show();
            return;
        }

        UserDAO.login(email, password, new UserDAO.LoginCallback() {
            @Override
            public void onSuccess(User user) {
                sessionManager.saveUserSession(user);

                Intent intent;

                switch (user.getRol()) {
                    case "Gerente":
                        intent = new Intent(MainActivity.this, GestorGerenteActivity.class);
                        Toast.makeText(MainActivity.this, "👨‍💼 Bienvenido, " + user.getName(), Toast.LENGTH_SHORT).show();
                        break;
                    case "Colaborador":
                        intent = new Intent(MainActivity.this, GestorColaboradorActivity.class);
                        Toast.makeText(MainActivity.this, "👤 Bienvenido, " + user.getName(), Toast.LENGTH_SHORT).show();
                        break;
                    case "RH":
                        intent = new Intent(MainActivity.this, AdministradorRHActivity.class);
                        Toast.makeText(MainActivity.this, "👨‍💼 Bienvenido, " + user.getName(), Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(MainActivity.this, "❌ Rol no reconocido", Toast.LENGTH_SHORT).show();
                        return;
                }

                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(MainActivity.this, "❌ " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}