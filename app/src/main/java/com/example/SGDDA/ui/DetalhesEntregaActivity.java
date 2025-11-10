package com.example.SGDDA.ui;

import android.os.Bundle;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.example.SGDDA.R;
import com.google.android.material.button.MaterialButton;

public class DetalhesEntregaActivity extends AppCompatActivity {

    // Declaração dos componentes
    private ImageButton backButton;
    private MaterialButton btnLigarInstituicao, btnLigarVoluntario;
    private MaterialButton btnConfirmarColeta, btnConfirmarEntrega;
    private RecyclerView itensRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes_entrega);

        // Encontrar os componentes pelo ID
        backButton = findViewById(R.id.backButton);
        btnLigarInstituicao = findViewById(R.id.btnLigarInstituicao);
        btnLigarVoluntario = findViewById(R.id.btnLigarVoluntario);
        btnConfirmarColeta = findViewById(R.id.btnConfirmarColeta);
        btnConfirmarEntrega = findViewById(R.id.btnConfirmarEntrega);
        itensRecyclerView = findViewById(R.id.itensRecyclerView);

        // --- Configurar Listeners (Regras de Negócio) ---

        // 1. Botão de Voltar
        backButton.setOnClickListener(v -> {
            // Fecha a tela atual e volta para a anterior
            finish();
        });

        // 2. Lógica para os botões de ligar (requer permissão de TELEFONE)
        // TODO: Adicionar a lógica de Intent.ACTION_DIAL para os botões btnLigarInstituicao e btnLigarVoluntario

        // 3. Lógica para os botões de confirmação
        // TODO: Adicionar lógica para btnConfirmarColeta (ex: atualizar status no banco)
        // TODO: Adicionar lógica para btnConfirmarEntrega (ex: atualizar status no banco)

        // 4. Configurar o RecyclerView
        // TODO: Criar um Adapter (ex: DetalhesEntregaAdapter) para o itensRecyclerView
        // TODO: Carregar a lista de itens da entrega e passar para o adapter
        // ex: setupRecyclerView();
    }

    /*
    private void setupRecyclerView() {
        // 1. Crie seu Adapter (ex: DetalhesEntregaAdapter)
        // 2. Crie uma lista de itens (ex: List<Item>)
        // 3. Configure o layout manager
        itensRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        // 4. Set o adapter
        // itensRecyclerView.setAdapter(seuAdapter);
    }
    */
}