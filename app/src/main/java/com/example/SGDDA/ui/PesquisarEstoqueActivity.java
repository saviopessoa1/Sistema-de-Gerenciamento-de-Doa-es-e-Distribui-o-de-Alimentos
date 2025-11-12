package com.example.SGDDA.ui;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button; // Import para Button
import android.widget.EditText; // Import para EditText
import android.widget.ImageButton;
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
import com.example.SGDDA.adapter.EstoqueAdapter;
import com.example.SGDDA.model.DoacaoItem;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query; // Import para Query
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale; // Import para Locale

public class PesquisarEstoqueActivity extends AppCompatActivity {

    private static final String TAG = "PesquisarEstoque";

    private RecyclerView estoqueRecyclerView;
    private ImageButton backButton;
    private BottomNavigationView bottomNavigationView;

    // --- NOVOS COMPONENTES ---
    private EditText searchBar;
    private FloatingActionButton fabAdicionarDoacao;

    private Button btnPerecivel, btnNaoPerecivel;
    private Button btnFiltros; // Botão de filtros avançados (sem uso por enquanto)

    private FirebaseFirestore db;

    private EstoqueAdapter adapter;

    // --- NOVAS LISTAS E VARIÁVEIS DE FILTRO ---
    private List<DoacaoItem> listaEstoqueCompleta; // Guarda todos os itens
    private List<DoacaoItem> listaFiltrada;        // Lista mostrada no adapter
    private String filtroPerecivelAtual = "todos"; // "todos", "perecivel", "nao_perecivel"


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pesquisar_estoque);

        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        db = FirebaseFirestore.getInstance();

        // Encontrar componentes
        backButton = findViewById(R.id.backButton);
        estoqueRecyclerView = findViewById(R.id.estoqueRecyclerView);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        searchBar = findViewById(R.id.searchBar);
        btnPerecivel = findViewById(R.id.btnPerecivel);
        btnNaoPerecivel = findViewById(R.id.btnNaoPerecivel);
        fabAdicionarDoacao = findViewById(R.id.fabAdicionarDoacao);
        btnFiltros = findViewById(R.id.btnFiltros); // (Opcional)

        // Inicializar listas
        listaEstoqueCompleta = new ArrayList<>();
        listaFiltrada = new ArrayList<>();

        // Configurar Adapter
        adapter = new EstoqueAdapter(this, listaFiltrada); // Usa a lista filtrada
        estoqueRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        estoqueRecyclerView.setAdapter(adapter);

        // Configurar Listeners
        backButton.setOnClickListener(v -> finish());
        setupBottomNavigation();
        setupFiltrosListeners(); // NOVO
        loadEstoqueData();
        setupFab();

    }


    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.nav_estoque);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_painel) {
                startActivity(new Intent(this, DashboardActivity.class));
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out); // ADICIONADO
                finish();
                return true;
            } else if (itemId == R.id.nav_estoque) {
                return true;
            } else if (itemId == R.id.nav_instituicoes) {
                startActivity(new Intent(this, InstituicoesActivity.class));
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out); // ADICIONADO
                finish();
                return true;
            } else if (itemId == R.id.nav_entregas) {
                startActivity(new Intent(this, EntregasActivity.class));
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out); // ADICIONADO
                finish();
                return true;
            }
            return false;
        });
    }
    private void setupFab() {
        fabAdicionarDoacao.setOnClickListener(v -> {
            Intent intent = new Intent(PesquisarEstoqueActivity.this, RegistrarDoacaoActivity.class);
            startActivity(intent);
        });
    }

    private void loadEstoqueData() {
        db.collection("estoque")
                .orderBy("dataValidade", Query.Direction.ASCENDING) // Ordena por FIFO
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.w(TAG, "Listen failed.", error);
                        return;
                    }
                    listaEstoqueCompleta.clear(); // Limpa a lista principal
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            DoacaoItem item = doc.toObject(DoacaoItem.class);
                            listaEstoqueCompleta.add(item); // Adiciona na lista principal
                        }
                        aplicarFiltros(); // Aplica os filtros (mesmo que vazios)
                    }
                });
    }

    // --- LÓGICA DE FILTROS ---

    private void setupFiltrosListeners() {
        // Filtro da Barra de Busca
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                aplicarFiltros(); // Filtra a cada letra digitada
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Filtro Botão Perecível
        btnPerecivel.setOnClickListener(v -> {
            if (filtroPerecivelAtual.equals("perecivel")) {
                filtroPerecivelAtual = "todos"; // Desmarca
            } else {
                filtroPerecivelAtual = "perecivel"; // Marca
            }
            updateFilterButtons();
            aplicarFiltros();
        });

        // Filtro Botão Não Perecível
        btnNaoPerecivel.setOnClickListener(v -> {
            if (filtroPerecivelAtual.equals("nao_perecivel")) {
                filtroPerecivelAtual = "todos"; // Desmarca
            } else {
                filtroPerecivelAtual = "nao_perecivel"; // Marca
            }
            updateFilterButtons();
            aplicarFiltros();
        });
    }
    private void aplicarFiltros() {
        listaFiltrada.clear();
        String queryBusca = searchBar.getText().toString().toLowerCase(Locale.ROOT).trim();

        for (DoacaoItem item : listaEstoqueCompleta) {
            boolean matchBusca = true;
            boolean matchPerecivel = true;

            // 1. Filtro de Busca (Nome)
            if (!queryBusca.isEmpty()) {
                matchBusca = item.getNomeItem().toLowerCase(Locale.ROOT).contains(queryBusca);
            }

            // 2. Filtro de Tipo (Perecível)
            if (filtroPerecivelAtual.equals("perecivel")) {
                matchPerecivel = item.isPerecivel();
            } else if (filtroPerecivelAtual.equals("nao_perecivel")) {
                matchPerecivel = !item.isPerecivel();
            }

            // Adiciona na lista se der match em ambos os filtros
            if (matchBusca && matchPerecivel) {
                listaFiltrada.add(item);
            }
        }

        adapter.updateList(listaFiltrada); // Atualiza o RecyclerView
    }

    private void updateFilterButtons() {
        int activeColor = Color.WHITE;
        int activeTextColor = ContextCompat.getColor(this, R.color.app_accent_blue);
        int inactiveColor = ContextCompat.getColor(this, R.color.app_primary_light);
        int inactiveTextColor = Color.WHITE;

        // Lógica do Botão Perecível
        if (filtroPerecivelAtual.equals("perecivel")) {
            btnPerecivel.setBackgroundTintList(ColorStateList.valueOf(activeColor));
            btnPerecivel.setTextColor(activeTextColor);
        } else {
            btnPerecivel.setBackgroundTintList(ColorStateList.valueOf(inactiveColor));
            btnPerecivel.setTextColor(inactiveTextColor);
        }

        // Lógica do Botão Não Perecível
        if (filtroPerecivelAtual.equals("nao_perecivel")) {
            btnNaoPerecivel.setBackgroundTintList(ColorStateList.valueOf(activeColor));
            btnNaoPerecivel.setTextColor(activeTextColor);
        } else {
            btnNaoPerecivel.setBackgroundTintList(ColorStateList.valueOf(inactiveColor));
            btnNaoPerecivel.setTextColor(inactiveTextColor);
        }
    }
}