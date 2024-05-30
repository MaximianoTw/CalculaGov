// SignupActivity.java
package com.example.calcgov;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

public class SignupActivity extends AppCompatActivity {
    EditText editTextName, editTextCPF, editTextPassword;
    Button buttonSignup;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        editTextName = findViewById(R.id.editTextName);
        editTextCPF = findViewById(R.id.editTextCPF);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonSignup = findViewById(R.id.buttonSignup);
        Button buttonVoltarSignUp = findViewById(R.id.VoltarSingUp); // Referência ao botão VoltarSignUp

        sharedPreferences = getSharedPreferences("myPrefs", MODE_PRIVATE);

        buttonSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = editTextName.getText().toString();
                String cpf = editTextCPF.getText().toString();
                String password = editTextPassword.getText().toString();

                // Salva o novo cadastro
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(cpf, password);
                editor.apply();

                Toast.makeText(SignupActivity.this, "Cadastro realizado com sucesso", Toast.LENGTH_SHORT).show();
                finish(); // Volta para a tela de login
            }
        });

        buttonVoltarSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Voltar para a MainActivity
                startActivity(new Intent(SignupActivity.this, MainActivity.class));
                finish(); // Finaliza a SignupActivity
            }
        });
    }}