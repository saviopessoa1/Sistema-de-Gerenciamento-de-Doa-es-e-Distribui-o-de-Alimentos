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
import com.example.SGDDA.adapter.EntregaAdapter;
import com.example.SGDDA.model.Entrega;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays; // Import necessário para Arrays.asList
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

    // Estado atual do filtro (Pendente ou Concluída)
    private String filtroAtual = "Pendente";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_entregas);

        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        db = FirebaseFirestore.getInstance();

        menuButton = findViewById(R.id.menuButton);
        buttonPendentes = findViewById(R.id.buttonPendentes);
        buttonConcluidos = findViewById(R.id.buttonConcluidos);
        buttonMontarEntrega = findViewById(R.id.buttonMontarEntrega);
        recyclerViewEntregas = findViewById(R.id.recyclerViewEntregas);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        entregaList = new ArrayList<>();
        adapter = new EntregaAdapter(this, entregaList);
        recyclerViewEntregas.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewEntregas.setAdapter(adapter);

        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadEntregas(filtroAtual);
        bottomNavigationView.setSelectedItemId(R.id.nav_entregas);
    }

    private void setupListeners() {
        buttonMontarEntrega.setOnClickListener(v -> {
            Intent intent = new Intent(EntregasActivity.this, MontarEntregaActivity.class);
            startActivity(intent);
        });

        buttonPendentes.setOnClickListener(v -> {
            filtroAtual = "Pendente";
            loadEntregas(filtroAtual);
            updateFilterButtons(filtroAtual);
        });

        buttonConcluidos.setOnClickListener(v -> {
            filtroAtual = "Concluída";
            loadEntregas(filtroAtual);
            updateFilterButtons(filtroAtual);
        });

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
                return true;
            }
            return false;
        });
    }

    private void loadEntregas(String filtro) {
        Log.d(TAG, "Carregando entregas com filtro: " + filtro);

        Query query;

        if (filtro.equals("Pendente")) {
            // Se o filtro for "Pendente", queremos mostrar: "Pendente" E "Em Coleta"
            query = db.collection("entregas")
                    .whereIn("status", Arrays.asList("Pendente", "Em Coleta"));
        } else {
            // Se for "Concluída", mostramos apenas as concluídas
            query = db.collection("entregas")
                    .whereEqualTo("status", "Concluída");
        }

        // Removido orderBy temporariamente para evitar erro de índice composto com 'whereIn'
        query.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.w(TAG, "Listen failed.", error);
                Toast.makeText(this, "Erro ao carregar: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            entregaList.clear();
            if (value != null) {
                for (QueryDocumentSnapshot doc : value) {
                    try {
                        Entrega entrega = doc.toObject(Entrega.class);
                        entrega.setDocumentId(doc.getId());
                        entregaList.add(entrega);
                    } catch (Exception e) {
                        Log.e(TAG, "Erro converter doc: " + doc.getId(), e);
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void updateFilterButtons(String statusAtivo) {
        if (statusAtivo.equals("Pendente")) {
            buttonPendentes.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
            buttonPendentes.setTextColor(ContextCompat.getColor(this, R.color.app_accent_blue));

            buttonConcluidos.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.app_primary_light)));
            buttonConcluidos.setTextColor(ContextCompat.getColor(this, R.color.white_60));
        } else {
            buttonConcluidos.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
            buttonConcluidos.setTextColor(ContextCompat.getColor(this, R.color.app_accent_blue));

            buttonPendentes.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.app_primary_light)));
            buttonPendentes.setTextColor(ContextCompat.getColor(this, R.color.white_60));
        }
    }
}


