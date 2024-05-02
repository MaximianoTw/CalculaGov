package com.example.calcgov;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ImageButton irendaButton = findViewById(R.id.irenda);
        irendaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Inicia a CalculoImpostoRendaActivity
                Intent intent = new Intent(HomeActivity.this, CalculoImpostoRendaActivity.class);
                startActivity(intent);
            }
        });
    }
}

