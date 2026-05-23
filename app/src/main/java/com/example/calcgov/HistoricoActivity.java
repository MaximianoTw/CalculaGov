package com.example.calcgov;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoricoActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private HistoricoAdapter adapter;
    private SharedPreferences sharedPreferences;
    private View rootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historico);

        sharedPreferences = getSharedPreferences("CalculosHistory", MODE_PRIVATE);
        rootView = findViewById(R.id.historicoRoot);
        
        recyclerView = findViewById(R.id.recyclerViewHistorico);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        loadHistorico();

        findViewById(R.id.buttonLimparHistorico).setOnClickListener(v -> {
            showClearHistoryDialog();
        });

        setupNavigation();
    }

    private void loadHistorico() {
        Map<String, ?> allEntries = sharedPreferences.getAll();
        List<HistoryEntry> historyEntries = new ArrayList<>();
        
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            if (entry.getKey().startsWith("log_") && entry.getValue() != null) {
                historyEntries.add(new HistoryEntry(entry.getKey(), entry.getValue().toString()));
            }
        }

        historyEntries.sort((first, second) -> Long.compare(second.timestamp, first.timestamp));
        
        View emptyView = findViewById(R.id.textViewVazio);
        if (historyEntries.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            List<String> logs = new ArrayList<>();
            List<String> keys = new ArrayList<>();
            for (HistoryEntry entry : historyEntries) {
                keys.add(entry.key);
                logs.add(entry.log);
            }

            adapter = new HistoricoAdapter(logs, keys, (key, position) -> {
                showDeleteDialog(key, logs.get(position));
            });
            recyclerView.setAdapter(adapter);
        }
    }

    private void showDeleteDialog(String key, String log) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_confirm_delete, null);
        TextView title = dialogView.findViewById(R.id.textDeleteDialogTitle);
        TextView message = dialogView.findViewById(R.id.textDeleteDialogMessage);
        TextView details = dialogView.findViewById(R.id.textDeleteDialogDetails);

        title.setText("Excluir registro?");
        message.setText("Este cálculo será removido do histórico.");
        details.setText(formatHistoryDetails(log));

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Excluir", (dialogInterface, which) -> deleteHistoryEntry(key, log))
                .create();

        dialog.setOnShowListener(dialogInterface -> styleDangerDialogButton(dialog));
        dialog.show();
    }

    private void showClearHistoryDialog() {
        Map<String, String> snapshot = getHistorySnapshot();
        if (snapshot.isEmpty()) {
            showSnackbar("Não há registros para excluir.", null);
            return;
        }

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_confirm_delete, null);
        TextView title = dialogView.findViewById(R.id.textDeleteDialogTitle);
        TextView message = dialogView.findViewById(R.id.textDeleteDialogMessage);
        TextView details = dialogView.findViewById(R.id.textDeleteDialogDetails);

        title.setText("Limpar histórico?");
        message.setText("Todos os cálculos salvos serão removidos.");
        details.setText(snapshot.size() + " registro(s) serão excluídos do histórico.");

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Limpar", (dialogInterface, which) -> clearHistory(snapshot))
                .create();

        dialog.setOnShowListener(dialogInterface -> styleDangerDialogButton(dialog));
        dialog.show();
    }

    private void deleteHistoryEntry(String key, String log) {
        sharedPreferences.edit().remove(key).apply();
        loadHistorico();

        showSnackbar("Registro excluído.", () -> {
            sharedPreferences.edit().putString(key, log).apply();
            loadHistorico();
        });
    }

    private void clearHistory(Map<String, String> snapshot) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        for (String key : snapshot.keySet()) {
            editor.remove(key);
        }
        editor.apply();
        loadHistorico();

        showSnackbar("Histórico limpo.", () -> {
            SharedPreferences.Editor restoreEditor = sharedPreferences.edit();
            for (Map.Entry<String, String> entry : snapshot.entrySet()) {
                restoreEditor.putString(entry.getKey(), entry.getValue());
            }
            restoreEditor.apply();
            loadHistorico();
        });
    }

    private void showSnackbar(String message, Runnable undoAction) {
        Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_LONG);
        snackbar.setAnchorView(findViewById(R.id.bottomNavigation));
        snackbar.setActionTextColor(0xFFFFCC00);

        if (undoAction != null) {
            snackbar.setAction("DESFAZER", v -> undoAction.run());
        }

        snackbar.show();
    }

    private void styleDangerDialogButton(AlertDialog dialog) {
        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        if (positiveButton != null) {
            positiveButton.setTextColor(0xFFD32F2F);
        }
        if (negativeButton != null) {
            negativeButton.setTextColor(0xFF00387E);
        }
    }

    private Map<String, String> getHistorySnapshot() {
        Map<String, String> snapshot = new HashMap<>();
        for (Map.Entry<String, ?> entry : sharedPreferences.getAll().entrySet()) {
            if (entry.getKey().startsWith("log_") && entry.getValue() != null) {
                snapshot.put(entry.getKey(), entry.getValue().toString());
            }
        }
        return snapshot;
    }

    private String formatHistoryDetails(String log) {
        String[] parts = log.split("\\|");
        if (parts.length < 5) {
            return "Registro selecionado";
        }

        return "Contribuinte: " + parts[0]
                + "\nData: " + parts[1]
                + "\nResultado: " + parts[2]
                + "\nValor: " + parts[3]
                + "\nIRRF: " + parts[4];
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

    private static class HistoryEntry {
        final String key;
        final String log;
        final long timestamp;

        HistoryEntry(String key, String log) {
            this.key = key;
            this.log = log;
            this.timestamp = parseTimestamp(key);
        }

        private static long parseTimestamp(String key) {
            try {
                return Long.parseLong(key.substring(4));
            } catch (Exception e) {
                return 0;
            }
        }
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
