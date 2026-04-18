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
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            logs.add(entry.getValue().toString());
        }
        
        View emptyView = findViewById(R.id.textViewVazio);
        if (logs.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            Collections.reverse(logs);
            adapter = new HistoricoAdapter(logs);
            recyclerView.setAdapter(adapter);
        }
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
            }
            return id == R.id.nav_historico;
        });
    }

    private static class HistoricoAdapter extends RecyclerView.Adapter<HistoricoAdapter.ViewHolder> {
        private final List<String> logs;

        public HistoricoAdapter(List<String> logs) { this.logs = logs; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_historico, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String data = logs.get(position);
            String[] parts = data.split("\\|");
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
            ViewHolder(View v) {
                super(v);
                tvNome = v.findViewById(R.id.textViewNomeHistorico);
                tvData = v.findViewById(R.id.textViewDataHistorico);
                tvStatus = v.findViewById(R.id.textViewStatusHistorico);
                tvValor = v.findViewById(R.id.textViewValorHistorico);
                tvIRRF = v.findViewById(R.id.textViewIRRFHistorico);
            }
        }
    }
}
