package com.example.SGDDA.ui;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import com.example.SGDDA.adapter.EntregaAdapter; // Importa o novo adapter
import com.example.SGDDA.model.Entrega; // Importa o novo modelo
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class EntregasActivity extends AppCompatActivity {

    private static final String TAG = "EntregasActivity";

    // Componentes
    private RecyclerView recyclerViewEntregas;
    private ImageButton menuButton;
    private Button buttonPendentes, buttonConcluidos, buttonMontarEntrega;
    private BottomNavigationView bottomNavigationView;

    // Firebase e Adapter
    private FirebaseFirestore db;
    private EntregaAdapter adapter;
    private List<Entrega> entregaList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_entregas);

        // Ajuste de layout (EdgeToEdge)
        // Precisamos adicionar o ID "main" no XML
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
        menuButton = findViewById(R.id.menuButton);
        buttonPendentes = findViewById(R.id.buttonPendentes);
        buttonConcluidos = findViewById(R.id.buttonConcluidos);
        buttonMontarEntrega = findViewById(R.id.buttonMontarEntrega);
        recyclerViewEntregas = findViewById(R.id.recyclerViewEntregas);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Configurar RecyclerView
        entregaList = new ArrayList<>();
        adapter = new EntregaAdapter(this, entregaList);
        recyclerViewEntregas.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewEntregas.setAdapter(adapter);

        // Configurar Listeners
        setupListeners();

        // Carregar dados iniciais (Pendentes)
        loadEntregas("Pendente");
        updateFilterButtons("Pendente");
    }

    private void setupListeners() {
        // Botão Montar Nova Entrega
        buttonMontarEntrega.setOnClickListener(v -> {
            Intent intent = new Intent(EntregasActivity.this, MontarEntregaActivity.class);
            startActivity(intent);
        });

        // Filtro Pendentes
        buttonPendentes.setOnClickListener(v -> {
            loadEntregas("Pendente");
            updateFilterButtons("Pendente");
        });

        // Filtro Concluídos
        buttonConcluidos.setOnClickListener(v -> {
            loadEntregas("Concluído");
            updateFilterButtons("Concluído");
        });

        // TODO: Adicionar listener para o menuButton (abrir o Drawer)

        // Navegação Inferior
        bottomNavigationView.setSelectedItemId(R.id.nav_entregas);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_painel) {
                startActivity(new Intent(this, DashboardActivity.class));
                return true;
            } else if (itemId == R.id.nav_estoque) {
                startActivity(new Intent(this, PesquisarEstoqueActivity.class));
                return true;
            } else if (itemId == R.id.nav_instituicoes) {
                startActivity(new Intent(this, InstituicoesActivity.class));
                return true;
            } else if (itemId == R.id.nav_entregas) {
                // Já estamos aqui
                return true;
            }
            return false;
        });
    }

    private void loadEntregas(String status) {
        Log.d(TAG, "Carregando entregas com status: " + status);
        db.collection("entregas")
                .whereEqualTo("status", status)
                .orderBy("dataAgendada", Query.Direction.ASCENDING) // Ordena pela data
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.w(TAG, "Listen failed.", error);
                        Toast.makeText(this, "Erro ao carregar entregas.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    entregaList.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            Entrega entrega = doc.toObject(Entrega.class);
                            entrega.setDocumentId(doc.getId()); // Guarda o ID
                            entregaList.add(entrega);
                        }
                        adapter.notifyDataSetChanged();
                        Log.d(TAG, "Entregas carregadas: " + entregaList.size());
                    } else {
                        Log.d(TAG, "Nenhum dado encontrado.");
                    }
                });
    }

    private void updateFilterButtons(String statusAtivo) {
        if (statusAtivo.equals("Pendente")) {
            // Ativa o botão Pendentes
            buttonPendentes.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
            buttonPendentes.setTextColor(ContextCompat.getColor(this, R.color.app_accent_blue));
            // Desativa o botão Concluídos
            buttonConcluidos.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.app_primary_light)));
            buttonConcluidos.setTextColor(ContextCompat.getColor(this, R.color.white_60));
        } else {
            // Ativa o botão Concluídos
            buttonConcluidos.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
            buttonConcluidos.setTextColor(ContextCompat.getColor(this, R.color.app_accent_blue));
            // Desativa o botão Pendentes
            buttonPendentes.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.app_primary_light)));
            buttonPendentes.setTextColor(ContextCompat.getColor(this, R.color.white_60));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Garante que o item correto esteja selecionado ao voltar para esta tela
        bottomNavigationView.setSelectedItemId(R.id.nav_entregas);
    }
}

