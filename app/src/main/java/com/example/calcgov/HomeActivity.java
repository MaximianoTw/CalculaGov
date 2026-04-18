package com.example.calcgov;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    private TextView textWelcome, textLastResultValue, textLastResultStatus;
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

        findViewById(R.id.cardIR).setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, CalculoImpostoRendaActivity.class));
        });

        findViewById(R.id.VoltarHome).setOnClickListener(v -> {
            SharedPreferences.Editor editor = userPrefs.edit();
            editor.remove("logged_cpf");
            editor.apply();
            finish();
        });
        
        cardProfileAlert.setOnClickListener(v -> {
            startActivity(new Intent(this, PerfilActivity.class));
        });
    }

    private void initViews() {
        textWelcome = findViewById(R.id.textWelcome);
        textLastResultValue = findViewById(R.id.textLastResultValue);
        textLastResultStatus = findViewById(R.id.textLastResultStatus);
        cardProfileAlert = findViewById(R.id.cardProfileAlert);
        cardLastResult = findViewById(R.id.cardLastResult);
    }

    private void loadUserData() {
        String loggedCpf = userPrefs.getString("logged_cpf", "");
        if (!loggedCpf.isEmpty()) {
            String name = userPrefs.getString(loggedCpf + "_name", "Cidadão");
            textWelcome.setText("Olá, " + name.split(" ")[0] + "!");

            // Check if profile is incomplete
            String renda = userPrefs.getString(loggedCpf + "_renda", "");
            if (renda.isEmpty()) {
                cardProfileAlert.setVisibility(View.VISIBLE);
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
                startActivity(new Intent(this, PerfilActivity.class));
                return true;
            }
            return id == R.id.nav_home;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserData(); // Update greeting/alerts if changed in profile
        loadLastSimulation(); // Update last simulation if a new one was made
    }
}
