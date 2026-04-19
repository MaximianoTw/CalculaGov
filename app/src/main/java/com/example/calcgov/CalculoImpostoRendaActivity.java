package com.example.calcgov;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.FileOutputStream;
import java.text.NumberFormat;
import java.util.Locale;

import com.google.android.material.tabs.TabLayout;

public class CalculoImpostoRendaActivity extends AppCompatActivity {

    private TextInputEditText editTextRendaMensal, editTextPrevidenciaMensal, editTextDepMensal,
            editTextRendaAnual, editTextPrevidenciaAnual, editTextDepAnual, editTextSaudeAnual, 
            editTextEduAnual, editTextPensaoAnual, editTextIRRFAnual;
    private TextView textResumoResultado, textResultadoSimplificado, textResultadoCompleto, textDicaEconomia;
    private View cardUltimoResultado, layoutMensal, layoutAnual, cardComparativo;
    private Button buttonCalcular, buttonDownloadPDF, buttonVoltar, btnAutoPreencherMensal;
    private TabLayout tabLayoutPeriodo;
    private SharedPreferences sharedPreferences, userPrefs;
    
    private Uri photoUri;
    private ActivityResultLauncher<Uri> takePictureLauncher;
    private ActivityResultLauncher<String> pickImageLauncher;
    private TextInputEditText currentTargetEditText;
    private List<String> evidencePaths = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculo_imposto_renda);

        sharedPreferences = getSharedPreferences("CalculosHistory", MODE_PRIVATE);
        userPrefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
        
        setupLaunchers();
        initViews();
        setupNavigation();
        setupBackConfirmation();
        setupMasks();
        
        buttonCalcular.setOnClickListener(v -> {
            boolean isAnual = tabLayoutPeriodo.getSelectedTabPosition() == 1;
            processarCalculo(isAnual);
        });
        
        buttonDownloadPDF.setOnClickListener(v -> gerarRelatorioPDF());
        buttonVoltar.setOnClickListener(v -> {
            if (temDadosPreenchidos()) {
                confirmarSaida(() -> finish());
            } else {
                finish();
            }
        });
        btnAutoPreencherMensal.setOnClickListener(v -> autoPreencher());
        
        setupEvidenciaButtons();
    }

    private void setupMasks() {
        applyMoneyMask(editTextRendaMensal);
        applyMoneyMask(editTextPrevidenciaMensal);
        applyMoneyMask(editTextRendaAnual);
        applyMoneyMask(editTextPrevidenciaAnual);
        applyMoneyMask(editTextSaudeAnual);
        applyMoneyMask(editTextEduAnual);
        applyMoneyMask(editTextPensaoAnual);
        applyMoneyMask(editTextIRRFAnual);
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

    private void setupLaunchers() {
        takePictureLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
            if (success) {
                processarImagemIA(photoUri);
            }
        });

        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                processarImagemIA(uri);
            }
        });
    }

    private void setupEvidenciaButtons() {
        findViewById(R.id.btnEvidenciaRendaMensal).setOnClickListener(v -> mostrarDialogoOrigem(editTextRendaMensal));
        findViewById(R.id.btnEvidenciaSaudeAnual).setOnClickListener(v -> mostrarDialogoOrigem(editTextSaudeAnual));
    }

    private void mostrarDialogoOrigem(TextInputEditText target) {
        currentTargetEditText = target;
        String[] options = {"Câmera (IA Scanner)", "Galeria"};
        new AlertDialog.Builder(this)
                .setTitle("Anexar Evidência")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) tirarFoto();
                    else abrirGaleria();
                })
                .show();
    }

    private void tirarFoto() {
        try {
            File photoFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "evidencia_" + System.currentTimeMillis() + ".jpg");
            photoUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", photoFile);
            evidencePaths.add(photoFile.getAbsolutePath());
            takePictureLauncher.launch(photoUri);
        } catch (Exception e) {
            Toast.makeText(this, "Erro ao abrir câmera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void abrirGaleria() {
        pickImageLauncher.launch("image/*");
    }

    private void processarImagemIA(Uri uri) {
        try {
            InputImage image = InputImage.fromFilePath(this, uri);
            TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

            Toast.makeText(this, "IA analisando documento...", Toast.LENGTH_SHORT).show();

            recognizer.process(image)
                    .addOnSuccessListener(visionText -> {
                        String detectedValue = extrairValorMonetario(visionText.getText());
                        if (detectedValue != null && currentTargetEditText != null) {
                            currentTargetEditText.setText(detectedValue);
                            Toast.makeText(this, "Valor extraído pela IA: R$ " + detectedValue, Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this, "Documento anexado. Não foi possível ler o valor automaticamente.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Erro na leitura da IA", Toast.LENGTH_SHORT).show();
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String extrairValorMonetario(String text) {
        // Regex para encontrar valores como 1.250,00 ou 500,00
        Pattern pattern = Pattern.compile("(\\d{1,3}(\\.\\d{3})*,\\d{2})");
        Matcher matcher = pattern.matcher(text);
        
        String maiorValor = null;
        double maxVal = -1;

        while (matcher.find()) {
            String valStr = matcher.group(1).replace(".", "").replace(",", ".");
            try {
                double currentVal = Double.parseDouble(valStr);
                if (currentVal > maxVal) {
                    maxVal = currentVal;
                    maiorValor = valStr;
                }
            } catch (Exception e) {}
        }
        return maiorValor;
    }

    private void setupBackConfirmation() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (temDadosPreenchidos()) {
                    confirmarSaida(() -> finish());
                } else {
                    finish();
                }
            }
        });
    }

    private boolean temDadosPreenchidos() {
        // Verifica campos mensais
        if (!editTextRendaMensal.getText().toString().isEmpty()) return true;
        if (!editTextPrevidenciaMensal.getText().toString().isEmpty()) return true;
        if (!editTextDepMensal.getText().toString().isEmpty()) return true;

        // Verifica campos anuais
        if (!editTextRendaAnual.getText().toString().isEmpty()) return true;
        if (!editTextPrevidenciaAnual.getText().toString().isEmpty()) return true;
        if (!editTextDepAnual.getText().toString().isEmpty()) return true;
        if (!editTextSaudeAnual.getText().toString().isEmpty()) return true;
        if (!editTextEduAnual.getText().toString().isEmpty()) return true;
        if (!editTextPensaoAnual.getText().toString().isEmpty()) return true;
        if (!editTextIRRFAnual.getText().toString().isEmpty()) return true;

        return false;
    }

    private void confirmarSaida(Runnable onConfirm) {
        new AlertDialog.Builder(this)
                .setTitle("Sair do preenchimento?")
                .setMessage("Se você mudar de página agora, os dados não serão salvos.")
                .setPositiveButton("SAIR", (dialog, which) -> onConfirm.run())
                .setNegativeButton("FICAR", null)
                .show();
    }

    private void setupNavigation() {
        BottomNavigationView nav = findViewById(R.id.bottomNavigation);
        nav.setSelectedItemId(R.id.nav_calculo);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Class<?> targetClass = null;
            if (id == R.id.nav_home) targetClass = HomeActivity.class;
            else if (id == R.id.nav_historico) targetClass = HistoricoActivity.class;
            else if (id == R.id.nav_perfil) targetClass = PerfilActivity.class;

            if (targetClass != null) {
                if (temDadosPreenchidos()) {
                    Class<?> finalTargetClass = targetClass;
                    confirmarSaida(() -> {
                        startActivity(new Intent(this, finalTargetClass));
                        finish();
                    });
                } else {
                    startActivity(new Intent(this, targetClass));
                    finish();
                }
                return true;
            }
            return id == R.id.nav_calculo;
        });
    }

    private void initViews() {
        tabLayoutPeriodo = findViewById(R.id.tabLayoutPeriodo);
        layoutMensal = findViewById(R.id.layoutMensal);
        layoutAnual = findViewById(R.id.layoutAnual);

        tabLayoutPeriodo.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    layoutMensal.setVisibility(View.VISIBLE);
                    layoutAnual.setVisibility(View.GONE);
                } else {
                    layoutMensal.setVisibility(View.GONE);
                    layoutAnual.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Campos Mensais
        editTextRendaMensal = findViewById(R.id.editTextRendaMensal);
        editTextPrevidenciaMensal = findViewById(R.id.editTextPrevidenciaMensal);
        editTextDepMensal = findViewById(R.id.editTextDepMensal);
        btnAutoPreencherMensal = findViewById(R.id.btnAutoPreencherMensal);

        // Campos Anuais
        editTextRendaAnual = findViewById(R.id.editTextRendaAnual);
        editTextPrevidenciaAnual = findViewById(R.id.editTextPrevidenciaAnual);
        editTextDepAnual = findViewById(R.id.editTextDepAnual);
        editTextSaudeAnual = findViewById(R.id.editTextSaudeAnual);
        editTextEduAnual = findViewById(R.id.editTextEduAnual);
        editTextPensaoAnual = findViewById(R.id.editTextPensaoAnual);
        editTextIRRFAnual = findViewById(R.id.editTextIRRFAnual);
        
        textResumoResultado = findViewById(R.id.textResumoResultado);
        cardUltimoResultado = findViewById(R.id.cardUltimoResultado);
        cardComparativo = findViewById(R.id.cardComparativo);
        textResultadoSimplificado = findViewById(R.id.textResultadoSimplificado);
        textResultadoCompleto = findViewById(R.id.textResultadoCompleto);
        textDicaEconomia = findViewById(R.id.textDicaEconomia);
        
        buttonCalcular = findViewById(R.id.buttonCalcular);
        buttonDownloadPDF = findViewById(R.id.buttonDownloadPDF);
        Button buttonSalvarCalculo = findViewById(R.id.buttonSalvarCalculo);
        buttonVoltar = findViewById(R.id.VoltarHome);

        buttonSalvarCalculo.setOnClickListener(v -> {
            String nome = userPrefs.getString(userPrefs.getString("logged_cpf", "") + "_name", "Cidadão");
            String resumo = textResumoResultado.getText().toString();
            if (!resumo.isEmpty()) {
                double finalResult = 0;
                if (resumo.contains("Restituição")) {
                    finalResult = -1;
                }
                salvarNoHistorico(nome, 0, finalResult);
                Toast.makeText(this, "Cálculo salvo no histórico!", Toast.LENGTH_SHORT).show();
                buttonSalvarCalculo.setEnabled(false);
                buttonSalvarCalculo.setAlpha(0.5f);
            }
        });

        buttonDownloadPDF.setOnClickListener(v -> gerarRelatorioPDF());
    }

    private void autoPreencher() {
        String loggedCpf = userPrefs.getString("logged_cpf", "");
        if (loggedCpf.isEmpty()) {
            Toast.makeText(this, "Erro: Usuário não identificado.", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = userPrefs.getString(loggedCpf + "_name", "");
        String renda = userPrefs.getString(loggedCpf + "_renda", "");
        String dependentes = userPrefs.getString(loggedCpf + "_dep", "");

        if (renda.isEmpty() && dependentes.isEmpty()) {
            Toast.makeText(this, "Seu perfil está incompleto. Vá em 'Perfil' para preencher.", Toast.LENGTH_LONG).show();
            return;
        }

        // Preenche campos mensais
        editTextRendaMensal.setText(renda);
        editTextDepMensal.setText(dependentes);

        // Preenche campos anuais (Simulando inteligência: mensal x 12)
        try {
            double rendaMensalVal = parseDouble(renda);
            editTextRendaAnual.setText(NumberFormat.getCurrencyInstance(new Locale("pt", "BR")).format(rendaMensalVal * 12));
            editTextDepAnual.setText(dependentes);
            
            double prevEstimada = rendaMensalVal * 0.11 * 12; // 11% médio
            editTextPrevidenciaAnual.setText(NumberFormat.getCurrencyInstance(new Locale("pt", "BR")).format(prevEstimada));
            
        } catch (Exception ignored) {}
        
        Toast.makeText(this, "Perfil gov.br carregado com sucesso!", Toast.LENGTH_SHORT).show();
    }

    private void processarCalculo(boolean isAnual) {
        double totalRendimentos, previdencia, saude, edu, pensao, irrfJaPago;
        int dependentes;
        String nomeContribuinte = userPrefs.getString(userPrefs.getString("logged_cpf", "") + "_name", "Cidadão");

        if (isAnual) {
            totalRendimentos = parseDouble(editTextRendaAnual.getText().toString());
            previdencia = parseDouble(editTextPrevidenciaAnual.getText().toString());
            dependentes = (int) parseDouble(editTextDepAnual.getText().toString());
            saude = parseDouble(editTextSaudeAnual.getText().toString());
            edu = parseDouble(editTextEduAnual.getText().toString());
            pensao = parseDouble(editTextPensaoAnual.getText().toString());
            irrfJaPago = parseDouble(editTextIRRFAnual.getText().toString());
        } else {
            totalRendimentos = parseDouble(editTextRendaMensal.getText().toString());
            previdencia = parseDouble(editTextPrevidenciaMensal.getText().toString());
            dependentes = (int) parseDouble(editTextDepMensal.getText().toString());
            saude = 0;
            edu = 0;
            pensao = 0;
            irrfJaPago = 0;
        }

        if (totalRendimentos <= 0) {
            Toast.makeText(this, "Informe o rendimento para calcular.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. DEDUÇÕES
        double valorPorDependente = isAnual ? 2275.08 : 189.59;
        double deducaoDependentes = dependentes * valorPorDependente;
        
        // Limite de educação no anual (aprox R$ 3.561,50)
        double eduEfetiva = isAnual ? Math.min(edu, 3561.50) : edu;
        
        double totalDeducoes = previdencia + saude + eduEfetiva + pensao + deducaoDependentes;

        // 2. BASE DE CÁLCULO
        double baseCalculo = totalRendimentos - totalDeducoes;
        if (baseCalculo < 0) baseCalculo = 0;

        // 3. CÁLCULO DO IMPOSTO DEVIDO (Tabela 2026)
        double impostoDevido = calcularIRPF(baseCalculo, isAnual);

        // 4. RESULTADO FINAL
        double resultadoFinal = isAnual ? (impostoDevido - irrfJaPago) : impostoDevido;

        if (isAnual) {
            calcularComparativo(totalRendimentos, totalDeducoes, irrfJaPago);
        } else {
            cardComparativo.setVisibility(View.GONE);
        }

        exibirResultado(nomeContribuinte, totalRendimentos, totalDeducoes, baseCalculo, impostoDevido, irrfJaPago, resultadoFinal, isAnual);
    }

    private void calcularComparativo(double totalRendimentos, double deducoesLegais, double irrfJaPago) {
        // Modelo Simplificado: Desconto padrão de 20% limitado a R$ 16.754,34
        double descontoSimplificado = Math.min(totalRendimentos * 0.20, 16754.34);
        double baseSimplificada = totalRendimentos - descontoSimplificado;
        double impostoSimplificado = calcularIRPF(baseSimplificada, true);
        double resultadoSimplificado = impostoSimplificado - irrfJaPago;

        // Modelo Completo (já calculado anteriormente como deducoesLegais)
        double impostoCompleto = calcularIRPF(totalRendimentos - deducoesLegais, true);
        double resultadoCompleto = impostoCompleto - irrfJaPago;

        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        textResultadoSimplificado.setText(nf.format(resultadoSimplificado));
        textResultadoCompleto.setText(nf.format(resultadoCompleto));

        cardComparativo.setVisibility(View.VISIBLE);

        if (resultadoCompleto < resultadoSimplificado) {
            double economia = resultadoSimplificado - resultadoCompleto;
            textDicaEconomia.setText("O Modelo Completo é melhor! Você economiza " + nf.format(economia));
            textDicaEconomia.setBackgroundColor(0xFFE7F3FF); // Azul suave
        } else {
            double economia = resultadoCompleto - resultadoSimplificado;
            textDicaEconomia.setText("O Modelo Simplificado é melhor! Você economiza " + nf.format(economia));
            textDicaEconomia.setBackgroundColor(0xFFE8F5E9); // Verde suave
        }
    }

    private double parseDouble(String val) {
        if (val == null || val.isEmpty()) return 0;
        try {
            // Remove R$, pontos de milhar e substitui vírgula decimal por ponto
            String clean = val.replaceAll("[R$\\.\\s\u00A0]", "").replace(",", ".");
            return Double.parseDouble(clean);
        } catch (Exception e) {
            return 0;
        }
    }

    private double calcularIRPF(double base, boolean isAnual) {
        if (isAnual) {
            if (base <= 24511.92) return 0;
            if (base <= 33919.80) return (base * 0.075) - 1838.39;
            if (base <= 45012.60) return (base * 0.15) - 4382.38;
            if (base <= 55976.16) return (base * 0.225) - 7758.32;
            return (base * 0.275) - 10557.13;
        } else {
            if (base <= 2259.20) return 0;
            if (base <= 2826.65) return (base * 0.075) - 169.44;
            if (base <= 3751.05) return (base * 0.15) - 381.44;
            if (base <= 4664.68) return (base * 0.225) - 662.77;
            return (base * 0.275) - 896.00;
        }
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

    private void exibirResultado(String nome, double rendimentos, double deducoes, double base, double devido, double pago, double finalResult, boolean isAnual) {
        salvarNoHistorico(nome, pago, finalResult);
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        
        StringBuilder sb = new StringBuilder();
        sb.append("Olá, ").append(nome).append("!\n\n");
        
        if (isAnual) {
            sb.append("Analisamos seus dados de todo o ano:\n");
            sb.append("• Ganhos totais: ").append(nf.format(rendimentos)).append("\n");
            sb.append("• Descontos aceitos: ").append(nf.format(deducoes)).append("\n");
            sb.append("• Valor base: ").append(nf.format(base)).append("\n\n");
            
            if (finalResult < 0) {
                sb.append("BOA NOTÍCIA! 🎉\n");
                sb.append("Você tem R$ ").append(nf.format(Math.abs(finalResult))).append(" para RECEBER de volta do governo.");
            } else if (finalResult > 0) {
                sb.append("AVISO: ✍️\n");
                sb.append("Você ainda precisa PAGAR R$ ").append(nf.format(finalResult)).append(" de imposto.");
            } else {
                sb.append("Tudo certo! Você não deve nada e também não tem valores a receber.");
            }
        } else {
            sb.append("Baseado no seu salário deste mês:\n");
            sb.append("• Seu imposto estimado é: ").append(nf.format(finalResult)).append("\n\n");
            sb.append("Dica: Se esse valor já foi descontado no seu contracheque, você está em dia!");
        }

        textResumoResultado.setText(sb.toString());
        cardUltimoResultado.setVisibility(View.VISIBLE);
        Toast.makeText(this, "Resultado pronto!", Toast.LENGTH_SHORT).show();
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
            
            if (!evidencePaths.isEmpty()) {
                document.add(new Paragraph("EVIDÊNCIAS DIGITAIS ANEXADAS:").setBold());
                for (String path : evidencePaths) {
                    try {
                        ImageData imageData = ImageDataFactory.create(path);
                        Image img = new Image(imageData);
                        img.setMaxWidth(300f);
                        document.add(new Paragraph("Documento: " + new File(path).getName()));
                        document.add(img);
                        document.add(new Paragraph(" "));
                    } catch (Exception e) {
                        document.add(new Paragraph("Erro ao carregar imagem: " + path));
                    }
                }
            }

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Data do Cálculo: " + new java.util.Date().toString()));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Atenção: Use este relatório para conferir sua declaração oficial na Receita Federal."));

            document.close();
            
            // Oferecer compartilhamento ou abertura do arquivo
            oferecerAcoesArquivo(filePath);
            
        } catch (Exception e) {
            Toast.makeText(this, "Erro ao gerar PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void oferecerAcoesArquivo(File file) {
        Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
        
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        
        Intent chooser = Intent.createChooser(intent, "Compartilhar ou Salvar Relatório");
        
        // Adicionar opção de abrir o arquivo diretamente
        Intent openIntent = new Intent(Intent.ACTION_VIEW);
        openIntent.setDataAndType(uri, "application/pdf");
        openIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        
        startActivity(chooser);
    }
}
