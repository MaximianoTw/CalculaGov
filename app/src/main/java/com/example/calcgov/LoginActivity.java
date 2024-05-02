// LoginActivity.java
package com.example.calcgov;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    EditText editTextCPF, editTextPassword;
    Button buttonLogin;
    TextView textViewSignUp;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextCPF = findViewById(R.id.editTextCPF);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewSignUp = findViewById(R.id.textViewSignUp);

        sharedPreferences = getSharedPreferences("myPrefs", MODE_PRIVATE);

        textViewSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            }
        });

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cpf = editTextCPF.getText().toString();
                String password = editTextPassword.getText().toString();

                // Verifica se o CPF e senha correspondem aos salvos
                String savedPassword = sharedPreferences.getString(cpf, "");
                if (savedPassword.equals(password)) {
                    // Login bem-sucedido, redireciona para a tela Home
                    startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                } else {
                    // Login falhou
                    Toast.makeText(LoginActivity.this, "CPF ou senha inválidos", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}