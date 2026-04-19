package com.example.calcgov;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ComprovantesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView textEmpty;
    private ComprovanteAdapter adapter;
    private List<File> filesList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comprovantes);

        recyclerView = findViewById(R.id.recyclerComprovantes);
        textEmpty = findViewById(R.id.textEmptyComprovantes);
        
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        
        loadComprovantes();
        setupNavigation();
    }

    private void loadComprovantes() {
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        filesList.clear();
        
        if (dir != null && dir.exists()) {
            File[] files = dir.listFiles((d, name) -> name.endsWith(".jpg") || name.endsWith(".png"));
            if (files != null) {
                Arrays.sort(files, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));
                filesList.addAll(Arrays.asList(files));
            }
        }

        if (filesList.isEmpty()) {
            textEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            textEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter = new ComprovanteAdapter(filesList, (file, position) -> {
                showDeleteDialog(file, position);
            });
            recyclerView.setAdapter(adapter);
        }
    }

    private void showDeleteDialog(File file, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir Comprovante")
                .setMessage("Deseja realmente excluir este documento? Esta ação não pode ser desfeita.")
                .setPositiveButton("Excluir", (dialog, which) -> {
                    if (file.delete()) {
                        adapter.removeAt(position);
                        Toast.makeText(this, "Comprovante excluído", Toast.LENGTH_SHORT).show();
                        if (filesList.isEmpty()) {
                            textEmpty.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        }
                    } else {
                        Toast.makeText(this, "Erro ao excluir arquivo", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void setupNavigation() {
        BottomNavigationView nav = findViewById(R.id.bottomNavigation);
        nav.getMenu().setGroupCheckable(0, false, true); 
        
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
            } else if (id == R.id.nav_perfil) {
                // Para simplificar, vamos deixar a biometria ser tratada na Home ao clicar nos cards
                startActivity(new Intent(this, PerfilActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }
}
