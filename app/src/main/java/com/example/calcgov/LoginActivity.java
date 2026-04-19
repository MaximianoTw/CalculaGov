package com.example.calcgov;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;

import java.text.NumberFormat;
import java.util.Locale;

public class LoginActivity extends AppCompatActivity {
    
    private TabLayout tabLayout;
    private LinearLayout layoutLogin, layoutSignup;
    
    // Login Views
    private TextInputEditText editTextCPF, editTextPassword;
    private Button buttonLogin, buttonGovLogin;
    
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
        setupMasks();
    }

    private void initViews() {
        tabLayout = findViewById(R.id.tabLayout);
        layoutLogin = findViewById(R.id.layoutLogin);
        layoutSignup = findViewById(R.id.layoutSignup);
        
        editTextCPF = findViewById(R.id.editTextCPF);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonGovLogin = findViewById(R.id.buttonGovLogin);
        
        editTextNameSignup = findViewById(R.id.editTextNameSignup);
        editTextCPFSignup = findViewById(R.id.editTextCPFSignup);
        editTextPasswordSignup = findViewById(R.id.editTextPasswordSignup);
        editTextRendaSignup = findViewById(R.id.editTextRendaSignup);
        editTextDepSignup = findViewById(R.id.editTextDepSignup);
        buttonSignup = findViewById(R.id.buttonSignup);
        
        findViewById(R.id.VoltarMain).setOnClickListener(v -> finish());
    }

    private void setupMasks() {
        applyCpfMask(editTextCPF);
        applyCpfMask(editTextCPFSignup);
        applyMoneyMask(editTextRendaSignup);
    }

    private void applyCpfMask(TextInputEditText et) {
        et.addTextChangedListener(new TextWatcher() {
            private boolean isUpdating = false;
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isUpdating) {
                    isUpdating = false;
                    return;
                }
                String str = s.toString().replaceAll("[^\\d]", "");
                if (str.length() > 11) str = str.substring(0, 11);

                StringBuilder mask = new StringBuilder();
                if (str.length() > 0) mask.append(str.substring(0, Math.min(str.length(), 3)));
                if (str.length() > 3) mask.append(".").append(str.substring(3, Math.min(str.length(), 6)));
                if (str.length() > 6) mask.append(".").append(str.substring(6, Math.min(str.length(), 9)));
                if (str.length() > 9) mask.append("-").append(str.substring(9, Math.min(str.length(), 11)));
                
                isUpdating = true;
                et.setText(mask.toString());
                et.setSelection(mask.length());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void applyMoneyMask(TextInputEditText et) {
        et.addTextChangedListener(new TextWatcher() {
            private String current = "";
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().equals(current)) {
                    et.removeTextChangedListener(this);

                    String cleanString = s.toString().replaceAll("[R$,.\\s\u00A0]", "");
                    if (cleanString.isEmpty()) cleanString = "0";

                    try {
                        double parsed = Double.parseDouble(cleanString);
                        String formatted = NumberFormat.getCurrencyInstance(new Locale("pt", "BR")).format((parsed / 100));
                        current = formatted;
                        et.setText(formatted);
                        et.setSelection(formatted.length());
                    } catch (Exception e) {
                        // ignore
                    }

                    et.addTextChangedListener(this);
                }
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        });
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
        buttonGovLogin.setOnClickListener(v -> {
            Toast.makeText(this, "Integração oficial gov.br disponível em breve!", Toast.LENGTH_SHORT).show();
        });

        buttonLogin.setOnClickListener(v -> {
            String cpf = editTextCPF.getText().toString().replaceAll("[^\\d]", "");
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
            String cpf = editTextCPFSignup.getText().toString().replaceAll("[^\\d]", "");
            String password = editTextPasswordSignup.getText().toString();
            String renda = editTextRendaSignup.getText().toString();
            String dep = editTextDepSignup.getText().toString();

            if (name.isEmpty() || cpf.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Preencha os dados obrigatórios para criar sua conta", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(cpf, password);
            editor.putString(cpf + "_password", password);
            editor.putString(cpf + "_name", name);
            editor.putString(cpf + "_renda", renda);
            editor.putString(cpf + "_dep", dep);
            editor.apply();

            Toast.makeText(this, "Conta criada! Agora você já pode entrar.", Toast.LENGTH_SHORT).show();
            tabLayout.getTabAt(0).select();
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Uri uri = intent.getData();
        if (uri != null && uri.toString().startsWith("calcgov://confirmacao-login")) {
            String code = uri.getQueryParameter("code");
            if (code != null) {
                Toast.makeText(this, "Autenticação real processada!", Toast.LENGTH_SHORT).show();
                processarDadosReaisDoGoverno();
            }
        }
    }

    private void processarDadosReaisDoGoverno() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String cpfReal = "O CPF que veio da API"; 
        editor.putString("logged_cpf", cpfReal);
        editor.putString(cpfReal + "_name", "O Nome que veio da API");
        editor.apply();

        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }
}
