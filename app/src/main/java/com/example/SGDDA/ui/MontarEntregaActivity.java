package com.example.SGDDA.ui;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher; // Importante para a busca
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
    private EditText searchBar; // Barra de busca

    private Button btnSelecionarLar, btnSelecionarCreche, btnSelecionarSopao;
    private Map<String, Button> selectionButtons;

    // Firebase e Dados
    private FirebaseFirestore db;
    private SelecaoEstoqueAdapter adapterEstoque;

    // Lista Mestra: Contém TODO o estoque vindo do banco (ordenado FIFO)
    private List<DoacaoItem> listaEstoqueCompleta;
    // Lista Exibida: O que está aparecendo na tela agora (filtrado)
    private List<DoacaoItem> listaExibida;

    private List<Instituicao> listaInstituicoes;
    private Instituicao instituicaoSelecionada = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_montar_entrega);

        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        db = FirebaseFirestore.getInstance();

        // Encontrar Componentes
        backButton = findViewById(R.id.backButton);
        itensEstoqueRecyclerView = findViewById(R.id.itensEstoqueRecyclerView);
        textInstituicaoSelecionada = findViewById(R.id.textInstituicaoSelecionada);
        btnProximo = findViewById(R.id.btnProximo);
        btnSelecionarLar = findViewById(R.id.btnSelecionarLar);
        btnSelecionarCreche = findViewById(R.id.btnSelecionarCreche);
        btnSelecionarSopao = findViewById(R.id.btnSelecionarSopao);
        searchBar = findViewById(R.id.searchBar);

        selectionButtons = new HashMap<>();
        selectionButtons.put("Lar dos Idosos", btnSelecionarLar);
        selectionButtons.put("Creche criança feliz", btnSelecionarCreche);
        selectionButtons.put("Sopão Comunitário", btnSelecionarSopao);

        // Inicializar Listas
        listaEstoqueCompleta = new ArrayList<>();
        listaExibida = new ArrayList<>();

        // Configurar Adapter
        adapterEstoque = new SelecaoEstoqueAdapter(this, listaExibida);
        itensEstoqueRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        itensEstoqueRecyclerView.setAdapter(adapterEstoque);

        // Carregar Dados
        listaInstituicoes = new ArrayList<>();
        loadInstituicoes();
        loadEstoque();

        setupListeners();
        setupSearch(); // Configura a busca
    }

    // Configura a lógica de busca e filtro
    private void setupSearch() {
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrarLista(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    // O coração da lógica da lista
    private void filtrarLista(String query) {
        listaExibida.clear();
        Map<String, Integer> selecionados = adapterEstoque.getSelectedQuantitiesMap();

        if (query.isEmpty()) {
            // SE A BUSCA ESTIVER VAZIA: Mostra apenas os itens selecionados ("Carrinho")
            for (DoacaoItem item : listaEstoqueCompleta) {
                if (selecionados.containsKey(item.getDocumentId()) && selecionados.get(item.getDocumentId()) > 0) {
                    listaExibida.add(item);
                }
            }
        } else {
            // SE TIVER TEXTO: Mostra itens do estoque que batem com a busca
            String lowerQuery = query.toLowerCase();
            for (DoacaoItem item : listaEstoqueCompleta) {
                if (item.getNomeItem().toLowerCase().contains(lowerQuery)) {
                    listaExibida.add(item);
                }
            }
        }
        // Atualiza o RecyclerView
        adapterEstoque.updateList(listaExibida);
    }

    private void loadInstituicoes() {
        db.collection("instituicoes").limit(3).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                listaInstituicoes.clear();
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    Instituicao inst = doc.toObject(Instituicao.class);
                    inst.setDocumentId(doc.getId());
                    listaInstituicoes.add(inst);
                }
            }
        });
    }

    private void loadEstoque() {
        // FIFO: Ordenado por validade (menor data primeiro)
        db.collection("estoque")
                .orderBy("dataValidade", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.w(TAG, "Listen failed.", error);
                        return;
                    }
                    listaEstoqueCompleta.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            DoacaoItem item = doc.toObject(DoacaoItem.class);
                            item.setDocumentId(doc.getId());
                            listaEstoqueCompleta.add(item);
                        }
                        // Ao carregar, atualiza a lista com base no estado atual da busca
                        filtrarLista(searchBar.getText().toString());
                        Log.d(TAG, "Estoque carregado: " + listaEstoqueCompleta.size());
                    }
                });
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());
        btnSelecionarLar.setOnClickListener(v -> selecionarInstituicao("Lar dos Idosos"));
        btnSelecionarCreche.setOnClickListener(v -> selecionarInstituicao("Creche criança feliz"));
        btnSelecionarSopao.setOnClickListener(v -> selecionarInstituicao("Sopão Comunitário"));
        btnProximo.setOnClickListener(v -> irParaResumo());
    }

    private void selecionarInstituicao(String nomeInstituicao) {
        int greenColor = ContextCompat.getColor(this, R.color.app_accent_green);
        for (Button btn : selectionButtons.values()) {
            btn.setText("Selecionar");
            btn.setBackgroundTintList(ColorStateList.valueOf(greenColor));
        }

        instituicaoSelecionada = null;
        for (Instituicao inst : listaInstituicoes) {
            if (inst.getNome().equalsIgnoreCase(nomeInstituicao)) {
                instituicaoSelecionada = inst;
                break;
            }
        }

        if (instituicaoSelecionada != null) {
            Button selectedButton = selectionButtons.get(nomeInstituicao);
            if(selectedButton != null) {
                selectedButton.setText("Selecionado");
                int greyColor = ContextCompat.getColor(this, R.color.app_primary_light);
                selectedButton.setBackgroundTintList(ColorStateList.valueOf(greyColor));
            }
            textInstituicaoSelecionada.setText(instituicaoSelecionada.getNome());
        } else {
            textInstituicaoSelecionada.setText(nomeInstituicao);
        }
    }

    private void irParaResumo() {
        // MONTAGEM DA LISTA FINAL PARA ENVIO
        // Aqui iteramos sobre a lista completa e pegamos apenas o que tem quantidade > 0 no mapa do adapter
        List<DoacaoItem> itensSelecionadosParaEnvio = new ArrayList<>();
        Map<String, Integer> mapSelecionados = adapterEstoque.getSelectedQuantitiesMap();

        for (DoacaoItem itemOriginal : listaEstoqueCompleta) {
            String id = itemOriginal.getDocumentId();
            if (mapSelecionados.containsKey(id)) {
                int qtd = mapSelecionados.get(id);
                if (qtd > 0) {
                    // Clona o item com a nova quantidade para enviar ao resumo
                    DoacaoItem itemParaEnvio = new DoacaoItem(
                            itemOriginal.getNomeItem(),
                            qtd, // Usa a quantidade selecionada
                            itemOriginal.isPerecivel(),
                            itemOriginal.getDataValidade(),
                            itemOriginal.getUidUsuario()
                    );
                    itemParaEnvio.setDocumentId(id);
                    itensSelecionadosParaEnvio.add(itemParaEnvio);
                }
            }
        }

        if (instituicaoSelecionada == null) {
            Toast.makeText(this, "Selecione uma instituição.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (itensSelecionadosParaEnvio.isEmpty()) {
            Toast.makeText(this, "Selecione pelo menos um item.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, ResumoAgendamentoActivity.class);
        intent.putExtra("INSTITUICAO_SELECIONADA", instituicaoSelecionada);
        intent.putExtra("ITENS_SELECIONADOS", (Serializable) itensSelecionadosParaEnvio);
        startActivity(intent);
    }
}