package com.example.calcgov;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Locale;

public class PerfilActivity extends AppCompatActivity {

    private TextInputEditText editNome, editRenda, editDep, editCPF, editRG, editEmpresa, 
                             editNomeDep, editCPFDep, editNascimento, editEstadoCivil, editTitulo;
    private Button btnSalvar, btnDesbloquear;
    private TextView txtStatusGov;
    private ImageView imgSeloGov;
    private LinearLayout containerDep, layoutStatusGov;
    private SwitchMaterial switchBiometria;
    private SharedPreferences sharedPreferences;
    private String loggedCpf;
    private boolean isUpdatingBiometricSwitch = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        sharedPreferences = getSharedPreferences("myPrefs", MODE_PRIVATE);
        loggedCpf = sharedPreferences.getString("logged_cpf", "");

        initViews();
        loadPerfilData();
        setupBiometricSwitch();
        setupNavigation();

        btnSalvar.setOnClickListener(v -> salvarPerfil());
        btnDesbloquear.setOnClickListener(v -> solicitarSenhaParaDesbloqueio());
    }

    private void initViews() {
        editNome = findViewById(R.id.editPerfilNome);
        editCPF = findViewById(R.id.editPerfilCPF);
        editRG = findViewById(R.id.editPerfilRG);
        editNascimento = findViewById(R.id.editPerfilNascimento);
        editEstadoCivil = findViewById(R.id.editPerfilEstadoCivil);
        editTitulo = findViewById(R.id.editPerfilTitulo);
        editEmpresa = findViewById(R.id.editPerfilEmpresa);
        editRenda = findViewById(R.id.editPerfilRenda);
        editDep = findViewById(R.id.editPerfilDep);
        editNomeDep = findViewById(R.id.editNomeDepInfo);
        editCPFDep = findViewById(R.id.editCPFDepInfo);
        
        containerDep = findViewById(R.id.containerDependentes);
        layoutStatusGov = findViewById(R.id.layoutStatusGov);
        btnSalvar = findViewById(R.id.buttonSalvarPerfil);
        btnDesbloquear = findViewById(R.id.buttonDesbloquear);
        txtStatusGov = findViewById(R.id.txtStatusGov);
        imgSeloGov = findViewById(R.id.imgSeloGov);
        switchBiometria = findViewById(R.id.switchBiometria);

        setupMasks();
        
        editDep.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                try {
                    int num = Integer.parseInt(s.toString());
                    containerDep.setVisibility(num > 0 ? View.VISIBLE : View.GONE);
                } catch (Exception e) {
                    containerDep.setVisibility(View.GONE);
                }
            }
        });
    }

    private void setupMasks() {
        applyCpfMask(editCPF);
        applyCpfMask(editCPFDep);
        applyRgMask(editRG);
        applyMoneyMask(editRenda);
        setupDatePicker();
    }

    private void setupDatePicker() {
        editNascimento.setFocusable(false);
        editNascimento.setOnClickListener(v -> {
            if (!editNascimento.isEnabled()) return;
            Calendar cal = Calendar.getInstance();
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            int day = cal.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                PerfilActivity.this,
                (view, year1, month1, dayOfMonth) -> {
                    String date = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month1 + 1, year1);
                    editNascimento.setText(date);
                },
                year, month, day
            );
            datePickerDialog.show();
        });
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

    private void applyRgMask(TextInputEditText et) {
        et.addTextChangedListener(new TextWatcher() {
            private boolean isUpdating = false;
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isUpdating) {
                    isUpdating = false;
                    return;
                }
                String str = s.toString().replaceAll("[^\\d]", "");
                if (str.length() > 9) str = str.substring(0, 9);

                StringBuilder mask = new StringBuilder();
                // Mask: 00.000.000-0
                if (str.length() > 0) mask.append(str.substring(0, Math.min(str.length(), 2)));
                if (str.length() > 2) mask.append(".").append(str.substring(2, Math.min(str.length(), 5)));
                if (str.length() > 5) mask.append(".").append(str.substring(5, Math.min(str.length(), 8)));
                if (str.length() > 8) mask.append("-").append(str.substring(8, Math.min(str.length(), 9)));
                
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

    private void loadPerfilData() {
        if (!loggedCpf.isEmpty()) {
            editNome.setText(sharedPreferences.getString(loggedCpf + "_name", ""));
            editCPF.setText(loggedCpf);
            editRG.setText(sharedPreferences.getString(loggedCpf + "_rg", ""));
            editNascimento.setText(sharedPreferences.getString(loggedCpf + "_nascimento", ""));
            editEstadoCivil.setText(sharedPreferences.getString(loggedCpf + "_estado_civil", ""));
            editTitulo.setText(sharedPreferences.getString(loggedCpf + "_titulo", ""));
            editEmpresa.setText(sharedPreferences.getString(loggedCpf + "_empresa", ""));
            editRenda.setText(sharedPreferences.getString(loggedCpf + "_renda", ""));
            editDep.setText(sharedPreferences.getString(loggedCpf + "_dep", ""));
            editNomeDep.setText(sharedPreferences.getString(loggedCpf + "_dep_nome", ""));
            editCPFDep.setText(sharedPreferences.getString(loggedCpf + "_dep_cpf", ""));
            switchBiometria.setChecked(BiometricHelper.isBiometricEnabled(this));

            boolean isGovVerified = sharedPreferences.getString(loggedCpf + "_is_gov_verified", "false").equals("true");
            if (isGovVerified) {
                layoutStatusGov.setVisibility(View.VISIBLE);
                txtStatusGov.setText("Perfil Prata/Ouro verificado via gov.br");
                bloquearCampos(true);
            } else {
                if (!editNome.getText().toString().isEmpty()) {
                    bloquearCampos(true);
                }
            }
        }
    }

    private void setupBiometricSwitch() {
        switchBiometria.setEnabled(!loggedCpf.isEmpty());
        switchBiometria.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isUpdatingBiometricSwitch) return;

            if (!isChecked) {
                BiometricHelper.setBiometricEnabled(this, false);
                Toast.makeText(this, "Biometria desativada.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!BiometricHelper.canUseBiometric(this)) {
                updateBiometricSwitch(false);
                BiometricHelper.setBiometricEnabled(this, false);
                Toast.makeText(this, "Biometria indisponível ou não configurada neste dispositivo.", Toast.LENGTH_LONG).show();
                return;
            }

            BiometricHelper.showBiometricPrompt(this, new BiometricHelper.BiometricCallback() {
                @Override
                public void onAuthenticationSucceeded() {
                    BiometricHelper.setBiometricEnabled(PerfilActivity.this, true);
                    Toast.makeText(PerfilActivity.this, "Biometria ativada.", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onAuthenticationError(String error) {
                    updateBiometricSwitch(false);
                    BiometricHelper.setBiometricEnabled(PerfilActivity.this, false);
                    Toast.makeText(PerfilActivity.this, "Não foi possível ativar a biometria.", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void updateBiometricSwitch(boolean checked) {
        isUpdatingBiometricSwitch = true;
        switchBiometria.setChecked(checked);
        isUpdatingBiometricSwitch = false;
    }

    private void bloquearCampos(boolean bloquear) {
        editNome.setEnabled(!bloquear);
        editCPF.setEnabled(false);
        editRG.setEnabled(!bloquear);
        editNascimento.setEnabled(!bloquear);
        editEstadoCivil.setEnabled(!bloquear);
        editTitulo.setEnabled(!bloquear);
        editEmpresa.setEnabled(!bloquear);
        editRenda.setEnabled(!bloquear);
        editDep.setEnabled(!bloquear);
        editNomeDep.setEnabled(!bloquear);
        editCPFDep.setEnabled(!bloquear);

        btnSalvar.setVisibility(bloquear ? View.GONE : View.VISIBLE);
        btnDesbloquear.setVisibility(bloquear ? View.VISIBLE : View.GONE);
    }

    private void solicitarSenhaParaDesbloqueio() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Segurança de Perfil");
        builder.setMessage("Confirme sua senha de acesso para habilitar a edição dos seus dados.");

        final TextInputLayout inputLayout = new TextInputLayout(this);
        inputLayout.setPadding(40, 20, 40, 0);
        final TextInputEditText input = new TextInputEditText(this);
        input.setHint("Senha");
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        inputLayout.addView(input);
        builder.setView(inputLayout);

        builder.setPositiveButton("DESBLOQUEAR", (dialog, which) -> {
            String senhaDigitada = input.getText().toString();
            String senhaSalva = sharedPreferences.getString(loggedCpf + "_password", "");

            if (senhaDigitada.equals(senhaSalva)) {
                bloquearCampos(false);
                Toast.makeText(this, "Edição habilitada!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Senha incorreta. Tente novamente.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("CANCELAR", (dialog, hide) -> dialog.cancel());
        builder.show();
    }

    private void salvarPerfil() {
        String nome = editNome.getText().toString();
        String cpfDep = editCPFDep.getText().toString().replaceAll("[^\\d]", "");
        String rg = editRG.getText().toString();

        if (nome.isEmpty()) {
            Toast.makeText(this, "O nome não pode estar vazio.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!rg.isEmpty() && rg.replaceAll("[^\\d]", "").length() < 7) {
            editRG.setError("RG Inválido");
            return;
        }

        if (containerDep.getVisibility() == View.VISIBLE && !cpfDep.isEmpty()) {
            if (!isValidCPF(cpfDep)) {
                editCPFDep.setError("CPF do dependente é inválido!");
                return;
            }
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(loggedCpf + "_name", nome);
        editor.putString(loggedCpf + "_rg", editRG.getText().toString());
        editor.putString(loggedCpf + "_nascimento", editNascimento.getText().toString());
        editor.putString(loggedCpf + "_estado_civil", editEstadoCivil.getText().toString());
        editor.putString(loggedCpf + "_titulo", editTitulo.getText().toString());
        editor.putString(loggedCpf + "_empresa", editEmpresa.getText().toString());
        editor.putString(loggedCpf + "_renda", editRenda.getText().toString());
        editor.putString(loggedCpf + "_dep", editDep.getText().toString());
        editor.putString(loggedCpf + "_dep_nome", editNomeDep.getText().toString());
        editor.putString(loggedCpf + "_dep_cpf", editCPFDep.getText().toString());
        editor.apply();

        Toast.makeText(this, "Perfil atualizado com sucesso!", Toast.LENGTH_SHORT).show();
        bloquearCampos(true);
    }

    private boolean isValidCPF(String cpf) {
        if (cpf == null || cpf.length() != 11 || cpf.matches("(\\d)\\1{10}")) return false;
        try {
            int d1 = 0, d2 = 0;
            int digit1, digit2, rest;
            int nCount;
            for (nCount = 1; nCount < cpf.length() - 1; nCount++) {
                int digito = Integer.parseInt(cpf.substring(nCount - 1, nCount));
                d1 = d1 + (11 - nCount) * digito;
                d2 = d2 + (12 - nCount) * digito;
            }
            rest = (d1 % 11);
            if (rest < 2) digit1 = 0;
            else digit1 = 11 - rest;
            d2 = d2 + 2 * digit1;
            rest = (d2 % 11);
            if (rest < 2) digit2 = 0;
            else digit2 = 11 - rest;
            String nDigVerific = cpf.substring(cpf.length() - 2);
            String nDigResult = String.valueOf(digit1) + String.valueOf(digit2);
            return nDigVerific.equals(nDigResult);
        } catch (Exception e) {
            return false;
        }
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
