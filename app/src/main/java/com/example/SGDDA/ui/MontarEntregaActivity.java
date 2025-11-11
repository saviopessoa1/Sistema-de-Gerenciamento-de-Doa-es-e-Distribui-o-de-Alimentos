package com.example.SGDDA.ui;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
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

    private ImageButton backButton;
    private RecyclerView itensEstoqueRecyclerView;
    private TextView textInstituicaoSelecionada;
    private Button btnProximo;
    private EditText searchBar;
    private ScrollView institutionsContainer;

    private Button btnSelecionarLar, btnSelecionarCreche, btnSelecionarSopao;
    private Map<String, Button> selectionButtons;

    private FirebaseFirestore db;
    private SelecaoEstoqueAdapter adapterEstoque;

    private List<DoacaoItem> listaEstoqueCompleta;
    private List<DoacaoItem> listaParaAdapter;
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

        backButton = findViewById(R.id.backButton);
        itensEstoqueRecyclerView = findViewById(R.id.itensEstoqueRecyclerView);
        textInstituicaoSelecionada = findViewById(R.id.textInstituicaoSelecionada);
        btnProximo = findViewById(R.id.btnProximo);
        btnSelecionarLar = findViewById(R.id.btnSelecionarLar);
        btnSelecionarCreche = findViewById(R.id.btnSelecionarCreche);
        btnSelecionarSopao = findViewById(R.id.btnSelecionarSopao);
        searchBar = findViewById(R.id.searchBar);
        institutionsContainer = findViewById(R.id.institutionsContainer);

        selectionButtons = new HashMap<>();
        selectionButtons.put("Lar dos Idosos", btnSelecionarLar);
        selectionButtons.put("Creche criança feliz", btnSelecionarCreche);
        selectionButtons.put("Sopão Comunitário", btnSelecionarSopao);

        listaEstoqueCompleta = new ArrayList<>();
        listaParaAdapter = new ArrayList<>();

        adapterEstoque = new SelecaoEstoqueAdapter(this, listaParaAdapter, itemClicado -> {
            // Quando clica em um item da busca, limpa a busca e esconde teclado
            searchBar.setText("");
            searchBar.clearFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(searchBar.getWindowToken(), 0);
            }
            institutionsContainer.setVisibility(View.VISIBLE);
        });

        itensEstoqueRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        itensEstoqueRecyclerView.setAdapter(adapterEstoque);

        listaInstituicoes = new ArrayList<>();
        loadInstituicoes();
        loadEstoque();

        setupListeners();
        setupSearch();
    }

    private void setupSearch() {
        searchBar.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                institutionsContainer.setVisibility(View.GONE);
            } else if (searchBar.getText().toString().isEmpty()) {
                institutionsContainer.setVisibility(View.VISIBLE);
            }
        });

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                atualizarListaVisual(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void atualizarListaVisual(String query) {
        listaParaAdapter.clear();
        boolean isSearch = !query.isEmpty();

        if (isSearch) {
            String lowerQuery = query.toLowerCase();
            for (DoacaoItem item : listaEstoqueCompleta) {
                if (item.getNomeItem().toLowerCase().contains(lowerQuery)) {
                    listaParaAdapter.add(item);
                }
            }
        } else {
            Map<String, Integer> selecionados = adapterEstoque.getSelectedQuantitiesMap();
            for (DoacaoItem item : listaEstoqueCompleta) {
                if (selecionados.containsKey(item.getDocumentId()) && selecionados.get(item.getDocumentId()) > 0) {
                    listaParaAdapter.add(item);
                }
            }
            if (!searchBar.hasFocus()) {
                institutionsContainer.setVisibility(View.VISIBLE);
            }
        }
        adapterEstoque.updateList(listaParaAdapter, isSearch);
    }

    private void loadInstituicoes() {
        // Carrega TODAS as instituições para garantir que acharemos a certa
        db.collection("instituicoes").get().addOnCompleteListener(task -> {
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
        db.collection("estoque")
                .orderBy("dataValidade", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;

                    listaEstoqueCompleta.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            DoacaoItem item = doc.toObject(DoacaoItem.class);
                            item.setDocumentId(doc.getId());
                            listaEstoqueCompleta.add(item);
                        }
                        atualizarListaVisual(searchBar.getText().toString());
                    }
                });
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());
        btnSelecionarLar.setOnClickListener(v -> selecionarInstituicao("Lar dos Idosos", "Alta"));
        btnSelecionarCreche.setOnClickListener(v -> selecionarInstituicao("Creche criança feliz", "Média"));
        btnSelecionarSopao.setOnClickListener(v -> selecionarInstituicao("Sopão Comunitário", "Normal"));
        btnProximo.setOnClickListener(v -> irParaResumo());
    }

    // CORREÇÃO AQUI: Adicionado parâmetro 'urgencia' para fallback
    private void selecionarInstituicao(String nomeInstituicao, String urgenciaPadrao) {
        int greenColor = ContextCompat.getColor(this, R.color.app_accent_green);
        for (Button btn : selectionButtons.values()) {
            btn.setText("Selecionar");
            btn.setBackgroundTintList(ColorStateList.valueOf(greenColor));
        }

        instituicaoSelecionada = null;
        // Tenta achar no banco de dados
        for (Instituicao inst : listaInstituicoes) {
            // Verifica se o nome contém o que procuramos (mais flexível que equals exato)
            if (inst.getNome() != null && inst.getNome().toLowerCase().contains(nomeInstituicao.toLowerCase())) {
                instituicaoSelecionada = inst;
                break;
            }
        }

        // FALLBACK: Se não achou no DB (nomes diferentes ou db vazio), cria um temporário para não travar
        if (instituicaoSelecionada == null) {
            instituicaoSelecionada = new Instituicao();
            instituicaoSelecionada.setNome(nomeInstituicao);
            instituicaoSelecionada.setDocumentId("temp_" + System.currentTimeMillis()); // ID temporário
            instituicaoSelecionada.setEndereco("Endereço não cadastrado");
            instituicaoSelecionada.setUrgencia(urgenciaPadrao);
            instituicaoSelecionada.setResponsavel("Não informado");
            instituicaoSelecionada.setTelefone("");
        }

        // Atualiza visual dos botões
        Button selectedButton = selectionButtons.get(nomeInstituicao);
        if(selectedButton != null) {
            selectedButton.setText("Selecionado");
            int greyColor = ContextCompat.getColor(this, R.color.app_primary_light);
            selectedButton.setBackgroundTintList(ColorStateList.valueOf(greyColor));
        }

        textInstituicaoSelecionada.setText(instituicaoSelecionada.getNome());
    }

    private void irParaResumo() {
        List<DoacaoItem> itensSelecionadosParaEnvio = new ArrayList<>();
        Map<String, Integer> mapSelecionados = adapterEstoque.getSelectedQuantitiesMap();

        for (DoacaoItem itemOriginal : listaEstoqueCompleta) {
            String id = itemOriginal.getDocumentId();
            if (mapSelecionados.containsKey(id)) {
                int qtd = mapSelecionados.get(id);
                if (qtd > 0) {
                    DoacaoItem itemParaEnvio = new DoacaoItem(
                            itemOriginal.getNomeItem(),
                            qtd,
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