package com.example.SGDDA.ui;

// --- IMPORTS NECESSÁRIOS ---
import android.content.Intent;
import android.os.Bundle;
import android.view.View; // Import do View
import android.widget.Button;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity; // Import principal
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView; // Para a lista

import com.example.SGDDA.R; // Import do R (recursos)
// --- FIM DOS IMPORTS ---

public class DetalhesEntregaConcluidaActivity extends AppCompatActivity {

    // 1. Declaração dos Componentes (FORA do onCreate)
    private ImageButton backButton;
    private Button verObservacoesButton;
    private Button okButton;
    private RecyclerView itensRecyclerView;
    // Adicione outros componentes se necessário (ex: textVoluntarioNome)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detalhes_entrega_concluida);

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
        verObservacoesButton = findViewById(R.id.verObservacoesButton);
        okButton = findViewById(R.id.okButton);
        itensRecyclerView = findViewById(R.id.itensRecyclerView);

        // TODO: Configurar o RecyclerView (LayoutManager e Adapter)
        // (Ex: itensRecyclerView.setLayoutManager(new LinearLayoutManager(this));)


        // 3. Configurar Listeners
        backButton.setOnClickListener(v -> {
            finish(); // Volta para a tela anterior
        });

        okButton.setOnClickListener(v -> {
            finish(); // Também fecha a tela
        });

        verObservacoesButton.setOnClickListener(v -> {
            // Abre a tela de observações
            Intent intent = new Intent(DetalhesEntregaConcluidaActivity.this, ObservacoesEntregaActivity.class);
            // TODO: Passar as observações via intent.putExtra("OBSERVACOES", "texto das obs");
            startActivity(intent);
        });

        // TODO: Carregar os dados da entrega (instituição, voluntário, itens)
        // vindos da Intent da tela anterior.
    }
}


