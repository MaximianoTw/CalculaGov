package com.example.calcgov;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {
    
    private TabLayout tabLayout;
    private LinearLayout layoutLogin, layoutSignup;
    
    // Login Views
    private TextInputEditText editTextCPF, editTextPassword;
    private Button buttonLogin;
    
    // Signup Views
    private TextInputEditText editTextNameSignup, editTextCPFSignup, editTextPasswordSignup, editTextRendaSignup, editTextDepSignup;
    private Button buttonSignup;
    
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sharedPreferences = getSharedPreferences("myPrefs", MODE_PRIVATE);
        initViews();
        setupTabs();
        setupActions();
    }

    private void initViews() {
        tabLayout = findViewById(R.id.tabLayout);
        layoutLogin = findViewById(R.id.layoutLogin);
        layoutSignup = findViewById(R.id.layoutSignup);
        
        editTextCPF = findViewById(R.id.editTextCPF);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        
        editTextNameSignup = findViewById(R.id.editTextNameSignup);
        editTextCPFSignup = findViewById(R.id.editTextCPFSignup);
        editTextPasswordSignup = findViewById(R.id.editTextPasswordSignup);
        editTextRendaSignup = findViewById(R.id.editTextRendaSignup);
        editTextDepSignup = findViewById(R.id.editTextDepSignup);
        buttonSignup = findViewById(R.id.buttonSignup);
        
        findViewById(R.id.VoltarMain).setOnClickListener(v -> finish());
    }

    private void setupTabs() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    layoutLogin.setVisibility(View.VISIBLE);
                    layoutSignup.setVisibility(View.GONE);
                } else {
                    layoutLogin.setVisibility(View.GONE);
                    layoutSignup.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupActions() {
        buttonLogin.setOnClickListener(v -> {
            String cpf = editTextCPF.getText().toString();
            String password = editTextPassword.getText().toString();

            if (cpf.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor, preencha o CPF e a senha", Toast.LENGTH_SHORT).show();
                return;
            }

            String savedPassword = sharedPreferences.getString(cpf, null);
            if (savedPassword != null && savedPassword.equals(password)) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("logged_cpf", cpf);
                editor.apply();
                startActivity(new Intent(this, HomeActivity.class));
                finish();
            } else {
                Toast.makeText(this, "CPF ou senha não conferem", Toast.LENGTH_SHORT).show();
            }
        });

        buttonSignup.setOnClickListener(v -> {
            String name = editTextNameSignup.getText().toString();
            String cpf = editTextCPFSignup.getText().toString();
            String password = editTextPasswordSignup.getText().toString();
            String renda = editTextRendaSignup.getText().toString();
            String dep = editTextDepSignup.getText().toString();

            if (name.isEmpty() || cpf.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Preencha os dados obrigatórios para criar sua conta", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(cpf, password);
            editor.putString(cpf + "_name", name);
            editor.putString(cpf + "_renda", renda);
            editor.putString(cpf + "_dep", dep);
            editor.apply();

            Toast.makeText(this, "Conta criada! Agora você já pode entrar.", Toast.LENGTH_SHORT).show();
            tabLayout.getTabAt(0).select(); // Volta para a aba de login
        });
    }
}
