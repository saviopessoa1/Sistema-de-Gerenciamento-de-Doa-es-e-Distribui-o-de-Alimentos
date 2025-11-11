package com.example.SGDDA.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.SGDDA.R;
import com.example.SGDDA.adapter.InstituicaoAdapter; // Import o novo adapter
import com.example.SGDDA.model.Instituicao; // Import o modelo
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup; // Import para os Chips
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class InstituicoesActivity extends AppCompatActivity {

    private static final String TAG = "InstituicoesActivity";

    private BottomNavigationView bottomNavigationView;
    private ImageButton menuButton;

    // Componentes da UI para filtro
    private RecyclerView recyclerViewInstituicoes;
    private EditText searchBar;
    private ChipGroup chipGroupFilters;
    private Chip chipAlta, chipMedia, chipNormal; // Adicionado

    // Firebase e Listas
    private FirebaseFirestore db;
    private InstituicaoAdapter adapter;
    private List<Instituicao> listaTodasInstituicoes;
    private List<Instituicao> listaFiltrada;

    private String filtroUrgenciaAtual = ""; // Armazena o filtro de urgência

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

        // Inicializar Firebase e Listas
        db = FirebaseFirestore.getInstance();
        listaTodasInstituicoes = new ArrayList<>();
        listaFiltrada = new ArrayList<>();

        // Encontrar Componentes
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        menuButton = findViewById(R.id.menuButton);
        recyclerViewInstituicoes = findViewById(R.id.recyclerViewInstituicoes);
        searchBar = findViewById(R.id.searchBar);
        chipGroupFilters = findViewById(R.id.chipGroupFilters);
        chipAlta = findViewById(R.id.chipAlta);
        chipMedia = findViewById(R.id.chipMedia);
        chipNormal = findViewById(R.id.chipNormal);


        // Configurar RecyclerView
        adapter = new InstituicaoAdapter(this, listaFiltrada);
        recyclerViewInstituicoes.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewInstituicoes.setAdapter(adapter);


        if (menuButton != null) {
            menuButton.setOnClickListener(v -> {
                startActivity(new Intent(this, DashboardActivity.class));
                finish();
            });
        }

        setupBottomNavigation();
        setupFiltrosListeners(); // Configura os listeners para busca e chips
        loadInstituicoes(); // Carrega os dados
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

    private void loadInstituicoes() {
        db.collection("instituicoes")
                .orderBy("urgencia", Query.Direction.DESCENDING) // Traz "Alta" primeiro
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    listaTodasInstituicoes.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Instituicao instituicao = doc.toObject(Instituicao.class);
                        instituicao.setDocumentId(doc.getId());
                        listaTodasInstituicoes.add(instituicao);
                    }
                    aplicarFiltros(); // Aplica filtros (inicialmente vazios)
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erro ao carregar instituições", e);
                    Toast.makeText(this, "Erro ao carregar dados.", Toast.LENGTH_SHORT).show();
                });
    }

    private void setupFiltrosListeners() {
        // Listener da Barra de Busca
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                aplicarFiltros(); // Filtra a cada letra digitada
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Listener dos Chips de Urgência
        chipGroupFilters.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipAlta) {
                filtroUrgenciaAtual = "Alta";
            } else if (checkedId == R.id.chipMedia) {
                filtroUrgenciaAtual = "Média";
            } else if (checkedId == R.id.chipNormal) {
                filtroUrgenciaAtual = "Normal";
            } else { // Nenhum chip selecionado
                filtroUrgenciaAtual = "";
            }
            aplicarFiltros();
        });

        // Lógica para desmarcar chips (se você clicar no que já está selecionado)
        View.OnClickListener chipClickListener = v -> {
            Chip chip = (Chip) v;
            if (!chip.isChecked()) {
                // Se o chip foi desmarcado (clicando nele de novo)
                chipGroupFilters.clearCheck();
                filtroUrgenciaAtual = "";
                aplicarFiltros();
            }
        };
        chipAlta.setOnClickListener(chipClickListener);
        chipMedia.setOnClickListener(chipClickListener);
        chipNormal.setOnClickListener(chipClickListener);
    }

    private void aplicarFiltros() {
        listaFiltrada.clear();
        String queryBusca = searchBar.getText().toString().toLowerCase(Locale.ROOT).trim();

        for (Instituicao inst : listaTodasInstituicoes) {
            boolean matchBusca = true;
            boolean matchUrgencia = true;

            // 1. Filtro de Busca (Nome)
            if (!queryBusca.isEmpty()) {
                matchBusca = inst.getNome().toLowerCase(Locale.ROOT).contains(queryBusca);
            }

            // 2. Filtro de Urgência
            if (!filtroUrgenciaAtual.isEmpty()) {
                matchUrgencia = inst.getUrgencia().equalsIgnoreCase(filtroUrgenciaAtual);
            }

            // Adiciona na lista se der match em ambos os filtros
            if (matchBusca && matchUrgencia) {
                listaFiltrada.add(inst);
            }
        }

        adapter.updateList(listaFiltrada); // Atualiza o RecyclerView
    }
}