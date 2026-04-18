package com.example.calcgov;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;

public class PerfilActivity extends AppCompatActivity {

    private TextInputEditText editNome, editRenda, editDep;
    private Button btnSalvar;
    private SharedPreferences sharedPreferences;
    private String loggedCpf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        sharedPreferences = getSharedPreferences("myPrefs", MODE_PRIVATE);
        loggedCpf = sharedPreferences.getString("logged_cpf", "");

        initViews();
        loadPerfilData();
        setupNavigation();

        btnSalvar.setOnClickListener(v -> salvarPerfil());
    }

    private void initViews() {
        editNome = findViewById(R.id.editPerfilNome);
        editRenda = findViewById(R.id.editPerfilRenda);
        editDep = findViewById(R.id.editPerfilDep);
        btnSalvar = findViewById(R.id.buttonSalvarPerfil);
    }

    private void loadPerfilData() {
        if (!loggedCpf.isEmpty()) {
            editNome.setText(sharedPreferences.getString(loggedCpf + "_name", ""));
            editRenda.setText(sharedPreferences.getString(loggedCpf + "_renda", ""));
            editDep.setText(sharedPreferences.getString(loggedCpf + "_dep", ""));
        }
    }

    private void salvarPerfil() {
        String nome = editNome.getText().toString();
        String renda = editRenda.getText().toString();
        String dep = editDep.getText().toString();

        if (nome.isEmpty()) {
            Toast.makeText(this, "O nome não pode estar vazio.", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(loggedCpf + "_name", nome);
        editor.putString(loggedCpf + "_renda", renda);
        editor.putString(loggedCpf + "_dep", dep);
        editor.apply();

        Toast.makeText(this, "Perfil atualizado com sucesso!", Toast.LENGTH_SHORT).show();
    }

    private void setupNavigation() {
        BottomNavigationView nav = findViewById(R.id.bottomNavigation);
        nav.setSelectedItemId(R.id.nav_perfil);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, HomeActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_calculo) {
                startActivity(new Intent(this, CalculoImpostoRendaActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_historico) {
                startActivity(new Intent(this, HistoricoActivity.class));
                finish();
                return true;
            }
            return id == R.id.nav_perfil;
        });
    }
}
