package com.example.SGDDA.ui;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.SGDDA.R;
import com.example.SGDDA.adapter.SelecaoEstoqueAdapter;
import com.example.SGDDA.model.DoacaoItem;
import com.example.SGDDA.model.Instituicao;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MontarEntregaActivity extends AppCompatActivity {

    private static final String TAG = "MontarEntregaActivity";

    // Componentes
    private ImageButton backButton;
    private RecyclerView itensEstoqueRecyclerView;
    private TextView textInstituicaoSelecionada;
    private Button btnProximo;

    // Botões de Seleção (Hardcoded do seu design)
    private Button btnSelecionarLar, btnSelecionarCreche, btnSelecionarSopao;
    private Map<String, Button> selectionButtons;

    // Firebase
    private FirebaseFirestore db;
    private SelecaoEstoqueAdapter adapterEstoque;
    private List<DoacaoItem> listaEstoque;
    private List<Instituicao> listaInstituicoes;

    // Controle
    private Instituicao instituicaoSelecionada = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_montar_entrega);

        // Ajuste de layout (EdgeToEdge)
        View mainView = findViewById(R.id.main); // Garanta que o ID "main" exista no XML
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();

        // Encontrar Componentes
        backButton = findViewById(R.id.backButton);
        itensEstoqueRecyclerView = findViewById(R.id.itensEstoqueRecyclerView);
        textInstituicaoSelecionada = findViewById(R.id.textInstituicaoSelecionada);
        btnProximo = findViewById(R.id.btnProximo);
        btnSelecionarLar = findViewById(R.id.btnSelecionarLar);
        btnSelecionarCreche = findViewById(R.id.btnSelecionarCreche);
        btnSelecionarSopao = findViewById(R.id.btnSelecionarSopao);

        // Mapeia os botões para facilitar o reset
        selectionButtons = new HashMap<>();
        selectionButtons.put("Lar dos Idosos", btnSelecionarLar);
        selectionButtons.put("Creche criança feliz", btnSelecionarCreche);
        selectionButtons.put("Sopão Comunitário", btnSelecionarSopao);

        // Configurar RecyclerView de Estoque
        listaEstoque = new ArrayList<>();
        adapterEstoque = new SelecaoEstoqueAdapter(this, listaEstoque);
        itensEstoqueRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        itensEstoqueRecyclerView.setAdapter(adapterEstoque);

        // Carregar Dados
        listaInstituicoes = new ArrayList<>();
        loadInstituicoes(); // Carrega as 3 instituições dos cards
        loadEstoque(); // Carrega o estoque

        // Configurar Cliques
        setupListeners();
    }

    private void loadInstituicoes() {
        // Carrega *todas* as instituições (embora o layout só tenha 3 botões)
        // O ideal seria carregar só as 3 prioritárias
        db.collection("instituicoes")
                .limit(3) // Limita aos 3 (só para popular a lista)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listaInstituicoes.clear();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Instituicao inst = doc.toObject(Instituicao.class);
                            inst.setDocumentId(doc.getId());
                            listaInstituicoes.add(inst);
                        }
                        Log.d(TAG, "Instituições prioritárias carregadas: " + listaInstituicoes.size());
                    } else {
                        Log.w(TAG, "Erro ao carregar instituições.", task.getException());
                    }
                });
    }

    private void loadEstoque() {
        // Carrega o estoque
        // CUMPRINDO O REQUISITO FIFO (5.1 do PDF)
        // Ordenamos pela data de validade, do mais antigo para o mais novo
        db.collection("estoque")
                .orderBy("dataValidade", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.w(TAG, "Listen failed.", error);
                        return;
                    }
                    listaEstoque.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            DoacaoItem item = doc.toObject(DoacaoItem.class);
                            item.setDocumentId(doc.getId());
                            listaEstoque.add(item);
                        }
                        adapterEstoque.notifyDataSetChanged();
                        Log.d(TAG, "Estoque (FIFO) carregado: " + listaEstoque.size() + " items.");
                    }
                });
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());

        // Listeners dos botões de seleção
        btnSelecionarLar.setOnClickListener(v -> selecionarInstituicao("Lar dos Idosos"));
        btnSelecionarCreche.setOnClickListener(v -> selecionarInstituicao("Creche criança feliz"));
        btnSelecionarSopao.setOnClickListener(v -> selecionarInstituicao("Sopão Comunitário"));

        // Botão PRÓXIMO
        btnProximo.setOnClickListener(v -> irParaResumo());
    }

    private void selecionarInstituicao(String nomeInstituicao) {
        // Reseta todos os botões para o estado "Selecionar" (verde)
        int greenColor = ContextCompat.getColor(this, R.color.app_accent_green);
        for (Button btn : selectionButtons.values()) {
            btn.setText("Selecionar");
            btn.setBackgroundTintList(ColorStateList.valueOf(greenColor));
        }

        // Encontra a instituição na lista carregada
        instituicaoSelecionada = null;
        for (Instituicao inst : listaInstituicoes) {
            if (inst.getNome().equalsIgnoreCase(nomeInstituicao)) {
                instituicaoSelecionada = inst;
                break;
            }
        }

        if (instituicaoSelecionada != null) {
            // Atualiza o botão clicado para "Selecionado" (cinza)
            Button selectedButton = selectionButtons.get(nomeInstituicao);
            if(selectedButton != null) {
                selectedButton.setText("Selecionado");
                int greyColor = ContextCompat.getColor(this, R.color.app_primary_light);
                selectedButton.setBackgroundTintList(ColorStateList.valueOf(greyColor));
            }
            // Atualiza o rodapé
            textInstituicaoSelecionada.setText(instituicaoSelecionada.getNome());
        } else {
            // Se não achou no DB (foi cadastrada manualmente errada)
            textInstituicaoSelecionada.setText(nomeInstituicao); // Finge que selecionou
            Log.w(TAG, "Instituição '" + nomeInstituicao + "' não encontrada no DB.");
        }
    }

    private void irParaResumo() {
        // 1. Pega os itens selecionados do adapter
        List<DoacaoItem> itensSelecionados = adapterEstoque.getSelectedItems();

        // 2. Validação
        if (instituicaoSelecionada == null) {
            Toast.makeText(this, "Selecione uma instituição.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (itensSelecionados.isEmpty()) {
            Toast.makeText(this, "Selecione pelo menos um item.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 3. Criar a Intent e passar os dados
        Intent intent = new Intent(this, ResumoAgendamentoActivity.class);

        // Passa a instituição (ela é Serializable)
        intent.putExtra("INSTITUICAO_SELECIONADA", instituicaoSelecionada);

        // Passa a lista de itens (ela é Serializable)
        intent.putExtra("ITENS_SELECIONADOS", (Serializable) itensSelecionados);

        startActivity(intent);
    }
}

