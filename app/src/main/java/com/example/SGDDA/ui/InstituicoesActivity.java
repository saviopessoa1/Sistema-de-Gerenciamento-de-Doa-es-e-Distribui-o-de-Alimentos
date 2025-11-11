package com.example.SGDDA.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.SGDDA.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class InstituicoesActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private ImageButton menuButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_instituicoes);

        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        menuButton = findViewById(R.id.menuButton);

        if (menuButton != null) {
            menuButton.setOnClickListener(v -> {
                // Se quiser voltar ao painel
                startActivity(new Intent(this, DashboardActivity.class));
                finish();
            });
        }

        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.nav_instituicoes);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_painel) {
                startActivity(new Intent(this, DashboardActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_estoque) {
                startActivity(new Intent(this, PesquisarEstoqueActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_instituicoes) {
                return true; // Já estamos aqui
            } else if (itemId == R.id.nav_entregas) {
                startActivity(new Intent(this, EntregasActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }
}

