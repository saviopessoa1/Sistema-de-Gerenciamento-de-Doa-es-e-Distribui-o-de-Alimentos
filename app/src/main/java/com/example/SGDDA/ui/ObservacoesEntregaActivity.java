package com.example.SGDDA.ui;

// --- IMPORTS NECESSÁRIOS ---
import android.os.Bundle;
import android.view.View; // Import do View
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity; // Import principal
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.SGDDA.R; // Import do R (recursos)
// --- FIM DOS IMPORTS ---

public class ObservacoesEntregaActivity extends AppCompatActivity {

    // 1. Declaração (FORA do onCreate)
    private ImageButton backButton;
    private TextView observacoesTextView;
    // Adicione os TextViews da instituição se quiser atualizá-los
    // private TextView titleInstituicao, textEntregue, textEndereco;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_observacoes_entrega);

        // Ajuste do padding para a status bar (EdgeToEdge)
        // O ID 'main' DEVE existir no seu XML (agora existe)
        View mainView = findViewById(R.id.main); // Encontrando o 'main'
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        // 2. Encontrar Componentes
        backButton = findViewById(R.id.backButton);
        observacoesTextView = findViewById(R.id.observacoesTextView);
        // titleInstituicao = findViewById(R.id.titleInstituicao);
        // textEntregue = findViewById(R.id.textEntregue);
        // textEndereco = findViewById(R.id.textEndereco);


        // 3. Configurar Listeners
        backButton.setOnClickListener(v -> {
            finish(); // Fecha esta tela e volta para os Detalhes
        });

        // 4. Carregar Dados
        // TODO: Receber os dados da Intent
        // String obs = getIntent().getStringExtra("OBSERVACOES");
        // String nomeInstituicao = getIntent().getStringExtra("NOME_INSTITUICAO");
        //
        // if (obs != null) {
        //    observacoesTextView.setText(obs);
        // }
        // if (nomeInstituicao != null) {
        //    titleInstituicao.setText(nomeInstituicao);
        // }
        // ... carregar resto dos dados
    }
}


