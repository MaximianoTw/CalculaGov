package com.example.calcgov;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import java.text.NumberFormat;
import java.util.Locale;

public class CalculoImpostoRendaActivity extends AppCompatActivity {

    private EditText editTextRenda;
    private EditText editTextDeducoes;
    private Button buttonCalcular;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculo_imposto_renda);

        editTextRenda = findViewById(R.id.editTextRenda);
        editTextDeducoes = findViewById(R.id.editTextDeducoes);
        buttonCalcular = findViewById(R.id.buttonCalcular);

        Button buttonVoltarHome = findViewById(R.id.VoltarHome);
        buttonVoltarHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Inicia a HomeActivity
                Intent intent = new Intent(CalculoImpostoRendaActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });


        buttonCalcular.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calcularImposto();
            }
        });
    }

    private void calcularImposto() {
        String rendaString = editTextRenda.getText().toString();
        String deducoesString = editTextDeducoes.getText().toString();

        if (rendaString.isEmpty() || deducoesString.isEmpty()) {
            Toast.makeText(this, "Por favor, preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }

        double rendaMensal = Double.parseDouble(rendaString);
        double deducoesMensais = Double.parseDouble(deducoesString);

        double rendaAnual = rendaMensal * 12;
        double deducoesAnuais = deducoesMensais * 12;

        double impostoAnual = calcularImpostoDeRenda(rendaAnual, deducoesAnuais);
        double impostoMensal = impostoAnual / 12;

        NumberFormat format = NumberFormat.getNumberInstance(new Locale("pt", "BR"));
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);

        String rendaMensalFormatada = format.format(rendaMensal);
        String rendaAnualFormatada = format.format(rendaAnual);
        String deducoesMensaisFormatadas = format.format(deducoesMensais);
        String deducoesAnuaisFormatadas = format.format(deducoesAnuais);
        String impostoMensalFormatado = format.format(impostoMensal);
        String impostoAnualFormatado = format.format(impostoAnual);

        exibirPopup("Resultado do Imposto de Renda", rendaMensalFormatada, rendaAnualFormatada,
                deducoesMensaisFormatadas, deducoesAnuaisFormatadas, impostoMensalFormatado, impostoAnualFormatado);
    }

    private double calcularImpostoDeRenda(double rendaAnual, double deducoesAnuais) {
        double rendaTributavel = rendaAnual - deducoesAnuais;
        if (rendaTributavel <= 22847.76) {
            return 0;
        } else if (rendaTributavel <= 33919.80) {
            return (rendaTributavel - 22847.76) * 0.075;
        } else if (rendaTributavel <= 45012.60) {
            return (rendaTributavel - 33919.80) * 0.15 + 1100.42;
        } else if (rendaTributavel <= 55976.16) {
            return (rendaTributavel - 45012.60) * 0.225 + 3141.62;
        } else {
            return (rendaTributavel - 55976.16) * 0.275 + 6881.25;
        }
    }

    private void exibirPopup(String titulo, String rendaMensal, String rendaAnual, String deducoesMensais,
                             String deducoesAnuais, String impostoMensal, String impostoAnual) {
        String mensagem =
                "Renda Mensal: R$ " + rendaMensal + "\n" +
                        "Renda Anual: R$ " + rendaAnual + "\n\n" +
                        "Deduções Mensais: R$ " + deducoesMensais + "\n" +
                        "Deduções Anuais: R$ " + deducoesAnuais + "\n\n" +
                        "Imposto a pagar (mensal): R$ " + impostoMensal + "\n" +
                        "Imposto a pagar (anual): R$ " + impostoAnual;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(titulo);
        builder.setMessage(mensagem);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }
}
