package com.example.calcgov;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

import java.io.File;
import java.io.FileOutputStream;
import java.text.NumberFormat;
import java.util.Locale;

public class CalculoImpostoRendaActivity extends AppCompatActivity {

    private TextInputEditText editTextNome, editTextRenda, editTextOutrosRendimentos, 
            editTextPrevidencia, editTextDependentes, editTextSaude, editTextEduPensao, editTextIRRF;
    private TextView textResumoResultado;
    private View cardUltimoResultado;
    private Button buttonCalcular, buttonDownloadPDF, buttonVoltar, buttonAutoPreencher;
    private SharedPreferences sharedPreferences, userPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculo_imposto_renda);

        sharedPreferences = getSharedPreferences("CalculosHistory", MODE_PRIVATE);
        userPrefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
        initViews();
        setupNavigation();
        setupBackConfirmation();
        
        buttonCalcular.setOnClickListener(v -> processarCalculo());
        buttonDownloadPDF.setOnClickListener(v -> gerarRelatorioPDF());
        buttonVoltar.setOnClickListener(v -> confirmarSaida());
        buttonAutoPreencher.setOnClickListener(v -> autoPreencher());
    }

    private void setupBackConfirmation() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                confirmarSaida();
            }
        });
    }

    private void confirmarSaida() {
        new AlertDialog.Builder(this)
                .setTitle("Sair do preenchimento?")
                .setMessage("Se você sair agora, os dados digitados não serão salvos. Deseja continuar?")
                .setPositiveButton("Sair", (dialog, which) -> finish())
                .setNegativeButton("Continuar", null)
                .show();
    }

    private void setupNavigation() {
        BottomNavigationView nav = findViewById(R.id.bottomNavigation);
        nav.setSelectedItemId(R.id.nav_calculo);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                confirmarSaidaESairPara(HomeActivity.class);
                return true;
            } else if (id == R.id.nav_historico) {
                confirmarSaidaESairPara(HistoricoActivity.class);
                return true;
            }
            return id == R.id.nav_calculo;
        });
    }

    private void confirmarSaidaESairPara(Class<?> activityClass) {
        new AlertDialog.Builder(this)
                .setTitle("Sair do preenchimento?")
                .setMessage("Se você mudar de página agora, os dados não serão salvos.")
                .setPositiveButton("Sair", (dialog, which) -> {
                    startActivity(new Intent(this, activityClass));
                    finish();
                })
                .setNegativeButton("Ficar", null)
                .show();
    }

    private void initViews() {
        editTextNome = findViewById(R.id.editTextNome);
        editTextRenda = findViewById(R.id.editTextRenda);
        editTextOutrosRendimentos = findViewById(R.id.editTextOutrosRendimentos);
        editTextPrevidencia = findViewById(R.id.editTextPrevidencia);
        editTextDependentes = findViewById(R.id.editTextDependentes);
        editTextSaude = findViewById(R.id.editTextSaude);
        editTextEduPensao = findViewById(R.id.editTextEduPensao);
        editTextIRRF = findViewById(R.id.editTextIRRF);
        
        textResumoResultado = findViewById(R.id.textResumoResultado);
        cardUltimoResultado = findViewById(R.id.cardUltimoResultado);
        
        buttonCalcular = findViewById(R.id.buttonCalcular);
        buttonDownloadPDF = findViewById(R.id.buttonDownloadPDF);
        buttonVoltar = findViewById(R.id.VoltarHome);
        buttonAutoPreencher = findViewById(R.id.buttonAutoPreencher);
    }

    private void autoPreencher() {
        String loggedCpf = userPrefs.getString("logged_cpf", "");
        if (loggedCpf.isEmpty()) {
            Toast.makeText(this, "Erro: Usuário não identificado.", Toast.LENGTH_SHORT).show();
            return;
        }

        String nome = userPrefs.getString(loggedCpf + "_name", "");
        String renda = userPrefs.getString(loggedCpf + "_renda", "");
        String dependentes = userPrefs.getString(loggedCpf + "_dep", "");

        if (nome.isEmpty() && renda.isEmpty() && dependentes.isEmpty()) {
            Toast.makeText(this, "Seu perfil está incompleto. Vá em 'Perfil' para preencher.", Toast.LENGTH_LONG).show();
            return;
        }

        editTextNome.setText(nome);
        editTextRenda.setText(renda);
        editTextDependentes.setText(dependentes);
        
        Toast.makeText(this, "Dados do perfil carregados!", Toast.LENGTH_SHORT).show();
    }

    private void processarCalculo() {
        String nome = editTextNome.getText().toString();
        if (nome.isEmpty()) {
            Toast.makeText(this, "Por favor, informe seu nome.", Toast.LENGTH_SHORT).show();
            return;
        }

        double rendaPrincipal = parseDouble(editTextRenda.getText().toString());
        double outrosRendimentos = parseDouble(editTextOutrosRendimentos.getText().toString());
        double totalRendimentos = rendaPrincipal + outrosRendimentos;

        double previdência = parseDouble(editTextPrevidencia.getText().toString());
        double saude = parseDouble(editTextSaude.getText().toString());
        double eduPensao = parseDouble(editTextEduPensao.getText().toString());
        int dependentes = (int) parseDouble(editTextDependentes.getText().toString());
        double irrfJaPago = parseDouble(editTextIRRF.getText().toString());

        // 1. DEDUÇÕES
        double deducaoDependentes = dependentes * 189.59;
        double totalDeducoes = previdência + saude + eduPensao + deducaoDependentes;

        // 2. BASE DE CÁLCULO
        double baseCalculo = totalRendimentos - totalDeducoes;
        if (baseCalculo < 0) baseCalculo = 0;

        // 3. CÁLCULO DO IMPOSTO DEVIDO (Tabela 2024)
        double impostoDevido = calcularIRPF(baseCalculo);

        // 4. RESULTADO FINAL (Restituição ou Pagamento)
        double resultadoFinal = impostoDevido - irrfJaPago;

        exibirResultado(nome, totalRendimentos, totalDeducoes, baseCalculo, impostoDevido, irrfJaPago, resultadoFinal);
    }

    private double parseDouble(String val) {
        if (val == null || val.isEmpty()) return 0;
        try { return Double.parseDouble(val.replace(",", ".")); } catch (Exception e) { return 0; }
    }

    private double calcularIRPF(double base) {
        if (base <= 2259.20) return 0;
        if (base <= 2826.65) return (base * 0.075) - 169.44;
        if (base <= 3751.05) return (base * 0.15) - 381.44;
        if (base <= 4664.68) return (base * 0.225) - 662.77;
        return (base * 0.275) - 896.00;
    }

    private void salvarNoHistorico(String nome, double pago, double finalResult) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String timestamp = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(new java.util.Date());
        String status = finalResult < 0 ? "RESTITUIÇÃO A RECEBER" : "IMPOSTO A PAGAR";
        String valorFormatado = NumberFormat.getCurrencyInstance(new Locale("pt", "BR")).format(Math.abs(finalResult));
        String irrfFormatado = NumberFormat.getCurrencyInstance(new Locale("pt", "BR")).format(pago);
        
        // Formato: Nome|Data|Status|Valor|IRRF
        String logEntry = nome + "|" + timestamp + "|" + status + "|" + valorFormatado + "|" + irrfFormatado;
        editor.putString("log_" + System.currentTimeMillis(), logEntry);
        editor.apply();
    }

    private void exibirResultado(String nome, double rendimentos, double deducoes, double base, double devido, double pago, double finalResult) {
        salvarNoHistorico(nome, pago, finalResult);
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        
        StringBuilder sb = new StringBuilder();
        sb.append("Contribuinte: ").append(nome).append("\n");
        sb.append("Rendimento Total: ").append(nf.format(rendimentos)).append("\n");
        sb.append("Total de Deduções: ").append(nf.format(deducoes)).append("\n");
        sb.append("Base de Cálculo: ").append(nf.format(base)).append("\n");
        sb.append("Imposto Devido: ").append(nf.format(devido)).append("\n");
        sb.append("Imposto já Pago (IRRF): ").append(nf.format(pago)).append("\n\n");
        
        if (finalResult < 0) {
            sb.append("VALOR A RESTITUIR: ").append(nf.format(Math.abs(finalResult)));
        } else if (finalResult > 0) {
            sb.append("VALOR A PAGAR: ").append(nf.format(finalResult));
        } else {
            sb.append("SALDO ZERADO (Nada a pagar ou restituir)");
        }

        textResumoResultado.setText(sb.toString());
        cardUltimoResultado.setVisibility(View.VISIBLE);
        Toast.makeText(this, "Cálculo realizado com sucesso!", Toast.LENGTH_SHORT).show();
    }

    private void gerarRelatorioPDF() {
        try {
            String fileName = "Simulacao_IRPF_" + System.currentTimeMillis() + ".pdf";
            File filePath = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName);
            
            PdfWriter writer = new PdfWriter(new FileOutputStream(filePath));
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("CALCULAGOV - RECIBO DE SIMULAÇÃO DIGITAL").setBold().setFontSize(16));
            document.add(new Paragraph("Este documento substitui a necessidade de um contador para fins de consulta prévia."));
            document.add(new Paragraph("--------------------------------------------------"));
            document.add(new Paragraph(textResumoResultado.getText().toString()));
            document.add(new Paragraph("--------------------------------------------------"));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Data do Cálculo: " + new java.util.Date().toString()));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Atenção: Use este relatório para conferir sua declaração oficial na Receita Federal."));

            document.close();
            Toast.makeText(this, "Relatório PDF salvo com sucesso!", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Erro ao gerar PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
