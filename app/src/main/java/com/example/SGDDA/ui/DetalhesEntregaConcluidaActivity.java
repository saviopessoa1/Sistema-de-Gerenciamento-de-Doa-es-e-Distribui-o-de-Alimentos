package com.example.SGDDA.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.SGDDA.R;
import com.example.SGDDA.adapter.DetalhesItemAdapter;
import com.example.SGDDA.model.Entrega;

public class DetalhesEntregaConcluidaActivity extends AppCompatActivity {

    // Componentes
    private ImageButton backButton;
    private TextView textInstituicao, textEntregueData, textEndereco, textVoluntario;
    private TextView textObservacoes; // Campo de texto das observações (dentro do card ou direto)
    private Button btnLigarVoluntario, btnVerObservacoes, okButton;
    private RecyclerView itensRecyclerView;

    private Entrega entrega;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detalhes_entrega_concluida);

        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        // 1. Encontrar Componentes
        backButton = findViewById(R.id.backButton);
        textInstituicao = findViewById(R.id.textInstituicao);
        textEntregueData = findViewById(R.id.textEntregueData);
        textEndereco = findViewById(R.id.textEndereco);
        textVoluntario = findViewById(R.id.textVoluntario);

        // Tente encontrar o TextView de observações se ele estiver no layout principal
        // Se estiver em outra activity (como era o plano original "Ver Observações"), mantemos o botão.
        // Mas pelo seu print, parece que tem um campo "Observações" direto na tela.
        // Vou assumir que você quer mostrar direto na tela OU clicar no botão.

        btnLigarVoluntario = findViewById(R.id.btnLigarVoluntario);
        btnVerObservacoes = findViewById(R.id.btnVerObservacoes); // Se esse botão existir
        okButton = findViewById(R.id.okButton);
        itensRecyclerView = findViewById(R.id.itensRecyclerView);

        // 2. Configurar RecyclerView
        itensRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 3. Receber Dados
        if (getIntent().hasExtra("ENTREGA_OBJETO")) {
            entrega = (Entrega) getIntent().getSerializableExtra("ENTREGA_OBJETO");
            preencherDados();
        } else {
            Toast.makeText(this, "Erro ao carregar dados.", Toast.LENGTH_SHORT).show();
            finish();
        }

        // 4. Configurar Listeners
        setupListeners();
    }

    private void preencherDados() {
        if (entrega == null) return;

        textInstituicao.setText(entrega.getInstituicaoNome());
        textEntregueData.setText("Entregue: " + entrega.getDataEntrega());
        textEndereco.setText(entrega.getInstituicaoEndereco());
        textVoluntario.setText(entrega.getVoluntarioNome());

        // Preencher Lista de Itens
        if (entrega.getItens() != null) {
            DetalhesItemAdapter adapter = new DetalhesItemAdapter(this, entrega.getItens());
            itensRecyclerView.setAdapter(adapter);
        }
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());
        okButton.setOnClickListener(v -> finish());

        btnLigarVoluntario.setOnClickListener(v -> {
            Toast.makeText(this, "Ligando para " + entrega.getVoluntarioNome() + "...", Toast.LENGTH_SHORT).show();
        });

        if (btnVerObservacoes != null) {
            btnVerObservacoes.setOnClickListener(v -> {
                Intent intent = new Intent(this, ObservacoesEntregaActivity.class);
                // Passa o texto das observações. Se for null ou vazio, passa uma mensagem padrão.
                String obs = entrega.getObservacoes();
                if (TextUtils.isEmpty(obs)) {
                    obs = "Nenhuma observação registrada.";
                }
                intent.putExtra("OBSERVACOES", obs);
                intent.putExtra("NOME_INSTITUICAO", entrega.getInstituicaoNome());
                startActivity(intent);
            });
        }
    }
}


