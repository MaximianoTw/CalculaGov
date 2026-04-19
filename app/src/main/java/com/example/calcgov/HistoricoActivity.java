package com.example.calcgov;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class HistoricoActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private HistoricoAdapter adapter;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historico);

        sharedPreferences = getSharedPreferences("CalculosHistory", MODE_PRIVATE);
        
        recyclerView = findViewById(R.id.recyclerViewHistorico);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        loadHistorico();

        findViewById(R.id.buttonLimparHistorico).setOnClickListener(v -> {
            sharedPreferences.edit().clear().apply();
            loadHistorico();
            Toast.makeText(this, "Histórico limpo!", Toast.LENGTH_SHORT).show();
        });

        setupNavigation();
    }

    private void loadHistorico() {
        Map<String, ?> allEntries = sharedPreferences.getAll();
        List<String> logs = new ArrayList<>();
        List<String> keys = new ArrayList<>();
        
        // Vamos guardar a chave para poder excluir individualmente
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            keys.add(entry.getKey());
            logs.add(entry.getValue().toString());
        }
        
        View emptyView = findViewById(R.id.textViewVazio);
        if (logs.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            
            // Inverter para mostrar os mais recentes primeiro
            Collections.reverse(logs);
            Collections.reverse(keys);
            
            adapter = new HistoricoAdapter(logs, keys, (key, position) -> {
                showDeleteDialog(key, position);
            });
            recyclerView.setAdapter(adapter);
        }
    }

    private void showDeleteDialog(String key, int position) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Excluir Registro")
                .setMessage("Deseja remover este cálculo do seu histórico?")
                .setPositiveButton("Excluir", (dialog, which) -> {
                    sharedPreferences.edit().remove(key).apply();
                    loadHistorico();
                    Toast.makeText(this, "Registro removido", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void setupNavigation() {
        BottomNavigationView nav = findViewById(R.id.bottomNavigation);
        nav.setSelectedItemId(R.id.nav_historico);
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
            } else if (id == R.id.nav_perfil) {
                startActivity(new Intent(this, PerfilActivity.class));
                finish();
                return true;
            }
            return id == R.id.nav_historico;
        });
    }

    private interface OnItemActionClickListener {
        void onDeleteClick(String key, int position);
    }

    private static class HistoricoAdapter extends RecyclerView.Adapter<HistoricoAdapter.ViewHolder> {
        private final List<String> logs;
        private final List<String> keys;
        private final OnItemActionClickListener actionListener;

        public HistoricoAdapter(List<String> logs, List<String> keys, OnItemActionClickListener listener) {
            this.logs = logs;
            this.keys = keys;
            this.actionListener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_historico, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String data = logs.get(position);
            String key = keys.get(position);
            String[] parts = data.split("\\|");
            
            holder.btnDelete.setOnClickListener(v -> {
                actionListener.onDeleteClick(key, position);
            });

            if (parts.length >= 5) {
                holder.tvNome.setText(parts[0]);
                holder.tvData.setText(parts[1]);
                holder.tvStatus.setText(parts[2]);
                holder.tvValor.setText(parts[3]);
                holder.tvIRRF.setText("Imposto Retido: " + parts[4]);

                if (parts[2].contains("RESTITUIÇÃO")) {
                    holder.tvStatus.setTextColor(0xFF2E7D32); // Verde
                } else {
                    holder.tvStatus.setTextColor(0xFFD32F2F); // Vermelho
                }
            }
        }

        @Override
        public int getItemCount() { return logs.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvNome, tvData, tvStatus, tvValor, tvIRRF;
            View btnDelete;
            ViewHolder(View v) {
                super(v);
                tvNome = v.findViewById(R.id.textViewNomeHistorico);
                tvData = v.findViewById(R.id.textViewDataHistorico);
                tvStatus = v.findViewById(R.id.textViewStatusHistorico);
                tvValor = v.findViewById(R.id.textViewValorHistorico);
                tvIRRF = v.findViewById(R.id.textViewIRRFHistorico);
                btnDelete = v.findViewById(R.id.btnDeleteHistorico);
            }
        }
    }
}
