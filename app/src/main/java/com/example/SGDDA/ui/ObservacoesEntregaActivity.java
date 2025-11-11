package com.example.SGDDA.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.SGDDA.R;

public class ObservacoesEntregaActivity extends AppCompatActivity {

    private ImageButton backButton;
    private TextView observacoesTextView;
    private TextView titleInstituicao; // Adicionado para mostrar o nome

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_observacoes_entrega);

        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        // Encontrar Componentes
        backButton = findViewById(R.id.backButton);
        observacoesTextView = findViewById(R.id.observacoesTextView);
        titleInstituicao = findViewById(R.id.titleInstituicao);

        backButton.setOnClickListener(v -> finish());

        // Carregar Dados da Intent
        String obs = getIntent().getStringExtra("OBSERVACOES");
        String nomeInstituicao = getIntent().getStringExtra("NOME_INSTITUICAO");

        if (obs != null) {
            observacoesTextView.setText(obs);
        } else {
            observacoesTextView.setText("Nenhuma observação disponível.");
        }

        if (nomeInstituicao != null && titleInstituicao != null) {
            titleInstituicao.setText(nomeInstituicao);
        }
    }
}

