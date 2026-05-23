package com.example.calcgov;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    private TextView textWelcome, textLastResultValue, textLastResultStatus;
    private TextView textDataInicio, textDataFim;
    private View cardProfileAlert, cardLastResult;
    private SharedPreferences userPrefs, historyPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        userPrefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
        historyPrefs = getSharedPreferences("CalculosHistory", MODE_PRIVATE);

        initViews();
        loadUserData();
        loadLastSimulation();
        setupNavigation();
        fetchFiscalCalendar();

        findViewById(R.id.cardIR).setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, CalculoImpostoRendaActivity.class));
        });

        findViewById(R.id.btnPowerOff).setOnClickListener(v -> {
            logout();
        });
        
        cardProfileAlert.setOnClickListener(v -> {
            checkBiometricsAndNavigate(PerfilActivity.class);
        });

        findViewById(R.id.cardDocumentos).setOnClickListener(v -> {
            startActivity(new Intent(this, ComprovantesActivity.class));
        });
    }

    private void initViews() {
        textWelcome = findViewById(R.id.textWelcome);
        textLastResultValue = findViewById(R.id.textLastResultValue);
        textLastResultStatus = findViewById(R.id.textLastResultStatus);
        textDataInicio = findViewById(R.id.textDataInicio);
        textDataFim = findViewById(R.id.textDataFim);
        cardProfileAlert = findViewById(R.id.cardProfileAlert);
        cardLastResult = findViewById(R.id.cardLastResult);
    }

    private void logout() {
        userPrefs.edit().remove("logged_cpf").apply();

        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        Toast.makeText(this, "Sessão encerrada.", Toast.LENGTH_SHORT).show();
    }

    private void checkBiometricsAndNavigate(Class<?> targetActivity) {
        BiometricHelper.authenticateIfEnabled(this, new BiometricHelper.BiometricCallback() {
            @Override
            public void onAuthenticationSucceeded() {
                startActivity(new Intent(HomeActivity.this, targetActivity));
            }

            @Override
            public void onAuthenticationError(String error) {
                Toast.makeText(HomeActivity.this, "Erro de autenticação: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchFiscalCalendar() {
        ProgressBar progress = findViewById(R.id.progressCalendar);
        progress.setVisibility(View.VISIBLE);
        
        // Simulação de busca dinâmica (Google/API)
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            textDataInicio.setText("Início das Declarações: 15 de Março de 2026");
            textDataFim.setText("Prazo Final: 31 de Maio de 2026");
            progress.setVisibility(View.GONE);
        }, 1500); 
    }

    private void loadUserData() {
        String loggedCpf = userPrefs.getString("logged_cpf", "");
        if (!loggedCpf.isEmpty()) {
            String name = userPrefs.getString(loggedCpf + "_name", "Cidadão");
            textWelcome.setText("Olá, " + name.split(" ")[0] + "!");

            String renda = userPrefs.getString(loggedCpf + "_renda", "");
            if (renda.isEmpty()) {
                cardProfileAlert.setVisibility(View.VISIBLE);
            } else {
                cardProfileAlert.setVisibility(View.GONE);
            }
        }
    }

    private void loadLastSimulation() {
        Map<String, ?> allEntries = historyPrefs.getAll();
        String latestKey = null;
        long maxTimestamp = -1;

        for (String key : allEntries.keySet()) {
            if (key.startsWith("log_")) {
                try {
                    long timestamp = Long.parseLong(key.substring(4));
                    if (timestamp > maxTimestamp) {
                        maxTimestamp = timestamp;
                        latestKey = key;
                    }
                } catch (Exception ignored) {}
            }
        }

        if (latestKey != null) {
            String log = (String) allEntries.get(latestKey);
            if (log != null) {
                String[] parts = log.split("\\|");
                if (parts.length >= 5) {
                    cardLastResult.setVisibility(View.VISIBLE);
                    textLastResultStatus.setText(parts[2]);
                    textLastResultValue.setText(parts[3]);
                }
            }
        } else {
            cardLastResult.setVisibility(View.GONE);
        }
    }

    private void setupNavigation() {
        BottomNavigationView nav = findViewById(R.id.bottomNavigation);
        nav.setSelectedItemId(R.id.nav_home);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_calculo) {
                startActivity(new Intent(this, CalculoImpostoRendaActivity.class));
                return true;
            } else if (id == R.id.nav_historico) {
                startActivity(new Intent(this, HistoricoActivity.class));
                return true;
            } else if (id == R.id.nav_perfil) {
                checkBiometricsAndNavigate(PerfilActivity.class);
                return true;
            }
            return id == R.id.nav_home;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserData();
        loadLastSimulation();
    }
}
