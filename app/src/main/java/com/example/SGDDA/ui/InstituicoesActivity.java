package com.example.SGDDA.ui;

import android.content.DialogInterface;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.SGDDA.R;
import com.example.SGDDA.adapter.InstituicaoAdapter;
import com.example.SGDDA.model.Instituicao;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class InstituicoesActivity extends AppCompatActivity implements InstituicaoAdapter.OnInstituicaoClickListener {

    private static final String TAG = "InstituicoesActivity";

    private BottomNavigationView bottomNavigationView;
    private ImageButton menuButton;

    // Componentes da UI para filtro
    private RecyclerView recyclerViewInstituicoes;
    private EditText searchBar;
    private ChipGroup chipGroupFilters;
    private Chip chipAlta, chipMedia, chipNormal;

    // Firebase e Listas
    private FirebaseFirestore db;
    private InstituicaoAdapter adapter;
    private List<Instituicao> listaTodasInstituicoes;
    private List<Instituicao> listaFiltrada;

    private String filtroUrgenciaAtual = "";

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
        // ★ CORREÇÃO AQUI: Adicionado o 4º parâmetro "Definir"
        adapter = new InstituicaoAdapter(this, listaFiltrada, this, "Definir");
        recyclerViewInstituicoes.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewInstituicoes.setAdapter(adapter);


        if (menuButton != null) {
            menuButton.setOnClickListener(v -> {
                startActivity(new Intent(this, DashboardActivity.class));
                finish();
            });
        }

        setupBottomNavigation();
        setupFiltrosListeners();
        loadInstituicoes();
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
                return true;
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
                .orderBy("nome", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Erro ao carregar instituições", error);
                        Toast.makeText(this, "Erro ao carregar dados.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    listaTodasInstituicoes.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            Instituicao instituicao = doc.toObject(Instituicao.class);
                            instituicao.setDocumentId(doc.getId());
                            listaTodasInstituicoes.add(instituicao);
                        }
                    }
                    aplicarFiltros();
                });
    }

    private void setupFiltrosListeners() {
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                aplicarFiltros();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        chipGroupFilters.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipAlta) {
                filtroUrgenciaAtual = "Alta";
            } else if (checkedId == R.id.chipMedia) {
                filtroUrgenciaAtual = "Média";
            } else if (checkedId == R.id.chipNormal) {
                filtroUrgenciaAtual = "Normal";
            } else {
                filtroUrgenciaAtual = "";
            }
            aplicarFiltros();
        });

        View.OnClickListener chipClickListener = v -> {
            Chip chip = (Chip) v;
            if (!chip.isChecked()) {
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

            if (!queryBusca.isEmpty()) {
                matchBusca = inst.getNome().toLowerCase(Locale.ROOT).contains(queryBusca);
            }

            if (!filtroUrgenciaAtual.isEmpty()) {
                String urgenciaInst = inst.getUrgencia() != null ? inst.getUrgencia() : "Normal";
                matchUrgencia = urgenciaInst.equalsIgnoreCase(filtroUrgenciaAtual);
            }

            if (matchBusca && matchUrgencia) {
                listaFiltrada.add(inst);
            }
        }

        adapter.updateList(listaFiltrada);
    }

    @Override
    public void onInstituicaoClick(Instituicao instituicao) {
        final String[] urgencias = {"Alta", "Média", "Normal"};
        String urgenciaAtual = instituicao.getUrgencia() != null ? instituicao.getUrgencia() : "Normal";

        int checkedItem = 2;
        if ("Alta".equals(urgenciaAtual)) checkedItem = 0;
        else if ("Média".equals(urgenciaAtual)) checkedItem = 1;

        new AlertDialog.Builder(this)
                .setTitle("Definir Urgência para:\n" + instituicao.getNome())
                .setSingleChoiceItems(urgencias, checkedItem, null)
                .setPositiveButton("Salvar", (dialog, which) -> {
                    int selectedPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                    String novaUrgencia = urgencias[selectedPosition];
                    salvarUrgencia(instituicao, novaUrgencia);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void salvarUrgencia(Instituicao instituicao, String novaUrgencia) {
        if (instituicao.getDocumentId() == null) {
            Toast.makeText(this, "Erro: ID da instituição não encontrado.", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("instituicoes").document(instituicao.getDocumentId())
                .update("urgencia", novaUrgencia)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Urgência atualizada!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao atualizar.", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Erro ao salvar urgência", e);
                });
    }
}


